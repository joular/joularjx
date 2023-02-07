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

import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RaplLinux implements Cpu {

    private static final Logger logger = JoularJXLogging.getLogger();

    static final String RAPL_PSYS = "/sys/class/powercap/intel-rapl/intel-rapl:1/energy_uj";

    static final String RAPL_PKG = "/sys/class/powercap/intel-rapl/intel-rapl:0/energy_uj";

    static final String RAPL_DRAM = "/sys/class/powercap/intel-rapl/intel-rapl:0/intel-rapl:0:2/energy_uj";

    /**
     * RAPL files existing on the current system. All files in this list will be used for reading the
     * energy values.
     */
    private final List<Path> raplFilesToRead = new ArrayList<>(3);

    /**
     * Filesystem where the RAPL files are located.
     */
    private final FileSystem fileSystem;

    /**
     * Create a new energy measurement via RAPL. The files will be read from the default filesystem.
     */
    public RaplLinux() {
        this(FileSystems.getDefault());
    }

    /**
     * Create a new energy measurement via RAPL. The files will be read from the passed filesystem.
     *
     * @param fileSystem The filesystem to use for reading the RAPL files
     */
    RaplLinux(final FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    /**
     * Check which RAPL files are available on the system to read the energy values from.
     */
    @Override
    public void initialize() {
        final Path psysFile = fileSystem.getPath(RAPL_PSYS);
        if (Files.exists(psysFile)) {
            checkFileReadable(psysFile);
            // psys exists, so use this for energy readings
            raplFilesToRead.add(psysFile);
        } else {
            // No psys supported, then check for pkg and dram
            final Path pkgFile = fileSystem.getPath(RAPL_PKG);
            if (Files.exists(pkgFile)) {
                checkFileReadable(pkgFile);
                // pkg exists, check also for dram
                raplFilesToRead.add(pkgFile);

                final Path dramFile = fileSystem.getPath(RAPL_DRAM);
                if (Files.exists(dramFile)) {
                    checkFileReadable(dramFile);
                    // dram and pkg exists, then get sum of both
                    raplFilesToRead.add(dramFile);
                }
            }
        }

        if (raplFilesToRead.isEmpty()) {
            logger.log(Level.SEVERE, "Found no RAPL files to read the energy measurement from. Exit ...");
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
            logger.log(Level.SEVERE, "Failed to get RAPL energy readings. Did you run JoularJX with elevated privileges (sudo)?");
            System.exit(1);
        }
    }

    /**
     * Get energy readings from RAPL through powercap
     * Calculates the best energy reading as supported by CPU (psys, or pkg+dram, or pkg)
     * @return Energy readings from RAPL
     */
    @Override
    public double getCurrentPower(final double cpuLoad) {
        double energyData = 0.0;

        for (final Path raplFile : raplFilesToRead) {
            try {
                energyData += Double.parseDouble(Files.readString(raplFile));
            } catch (IOException exception) {
                logger.throwing(getClass().getName(), "getCurrentPower", exception);
            }
        }

        // Divide by 1 million to convert microJoules to Joules
        return energyData / 1000000;
    }

    /**
     * Returns the
     *
     * @return Energy readings from RAPL
     */
    @Override
    public double getInitialPower() {
        return getCurrentPower(0);
    }

    @Override
    public void close() {
        // Nothing to do for RAPL Linux
    }
}