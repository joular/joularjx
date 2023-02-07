/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.monitor;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.noureddine.joularjx.cpu.Cpu;
import org.noureddine.joularjx.result.ResultTreeManager;
import org.noureddine.joularjx.result.ResultWriter;
import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;

/**
 * The ShutdownHandler is meant to be called at the end of the agent and is responsible for displaying and writing all the consumption data in dedicated files.
 * It also performs resource closing operations.
 */
public class ShutdownHandler implements Runnable {

    private static final Logger logger = JoularJXLogging.getLogger();

    private final long appPid;
    private final ResultWriter resultWriter;
    private final Cpu cpu;
    private final MonitoringStatus status;
    private final AgentProperties properties;
    private final ResultTreeManager resultTreeManager;

    /**
     * Creates a new ShutdownHandler.
     * @param appPid the PID of the monitored application 
     * @param resultWriter the writer that will be used to save data in files
     * @param cpu an implementation of the CPU interface, depending on the OS and hardware
     * @param status where all the runtime data will be saved
     * @param properties the agent's configuration properties
     * @param resultTreeManager the ResultTreeManager used to provide correct filepaths for the generated files
     */
    public ShutdownHandler(long appPid, ResultWriter resultWriter, Cpu cpu, MonitoringStatus status, AgentProperties properties, ResultTreeManager resultTreeManager) {
        this.appPid = appPid;
        this.resultWriter = resultWriter;
        this.cpu = cpu;
        this.status = status;
        this.properties = properties;
        this.resultTreeManager = resultTreeManager;
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
            //Writing methods and filtered methods energy consumption
            this.saveResults(status.getMethodsConsumedEnergy(), this.resultTreeManager.getAllTotalMethodsPath()+String.format("/joularJX-%d-all-methods-energy", appPid));
            this.saveResults(status.getFilteredMethodsConsumedEnergy(), this.resultTreeManager.getFilteredTotalMethodsPath()+String.format("/joularJX-%d-filtered-methods-energy", appPid));
            
            //Writing consumption evolution files only if the option is enabled
            if (this.properties.trackConsumptionEvolution()) {
                //All methods
                for (var methodEntry : this.status.getMethodsConsumptionEvolution().entrySet()) {
                    this.saveResults(methodEntry.getValue(), this.resultTreeManager.getAllEvolutionPath()+String.format("/joularJX-%d-%s-evolution", appPid, methodEntry.getKey().replace('<', '_').replace('>', '_')));
                }
                
                //Filtered methods
                for (var methodEntry : this.status.getFilteredMethodsConsumptionEvolution().entrySet()) {
                    this.saveResults(methodEntry.getValue(), this.resultTreeManager.getFilteredEvolutionPath()+String.format("/joularJX-%d-%s-evolution", appPid, methodEntry.getKey().replace('<', '_').replace('>', '_')));
                }
            }

            //Writing call trees consumption file only if the option is enabled
            if (this.properties.callTreesConsumption()) {
                this.saveResults(status.getCallTreesConsumedEnergy(), this.resultTreeManager.getAllTotalCallTreePath()+String.format("/joularJX-%d-all-call-trees-energy", appPid));
                this.saveResults(status.getFilteredCallTreesConsumedEnergy(), this.resultTreeManager.getFilteredTotalCallTreePath()+String.format("/joularJX-%d-filtered-call-trees-energy", appPid));
            }
        } catch (IOException exception) {
            // Continue shutting down
        }

        logger.log(Level.INFO, "Energy consumption of methods and filtered methods written to files");
    }

    /**
    * Writes the results in a file. The filename is partially defined by the given parameters.
     * @param <K> The type of key that will be written in the file. Must implement the toString() method.*
     * @param consumedEnergyMap the data to be written.
     * @param filePath the path of the file where the data will be written.
     * @throws IOException if an I/O error occurs while writing the data.
     */
    public <K> void saveResults(Map<K, Double> consumedEnergyMap, String filePath) throws IOException {
        //String fileName = String.format("joularJX-%d-%s-%s", appPid, nodeType, dataType);
        resultWriter.setTarget(filePath, true);

        for (var entry : consumedEnergyMap.entrySet()) {
            resultWriter.write(entry.getKey().toString(), entry.getValue());
        }

        resultWriter.closeTarget();
    }
}
