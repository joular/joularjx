/*
 * Copyright (c) 2021-2026, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Author : Adel Noureddine
 */

package org.noureddine.joularjx;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.noureddine.joularjx.cpu.Cpu;
import org.noureddine.joularjx.cpu.CpuFactory;
import org.noureddine.joularjx.monitor.MonitoringHandler;
import org.noureddine.joularjx.monitor.MonitoringStatus;
import org.noureddine.joularjx.monitor.ShutdownHandler;
import org.noureddine.joularjx.result.ResultTreeManager;
import org.noureddine.joularjx.result.ResultWriter;
import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;

import com.sun.management.OperatingSystemMXBean;

/**
 * Main JoularJX agent class
 */
public class Agent {

    /**
     * JoularJX agent thread name
     */
    public static final String NAME_THREAD_NAME = "JoularJX Agent Thread";

    /**
     * JoularJX computation thread name
     */
    public static final String COMPUTATION_THREAD_NAME = "JoularJX Agent Computation";

    /**
     * Logger
     */
    private static final Logger logger = JoularJXLogging.getLogger();

    /**
     * JVM hook to statically load the java agent at startup.
     * After the Java Virtual Machine (JVM) has initialized, the premain method
     * will be called. Then the real application main method will be called.
     * 
     * @param args command line arguments
     * @param inst JVM instrumentation
     */
    public static void premain(String args, Instrumentation inst) {
        Thread.currentThread().setName(NAME_THREAD_NAME);
        AgentProperties properties = new AgentProperties();
        JoularJXLogging.updateLevel(properties.getLoggerLevel());

        logger.info("+---------------------------------+");
        logger.info("| JoularJX Agent Version 3.1.0    |");
        logger.info("+---------------------------------+");

        ThreadMXBean threadBean = createThreadBean();

        // Get Process ID of current application
        long appPid = ProcessHandle.current().pid();

        // Creating the required folders to store the result files generated later on
        final long currentTime = System.currentTimeMillis();
        ResultTreeManager resultTreeManager = new ResultTreeManager(properties, appPid, currentTime);
        if (!resultTreeManager.create()) {
            logger.log(Level.WARNING,
                    "Error(s) occurred while creating the result folder hierarchy. Some results may not be reported.");
        }

        Cpu cpu = CpuFactory.getCpu(properties);

        OperatingSystemMXBean osBean = createOperatingSystemBean(cpu);
        MonitoringStatus status = new MonitoringStatus();
        List<ResultWriter> resultWriters = getWriters(properties, appPid, currentTime);
        MonitoringHandler monitoringHandler = new MonitoringHandler(appPid, properties, resultWriters, cpu, status,
                osBean, threadBean);
        ShutdownHandler shutdownHandler = new ShutdownHandler(appPid, resultWriters, cpu, status, properties);

        logger.log(Level.INFO, "Initialization finished");

        new Thread(monitoringHandler, COMPUTATION_THREAD_NAME).start();
        Runtime.getRuntime().addShutdownHook(new Thread(shutdownHandler));
    }

    /**
     * Get all output classes from SPI
     * 
     * @param props     application properties
     * @param pid       app PID
     * @param timestamp current timestamp
     *
     * @return the initialized implementations to output data
     */
    public static List<ResultWriter> getWriters(AgentProperties props, long pid, long timestamp) {
        List<ResultWriter> services = new ArrayList<>();
        ServiceLoader<ResultWriter> loader = ServiceLoader.load(ResultWriter.class);
        loader.forEach(service -> {
            service.initialize(props, pid, timestamp);
            services.add(service);
        });
        return services;
    }

    /**
     * Creates and returns a ThreadMXBean.
     * Checks if the Thread CPU Time is supported by the JVM and enables it if it is
     * disabled.
     */
    private static ThreadMXBean createThreadBean() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        // Check if CPU Time measurement is supported by the JVM. Quit otherwise
        if (!threadBean.isThreadCpuTimeSupported()) {
            logger.log(Level.SEVERE, "Thread CPU Time is not supported on this Java Virtual Machine. Exiting...");
            System.exit(1);
        }

        // Enable CPU Time measurement if it is disabled
        if (!threadBean.isThreadCpuTimeEnabled()) {
            threadBean.setThreadCpuTimeEnabled(true);
        }

        return threadBean;
    }

    /**
     * Creates and returns an OperatingSystemMXBean, used to collect CPU and process
     * loads.
     * 
     * @param cpu a {@link Cpu} implementation
     * @return an OperatingSystemMXBean
     */
    private static OperatingSystemMXBean createOperatingSystemBean(Cpu cpu) {
        // Get OS MxBean to collect CPU and Process loads
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Loop for a couple of seconds to initialize OSMXBean to get accurate details
        // (first call will return -1)
        logger.log(Level.INFO, "Please wait while initializing JoularJX...");
        cpu.initialize();

        for (int i = 0; i < 2; i++) {
            osBean.getSystemCpuLoad(); // In future when Java 17 becomes widely deployed, use getCpuLoad() instead
            osBean.getProcessCpuLoad();

            try {
                Thread.sleep(500);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }
        return osBean;
    }

    /**
     * Private constructor
     */
    private Agent() {
    }
}
