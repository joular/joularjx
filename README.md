# [![Joular Project](https://gitlab.com/uploads/-/system/group/avatar/10668049/joular.png?width=64)](https://www.noureddine.org/research/joular/) JoularJX :microscope:

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue)](https://www.gnu.org/licenses/gpl-3.0)
[![Java](https://img.shields.io/badge/Made%20with-Java-orange)](https://openjdk.java.net)

![JoularJX Logo](joularjx.png)

JoularJX is a Java-based agent for software power monitoring at the source code level.

## :rocket: Features

- Monitor power consumption of each method at runtime
- Uses a Java agent, no source code instrumentation needed
- Uses Intel RAPL (powercap interface) for getting accurate power reading on GNU/Linux, our research-based regression models on Raspberry Pi devices, and a custom program monitor (based on Intel Power Gadget) for accurate power readings on Windows
- Provides real time power consumption of every method in the monitored program
- Provides total energy for every method on program exit

## :package: Installation

Just run the installation script in the ```install/``` folder:
- On Windows, run in a command line: ```windows-install.bat```. This will install JoularJX jar and ProgramMonitor to ```C:\joularjx```.
- On GNU/linux, run in a terminal: ```sh linux-install.sh```. This will install JoularJX to ```/opt/joularjx```.

You can also just use the compiled jar package for JoularJX.

JoularJX depend on the following software or packages in order to get power reading:
- On Windows, JoularJX uses a custom power monitor program that uses Intel Power Gadget API on Windows, and therefore require installing the [Intel Power Gadget tool](https://www.intel.com/content/www/us/en/developer/articles/tool/power-gadget.html) and using a supported Intel CPU.
- On PC/server GNU/Linux, JoularJX uses Intel RAPL interface through powercap, and therefore requires running on an Intel CPU or an AMD Ryzen CPU.
- On Raspberry Pi devices on GNU/Linux, JoularJX uses our own research-based regression models to estimate CPU power consumption with support for the following device models:
  - Model Zero W (rev 1.1), for 32 bits OS
  - Model 1 B (rev 2), for 32 bits OS
  - Model 1 B+ (rev 1.2), for 32 bits OS
  - Model 2 B (rev 1.1), for 32 bits OS
  - Model 3 B (rev 1.2), for 32 bits OS
  - Model 3 B+ (rev 1.3), for 32 bits OS
  - Model 4 B (rev 1.1, and rev 1.2), for both 32 bits and 64 bits OS
  - Model 400 (rev 1.0), for 64 bits OS

## :bulb: Usage

JoularJX is a Java agent where you can simply hook it to the Java Virtual Machine when starting your Java program:

```java -javaagent:joularjx-$version.jar yourProgram```

JoularJX will generate two CSV files (one for all methods, and one for the filtered methods) during runtime with real time power data of each method, and that are overwritten each second by the new power data.
When the program exits, JoularJX generates two new CSV files with the total energy of all monitored methods.

JoularJX can be configured by modifying the ```config.properties``` files:
- ```filter-method-names```: list of strings which will be used to filter the monitored methods (see Generated files below for explanations).
- ```save-runtime-data```: write runtime methods power consumption in a CSV file.
- ```overwrite-runtime-data```: overwrite runtime power data files, or if set to false, it will write new files for each monitoring cycle.
- ```logger-level```: set the level of information (by logger) given by JoularJX in the terminal (allowed values: OFF, INFO, WARNING, SEVERE).
- ```powermonitor-path```: Full path to the power monitor program (only for Windows).
- ```track-consumption-evolution```: generate .csv files for each method containing details of the method's consumption over the time. Each consumption value is mapped to an Unix timestamp.
- ```evolution-data-path```: Path to the location where the consumption evolution csv files will be stored. The tool will attemp to create the folders if they do not alreay exists.

You can install the jar package (and the PowerMonitor.exe on Windows) wherever you want, and call it in the ```javaagent``` with the full path.
However, ```config.properties``` must be copied to the same folder as where you run the Java command.

## :floppy_disk: Compilation

To build JoularJX, you need Java 11+ and Maven, then just build:

```mvn clean install```

To compile the Windows power monitor tool, required by JoularJX on Windows, open the project in Visual Studio and compile there.

## Generated files

For real time power data or the total energy at the program exit, JoularJX generated two CSV files:

- A file which contains power or energy data for all methods, include the JDK's ones.
- A *filtered file* which only includes the power or energy data of those filtered methods (can be configured in ```config.properties```). This data is not just a subset of the first data file, but rather a re-calculation done by JoularJX to provide accurate data: methods that start with the filtered keyword, will be allocated the power or energy consumed by the JDK methods that it calls.

For example, if ```Package1.MethodA``` calls ```java.io.PrintStream.println``` to print some text to a terminal, then we calculate:

- In the first file, the power or energy consumed by ```println``` separately from ```MethodA```. The latter power consumption won't include those consumed by ```println```.
- In the second file, if we filter methods from ```Package1```, then the power consumption of ```println``` will be added to ```MethodA``` power consumption, and the file will only provide power or energy of ```Package1``` methods.

We manage to do this by analyzing the stacktrace of all running threads on runtime.

## :newspaper: License

JoularJX is licensed under the GNU GPL 3 license only (GPL-3.0-only).

Copyright (c) 2021-2022, Adel Noureddine, Université de Pau et des Pays de l'Adour.
All rights reserved. This program and the accompanying materials are made available under the terms of the GNU General Public License v3.0 only (GPL-3.0-only) which accompanies this distribution, and is available at: https://www.gnu.org/licenses/gpl-3.0.en.html

Author : Adel Noureddine
