package org.dxworks.kolekt.codesmells

import org.dxworks.kolekt.details.FileController

class CodeSmellComputer {
private val detectors = listOf(
        GodClassDetector(),
        LazyClassDetector(),
        LargeClassDetector(),
        HighCyclomaticComplexityDetector(),
        LongParameterListDetector(),
        ShotgunSurgeryDetector(),
        LazyMethodDetector(),
    )

    fun computeAll() {
        val results = mutableMapOf<String, Int>()
        val allFilesNameList = FileController.getNamesOfAllTheFiles()
        for (file in allFilesNameList) {
            computeCodeSmells(file)
        }
    }

    fun computeCodeSmells(sourceFile: String) {
        val sourceFileDTO = FileController.getFile(sourceFile) ?: throw IllegalArgumentException("Source file not found")
        val classDTOs = sourceFileDTO.classes
        for (classDTO in classDTOs) {
            for (detector in detectors) {
                val codeSmell = detector.detect(classDTO)
                if (codeSmell != null) {
                    println(codeSmell)
                }
            }
        }
    }
}