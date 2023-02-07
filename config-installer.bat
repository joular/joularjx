:: Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
:: All rights reserved. This program and the accompanying materials
:: are made available under the terms of the
:: GNU General Public License v3.0 only (GPL-3.0-only)
:: which accompanies this distribution, and is available at
:: https://www.gnu.org/licenses/gpl-3.0.en.html
::
:: Author : Adel Noureddine

@echo off
title JoularJX Installer Configurator

echo Compiling and building JoularJX
call mvn clean install

echo Compiling ProgramMonitor on Windows x64
set DEVENV_COM=C:\Program Files\Microsoft Visual Studio\2022\Community\Common7\IDE\devenv.com
cd PowerMonitor
"%DEVENV_COM%" PowerMonitor.sln /Build "Release|x64"
cd ..

echo Copying files to install folder
copy config.properties install
copy target\joularjx-*.jar install
copy PowerMonitor\x64\Release\PowerMonitor.exe install
pause