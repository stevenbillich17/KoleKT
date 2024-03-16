package org.dxworks.kolekt.dtos

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.dxworks.kolekt.enums.AttributeType
import org.dxworks.kolekt.enums.Modifier
import org.dxworks.kolekt.utils.ClassTypesUtils
import org.slf4j.LoggerFactory
import java.util.*

@Serializable
class AttributeDTO {
    constructor (name: String,  type: String, attributeType: AttributeType) {
        this.name = name.trim()
        this.type = type.trim()
        this.attributeType = attributeType
    }

    var name: String = ""
    var type: String = ""
    var attributeType: AttributeType = AttributeType.FIELD
    var isSetByMethodCall = false
    var methodCallDTO: MethodCallDTO? = null
    var attributeModifiers: MutableList<Modifier> = mutableListOf()

    private var classDTO : ClassDTO? = null

    @Transient
    private val logger = LoggerFactory.getLogger("AttributeDTO@$name")

    fun setByMethodCall(methodCallDTO: MethodCallDTO) {
        this.methodCallDTO = methodCallDTO
        isSetByMethodCall = true
    }

    override fun toString(): String {
        var result = "AttributeDTO(name='$name', type='$type', attributeType='$attributeType'"
        if(isSetByMethodCall) {
           result = "$result, methodCallDTO=$methodCallDTO" 
        }
        result = "$result, attributeModifiers=(${buildAttributeModifiersString()})"
        result = "$result)"
        return result
    }

    private fun buildAttributeModifiersString(): String {
        if (attributeModifiers.isEmpty()) return ""
        var result = ""
        attributeModifiers.forEach { result += it.toString() + ", " }
        return result
    }

    private fun addModifier(modifierString: String) {
        try {
            val modifier = Modifier.valueOf(modifierString.uppercase(Locale.getDefault()))
            attributeModifiers.add(modifier)
        } catch (e: IllegalArgumentException) {
            logger.error("Modifier $modifierString not found")
        }
    }

    fun addAllModifiers(modifiers: List<String>) {
        modifiers.forEach { addModifier(it) }
    }

    fun isBasicType() : Boolean {
        return ClassTypesUtils.isBasicType(type)
    }

    fun setClassDTO(classDTO: ClassDTO) {
        this.classDTO = classDTO
    }

    fun getClassDTO() : ClassDTO? {
        return classDTO
    }
}