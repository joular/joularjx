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

    private static final String ALL_METHODS_EVOLUTION_FOLDER_NAME = "all";
    private static final String FILTERED_METHODS_EVOLUTION_FOLDER_NAME = "filtered";

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

    /**
     * Writes each method's consumption evolution into a separate CSV file.
     * @param consumptionEvolution a Map mapping each method name to another Map mapping Unix timestamps to energy consumption.
     * @param scope the scope of the given methods (all or filtered). Used to know in which folder to write the data.
     * @throws IOException if an error occurs while creating the folders or writing the data
     */
    private void writeConsumptionEvolution(Map<String, Map<Long, Double>> consumptionEvolution, Scope scope) throws IOException{
        String[] foldersToCreate = {this.properties.getEvolutionDataPath()+"/"+ALL_METHODS_EVOLUTION_FOLDER_NAME,
                                   this.properties.getEvolutionDataPath()+"/"+FILTERED_METHODS_EVOLUTION_FOLDER_NAME};

        //Creating the required folders to store consumption evolution files, if they do not already exists
        for (String dirName : foldersToCreate) {
            File dir = new File(dirName);
            if(!dir.exists() && !dir.mkdirs()){
                logger.log(Level.SEVERE, String.format("Cannot create %s folder. Methods consumption evolution cannot be reported.", dirName));
                return;
            }
        }

        //Selecting the correct target folder regarding the scope
        String targetFolderName;
        if (scope == Scope.ALL) {
            targetFolderName = foldersToCreate[0]; //All methods
        } else {
            targetFolderName = foldersToCreate[1]; //Filtered methods
        }

        //Writing a file per method
        for(var entry : consumptionEvolution.entrySet()){
            String fileName = String.format("%s/joularJX-%d-%s-evolution", targetFolderName, appPid, entry.getKey().replace('<', '_').replace('>', '_')); //replacing special chars

            resultWriter.setTarget(fileName, false);

            for(var methodEntry : entry.getValue().entrySet()){
                resultWriter.write(methodEntry.getKey().toString(), methodEntry.getValue());
            }
        }
    }
}
