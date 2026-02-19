/*
 * Copyright (c) 2026, Adel Noureddine
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
 * CPU implementation for Joular Core
 */
public class JoularCoreCpu implements Cpu {

    private static final Logger logger = JoularJXLogging.getLogger();

    /**
     * Path for Joular Core
     */
    private final String programPath;
    private final String programParameters;

    /**
     * Process to run Joular Core
     */
    private Process process;

    private Thread readerThread;

    private volatile double currentPower = 0.0;

    /**
     * If the monitoring process was initialized
     */
    private boolean initialized;

    /**
     * Creates a new Joular Core CPU monitor instance.
     *
     * @param programPath       path to the Joular Core executable
     * @param programParameters Joular Core command line parameters
     */
    public JoularCoreCpu(final String programPath, final String programParameters) {
        if (programPath == null || programPath.isBlank()) {
            logger.severe("Can't start because of missing Joular Core path. Set it in config.properties under the '"
                    + AgentProperties.JOULAR_CORE_PATH_PROPERTY + "' key.");
            System.exit(1);
        }
        this.programPath = programPath;
        this.programParameters = programParameters;
    }

    @Override
    public void initialize() {
        if (initialized) {
            // Do not initialize the same instance multiple times
            return;
        }

        try {
            final var command = CommandLineUtils.buildCommand(programPath, programParameters);
            if (command.isEmpty()) {
                logger.severe("Can't start because of missing Joular Core path. Set it in config.properties under the '"
                        + AgentProperties.JOULAR_CORE_PATH_PROPERTY + "' key.");
                System.exit(1);
            }
            process = new ProcessBuilder(command).start();

            startReaderThread();

            initialized = true;
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Can't start Joular Core. Exiting...");
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
        readerThread.setName("JoularCore-Reader");
        readerThread.start();
    }

    @Override
    public double getCurrentPower(final double cpuLoad) {
        return currentPower;
    }

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

    @Override
    public double getMaxPower(final double cpuLoad) {
        return 0;
    }
}
