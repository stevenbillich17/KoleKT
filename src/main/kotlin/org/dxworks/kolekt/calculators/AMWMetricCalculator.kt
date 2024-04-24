package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.calculators.utils.CommonCalculator
import org.dxworks.kolekt.dtos.ClassDTO

class AMWMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        var amw : Double = 0.0
        val wmc = CommonCalculator.computeTotalCyclomaticComplexity(classDTO.classMethods)
        if (classDTO.classMethods.isNotEmpty()) {
            amw = wmc.toDouble() / classDTO.classMethods.size
        }
        return buildJsonObject {
            put("AMW", amw)
        }
    }
}