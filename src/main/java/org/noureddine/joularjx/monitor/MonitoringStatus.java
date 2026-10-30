// Features proposed by Rushin Dipak Makwana under supervision of Dr. Denis Pankratov and Dr. Yann-Gael Gueheneuc
/*
 * Copyright (c) 2021-2026, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.monitor;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.noureddine.joularjx.utils.CallTree;

/**
 * The MonitoringStatus contains several data structures used to save runtime data and global consumption values.
 */
public class MonitoringStatus {

    private final Object consumedEnergyLock;
    private final Map<String, Double> methodsConsumedEnergy;
    private final Map<String, Double> filteredMethodsConsumedEnergy;
    private final Map<String, Double> totalMethodsConsumedEnergy;
    private final Map<String, Double> totalFilteredMethodsConsumedEnergy;


    //Map method names to a Map of timestamps mapped to energy consumption
    private final Map<String, Map<Long, Double>> methodsConsumptionEvolution;
    private final Map<String, Map<Long, Double>> filteredMethodsConsumptionEvolution;

    //Map CallTrees to their energy consumption
    private final Map<CallTree, Double> callTreesConsumption;
    private final Map<CallTree, Double> filteredCallTreesConsumption;

    private double totalConsumedEnergy;

    private final Map<Thread, Map<String, Double>> methodsConsumedEnergyPerThread;
    private final Map<Thread, Map<String, Double>> filteredMethodsConsumedEnergyPerThread;
    private final Map<Thread, Map<String, Double>> totalMethodsConsumedEnergyPerThread;
    private final Map<Thread, Map<String, Double>> totalFilteredMethodsConsumedEnergyPerThread;

    private final Map<Thread, Map<String, Map<Long, Double>>> methodsConsumptionEvolutionPerThread;
    private final Map<Thread, Map<String, Map<Long, Double>>> filteredMethodsConsumptionEvolutionPerThread;

    private final Map<Thread, Map<CallTree, Double>> callTreesConsumptionPerThread;
    private final Map<Thread, Map<CallTree, Double>> filteredCallTreesConsumptionPerThread;

    private final Map<Thread, Double> totalConsumedEnergyPerThread;

    /**
     * Constructor for the MonitoringStatus class. Initializes empty data structures and sets the total consumed energy to zero.
     */
    public MonitoringStatus() {
        this.consumedEnergyLock = new Object();
        this.methodsConsumedEnergy = new ConcurrentHashMap<>();
        this.filteredMethodsConsumedEnergy = new ConcurrentHashMap<>();
        this.totalMethodsConsumedEnergy = new ConcurrentHashMap<>();
        this.totalFilteredMethodsConsumedEnergy = new ConcurrentHashMap<>();
        this.methodsConsumptionEvolution = new ConcurrentHashMap<>();
        this.filteredMethodsConsumptionEvolution = new ConcurrentHashMap<>();
        this.callTreesConsumption = new ConcurrentHashMap<>();
        this.filteredCallTreesConsumption = new ConcurrentHashMap<>();

        this.totalConsumedEnergy = 0;

        this.methodsConsumedEnergyPerThread = new ConcurrentHashMap<>();
        this.filteredMethodsConsumedEnergyPerThread = new ConcurrentHashMap<>();
        this.totalMethodsConsumedEnergyPerThread = new ConcurrentHashMap<>();
        this.totalFilteredMethodsConsumedEnergyPerThread = new ConcurrentHashMap<>();

        this.methodsConsumptionEvolutionPerThread = new ConcurrentHashMap<>();
        this.filteredMethodsConsumptionEvolutionPerThread = new ConcurrentHashMap<>();

        this.callTreesConsumptionPerThread = new ConcurrentHashMap<>();
        this.filteredCallTreesConsumptionPerThread = new ConcurrentHashMap<>();

        this.totalConsumedEnergyPerThread = new ConcurrentHashMap<>();
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
     * @param delta a double, the amount of self energy consumption to be added
     */
    public void addMethodConsumedEnergy(String methodName, double delta) {
        methodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    /**
     * Adds the given energy consumption to the given filtered method.
     * @param methodName a String, the filtered method name to which the consumption is mapped
     * @param delta a double, the amount of self energy to be added
     */
    public void addFilteredMethodConsumedEnergy(String methodName, double delta) {
        filteredMethodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

   /**  Adds the given energy consumption to the given method.
     * @param methodName a String, the method name to which the consumption is mapped
     * @param delta a double, the amount of total energy consumption to be added
     */
    public void addTotalMethodConsumedEnergy(String methodName, double delta) {
        totalMethodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    /**
     * Adds the given energy consumption to the given filtered method.
     * @param methodName a String, the filtered method name to which the consumption is mapped
     * @param delta a double, the amount of total energy to be added
     */
    public void addTotalFilteredMethodConsumedEnergy(String methodName, double delta) {
         totalFilteredMethodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    /**
     * Adds the given energy consumption to the given call tree.
     * @param callTree a {@link CallTree} the call tree to which the consumption is mapped
     * @param delta a double, the amount of energy to be added
     */
    public void addCallTreeConsumedEnergy(CallTree callTree, double delta) {
        this.callTreesConsumption.merge(callTree, delta, Double::sum);
    }

    /**
     * Adds the given energy consumption to the given filtered call tree
     * @param callTree a {@link CallTree}, the call tree to which the consumption is mapped
     * @param delta a double, the amount of energy to be added
     */
    public void addFilteredCallTreeConsumedEnergy(CallTree callTree, double delta) {
        this.filteredCallTreesConsumption.merge(callTree, delta, Double::sum);
    }

    /**
     * Tracks the method's energy consumption over the time. Maps the given UNIX timestamp to the given energy consumption for the given method name. 
     * Creates or update the needed data structures.
     * @param methodName a String, the name of the method
     * @param timestamp a long representing an UNIX timestamp
     * @param energy a double, the method energy consumption
     */
    public void trackMethodConsumption(String methodName, long timestamp, double energy) {
        if (!this.methodsConsumptionEvolution.containsKey(methodName)) {
            // Creating a new map for tracking the method's energy consumption over the time
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
     * @param energy a double, the method energy consumption
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
     * @return a {@code Map<String, Double>} mapping method's name to their respective energy consumption.
     */
    public Map<String, Double> getMethodsConsumedEnergy() {
        return Collections.unmodifiableMap(methodsConsumedEnergy);
    }

    /**
     * Returns the energy consumed by each filtered method.
     * @return a {@code Map<String, Double>} mapping filtered method's name to their respective energy consumption.
     */
    public Map<String, Double> getFilteredMethodsConsumedEnergy() {
        return Collections.unmodifiableMap(filteredMethodsConsumedEnergy);
    }
    /**
     * Returns the total energy consumed by each method.
     * @return a {@code Map<String, Double>} mapping method's name to their respective total energy consumption.
     */
    public Map<String, Double> getMethodsTotalConsumedEnergy() {
        return Collections.unmodifiableMap(totalMethodsConsumedEnergy);
    }

    /**
     * Returns the total energy consumed by each filtered method.
     * @return a {@code Map<String, Double>} mapping filtered method's name to their respective total energy consumption.
     */
    public Map<String, Double> getFilteredMethodsTotalConsumedEnergy() {
        return Collections.unmodifiableMap(totalFilteredMethodsConsumedEnergy);
    }
    /**
     * Returns the energy consumption evolution of each method.
     * @return a {@code Map<String, Map<Long,Double>>} mapping each method's name to their consumption evolution. 
     * Consumption evolution is stored as a {@code Map<Long,Double>} mapping UNIX timestamps to energy consumption.
     */
    public Map<String, Map<Long, Double>> getMethodsConsumptionEvolution(){
        return this.methodsConsumptionEvolution;
    }

    /**
     * Returns the energy consumption evolution of each filtered method.
     * @return a {@code Map<String, Map<Long,Double>>} mapping each filtered method's name to their consumption evolution. 
     * Consumption evolution is stored as a {@code Map<Long,Double>} mapping UNIX timestamps to energy consumption.
     */
    public Map<String, Map<Long, Double>> getFilteredMethodsConsumptionEvolution(){
        return this.filteredMethodsConsumptionEvolution;
    }

    /**
     * Returns the energy consumption of each call tree.
     * @return a {@code Map<CallTree, Double>} mapping each call tree to their total energy consumption.
     */
    public Map<CallTree, Double> getCallTreesConsumedEnergy() {
        return this.callTreesConsumption;
    }

    /**
     * Returns the energy consumption of each filtered call tree.
     * @return a {@code Map<CallTree, Double>} mapping each filtered call tree to their total energy consumption.
     */
    public Map<CallTree, Double> getFilteredCallTreesConsumedEnergy() {
        return this.filteredCallTreesConsumption;
    }

    /**
     * To distinguish among the threads (worker threads and non-active/sleeping threads)- ( Feature #2)
     */
    public void addMethodConsumedEnergyPerThread(Thread th, String methodName, double delta) {
        methodsConsumedEnergyPerThread.computeIfAbsent(th, k -> new ConcurrentHashMap<>())
                .merge(methodName, delta, Double::sum);
    }

    public void addFilteredMethodConsumedEnergyPerThread(Thread th, String methodName, double delta) {
        filteredMethodsConsumedEnergyPerThread.computeIfAbsent(th, k -> new ConcurrentHashMap<>())
                .merge(methodName, delta, Double::sum);
    }

    public void addTotalMethodConsumedEnergyPerThread(Thread th, String methodName, double delta) {
        totalMethodsConsumedEnergyPerThread.computeIfAbsent(th, k -> new ConcurrentHashMap<>())
                .merge(methodName, delta, Double::sum);
    }

    public void addTotalFilteredMethodConsumedEnergyPerThread(Thread th, String methodName, double delta) {
        totalFilteredMethodsConsumedEnergyPerThread.computeIfAbsent(th, k -> new ConcurrentHashMap<>())
                .merge(methodName, delta, Double::sum);
    }

    public void trackMethodConsumptionPerThread(Thread th, String methodName, long timestamp, double energy) {
        methodsConsumptionEvolutionPerThread.computeIfAbsent(th, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(methodName, k -> new ConcurrentHashMap<>())
                .put(timestamp, energy);
    }

    public void trackFilteredMethodConsumptionPerThread(Thread th, String methodName, long timestamp, double energy) {
        filteredMethodsConsumptionEvolutionPerThread.computeIfAbsent(th, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(methodName, k -> new ConcurrentHashMap<>())
                .put(timestamp, energy);
    }

    public void addCallTreeConsumedEnergyPerThread(Thread th, CallTree callTree, double delta) {
        callTreesConsumptionPerThread.computeIfAbsent(th, k -> new ConcurrentHashMap<>())
                .merge(callTree, delta, Double::sum);
    }

    public void addFilteredCallTreeConsumedEnergyPerThread(Thread th, CallTree callTree, double delta) {
        filteredCallTreesConsumptionPerThread.computeIfAbsent(th, k -> new ConcurrentHashMap<>())
                .merge(callTree, delta, Double::sum);
    }

    public void addConsumedEnergyPerThread(Thread th, double delta) {
        totalConsumedEnergyPerThread.merge(th, delta, Double::sum);
    }

    public Map<Thread, Map<String, Double>> getMethodsConsumedEnergyPerThread() {
        return Collections.unmodifiableMap(methodsConsumedEnergyPerThread);
    }

    public Map<Thread, Map<String, Double>> getFilteredMethodsConsumedEnergyPerThread() {
        return Collections.unmodifiableMap(filteredMethodsConsumedEnergyPerThread);
    }

    public Map<Thread, Map<String, Double>> getTotalMethodsConsumedEnergyPerThread() {
        return Collections.unmodifiableMap(totalMethodsConsumedEnergyPerThread);
    }

    public Map<Thread, Map<String, Double>> getTotalFilteredMethodsConsumedEnergyPerThread() {
        return Collections.unmodifiableMap(totalFilteredMethodsConsumedEnergyPerThread);
    }

    public Map<Thread, Map<String, Map<Long, Double>>> getMethodsConsumptionEvolutionPerThread() {
        return Collections.unmodifiableMap(methodsConsumptionEvolutionPerThread);
    }

    public Map<Thread, Map<String, Map<Long, Double>>> getFilteredMethodsConsumptionEvolutionPerThread() {
        return Collections.unmodifiableMap(filteredMethodsConsumptionEvolutionPerThread);
    }

    public Map<Thread, Map<CallTree, Double>> getCallTreesConsumedEnergyPerThread() {
        return Collections.unmodifiableMap(callTreesConsumptionPerThread);
    }

    public Map<Thread, Map<CallTree, Double>> getFilteredCallTreesConsumedEnergyPerThread() {
        return Collections.unmodifiableMap(filteredCallTreesConsumptionPerThread);
    }

    public Map<Thread, Double> getTotalConsumedEnergyPerThread() {
        return Collections.unmodifiableMap(totalConsumedEnergyPerThread);
    }
}
