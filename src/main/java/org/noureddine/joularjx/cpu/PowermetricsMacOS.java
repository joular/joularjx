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
            // todo: detect when sudo fails as this currently won't throw an exception
            process = Runtime.getRuntime().exec("sudo powermetrics --samplers cpu_power -i 1000");
            initialized = true;
        } catch (Exception exception) {
            logger.log(Level.SEVERE, "Can't start powermetrics. Exiting...");
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
            	// it seems this output is for older versions (?), current version (Sonoma 14.0) gives the following line:
            	// `Intel energy model derived package power (CPUs+GT+SA): 48.61W`
                if (!line.startsWith("Intel energy model derived package power")) {
                    continue;
                }
                final var powerValue = line.split(":")[1];
                final var powerInMilliwatts = powerValue.split("W")[0];
                //logger.info("Current power (w): " + powerInMilliwatts);
                return Double.parseDouble(powerInMilliwatts); // / 1000; // NOTE not needed to divide by 1000 since the values is not in mW!
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
