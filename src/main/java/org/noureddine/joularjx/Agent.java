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
import org.noureddine.joularjx.monitor.MonitoringHandler;
import org.noureddine.joularjx.monitor.MonitoringStatus;
import org.noureddine.joularjx.monitor.ShutdownHandler;
import org.noureddine.joularjx.power.CPU;
import org.noureddine.joularjx.power.CPUFactory;
import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Agent {

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

        CPU cpu = CPUFactory.getCpu(properties);

        OperatingSystemMXBean osBean = createOperatingSystemBean(cpu);
        MonitoringStatus status = new MonitoringStatus();

        Agent.jxlogger.log(Level.INFO, "Initialization finished");

        new Thread(() -> new MonitoringHandler(appPid, properties, cpu, status, osBean, threadBean)).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> new ShutdownHandler(appPid, cpu, status)));
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

    private static OperatingSystemMXBean createOperatingSystemBean(CPU cpu) {
        // Get OS MxBean to collect CPU and Process loads
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // Loop for a couple of seconds to initialize OSMXBean to get accurate details (first call will return -1)
        int i = 0;
        Agent.jxlogger.log(Level.INFO, "Please wait while initializing JoularJX...");
        while (i < 2) {
            osBean.getSystemCpuLoad(); // In future when Java 17 becomes widely deployed, use getCpuLoad() instead
            osBean.getProcessCpuLoad();

            cpu.initialize();

            i++;
            try {
                Thread.sleep(500);
            } catch (Exception ignoredException) {}
        }
        return osBean;
    }

    /**
     * Private constructor
     */
    private Agent() {
    }
}
