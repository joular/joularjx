/*
 * Copyright (c) 2021-2026, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.cpu;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

public class PowermetricsMacOSTest {

    @Test
    void parseSonomaM1MaxPowerLines() {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream("/powermetrics-sonoma-m1max.txt")));
            }
        };
        cpu.intelCpu = false;
        cpu.processStream(cpu.getReader()); // synchronously parse the mock file to EOF

        // the ??-Cluster and Combined lines are to be ignored, hence do not count the
        // 359mW
        assertEquals(0.211d + 0.147d + 0d /* +0.359d */, cpu.getCurrentPower(0), 0.0001d);
        cpu.close();
    }

    @Test
    void parseMontereyM2PowerLines() {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream("/powermetrics-monterey-m2.txt")));
            }
        };
        cpu.intelCpu = false;
        cpu.processStream(cpu.getReader());

        // the ??-Cluster and Combined lines are to be ignored, hence do not count the
        // 6mW
        assertEquals(/* 0.006d */ +0d + 0.019d + 0.036d + 0.010d + 0d + 0.025d, cpu.getCurrentPower(0), 0.0001d);
        cpu.close();
    }

    @Test
    void parseSonomaIntelPowerLines() {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream("/powermetrics-sonoma-intel.txt")));
            }
        };

        cpu.intelCpu = true;
        cpu.processStream(cpu.getReader());

        // The background thread logic now correctly parses discrete blocks and
        // publishes the latest block reading.
        // The last block in the mock file has a power of 3.21W.
        assertEquals(3.21d, cpu.getCurrentPower(0), 0.0001d);
        cpu.close();
    }

    @Test
    void parseHeaderIntel() throws IOException {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream("/powermetrics-sonoma-intel.txt")));
            }
        };

        cpu.readHeader();
        assertTrue(cpu.intelCpu);
        cpu.close();
    }

    @Test
    void parseHeaderM1() throws IOException {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(
                        new InputStreamReader(getClass().getResourceAsStream("/powermetrics-monterey-m2.txt")));
            }
        };

        cpu.readHeader();
        assertFalse(cpu.intelCpu);
        cpu.close();
    }

    /**
     * Test if the reader returns whenever there are results.
     * 
     * @throws IOException
     */
    @Test
    void testIntermittentResults() throws IOException, URISyntaxException, InterruptedException {
        // hookup a writer to a reader
        PipedInputStream intermittentInputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(intermittentInputStream);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        BufferedReader reader = new BufferedReader(new InputStreamReader(intermittentInputStream));

        // Note: The processor only publishes results on the next block marker ("***")
        // or EOF.
        // We simulate a stream that outputs markers continuously.
        final String contents1 = "*** Sampled system activity ***\n" + "CPU Power: 742 mW\n".repeat(2)
                + "*** Next Sample ***\n";
        final String contents2 = "CPU Power: 1200 mW\n".repeat(2);

        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return reader;
            }
        };
        cpu.intelCpu = false;

        // Start async logic
        cpu.startReaderThread();

        // nothing written yet, so expect 0
        assertEquals(0d, cpu.getCurrentPower(0), 0.0001d);

        // Write block 1
        writer.write(contents1);
        writer.flush();

        // Let CPU thread process it. It will see "*** Next Sample ***" and update
        // currentPower.
        Thread.sleep(100);
        assertEquals(2 * 0.742d, cpu.getCurrentPower(0), 0.0001d);

        // Write block 2
        writer.write(contents2);
        writer.flush();

        // At this point, no new block marker has been sent, so the power shouldn't be
        // updated yet!
        assertEquals(2 * 0.742d, cpu.getCurrentPower(0), 0.0001d);

        // Now close the stream to trigger EOF flush.
        writer.close();
        Thread.sleep(100);

        assertEquals(2 * 1.2d, cpu.getCurrentPower(0), 0.0001d);

        // Clean up
        cpu.close();
    }

}
