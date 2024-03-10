package org.dxworks.kolekt.dtos

import org.dxworks.kolekt.details.DictionariesController
import org.dxworks.kolekt.enums.Modifier
import org.slf4j.LoggerFactory
import java.util.*

class ClassDTO(internal val className : String? = null) {
    internal var classPackage : String? = null
    internal var superClass : String = ""

    internal val classMethods : MutableList<MethodDTO> = mutableListOf()
    internal val classFields : MutableList<AttributeDTO> = mutableListOf()
    internal val classAnnotations : MutableList<AnnotationDTO> = mutableListOf()
    internal val classModifiers : MutableList<Modifier> = mutableListOf()
    internal val classInterfaces : MutableList<String> = mutableListOf()

    // Special types
    // ("org.dxworks.kolekt.notfound")
    // ("org.dxworks.kolekt.external")
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

    fun getFQN() : String {
        if (classPackage == null) return className ?: ""
        return "$classPackage.$className"
    }

    fun analyse(importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Analysing class $className")
        analyseClassFields(importsList, shouldReturnExternal)

        // todo: analyse class methods (their attribute dtos)
    }

    fun analyseClassFields(importsList: MutableList<String>, shouldReturnExternal: Boolean) {
        logger.debug("Analysing class fields")
        classFields.forEach { field ->
            if (!field.isBasicType()) {
                // we resolve just for non-basic types
                var resolvedType = resolveType(field.type, importsList)
                if (resolvedType == "" && field.isSetByMethodCall) {
                    resolvedType = resolveMethodCallType(field, importsList)
                    logger.trace("Found method return type for field set by method call: $resolvedType")
                    field.type = resolvedType
                    if (field.isBasicType()) {
                        logger.trace("Field type is basic type, setting resolvedType to empty string")
                        resolvedType = ""
                    }
                }
                tryLinkingType(resolvedType, field, shouldReturnExternal)
            }
        }
    }

    private fun resolveMethodCallType(field: AttributeDTO, importsList: MutableList<String>): String {
        logger.trace("Resolving method call type for field ${field.name}, methodCallDTO: ${field.methodCallDTO}")

        val methodCallDTO = field.methodCallDTO
        val methodCallReference = methodCallDTO?.referenceName

        if (methodCallReference == null) {
            return searchTypesInsideClassOrImports(methodCallDTO!!, importsList)
        } else {
            return ""
//            val referenceType = findReferenceType(methodCallReference)
//            return searchTypeForMethodReturnType(referenceType, methodCallDTO)
        }
    }

    private fun searchTypesInsideClassOrImports(methodCallDTO: MethodCallDTO, importsList: MutableList<String>): String {
        for (method in classMethods) {
            if (method.methodName == methodCallDTO.methodName && methodCallDTO.parameters.size == method.methodParameters.size) {
                // todo: should match the types also
                return method.getMethodReturnType()
            }
        }
        for (imports in importsList) {
            if (imports.endsWith(methodCallDTO.methodName)) {
                return imports
            }
        }
        return ""
    }

    private fun tryLinkingType(resolvedType: String, field: AttributeDTO, shouldReturnExternal: Boolean) {
        logger.trace("Trying to link field ${field.name}, resolveType: ($resolvedType) ...")

        if (typesFoundInClass.contains(resolvedType)) {
            typesFoundInClass[resolvedType]?.let { field.setClassDTO(it) }

            logger.trace("Type $resolvedType found in class, easy peasy")
        } else {
            val searchedType = DictionariesController.findClassAfterFQN(resolvedType, shouldReturnExternal)
            typesFoundInClass[resolvedType] = searchedType
            field.setClassDTO(searchedType)

            logger.trace("Searched big dictionary found: ${searchedType.getFQN()}")
        }

        logger.debug("FieldDTO: ${field.name} linked to type: ${field.getClassDTO()?.getFQN()}")
    }

    private fun resolveType(type: String, importsList: List<String>): String {
        logger.trace("Resolving type ($type)")

        if (type == "") return ""
        for (import in importsList) {
            if (import.endsWith(type)) {
                return import
            }
        }
        return type
    }
}