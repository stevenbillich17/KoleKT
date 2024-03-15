package org.dxworks.kolekt

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.jetbrains.kotlin.spec.grammar.KotlinLexer
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener

fun main(args: Array<String>) {
    //runBasicTest()
    val extractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
    //val extractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\W.PROIECTE_DE_ANALIZAT\\stylist-master\\stylist-core\\src\\main")
    extractor.parse()
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