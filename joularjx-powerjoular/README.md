# [![Joular Project](https://gitlab.com/uploads/-/system/group/avatar/10668049/joular.png?width=64)](https://www.noureddine.org/research/joular/) JoularJX :microscope:

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue)](https://www.gnu.org/licenses/gpl-3.0)
[![Java](https://img.shields.io/badge/Made%20with-Java-orange)](https://openjdk.java.net)

JoularJX is a Java-based agent for software power monitoring at the source code level.

## :rocket: Features

- Monitor power consumption of each method at runtime
- Uses a Java agent, no source code instrumentation needed
- Uses [PowerJoular](https://gitlab.com/joular/powerjoular) for getting accurate power reading on multiple platforms
- Provides real time power consumption of every method in the monitored program
- Provides total energy for every method on program exit

## :package: Installation

You can just use the compiled Jar package for JoularJX.
To get power readings, JoularJX uses [PowerJoular](https://gitlab.com/joular/powerjoular), and therefore you need to install it first.

## :bulb: Usage

JoularJX is a Java agent where you can simply hook it to the Java Virtual Machine when starting your Java program:

```java -javaagent:joularjx.jar yourProgram```

JoularJX will generate two CSV files (one for all methods, and one for the filtered methods) during runtime with real time power data of each method, and that are overwritten each second by the new power data.
When the program exits, JoularJX generates two new CSV files with the total energy of all monitored methods.

JoularJX can be configured by modifying the ```config.properties``` files.

## :floppy_disk: Compilation

To build JoularJX, you need Java 11+ and Maven, then just build:

```mvn clean install``` 

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

Copyright (c) 2021, Adel Noureddine, Université de Pau et des Pays de l'Adour.
All rights reserved. This program and the accompanying materials are made available under the terms of the GNU General Public License v3.0 only (GPL-3.0-only) which accompanies this distribution, and is available at: https://www.gnu.org/licenses/gpl-3.0.en.html

Author : Adel Noureddine
