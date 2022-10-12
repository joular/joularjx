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

public class RaspberryPi implements CPU {

    /**
     * Raspberry Pi model name
     */
    private final String rpiModel;

    public RaspberryPi(final String rpiModel) {
        this.rpiModel = rpiModel;
    }

    /**
     * Calculate CPU energy consumption for last second (power) on supported Raspberry Pi devices
     * @param rpiModel Raspberry Pi model name
     * @param CPUUsage CPU usage
     * @return CPU energy consumption for last second (power)
     */
    private static double calculateCPUEnergyForRaspberryPi(String rpiModel, double CPUUsage) {
        double result = 0.0;

        switch (rpiModel) {
            case "rbp4001.0-64":
                result = 2.6630056198236938 + (0.82814554 * CPUUsage) +
                        (-112.17687631 * Math.pow(CPUUsage, 2)) +
                        (1753.99173239 * Math.pow(CPUUsage, 3)) +
                        (-10992.65341181 * Math.pow(CPUUsage, 4)) +
                        (35988.45610911 * Math.pow(CPUUsage, 5)) +
                        (-66254.20051068 * Math.pow(CPUUsage, 6)) +
                        (69071.21138567 * Math.pow(CPUUsage, 7)) +
                        (-38089.87171735 * Math.pow(CPUUsage, 8)) +
                        (8638.45610698 * Math.pow(CPUUsage, 9));
                break;
            case "rbp4b1.2-64":
                result = 3.039940056604439 + (-3.074225 * CPUUsage) +
                        (47.753114 * Math.pow(CPUUsage, 2)) +
                        (-271.974551 * Math.pow(CPUUsage, 3)) +
                        (879.966571 * Math.pow(CPUUsage, 4)) +
                        (-1437.466442 * Math.pow(CPUUsage, 5)) +
                        (1133.325791 * Math.pow(CPUUsage, 6)) +
                        (-345.134888 * Math.pow(CPUUsage, 7));
                break;
            case "rbp4b1.2":
                result = 2.58542069543335 + (12.335449 * CPUUsage) +
                        (-248.010554 * Math.pow(CPUUsage, 2)) +
                        (2379.832320 * Math.pow(CPUUsage, 3)) +
                        (-11962.419149 * Math.pow(CPUUsage, 4)) +
                        (34444.268647 * Math.pow(CPUUsage, 5)) +
                        (-58455.266502 * Math.pow(CPUUsage, 6)) +
                        (57698.685016 * Math.pow(CPUUsage, 7)) +
                        (-30618.557703 * Math.pow(CPUUsage, 8)) +
                        (6752.265368 * Math.pow(CPUUsage, 9));
                break;
            case "rbp4b1.1-64":
                result = 3.405685008777926 + (-11.834416 * CPUUsage) +
                        (137.312822 * Math.pow(CPUUsage, 2)) +
                        (-775.891511 * Math.pow(CPUUsage, 3)) +
                        (2563.399671 * Math.pow(CPUUsage, 4)) +
                        (-4783.024354 * Math.pow(CPUUsage, 5)) +
                        (4974.960753 * Math.pow(CPUUsage, 6)) +
                        (-2691.923074 * Math.pow(CPUUsage, 7)) +
                        (590.355251 * Math.pow(CPUUsage, 8));
                break;
            case "rbp4b1.1":
                result = 2.5718068562852086 + (2.794871 * CPUUsage) +
                        (-58.954883 * Math.pow(CPUUsage, 2)) +
                        (838.875781 * Math.pow(CPUUsage, 3)) +
                        (-5371.428686 * Math.pow(CPUUsage, 4)) +
                        (18168.842874 * Math.pow(CPUUsage, 5)) +
                        (-34369.583554 * Math.pow(CPUUsage, 6)) +
                        (36585.681749 * Math.pow(CPUUsage, 7)) +
                        (-20501.307640 * Math.pow(CPUUsage, 8)) +
                        (4708.331490 * Math.pow(CPUUsage, 9));
                break;
            case "rbp3b+1.3":
                result = 2.484396997449118 + (2.933542 * CPUUsage) +
                        (-150.400134 * Math.pow(CPUUsage, 2)) +
                        (2278.690310 * Math.pow(CPUUsage, 3)) +
                        (-15008.559279 * Math.pow(CPUUsage, 4)) +
                        (51537.315529 * Math.pow(CPUUsage, 5)) +
                        (-98756.887779 * Math.pow(CPUUsage, 6)) +
                        (106478.929766 * Math.pow(CPUUsage, 7)) +
                        (-60432.910139 * Math.pow(CPUUsage, 8)) +
                        (14053.677709 * Math.pow(CPUUsage, 9));
                break;
            case "rbp3b1.2":
                result = 1.524116907651687 + (10.053851 * CPUUsage) +
                        (-234.186930 * Math.pow(CPUUsage, 2)) +
                        (2516.322119 * Math.pow(CPUUsage, 3)) +
                        (-13733.555536 * Math.pow(CPUUsage, 4)) +
                        (41739.918887 * Math.pow(CPUUsage, 5)) +
                        (-73342.794259 * Math.pow(CPUUsage, 6)) +
                        (74062.644914 * Math.pow(CPUUsage, 7)) +
                        (-39909.425362 * Math.pow(CPUUsage, 8)) +
                        (8894.110508 * Math.pow(CPUUsage, 9));
                break;
            case "rbp2b1.1":
                result = 1.3596870187778196 + (5.135090 * CPUUsage) +
                        (-103.296366 * Math.pow(CPUUsage, 2)) +
                        (1027.169748 * Math.pow(CPUUsage, 3)) +
                        (-5323.639404 * Math.pow(CPUUsage, 4)) +
                        (15592.036875 * Math.pow(CPUUsage, 5)) +
                        (-26675.601585 * Math.pow(CPUUsage, 6)) +
                        (26412.963366 * Math.pow(CPUUsage, 7)) +
                        (-14023.471809 * Math.pow(CPUUsage, 8)) +
                        (3089.786200 * Math.pow(CPUUsage, 9));
                break;
            case "rbp1b+1.2":
                result = 1.2513999338064061 + (1.857815 * CPUUsage) +
                        (-18.109537 * Math.pow(CPUUsage, 2)) +
                        (101.531231 * Math.pow(CPUUsage, 3)) +
                        (-346.386617 * Math.pow(CPUUsage, 4)) +
                        (749.560352 * Math.pow(CPUUsage, 5)) +
                        (-1028.802514 * Math.pow(CPUUsage, 6)) +
                        (863.877618 * Math.pow(CPUUsage, 7)) +
                        (-403.270951 * Math.pow(CPUUsage, 8)) +
                        (79.925932 * Math.pow(CPUUsage, 9));
                break;
            case "rbp1b2":
                result = 2.826093843916506 + (3.539891 * CPUUsage) +
                        (-43.586963 * Math.pow(CPUUsage, 2)) +
                        (282.488560 * Math.pow(CPUUsage, 3)) +
                        (-1074.116844 * Math.pow(CPUUsage, 4)) +
                        (2537.679443 * Math.pow(CPUUsage, 5)) +
                        (-3761.784242 * Math.pow(CPUUsage, 6)) +
                        (3391.045904 * Math.pow(CPUUsage, 7)) +
                        (-1692.840870 * Math.pow(CPUUsage, 8)) +
                        (357.800968 * Math.pow(CPUUsage, 9));
                break;
            case "rbpzw1.1":
                result = 0.8551610676717238 + (7.207151 * CPUUsage) +
                        (-135.517893 * Math.pow(CPUUsage, 2)) +
                        (1254.808001 * Math.pow(CPUUsage, 3)) +
                        (-6329.450524 * Math.pow(CPUUsage, 4)) +
                        (18502.371291 * Math.pow(CPUUsage, 5)) +
                        (-32098.028941 * Math.pow(CPUUsage, 6)) +
                        (32554.679890 * Math.pow(CPUUsage, 7)) +
                        (-17824.350159 * Math.pow(CPUUsage, 8)) +
                        (4069.178175 * Math.pow(CPUUsage, 9));
                break;
            default:
                break;
        }

        return result;
    }

    @Override
    public void initialize() {
        // Nothing to do for Raspberry Pi
    }

    @Override
    public double getPower(final double cpuLoad) {
        return calculateCPUEnergyForRaspberryPi(rpiModel, cpuLoad);
    }

    @Override
    public void close() {
        // Nothing to do for Raspberry Pi
    }
}