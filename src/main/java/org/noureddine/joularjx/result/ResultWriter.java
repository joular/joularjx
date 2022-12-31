package org.noureddine.joularjx.result;

import java.io.IOException;

public interface ResultWriter {

    void setTarget(String name, boolean overwrite) throws IOException;

    void write(String methodName, double methodPower) throws IOException;
}
