package org.noureddine.joularjx;

/**
 * Workload test to verify that JoularJX attributes energy metrics per thread.
 * Spawns two distinct named worker threads performing different calculations.
 */
public class PerThreadTest {

    /**
     * Main entry point to start the PerThreadTest workload.
     * Spawns Worker-A and Worker-B and joins them.
     * both threads are declared using lambda expressions (() -> { ... })
     * inside the main method, the Java compiler compiles them as two separate synthetic methods belonging to main
     * @param args the command-line arguments
     * @throws InterruptedException if any thread execution is interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting PerThreadTest workload...");
        
        Thread threadA = new Thread(() -> {
            long start = System.currentTimeMillis();
            double acc = 0;
            while (System.currentTimeMillis() - start < 8000) {
                acc += runWorkloadA();
            }
            System.out.println("Thread-A finished. Result: " + acc);
        }, "Worker-A");

        Thread threadB = new Thread(() -> {
            long start = System.currentTimeMillis();
            double acc = 0;
            while (System.currentTimeMillis() - start < 8000) {
                acc += runWorkloadB();
            }
            System.out.println("Thread-B finished. Result: " + acc);
        }, "Worker-B");

        threadA.start();
        threadB.start();

        threadA.join();
        threadB.join();

        System.out.println("PerThreadTest finished.");
    }

    /**
     * Executes Worker-A's heavy CPU floating point sine workload.
     *
     * @return the computation result
     */
    public static double runWorkloadA() {
        double val = 0;
        for (int i = 0; i < 5000; i++) {
            val += Math.sin(i);
        }
        return val;
    }

    /**
     * Executes Worker-B's heavy CPU floating point cosine workload.
     *
     * @return the computation result
     */
    public static double runWorkloadB() {
        double val = 0;
        for (int i = 0; i < 5000; i++) {
            val += Math.cos(i);
        }
        return val;
    }
}


// Total Energy consumption = Worker Threads (A + B) + java.io.FileInputStream.open0 - loading classes off the classpath)