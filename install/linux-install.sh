#!/bin/sh

# Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the
# GNU General Public License v3.0 only (GPL-3.0-only)
# which accompanies this distribution, and is available at
# https://www.gnu.org/licenses/gpl-3.0.en.html
#
# Author : Adel Noureddine

INSTALLATION_PATH=/opt/joularjx

read -p "Installation to $INSTALLATION_PATH. Continue ([y]/n)? " USER_CONFIRMATION
echo
if [ "$USER_CONFIRMATION" = "y" ]
then
    mkdir $INSTALLATION_PATH
    cp joularjx-*.jar $INSTALLATION_PATH
    cp config.properties $INSTALLATION_PATH

    echo "Installation complete. JoularJX files installed in $INSTALLATION_PATH"
fi