package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.calculators.utils.CommonCalculator
import org.dxworks.kolekt.dtos.ClassDTO

class WMCMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        val wmc = CommonCalculator.computeTotalCyclomaticComplexity(classDTO.classMethods)
        return buildJsonObject {
            put("WMC", wmc)
        }
    }
}