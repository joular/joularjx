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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
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

	/**
	 * Builder for creating a {@link Path} object based on the specified attributes.
	 */
	public class PathBuilder {

		private boolean timestamp;
		private ResultScope scope;
		private String methodName;

		/**
		 * @param scope the scope
		 */
		public PathBuilder(ResultScope scope) {
			this.scope = scope;
		}

		/**
		 * Builds the {@link Path} based on the current state of the builder.
		 *
		 * @return a constructed {@link Path} instance
		 */
		public Path build() {
			final StringJoiner joiner = new StringJoiner("-", "joularJX-", "");
			joiner.add(String.valueOf(pid));
			if (timestamp) {
				joiner.add(String.valueOf(System.currentTimeMillis()));
			}
			if (Objects.nonNull(methodName)) {
				joiner.add(methodName);
			}
			joiner.add(scope.getScope()).add(scope.getSuffix());

			final String fileName = joiner.toString();
			return leafPaths.get(scope).resolve(fileName);
		}

		/**
		 * Sets the method name
		 *
		 * @param methodName the method name
		 * @return this builder instance
		 */
		public PathBuilder withMethodName(String methodName) {
			this.methodName = methodName;
			return this;
		}

		/**
		 * Sets the result scope.
		 *
		 * @param scope the {@link ResultScope} to set
		 * @return this builder instance
		 */
		public PathBuilder withScope(ResultScope scope) {
			this.scope = scope;
			return this;
		}

		/**
		 * Sets whether to include the current timestamp.
		 *
		 * @param timestamp true to include timestamp, false otherwise
		 * @return this builder instance
		 */
		public PathBuilder withTimestamp(boolean timestamp) {
			this.timestamp = timestamp;
			return this;
		}
	}

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

	private final long pid;

	// The directory where the result of the current execution will be written
	private final Path runDirectoryPath;
	// All leaf directory paths
	private final Map<ResultScope, Path> leafPaths;

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
		this.pid = pid;
		this.leafPaths = new HashMap<>(ResultScope.values().length);

		// Building the path of all the directories
		this.runDirectoryPath = Paths.get(GLOBAL_RESULT_DIRECTORY_NAME, String.format("%d-%d", pid, startTimestamp));

		final Path allDirectoryPath = runDirectoryPath.resolve(ALL_DIRECTORY_NAME);
		final Path filteredDirectoryPath = runDirectoryPath.resolve(FILTERED_DIRECTORY_NAME);

		leafPaths.put(ResultScope.ALL_TOTAL_METHODS,
				allDirectoryPath.resolve(TOTAL_DIRECTORY_NAME).resolve(METHOD_DIRECTORY_NAME));
		leafPaths.put(ResultScope.FILTERED_TOTAL_METHODS,
				filteredDirectoryPath.resolve(TOTAL_DIRECTORY_NAME).resolve(METHOD_DIRECTORY_NAME));
		leafPaths.put(ResultScope.ALL_RUNTIME_METHODS,
				allDirectoryPath.resolve(RUNTIME_DIRECTORY_NAME).resolve(METHOD_DIRECTORY_NAME));
		leafPaths.put(ResultScope.FILTERED_RUNTIME_METHODS,
				filteredDirectoryPath.resolve(RUNTIME_DIRECTORY_NAME).resolve(METHOD_DIRECTORY_NAME));

		leafPaths.put(ResultScope.ALL_RUNTIME_CALL_TREE,
				allDirectoryPath.resolve(RUNTIME_DIRECTORY_NAME).resolve(CALLTREE_DIRECTORY_NAME));
		leafPaths.put(ResultScope.FILTERED_RUNTIME_CALL_TREE,
				filteredDirectoryPath.resolve(RUNTIME_DIRECTORY_NAME).resolve(CALLTREE_DIRECTORY_NAME));
		leafPaths.put(ResultScope.ALL_TOTAL_CALL_TREE,
				allDirectoryPath.resolve(TOTAL_DIRECTORY_NAME).resolve(CALLTREE_DIRECTORY_NAME));
		leafPaths.put(ResultScope.FILTERED_TOTAL_CALL_TREE,
				filteredDirectoryPath.resolve(TOTAL_DIRECTORY_NAME).resolve(CALLTREE_DIRECTORY_NAME));

		leafPaths.put(ResultScope.ALL_EVOLUTION, allDirectoryPath.resolve(EVOLUTION_DIRECTORY_NAME));
		leafPaths.put(ResultScope.FILTERED_EVOLUTION, filteredDirectoryPath.resolve(EVOLUTION_DIRECTORY_NAME));
	}

	/**
	 * Creates the tree hierarchy. Creates the required folders, if they do not exist
	 * yet. Only the necessary folders are created, depending on the provided
	 * configuration properties.
	 *
	 * @return a boolean indicating whether an error occurs while creating the
	 *         folder hierarchy (false), or not (true).
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
		directoriesToCreate.add(this.leafPaths.get(ResultScope.ALL_TOTAL_METHODS));
		directoriesToCreate.add(this.leafPaths.get(ResultScope.FILTERED_TOTAL_METHODS));

		// Optional directories (directories that depends of configuration properties)
		// Runtime
		if (properties.savesRuntimeData()) {
			directoriesToCreate.add(this.leafPaths.get(ResultScope.ALL_RUNTIME_METHODS));
			directoriesToCreate.add(this.leafPaths.get(ResultScope.FILTERED_RUNTIME_METHODS));
		}

		// Call trees
		if (properties.callTreesConsumption()) {
			// Runtime
			if (properties.saveCallTreesRuntimeData()) {
				directoriesToCreate.add(this.leafPaths.get(ResultScope.ALL_RUNTIME_CALL_TREE));
				directoriesToCreate.add(this.leafPaths.get(ResultScope.FILTERED_RUNTIME_CALL_TREE));
			}
			// Total
			directoriesToCreate.add(this.leafPaths.get(ResultScope.ALL_TOTAL_CALL_TREE));
			directoriesToCreate.add(this.leafPaths.get(ResultScope.FILTERED_TOTAL_CALL_TREE));
		}

		// Methods consumption evolution
		if (properties.trackConsumptionEvolution()) {
			directoriesToCreate.add(this.leafPaths.get(ResultScope.ALL_EVOLUTION));
			directoriesToCreate.add(this.leafPaths.get(ResultScope.FILTERED_EVOLUTION));
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
	 * @param methodName name of the method being tracked
	 *
	 * @return the path to the methods consumption evolution folder
	 */
	public Path getAllEvolutionPath(String methodName) {
		return new PathBuilder(ResultScope.ALL_EVOLUTION).withMethodName(methodName).build();
	}

	/**
	 * Returns the path to the call trees runtime consumption file
	 *
	 * @param timestamped if the file must have the current timestamp in its name
	 * @return the path to the call trees runtime consumption file
	 */
	public Path getAllRuntimeCallTreePath(boolean timestamped) {
		return new PathBuilder(ResultScope.ALL_RUNTIME_CALL_TREE).withTimestamp(timestamped).build();
	}

	/**
	 * Returns the path to the methods runtime consumption file
	 *
	 * @param timestamped if the file must have the current timestamp in its name
	 * @return the path to the methods runtime consumption file
	 */
	public Path getAllRuntimeMethodsPath(boolean timestamped) {
		return new PathBuilder(ResultScope.ALL_RUNTIME_METHODS).withTimestamp(timestamped).build();
	}

	/**
	 * Returns the path to the call trees total consumption file
	 *
	 * @return the path to the call trees total consumption file
	 */
	public Path getAllTotalCallTreePath() {
		return getPath(ResultScope.ALL_TOTAL_CALL_TREE);
	}

	/**
	 * Returns the path to the methods total consumption file
	 *
	 * @return the path to the methods total consumption file
	 */
	public Path getAllTotalMethodsPath() {
		return getPath(ResultScope.ALL_TOTAL_METHODS);
	}

	/**
	 * Returns the path to the filtered methods consumption evolution folder
	 *
	 * @param methodName TODO
	 *
	 * @return the path to the filtered methods consumption evolution folder
	 */
	public Path getFilteredEvolutionPath(String methodName) {
		return new PathBuilder(ResultScope.FILTERED_EVOLUTION).withMethodName(methodName).build();
	}

	/**
	 * Returns the path to the filtered call trees runtime consumption file
	 *
	 * @param timestamped if the file must have the current timestamp in its name
	 * @return the path to the filtered call trees runtime consumption file
	 */
	public Path getFilteredRuntimeCallTreePath(boolean timestamped) {
		return new PathBuilder(ResultScope.FILTERED_RUNTIME_CALL_TREE).withTimestamp(timestamped).build();
	}

	/**
	 * Returns the path to the filtered methods runtime consumption file
	 *
	 * @param timestamped if the file must have the current timestamp in its name
	 * @return the path to the filtered methods runtime consumption file
	 */
	public Path getFilteredRuntimeMethodsPath(boolean timestamped) {
		return new PathBuilder(ResultScope.FILTERED_RUNTIME_METHODS).withTimestamp(timestamped).build();
	}

	/**
	 * Returns the path to the filtered call trees total consumption file
	 *
	 * @return the path to the filtered call trees total consumption file
	 */
	public Path getFilteredTotalCallTreePath() {
		return getPath(ResultScope.FILTERED_TOTAL_CALL_TREE);
	}

	/**
	 * Returns the path to the filtered methods total consumption file
	 *
	 * @return the path to the filtered methods total consumption file
	 */
	public Path getFilteredTotalMethodsPath() {
		return getPath(ResultScope.FILTERED_TOTAL_METHODS);
	}

	public Path getPath(ResultScope scope) {
		return new PathBuilder(scope).build();
	}

}
