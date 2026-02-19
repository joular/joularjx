/*
 * Copyright (c) 2021-2026, Adel Noureddine, Université de Pau et des Pays de l'Adour.
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
 * A {@link Cpu} implementation using powermetrics.
 */
public class PowermetricsMacOS implements Cpu {
    private static final Logger logger = JoularJXLogging.getLogger();
    private static final String POWER_INDICATOR_M_CHIP = " Power: ";
    private static final String POWER_INDICATOR_INTEL_CHIP = "Intel energy model derived package power (CPUs+GT+SA): ";
    private Process process;
    private BufferedReader reader;

    private boolean initialized;
    boolean intelCpu = false;

    private Thread readerThread;
    private volatile double currentPower = 0.0;

    /**
     * Creates a new powermetrics-based CPU monitor.
     */
    public PowermetricsMacOS() {
        super();
    }

    @Override
    public void initialize() {
        if (initialized) {
            return;
        }

        try {
            // todo: detect when sudo fails as this currently won't throw an exception
            process = new ProcessBuilder("sudo", "powermetrics", "--samplers", "cpu_power", "-i", "1000").start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            readHeader();
            startReaderThread();
            initialized = true;
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
            if (line != null && line.startsWith("EFI version")) {
                intelCpu = true;
            }
        }
    }

    /**
     * Processes the continuous output stream from the powermetrics tool.
     * It reads lines, identifies power blocks, and updates the current power
     * variable.
     * 
     * @param readerObj the reader to process lines from
     */
    protected void processStream(BufferedReader readerObj) {
        try {
            String line;
            double accumulatedPowerM = 0.0;
            double accumulatedPowerIntel = 0.0;
            boolean firstBlock = true;

            while ((line = readerObj.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                if (line.startsWith("*")) { // Start of a new sample block e.g., "*** Sampled system activity ***"
                    if (intelCpu) {
                        if (!firstBlock) {
                            currentPower = accumulatedPowerIntel;
                        }
                        accumulatedPowerIntel = 0.0;
                    } else {
                        if (!firstBlock) {
                            currentPower = accumulatedPowerM;
                        }
                        accumulatedPowerM = 0.0;
                    }
                    firstBlock = false;
                    continue;
                }

                if (intelCpu) {
                    final var i = line.indexOf(POWER_INDICATOR_INTEL_CHIP);
                    if (i >= 0) {
                        try {
                            accumulatedPowerIntel += Double.parseDouble(
                                    line.substring(i + POWER_INDICATOR_INTEL_CHIP.length(), line.indexOf('W')).trim());
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } else {
                    final var i = line.indexOf(POWER_INDICATOR_M_CHIP);
                    if (i >= 0 && '-' != line.charAt(1) && !line.startsWith("Combined")) {
                        try {
                            int powerInMilliwatts = Integer.parseInt(
                                    line.substring(i + POWER_INDICATOR_M_CHIP.length(), line.indexOf('m') - 1).trim());
                            accumulatedPowerM += (double) powerInMilliwatts / 1000.0;
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }

            // EOF flush
            if (intelCpu) {
                if (!firstBlock)
                    currentPower = accumulatedPowerIntel;
            } else {
                if (!firstBlock)
                    currentPower = accumulatedPowerM;
            }
        } catch (IOException exception) {
            logger.throwing(getClass().getName(), "processStream", exception);
        }
    }

    /**
     * Starts a background daemon thread that continuously reads the powermetrics
     * standard output.
     */
    protected void startReaderThread() {
        readerThread = new Thread(() -> processStream(getReader()));
        readerThread.setDaemon(true);
        readerThread.setName("PowermetricsMacOS-Reader");
        readerThread.start();
    }

    @Override
    public double getInitialPower() {
        return 0;
    }

    @Override
    public double getCurrentPower(double cpuLoad) {
        return currentPower;
    }

    /**
     * Override point for testing.
     * 
     * @return the reader getting outputs of the powermetrics tool
     */
    protected BufferedReader getReader() {
        return reader;
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
