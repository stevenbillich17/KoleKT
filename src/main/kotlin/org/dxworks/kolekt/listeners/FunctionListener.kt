package org.dxworks.kolekt.listeners

import org.dxworks.kolekt.dtos.AnnotationDTO
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
    private var insideAdditiveExpression = false
    private var insideMultiplicativeExpression = false
    private var insideAsExpression = false
    private var insidePrefixUnaryExpression = false
    private var insidePostfixUnaryExpression = false
    private var insidePostFixUnarySuffix = false
    private var insideNavigationSuffix = false
    private var insideFunctionParameters = false
    private var insideAnnotation = false
    private var insideSingleAnnotation = false
    private var insideUserType = false
    private var insideConstructorInvocation = false

    private var nameAlreadySetForMethod = false
    private var wasThereAnCallSuffix = false
    private var wasThereAnInfixFunctionCall = false
    private var wasThereAnNavigationSuffix = false

    private var valueName = ""
    private var referenceName: String? = ""
    private var valueType = ""
    private var calledMethodName = ""
    private var calledMethodParameters = mutableListOf<String>()
    private var lastCallSuffixMethodCall: MethodCallDTO? = null
    private var annotationArguments = mutableListOf<String>()
    private var annotationName = ""


    private var currentDeclaration: KotlinParser.DeclarationContext? = null
    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if ( ctx == null || shouldStop) {
            shouldStop = true
            return
        }
        methodDTO = MethodDTO(ctx.simpleIdentifier().text)
    }

    override fun enterAnnotation(ctx: KotlinParser.AnnotationContext?) {
        if (ctx == null || shouldStop) return
        print("Annotation: ${ctx.text}")
        insideAnnotation = true
    }

    override fun exitAnnotation(ctx: KotlinParser.AnnotationContext?) {
        if (ctx == null || shouldStop) return
        insideAnnotation = false
    }

    override fun enterSingleAnnotation(ctx: KotlinParser.SingleAnnotationContext?) {
        if (ctx == null || shouldStop) return
        println("Single annotation: ${ctx.text}")
        insideSingleAnnotation = true
    }

    override fun exitSingleAnnotation(ctx: KotlinParser.SingleAnnotationContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting single annotation: ${ctx.text}")
        insideSingleAnnotation = false
        // function annotation is outside the function body
        if (!insideFunctionBody) {
            val singleAnnotation = AnnotationDTO(annotationName)
            singleAnnotation.addAnnotationArguments(annotationArguments)
            methodDTO!!.addAnnotation(singleAnnotation)
        }
    }

    override fun enterConstructorInvocation(ctx: KotlinParser.ConstructorInvocationContext?) {
        if (ctx == null || shouldStop) return
        println("Constructor invocation: ${ctx.text}")

        insideConstructorInvocation = true
    }

    override fun exitConstructorInvocation(ctx: KotlinParser.ConstructorInvocationContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting constructor invocation: ${ctx.text}")

        insideConstructorInvocation = false
    }

    override fun enterUserType(ctx: KotlinParser.UserTypeContext?) {
        if (ctx == null || shouldStop) return
        println("User type: ${ctx.text}")

        insideUserType = true
        if (insideSingleAnnotation) {
            annotationName = ctx.text
        }
    }

    override fun exitUserType(ctx: KotlinParser.UserTypeContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting user type: ${ctx.text}")

        insideUserType = false
    }

    override fun enterType(ctx: KotlinParser.TypeContext?) {
        if (ctx == null || shouldStop) return
        if (!insideFunctionBody && !insideFunctionParameters) {
            methodDTO!!.setMethodReturnType(ctx.text)
        }
    }

    override fun enterFunctionValueParameters(ctx: KotlinParser.FunctionValueParametersContext?) {
        if (ctx == null || shouldStop) return
        insideFunctionParameters = true
    }

    override fun exitFunctionValueParameters(ctx: KotlinParser.FunctionValueParametersContext?) {
        if (ctx == null || shouldStop) return
        insideFunctionParameters = false
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
                foundAttribute.setByMethodCall(lastCallSuffixMethodCall!!)
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
        if (insideInfixFunctionCall && insideAdditiveExpression && insidePostfixUnaryExpression && !insideCallSuffix) {
            referenceName = ctx.simpleIdentifier()?.text
        }
    }

    override fun exitPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
        println("Exiting primary expression: ${ctx?.text}")
        insidePrimaryExpression = false
    }


    override fun enterAdditiveExpression(ctx: KotlinParser.AdditiveExpressionContext?) {
        if (ctx == null || shouldStop) return
        //println("Additive expression: ${ctx.text}")
        insideAdditiveExpression = true
    }

    override fun exitAdditiveExpression(ctx: KotlinParser.AdditiveExpressionContext?) {
        if (ctx == null || shouldStop) return
        //println("Exiting additive expression: ${ctx.text}")
        insideAdditiveExpression = false
    }

    override fun enterMultiplicativeExpression(ctx: KotlinParser.MultiplicativeExpressionContext?) {
        if (ctx == null || shouldStop) return
        //println("Multiplicative expression: ${ctx.text}")
        insideMultiplicativeExpression = true
    }

    override fun exitMultiplicativeExpression(ctx: KotlinParser.MultiplicativeExpressionContext?) {
        if (ctx == null || shouldStop) return
        //println("Exiting multiplicative expression: ${ctx.text}")
        insideMultiplicativeExpression = false
    }

    override fun enterAsExpression(ctx: KotlinParser.AsExpressionContext?) {
        if (ctx == null || shouldStop) return
        println("As expression: ${ctx.text}")
        insideAsExpression = true
    }

    override fun exitAsExpression(ctx: KotlinParser.AsExpressionContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting as expression: ${ctx.text}")
        insideAsExpression = false
    }

    override fun enterPrefixUnaryExpression(ctx: KotlinParser.PrefixUnaryExpressionContext?) {
        if (ctx == null || shouldStop) return
        //println("Prefix unary expression: ${ctx.text}")
        insidePrefixUnaryExpression = true
    }

    override fun exitPrefixUnaryExpression(ctx: KotlinParser.PrefixUnaryExpressionContext?) {
        if (ctx == null || shouldStop) return
        //println("Exiting prefix unary expression: ${ctx.text}")
        insidePrefixUnaryExpression = false
    }

    override fun enterPostfixUnaryExpression(ctx: KotlinParser.PostfixUnaryExpressionContext?) {
        if (ctx == null || shouldStop) return
        //println("Postfix unary expression: ${ctx.text}")
        insidePostfixUnaryExpression = true
    }

    override fun exitPostfixUnaryExpression(ctx: KotlinParser.PostfixUnaryExpressionContext?) {
        if (ctx == null || shouldStop) return
        //println("Exiting postfix unary expression: ${ctx.text}")
        insidePostfixUnaryExpression = false
    }

    override fun enterPostfixUnarySuffix(ctx: KotlinParser.PostfixUnarySuffixContext?) {
        if (ctx == null || shouldStop) return
        //println("Postfix unary suffix: ${ctx.text}")
        insidePostFixUnarySuffix = true
    }

    override fun exitPostfixUnarySuffix(ctx: KotlinParser.PostfixUnarySuffixContext?) {
        if (ctx == null || shouldStop) return
        //println("Exiting postfix unary suffix: ${ctx.text}")
        insidePostFixUnarySuffix = false
    }

    override fun enterNavigationSuffix(ctx: KotlinParser.NavigationSuffixContext?) {
        if (ctx == null || shouldStop) return
        println("Navigation suffix: ${ctx.text}")
        insideNavigationSuffix = true
        wasThereAnNavigationSuffix = true
        if (insideInfixFunctionCall && insideAdditiveExpression && insidePostFixUnarySuffix && ctx.memberAccessOperator() != null) {
            calledMethodName = ctx.simpleIdentifier().text
        }
    }

    override fun exitNavigationSuffix(ctx: KotlinParser.NavigationSuffixContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting navigation suffix: ${ctx.text}")
        insideNavigationSuffix = false
    }

    override fun enterCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null || shouldStop) return
        println("Enter call suffix: ${ctx.text}")
        insideCallSuffix = true
        wasThereAnCallSuffix = true
    }

    override fun exitCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null || shouldStop) return
        println("Exiting call suffix: ${ctx.text}")
        insideCallSuffix = false

        // add new call
        if (!wasThereAnNavigationSuffix) {
            lastCallSuffixMethodCall = MethodCallDTO(calledMethodName, calledMethodParameters)
            methodDTO!!.methodCalls.add(lastCallSuffixMethodCall!!)
        } else {
            // add new call to the last call
            lastCallSuffixMethodCall = MethodCallDTO(calledMethodName, calledMethodParameters)
            lastCallSuffixMethodCall!!.addReference(referenceName!!)
            methodDTO!!.methodCalls.add(lastCallSuffixMethodCall!!)
        }
        // reset parameters
        calledMethodName = ""
        calledMethodParameters = mutableListOf()
        referenceName = ""
        wasThereAnNavigationSuffix = false

    }

    override fun enterValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null || shouldStop) return
        println("Value argument: ${ctx.text}")
        insideValueArgument = true
        if (insideCallSuffix) {
            calledMethodParameters.add(ctx.text)
        }
        if (insideAnnotation) {
            annotationArguments.add(ctx.text)
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