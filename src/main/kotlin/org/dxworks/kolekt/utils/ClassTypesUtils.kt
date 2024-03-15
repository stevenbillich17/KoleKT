package org.dxworks.kolekt.utils

object ClassTypesUtils {

    fun isBasicType(type: String): Boolean {
        return type in listOf(
            "Bin",
            "Boolean",
            "Character",
            "Double",
            "Float",
            "Integer",
            "Long",
            "Short",
            "String",
            "Hex",
            "Unsigned",
            "Int",
            "Void"
        )
    }
}