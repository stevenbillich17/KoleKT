package org.dxworks.kolekt.extraction

import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.dxworks.kolekt.dtos.*
import org.dxworks.kolekt.enums.AttributeType
import org.dxworks.kolekt.listeners.FieldListener
import org.dxworks.kolekt.listeners.FunctionListener
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener

class FileExtractionListener(private val pathToFile: String, private val name: String) : KotlinParserBaseListener() {
    private val fileDTO = FileDTO(pathToFile, name)
    private val classesDTOs: MutableList<ClassDTO> = mutableListOf()

    private var insidePrimaryConstructor = false

    private var mutableListOfClassParameters = mutableListOf<AttributeDTO>()

    private var insideClassDeclaration: Boolean = false
    override fun enterKotlinFile(ctx: KotlinParser.KotlinFileContext?) {
        ctx?.let {
            fileDTO.filePackage = it.packageHeader().identifier().text
        }
    }

    override fun enterPrimaryConstructor(ctx: KotlinParser.PrimaryConstructorContext?) {
        if (ctx == null) {
            return
        }
        insidePrimaryConstructor = true
    }

    override fun exitPrimaryConstructor(ctx: KotlinParser.PrimaryConstructorContext?) {
        insidePrimaryConstructor = false
    }


    override fun enterClassParameter(ctx: KotlinParser.ClassParameterContext?) {
        if (ctx == null) {
            return
        }
        var name: String? = null
        var type: String? = null
        ctx.type()?.let {
            println("Class parameter type: ${it.text}")
        }
        ctx.simpleIdentifier()?.let {
            println("Class parameter name: ${it.text}")
        }
        if (name == null || type == null) {
            return
        }
        mutableListOfClassParameters.add(AttributeDTO(
            name,
            type,
            AttributeType.FIELD
        ))
    }

    override fun enterFunctionValueParameters(ctx: KotlinParser.FunctionValueParametersContext?) {
        if (ctx == null) {
            return
        }
        println("Function value parameters: ${ctx.text}")
    }

    override fun exitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        ctx?.let {
            if (fileDTO.filePackage == null) {
                fileDTO.filePackage = "UNKNOWN"
            }
            parseClassDeclaration(ctx)?.let { classDTO ->
                classesDTOs.add(classDTO)
            }
        }
        insideClassDeclaration = false
    }

    override fun enterImportHeader(ctx: KotlinParser.ImportHeaderContext?) {
        ctx?.let {
            fileDTO.addImport(it.identifier().text)
        }
    }

    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        if (ctx == null) {
            return
        }
        insideClassDeclaration = true
    }

    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if (ctx == null) {
            return
        }
        if (insideClassDeclaration) {
            return
        }

        parseFunctionDeclaration(ctx)?.let { methodDTO ->
            fileDTO.functions.add(methodDTO)
        }
    }

    private fun parseClassDeclaration(ctx: KotlinParser.ClassDeclarationContext): ClassDTO? {
        if (ctx.simpleIdentifier() == null) {
            return null
        }
        val classDTO = ClassDTO(ctx.simpleIdentifier().text)
        classDTO.classPackage = fileDTO.filePackage

        ctx.primaryConstructor()?.classParameters()?.classParameter()?.forEach() { classParameter ->
            run {
                val field = AttributeDTO(
                    classParameter.simpleIdentifier().text,
                    classParameter.type().text,
                    AttributeType.FIELD
                )
                classDTO.addField(field)
            }
        }
        ctx.classBody()?.let { classBody ->
            classBody.classMemberDeclarations().classMemberDeclaration().forEach { classMemberDeclaration ->
                run {
                    if (classMemberDeclaration.declaration() != null) {
                        parseClass(classMemberDeclaration.declaration(), classDTO)
                    }
                }
            }
        }

        return classDTO
    }

    private fun parseClass(declaration: KotlinParser.DeclarationContext, classDTO: ClassDTO) {
        if (declaration.functionDeclaration() != null) {
            parseFunctionDeclaration(declaration.functionDeclaration())?.let { methodDTO ->
                classDTO.classMethods.add(methodDTO)
            }
        } else if (declaration.propertyDeclaration() != null) {
            parsePropertyDeclaration(declaration.propertyDeclaration())?.let { attributeDTO ->
                classDTO.classFields.add(attributeDTO)
            }
        }
    }

    private fun parsePropertyDeclaration(propertyDeclaration: KotlinParser.PropertyDeclarationContext): AttributeDTO? {
        val parserTreeWalker = ParseTreeWalker()
        val functionListener = FieldListener()
        parserTreeWalker.walk(functionListener, propertyDeclaration)
        return functionListener.attributeDTO
    }

    private fun parseFunctionDeclaration(functionDeclaration: KotlinParser.FunctionDeclarationContext): MethodDTO? {
        val parserTreeWalker = ParseTreeWalker()
        val functionListener = FunctionListener()
        parserTreeWalker.walk(functionListener, functionDeclaration)
        return functionListener.methodDTO
    }

    fun getFileDTO(): FileDTO {
        return fileDTO
    }

    fun getClassesDTOs(): List<ClassDTO> {
        return classesDTOs
    }
}