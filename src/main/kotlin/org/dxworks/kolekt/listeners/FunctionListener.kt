package org.dxworks.kolekt.listeners

import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.MethodCallDTO
import org.dxworks.kolekt.dtos.MethodDTO
import org.dxworks.kolekt.enums.AttributeType
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener

class FunctionListener : KotlinParserBaseListener() {
    var methodDTO: MethodDTO? = null
    private var shouldStop = false

    private var insideFunctionBody = false
    private var insidePropertyDeclaration = false
    private var insideDeclaration = false
    private var insideVariableDeclaration = false
    private var insideExpression = false
    private var insideInfixFunctionCall = false
    private var insidePrimaryExpression = false
    private var insideCallSuffix = false
    private var insideValueArgument = false
    private var nameAlreadySetForMethod = false
    private var wasThereAnCallSuffix = false

    private var wasThereAnInfixFunctionCall = false
    private var valueName = ""

    private var valueType = ""
    private var calledMethodName = ""
    private var calledMethodParameters = mutableListOf<String>()
    private var lastCallSufixMethodCall: MethodCallDTO? = null


    private var currentDeclaration: KotlinParser.DeclarationContext? = null
    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if ( ctx == null || shouldStop) {
            shouldStop = true
            return
        }

        methodDTO = MethodDTO(ctx.simpleIdentifier().text)
    }

    override fun enterFunctionValueParameter(ctx: KotlinParser.FunctionValueParameterContext?) {
        if (ctx == null || shouldStop) return

        val parameterFromCtx: KotlinParser.ParameterContext = ctx.parameter()
        val foundParameter = AttributeDTO(
            parameterFromCtx.simpleIdentifier().text,
            parameterFromCtx.type().text,
            AttributeType.PARAMETER
        )

        methodDTO!!.methodParameters.add(foundParameter)
    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        insideFunctionBody = true
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        insideFunctionBody = false
    }

    override fun enterDeclaration(ctx: KotlinParser.DeclarationContext?) {
        if (ctx == null || shouldStop) return
        insideDeclaration = true
        currentDeclaration = ctx
    }

    override fun exitDeclaration(ctx: KotlinParser.DeclarationContext?) {
        if (ctx == null || shouldStop) return
        if (currentDeclaration == ctx) {
            insideDeclaration = false
        }
    }

    override fun enterPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        if (ctx == null || shouldStop) return
        insidePropertyDeclaration = true
        wasThereAnCallSuffix = false
        wasThereAnInfixFunctionCall = false
        println("Property declaration: ${ctx.text}")
    }

    override fun exitPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        if (ctx == null || shouldStop) return
        insidePropertyDeclaration = false
        println("Exiting property declaration: ${ctx.text}")
        if (ctx.variableDeclaration() != null) {
            val tempType = ctx.variableDeclaration().type()?.text ?: valueType

            // we were inside a local variable declaration
            val foundAttribute = AttributeDTO(
                ctx.variableDeclaration().simpleIdentifier().text,
                tempType,
                AttributeType.LOCAL_VARIABLE
            )

            if (tempType == "null" && wasThereAnCallSuffix && wasThereAnInfixFunctionCall) {
                foundAttribute.setByMethodCall(lastCallSufixMethodCall!!)
            }

            methodDTO!!.methodLocalVariables.add(foundAttribute)
        }
    }

    override fun enterVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || shouldStop) return
        println("Variable declaration: ${ctx.text}")
        insideVariableDeclaration = true
    }

    override fun exitVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting variable declaration: ${ctx.text}")
        insideVariableDeclaration = false
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext?) {
        if (ctx == null || shouldStop) return
        println("Expression: ${ctx.text}")
        insideExpression = true
    }

    override fun exitExpression(ctx: KotlinParser.ExpressionContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting expression: ${ctx.text}")
        insideExpression = false
    }

    override fun enterInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null || shouldStop) return
        println("Infix function call: ${ctx.text}")
        insideInfixFunctionCall = true
        nameAlreadySetForMethod = false
        wasThereAnInfixFunctionCall = true
    }

    override fun exitInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting infix function call: ${ctx.text}")
        insideInfixFunctionCall = false
        nameAlreadySetForMethod = false
    }

    override fun enterPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
        if (ctx == null || shouldStop) return
        println("Primary expression: ${ctx.text}")
        insidePrimaryExpression = true
        if (insideFunctionBody && insideDeclaration) {
            // special case for string literals
            if (ctx.stringLiteral() != null) {
                valueType = "String"
            }
        }
        if (!nameAlreadySetForMethod && !insideCallSuffix) {
            calledMethodName = ctx.simpleIdentifier()?.text ?: "UNKNOWN"
            nameAlreadySetForMethod = true
        }
    }

    override fun exitPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
        println("Exiting primary expression: ${ctx?.text}")
        insidePrimaryExpression = false
    }

    override fun enterCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null || shouldStop) return
        println("Call suffix: ${ctx.text}")
        insideCallSuffix = true
        wasThereAnCallSuffix = true
    }

    override fun exitCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting call suffix: ${ctx.text}")
        insideCallSuffix = false

        // add new call
        lastCallSufixMethodCall = MethodCallDTO(calledMethodName, calledMethodParameters)
        methodDTO!!.methodCalls.add(lastCallSufixMethodCall!!)

        // reset parameters
        calledMethodName = ""
        calledMethodParameters = mutableListOf()

    }

    override fun enterValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null || shouldStop) return
        println("Value argument: ${ctx.text}")
        insideValueArgument = true
        if (insideCallSuffix) {
            calledMethodParameters.add(ctx.text)
        }
    }

    override fun exitValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting value argument: ${ctx.text}")
        insideValueArgument = false
    }

    override fun enterLiteralConstant(ctx: KotlinParser.LiteralConstantContext?) {
        if (ctx == null || shouldStop) return
        println("Literal constant: ${ctx.text}")
        valueType = "unknown"
        if (insideFunctionBody && insideDeclaration && insidePrimaryExpression) {
            if (ctx.BinLiteral() != null) {
                valueType = "Bin"
            } else if (ctx.BooleanLiteral() != null) {
                valueType = "Boolean"
            } else if (ctx.CharacterLiteral() != null) {
                valueType = "Character"
            } else if (ctx.HexLiteral() != null) {
                valueType = "Hex"
            } else if (ctx.IntegerLiteral() != null) {
                valueType = "Integer"
            } else if (ctx.RealLiteral() != null) {
                valueType = "Double"
            } else if (ctx.NullLiteral() != null) {
                valueType = "null"
            } else if (ctx.UnsignedLiteral() != null) {
                valueType = "Unsigned"
            } else if (ctx.LongLiteral() != null) {
                valueType = "Long"
            }
        }
    }
}