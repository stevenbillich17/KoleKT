package org.dxworks.kolekt

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.dxworks.kolekt.binders.ClassBinder
import org.dxworks.kolekt.details.DictionariesController
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.FileDTO
import org.dxworks.kolekt.extraction.FileExtractionListener
import org.dxworks.kolekt.serialization.KoleSerializer
import org.jetbrains.kotlin.spec.grammar.KotlinLexer
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParser.KotlinFileContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.ByteArrayInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.io.IOException
import java.lang.Exception
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.pathString

class ProjectExtractor(private val pathToProject: String, private val pathToGenerated: String? = null) {
    val kotlinExtension = ".kt"
    var pathToFiles: MutableList<String> = mutableListOf()
    var filesDTOs: MutableList<FileDTO> = mutableListOf()
    val logger = LoggerFactory.getLogger(ProjectExtractor::class.java)


    fun parse() {
        readPathToFiles()
        parseFiles()
        printDetailsFromFiles()
        printInteractiveInferface()
    }

    fun simpleParse() {
        readPathToFiles()
        parseFiles()
    }

    fun printInteractiveInferface() {
        var option = 0
        while (option !=5) {
            showMenu()
            option = readLine()?.toInt() ?: 0
            when (option) {
                1 -> enterClassesDtos()
                2 -> bindClasses()
                3 -> exportClasses()
                4 -> exportAllClasses()
                5 -> println("Exiting")
                else -> println("Invalid option")
            }
        }
    }

    private fun exportAllClasses() {
        if (pathToGenerated == null) {
            println("Path to generated not set")
            return
        }
        // store all the classes from all the files
        filesDTOs.forEach { fileDTO ->
            fileDTO.classes.forEach { classDTO ->
                File( "${pathToGenerated}\\${classDTO.getFQN()}.json").writeText(KoleSerializer.serialize(classDTO))
            }
        }
    }


    private fun exportClasses() {
        val cls = chooseClass()
        println(KoleSerializer.serialize(cls))
    }

    fun showMenu() {
        println("Menu:")
        println("1. Print classes Dtos")
        println("2. Bind classes Dtos")
        println("3. Serialize classes Dtos")
        println("4. Serialize all classes Dtos")
        println("5. Exit")
    }

    fun enterClassesDtos() {
        println(chooseClass())
    }

    private fun bindClasses() {
        val cls = chooseClass()
        val imports = mutableListOf<String>()
        for (file in filesDTOs) {
            if (file.filePackage == cls.classPackage) {
                imports.addAll(file.imports)
            }
        }
        val classBinder = ClassBinder(cls)
        classBinder.bind(imports, false)
    }

    fun bindAllClasses() {
        filesDTOs.forEach { fileDTO ->
            val imports = fileDTO.imports
            fileDTO.classes.forEach { classDTO ->
                val classBinder = ClassBinder(classDTO)
                classBinder.bind(imports, false)
            }
        }
    }

    private fun chooseClass(): ClassDTO {
        val optionsMap = mutableMapOf<Int, String>()
        var option = 0
        DictionariesController.getFQNClassesDictionary().forEach { (key,) ->
            optionsMap[option] = key
            println("$option. $key")
            option++
        }
        println("Choose a class")
        option = readLine()?.toInt() ?: 0
        val classFQN = optionsMap[option] ?: ""
        return DictionariesController.findClassAfterFQN(classFQN, true)
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

    private fun parseFiles() {
        val total  = pathToFiles.size
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
                filesDTOs.add(fileDTO)

            } catch (e : Exception) {
                logger.error("Exception at parsing file: {$filePath}. With message: {$e} stack trace:")
                e.printStackTrace()
            }
            printProgressBar(i+1, total)
        }
    }

    private fun buildTreeFromFile(file: File) : KotlinFileContext {
        val chStream = CharStreams.fromString(file.readText())
        val tokens = CommonTokenStream(KotlinLexer(chStream))
        val parser = KotlinParser(tokens)
        return parser.kotlinFile()
    }

    private fun readPathToFiles() {
        val folderPath = Paths.get(pathToProject)
        val visitor = object : SimpleFileVisitor<Path>() {
            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                file?.let {
                    if (it.toString().endsWith(kotlinExtension)) {
                        pathToFiles.add(it.toString())
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
    }

}

