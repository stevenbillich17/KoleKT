package runnables.names

import org.dxworks.kolekt.ProjectExtractor
import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.AttributeDTO
import org.dxworks.kolekt.dtos.ClassDTO
import org.dxworks.kolekt.dtos.MethodDTO
import kotlin.test.Test

class TestFullNameResolving {
    @Test
    fun testFieldDeclaration() {
        val projectExtractor =
            ProjectExtractor("E:\\AA.Faculta\\LICENTA\\A.KoleKT\\KoleKT-tool\\KoleKT\\src\\main\\kotlin\\org\\dxworks\\kolekt\\testpackage")
        projectExtractor.simpleParse()
        projectExtractor.bindAllClasses()
        testFullNameResolvingForFields()
        testFullNamesResolvingForMethods()
    }

    private fun testFullNamesResolvingForMethods() {
        testFullNameresolvingForTestFilefun2()
    }

    private fun testFullNameresolvingForTestFilefun2() {
        val testClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.TestClass")
            ?: throw Exception("Class not found")
        val methodDTO = testClass.classMethods.find { it.methodName == "fun2" } ?: throw Exception("Method not found")
        assert(findParameter(methodDTO, "m1").type == "String")
        assert(findParameter(methodDTO, "m2").type == "Double")
        assert(findMethodLocalVariable(methodDTO, "x").type == "String")
        assert(findMethodLocalVariable(methodDTO, "xTurbat").type == "Integer")
        assert(findMethodLocalVariable(methodDTO, "y").type == "String")
        assert(findMethodLocalVariable(methodDTO, "z").type == "org.dxworks.kolekt.testpackage.malware.MalwareWriter")
        assert(findMethodLocalVariable(methodDTO, "s").type == "String")
        assert(findMethodLocalVariable(methodDTO, "scs").type == "org.dxworks.kolekt.testpackage.malware.AmazingMalware")
        assert(findMethodLocalVariable(methodDTO, "cpyMwWriter").type == "org.dxworks.kolekt.testpackage.malware.MalwareWriter")
    }

    private fun testFullNameResolvingForFields() {
        // create test for TestClass
        testFieldsResolvedNamesForTestClass()
        testFieldsResolvedNamesForMegaType()
        testFieldsResolvedNamesForMalwareWriter()
        testFieldResolvedNamesForClassWithConstructor()
        testFieldResolvedNamesForDifferentMethodOfDeclaring()
    }

    private fun testFieldResolvedNamesForDifferentMethodOfDeclaring() {
        val diffClass = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.fieldtypes.declarations.DifferentMethodOfDeclaring")
            ?: throw Exception("Class not found")
        assert(diffClass.getFQN() == "org.dxworks.kolekt.testpackage.fieldtypes.declarations.DifferentMethodOfDeclaring")
        assert(findField(diffClass, "constructorParameter").type == "String")
        assert(findField(diffClass, "nullableString").type == "String")
        assert(findField(diffClass, "nonNullableString").type == "String")
        assert(findField(diffClass, "implicitlyTypedString").type == "String")
        assert(findField(diffClass, "array").collectionType == listOf("Int"))
        assert(findField(diffClass, "arrayOfHiddenOmega").collectionType == listOf("org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega"))
        assert(findField(diffClass, "list").collectionType == listOf("Int"))
        assert(findField(diffClass, "set").collectionType == listOf("Int"))
        assert(findField(diffClass, "map").collectionType == listOf("Int", "String"))
        assert(findField(diffClass, "mutableList").collectionType == listOf("Int"))
        assert(findField(diffClass, "mutableSet").collectionType == listOf("Long"))
        assert(findField(diffClass, "mutableMap").collectionType == listOf("Int", "String"))
        assert(findField(diffClass, "collection").collectionType == listOf("String"))
        assert(findField(diffClass, "mutableCollection").collectionType == listOf("String"))
        assert(findField(diffClass, "iterable").collectionType == listOf("String"))
        assert(findField(diffClass, "sequence").collectionType == listOf("String"))

    }

    private fun testFieldResolvedNamesForClassWithConstructor() {
        val classWithConstructor: ClassDTO = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.fieldtypes.ClassWithConstructor")
            ?: throw Exception("Class not found")
        assert(classWithConstructor.getFQN() == "org.dxworks.kolekt.testpackage.fieldtypes.ClassWithConstructor")
        assert(findField(classWithConstructor, "name").type == "String")
        assert(findField(classWithConstructor, "age").type == "Int")
        assert(findField(classWithConstructor, "hiddenOmega").type == "org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega")
    }

    private fun testFieldsResolvedNamesForMalwareWriter() {
        val malwareWriter: ClassDTO = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.malware.MalwareWriter")
            ?: throw Exception("Class not found")
        assert(malwareWriter.getFQN() == "org.dxworks.kolekt.testpackage.malware.MalwareWriter")
        assert(findField(malwareWriter, "s").type == "String")
    }

    private fun testFieldsResolvedNamesForMegaType() {
        val megaType: ClassDTO = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.fieldtypes.MegaType")
            ?: throw Exception("Class not found")
        assert(megaType.getFQN() == "org.dxworks.kolekt.testpackage.fieldtypes.MegaType")
        assert(findField(megaType, "megaField").type == "org.dxworks.kolekt.testpackage.fieldtypes.OmegaType")
        assert(findField(megaType, "megaField2").type == "org.dxworks.kolekt.testpackage.fieldtypes.OmegaType")
        assert(findField(megaType, "megaHiddenOmega").type == "org.dxworks.kolekt.testpackage.fieldtypes.hidden.HiddenOmega")
    }

    private fun testFieldsResolvedNamesForTestClass() {
        val testClass: ClassDTO = FileController.findClassInFiles("org.dxworks.kolekt.testpackage.TestClass")
            ?: throw Exception("Class not found")
        assert(testClass.getFQN() == "org.dxworks.kolekt.testpackage.TestClass")
        assert(findField(testClass, "age").type == "Int")
        assert(findField(testClass, "name").type == "String")
        assert(findField(testClass, "address").type == "String")
        assert(findField(testClass, "phoneNumber").type == "String")
        assert(findField(testClass, "height").type == "Double")
        assert(findField(testClass, "mwWriter").type == "org.dxworks.kolekt.testpackage.malware.MalwareWriter")
        assert(findField(testClass, "counter").type == "Int")
        assert(findField(testClass, "mwWriterStringAccessed").type == "String")
        assert(findField(testClass, "amazingMalware").type == "org.dxworks.kolekt.testpackage.malware.AmazingMalware")
        assert(findField(testClass, "weight").type == "Double")
        assert(findField(testClass, "weight2").type == "String")
        assert(findField(testClass, "weight3").type == "String")
        assert(findField(testClass, "weight4").type == "String")
    }

    private fun findField(testClass: ClassDTO, fieldName: String): AttributeDTO {
        return testClass.classFields.find { it.name == fieldName } ?: throw Exception("Field not found")
    }

    private fun findParameter(methodDTO: MethodDTO, parameterName: String): AttributeDTO {
        return methodDTO.methodParameters.find { it.name == parameterName } ?: throw Exception("Parameter not found")
    }

    private fun findMethodLocalVariable(methodDTO: MethodDTO, variableName: String): AttributeDTO {
        return methodDTO.methodLocalVariables.find { it.name == variableName } ?: throw Exception("Local variable not found")
    }
}