/*
 * Copyright (c) 2021-2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
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
                return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/powermetrics-sonoma-m1max.txt")));
            }
        };
        cpu.intelCpu = false;


        // the ??-Cluster and Combined lines are to be ignored, hence do not count the 359mW
        assertEquals(0.211d + 0.147d + 0d /* +0.359d */, cpu.getCurrentPower(0), 0.0001d);
    }

    @Test
    void parseMontereyM2PowerLines() {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/powermetrics-monterey-m2.txt")));
            }
        };
        cpu.intelCpu = false;

        // the ??-Cluster and Combined lines are to be ignored, hence do not count the 6mW
        assertEquals(/*0.006d*/ + 0d + 0.019d + 0.036d + 0.010d + 0d + 0.025d , cpu.getCurrentPower(0), 0.0001d);
    }

    @Test
    void parseSonomaIntelPowerLines() {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/powermetrics-sonoma-intel.txt")));
            }
        };

        cpu.intelCpu = true;

        assertEquals(4.87d + 3.43d + 3.38d + 4.21d + 3.21d , cpu.getCurrentPower(0), 0.0001d);
    }


    @Test
    void parseHeaderIntel() throws IOException {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/powermetrics-sonoma-intel.txt")));
            }
        };

        cpu.readHeader();
        assertTrue(cpu.intelCpu);
    }

    @Test
    void parseHeaderM1() throws IOException {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/powermetrics-monterey-m2.txt")));
            }
        };

        cpu.readHeader();
        assertFalse(cpu.intelCpu);
    }

    /**
     * Test if the reader returns whenever there are results.
     * @throws IOException
     */
    @Test
    void testIntermittentResults() throws IOException, URISyntaxException, InterruptedException {
        // hookup a writer to a reader
        PipedInputStream intermittentInputStream = new PipedInputStream();
        PipedOutputStream outputStream = new PipedOutputStream(intermittentInputStream);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        BufferedReader reader = new BufferedReader(new InputStreamReader(intermittentInputStream));

        // create the content blocks including headers
        final String contents1 = "\n".repeat(10) + "CPU Power: 742 mW\n".repeat(2);
        final String contents2 = "\n".repeat(10) + "CPU Power: 1200 mW\n".repeat(2);

        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return reader;
            }
        };

        // nothing written yet, so expect 0
        assertEquals(0d, cpu.getCurrentPower(0), 0.0001d);

        Thread writerBlock1 = createWriter(writer, contents1);
        writerBlock1.start();
        writerBlock1.join();
        assertEquals(2*0.742d, cpu.getCurrentPower(0), 0.0001d);

        Thread writerBlock2 = createWriter(writer, contents2);
        writerBlock2.start();
        writerBlock2.join();
        assertEquals(2*1.2d, cpu.getCurrentPower(0), 0.0001d);
    }

    /**
     * Create a thread that writes the contents to the writer, simulating the actual process.
     * @param writer the writer to write to
     * @param contents the contents to write
     * @return a thread
     */
    private static Thread createWriter(BufferedWriter writer, String contents) {
        Thread writerBlock1 = new Thread() {
            @Override
            public void run() {
                try {
                    writer.write(contents);
                    writer.flush();
                } catch(Exception e)  {
                    throw new RuntimeException(e);
                }
            }
        };
        return writerBlock1;
    }


}
