package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.calculators.utils.CommonFunctions
import org.dxworks.kolekt.dtos.ClassDTO

class NOPAMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        var nopa = 0
        for (field in classDTO.classFields) {
            if (CommonFunctions.checkIfPublic(field.attributeModifiers)) {
                nopa++
            }
        }
        return buildJsonObject {
            put("NOPA", nopa)
        }
    }
}