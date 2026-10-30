// Features proposed by Rushin Dipak Makwana under supervision of Dr. Denis Pankratov and Dr. Yann-Gael Gueheneuc
/*
 * Copyright (c) 2021-2026, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.monitor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.noureddine.joularjx.cpu.Cpu;
import org.noureddine.joularjx.result.ResultScope;
import org.noureddine.joularjx.result.ResultWriter;
import org.noureddine.joularjx.result.ResultWriterConfiguration;
import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;

/**
 * The ShutdownHandler is meant to be called at the end of the agent and is
 * responsible for displaying and writing all the consumption data in dedicated
 * files. It also performs resource closing operations.
 */
public class ShutdownHandler implements Runnable {

	private static final Logger logger = JoularJXLogging.getLogger();

	private final long appPid;
	private final List<ResultWriter> resultWriters;
	private final Cpu cpu;
	private final MonitoringStatus status;
	private final AgentProperties properties;

	/**
	 * Creates a new ShutdownHandler.
	 *
	 * @param appPid        the PID of the monitored application
	 * @param resultWriters the writer that will be used to save data in files
	 * @param cpu           an implementation of the CPU interface, depending on the
	 *                      OS and hardware
	 * @param status        where all the runtime data will be saved
	 * @param properties    the agent's configuration properties
	 */
	public ShutdownHandler(long appPid, List<ResultWriter> resultWriters, Cpu cpu, MonitoringStatus status,
			AgentProperties properties) {
		this.appPid = appPid;
		this.resultWriters = resultWriters;
		this.cpu = cpu;
		this.status = status;
		this.properties = properties;
	}

	@Override
	public void run() {
		// Close monitoring implementation to release all resources
		try {
			cpu.close();
		} catch (final Exception exception) {
			// Continue shutting down
		}

		logger.log(Level.INFO, String.format("JoularJX finished monitoring application with ID %d", appPid));
		logger.log(Level.INFO, "Program consumed {0,number,#.##} joules", status.getTotalConsumedEnergy());

		try {
			// Writing methods and filtered methods energy consumption
			this.saveSelfAndTotalResults(status.getMethodsConsumedEnergy(), status.getMethodsTotalConsumedEnergy(),
					new ResultWriterConfiguration(ResultScope.ALL_TOTAL_METHODS));
			this.saveSelfAndTotalResults(status.getFilteredMethodsConsumedEnergy(),status.getFilteredMethodsTotalConsumedEnergy(),
					new ResultWriterConfiguration(ResultScope.FILTERED_TOTAL_METHODS));


			if (this.properties.getStatusPerThread()) {
				// All methods per thread
				this.saveSelfAndTotalResultsPerThread(
						status.getMethodsConsumedEnergyPerThread(),
						status.getTotalMethodsConsumedEnergyPerThread(),
						new ResultWriterConfiguration(ResultScope.ALL_TOTAL_METHODS, "per-thread"));

				// Filtered methods per thread
				this.saveSelfAndTotalResultsPerThread(
						status.getFilteredMethodsConsumedEnergyPerThread(),
						status.getTotalFilteredMethodsConsumedEnergyPerThread(),
						new ResultWriterConfiguration(ResultScope.FILTERED_TOTAL_METHODS, "per-thread"));
			}

			// Writing consumption evolution files only if the option is enabled
			if (this.properties.trackConsumptionEvolution()) {
				// All methods
				for (final var methodEntry : this.status.getMethodsConsumptionEvolution().entrySet()) {
					this.saveResults(methodEntry.getValue(), new ResultWriterConfiguration(ResultScope.ALL_EVOLUTION,
							sanitizeMethodName(methodEntry.getKey())));
				}

				// Filtered methods
				for (final var methodEntry : this.status.getFilteredMethodsConsumptionEvolution().entrySet()) {
					this.saveResults(methodEntry.getValue(), new ResultWriterConfiguration(
							ResultScope.FILTERED_EVOLUTION, sanitizeMethodName(methodEntry.getKey())));
				}

				if (this.properties.getStatusPerThread()) {
					// All methods evolution per thread
					this.saveEvolutionResultsPerThread(this.status.getMethodsConsumptionEvolutionPerThread(),
							new ResultWriterConfiguration(ResultScope.ALL_EVOLUTION, "per-thread"));

					// Filtered methods evolution per thread
					this.saveEvolutionResultsPerThread(this.status.getFilteredMethodsConsumptionEvolutionPerThread(),
							new ResultWriterConfiguration(ResultScope.FILTERED_EVOLUTION, "per-thread"));
				}
			}

			// Writing call trees consumption file only if the option is enabled
			if (this.properties.callTreesConsumption()) {
				this.saveResults(status.getCallTreesConsumedEnergy(),
						new ResultWriterConfiguration(ResultScope.ALL_TOTAL_CALL_TREE));
				this.saveResults(status.getFilteredCallTreesConsumedEnergy(),
						new ResultWriterConfiguration(ResultScope.FILTERED_TOTAL_CALL_TREE));

				if (this.properties.getStatusPerThread()) {
					// All call trees per thread
					this.saveResultsPerThread(status.getCallTreesConsumedEnergyPerThread(),
							new ResultWriterConfiguration(ResultScope.ALL_TOTAL_CALL_TREE, "per-thread"));

					// Filtered call trees per thread
					this.saveResultsPerThread(status.getFilteredCallTreesConsumedEnergyPerThread(),
							new ResultWriterConfiguration(ResultScope.FILTERED_TOTAL_CALL_TREE, "per-thread"));
				}
			}
		} catch (final IOException exception) {
			// Continue shutting down
		}

		logger.log(Level.INFO, "Energy consumption of methods and filtered methods written to files");
	}

	/**
	 * Cleans a method name to make it suitable for inclusion in a filename
	 *
	 * @param methodName the method name
	 * @return the sanitized version
	 */
	private String sanitizeMethodName(String methodName) {
		return methodName.replaceAll("[<>]", "_");
	}

	/**
	 * Writes the results. The filename is partially defined by the given
	 * parameters.
	 *
	 * @param <K>               The type of key that will be written in the file.
	 *                          Must implement the {@code toString()} method.
	 * @param consumedEnergyMap the data to be written.
	 * @param config            configuration for the writers
	 * @throws IOException if an I/O error occurs while writing the data.
	 */
	public <K> void saveResults(Map<K, Double> consumedEnergyMap, ResultWriterConfiguration config) throws IOException {
		// String fileName = String.format("joularJX-%d-%s-%s", appPid, nodeType,
		// dataType);
		for (final ResultWriter resultWriter : resultWriters) {
			resultWriter.setConfiguration(config);
		}

		for (final var entry : consumedEnergyMap.entrySet()) {
			for (final ResultWriter resultWriter : resultWriters) {
				resultWriter.write(entry.getKey().toString(), entry.getValue());
			}
		}

		for (final ResultWriter resultWriter : resultWriters) {
			resultWriter.closeTarget();
		}

	}
    /**
     * Writes the results. The filename is partially defined by the given
     * parameters.
     *
     * @param <K>               The type of key that will be written in the file.
     *                          Must implement the {@code toString()} method.
     * @param selfEnergyMap the data to be written.
     * @param totalEnergyMap the data to be written.
     * @param config            configuration for the writers
     * @throws IOException if an I/O error occurs while writing the data.
     */
    public <K> void saveSelfAndTotalResults(Map<K, Double> selfEnergyMap, Map<K, Double> totalEnergyMap, ResultWriterConfiguration config) throws IOException {
        // String fileName = String.format("joularJX-%d-%s-%s", appPid, nodeType,
        // dataType);
        for (final ResultWriter resultWriter : resultWriters) {
            resultWriter.setConfiguration(config);
        }
        for (final ResultWriter resultWriter : resultWriters) {
            resultWriter.writeString("Method Name, Self Energy (Joules), Total Energy (Joules)");
        }
        for (final var entry : totalEnergyMap.entrySet()) {
            var selfEnergyValue = selfEnergyMap.getOrDefault(entry.getKey(), 0.0);
            var totalEnergyValue = entry.getValue();
            for (final ResultWriter resultWriter : resultWriters) {
                resultWriter.write(entry.getKey().toString(), selfEnergyValue, totalEnergyValue);
            }
        }

        for (final ResultWriter resultWriter : resultWriters) {
            resultWriter.closeTarget();
        }

    }

	public <K> void saveSelfAndTotalResultsPerThread(Map<Thread, Map<K, Double>> selfEnergyMap, Map<Thread, Map<K, Double>> totalEnergyMap, ResultWriterConfiguration config) throws IOException {
		for (final ResultWriter resultWriter : resultWriters) {
			resultWriter.setConfiguration(config);
		}
		for (final ResultWriter resultWriter : resultWriters) {
			resultWriter.writeString("Thread ID, Method Name, Self Energy (Joules), Total Energy (Joules)");
		}
		for (final var threadEntry : totalEnergyMap.entrySet()) {
			Thread thread = threadEntry.getKey();
			String threadIdStr = String.valueOf(thread.getId());
			Map<K, Double> innerTotalMap = threadEntry.getValue();
			Map<K, Double> innerSelfMap = selfEnergyMap.getOrDefault(thread, java.util.Collections.emptyMap());

			for (final var methodEntry : innerTotalMap.entrySet()) {
				K methodName = methodEntry.getKey();
				double selfEnergyValue = innerSelfMap.getOrDefault(methodName, 0.0);
				double totalEnergyValue = methodEntry.getValue();
				String combinedKey = threadIdStr + ",\"" + methodName.toString() + "\"";
				for (final ResultWriter resultWriter : resultWriters) {
					resultWriter.write(combinedKey, selfEnergyValue, totalEnergyValue);
				}
			}
		}

		for (final ResultWriter resultWriter : resultWriters) {
			resultWriter.closeTarget();
		}
	}

	public <K> void saveResultsPerThread(Map<Thread, Map<K, Double>> perThreadMap, ResultWriterConfiguration config) throws IOException {
		for (final ResultWriter resultWriter : resultWriters) {
			resultWriter.setConfiguration(config);
		}

		for (final var threadEntry : perThreadMap.entrySet()) {
			String threadId = String.valueOf(threadEntry.getKey().getId());
			for (final var innerEntry : threadEntry.getValue().entrySet()) {
				String combinedKey = threadId + "," + innerEntry.getKey().toString();
				for (final ResultWriter resultWriter : resultWriters) {
					resultWriter.write(combinedKey, innerEntry.getValue());
				}
			}
		}

		for (final ResultWriter resultWriter : resultWriters) {
			resultWriter.closeTarget();
		}
	}

	public <K> void saveEvolutionResultsPerThread(Map<Thread, Map<K, Map<Long, Double>>> perThreadEvolutionMap, ResultWriterConfiguration config) throws IOException {
		for (final ResultWriter resultWriter : resultWriters) {
			resultWriter.setConfiguration(config);
		}

		for (final var threadEntry : perThreadEvolutionMap.entrySet()) {
			String threadId = String.valueOf(threadEntry.getKey().getId());
			for (final var methodEntry : threadEntry.getValue().entrySet()) {
				for (final var timeEntry : methodEntry.getValue().entrySet()) {
					String combinedKey = threadId + "," + methodEntry.getKey().toString() + "," + timeEntry.getKey();
					for (final ResultWriter resultWriter : resultWriters) {
						resultWriter.write(combinedKey, timeEntry.getValue());
					}
				}
			}
		}

		for (final ResultWriter resultWriter : resultWriters) {
			resultWriter.closeTarget();
		}
	}
}
