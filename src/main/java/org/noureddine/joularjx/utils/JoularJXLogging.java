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

package org.noureddine.joularjx.utils;

import org.noureddine.joularjx.Agent;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JoularJXLogging {
    private static JoularJXLogging instance;
    private final Logger jxlogger;

    private JoularJXLogging(Level loggerLevel) {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new JoularJXFormatter());
        ch.setLevel(loggerLevel);
        this.jxlogger = Logger.getAnonymousLogger();
        this.jxlogger.addHandler(ch);
        this.jxlogger.setLevel(Level.CONFIG);
        this.jxlogger.setUseParentHandlers(false);
    }

    public Logger getLogger() {
        return this.jxlogger;
    }

    public static JoularJXLogging getInstance(Level loggerLevel) {
        JoularJXLogging result = instance;
        if (result != null) {
            return result;
        }
        synchronized(JoularJXLogging.class) {
            if (instance == null) {
                instance = new JoularJXLogging(loggerLevel);
            }
            return instance;
        }
    }
}