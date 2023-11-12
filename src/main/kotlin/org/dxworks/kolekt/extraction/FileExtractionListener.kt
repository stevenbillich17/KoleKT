package org.dxworks.kolekt.extraction

import org.dxworks.kolekt.enums.AttributeType
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.dxworks.kolekt.dtos.*
import org.dxworks.kolekt.listeners.FunctionListener
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener

class FileExtractionListener(private val pathToFile: String, private val name: String) : KotlinParserBaseListener() {
    private val fileDTO = FileDTO(pathToFile, name)
    private val classesDTOs: MutableList<ClassDTO> = mutableListOf()
    override fun enterKotlinFile(ctx: KotlinParser.KotlinFileContext?) {
        ctx?.let {
            fileDTO.filePackage = it.packageHeader().identifier().text
        }
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
    }

    private fun parseClassDeclaration(ctx: KotlinParser.ClassDeclarationContext): ClassDTO? {
        if (ctx.simpleIdentifier() == null) {
            return null
        }
        val classDTO = ClassDTO(ctx.simpleIdentifier().text)
        classDTO.classPackage = fileDTO.filePackage
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
        var propertyName: String? = null
        var propertyType: String? = null
        propertyDeclaration.variableDeclaration()?.let {
            it.simpleIdentifier()?.let { simpleIdentifier ->
                propertyName = simpleIdentifier.text
            }
            it.type()?.let { type ->
                propertyType = getPropertyType(type)
            }
        }
        return if (propertyName != null && propertyType != null) {
            AttributeDTO(propertyName!!, propertyType!!, AttributeType.FIELD)
        } else {
            null
        }
    }

    private fun getPropertyType(it: KotlinParser.TypeContext): String? {
        it.nullableType()?.let { nullableType ->
            return nullableType.typeReference()?.text
        }
        it.typeReference()?.let { typeReference ->
            return typeReference.text
        }
        return null
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