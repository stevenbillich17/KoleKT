package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.dtos.ClassDTO

class NOMMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        val nom = classDTO.classMethods.size
        return buildJsonObject {
            put("NOM", nom)
        }
    }

}