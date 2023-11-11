package org.dxworks.kolekt.dtos

import org.dxworks.kolekt.enums.AttributeType
import org.jetbrains.kotlin.spec.grammar.KotlinParser

data class AttributeDTO(val name: String, val type: String, val attributeType: AttributeType) {
    override fun toString(): String {
        return "{AttributeDTO(parameterName='$name', parameterType='$type', attributeType='$attributeType'})"
    }
}