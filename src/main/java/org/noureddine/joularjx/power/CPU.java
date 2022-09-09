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

public interface CPU {
    /**
     * Start our power monitoring program on Windows
     * Only used for Intel Windows
     * Will return null for other operating systems and platforms
     * @param programPath Path for our power monitor program
     * @return Process to run power monitor on Windows, null for other OS
     */
    public Process startPowerMonitoring(String programPath);
}
