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
 * Generic class to pass configuration to a {@link ResultWriter} instance 
 * This allows the instance to know which contents will be written to it
 */
public final class ResultWriterConfiguration {

	private final ResultScope scope;
	private String methodName;
	private boolean timestamped;
	private boolean overwrite;

	/**
	 * Constructor
	 *
	 * @param scope scope of the data to be written
	 */
	public ResultWriterConfiguration(ResultScope scope) {
		this.scope = scope;
	}

	/**
	 * Specialized constructor for timestamped data
	 *
	 * @param scope       scope of the data to be written
	 * @param timestamped true if the data to be written is timestamped, i.e.,
	 *                    instant data
	 */
	public ResultWriterConfiguration(ResultScope scope, boolean timestamped) {
		this(scope);
		this.timestamped = timestamped;
		this.overwrite = !timestamped;
	}

	/**
	 * Specialized constructor for methods
	 *
	 * @param scope      scope of the data to be written
	 * @param methodName name of the method
	 */
	public ResultWriterConfiguration(ResultScope scope, String methodName) {
		this(scope);
		this.methodName = methodName;
	}

	/**
	 * Returns the method name associated with this configuration.
	 *
	 * @return the method name or null if not set
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * Returns the result scope associated with this configuration.
	 *
	 * @return the scope
	 */
	public ResultScope getScope() {
		return scope;
	}

	/**
	 * Indicates whether the output should overwrite existing data.
	 *
	 * @return true if overwrite is enabled, false otherwise
	 */
	public boolean isOverwrite() {
		return overwrite;
	}

	/**
	 * Indicates whether the output should be timestamped.
	 *
	 * @return true if timestamped, false otherwise
	 */
	public boolean isTimestamped() {
		return timestamped;
	}
}
