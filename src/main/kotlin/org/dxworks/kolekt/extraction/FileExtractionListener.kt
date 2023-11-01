package org.dxworks.kolekt.extraction

import org.dxworks.kolekt.enums.AttributeType
import org.dxworks.kolekt.listeners.MethodCallListener
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.dxworks.kolekt.dtos.*
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
                println(classDTO)
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
        var methodDTO: MethodDTO? = null
        functionDeclaration.simpleIdentifier()?.let {
            methodDTO = MethodDTO(it.text)
        }

        methodDTO?.let {
            parseAllFunctionParameters(functionDeclaration, methodDTO!!)
            parseFunctionBody(functionDeclaration, methodDTO!!)
        }
        return methodDTO
    }

    private fun parseFunctionBody(functionDeclaration: KotlinParser.FunctionDeclarationContext, methodDTO: MethodDTO) {
        functionDeclaration.functionBody()?.block()?.statements()?.statement()?.forEach { statementContext ->
            statementContext?.let {
                it.expression()?.let { expressionContext ->
                    parseExpression(expressionContext)?.let { methodCallDTO ->
                        methodDTO.methodCalls.add(methodCallDTO)
                    }
                }
                it.declaration()?.let { declarationContext ->
                    parseDeclaration(declarationContext)
                }
                it.assignment()?.let { assignmentContext ->
                    println(assignmentContext.text)
                }
            }
        }
    }

    private fun parseDeclaration(declarationContext: KotlinParser.DeclarationContext) {

    }

    private fun parseExpression(expressionContext: KotlinParser.ExpressionContext): MethodCallDTO? {
        val parserTreeWalker = ParseTreeWalker()
        val methodCallListener = MethodCallListener()
        parserTreeWalker.walk(methodCallListener, expressionContext)

        val name = methodCallListener.methodName
        val arguments = methodCallListener.methodArguments

        return if (name != null) {
            MethodCallDTO(name, arguments)
        } else
            null

    }

    private fun parseAllFunctionParameters(
        functionDeclaration: KotlinParser.FunctionDeclarationContext,
        methodDTO: MethodDTO
    ) {
        functionDeclaration.functionValueParameters()?.functionValueParameter()
            ?.forEach() { functionValueParameter ->
                run {
                    parseFunctionParameter(functionValueParameter)?.let { parameter ->
                        methodDTO.methodParameters.add(parameter)
                    }
                }
            }
    }

    private fun parseFunctionParameter(functionValueParameter: KotlinParser.FunctionValueParameterContext?): AttributeDTO? {
        var parameterName: String? = null
        var parameterType: String? = null

        functionValueParameter?.let {
            it.parameter()?.simpleIdentifier()?.let { parameterNameIdentifier ->
                parameterName = parameterNameIdentifier.text
            }
            it.parameter().type()?.let { typeReference ->
                parameterType = typeReference.text
            }
        }
        return if (parameterName != null && parameterType != null) {
            AttributeDTO(parameterName!!, parameterType!!, AttributeType.PARAMETER)
        } else {
            null
        }
    }

    fun getFileDTO(): Any {
        return fileDTO
    }
}

fun bubu(): KotlinParser.KotlinFileContext? {
    return null
}