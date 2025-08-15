/*
 * Copyright (c) 2021-2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.result;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // For the moment, only paths are tested, so the AgentProperties parameter is not required
        this.manager = new ResultTreeManager(null, pid, timestamp);
    }

    @Test
    public void getAllRuntimeMethodsPathTest() {
        Path reference = Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME, this.appDirectory, 
                ResultTreeManager.ALL_DIRECTORY_NAME,
                ResultTreeManager.RUNTIME_DIRECTORY_NAME,
                ResultTreeManager.METHOD_DIRECTORY_NAME,
                String.format("joularJX-%d-all-methods-power", pid));
        assertEquals(reference, this.manager.getPath(ResultScope.ALL_RUNTIME_METHODS));
    }
    
    @Test
    public void getAllRuntimeMethodsPathTimestampedTest() {
    	// Note: this timestamp test may be better either with a mock library or an API change on the Builder
        long timestamp1 = System.currentTimeMillis();
        Pattern pattern = Pattern.compile("joularJX-(-?\\d+)-(\\d+)-all-methods-power");
        Path reference = Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME, this.appDirectory, 
                ResultTreeManager.ALL_DIRECTORY_NAME,
                ResultTreeManager.RUNTIME_DIRECTORY_NAME,
                ResultTreeManager.METHOD_DIRECTORY_NAME);
        Path result = this.manager.getBuilder(ResultScope.ALL_RUNTIME_METHODS).withTimestamp(true).build();
        long timestamp2 = System.currentTimeMillis();
        assertEquals(reference, result.getParent());
        Matcher match = pattern.matcher(result.getFileName().toString());
        match.find();
        assertEquals(pid, Long.parseLong(match.group(1)));
        long timestamp = Long.parseLong(match.group(2));
        assertTrue(timestamp1 <= timestamp && timestamp2 >= timestamp);
    }

    @Test
    public void getFilteredRuntimeMethodsPathTest() {
        assertEquals(Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME,
        this.appDirectory,
        ResultTreeManager.FILTERED_DIRECTORY_NAME,
        ResultTreeManager.RUNTIME_DIRECTORY_NAME,
        ResultTreeManager.METHOD_DIRECTORY_NAME,
        String.format("joularJX-%d-filtered-methods-power", pid))
        , this.manager.getPath(ResultScope.FILTERED_RUNTIME_METHODS));
    }

    @Test
    public void getAllTotalMethodsPathTest() {
        assertEquals(Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME,
        this.appDirectory,
        ResultTreeManager.ALL_DIRECTORY_NAME,
        ResultTreeManager.TOTAL_DIRECTORY_NAME,
        ResultTreeManager.METHOD_DIRECTORY_NAME,
        String.format("joularJX-%d-all-methods-energy", pid))
        , this.manager.getPath(ResultScope.ALL_TOTAL_METHODS));
    }

    @Test
    public void getFilteredTotalMethodsPathTest() {
        assertEquals(Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME,
        this.appDirectory,
        ResultTreeManager.FILTERED_DIRECTORY_NAME,
        ResultTreeManager.TOTAL_DIRECTORY_NAME,
        ResultTreeManager.METHOD_DIRECTORY_NAME,
        String.format("joularJX-%d-filtered-methods-energy", pid))
        , this.manager.getPath(ResultScope.FILTERED_TOTAL_METHODS));
    }

    @Test
    public void getAllRuntimeCallTreePathTest() {
        assertEquals(Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME,
            this.appDirectory,
            ResultTreeManager.ALL_DIRECTORY_NAME,
            ResultTreeManager.RUNTIME_DIRECTORY_NAME,
            ResultTreeManager.CALLTREE_DIRECTORY_NAME,
            String.format("joularJX-%d-all-call-trees-power", pid))
            , this.manager.getPath(ResultScope.ALL_RUNTIME_CALL_TREE));
    }

    @Test
    public void getFilteredRuntimeCallTreePathTest() {
        assertEquals(Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME,
            this.appDirectory,
            ResultTreeManager.FILTERED_DIRECTORY_NAME,
            ResultTreeManager.RUNTIME_DIRECTORY_NAME,
            ResultTreeManager.CALLTREE_DIRECTORY_NAME,
            String.format("joularJX-%d-filtered-call-trees-power", pid))
            , this.manager.getPath(ResultScope.FILTERED_RUNTIME_CALL_TREE));
    }

    @Test
    public void getAllTotalCallTreePathTest() {
        assertEquals(Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME,
            this.appDirectory,
            ResultTreeManager.ALL_DIRECTORY_NAME,
            ResultTreeManager.TOTAL_DIRECTORY_NAME,
            ResultTreeManager.CALLTREE_DIRECTORY_NAME,
            String.format("joularJX-%d-all-call-trees-energy", pid))
            , this.manager.getPath(ResultScope.ALL_TOTAL_CALL_TREE));
    }

    @Test
    public void getFilteredTotalCallTreePathTest() {
        assertEquals(Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME,
            this.appDirectory,
            ResultTreeManager.FILTERED_DIRECTORY_NAME,
            ResultTreeManager.TOTAL_DIRECTORY_NAME,
            ResultTreeManager.CALLTREE_DIRECTORY_NAME,
            String.format("joularJX-%d-filtered-call-trees-energy", pid))
            , this.manager.getPath(ResultScope.FILTERED_TOTAL_CALL_TREE));
    }

    @Test
    public void getAllEvolutionPathTest(){
        String method = "traditional";
        assertEquals(Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME,
            this.appDirectory,
            ResultTreeManager.ALL_DIRECTORY_NAME,
            ResultTreeManager.EVOLUTION_DIRECTORY_NAME,
            String.format("joularJX-%d-%s-evolution", pid, method))
            , this.manager.getAllEvolutionPath(method));
    }

    @Test
    public void getFilteredEvolutionPathTest() {
        String method = "traditional";
        assertEquals(Path.of(ResultTreeManager.GLOBAL_RESULT_DIRECTORY_NAME,
            this.appDirectory,
            ResultTreeManager.FILTERED_DIRECTORY_NAME,
            ResultTreeManager.EVOLUTION_DIRECTORY_NAME,
            String.format("joularJX-%d-%s-evolution", pid, method))
            , this.manager.getFilteredEvolutionPath(method));
    }
}
