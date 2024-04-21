//package org.dxworks.kolekt.binders
//
//import org.dxworks.kolekt.dtos.AttributeDTO
//import org.dxworks.kolekt.dtos.ClassDTO
//import org.dxworks.kolekt.dtos.FileDTO
//import org.dxworks.kolekt.utils.ClassTypesUtils
//
//class FieldBinder(val field: AttributeDTO, classDTO: ClassDTO, file: FileDTO) {
//
//    fun bind() {
//        val fieldVisibleType  = findVisibleType()
//    }
//
//
//}

/*
ClassDTO(
 className='TestClass',
 classPackage='org.dxworks.kolekt.testpackage',
 superClass='',
 imports=
    org.dxworks.kolekt.testpackage.malware.MalwareWriter
    org.dxworks.kolekt.testpackage.malware.testMalwareOutside
    org.dxworks.kolekt.testpackage.malware.writeMalwareOutside
    org.dxworks.kolekt.testpackage.malware.testMalwareOutside
    ,
 importsAliases=
    aliasTestMalware -> org.dxworks.kolekt.testpackage.malware.testMalwareOutside
    ,
 classSubClassesNames=
    ,
 classType=CLASS,
 classInterfaces=(
    ),
 classModifiers=(
    ),
 classAnnotations=[],
 classConstructors=[
  {
   MethodDTO(
   methodName='TestClass',
   methodReturnType='org.dxworks.kolekt.testpackage.TestClass',
   methodModifiers=(
    ),
   methodParameters=(
    AttributeDTO(name='age', type='Int', attributeType='PARAMETER', isCollection='false', attributeModifiers=())
    AttributeDTO(name='name', type='String', attributeType='PARAMETER', isCollection='false', attributeModifiers=())
    ),
   methodLocalVariables=(
    ),
   calls=(
    )
   annotations=(
    )
   },
  {
   MethodDTO(
   methodName='TestClass',
   methodReturnType='org.dxworks.kolekt.testpackage.TestClass',
   methodModifiers=(
    ),
   methodParameters=(
    AttributeDTO(name='address', type='String', attributeType='PARAMETER', isCollection='false', attributeModifiers=())
    AttributeDTO(name='nothing', type='Int', attributeType='PARAMETER', isCollection='false', attributeModifiers=())
    AttributeDTO(name='name', type='String', attributeType='PARAMETER', isCollection='false', attributeModifiers=())
    AttributeDTO(name='anotherInt', type='Int', attributeType='PARAMETER', isCollection='false', attributeModifiers=())
    ),
   methodLocalVariables=(
    ),
   calls=(
    )
   annotations=(
    )
   }],
 classMethods=[
  {
   MethodDTO(
   methodName='getWeightValue',
   methodReturnType='Double',
   methodModifiers=(
    PRIVATE
    ),
   methodParameters=(
    ),
   methodLocalVariables=(
    ),
   calls=(
    )
   annotations=(
    )
   },
  {
   MethodDTO(
   methodName='callOutsideFunction',
   methodReturnType='Void',
   methodModifiers=(
    ),
   methodParameters=(
    AttributeDTO(name='message', type='String', attributeType='PARAMETER', isCollection='false', attributeModifiers=())
    ),
   methodLocalVariables=(
    ),
   calls=(
    MethodCallDTO(methodName='outsideFunction', parameters=[], referenceName=null)
    )
   annotations=(
       AnnotationDTO(annotationName='Deprecated', annotationParameters=["Use the other constructor"])
    )
   },
  {
   MethodDTO(
   methodName='testReturn',
   methodReturnType='MalwareWriter',
   methodModifiers=(
    ),
   methodParameters=(
    ),
   methodLocalVariables=(
    ),
   calls=(
    )
   annotations=(
    )
   },
  {
   MethodDTO(
   methodName='fun2',
   methodReturnType='Void',
   methodModifiers=(
    ),
   methodParameters=(
    AttributeDTO(name='m1', type='String', attributeType='PARAMETER', isCollection='false', attributeModifiers=())
    AttributeDTO(name='m2', type='Double?', attributeType='PARAMETER', isCollection='false', attributeModifiers=())
    ),
   methodLocalVariables=(
    AttributeDTO(name='x', type='String', attributeType='LOCAL_VARIABLE', isCollection='false', attributeModifiers=())
    AttributeDTO(name='xTurbat', type='Integer', attributeType='LOCAL_VARIABLE', isCollection='false', attributeModifiers=())
    AttributeDTO(name='y', type='String?', attributeType='LOCAL_VARIABLE', isCollection='false', attributeModifiers=())
    AttributeDTO(name='z', type='null', attributeType='LOCAL_VARIABLE', isCollection='false', methodCallDTO=MethodCallDTO(methodName='MalwareWriter', parameters=[], referenceName=null), attributeModifiers=())
    AttributeDTO(name='cpyMwWriter', type='null', attributeType='LOCAL_VARIABLE', isCollection='false', methodCallDTO=MethodCallDTO(methodName='testReturn', parameters=[], referenceName=null), attributeModifiers=())
    ),
   calls=(
    MethodCallDTO(methodName='MalwareWriter', parameters=[], referenceName=null)
    MethodCallDTO(methodName='testReturn', parameters=[], referenceName=null)
    MethodCallDTO(methodName='writeMalware', parameters=[], referenceName=z)
    MethodCallDTO(methodName='writeMalwareWithParameters', parameters=["ceva", "altceva"], referenceName=z)
    MethodCallDTO(methodName='writeMalwareOutside', parameters=["ceva"+"wow", "altceva"], referenceName=null)
    )
   annotations=(
    )
   }],
 classFields=(
    AttributeDTO(name='address', type='String', attributeType='FIELD', isCollection='false', attributeModifiers=(PRIVATE, ))
    AttributeDTO(name='phoneNumber', type='String', attributeType='FIELD', isCollection='false', attributeModifiers=(PROTECTED, ))
    AttributeDTO(name='height', type='Double', attributeType='FIELD', isCollection='false', attributeModifiers=())
    AttributeDTO(name='mwWriter', type='MalwareWriter', attributeType='FIELD', isCollection='false', methodCallDTO=MethodCallDTO(methodName='MalwareWriter', parameters=[], referenceName=null), attributeModifiers=())
    AttributeDTO(name='counter', type='', attributeType='FIELD', isCollection='false', methodCallDTO=MethodCallDTO(methodName='initializeInt', parameters=[], referenceName=mwWriter), attributeModifiers=())
    AttributeDTO(name='amazingMalware', type='', attributeType='FIELD', isCollection='false', methodCallDTO=MethodCallDTO(methodName='makeCoolStuff', parameters=[], referenceName=mwWriter), attributeModifiers=())
    AttributeDTO(name='weight', type='', attributeType='FIELD', isCollection='false', methodCallDTO=MethodCallDTO(methodName='getWeightValue', parameters=[], referenceName=null), attributeModifiers=())
    AttributeDTO(name='weight2', type='', attributeType='FIELD', isCollection='false', methodCallDTO=MethodCallDTO(methodName='aliasTestMalware', parameters=[], referenceName=null), attributeModifiers=())
    AttributeDTO(name='weight3', type='String', attributeType='FIELD', isCollection='false', methodCallDTO=MethodCallDTO(methodName='testMalwareOutside', parameters=[], referenceName=null), attributeModifiers=())
    AttributeDTO(name='weight4', type='', attributeType='FIELD', isCollection='false', methodCallDTO=MethodCallDTO(methodName='testMalwareOutside', parameters=[], referenceName=null), attributeModifiers=())
    AttributeDTO(name='age', type='Int', attributeType='FIELD', isCollection='false', attributeModifiers=())
    AttributeDTO(name='name', type='String', attributeType='FIELD', isCollection='false', attributeModifiers=())
    )
)}
 */