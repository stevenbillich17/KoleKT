package org.dxworks.kolekt.dtos

data class MethodCallDTO(val methodName: String, val parameters: List<String>) {
    private var referenceName : String? = null
    fun addReference(referenceName: String) {
        this.referenceName = referenceName
    }

    override fun toString(): String {
        return "MethodCallDTO(methodName='$methodName', parameters=$parameters, referenceName=$referenceName)"
    }
}