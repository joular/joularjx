package org.noureddine.joularjx.result;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

        writer.write(String.format("%s,%.4f%n", methodName, methodPower));
    }

    private Path getPath(String name) {
        return Path.of(name + ".csv");
    }
}
