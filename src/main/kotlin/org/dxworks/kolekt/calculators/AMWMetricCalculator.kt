package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.enums.Modifier

class AMWMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        var amw : Double = 0.0
        var wmc : Int = 0
        for (method in classDTO.classMethods) {
            wmc += method.getCyclomaticComplexity()
        }
        if (classDTO.classMethods.isNotEmpty()) {
            amw = wmc.toDouble() / classDTO.classMethods.size
        }
        return buildJsonObject {
            put("AMW", amw)
        }
    }
}