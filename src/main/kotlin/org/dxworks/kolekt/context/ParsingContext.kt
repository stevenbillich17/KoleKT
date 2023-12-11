package org.dxworks.kolekt.context

import org.dxworks.kolekt.dtos.MethodCallDTO

class ParsingContext {
    var shouldStop = false
    var insideFunctionBody = false
    var insidePropertyDeclaration = false
    var insideDeclaration = false
    var insideVariableDeclaration = false
    var insideExpression = false
    var insideInfixFunctionCall = false
    var insidePrimaryExpression = false
    var insideCallSuffix = false
    var insideValueArgument = false
    var insideAdditiveExpression = false
    var insideMultiplicativeExpression = false
    var insideAsExpression = false
    var insidePrefixUnaryExpression = false
    var insidePostfixUnaryExpression = false
    var insidePostFixUnarySuffix = false
    var insideNavigationSuffix = false
    var insideFunctionParameters = false
    var insideAnnotation = false
    var insideSingleAnnotation = false
    var insideUserType = false
    var insideConstructorInvocation = false

    var nameAlreadySetForMethod = false
    var wasThereAnCallSuffix = false
    var wasThereAnInfixFunctionCall = false
    var wasThereAnNavigationSuffix = false

    var valueName = ""
    var referenceName: String? = ""
    var valueType = ""
    var calledMethodName = ""
    var calledMethodParameters = mutableListOf<String>()
    var lastCallSuffixMethodCall: MethodCallDTO? = null
    var annotationArguments = mutableListOf<String>()
    var annotationName = ""
}