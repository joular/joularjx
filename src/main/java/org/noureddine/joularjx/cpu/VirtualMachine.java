/*
 * Copyright (c) 2021-2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
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
import java.util.logging.Logger;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.FileSystem;
import java.util.logging.Level;

public class VirtualMachine implements Cpu {
    
    private static final Logger logger = JoularJXLogging.getLogger();

    private static String VM_POWER_PATH_NAME;

    private static String VM_POWER_FORMAT;

    private Path VM_POWER_PATH;

    private final FileSystem fileSystem;

    public VirtualMachine(String VMPowerPath, String VMPowerFormat) {
        this(FileSystems.getDefault());
        VM_POWER_PATH_NAME = VMPowerPath;
        VM_POWER_FORMAT = VMPowerFormat;
    }

    public VirtualMachine(final FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public void initialize() {
        // Check if VM_POWER_PATH exists and can be read
        this.VM_POWER_PATH = fileSystem.getPath(VM_POWER_PATH_NAME);

        if (Files.exists(this.VM_POWER_PATH)) {
            checkFileReadable(this.VM_POWER_PATH);
        } else {
            logger.log(Level.SEVERE, "The shared VM power file cannot be found. Exiting...");
            System.exit(1);
        }
    }

    /**
     * Check that the passed file can be read by the program. Log error message and exit if reading the file is not
     * possible.
     * @param file the file to check the read access
     */
    private void checkFileReadable(final Path file) {
        if (!Files.isReadable(file)) {
            logger.log(Level.SEVERE, "Failed to read the shared VM power file. Please check you have permissions to read it.");
            System.exit(1);
        }
    }

    /**
     * The power is approximated based on the CPU load, so it does not need an offset.
     *
     * @return 0
     */
    @Override
    public double getInitialPower() {
        return 0;
    }

    @Override
    public double getCurrentPower(double cpuLoad) {
        double powerData = 0.0;


        try {
            if (VM_POWER_FORMAT.equals("watts")) {
                powerData = Double.parseDouble(Files.readString(VM_POWER_PATH));
            } else if (VM_POWER_FORMAT.equals("powerjoular")) {
                String[] powerDataInfo = Files.readString(VM_POWER_PATH).split(",");
                // Get 3rd column (index 2) for power consumption
                powerData = Double.parseDouble(powerDataInfo[2]);
            } else {
                logger.log(Level.WARNING, "Power data format for VM not supported. Returning 0.");
            }
        } catch (IOException exception) {
            logger.throwing(getClass().getName(), "getCurrentPower", exception);
        }

        return powerData;
    }
    
    /**
     * Nothing to do here. Method only useful for RAPL
     */
    @Override
    public double getMaxPower(double cpuLoad) {
        return 0;
    }
    
    @Override
    public void close() {
        // Nothing to do for virtual machines
    }
    
}