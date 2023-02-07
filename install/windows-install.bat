:: Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
:: All rights reserved. This program and the accompanying materials
:: are made available under the terms of the
:: GNU General Public License v3.0 only (GPL-3.0-only)
:: which accompanies this distribution, and is available at
:: https://www.gnu.org/licenses/gpl-3.0.en.html
::
:: Author : Adel Noureddine

@echo off
title JoularJX Windows Installer

set INSTALLATION_PATH=C:\joularjx

set USER_CONFIRMATION=y
set /p USER_CONFIRMATION=Installation to %INSTALLATION_PATH%. Continue ([y]/n)?
if /i not "%USER_CONFIRMATION%" == "y" goto :eof

md %INSTALLATION_PATH%
copy joularjx-*.jar %INSTALLATION_PATH%
copy config.properties %INSTALLATION_PATH%
copy PowerMonitor.exe %INSTALLATION_PATH%

echo Installation complete. JoularJX files installed in %INSTALLATION_PATH%
pause