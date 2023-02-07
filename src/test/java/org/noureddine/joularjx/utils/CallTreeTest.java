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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

public class CallTreeTest {

    @Test
    public void getCallTreeTest() {
        StackTraceElement[] stackTraceArray = Thread.currentThread().getStackTrace();

        CallTree stackTrace = new CallTree(stackTraceArray);

        assertEquals(stackTrace.getCallTree(), Arrays.asList(stackTraceArray));
    }

    @Test
    public void setCallTreeTest() {
        CallTree stackTrace = new CallTree();
        StackTraceElement[] stackTraceArray = Thread.currentThread().getStackTrace();

        stackTrace.setCallTree(stackTraceArray);

        assertEquals(stackTrace.getCallTree(), Arrays.asList(stackTraceArray));
    }

    @Test
    public void equalsTest() {
        StackTraceElement[] stackTraceArray = Thread.currentThread().getStackTrace();

        CallTree stackTrace = new CallTree(stackTraceArray);

        assertTrue(stackTrace.equals(new CallTree(stackTraceArray)));
        assertFalse(stackTrace.equals(new CallTree()));
    }

    @Test
    public void equalsWithTheSameElementsButNotTheSameOrderTest() {
        StackTraceElement e = new StackTraceElement("ClassA", "MethodA", "FileA", 20);
        StackTraceElement e1 = new StackTraceElement("ClassB", "MethodB", "FileB", 12);
        StackTraceElement e2 = new StackTraceElement("ClassC", "MethodC", "FileC", 59);

        StackTraceElement[] arr1 = {e, e1, e2};
        StackTraceElement[] arr2 = {e2, e1, e};

        CallTree s1 = new CallTree(arr1);
        CallTree s2 = new CallTree(arr2);

        assertFalse(s1.equals(s2));
    }

    @Test
    public void equalsWithSameElementsButNotSameLinesTest() {
        StackTraceElement e = new StackTraceElement("ClassA", "MethodA", "FileA", 20);
        StackTraceElement e1 = new StackTraceElement("ClassB", "MethodB", "FileB", 12);

        StackTraceElement e2 = new StackTraceElement("ClassA", "MethodA", "FileA", 59);
        StackTraceElement e3 = new StackTraceElement("ClassB", "MethodB", "FileB", 02);

        StackTraceElement[] arr1 = {e, e1};
        StackTraceElement[] arr2 = {e2, e3};

        CallTree s1 = new CallTree(arr1);
        CallTree s2 = new CallTree(arr2);

        assertEquals(s1, s2);
    }

    @Test
    public void equalsWithoutFileNameDoesNotFailsTest() {
        StackTraceElement e = new StackTraceElement("ClassA", "MethodA", null, 20);
        StackTraceElement e1 = new StackTraceElement("ClassB", "MethodB", null, 12);
        StackTraceElement e2 = new StackTraceElement("ClassC", "MethodC", null, 59);

        StackTraceElement[] arr1 = {e, e1, e2};
        StackTraceElement[] arr2 = {e, e1, e2};

        CallTree s1 = new CallTree(arr1);
        CallTree s2 = new CallTree(arr2);

        assertTrue(s1.equals(s2));
    }

    @Test
    public void toSringTest() {
        StackTraceElement e = new StackTraceElement("ClassA", "MethodA", "FileA", 20);
        StackTraceElement e1 = new StackTraceElement("ClassB", "MethodB", "FileB", 12);
        StackTraceElement e2 = new StackTraceElement("ClassC", "MethodC", "FileC", 59);

        StackTraceElement[] arr = {e2, e1, e};
        CallTree stackTrace = new CallTree(arr);

        String oracle = "ClassA.MethodA;ClassB.MethodB;ClassC.MethodC";

        assertEquals(oracle, stackTrace.toString());
    }
    
}
