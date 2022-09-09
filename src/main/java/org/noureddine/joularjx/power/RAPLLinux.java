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

package org.noureddine.joularjx.power;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RAPLLinux implements CPU {
    /**
     * Get energy readings from RAPL through powercap
     * Calculates the best energy reading as supported by CPU (psys, or pkg+dram, or pkg)
     * @return Energy readings from RAPL
     */
    private static Double getRAPLEnergy() {
        String psys = "/sys/class/powercap/intel-rapl/intel-rapl:1/energy_uj";
        String pkg = "/sys/class/powercap/intel-rapl/intel-rapl:0/energy_uj";
        String dram = "/sys/class/powercap/intel-rapl/intel-rapl:0/intel-rapl:0:2/energy_uj";
        Double energyData = 0.0;

        try {
            File psysFile = new File(psys);
            if (psysFile.exists()) {
                // psys exists, so use this for energy readings
                Path psysPath = Path.of(psys);
                try {
                    energyData = Double.parseDouble(Files.readString(psysPath));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // No psys supported, then check for pkg and dram
                File pkgFile = new File(pkg);
                if (pkgFile.exists()) {
                    // pkg exists, check also for dram
                    Path pkgPath = Path.of(pkg);
                    try {
                        energyData = Double.parseDouble(Files.readString(pkgPath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    File dramFile = new File(dram);
                    if (dramFile.exists()) {
                        // dram and pkg exists, then get sum of both
                        Path dramPath = Path.of(dram);
                        try {
                            energyData += Double.parseDouble(Files.readString(dramPath));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Failed to get RAPL energy readings. Did you run JoularJX with elevated privileges (sudo)?");
            System.exit(1);
        }

        // Divide by 1 million to convert microJoules to Joules
        energyData = energyData / 1000000;
        return energyData;
    }

    /**
     * Calculate process energy consumption
     * @param totalCPUUsage Total CPU usage
     * @param processCPUUSage Process CPU usage
     * @param CPUEnergy CPU energy
     * @return Process energy consumption
     */
    private static double calculateProcessCPUEnergy(Double totalCPUUsage, Double processCPUUSage, Double CPUEnergy) {
        return (processCPUUSage * CPUEnergy) / totalCPUUsage;
    }

    @Override
    public Process startPowerMonitoring(String programPath) {
        // Nothing to do for RAPL Linux
        return null;
    }
}