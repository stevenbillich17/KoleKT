package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.enums.Modifier

class NProtMMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        var nprotm = 0
        for (field in classDTO.classFields) {
            if (field.attributeModifiers.contains(Modifier.PROTECTED)) {
                nprotm++
            }
        }
        for (method in classDTO.classMethods) {
            if (method.methodModifiers.contains(Modifier.PROTECTED)) {
                nprotm++
            }
        }
        return buildJsonObject {
            put("NProtM", nprotm)
        }
    }
}