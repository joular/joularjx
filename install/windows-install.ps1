# Copyright (c) 2021-2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the
# GNU General Public License v3.0 only (GPL-3.0-only)
# which accompanies this distribution, and is available at
# https://www.gnu.org/licenses/gpl-3.0.en.html
#
# Author : Adel Noureddine

$host.UI.RawUI.WindowTitle = "JoularJX Windows Installer"

$INSTALLATION_PATH = "C:\joularjx"

$USER_CONFIRMATION = Read-Host "Installation to $INSTALLATION_PATH. Continue ([y]/n)"
if ($USER_CONFIRMATION -ine "y") {
    exit
}

New-Item -ItemType Directory -Force -Path $INSTALLATION_PATH
Copy-Item "joularjx-*.jar" -Destination $INSTALLATION_PATH
Copy-Item "config.properties" -Destination $INSTALLATION_PATH
Copy-Item "PowerMonitor.exe" -Destination $INSTALLATION_PATH
Write-Host "Installation complete. JoularJX files installed in $INSTALLATION_PATH"

Read-Host "Press Enter to continue..."