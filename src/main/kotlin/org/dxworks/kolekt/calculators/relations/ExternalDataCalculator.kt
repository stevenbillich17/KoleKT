package org.dxworks.kolekt.calculators.relations

import org.dxworks.kolekt.calculators.utils.CommonFunctions
import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.*
import org.dxworks.kolekt.enums.Modifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ExternalDataCalculator {
    val logger: Logger = LoggerFactory.getLogger(ExternalDataCalculator::class.java)
    val classesFromTarget = mutableListOf<String>()

    fun computeExternalData(sourceFileDTO: FileDTO, targetFileDTO: FileDTO): Int {
        logger.debug("Computing external data")
        var numberOfDataAccessed = 0
        addAllClassesFromTarget(targetFileDTO)
        for (classDTO in sourceFileDTO.classes) {
            numberOfDataAccessed += countNumberOfAccessesToFile(classDTO.classFields)
            numberOfDataAccessed += countNumberOfDataAccessedFromFunctions(classDTO.classMethods)
        }
        numberOfDataAccessed += countNumberOfDataAccessedFromFunctions(
            sourceFileDTO.functions
        )
        logger.info("Number of data accessed from ${sourceFileDTO.getFileSavedName()} to ${targetFileDTO.getFileSavedName()}: $numberOfDataAccessed")
        return numberOfDataAccessed
    }

    private fun addAllClassesFromTarget(targetFileDTO: FileDTO) {
        for (classDTO in targetFileDTO.classes) {
            classesFromTarget.add(classDTO.getFQN())
        }
        logger.trace("Added ${classesFromTarget.size} classes from target file")
    }

    private fun countNumberOfDataAccessedFromFunctions(
        classMethods: MutableList<MethodDTO>
    ): Int {
        var numberOfDataAccessed = 0
        for (methodDTO in classMethods) {
            numberOfDataAccessed += countNumberOfAccessesToFile(methodDTO.methodLocalVariables)
            logger.trace("From method ${methodDTO.methodName} accessed $numberOfDataAccessed variables")
        }
        return numberOfDataAccessed
    }

    private fun countNumberOfAccessesToFile(
        attributes: MutableList<AttributeDTO>
    ): Int {
        var numberOfAccesses = 0
        for (attributeDTO in attributes) {
            if (attributeDTO.isSetByMethodCall) {
                val methodCall = attributeDTO.methodCallDTO ?: continue
                if (checkIfTheMethodCallIsForGetter(methodCall)) {
                    logger.trace("Found access to ${methodCall.methodName}")
                    numberOfAccesses++
                }
            }
            if (attributeDTO.isSetByAttributeAccess) {
                val attributeAccessDTO =
                    attributeDTO.attributeAccessDTO ?: throw IllegalArgumentException("Attribute access not found")
                if (checkTheAttributeAccess(attributeAccessDTO)) {
                    logger.trace("Found access to ${attributeAccessDTO.getAttributeClassFQN()} from ${attributeAccessDTO.attributeName}")
                    numberOfAccesses++
                }
            }
        }
        return numberOfAccesses
    }

    private fun checkIfTheMethodCallIsForGetter(methodCall: MethodCallDTO) =
        (classesFromTarget.contains(methodCall.getClassThatIsCalled())
                && CommonFunctions.checkTheMethodCallToBeGetter(methodCall))

    private fun checkTheAttributeAccess(attributeAccessDTO: AttributeAccessDTO): Boolean {
        val attributeClass = attributeAccessDTO.getAttributeClassFQN()
        if (CommonFunctions.isTheFieldProtected(attributeClass, attributeAccessDTO.attributeName)) {
            return false
        }
        return classesFromTarget.contains(attributeClass)
    }

}