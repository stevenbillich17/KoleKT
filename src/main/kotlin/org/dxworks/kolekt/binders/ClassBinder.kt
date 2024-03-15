package org.dxworks.kolekt.binders

import org.dxworks.kolekt.details.DictionariesController
import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.MethodCallDTO
import org.dxworks.kolekt.dtos.MethodDTO
import org.slf4j.LoggerFactory

class ClassBinder(val classDTO: ClassDTO) {

    val logger = LoggerFactory.getLogger("ClassBinder@${classDTO.className}")

    fun bind(importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Binding class")
        bindClassFields(importsList, shouldReturnExternal)
        bindClassMethods(importsList, shouldReturnExternal)
    }

    private fun bindClassMethods(importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Binding class methods")
        classDTO.classMethods.forEach { method ->
            logger.trace("Binding method ${method.methodName}")
            bindMethodParameters(method, importsList, shouldReturnExternal)
            bindMethodReturnType(method, importsList, shouldReturnExternal)
            bindMethodLocalVariables(method, importsList, shouldReturnExternal)
        }
    }

    private fun bindMethodLocalVariables(method: MethodDTO, importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Binding method local variables")
        method.methodLocalVariables.forEach { variable ->
            logger.trace("Binding local variable ${variable.name} with type ${variable.type}")
            variable.type = searchTypeFQN(variable, importsList)
            linkType(variable, shouldReturnExternal)
            logger.debug("Local variable: ${variable.name} linked to type: ${variable.getClassDTO()?.getFQN()}")
        }
    }

    private fun bindMethodParameters(method: MethodDTO, importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Binding method attributes")
        method.methodParameters.forEach { parameter ->
            logger.trace("Binding parameter ${parameter.name} with type ${parameter.type}")
            parameter.type = searchTypeFQN(parameter, importsList)
            linkType(parameter, shouldReturnExternal)
            logger.debug("ParameterDTO: ${parameter.name} linked to type: ${parameter.getClassDTO()?.getFQN()}")
        }
    }

    private fun bindMethodReturnType(method: MethodDTO, importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        var methodReturnType = method.getMethodReturnType()
        if (method.isBasicReturnType()) {
            logger.trace("Method ${method.methodName} has basic return type")
            method.setMethodReturnTypeClassDTO(DictionariesController.BASIC_CLASS)
        } else {
            if (!methodReturnType.contains(".")) {
                methodReturnType = discoverFromImportsFQN(methodReturnType, importsList)
                if (!methodReturnType.contains(".")) {
                    methodReturnType = "${classDTO.classPackage}.$methodReturnType"
                }
            }
            val classDTO = DictionariesController.findClassAfterFQN(methodReturnType, shouldReturnExternal)
            method.setMethodReturnTypeClassDTO(classDTO)
        }
        logger.debug("MethodDTO: ${method.methodName} return linked to type: ${method.getMethodReturnTypeClassDTO()?.getFQN()}")
    }

    private fun bindClassFields(importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Binding class fields")
        classDTO.classFields.forEach { field ->
            logger.trace("Binding field ${field.name} with type ${field.type}")
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
    ) = if ((field.type == "null" || field.type == "") && field.isSetByMethodCall) {
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
        for (field in classDTO.classFields) {
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
        var returnType: String? = null
        for (method in classDTO.classMethods) {
            if (method.methodName == methodCallDTO.methodName && methodCallDTO.parameters.size == method.methodParameters.size) {
                // todo: should match the types also
                returnType = method.getMethodReturnType()
            }
        }
        // todo: treat them better with classes
        for (imports in importsList) {
            if (imports.endsWith(methodCallDTO.methodName)) {
                return imports
            }
            if (returnType != null && imports.endsWith(returnType)) {
                return imports
            }
        }
        return ""
    }

    private fun tryLinkingType(field: AttributeDTO, shouldReturnExternal: Boolean) {
        logger.trace("Trying to link field ${field.name}, foundType: (${field.type}) ...")

        if (classDTO.typesFoundInClass.contains(field.type)) {
            classDTO.typesFoundInClass[field.type]?.let { field.setClassDTO(it) }
            logger.trace("Type ${field.type} found in class dict, easy peasy")
        } else {
            val searchedType: ClassDTO = DictionariesController.findClassAfterFQN(field.type, shouldReturnExternal)
            classDTO.typesFoundInClass[field.type] = searchedType
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
}