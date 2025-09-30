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

import java.io.IOException;

import org.noureddine.joularjx.utils.AgentProperties;

public interface ResultWriter {

	/**
	 * Closes the target. Call this method once all writing operations have
	 * completed
	 *
	 * @throws IOException in case of error
	 */
	void closeTarget() throws IOException;

	/**
	 * Initializes this writer. This method should be called only once at instance
	 * creation.
	 *
	 * @param props     properties
	 * @param pid       pid of the software being monitored
	 * @param timestamp current execution timestamp
	 */
	void initialize(AgentProperties props, long pid, long timestamp);

	/**
	 * Configures this writer for the current write operation. This method should be
	 * called once for each different type of data being written.
	 *
	 * @param configuration current configuration to use for this operation
	 * @throws IOException in case of error
	 */
	void setConfiguration(ResultWriterConfiguration configuration) throws IOException;

	/**
	 * Writes a line. {@link #setConfiguration(ResultWriterConfiguration)} should
	 * have been called before using this method.
	 *
	 * @param methodName  name of the method
	 * @param methodPower power consumption of the method
	 * @throws IOException in case of error
	 */
	void write(String methodName, double methodPower) throws IOException;
}
