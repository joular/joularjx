/*
 * Copyright (c) 2021-2026, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 */

package org.noureddine.joularjx.result;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Objects;

import org.noureddine.joularjx.utils.AgentProperties;

import com.google.auto.service.AutoService;

/**
 * {@link ResultWriter} implementation that writes results to CSV files.
 */
@AutoService(ResultWriter.class)
public class CsvResultWriter implements ResultWriter {

	private static final OpenOption[] APPEND_OPEN_OPTIONS = new OpenOption[] { StandardOpenOption.CREATE,
			StandardOpenOption.WRITE, StandardOpenOption.APPEND };
	private static final OpenOption[] OVERWRITE_OPEN_OPTIONS = new OpenOption[] { StandardOpenOption.CREATE,
			StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING };

	private final ThreadLocal<BufferedWriter> writer;

	private ResultTreeManager rtManager;

	/**
	 * Constructor
	 */
	public CsvResultWriter() {
		this.writer = new ThreadLocal<>();
	}

	/** {@inheritDoc} */
	@Override
	public void closeTarget() throws IOException {
		writer.get().close();
		writer.remove();
	}

	private Path getCsvPath(Path path) {
		return path.resolveSibling(path.getFileName() + ".csv");
	}

	/** {@inheritDoc} */
	@Override
	public void initialize(AgentProperties props, long pid, long timestamp) {
		rtManager = new ResultTreeManager(props, pid, timestamp);
	}

	/** {@inheritDoc} */
	@Override
	public void setConfiguration(ResultWriterConfiguration configuration) throws IOException {
		final ResultTreeManager.PathBuilder builder = rtManager.getBuilder(configuration.getScope());
		final Path target = builder.withMethodName(configuration.getMethodName())
				.withTimestamp(configuration.isTimestamped()).build();
		setTarget(target, configuration.isOverwrite());
	}

	private void setTarget(Path name, boolean overwrite) throws IOException {
		final BufferedWriter previousWriter = writer.get();
		if (Objects.nonNull(previousWriter)) {
			previousWriter.close();
		}

		writer.set(Files.newBufferedWriter(getCsvPath(name), overwrite ? OVERWRITE_OPEN_OPTIONS : APPEND_OPEN_OPTIONS));
	}

	/** {@inheritDoc} */
	@Override
	public void write(String methodName, double methodPower) throws IOException {
		final BufferedWriter writer = this.writer.get();
		if (Objects.isNull(writer)) {
			throw new IllegalStateException("Please call ResultWriter#setTarget(String) first");
		}

		writer.write(String.format(Locale.US, "%s,%.4f%n", methodName, methodPower));
	}
}
