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
	 * @param scope
	 */
	public ResultWriterConfiguration(ResultScope scope) {
		this.scope = scope;
	}

	/**
	 * @param scope
	 * @param methodName
	 */
	public ResultWriterConfiguration(ResultScope scope, String methodName) {
		this(scope);
		this.methodName = methodName;
	}

	/**
	 * @param scope
	 * @param timestamped
	 */
	public ResultWriterConfiguration(ResultScope scope, boolean timestamped) {
		this(scope);
		this.timestamped = timestamped;
		this.overwrite = !timestamped;
	}

	/**
	 * @return the methodName
	 */
	public String getMethodName() {
		return methodName;
	}

	/**
	 * @return the scope
	 */
	public ResultScope getScope() {
		return scope;
	}

	/**
	 * @return the timestamped
	 */
	public boolean isTimestamped() {
		return timestamped;
	}

	/**
	 * @return the overwrite
	 */
	public boolean isOverwrite() {
		return overwrite;
	}
}
