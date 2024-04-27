package org.dxworks.kolekt.calculators.utils


import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.MethodCallDTO
import org.dxworks.kolekt.dtos.MethodDTO
import org.dxworks.kolekt.enums.Modifier

object CommonFunctions {

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
}