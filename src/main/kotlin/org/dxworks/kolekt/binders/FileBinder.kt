package org.dxworks.kolekt.binders

import org.dxworks.kolekt.details.FileController
import org.dxworks.kolekt.dtos.*
import org.dxworks.kolekt.utils.ClassTypesUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class FileBinder(private val fileDTO: FileDTO) {
    val logger: Logger = LoggerFactory.getLogger("FileBinder@${fileDTO.fileName}")


    fun bind() {
        for (classDTO in fileDTO.classes) {
            bindFieldsForClass(classDTO)
        }
    }

    private fun bindFieldsForClass(classDTO: ClassDTO) {
        logger.debug("\nBinding fields for class ${classDTO.className}")
        for (field in classDTO.classFields) {
            if (!field.isCollectionType()) {
                val type: String? = findAttributeType(field, classDTO, fileDTO)
                if (type != null) {
                    field.type = type
                }
                logger.debug("Field ${field.name} has type ${field.type}")
            } else {
                // todo: it is possible here that we already got the types (ex: List<Zuzu> => Zuzu)
                val types: List<String> = findGenericAttributeType(field, classDTO, fileDTO)
                if (types.isNotEmpty()) {
                    field.collectionType = types
                }
                logger.debug("Field {} has types {}", field.name, types)
            }
        }
    }

    fun findAttributeType(attribute: AttributeDTO, classDTO: ClassDTO, fileDTO: FileDTO): String? {
        // finding the simple type
        var type: String? = null
        if (attribute.isSetByMethodCall && isNullOrEmpty(attribute.type)) {
            val methodDTO: MethodDTO? = findMethodCall(attribute.methodCallDTO!!, classDTO, fileDTO)
            if (methodDTO != null) {
                type = findMethodReturnType(methodDTO)
            }
        } else {
            type = attribute.type
        }
        // solving the fully qualified name
        if (type != null) {
            val typeFQN: String? = findFullyQualifiedNameForClass(type, classDTO.classPackage, fileDTO)
            if (typeFQN != null) {
                return typeFQN
            }
        }
        return type
    }

    private fun findMethodReturnType(methodDTO: MethodDTO): String {
        val methodReturnType = methodDTO.getMethodReturnType()
        if (ClassTypesUtils.isBasicType(methodReturnType)) {
            return methodReturnType
        }

        if (methodDTO.getParentFileDTO() == null) {
            throw RuntimeException("Parent file not found for method ${methodDTO.methodName}")
        }
        if (methodDTO.getParenClassDTO() != null) {
            val methodReturnTypeFQN = findFullyQualifiedNameForClass(
                methodReturnType,
                methodDTO.getParenClassDTO()!!.classPackage,
                methodDTO.getParentFileDTO()!!
            )
            if (methodReturnTypeFQN != null) {
                return methodReturnTypeFQN
            }
        }
        // todo: add support for the moment when no parent class file is found (FILE SHOULD BE FOUND)
        return methodReturnType
    }

    private fun findGenericAttributeType(attribute: AttributeDTO, classDTO: ClassDTO, fileDTO: FileDTO): List<String> {
        val types: List<String>? = attribute.collectionType
        if (!attribute.isCollectionType() || types == null) {
            throw RuntimeException("Attribute ${attribute.name} is not a collection")
        }
        val typesFQN: MutableList<String> = mutableListOf()
        for (type in types) {
            val typeFQN: String? = findFullyQualifiedNameForClass(type, classDTO.classPackage, fileDTO)
            if (typeFQN != null) {
                typesFQN.add(typeFQN)
            } else {
                typesFQN.add(type)
            }
        }
        return typesFQN
    }

    private fun isNullOrEmpty(type: String?): Boolean {
        return type.isNullOrEmpty() || type == "null"
    }

    private fun findMethodCall(methodCallDTO: MethodCallDTO, classDTO: ClassDTO, fileDTO: FileDTO): MethodDTO? {
        // first search if there is a reference for that method call
        val methodCallName = methodCallDTO.methodName
        val referenceName = methodCallDTO.referenceName
        var methodDTO: MethodDTO? = null
        if (referenceName == null) {
            methodDTO = findMethodInsideClassOrImports(methodCallName, classDTO, fileDTO)
            if (methodDTO == null) {
                methodDTO = findConstructorInsideImportsOrSamePackage(methodCallName, classDTO.classPackage, fileDTO)
            }
        } else {
            methodDTO = findMethodAfterNameAndReference(methodCallName, referenceName, classDTO, fileDTO)
        }
        if (methodCallDTO.methodName == "makeCoolStuff") {
            logger.debug("Jere")
        }
        return methodDTO
    }

    private fun findConstructorInsideImportsOrSamePackage(
        constructorName: String,
        classPackage: String?,
        fileDTO: FileDTO
    ): MethodDTO? {
        for (import in fileDTO.imports) {
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
        classDTO: ClassDTO,
        fileDTO: FileDTO
    ): MethodDTO? {
        // the reference name can be a field from the class
        val attributeDTO = searchFieldWithName(referenceName, classDTO)
        if (attributeDTO != null) {
            // find attribute class and search for the method
            val attributeClass = attributeDTO.type
            // build fully qualified name for the attribute class
            val fullyQualifiedNameOrClassName =
                findFullyQualifiedNameForClass(attributeClass, classDTO.classPackage, fileDTO) ?: attributeClass
            val attributeClassDTO =
                FileController.findClassInFiles(fullyQualifiedNameOrClassName) //todo: maybe it should be fully qualified name here (done)
            if (attributeClassDTO != null) {
                return findMethodInsideClassOrImports(methodCallName, attributeClassDTO, fileDTO)
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
    private fun findFullyQualifiedNameForClass(
        targetClassName: String,
        currentClassPackage: String?,
        currentFileDTO: FileDTO
    ): String? {
        // check if the class name is already fully qualified
        if (targetClassName.contains('.')) {
            return targetClassName
        }
        // search for the class name in the imports
        var typeClass: ClassDTO? = searchClassInImports(targetClassName, currentFileDTO)
        if (typeClass != null) {
            return typeClass.getFQN()
        }
        // search for the class name in the same package
        if (currentClassPackage != null) {
            typeClass = findClassInPackage(targetClassName, currentClassPackage)
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

    private fun findMethodInsideClassOrImports(
        methodCallName: String,
        classDTO: ClassDTO,
        fileDTO: FileDTO
    ): MethodDTO? {
        // 1. the method can be inside the class
        val methodDTOs: MutableList<MethodDTO> = searchMethodsWithName(methodCallName, classDTO)
        if (methodDTOs.size != 0) {
            // todo: in future we should match the parameters too
            return methodDTOs[0]
        }
        // 2. the method can be imported
        val importedMethod: MethodDTO? = searchMethodInImports(methodCallName, fileDTO)
        if (importedMethod != null) {
            return importedMethod
        }
        return null
    }

    private fun searchClassInImports(className: String, fileDTO: FileDTO): ClassDTO? {
        // search the aliases
        val importNotAliased: String? = fileDTO.importAliases[className]
        if (importNotAliased != null) {
            return findClassInPackageUsingCleanImport(importNotAliased)
        }
        // search the imports
        for (import in fileDTO.imports) {
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

    private fun searchMethodInImports(methodCallName: String, fileDTO: FileDTO): MethodDTO? {
        // search the aliases
        val importNotAliased: String? = fileDTO.importAliases[methodCallName]
        if (importNotAliased != null) {
            return findMethodInPackageUsingCleanImport(importNotAliased)
        }
        // search the imports
        for (import in fileDTO.imports) {
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
