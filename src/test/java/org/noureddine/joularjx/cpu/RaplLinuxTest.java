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

package org.noureddine.joularjx.cpu;

import com.ginsberg.junit.exit.ExpectSystemExitWithStatus;
import com.github.marschall.memoryfilesystem.MemoryFileSystemBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.logging.Level;

import static org.junit.jupiter.api.Assertions.*;

@EnabledOnOs(OS.LINUX)
class RaplLinuxTest {

    private FileSystem fileSystem;

    private Cpu cpu;

    @BeforeEach
    void init() throws IOException {
        JoularJXLogging.updateLevel(Level.INFO);
        fileSystem = MemoryFileSystemBuilder.newLinux().build();
        cpu = new RaplLinux(fileSystem);
    }

    @AfterEach
    void cleanup() throws Exception {
        fileSystem.close();
        cpu.close();
    }

    @Test
    @ExpectSystemExitWithStatus(1)
    void noRaplFilesFound() {
        cpu.initialize();
    }

    @Test
    void psysFileSupported() throws IOException {
        Path psys = fileSystem.getPath(RaplLinux.RAPL_PSYS);
        Path psysMax = fileSystem.getPath(RaplLinux.RAPL_PSYS_MAX);
        Files.createDirectories(psys.getParent());
        Files.writeString(psys, "1000000");
        Files.writeString(psysMax, "1000000");

        cpu.initialize();

        assertEquals(1.0, cpu.getCurrentPower(0));
    }

   @Test
    void pkgFileSupported() throws IOException {
        Path pkg = fileSystem.getPath(RaplLinux.RAPL_PKG);
        Path pkgMax = fileSystem.getPath(RaplLinux.RAPL_PKG_MAX);
        Files.createDirectories(pkg.getParent());
        Files.writeString(pkg, "1000000");
        Files.writeString(pkgMax, "1000000");

        cpu.initialize();

        assertEquals(1.0, cpu.getCurrentPower(0));
    }

    @Test
    void pkgAndDramFileSupported() throws IOException {
        Path pkg = fileSystem.getPath(RaplLinux.RAPL_PKG);
        Path dram = fileSystem.getPath(RaplLinux.RAPL_DRAM);

        Path pkgMax = fileSystem.getPath(RaplLinux.RAPL_PKG_MAX);
        Path dramMax = fileSystem.getPath(RaplLinux.RAPL_DRAM_MAX);

        Files.createDirectories(pkg.getParent());
        Files.createDirectories(dram.getParent());

        Files.writeString(pkg, "1000000");
        Files.writeString(dram, "1000000");
        Files.writeString(pkgMax, "1000000");
        Files.writeString(dramMax, "1000000");

        cpu.initialize();

        assertEquals(2.0, cpu.getCurrentPower(0));
    }

@Test
    @ExpectSystemExitWithStatus(1)
    void raplFileNotReadable() throws IOException {
        Path psys = fileSystem.getPath(RaplLinux.RAPL_PSYS);
        Path psysMax = fileSystem.getPath(RaplLinux.RAPL_PSYS_MAX);
        Files.createDirectories(psys.getParent());
        Files.writeString(psys, "1000000");
        Files.writeString(psysMax, "1000000");

        assertTrue(Files.isReadable(psys));
        assertTrue(Files.isWritable(psys));

        // Change file permissions, user and group
        Files.setPosixFilePermissions(psys, EnumSet.noneOf(PosixFilePermission.class));

        assertFalse(Files.isReadable(psys));
        assertFalse(Files.isWritable(psys));

        cpu.initialize();
    }
}
