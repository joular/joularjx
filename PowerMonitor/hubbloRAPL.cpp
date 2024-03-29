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

#include <iostream>
#include <stdio.h>

#include "hubbloRAPL.h"

using namespace std;

HANDLE raplDriverHandle = NULL;

const DWORD RAPL_CTL_CODE = CTL_CODE(
    FILE_DEVICE_UNKNOWN, 
    static_cast<UINT16>(MSR_RAPL_POWER_UNIT),
    METHOD_BUFFERED,
    FILE_READ_DATA | FILE_WRITE_DATA
);

float powerUnit, energyUnit, timeUnit;

bool psys = false, pkg = false, dram = false;

bool getDataFromDriver(UINT64 msr, UINT64* replyData, DWORD* replyLength) {
    if (!DeviceIoControl(
        raplDriverHandle,
        RAPL_CTL_CODE,
        &msr,
        sizeof(msr),
        replyData,
        sizeof(replyData),
        replyLength,
        NULL
    )) {
        return 0;
    }

    return 1;
}

void getEnergyUnits() {
    UINT64 replyData;
    DWORD replyLength;

    if (!getDataFromDriver(MSR_RAPL_POWER_UNIT, &replyData, &replyLength)) {
        return;
    }

    // Time Units
    constexpr UINT64 time_mask = 0xF0000;
    const UINT64 time_val = replyData & time_mask;
    timeUnit = 1.0f / (pow(2.0f, static_cast<float>(time_val >> 16)));

    // Energy Units
    constexpr UINT64 energy_mask = 0x1F00;
    const UINT64 energy_val = replyData & energy_mask;
    energyUnit = 1.0f / (pow(2.0f, static_cast<float>(energy_val >> 8)));

    // Power Units
    constexpr UINT64 power_mask = 0xF;
    const UINT64 power_val = replyData & power_mask;
    powerUnit = 1.0f / (pow(2.0f, static_cast<float>(power_val)));
}

void checkSupportedPlatform() {
    UINT64 replyData;
    DWORD replyLength;

    if (getDataFromDriver(MSR_PLATFORM_ENERGY_STATUS, &replyData, &replyLength)) {
        if (replyData != 0) {
            psys = true;
        }
    }

    if (!psys) {
        if (getDataFromDriver(MSR_PKG_ENERGY_STATUS, &replyData, &replyLength)) {
            if (replyData != 0) {
                pkg = true;
            }
        }

        if (getDataFromDriver(MSR_DRAM_ENERGY_STATUS, &replyData, &replyLength)) {
            if (replyData != 0) {
                dram = true;
            }
        }
    }
}

int loadDriver() {
    if (raplDriverHandle == NULL) {
        raplDriverHandle = CreateFile(
        L"\\\\.\\ScaphandreDriver",
        GENERIC_READ | GENERIC_WRITE,
        FILE_SHARE_READ | FILE_SHARE_WRITE,
        NULL,
        OPEN_EXISTING,
        FILE_FLAG_OVERLAPPED,
        NULL
        );
        
        if (raplDriverHandle == INVALID_HANDLE_VALUE) {
            return 0;
        }

        getEnergyUnits();

        checkSupportedPlatform();
        
        return 1;
    };
    
    return 0;
}

void closeDriver() {
    if(raplDriverHandle != NULL) {
        CloseHandle(raplDriverHandle);
        raplDriverHandle = NULL;
    }
}

float getRAPLEnergy() {
    UINT64 replyData;
    DWORD replyLength;
    UINT64 msr = NULL;

    if (psys) {
        cout << "psys: " << psys << endl;
        if (!getDataFromDriver(MSR_PLATFORM_ENERGY_STATUS, &replyData, &replyLength)) {
            cout << "not" << endl;
            return 0.0;
        }
        cout << "getting data" << endl;
        cout << "replydata: " << replyData << endl;
        const UINT32 rawPSYSEnergy = replyData & 0xFFFFFFFF;
        cout << "raw: " << rawPSYSEnergy << endl;
        const float PSYSEnergy = static_cast<float>(rawPSYSEnergy) * energyUnit;
        return PSYSEnergy;
    }
    else {
        if (pkg) {
            if (!getDataFromDriver(MSR_PKG_ENERGY_STATUS, &replyData, &replyLength)) {
                return 0.0;
            }
            const UINT32 rawPKGEnergy = replyData & 0xFFFFFFFF;
            const float PKGEnergy = static_cast<float>(rawPKGEnergy) * energyUnit;

            if (dram) {
                if (!getDataFromDriver(MSR_DRAM_ENERGY_STATUS, &replyData, &replyLength)) {
                    return 0.0;
                }
                const UINT32 rawDRAMEnergy = replyData & 0xFFFFFFFF;
                const float DRAMEnergy = static_cast<float>(rawDRAMEnergy) * energyUnit;

                return PKGEnergy + DRAMEnergy;
            }
            else {
                return PKGEnergy;
            }
        }
    }
    return 0.0;
}