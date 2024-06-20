package runnables.perclass

import org.dxworks.kolekt.ProjectExtractor
import org.dxworks.kolekt.details.FileController
import kotlin.test.Test
import kotlin.test.assertEquals

class TestComplexTestClass {

    @Test
    fun testWithoutCache() {
//        val projectExtractor =
//            ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
//        projectExtractor.simpleParse()
//        projectExtractor.bindAllClasses()
//        runTests()
    }

    @Test
    fun testUsingCache() {
//        val projectExtractor =
//            ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
//        projectExtractor.parseAndSaveToDisk("E:\\AA.Faculta\\LICENTA\\A.KoleKT-Generated-for-test\\test-1")
//        projectExtractor.bindFromDisk(10, "E:\\AA.Faculta\\LICENTA\\A.KoleKT-Generated-for-test\\test-1")
//        runTests()
    }

    private fun runTests() {
        testFile()
        testOutsideFunction()
        testFileNameAndPackage()
        testFields()
        testFieldsSetByMethodCalls()
        testMethodsFromTestClass()
    }

    private fun testMethodsFromTestClass() {
        testCallOutsideFunctionMethod()
        testTestReturnMethod()
        testFun2Method()
        testFunctionWithIncreasedComplexityMethod()
    }

    private fun testFunctionWithIncreasedComplexityMethod() {
        val fileDTO =
            FileController.getFile("org.dxworks.kolekt.testpackage.TestFile.kt") ?: throw Exception("File not found")
        val testClass = fileDTO.classes.find { it.getFQN() == "org.dxworks.kolekt.testpackage.TestClass" }
            ?: throw Exception("Class not found")
        val functionWithIncreasedComplexityMethod =
            testClass.classMethods.find { it.methodName == "functionWithIncreasedComplexity" }
                ?: throw Exception("Method not found")
        assertEquals(false, functionWithIncreasedComplexityMethod.isConstructor())
        // test there is a MalwareWriter method call
        val malwareWriterMethodCall = functionWithIncreasedComplexityMethod.methodCalls.find { it.methodName == "MalwareWriter" }
            ?: throw Exception("Method call not found")
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", malwareWriterMethodCall.getClassThatIsCalled())
        // test that there is a writeMalwareWithParameters method call
        val writeMalwareWithParametersMethodCall = functionWithIncreasedComplexityMethod.methodCalls.find { it.methodName == "writeMalwareWithParameters" }
            ?: throw Exception("Method call not found")
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", writeMalwareWithParametersMethodCall.getClassThatIsCalled())
        // test the cylomatic complexity to be 7
        assertEquals(7, functionWithIncreasedComplexityMethod.getCyclomaticComplexity())
    }

    private fun testFun2Method() {
        val fileDTO =
            FileController.getFile("org.dxworks.kolekt.testpackage.TestFile.kt") ?: throw Exception("File not found")
        val testClass = fileDTO.classes.find { it.getFQN() == "org.dxworks.kolekt.testpackage.TestClass" }
            ?: throw Exception("Class not found")
        val fun2Method = testClass.classMethods.find { it.methodName == "fun2" }
            ?: throw Exception("Method not found")
        assertEquals(false, fun2Method.isConstructor())
        // test that no other methods call this method
        assertEquals(0, fun2Method.getMethodsThatCallThisMethod().size)
        assertEquals(0, fun2Method.getClassesThatCallThisMethod().size)
        // test if the list of annotations has a no element (AnnotationDTO)
        assertEquals(0, fun2Method.methodAnnotations.size)
        // check paratemer to be of type String and Double
        assertEquals(2, fun2Method.methodParameters.size)
        assertEquals("String", fun2Method.methodParameters[0].type)
        assertEquals("Double", fun2Method.methodParameters[1].type)
        // test return type to be of type Void
        assertEquals("Void", fun2Method.getMethodReturnType())
        // test cyclomatic complexity to be 1
        assertEquals(1, fun2Method.getCyclomaticComplexity())
        // test number of method calls to be 6
        assertEquals(6, fun2Method.methodCalls.size)
        // get testReturn() method call
        val testReturnMethodCall = fun2Method.methodCalls.find { it.methodName == "testReturn" }
            ?: throw Exception("Method call not found")
        assertEquals("org.dxworks.kolekt.testpackage.TestClass", testReturnMethodCall.getClassThatIsCalled())
    }

    private fun testTestReturnMethod() {
        val fileDTO =
            FileController.getFile("org.dxworks.kolekt.testpackage.TestFile.kt") ?: throw Exception("File not found")
        val testClass = fileDTO.classes.find { it.getFQN() == "org.dxworks.kolekt.testpackage.TestClass" }
            ?: throw Exception("Class not found")
        val testReturnMethod = testClass.classMethods.find { it.methodName == "testReturn" }
            ?: throw Exception("Method not found")
        assertEquals(false, testReturnMethod.isConstructor())
        // test that no other methods call this method
        assertEquals(0, testReturnMethod.getMethodsThatCallThisMethod().size)
        assertEquals(0, testReturnMethod.getClassesThatCallThisMethod().size)
        // test if the list of annotations has a single element (AnnotationDTO)
        assertEquals(0, testReturnMethod.methodAnnotations.size)
        // check paratemer to be of type String
        assertEquals(0, testReturnMethod.methodParameters.size)
        // test return type to be of type MalwareWriter
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", testReturnMethod.getMethodReturnType())
        // test cyclomatic complexity to be 1
        assertEquals(1, testReturnMethod.getCyclomaticComplexity())
    }

    private fun testCallOutsideFunctionMethod() {
        val fileDTO =
            FileController.getFile("org.dxworks.kolekt.testpackage.TestFile.kt") ?: throw Exception("File not found")
        val testClass = fileDTO.classes.find { it.getFQN() == "org.dxworks.kolekt.testpackage.TestClass" }
            ?: throw Exception("Class not found")
        val callOutsideFunctionMethod = testClass.classMethods.find { it.methodName == "callOutsideFunction" }
            ?: throw Exception("Method not found")
        assertEquals(false, callOutsideFunctionMethod.isConstructor())
        // test that no other methods call this method
        assertEquals(0, callOutsideFunctionMethod.getMethodsThatCallThisMethod().size)
        assertEquals(0, callOutsideFunctionMethod.getClassesThatCallThisMethod().size)
        // test if the list of annotations has a single element (AnnotationDTO)
        assertEquals(1, callOutsideFunctionMethod.methodAnnotations.size)
        assertEquals("Deprecated", callOutsideFunctionMethod.methodAnnotations[0].annotationName)
        // check paratemer to be of type String
        assertEquals(1, callOutsideFunctionMethod.methodParameters.size)
        assertEquals("String", callOutsideFunctionMethod.methodParameters[0].type)
    }

    private fun testFieldsSetByMethodCalls() {
        testMalwareWriterMethodCall()
        testMakeCoolStuffMethodCall()
    }

    private fun testMakeCoolStuffMethodCall() {
        val fileDTO =
            FileController.getFile("org.dxworks.kolekt.testpackage.TestFile.kt") ?: throw Exception("File not found")
        val testClass = fileDTO.classes.find { it.getFQN() == "org.dxworks.kolekt.testpackage.TestClass" }
            ?: throw Exception("Class not found")
        // find field amazingMalware
        val amazingMalwareField = testClass.classFields.find { it.name == "amazingMalware" }
            ?: throw Exception("Field not found")
        // get his method call
        val amazingMalwareMethodCall = amazingMalwareField.methodCallDTO ?: throw Exception("Method call not found")
        // check class that is called
        assertEquals(
            "org.dxworks.kolekt.testpackage.malware.MalwareWriter",
            amazingMalwareMethodCall.getClassThatIsCalled()
        )
        // check file that is called
        assertEquals(
            "org.dxworks.kolekt.testpackage.malware.MalwareWriter.kt",
            amazingMalwareMethodCall.getFileThatIsCalled()
        )
        // find the file that is called using FileController
        val amazingMalwareFile = FileController.getFile(amazingMalwareMethodCall.getFileThatIsCalled())
            ?: throw Exception("File not found")
        // find the class that is called using FileController
        val malwareWriterClass =
            amazingMalwareFile.classes.find { it.getFQN() == amazingMalwareMethodCall.getClassThatIsCalled() }
                ?: throw Exception("Class not found")
        val amazingMalwareMethod = malwareWriterClass.classMethods.find { it.methodName == "makeCoolStuff" }
            ?: throw Exception("Method not found")
        assertEquals(false, amazingMalwareMethod.isConstructor())
        assert(amazingMalwareMethod.getClassesThatCallThisMethod().contains("org.dxworks.kolekt.testpackage.TestClass"))
        assert(amazingMalwareMethod.getMethodsThatCallThisMethod().contains("org.dxworks.kolekt.testpackage.TestClass@fun2"))
    }

    private fun testMalwareWriterMethodCall() {
        val fileDTO =
            FileController.getFile("org.dxworks.kolekt.testpackage.TestFile.kt") ?: throw Exception("File not found")
        val testClass = fileDTO.classes.find { it.getFQN() == "org.dxworks.kolekt.testpackage.TestClass" }
            ?: throw Exception("Class not found")
        // find field mwWriter
        val mwWriterField = testClass.classFields.find { it.name == "mwWriter" } ?: throw Exception("Field not found")
        // get his method call
        val mwWriterMethodCall = mwWriterField.methodCallDTO ?: throw Exception("Method call not found")
        // check class that is called
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", mwWriterMethodCall.getClassThatIsCalled())
        // check file that is called
        assertEquals(
            "org.dxworks.kolekt.testpackage.malware.MalwareWriter.kt",
            mwWriterMethodCall.getFileThatIsCalled()
        )
        // find the file that is called using FileController
        val mwWriterFile = FileController.getFile(mwWriterMethodCall.getFileThatIsCalled())
            ?: throw Exception("File not found")
        // find the class that is called using FileController
        val mwWriterClass =
            mwWriterFile.classes.find { it.getFQN() == "org.dxworks.kolekt.testpackage.malware.MalwareWriter" }
                ?: throw Exception("Class not found")
        val mwWriterMethod = mwWriterClass.getConstructors().find { it.methodName == "MalwareWriter" }
            ?: throw Exception("Method not found")
        assertEquals(true, mwWriterMethod.isConstructor())
        assert(mwWriterMethod.getClassesThatCallThisMethod().contains("org.dxworks.kolekt.testpackage.TestClass"))
        assert(mwWriterMethod.getMethodsThatCallThisMethod().contains("org.dxworks.kolekt.testpackage.TestClass@fun2"))
        assert(
            mwWriterMethod.getMethodsThatCallThisMethod()
                .contains("org.dxworks.kolekt.testpackage.TestClass@functionWithIncreasedComplexity")
        )
    }

    private fun testFields() {
        val fileDTO =
            FileController.getFile("org.dxworks.kolekt.testpackage.TestFile.kt") ?: throw Exception("File not found")
        val testClass = fileDTO.classes.find { it.getFQN() == "org.dxworks.kolekt.testpackage.TestClass" }
            ?: throw Exception("Class not found")
        // test field type one by one
        // field age should be Int
        val ageField = testClass.classFields.find { it.name == "age" } ?: throw Exception("Field not found")
        assertEquals("Int", ageField.type)
        // field name should be String
        val nameField = testClass.classFields.find { it.name == "name" } ?: throw Exception("Field not found")
        assertEquals("String", nameField.type)
        // field address should be String
        val addressField = testClass.classFields.find { it.name == "address" } ?: throw Exception("Field not found")
        assertEquals("String", addressField.type)
        // field phoneNumber should be String
        val phoneNumberField =
            testClass.classFields.find { it.name == "phoneNumber" } ?: throw Exception("Field not found")
        assertEquals("String", phoneNumberField.type)
        // field height should be Double
        val heightField = testClass.classFields.find { it.name == "height" } ?: throw Exception("Field not found")
        assertEquals("Double", heightField.type)
        // field mwWriter should be MalwareWriter
        val mwWriterField = testClass.classFields.find { it.name == "mwWriter" } ?: throw Exception("Field not found")
        assertEquals("org.dxworks.kolekt.testpackage.malware.MalwareWriter", mwWriterField.type)
        // field counter should be Int
        val counterField = testClass.classFields.find { it.name == "counter" } ?: throw Exception("Field not found")
        assertEquals("Int", counterField.type)
        // field mwWriterStringAccessed should be String
        val mwWriterStringAccessedField = testClass.classFields.find { it.name == "mwWriterStringAccessed" }
            ?: throw Exception("Field not found")
        assertEquals("String", mwWriterStringAccessedField.type)
        // field amazingMalware should be AmazingMalware
        val amazingMalwareField =
            testClass.classFields.find { it.name == "amazingMalware" } ?: throw Exception("Field not found")
        assertEquals("org.dxworks.kolekt.testpackage.malware.AmazingMalware", amazingMalwareField.type)
        // field weight should be Double
        val weightField = testClass.classFields.find { it.name == "weight" } ?: throw Exception("Field not found")
        assertEquals("Double", weightField.type)
        // field weight2 should be String
        val weight2Field = testClass.classFields.find { it.name == "weight2" } ?: throw Exception("Field not found")
        assertEquals("String", weight2Field.type)
        // field weight3 should be String
        val weight3Field = testClass.classFields.find { it.name == "weight3" } ?: throw Exception("Field not found")
        assertEquals("String", weight3Field.type)
        // field weight4 should be String
        val weight4Field = testClass.classFields.find { it.name == "weight4" } ?: throw Exception("Field not found")
        assertEquals("String", weight4Field.type)
    }

    private fun testOutsideFunction() {
        val fileDTO =
            FileController.getFile("org.dxworks.kolekt.testpackage.TestFile.kt") ?: throw Exception("File not found")
        val outsideFunction =
            fileDTO.functions.find { it.methodName == "outsideFunction" } ?: throw Exception("Function not found")
        assertEquals(null, outsideFunction.getParentClassFQN())
        assertEquals("org.dxworks.kolekt.testpackage.TestFile.kt", outsideFunction.getParentFileSavedName())
        assertEquals(0, outsideFunction.methodParameters.size)
        assertEquals("Void", outsideFunction.getMethodReturnType())
        assertEquals(3, outsideFunction.getCyclomaticComplexity()) // todo: maybe ask if it is good to be this one
        assertEquals(2, outsideFunction.methodLocalVariables.size)
        assertEquals("Integer", outsideFunction.methodLocalVariables[0].type)
        assertEquals("Integer", outsideFunction.methodLocalVariables[1].type)

    }

    private fun testFile() {
        // find file
        val file =
            FileController.getFile("org.dxworks.kolekt.testpackage.TestFile.kt") ?: throw Exception("File not found")
        assertEquals("org.dxworks.kolekt.testpackage", file.filePackage)
        assertEquals("TestFile.kt", file.fileName)
        assertEquals("org.dxworks.kolekt.testpackage.TestFile.kt", file.getFileSavedName())
        assertEquals(1, file.classes.size)
        assertEquals("org.dxworks.kolekt.testpackage.TestClass", file.classes[0].getFQN())
        assertEquals(1, file.functions.size)
        assertEquals("outsideFunction", file.functions[0].methodName)
        assertEquals(4, file.imports.size) // todo: the alias is counted again
        assert(file.imports.contains("org.dxworks.kolekt.testpackage.malware.MalwareWriter"))
        assert(file.imports.contains("org.dxworks.kolekt.testpackage.malware.testMalwareOutside"))
        assert(file.imports.contains("org.dxworks.kolekt.testpackage.malware.writeMalwareOutside"))
        assertEquals(1, file.importAliases.size)
        assert(file.importAliases.contains("aliasTestMalware"))
        assertEquals(
            "org.dxworks.kolekt.testpackage.malware.testMalwareOutside",
            file.importAliases["aliasTestMalware"]
        )
    }

    private fun testFileNameAndPackage() {
        val megaType = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.TestClass")
            ?: throw Exception("Class not found")
        assertEquals("org.dxworks.kolekt.testpackage", megaType.classPackage)
    }

}
