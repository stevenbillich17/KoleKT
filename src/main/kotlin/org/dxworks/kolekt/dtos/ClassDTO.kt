package org.dxworks.kolekt.dtos

import org.dxworks.kolekt.enums.Modifier
import org.slf4j.LoggerFactory
import java.util.*

class ClassDTO(private val className : String? = null) {
    internal var classPackage : String? = null
    internal val classMethods : MutableList<MethodDTO> = mutableListOf()
    internal val classFields : MutableList<AttributeDTO> = mutableListOf()
    internal val classAnnotations : MutableList<AnnotationDTO> = mutableListOf()
    internal val classModifiers : MutableList<Modifier> = mutableListOf()
    internal val classInterfaces : MutableList<String> = mutableListOf()
    internal var superClass : String = ""
    private val logger = LoggerFactory.getLogger("ClassDTO@$className")

    override fun toString(): String {
        return "ClassDTO(\n" +
                " className='$className',\n" +
                " classPackage='$classPackage',\n" +
                " superClass='$superClass',\n" +
                " classInterfaces=(${buildClassInterfacesString()}),\n" +
                " classModifiers=(${buildClassModifiersString()}),\n" +
                " classAnnotations=$classAnnotations, \n" +
                " classMethods=$classMethods, \n" +
                " classFields=${buildClassFieldsString()}\n)}"
    }

    private fun buildClassInterfacesString(): String {
        var result = "\n    "
        classInterfaces.forEach { result += "$it\n    " }
        return result
    }

    fun addField(field: AttributeDTO?) {
        println("Adding fields: $field")
        field?.let {
            classFields.add(it)
        }
    }

    private fun buildClassModifiersString(): String {
        var result = "\n    "
        classModifiers.forEach { result += it.toString() + "\n    " }
        return result
    }

    private fun buildClassFieldsString(): String {
        var result = "(\n    "
        classFields.forEach { result += it.toString() + "\n    " }
        result += ")"
        return result
    }

    fun addModifier(modifierString: String) {
        try {
            val modifier = Modifier.valueOf(modifierString.uppercase(Locale.getDefault()))
            classModifiers.add(modifier)
        } catch (e: IllegalArgumentException) {
            logger.error("Modifier $modifierString not found")
        }
    }
}