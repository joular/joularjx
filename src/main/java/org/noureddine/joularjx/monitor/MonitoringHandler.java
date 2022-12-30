package org.noureddine.joularjx.monitor;

import com.sun.management.OperatingSystemMXBean;
import org.noureddine.joularjx.Agent;
import org.noureddine.joularjx.power.CPU;
import org.noureddine.joularjx.power.RAPLLinux;
import org.noureddine.joularjx.utils.AgentProperties;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class MonitoringHandler implements Runnable {

    private final long appPid;
    private final AgentProperties properties;
    private final CPU cpu;
    private final MonitoringStatus status;
    private final OperatingSystemMXBean osBean;
    private final ThreadMXBean threadBean;

    public MonitoringHandler(long appPid, AgentProperties properties, CPU cpu, MonitoringStatus status,
                             OperatingSystemMXBean osBean, ThreadMXBean threadBean) {
        this.appPid = appPid;
        this.properties = properties;
        this.cpu = cpu;
        this.status = status;
        this.osBean = osBean;
        this.threadBean = threadBean;
    }

    @Override
    public void run() {
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
                if (cpu instanceof RAPLLinux) {
                    // Get CPU energy consumption with Intel RAPL
                    energyBefore = cpu.getPower(0);
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
                                    Map<String, Integer> methodData = methodsStats.get(threadId);
                                    methodData.merge(methodName, 1, Integer::sum);
                                }
                                onlyFirst++;

                                // Check filtered methods if in stacktrace
                                if (properties.filtersMethod(methodName)) {
                                    if (onlyFirstFiltered == 0) {
                                        Map<String, Integer> methodData = methodsStatsFiltered.get(threadId);
                                        methodData.merge(methodName, 1, Integer::sum);
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

                final double energyAfter = cpu.getPower(cpuLoad);
                final double cpuEnergy = energyAfter - energyBefore;

                // Calculate CPU energy consumption of the process of the JVM all its apps
                double processEnergy = calculateProcessCpuEnergy(cpuLoad, processCpuLoad, cpuEnergy);

                // Adds current power to total energy
                status.addConsumedEnergy(processEnergy);

                // Now we have:
                // CPU energy for JVM process
                // CPU energy for all processes
                // We need to calculate energy for each thread
                long totalThreadsCpuTime = 0;
                for (Thread thread : threads) {
                    long threadCpuTime = threadBean.getThreadCpuTime(thread.getId());

                    // If thread already monitored, then calculate CPU time since last time
                    threadCpuTime = threadsCpuTime.merge(thread.getId(), threadCpuTime,
                            (present, newValue) -> newValue - present);

                    totalThreadsCpuTime += threadCpuTime;
                }

                Map<Long, Double> threadsPower = new HashMap<>();
                for (Map.Entry<Long, Long> entry : threadsCpuTime.entrySet()) {
                    double percentageCpuTime = (entry.getValue() * 100.0) / totalThreadsCpuTime;
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
                        status.addMethodConsumedEnergy(methodName, methodPower);
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
                        status.addFilteredMethodConsumedEnergy(methodName, methodPower);
                        methodFilteredBuffer.append(methodName).append(',').append(methodPower).append("\n");
                    }
                }

                if (properties.savesRuntimeData()) {
                    String fileNameMethods = "joularJX-" + appPid + "-methods-power.csv";
                    String fileNameMethodsFiltered = "joularJX-" + appPid + "-methods-filtered-power.csv";
                    if (!properties.overwritesRuntimeData()) {
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

    /**
     * Calculate process energy consumption
     * @param totalCpuUsage Total CPU usage
     * @param processCpuUsage Process CPU usage
     * @param cpuEnergy CPU energy
     * @return Process energy consumption
     */
    private double calculateProcessCpuEnergy(double totalCpuUsage, double processCpuUsage, double cpuEnergy) {
        return (processCpuUsage * cpuEnergy) / totalCpuUsage;
    }
}
