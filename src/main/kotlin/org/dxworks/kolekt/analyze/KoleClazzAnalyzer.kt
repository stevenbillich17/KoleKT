package org.dxworks.kolekt.analyze

import kotlinx.serialization.json.*
import org.dxworks.kolekt.calculators.*
import org.dxworks.kolekt.details.FileController

object KoleClazzAnalyzer {
    private val metricsCalculators: List<MetricsCalculator> = listOf(
        NOMMetricCalculator(),
        NOPAMetricCalculator(),
        WMCMetricCalculator(),
        InheritanceMetricsCalculator(),
        CCMetricCalculator(),
        CMMetricCalculator(),
        NProtMMetricCalculator(),
        AMWMetricCalculator(),
        CINTMetricCalculator(),
        CDISPMetricCalculator(),
        BOvRMetricCalculator(),
    )

    fun analyze(classFQN: String, setInClass: Boolean = false): JsonObject {
        val classDTO = FileController.findClassInFiles(classFQN) ?: throw IllegalArgumentException("Class not found")

        val metrics = metricsCalculators.map { it.calculateMetrics(classDTO, setInClass) }

        val classNameJson = JsonPrimitive(classFQN)
        val finalResult: Map<String, JsonElement> = buildMap {
            put("class_fqn", classNameJson) // Add the class FQN to the final result
            metrics.forEach { it.forEach { key, value -> put(key, value) } }
        }

        return JsonObject(finalResult)
    }
}