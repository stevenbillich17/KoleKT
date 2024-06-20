# KoleKT
Is a tool used for parsing and creating a model of Kotlin code. It is able
to compute different metrics on the provided source code.

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


## Running the tool
The tool can be run using the generated ```.jar``` file (how to obtain it is mentioned above).
The user can run the following command:
```shell
java -jar KoleKT.jar -h
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


## Acknowledgements
This project incorporates code from the ```Grammar-Tools``` project, which are licensed 
under the Apache License, Version 2.0. The full text of the license can be found in the
'license\APACHE-2.0.txt' file

The project computes metrics taken from the book “Object Oriented Metrics In
Practice” written by Michele Lanza and Radu Marinescu. ISBN: 9783540395386

