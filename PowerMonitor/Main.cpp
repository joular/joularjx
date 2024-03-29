/*
 * Copyright (c) 2021-2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
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
#include <signal.h>

#include "hubbloRAPL.h"

using namespace std;

void sighandler(int snum) {
    closeDriver();
    exit(1);
}

int main() {
    signal(SIGINT, sighandler);

    if (loadDriver()) {
        float beforeEnergy = 0.0;
        float afterEnergy = 0.0;
        float energy = 0.0;
        while (true) {
            afterEnergy = getRAPLEnergy();
            energy = afterEnergy - beforeEnergy;
            beforeEnergy = afterEnergy;
            cout << energy << endl;

            // Sleep for one second
            Sleep(1000);
        }
        return 0;
    }
    else {
        cout << "Driver failed" << endl;
    }
}