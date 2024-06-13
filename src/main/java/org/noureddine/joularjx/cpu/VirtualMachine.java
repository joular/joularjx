package org.noureddine.joularjx.cpu;

import org.noureddine.joularjx.utils.JoularJXLogging;
import java.util.logging.Logger;
import java.nio.file.Files;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.FileSystem;

public class VirtualMachine implements Cpu {
    
    private static final Logger logger = JoularJXLogging.getLogger();

    private static String VM_POWER_PATH_NAME;

    private static String VM_POWER_FORMAT;

    private Path VM_POWER_PATH;

    private final FileSystem fileSystem;

    public VirtualMachine() {
        this(FileSystems.getDefault());
    }

    public VirtualMachine(final FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public void initialize() {
        // initialize VM_POWER_PATH and VM_POWER_FORMAT
        this.VM_POWER_PATH = fileSystem.getPath(VM_POWER_PATH_NAME);
        // todo: check if file exists and readable
    }

    /**
     * The power is approximated based on the CPU load, so it does not need an offset.
     *
     * @return 0
     */
    @Override
    public double getInitialPower() {
        return 0;
    }

    @Override
    public double getCurrentPower(double cpuLoad) {
        double powerData = 0.0;

        try {
            powerData += Double.parseDouble(Files.readString(VM_POWER_PATH));
            // todo: check for power format (powerjoular or watts)
        } catch (IOException exception) {
            logger.throwing(getClass().getName(), "getCurrentPower", exception);
        }

        return powerData;
    }
    
    /**
     * Nothing to do here. Method only useful for RAPL
     */
    @Override
    public double getMaxPower(double cpuLoad) {
        return 0;
    }
    
    @Override
    public void close() {
        // Nothign to do for virtual machines
    }
    
}