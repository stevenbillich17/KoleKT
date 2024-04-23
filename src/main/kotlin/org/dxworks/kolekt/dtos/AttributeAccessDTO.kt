package org.dxworks.kolekt.dtos

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
class AttributeAccessDTO(val attributeName: String, val referenceName: String) {

    private var attributeClassFQN: String? = null

    @Transient
    private var attributeClassDTO: ClassDTO? = null

    override fun toString(): String {
        return "AttributeAccessDTO(attributeName='$attributeName', referenceName='$referenceName')"
    }

    fun setAttributeClassDTO(classDTO: ClassDTO) {
        this.attributeClassFQN = classDTO.getFQN()
        this.attributeClassDTO = classDTO
    }

    fun getAttributeClassDTO(): ClassDTO? {
        return attributeClassDTO
    }

    fun getAttributeClassFQN(): String? {
        return attributeClassFQN
    }
}