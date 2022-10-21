package org.noureddine.joularjx.utils;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                    () -> assertTrue(properties.getFilterMethodNames().isEmpty()),
                    () -> assertNull(properties.getPowerMonitorPath())
            );
        }
    }

    @Test
    void fullConfiguration() throws IOException {
        try (final FileSystem fs = MemoryFileSystemBuilder.newEmpty().build()) {
            Files.write(fs.getPath("config.properties"), ("filter-method-names=org.noureddine.joularjx\n" +
                    "powermonitor-path=C:\\\\joularjx\\\\PowerMonitor.exe").getBytes(StandardCharsets.UTF_8));

            AgentProperties properties = new AgentProperties(fs);

            assertAll(
                    () -> assertEquals(List.of("org.noureddine.joularjx"), properties.getFilterMethodNames()),
                    () -> assertEquals("C:\\joularjx\\PowerMonitor.exe", properties.getPowerMonitorPath())
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
                    () -> assertEquals(List.of("org.noureddine.joularjx", "org.noureddine.joularjx2"), properties.getFilterMethodNames()),
                    () -> assertNull(properties.getPowerMonitorPath())
            );
        }
    }
}