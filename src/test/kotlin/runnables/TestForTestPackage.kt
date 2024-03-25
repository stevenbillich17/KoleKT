package runnables

import org.dxworks.kolekt.ProjectExtractor
import org.dxworks.kolekt.details.DictionariesController
import org.dxworks.kolekt.enums.ClassTypes
import org.dxworks.kolekt.enums.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestForTestPackage {

    @Test
    fun runTestForPackage() {
        val extractor = ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
        extractor.simpleParse()
        extractor.bindAllClasses()
        testBaseDetector()
        testGenericMalwareWriter()
        testMalwareDetector()
        testMalwareWriter()
        testTestClass()
        testDifferentTypeOfClasses()
        testInheritance()
    }

    private fun testInheritance() {
        val farAwayAunt = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.inheritance_second.FarAwayAunt", false)
        val farAwayAuntNephew = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.inheritance.FarAwayAuntNephew", false)
        assertEquals(farAwayAunt.getFQN(), farAwayAuntNephew.superClassDTO?.getFQN())
        val farAwayAuntSubClasses = farAwayAunt.mutableListOfSubClasses
        assertTrue(farAwayAuntSubClasses.any { it.getFQN() == farAwayAuntNephew.getFQN() })


        val son = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.inheritance.Son", false)
        val parent = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.inheritance.Parent", false)
        val grandparent = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.inheritance.Grandparent", false)
        val sonBrother = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.inheritance.SonBrother", false)
        assertEquals(parent, son.superClassDTO)
        assertEquals(grandparent, parent.superClassDTO)

        // find grandparent sub classes
        val grandparentSubClasses = grandparent.mutableListOfSubClasses
        assertTrue(grandparentSubClasses.any { it == parent })

        // find parent sub classes
        val fatherSubClasses = parent.mutableListOfSubClasses
        assertTrue(fatherSubClasses.any { it == son })
        assertTrue { fatherSubClasses.any { it == sonBrother } }

    }

    private fun testDifferentTypeOfClasses() {
        // data class
        val dataClazz = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.classes.DataClazz", false)
        val classTypeData = dataClazz.typeOfClass
        assertEquals(ClassTypes.DATA, classTypeData)

        // object class
        val objectClazz = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.classes.ObjectClazz", false)
        val classType = objectClazz.typeOfClass
        assertEquals(ClassTypes.OBJECT, classType)

        // enum class
        val enumClazz = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.classes.EnumClazz", false)
        val classTypeEnum = enumClazz.typeOfClass
        assertEquals(ClassTypes.ENUM, classTypeEnum)

        // annotation class
        val annotationClazz = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.classes.AnnotationClazz", false)
        val classTypeAnnotation = annotationClazz.typeOfClass
        assertEquals(ClassTypes.ANNOTATION, classTypeAnnotation)

        // interface class
        val interfaceClazz = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.classes.InterfaceClazz", false)
        val classTypeInterface = interfaceClazz.typeOfClass
        assertEquals(ClassTypes.INTERFACE, classTypeInterface)

        // basic class
        val basicClazz = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.classes.BasicClazz", false)
        val classTypeBasic = basicClazz.typeOfClass
        assertEquals(ClassTypes.CLASS, classTypeBasic)
    }

    private fun testMalwareWriter() {
        // CLASS
        val x = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.malware.MalwareWriter", false)
        assertEquals("MalwareWriter", x.className)
        assertEquals("org.dxworks.kolekt.testpackage.malware", x.classPackage)
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", x.getFQN())
        assertEquals("org.dxworks.kolekt.testpackage.malware.GenericMalwareWriter", x.superClass)

        // FIELDS
        assertEquals(1, x.classFields.size)
        assertTrue { x.classFields.any { it.name == "s" } }
        val sField = x.classFields.find { it.name == "s" }
        val sClassDTO = sField?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", sClassDTO?.className)

        // METHODS
        assertEquals(6, x.classMethods.size)

        assertTrue(x.classMethods.any { it.methodName == "writeMalware" })
        val method = x.classMethods.find { it.methodName == "writeMalware" }
        assertTrue(method?.methodModifiers?.contains(Modifier.OVERRIDE) ?: false, "The 'writeMalware' method does not have the 'OVERRIDE' modifier")

        assertTrue(x.classMethods.any { it.methodName == "writeMalwareWithParameters" })
        val methodWithParameters = x.classMethods.find { it.methodName == "writeMalwareWithParameters" }
        assertEquals(2, methodWithParameters?.methodParameters?.size)
        assertTrue(methodWithParameters?.methodParameters?.any { it.name == "x" } ?: false)
        assertTrue(methodWithParameters?.methodParameters?.any { it.name == "y" } ?: false)
        var xClassDTO = methodWithParameters?.methodParameters?.find { it.name == "x" }?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", xClassDTO?.className)
        var yClassDTO = methodWithParameters?.methodParameters?.find { it.name == "y" }?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", yClassDTO?.className)
        val methodCallFromWriteMalwareWithParam = methodWithParameters?.methodCalls?.first()
        assertEquals("println", methodCallFromWriteMalwareWithParam?.methodName)

        assertTrue(x.classMethods.any { it.methodName == "somethingWeird" })
        var methodWeird = x.classMethods.find { it.methodName == "somethingWeird" }
        var mldClassDTO = methodWeird?.methodParameters?.firstOrNull()?.getClassDTO() // checking binding
        assertEquals("org.dxworks.kolekt.testpackage.detector.MalwareDetector", mldClassDTO?.getFQN())
        val parameter = methodWeird?.methodParameters?.firstOrNull()
        assertEquals("mld", parameter?.name)
        val parameterClassDTO = parameter?.getClassDTO()
        assertEquals("org.dxworks.kolekt.testpackage.detector.MalwareDetector", parameterClassDTO?.getFQN())

        assertTrue(x.classMethods.any { it.methodName == "calculate" })
        val methodCalculate = x.classMethods.find { it.methodName == "calculate" }
        assertEquals(2, methodCalculate?.methodParameters?.size)
        assertTrue(methodCalculate?.methodParameters?.any { it.name == "a" } ?: false)
        assertTrue(methodCalculate?.methodParameters?.any { it.name == "b" } ?: false)
        val aClassDTO = methodCalculate?.methodParameters?.find { it.name == "a" }?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", aClassDTO?.className)
        val bClassDTO = methodCalculate?.methodParameters?.find { it.name == "b" }?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", bClassDTO?.className)
        val returnType = methodCalculate?.getMethodReturnTypeClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", returnType?.className)

        assertTrue(x.classMethods.any { it.methodName == "initializeInt" })
        val methodInitializeInt = x.classMethods.find { it.methodName == "initializeInt" }
        val returnTypeInitializeInt = methodInitializeInt?.getMethodReturnTypeClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", returnTypeInitializeInt?.className)

        assertTrue(x.classMethods.any { it.methodName == "makeCoolStuff" })
        // check return type of makeCoolStuff
        val methodMakeCoolStuff = x.classMethods.find { it.methodName == "makeCoolStuff" }
        val returnTypeMakeCoolStuff = methodMakeCoolStuff?.getMethodReturnTypeClassDTO()
        assertEquals("org.dxworks.kolekt.testpackage.malware.AmazingMalware", returnTypeMakeCoolStuff?.getFQN())
        // check method call from makeCoolStuff
        val methodCall = methodMakeCoolStuff?.methodCalls?.find { it.methodName == "AmazingMalware" }
        assertTrue(methodCall != null)
        // todo: bind method call to some type
        // todo: should add AmazingMalware() as method call
        val methodCallFromMakeCoolStuff = methodMakeCoolStuff?.methodCalls?.find { it.methodName == "calculate" }
        assertEquals("calculate", methodCallFromMakeCoolStuff?.methodName)
        // check local variables declared in method
        assertTrue(methodMakeCoolStuff?.methodLocalVariables?.any { it.name == "x" } ?: false)
        assertTrue(methodMakeCoolStuff?.methodLocalVariables?.any { it.name == "y" } ?: false)
        assertTrue(methodMakeCoolStuff?.methodLocalVariables?.any { it.name == "z" } ?: false)
        var xLocalVariable = methodMakeCoolStuff?.methodLocalVariables?.find { it.name == "x" }
        var xLocalVariableClassDTO = xLocalVariable?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", xLocalVariableClassDTO?.className)
        var yLocalVariable = methodMakeCoolStuff?.methodLocalVariables?.find { it.name == "y" }
        var yLocalVariableClassDTO = yLocalVariable?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", yLocalVariableClassDTO?.className)
        val zLocalVariable = methodMakeCoolStuff?.methodLocalVariables?.find { it.name == "z" }
        val zLocalVariableClassDTO = zLocalVariable?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", zLocalVariableClassDTO?.className)


        // TODO: add support for file methods
//        val methodCallFromWriteMalwareOutside = x.classMethods.find { it.methodName == "writeMalwareOutside" }?.methodCalls?.first()
//        assertEquals("println", methodCallFromWriteMalwareOutside?.methodName)
//        val methodWriteMalwareOutside = x.classMethods.find { it.methodName == "writeMalwareOutside" }
//        assertEquals(2, methodWriteMalwareOutside?.methodParameters?.size)
//        assertTrue(methodWriteMalwareOutside?.methodParameters?.any { it.name == "x" } ?: false)
//        assertTrue(methodWriteMalwareOutside?.methodParameters?.any { it.name == "y" } ?: false)
//        xClassDTO = methodWriteMalwareOutside?.methodParameters?.find { it.name == "x" }?.getClassDTO()
//        assertEquals("com.dxworks.kolekt.BasicClassDTO", xClassDTO?.className)
//        yClassDTO = methodWriteMalwareOutside?.methodParameters?.find { it.name == "y" }?.getClassDTO()
//        assertEquals("com.dxworks.kolekt.BasicClassDTO", yClassDTO?.className)

    }

    private fun testTestClass() {
        val x = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.TestClass", false)
        assertEquals("TestClass", x.className)
        assertEquals("org.dxworks.kolekt.testpackage", x.classPackage)
        assertEquals("org.dxworks.kolekt.testpackage.TestClass", x.getFQN())
        assertEquals(12, x.classFields.size)
        assertTrue { x.classFields.any { it.name == "age" } }
        assertTrue { x.classFields.any { it.name == "name" } }
        assertTrue { x.classFields.any { it.name == "address" } }
        assertTrue { x.classFields.any { it.name == "phoneNumber" } }
        assertTrue { x.classFields.any { it.name == "height" } }
        assertTrue { x.classFields.any { it.name == "mwWriter" } }
        assertTrue { x.classFields.any { it.name == "counter" } }
        assertTrue { x.classFields.any { it.name == "amazingMalware" } }
        assertTrue { x.classFields.any { it.name == "weight" } }
        assertTrue { x.classFields.any { it.name == "weight2" } }
        assertTrue { x.classFields.any { it.name == "weight3" } }
        assertTrue { x.classFields.any { it.name == "weight4" } }
        val ageField = x.classFields.find { it.name == "age" }
        val ageClassDTO = ageField?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", ageClassDTO?.className)
        val nameField = x.classFields.find { it.name == "name" }
        val nameClassDTO = nameField?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", nameClassDTO?.className)
        val addressField = x.classFields.find { it.name == "address" }
        val addressClassDTO = addressField?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", addressClassDTO?.className)
        val phoneNumberField = x.classFields.find { it.name == "phoneNumber" }
        val phoneNumberClassDTO = phoneNumberField?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", phoneNumberClassDTO?.className)
        val heightField = x.classFields.find { it.name == "height" }
        val heightClassDTO = heightField?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", heightClassDTO?.className)
        val mwWriterField = x.classFields.find { it.name == "mwWriter" }
        val mwWriterClassDTO = mwWriterField?.getClassDTO()
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", mwWriterClassDTO?.getFQN())
        val counterField = x.classFields.find { it.name == "counter" }
        val counterClassDTO = counterField?.getClassDTO()
        //assertEquals("com.dxworks.kolekt.BasicClassDTO", counterClassDTO?.className) todo: repair this, not it is not found yet dto
        val amazingMalwareField = x.classFields.find { it.name == "amazingMalware" }
        val amazingMalwareClassDTO = amazingMalwareField?.getClassDTO()
        assertEquals("org.dxworks.kolekt.testpackage.malware.AmazingMalware", amazingMalwareClassDTO?.getFQN())
        val weightField = x.classFields.find { it.name == "weight" }
        val weightClassDTO = weightField?.getClassDTO()
        // assertEquals("com.dxworks.kolekt.BasicClassDTO", weightClassDTO?.className) todo: repair this, not it is not found yet dto
        val weight2Field = x.classFields.find { it.name == "weight2" }
        val weight2ClassDTO = weight2Field?.getClassDTO()
        // assertEquals("com.dxworks.kolekt.BasicClassDTO", weight2ClassDTO?.className) todo: repair this, not it is not found yet dto
        val weight3Field = x.classFields.find { it.name == "weight3" }
        val weight3ClassDTO = weight3Field?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", weight3ClassDTO?.className)
        val weight4Field = x.classFields.find { it.name == "weight4" }
        val weight4ClassDTO = weight4Field?.getClassDTO()
        // assertEquals("com.dxworks.kolekt.BasicClassDTO", weight4ClassDTO?.className) todo: repair this, not it is not found yet dto

        // Test getWeightValue method
        val getWeightValueMethod = x.classMethods.find { it.methodName == "getWeightValue" }
        val returnTypeGetWeightValue = getWeightValueMethod?.getMethodReturnTypeClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", returnTypeGetWeightValue?.className)

        // test constructor todo: add support for constructors
//        val constructor = x.classMethods.find { it.methodName == "TestClass" }
//        assertEquals(4, constructor?.methodParameters?.size)
//        assertTrue(constructor?.methodParameters?.any { it.name == "address" } ?: false)
//        assertTrue(constructor?.methodParameters?.any { it.name == "nothing" } ?: false)
//        assertTrue(constructor?.methodParameters?.any { it.name == "name" } ?: false)
//        assertTrue(constructor?.methodParameters?.any { it.name == "anotherInt" } ?: false)

        // test callOutsideFunction method
        val callOutsideFunctionMethod = x.classMethods.find { it.methodName == "callOutsideFunction" }
        val callOutsideFunctionMethodAnnotation = callOutsideFunctionMethod?.methodAnnotations?.firstOrNull()
        assertEquals("Deprecated", callOutsideFunctionMethodAnnotation?.annotationName)
        val callOutsideFunctionMethodCall = callOutsideFunctionMethod?.methodCalls?.firstOrNull()
        assertEquals("outsideFunction", callOutsideFunctionMethodCall?.methodName) // todo: add method call DTO (to class that was called)


        // test testReturn method
        val testReturnMethod = x.classMethods.find { it.methodName == "testReturn" }
        val returnTypeTestReturn = testReturnMethod?.getMethodReturnTypeClassDTO()
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", returnTypeTestReturn?.getFQN())

        // test fun2 method
        val fun2Method = x.classMethods.find { it.methodName == "fun2" }
        val fun2MethodLocalVariables = fun2Method?.methodLocalVariables
        assertTrue(fun2MethodLocalVariables?.any { it.name == "x" } ?: false)
        assertTrue(fun2MethodLocalVariables?.any { it.name == "xTurbat" } ?: false)
        assertTrue(fun2MethodLocalVariables?.any { it.name == "y" } ?: false)
        assertTrue(fun2MethodLocalVariables?.any { it.name == "z" } ?: false)
        assertTrue(fun2MethodLocalVariables?.any { it.name == "cpyMwWriter" } ?: false)
        // test binding for local variables
        val xLocalVariable = fun2MethodLocalVariables?.find { it.name == "x" }
        val xLocalVariableClassDTO = xLocalVariable?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", xLocalVariableClassDTO?.className)
        val xTurbatLocalVariable = fun2MethodLocalVariables?.find { it.name == "xTurbat" }
        val xTurbatLocalVariableClassDTO = xTurbatLocalVariable?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", xTurbatLocalVariableClassDTO?.className)
        val yLocalVariable = fun2MethodLocalVariables?.find { it.name == "y" }
        val yLocalVariableClassDTO = yLocalVariable?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", yLocalVariableClassDTO?.className)
        val zLocalVariable = fun2MethodLocalVariables?.find { it.name == "z" }
        val zLocalVariableClassDTO = zLocalVariable?.getClassDTO()
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", zLocalVariableClassDTO?.getFQN())
        val cpyMwWriterLocalVariable = fun2MethodLocalVariables?.find { it.name == "cpyMwWriter" }
        val cpyMwWriterLocalVariableClassDTO = cpyMwWriterLocalVariable?.getClassDTO()
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", cpyMwWriterLocalVariableClassDTO?.getFQN())
        // test method calls
        val fun2MethodCall = fun2Method?.methodCalls?.find { it.methodName == "writeMalware"}
        assertEquals("writeMalware", fun2MethodCall?.methodName)
        val fun2MethodCall2 = fun2Method?.methodCalls?.find { it.methodName == "writeMalwareWithParameters" }
        assertEquals("writeMalwareWithParameters", fun2MethodCall2?.methodName)
        val fun2MethodCall3 = fun2Method?.methodCalls?.find { it.methodName == "writeMalwareOutside" }
        assertEquals("writeMalwareOutside", fun2MethodCall3?.methodName)

    }

    private fun testMalwareDetector() {
        val x = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.detector.MalwareDetector", false)
        assertEquals("MalwareDetector", x.className)
        assertEquals("org.dxworks.kolekt.testpackage.detector", x.classPackage)
        assertEquals("org.dxworks.kolekt.testpackage.detector.MalwareDetector", x.getFQN())
        assertEquals(1, x.classFields.size)
        assertTrue { x.classFields.any { it.name == "mw" } }
        val mwField = x.classFields.find { it.name == "mw" }
        val mwClassDTO = mwField?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", mwClassDTO?.className)
        assertEquals(2, x.classMethods.size)
        assertTrue(x.classMethods.any { it.methodName == "detectMalware" })
        assertTrue(x.classMethods.any { it.methodName == "sendDetection" })
        val detectMalwareMethod = x.classMethods.find { it.methodName == "detectMalware" }
        detectMalwareMethod?.methodAnnotations?.any { it.annotationName == "Override" }?.let { assertTrue(it) }
        assertTrue(detectMalwareMethod?.methodModifiers?.contains(Modifier.OVERRIDE) ?: false, "The 'detectMalware' method does not have the 'OVERRIDE' modifier")
        assertTrue(detectMalwareMethod?.methodLocalVariables?.any { it.name == "detectionString" } ?: false)
        assertTrue(detectMalwareMethod?.methodLocalVariables?.any { it.name == "pathToMalware" } ?: false)
        val detectionStringClassDTO = detectMalwareMethod?.methodLocalVariables?.find { it.name == "detectionString" }?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", detectionStringClassDTO?.className)
        val pathToMalwareClassDTO = detectMalwareMethod?.methodLocalVariables?.find { it.name == "pathToMalware" }?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", pathToMalwareClassDTO?.className)

        // test Interfaces
        println(x.classInterfaces)
        assertTrue(x.classInterfaces.any { it == "org.dxworks.kolekt.testpackage.detector.DetectionSender" })
        assertTrue(x.classInterfaces.any { it == "org.dxworks.kolekt.testpackage.detector.DetectionScanner" })
        assertTrue(x.classInterfaces.any { it == "org.dxworks.kolekt.testpackage.destroyer.AppDestroyer" })

        // test supper class
        assertEquals("org.dxworks.kolekt.testpackage.detector.BaseDetector", x.superClass)

    }

    private fun testGenericMalwareWriter() {
        val x = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.malware.GenericMalwareWriter", false)
        assertEquals("GenericMalwareWriter", x.className)
        assertEquals("org.dxworks.kolekt.testpackage.malware", x.classPackage)
        assertEquals("org.dxworks.kolekt.testpackage.malware.GenericMalwareWriter", x.getFQN())
        assertEquals(1, x.classMethods.size)
        assertTrue(x.classMethods.any { it.methodName == "writeMalware" })
        val method = x.classMethods.find { it.methodName == "writeMalware" }
        assertTrue(method?.methodModifiers?.contains(Modifier.ABSTRACT) ?: false, "The 'writeMalware' method does not have the 'ABSTRACT' modifier")
        assertTrue(x.classModifiers.contains(Modifier.ABSTRACT), "The 'GenericMalwareWriter' class does not have the 'DEPRECATED' modifier")
        // test annotation for class
        assertTrue(x.classAnnotations.any { it.annotationName == "Deprecated" })
    }

    fun testBaseDetector() {
        val x = DictionariesController.findClassAfterFQN("org.dxworks.kolekt.testpackage.detector.BaseDetector", false)
        assertEquals("BaseDetector", x.className)
        assertEquals("org.dxworks.kolekt.testpackage.detector", x.classPackage)
        assertEquals("org.dxworks.kolekt.testpackage.detector.BaseDetector", x.getFQN())
        assertEquals(1, x.classFields.size)
        assertTrue { x.classFields.any { it.name == "code" } }
        val codeField = x.classFields.find { it.name == "code" }
        assertTrue(codeField?.attributeModifiers?.contains(Modifier.OPEN) ?: false, "The 'code' field does not have the 'OPEN' modifier")
        assertEquals(2, x.classMethods.size)
        assertTrue(x.classMethods.any { it.methodName == "detectMalware" })
        assertTrue(x.classMethods.any { it.methodName == "detectMalwareWithParameters" })
        val method = x.classMethods.find { it.methodName == "detectMalwareWithParameters" }
        assertEquals(2, method?.methodParameters?.size)
        assertTrue(method?.methodParameters?.any { it.name == "x" } ?: false)
        assertTrue(method?.methodParameters?.any { it.name == "y" } ?: false)

        // test if parameter got the right class dto
        val xClassDTO = x.classMethods.find { it.methodName == "detectMalwareWithParameters" }?.methodParameters?.find { it.name == "x" }?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", xClassDTO?.className)
        val yClassDTO = x.classMethods.find { it.methodName == "detectMalwareWithParameters" }?.methodParameters?.find { it.name == "y" }?.getClassDTO()
        assertEquals("com.dxworks.kolekt.BasicClassDTO", yClassDTO?.className)

        // test detectMalware method call
        val detectMalwareMethod = x.classMethods.find { it.methodName == "detectMalware" }
        assertEquals("println", detectMalwareMethod?.methodCalls?.first()?.methodName)

        // test detectMalwareWithParameters method call
        val detectMalwareWithParametersMethod = x.classMethods.find { it.methodName == "detectMalwareWithParameters" }
        assertEquals("println", detectMalwareWithParametersMethod?.methodCalls?.first()?.methodName)

        // test detectMalware modifier
        assertTrue(detectMalwareMethod?.methodModifiers?.contains(Modifier.OPEN) ?: false, "The 'detectMalware' method does not have the 'OPEN' modifier")
    }
}