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

public class RaspberryPi implements Cpu {

    /**
     * Raspberry Pi model name
     */
    private final RaspberryPiModels rpiModel;

    public RaspberryPi(final RaspberryPiModels rpiModel) {
        this.rpiModel = rpiModel;
    }

    /**
     * Calculate CPU energy consumption for last second (power) on supported Raspberry Pi devices
     * @param rpiModel Raspberry Pi model
     * @param cpuUsage CPU usage
     * @return CPU energy consumption for last second (power)
     */
    private double calculateCpuEnergyForRaspberryPi(final RaspberryPiModels rpiModel, final double cpuUsage) {
        double result = 0.0;

        switch (rpiModel) {
            case RPI_400_10_64:
                result = 2.6630056198236938 + (0.82814554 * cpuUsage) +
                        (-112.17687631 * Math.pow(cpuUsage, 2)) +
                        (1753.99173239 * Math.pow(cpuUsage, 3)) +
                        (-10992.65341181 * Math.pow(cpuUsage, 4)) +
                        (35988.45610911 * Math.pow(cpuUsage, 5)) +
                        (-66254.20051068 * Math.pow(cpuUsage, 6)) +
                        (69071.21138567 * Math.pow(cpuUsage, 7)) +
                        (-38089.87171735 * Math.pow(cpuUsage, 8)) +
                        (8638.45610698 * Math.pow(cpuUsage, 9));
                break;
            case RPI_4B_12_64:
                result = 3.039940056604439 + (-3.074225 * cpuUsage) +
                        (47.753114 * Math.pow(cpuUsage, 2)) +
                        (-271.974551 * Math.pow(cpuUsage, 3)) +
                        (879.966571 * Math.pow(cpuUsage, 4)) +
                        (-1437.466442 * Math.pow(cpuUsage, 5)) +
                        (1133.325791 * Math.pow(cpuUsage, 6)) +
                        (-345.134888 * Math.pow(cpuUsage, 7));
                break;
            case RPI_4B_12:
                result = 2.58542069543335 + (12.335449 * cpuUsage) +
                        (-248.010554 * Math.pow(cpuUsage, 2)) +
                        (2379.832320 * Math.pow(cpuUsage, 3)) +
                        (-11962.419149 * Math.pow(cpuUsage, 4)) +
                        (34444.268647 * Math.pow(cpuUsage, 5)) +
                        (-58455.266502 * Math.pow(cpuUsage, 6)) +
                        (57698.685016 * Math.pow(cpuUsage, 7)) +
                        (-30618.557703 * Math.pow(cpuUsage, 8)) +
                        (6752.265368 * Math.pow(cpuUsage, 9));
                break;
            case RPI_4B_11_64:
                result = 3.405685008777926 + (-11.834416 * cpuUsage) +
                        (137.312822 * Math.pow(cpuUsage, 2)) +
                        (-775.891511 * Math.pow(cpuUsage, 3)) +
                        (2563.399671 * Math.pow(cpuUsage, 4)) +
                        (-4783.024354 * Math.pow(cpuUsage, 5)) +
                        (4974.960753 * Math.pow(cpuUsage, 6)) +
                        (-2691.923074 * Math.pow(cpuUsage, 7)) +
                        (590.355251 * Math.pow(cpuUsage, 8));
                break;
            case RPI_4B_11:
                result = 2.5718068562852086 + (2.794871 * cpuUsage) +
                        (-58.954883 * Math.pow(cpuUsage, 2)) +
                        (838.875781 * Math.pow(cpuUsage, 3)) +
                        (-5371.428686 * Math.pow(cpuUsage, 4)) +
                        (18168.842874 * Math.pow(cpuUsage, 5)) +
                        (-34369.583554 * Math.pow(cpuUsage, 6)) +
                        (36585.681749 * Math.pow(cpuUsage, 7)) +
                        (-20501.307640 * Math.pow(cpuUsage, 8)) +
                        (4708.331490 * Math.pow(cpuUsage, 9));
                break;
            case RPI_3BP_13:
                result = 2.484396997449118 + (2.933542 * cpuUsage) +
                        (-150.400134 * Math.pow(cpuUsage, 2)) +
                        (2278.690310 * Math.pow(cpuUsage, 3)) +
                        (-15008.559279 * Math.pow(cpuUsage, 4)) +
                        (51537.315529 * Math.pow(cpuUsage, 5)) +
                        (-98756.887779 * Math.pow(cpuUsage, 6)) +
                        (106478.929766 * Math.pow(cpuUsage, 7)) +
                        (-60432.910139 * Math.pow(cpuUsage, 8)) +
                        (14053.677709 * Math.pow(cpuUsage, 9));
                break;
            case RPI_3B_12:
                result = 1.524116907651687 + (10.053851 * cpuUsage) +
                        (-234.186930 * Math.pow(cpuUsage, 2)) +
                        (2516.322119 * Math.pow(cpuUsage, 3)) +
                        (-13733.555536 * Math.pow(cpuUsage, 4)) +
                        (41739.918887 * Math.pow(cpuUsage, 5)) +
                        (-73342.794259 * Math.pow(cpuUsage, 6)) +
                        (74062.644914 * Math.pow(cpuUsage, 7)) +
                        (-39909.425362 * Math.pow(cpuUsage, 8)) +
                        (8894.110508 * Math.pow(cpuUsage, 9));
                break;
            case RPI_2B_11:
                result = 1.3596870187778196 + (5.135090 * cpuUsage) +
                        (-103.296366 * Math.pow(cpuUsage, 2)) +
                        (1027.169748 * Math.pow(cpuUsage, 3)) +
                        (-5323.639404 * Math.pow(cpuUsage, 4)) +
                        (15592.036875 * Math.pow(cpuUsage, 5)) +
                        (-26675.601585 * Math.pow(cpuUsage, 6)) +
                        (26412.963366 * Math.pow(cpuUsage, 7)) +
                        (-14023.471809 * Math.pow(cpuUsage, 8)) +
                        (3089.786200 * Math.pow(cpuUsage, 9));
                break;
            case RPI_1BP_12:
                result = 1.2513999338064061 + (1.857815 * cpuUsage) +
                        (-18.109537 * Math.pow(cpuUsage, 2)) +
                        (101.531231 * Math.pow(cpuUsage, 3)) +
                        (-346.386617 * Math.pow(cpuUsage, 4)) +
                        (749.560352 * Math.pow(cpuUsage, 5)) +
                        (-1028.802514 * Math.pow(cpuUsage, 6)) +
                        (863.877618 * Math.pow(cpuUsage, 7)) +
                        (-403.270951 * Math.pow(cpuUsage, 8)) +
                        (79.925932 * Math.pow(cpuUsage, 9));
                break;
            case RPI_1B_2:
                result = 2.826093843916506 + (3.539891 * cpuUsage) +
                        (-43.586963 * Math.pow(cpuUsage, 2)) +
                        (282.488560 * Math.pow(cpuUsage, 3)) +
                        (-1074.116844 * Math.pow(cpuUsage, 4)) +
                        (2537.679443 * Math.pow(cpuUsage, 5)) +
                        (-3761.784242 * Math.pow(cpuUsage, 6)) +
                        (3391.045904 * Math.pow(cpuUsage, 7)) +
                        (-1692.840870 * Math.pow(cpuUsage, 8)) +
                        (357.800968 * Math.pow(cpuUsage, 9));
                break;
            case RPI_ZW_11:
                result = 0.8551610676717238 + (7.207151 * cpuUsage) +
                        (-135.517893 * Math.pow(cpuUsage, 2)) +
                        (1254.808001 * Math.pow(cpuUsage, 3)) +
                        (-6329.450524 * Math.pow(cpuUsage, 4)) +
                        (18502.371291 * Math.pow(cpuUsage, 5)) +
                        (-32098.028941 * Math.pow(cpuUsage, 6)) +
                        (32554.679890 * Math.pow(cpuUsage, 7)) +
                        (-17824.350159 * Math.pow(cpuUsage, 8)) +
                        (4069.178175 * Math.pow(cpuUsage, 9));
                break;
            case ASUSTBS:
                result = 3.9146162374630173 + (-19.85430796 * cpuUsage) +
                        (141.7306532 * Math.pow(cpuUsage, 2)) +
                        (-298.12713091 * Math.pow(cpuUsage, 3)) +
                        (-1115.76983141 * Math.pow(cpuUsage, 4)) +
                        (8238.275731321 * Math.pow(cpuUsage, 5)) +
                        (-20976.13898406 * Math.pow(cpuUsage, 6)) +
                        (27132.90930519 * Math.pow(cpuUsage, 7)) +
                        (-17741.01303757 * Math.pow(cpuUsage, 8)) +
                        (4640.69530931 * Math.pow(cpuUsage, 9));
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
    public double getCurrentPower(final double cpuLoad) {
        return calculateCpuEnergyForRaspberryPi(rpiModel, cpuLoad);
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
    public void close() {
        // Nothing to do for Raspberry Pi
    }
}