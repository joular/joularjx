/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.result;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class CsvResultWriter implements ResultWriter {

    private static final OpenOption[] APPEND_OPEN_OPTIONS =
            new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND};
    private static final OpenOption[] OVERWRITE_OPEN_OPTIONS
            = new OpenOption[] {StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING};

    private final ThreadLocal<BufferedWriter> writer;

    public CsvResultWriter() {
        this.writer = new ThreadLocal<>();
    }

    @Override
    public void setTarget(String name, boolean overwrite) throws IOException {
        BufferedWriter previousWriter = writer.get();
        if (previousWriter != null) {
            previousWriter.close();
        }

        writer.set(Files.newBufferedWriter(getPath(name), overwrite ? OVERWRITE_OPEN_OPTIONS : APPEND_OPEN_OPTIONS));
    }

    @Override
    public void write(String methodName, double methodPower) throws IOException {
        BufferedWriter writer = this.writer.get();
        if (writer == null) {
            throw new IllegalStateException("Please call ResultWriter#setTarget(String) first");
        }

        writer.write(String.format(Locale.US, "%s,%.4f%n", methodName, methodPower));
    }

    @Override
    public void closeTarget() throws IOException {
        writer.get().close();
        writer.remove();
    }

    private Path getPath(String name) {
        return Path.of(name + ".csv");
    }
}
