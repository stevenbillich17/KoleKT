package org.dxworks.kolekt.enums

enum class CollectionType(val writtenType: String) {
    ARRAY("Array"),
    LIST("List"),
    SET("Set"),
    MAP("Map"),
    MUTABLE_LIST("MutableList"),
    MUTABLE_SET("MutableSet"),
    MUTABLE_MAP("MutableMap"),
    COLLECTION("Collection"),
    MUTABLE_COLLECTION("MutableCollection"),
    SEQUENCE("Sequence"),
    ITERABLE("Iterable");

    companion object {
        fun fromString(type: String?): CollectionType? {
            if (type == null) return null
            return entries.find { it.writtenType.equals(type, ignoreCase = true) }
        }

        fun fromStringType(type: String?): CollectionType? {
            if (type == null) return null
            return entries.find { it.name.equals(type, ignoreCase = true) }
        }
    }
}