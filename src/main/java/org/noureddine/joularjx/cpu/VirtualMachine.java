package org.noureddine.joularjx.cpu;

public class VirtualMachine implements Cpu {
    
    @Override
    public void initialize() {
        
    }

    @Override
    public double getInitialPower() {
        return 0;
    }

    @Override
    public double getCurrentPower(double cpuLoad) {
        return 0;
    }
    
    @Override
    public double getMaxPower(double cpuLoad) {
        return 0;
    }
    
    @Override
    public void close() {
        // Nothign to do for virtual machines
    }
    
}