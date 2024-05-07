package org.dxworks.kolekt.analyze

import kotlinx.serialization.json.JsonObject
import org.dxworks.kolekt.calculators.relations.ExternalCallsCalculator
import org.dxworks.kolekt.calculators.relations.ReturnsCalculator
import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.FileDTO

class KoleAnalyzer {
    val externalCallsCalculator = ExternalCallsCalculator()
    val returnsCalculator = ReturnsCalculator()

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
        fullPath: Boolean // todo: not supported yet
    ) {
        var result: Int = 0

        val sourceFileDTO = FileController.getFile(sourceFile) ?: throw IllegalArgumentException("Source file not found")
        val targetFileDTO = FileController.getFile(targetFile) ?: throw IllegalArgumentException("Target file not found")

        result = when(metric) {
            "extCalls" -> externalCallsCalculator.computeExternalCalls(sourceFileDTO, targetFileDTO)
            "returns" -> returnsCalculator.computeReturns(sourceFileDTO, targetFileDTO)
            else -> throw IllegalArgumentException("Metric not supported")
        }
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
    ) {
        println("Analyzing")
        for (metric in metrics) {
            computeMetric(metric, sourceFile, targetFile, fullPath)
        }
    }
}