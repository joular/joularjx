/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A CallTree (or a stack trace) is a collection of StackTraceElements in a given order. This class proivdes methods in order to easily represent and manage such stack traces. 
 */
public class CallTree {

    //The stack trace is stored in the form of a List of StackTraceElements.
    private List<StackTraceElement> callTree;
    
    /**
     * Creates a new empty CallTree.
     */
    public CallTree() {
        this.callTree = new ArrayList<>();
    }

    /**
     * Creates a new CallTree.
     * @param stackTrace a java array of StackTraceElement, representing a stack trace. This array will be automatically converted to a List.
     */
    public CallTree(StackTraceElement[] stackTrace) {
        this.callTree = Arrays.asList(stackTrace);
    }

    /**
     * Creates a new CallTree.
     * @param stackTrace a List of StackTraceElement, representing a stack trace
     */
    public CallTree(List<StackTraceElement> stackTrace) {
        this.callTree = stackTrace;
    }

    /**
     * Sets the given stack trace.
     * @param stackTrace a java array of StackTraceElement, representing a stack trace.
     */
    public void setCallTree(StackTraceElement[] stackTrace) {
        this.callTree = Arrays.asList(stackTrace);
    }

    /**
     * Returns the call tree.
     * @return a List of StackTraceElement, representing a call tree.
     */
    public List<StackTraceElement> getCallTree() {
        return this.callTree;
    }

    @Override
    public String toString() {
        String res = "";

        /*Appening elements to res String in reverse order. The least recent element (the bottom of the stack trace) will be written first, and the most recent one last.*/
        for(int i = this.callTree.size()-1; i >= 0; i--){
            StackTraceElement element = this.callTree.get(i);
            res += element.getClassName()+"."+element.getMethodName()+";";
        }

        //Removing the last ";"
        return res.substring(0, res.length()-1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.callTree == null) ? 0 : hashCode(this.callTree));
        
        return result;
    }

    /**
     * Custom implementation of the hashCode method for List of StackTraceElement.
     * @param l the List<StackTraceElement> which hashCode will be computed.
     * @return an int, the hashCode of the given List.
     */
    private int hashCode(List<StackTraceElement> l) {
        int result = 1;
        for (StackTraceElement element : l) {
            result = 31 * result + hashCode(element);
        }
        
        return result;
    }

    /**
     * Custom implementation of the hashCode method for StackTraceElement.
     * The hashCode is computed based on the class, the method and the file of the given element (if available).
     * Custom hashCode does NOT take into account the line number.
     * @param e the StackTraceElement which hashCode will be computed.
     * @return an int, the hashCode for the given StackTraceElement.
     */
    private int hashCode(StackTraceElement e) {
        int result = 31 * e.getClassName().hashCode() + e.getMethodName().hashCode();

        if (e.getFileName() != null) {
            result = 31 * result + e.getFileName().hashCode();
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
            
        CallTree other = (CallTree) obj;
        if (callTree == null) {
            if (other.callTree != null)
                return false;
        } else if (!equals(this.callTree, other.callTree))
            return false;
        return true;
    }

    /**
     * Custom implementation of the equals method for call trees
     * Two callTrees are equals if they contains the same elements in the same order
     * @param callTree a List of StackTraceElement, representing a call tree
     * @param other a List of StackTraceElement, representing a call tree
     * @return a boolean, true if the two call trees are equals, false otherwise
     */
    private boolean equals(List<StackTraceElement> callTree, List<StackTraceElement> other) {
        if (callTree == null ^ other == null) {
            return false;
        }

        if (callTree == null && other == null) {
            return true;
        }

        if (callTree.size() != other.size()) {
            return false;
        }

        for (int i = 0; i < callTree.size(); i++) {
            StackTraceElement e = callTree.get(i);
            if (!equals(e, other.get(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Custom implementation of the equals method for StackTraceElement
     * Two StackTraceElement are equals if they have the same file name (if available), class name and method name. 
     * Please note that the line number is NOT checked, and if the three previous conditions are satisfied, two StackTraceElement with a different line number will be considered equals.
     * @param e a StackTraceElement
     * @param other a StackTraceElement
     * @return a boolean, true if the two given StackTraceElement are equals, false otherwise
     */
    private boolean equals(StackTraceElement e, StackTraceElement other) {
        boolean result = true;

        if(e.getFileName() != null && other.getFileName() != null) {
            result = result && e.getFileName().equals(other.getFileName());
        }
        
        return result && e.getClassName().equals(other.getClassName()) && e.getMethodName().equals(other.getMethodName());
    }
    
}
