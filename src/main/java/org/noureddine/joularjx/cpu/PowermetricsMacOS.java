/*
 * Copyright (c) 2021-2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.cpu;

import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link Cpu} implementation using <a href='https://firefox-source-docs.mozilla.org/performance/powermetrics.htm'>powermetrics</a>.
 */
public class PowermetricsMacOS implements Cpu {
    private static final Logger logger = JoularJXLogging.getLogger();
    private static final String POWER_INDICATOR_M_CHIP = " Power: ";
    private static final String POWER_INDICATOR_INTEL_CHIP = "Intel energy model derived package power (CPUs+GT+SA): ";
    private Process process;
    private BufferedReader reader;

    private boolean initialized;
    boolean intelCpu = false;

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }

        try {
            // todo: detect when sudo fails as this currently won't throw an exception
            process = Runtime.getRuntime().exec("sudo powermetrics --samplers cpu_power -i 1000");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            initialized = true;
            readHeader();
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Can't start powermetrics. Exiting...");
            logger.throwing(getClass().getName(), "initialize", exception);
            System.exit(1);
        }
    }

    void readHeader() throws IOException {
        BufferedReader reader = getReader();
        for (int i = 0; i < 6; i++) {
            String line = reader.readLine();
            if (line.startsWith("EFI version")) {
                intelCpu = true;
            }
        }
    }

    @Override
    public double getInitialPower() {
        return 0;
    }

    @Override
    public double getCurrentPower(double cpuLoad) {
        if (intelCpu) {
            return getCurrentPowerIntel();
        } else {
            return getCurrentPowerM();
        }
    }

    private double getCurrentPowerIntel() {
        double powerInWatts = 0;
        try {
            String line;
            BufferedReader reader = getReader();
            while (reader.ready() && (line = reader.readLine()) != null) {

                // skip empty / header lines
                if (line.isEmpty() || line.startsWith("*")) {
                    continue;
                }

                // for Intel chips, the: "Intel energy model derived package power (CPUs+GT+SA): xxx W" pattern
                final var i = line.indexOf(POWER_INDICATOR_INTEL_CHIP);
                if (i >= 0) {
                    powerInWatts += Double.parseDouble(line.substring(i + POWER_INDICATOR_INTEL_CHIP.length(), line.indexOf('W')));
                }
            }
            return powerInWatts;
        } catch (IOException e) {
            logger.throwing(getClass().getName(), "getCurrentPower", e);
        }

        return 0.0;
    }

    public double getCurrentPowerM() {
        int powerInMilliwatts = 0;
        try {
            String line;
            BufferedReader reader = getReader();
            while (reader.ready() && (line = reader.readLine()) != null) {

                // skip empty / header lines
                if (line.isEmpty() || line.startsWith("*")) {
                    continue;
                }

                // looking for line fitting the: "<name> Power: xxx mW" pattern and add all of the associated values together
                final var i = line.indexOf(POWER_INDICATOR_M_CHIP);
                if (i >= 0 && '-' != line.charAt(1) && !line.startsWith("Combined")) {
                    powerInMilliwatts += Integer.parseInt(line.substring(i + POWER_INDICATOR_M_CHIP.length(), line.indexOf('m') - 1));
                }
            }
            return (double) powerInMilliwatts / 1000;
        } catch (IOException e) {
            logger.throwing(getClass().getName(), "getCurrentPower", e);
        }

        return 0.0;
    }

    /**
     * Override point for testing.
     */
    protected BufferedReader getReader() {
        return reader;
    }

    private static int extractPowerInMilliwatts(String line, int powerIndex) {
        try {
            if (line.trim().endsWith("mW")) {
                return Integer.parseInt(line.substring(powerIndex, line.indexOf('m') - 1));
            } else if (line.trim().endsWith("W")) {
                return (int) (1000.0 * Double.parseDouble(line.substring(powerIndex, line.indexOf('W'))));
            } else {
                logger.log(Level.SEVERE, "Power line does not end with mW or W, ignoring line: " + line);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot parse power value from line '" + line + "'", e);
        }
        return 0;
    }

    @Override
    public void close() {
        if (initialized) {
            process.destroy();
        }
    }

    /**
     * Nothing to do here. Method only useful for RAPL
     */
    public double getMaxPower(final double cpuLoad) {
        return 0;
    }
}
