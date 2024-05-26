package org.dxworks.kolekt.analyze

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.calculators.relations.*
import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.FileDTO

class KoleAnalyzer {
    val externalCallsCalculator = ExternalCallsCalculator()
    val returnsCalculator = ReturnsCalculator()
    val externalDataCalculator = ExternalDataCalculator()
    val externalDataStrictCalculator = ExternalDataStrictCalculator()
    val declarationsCalculator = DeclarationsCalculator()


    /**
     * Computes a metric between two files
     * @param metric the metric to compute
     * @param sourceFile the source file
     * @param targetFile the target file
     * @param fullPath whether the files are specified by their full path or by their name
     */
    fun computeMetric(
        metric: String,
        sourceFile: String,
        targetFile: String,
        fullPath: Boolean
    ) : Int {
        var result: Int = 0

        val sourceFileDTO = FileController.getFile(sourceFile) ?: throw IllegalArgumentException("Source file not found")
        val targetFileDTO = FileController.getFile(targetFile) ?: throw IllegalArgumentException("Target file not found")

        result = when(metric) {
            "extCalls" -> externalCallsCalculator.computeExternalCalls(sourceFileDTO, targetFileDTO)
            "returns" -> returnsCalculator.computeReturns(sourceFileDTO, targetFileDTO)
            "extData" -> externalDataCalculator.computeExternalData(sourceFileDTO, targetFileDTO)
            "extDataStrict" -> externalDataStrictCalculator.computeExternalData(sourceFileDTO, targetFileDTO)
            "declarations" -> declarationsCalculator.computeDeclarations(sourceFileDTO, targetFileDTO)
            else -> throw IllegalArgumentException("Metric not supported")
        }
        return result
    }

    /**
     * Computes multiple metrics between two files
     * @param metrics the metrics to compute
     * @param sourceFile the source file
     * @param targetFile the target file
     * @param fullPath whether the files are specified by their full path or by their name
     */
    fun computeMetric(
        metrics: List<String>,
        sourceFile: String,
        targetFile: String,
        fullPath: Boolean
    ) : JsonObject {
        println("Analyzing")
        val results = mutableMapOf<String, Int>()
        for (metric in metrics) {
            results[metric] = computeMetric(metric, sourceFile, targetFile, fullPath)
        }
        return buildJsonObject {
            put("sourceFile", sourceFile)
            put("targetFile", targetFile)
            results.forEach { (key, value) -> put(key, value) }
        }
    }
}