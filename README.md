
<div style="display: flex; align-items: center;">
  <img src="logo/KoleKT-logo.png" alt="Example Image" width="160" style="margin-right: 10px;">
  <h1 style="margin: 0;">KoleKT</h1>
</div>

KoleKT is a tool used for parsing and creating a model of Kotlin code. It is able to compute both
software and relation metrics on the provided source code and even some general ones.
It has a modular structure, making it able to run different stages at different times.

In the first stage, KoleKT can quickly create a simple model of the code. In the second stage,
it can resolve the types for each element of the code, significantly improving the functionality
and enlarging the possibilities of using the model. For the last stage, KoleKT computes different
metrics on the resulting project model, and these can help a developer spot further code problems,
like poor code design. So, KoleKT is an ideal solution for static analysis of Kotlin codebases for
specific problems.

## Build the JAR
Firstly, clone the project on your local machine using the following command:

`git clone https://github.com/stevenbillich17/KoleKT.git`

To be able to build and run the tool, you must have a ```.jar``` file from the 'grammar-tools'
repository. [Link to the repository](https://github.com/Kotlin/grammar-tools)

The currently supported version for the 'grammar-tools' is "v0.1-43".

After getting the file, the path from KoleKT project for it that resides in the KoleKT root folder
in `build.gradle` file and refers to the 'grammar-tools' must be changed so that it matches the one where
the 'grammar-tools' `.jar` file was placed after following the previous step.

To be able to build the application Java Development Kit must also be installed on the machine where
KoleKT is placed along with Kotlin compiler and Gradle.

To build a `.jar` file the following command must be run inside the root of the KoleKT project, where the
'gradlew.bat' file is located:
```shell
gradlew jar
```
The command above will generate the file under the path "build\libs". An important observation
is that the command might differ slightly on different operating systems. This one
works for Windows OS.

Default implementation uses _ERROR_ logging level. When running the tool this will NOT generate
a lot of information for debugging the app. This can be changed inside the `src/main/resources/logback.xml`
by using different levels, for example changing *ERROR* to *TRACE* will result in a lot of debugging
information, this can be used for developing the tool.


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
KoleKT computes 14 different metrics for each class. It is also able to compute 5 relational metrics
between two files given as input to the tool. It has the ability to compute some general
metrics about the project.

## Results
The model of the code will be placed as different JSON files in the path provided for it. These will
contain the extracted information about the code and if the flag for binding was set the stage for
clarification will update these values with the right information about the fully qualified names.

The software metrics generated for each class will be placed inside the specified folder by the user
under the format of a JSON file for each class. The returned JSON for the relation metrics and for
the general metrics will be sent to the standard output.

## Acknowledgements
This project incorporates code from the ```Grammar-Tools``` project, which are licensed 
under the Apache License, Version 2.0. The full text of the license can be found in the
'licenses\APACHE-2.0.txt' file. KoleKT uses the tree and the listener interfaces
generated by the library to extract information about the Kotlin source code, without making changes
in the original library.

The project computes metrics taken from the book “Object Oriented Metrics In
Practice” written by Michele Lanza and Radu Marinescu. ISBN: 9783540395386

## Github Contact
User: stevenbillich17
