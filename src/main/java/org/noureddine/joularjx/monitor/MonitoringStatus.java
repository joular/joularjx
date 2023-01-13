package org.noureddine.joularjx.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitoringStatus {

    private final Object consumedEnergyLock;
    private final Map<String, Double> methodsConsumedEnergy;
    private final Map<String, Double> filteredMethodsConsumedEnergy;

    //Method name -> [timestamp -> consumption]
    private final Map<String, Map<Long, Double>> methodsConsumptionEvolution;
    private final Map<String, Map<Long, Double>> filteredMethodsConsumptionEvolution;

    private double totalConsumedEnergy;

    public MonitoringStatus() {
        this.consumedEnergyLock = new Object();
        this.methodsConsumedEnergy = new ConcurrentHashMap<>();
        this.filteredMethodsConsumedEnergy = new ConcurrentHashMap<>();
        this.methodsConsumptionEvolution = new ConcurrentHashMap<>();
        this.filteredMethodsConsumptionEvolution = new ConcurrentHashMap<>();

        this.totalConsumedEnergy = 0;
    }

    public void addConsumedEnergy(double delta) {
        synchronized (consumedEnergyLock) {
            totalConsumedEnergy += delta;
        }
    }

    public void addMethodConsumedEnergy(String methodName, double delta) {
        methodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    public void addFilteredMethodConsumedEnergy(String methodName, double delta) {
        filteredMethodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    public void trackMethodConsumption(String methodName, long timestamp, double energy) {
        if (!this.methodsConsumptionEvolution.containsKey(methodName)) {
            //Creating a new map for tracking the method's energy consumption over the time
            Map<Long, Double> methodEnergyTrackMap = new ConcurrentHashMap<>();
            methodEnergyTrackMap.put(timestamp, energy);

            this.methodsConsumptionEvolution.put(methodName, methodEnergyTrackMap);
        } else {
            this.methodsConsumptionEvolution.get(methodName).put(timestamp, energy);
        }
    }

    public void trackFilteredMethodConsumption(String methodName, long timestamp, double energy) {
        if (!this.filteredMethodsConsumptionEvolution.containsKey(methodName)) {
            Map<Long, Double> methodEnergyTrackMap = new ConcurrentHashMap<>();
            methodEnergyTrackMap.put(timestamp, energy);

            this.filteredMethodsConsumptionEvolution.put(methodName, methodEnergyTrackMap);
        } else {
            this.filteredMethodsConsumptionEvolution.get(methodName).put(timestamp, energy);
        }
    }

    public double getTotalConsumedEnergy() {
        synchronized (consumedEnergyLock) {
            return totalConsumedEnergy;
        }
    }

    public Map<String, Double> getMethodsConsumedEnergy() {
        return Collections.unmodifiableMap(methodsConsumedEnergy);
    }

    public Map<String, Double> getFilteredMethodsConsumedEnergy() {
        return Collections.unmodifiableMap(filteredMethodsConsumedEnergy);
    }

    public Map<String, Map<Long, Double>> getMethodsConsumptionEvolution(){
        return this.methodsConsumptionEvolution;
    }

    public Map<String, Map<Long, Double>> getFilteredMethodsConsumptionEvolution(){
        return this.filteredMethodsConsumptionEvolution;
    }
}
