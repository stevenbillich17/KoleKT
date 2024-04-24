package org.dxworks.kolekt.calculators

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.dxworks.kolekt.dtos.AnnotationDTO

import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.enums.Modifier


class BOvRMetricCalculator : MetricsCalculator {

    override fun calculateMetrics(classDTO: ClassDTO, setInClass: Boolean): JsonObject {
        var numberOfMethodsOverride = 0
        for (method in classDTO.classMethods) {
            if (checkIfOverrideAnnotated(method.methodAnnotations) || checkIfOverrideModified(method.methodModifiers)) {
                numberOfMethodsOverride++
            }
        }
        return buildJsonObject {
            put("BOvR", numberOfMethodsOverride / classDTO.classMethods.size.toDouble())
        }
    }

    private fun checkIfOverrideAnnotated(methodAnnotations: MutableList<AnnotationDTO>): Boolean {
        for (annotation in methodAnnotations) {
            if (annotation.annotationName == "Override") {
                return true
            }
        }
        return false
    }

    private fun checkIfOverrideModified(methodModifiers: MutableList<Modifier>): Boolean {
        for (modifier in methodModifiers) {
            if (modifier == Modifier.OVERRIDE) {
                return true
            }
        }
        return false
    }
}