package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

import org.dxworks.kolekt.calculators.utils.CommonFunctions
import org.dxworks.kolekt.dtos.ClassDTO
class WOCMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        val numberOfPublicMethods = classDTO.classMethods.count { CommonFunctions.checkIfPublic(it.methodModifiers) }
        val numberOfConstructors = classDTO.getConstructors().size
        // todo: add property accesor methods (they should be counted as methods tho? )

        return buildJsonObject {
            put("WOC", numberOfPublicMethods + numberOfConstructors)
        }
    }
}