package org.dxworks.kolekt.dtos

data class MethodDTO(val methodName: String) {
    val methodParameters = mutableListOf<AttributeDTO>()
    val methodCalls = mutableListOf<MethodCallDTO>()
    val methodLocalVariables = mutableListOf<AttributeDTO>()
    override fun toString(): String {
        return "\n  {\n" +
                "   MethodDTO(\n" +
                "   methodName='$methodName',\n" +
                "   methodParameters=$methodParameters), \n" +
                "   methodLocalVariables=$methodLocalVariables), \n" +
                "   calls=$methodCalls)\n" +
                "   }"
    }
}