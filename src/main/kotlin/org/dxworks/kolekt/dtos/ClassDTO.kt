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
    internal var subClassesFQNs: MutableList<String> = mutableListOf()
    internal var typeOfClass: ClassTypes = ClassTypes.CLASS

    internal val classMethods: MutableList<MethodDTO> = mutableListOf()
    private  val classConstructors: MutableList<MethodDTO> = mutableListOf()
    internal val classFields: MutableList<AttributeDTO> = mutableListOf()
    internal val classAnnotations: MutableList<AnnotationDTO> = mutableListOf()
    internal val classModifiers: MutableList<Modifier> = mutableListOf()
    internal val classInterfaces: MutableList<String> = mutableListOf()
    private val classImports: MutableList<String> = mutableListOf()
    private val classImportsAliases: MutableMap<String, String> = mutableMapOf()

    private var HIT = 0
    private var DIT = 0
    private var NOC = 0

    @Transient
    internal val typesFoundInClass = mutableMapOf<String, ClassDTO>()

    @Transient
    private var superClassDTO: ClassDTO? = null
    @Transient
    private val subClassesDTOs = mutableListOf<ClassDTO>()

    @Transient
    private val logger = LoggerFactory.getLogger("ClassDTO@$className")

    override fun toString(): String {
        return "ClassDTO(\n" +
                " className='$className',\n" +
                " classPackage='$classPackage',\n" +
                " superClass='$superClass',\n" +
                " HIT=$HIT,\n" +
                " DIT=$DIT,\n" +
                " NOC=$NOC,\n" +
                " imports=${buildImportsString()},\n" +
                " importsAliases=${buildImportsAliasesString()},\n" +
                " classSubClassesNames=${buildSubClassesString()},\n" +
                " classType=$typeOfClass,\n" +
                " classInterfaces=(${buildClassInterfacesString()}),\n" +
                " classModifiers=(${buildClassModifiersString()}),\n" +
                " classAnnotations=$classAnnotations, \n" +
                " classConstructors=$classConstructors, \n" +
                " classMethods=$classMethods, \n" +
                " classFields=${buildClassFieldsString()}\n)}"
    }

    private fun buildImportsAliasesString(): String {
        var result = "\n    "
        classImportsAliases.forEach { result += "${it.key} -> ${it.value}\n    " }
        return result
    }

    private fun buildImportsString(): String {
        var result = "\n    "
        classImports.forEach { result += "$it\n    " }
        return result
    }

    private fun buildClassInterfacesString(): String {
        var result = "\n    "
        classInterfaces.forEach { result += "$it\n    " }
        return result
    }

    private fun buildSubClassesString(): String {
        var result = "\n    "
        subClassesFQNs.forEach { result += "$it\n    " }
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
            logger.debug("Adding modifier: $modifierString")
        }
        try {
            val classType = ClassTypesUtils.getClassType(modifierString)
            if (classType != ClassTypes.CLASS)  {
                this.typeOfClass = ClassTypesUtils.getClassType(modifierString)
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
        typeOfClass = ClassTypes.OBJECT
    }

    fun setToInterfaceType() {
        typeOfClass = ClassTypes.INTERFACE
    }

    fun addConstructor(constructor: MethodDTO) {
        if (constructor.methodName != className)  {
            logger.error("Constructor name ${constructor.methodName} does not match class name $className")
            throw IllegalArgumentException("Constructor name ${constructor.methodName} does not match class name $className")
        }
        constructor.setConstructor()
        classConstructors.add(constructor)
    }

    fun getConstructors(): List<MethodDTO> {
        return classConstructors
    }

    fun setImports(imports: List<String>) {
        classImports.addAll(imports)
    }

    fun setImportAliases(aliases: Map<String, String>) {
        classImportsAliases.putAll(aliases)
    }

    fun getImports(): List<String> {
        return classImports
    }

    fun setSuperClassDTO(classDTO: ClassDTO) {
        superClassDTO = classDTO
    }

    fun getSuperClassDTO(): ClassDTO? {
        return superClassDTO
    }

    fun addSubClass(classDTO: ClassDTO) {
        subClassesFQNs.add(classDTO.getFQN())
        subClassesDTOs.add(classDTO)
    }

    fun getHIT(): Int {
        return HIT
    }

    fun getDIT(): Int {
        return DIT
    }

    fun getNOC(): Int {
        return NOC
    }

    fun setHIT(hit: Int) {
        HIT = hit
    }

    fun setDIT(dit: Int) {
        DIT = dit
    }

    fun setNOC(noc: Int) {
        NOC = noc
    }

    fun getSubClassesDTOs(): List<ClassDTO> {
        return subClassesDTOs
    }

    fun getSubClassesFQNs(): List<String> {
        return subClassesFQNs
    }

}