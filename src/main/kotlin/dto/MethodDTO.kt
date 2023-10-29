package dto

data class MethodDTO(val methodName: String) {
    val methodParameters: MutableList<ParameterDTO> = mutableListOf()
    override fun toString(): String {
        return "\n  {MethodDTO(" +
                "methodName='$methodName'," +
                "methodParameters=$methodParameters)}"
    }
}