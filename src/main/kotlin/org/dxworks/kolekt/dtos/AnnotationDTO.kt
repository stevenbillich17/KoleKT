package org.dxworks.kolekt.dtos

class AnnotationDTO (val annotationName: String){
    private val annotationParameters = mutableListOf<String>()

    fun addParameter(parameter: String) {
        annotationParameters.add(parameter)
    }

    override fun toString(): String {
        return "   AnnotationDTO(annotationName='$annotationName', annotationParameters=$annotationParameters)"
    }

    fun addAnnotationArguments(annotationArguments: MutableList<String>) {
        annotationArguments.forEach(::addParameter)
    }
}