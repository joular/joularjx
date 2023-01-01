package org.noureddine.joularjx.monitor;

import com.sun.management.OperatingSystemMXBean;
import org.noureddine.joularjx.cpu.Cpu;
import org.noureddine.joularjx.result.ResultWriter;
import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.IOException;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitoringHandler implements Runnable {

    private static final String DESTROY_THREAD_NAME = "DestroyJavaVM";
    private static final long SAMPLE_TIME_MILLISECONDS = 1000;
    private static final long SAMPLE_RATE_MILLISECONDS = 10;
    private static final int SAMPLE_ITERATIONS = (int) (SAMPLE_TIME_MILLISECONDS / SAMPLE_RATE_MILLISECONDS);
    private static final Logger logger = JoularJXLogging.getLogger();

    private final long appPid;
    private final AgentProperties properties;
    private final ResultWriter resultWriter;
    private final Cpu cpu;
    private final MonitoringStatus status;
    private final OperatingSystemMXBean osBean;
    private final ThreadMXBean threadBean;

    public MonitoringHandler(long appPid, AgentProperties properties, ResultWriter resultWriter, Cpu cpu,
                             MonitoringStatus status, OperatingSystemMXBean osBean, ThreadMXBean threadBean) {
        this.appPid = appPid;
        this.properties = properties;
        this.resultWriter = resultWriter;
        this.cpu = cpu;
        this.status = status;
        this.osBean = osBean;
        this.threadBean = threadBean;
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "Started monitoring application with ID {0}", appPid);

        // CPU time for each thread
        Map<Long, Long> threadsCpuTime = new HashMap<>();

        while (!destroyingVM()) {
            try {
                double energyBefore = cpu.getInitialPower();

                var samples = sample();
                var methodsStats = extractStats(samples, methodName -> true);
                var methodsStatsFiltered = extractStats(samples, properties::filtersMethod);

                double cpuLoad = osBean.getSystemCpuLoad(); // In future when Java 17 becomes widely deployed, use getCpuLoad() instead
                double processCpuLoad = osBean.getProcessCpuLoad();

                double energyAfter = cpu.getCurrentPower(cpuLoad);
                double cpuEnergy = energyAfter - energyBefore;

                // Calculate CPU energy consumption of the process of the JVM all its apps
                double processEnergy = calculateProcessCpuEnergy(cpuLoad, processCpuLoad, cpuEnergy);

                // Adds current power to total energy
                status.addConsumedEnergy(processEnergy);

                // Now we have:
                // CPU energy for JVM process
                // CPU energy for all processes
                // We need to calculate energy for each thread
                long totalThreadsCpuTime = updateThreadsCpuTime(methodsStats, threadsCpuTime);
                var threadCpuTimePercentages = getThreadsCpuTimePercentage(threadsCpuTime, totalThreadsCpuTime, processEnergy);

                updateMethodsConsumedEnergy(methodsStats, threadCpuTimePercentages, status::addMethodConsumedEnergy);
                updateMethodsConsumedEnergy(methodsStatsFiltered, threadCpuTimePercentages, status::addFilteredMethodConsumedEnergy);

                shareResults("all", methodsStats, threadCpuTimePercentages);
                shareResults("filtered", methodsStatsFiltered, threadCpuTimePercentages);

                Thread.sleep(SAMPLE_RATE_MILLISECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            } catch (IOException exception) {
                logger.log(Level.SEVERE, "Cannot perform IO \"{0}\"", exception.getMessage());
                logger.throwing(getClass().getName(), "run", exception);
                System.exit(1);
            }
        }
    }

    private Map<Thread, List<StackTraceElement[]>> sample() {
        Map<Thread, List<StackTraceElement[]>> result = new HashMap<>();
        try {
            for (int duration = 0; duration < SAMPLE_TIME_MILLISECONDS; duration += SAMPLE_RATE_MILLISECONDS) {
                for (var entry : Thread.getAllStackTraces().entrySet()) {
                    // Only check runnable threads (not waiting or blocked)
                    if (entry.getKey().getState() == Thread.State.RUNNABLE) {
                        var target = result.computeIfAbsent(entry.getKey(),
                                t -> new ArrayList<>(SAMPLE_ITERATIONS));
                        target.add(entry.getValue());
                    }
                }

                Thread.sleep(SAMPLE_RATE_MILLISECONDS);
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }

        return result;
    }

    private Map<Thread, Map<String, Integer>> extractStats(Map<Thread, List<StackTraceElement[]>> samples,
                                                           Predicate<String> covers) {
        Map<Thread, Map<String, Integer>> stats = new HashMap<>();

        for (var entry : samples.entrySet()) {
            Map<String, Integer> target = new HashMap<>();
            stats.put(entry.getKey(), target);

            for (StackTraceElement[] stackTrace : entry.getValue()) {
                for (StackTraceElement stackTraceElement : stackTrace) {
                    String methodName = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
                    if (covers.test(methodName)) {
                        target.merge(methodName, 1, Integer::sum);
                        break;
                    }
                }
            }
        }

        return stats;
    }

    private long updateThreadsCpuTime(Map<Thread, Map<String, Integer>> methodsStats, Map<Long, Long> threadsCpuTime) {
        long totalThreadsCpuTime = 0;
        for (var entry : methodsStats.entrySet()) {
            long threadCpuTime = threadBean.getThreadCpuTime(entry.getKey().getId());

            threadCpuTime *= entry.getValue().values().stream().mapToDouble(i -> i).sum() / SAMPLE_ITERATIONS;

            // If thread already monitored, then calculate CPU time since last time
            threadCpuTime = threadsCpuTime.merge(entry.getKey().getId(), threadCpuTime,
                    (present, newValue) -> newValue - present);

            totalThreadsCpuTime += threadCpuTime;
        }
        return totalThreadsCpuTime;
    }

    private Map<Long, Double> getThreadsCpuTimePercentage(Map<Long, Long> threadsCpuTime,
                                                          long totalThreadsCpuTime,
                                                          double processEnergy) {
        Map<Long, Double> threadsPower = new HashMap<>();
        for (var entry : threadsCpuTime.entrySet()) {
            double percentageCpuTime = (entry.getValue() * 100.0) / totalThreadsCpuTime;
            double threadPower = processEnergy * (percentageCpuTime / 100.0);
            threadsPower.put(entry.getKey(), threadPower);
        }
        return threadsPower;
    }

    private void updateMethodsConsumedEnergy(Map<Thread, Map<String, Integer>> methodsStats,
                                             Map<Long, Double> threadCpuTimePercentages,
                                             ObjDoubleConsumer<String> updateMethodConsumedEnergy) {
        for (var threadEntry : methodsStats.entrySet()) {
            double totalEncounters = threadEntry.getValue().values().stream().mapToDouble(i -> i).sum();
            for (var methodEntry : threadEntry.getValue().entrySet()) {
                double methodPower = threadCpuTimePercentages.get(threadEntry.getKey().getId())
                        * (methodEntry.getValue() / totalEncounters);
                updateMethodConsumedEnergy.accept(methodEntry.getKey(), methodPower);
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

    private void shareResults(String modeName,
                              Map<Thread, Map<String, Integer>> methodsStats,
                              Map<Long, Double> threadCpuTimePercentages) throws IOException {
        if (!properties.savesRuntimeData()) {
            return;
        }

        String fileName = properties.overwritesRuntimeData() ?
                String.format("joularJX-%d-%s-methods-power", appPid, modeName) :
                String.format("joularJX-%d-%d-%s-methods-power", appPid, System.currentTimeMillis(), modeName);

        resultWriter.setTarget(fileName, true);

        for (var stats : methodsStats.entrySet()) {
            for (var methodEntry : stats.getValue().entrySet()) {
                double methodPower = threadCpuTimePercentages.get(stats.getKey().getId()) * (methodEntry.getValue() / 100.0);
                resultWriter.write(methodEntry.getKey(), methodPower);
            }
        }

        resultWriter.closeTarget();
    }

    private boolean destroyingVM() {
        return Thread.getAllStackTraces().keySet().stream()
                .anyMatch(thread -> thread.getName().equals(DESTROY_THREAD_NAME));
    }
}
