package org.dxworks.kolekt.listeners

import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener

class MethodCallListener : KotlinParserBaseListener() {
    // This proves that we can use extra listeners to parse the tree again (SHOULD NOT OVERUSE THIS)

    var methodName: String? = null
    val methodArguments = mutableListOf<String>()
    private var enteredInArgumentList = false
    override fun enterPrimaryExpression(ctx: KotlinParser.PrimaryExpressionContext?) {
        ctx?.let {
            it.simpleIdentifier()?.let { simpleIdentifier ->
                if (methodName == null) {
                    methodName = simpleIdentifier.text
                } else {
                    if (enteredInArgumentList) {
                        methodArguments.add(simpleIdentifier.text)
                    }
                }
            }
        }
    }
    override fun enterValueArguments(ctx: KotlinParser.ValueArgumentsContext?) {
        enteredInArgumentList = true
    }

    // This should be equivalent to doing this
    /*
    println(expressionContext.disjunction().conjunction(0).equality(0).comparison(0).infixOperation(0)
            .elvisExpression(0).infixFunctionCall(0).rangeExpression(0).additiveExpression(0).multiplicativeExpression(0)
            .asExpression(0).prefixUnaryExpression().postfixUnaryExpression().postfixUnarySuffix(0).callSuffix().valueArguments())
            //primaryExpression().simpleIdentifier().text)
     */

}

fun bubu() : KotlinParser.KotlinFileContext? {
    return null
}