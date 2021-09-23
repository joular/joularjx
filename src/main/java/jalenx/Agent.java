/*
 * Copyright (c) 2021, Adel Noureddine, Université de Pays et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Author : Adel Noureddine
 */

package jalenx;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Agent {
    /**
     * Private constructor
     */
    private Agent() {
    }

    /**
     * Variables to collect the program energy consumption
     */
    private static double totalProcessEnergy = 0;

    /**
     * Process to run PowerJoular on
     */
    private static Process powerJoularProcess;

    /**
     * Map to store total energy for each method
     */
    private static Map<String, Double> methodsEnergy = new HashMap<>();

    /**
     * List of methods to filter for energy
     */
    private static String filterMethodName = "";

    /**
     * Map to store total energy for filtered methods
     */
    private static Map<String, Double> methodsEnergyFiltered = new HashMap<>();

    /**
     * JVM hook to statically load the java agent at startup.
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     */
    public static void premain(String args, Instrumentation inst) {
        Thread.currentThread().setName("JalenX Agent Thread");
        System.out.println("+---------------------------------+");
        System.out.println("| JalenX Agent Version 0.1        |");
        System.out.println("+---------------------------------+");

        ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
        // Check if CPU Time measurement is supported by the JVM. Quit otherwise
        if (!mxbean.isThreadCpuTimeSupported()) {
            System.out.println("Thread CPU Time is not supported on this Java Virtual Machine");
            System.exit(1);
        }

        // Enable CPU Time measurement if it is disabled
        if (!mxbean.isThreadCpuTimeEnabled())
            mxbean.setThreadCpuTimeEnabled(true);

        // Get Process ID of current application
        long appPid = ProcessHandle.current().pid();
        
		// Read properties file
		Properties prop = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("./config.properties");
			prop.load(fis);
		} catch (IOException e) {
			System.exit(1);
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Get filtered methods
        Agent.filterMethodName = prop.getProperty("filter-method-name");

		// Prepare and start PowerJoular to monitor the program's power consumption
		String powerJoularPath = prop.getProperty("powerjoular-path");
		String commandString = powerJoularPath + " -p " + appPid + " -o /tmp/joularJX-PowerJoular";
        System.out.println(commandString);
        try {
            powerJoularProcess = Runtime.getRuntime().exec(commandString);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        /**
         * Thread to calculate at runtime the power consumption per thread following a determined cycle duration
         */
        new Thread() {
            public void run() {
                Thread.currentThread().setName("JalenX Agent Computation");
                System.out.println("Started monitoring application with ID " + appPid);

                String csvFileName = "/tmp/joularJX-PowerJoular-" + appPid + ".csv";

                // CPU time for each thread
                Map<Long, Long> threadsCPUTime = new HashMap<>();

                while (true) {
                    try {
                        Map<Long, Map<String, Integer>> methodsStats = new HashMap<>();
                        Map<Long, Map<String, Integer>> methodsStatsFiltered = new HashMap<>();
                        Set<Thread> threads = Thread.getAllStackTraces().keySet();

                        int duration = 0;
                        while (duration < 1000) {
                            for (Thread t : threads) {
                                long threadID = t.getId();
                                if (! methodsStats.containsKey(t.getId())) {
                                    methodsStats.put(threadID, new HashMap<>());
                                }

                                if (! methodsStatsFiltered.containsKey(t.getId())) {
                                    methodsStatsFiltered.put(threadID, new HashMap<>());
                                }
                                
                                // Only check runnable threads (not waiting or blocked)
                                if (t.getState() == State.RUNNABLE) {
                                    int onlyFirst = 0;
                                    int onlyFirstFiltered = 0;
                                    for (StackTraceElement ste : t.getStackTrace()) {
                                        String methName = ste.getClassName() + "." + ste.getMethodName();
                                        if (onlyFirst == 0) {
                                            synchronized (GLOBALLOCK) {
                                                Map<String, Integer> methData = methodsStats.get(threadID);
                                                if (methData.containsKey(methName)) {
                                                    int methNumber = methData.get(methName) + 1;
                                                    methData.put(methName, methNumber);
                                                } else {
                                                    methData.put(methName, 1);
                                                }
                                            }
                                        }
                                        onlyFirst++;

                                        // Check filtered methods if in stacktrace
                                        if (methName.startsWith(Agent.filterMethodName)) {
                                            if (onlyFirstFiltered == 0) {
                                                synchronized (GLOBALLOCK) {
                                                    Map<String, Integer> methData = methodsStatsFiltered.get(threadID);
                                                    if (methData.containsKey(methName)) {
                                                        int methNumber = methData.get(methName) + 1;
                                                        methData.put(methName, methNumber);
                                                    } else {
                                                        methData.put(methName, 1);
                                                    }
                                                }
                                            }
                                            onlyFirstFiltered++;
                                        }
                                    }
                                }
                            }

                            duration += 10;
                            // Sleep for 10 ms
                            Thread.sleep(10);
                        }

                        double currentPower = Agent.getPowerFromCSVFile(csvFileName);

                        // Adds current power to total energy
                        // We can mix power and energy here because we collect power each second through PowerJoular
                        totalProcessEnergy += currentPower;

                        long totalCPUTime = 0;
                        for (Thread t : threads) {
                            long threadCPUTime = mxbean.getThreadCpuTime(t.getId());

                            // If thread already monitored, then calculate CPU time since last time
                            if (threadsCPUTime.containsKey(t.getId())) {
                                threadCPUTime -= threadsCPUTime.get(t.getId());
                            }

                            threadsCPUTime.put(t.getId(), threadCPUTime);
                            totalCPUTime += threadCPUTime;
                        }

                        Map<Long, Double> threadsPower = new HashMap<>();
                        for (Map.Entry<Long, Long> entry : threadsCPUTime.entrySet()) {
                            double percentageCPUTime = (entry.getValue() * 100.0) / totalCPUTime;
                            double threadPower = currentPower * (percentageCPUTime / 100.0);
                            threadsPower.put(entry.getKey(), threadPower);
                        }

                        // Now we have power for each thread, and stats for methods in each thread
                        // We allocated power for each method based on statistics
                        StringBuffer bufMeth = new StringBuffer();
                        for (Map.Entry<Long, Map<String, Integer>> entry : methodsStats.entrySet()) {
                            long threadID = entry.getKey();
                            for (Map.Entry<String, Integer> methEntry : entry.getValue().entrySet()) {
                                String methName = methEntry.getKey();
                                double methPower = threadsPower.get(threadID) * (methEntry.getValue() / 100.0);
                                if (methodsEnergy.containsKey(methEntry.getKey())) {
                                    // Add power (for 1 sec = energy) to total method energy
                                    double newMethEnergy = methodsEnergy.get(methName) + methPower;
                                    methodsEnergy.put(methName, newMethEnergy);
                                } else {
                                    methodsEnergy.put(methName, methPower);
                                }
                                bufMeth.append(methName + "," + methPower + "\n");
                            }
                        }

                        // For filtered methods
                        // Now we have power for each thread, and stats for methods in each thread
                        // We allocated power for each method based on statistics
                        StringBuffer bufMethFiltered = new StringBuffer();
                        for (Map.Entry<Long, Map<String, Integer>> entry : methodsStatsFiltered.entrySet()) {
                            long threadID = entry.getKey();
                            for (Map.Entry<String, Integer> methEntry : entry.getValue().entrySet()) {
                                String methName = methEntry.getKey();
                                double methPower = threadsPower.get(threadID) * (methEntry.getValue() / 100.0);
                                if (methodsEnergyFiltered.containsKey(methEntry.getKey())) {
                                    // Add power (for 1 sec = energy) to total method energy
                                    double newMethEnergy = methodsEnergyFiltered.get(methName) + methPower;
                                    methodsEnergyFiltered.put(methName, newMethEnergy);
                                } else {
                                    methodsEnergyFiltered.put(methName, methPower);
                                }
                                bufMethFiltered.append(methName + "," + methPower + "\n");
                            }
                        }

                        // Write to CSV file
                        String fileNameMethods = "joularJX-" + appPid + "-methods-power.csv";
                        try {
                            BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethods, false));
                            out.write(bufMeth.toString());
                            out.close();
                        } catch (Exception ee) {}

                        // Write to CSV file for filtered methods
                        String fileNameMethodsFiltered = "joularJX-" + appPid + "-methods-filtered-power.csv";
                        try {
                            BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethodsFiltered, false));
                            out.write(bufMethFiltered.toString());
                            out.close();
                        } catch (Exception ee) {}

                        // Sleep for 1 second
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        /**
         * Code to execute when exiting the program and the agent
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                powerJoularProcess.destroy();
                System.out.println("+---------------------------------+");
                System.out.println("JoularJX finished monitoring application with ID " + appPid);
                System.out.println("Program consumed " + String.format("%.2f", totalProcessEnergy) + " joules");

                // Prepare buffer for methods energy
                StringBuffer buf = new StringBuffer();
                for (Map.Entry<String, Double> entry : methodsEnergy.entrySet()) {
                    String key = entry.getKey();
                    Double value = entry.getValue();
                    buf.append(key + "," + value + "\n");
                }

                // Write to CSV file
                String fileNameMethods = "joularJX-" + appPid + "-methods-energy.csv";
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethods, true));
                    out.write(buf.toString());
                    out.close();
                } catch (Exception ee) {}

                // Prepare buffer for filtered methods energy
                StringBuffer bufFil = new StringBuffer();
                for (Map.Entry<String, Double> entry : methodsEnergyFiltered.entrySet()) {
                    String key = entry.getKey();
                    Double value = entry.getValue();
                    bufFil.append(key + "," + value + "\n");
                }

                // Write to CSV file for filtered methods
                String fileNameMethodsFiltered = "joularJX-" + appPid + "-methods-energy-filtered.csv";
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethodsFiltered, true));
                    out.write(bufFil.toString());
                    out.close();
                } catch (Exception ee) {}

                System.out.println("Energy consumption of methods and filtered methods written to " + fileNameMethods + " and " + fileNameMethodsFiltered + " files");
                System.out.println("+---------------------------------+");
            }
        });
    }

    /**
     * Global monitor used to implement mutual-exclusion. In the future this single
     * monitor may be broken up into many different monitors to reduce contention.
     */
    public static final Object GLOBALLOCK = new GlobalLock();

    public static class GlobalLock {
    }

    /**
     * Read power data from PowerJoular CSV file
     * @param fileName Path and name of PowerJoular power CSV file
     * @return Power consumption as reported by PowerJoular for the CPU
     */
    public static double getPowerFromCSVFile(String fileName) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            // Only read first line
            String line = br.readLine();
            if ((line != null) && (line.length() > 0)) {
                String[] values = line.split(",");
                br.close();
                // Line should have 3 values: date, CPU utilization and power
                // Example: 2021-04-28 15:40:45;0.08023;17.38672
                return Double.parseDouble(values[2]);
            }
            br.close();
            return 0;
        } catch (Exception e) {
            // First few times, CSV file isn't created yet
            // Also first time PowerJoular runs will generate a file with text Date, CPU Utilization, CPU Power
            // So, accurate values will be available after around 2-3 seconds
            // We return 0 in this case and in case any error reading the file or PowerJoular not installed
            return 0;
        }
    }
}
