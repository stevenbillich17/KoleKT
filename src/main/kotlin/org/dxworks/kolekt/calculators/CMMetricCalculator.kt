package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.dtos.ClassDTO

class CMMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        val setOfMethods = mutableSetOf<String>()
        for (method in classDTO.classMethods) {
            setOfMethods.addAll(method.getMethodsThatCallThisMethod())
        }
        return buildJsonObject {
            put("CM", setOfMethods.size)
        }
    }

}