package org.dxworks.kolekt.calculators.relations

import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.FileDTO
import org.dxworks.kolekt.dtos.MethodDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ReturnsCalculator {
    val logger: Logger = LoggerFactory.getLogger(ExternalCallsCalculator::class.java)

    fun computeReturns(sourceFileDTO: FileDTO, targetFileDTO: FileDTO): Int {
        logger.debug("Computing returns")
        var numberOfReturns = 0
        for (classDTO in sourceFileDTO.classes) {
            numberOfReturns += countNumberOfReturnsFromFunctions(classDTO.classMethods, targetFileDTO.classes)
        }
        numberOfReturns += countNumberOfReturnsFromFunctions(sourceFileDTO.functions, targetFileDTO.classes)
        logger.info("Number of returns from ${sourceFileDTO.getFileSavedName()} to ${targetFileDTO.getFileSavedName()}: $numberOfReturns")
        return numberOfReturns
    }

    private fun countNumberOfReturnsFromFunctions(classMethods: MutableList<MethodDTO>, classesFromTarget: MutableList<ClassDTO>): Int {
        var numberOfReturns = 0
        for (methodDTO in classMethods) {
            val methodReturnType = methodDTO.getMethodReturnType()
            for (classDTO in classesFromTarget) {
                if (classDTO.getFQN() == methodReturnType) {
                    numberOfReturns++
                    break
                }
            }
        }
        return numberOfReturns
    }
}