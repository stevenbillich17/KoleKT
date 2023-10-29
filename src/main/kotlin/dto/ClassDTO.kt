package dto

class ClassDTO(private val className : String? = null) {
    internal var classPackage : String? = null
    internal val classMethods : MutableList<MethodDTO> = mutableListOf()

    override fun toString(): String {
        return "ClassDTO(\n" +
                " className='$className',\n" +
                " classPackage='$classPackage',\n" +
                " classMethods=$classMethods)}"
    }
}