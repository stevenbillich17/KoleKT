package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.calculators.utils.CommonFunctions
import org.dxworks.kolekt.dtos.ClassDTO

class CDISPMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        val cint = CommonFunctions.computeNumberOfMethodCalls(classDTO.classMethods, classDTO.classFields)
        val setOfClassesThatAreCalled = mutableSetOf<String>()

        for (method in classDTO.classMethods) {
            for (call in method.methodCalls) {
                if (call.getClassThatIsCalled() != null) {
                    setOfClassesThatAreCalled.add(call.getClassThatIsCalled()!!)
                }
            }
        }

        for (field in classDTO.classFields) {
            if (field.isSetByMethodCall && CommonFunctions.getCalledClass(field.methodCallDTO!!) != null) {
                setOfClassesThatAreCalled.add(field.methodCallDTO!!.getClassThatIsCalled()!!)
            }
        }

        return buildJsonObject {
            put("CDISP", setOfClassesThatAreCalled.size / cint.toDouble())
        }
    }
}