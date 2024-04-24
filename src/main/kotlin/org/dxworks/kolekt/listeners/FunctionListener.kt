package org.dxworks.kolekt.listeners

import org.dxworks.kolekt.context.ParsingContext
import org.dxworks.kolekt.dtos.*
import org.dxworks.kolekt.enums.AttributeType
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FunctionListener : KotlinParserBaseListener() {
    var methodDTO: MethodDTO? = null
    var classDTO: ClassDTO? = null
    var fileDTO: FileDTO? = null
    val parsingContext = ParsingContext()
    private val logger: Logger = LoggerFactory.getLogger(FunctionListener::class.java)

    private var currentDeclaration: KotlinParser.DeclarationContext? = null
    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if ( ctx == null || parsingContext.shouldStop) {
            parsingContext.shouldStop = true
            return
        }
        logger.trace("Function declaration: ${ctx.simpleIdentifier().text}")
        methodDTO = MethodDTO(ctx.simpleIdentifier().text)
        if (classDTO != null) {
            methodDTO!!.setParentClassDTO(classDTO!!)
        }
        if (fileDTO != null) {
            methodDTO!!.setParentFileDTO(fileDTO!!)
        }
    }

    override fun enterAnnotation(ctx: KotlinParser.AnnotationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Annotation: ${ctx.text}")
        parsingContext.insideAnnotation = true
    }

    override fun exitAnnotation(ctx: KotlinParser.AnnotationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insideAnnotation = false
    }

    override fun enterSingleAnnotation(ctx: KotlinParser.SingleAnnotationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Single annotation: ${ctx.text}")
        parsingContext.insideSingleAnnotation = true
    }

    override fun exitSingleAnnotation(ctx: KotlinParser.SingleAnnotationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting single annotation: ${ctx.text}")
        parsingContext.insideSingleAnnotation = false
        // function annotation is outside the function body
        if (!parsingContext.insideFunctionBody) {
            val singleAnnotation = AnnotationDTO(parsingContext.annotationName)
            singleAnnotation.addAnnotationArguments(parsingContext.annotationArguments)
            parsingContext.annotationArguments.clear()
            logger.debug("Adding function annotation: {}", singleAnnotation)
            methodDTO!!.addAnnotation(singleAnnotation)
        }
    }

    override fun enterModifier(ctx: KotlinParser.ModifierContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Modifier: ${ctx.text}")
        methodDTO!!.addModifier(ctx.text)
        parsingContext.insideModifier = true
    }

    override fun exitModifier(ctx: KotlinParser.ModifierContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting modifier: ${ctx.text}")

        parsingContext.insideModifier = false
    }

    override fun enterConstructorInvocation(ctx: KotlinParser.ConstructorInvocationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Constructor invocation: ${ctx.text}")

        parsingContext.insideConstructorInvocation = true
    }

    override fun exitConstructorInvocation(ctx: KotlinParser.ConstructorInvocationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting constructor invocation: ${ctx.text}")

        parsingContext.insideConstructorInvocation = false
    }

    override fun enterUserType(ctx: KotlinParser.UserTypeContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("User type: ${ctx.text}")

        parsingContext.insideUserType = true
        if (parsingContext.insideSingleAnnotation) {
            parsingContext.annotationName = ctx.text
        }
    }

    override fun exitUserType(ctx: KotlinParser.UserTypeContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting user type: ${ctx.text}")

        parsingContext.insideUserType = false
    }

    override fun enterType(ctx: KotlinParser.TypeContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        if (!parsingContext.insideFunctionBody && !parsingContext.insideFunctionParameters) {
            methodDTO!!.setMethodReturnType(ctx.text)
        }
    }

    override fun enterFunctionValueParameters(ctx: KotlinParser.FunctionValueParametersContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insideFunctionParameters = true
    }

    override fun exitFunctionValueParameters(ctx: KotlinParser.FunctionValueParametersContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insideFunctionParameters = false
    }

    override fun enterFunctionValueParameter(ctx: KotlinParser.FunctionValueParameterContext?) {
        if (ctx == null || parsingContext.shouldStop) return

        val parameterFromCtx: KotlinParser.ParameterContext = ctx.parameter()
        val foundParameter = AttributeDTO(
            parameterFromCtx.simpleIdentifier().text,
            parameterFromCtx.type().text,
            AttributeType.PARAMETER
        )
        logger.debug("Adding parameter: {}", foundParameter)
        methodDTO!!.methodParameters.add(foundParameter)
    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        parsingContext.insideFunctionBody = true
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        parsingContext.insideFunctionBody = false
    }

    override fun enterDeclaration(ctx: KotlinParser.DeclarationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insideDeclaration = true
        currentDeclaration = ctx
    }

    override fun exitDeclaration(ctx: KotlinParser.DeclarationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        if (currentDeclaration == ctx) {
            parsingContext.insideDeclaration = false
        }
    }

    override fun enterJumpExpression(ctx: KotlinParser.JumpExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        if (ctx.RETURN() == null) {
            methodDTO!!.increaseCyclomaticComplexity()
        }
    }

    override fun enterIfExpression(ctx: KotlinParser.IfExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        methodDTO!!.increaseCyclomaticComplexity()
        if (ctx.text.contains("||") || ctx.text.contains("&&")) {
            methodDTO!!.increaseCyclomaticComplexityByValue(countOccurrences(ctx.text, "||"))
            methodDTO!!.increaseCyclomaticComplexityByValue(countOccurrences(ctx.text, "&&"))
        }
    }

    fun countOccurrences(text: String, toFind: String): Int {
        return text.windowed(toFind.length, 1, partialWindows = false)
            .count { it == toFind }
    }

    override fun enterWhenEntry(ctx: KotlinParser.WhenEntryContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        methodDTO!!.increaseCyclomaticComplexity()
    }

    override fun enterWhileStatement(ctx: KotlinParser.WhileStatementContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        methodDTO!!.increaseCyclomaticComplexity()
    }

    override fun enterDoWhileStatement(ctx: KotlinParser.DoWhileStatementContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        methodDTO!!.increaseCyclomaticComplexity()
    }

    override fun enterForStatement(ctx: KotlinParser.ForStatementContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        methodDTO!!.increaseCyclomaticComplexity()
    }

    override fun enterCatchBlock(ctx: KotlinParser.CatchBlockContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        methodDTO!!.increaseCyclomaticComplexity()
    }

    override fun enterPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insidePropertyDeclaration = true
        parsingContext.wasThereAnCallSuffix = false
        parsingContext.wasThereAnInfixFunctionCall = false
        logger.trace("Property declaration: ${ctx.text}")
    }

    override fun exitPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insidePropertyDeclaration = false
        logger.trace("Exiting property declaration: ${ctx.text}")

        // possible attribute access
        var attributeAccessDTO: AttributeAccessDTO? = null
        if (parsingContext.wasThereAnNavigationSuffix && !parsingContext.wasThereAnCallSuffix) {
            attributeAccessDTO = AttributeAccessDTO(parsingContext.accessedFieldName, parsingContext.referenceName!!)
            logger.debug("Adding attribute access: {}", attributeAccessDTO)
        }

        if (ctx.variableDeclaration() != null) {
            val tempType = ctx.variableDeclaration().type()?.text ?: parsingContext.valueType

            // we were context.inside a local variable declaration
            val foundAttribute = AttributeDTO(
                ctx.variableDeclaration().simpleIdentifier().text,
                tempType,
                AttributeType.LOCAL_VARIABLE
            )
            if ((tempType == "" || tempType == "null") && parsingContext.wasThereAnCallSuffix && parsingContext.wasThereAnInfixFunctionCall) {
                foundAttribute.setByMethodCall(parsingContext.lastCallSuffixMethodCall!!)
            } else if (attributeAccessDTO != null) {
                foundAttribute.setByAttributeAccess(attributeAccessDTO)
            }

            logger.debug("Adding local variable: {}", foundAttribute)

            methodDTO!!.methodLocalVariables.add(foundAttribute)
        }
    }

    override fun enterVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Variable declaration: ${ctx.text}")
        parsingContext.insideVariableDeclaration = true
    }

    override fun exitVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting variable declaration: ${ctx.text}")
        parsingContext.insideVariableDeclaration = false
    }

    override fun enterExpression(ctx: KotlinParser.ExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Expression: ${ctx.text}")
        parsingContext.insideExpression = true
    }

    override fun exitExpression(ctx: KotlinParser.ExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting expression: ${ctx.text}")
        parsingContext.insideExpression = false
    }

    override fun enterInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Infix function call: ${ctx.text}")
        parsingContext.insideInfixFunctionCall = true
        parsingContext.nameAlreadySetForMethod = false
        parsingContext.wasThereAnInfixFunctionCall = true
    }

    override fun exitInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting infix function call: ${ctx.text}")
        parsingContext.insideInfixFunctionCall = false
        parsingContext.nameAlreadySetForMethod = false
    }

    override fun enterPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Primary expression: ${ctx.text}")
        parsingContext.insidePrimaryExpression = true
        if (parsingContext.insideFunctionBody && parsingContext.insideDeclaration) {
            // special case for string literals
            if (ctx.stringLiteral() != null) {
                parsingContext.valueType = "String"
                logger.trace("String literal: ${ctx.text}")
            }
        }
        if (!parsingContext.nameAlreadySetForMethod && !parsingContext.insideCallSuffix) {
            parsingContext.calledMethodName = ctx.simpleIdentifier()?.text ?: "UNKNOWN"
            parsingContext.nameAlreadySetForMethod = true
            logger.trace("Method name: ${parsingContext.calledMethodName}")
        }
        if (parsingContext.insideInfixFunctionCall && parsingContext.insideAdditiveExpression && parsingContext.insidePostfixUnaryExpression && !parsingContext.insideCallSuffix) {
            parsingContext.referenceName = ctx.simpleIdentifier()?.text
            logger.trace("Reference name: ${parsingContext.referenceName}")
        }
    }

    override fun exitPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
        logger.trace("Exiting primary expression: ${ctx?.text}")
        parsingContext.insidePrimaryExpression = false
    }


    override fun enterAdditiveExpression(ctx: KotlinParser.AdditiveExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insideAdditiveExpression = true
    }

    override fun exitAdditiveExpression(ctx: KotlinParser.AdditiveExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insideAdditiveExpression = false
    }

    override fun enterMultiplicativeExpression(ctx: KotlinParser.MultiplicativeExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insideMultiplicativeExpression = true
    }

    override fun exitMultiplicativeExpression(ctx: KotlinParser.MultiplicativeExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insideMultiplicativeExpression = false
    }

    override fun enterAsExpression(ctx: KotlinParser.AsExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("As expression: ${ctx.text}")
        parsingContext.insideAsExpression = true
    }

    override fun exitAsExpression(ctx: KotlinParser.AsExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting as expression: ${ctx.text}")
        parsingContext.insideAsExpression = false
    }

    override fun enterPrefixUnaryExpression(ctx: KotlinParser.PrefixUnaryExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insidePrefixUnaryExpression = true
    }

    override fun exitPrefixUnaryExpression(ctx: KotlinParser.PrefixUnaryExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insidePrefixUnaryExpression = false
    }

    override fun enterPostfixUnaryExpression(ctx: KotlinParser.PostfixUnaryExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insidePostfixUnaryExpression = true
    }

    override fun exitPostfixUnaryExpression(ctx: KotlinParser.PostfixUnaryExpressionContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insidePostfixUnaryExpression = false
    }

    override fun enterPostfixUnarySuffix(ctx: KotlinParser.PostfixUnarySuffixContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insidePostFixUnarySuffix = true
    }

    override fun exitPostfixUnarySuffix(ctx: KotlinParser.PostfixUnarySuffixContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        parsingContext.insidePostFixUnarySuffix = false
    }

    override fun enterNavigationSuffix(ctx: KotlinParser.NavigationSuffixContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Navigation suffix: ${ctx.text}")
        parsingContext.insideNavigationSuffix = true
        parsingContext.wasThereAnNavigationSuffix = true
        if (parsingContext.insideInfixFunctionCall && parsingContext.insideAdditiveExpression && parsingContext.insidePostFixUnarySuffix && ctx.memberAccessOperator() != null) {
            parsingContext.calledMethodName = ctx.simpleIdentifier().text
            parsingContext.accessedFieldName = ctx.simpleIdentifier().text
            logger.trace("Method name or accessed field: ${parsingContext.calledMethodName}")
        }
    }

    override fun exitNavigationSuffix(ctx: KotlinParser.NavigationSuffixContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting navigation suffix: ${ctx.text}")
        parsingContext.insideNavigationSuffix = false
    }

    override fun enterCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Enter call suffix: ${ctx.text}")
        parsingContext.insideCallSuffix = true
        parsingContext.wasThereAnCallSuffix = true
    }

    override fun exitCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting call suffix: ${ctx.text}")
        parsingContext.insideCallSuffix = false

        // add new call
        if (!parsingContext.wasThereAnNavigationSuffix) {
            parsingContext.lastCallSuffixMethodCall = MethodCallDTO(parsingContext.calledMethodName, parsingContext.calledMethodParameters)
            methodDTO!!.methodCalls.add(parsingContext.lastCallSuffixMethodCall!!)
            logger.debug("Adding method call: {}", parsingContext.lastCallSuffixMethodCall)
        } else {
            // add new call to the last call
            parsingContext.lastCallSuffixMethodCall = MethodCallDTO(parsingContext.calledMethodName, parsingContext.calledMethodParameters)
            parsingContext. lastCallSuffixMethodCall!!.addReference(parsingContext.referenceName!!)
            methodDTO!!.methodCalls.add(parsingContext.lastCallSuffixMethodCall!!)
            logger.debug("Adding method call: {}", parsingContext.lastCallSuffixMethodCall)
        }
        // reset parameters
        parsingContext.calledMethodName = ""
        parsingContext.calledMethodParameters = mutableListOf()
        parsingContext.referenceName = ""
        parsingContext.wasThereAnNavigationSuffix = false

    }

    override fun enterValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Value argument: ${ctx.text}")
        parsingContext.insideValueArgument = true
        if (parsingContext.insideCallSuffix) {
            parsingContext.calledMethodParameters.add(ctx.text)
        }
        if (parsingContext.insideAnnotation) {
            parsingContext.annotationArguments.add(ctx.text)
        }
    }

    override fun exitValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Exiting value argument: ${ctx.text}")
        parsingContext.insideValueArgument = false
    }

    override fun enterLiteralConstant(ctx: KotlinParser.LiteralConstantContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Literal constant: ${ctx.text}")
        parsingContext.valueType = "unknown"
        if (parsingContext.insideFunctionBody && parsingContext.insideDeclaration && parsingContext.insidePrimaryExpression) {
            if (ctx.BinLiteral() != null) {
                parsingContext.valueType = "Bin"
            } else if (ctx.BooleanLiteral() != null) {
                parsingContext.valueType = "Boolean"
            } else if (ctx.CharacterLiteral() != null) {
                parsingContext.valueType = "Character"
            } else if (ctx.HexLiteral() != null) {
                parsingContext.valueType = "Hex"
            } else if (ctx.IntegerLiteral() != null) {
                parsingContext.valueType = "Integer"
            } else if (ctx.RealLiteral() != null) {
                parsingContext.valueType = "Double"
            } else if (ctx.NullLiteral() != null) {
                parsingContext.valueType = "null"
            } else if (ctx.UnsignedLiteral() != null) {
                parsingContext.valueType = "Unsigned"
            } else if (ctx.LongLiteral() != null) {
                parsingContext.valueType = "Long"
            }
            logger.trace("Value type: ${parsingContext.valueType}")
        }
    }
}