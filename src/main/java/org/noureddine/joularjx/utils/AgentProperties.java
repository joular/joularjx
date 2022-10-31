/*
 * Copyright (c) 2021-2022, Adel Noureddine, Université de Pays et des Pays de l'Adour.
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
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Agent properties configured by the config.properties file
 */
public class AgentProperties {

    private static final String FILTER_METHOD_NAME_PROPERTY = "filter-method-names";

    private static final String POWER_MONITOR_PATH_PROPERTY = "powermonitor-path";

    private static final String SAVE_RUNTIME_DATA_PROPERTY = "save-runtime-data";

    private static final String OVERWRITE_RUNTIME_DATA_PROPERTY = "overwrite-runtime-data";

    private static final String LOGGER_LEVEL_PROPERTY = "logger-level";

    /**
     * Loaded configuration properties
     */
    private final Properties prop = new Properties();

    /**
     * Instantiate a new instance which will load the properties
     */
    public AgentProperties(final FileSystem fileSystem) {
        // Read properties file
        try (final InputStream input =
                     new BufferedInputStream(Files.newInputStream(fileSystem.getPath("config.properties")))) {
            prop.load(input);
        } catch (IOException e) {
            System.exit(1);
        }
    }

    public List<String> getFilterMethodNames() {
        String filterMethods = prop.getProperty(FILTER_METHOD_NAME_PROPERTY);
        if (filterMethods == null || filterMethods.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(filterMethods.split(","));
    }

    public String getPowerMonitorPath() {
        return prop.getProperty(POWER_MONITOR_PATH_PROPERTY);
    }

    public boolean getSaveRuntimeData() {
        String propValue = prop.getProperty(SAVE_RUNTIME_DATA_PROPERTY);
        if (propValue.equals("true")) {
            return true;
        }
        return false;
    }

    public boolean getOverwriteRuntimeData() {
        String propValue = prop.getProperty(OVERWRITE_RUNTIME_DATA_PROPERTY);
        if (propValue.equals("true")) {
            return true;
        }
        return false;
    }

    public Level getLoggerLevel() {
        String loggerLevel = prop.getProperty(LOGGER_LEVEL_PROPERTY);
        Level loggerLevelEnum = Level.INFO;

        switch (loggerLevel) {
            case "OFF":
                loggerLevelEnum = Level.OFF;
                break;
            case "WARNING":
                loggerLevelEnum = Level.WARNING;
                break;
            case "SEVERE":
                loggerLevelEnum = Level.SEVERE;
                break;
            case "INFO":
            default:
                loggerLevelEnum = Level.INFO;
                break;
        }

        return loggerLevelEnum;
    }

}
