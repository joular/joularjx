/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.utils;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AgentPropertiesTest {

    @Test
    @ExpectSystemExitWithStatus(1)
    void loadNonExistentFile() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            new AgentProperties(fs);
        }
    }

    @Test
    void loadEmptyFile() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.createFile(fs.getPath("config.properties"));

            AgentProperties properties = new AgentProperties(fs);

            assertAll(
                    () -> assertFalse(properties.filtersMethod("")),
                    () -> assertNull(properties.getPowerMonitorPath()),
                    () -> assertFalse(properties.overwritesRuntimeData()),
                    () -> assertFalse(properties.savesRuntimeData()),
                    () -> assertEquals(Level.INFO, properties.getLoggerLevel()),
                    () -> assertFalse(properties.loadConsumptionEvolution()),
                    () -> assertFalse(properties.loadAgentConsumption()),
                    () -> assertFalse(properties.loadCallTreesConsumption()),
                    () -> assertFalse(properties.loadSaveCallTreesRuntimeData()),
                    () -> assertFalse(properties.loadOverwriteCallTreeRuntimeData())
            );
        }
    }

    @Test
    void fullConfiguration() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            String props = "filter-method-names=org.noureddine.joularjx\n" +
                                "powermonitor-path=C:\\\\joularjx\\\\PowerMonitor.exe\n" +
                                "save-runtime-data=true\n"+
                                "overwrite-runtime-data=true\n"+
                                "track-consumption-evolution=true\n"+
                                "hide-agent-consumption=true\n"+
                                "enable-call-trees-consumption=true\n"+
                                "save-call-trees-runtime-data=true\n"+
                                "overwrite-call-trees-runtime-data=true";
            Files.write(fs.getPath("config.properties"), (props).getBytes(StandardCharsets.UTF_8));

            AgentProperties properties = new AgentProperties(fs);

            assertAll(
                    () -> assertTrue(properties.filtersMethod("org.noureddine.joularjx")),
                    () -> assertEquals("C:\\joularjx\\PowerMonitor.exe", properties.getPowerMonitorPath()),
                    () -> assertTrue(properties.savesRuntimeData()),
                    () -> assertTrue(properties.overwritesRuntimeData()),
                    () -> assertTrue(properties.trackConsumptionEvolution()),
                    () -> assertTrue(properties.hideAgentConsumption()),
                    () -> assertTrue(properties.callTreesConsumption()),
                    () -> assertTrue(properties.saveCallTreesRuntimeData()),
                    () -> assertTrue(properties.overwriteCallTreesRuntimeData())
            );
        }
    }

    @Test
    void multipleFilterMethods() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.write(fs.getPath("config.properties"),
                    "filter-method-names=org.noureddine.joularjx,org.noureddine.joularjx2".getBytes(StandardCharsets.UTF_8));

            AgentProperties properties = new AgentProperties(fs);

            assertAll(
                    () -> assertTrue(properties.filtersMethod("org.noureddine.joularjx")),
                    () -> assertTrue(properties.filtersMethod("org.noureddine.joularjx2")),
                    () -> assertNull(properties.getPowerMonitorPath())
            );
        }
    }

    @Test
    void capsBoolean() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.write(fs.getPath("config.properties"),
                    "save-runtime-data=TrUe\noverwrite-runtime-data=FaLse".getBytes(StandardCharsets.UTF_8));

            AgentProperties properties = new AgentProperties(fs);

            assertAll(
                    () -> assertTrue(properties.savesRuntimeData()),
                    () -> assertFalse(properties.overwritesRuntimeData())
            );
        }
    }

    static Stream<Arguments> getLogLevels() {
        return Stream.of(
                Arguments.of("INFO", Level.INFO),
                Arguments.of("OFF", Level.OFF),
                Arguments.of("SEVERE", Level.SEVERE),
                Arguments.of("WARNING", Level.WARNING),
                Arguments.of("FINE", Level.FINE),
                Arguments.of("CONFIG", Level.CONFIG),
                Arguments.of("ALL", Level.ALL),
                Arguments.of("FINER", Level.FINER),
                Arguments.of("FINEST", Level.FINEST)
        );
    }

    @ParameterizedTest
    @MethodSource("getLogLevels")
    void logLevel(final String level, final Level expected) throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.write(fs.getPath("config.properties"),
                    ("logger-level=" + level).getBytes(StandardCharsets.UTF_8));

            AgentProperties properties = new AgentProperties(fs);

            assertEquals(expected, properties.getLoggerLevel());
        }
    }
}