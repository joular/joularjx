package org.noureddine.joularjx.cpu;

import org.noureddine.joularjx.utils.JoularJXLogging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link Cpu} implementation using <a href='https://firefox-source-docs.mozilla.org/performance/powermetrics.htm'>powermetrics</a>.
 */
public class PowermetricsMacOS implements Cpu {
    private static final Logger logger = JoularJXLogging.getLogger();
    private static final String POWER_INDICATOR = " Power: ";
    private static final int POWER_INDICATOR_LENGTH = POWER_INDICATOR.length();
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
        int headerLinesToSkip = 10;
        int powerInMilliwatts = 0;
        try {
            // Should not be closed since it closes the process, so no try-with-resource
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean processingPower = false;
            while ((line = input.readLine()) != null) {
                if (headerLinesToSkip != 0) {
                    headerLinesToSkip--;
                    continue;
                }

                // skip empty / header lines
                if (line.isEmpty() || line.startsWith("*")) {
                    continue;
                }

                // looking for line fitting the: "<name> Power: xxx mW" pattern and add all of the associated values together
                final var powerIndicatorIndex = line.indexOf(POWER_INDICATOR);

                // we need an exit condition to avoid looping forever (since there are always new lines, the process being periodical)
                // if we started processing power lines and we don't find any anymore, we've reached the end of this "page" so exit the loop
                if(processingPower && powerIndicatorIndex < 0) {
                    break;
                }

                // lines with `-` as the second char are disregarded as of the form: "E-Cluster Power: 6 mW" which fits the pattern but shouldn't be considered
                // also ignore Combined Power if available since it is the sum of the other components
                if (powerIndicatorIndex >= 0 && '-' != line.charAt(1) && !line.startsWith("Combined")) {
                    powerInMilliwatts += extractPowerInMilliwatts(line, powerIndicatorIndex);
                    processingPower = true; // record we're in the power lines section of the powermetrics output
                }
            }
            return (double) powerInMilliwatts / 1000;
        } catch (IOException e) {
            logger.throwing(getClass().getName(), "getCurrentPower", e);
        }

        return 0.0;
    }

    private static int extractPowerInMilliwatts(String line, int powerIndex) {
        try {
            return Integer.parseInt(line.substring(powerIndex + POWER_INDICATOR_LENGTH, line.indexOf('m') - 1));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Cannot parse power value from line '" + line + "'", e);
        }
        return 0;
    }

    @Override
    public void close() {
        if (initialized) {
            process.destroy();
        }
    }
}
