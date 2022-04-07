/*
 * Copyright (c) 2022, Adel Noureddine, Université de Pays et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Author : Adel Noureddine
 */

package org.noureddine.joularjx;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.lang.management.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

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
     * RuntimeMXBean start time
     */
    private static Long runtimeStartTime;

    /**
     * RuntimeMXBean end time
     */
    private static Long runtimeEndTime;

    private static Double getRAPLEnergy() {
        String psys = "/sys/class/powercap/intel-rapl/intel-rapl:1/energy_uj";
        String pkg = "/sys/class/powercap/intel-rapl/intel-rapl:0/energy_uj";
        String dram = "/sys/class/powercap/intel-rapl/intel-rapl:0/intel-rapl:0:2/energy_uj";
        Double energyData = 0.0;

        File psysFile = new File(psys);
        if (psysFile.exists()) {
            // psys exists, so use this for energy readings
            Path psysPath = Path.of(psys);
            try {
                energyData = Double.parseDouble(Files.readString(psysPath));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // No psys supported, then check for pkg and dram
            File pkgFile = new File(pkg);
            if (pkgFile.exists()) {
                // pkg exists, check also for dram
                Path pkgPath = Path.of(pkg);
                try {
                    energyData = Double.parseDouble(Files.readString(pkgPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                File dramFile = new File(dram);
                if (dramFile.exists()) {
                    // dram and pkg exists, then get sum of both
                    Path dramPath = Path.of(dram);
                    try {
                        energyData += Double.parseDouble(Files.readString(dramPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Divide by 1 million to convert microJoules to Joules
        energyData = energyData / 1000000;
        return energyData;
    }

    /**
     * JVM hook to statically load the java agent at startup.
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     */
    public static void premain(String args, Instrumentation inst) {
        Thread.currentThread().setName("JalenX Agent Thread");
        System.out.println("+-------------------------------------------------+");
        System.out.println("| JoularJX (RAPL version) Agent Version 0.1       |");
        System.out.println("+-------------------------------------------------+");

        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        // Check if CPU Time measurement is supported by the JVM. Quit otherwise
        if (!threadMxBean.isThreadCpuTimeSupported()) {
            System.out.println("Thread CPU Time is not supported on this Java Virtual Machine");
            System.exit(1);
        }

        // Enable CPU Time measurement if it is disabled
        if (!threadMxBean.isThreadCpuTimeEnabled())
            threadMxBean.setThreadCpuTimeEnabled(true);

        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean osMxBean = ManagementFactory.getOperatingSystemMXBean();

        // Maps to store threads data
        Map<Long, Long> threadInitialCPU = new HashMap<Long, Long>();
        Map<Long, Float> threadCPUUsage = new HashMap<Long, Float>();
        Map<Long, String> threadNames = new HashMap<Long, String>();
        runtimeStartTime = runtimeMxBean.getUptime();

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

        /**
         * Thread to calculate at runtime the power consumption per thread following a determined cycle duration
         */
        new Thread() {
            public void run() {
                Thread.currentThread().setName("JoularJX (RAPL version) Agent Computation");
                System.out.println("Started monitoring application with ID " + appPid);

                try {
                    // 1st energy monitoring loop
                    while (true) {
                        // Get info about all threads, and their initial CPU time
                        ThreadInfo[] threadInfos = threadMxBean.dumpAllThreads(false, false);
                        for (ThreadInfo info : threadInfos) {
                            threadInitialCPU.put(info.getThreadId(), threadMxBean.getThreadCpuTime(info.getThreadId()));
                        }

                        // Get CPU energy consumption with Intel RAPL
                        // At the begining of the monitoring loop
                        Double RAPLBefore = getRAPLEnergy();

                        // Maps and sets for threads and methods statistics
                        Map<Long, Map<String, Integer>> methodsStats = new HashMap<>();
                        Map<Long, Map<String, Integer>> methodsStatsFiltered = new HashMap<>();
                        Set<Thread> threads = Thread.getAllStackTraces().keySet();

                        // 10ms monitoring loop for statistical sampling
                        // To get method name on top of stack trace
                        int duration = 0;
                        while (duration < 1000) {
                            for (Thread t : threads) {
                                long threadID = t.getId();

                                if (!methodsStats.containsKey(t.getId())) {
                                    methodsStats.put(threadID, new HashMap<>());
                                }

                                if (!methodsStatsFiltered.containsKey(t.getId())) {
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

                        // Get CPU energy consumption with Intel RAPL
                        // At the end of the monitoring loop
                        Double RAPLAfter = getRAPLEnergy();

                        // Calculate total energy consumed in the monitoring loop
                        Double RAPLEnergy = RAPLAfter - RAPLBefore;
                        totalProcessEnergy += RAPLEnergy;

                        runtimeEndTime = runtimeMxBean.getUptime();

                        // Get threads end CPU time
                        Map<Long, Long> threadCurrentCPU = new HashMap<Long, Long>();
                        threadInfos = threadMxBean.dumpAllThreads(false, false);
                        for (ThreadInfo info : threadInfos) {
                            threadCurrentCPU.put(info.getThreadId(), threadMxBean.getThreadCpuTime(info.getThreadId()));
                        }

                        // CPU over all processes
                        int nrCPUs = osMxBean.getAvailableProcessors();
                        // total CPU: CPU % can be more than 100% (devided over multiple cpus)
                        //long nrCPUs = 1;
                        // elapsedTime is in ms.
                        long elapsedTime = (runtimeEndTime - runtimeStartTime);
                        for (ThreadInfo info : threadInfos) {
                            // elapsedCpu is in ns
                            Long initialCPU = threadInitialCPU.get(info.getThreadId());
                            if (initialCPU != null) {
                                long elapsedCpu = threadCurrentCPU.get(info.getThreadId()) - initialCPU;
                                float cpuUsage = elapsedCpu / (elapsedTime * 1000000F * nrCPUs);
                                threadCPUUsage.put(info.getThreadId(), cpuUsage);
                                threadNames.put(info.getThreadId(), info.getThreadName());
                            }
                        }

                        Map<Long, Double> threadsEnergy = new HashMap<>();

                        // threadCPUUsage contains cpu % per thread

                        // Calculate energy consumed per thread
                        for (Map.Entry<Long, Float> entry : threadCPUUsage.entrySet()) {
                            Double EnergyPerThread = RAPLEnergy * entry.getValue();
                            threadsEnergy.put(entry.getKey(), EnergyPerThread);
                        }

                        // Now we have power (because we measure energy in one second loop) for each thread, and stats for methods in each thread
                        // We allocated power for each method based on statistics
                        StringBuffer bufMeth = new StringBuffer();
                        for (Map.Entry<Long, Map<String, Integer>> entry : methodsStats.entrySet()) {
                            long threadID = entry.getKey();
                            for (Map.Entry<String, Integer> methEntry : entry.getValue().entrySet()) {
                                try {
                                    String methName = methEntry.getKey();
                                    double methEnergy = threadsEnergy.get(threadID) * (methEntry.getValue() / 100.0);
                                    if (methodsEnergy.containsKey(methEntry.getKey())) {
                                        // Add power (for 1 sec = energy) to total method energy
                                        double newMethEnergy = methodsEnergy.get(methName) + methEnergy;
                                        methodsEnergy.put(methName, newMethEnergy);
                                    } else {
                                        methodsEnergy.put(methName, methEnergy);
                                    }
                                    bufMeth.append(methName + "," + methEnergy + "\n");
                                } catch (Exception e) {
                                }
                            }
                        }

                        // For filtered methods
                        // Now we have power for each thread, and stats for methods in each thread
                        // We allocated power for each method based on statistics
                        StringBuffer bufMethFiltered = new StringBuffer();
                        for (Map.Entry<Long, Map<String, Integer>> entry : methodsStatsFiltered.entrySet()) {
                            long threadID = entry.getKey();
                            for (Map.Entry<String, Integer> methEntry : entry.getValue().entrySet()) {
                                try {
                                    String methName = methEntry.getKey();
                                    double methEnergy = threadsEnergy.get(threadID) * (methEntry.getValue() / 100.0);
                                    if (methodsEnergyFiltered.containsKey(methEntry.getKey())) {
                                        // Add power (for 1 sec = energy) to total method energy
                                        double newMethEnergy = methodsEnergyFiltered.get(methName) + methEnergy;
                                        methodsEnergyFiltered.put(methName, newMethEnergy);
                                    } else {
                                        methodsEnergyFiltered.put(methName, methEnergy);
                                    }
                                    bufMethFiltered.append(methName + "," + methEnergy + "\n");
                                } catch (Exception e) {
                                }
                            }
                        }

                        // Write to CSV file
                        String fileNameMethods = "joularjx-" + appPid + "-methods-energy.csv";
                        try {
                            BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethods, false));
                            out.write(bufMeth.toString());
                            out.close();
                        } catch (Exception ee) {}

                        // Write to CSV file for filtered methods
                        String fileNameMethodsFiltered = "joularjx-" + appPid + "-methods-filtered-energy.csv";
                        try {
                            BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethodsFiltered, false));
                            out.write(bufMethFiltered.toString());
                            out.close();
                        } catch (Exception ee) {}

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }.start();

        /**
         * Code to execute when exiting the program and the agent
         */
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("+---------------------------------+");
                System.out.println("JoularJX (RAPL version) finished monitoring application with ID " + appPid);
                //System.out.println("Program consumed " + String.format("%.2f", totalProcessEnergy) + " joules");
                System.out.println("Program consumed " + totalProcessEnergy + " joules");

                // Prepare buffer for methods energy
                StringBuffer buf = new StringBuffer();
                for (Map.Entry<String, Double> entry : methodsEnergy.entrySet()) {
                    String key = entry.getKey();
                    Double value = entry.getValue();
                    buf.append(key + "," + value + "\n");
                }

                // Write to CSV file
                String fileNameMethods = "joularjx-" + appPid + "-methods-energy-total.csv";
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
                String fileNameMethodsFiltered = "joularjx-" + appPid + "-methods-energy-filtered-total.csv";
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
}