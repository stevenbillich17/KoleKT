import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.jetbrains.kotlin.spec.grammar.KotlinLexer
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParser.KotlinFileContext
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import kotlin.io.path.pathString

class ProjectExtractor(private val pathToProject: String) {
    val kotlinExtension = ".kt"
    var pathToFiles: MutableList<String> = mutableListOf()
    fun parse() {
        readPathToFiles()
        parseFiles()
    }

    private fun parseFiles() {
        pathToFiles.forEach { filePath ->
            try {
                val file = File(filePath)
                val tree = buildTreeFromFile(file)
                val listener = FileExtractionListener(filePath)

                val walker = ParseTreeWalker()
                walker.walk(listener, tree)

                val fileDTO = listener.getFileDTO();


            } catch (e : Exception) {
                println("ERROR {$e}")
            }
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
                        println("Added new path: {${it}}")
                    }
                }
                return FileVisitResult.CONTINUE
            }

            override fun visitFileFailed(file: Path?, exc: IOException?): FileVisitResult {
                val stringPath = file?.pathString ?: "UNKNOWN PATH"
                val excMessage = exc?.message ?: "UNKNOWN MESSAGE"
                println("ERROR at visiting: $stringPath. With text: $excMessage")

                return FileVisitResult.CONTINUE
            }
        }

        Files.walkFileTree(folderPath, visitor)
    }

}