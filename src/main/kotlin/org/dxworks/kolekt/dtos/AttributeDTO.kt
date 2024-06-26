package org.dxworks.kolekt.dtos

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.dxworks.kolekt.enums.AttributeType
import org.dxworks.kolekt.enums.CollectionType
import org.dxworks.kolekt.enums.Modifier
import org.dxworks.kolekt.utils.ClassTypesUtils
import org.slf4j.LoggerFactory
import java.util.*

@Serializable
class AttributeDTO {
    constructor (name: String,  type: String, attributeType: AttributeType) {
        this.name = name.trim()
        this.type = type.trim()
        if (this.type.contains("?")) {
            this.type = this.type.replace("?", "")
            isNullable = true
        }
        this.attributeType = attributeType
    }

    var name: String = ""
    var type: String = ""
    var isNullable: Boolean = false
    var attributeType: AttributeType = AttributeType.FIELD
    var isSetByMethodCall = false
    var methodCallDTO: MethodCallDTO? = null
    var isSetByAttributeAccess = false
    var attributeAccessDTO: AttributeAccessDTO? = null
    var attributeModifiers: MutableList<Modifier> = mutableListOf()
    var isCollection : Boolean = false
    var typeOfCollection: CollectionType? = null
    var collectionType: List<String>? = null

    private var classFQN: String? = null

    private var fileSavedName: String? = null


    @Transient
    private val logger = LoggerFactory.getLogger("AttributeDTO@$name")

    fun setByMethodCall(methodCallDTO: MethodCallDTO) {
        this.methodCallDTO = methodCallDTO
        isSetByMethodCall = true
    }

    fun setByAttributeAccess(attributeAccessDTO: AttributeAccessDTO) {
        this.attributeAccessDTO = attributeAccessDTO
        isSetByAttributeAccess = true
    }

    override fun toString(): String {
        var result = "AttributeDTO(name='$name', type='$type', attributeType='$attributeType', isCollection='$isCollection'"
        if (isCollection) {
            result = "$result, collectionType='$collectionType'"
        }
        if(isSetByMethodCall) {
           result = "$result, methodCallDTO=$methodCallDTO" 
        }
        if (isSetByAttributeAccess) {
            result = "$result, attributeAccessDTO=$attributeAccessDTO"
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

    fun addCollectionType(foundCollectionType: String) {
        isCollection = true
        if (typeOfCollection == null) {
            typeOfCollection = CollectionType.fromStringType(type)
        }
        if (collectionType == null) {
            collectionType = listOf()
        }
        collectionType = collectionType?.plus(foundCollectionType)
    }

    fun isCollectionType() : Boolean {
        return isCollection
    }

    fun addAllModifiers(modifiers: List<String>) {
        modifiers.forEach { addModifier(it) }
    }

    fun isBasicType() : Boolean {
        return ClassTypesUtils.isBasicType(type)
    }

    fun setClassDTO(classDTO: ClassDTO) {
        this.classFQN = classDTO.getFQN()
    }

    fun setFileDTO(fileDTO: FileDTO) {
        this.fileSavedName = fileDTO.filePackage + "." + fileDTO.fileName
    }

    fun getFileSavedName() : String? {
        return fileSavedName
    }

    fun getClassFQN() : String? {
        return classFQN
    }
}