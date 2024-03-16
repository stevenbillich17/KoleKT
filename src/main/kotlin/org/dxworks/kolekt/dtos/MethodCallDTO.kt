package org.dxworks.kolekt.dtos

import kotlinx.serialization.Serializable

@Serializable
data class MethodCallDTO(val methodName: String, val parameters: List<String>) {
    var referenceName : String? = null
    fun addReference(referenceName: String) {
        this.referenceName = referenceName
    }

    override fun toString(): String {
        return "MethodCallDTO(methodName='$methodName', parameters=$parameters, referenceName=$referenceName)"
    }
}