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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class JoularJXFormatter extends Formatter {
    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

    @Override
    public String format(LogRecord logRecord) {
        StringBuilder builder = new StringBuilder(1000);
        builder.append(dateFormat.format(new Date(logRecord.getMillis()))).append(" - ");
        builder.append("[").append(logRecord.getLevel()).append("] - ");
        builder.append(formatMessage(logRecord));
        builder.append("\n");

        if (logRecord.getThrown() != null) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            logRecord.getThrown().printStackTrace(printWriter);
            builder.append(stringWriter);
        }

        return builder.toString();
    }
}