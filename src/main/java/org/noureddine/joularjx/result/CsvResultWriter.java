package org.noureddine.joularjx.result;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CsvResultWriter implements ResultWriter {

    private final long appPid;
    private final ThreadLocal<BufferedWriter> writer;

    public CsvResultWriter(long appPid) {
        this.appPid = appPid;

        this.writer = new ThreadLocal<>();
    }

    @Override
    public void setTarget(String name) throws IOException {
        BufferedWriter previousWriter = writer.get();
        if (previousWriter != null) {
            previousWriter.close();
        }

        writer.set(Files.newBufferedWriter(getPath(name)));
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
        return Path.of(String.format("joularJX-%d-method-%s.csv", appPid, name));
    }
}
