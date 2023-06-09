package com.bachlinh.order.annotation.processor.parser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import com.bachlinh.order.annotation.ActiveReflection;
import com.bachlinh.order.annotation.DtoProxy;
import com.bachlinh.order.annotation.processor.meta.FieldMeta;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

class DtoProxyClassMetadataParser implements ClassMetadataParser {
    private final Element element;
    private final String packageName;
    private final TypeElement delegateType;

    DtoProxyClassMetadataParser(Element element, Elements elements, TypeElement delegateType) {
        this.element = element;
        this.packageName = elements.getPackageOf(element).getQualifiedName().toString();
        this.delegateType = delegateType;
    }


    @Override
    public String getPackage() {
        return packageName;
    }

    @Override
    public Collection<String> getImports() {
        var importCollection = new ArrayList<String>();
        importCollection.add(JsonIgnore.class.getName());
        importCollection.add(JsonProperty.class.getName());
        importCollection.add(ActiveReflection.class.getName());
        importCollection.add(DtoProxy.class.getName());
        importCollection.add("com.bachlinh.order.dto.proxy.Proxy");
        importCollection.add(delegateType.getQualifiedName().toString());
        return importCollection;
    }

    @Override
    public String getClassName() {
        var template = "{0}.{1}{2}";
        var entityClassName = this.element.getSimpleName();
        return MessageFormat.format(template, getPackage(), entityClassName, "Proxy");
    }

    @Override
    public FieldMeta getField() {
        return new FieldMeta(this.delegateType.getSimpleName(), "delegate");
    }
}
