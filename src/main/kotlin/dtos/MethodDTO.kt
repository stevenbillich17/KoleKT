package dtos

data class MethodDTO(val methodName: String) {
    val methodParameters: MutableList<AttributeDTO> = mutableListOf()
    override fun toString(): String {
        return "\n  {MethodDTO(" +
                "methodName='$methodName'," +
                "methodParameters=$methodParameters)}"
    }
}