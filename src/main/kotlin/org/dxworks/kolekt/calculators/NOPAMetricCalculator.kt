package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.enums.Modifier

class NOPAMetricCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        var nopa = 0
        for (field in classDTO.classFields) {
            if (checkIfFieldIsPublic(field.attributeModifiers)) {
                nopa++
            }
        }
        return buildJsonObject {
            put("NOPA", nopa)
        }
    }

    private fun checkIfFieldIsPublic(attributeModifiers: MutableList<Modifier>): Boolean {
        val restrictingModifiers = listOf(Modifier.PRIVATE, Modifier.PROTECTED, Modifier.INTERNAL)
        return attributeModifiers.none { restrictingModifiers.contains(it) }
    }
}