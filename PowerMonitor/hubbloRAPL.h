/*
 * Copyright (c) 2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Author : Adel Noureddine
 */

#pragma once

#include <Windows.h>

// RAPL units
constexpr UINT64 MSR_RAPL_POWER_UNIT = 0x606;

// Package
constexpr UINT64 MSR_PKG_ENERGY_STATUS = 0x611;

// DRAM
constexpr UINT64 MSR_DRAM_ENERGY_STATUS = 0x00000619;

// Platform = PSYS
constexpr UINT64 MSR_PLATFORM_ENERGY_STATUS = 0x0000064d;

// Load and close driver
int loadDriver();
void closeDriver();

// Get RAPL energy data
float getRAPLEnergy();