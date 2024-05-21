package org.dxworks.kolekt.calculators.relations

import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.FileDTO
import org.dxworks.kolekt.dtos.MethodDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DeclarationsCalculator {
    val logger: Logger = LoggerFactory.getLogger(DeclarationsCalculator::class.java)
    var setOfTypesDefinedInTarget: Set<String> = emptySet()

    fun computeReturns(sourceFileDTO: FileDTO, targetFileDTO: FileDTO): Int {
        logger.debug("Computing declarations")
        setTypesFromTarget(targetFileDTO)
        var numberOfFields = 0
        for (classDTO in sourceFileDTO.classes) {
            numberOfFields += countNumberOfDeclarationsForClass(classDTO)
        }
        for (methodDTO in sourceFileDTO.functions) {
            numberOfFields += countNumberOfAttributesFromMethod(methodDTO)
        }
        logger.info("Number of declarations from ${sourceFileDTO.getFileSavedName()} to ${targetFileDTO.getFileSavedName()}: $numberOfFields")
        return numberOfFields
    }

    private fun countNumberOfDeclarationsForClass(classDTO: ClassDTO): Int {
        logger.trace("Calculating for class: ${classDTO.getFQN()}")
        var numberOfDeclarations = 0
        numberOfDeclarations += countNumberOfAttributes(classDTO.classFields)
        logger.trace("Number of declarations from fields: {}", numberOfDeclarations)
        for (methodDTO in classDTO.classMethods) {
            numberOfDeclarations += countNumberOfAttributesFromMethod(methodDTO)
        }
        return numberOfDeclarations
    }

    private fun countNumberOfAttributesFromMethod(methodDTO: MethodDTO): Int {
        var numberOfFields = 0
        numberOfFields += countNumberOfAttributes(methodDTO.methodParameters)
        numberOfFields += countNumberOfAttributes(methodDTO.methodLocalVariables)
        logger.trace("Number of declarations from method: {}", numberOfFields)
        return numberOfFields
    }

    private fun setTypesFromTarget(targetFileDTO: FileDTO) {
        logger.trace("Setting types from target")
        setOfTypesDefinedInTarget = targetFileDTO.classes.map { it.getFQN() }.toSet()
        logger.trace("Types from target set: {}", setOfTypesDefinedInTarget)
    }

    private fun countNumberOfAttributes(attributesList: MutableList<AttributeDTO>): Int {
        for (attributeDTO in attributesList) {
            val type = attributeDTO.type
            if (setOfTypesDefinedInTarget.contains(type)) {
                return 1
            }
        }
        return 0
    }

}