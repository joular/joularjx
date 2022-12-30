package org.noureddine.joularjx.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitoringStatus {

    private final Object consumedEnergyLock;

    private final Map<String, Double> methodsConsumedEnergy;
    private final Map<String, Double> filteredMethodsConsumedEnergy;

    private double totalConsumedEnergy;

    public MonitoringStatus() {
        this.consumedEnergyLock = new Object();
        this.methodsConsumedEnergy = new ConcurrentHashMap<>();
        this.filteredMethodsConsumedEnergy = new ConcurrentHashMap<>();

        this.totalConsumedEnergy = 0;
    }

    public void addConsumedEnergy(double delta) {
        synchronized(consumedEnergyLock) {
            totalConsumedEnergy += delta;
        }
    }

    public void addMethodConsumedEnergy(String methodName, double delta) {
        methodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    public void addFilteredMethodConsumedEnergy(String methodName, double delta) {
        filteredMethodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    public double getTotalConsumedEnergy() {
        synchronized(consumedEnergyLock) {
            return totalConsumedEnergy;
        }
    }

    public Map<String, Double> getMethodsConsumedEnergy() {
        return Collections.unmodifiableMap(methodsConsumedEnergy);
    }

    public Map<String, Double> getFilteredMethodsConsumedEnergy() {
        return Collections.unmodifiableMap(filteredMethodsConsumedEnergy);
    }
}
