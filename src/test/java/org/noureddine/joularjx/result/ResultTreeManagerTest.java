/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.result;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResultTreeManagerTest {

    private long pid;
    private long timestamp;

    private String appDirectory;

    private ResultTreeManager manager;

    @BeforeEach
    public void init() {
        this.pid = new Random().nextLong();
        this.timestamp = System.currentTimeMillis();

        this.appDirectory = String.format("%d-%d", pid, timestamp);

        //For the moment, only peths are tested, so the AgentProperies parameter is not required
        this.manager = new ResultTreeManager(null, pid, timestamp);
    }

    @Test
    public void getAllRuntimeMethodsPathTest() {
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
            +this.appDirectory+"/"
            +ResultTreeManager.ALL_DIRECTORY_NAME+"/"
            +ResultTreeManager.RUNTIME_DIRECTORY_NAME+"/"
            +ResultTreeManager.METHOD_DIRECTORY_NAME
            , this.manager.getAllRuntimeMethodsPath());
    }

    @Test
    public void getFilteredRuntimeMethodsPathTest() {
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
        +this.appDirectory+"/"
        +ResultTreeManager.FILTERED_DIRECTORY_NAME+"/"
        +ResultTreeManager.RUNTIME_DIRECTORY_NAME+"/"
        +ResultTreeManager.METHOD_DIRECTORY_NAME
        , this.manager.getFilteredRuntimeMethodsPath());
    }

    @Test
    public void getAllTotalMethodsPathTest() {
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
        +this.appDirectory+"/"
        +ResultTreeManager.ALL_DIRECTORY_NAME+"/"
        +ResultTreeManager.TOTAL_DIRECTORY_NAME+"/"
        +ResultTreeManager.METHOD_DIRECTORY_NAME
        , this.manager.getAllTotalMethodsPath());
    }

    @Test
    public void getFilteredTotalMethodsPathTest() {
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
        +this.appDirectory+"/"
        +ResultTreeManager.FILTERED_DIRECTORY_NAME+"/"
        +ResultTreeManager.TOTAL_DIRECTORY_NAME+"/"
        +ResultTreeManager.METHOD_DIRECTORY_NAME
        , this.manager.getFilteredTotalMethodsPath());
    }

    @Test
    public void getAllRuntimeCallTreePathTest() {
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
            +this.appDirectory+"/"
            +ResultTreeManager.ALL_DIRECTORY_NAME+"/"
            +ResultTreeManager.RUNTIME_DIRECTORY_NAME+"/"
            +ResultTreeManager.CALLTREE_DIRECTORY_NAME
            , this.manager.getAllRuntimeCallTreePath());
    }

    @Test
    public void getFilteredRuntimeCallTreePathTest() {
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
            +this.appDirectory+"/"
            +ResultTreeManager.FILTERED_DIRECTORY_NAME+"/"
            +ResultTreeManager.RUNTIME_DIRECTORY_NAME+"/"
            +ResultTreeManager.CALLTREE_DIRECTORY_NAME
            , this.manager.getFilteredRuntimeCallTreePath());
    }

    @Test
    public void getAllTotalCallTreePathTest() {
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
            +this.appDirectory+"/"
            +ResultTreeManager.ALL_DIRECTORY_NAME+"/"
            +ResultTreeManager.TOTAL_DIRECTORY_NAME+"/"
            +ResultTreeManager.CALLTREE_DIRECTORY_NAME
            , this.manager.getAllTotalCallTreePath());
    }

    @Test
    public void getFilteredTotalCallTreePathTest() {
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
            +this.appDirectory+"/"
            +ResultTreeManager.FILTERED_DIRECTORY_NAME+"/"
            +ResultTreeManager.TOTAL_DIRECTORY_NAME+"/"
            +ResultTreeManager.CALLTREE_DIRECTORY_NAME
            , this.manager.getFilteredTotalCallTreePath());
    }

    @Test
    public void getAllEvolutionPathTest(){
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
            +this.appDirectory+"/"
            +ResultTreeManager.ALL_DIRECTORY_NAME+"/"
            +ResultTreeManager.EVOLUTION_DIRECTORY_NAME
            , this.manager.getAllEvolutionPath());
    }

    @Test
    public void getFilteredEvolutionPathTest() {
        assertEquals(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME+"/"
            +this.appDirectory+"/"
            +ResultTreeManager.FILTERED_DIRECTORY_NAME+"/"
            +ResultTreeManager.EVOLUTION_DIRECTORY_NAME
            , this.manager.getFilteredEvolutionPath());
    }
}
