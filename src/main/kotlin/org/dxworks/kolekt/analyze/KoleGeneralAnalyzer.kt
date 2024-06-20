package org.dxworks.kolekt.analyze

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.details.FileController

object KoleGeneralAnalyzer {

    fun getStatistics() : JsonObject {
        val results = mutableMapOf<String, Int>()
        val allFilesNameList = FileController.getNamesOfAllTheFiles()
        results["numberOfFiles"] = allFilesNameList.size
        var numberOfClasses = 0
        var numberOfFunctions = 0
        var numberOfFields = 0
        var setOfPackages = mutableSetOf<String>()
        for (file in allFilesNameList) {
            val fileDTO = FileController.getFile(file) ?: continue
            setOfPackages.add(fileDTO.filePackage ?: "No package")
            numberOfClasses += fileDTO.classes.size
            for (classDTO in fileDTO.classes) {
                numberOfFunctions += classDTO.classMethods.size
                numberOfFields += classDTO.classFields.size
            }
            numberOfFunctions += fileDTO.functions.size
        }
        results["numberOfClasses"] = numberOfClasses
        results["numberOfFunctions"] = numberOfFunctions
        results["numberOfFields"] = numberOfFields
        results["numberOfPackages"] = setOfPackages.size
        return buildJsonObject {
            results.forEach { (k, v) -> put(k, v) }
        }
    }
}