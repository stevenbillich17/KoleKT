package org.dxworks.kolekt.extraction

import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.antlr.v4.runtime.tree.TerminalNode
import org.dxworks.kolekt.context.ParsingContext
import org.dxworks.kolekt.details.DictionariesController
import org.dxworks.kolekt.dtos.*
import org.dxworks.kolekt.enums.AttributeType
import org.dxworks.kolekt.listeners.FieldListener
import org.dxworks.kolekt.listeners.FunctionListener
import org.dxworks.kolekt.utils.ClassTypesUtils
import org.jetbrains.kotlin.spec.grammar.KotlinParser
import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener
import org.slf4j.LoggerFactory

class FileExtractionListener(private val pathToFile: String, private val name: String) : KotlinParserBaseListener() {
    private val fileDTO = FileDTO(pathToFile, name)
    private val parsingContext = ParsingContext()
    private val logger = LoggerFactory.getLogger(FileExtractionListener::class.java)
    
    override fun enterKotlinFile(ctx: KotlinParser.KotlinFileContext?) {
        ctx?.let {
            fileDTO.filePackage = it.packageHeader().identifier().text
        }
    }

    override fun enterPrimaryConstructor(ctx: KotlinParser.PrimaryConstructorContext?) {
        if (ctx == null) return
        parsingContext.insidePrimaryConstructor = true
    }

    override fun exitPrimaryConstructor(ctx: KotlinParser.PrimaryConstructorContext?) {
        if (ctx == null) return
        createAndAddConstructor()
        parsingContext.insidePrimaryConstructor = false
    }

    override fun enterSecondaryConstructor(ctx: KotlinParser.SecondaryConstructorContext?) {
        if (ctx == null) return
        parsingContext.insideSecondaryConstructor = true
    }

    override fun exitSecondaryConstructor(ctx: KotlinParser.SecondaryConstructorContext?) {
        if (ctx == null) return
        createAndAddConstructor()
        parsingContext.insideSecondaryConstructor = false
    }

    override fun enterClassParameter(ctx: KotlinParser.ClassParameterContext?) {
        if (ctx == null) return
        parsingContext.insideClassParameter = true
        logger.debug("Class parameter: ${ctx.text}")
    }

    override fun exitClassParameter(ctx: KotlinParser.ClassParameterContext?) {
        if (ctx == null) return
        addParameterForConstructor(ctx)
        parsingContext.insideClassParameter = false
    }

    override fun enterType(ctx: KotlinParser.TypeContext?) {
        if (ctx == null) return
        parsingContext.insideType = true
    }

    override fun exitType(ctx: KotlinParser.TypeContext?) {
        if (ctx == null) return
        parsingContext.insideType = false
    }



    override fun enterFunctionValueParameters(ctx: KotlinParser.FunctionValueParametersContext?) {
        if (ctx == null) return
        parsingContext.insideFunctionParameters = true
    }

    override fun exitFunctionValueParameters(ctx: KotlinParser.FunctionValueParametersContext?) {
        if (ctx == null) return
        parsingContext.insideFunctionParameters = false
    }

    override fun enterParameter(ctx: KotlinParser.ParameterContext?) {
        if (ctx == null) return
        addParameterForConstructor(ctx)
        parsingContext.insideParameter = true
    }

    override fun exitParameter(ctx: KotlinParser.ParameterContext?) {
        if (ctx == null) return
        parsingContext.insideParameter = false
    }

    override fun enterImportHeader(ctx: KotlinParser.ImportHeaderContext?) {
        ctx?.let {
            val fqnImport = it.identifier().text
            fileDTO.addImport(fqnImport)
            logger.debug("Import: ${it.identifier().text}")
            // todo: fix bug for AppDestroyer alias import inside MalwareDetector
            ctx.importAlias()?.let { importAlias ->
                fileDTO.addImportAlias(importAlias.simpleIdentifier().text, fqnImport)
            }
        }
    }

    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        if (ctx == null) return
        parsingContext.classDTO =  ClassDTO(ctx.simpleIdentifier().text)
        parsingContext.classDTO!!.classPackage = fileDTO.filePackage
        parsingContext.insideClassDeclaration = true
    }

    override fun exitClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
        ctx?.let {
            if (fileDTO.filePackage == null) {
                fileDTO.filePackage = "UNKNOWN"
            }

            // added primary constructor fields
            ctx.primaryConstructor()?.classParameters()?.classParameter()?.forEach { classParameter ->
                run {
                    val field = AttributeDTO(
                        classParameter.simpleIdentifier().text,
                        classParameter.type().text,
                        AttributeType.FIELD
                    )
                    parsingContext.classDTO!!.classFields.add(field)
                }
            }

            parsingContext.classDTO!!.classAnnotations.addAll(parsingContext.mutableListOfAnnotations)
            parsingContext.mutableListOfAnnotations.clear()

            parsingContext.classDTO!!.superClass = resolveImport(parsingContext.superClass)
            parsingContext.superClass = ""

            parsingContext.implementedInterfaces.forEach {
                parsingContext.classDTO!!.classInterfaces.add(resolveImport(it))
            }
            parsingContext.implementedInterfaces.clear()

            if (parsingContext.classDTO!!.getConstructors().size == 0) {
                // no declared constructor, add default constructor
                val defaultConstructor = MethodDTO(parsingContext.classDTO!!.className!!)
                defaultConstructor.setMethodReturnType(parsingContext.classDTO!!.getFQN())
                defaultConstructor.setParentClassDTO(parsingContext.classDTO!!)
                defaultConstructor.setParentFileDTO(fileDTO)
                parsingContext.classDTO!!.addConstructor(defaultConstructor)
            }

            parsingContext.classesDTOs.add(parsingContext.classDTO!!)
            DictionariesController.addClassDTO(parsingContext.classDTO!!)
            parsingContext.classDTO = null

        }
        parsingContext.insideClassDeclaration = false
    }

    override fun enterObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) {
        if (ctx == null) return
        parsingContext.classDTO =  ClassDTO(ctx.simpleIdentifier().text)
        parsingContext.classDTO!!.setToObjectType()
        parsingContext.classDTO!!.classPackage = fileDTO.filePackage
        parsingContext.insideClassDeclaration = true
    }

    override fun exitObjectDeclaration(ctx: KotlinParser.ObjectDeclarationContext?) {
        ctx?.let {
            if (fileDTO.filePackage == null) {
                fileDTO.filePackage = "UNKNOWN"
            }

            parsingContext.classDTO!!.classAnnotations.addAll(parsingContext.mutableListOfAnnotations)
            parsingContext.mutableListOfAnnotations.clear()

            parsingContext.classDTO!!.superClass = resolveImport(parsingContext.superClass)
            parsingContext.superClass = ""

            parsingContext.implementedInterfaces.forEach {
                parsingContext.classDTO!!.classInterfaces.add(resolveImport(it))
            }
            parsingContext.implementedInterfaces.clear()

            parsingContext.classesDTOs.add(parsingContext.classDTO!!)
            DictionariesController.addClassDTO(parsingContext.classDTO!!)
            parsingContext.classDTO = null

        }
        parsingContext.insideClassDeclaration = false
    }

    private fun resolveImport(shortName: String): String {
        if (ClassTypesUtils.isBasicType(shortName)) {
            return shortName
        }
        if (shortName == "") {
            return shortName
        }
        if (shortName.contains(".")) {
            // is already in form of package.Class
            return shortName
        }
        return fileDTO.getImport(shortName)
    }

    override fun enterAnnotatedDelegationSpecifier(ctx: KotlinParser.AnnotatedDelegationSpecifierContext?) {
        if (ctx == null) return
        parsingContext.insideAnnotatedDelegationSpecifier = true
    }

    override fun exitAnnotatedDelegationSpecifier(ctx: KotlinParser.AnnotatedDelegationSpecifierContext?) {
        if (ctx == null) return
        parsingContext.insideAnnotatedDelegationSpecifier = false
    }

    override fun enterConstructorInvocation(ctx: KotlinParser.ConstructorInvocationContext?) {
        if (ctx == null) return
        parsingContext.insideConstructorInvocation = true
    }

    override fun exitConstructorInvocation(ctx: KotlinParser.ConstructorInvocationContext?) {
        if (ctx == null) return
        parsingContext.insideConstructorInvocation = false
    }

    override fun enterSimpleIdentifier(ctx: KotlinParser.SimpleIdentifierContext?) {
        if (ctx == null) return
        parsingContext.insideSimpleIdentifier = true
        parsingContext.lastSimpleIdentifier = ctx.text
        if (checkForSuperClass()) {
            logger.trace("Super class: ${ctx.text}")
            parsingContext.superClass = ctx.text
        } else if (checkForInterface()) {
            logger.trace("Interface: ${ctx.text}")
            parsingContext.implementedInterfaces.add(ctx.text)
        }
    }

    override fun exitSimpleIdentifier(ctx: KotlinParser.SimpleIdentifierContext?) {
        if (ctx == null) return
        parsingContext.insideSimpleIdentifier = false
    }

    override fun enterSimpleUserType(ctx: KotlinParser.SimpleUserTypeContext?) {
        if (ctx == null) return
        parsingContext.insideSimpleUserType = true
    }

    override fun exitSimpleUserType(ctx: KotlinParser.SimpleUserTypeContext?) {
        if (ctx == null) return
        parsingContext.insideSimpleUserType = false
    }



    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if (ctx == null) return

        if (parsingContext.insideClassDeclaration) {
            // todo: should remove this when moving function parsing logic her
            return
        }
        parsingContext.insideFunctionDeclaration = true

        parseFunctionDeclaration(ctx)?.let { methodDTO ->
            fileDTO.functions.add(methodDTO)
        }
    }

    override fun exitFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
        if (ctx == null) return

        parsingContext.insideFunctionDeclaration = false
    }

    override fun enterFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        if (ctx == null) return

        parsingContext.insideFunctionBody = true
    }

    override fun exitFunctionBody(ctx: KotlinParser.FunctionBodyContext?) {
        if (ctx == null) return

        parsingContext.insideFunctionBody = false
    }

    override fun enterClassMemberDeclaration(ctx: KotlinParser.ClassMemberDeclarationContext?) {
        if (ctx == null) return
        parsingContext.insideClassMemberDeclaration = true
    }

    override fun enterModifier(ctx: KotlinParser.ModifierContext?) {
        if (ctx == null) return
        if (checkIfClassModifier()) {
            parsingContext.classDTO!!.addModifier(ctx.text)
        }
    }

    override fun exitClassMemberDeclaration(ctx: KotlinParser.ClassMemberDeclarationContext?) {
        if (ctx == null) return
        parsingContext.insideClassMemberDeclaration = false
    }

    override fun enterDeclaration(ctx: KotlinParser.DeclarationContext?) {
        if (ctx == null) return
        if (ctx.functionDeclaration() != null && checkIfClassMethod()) {
            parseFunctionDeclaration(ctx.functionDeclaration())?.let { methodDTO ->
                parsingContext.classDTO!!.classMethods.add(methodDTO)
            }
        } else if (ctx.propertyDeclaration() != null && checkIfClassField()) {
            parsePropertyDeclaration(ctx.propertyDeclaration())?.let { attributeDTO ->
                parsingContext.classDTO!!.classFields.add(attributeDTO)
            }
        }
        parsingContext.insideDeclaration = true
    }

    override fun enterClassBody(ctx: KotlinParser.ClassBodyContext?) {
        if (ctx == null) return
        parsingContext.insideClassBody = true
    }

    override fun exitClassBody(ctx: KotlinParser.ClassBodyContext?) {
        if (ctx == null) return
        parsingContext.insideClassBody = false
    }

    override fun enterUserType(ctx: KotlinParser.UserTypeContext?) {
        if (ctx == null) return
        parsingContext.insideUserType = true
        if (checkIfUserTypeIsForAnnotation()) {
            parsingContext.annotationName = ctx.text
        }
    }

    override fun exitUserType(ctx: KotlinParser.UserTypeContext?) {
        if (ctx == null) return
        parsingContext.insideUserType = false
    }

    override fun enterValueArgument(ctx: KotlinParser.ValueArgumentContext?) {
        if (ctx == null) return
        if (parsingContext.insideAnnotation) {
            parsingContext.annotationArguments.add(ctx.text)
        }
    }

    override fun enterAnnotation(ctx: KotlinParser.AnnotationContext?) {
        if (ctx == null ) return
        parsingContext.insideAnnotation = true
    }

    override fun exitAnnotation(ctx: KotlinParser.AnnotationContext?) {
        if (ctx == null ) return
        parsingContext.insideAnnotation = false
    }

    override fun enterSingleAnnotation(ctx: KotlinParser.SingleAnnotationContext?) {
        if (ctx == null ) return
        parsingContext.insideSingleAnnotation = true
    }

    override fun exitSingleAnnotation(ctx: KotlinParser.SingleAnnotationContext?) {
        if (ctx == null ) return
        parsingContext.insideSingleAnnotation = false
        if (checkIfClassAnnotation()) {
            val singleAnnotation = AnnotationDTO(parsingContext.annotationName)
            singleAnnotation.addAnnotationArguments(parsingContext.annotationArguments)
            parsingContext.annotationArguments.clear()
            logger.debug("Adding class annotation: {}", singleAnnotation)
            parsingContext.mutableListOfAnnotations.add(singleAnnotation)
        }
    }

    override fun visitTerminal(node: TerminalNode?) {
        if (node == null) return
        if (checkIfInterfaceDeclaration()) {
            if (node.text == "interface") {
                parsingContext.classDTO!!.setToInterfaceType()
            }
        }
    }

    private fun checkIfInsideConstructor(): Boolean {
        return  parsingContext.insidePrimaryConstructor || parsingContext.insideSecondaryConstructor
    }

    private fun checkIfInterfaceDeclaration(): Boolean {
        return parsingContext.insideClassDeclaration
                && !parsingContext.insideClassBody
    }


    private fun checkForSuperClass(): Boolean {
        return insideClassDeclarationButOutsideBody()
                && parsingContext.insideAnnotatedDelegationSpecifier
                && parsingContext.insideConstructorInvocation
    }

    private fun checkForInterface(): Boolean {
        return insideClassDeclarationButOutsideBody()
                && parsingContext.insideAnnotatedDelegationSpecifier
                && parsingContext.insideSimpleUserType
    }

    private fun checkIfClassModifier(): Boolean {
        return insideClassDeclarationButOutsideBody()
    }

    private fun checkIfClassAnnotation(): Boolean {
        return insideClassDeclarationButOutsideBody()
    }

    private fun insideClassDeclarationButOutsideBody(): Boolean {
        return parsingContext.insideClassDeclaration
                && !parsingContext.insideClassBody
    }

    private fun checkIfUserTypeIsForAnnotation(): Boolean {
        return parsingContext.insideSingleAnnotation
    }
    private fun checkIfClassField(): Boolean {
        return parsingContext.insideClassMemberDeclaration
                && !parsingContext.insideFieldDeclaration
                && !parsingContext.insideFunctionDeclaration
                && !parsingContext.insideFunctionBody
    }

    private fun checkIfClassMethod(): Boolean {
        return  parsingContext.insideClassMemberDeclaration
                && !parsingContext.insideFunctionDeclaration
    }

    override fun exitDeclaration(ctx: KotlinParser.DeclarationContext?) {
        if (ctx == null) return
        parsingContext.insideDeclaration = false
    }


    private fun addParameterForConstructor(ctx: ParserRuleContext) {
        if (checkIfInsideConstructor()) {
            val variableName = ctx.getChild(KotlinParser.SimpleIdentifierContext::class.java, 0).text.trim()
            val variableType = ctx.getChild(KotlinParser.TypeContext::class.java, 0).text.trim()
            val resolvedVariableType = resolveImport(variableType)
            val parameter = AttributeDTO(variableName, resolvedVariableType, AttributeType.PARAMETER)
            parsingContext.parametersForConstructor.add(parameter)
            logger.debug("Found constructor parameter: $variableName with type $variableType")
        }
    }

    private fun createAndAddConstructor() {
        val methodDTO = parsingContext.classDTO!!.className?.let { MethodDTO(it) }
        methodDTO?.let {
            methodDTO.methodParameters.addAll(parsingContext.parametersForConstructor)
            parsingContext.parametersForConstructor.clear()
            methodDTO.setMethodReturnType(parsingContext.classDTO!!.getFQN())
            methodDTO.setParentClassDTO(parsingContext.classDTO!!)
            methodDTO.setParentFileDTO(fileDTO)
            parsingContext.classDTO!!.addConstructor(methodDTO)
        }
    }

    private fun parsePropertyDeclaration(propertyDeclaration: KotlinParser.PropertyDeclarationContext): AttributeDTO? {
        parsingContext.insideFieldDeclaration = true
        val parserTreeWalker = ParseTreeWalker()
        val functionListener = FieldListener()
        parserTreeWalker.walk(functionListener, propertyDeclaration)
        parsingContext.insideFieldDeclaration = false
        return functionListener.attributeDTO
    }

    private fun parseFunctionDeclaration(functionDeclaration: KotlinParser.FunctionDeclarationContext): MethodDTO? {
        parsingContext.insideFunctionDeclaration = true
        val parserTreeWalker = ParseTreeWalker()
        val functionListener = FunctionListener()
        if (parsingContext.insideClassBody) {
            functionListener.classDTO = parsingContext.classDTO
        }
        functionListener.fileDTO = fileDTO
        parserTreeWalker.walk(functionListener, functionDeclaration)
        parsingContext.insideFunctionDeclaration = false
        return functionListener.methodDTO
    }

    fun getFileDTO(): FileDTO {
        return fileDTO
    }

    fun getClassesDTOs(): List<ClassDTO> {
        return parsingContext.classesDTOs
    }
}