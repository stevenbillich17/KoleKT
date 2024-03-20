package org.dxworks.kolekt.testpackage.fieldtypes.declarations

import org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega

class DifferentMethodOfDeclaring(val constructorParameter: String?) {
    val nullableString: String? = null
    val nonNullableString: String = "nonNullableString"
    val implicitlyTypedString = "implicitlyTypedString"
    val array: Array<Int> = arrayOf(1, 2, 3)
    val arrayOfHiddenOmega: Array<HiddenOmega> = arrayOf(HiddenOmega())
    val list: List<Int> = listOf(1, 2, 3)
    val set: Set<Int> = setOf(1, 2, 3)
    val map: Map<Int, String> = mapOf(1 to "one", 2 to "two", 3 to "three")
    val mutableList: MutableList<Int> = mutableListOf(1, 2, 3)
    val mutableSet: MutableSet<Long> = mutableSetOf(1, 2, 3)
    val mutableMap: MutableMap<Int, String> = mutableMapOf(1 to "one", 2 to "two", 3 to "three")
    val collection: Collection<String> = listOf("one", "two", "three")
    val mutableCollection: MutableCollection<String> = mutableListOf("one", "two", "three")

    val iterable: Iterable<String> = listOf("one", "two", "three")
    val sequence: Sequence<String> = sequenceOf("one", "two", "three")
}