package me.danwi.kato.kapt

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import me.danwi.kato.apt.model.ClassDoc

class KotlinDocProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    private val mapper = ObjectMapper()

    private val KSClassDeclaration.javaClassName: String
        get() {
            val parentClass = parentDeclaration?.closestClassDeclaration()
            return if (parentClass == null)
                simpleName.asString()
            else
                "${parentClass.javaClassName}\$${simpleName.asString()}"

        }


    override fun process(resolver: Resolver): List<KSAnnotated> {
        resolver.getAllFiles().forEach {
            it.accept(object : KSTopDownVisitor<Unit, Unit>() {
                override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
                    super.visitClassDeclaration(classDeclaration, data)
                    val sourceFile = classDeclaration.containingFile ?: return
                    try {
                        val kotlinDocFile = codeGenerator.createNewFile(
                            Dependencies(false, sourceFile),
                            classDeclaration.packageName.asString(),
                            classDeclaration.javaClassName,
                            "javadoc.json"
                        )
                        kotlinDocFile.use { s ->
                            val classDoc = generateClassDoc(classDeclaration)
                            s.write(mapper.writeValueAsBytes(classDoc))
                        }
                    } catch (_: Exception) {
                    }
                }

                override fun defaultHandler(node: KSNode, data: Unit) {}
            }, Unit)
        }
        return emptyList()
    }

    fun generateClassDoc(classDeclaration: KSClassDeclaration): ClassDoc {
        val classDoc = ClassDoc()
        classDoc.description = classDeclaration.docString
        return classDoc
    }
}

class KotlinDocProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KotlinDocProcessor(environment.codeGenerator)
    }
}