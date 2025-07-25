/*
 * Copyright (c) 2021-2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.result;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;

/**
 * The ResultTreeManager provides a method to create the required folder
 * hierarchy, enabling the agent to write data in files later on. This class
 * also provides methods that return the proper filepath depending on the type
 * (method, call-tree, ...) and granularity (all, filtered) of the data.
 */
public class ResultTreeManager {

	private static final Logger logger = JoularJXLogging.getLogger();

	// Folders names
	public final static String GLOBAL_RESULT_DIRECTORY_NAME = "joularjx-result";

	public final static String ALL_DIRECTORY_NAME = "all";
	public final static String FILTERED_DIRECTORY_NAME = "app";

	public final static String RUNTIME_DIRECTORY_NAME = "runtime";
	public final static String TOTAL_DIRECTORY_NAME = "total";
	public final static String EVOLUTION_DIRECTORY_NAME = "evolution";

	public final static String CALLTREE_DIRECTORY_NAME = "calltrees";
	public final static String METHOD_DIRECTORY_NAME = "methods";

	private final AgentProperties properties;

	// The directory where the result of the current execution will be written
	private final Path runDirectoryPath;

	// All leaf directory paths
	private final Path allTotalMethodsPath;
	private final Path filteredTotalMethodsPath;
	private final Path allRuntimeMethodsPath;
	private final Path filteredRuntimeMethodsPath;

	private final Path allRuntimeCallTreePath;
	private final Path filteredRuntimeCallTreePath;
	private final Path allTotalCallTreePath;
	private final Path filteredTotalCallTreePath;

	private final Path allEvolutionPath;
	private final Path filteredEvolutionPath;

	/**
	 * Creates a new ResultTreeManager. All the filepaths will be initialized (but
	 * not created yet!) with the informations provided by the given configuration
	 * properties.
	 *
	 * @param properties     the agent's configuration properties
	 * @param pid            the application PID
	 * @param startTimestamp the timestamp at which the creation has been
	 *                       initialized
	 */
	public ResultTreeManager(AgentProperties properties, long pid, long startTimestamp) {
		this.properties = properties;

		// Building the path of all the directories
		this.runDirectoryPath = Paths.get(GLOBAL_RESULT_DIRECTORY_NAME, String.format("%d-%d", pid, startTimestamp));

		final Path allDirectoryPath = runDirectoryPath.resolve(ALL_DIRECTORY_NAME);
		final Path filteredDirectoryPath = runDirectoryPath.resolve(FILTERED_DIRECTORY_NAME);

		this.allTotalMethodsPath = allDirectoryPath.resolve(TOTAL_DIRECTORY_NAME).resolve(METHOD_DIRECTORY_NAME);
		this.filteredTotalMethodsPath = filteredDirectoryPath.resolve(TOTAL_DIRECTORY_NAME)
				.resolve(METHOD_DIRECTORY_NAME);
		this.allRuntimeMethodsPath = allDirectoryPath.resolve(RUNTIME_DIRECTORY_NAME).resolve(METHOD_DIRECTORY_NAME);
		this.filteredRuntimeMethodsPath = filteredDirectoryPath.resolve(RUNTIME_DIRECTORY_NAME)
				.resolve(METHOD_DIRECTORY_NAME);

		this.allRuntimeCallTreePath = allDirectoryPath.resolve(RUNTIME_DIRECTORY_NAME).resolve(CALLTREE_DIRECTORY_NAME);
		this.filteredRuntimeCallTreePath = filteredDirectoryPath.resolve(RUNTIME_DIRECTORY_NAME)
				.resolve(CALLTREE_DIRECTORY_NAME);
		this.allTotalCallTreePath = allDirectoryPath.resolve(TOTAL_DIRECTORY_NAME).resolve(CALLTREE_DIRECTORY_NAME);
		this.filteredTotalCallTreePath = filteredDirectoryPath.resolve(TOTAL_DIRECTORY_NAME)
				.resolve(CALLTREE_DIRECTORY_NAME);

		this.allEvolutionPath = allDirectoryPath.resolve(EVOLUTION_DIRECTORY_NAME);
		this.filteredEvolutionPath = filteredDirectoryPath.resolve(EVOLUTION_DIRECTORY_NAME);

	}

	/**
	 * Creates the tree hierarchy. Creates the required folder, if they do not exist
	 * yet. Only the necessary folders are created, depending on the provided
	 * configuration properties.
	 *
	 * @return a boolean indicating whether an error occurs while creating the
	 *         folder hierarchy (false), or no (true).
	 */
	public boolean create() {
		// This boolean acts as a check. If an error occurs during the initialization of
		// the file hierarchy, the method will
		// continue its execution, as other directories may be created successfully, but
		// this boolean will be set to false,
		// to indicate that an error occurred.
		boolean verif = true;

		logger.log(Level.INFO, String.format("Results will be stored in %s%s", this.runDirectoryPath, File.separator));

		// List of all the directories that will be created
		final List<Path> directoriesToCreate = new ArrayList<>();

		// Mandatory directories (directories that do not depend of configuration
		// properties)
		directoriesToCreate.add(this.allTotalMethodsPath);
		directoriesToCreate.add(this.filteredTotalMethodsPath);

		// Optional directories (directories that depends of configuration properties)
		// Runtime
		if (properties.savesRuntimeData()) {
			directoriesToCreate.add(this.allRuntimeMethodsPath);
			directoriesToCreate.add(this.filteredRuntimeMethodsPath);
		}

		// Call trees
		if (properties.callTreesConsumption()) {
			// Runtime
			if (properties.saveCallTreesRuntimeData()) {
				directoriesToCreate.add(this.allRuntimeCallTreePath);
				directoriesToCreate.add(this.filteredRuntimeCallTreePath);
			}
			// Total
			directoriesToCreate.add(this.allTotalCallTreePath);
			directoriesToCreate.add(this.filteredTotalCallTreePath);
		}

		// Methods consumption evolution
		if (properties.trackConsumptionEvolution()) {
			directoriesToCreate.add(this.allEvolutionPath);
			directoriesToCreate.add(this.filteredEvolutionPath);
		}

		// Creating all the directories
		for (final Path dirPath : directoriesToCreate) {
			final File dir = dirPath.toFile();
			if (!dir.exists() && !dir.mkdirs()) {
				logger.log(Level.WARNING, String.format("Failed to create directory %s", dirPath));
				verif = false;
			}
		}

		return verif;
	}

	/**
	 * Returns the path to the methods consumption evolution folder
	 *
	 * @return the path to the methods consumption evolution folder
	 */
	public Path getAllEvolutionPath() {
		return this.allEvolutionPath;
	}

	/**
	 * Returns the path to the call trees runtime consumption folder
	 *
	 * @return the path to the call trees runtime consumption folder
	 */
	public Path getAllRuntimeCallTreePath() {
		return this.allRuntimeCallTreePath;
	}

	/**
	 * Returns the path to the methods runtime consumption folder
	 *
	 * @return the path to the methods runtime consumption folder
	 */
	public Path getAllRuntimeMethodsPath() {
		return this.allRuntimeMethodsPath;
	}

	/**
	 * Returns the path to the call trees total consumption folder
	 *
	 * @return the path to the call trees total consumption folder
	 */
	public Path getAllTotalCallTreePath() {
		return this.allTotalCallTreePath;
	}

	/**
	 * Returns the path to the methods total consumption folder
	 *
	 * @return the path to the methods total consumption folder
	 */
	public Path getAllTotalMethodsPath() {
		return this.allTotalMethodsPath;
	}

	/**
	 * Returns the path to the filtered methods consumption evolution folder
	 *
	 * @return the path to the filtered methods consumption evolution folder
	 */
	public Path getFilteredEvolutionPath() {
		return this.filteredEvolutionPath;
	}

	/**
	 * Returns the path to the filtered call trees runtime consumption folder
	 *
	 * @return the path to the filtered call trees runtime consumption folder
	 */
	public Path getFilteredRuntimeCallTreePath() {
		return this.filteredRuntimeCallTreePath;
	}

	/**
	 * Returns the path to the filtered methods runtime consumption folder
	 *
	 * @return the path to the filtered methods runtime consumption folder
	 */
	public Path getFilteredRuntimeMethodsPath() {
		return this.filteredRuntimeMethodsPath;
	}

	/**
	 * Returns the path to the filtered call trees total consumption folder
	 *
	 * @return the path to the filtered call trees total consumption folder
	 */
	public Path getFilteredTotalCallTreePath() {
		return this.filteredTotalCallTreePath;
	}

	/**
	 * Returns the path to the filtered methods total consumption folder
	 *
	 * @return the path to the filtered methods total consumption folder
	 */
	public Path getFilteredTotalMethodsPath() {
		return this.filteredTotalMethodsPath;
	}

}
