package org.dxworks.kolekt.binders

import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FileBinder(private val fileDTO: FileDTO) {
    val logger: Logger = LoggerFactory.getLogger("FileBinder@${fileDTO.fileName}")
    val classesInFile = fileDTO.classes
    val functionsInFile = fileDTO.functions
    val importsInFile = fileDTO.imports
    val cachedMethods = mutableMapOf<String, MethodDTO>()


    fun bind() {
        logger.debug("Working on binding file...");
        for (classDTO in classesInFile) {
            bindFieldsForClass(classDTO)
        }
    }

    private fun bindFieldsForClass(classDTO: ClassDTO) {
        logger.debug("Binding fields for class ${classDTO.className}")
        for (field in classDTO.classFields) {
            if (!field.isCollectionType()) {
                val visibleType = findSimpleAttributeType(field, classDTO)
                logger.debug("Field ${field.name} has type $visibleType")
            }
            // todo: add support for generics also
            // todo: must resolve the type after finding
        }
    }

    fun findSimpleAttributeType(attribute: AttributeDTO, classDTO: ClassDTO): String? {
        if (attribute.isSetByMethodCall && isNullOrEmpty(attribute.type)) {
            return findMethodCallReturnType(attribute.methodCallDTO!!, classDTO)
        } else {
            return attribute.type
        }
    }

    private fun isNullOrEmpty(type: String?): Boolean {
        return type.isNullOrEmpty() || type == "null"
    }

    private fun findAttributeType(attribute: AttributeDTO, classDTO: ClassDTO): String {
        val attributeType = attribute.type
        return "";
    }

    private fun findGenericAttributeType(attribute: AttributeDTO, classDTO: ClassDTO): List<String> {
        if (!attribute.isCollectionType()) {
            throw RuntimeException("Attribute ${attribute.name} is not a collection")
        }
        // todo: add support for generics
        val resolvedTypes: MutableList<String> = mutableListOf()
        return resolvedTypes
    }

    private fun findMethodCallReturnType(methodCallDTO: MethodCallDTO, classDTO: ClassDTO): String? {
        // first search if there is a reference for that method call
        val methodCallName = methodCallDTO.methodName
        val referenceName = methodCallDTO.referenceName
        var methodDTO: MethodDTO? = null
        if (referenceName == "mwWriter") {
            logger.debug("mwWriter reference found")
        }
        if (referenceName == null) {
            methodDTO = findMethodInsideClassOrImports(methodCallName, classDTO)
            if (methodDTO == null) {
                methodDTO = findConstructorInsideImportsOrSamePackage(methodCallName, classDTO.classPackage)
            }
        } else {
            methodDTO = findMethodAfterNameAndReference(methodCallName, referenceName, classDTO)
        }
        return methodDTO?.getMethodReturnType()
    }

    private fun findConstructorInsideImportsOrSamePackage(constructorName: String, classPackage: String?): MethodDTO? {
        for (import in importsInFile) {
            if (import.endsWith(constructorName)) {
                // search the files into the package of the import for the constructor
                val packageName = getPackageNameFromImport(import)
                val constructorDTO = findConstructorInPackage(constructorName, packageName)
                if (constructorDTO != null) {
                    return constructorDTO
                }
            }
        }
        // maybe the constructor is in the same package
        if (classPackage != null) { //todo: maybe in the future we should make sure that there are no matching import
            return findConstructorInPackage(constructorName, classPackage)
        }
        return null
    }

    private fun findConstructorInPackage(constructorName: String, packageName: String): MethodDTO? {
        val filesInPackage = FileController.getFilesWithSamePackageName(packageName)
        for (file in filesInPackage) {
            val constructorDTO = findConstructorAfterNameInsideClasses(constructorName, file.value.classes)
            if (constructorDTO != null) {
                return constructorDTO
            }
        }
        return null
    }

    private fun findConstructorAfterNameInsideClasses(
        constructorName: String,
        classesDTOs: List<ClassDTO>
    ): MethodDTO? {
        for (classDTO in classesDTOs) {
            // make an if and drop the search if it does not have the same name
            if (classDTO.className == constructorName) {
                val constructorDTO = findConstructorInsideClass(constructorName, classDTO)
                if (constructorDTO != null) {
                    return constructorDTO
                }
            }
        }
        return null
    }

    private fun findConstructorInsideClass(constructorName: String, classDTO: ClassDTO): MethodDTO? {
        // todo: this will be used in future for matching the parameters
        for (classConstructor in classDTO.getConstructors()) {
            if (classConstructor.methodName == constructorName) {
                return classConstructor
            }
        }
        return null
    }

    private fun findMethodAfterNameAndReference(
        methodCallName: String,
        referenceName: String,
        classDTO: ClassDTO
    ): MethodDTO? {
        // the reference name can be a field from the class
        val attributeDTO = searchFieldWithName(referenceName, classDTO)
        if (attributeDTO != null) {
            // find attribute class and search for the method
            val attributeClass = attributeDTO.type
            // build fully qualified name for the attribute class
            val fullyQualifiedNameOrClassName = findFullyQualifiedNameForClass(attributeClass, classDTO.classPackage) ?: attributeClass
            val attributeClassDTO =
                FileController.findClassInFiles(fullyQualifiedNameOrClassName) //todo: maybe it should be fully qualified name here
            if (attributeClassDTO != null) {
                return findMethodInsideClassOrImports(methodCallName, attributeClassDTO)
            }
        }
        return null
        // the reference name can be an import
        // todo: in future we should search for the method in the import
    }

    /**
     * Receives a simple type name and returns the fully qualified name
     * Example (for a declaration "val x: Zuzu = Zuzu()"):
     * - simpleTypeName = "Zuzu"
     * - returns "org.dxworks.kolekt.testpackage.Zuzu"
     */
    private fun findFullyQualifiedNameForClass(simpleClassName: String, classPackage: String?): String? {
        // check if the class name is already fully qualified
        if (simpleClassName.contains('.')) {
            return simpleClassName
        }
        // search for the class name in the imports
        var typeClass: ClassDTO? = searchClassInImports(simpleClassName)
        if (typeClass != null) {
            return typeClass.getFQN()
        }
        // search for the class name in the same package
        if (classPackage != null) {
            typeClass = findClassInPackage(simpleClassName, classPackage)
            if (typeClass != null) {
                return typeClass.getFQN()
            }
        }
        return null
    }

    private fun searchFieldWithName(referenceName: String, classDTO: ClassDTO): AttributeDTO? {
        for (field in classDTO.classFields) {
            if (field.name == referenceName) {
                return field
            }
        }
        return null
    }

    private fun findMethodInsideClassOrImports(methodCallName: String, classDTO: ClassDTO): MethodDTO? {
        // 1. the method can be inside the class
        val methodDTOs: MutableList<MethodDTO> = searchMethodsWithName(methodCallName, classDTO)
        if (methodDTOs.size != 0) {
            // todo: in future we should match the parameters too
            return methodDTOs[0]
        }
        // 2. the method can be imported
        val importedMethod: MethodDTO? = searchMethodInImports(methodCallName)
        if (importedMethod != null) {
            return importedMethod
        }
        return null
    }

    private fun searchClassInImports(className: String): ClassDTO? {
        // search the aliases
        val importNotAliased: String? = fileDTO.importAliases[className]
        if (importNotAliased != null) {
            return findClassInPackageUsingCleanImport(importNotAliased)
        }
        // search the imports
        for (import in importsInFile) {
            if (import.endsWith(className)) {
                // search the files into the package of the import for the class
                val packageName = getPackageNameFromImport(import)
                val classDTO = findClassInPackage(className, packageName)
                if (classDTO != null) {
                    return classDTO
                }
            }
        }
        return null
    }

    private fun searchMethodInImports(methodCallName: String): MethodDTO? {
        // search the aliases
        val importNotAliased: String? = fileDTO.importAliases[methodCallName]
        if (importNotAliased != null) {
            return findMethodInPackageUsingCleanImport(importNotAliased)
        }
        // search the imports
        for (import in importsInFile) {
            if (import.endsWith(methodCallName)) {
                val methodDTO = findMethodInPackageUsingCleanImport(import)
                if (methodDTO != null) {
                    return methodDTO
                }
            }
        }
        return null
    }

    private fun findClassInPackageUsingCleanImport(importNotAliased: String): ClassDTO? {
        val className = getMethodNameFromImport(importNotAliased)
        val packageName = getPackageNameFromImport(importNotAliased)
        return findClassInPackage(className, packageName)
    }

    private fun findMethodInPackageUsingCleanImport(import: String): MethodDTO? {
        val methodName = getMethodNameFromImport(import)
        val packageName = getPackageNameFromImport(import)
        return findMethodInPackage(methodName, packageName)
    }

    private fun getMethodNameFromImport(import: String): String {
        val lastDot = import.lastIndexOf('.')
        return import.substring(lastDot + 1)
    }

    private fun getPackageNameFromImport(import: String): String {
        val lastDot = import.lastIndexOf('.')
        return import.substring(0, lastDot)
    }

    private fun findClassInPackage(className: String, packageName: String): ClassDTO? {
        val classesInPackage = FileController.getFilesWithSamePackageName(packageName)
        for (file in classesInPackage) {
            // search inside the file classes
            for (classDTO in file.value.classes) {
                if (classDTO.className == className) {
                    return classDTO
                }
            }
        }
        return null
    }

    private fun findMethodInPackage(methodName: String, packageName: String): MethodDTO? {
        val classesInPackage = FileController.getFilesWithSamePackageName(packageName)
        for (file in classesInPackage) {
            // search inside the file functions
            for (function in file.value.functions) {
                if (function.methodName == methodName) {
                    // todo: in future we should match the parameters too
                    return function
                }
            }
        }
        return null
    }

    private fun searchMethodsWithName(methodName: String, classDTO: ClassDTO): MutableList<MethodDTO> {
        val methods: MutableList<MethodDTO> = mutableListOf()
        for (method in classDTO.classMethods) {
            if (method.methodName == methodName) {
                methods.add(method)
            }
        }
        return methods
    }

}
