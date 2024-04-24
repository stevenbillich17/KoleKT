package org.dxworks.kolekt.calculators.utils


import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.MethodDTO

object CommonCalculator {

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
}