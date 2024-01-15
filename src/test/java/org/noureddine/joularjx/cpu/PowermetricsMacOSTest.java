package org.noureddine.joularjx.cpu;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PowermetricsMacOSTest {

    @Test
    void parseM1M2PowerLines() {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/powermetrics-m1-m2.txt")));
            }
        };

        // the ??-Cluster lines are to be ignored, hence do not count the 100mW
        assertEquals(/*0.100d +*/ 0d + 0.688d+0.742d + 0.026d + 2.151d, cpu.getCurrentPower(0), 0.0001d);
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
        final String contents2 = "\n".repeat(10) + "CPU Power: 1.2W\n".repeat(2);

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

    @Test
    void parseIntelPowerLines() {
        PowermetricsMacOS cpu = new PowermetricsMacOS() {
            @Override
            protected BufferedReader getReader() {
                return new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/powermetrics-intel.txt")));
            }
        };

        assertEquals(4.87d + 3.43d + 3.38d + 4.21d + 3.21d , cpu.getCurrentPower(0), 0.0001d);
    }

}
