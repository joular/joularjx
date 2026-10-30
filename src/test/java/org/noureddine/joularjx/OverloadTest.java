package org.noureddine.joularjx;

/**
 * Workload test to verify that JoularJX can resolve exact method signatures
 * for overloaded methods (e.g., matching parameter types) by inspecting bytecode.
 */
public class OverloadTest {

    /**
     * Entry point for running the OverloadTest workload.
     * Runs for a duration of 10 seconds calling both process(int) and process(double).
     *
     * @param args the command-line arguments
     * @throws InterruptedException if the execution is interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting OverloadTest workload...");
        long start = System.currentTimeMillis();
        double total = 0;
        // Run for about 10 seconds
        while (System.currentTimeMillis() - start < 10000) {
            total += process(42);
            total += process(4.2);
        }
        System.out.println("OverloadTest workload finished. Result: " + total);
    }

    /**
     * Performs a floating point sine accumulation using an integer parameter.
     *
     * @param x the integer input
     * @return the accumulated sum
     */
    public static double process(int x) {
        double acc = 0;
        for (int i = 0; i < 5000; i++) {
            acc += Math.sin(x + i);
        }
        return acc;
    }

    /**
     * Performs a floating point cosine accumulation using a double parameter.
     *
     * @param x the double input
     * @return the accumulated sum
     */
    public static double process(double x) {
        double acc = 0;
        for (int i = 0; i < 5000; i++) {
            acc += Math.cos(x + i);
        }
        return acc;
    }
}
