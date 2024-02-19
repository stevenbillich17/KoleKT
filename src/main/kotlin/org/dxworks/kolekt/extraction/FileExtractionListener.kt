package org.dxworks.kolekt.extraction

import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.dxworks.kolekt.context.ParsingContext
import org.dxworks.kolekt.dtos.*
import org.dxworks.kolekt.enums.AttributeType
import org.dxworks.kolekt.listeners.FieldListener
import org.dxworks.kolekt.listeners.FunctionListener
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener
import org.slf4j.LoggerFactory

class FileExtractionListener(private val pathToFile: String, private val name: String) : KotlinParserBaseListener() {
    private val fileDTO = FileDTO(pathToFile, name)
    private val parsingContext = ParsingContext()
    private val logger = LoggerFactory.getLogger(FileExtractionListener::class.java)
    
    override fun enterKotlinFile(ctx: KotlinParser.KotlinFileContext?) {
        ctx?.let {
            fileDTO.filePackage = it.packageHeader().identifier().text
        }
    }

    override fun enterPrimaryConstructor(ctx: KotlinParser.PrimaryConstructorContext?) {
        if (ctx == null) return
        parsingContext.insidePrimaryConstructor = true
    }

    override fun exitPrimaryConstructor(ctx: KotlinParser.PrimaryConstructorContext?) {
        parsingContext.insidePrimaryConstructor = false
    }

    override fun enterFunctionValueParameters(ctx: KotlinParser.FunctionValueParametersContext?) {
        if (ctx == null) return
        logger.debug("Function value parameters: ${ctx.text}")
    }

    override fun enterImportHeader(ctx: KotlinParser.ImportHeaderContext?) {
        ctx?.let {
            fileDTO.addImport(it.identifier().text)
        }
    }

    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        if (ctx == null) return
        parsingContext.classDTO =  ClassDTO(ctx.simpleIdentifier().text)
        parsingContext.classDTO!!.classPackage = fileDTO.filePackage
        parsingContext.insideClassDeclaration = true
    }

    override fun exitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        ctx?.let {
            if (fileDTO.filePackage == null) {
                fileDTO.filePackage = "UNKNOWN"
            }

            // added primary constructor fields
            ctx.primaryConstructor()?.classParameters()?.classParameter()?.forEach { classParameter ->
                run {
                    val field = AttributeDTO(
                        classParameter.simpleIdentifier().text,
                        classParameter.type().text,
                        AttributeType.FIELD
                    )
                    parsingContext.classDTO!!.classFields.add(field)
                }
            }

            parsingContext.classDTO!!.classAnnotations.addAll(parsingContext.mutableListOfAnnotations)
            parsingContext.mutableListOfAnnotations.clear()

            parsingContext.classesDTOs.add(parsingContext.classDTO!!)
            parsingContext.classDTO = null
        }
        parsingContext.insideClassDeclaration = false
    }

    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if (ctx == null) return

        if (parsingContext.insideClassDeclaration) {
            // todo: should remove this when moving function parsing logic her
            return
        }
        parsingContext.insideFunctionDeclaration = true

        parseFunctionDeclaration(ctx)?.let { methodDTO ->
            fileDTO.functions.add(methodDTO)
        }
    }

    override fun exitFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if (ctx == null) return

        parsingContext.insideFunctionDeclaration = false
    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        if (ctx == null) return

        parsingContext.insideFunctionBody = true
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        if (ctx == null) return

        parsingContext.insideFunctionBody = false
    }

    override fun enterClassMemberDeclaration(ctx: KotlinParser.ClassMemberDeclarationContext?) {
        if (ctx == null) return
        parsingContext.insideClassMemberDeclaration = true
    }

    override fun exitClassMemberDeclaration(ctx: KotlinParser.ClassMemberDeclarationContext?) {
        if (ctx == null) return
        parsingContext.insideClassMemberDeclaration = false
    }

    override fun enterDeclaration(ctx: KotlinParser.DeclarationContext?) {
        if (ctx == null) return
        if (ctx.functionDeclaration() != null && checkIfClassMethod()) {
            parseFunctionDeclaration(ctx.functionDeclaration())?.let { methodDTO ->
                parsingContext.classDTO!!.classMethods.add(methodDTO)
            }
        } else if (ctx.propertyDeclaration() != null && checkIfClassField()) {
            parsePropertyDeclaration(ctx.propertyDeclaration())?.let { attributeDTO ->
                parsingContext.classDTO!!.classFields.add(attributeDTO)
            }
        }
        parsingContext.insideDeclaration = true
    }

    override fun enterClassBody(ctx: KotlinParser.ClassBodyContext?) {
        if (ctx == null) return
        parsingContext.insideClassBody = true
    }

    override fun exitClassBody(ctx: KotlinParser.ClassBodyContext?) {
        if (ctx == null) return
        parsingContext.insideClassBody = false
    }

    override fun enterUserType(ctx: KotlinParser.UserTypeContext?) {
        if (ctx == null) return
        parsingContext.insideUserType = true
        if (checkIfUserTypeIsForAnnotation()) {
            parsingContext.annotationName = ctx.text
        }
    }

    override fun exitUserType(ctx: KotlinParser.UserTypeContext?) {
        if (ctx == null) return
        parsingContext.insideUserType = false
    }

    override fun enterValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null) return
        if (parsingContext.insideAnnotation) {
            parsingContext.annotationArguments.add(ctx.text)
        }
    }

    override fun enterAnnotation(ctx: KotlinParser.AnnotationContext?) {
        if (ctx == null ) return
        parsingContext.insideAnnotation = true
    }

    override fun exitAnnotation(ctx: KotlinParser.AnnotationContext?) {
        if (ctx == null ) return
        parsingContext.insideAnnotation = false
    }

    override fun enterSingleAnnotation(ctx: KotlinParser.SingleAnnotationContext?) {
        if (ctx == null ) return
        parsingContext.insideSingleAnnotation = true
    }

    override fun exitSingleAnnotation(ctx: KotlinParser.SingleAnnotationContext?) {
        if (ctx == null ) return
        parsingContext.insideSingleAnnotation = false
        if (checkIfClassAnnotation()) {
            val singleAnnotation = AnnotationDTO(parsingContext.annotationName)
            singleAnnotation.addAnnotationArguments(parsingContext.annotationArguments)
            parsingContext.annotationArguments.clear()
            logger.debug("Adding class annotation: {}", singleAnnotation)
            parsingContext.mutableListOfAnnotations.add(singleAnnotation)
        }
    }

    private fun checkIfClassAnnotation(): Boolean {
        return parsingContext.insideClassDeclaration
                && !parsingContext.insideClassBody
    }

    private fun checkIfUserTypeIsForAnnotation(): Boolean {
        return parsingContext.insideSingleAnnotation
    }
    private fun checkIfClassField(): Boolean {
        return parsingContext.insideClassMemberDeclaration
                && !parsingContext.insideFieldDeclaration
                && !parsingContext.insideFunctionDeclaration
                && !parsingContext.insideFunctionBody
    }

    private fun checkIfClassMethod(): Boolean {
        return  parsingContext.insideClassMemberDeclaration
                && !parsingContext.insideFunctionDeclaration
    }

    override fun exitDeclaration(ctx: KotlinParser.DeclarationContext?) {
        if (ctx == null) return
        parsingContext.insideDeclaration = false
    }

    private fun parsePropertyDeclaration(propertyDeclaration: KotlinParser.PropertyDeclarationContext): AttributeDTO? {
        parsingContext.insideFieldDeclaration = true
        val parserTreeWalker = ParseTreeWalker()
        val functionListener = FieldListener()
        parserTreeWalker.walk(functionListener, propertyDeclaration)
        parsingContext.insideFieldDeclaration = false
        return functionListener.attributeDTO
    }

    private fun parseFunctionDeclaration(functionDeclaration: KotlinParser.FunctionDeclarationContext): MethodDTO? {
        parsingContext.insideFunctionDeclaration = true
        val parserTreeWalker = ParseTreeWalker()
        val functionListener = FunctionListener()
        parserTreeWalker.walk(functionListener, functionDeclaration)
        parsingContext.insideFunctionDeclaration = false
        return functionListener.methodDTO
    }

    fun getFileDTO(): FileDTO {
        return fileDTO
    }

    fun getClassesDTOs(): List<ClassDTO> {
        return parsingContext.classesDTOs
    }
}