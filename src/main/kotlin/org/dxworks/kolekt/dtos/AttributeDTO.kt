package org.dxworks.kolekt.dtos

import org.dxworks.kolekt.enums.AttributeType

data class AttributeDTO(val name: String, val type: String, val attributeType: AttributeType) {

    private var isSetByMethodCall = false
    private var methodCallDTO: MethodCallDTO? = null
    fun setByMethodCall(methodCallDTO: MethodCallDTO) {
        this.methodCallDTO = methodCallDTO
        isSetByMethodCall = true
    }

    override fun toString(): String {
        var result = "AttributeDTO(name='$name', type='$type', attributeType='$attributeType'"
        if(isSetByMethodCall) {
           result = "$result, methodCallDTO=$methodCallDTO" 
        }
        result = "$result)"
        return result
    }
}