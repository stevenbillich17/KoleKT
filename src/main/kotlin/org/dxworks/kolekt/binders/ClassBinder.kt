package org.dxworks.kolekt.binders

import org.dxworks.kolekt.details.DictionariesController
import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.MethodCallDTO
import org.dxworks.kolekt.dtos.MethodDTO
import org.dxworks.kolekt.utils.ClassTypesUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ClassBinder(private val classDTO: ClassDTO) {

    val logger: Logger = LoggerFactory.getLogger("ClassBinder@${classDTO.className}")
    private var importsList: MutableList<String> = mutableListOf()
    private var shouldReturnExternal: Boolean = false

    fun bind(importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Binding class")
        this.importsList = importsList
        this.shouldReturnExternal = shouldReturnExternal
        bindClassFields()
        bindClassMethods()
        bindClassSuperClass()
        // todo: add binding for interfaces
    }

    private fun bindClassSuperClass() {
        logger.debug("Binding class super class")
        val superClass = classDTO.superClass
        if (superClass != "") {
            val superClassDTO = DictionariesController.findClassAfterFQN(superClass, true)
            if (superClassDTO != DictionariesController.EXTERNAL_CLASS) {
                classDTO.superClassDTO = superClassDTO
                superClassDTO.addSubClass(classDTO)
                logger.debug("Super class linked to type: ${classDTO.superClassDTO?.getFQN()}")
            }
        }
    }

    private fun bindClassMethods() {
        classDTO.classMethods.forEach { method ->
            logger.trace("Binding method ${method.methodName}")
            bindMethodParameters(method)
            bindMethodReturnType(method)
            bindMethodLocalVariables(method)
        }
    }

    private fun bindMethodLocalVariables(method: MethodDTO) {
        method.methodLocalVariables.forEach { variable ->
            logger.trace("Binding local variable ${variable.name} with type ${variable.type}")
            variable.type = searchTypeFQN(variable) // todo: should also search local variables
            linkType(variable)
            logger.debug("Local variable: ${variable.name} linked to type: ${variable.getClassDTO()?.getFQN()}")
        }
    }

    private fun bindMethodParameters(method: MethodDTO) {
        method.methodParameters.forEach { parameter ->
            logger.trace("Binding parameter ${parameter.name} with type ${parameter.type}")
            parameter.type = searchTypeFQN(parameter)
            linkType(parameter)
            logger.debug("ParameterDTO: ${parameter.name} linked to type: ${parameter.getClassDTO()?.getFQN()}")
        }
    }

    private fun bindMethodReturnType(method: MethodDTO) {
        var methodReturnType = method.getMethodReturnType()
        if (method.isBasicReturnType()) {
            method.setMethodReturnTypeClassDTO(DictionariesController.BASIC_CLASS)
        } else {
            if (!methodReturnType.contains(".")) {
                methodReturnType = searchImports(methodReturnType)
//                if (!methodReturnType.contains(".")) {
//                    methodReturnType = "${classDTO.classPackage}.$methodReturnType"
//                }
            }
            val classDTO = DictionariesController.findClassAfterFQN(methodReturnType, shouldReturnExternal)
            method.setMethodReturnTypeClassDTO(classDTO)
        }
        logger.debug("MethodDTO: ${method.methodName} return linked to type: ${method.getMethodReturnTypeClassDTO()?.getFQN()}")
    }

    private fun bindClassFields() {
        logger.debug("Binding class fields")
        classDTO.classFields.forEach { field ->
            logger.trace("Binding field ${field.name} with type ${field.type}")
            field.type = searchTypeFQN(field)
            linkType(field)
            logger.debug("FieldDTO: ${field.name} linked to type: ${field.getClassDTO()?.getFQN()}")
        }
    }

    private fun linkType(field: AttributeDTO) {
        if (field.isBasicType()) {
            field.setClassDTO(DictionariesController.BASIC_CLASS)
        } else {
            tryLinkingType(field)
        }
    }

    private fun searchTypeFQN(
        field: AttributeDTO,
    ) = if ((field.type == "null" || field.type == "") && field.isSetByMethodCall) {
        resolveMethodCallType(field)
    } else {
        searchImports(field.type)
    }

    private fun resolveMethodCallType(field: AttributeDTO): String {
        logger.trace("Resolving method call type for field {}, methodCallDTO: {}", field.name, field.methodCallDTO)
        val methodCallDTO = field.methodCallDTO
        val methodCallReference = methodCallDTO?.referenceName

        val foundedType: String
        if (methodCallReference == null) {
            foundedType = searchTypesInsideClassOrImports(methodCallDTO!!)
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
    ): String {
        // first search in class methods
        var returnType: String? = null
        if (methodCallDTO.methodName == "testReturn") {
            logger.trace("Found weight field")
        }
        for (method in classDTO.classMethods) {
            if (method.methodName == methodCallDTO.methodName && methodCallDTO.parameters.size == method.methodParameters.size) {
                // todo: should match the types also
                returnType = method.getMethodReturnType() // todo: problem see getWeight
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
        // searching if the method name is a constructor for other classes
        val classDTOs = DictionariesController.findClassesInSamePackage(classDTO.classPackage)
        for (cls in classDTOs) {
            for (method in cls.classMethods) { //todo: this are not constructors
                if (method.methodName == methodCallDTO.methodName && methodCallDTO.parameters.size == method.methodParameters.size) {
                    return cls.getFQN()
                }
            }
        }
        return ""
    }

    private fun tryLinkingType(field: AttributeDTO) {
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

    private fun searchImports(type: String): String {
        if (ClassTypesUtils.isBasicType(type)) {
            return type
        }

        logger.trace("Search type ($type) in imports")
        if (type == "") return ""
        for (import in importsList) {
            if (import.endsWith(type)) {
                return import
            }
        }
        return "${classDTO.classPackage}.$type"
    }
}