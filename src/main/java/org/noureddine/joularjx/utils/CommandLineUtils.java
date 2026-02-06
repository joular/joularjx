/*
 * Copyright (c) 2026, Adel Noureddine
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Author : Adel Noureddine
 */

package org.noureddine.joularjx.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helpers for parsing command line strings into arguments.
 */
public final class CommandLineUtils {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("([^\"]\\S*|\".+?\")\\s*");

    private CommandLineUtils() {
        super();
    }

    /**
     * Splits a command line into tokens while preserving quoted segments.
     *
     * @param command the command line to split
     * @return list of command tokens
     */
    public static List<String> splitCommand(String command) {
        List<String> tokens = new ArrayList<>();
        if (command == null || command.isBlank()) {
            return tokens;
        }

        Matcher matcher = TOKEN_PATTERN.matcher(command);
        while (matcher.find()) {
            String token = matcher.group(1);
            if (token.startsWith("\"") && token.endsWith("\"") && token.length() >= 2) {
                token = token.substring(1, token.length() - 1);
            }
            tokens.add(token);
        }

        return tokens;
    }

    /**
     * Builds a command by splitting and concatenating multiple command parts.
     *
     * @param parts command parts to split and append
     * @return list of command tokens
     */
    public static List<String> buildCommand(String... parts) {
        List<String> tokens = new ArrayList<>();
        if (parts == null) {
            return tokens;
        }

        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            tokens.addAll(splitCommand(part));
        }

        return tokens;
    }
}
