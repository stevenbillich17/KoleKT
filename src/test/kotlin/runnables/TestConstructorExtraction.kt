package runnables

import org.dxworks.kolekt.ProjectExtractor
import org.dxworks.kolekt.details.DictionariesController
import kotlin.test.Test
import kotlin.test.assertEquals

class TestConstructorExtraction {

    @Test
    fun testFieldDeclaration() {
        val projectExtractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage\\fieldtypes")
        projectExtractor.simpleParse()
        projectExtractor.bindAllClasses()
        testConstructorExtraction()
    }

    private fun testConstructorExtraction() {
        val classWithConstructor = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.fieldtypes.ClassWithConstructor", false)
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.ClassWithConstructor", classWithConstructor.getFQN())

        val constructors = classWithConstructor.getConstructors()
        assertEquals(4, constructors.size)

        // test their return type
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.ClassWithConstructor", constructors[0].getMethodReturnType())
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.ClassWithConstructor", constructors[1].getMethodReturnType())
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.ClassWithConstructor", constructors[2].getMethodReturnType())
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.ClassWithConstructor", constructors[3].getMethodReturnType())

        // find one with the three parameters
        val constructorWithThreeParameters = constructors.find { it.methodParameters.size == 3 }
        assertEquals("String", constructorWithThreeParameters?.methodParameters?.get(0)?.type)
        assertEquals("Int", constructorWithThreeParameters?.methodParameters?.get(1)?.type)
        assertEquals("org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega", constructorWithThreeParameters?.methodParameters?.get(2)?.type)

        // find one with the two parameters
        val constructorWithTwoParameters = constructors.find { it.methodParameters.size == 2 }
        assertEquals("String", constructorWithTwoParameters?.methodParameters?.get(0)?.type)
        assertEquals("Int", constructorWithTwoParameters?.methodParameters?.get(1)?.type)

        // find one with the one parameter
        val constructorWithOneParameter = constructors.find { it.methodParameters.size == 1 }
        assertEquals("String", constructorWithOneParameter?.methodParameters?.get(0)?.type)

        // find one with no parameters
        val constructorWithNoParameter = constructors.find { it.methodParameters.isEmpty() }
        assertEquals(0, constructorWithNoParameter?.methodParameters?.size)

    }
}