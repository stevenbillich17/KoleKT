package org.dxworks.kolekt.dtos

class ClassDTO(private val className : String? = null) {
    internal var classPackage : String? = null
    internal val classMethods : MutableList<MethodDTO> = mutableListOf()
    internal val classFields : MutableList<AttributeDTO> = mutableListOf()
    internal val classAnnotations : MutableList<AnnotationDTO> = mutableListOf()

    override fun toString(): String {
        return "ClassDTO(\n" +
                " className='$className',\n" +
                " classPackage='$classPackage',\n" +
                " classAnnotations=$classAnnotations, \n" +
                " classMethods=$classMethods, \n" +
                " classFields=$classFields\n)}"
    }

    fun addField(field: AttributeDTO?) {
        println("Adding fields: $field")
        field?.let {
            classFields.add(it)
        }
    }
}