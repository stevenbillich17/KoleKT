package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.calculators.utils.CommonFunctions
import org.dxworks.kolekt.dtos.ClassDTO

class CINTMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        val cint = CommonFunctions.computeNumberOfMethodCalls(classDTO.classMethods, classDTO.classFields)
        return buildJsonObject {
            put("CINT", cint)
        }
    }
}