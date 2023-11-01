package org.dxworks.kolekt.dtos

import org.dxworks.kolekt.enums.AttributeType

data class AttributeDTO(val name: String, val type: String, val attributeType: AttributeType) {
    override fun toString(): String {
        return "{ParameterDTO(parameterName='$name', parameterType='$type', attributeType='$attributeType'})"
    }
}