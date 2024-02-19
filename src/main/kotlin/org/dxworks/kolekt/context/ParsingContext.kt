package org.dxworks.kolekt.context

import org.dxworks.kolekt.dtos.AnnotationDTO
import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.MethodCallDTO

class ParsingContext {
    var shouldStop: Boolean = false
    var insideFunctionBody: Boolean = false
    var insidePropertyDeclaration: Boolean = false
    var insideDeclaration: Boolean = false
    var insideVariableDeclaration: Boolean = false
    var insideExpression: Boolean = false
    var insideInfixFunctionCall: Boolean = false
    var insidePrimaryExpression: Boolean = false
    var insideCallSuffix: Boolean = false
    var insideValueArgument: Boolean = false
    var insideAdditiveExpression: Boolean = false
    var insideMultiplicativeExpression: Boolean = false
    var insideAsExpression: Boolean = false
    var insidePrefixUnaryExpression: Boolean = false
    var insidePostfixUnaryExpression: Boolean = false
    var insidePostFixUnarySuffix: Boolean = false
    var insideNavigationSuffix: Boolean = false
    var insideFunctionParameters: Boolean = false
    var insideAnnotation: Boolean = false
    var insideSingleAnnotation: Boolean = false
    var insideUserType: Boolean = false
    var insideConstructorInvocation: Boolean = false
    var insideClassDeclaration: Boolean = false
    var insidePrimaryConstructor: Boolean = false
    var insideSimpleIdentifier: Boolean = false
    var containsColon: Boolean = false
    var insideClassMemberDeclaration: Boolean = false
    var insideFunctionDeclaration: Boolean = false
    var insideFieldDeclaration: Boolean = false
    var insideClassBody: Boolean = false

    var nameAlreadySetForMethod: Boolean = false
    var wasThereAnCallSuffix: Boolean = false
    var wasThereAnInfixFunctionCall: Boolean = false
    var wasThereAnNavigationSuffix: Boolean = false

    var valueName = ""
    var referenceName: String? = ""
    var valueType = ""
    var calledMethodName = ""
    var calledMethodParameters = mutableListOf<String>()
    var lastCallSuffixMethodCall: MethodCallDTO? = null
    var annotationArguments = mutableListOf<String>()
    var annotationName = ""

    var classDTO: ClassDTO? = null
    val classesDTOs: MutableList<ClassDTO> = mutableListOf()
    var mutableListOfClassParameters = mutableListOf<AttributeDTO>()
    var mutableListOfAnnotations = mutableListOf<AnnotationDTO>()
}