package org.dxworks.kolekt.listeners

import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.MethodCallDTO
import org.dxworks.kolekt.enums.AttributeType
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener

class FieldListener : KotlinParserBaseListener() {
    var attributeDTO: AttributeDTO? = null
    private var insidePropertyDeclaration = false
    private var insideSimpleIdentifier = false
    private var insideVariableDeclaration = false
    private var insideInfixFunctionCall = false
    private var insideCallSuffix = false
    private var insideValueArgument = false
    private var containsColon = false

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
    override fun enterPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        if (ctx == null) return
        insidePropertyDeclaration = true
    }

    override fun exitPropertyDeclaration(ctx: KotlinParser.PropertyDeclarationContext?) {
        if (ctx == null) return
        insidePropertyDeclaration = false

        attributeDTO = AttributeDTO(fieldName, fieldType, AttributeType.FIELD)
        methodCallDTO?.let {
            attributeDTO!!.setByMethodCall(it)
        }
        println(attributeDTO)

    }

    override fun enterVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || !insidePropertyDeclaration) return
        insideVariableDeclaration = true

        ctx.COLON()?.let {
            containsColon = true
        }
    }

    override fun enterInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null) return
        insideInfixFunctionCall = true
    }

    override fun exitInfixFunctionCall(ctx: KotlinParser.InfixFunctionCallContext?) {
        if (ctx == null) return
        insideInfixFunctionCall = false
    }


    override fun enterCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null) return
        insideCallSuffix = true
    }

    override fun exitCallSuffix(ctx: KotlinParser.CallSuffixContext?) {
        if (ctx == null) return
        insideCallSuffix = false
        wasCallSuffix = true

        methodCallDTO = MethodCallDTO(calledMethodName, calledMethodParameters)
    }

    override fun exitVariableDeclaration(ctx: KotlinParser.VariableDeclarationContext?) {
        if (ctx == null || !insidePropertyDeclaration) return
        insideVariableDeclaration = false
    }

    override fun enterSimpleIdentifier(ctx: KotlinParser.SimpleIdentifierContext?) {
        if (ctx == null || !insidePropertyDeclaration) return
        insideSimpleIdentifier = true

        if (!wasFieldNameSet) {
            fieldName = ctx.text
            wasFieldNameSet = true
        } else if (containsColon && insideVariableDeclaration && !wasFieldTypeSet) {
            // after the information is not placed insideVariableDeclaration
            fieldType = ctx.text
            wasFieldTypeSet = true
        } else if (!wasMethodNameSet && insideInfixFunctionCall) {
            // is set by a method call
            calledMethodName = ctx.text ?: "UNKNOWN"
            wasMethodNameSet = true
        }
    }

    override fun enterStringLiteral(ctx: KotlinParser.StringLiteralContext?) {
        if (ctx == null) return
        if (insideInfixFunctionCall && !wasFieldTypeSet && !insideVariableDeclaration && !containsColon) {
            fieldType = "String"
            wasFieldTypeSet = true
        }
    }

    override fun enterLiteralConstant(ctx: KotlinParser.LiteralConstantContext?) {
        if (ctx == null) return

        if (!containsColon) {
            fieldType = "unknown"
            if (insideInfixFunctionCall && !wasFieldTypeSet) {
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
            wasFieldTypeSet = true
        }
    }

    override fun enterValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null) return
        insideValueArgument = true
        if (insideCallSuffix) {
            calledMethodParameters.add(ctx.text)
        }
    }

    override fun exitValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null) return
        insideValueArgument = false
    }

}