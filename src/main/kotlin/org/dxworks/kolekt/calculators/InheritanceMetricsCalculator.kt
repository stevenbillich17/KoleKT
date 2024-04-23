package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.dtos.ClassDTO

class InheritanceMetricsCalculator : MetricsCalculator {
    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        val noc = classDTO.getSubClassesFQNs().size
        if (setInClass) {
            classDTO.setNOC(noc)
        }
        return buildJsonObject {
            put("HIT", classDTO.getHIT())
            put("DIT", classDTO.getDIT())
            put("NOC", noc)
        }
    }

}