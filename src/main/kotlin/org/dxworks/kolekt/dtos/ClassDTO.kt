package org.dxworks.kolekt.dtos

import org.dxworks.kolekt.details.DictionariesController
import org.dxworks.kolekt.enums.Modifier
import org.slf4j.LoggerFactory
import java.util.*

class ClassDTO(internal val className: String? = null) {
    internal var classPackage: String? = null
    internal var superClass: String = ""

    internal val classMethods: MutableList<MethodDTO> = mutableListOf()
    internal val classFields: MutableList<AttributeDTO> = mutableListOf()
    internal val classAnnotations: MutableList<AnnotationDTO> = mutableListOf()
    internal val classModifiers: MutableList<Modifier> = mutableListOf()
    internal val classInterfaces: MutableList<String> = mutableListOf()

    internal val typesFoundInClass = mutableMapOf<String, ClassDTO>()

    private val logger = LoggerFactory.getLogger("ClassDTO@$className")

    override fun toString(): String {
        return "ClassDTO(\n" +
                " className='$className',\n" +
                " classPackage='$classPackage',\n" +
                " superClass='$superClass',\n" +
                " classInterfaces=(${buildClassInterfacesString()}),\n" +
                " classModifiers=(${buildClassModifiersString()}),\n" +
                " classAnnotations=$classAnnotations, \n" +
                " classMethods=$classMethods, \n" +
                " classFields=${buildClassFieldsString()}\n)}"
    }

    private fun buildClassInterfacesString(): String {
        var result = "\n    "
        classInterfaces.forEach { result += "$it\n    " }
        return result
    }

    fun addField(field: AttributeDTO?) {
        println("Adding fields: $field")
        field?.let {
            classFields.add(it)
        }
    }

    private fun buildClassModifiersString(): String {
        var result = "\n    "
        classModifiers.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildClassFieldsString(): String {
        var result = "(\n    "
        classFields.forEach { result += it.toString() + "\n    " }
        result += ")"
        return result
    }

    fun addModifier(modifierString: String) {
        try {
            val modifier = Modifier.valueOf(modifierString.uppercase(Locale.getDefault()))
            classModifiers.add(modifier)
        } catch (e: IllegalArgumentException) {
            logger.error("Modifier $modifierString not found")
        }
    }

    fun getFQN(): String {
        if (classPackage == null) return className ?: ""
        return "$classPackage.$className"
    }

    fun analyse(importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Analysing class $className")
        analyseClassFields(importsList, shouldReturnExternal)
        analyseClassMethods(importsList, shouldReturnExternal)
    }

    private fun analyseClassMethods(importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Analysing class methods")
        classMethods.forEach { method ->
            logger.trace("Analysing method ${method.methodName}")
            analyseMethodReturnType(method, importsList, shouldReturnExternal)
        }
    }

    private fun analyseMethodReturnType(method: MethodDTO, importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        var methodReturnType = method.getMethodReturnType()
        if (method.isBasicReturnType()) {
            logger.trace("Method ${method.methodName} has basic return type")
            method.setMethodReturnTypeClassDTO(DictionariesController.BASIC_CLASS)
        } else {
            if (!methodReturnType.contains(".")) {
                methodReturnType = discoverFromImportsFQN(methodReturnType, importsList)
                if (!methodReturnType.contains(".")) {
                    methodReturnType = "${classPackage}.$methodReturnType"
                }
            }
            val classDTO = DictionariesController.findClassAfterFQN(methodReturnType, shouldReturnExternal)
            method.setMethodReturnTypeClassDTO(classDTO)
        }
        logger.debug("MethodDTO: ${method.methodName} linked to type: ${method.getMethodReturnTypeClassDTO()?.getFQN()}")
    }

    private fun analyseClassFields(importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Analysing class fields")
        classFields.forEach { field ->
            logger.trace("Analysing field ${field.name} with type ${field.type}")
            field.type = searchTypeFQN(field, importsList)
            linkType(field, shouldReturnExternal)
            logger.debug("FieldDTO: ${field.name} linked to type: ${field.getClassDTO()?.getFQN()}")
        }
    }

    private fun linkType(field: AttributeDTO, shouldReturnExternal: Boolean) {
        if (field.isBasicType()) {
            field.setClassDTO(DictionariesController.BASIC_CLASS)
        } else {
            tryLinkingType(field, shouldReturnExternal)
        }
    }

    private fun searchTypeFQN(
        field: AttributeDTO,
        importsList: MutableList<String>
    ) = if (field.type == "" && field.isSetByMethodCall) {
        resolveMethodCallType(field, importsList)
    } else {
        discoverFromImportsFQN(field.type, importsList)
    }

    private fun resolveMethodCallType(field: AttributeDTO, importsList: MutableList<String>): String {
        logger.trace("Resolving method call type for field {}, methodCallDTO: {}", field.name, field.methodCallDTO)

        val methodCallDTO = field.methodCallDTO
        val methodCallReference = methodCallDTO?.referenceName

        val foundedType: String
        if (methodCallReference == null) {
            foundedType = searchTypesInsideClassOrImports(methodCallDTO!!, importsList)
        } else {
            val foundedField = findField(methodCallReference)
            foundedType = searchFieldTypeForMethodReturnType(foundedField, methodCallDTO)
        }
        logger.trace("Founded: $foundedType")
        return foundedType
    }

    private fun searchFieldTypeForMethodReturnType(referenceField: AttributeDTO?, methodCallDTO: MethodCallDTO): String {
        if (referenceField == null) {
            return ""
        }
        val referenceFieldType = referenceField.type
        logger.trace("Searching after field type: $referenceFieldType")
        val classDTO = DictionariesController.findClassAfterFQN(referenceFieldType, false)
        val methodDTO = classDTO.findMethodBasedOnMethodCall(methodCallDTO)
        val returnType = methodDTO?.getMethodReturnType()
        if (returnType != null && !returnType.contains(".")) {
            return "${classDTO.classPackage}.$returnType"
        }
        return returnType ?: ""
    }

    private fun findField(methodCallReference: String): AttributeDTO? {
        logger.trace("Try using method call reference:  $methodCallReference")
        for (field in classFields) {
            if (field.name == methodCallReference) {
                return field
            }
        }
        return null
    }

    private fun searchTypesInsideClassOrImports(
        methodCallDTO: MethodCallDTO,
        importsList: MutableList<String>
    ): String {
        // first search in class methods
        for (method in classMethods) {
            if (method.methodName == methodCallDTO.methodName && methodCallDTO.parameters.size == method.methodParameters.size) {
                // todo: should match the types also
                return method.getMethodReturnType()
            }
        }
        // todo: treat them better with classes
        for (imports in importsList) {
            if (imports.endsWith(methodCallDTO.methodName)) {
                return imports
            }
        }
        return ""
    }

    private fun tryLinkingType(field: AttributeDTO, shouldReturnExternal: Boolean) {
        logger.trace("Trying to link field ${field.name}, foundType: (${field.type}) ...")

        if (typesFoundInClass.contains(field.type)) {
            typesFoundInClass[field.type]?.let { field.setClassDTO(it) }
            logger.trace("Type ${field.type} found in class dict, easy peasy")
        } else {
            val searchedType: ClassDTO = DictionariesController.findClassAfterFQN(field.type, shouldReturnExternal)
            typesFoundInClass[field.type] = searchedType
            field.setClassDTO(searchedType)
            logger.trace("Searched big dictionary found: ${searchedType.getFQN()}")
        }
    }

    private fun discoverFromImportsFQN(type: String, importsList: List<String>): String {
        logger.trace("Search type ($type) in imports")

        if (type == "") return ""
        for (import in importsList) {
            if (import.endsWith(type)) {
                return import
            }
        }
        return type
    }

    private fun findMethodBasedOnMethodCall(methodCallDTO: MethodCallDTO): MethodDTO? {
        for (method in classMethods) {
            if (method.methodName == methodCallDTO.methodName && methodCallDTO.parameters.size == method.methodParameters.size) {
                return method
            }
        }
        return null
    }
}