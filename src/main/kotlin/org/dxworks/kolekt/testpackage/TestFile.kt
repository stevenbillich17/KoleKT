package org.dxworks.kolekt.testpackage

import org.dxworks.kolekt.testpackage.malware.MalwareWriter
import org.dxworks.kolekt.testpackage.malware.testMalwareOutside
import org.dxworks.kolekt.testpackage.malware.writeMalwareOutside
import org.dxworks.kolekt.testpackage.malware.testMalwareOutside as aliasTestMalware


class TestClass(var age: Int, var name: String) {
    private var address = "Default Address"
    protected var phoneNumber: String? = null
    var height: Double = 5.0
    val mwWriter: MalwareWriter = MalwareWriter()
    val counter = mwWriter.initializeInt()
    val mwWriterStringAccessed = mwWriter.s
    val amazingMalware = mwWriter.makeCoolStuff()
    var weight = getWeightValue()
    var weight2  = aliasTestMalware()
    var weight3: String  = testMalwareOutside()
    var weight4 = testMalwareOutside()

    private fun getWeightValue(): Double {
        return 5.0
    }

    constructor(address: String, nothing: Int, name: String, anotherInt: Int ) : this(nothing, name) {
        this.address = address
    }

    @Deprecated("Use the other constructor")
    fun callOutsideFunction(message: String) {
        outsideFunction()
    }

    fun testReturn(): MalwareWriter {
        return mwWriter
    }

    fun fun2(m1: String, m2: Double? = 5.0) {
        val x = "Hello"

        val xTurbat = 5
        var y: String? = null
        val z = MalwareWriter()
        val s = z.s
        // todo: should make the calls and attributes a linked list
        //val slen = z.s.length
        //val scsFar = z.makeCoolStuff().hashCode()
        val scs = z.makeCoolStuff()
        val cpyMwWriter = testReturn()
        z.writeMalware()
        z.writeMalwareWithParameters("ceva", "altceva")
        writeMalwareOutside("ceva" + "wow", "altceva")
    }

    fun functionWithIncreasedComplexity(): Int {
        val x = 3
        if (x > 2) {
            println("x is greater than 2")
        } else {
            println("x is not greater than 2")
        }
        while (x < 0) {
            println("x is greater than 0")
        }
        for (i in 0..10) {
            println("i is $i")
        }
        when (x) {
            1 -> println("x is 1")
            2 -> println("x is 2")
            else -> println("x is not 1 or 2")
        }
        return x
    }
}

fun outsideFunction() {
    var x = 5
    val y = 2
    if (x == 5 || y == 1) {
        return
    }
    x = 3
}