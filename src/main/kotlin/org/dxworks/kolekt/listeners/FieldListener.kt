package org.dxworks.kolekt.listeners

import org.dxworks.kolekt.context.ParsingContext
import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.MethodCallDTO
import org.dxworks.kolekt.enums.AttributeType
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener
import org.slf4j.LoggerFactory

class FieldListener : KotlinParserBaseListener() {
    var attributeDTO: AttributeDTO? = null
    val parsingContext = ParsingContext()

    private var wasFieldNameSet = false

    private var wasFieldTypeSet = false
    private var wasMethodNameSet = false
    private var wasCallSuffix = false
    private var fieldName = ""

    private var fieldType = ""
    private var fieldValue = ""
    private var calledMethodName = ""
    private var methodCallDTO: MethodCallDTO? = null
    private val calledMethodParameters = mutableListOf<String>()
    private val modifiers: MutableList<String> = mutableListOf()

    private val logger = LoggerFactory.getLogger(FieldListener::class.java)
    override fun enterPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        if (ctx == null) return
        parsingContext.insidePropertyDeclaration = true
    }

    override fun enterModifier(ctx: KotlinParser.ModifierContext?) {
        if (ctx == null || parsingContext.shouldStop) return
        logger.trace("Modifier: ${ctx.text}")
        modifiers.add(ctx.text)
        parsingContext.insideModifier = true
    }

    override fun exitPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        if (ctx == null) return
        parsingContext.insidePropertyDeclaration = false

        attributeDTO = AttributeDTO(fieldName, fieldType, AttributeType.FIELD)
        attributeDTO!!.addAllModifiers(modifiers)
        methodCallDTO?.let {
            attributeDTO!!.setByMethodCall(it)
        }
        logger.debug("AttributeDTO: {}", attributeDTO)
    }

    override fun enterVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || !parsingContext.insidePropertyDeclaration) return
        parsingContext.insideVariableDeclaration = true

        ctx.COLON()?.let {
            parsingContext.containsColon = true
        }
    }

    override fun enterInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null) return
        parsingContext.insideInfixFunctionCall = true
        wasMethodNameSet = false
        logger.trace("Inside infix function call: ${ctx.text}")
    }

    override fun exitInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null) return
        parsingContext.insideInfixFunctionCall = false
    }

    override fun enterAdditiveExpression(ctx: KotlinParser.AdditiveExpressionContext?) {
        if (ctx == null) return
        parsingContext.insideAdditiveExpression = true
        logger.trace("Inside additive expression: ${ctx.text}")
    }

    override fun exitAdditiveExpression(ctx: KotlinParser.AdditiveExpressionContext?) {
        if (ctx == null) return
        parsingContext.insideAdditiveExpression = false
    }


    override fun enterPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
        if (ctx == null) return
        parsingContext.insidePrimaryExpression = true
        logger.trace("Inside primary expression: ${ctx.text}")
        if (!wasMethodNameSet && !parsingContext.insideCallSuffix) {
            calledMethodName = ctx.simpleIdentifier()?.text ?: "UNKNOWN"
            wasMethodNameSet = true
            logger.trace("CALLED method name: {}", calledMethodName)
        }
        if (parsingContext.insideInfixFunctionCall && parsingContext.insideAdditiveExpression && parsingContext.insidePostfixUnaryExpression && !parsingContext.insideCallSuffix) {
            parsingContext.referenceName = ctx.simpleIdentifier()?.text
            logger.trace("Reference name: {}", parsingContext.referenceName)
        }
    }

    override fun exitPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
        if (ctx == null) return
        parsingContext.insidePrimaryExpression = false
    }

    override fun enterPostfixUnaryExpression(ctx: KotlinParser.PostfixUnaryExpressionContext?) {
        if (ctx == null) return
        parsingContext.insidePostfixUnaryExpression = true
        logger.trace("Inside postfix unary expression: ${ctx.text}")
    }

    override fun exitPostfixUnaryExpression(ctx: KotlinParser.PostfixUnaryExpressionContext?) {
        if (ctx == null) return
        parsingContext.insidePostfixUnaryExpression = false
    }

    override fun enterPostfixUnarySuffix(ctx: KotlinParser.PostfixUnarySuffixContext?) {
        if (ctx == null) return
        parsingContext.insidePostFixUnarySuffix = true
        logger.trace("Inside postfix unary suffix: ${ctx.text}")
    }

    override fun exitPostfixUnarySuffix(ctx: KotlinParser.PostfixUnarySuffixContext?) {
        if (ctx == null) return
        parsingContext.insidePostFixUnarySuffix = false
    }

    override fun enterNavigationSuffix(ctx: KotlinParser.NavigationSuffixContext?) {
        if (ctx == null) return
        parsingContext.insideNavigationSuffix = true
        logger.trace("Inside navigation suffix: ${ctx.text}")
        if (parsingContext.insideInfixFunctionCall) {
            parsingContext.wasThereAnNavigationSuffix = true
            calledMethodName = ctx.simpleIdentifier().text
            wasMethodNameSet = true
        }
    }

    override fun exitNavigationSuffix(ctx: KotlinParser.NavigationSuffixContext?) {
        if (ctx == null) return
        parsingContext.insideNavigationSuffix = false
        logger.trace("Exiting navigation suffix: ${ctx.text}")
    }

    override fun enterCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null) return
        parsingContext.insideCallSuffix = true
    }


    override fun exitCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null) return
        parsingContext.insideCallSuffix = false
        wasCallSuffix = true

        methodCallDTO = MethodCallDTO(calledMethodName, calledMethodParameters)
        if (parsingContext.referenceName != null && parsingContext.referenceName != "" && parsingContext.wasThereAnNavigationSuffix) {
            methodCallDTO!!.addReference(parsingContext.referenceName!!)
        }
    }

    override fun exitVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || !parsingContext.insidePropertyDeclaration) return
        parsingContext.insideVariableDeclaration = false
    }

    override fun enterSimpleIdentifier(ctx: KotlinParser.SimpleIdentifierContext?) {
        if (ctx == null || !parsingContext.insidePropertyDeclaration) return
        parsingContext.insideSimpleIdentifier = true

        if (!wasFieldNameSet) {
            fieldName = ctx.text
            wasFieldNameSet = true
        } else if (parsingContext.containsColon && parsingContext.insideVariableDeclaration && !wasFieldTypeSet) {
            // after the information is not placed parsingContext.insideVariableDeclaration
            fieldType = ctx.text
            wasFieldTypeSet = true
        } else if (!wasMethodNameSet && parsingContext.insideInfixFunctionCall) {
            // is set by a method call
            calledMethodName = ctx.text ?: "UNKNOWN"
            wasMethodNameSet = true
            logger.trace("Called method name: {}", calledMethodName)
        }
    }

    override fun enterStringLiteral(ctx: KotlinParser.StringLiteralContext?) {
        if (ctx == null) return
        if (parsingContext.insideInfixFunctionCall && !wasFieldTypeSet && !parsingContext.insideVariableDeclaration && !parsingContext.containsColon) {
            fieldType = "String"
            wasFieldTypeSet = true
        }
    }

    override fun enterLiteralConstant(ctx: KotlinParser.LiteralConstantContext?) {
        if (ctx == null) return

        if (!parsingContext.containsColon) {
            fieldType = "unknown"
            if (parsingContext.insideInfixFunctionCall && !wasFieldTypeSet) {
                if (ctx.BinLiteral() != null) {
                    fieldType = "Bin"
                } else if (ctx.BooleanLiteral() != null) {
                    fieldType = "Boolean"
                } else if (ctx.CharacterLiteral() != null) {
                    fieldType = "Character"
                } else if (ctx.HexLiteral() != null) {
                    fieldType = "Hex"
                } else if (ctx.IntegerLiteral() != null) {
                    fieldType = "Integer"
                } else if (ctx.RealLiteral() != null) {
                    fieldType = "Double"
                } else if (ctx.NullLiteral() != null) {
                    fieldType = "null"
                } else if (ctx.UnsignedLiteral() != null) {
                    fieldType = "Unsigned"
                } else if (ctx.LongLiteral() != null) {
                    fieldType = "Long"
                }
            }
            logger.trace("Field type: {}", fieldType)
            wasFieldTypeSet = true
        }
    }

    override fun enterValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null) return
        parsingContext.insideValueArgument = true
        if (parsingContext.insideCallSuffix) {
            calledMethodParameters.add(ctx.text)
            logger.trace("Called method parameters: {}", calledMethodParameters)
        }
    }

    override fun exitValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null) return
        parsingContext.insideValueArgument = false
    }

}