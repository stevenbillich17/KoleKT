package org.dxworks.kolekt.dtos

data class MethodDTO(val methodName: String) {
    val methodParameters = mutableListOf<AttributeDTO>()
    val methodCalls = mutableListOf<MethodCallDTO>()
    val methodLocalVariables = mutableListOf<AttributeDTO>()
    private var methodReturnType: String = "void"
    override fun toString(): String {
        return "\n  {\n" +
                "   MethodDTO(\n" +
                "   methodName='$methodName',\n" +
                "   methodReturnType='$methodReturnType',\n" +
                "   methodParameters=(${buildMethodParametersString()}), \n" +
                "   methodLocalVariables=(${buildMethodLocalVariablesString()}), \n" +
                "   calls=(${buildMethodCallsString()})\n" +
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
}