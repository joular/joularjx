/*
 * Copyright (c) 2021-2026, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Author : Adel Noureddine
 */

package org.noureddine.joularjx.cpu;

/**
 * Abstraction for CPU power measurement implementations.
 */
public interface Cpu extends AutoCloseable {

    /**
     * Initializes the CPU monitor implementation.
     */
    void initialize();

    /**
     * Returns the initial power reading for this CPU monitor.
     *
     * @return the initial power reading
     */
    double getInitialPower();

    /**
     * Returns the current power reading for the given CPU load.
     *
     * @param cpuLoad the current CPU load (0..1 or implementation-specific)
     * @return the current power reading
     */
    double getCurrentPower(double cpuLoad);

    /**
     * Returns the max power reading for the given CPU load when supported.
     *
     * @param cpuLoad the current CPU load (0..1 or implementation-specific)
     * @return the max power reading
     */
    double getMaxPower(double cpuLoad);
}
