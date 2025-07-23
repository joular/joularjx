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
import java.util.Properties;

public interface ResultWriter {

	/**
	 * Close the target. Call this method once all writing operations have completed
	 *
	 * @throws IOException in case of error
	 */
	void closeTarget() throws IOException;

	void setProperties(Properties props);

	/**
	 * Set the target for this writer, aka the place where the data will be written.
	 * Call this method before any writing operation.
	 *
	 * @param scope     type of the target
	 * @param overwrite true to overwrite any existing data in the target
	 * @throws IOException in case of error
	 */
	void setTarget(ResultScope scope, boolean overwrite) throws IOException;

	/**
	 * Set the target for this writer, aka the place where the data will be written.
	 * Call this method before any writing operation.
	 *
	 * @param name      name of the target (can be a file name, for example)
	 * @param overwrite true to overwrite any existing data in the target
	 * @throws IOException in case of error
	 */
	void setTarget(String name, boolean overwrite) throws IOException;

	/**
	 * Write a line. {@link #setTarget(String, boolean)} should have been called
	 * before using this method.
	 *
	 * @param methodName  name of the method
	 * @param methodPower power consumption of the method
	 * @throws IOException in case of error
	 */
	void write(String methodName, double methodPower) throws IOException;
}
