package org.noureddine.joularjx.monitor;

import org.noureddine.joularjx.Agent;
import org.noureddine.joularjx.cpu.Cpu;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Map;
import java.util.logging.Level;

public class ShutdownHandler implements Runnable {

    private final long appPid;
    private final Cpu cpu;
    private final MonitoringStatus status;

    public ShutdownHandler(long appPid, Cpu cpu, MonitoringStatus status) {
        this.appPid = appPid;
        this.cpu = cpu;
        this.status = status;
    }

    @Override
    public void run() {
        // Close monitoring implementation to release all resources
        try {
            cpu.close();
        } catch (Exception e) {}

        Agent.jxlogger.log(Level.INFO, "JoularJX finished monitoring application with ID {0}", appPid);
        Agent.jxlogger.log(Level.INFO, "Program consumed {0,number,#.##} joules", status.getTotalConsumedEnergy());

        // Prepare buffer for methods energy
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, Double> entry : status.getMethodsConsumedEnergy().entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            buf.append(key).append(',').append(value).append("\n");
        }

        // Write to CSV file
        String fileNameMethods = "joularJX-" + appPid + "-methods-energy.csv";
        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethods, true))) {
            out.write(buf.toString());
        } catch (Exception ignored) {}

        // Prepare buffer for filtered methods energy
        StringBuilder bufFil = new StringBuilder();
        for (Map.Entry<String, Double> entry : status.getFilteredMethodsConsumedEnergy().entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            bufFil.append(key).append(',').append(value).append("\n");
        }

        // Write to CSV file for filtered methods
        String fileNameMethodsFiltered = "joularJX-" + appPid + "-methods-energy-filtered.csv";
        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileNameMethodsFiltered, true))) {
            out.write(bufFil.toString());
        } catch (Exception ignored) {}

        Agent.jxlogger.log(Level.INFO, "Energy consumption of methods and filtered methods written to {0} and {1} files",
                new Object[]{fileNameMethods, fileNameMethodsFiltered});
    }
}
