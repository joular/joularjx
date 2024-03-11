/*
 * Copyright (c) 2021-2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Author : Adel Noureddine
 */

package org.noureddine.joularjx.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Agent properties configured by the config.properties file
 */
public class AgentProperties {

    private static final Logger logger = JoularJXLogging.getLogger();

    //Properties names in the config.properties file
    private static final String FILTER_METHOD_NAME_PROPERTY = "filter-method-names";
    public static final String POWER_MONITOR_PATH_PROPERTY = "powermonitor-path";
    private static final String SAVE_RUNTIME_DATA_PROPERTY = "save-runtime-data";
    private static final String OVERWRITE_RUNTIME_DATA_PROPERTY = "overwrite-runtime-data";
    private static final String LOGGER_LEVEL_PROPERTY = "logger-level";
    private static final String TRACK_CONSUMPTION_EVOLUTION_PROPERTY = "track-consumption-evolution";
    private static final String HIDE_AGENT_CONSUMPTION_PROPERTY = "hide-agent-consumption";
    private static final String CALL_TREES_CONSUMPTION_PROPERTY = "enable-call-trees-consumption";
    private static final String SAVE_CT_RUNTIME_DATA_PROPERTY = "save-call-trees-runtime-data";
    private static final String OVERWRITE_CT_RUNTIME_DATA_PROPERTY = "overwrite-call-trees-runtime-data";
    private static final String STACK_MONITORING_SAMPLE_RATE_PROPERTY = "stack-monitoring-sample-rate";
    private static final String APPLICATION_SERVER_PROPERTY = "application-server";

    /**
     * Loaded configuration properties
     */
    private final Properties properties;

    private final Collection<String> filterMethodNames;
    private final String powerMonitorPath;
    private final boolean saveRuntimeData;
    private final boolean overwriteRuntimeData;
    private final Level loggerLevel;
    private final boolean consumptionEvolution;
    private final boolean hideAgentConsumption;
    private final boolean callTreesConsumption;
    private final boolean saveCtRuntimeData;
    private final boolean overwriteCtRuntimeData;
    private final int stackMonitoringSampleRate;
    private final boolean applicationServer;

    /**
     * Instantiate a new instance which will load the properties
     */
    public AgentProperties(FileSystem fileSystem) {
        this.properties = loadProperties(fileSystem);

        this.filterMethodNames = loadFilterMethodNames();
        this.powerMonitorPath = loadPowerMonitorPath();
        this.saveRuntimeData = loadSaveRuntimeData();
        this.overwriteRuntimeData = loadOverwriteRuntimeData();
        this.loggerLevel = loadLoggerLevel();
        this.consumptionEvolution = loadConsumptionEvolution();
        this.hideAgentConsumption = loadAgentConsumption();
        this.callTreesConsumption = loadCallTreesConsumption();
        this.saveCtRuntimeData = loadSaveCallTreesRuntimeData();
        this.overwriteCtRuntimeData = loadOverwriteCallTreeRuntimeData();
        this.stackMonitoringSampleRate = loadStackMonitoringSampleRate();
        this.applicationServer = loadApplicationServer();
    }

    public AgentProperties() {
        this(FileSystems.getDefault());
    }

    public boolean filtersMethod(String methodName) {
        for (String filterMethod : filterMethodNames) {
            if (methodName.startsWith(filterMethod)) {
                return true;
            }
        }
        return false;
    }

    public Level getLoggerLevel() {
        return loggerLevel;
    }

    public String getPowerMonitorPath() {
        return powerMonitorPath;
    }

    public boolean savesRuntimeData() {
        return saveRuntimeData;
    }

    public boolean overwritesRuntimeData() {
        return overwriteRuntimeData;
    }

    public boolean trackConsumptionEvolution() {
        return consumptionEvolution;
    }

    public boolean hideAgentConsumption() {
        return this.hideAgentConsumption;
    }

    public boolean callTreesConsumption() {
        return this.callTreesConsumption;
    }

    public boolean saveCallTreesRuntimeData() {
        return this.saveCtRuntimeData;
    }

    public boolean overwriteCallTreesRuntimeData() { return this.overwriteCtRuntimeData; }

    public int stackMonitoringSampleRate() { return this.stackMonitoringSampleRate; }

    public boolean isApplicationServer() { return this.applicationServer; }

    private Properties loadProperties(FileSystem fileSystem) {
        Properties result = new Properties();

        // Read properties file if possible
        getPropertiesPathIfExists(fileSystem).ifPresent(path -> {
            try (InputStream input = new BufferedInputStream(Files.newInputStream(path))) {
                result.load(input);
            } catch (IOException e) {
                logger.log(Level.INFO, "Couldn't load local config: \"{0}\"", e.getMessage());
            }
        });

        return result;
    }

    private Collection<String> loadFilterMethodNames() {
        String filterMethods = properties.getProperty(FILTER_METHOD_NAME_PROPERTY);
        if (filterMethods == null || filterMethods.isEmpty()) {
            return Collections.emptyList();
        }
        return Set.of(filterMethods.split(","));
    }

    public String loadPowerMonitorPath() {
        return properties.getProperty(POWER_MONITOR_PATH_PROPERTY);
    }

    public boolean loadSaveRuntimeData() {
        return Boolean.parseBoolean(properties.getProperty(SAVE_RUNTIME_DATA_PROPERTY));
    }

    public boolean loadOverwriteRuntimeData() {
        return Boolean.parseBoolean(properties.getProperty(OVERWRITE_RUNTIME_DATA_PROPERTY));
    }

    public Level loadLoggerLevel() {
        String property = properties.getProperty(LOGGER_LEVEL_PROPERTY);
        if (property == null) {
            return Level.INFO;
        }

        try {
            return Level.parse(property);
        } catch (IllegalArgumentException exception) {
            return Level.INFO;
        }
    }

    public boolean loadConsumptionEvolution() {
        return Boolean.parseBoolean(properties.getProperty(TRACK_CONSUMPTION_EVOLUTION_PROPERTY));
    }

    public boolean loadAgentConsumption() {
        return Boolean.parseBoolean(properties.getProperty(HIDE_AGENT_CONSUMPTION_PROPERTY));
    }

    public boolean loadCallTreesConsumption() {
        return Boolean.parseBoolean(properties.getProperty(CALL_TREES_CONSUMPTION_PROPERTY));
    }

    public boolean loadSaveCallTreesRuntimeData() {
        return Boolean.parseBoolean(properties.getProperty(SAVE_CT_RUNTIME_DATA_PROPERTY));
    }

    public boolean loadOverwriteCallTreeRuntimeData() {
        return Boolean.parseBoolean(properties.getProperty(OVERWRITE_CT_RUNTIME_DATA_PROPERTY));
    }

    public boolean loadApplicationServer() {
        return Boolean.parseBoolean(properties.getProperty(APPLICATION_SERVER_PROPERTY));
    }

    public int loadStackMonitoringSampleRate() {
        String property = properties.getProperty(STACK_MONITORING_SAMPLE_RATE_PROPERTY);
        int value = 10; // default of 10 milliseconds
        if(property != null) {
            int parsedValue = Integer.parseInt(property);
            if (parsedValue > 0 && parsedValue <= 1000) {
                value = parsedValue;
            }
        }
        return value;
    }

    private Optional<Path> getPropertiesPathIfExists(FileSystem fileSystem) {
        Path path = fileSystem.getPath("config.properties");

        if (Files.notExists(path)) {
            logger.log(Level.INFO, "Could not locate config.properties, will use default values");
           return Optional.empty();
        }

        return Optional.of(path);
    }
}
