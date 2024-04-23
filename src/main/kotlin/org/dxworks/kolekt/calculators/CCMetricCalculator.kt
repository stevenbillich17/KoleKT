package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.dtos.ClassDTO

class CCMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        val setOfClasses = mutableSetOf<String>()
        for (method in classDTO.classMethods) {
            setOfClasses.addAll(method.getClassesThatCallThisMethod())
        }
        return buildJsonObject {
            put("CC", setOfClasses.size)
        }
    }
}