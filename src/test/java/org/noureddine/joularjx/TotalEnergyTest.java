package org.noureddine.joularjx;

/**
 * Workload test to verify that the sum of self energy of all methods
 * is consistent with the total energy of the main method.
 */
public class TotalEnergyTest {

    /**
     * Main entry point to run the TotalEnergyTest workload.
     * Runs for a duration of 10 seconds.
     *
     * @param args the command-line arguments
     * @throws InterruptedException if the execution is interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting TotalEnergyTest workload...");
        long start = System.currentTimeMillis();
        double sum = 0;
        // Run for 10 seconds
        while (System.currentTimeMillis() - start < 10000) {
            sum += runHeavyMath();
        }
        System.out.println("TotalEnergyTest finished. Result: " + sum);
    }

    /**
     * Executes CPU-heavy math operations to verify mathematical consistency
     * in the agent's energy mapping outputs.
     *
     * @return the result of calculation
     */
    public static double runHeavyMath() {
        double acc = 0;
        for (int i = 0; i < 5000; i++) {
            acc += Math.sqrt(i) * Math.sin(i) - Math.cos(i);
        }
        return acc;
    }
}
