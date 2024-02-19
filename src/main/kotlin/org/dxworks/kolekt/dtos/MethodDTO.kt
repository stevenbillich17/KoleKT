package org.dxworks.kolekt.dtos

import org.dxworks.kolekt.enums.Modifier
import java.util.*

data class MethodDTO(val methodName: String) {
    val methodParameters = mutableListOf<AttributeDTO>()
    val methodCalls = mutableListOf<MethodCallDTO>()
    val methodLocalVariables = mutableListOf<AttributeDTO>()
    val methodAnnotations = mutableListOf<AnnotationDTO>()
    val methodModifiers = mutableListOf<Modifier>()
    private var methodReturnType: String = "void"
    override fun toString(): String {
        return "\n  {\n" +
                "   MethodDTO(\n" +
                "   methodName='$methodName',\n" +
                "   methodReturnType='$methodReturnType',\n" +
                "   methodModifiers=(${buildMethodModifiersString()}),\n" +
                "   methodParameters=(${buildMethodParametersString()}), \n" +
                "   methodLocalVariables=(${buildMethodLocalVariablesString()}), \n" +
                "   calls=(${buildMethodCallsString()})\n" +
                "   annotations=(${buildMethodAnnotationsString()})\n" +
                "   }"
    }

    fun setMethodReturnType(methodReturnType: String) {
        this.methodReturnType = methodReturnType
    }

    private fun buildMethodParametersString(): String {
        var result = "\n    "
        methodParameters.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildMethodLocalVariablesString(): String {
        var result = "\n    "
        methodLocalVariables.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildMethodCallsString(): String {
        var result = "\n    "
        methodCalls.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildMethodAnnotationsString(): String {
        var result = "\n    "
        methodAnnotations.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildMethodModifiersString(): String {
        var result = "\n    "
        methodModifiers.forEach { result += it.toString() + "\n    " }
        return result
    }

    fun addAnnotation(annotation: AnnotationDTO) {
        methodAnnotations.add(annotation)
    }

    fun addModifier(modifierString: String) {
        try {
            val modifier = Modifier.valueOf(modifierString.uppercase(Locale.getDefault()))
            methodModifiers.add(modifier)
        } catch (e: IllegalArgumentException) {
            // Handle the case where the modifierString does not match any Modifier
        }
    }
}