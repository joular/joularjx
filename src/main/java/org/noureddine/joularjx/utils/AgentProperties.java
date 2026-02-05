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

    // Properties names in the config.properties file
    private static final String FILTER_METHOD_NAME_PROPERTY = "filter-method-names";
    /**
     * Property key for the external power monitor executable path.
     */
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
    private static final String VM_MONITORING_PROPERTY = "vm-monitoring";
    /**
     * Property key for the virtual machine power monitor path.
     */
    public static final String VM_POWER_PATH_PROPERTY = "vm-power-path";
    private static final String VM_POWER_FORMAT_PROPERTY = "vm-power-format";
    private static final String JOULAR_CORE_PROPERTY = "joular-core";
    /**
     * Property key for the Joular Core executable path.
     */
    public static final String JOULAR_CORE_PATH_PROPERTY = "joular-core-path";
    /**
     * Property key for the Joular Core parameters.
     */
    public static final String JOULAR_CORE_PARAMETERS_PROPERTY = "joular-core-parameters";

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
    private final boolean vmMonitoring;
    private final String vmPowerPath;
    private final String vmPowerFormat;
    private final boolean joularCore;
    private final String joularCorePath;
    private final String joularCoreParameters;

    /**
     * Instantiate a new instance which will load the properties
     * 
     * @param fileSystem file system abstraction for accessing properties
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
        this.vmMonitoring = loadVMMonitoring();
        this.vmPowerPath = loadVMPowerPath();
        this.vmPowerFormat = loadVMPowerFormat();
        this.joularCore = loadJoularCore();
        this.joularCorePath = loadJoularCorePath();
        this.joularCoreParameters = loadJoularCoreParameters();
    }

    /**
     * Instantiate a new instance using the default filesystem.
     */
    public AgentProperties() {
        this(FileSystems.getDefault());
    }

    /**
     * Checks if the given method name matches any of the configured filters.
     *
     * @param methodName the fully qualified method name to check
     * @return true if the method name is filtered, false otherwise
     */
    public boolean filtersMethod(String methodName) {
        for (String filterMethod : filterMethodNames) {
            if (methodName.startsWith(filterMethod)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the configured logger level.
     *
     * @return the logger level
     */
    public Level getLoggerLevel() {
        return loggerLevel;
    }

    /**
     * Returns the configured power monitor path.
     *
     * @return the power monitor path or null if not configured
     */
    public String getPowerMonitorPath() {
        return powerMonitorPath;
    }

    /**
     * Indicates whether runtime data should be saved.
     *
     * @return true if runtime data should be saved, false otherwise
     */
    public boolean savesRuntimeData() {
        return saveRuntimeData;
    }

    /**
     * Indicates whether runtime data should be overwritten.
     *
     * @return true if runtime data should be overwritten, false otherwise
     */
    public boolean overwritesRuntimeData() {
        return overwriteRuntimeData;
    }

    /**
     * Indicates whether consumption evolution tracking is enabled.
     *
     * @return true if tracking is enabled, false otherwise
     */
    public boolean trackConsumptionEvolution() {
        return consumptionEvolution;
    }

    /**
     * Indicates whether agent consumption should be hidden.
     *
     * @return true if agent consumption should be hidden, false otherwise
     */
    public boolean hideAgentConsumption() {
        return this.hideAgentConsumption;
    }

    /**
     * Indicates whether call trees consumption tracking is enabled.
     *
     * @return true if call trees consumption tracking is enabled, false otherwise
     */
    public boolean callTreesConsumption() {
        return this.callTreesConsumption;
    }

    /**
     * Indicates whether call trees runtime data should be saved.
     *
     * @return true if call trees runtime data should be saved, false otherwise
     */
    public boolean saveCallTreesRuntimeData() {
        return this.saveCtRuntimeData;
    }

    /**
     * Indicates whether call trees runtime data should be overwritten.
     *
     * @return true if call trees runtime data should be overwritten, false otherwise
     */
    public boolean overwriteCallTreesRuntimeData() {
        return this.overwriteCtRuntimeData;
    }

    /**
     * Returns the stack monitoring sample rate in milliseconds.
     *
     * @return the stack monitoring sample rate
     */
    public int stackMonitoringSampleRate() {
        return this.stackMonitoringSampleRate;
    }

    /**
     * Indicates whether the monitored application is an application server.
     *
     * @return true if it is an application server, false otherwise
     */
    public boolean isApplicationServer() {
        return this.applicationServer;
    }

    /**
     * Indicates whether virtual machine monitoring is enabled.
     *
     * @return true if VM monitoring is enabled, false otherwise
     */
    public boolean isVirtualMachine() {
        return this.vmMonitoring;
    }

    /**
     * Returns the configured virtual machine power path.
     *
     * @return the VM power path or null if not configured
     */
    public String getVMPowerPath() {
        return this.vmPowerPath;
    }

    /**
     * Returns the configured virtual machine power format.
     *
     * @return the VM power format or null if not configured
     */
    public String getVMPowerFormat() {
        return this.vmPowerFormat;
    }

    /**
     * Indicates whether Joular Core integration is enabled.
     *
     * @return true if Joular Core is enabled, false otherwise
     */
    public boolean isJoularCoreEnabled() {
        return this.joularCore;
    }

    /**
     * Returns the configured Joular Core path.
     *
     * @return the Joular Core path or null if not configured
     */
    public String getJoularCorePath() {
        return this.joularCorePath;
    }

    /**
     * Returns the configured Joular Core parameters.
     *
     * @return the Joular Core parameters or null if not configured
     */
    public String getJoularCoreParameters() {
        return this.joularCoreParameters;
    }

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

    /**
     * Loads the configured power monitor path from properties.
     *
     * @return the configured power monitor path or null if not present
     */
    public String loadPowerMonitorPath() {
        return properties.getProperty(POWER_MONITOR_PATH_PROPERTY);
    }

    /**
     * Loads the save runtime data flag from properties.
     *
     * @return true if runtime data should be saved, false otherwise
     */
    public boolean loadSaveRuntimeData() {
        return Boolean.parseBoolean(properties.getProperty(SAVE_RUNTIME_DATA_PROPERTY));
    }

    /**
     * Loads the overwrite runtime data flag from properties.
     *
     * @return true if runtime data should be overwritten, false otherwise
     */
    public boolean loadOverwriteRuntimeData() {
        return Boolean.parseBoolean(properties.getProperty(OVERWRITE_RUNTIME_DATA_PROPERTY));
    }

    /**
     * Loads the logger level from properties.
     *
     * @return the configured logger level, or {@link Level#INFO} if missing or invalid
     */
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

    /**
     * Loads the consumption evolution flag from properties.
     *
     * @return true if consumption evolution tracking is enabled, false otherwise
     */
    public boolean loadConsumptionEvolution() {
        return Boolean.parseBoolean(properties.getProperty(TRACK_CONSUMPTION_EVOLUTION_PROPERTY));
    }

    /**
     * Loads the hide agent consumption flag from properties.
     *
     * @return true if agent consumption should be hidden, false otherwise
     */
    public boolean loadAgentConsumption() {
        return Boolean.parseBoolean(properties.getProperty(HIDE_AGENT_CONSUMPTION_PROPERTY));
    }

    /**
     * Loads the call trees consumption flag from properties.
     *
     * @return true if call trees consumption is enabled, false otherwise
     */
    public boolean loadCallTreesConsumption() {
        return Boolean.parseBoolean(properties.getProperty(CALL_TREES_CONSUMPTION_PROPERTY));
    }

    /**
     * Loads the save call trees runtime data flag from properties.
     *
     * @return true if call trees runtime data should be saved, false otherwise
     */
    public boolean loadSaveCallTreesRuntimeData() {
        return Boolean.parseBoolean(properties.getProperty(SAVE_CT_RUNTIME_DATA_PROPERTY));
    }

    /**
     * Loads the overwrite call trees runtime data flag from properties.
     *
     * @return true if call trees runtime data should be overwritten, false otherwise
     */
    public boolean loadOverwriteCallTreeRuntimeData() {
        return Boolean.parseBoolean(properties.getProperty(OVERWRITE_CT_RUNTIME_DATA_PROPERTY));
    }

    /**
     * Loads the application server flag from properties.
     *
     * @return true if application server mode is enabled, false otherwise
     */
    public boolean loadApplicationServer() {
        return Boolean.parseBoolean(properties.getProperty(APPLICATION_SERVER_PROPERTY));
    }

    /**
     * Loads the stack monitoring sample rate from properties.
     *
     * @return the sample rate in milliseconds
     */
    public int loadStackMonitoringSampleRate() {
        String property = properties.getProperty(STACK_MONITORING_SAMPLE_RATE_PROPERTY);
        int value = 10; // default of 10 milliseconds
        if (property != null) {
            int parsedValue = Integer.parseInt(property);
            if (parsedValue > 0 && parsedValue <= 1000) {
                value = parsedValue;
            }
        }
        return value;
    }

    private Optional<Path> getPropertiesPathIfExists(FileSystem fileSystem) {
        Path path = fileSystem.getPath(System.getProperty("joularjx.config", "config.properties"));

        if (Files.notExists(path)) {
            logger.log(Level.INFO, "Could not locate config.properties, will use default values");
            return Optional.empty();
        }

        return Optional.of(path);
    }

    /**
     * Loads the VM monitoring flag from properties.
     *
     * @return true if VM monitoring is enabled, false otherwise
     */
    public boolean loadVMMonitoring() {
        return Boolean.parseBoolean(properties.getProperty(VM_MONITORING_PROPERTY));
    }

    /**
     * Loads the VM power path from properties.
     *
     * @return the VM power path or null if not configured
     */
    public String loadVMPowerPath() {
        return properties.getProperty(VM_POWER_PATH_PROPERTY);
    }

    /**
     * Loads the VM power format from properties.
     *
     * @return the VM power format or null if not configured
     */
    public String loadVMPowerFormat() {
        return properties.getProperty(VM_POWER_FORMAT_PROPERTY);
    }

    /**
     * Loads the Joular Core enabled flag from properties.
     *
     * @return true if Joular Core is enabled, false otherwise
     */
    public boolean loadJoularCore() {
        return Boolean.parseBoolean(properties.getProperty(JOULAR_CORE_PROPERTY));
    }

    /**
     * Loads the Joular Core path from properties.
     *
     * @return the Joular Core path or null if not configured
     */
    public String loadJoularCorePath() {
        return properties.getProperty(JOULAR_CORE_PATH_PROPERTY);
    }

    /**
     * Loads the Joular Core parameters from properties.
     *
     * @return the Joular Core parameters or null if not configured
     */
    public String loadJoularCoreParameters() {
        return properties.getProperty(JOULAR_CORE_PARAMETERS_PROPERTY);
    }
}
