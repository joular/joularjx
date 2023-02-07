/*
 * Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the
 * GNU General Public License v3.0 only (GPL-3.0-only)
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * Author : Adel Noureddine
 */

#include <iostream>

#include <SDKDDKVer.h>

#include <stdio.h>
#include <tchar.h>
#include "IntelPowerGadgetLib.h"

using namespace std;

int main() {
    CIntelPowerGadgetLib energyLib;

    if (energyLib.IntelEnergyLibInitialize() == false) {
        return 0;
    }

    int nNodes = 0;
    int nMsrs = 0;

    energyLib.GetNumNodes(&nNodes);
    energyLib.GetNumMsrs(&nMsrs);

    while (true) {
        if (!energyLib.ReadSample()) {
            return 0;
        }

        double data[3];
        int nData, funcId;
        double power = 0;
        wchar_t szName[MAX_PATH];

        // Processor (i=0, j=1)
        energyLib.GetMsrFunc(1, &funcId);
        energyLib.GetMsrName(1, szName);

        if (funcId != 1) {
            continue;
        }
        energyLib.GetPowerData(0, 1, data, &nData);
        power += data[0]; // power in W %6.2f

        // Now check for DRAM and add it to total power (i=0, j=4)
        energyLib.GetMsrFunc(4, &funcId);
        energyLib.GetMsrName(4, szName);

        if (funcId == 1) {
            energyLib.GetPowerData(0, 4, data, &nData);
            power += data[0]; // power in W %6.2f
        }

        cout << power << endl;

        // Sleep for one second
        Sleep(1000);
    }
    return 0;
}