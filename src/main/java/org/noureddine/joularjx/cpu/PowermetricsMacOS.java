package org.noureddine.joularjx.cpu;

import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link Cpu} implementation using <a href='https://firefox-source-docs.mozilla.org/performance/powermetrics.htm'>powermetrics</a>.
 */
public class PowermetricsMacOS implements Cpu {
    private static final Logger logger = JoularJXLogging.getLogger();
    private Process process;
    private boolean initialized;

    @Override
    public void initialize() {
        if(initialized) {
            return;
        }
        
        try {
            process = Runtime.getRuntime().exec("sudo powermetrics --samplers cpu_power -i 500");
            

            initialized = true;
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Can''t start powermetrics. Exiting...");
            logger.throwing(getClass().getName(), "initialize", exception);
            System.exit(1);
        }
    }

    @Override
    public double getInitialPower() {
        return 0;
    }

    @Override
    public double getCurrentPower(double cpuLoad) {
        try {
            // Should not be closed since it closes the process
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                // look for line that contains combined power measurement
                // which should look similar to: `Combined Power (CPU + GPU + ANE): 377 mW`
                if (!line.startsWith("Combined")) {
                    continue;
                }
                final var powerValue = line.split(":")[1];
                final var powerInMilliwatts = powerValue.split("m")[0];
                return Double.parseDouble(powerInMilliwatts) / 1000;
            }
        } catch (Exception exception) {
            logger.throwing(getClass().getName(), "getCurrentPower", exception);
        }
        return 0;
    }

    @Override
    public void close() throws Exception {
        if (initialized) {
            process.destroy();
        }
    }
}
