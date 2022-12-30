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

import com.sun.management.OperatingSystemMXBean;
import org.noureddine.joularjx.power.CPU;
import org.noureddine.joularjx.power.CPUFactory;
import org.noureddine.joularjx.power.RAPLLinux;
import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.FileSystems;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Agent {

    /**
     * Global monitor used to implement mutual-exclusion. In the future this single
     * monitor may be broken up into many different monitors to reduce contention.
     */
    public static final Object GLOBALLOCK = new Object();

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
    private static List<String> filterMethodNames = new ArrayList<>();

    /**
     * Map to store total energy for filtered methods
     */
    private static Map<String, Double> methodsEnergyFiltered = new HashMap<>();

    /**
     * Monitoring the agent will use to monitor the energy usage
     */
    private static CPU cpuMonitoring;

    public static Logger jxlogger;

    /**
     * JVM hook to statically load the java agent at startup.
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     */
    public static void premain(String args, Instrumentation inst) {
        Thread.currentThread().setName("JoularJX Agent Thread");
        AgentProperties properties = new AgentProperties(FileSystems.getDefault());
        Agent.jxlogger = JoularJXLogging.getInstance(properties.getLoggerLevel()).getLogger();

        System.out.println("+---------------------------------+");
        System.out.println("| JoularJX Agent Version 1.5      |");
        System.out.println("+---------------------------------+");

        ThreadMXBean threadBean = createThreadBean();

        // Get Process ID of current application
        long appPid = ProcessHandle.current().pid();

        cpuMonitoring = CPUFactory.getCpu(properties);

        // Get filtered methods
        Agent.filterMethodNames = properties.getFilterMethodNames();

        OperatingSystemMXBean osBean = createOperatingSystemBean();

        Agent.jxlogger.log(Level.INFO, "Initialization finished");

        new Thread(() -> monitor(appPid, properties, osBean, threadBean)).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(appPid)));
    }

    private static ThreadMXBean createThreadBean() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        // Check if CPU Time measurement is supported by the JVM. Quit otherwise
        if (!threadBean.isThreadCpuTimeSupported()) {
            Agent.jxlogger.log(Level.SEVERE, "Thread CPU Time is not supported on this Java Virtual Machine. Existing...");
            System.exit(1);
        }

        // Enable CPU Time measurement if it is disabled
        if (!threadBean.isThreadCpuTimeEnabled()) {
            threadBean.setThreadCpuTimeEnabled(true);
        }

        return threadBean;
    }

    private static OperatingSystemMXBean createOperatingSystemBean() {
        // Get OS MxBean to collect CPU and Process loads
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Loop for a couple of seconds to initialize OSMXBean to get accurate details (first call will return -1)
        int i = 0;
        Agent.jxlogger.log(Level.INFO, "Please wait while initializing JoularJX...");
        while (i < 2) {
            osBean.getSystemCpuLoad(); // In future when Java 17 becomes widely deployed, use getCpuLoad() instead
            osBean.getProcessCpuLoad();

            cpuMonitoring.initialize();

            i++;
            try {
                Thread.sleep(500);
            } catch (Exception ignoredException) {}
        }
        return osBean;
    }

    private static void monitor(long appPid, AgentProperties properties,
                                OperatingSystemMXBean osBean, ThreadMXBean threadBean) {
        Thread.currentThread().setName("JoularJX Agent Computation");
        Agent.jxlogger.log(Level.INFO, "Started monitoring application with ID {0}", appPid);

        // CPU time for each thread
        Map<Long, Long> threadsCpuTime = new HashMap<>();

        while (true) {
            try {
                Map<Long, Map<String, Integer>> methodsStats = new HashMap<>();
                Map<Long, Map<String, Integer>> methodsStatsFiltered = new HashMap<>();
                Set<Thread> threads = Thread.getAllStackTraces().keySet();

                final double energyBefore;
                if (cpuMonitoring instanceof RAPLLinux) {
                    // Get CPU energy consumption with Intel RAPL
                    energyBefore = cpuMonitoring.getPower(0);
                } else {
                    energyBefore = 0.0;
                }

                int duration = 0;
                while (duration < 1000) {
                    for (Thread thread : threads) {
                        long threadId = thread.getId();
                        methodsStats.computeIfAbsent(threadId, tID -> new HashMap<>());
                        methodsStatsFiltered.computeIfAbsent(threadId, tID -> new HashMap<>());

                        // Only check runnable threads (not waiting or blocked)
                        if (thread.getState() == Thread.State.RUNNABLE) {
                            int onlyFirst = 0;
                            int onlyFirstFiltered = 0;
                            for (StackTraceElement ste : thread.getStackTrace()) {
                                String methodName = ste.getClassName() + "." + ste.getMethodName();
                                if (onlyFirst == 0) {
                                    synchronized (GLOBALLOCK) {
                                        Map<String, Integer> methData = methodsStats.get(threadId);
                                        methData.merge(methodName, 1, Integer::sum);
                                    }
                                }
                                onlyFirst++;

                                // Check filtered methods if in stacktrace
                                if (Agent.isStartsFilterMethodNames(methodName)) {
                                    if (onlyFirstFiltered == 0) {
                                        synchronized (GLOBALLOCK) {
                                            Map<String, Integer> methData = methodsStatsFiltered.get(threadId);
                                            methData.merge(methodName, 1, Integer::sum);
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

                double cpuLoad = osBean.getSystemCpuLoad(); // In future when Java 17 becomes widely deployed, use getCpuLoad() instead
                double processCpuLoad = osBean.getProcessCpuLoad();

                final double energyAfter = cpuMonitoring.getPower(cpuLoad);
                final double cpuEnergy = energyAfter - energyBefore;

                // Calculate CPU energy consumption of the process of the JVM all its apps
                double processEnergy = Agent.calculateProcessCPUEnergy(cpuLoad, processCpuLoad, cpuEnergy);

                // Adds current power to total energy
                totalProcessEnergy += processEnergy;

                // Now we have:
                // CPU energy for JVM process
                // CPU energy for all processes
                // We need to calculate energy for each thread
                long totalThreadsCPUTime = 0;
                for (Thread thread : threads) {
                    long threadCPUTime = threadBean.getThreadCpuTime(thread.getId());

                    // If thread already monitored, then calculate CPU time since last time
                    threadCPUTime = threadsCpuTime.merge(thread.getId(), threadCPUTime,
                            (present, newValue) -> newValue - present);

                    totalThreadsCPUTime += threadCPUTime;
                }

                Map<Long, Double> threadsPower = new HashMap<>();
                for (Map.Entry<Long, Long> entry : threadsCpuTime.entrySet()) {
                    double percentageCpuTime = (entry.getValue() * 100.0) / totalThreadsCPUTime;
                    double threadPower = processEnergy * (percentageCpuTime / 100.0);
                    threadsPower.put(entry.getKey(), threadPower);
                }

                // Now we have power for each thread, and stats for methods in each thread
                // We allocated power for each method based on statistics
                StringBuilder methodBuffer = new StringBuilder();
                for (Map.Entry<Long, Map<String, Integer>> entry : methodsStats.entrySet()) {
                    long threadId = entry.getKey();
                    for (Map.Entry<String, Integer> methodEntry : entry.getValue().entrySet()) {
                        String methodName = methodEntry.getKey();
                        double methodPower = threadsPower.get(threadId) * (methodEntry.getValue() / 100.0);
                        // Add power (for 1 sec = energy) to total method energy
                        methodsEnergy.merge(methodName, methodPower, Double::sum);
                        methodBuffer.append(methodName).append(',').append(methodPower).append("\n");
                    }
                }

                // For filtered methods
                // Now we have power for each thread, and stats for methods in each thread
                // We allocated power for each method based on statistics
                StringBuilder methodFilteredBuffer = new StringBuilder();
                for (Map.Entry<Long, Map<String, Integer>> entry : methodsStatsFiltered.entrySet()) {
                    long threadId = entry.getKey();
                    for (Map.Entry<String, Integer> methodEntry : entry.getValue().entrySet()) {
                        String methodName = methodEntry.getKey();
                        double methodPower = threadsPower.get(threadId) * (methodEntry.getValue() / 100.0);
                        // Add power (for 1 sec = energy) to total method energy
                        methodsEnergyFiltered.merge(methodName, methodPower, Double::sum);
                        methodFilteredBuffer.append(methodName).append(',').append(methodPower).append("\n");
                    }
                }

                if (properties.getSaveRuntimeData()) {
                    String fileNameMethods = "joularJX-" + appPid + "-methods-power.csv";
                    String fileNameMethodsFiltered = "joularJX-" + appPid + "-methods-filtered-power.csv";
                    if (! properties.getOverwriteRuntimeData()) {
                        long unixTime = Instant.now().getEpochSecond();
                        fileNameMethods = "joularJX-" + appPid + "-" + unixTime + "-methods-power.csv";
                        fileNameMethodsFiltered = "joularJX-" + appPid + "-" + unixTime + "-methods-filtered-power.csv";
                    }

                    // Write to CSV file
                    try {
                        BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethods, false));
                        out.write(methodBuffer.toString());
                        out.close();
                    } catch (Exception ignored) {
                    }

                    // Write to CSV file for filtered methods
                    try {
                        BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethodsFiltered, false));
                        out.write(methodFilteredBuffer.toString());
                        out.close();
                    } catch (Exception ignored) {
                    }
                }

                // Sleep for 10 milliseconds
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void shutdown(long appPid) {
        // Close monitoring implementation to release all resources
        try {
            cpuMonitoring.close();
        } catch (Exception e) {}

        Agent.jxlogger.log(Level.INFO, "JoularJX finished monitoring application with ID {0}", appPid);
        Agent.jxlogger.log(Level.INFO, "Program consumed {0,number,#.##} joules", totalProcessEnergy);

        // Prepare buffer for methods energy
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, Double> entry : methodsEnergy.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            buf.append(key).append(',').append(value).append("\n");
        }

        // Write to CSV file
        String fileNameMethods = "joularJX-" + appPid + "-methods-energy.csv";
        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethods, true))) {
            out.write(buf.toString());
        } catch (Exception ignored) {}

        // Prepare buffer for filtered methods energy
        StringBuilder bufFil = new StringBuilder();
        for (Map.Entry<String, Double> entry : methodsEnergyFiltered.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            bufFil.append(key).append(',').append(value).append("\n");
        }

        // Write to CSV file for filtered methods
        String fileNameMethodsFiltered = "joularJX-" + appPid + "-methods-energy-filtered.csv";
        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethodsFiltered, true))) {
            out.write(bufFil.toString());
        } catch (Exception ignored) {}

        Agent.jxlogger.log(Level.INFO, "Energy consumption of methods and filtered methods written to {0} and {1} files",
                new Object[]{fileNameMethods, fileNameMethodsFiltered});
    }

    /**
     * Calculate process energy consumption
     * @param totalCpuUsage Total CPU usage
     * @param processCpuUsage Process CPU usage
     * @param cpuEnergy CPU energy
     * @return Process energy consumption
     */
    private static double calculateProcessCPUEnergy(double totalCpuUsage, double processCpuUsage, double cpuEnergy) {
        return (processCpuUsage * cpuEnergy) / totalCpuUsage;
    }

    /**
     * Check if methodName starts with one of the filtered method names
     * @param methodName Name of method
     * @return True if methodName starts with one of the filtered method names, false if not
     */
    private static boolean isStartsFilterMethodNames(String methodName) {
        // In most cases, there will be one filtered method name
        // So we check that to gain performance and avoid looping the list
        if (Agent.filterMethodNames.size() == 1) {
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
     * Private constructor
     */
    private Agent() {
    }
}
