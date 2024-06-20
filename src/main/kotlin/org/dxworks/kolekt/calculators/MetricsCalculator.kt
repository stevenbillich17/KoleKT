package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import org.dxworks.kolekt.dtos.ClassDTO

interface MetricsCalculator {

    /**
     * Calculates the metrics for a class and returns them as a JsonObject
     */
    fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean) : JsonObject
}