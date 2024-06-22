package org.dxworks.kolekt

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.jetbrains.kotlin.spec.grammar.KotlinLexer
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener

fun main(args: Array<String>) {
    //runBasicTest()

//    val extractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage",
//        "E:\\AA.Faculta\\LICENTA\\A.KoleKT-Generated")

    //val extractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage\\fieldtypes")
    //val extractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage\\fieldtypes\\declarations")
    //val extractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\W.PROIECTE_DE_ANALIZAT\\stylist-master\\stylist-core\\src\\main")
    //extractor.parse()


    var parsingInputPath: String? = null
    var parsingOutputPath: String? = null
    var loadPath: String? = null
    var onlyLoad = false
    var cacheSize: Int = 100
    var bind: Boolean = false
    var storeBindings: Boolean = false

    var pathToMetrics: String? = null
    var computeClassMetricsOnAll = false
    var computeSpecialMetrcs = false
    var computeGeneralMetrics = false
    var smellCode = false

    var sourceFile: String? = null
    var targetFile: String? = null


    for (i in args.indices) {
        if (args[i] == "-pi" && i + 1 < args.size) {
            parsingInputPath = args[i + 1]
        } else if (args[i] == "-po" && i + 1 < args.size) {
            parsingOutputPath = args[i + 1]
        } else if (args[i] == "-ld" && i + 1 < args.size) {
            loadPath = args[i + 1]
            onlyLoad = true
        } else if (args[i] == "-h") {
            // check if the next argument is a command
            if (i+1 < args.size && !args[i+1].startsWith("-")) {
                printHelpForCommand(args[i+1])
            } else {
                printHelp()
            }
        } else if (args[i] == "-mo" && i + 1 < args.size) {
            pathToMetrics = args[i + 1]
        } else if (args[i] == "-classMetrics") {
            computeClassMetricsOnAll = true
        } else if (args[i] == "-cs" && i + 1 < args.size) {
            cacheSize = args[i + 1].toInt()
        } else if (args[i] == "-bind") {
            bind = true
        } else if (args[i] == "-specialMetrics" && i + 2 < args.size) {
            computeSpecialMetrcs = true
            // read names of two files
            sourceFile = args[i + 1]
            targetFile = args[i + 2]
        } else if (args[i] == "-storeBindings") {
            storeBindings = true
        } else if (args[i] == "-generalMetrics") {
            computeGeneralMetrics = true
        } else if (args[i] == "-smell") {
            smellCode = true
        }
    }

    if (parsingInputPath != null && parsingOutputPath != null) {
        val extractor = ProjectExtractor(parsingInputPath, parsingOutputPath)
        extractor.parseAndSaveToDisk(parsingOutputPath)
    }

    if (parsingOutputPath != null && loadPath == null) {
        loadPath = parsingOutputPath
    }

    if (computeClassMetricsOnAll && loadPath != null) {
        val extractor = ProjectExtractor("", loadPath)
        if (bind) {
            extractor.bindFromDisk(cacheSize, loadPath)
        } else {
            extractor.loadFilesFromDisk(cacheSize, loadPath)
        }
        extractor.computeMetricsForAllClasses(pathToMetrics)
    }

    if (computeGeneralMetrics && loadPath != null) {
        val extractor = ProjectExtractor("", loadPath)
        if (bind) {
            extractor.bindFromDisk(cacheSize, loadPath)
        } else {
            extractor.loadFilesFromDisk(cacheSize, loadPath)
        }
        extractor.computeGeneralMetrics()
    }

    if (computeSpecialMetrcs && loadPath != null) {
        val extractor = ProjectExtractor("", loadPath)
        if (bind) {
            extractor.bindFromDisk(cacheSize, loadPath)
        } else {
            extractor.loadFilesFromDisk(cacheSize, loadPath)
        }
        if (sourceFile != null && targetFile != null) {
            extractor.computeSpecialMetricsFor(sourceFile, targetFile)
        } else {
            println("Special metrics require two files to be provided.")
        }
    }

    if (smellCode && loadPath != null) {
        val extractor = ProjectExtractor("", loadPath)
        extractor.computeCodeSmells()
    }

    if (storeBindings && loadPath != null) {
        val extractor = ProjectExtractor("", loadPath)
        extractor.storeFilesOnDisk()
    }

}

fun printHelpForCommand(s: String) {
    if (s == "specialMetrics") {
        println("""
            Special Metrics:
            This command computes special metrics for two files. The metrics are:
            The file names are passed as arguments to the command under the tool form
            example: java -jar KoleKT.jar -specialMetrics org.example.FirstFile.kt org.example.SecondFile.kt
            """.trimIndent())
    } else if (s == "cs") {
        println("""
            Cache Size:
            This command sets the size of the cache used for binding the project. Meaning how many File DTOs can
            be stored in memory at a time. The default size is 100
            example: java -jar KoleKT.jar -cs 200
            """.trimIndent())
    } else if (s == "pi") {
        println("""
            Parsing Input Path:
            This command sets the path to the Kotlin project to be analyzed and parsed
            example: java -jar KoleKT.jar -pi E:\\Example\\Project
            """.trimIndent())
    } else if (s == "po") {
        println("""
            Parsing Output Path:
            This command sets the path to the directory where the parsed project will be saved
            example: java -jar KoleKT.jar -po E:\\Example\\ParsedProject
            """.trimIndent())
    } else {
        println("Command does not have help.")
    }
}

fun printHelp() {
    println("""
        Usage: java -jar KoleKT.jar [-cs <cacheSize>] [-pi <parsing inputPath>] [-po <parsing outputPath>] [-ld <load inputPath>] [-h] [-h <command>] [-bind] [-mo <metrics outputPath>] [-classMetrics] [-specialMetrics <file1> <file2>] [-storeBindings
        
        Options:
        -cs <cacheSize>                  The size of the cache used for binding the project. Default is 100
        -pi <parsing inputPath>          The path to the Kotlin project to be analyzed and parsed
        -po <parsing outputPath>         The path to the directory where the parsed project will be saved
        -ld <load inputPath>             The path to the directory where the parsed project is saved. Can be ignored if the parsing outputPath is provided.
                                         By loading the project from disk, the project will automatically binded
        -h                               Print this help message
        -h <command>                     Print help for the specified command, it also contains some examples of how to use the command. 
                                         Not all commands have help
        -bind                            Bind the project after loading it from disk
        -mo <metrics outputPath>         The path to the directory where the metrics will be saved. Otherwise the metrics will be printed to the console
        -classMetrics                    Compute class metrics for all classes in the project
        -specialMetrics <file1> <file2>  Compute special metrics for the two files
        -storeBindings                   Store the bindings on disk. It requires the project a load path to be provided
        -generalMetrics                  Compute general metrics for the project
        """.trimIndent()
    )
}

fun runBasicTest() {
    println("Basic Test:")
    val basicTest = "var number: Double;"
    val listener = object : KotlinParserBaseListener() {
        override fun enterVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
            ctx?.let {
                println("Variable name: ${it.simpleIdentifier().text}")
                println("Variable type: ${it.type()?.text}")
            }
        }
    }

    val chStream = CharStreams.fromString(basicTest)
    val tokens = CommonTokenStream(KotlinLexer(chStream))
    val parser = KotlinParser(tokens)
    val tree = parser.kotlinFile()
    val walker = ParseTreeWalker()

    walker.walk(listener, tree)
}