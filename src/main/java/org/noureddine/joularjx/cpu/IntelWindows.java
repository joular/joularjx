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

package org.noureddine.joularjx.cpu;

import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    /**
     * If the monitoring process was initialized
     */
    private boolean initialized;

    public IntelWindows(final String programPath) {
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
            initialized = true;
        } catch (IOException ex) {
            ex.printStackTrace();
            logger.log(Level.SEVERE, "Can't start power monitor on Windows. Existing...");
            System.exit(1);
        }
    }

    @Override
    public double getCurrentPower(final double cpuLoad) {
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = input.readLine();
            return Double.parseDouble(line);
        } catch (Exception ignoredException) {
            ignoredException.printStackTrace();
        }
        return 0;
    }

    @Override
    public void close() {
        if (initialized) {
            process.destroy();
        }
    }
}