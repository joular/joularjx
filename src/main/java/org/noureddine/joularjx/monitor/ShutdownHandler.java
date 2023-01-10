package org.noureddine.joularjx.monitor;

import org.noureddine.joularjx.cpu.Cpu;
import org.noureddine.joularjx.result.ResultWriter;
import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ShutdownHandler implements Runnable {

    private static final Logger logger = JoularJXLogging.getLogger();

    private final long appPid;
    private final ResultWriter resultWriter;
    private final Cpu cpu;
    private final MonitoringStatus status;

    public ShutdownHandler(long appPid, ResultWriter resultWriter, Cpu cpu, MonitoringStatus status) {
        this.appPid = appPid;
        this.resultWriter = resultWriter;
        this.cpu = cpu;
        this.status = status;
    }

    @Override
    public void run() {
        // Close monitoring implementation to release all resources
        try {
            cpu.close();
        } catch (Exception exception) {
            // Continue shutting down
        }

        logger.log(Level.INFO, String.format("JoularJX finished monitoring application with ID %d", appPid));
        logger.log(Level.INFO, "Program consumed {0,number,#.##} joules", status.getTotalConsumedEnergy());

        try {
            shareResults("all", status.getMethodsConsumedEnergy());
            shareResults("filtered", status.getFilteredMethodsConsumedEnergy());
        } catch (IOException exception) {
            // Continue shutting down
        }

        logger.log(Level.INFO, "Energy consumption of methods and filtered methods written to files");
    }

    private void shareResults(String modeName, Map<String, Double> methodsConsumedEnergy) throws IOException {
        String fileName = String.format("joularJX-%d-%s-methods-energy", appPid, modeName);

        resultWriter.setTarget(fileName, false);

        for (var entry : methodsConsumedEnergy.entrySet()) {
            resultWriter.write(entry.getKey(), entry.getValue());
        }

        resultWriter.closeTarget();
    }
}
