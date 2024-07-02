# <a href="https://www.noureddine.org/research/joular/"><img src="https://raw.githubusercontent.com/joular/.github/main/profile/joular.png" alt="Joular Project" width="64" /></a> JoularJX :microscope:

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue)](https://www.gnu.org/licenses/gpl-3.0)
[![Java](https://img.shields.io/badge/Made%20with-Java-orange)](https://openjdk.java.net)

![JoularJX Logo](joularjx.png)

JoularJX is a Java-based agent for software power monitoring at the source code level.

Detailed documentation (including user and reference guides) are available at: [https://joular.github.io/joularjx/](https://joular.github.io/joularjx/).

## :rocket: Features

- Monitor power consumption of each method at runtime
- Uses a Java agent, no source code instrumentation needed
- Uses Intel RAPL (powercap interface) for getting accurate power reading on GNU/Linux, our research-based regression models on Raspberry Pi devices, and a custom program monitor (using a RAPL driver) for accurate power readings on Windows
- Monitor energy of Java applications running in virtual machines
- Provides real-time power consumption of every method in the monitored program
- Provides total energy for every method on program exit

## :package: Compilation and Installation

To build JoularJX, you need Java 11+ and Maven, then just build:

```
mvn clean install -DskipTests
```

Alternatively, you can use the Maven wrapper shipped with the project with the command:

```
Linux: ./mvnw clean install -DskipTests
Windows: ./mvnw.cmd clean install -DskipTests
```

JoularJX depend on the following software or packages in order to get power reading:
- On Windows, JoularJX uses a custom power monitor program that uses the [Windows RAPL driver by Hubblo](https://github.com/hubblo-org/windows-rapl-driver), and therefore require installing the driver first, and runs on Intel or AMD CPUs (since Ryzen).
- On Windows, to read the data from the RAPL driver, we use a custom program monitor called [Power Monitor for Windows](https://github.com/joular/WinPowerMonitor). It used to be part of JoularJX, but now it is in its own repository. Download the binary (or compile the source code), and specify its path in ```config.properties```.
- On PC/server GNU/Linux, JoularJX uses Intel RAPL interface through powercap, and therefore requires running on an Intel CPU or an AMD CPU (since Ryzen).
- On macOS, JoularJX uses `powermetrics`, a tool bundled with macOS which requires running with `sudo` access. It is recommended to authorize the current users to run `/usr/bin/powermetrics` without requiring a password by making the proper modification to the `sudoers` file.
- On Raspberry Pi devices on GNU/Linux, JoularJX uses our own research-based regression models to estimate CPU power consumption with support for the following device models (we support all revisions of each model lineup. However, the model is generated and trained on a specific revision, listed between brackets, and the accuracy is best on this particular revision):
  - Model Zero W (rev 1.1), for 32-bit OS
  - Model 1 B (rev 2), for 32-bit OS
  - Model 1 B+ (rev 1.2), for 32-bit OS
  - Model 2 B (rev 1.1), for 32-bit OS
  - Model 3 B (rev 1.2), for 32-bit OS
  - Model 3 B+ (rev 1.3), for 32-bit OS
  - Model 4 B (rev 1.1, and rev 1.2), for both 32 bits and 64-bit OS
  - Model 400 (rev 1.0), for 64-bit OS
  - Model 5 B (rev 1.0), for 64-bit OS
- On virtual machines, JoularJX reads the power consumption of the virtual machine (measured in the host) from a file shared between the host and the guest.

We also support Asus Tinker Board (S).

## :bulb: Usage

JoularJX is a Java agent where you can simply hook it to the Java Virtual Machine when starting your Java program's main class:

```
java -javaagent:joularjx-$version.jar YourProgramMainClass
```

If your program is a JAR file, then just run it as usual while adding JoularJX:

```
java -javaagent:joularjx-$version.jar -jar yourProgram.jar
```

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
- ```powermonitor-path```: Full path to the [Power Monitor for Windows](https://github.com/joular/WinPowerMonitor) program (only for Windows).
- ```track-consumption-evolution```: generate CSV files for each method containing details of the method's consumption over the time. Each consumption value is mapped to an Unix timestamp.
- ```hide-agent-consumption```: if set to true, the energy consumption of the agent threads will not be reported.
- ```enable-call-trees-consumption```: compute methods call trees energy consumption. A CSV file will be generated at the end of the agent's execution, associating to each call tree it's total energy consumption.
- ```save-call-trees-runtime-data```: write runtime call trees power consumption in a CSV file. For each monitoring cycle (1 second), a new CSV file will be generated, containing the runtime power consumption of the call trees. The generated files will include timestamps in their names.
- ```overwrite-call-trees-runtime-data```: overwrite runtime call trees power data file, or if set to false, it will write new file for each monitoring cycle.
- ```application-server```: properly handles application servers and frameworks (Sprig Boot, Tomcat, etc.). Set ```true``` when running on application servers. If false, the monitoring loop will check if the JVM is destroyed, hence closing JoularJX when the application ends (in regular Java application). If true, JoularJX will continue to monitor correctly as the JVM isn't destroyed in a application server.
- ```vm-power-path```: the path for the power consumption of the virtual machine. Inside a virtual machine, indicate the file containing power consumption of the VM (which is usually a file in the host that is shared with the guest).
- ```vm-power-format```: power format of the shared VM power file. We currently support two formats: ```watts``` (a file containing one float value which is the power consumption of the VM), and ```powerjoular``` (a csv file generated by [PowerJoular](https://github.com/joular/powerjoular) in the host, containing 3 columns: timestamp, CPU utilization of the VM and CPU power of the VM).

You can install the jar package (and the PowerMonitor.exe on Windows) wherever you want, and call it in the ```javaagent``` with the full path.
However, ```config.properties``` must be copied to the same folder as where you run the Java command.

In virtual machines, JoularJX requires two steps:
- Installing a power monitoring tool in the host machine, which will monitor the virtual machine power consumption every second and writing it to a file (to be shared with the guest VM).
For example, you can use our [PowerJoular](https://github.com/joular/powerjoular).
- Use JoularJ in the guest VM while specifying the path of the power file shared with the host and its format.

## Generated files

For real-time power data or the total energy at the program exit, JoularJX generated two CSV files:

- A file which contains power or energy data for all methods, which include the JDK's ones.
- A *filtered file* which only includes the power or energy data of those filtered methods (can be configured in ```config.properties```). This data is not just a subset of the first data file, but rather a recalculation done by JoularJX to provide accurate data: methods that start with the filtered keyword, will be allocated the power or energy consumed by the JDK methods that it calls.

For example, if ```Package1.MethodA``` calls ```java.io.PrintStream.println``` to print some text to a terminal, then we calculate:

- In the first file, the power or energy consumed by ```println``` separately from ```MethodA```. The latter power consumption won't include those consumed by ```println```.
- In the second file, if we filter methods from ```Package1```, then the power consumption of ```println``` will be added to ```MethodA``` power consumption, and the file will only provide power or energy of ```Package1``` methods.

We manage to do this by analyzing the stacktrace of all running threads on runtime.

## JoularJX Reader

JoularJX Reader is a GUI to process, analyze and visualize JoularJX generated energy files.
It is available at its [own repository here](https://github.com/joular/joularjx-reader).

## :bookmark_tabs: Cite this work

To cite our work in a research paper, please cite our paper in the 18th International Conference on Intelligent Environments (IE2022).

- **PowerJoular and JoularJX: Multi-Platform Software Power Monitoring Tools**. Adel Noureddine. In the 18th International Conference on Intelligent Environments (IE2022). Biarritz, France, 2022.

```
@inproceedings{noureddine-ie-2022,
  title = {PowerJoular and JoularJX: Multi-Platform Software Power Monitoring Tools},
  author = {Noureddine, Adel},
  booktitle = {18th International Conference on Intelligent Environments (IE2022)},
  address = {Biarritz, France},
  year = {2022},
  month = {Jun},
  keywords = {Power Monitoring; Measurement; Power Consumption; Energy Analysis}
}
```

## :newspaper: License

JoularJX is licensed under the GNU GPL 3 license only (GPL-3.0-only).

Copyright (c) 2021-2024, Adel Noureddine, Université de Pau et des Pays de l'Adour.
All rights reserved. This program and the accompanying materials are made available under the terms of the GNU General Public License v3.0 only (GPL-3.0-only) which accompanies this distribution, and is available at: https://www.gnu.org/licenses/gpl-3.0.en.html

Author : Adel Noureddine
