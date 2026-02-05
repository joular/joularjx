/*
 * Copyright (c) 2021-2025, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.result;

/**
 * Enumeration of result scopes and their associated suffixes.
 */
public enum ResultScope {
	/** All methods evolution output. */
	ALL_EVOLUTION("", "evolution"),
	/** Filtered methods evolution output. */
	FILTERED_EVOLUTION("", "evolution"),
	/** All runtime call tree output. */
	ALL_RUNTIME_CALL_TREE("all", "call-trees-power"),
	/** Filtered runtime call tree output. */
	FILTERED_RUNTIME_CALL_TREE("filtered", "call-trees-power"),
	/** All runtime methods output. */
	ALL_RUNTIME_METHODS("all", "methods-power"),
	/** Filtered runtime methods output. */
	FILTERED_RUNTIME_METHODS("filtered", "methods-power"),
	/** All total call tree output. */
	ALL_TOTAL_CALL_TREE("all", "call-trees-energy"),
	/** Filtered total call tree output. */
	FILTERED_TOTAL_CALL_TREE("filtered", "call-trees-energy"),
	/** All total methods output. */
	ALL_TOTAL_METHODS("all", "methods-energy"),
	/** Filtered total methods output. */
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
	 * Returns the scope token used in file paths.
	 *
	 * @return the scope token
	 */
	public String getScope() {
		return scope;
	}

	/**
	 * Returns the suffix used in result file names.
	 *
	 * @return the suffix
	 */
	public String getSuffix() {
		return suffix;
	}
}
