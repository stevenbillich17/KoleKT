package org.dxworks.kolekt.utils

import org.dxworks.kolekt.enums.ClassTypes

object ClassTypesUtils {

    fun getClassType(type: String): ClassTypes {
        return when {
            type == "data" -> ClassTypes.DATA
            type == "enum" -> ClassTypes.ENUM
            type == "interface" -> ClassTypes.INTERFACE
            type == "object" -> ClassTypes.OBJECT
            type == "annotation" -> ClassTypes.ANNOTATION
            else -> ClassTypes.CLASS
        }
    }

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
            "Void",
            "Bin?",
            "Boolean?",
            "Character?",
            "Double?",
            "Float?",
            "Integer?",
            "Long?",
            "Short?",
            "String?",
            "Hex?",
            "Unsigned?",
            "Int?",
            "Void?"
        )
    }
}