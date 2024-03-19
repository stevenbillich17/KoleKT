package org.dxworks.kolekt.testpackage.fieldtypes

import org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega

class ClassWithConstructor(val name: String, val age: Int = 4, hiddenOmega: HiddenOmega?) {

    constructor(name: String, age: Int) : this(name, age, null)

    constructor(name: String) : this(name, 4, null)

    constructor() : this("John", 4, null)
}