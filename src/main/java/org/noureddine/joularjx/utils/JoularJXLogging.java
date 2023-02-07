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

package org.noureddine.joularjx.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JoularJXLogging {

    private static final JoularJXLogging instance = new JoularJXLogging();

    private final Logger logger;
    private final ConsoleHandler consoleHandler;

    public static void updateLevel(Level loggerLevel) {
        instance.logger.setLevel(loggerLevel);
        instance.consoleHandler.setLevel(loggerLevel);
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    private JoularJXLogging() {
        this.consoleHandler = new ConsoleHandler();
        this.consoleHandler.setFormatter(new JoularJXFormatter());
        this.logger = Logger.getAnonymousLogger();
        this.logger.addHandler(consoleHandler);
        this.logger.setLevel(Level.CONFIG);
        this.logger.setUseParentHandlers(false);
    }
}