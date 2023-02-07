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
import java.util.List;
import java.util.function.Predicate;

public class StackTraceFilter {

    /**
     * Filters the elements of a stack trace using the given predicate, based on the class and method names of the elements.
     * @param input the stack trace to filter, as an array of StackTraceElement
     * @param filter the predicate used to filter the stack trace
     * @return a List of StackTraceElement, representing the filtered stack trace. 
     * The order of the elements will be the same as the input array, but the list will only contains elements that pass the filter.
     */
    public static List<StackTraceElement> filter(StackTraceElement[] input, Predicate<String> filter) {
        List<StackTraceElement> output = new ArrayList<>();
        
        for (StackTraceElement element : input) {
            String canonicalName = element.getClassName() + "." + element.getMethodName();
            if (filter.test(canonicalName)) {
                output.add(element);
            }
        }

        return output;
    }
    
}
