package org.noureddine.joularjx.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.noureddine.joularjx.utils.StackTrace;

public class MonitoringStatus {

    private final Object consumedEnergyLock;
    private final Map<String, Double> methodsConsumedEnergy;
    private final Map<String, Double> filteredMethodsConsumedEnergy;

    //Map method names to a Map of timestamps mapped to energy consumption
    private final Map<String, Map<Long, Double>> methodsConsumptionEvolution;
    private final Map<String, Map<Long, Double>> filteredMethodsConsumptionEvolution;

    //Map StackTraces to their energy consumption
    private final Map<StackTrace, Double> stackTracesConsumption;

    private double totalConsumedEnergy;

    /**
     * Constructor for the MonitoringStatus class. Initializes empty data structures and sets the total consumed energy to zero.
     */
    public MonitoringStatus() {
        this.consumedEnergyLock = new Object();
        this.methodsConsumedEnergy = new ConcurrentHashMap<>();
        this.filteredMethodsConsumedEnergy = new ConcurrentHashMap<>();
        this.methodsConsumptionEvolution = new ConcurrentHashMap<>();
        this.filteredMethodsConsumptionEvolution = new ConcurrentHashMap<>();
        this.stackTracesConsumption = new ConcurrentHashMap<>();

        this.totalConsumedEnergy = 0;
    }

    /**
     * Adds the given energy to the total consumed energy count.
     * @param delta a double, the amount of energy to be added
     */
    public void addConsumedEnergy(double delta) {
        synchronized (consumedEnergyLock) {
            totalConsumedEnergy += delta;
        }
    }

    /**
     * Adds the given energy consumption to the given method.
     * @param methodName a String, the method name to which the consumption is mapped
     * @param delta a double, the amount of energy consumption to be added
     */
    public void addMethodConsumedEnergy(String methodName, double delta) {
        methodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    /**
     * Adds the given energy consumption to the given filtered method.
     * @param methodName a String, the filtered method name to which the consumption is mapped
     * @param delta a souble, the amount of energy to be added
     */
    public void addFilteredMethodConsumedEnergy(String methodName, double delta) {
        filteredMethodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    public void addStackTraceConsumedEnergy(StackTrace stackTrace, double delta) {
        this.stackTracesConsumption.merge(stackTrace, delta, Double::sum);
    }

    /**
     * Tracks the method's energy consumption over the time. Maps the given UNIX timestamp to the given energy consumption for the given method name. 
     * Creates or update the needed data structures.
     * @param methodName a String, the name of the method
     * @param timestamp a long representing an UNIX timestamp
     * @param energy a double, the method energy consomption
     */
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

    /**
     * Tracks the filtered method's energy consumption over the time. Maps the given UNIX timestamp to the given energy consumption for the given method name. 
     * Creates or update the needed data structures.
     * @param methodName a String, the name of the method
     * @param timestamp a long representing an UNIX timestamp
     * @param energy a double, the method energy consomption
     */
    public void trackFilteredMethodConsumption(String methodName, long timestamp, double energy) {
        if (!this.filteredMethodsConsumptionEvolution.containsKey(methodName)) {
            Map<Long, Double> methodEnergyTrackMap = new ConcurrentHashMap<>();
            methodEnergyTrackMap.put(timestamp, energy);

            this.filteredMethodsConsumptionEvolution.put(methodName, methodEnergyTrackMap);
        } else {
            this.filteredMethodsConsumptionEvolution.get(methodName).put(timestamp, energy);
        }
    }

    /**
     * Returns the total consumed energy since the monitoring started.
     * @return a double, the total consumed energy.
     */
    public double getTotalConsumedEnergy() {
        synchronized (consumedEnergyLock) {
            return totalConsumedEnergy;
        }
    }

    /**
     * Returns the energy consumed by each method.
     * @return a Map<String, Double> mapping method's name to their respective energy consumption.
     */
    public Map<String, Double> getMethodsConsumedEnergy() {
        return Collections.unmodifiableMap(methodsConsumedEnergy);
    }

    /**
     * Returns the energy consumed by each filtered method.
     * @return a Map<String, Double> mapping filtered method's name to their respective energy consumption.
     */
    public Map<String, Double> getFilteredMethodsConsumedEnergy() {
        return Collections.unmodifiableMap(filteredMethodsConsumedEnergy);
    }

    /**
     * Returns the energy consumption evolution of each method.
     * @return a Map<String, Map<Long,Double>> mapping each method's name to their consumption evolution. Consumption evolution is stored as a Map<Long,Double> mapping UNIX timestamps to energy consumption.
     */
    public Map<String, Map<Long, Double>> getMethodsConsumptionEvolution(){
        return this.methodsConsumptionEvolution;
    }

    /**
     * Returns the energy consumption evolution of each filtered method.
     * @return a Map<String, Map<Long,Double>> mapping each filtered method's name to their consumption evolution. Consumption evolution is stored as a Map<Long,Double> mapping UNIX timestamps to energy consumption.
     */
    public Map<String, Map<Long, Double>> getFilteredMethodsConsumptionEvolution(){
        return this.filteredMethodsConsumptionEvolution;
    }

    public Map<StackTrace, Double> getStackTracesConsumedEnergy() {
        return this.stackTracesConsumption;
    }
}
