package org.dxworks.kolekt

import kotlinx.serialization.json.JsonObject
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.dxworks.kolekt.analyze.KoleAnalyzer
import org.dxworks.kolekt.analyze.KoleClazzAnalyzer
import org.dxworks.kolekt.analyze.KoleGeneralAnalyzer
import org.dxworks.kolekt.binders.FileBinder
import org.dxworks.kolekt.details.DictionariesController
import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.extraction.FileExtractionListener
import org.dxworks.kolekt.serialization.KoleSerializer
import org.jetbrains.kotlin.spec.grammar.KotlinLexer
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParser.KotlinFileContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.pathString

class ProjectExtractor(private val pathToProject: String, private val pathToGenerated: String? = null) {
    val kotlinExtension = ".kt"
    var pathToFiles: MutableList<String> = mutableListOf()
    val logger = LoggerFactory.getLogger(ProjectExtractor::class.java)

    init {
        FileController.setPathOnDisk(pathToGenerated)
    }

    fun computeSpecialMetricsFor(sourceFileName: String, targetFileName: String) {
        val sourceFileDTO = FileController.getFileFromCache(sourceFileName)
        val targetFileDTO = FileController.getFileFromCache(targetFileName)
        val listOfMetrics = listOf("extCalls", "returns", "extData", "extDataStrict", "declarations")
        val json = KoleAnalyzer.computeMetric(listOfMetrics, sourceFileDTO.getFileSavedName(), targetFileDTO.getFileSavedName(), false)
        println(json)
    }

    fun storeFilesOnDisk() {
        FileController.storeAllFilesOnDisk()
    }

    fun computeMetricsForAllClasses(pathToMetricsOnDisk: String?) {
        val allFiles = FileController.getFileNames()
        allFiles.forEach { fileName ->
            val fileDTO = FileController.getFileFromCache(fileName)
            fileDTO.classes.forEach { classDTO ->
                val jsonObject = KoleClazzAnalyzer.analyze(classDTO.getFQN(), false)
                if (pathToMetricsOnDisk != null) {
                    val file = File("$pathToMetricsOnDisk\\${classDTO.getFQN()}.json")
                    file.writeText(jsonObject.toString())
                } else {
                    println(jsonObject)
                }
            }
        }
    }

    fun loadFilesFromDisk(maximumNumberOfFiles: Int, pathOnDisk: String) {
        FileController.clean()
        FileController.setMaximumNumberOfFiles(maximumNumberOfFiles)
        FileController.setPathOnDisk(pathOnDisk)
        FileController.loadFilesFromDisk()
    }


    fun bindFromDisk(maximumNumberOfFiles: Int, pathOnDisk: String) {
        FileController.clean()
        FileController.setMaximumNumberOfFiles(maximumNumberOfFiles)
        FileController.setPathOnDisk(pathOnDisk)
        FileController.loadFilesFromDisk()
        bindAllClasses()
    }

    fun parseAndSaveToDisk(customCachePlace: String) {
        FileController.clean()
        val startTime = System.currentTimeMillis()
        val filePaths = readPathToFiles(pathToProject)
        FileController.setPathOnDisk(customCachePlace)
        parseFiles(filePaths)
        val endTime = System.currentTimeMillis()
        val minutes = (endTime - startTime) / 1000 / 60
        val seconds = (endTime - startTime) / 1000 % 60
        println("Parsing took $minutes minutes and $seconds seconds")
        FileController.storeAllFilesOnDisk()
    }

    fun parse() {
        readPathToFiles(pathToProject)
        parseFiles()
        printDetailsFromFiles()
        printInteractiveInferface()
    }

    fun simpleParse() {
        val startTime = System.currentTimeMillis()
        readPathToFiles(pathToProject)
        parseFiles()
        val endTime = System.currentTimeMillis()
        val minutes = (endTime - startTime) / 1000 / 60
        val seconds = (endTime - startTime) / 1000 % 60
        println("Parsing took $minutes minutes and $seconds seconds")
    }

    fun printInteractiveInferface() {
        var option = 0
        while (option != 9) {
            showMenu()
            option = readLine()?.toInt() ?: 0
            when (option) {
                1 -> enterClassesDtos()
                2 -> bindAllClasses()
                3 -> computeSpecialMetrics()
                4 -> computeBasicMetrics()
                5 -> exportClasses()
                6 -> exportAllFiles()
                7 -> enterFileDtos()
                8 -> getGeneralStats()
                9 ->  {
                    println("Exiting")
                    return
                }
                else -> println("Invalid option")
            }
        }
    }

    private fun getGeneralStats() {
        println(KoleGeneralAnalyzer.getStatistics())
    }

    private fun enterFileDtos() {
        val allFiles = FileController.getFileNames()
        val optionsMap = mutableMapOf<Int, String>()
        var option = 0
        allFiles.forEach { fileName ->
            optionsMap[option] = fileName
            println("$option -> $fileName")
            option++
        }
        println("Choose a file")
        option = readLine()?.toInt() ?: 0
        val fileDTO = FileController.getFileFromCache(optionsMap[option] ?: "")
        println(fileDTO)
    }

    private fun computeBasicMetrics() {
        val classDTO = chooseClass()
        val jsonObject = KoleClazzAnalyzer.analyze(classDTO.getFQN(), false)
        println(jsonObject)
    }

    private fun computeSpecialMetrics() {
        val allFiles = FileController.getFileNames()
        val optionsMap = mutableMapOf<Int, String>()
        var option = 0
        allFiles.forEach { fileName ->
            optionsMap[option] = fileName
            println("$option -> $fileName")
            option++
        }
        println("Choose source file")
        option = readLine()?.toInt() ?: 0
        val fileDTO = FileController.getFileFromCache(optionsMap[option] ?: "")
        println("Choose target file")
        option = readLine()?.toInt() ?: 0
        val targetFileDTO = FileController.getFileFromCache(optionsMap[option] ?: "")
        val listOfMetrics = listOf("extCalls", "returns", "extData", "extDataStrict", "declarations")
        val json = KoleAnalyzer.computeMetric(listOfMetrics, fileDTO.getFileSavedName(), targetFileDTO.getFileSavedName(), false)
        println(json)
    }

    private fun exportAllFiles() {
        if (pathToGenerated == null) {
            println("Path to generated not set")
            return
        }
        // store all the classes from all the files without binding them
        val allFiles = FileController.getFileNames()
        allFiles.forEach { fileName ->
            val fileDTO = FileController.getFileFromCache(fileName)
            val file = File("$pathToGenerated\\${fileDTO.filePackage}.${fileDTO.fileName}.json")
            file.writeText(KoleSerializer.serialize(fileDTO))
        }
        println("Files exported successfully at $pathToGenerated")
    }


    private fun exportClasses() {
        val cls = chooseClass()
        println(KoleSerializer.serialize(cls))
    }

    fun showMenu() {
        println("")
        println("Menu:")
        println("1. Print classes Dtos")
        println("2. Bind all classes Dtos")
        println("3. Compute special metrics")
        println("4. Compute basic metrics")
        println("5. Serialize classes Dtos")
        println("6. Serialize files and store to disk")
        println("7. Print a file")
        println("8. Get general stats")
        println("9. Exit")
    }

    fun enterClassesDtos() {
        println(chooseClass())
    }

    fun bindAllClasses() {
        val allFiles = FileController.getFileNames()
        val total = allFiles.size
        val starTime = System.currentTimeMillis()
        println("Binding all files...")
        print("\r[]0%")
        var i = 0
        for (fileName in allFiles) {
            printProgressBar(i+1, total)
            try {
                val fileBinder = FileBinder(FileController.getFileFromCache(fileName))
                fileBinder.bind()
            }catch (e : Exception) {
                logger.warn("Exception at binding file: {$fileName}. With message: {$e} stack trace:")
            }
            i++
        }
        val endTime = System.currentTimeMillis()
        println("\nDone binding files")
        val minutes = (endTime - starTime) / 1000 / 60
        val seconds = (endTime - starTime) / 1000 % 60
        println("Binding took $minutes minutes and $seconds seconds")
    }

    private fun chooseClass(): ClassDTO {
        val optionsMap = mutableMapOf<Int, String>()
        var option = 0
        val allFiles = FileController.getFileNames()
        allFiles.forEach { fileName ->
            val fileDTO = FileController.getFileFromCache(fileName)
            fileDTO.classes.forEach { classDTO ->
                optionsMap[option] = classDTO.getFQN()
                println("$option. ${classDTO.getFQN()}")
                option++
            }
        }
        println("Choose a class")
        option = readLine()?.toInt() ?: 0
        val classFQN = optionsMap[option] ?: ""
        return FileController.findClassInFiles(classFQN) ?: throw Exception("Class not found")
    }

    private fun printDetailsFromFiles() {
//        println("\n\n----------FROM HERE ON, WE PRINT THE FILES DETAILS----------\n\n")
//        filesDTOs.forEach { fileDTO ->
//            println(fileDTO)
//        }
    }

    private fun printProgressBar(progress: Int, total: Int) {
        val progressBarLength = 50 // Length of the progress bar in characters
        val progressPercentage = (progress.toFloat() / total.toFloat())
        val progressChars = (progressPercentage * progressBarLength).toInt()
        val progressBar = "=".repeat(progressChars) + " ".repeat(progressBarLength - progressChars)
        print("\r[$progressBar] ${String.format("%.2f", progressPercentage * 100)}%")
    }

    private fun parseFiles(pathToFiles: MutableList<String> = this.pathToFiles) {
        val total  = pathToFiles.size
        println("Parsing files...")
        print("\r[]0%")
        for (i in 0..<total) {
            val filePath = pathToFiles[i]
            try {
                val file = File(filePath)
                val tree = buildTreeFromFile(file)
                val listener = FileExtractionListener(filePath, file.name)

                val walker = ParseTreeWalker()
                walker.walk(listener, tree)

                val fileDTO = listener.getFileDTO()
                fileDTO.addClasses(listener.getClassesDTOs())
                fileDTO.classes.forEach() {
                    it.setImports(fileDTO.imports)
                    it.setImportAliases(fileDTO.importAliases)
                }
                //filesDTOs.add(fileDTO)
                FileController.addFileDTO(fileDTO)
            } catch (e : Exception) {
                logger.error("Exception at parsing file: {$filePath}. With message: {$e} stack trace:")
                e.printStackTrace()
            }
            printProgressBar(i+1, total)
        }
        println("\nDone parsing files")
    }

    private fun buildTreeFromFile(file: File) : KotlinFileContext {
        val chStream = CharStreams.fromString(file.readText())
        val tokens = CommonTokenStream(KotlinLexer(chStream))
        val parser = KotlinParser(tokens)
        return parser.kotlinFile()
    }

    private fun readPathToFiles(pathToProject: String): MutableList<String> {
        val localPathToFiles = mutableListOf<String>()
        val folderPath = Paths.get(pathToProject)
        val visitor = object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                file?.let {
                    if (it.toString().endsWith(kotlinExtension)) {
                        pathToFiles.add(it.toString())
                        localPathToFiles.add(it.toString())
                        logger.debug("Added new path: {{}}", it)
                    }
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
                val stringPath = file?.pathString ?: "UNKNOWN PATH"
                val excMessage = exc?.message ?: "UNKNOWN MESSAGE"
                logger.error("ERROR at visiting: {{}}. With text: {{}}", stringPath, excMessage)
                return FileVisitResult.CONTINUE
            }
        }

        Files.walkFileTree(folderPath, visitor)
        return localPathToFiles
    }

    fun computeGeneralMetrics() {
        println(KoleGeneralAnalyzer.getStatistics())
    }

}

