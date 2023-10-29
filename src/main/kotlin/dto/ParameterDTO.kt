package dto

data class ParameterDTO(val parameterName: String, val parameterType: String) {
    init {
        println("ParameterDTO created: {parameterName: $parameterName, parameterType: $parameterType}")
    }

    override fun toString(): String {
        return "{ParameterDTO(parameterName='$parameterName', parameterType='$parameterType'})"
    }
}