/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.build.docs.dsl.docbook

import org.gradle.build.docs.dsl.XmlSpecification
import org.gradle.build.docs.dsl.model.*

class ClassDocTest extends XmlSpecification {
    final JavadocConverter javadocConverter = Mock()
    final DslDocModel docModel = Mock()

    def buildsPropertiesForClass() {
        ClassMetaData classMetaData = classMetaData()
        PropertyMetaData propertyA = property('a', classMetaData, comment: 'prop a')
        PropertyMetaData propertyB = property('b', classMetaData, comment: 'prop b')
        ClassDoc superDoc = classDoc()
        PropertyDoc propertyDocA = propertyDoc('a')
        PropertyDoc propertyDocC = propertyDoc('c')

        def content = parse('''
<section>
    <section><title>Properties</title>
        <table>
            <thead><tr><td>Name</td></tr></thead>
            <tr><td>b</td></tr>
            <tr><td>a</td></tr>
        </table>
    </section>
    <section><title>Methods</title><table><thead><tr></tr></thead></table></section>
</section>
''')

        when:
        ClassDoc doc = withCategories {
            new ClassDoc('org.gradle.Class', content, document, classMetaData, null, docModel, javadocConverter).buildProperties()
        }

        then:
        doc.classProperties.size() == 3
        doc.classProperties[0].name == 'a'
        doc.classProperties[1].name == 'b'
        doc.classProperties[2] == propertyDocC

        _ * classMetaData.findProperty('b') >> propertyB
        _ * classMetaData.findProperty('a') >> propertyA
        _ * classMetaData.superClassName >> 'org.gradle.SuperType'
        _ * docModel.getClassDoc('org.gradle.SuperType') >> superDoc
        _ * superDoc.getClassProperties() >> [propertyDocC, propertyDocA]
    }

    def buildsMethodsForClass() {
        ClassMetaData classMetaData = classMetaData()
        MethodMetaData methodA = method('a', classMetaData)
        MethodMetaData methodB = method('b', classMetaData)
        MethodMetaData methodBOverload = method('b', classMetaData)
        MethodDoc methodAOverridden = methodDoc('a')
        MethodDoc methodC = methodDoc('c')
        ClassDoc superClass = classDoc('org.gradle.SuperClass')

        def content = parse('''
<section>
    <section><title>Methods</title>
        <table>
            <thead><tr><td>Name</td></tr></thead>
            <tr><td>a</td></tr>
            <tr><td>b</td></tr>
        </table>
    </section>
    <section><title>Properties</title><table><thead><tr>Name</tr></thead></table></section>
</section>
''')

        when:
        ClassDoc doc = withCategories {
            new ClassDoc('org.gradle.Class', content, document, classMetaData, null, docModel, javadocConverter).buildMethods()
        }

        then:
        doc.classMethods.size() == 4

        doc.classMethods[0].name == 'a'
        doc.classMethods[1].name == 'b'
        doc.classMethods[2].name == 'b'
        doc.classMethods[3].name == 'c'

        _ * classMetaData.declaredMethods >> ([methodA, methodB, methodBOverload] as Set)
        _ * classMetaData.superClassName >> 'org.gradle.SuperClass'
        _ * docModel.getClassDoc('org.gradle.SuperClass') >> superClass
        _ * superClass.classMethods >> [methodC, methodAOverridden]
    }

    def buildsBlocksForClass() {
        ClassMetaData classMetaData = classMetaData()
        PropertyMetaData blockProperty = property('block', classMetaData)
        MethodMetaData blockMethod = method('block', classMetaData, paramTypes: [Closure.class.name])
        MethodMetaData tooManyParams = method('block', classMetaData, paramTypes: ['String', 'boolean'])
        MethodMetaData notAClosure = method('block', classMetaData, paramTypes: ['String'])
        MethodMetaData noBlockProperty = method('notBlock', classMetaData, paramTypes: [Closure.class.name])

        def content = parse('''
<section>
    <section><title>Methods</title>
        <table>
            <thead><tr><td>Name</td></tr></thead>
            <tr><td>block</td></tr>
            <tr><td>notBlock</td></tr>
        </table>
    </section>
    <section><title>Properties</title>
        <table>
            <thead><tr>Name</tr></thead>
            <tr><td>block</td></tr>
        </table>
    </section>
</section>
''')

        when:
        ClassDoc doc = withCategories {
            new ClassDoc('org.gradle.Class', content, document, classMetaData, null, docModel, javadocConverter).buildProperties().buildMethods()
        }

        then:
        doc.classProperties.size() == 1
        doc.classProperties[0].name == 'block'

        doc.classMethods.size() == 3

        doc.classBlocks.size() == 1
        doc.classBlocks[0].name == 'block'

        _ * classMetaData.findProperty('block') >> blockProperty
        _ * classMetaData.declaredMethods >> [blockMethod, tooManyParams, notAClosure, noBlockProperty]
    }

    def buildsExtensionsForClass(){
        ClassMetaData classMetaData = classMetaData()
        ExtensionMetaData extensionMetaData = new ExtensionMetaData('org.gradle.Class')
        extensionMetaData.add('a', 'org.gradle.ExtensionA1')
        extensionMetaData.add('a', 'org.gradle.ExtensionA2')
        extensionMetaData.add('b', 'org.gradle.ExtensionB')
        ClassDoc extensionA1 = classDoc('org.gradle.ExtensionA1')
        ClassDoc extensionA2 = classDoc('org.gradle.ExtensionA2')
        ClassDoc extensionB = classDoc('org.gradle.ExtensionB')
        _ * docModel.getClassDoc('org.gradle.ExtensionA1') >> extensionA1
        _ * docModel.getClassDoc('org.gradle.ExtensionA2') >> extensionA2
        _ * docModel.getClassDoc('org.gradle.ExtensionB') >> extensionB

        def content = parse('''<section>
                <section><title>Properties</title>
                    <table><thead><tr><td/></tr></thead></table>
                </section>
                <section><title>Methods</title>
                    <table><thead><tr><td/></tr></thead></table>
                </section>
            </section>
        ''')

        when:
        ClassDoc doc = withCategories {
            new ClassDoc('org.gradle.Class', content, document, classMetaData, extensionMetaData, docModel, javadocConverter).buildExtensions()
        }

        then:
        doc.classExtensions.size() == 2

        doc.classExtensions[0].pluginId == 'a'
        doc.classExtensions[0].extensionClasses == [extensionA1, extensionA2]

        doc.classExtensions[1].pluginId == 'b'
        doc.classExtensions[1].extensionClasses == [extensionB]
    }

    def classMetaData(String name = 'org.gradle.Class') {
        ClassMetaData classMetaData = Mock()
        _ * classMetaData.className >> name
        return classMetaData
    }

    def classDoc(String name = 'org.gradle.Class') {
        ClassDoc doc = Mock()
        return doc
    }

    def property(String name, ClassMetaData classMetaData) {
        return property([:], name, classMetaData)
    }

    def property(Map<String, ?> args, String name, ClassMetaData classMetaData) {
        PropertyMetaData property = Mock()
        _ * property.name >> name
        _ * property.ownerClass >> classMetaData
        _ * property.type >> new TypeMetaData(args.type ?: 'org.gradle.Type')
        _ * property.signature >> "$name-signature"
        _ * javadocConverter.parse(property) >> ({[parse("<para>${args.comment ?: 'comment'}</para>")]} as DocComment)
        return property
    }

    def propertyDoc(String name) {
        PropertyDoc propertyDoc = Mock()
        _ * propertyDoc.name >> name
        _ * propertyDoc.id >> "$name-id"
        _ * propertyDoc.description >> parse("<para>$name comment</para>")
        _ * propertyDoc.metaData >> property(name, null)
        _ * propertyDoc.additionalValues >> []
        _ * propertyDoc.forClass(!null) >> propertyDoc
        return propertyDoc
    }

    def method(String name, ClassMetaData classMetaData) {
        return method([:], name, classMetaData)
    }

    def method(Map<String, ?> args, String name, ClassMetaData classMetaData) {
        MethodMetaData method = Mock()
        List<String> paramTypes = args.paramTypes ?: []
        _ * method.name >> name
        _ * method.overrideSignature >> "$name(${paramTypes.join(', ')})"
        _ * method.parameters >> paramTypes.collect {
            def param = new ParameterMetaData("p", method);
            param.type = new TypeMetaData(it)
            return param
        }
        _ * method.ownerClass >> classMetaData
        _ * method.returnType >> new TypeMetaData(args.returnType ?: 'ReturnType')
        _ * javadocConverter.parse(method) >> ({[parse("<para>${args.comment ?: 'comment'}</para>")]} as DocComment)
        return method
    }

    def methodDoc(String name) {
        MethodDoc methodDoc = Mock()
        _ * methodDoc.name >> name
        _ * methodDoc.metaData >> method(name, null)
        _ * methodDoc.forClass(!null) >> methodDoc
        return methodDoc
    }
}
