package org.dxworks.kolekt.listeners

import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.MethodDTO
import org.dxworks.kolekt.enums.AttributeType
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener

class FunctionListener : KotlinParserBaseListener() {
    var methodDTO: MethodDTO? = null

    private var shouldStop = false
    private var insideFunctionBody = false
    private var insideDeclaration = false

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

    override fun enterAssignment(ctx: KotlinParser.AssignmentContext?) {
        println("Declaration: ${ctx?.text}")
    }

    override fun enterDeclaration(ctx: KotlinParser.DeclarationContext?) {
        if (ctx == null || shouldStop) return
        println("Declaration: ${ctx.text}")
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

//        println("Property: ${ctx.variableDeclaration().simpleIdentifier().text}")
//        println("Property.Type: ${ctx.variableDeclaration()?.type()?.text}")
    }

    override fun enterVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || shouldStop) return

        if (insideFunctionBody && insideDeclaration) {
            val foundAttribute = AttributeDTO(
                ctx.simpleIdentifier().text,
                ctx.type()?.text ?: tryToFindType(ctx.text),
                AttributeType.LOCAL_VARIABLE
            )
            methodDTO!!.methodLocalVariables.add(foundAttribute)
        }
    }

//    override fun exitVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
//        if (ctx == null || shouldStop) return
//
//        if (insideFunctionBody && insideDeclaration) {
//            val foundAttribute = AttributeDTO(
//                ctx.simpleIdentifier().text,
//                ctx.type()?.text ?: tryToFindType(ctx.text),
//                AttributeType.LOCAL_VARIABLE
//            )
//            methodDTO!!.methodLocalVariables.add(foundAttribute)
//        }
//    }

    private fun tryToFindType(text: String?): String {
        var foundedType = "UNKNOWN"
        currentDeclaration?.let {
            val foundExpression = currentDeclaration?.propertyDeclaration()?.expression()
            if (foundExpression != null) {
                foundedType = foundExpression.text
            }
        }
        println("Founded type: $foundedType")
        return foundedType
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext?) {
        //println("Expression: " + ctx?.text)
    }

//    override fun enterPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
//        println("Primary: ${ctx?.text}")
//    }
}