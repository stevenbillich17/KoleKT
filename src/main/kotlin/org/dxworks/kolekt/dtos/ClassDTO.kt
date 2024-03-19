package org.dxworks.kolekt.dtos

import org.dxworks.kolekt.enums.Modifier
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.dxworks.kolekt.enums.ClassTypes
import org.dxworks.kolekt.utils.ClassTypesUtils
import org.slf4j.LoggerFactory
import java.util.*

@Serializable
class ClassDTO(internal val className: String? = null) {
    internal var classPackage: String? = null
    internal var superClass: String = ""
    internal var classSubClassesNames: MutableList<String> = mutableListOf()
    internal var classType: ClassTypes = ClassTypes.CLASS

    internal val classMethods: MutableList<MethodDTO> = mutableListOf()
    private  val classConstructors: MutableList<MethodDTO> = mutableListOf()
    internal val classFields: MutableList<AttributeDTO> = mutableListOf()
    internal val classAnnotations: MutableList<AnnotationDTO> = mutableListOf()
    internal val classModifiers: MutableList<Modifier> = mutableListOf()
    internal val classInterfaces: MutableList<String> = mutableListOf()

    internal val typesFoundInClass = mutableMapOf<String, ClassDTO>()

    @Transient
    var superClassDTO: ClassDTO? = null
    @Transient
    val mutableListOfSubClasses = mutableListOf<ClassDTO>()

    @Transient
    private val logger = LoggerFactory.getLogger("ClassDTO@$className")

    override fun toString(): String {
        return "ClassDTO(\n" +
                " className='$className',\n" +
                " classPackage='$classPackage',\n" +
                " superClass='$superClass',\n" +
                " classSubClassesNames=${buildSubClassesString()},\n" +
                " classType=$classType,\n" +
                " classInterfaces=(${buildClassInterfacesString()}),\n" +
                " classModifiers=(${buildClassModifiersString()}),\n" +
                " classAnnotations=$classAnnotations, \n" +
                " classConstructors=$classConstructors, \n" +
                " classMethods=$classMethods, \n" +
                " classFields=${buildClassFieldsString()}\n)}"
    }

    private fun buildClassInterfacesString(): String {
        var result = "\n    "
        classInterfaces.forEach { result += "$it\n    " }
        return result
    }

    private fun buildSubClassesString(): String {
        var result = "\n    "
        classSubClassesNames.forEach { result += "$it\n    " }
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
        if (className == "AnnotationClazz") {
            println("Adding modifier: $modifierString")
        }
        try {
            val classType = ClassTypesUtils.getClassType(modifierString)
            if (classType != ClassTypes.CLASS)  {
                this.classType = ClassTypesUtils.getClassType(modifierString)
            }
            val modifier = Modifier.valueOf(modifierString.uppercase(Locale.getDefault()))
            classModifiers.add(modifier)
        } catch (e: IllegalArgumentException) {
            logger.error("Modifier $modifierString not found")
        }
    }

    fun getFQN(): String {
        if (classPackage == null) return className ?: ""
        return "$classPackage.$className"
    }

    fun findMethodBasedOnMethodCall(methodCallDTO: MethodCallDTO): MethodDTO? {
        for (method in classMethods) {
            if (method.methodName == methodCallDTO.methodName && methodCallDTO.parameters.size == method.methodParameters.size) {
                return method
            }
        }
        return null
    }

    fun setToObjectType() {
        classType = ClassTypes.OBJECT
    }

    fun setToInterfaceType() {
        classType = ClassTypes.INTERFACE
    }

    fun addSubClass(classDTO: ClassDTO) {
        classSubClassesNames.add(classDTO.getFQN())
        mutableListOfSubClasses.add(classDTO)
        logger.debug("Added sub class: ${classDTO.getFQN()}")
    }

    fun addConstructor(constructor: MethodDTO) {
        if (constructor.methodName != className)  {
            logger.error("Constructor name ${constructor.methodName} does not match class name $className")
            throw IllegalArgumentException("Constructor name ${constructor.methodName} does not match class name $className")
        }
        classConstructors.add(constructor)
    }

    fun getConstructors(): List<MethodDTO> {
        return classConstructors
    }

}