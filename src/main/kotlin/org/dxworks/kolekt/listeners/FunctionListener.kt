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
    private var insideDeclaration = false
    private var insideVariableDeclaration = false
    private var insideExpression = false
    private var insideInfixFunctionCall = false
    private var insidePrimaryExpression = false
    private var insideCallSuffix = false
    private var insideValueArgument = false
    private var nameAlreadySetForMethod = false

    private var valueName = ""
    private var valueType = ""
    private var calledMethodName = ""
    private var calledMethodParameters = mutableListOf<String>()


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
        //println("Enter function body:\n ${ctx?.text}\n")
        insideFunctionBody = true
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        //println("Exit function body:\n ${ctx?.text}\n")
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
    }

    override fun exitPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        if (ctx == null || shouldStop) return
        if (ctx.variableDeclaration() != null) {
            // we were inside a local variable declaration
            val foundAttribute = AttributeDTO(
                ctx.variableDeclaration().simpleIdentifier().text,
                ctx.variableDeclaration().type()?.text ?: valueType,
                AttributeType.LOCAL_VARIABLE
            )
            methodDTO!!.methodLocalVariables.add(foundAttribute)
            //println(foundAttribute)
        }
    }

    override fun enterVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || shouldStop) return
        insideVariableDeclaration = true
    }

    override fun exitVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || shouldStop) return
        insideVariableDeclaration = false
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext?) {
        if (ctx == null || shouldStop) return
        insideExpression = true
//        println("Expression: ${ctx.disjunction()?.conjunction()?.get(0)?.equality(0)?.comparison(0)?.infixOperation(0)?.elvisExpression(0)
//            ?.infixFunctionCall(0)?.rangeExpression(0)?.additiveExpression(0)?.multiplicativeExpression(0)
//            ?.asExpression(0)?.prefixUnaryExpression()?.postfixUnaryExpression()?.primaryExpression()?.text }")
    }

    override fun exitExpression(ctx: KotlinParser.ExpressionContext?) {
        if (ctx == null || shouldStop) return
        insideExpression = false
    }

    override fun enterInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null || shouldStop) return
        insideInfixFunctionCall = true
        nameAlreadySetForMethod = false
    }

    override fun exitInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null || shouldStop) return
        insideInfixFunctionCall = false
        nameAlreadySetForMethod = false
    }

    override fun enterPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
        if (ctx == null || shouldStop) return
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
        insidePrimaryExpression = false
    }

    override fun enterCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null || shouldStop) return
        insideCallSuffix = true
    }

    override fun exitCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null || shouldStop) return
        insideCallSuffix = false

        methodDTO!!.methodCalls.add(MethodCallDTO(calledMethodName, calledMethodParameters))
    }

    override fun enterValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null || shouldStop) return
        insideValueArgument = true
        if (insideCallSuffix) {
            calledMethodParameters.add(ctx.text)
        }
    }

    override fun exitValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null || shouldStop) return
        insideValueArgument = false
    }

//    override fun enterCallableReference(ctx: KotlinParser.CallableReferenceContext?) {
//        if (ctx == null || shouldStop) return
//        println("Callable reference: ${ctx.text}")
//    }

    override fun enterLiteralConstant(ctx: KotlinParser.LiteralConstantContext?) {
        if (ctx == null || shouldStop) return
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