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
        WOCMetricCalculator(),
    )

    /**
     * Analyzes a class and returns a JSON object with the metrics
     * @param classFQN the fully qualified name of the class to analyze
     * @param setInClass whether to set the metrics in the class or not
     * @return a JSON object with the metrics
     * @throws IllegalArgumentException if the class is not found
     * Supported 14 metrics:
     * NOM, NOPA, WMC, CC, CM, NProtM, AMW, CINT, CDISP, BOvR, WOC, DIT, HIT, NOC
     */
    fun analyze(classFQN: String, setInClass: Boolean = false): JsonObject {
        val classDTO = FileController.findClassInFiles(classFQN) ?: throw IllegalArgumentException("Class not found")
        val metrics = metricsCalculators.map { it.calculateMetrics(classDTO, setInClass) }
        val classNameJson = JsonPrimitive(classFQN)
        val finalResult: Map<String, JsonElement> = buildMap {
            put("class_fqn", classNameJson)
            metrics.forEach { it.forEach { key, value -> put(key, value) } }
        }
        return JsonObject(finalResult)
    }
}