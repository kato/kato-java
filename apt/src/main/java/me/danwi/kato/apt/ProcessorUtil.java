package me.danwi.kato.apt;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

public class ProcessorUtil {
    private final ProcessingEnvironment env;
    private final Types types;
    private final Elements elements;

    public ProcessorUtil(ProcessingEnvironment env) {
        this.env = env;
        this.types = env.getTypeUtils();
        this.elements = env.getElementUtils();
    }

    public String getQualifiedName(TypeMirror type) {
        return getQualifiedName(types.asElement(type));
    }

    public String getQualifiedName(Element element) {
        if (element instanceof QualifiedNameable) {
            return ((QualifiedNameable) element).getQualifiedName().toString();
        }
        return element.toString();
    }

    public Element getSuperClass(TypeMirror type) {
        return getSuperClass(types.asElement(type));
    }

    public PackageElement getPackageElement(Element element) {
        if (element == null)
            return null;
        if (element instanceof PackageElement)
            return (PackageElement) element;
        return getPackageElement(element.getEnclosingElement());
    }

    public boolean isGetter(Element element) {
        if (!(element instanceof ExecutableElement))
            return false;
        ExecutableElement executableElement = (ExecutableElement) element;
        return (executableElement.getSimpleName().toString().startsWith("get")
                || executableElement.getSimpleName().toString().startsWith("is"))
                && executableElement.getParameters().isEmpty();
    }

    public Element getSuperClass(Element element) {
        List<? extends TypeMirror> supertypes = this.types.directSupertypes(element.asType());
        if (supertypes.isEmpty())
            return null;
        return this.types.asElement(supertypes.get(0));
    }
}
