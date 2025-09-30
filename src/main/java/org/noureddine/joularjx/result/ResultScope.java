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

public enum ResultScope {
	ALL_EVOLUTION("", "evolution"),
	FILTERED_EVOLUTION("", "evolution"),
	ALL_RUNTIME_CALL_TREE("all", "call-trees-power"),
	FILTERED_RUNTIME_CALL_TREE("filtered", "call-trees-power"),
	ALL_RUNTIME_METHODS("all", "methods-power"),
	FILTERED_RUNTIME_METHODS("filtered", "methods-power"),
	ALL_TOTAL_CALL_TREE("all", "call-trees-energy"),
	FILTERED_TOTAL_CALL_TREE("filtered", "call-trees-energy"),
	ALL_TOTAL_METHODS("all", "methods-energy"),
	FILTERED_TOTAL_METHODS("filtered", "methods-energy");
	
	String scope;
	String suffix;

	/**
	 * Constructor
	 * 
	 * @param string  scope (all or filtered)
	 * @param string2 suffix for the file names
	 */
	ResultScope(String string, String string2) {
		scope = string;
		suffix = string2;
	}

	/**
	 * @return the scope
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}
}
