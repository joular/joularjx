# <a href="https://www.noureddine.org/research/joular/"><img src="https://raw.githubusercontent.com/joular/.github/main/profile/joular.png" alt="Joular Project" width="64" /></a> JoularJX :microscope:

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue)](https://www.gnu.org/licenses/gpl-3.0)
[![Java](https://img.shields.io/badge/Made%20with-Java-orange)](https://openjdk.java.net)

![JoularJX Logo](joularjx.png)

JoularJX is a Java-based agent for software power monitoring at the source code level.

## :rocket: Features

- Monitor power consumption of each method at runtime
- Uses a Java agent, no source code instrumentation needed
- Uses Intel RAPL (powercap interface) for getting accurate power reading on GNU/Linux, our research-based regression models on Raspberry Pi devices, and a custom program monitor (based on Intel Power Gadget) for accurate power readings on Windows
- Provides real-time power consumption of every method in the monitored program
- Provides total energy for every method on program exit

## :package: Installation

Just run the installation script in the ```install/``` folder:
- On Windows, run in a command line: ```windows-install.bat```. This will install JoularJX jar and ProgramMonitor to ```C:\joularjx```.
- On GNU/Linux, run in a terminal: ```sh linux-install.sh```. This will install JoularJX to ```/opt/joularjx```.

You can also just use the compiled jar package for JoularJX.

JoularJX requires a minimum version of Java 11+.

JoularJX depend on the following software or packages in order to get power reading:
- On Windows, JoularJX uses a custom power monitor program that uses Intel Power Gadget API on Windows, and therefore require installing the [Intel Power Gadget tool](https://www.intel.com/content/www/us/en/developer/articles/tool/power-gadget.html) and using a supported Intel CPU.
- On PC/server GNU/Linux, JoularJX uses Intel RAPL interface through powercap, and therefore requires running on an Intel CPU or an AMD Ryzen CPU.
- On Raspberry Pi devices on GNU/Linux, JoularJX uses our own research-based regression models to estimate CPU power consumption with support for the following device models:
  - Model Zero W (rev 1.1), for 32-bit OS
  - Model 1 B (rev 2), for 32-bit OS
  - Model 1 B+ (rev 1.2), for 32-bit OS
  - Model 2 B (rev 1.1), for 32-bit OS
  - Model 3 B (rev 1.2), for 32-bit OS
  - Model 3 B+ (rev 1.3), for 32-bit OS
  - Model 4 B (rev 1.1, and rev 1.2), for both 32 bits and 64-bit OS
  - Model 400 (rev 1.0), for 64-bit OS

We also support Asus Tinker Board (S).

## :bulb: Usage

JoularJX is a Java agent where you can simply hook it to the Java Virtual Machine when starting your Java program's main class:

```java -javaagent:joularjx-$version.jar YourProgramMainClass```

If your program is a JAR file, then just run it as usual while adding JoularJX:

```java -javaagent:joularjx-$version.jar -jar yourProgram.jar```

JoularJX will generate multiple CSV files according to the configuration settings (in ```config.properties```), and will create these files in a ```joularjx-results```folder.

The generated files are available under the following folder structure:
- joularjx-results
  - appName-PID-start_timestamp
    - all (power/energy data for all methods, including the JDK ones)
      - runtime (power consumption every second)
        - calltree (consumption for each call tree branch)
        - methods (consumption for each methods)
      - total (total energy consumption, generated at the program's end)
        - calltree
        - methods
      - evolution (power consumption evolution of every method, throughout the execution of the application)
    - app (power/energy data for methods of the monitored application, according to the ```filter-method-names``` setting)
      - runtime
        - calltree
        - methods
      - total
        - calltree
        - methods
      - evolution

JoularJX can be configured by modifying the ```config.properties``` files:
- ```filter-method-names```: list of strings which will be used to filter the monitored methods (see Generated files below for explanations).
- ```save-runtime-data```: write runtime methods power consumption in a CSV file.
- ```overwrite-runtime-data```: overwrite runtime power data files, or if set to false, it will write new files for each monitoring cycle.
- ```logger-level```: set the level of information (by logger) given by JoularJX in the terminal (allowed values: OFF, INFO, WARNING, SEVERE).
- ```powermonitor-path```: Full path to the power monitor program (only for Windows).
- ```track-consumption-evolution```: generate CSV files for each method containing details of the method's consumption over the time. Each consumption value is mapped to an Unix timestamp.
- ```hide-agent-consumption```: if set to true, the energy consumption of the agent threads will not be reported.
- ```enable-call-trees-consumption```: compute methods call trees energy consumption. A CSV file will be generated at the end of the agent's execution, associating to each call tree it's total energy consumption.
- ```save-call-trees-runtime-data```: write runtime call trees power consumption in a CSV file. For each monitoring cycle (1 second), a new CSV file will be generated, containing the runtime power consumption of the call trees. The generated files will include timestamps in their names.
- ```overwrite-call-trees-runtime-data```: overwrite runtime call trees power data file, or if set to false, it will write new file for each monitoring cycle.

You can install the jar package (and the PowerMonitor.exe on Windows) wherever you want, and call it in the ```javaagent``` with the full path.
However, ```config.properties``` must be copied to the same folder as where you run the Java command.

## :floppy_disk: Compilation

To build JoularJX, you need Java 11+ and Maven, then just build:

```mvn clean install```

To compile the Windows power monitor tool, required by JoularJX on Windows, open the project in Visual Studio and compile there.

## Generated files

For real-time power data or the total energy at the program exit, JoularJX generated two CSV files:

- A file which contains power or energy data for all methods, which include the JDK's ones.
- A *filtered file* which only includes the power or energy data of those filtered methods (can be configured in ```config.properties```). This data is not just a subset of the first data file, but rather a recalculation done by JoularJX to provide accurate data: methods that start with the filtered keyword, will be allocated the power or energy consumed by the JDK methods that it calls.

For example, if ```Package1.MethodA``` calls ```java.io.PrintStream.println``` to print some text to a terminal, then we calculate:

- In the first file, the power or energy consumed by ```println``` separately from ```MethodA```. The latter power consumption won't include those consumed by ```println```.
- In the second file, if we filter methods from ```Package1```, then the power consumption of ```println``` will be added to ```MethodA``` power consumption, and the file will only provide power or energy of ```Package1``` methods.

We manage to do this by analyzing the stacktrace of all running threads on runtime.

## :newspaper: License

JoularJX is licensed under the GNU GPL 3 license only (GPL-3.0-only).

Copyright (c) 2021-2023, Adel Noureddine, Université de Pau et des Pays de l'Adour.
All rights reserved. This program and the accompanying materials are made available under the terms of the GNU General Public License v3.0 only (GPL-3.0-only) which accompanies this distribution, and is available at: https://www.gnu.org/licenses/gpl-3.0.en.html

Author : Adel Noureddine
