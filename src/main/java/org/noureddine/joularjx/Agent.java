/*
 * Copyright (c) 2021-2022, Adel Noureddine, Université de Pays et des Pays de l'Adour.
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
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import com.sun.management.OperatingSystemMXBean;
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
    private static List<String> filterMethodNames = new ArrayList<String>();

    /**
     * Size of list containing methods to filter for energy
     */
    private static int sizeFilterMethodNames = 0;

    /**
     * Map to store total energy for filtered methods
     */
    private static Map<String, Double> methodsEnergyFiltered = new HashMap<>();

    /**
     * Sensor to use for monitor CPU energy/power consumption
     */
    private static String energySensor = "";

    /**
     * Raspberry Pi model name
     */
    private static String raspberryPiModel = "";

    /**
     * Path for our power monitor program on Windows
     */
    private static String powerMonitorPathWindows = "";

    /**
     * Process to run power monitor on Windows
     */
    private static Process powerMonitorWindowsProcess;

    /**
     * Check if methodName starts with one of the filtered method names
     * @param methodName Name of method
     * @return True if methodName starts with one of the filtered method names, false if not
     */
    private static boolean isStartsFilterMethodNames(String methodName) {
        // In most cases, there will be one filtered method name
        // So we check that to gain performance and avoid looping the list
        if (Agent.sizeFilterMethodNames == 1) {
            return methodName.startsWith(Agent.filterMethodNames.get(0));
        } else {
            // Check for every filtered method name if methodName start with any of them
            for (String filterMethod : Agent.filterMethodNames) {
                if (methodName.startsWith(filterMethod)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get energy readings from RAPL through powercap
     * Calculates the best energy reading as supported by CPU (psys, or pkg+dram, or pkg)
     * @return Energy readings from RAPL
     */
    private static Double getRAPLEnergy() {
        String psys = "/sys/class/powercap/intel-rapl/intel-rapl:1/energy_uj";
        String pkg = "/sys/class/powercap/intel-rapl/intel-rapl:0/energy_uj";
        String dram = "/sys/class/powercap/intel-rapl/intel-rapl:0/intel-rapl:0:2/energy_uj";
        Double energyData = 0.0;

        try {
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
        } catch (Exception e) {
            System.out.println("Failed to get RAPL energy readings. Did you run JoularJX with elevated privileges (sudo)?");
            System.exit(1);
        }

        // Divide by 1 million to convert microJoules to Joules
        energyData = energyData / 1000000;
        return energyData;
    }

    /**
     * Calculate process energy consumption
     * @param totalCPUUsage Total CPU usage
     * @param processCPUUSage Process CPU usage
     * @param CPUEnergy CPU energy
     * @return Process energy consumption
     */
    private static double calculateProcessCPUEnergy(Double totalCPUUsage, Double processCPUUSage, Double CPUEnergy) {
        return (processCPUUSage * CPUEnergy) / totalCPUUsage;
    }

    /**
     * Calculate CPU energy consumption for last second (power) on supported Raspberry Pi devices
     * @param rpiModel Raspberry Pi model name
     * @param CPUUsage CPU usage
     * @return CPU energy consumption for last second (power)
     */
    private static double calculateCPUEnergyForRaspberryPi(String rpiModel, double CPUUsage) {
        double result = 0.0;

        switch (rpiModel) {
            case "rbp4001.0-64":
                result = 2.6630056198236938 + (0.82814554 * CPUUsage) +
                        (-112.17687631 * Math.pow(CPUUsage, 2)) +
                        (1753.99173239 * Math.pow(CPUUsage, 3)) +
                        (-10992.65341181 * Math.pow(CPUUsage, 4)) +
                        (35988.45610911 * Math.pow(CPUUsage, 5)) +
                        (-66254.20051068 * Math.pow(CPUUsage, 6)) +
                        (69071.21138567 * Math.pow(CPUUsage, 7)) +
                        (-38089.87171735 * Math.pow(CPUUsage, 8)) +
                        (8638.45610698 * Math.pow(CPUUsage, 9));
                break;
            case "rbp4b1.2-64":
                result = 3.039940056604439 + (-3.074225 * CPUUsage) +
                        (47.753114 * Math.pow(CPUUsage, 2)) +
                        (-271.974551 * Math.pow(CPUUsage, 3)) +
                        (879.966571 * Math.pow(CPUUsage, 4)) +
                        (-1437.466442 * Math.pow(CPUUsage, 5)) +
                        (1133.325791 * Math.pow(CPUUsage, 6)) +
                        (-345.134888 * Math.pow(CPUUsage, 7));
                break;
            case "rbp4b1.2":
                result = 2.58542069543335 + (12.335449 * CPUUsage) +
                        (-248.010554 * Math.pow(CPUUsage, 2)) +
                        (2379.832320 * Math.pow(CPUUsage, 3)) +
                        (-11962.419149 * Math.pow(CPUUsage, 4)) +
                        (34444.268647 * Math.pow(CPUUsage, 5)) +
                        (-58455.266502 * Math.pow(CPUUsage, 6)) +
                        (57698.685016 * Math.pow(CPUUsage, 7)) +
                        (-30618.557703 * Math.pow(CPUUsage, 8)) +
                        (6752.265368 * Math.pow(CPUUsage, 9));
                break;
            case "rbp4b1.1-64":
                result = 3.405685008777926 + (-11.834416 * CPUUsage) +
                        (137.312822 * Math.pow(CPUUsage, 2)) +
                        (-775.891511 * Math.pow(CPUUsage, 3)) +
                        (2563.399671 * Math.pow(CPUUsage, 4)) +
                        (-4783.024354 * Math.pow(CPUUsage, 5)) +
                        (4974.960753 * Math.pow(CPUUsage, 6)) +
                        (-2691.923074 * Math.pow(CPUUsage, 7)) +
                        (590.355251 * Math.pow(CPUUsage, 8));
                break;
            case "rbp4b1.1":
                result = 2.5718068562852086 + (2.794871 * CPUUsage) +
                        (-58.954883 * Math.pow(CPUUsage, 2)) +
                        (838.875781 * Math.pow(CPUUsage, 3)) +
                        (-5371.428686 * Math.pow(CPUUsage, 4)) +
                        (18168.842874 * Math.pow(CPUUsage, 5)) +
                        (-34369.583554 * Math.pow(CPUUsage, 6)) +
                        (36585.681749 * Math.pow(CPUUsage, 7)) +
                        (-20501.307640 * Math.pow(CPUUsage, 8)) +
                        (4708.331490 * Math.pow(CPUUsage, 9));
                break;
            case "rbp3b+1.3":
                result = 2.484396997449118 + (2.933542 * CPUUsage) +
                        (-150.400134 * Math.pow(CPUUsage, 2)) +
                        (2278.690310 * Math.pow(CPUUsage, 3)) +
                        (-15008.559279 * Math.pow(CPUUsage, 4)) +
                        (51537.315529 * Math.pow(CPUUsage, 5)) +
                        (-98756.887779 * Math.pow(CPUUsage, 6)) +
                        (106478.929766 * Math.pow(CPUUsage, 7)) +
                        (-60432.910139 * Math.pow(CPUUsage, 8)) +
                        (14053.677709 * Math.pow(CPUUsage, 9));
                break;
            case "rbp3b1.2":
                result = 1.524116907651687 + (10.053851 * CPUUsage) +
                        (-234.186930 * Math.pow(CPUUsage, 2)) +
                        (2516.322119 * Math.pow(CPUUsage, 3)) +
                        (-13733.555536 * Math.pow(CPUUsage, 4)) +
                        (41739.918887 * Math.pow(CPUUsage, 5)) +
                        (-73342.794259 * Math.pow(CPUUsage, 6)) +
                        (74062.644914 * Math.pow(CPUUsage, 7)) +
                        (-39909.425362 * Math.pow(CPUUsage, 8)) +
                        (8894.110508 * Math.pow(CPUUsage, 9));
                break;
            case "rbp2b1.1":
                result = 1.3596870187778196 + (5.135090 * CPUUsage) +
                        (-103.296366 * Math.pow(CPUUsage, 2)) +
                        (1027.169748 * Math.pow(CPUUsage, 3)) +
                        (-5323.639404 * Math.pow(CPUUsage, 4)) +
                        (15592.036875 * Math.pow(CPUUsage, 5)) +
                        (-26675.601585 * Math.pow(CPUUsage, 6)) +
                        (26412.963366 * Math.pow(CPUUsage, 7)) +
                        (-14023.471809 * Math.pow(CPUUsage, 8)) +
                        (3089.786200 * Math.pow(CPUUsage, 9));
                break;
            case "rbp1b+1.2":
                result = 1.2513999338064061 + (1.857815 * CPUUsage) +
                        (-18.109537 * Math.pow(CPUUsage, 2)) +
                        (101.531231 * Math.pow(CPUUsage, 3)) +
                        (-346.386617 * Math.pow(CPUUsage, 4)) +
                        (749.560352 * Math.pow(CPUUsage, 5)) +
                        (-1028.802514 * Math.pow(CPUUsage, 6)) +
                        (863.877618 * Math.pow(CPUUsage, 7)) +
                        (-403.270951 * Math.pow(CPUUsage, 8)) +
                        (79.925932 * Math.pow(CPUUsage, 9));
                break;
            case "rbp1b2":
                result = 2.826093843916506 + (3.539891 * CPUUsage) +
                        (-43.586963 * Math.pow(CPUUsage, 2)) +
                        (282.488560 * Math.pow(CPUUsage, 3)) +
                        (-1074.116844 * Math.pow(CPUUsage, 4)) +
                        (2537.679443 * Math.pow(CPUUsage, 5)) +
                        (-3761.784242 * Math.pow(CPUUsage, 6)) +
                        (3391.045904 * Math.pow(CPUUsage, 7)) +
                        (-1692.840870 * Math.pow(CPUUsage, 8)) +
                        (357.800968 * Math.pow(CPUUsage, 9));
                break;
            case "rbpzw1.1":
                result = 0.8551610676717238 + (7.207151 * CPUUsage) +
                        (-135.517893 * Math.pow(CPUUsage, 2)) +
                        (1254.808001 * Math.pow(CPUUsage, 3)) +
                        (-6329.450524 * Math.pow(CPUUsage, 4)) +
                        (18502.371291 * Math.pow(CPUUsage, 5)) +
                        (-32098.028941 * Math.pow(CPUUsage, 6)) +
                        (32554.679890 * Math.pow(CPUUsage, 7)) +
                        (-17824.350159 * Math.pow(CPUUsage, 8)) +
                        (4069.178175 * Math.pow(CPUUsage, 9));
                break;
            default:
                break;
        }

        return result;
    }

    /**
     * Get model name of Raspberry Pi
     * @param osArch OS Architecture (arm, aarch64)
     * @return Raspberry Pi model name
     */
    private static String getRPiModelName(String osArch) {
        String deviceTreeModel = "/proc/device-tree/model";
        File deviceTreeModelFile = new File(deviceTreeModel);
        String result = "";

        if (deviceTreeModelFile.exists()) {
            Path procstatPath = Path.of(deviceTreeModel);
            try {
                // Read only first line of stat file
                // We need to read values at index 1, 2, 3 and 4 (assuming index starts at 0)
                // Example of line: cpu  83141 56 28074 2909632 3452 10196 3416 0 0 0
                // Split the first line over spaces to get each column
                List<String> allLines = Files.readAllLines(procstatPath);
                for (String currentLine : allLines) {
                    if (currentLine.contains("Raspberry Pi 400 Rev 1.0")) {
                        if (osArch.contains("aarch64")) {
                            return "rbp4001.0-64";
                        }
                    }
                    if (currentLine.contains("Raspberry Pi 4 Model B Rev 1.2")) {
                        if (osArch.contains("aarch64")) {
                            return "rbp4b1.2-64";
                        } else {
                            return "rbp4b1.2";
                        }
                    } else if (currentLine.contains("Raspberry Pi 4 Model B Rev 1.1")) {
                        if (osArch.contains("aarch64")) {
                            return "rbp4b1.1-64";
                        } else {
                            return "rbp4b1.1";
                        }
                    } else if (currentLine.contains("Raspberry Pi 3 Model B Plus Rev 1.3")) {
                        return "rbp3b+1.3";
                    } else if (currentLine.contains("Raspberry Pi 3 Model B Rev 1.2")) {
                        return "rbp3b1.2";
                    } else if (currentLine.contains("Raspberry Pi 2 Model B Rev 1.1")) {
                        return "rbp2b1.1";
                    } else if (currentLine.contains("Raspberry Pi Model B Plus Rev 1.2")) {
                        return "rbp1b+1.2";
                    } else if (currentLine.contains("Raspberry Pi Model B Rev 2")) {
                        return "rbp1b2";
                    } else if (currentLine.contains("Raspberry Pi Zero W Rev 1.1")) {
                        return "rbpzw1.1";
                    }
                }
            } catch (IOException ignored) {}
        }

        return result;
    }

    /**
     * JVM hook to statically load the java agent at startup.
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     */
    public static void premain(String args, Instrumentation inst) {
        Thread.currentThread().setName("JoularJX Agent Thread");
        System.out.println("+---------------------------------+");
        System.out.println("| JoularJX Agent Version 1.1      |");
        System.out.println("+---------------------------------+");

        ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
        // Check if CPU Time measurement is supported by the JVM. Quit otherwise
        if (!mxbean.isThreadCpuTimeSupported()) {
            System.out.println("Thread CPU Time is not supported on this Java Virtual Machine. Existing...");
            System.exit(1);
        }

        // Enable CPU Time measurement if it is disabled
        if (!mxbean.isThreadCpuTimeEnabled())
            mxbean.setThreadCpuTimeEnabled(true);

        // Get Process ID of current application
        Long appPid = ProcessHandle.current().pid();

        // Get OS
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        Agent.raspberryPiModel = Agent.getRPiModelName(osArch);

        if (osName.contains("linux")) {
            // GNU/Linux
            if (osArch.contains("aarch64") || osArch.contains("arm")) {
                // Check if Raspberry Pi and use formulas
                if (! Agent.raspberryPiModel.equals("")) {
                    Agent.energySensor = "raspberry";
                } else {
                    // Platform not supported
                    System.out.println("Platform not supported. Existing...");
                    System.exit(1);
                }
            } else {
                // Suppose it's x86/64, check for powercap RAPL
                try {
                    String raplFolderPath = "/sys/class/powercap/intel-rapl/intel-rapl:0";
                    File raplFolder = new File(raplFolderPath);
                    if (raplFolder.exists()) {
                        // Rapl is supported
                        Agent.energySensor = "rapl";
                    } else {
                        // If no RAPL, then no support
                        System.out.println("Platform not supported. Existing...");
                        System.exit(1);
                    }
                } catch (Exception e) {
                    // If no RAPL, then no support
                    System.out.println("Platform not supported. Existing...");
                    System.exit(1);
                }
            }
        } else if (osName.contains("win")) {
            // Windows
            // Check for Intel Power Gadget, and PowerJoular Windows
            Agent.energySensor = "windows";
        } else {
            // Other platforms not supported
            System.out.println("Platform not supported. Existing...");
            System.exit(1);
        }

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
        Agent.filterMethodNames = Arrays.asList(prop.getProperty("filter-method-names").split(","));
        Agent.sizeFilterMethodNames = Agent.filterMethodNames.size();
        Agent.powerMonitorPathWindows = prop.getProperty("powermonitor-path");

        // Get OS MxBean to collect CPU and Process loads
        OperatingSystemMXBean osMxBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Loop for a couple of seconds to initialize OSMXBean to get accurate details (first call will return -1)
        int i = 0;
        System.out.println("Please wait while initializing JoularJX...");
        while (i < 2) {
            osMxBean.getSystemCpuLoad(); // In future when Java 17 becomes widely deployed, use getCpuLoad() instead
            osMxBean.getProcessCpuLoad();
            if (Agent.energySensor.equals("windows")) {
                // On windows, start power monitoring a few seconds to initialize
                try {
                    Agent.powerMonitorWindowsProcess = Runtime.getRuntime().exec(Agent.powerMonitorPathWindows);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Can't start power monitor on Windows. Existing...");
                    System.exit(1);
                }
            }
            i++;
            try {
                Thread.sleep(500);
            } catch (Exception ignoredException) {}
        }
        System.out.println("Initialization finished");

        /**
         * Thread to calculate at runtime the power consumption per thread following a determined cycle duration
         */
        new Thread() {
            public void run() {
                Thread.currentThread().setName("JoularJX Agent Computation");
                System.out.println("Started monitoring application with ID " + appPid);

                // CPU time for each thread
                Map<Long, Long> threadsCPUTime = new HashMap<>();

                while (true) {
                    try {
                        Map<Long, Map<String, Integer>> methodsStats = new HashMap<>();
                        Map<Long, Map<String, Integer>> methodsStatsFiltered = new HashMap<>();
                        Set<Thread> threads = Thread.getAllStackTraces().keySet();

                        double energyBefore = 0.0;
                        switch (Agent.energySensor) {
                            case "rapl":
                                // Get CPU energy consumption with Intel RAPL
                                energyBefore = getRAPLEnergy();
                                break;
                            case "raspberry":
                                // Get CPU energy consumption with Raspberry Pi power models
                                // Nothing to do here, energy will be calculated after
                                break;
                            case "windows":
                                // Get CPU energy consumption on Windows using program monitor
                                // Nothing to do here, energy will be calculated after
                                break;
                            default:
                                break;
                        }

                        int duration = 0;
                        while (duration < 1000) {
                            for (Thread t : threads) {
                                long threadID = t.getId();
                                methodsStats.computeIfAbsent(threadID, tID -> new HashMap<>());
                                methodsStatsFiltered.computeIfAbsent(threadID, tID -> new HashMap<>());

                                // Only check runnable threads (not waiting or blocked)
                                if (t.getState() == State.RUNNABLE) {
                                    int onlyFirst = 0;
                                    int onlyFirstFiltered = 0;
                                    for (StackTraceElement ste : t.getStackTrace()) {
                                        String methName = ste.getClassName() + "." + ste.getMethodName();
                                        if (onlyFirst == 0) {
                                            synchronized (GLOBALLOCK) {
                                                Map<String, Integer> methData = methodsStats.get(threadID);
                                                methData.merge(methName, 1, Integer::sum);
                                            }
                                        }
                                        onlyFirst++;

                                        // Check filtered methods if in stacktrace
                                        if (Agent.isStartsFilterMethodNames(methName)) {
                                            if (onlyFirstFiltered == 0) {
                                                synchronized (GLOBALLOCK) {
                                                    Map<String, Integer> methData = methodsStatsFiltered.get(threadID);
                                                    methData.merge(methName, 1, Integer::sum);
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

                        double energyAfter = 0.0;
                        double CPUEnergy = 0.0;
                        double cpuLoad = osMxBean.getSystemCpuLoad(); // In future when Java 17 becomes widely deployed, use getCpuLoad() instead
                        double processCpuLoad = osMxBean.getProcessCpuLoad();

                        switch (Agent.energySensor) {
                            case "rapl":
                                // At the end of the monitoring loop
                                energyAfter = getRAPLEnergy();
                                // Calculate total energy consumed in the monitoring loop
                                CPUEnergy = energyAfter - energyBefore;
                                break;
                            case "raspberry":
                                // Get CPU energy consumption with Raspberry Pi power models
                                CPUEnergy = calculateCPUEnergyForRaspberryPi(Agent.raspberryPiModel, cpuLoad);
                                break;
                            case "windows":
                                // Get CPU energy consumption on Windows using program monitor
                                try {
                                    BufferedReader input = new BufferedReader(new InputStreamReader(Agent.powerMonitorWindowsProcess.getInputStream()));
                                    String line = input.readLine();
                                    CPUEnergy = Double.parseDouble(line);
                                } catch (Exception ignoredException) {
                                    ignoredException.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }

                        // Calculate CPU energy consumption of the process of the JVM all its apps
                        double ProcessEnergy = Agent.calculateProcessCPUEnergy(cpuLoad, processCpuLoad, CPUEnergy);

                        // Adds current power to total energy
                        totalProcessEnergy += ProcessEnergy;

                        // Now we have:
                        // CPU energy for JVM process
                        // CPU energy for all processes
                        // We need to calculate energy for each thread
                        long totalThreadsCPUTime = 0;
                        for (Thread t : threads) {
                            long threadCPUTime = mxbean.getThreadCpuTime(t.getId());

                            // If thread already monitored, then calculate CPU time since last time
                            threadCPUTime = threadsCPUTime.merge(t.getId(), threadCPUTime,
                                    (present, newValue) -> newValue - present);

                            totalThreadsCPUTime += threadCPUTime;
                        }

                        Map<Long, Double> threadsPower = new HashMap<>();
                        for (Map.Entry<Long, Long> entry : threadsCPUTime.entrySet()) {
                            double percentageCPUTime = (entry.getValue() * 100.0) / totalThreadsCPUTime;
                            double threadPower = ProcessEnergy * (percentageCPUTime / 100.0);
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
                                // Add power (for 1 sec = energy) to total method energy
                                methodsEnergy.merge(methName, methPower, Double::sum);
                                bufMeth.append(methName).append(',').append(methPower).append("\n");
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
                                // Add power (for 1 sec = energy) to total method energy
                                methodsEnergyFiltered.merge(methName, methPower, Double::sum);
                                bufMethFiltered.append(methName).append(',').append(methPower).append("\n");
                            }
                        }

                        // Write to CSV file
                        String fileNameMethods = "joularJX-" + appPid + "-methods-power.csv";
                        try {
                            BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethods, false));
                            out.write(bufMeth.toString());
                            out.close();
                        } catch (Exception ignored) {}

                        // Write to CSV file for filtered methods
                        String fileNameMethodsFiltered = "joularJX-" + appPid + "-methods-filtered-power.csv";
                        try {
                            BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethodsFiltered, false));
                            out.write(bufMethFiltered.toString());
                            out.close();
                        } catch (Exception ignored) {}

                        // Sleep for 10 milliseconds
                        Thread.sleep(10);
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
                // Kill power monitor on Windows process if ever used
                try {
                    Agent.powerMonitorWindowsProcess.destroy();
                } catch (Exception ignoredException) {}

                System.out.println("+---------------------------------+");
                System.out.println("JoularJX finished monitoring application with ID " + appPid);
                System.out.println("Program consumed " + String.format("%.2f", totalProcessEnergy) + " joules");

                // Prepare buffer for methods energy
                StringBuffer buf = new StringBuffer();
                for (Map.Entry<String, Double> entry : methodsEnergy.entrySet()) {
                    String key = entry.getKey();
                    Double value = entry.getValue();
                    buf.append(key).append(',').append(value).append("\n");
                }

                // Write to CSV file
                String fileNameMethods = "joularJX-" + appPid + "-methods-energy.csv";
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethods, true));
                    out.write(buf.toString());
                    out.close();
                } catch (Exception ignored) {}

                // Prepare buffer for filtered methods energy
                StringBuffer bufFil = new StringBuffer();
                for (Map.Entry<String, Double> entry : methodsEnergyFiltered.entrySet()) {
                    String key = entry.getKey();
                    Double value = entry.getValue();
                    bufFil.append(key).append(',').append(value).append("\n");
                }

                // Write to CSV file for filtered methods
                String fileNameMethodsFiltered = "joularJX-" + appPid + "-methods-energy-filtered.csv";
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethodsFiltered, true));
                    out.write(bufFil.toString());
                    out.close();
                } catch (Exception ignored) {}

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
