package org.dxworks.kolekt.dtos

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.dxworks.kolekt.enums.Modifier
import org.dxworks.kolekt.utils.ClassTypesUtils
import org.slf4j.LoggerFactory
import java.util.*

@Serializable
data class MethodDTO(val methodName: String) {
    val methodParameters = mutableListOf<AttributeDTO>()
    val methodCalls = mutableListOf<MethodCallDTO>()
    val methodLocalVariables = mutableListOf<AttributeDTO>()
    val methodAnnotations = mutableListOf<AnnotationDTO>()
    val methodModifiers = mutableListOf<Modifier>()
    private var methodCyclomaticComplexity = 1
    private var isConstructor = false

    private var methodReturnType: String = "Void"

    private var classesThatCallThisMethod = mutableSetOf<String>()
    private var methodsThatCallThisMethod = mutableSetOf<String>()

    private var parentClassFQN: String? = null
    private var parentFileSavedName: String? = null


    @Transient
    private val logger = LoggerFactory.getLogger("ClassDTO@$methodName")


    override fun toString(): String {
        return "\n  {\n" +
                "   MethodDTO(\n" +
                "   methodName='$methodName',\n" +
                "   isConstructor='$isConstructor',\n" +
                "   cyclomaticComplexity='$methodCyclomaticComplexity',\n" +
                "   methodReturnType='$methodReturnType',\n" +
                "   methodModifiers=(${buildMethodModifiersString()}),\n" +
                "   methodParameters=(${buildMethodParametersString()}), \n" +
                "   methodLocalVariables=(${buildMethodLocalVariablesString()}), \n" +
                "   calls=(${buildMethodCallsString()})\n" +
                "   annotations=(${buildMethodAnnotationsString()})\n" +
                "   classesThatCallThisMethod=(${classesThatCallThisMethod}),\n" +
                "   }"
    }

    fun setParentClass(classDTO: ClassDTO) {
        this.parentClassFQN = classDTO.getFQN()
    }

    fun getParentClassFQN(): String? {
        return parentClassFQN
    }

    fun setParentFile(fileDTO: FileDTO) {
        this.parentFileSavedName = fileDTO.filePackage + "." + fileDTO.fileName
    }

    fun getParentFileSavedName(): String? {
        return parentFileSavedName
    }

    fun setMethodReturnType(methodReturnType: String) {
        this.methodReturnType = methodReturnType
    }

    fun getMethodReturnType(): String {
        return methodReturnType
    }

    fun setConstructor() {
        isConstructor = true
    }

    fun isConstructor(): Boolean {
        return isConstructor
    }

    private fun buildMethodParametersString(): String {
        var result = "\n    "
        methodParameters.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildMethodLocalVariablesString(): String {
        var result = "\n    "
        methodLocalVariables.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildMethodCallsString(): String {
        var result = "\n    "
        methodCalls.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildMethodAnnotationsString(): String {
        var result = "\n    "
        methodAnnotations.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildMethodModifiersString(): String {
        var result = "\n    "
        methodModifiers.forEach { result += it.toString() + "\n    " }
        return result
    }

    fun addAnnotation(annotation: AnnotationDTO) {
        methodAnnotations.add(annotation)
    }

    fun addModifier(modifierString: String) {
        try {
            val modifier = Modifier.valueOf(modifierString.uppercase(Locale.getDefault()))
            methodModifiers.add(modifier)
        } catch (e: IllegalArgumentException) {
            logger.error("Modifier $modifierString not found")
        }
    }

    fun increaseCyclomaticComplexity() {
        methodCyclomaticComplexity++
    }

    fun increaseCyclomaticComplexityByValue(value: Int) {
        methodCyclomaticComplexity += value
    }

    fun getCyclomaticComplexity() : Int {
        return methodCyclomaticComplexity
    }

    fun addClassThatCallsThisMethod(classFQN: String) {
        classesThatCallThisMethod.add(classFQN)
    }

    fun getClassesThatCallThisMethod(): Set<String> {
        return classesThatCallThisMethod
    }

    fun addMethodThatCallsThisMethod(methodFQN: String) {
        methodsThatCallThisMethod.add(methodFQN)
    }

    fun getMethodsThatCallThisMethod(): Set<String> {
        return methodsThatCallThisMethod
    }
}