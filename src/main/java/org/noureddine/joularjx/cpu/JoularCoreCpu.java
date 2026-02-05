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

    /**
     * Process to run Joular Core
     */
    private Process process;

    /**
     * If the monitoring process was initialized
     */
    private boolean initialized;

    public JoularCoreCpu(final String programPath) {
        if (programPath == null || programPath.isBlank()) {
            logger.severe("Can't start because of missing Joular Core path. Set it in config.properties under the '"
                    + AgentProperties.JOULAR_CORE_PATH_PROPERTY + "' key.");
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
            process = Runtime.getRuntime().exec(programPath);
            
            // The first result is not useful
            getCurrentPower(0);
            
            initialized = true;
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Can't start Joular Core \"{0}\". Exiting...", programPath);
            logger.throwing(getClass().getName(), "initialize", exception);
            System.exit(1);
        }
    }

    @Override
    public double getCurrentPower(final double cpuLoad) {
        try {
            // Should not be closed since it closes the process stream
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = input.readLine();
            if (line != null && !line.isBlank()) {
                return Double.parseDouble(line);
            }
        } catch (Exception exception) {
            logger.throwing(getClass().getName(), "getCurrentPower", exception);
        }
        return 0;
    }

    @Override
    public double getInitialPower() {
        return 0;
    }

    @Override
    public void close() {
        if (initialized && process != null) {
            process.destroy();
        }
    }

    @Override
    public double getMaxPower(final double cpuLoad) {
        return 0;
    }
}
