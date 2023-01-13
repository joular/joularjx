package org.noureddine.joularjx.monitor;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.noureddine.joularjx.cpu.Cpu;
import org.noureddine.joularjx.result.ResultWriter;
import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;
import org.noureddine.joularjx.utils.Scope;

public class ShutdownHandler implements Runnable {

    private static final Logger logger = JoularJXLogging.getLogger();

    private final long appPid;
    private final ResultWriter resultWriter;
    private final Cpu cpu;
    private final MonitoringStatus status;
    private final AgentProperties properties;

    public ShutdownHandler(long appPid, ResultWriter resultWriter, Cpu cpu, MonitoringStatus status, AgentProperties properties) {
        this.appPid = appPid;
        this.resultWriter = resultWriter;
        this.cpu = cpu;
        this.status = status;
        this.properties = properties;
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
            if (this.properties.trackConsumptionEvolution()) {
                writeConsumptionEvolution(status.getMethodsConsumptionEvolution(), Scope.ALL);
                writeConsumptionEvolution(status.getFilteredMethodsConsumptionEvolution(), Scope.FILTERED);
            }
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

    private void writeConsumptionEvolution(Map<String, Map<Long, Double>> consumptionEvolution, Scope scope) throws IOException{
        String[] folderToCreate = {"evolution", "evolution/all", "evolution/filtered"};

        //Creating the required folders to store consumption evolution files, if they do not already exists
        for (String dirName : folderToCreate) {
            File dir = new File(dirName);
            if(!dir.exists() && !dir.mkdir()){
                logger.log(Level.SEVERE, String.format("Cannot create %s folder. Methods consumption evolution cannot be reported.", dirName));
                return;
            }
        }

        String targetFolderName;
        if (scope == Scope.ALL) {
            targetFolderName = "evolution/all";
        } else {
            targetFolderName = "evolution/filtered";
        }

        for(var entry : consumptionEvolution.entrySet()){
            String fileName = String.format("%s/joularJX-%d-%s-evolution", targetFolderName, appPid, entry.getKey().replace('<', '_').replace('>', '_'));

            resultWriter.setTarget(fileName, false);

            for(var methodEntry : entry.getValue().entrySet()){
                resultWriter.write(methodEntry.getKey().toString(), methodEntry.getValue());
            }
        }
        logger.log(Level.INFO, "Methods energy consumption evolution written to files.");
    }
}
