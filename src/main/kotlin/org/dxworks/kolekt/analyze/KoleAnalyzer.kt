package org.dxworks.kolekt.analyze

import org.dxworks.kolekt.dtos.FileDTO

class KoleAnalyzer {

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
        sourceFilePackage: String? = null,
        targetFile: String,
        targetFilePackage: String? = null,
        fullPath: Boolean
    ) {
        // todo: implement
    }

    /**
     * Computes a metric between two files
     * @param metrics the metrics to compute
     * @param sourceFile the source file
     * @param targetFile the target file
     * @param fullPath whether the files are specified by their full path or by their name
     * @param sourceFilePackage the package of the source file
     * @param targetFilePackage the package of the target file
     * @param fullPath whether the files are specified by their full path or by their name
     */
    fun computeMetric(
        metrics: List<String>, sourceFile: String,
        sourceFilePackage: String? = null,
        targetFile: String,
        targetFilePackage: String? = null,
        fullPath: Boolean
    ) {
        println("Analyzing")
    }

    fun computeExtCalls(sourceFileDTO: FileDTO, targetFileDTO: FileDTO) {
        println("Computing external calls...")

    }
}