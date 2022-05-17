package me.danwi.kato.kapt

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.chhorz.javadoc.JavaDocParserBuilder
import com.github.chhorz.javadoc.OutputType
import com.google.devtools.ksp.*
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.visitor.KSTopDownVisitor
import me.danwi.kato.apt.ProcessorUtil
import me.danwi.kato.apt.model.ClassDoc
import me.danwi.kato.apt.model.MethodDoc
import me.danwi.kato.apt.model.PropertyDoc

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
        //构造ClassDoc
        val classDoc = ClassDoc(classDeclaration.docString)
        //方法
        classDoc.methodDocs = classDeclaration.getDeclaredFunctions()
            .filter { it.isPublic() && !it.isConstructor() && !it.isInternal() && !it.isAbstract }
            .filter { ProcessorUtil.getterNameToPropertyName(it.simpleName.asString()) == null }
            .map { generateMethodDoc(it) }
            .toList().toTypedArray()
        //属性Getter方法
        val getterFunctions = classDeclaration.getDeclaredFunctions()
            .filter { it.isPublic() && !it.isConstructor() && !it.isInternal() && !it.isAbstract }
            .filter { ProcessorUtil.getterNameToPropertyName(it.simpleName.asString()) != null }
            .map { generatePropertyDoc(it) }
        //kotlin中的属性
        val properties = classDeclaration.getDeclaredProperties()
            .filter { it.isPublic() && !it.isInternal() }
            .map { generatePropertyDoc(it) }
        classDoc.propertyDocs =
            (classDoc.propertyDocs.asSequence() + getterFunctions + properties).toList().toTypedArray()
        return classDoc
    }

    private fun generateMethodDoc(functionDeclaration: KSFunctionDeclaration): MethodDoc {
        val methodDoc = MethodDoc(functionDeclaration.docString)
        methodDoc.name = functionDeclaration.simpleName.asString()
        return methodDoc
    }

    private fun generatePropertyDoc(functionDeclaration: KSFunctionDeclaration): PropertyDoc {
        //解析java doc
        val javaDocParser = JavaDocParserBuilder.withBasicTags().withOutputType(OutputType.PLAIN).build()
        val javaDoc = javaDocParser.parse(functionDeclaration.docString)
        //构造PropertyDoc
        return PropertyDoc(
            ProcessorUtil.getterNameToPropertyName(functionDeclaration.simpleName.asString()),
            javaDoc.description
        )
    }

    private fun generatePropertyDoc(propertyDeclaration: KSPropertyDeclaration): PropertyDoc {
        //解析java doc
        val javaDocParser = JavaDocParserBuilder.withBasicTags().withOutputType(OutputType.PLAIN).build()
        val javaDoc = javaDocParser.parse(propertyDeclaration.docString)
        //构造PropertyDoc
        return PropertyDoc(
            propertyDeclaration.simpleName.asString(),
            javaDoc.description
        )
    }
}

class KotlinDocProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return KotlinDocProcessor(environment.codeGenerator)
    }
}