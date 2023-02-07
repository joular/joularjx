/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class for the {@link Cpu} implementation
 */
public class CpuFactory {

    private static final Logger logger = JoularJXLogging.getLogger();

    /**
     * Select the supported {@link Cpu} implementation
     * @param properties the properties passed to the agent
     * @return the selected CPU implementation
     */
    public static Cpu getCpu(final AgentProperties properties) {
        // Get OS
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();

        if (osName.contains("win")) {
            return new IntelWindows(properties.getPowerMonitorPath());
        }

        if (!osName.contains("linux")) {
            logger.log(Level.SEVERE, "Platform not supported. Exiting...");
            System.exit(1);
        }

        if (osArch.contains("aarch64") || osArch.contains("arm")) {
            return new RaspberryPi(getRPiModelName(osArch).orElseGet(() -> {
                logger.log(Level.SEVERE, "Platform not supported. Exiting...");
                System.exit(1);
                return null;
            }));
        }

        // Suppose it's x86/64, check for powercap RAPL
        try {
            String raplFolderPath = "/sys/class/powercap/intel-rapl/intel-rapl:0";
            File raplFolder = new File(raplFolderPath);
            if (raplFolder.exists()) {
                // Rapl is supported
                return new RaplLinux();
            } else {
                // If no RAPL, then no support
                logger.log(Level.SEVERE, "Platform not supported. Exiting...");
                System.exit(1);
            }
        } catch (Exception e) {
            // If no RAPL, then no support
            logger.log(Level.SEVERE, "Platform not supported. Exiting...");
            System.exit(1);
        }

        // Should never reach here because we stop the agent. But the compiler needs the return to compile the code.
        return null;
    }

    /**
     * Get model name of Raspberry Pi
     * @param osArch OS Architecture (arm, aarch64)
     * @return Raspberry Pi model name
     */
    private static Optional<RaspberryPiModels> getRPiModelName(String osArch) {
        String deviceTreeModel = "/proc/device-tree/model";
        File deviceTreeModelFile = new File(deviceTreeModel);
        if (!deviceTreeModelFile.exists()) {
            return Optional.empty();
        }

        Path procstatPath = Path.of(deviceTreeModel);
        try {
            // Read only first line of stat file
            // We need to read values at index 1, 2, 3 and 4 (assuming index starts at 0)
            // Example of line: cpu  83141 56 28074 2909632 3452 10196 3416 0 0 0
            // Split the first line over spaces to get each column
            List<String> allLines = Files.readAllLines(procstatPath);
            for (String currentLine : allLines) {
                if (currentLine.contains("Raspberry Pi 400 Rev 1.0")) {
                    if (osArch.contains("aarch64")) {
                        return Optional.of(RaspberryPiModels.RPI_400_10_64);
                    }
                }
                if (currentLine.contains("Raspberry Pi 4 Model B Rev 1.2")) {
                    if (osArch.contains("aarch64")) {
                        return Optional.of(RaspberryPiModels.RPI_4B_12_64);
                    } else {
                        return Optional.of(RaspberryPiModels.RPI_4B_12);
                    }
                } else if (currentLine.contains("Raspberry Pi 4 Model B Rev 1.1")) {
                    if (osArch.contains("aarch64")) {
                        return Optional.of(RaspberryPiModels.RPI_4B_11_64);
                    } else {
                        return Optional.of(RaspberryPiModels.RPI_4B_11);
                    }
                } else if (currentLine.contains("Raspberry Pi 3 Model B Plus Rev 1.3")) {
                    return Optional.of(RaspberryPiModels.RPI_3BP_13);
                } else if (currentLine.contains("Raspberry Pi 3 Model B Rev 1.2")) {
                    return Optional.of(RaspberryPiModels.RPI_3B_12);
                } else if (currentLine.contains("Raspberry Pi 2 Model B Rev 1.1")) {
                    return Optional.of(RaspberryPiModels.RPI_2B_11);
                } else if (currentLine.contains("Raspberry Pi Model B Plus Rev 1.2")) {
                    return Optional.of(RaspberryPiModels.RPI_1BP_12);
                } else if (currentLine.contains("Raspberry Pi Model B Rev 2")) {
                    return Optional.of(RaspberryPiModels.RPI_1B_2);
                } else if (currentLine.contains("Raspberry Pi Zero W Rev 1.1")) {
                    return Optional.of(RaspberryPiModels.RPI_ZW_11);
                } else if (currentLine.contains("ASUS Tinker Board (S)")) {
                    return Optional.of(RaspberryPiModels.ASUSTBS);
                }
            }
        } catch (IOException exception) {
            logger.throwing(CpuFactory.class.getName(), "getRPiModelName", exception);
        }

        return Optional.empty();
    }

    /**
     * Private constructor which hides the default one
     */
    private CpuFactory() {

    }
}
