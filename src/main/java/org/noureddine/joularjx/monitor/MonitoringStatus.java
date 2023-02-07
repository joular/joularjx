/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
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

    //Map method names to a Map of timestamps mapped to energy consumption
    private final Map<String, Map<Long, Double>> methodsConsumptionEvolution;
    private final Map<String, Map<Long, Double>> filteredMethodsConsumptionEvolution;

    //Map CallTrees to their energy consumption
    private final Map<CallTree, Double> callTreesConsumption;
    private final Map<CallTree, Double> filteredCallTreesConsumption;

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
        this.callTreesConsumption = new ConcurrentHashMap<>();
        this.filteredCallTreesConsumption = new ConcurrentHashMap<>();

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
     * @param delta a double, the amount of energy to be added
     */
    public void addFilteredMethodConsumedEnergy(String methodName, double delta) {
        filteredMethodsConsumedEnergy.merge(methodName, delta, Double::sum);
    }

    /**
     * Adds the given energy consumption to the given call tree.
     * @param callTree a CallTree, the call tree to which the consumption is mapped
     * @param delta a double, the amount of energy to be added
     */
    public void addCallTreeConsumedEnergy(CallTree callTree, double delta) {
        this.callTreesConsumption.merge(callTree, delta, Double::sum);
    }

    /**
     * Adds the given energy consumption to the given filtered call tree
     * @param callTree a CallTree, the call tree to which the consumption is mapped
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

    /**
     * Returns the energy consumption of each call tree.
     * @return a Map<CallTree, Double> mapping each call tree to their total energy consumption.
     */
    public Map<CallTree, Double> getCallTreesConsumedEnergy() {
        return this.callTreesConsumption;
    }

    /**
     * Returns the energy consumption of each filtered call tree.
     * @return a Map<CallTree, Double> mapping each filtered call tree to their total energy consumption.
     */
    public Map<CallTree, Double> getFilteredCallTreesConsumedEnergy() {
        return this.filteredCallTreesConsumption;
    }
}
