
<div style="display: flex; align-items: center;">
  <img src="logo/KoleKT-logo-1.png" alt="Example Image" width="120" style="margin-right: 10px;">
  <h1 style="margin: 0;">KoleKT</h1>
</div>

Is a tool used for parsing and creating a model of Kotlin code. It is able
to compute both software and relation metrics on the provided source code.

## Build the JAR
To be able to build and run the tool. You must have a ```.jar``` file from the 'grammar-tools'
repository. [Link to the repository](https://github.com/Kotlin/grammar-tools)

After getting the file, the path for it that resides in the ```build.gradle``` must be changed
so that it matches the one where the ```.jar``` file was placed.

To be able to build the application Java Development Kit must also be installed on the
machine where KoleKT is placed.

Kotlin compiler and Gradle must also be installed on the machine for this process.

To build a ```.jar``` file the following command must be run:
```shell
gradlew jar
```
The command above will generate the file under the path "build\libs". An important observation
is that the command might differ slightly on different operating systems. This one
works for Windows OS.

Default implementation uses _TRACE_ logging level. When running the tool this will generate
a lot of information for debugging the app. This can be changed inside the `src/main/resources/logback.xml`
by using different levels, for example changing *DEBUG* to *ERROR* will result in less debugging
information, this can be used for production environment.


## Running the tool
The tool can be run using the generated ```.jar``` file (how to obtain it is mentioned above).
The user can run the following command:
```shell
java -jar KoleKT-v1.0.jar -h
```
The option `-h` indicated the help menu for KoleKT. This should contain all the needed
information for accessing all the functionalities of the tool.

## Options

- `-h`

   Prints the help message


- `-h <command>`

  Print help for the specified command, it also contains some examples of how to use the command.


- `-cs <cacheSize>`

  The size of the cache used for binding the project. Default is 100


- `-pi <parsing inputPath>`

  The path to the Kotlin project to be analyzed and parsed


- `-po <parsing outputPath>`

  The path to the directory where the parsed project will be saved


- `-ld <load inputPath> `

  The path to the directory where the parsed project is saved. Can be ignored if the parsing outputPath is provided.
  By loading the project from disk, the project will automatically binded


- `-bind`

  Bind the project after loading it from disk


- `-mo <metrics outputPath>`

  The path to the directory where the metrics will be saved. Otherwise the metrics will be printed to the console


- `-classMetrics`

  Compute class metrics for all classes in the project


- `-specialMetrics <file1> <file2>`

  Compute special metrics for the two files


- `-storeBindings`

  Store the bindings on disk. It requires the project a load path to be provided


- `-generalMetrics `

  Compute general metrics for the project


## Info about metrics
KoleKT computes 15 different metrics on each class. It is also able to compute
5 relational metrics between two files given as input to the tool.


## Acknowledgements
This project incorporates code from the ```Grammar-Tools``` project, which are licensed 
under the Apache License, Version 2.0. The full text of the license can be found in the
'licenses\APACHE-2.0.txt' file

The project computes metrics taken from the book “Object Oriented Metrics In
Practice” written by Michele Lanza and Radu Marinescu. ISBN: 9783540395386

