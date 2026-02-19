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

package org.noureddine.joularjx.cpu;

import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.CommandLineUtils;
import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Cpu} implementation for Windows using the external power monitor tool.
 */
public class IntelWindows implements Cpu {

    private static final Logger logger = JoularJXLogging.getLogger();

    /**
     * Path for our power monitor program on Windows
     */
    private final String programPath;

    /**
     * Process to run power monitor on Windows
     */
    private Process process;

    private Thread readerThread;

    private volatile double currentPower = 0.0;

    /**
     * If the monitoring process was initialized
     */
    private boolean initialized;

    /**
     * Creates a new Windows power monitor instance.
     *
     * @param programPath path to the power monitor executable
     */
    public IntelWindows(final String programPath) {
        if (programPath == null || programPath.isBlank()) {
            logger.severe("Can't start because of missing power monitor path. Set it in config.properties under the '"
                    + AgentProperties.POWER_MONITOR_PATH_PROPERTY + "' key.");
            System.exit(1);
        }
        this.programPath = programPath;
    }

    @Override
    public void initialize() {
        if (initialized) {
            // Do not initialize the same instance multiple times
            return;
        }

        try {
            final var command = CommandLineUtils.splitCommand(programPath);
            if (command.isEmpty()) {
                logger.severe(
                        "Can't start because of missing power monitor path. Set it in config.properties under the '"
                                + AgentProperties.POWER_MONITOR_PATH_PROPERTY + "' key.");
                System.exit(1);
            }
            process = new ProcessBuilder(command).start();

            startReaderThread();

            initialized = true;
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Can't start power monitor \"{0}\" on Windows. Exiting...", programPath);
            logger.throwing(getClass().getName(), "initialize", exception);
            System.exit(1);
        }
    }

    private void startReaderThread() {
        readerThread = new Thread(() -> {
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                boolean isFirst = true;
                while ((line = input.readLine()) != null) {
                    if (!line.isBlank()) {
                        try {
                            double power = Double.parseDouble(line);
                            if (isFirst) {
                                isFirst = false; // first result is not useful
                            } else {
                                currentPower = power;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            } catch (Exception exception) {
                logger.throwing(getClass().getName(), "startReaderThread", exception);
            }
        });
        readerThread.setDaemon(true);
        readerThread.setName("IntelWindows-Reader");
        readerThread.start();
    }

    @Override
    public double getCurrentPower(final double cpuLoad) {
        return currentPower;
    }

    /**
     * The power is returned every second, so it does not need an offset.
     *
     * @return 0
     */
    @Override
    public double getInitialPower() {
        return 0;
    }

    @Override
    public void close() {
        if (initialized) {
            if (readerThread != null) {
                readerThread.interrupt();
            }
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Nothing to do here. Method only useful for RAPL
     */
    public double getMaxPower(final double cpuLoad) {
        return 0;
    }
}
