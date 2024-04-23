package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.dtos.ClassDTO

class WMCMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        var wmc = 0
        for (method in classDTO.classMethods) {
            wmc += method.getCyclomaticComplexity()
        }
        return buildJsonObject {
            put("WMC", wmc)
        }
    }
}