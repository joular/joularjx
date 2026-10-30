package org.noureddine.joularjx;

import java.math.BigInteger;

public class RecursionTest {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting RecursionTest workload...");
        long start = System.currentTimeMillis();
        // Run for about 10 seconds to allow JoularJX to sample multiple times
        while (System.currentTimeMillis() - start < 10000) {
            factorial(BigInteger.valueOf(200));
        }
        System.out.println("RecursionTest workload finished.");
    }

    public static BigInteger factorial(BigInteger n) {
        if (n.equals(BigInteger.ZERO)) {
            return BigInteger.ONE;
        }
        BigInteger result = factorial(n.subtract(BigInteger.ONE));
        result = result.multiply(n);
        return result;
    }
}
