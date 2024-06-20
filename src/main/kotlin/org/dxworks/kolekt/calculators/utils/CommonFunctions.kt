package org.dxworks.kolekt.calculators.utils


import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.MethodCallDTO
import org.dxworks.kolekt.dtos.MethodDTO
import org.dxworks.kolekt.enums.Modifier
import org.slf4j.LoggerFactory

object CommonFunctions {
    val logger = LoggerFactory.getLogger(CommonFunctions::class.java)

    fun computeTotalCyclomaticComplexity(methods: List<MethodDTO>): Int {
        var totalCyclomaticComplexity = 0
        for (m in methods) {
            totalCyclomaticComplexity += m.getCyclomaticComplexity()
        }
        return totalCyclomaticComplexity
    }

    fun computeNumberOfMethodCalls(methods: List<MethodDTO>, attributes: List<AttributeDTO>): Int {
        var totalMethodCalls = 0
        for (m in methods) {
            totalMethodCalls += m.methodCalls.size
        }
        for (a in attributes) {
            if (a.isSetByMethodCall) {
                totalMethodCalls++
            }
        }
        return totalMethodCalls
    }

    fun checkIfPublic(attributeModifiers: MutableList<Modifier>): Boolean {
        val restrictingModifiers = listOf(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.INTERNAL)
        return attributeModifiers.none { restrictingModifiers.contains(it) }
    }

    fun getCalledClass(methodCallDTO: MethodCallDTO): ClassDTO? {
        if (methodCallDTO.getClassThatIsCalled() == null) {
            return null
        }
        return FileController.findClassInFiles(methodCallDTO.getClassThatIsCalled()!!)
    }

    fun isTheFieldProtected(attributeClass: String?, attributeName: String): Boolean {
        if (attributeClass == null) {
            return false
        }
        try {
            val classDTO = FileController.getClass(attributeClass)
            if (classDTO == null) {
                return false
            }
            val classField = classDTO.classFields.find { it.name == attributeName }
            if (classField == null) {
                return false
            }
            return classField.attributeModifiers.contains(Modifier.PROTECTED)
        } catch (e: Exception) {
            logger.error("Error while checking if the field is protected", e)
        }
        return false
    }

    fun checkTheMethodCallToBeGetter(methodCall: MethodCallDTO): Boolean {
        val classThatIsCalled = methodCall.getClassThatIsCalled()
        if (classThatIsCalled == null) {
            return false
        }
        val calledClassDTO = FileController.getClass(classThatIsCalled)
        if (calledClassDTO == null) {
            logger.error("Class $classThatIsCalled not found")
            return false
        }
        val calledMethod = calledClassDTO.classMethods.find { it.methodName == methodCall.methodName }
        if (calledMethod == null) {
            logger.error("Method ${methodCall.methodName} not found in class $classThatIsCalled")
            return false
        }
        if (checkMethodToBeGetter(calledMethod)) {
            return true
        }
        return false
    }

    fun checkMethodToBeGetter(calledMethod: MethodDTO): Boolean {
        val notPublic = calledMethod.methodModifiers.contains(Modifier.PROTECTED) ||
                calledMethod.methodModifiers.contains(Modifier.PRIVATE)
        if (notPublic) {
            return false
        }
        return calledMethod.methodParameters.isEmpty() && calledMethod.methodLocalVariables.isEmpty() && calledMethod.methodCalls.isEmpty()
                && calledMethod.methodName.startsWith("get") && calledMethod.getCyclomaticComplexity() == 1
    }
}