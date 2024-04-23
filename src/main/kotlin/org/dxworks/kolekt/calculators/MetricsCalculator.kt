package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import org.dxworks.kolekt.dtos.ClassDTO

interface MetricsCalculator {
    fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean) : JsonObject
}