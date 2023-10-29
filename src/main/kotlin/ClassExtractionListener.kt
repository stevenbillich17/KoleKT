//import DTO.ClassDTO
//import org.jetbrains.kotlin.spec.grammar.KotlinParser
//import org.jetbrains.kotlin.spec.grammar.KotlinParserBaseListener
//
//class ClassExtractionListener(val classDTO: ClassDTO): KotlinParserBaseListener() {
//    override fun enterFunctionDeclaration(ctx: KotlinParser.FunctionDeclarationContext?) {
//        ctx?.let {
//            println("Function name: {${it.simpleIdentifier().text}}")
//        }
//    }
//
//    override fun enterClassMemberDeclaration(ctx: KotlinParser.ClassMemberDeclarationContext?) {
//        ctx?.let {
//            println("Class member name: {${it.text}}")
//        }
//    }
//
//    override fun enterClassDeclaration(ctx: KotlinParser.ClassDeclarationContext?) {
//        ctx?.let {
//            println("Parsing class: ${classDTO.className}")
//
//        }
//    }
//}