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
import org.gradle.build.docs.dsl.model.ClassMetaData
import org.gradle.build.docs.dsl.model.PropertyMetaData

class JavadocConverterTest extends XmlSpecification {
    final ClassMetaData classMetaData = Mock()
    final JavadocLinkConverter linkConverter = Mock()
    final JavadocConverter parser = new JavadocConverter(document, linkConverter)

    def removesLeadingAsterixFromEachLine() {
        _ * classMetaData.rawCommentText >> ''' * line 1
 * line 2
'''
        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para>line 1
line 2</para>'''
    }

    def removesTagBlockFromComment() {
        _ * classMetaData.rawCommentText >> ''' * line 1
 * @tag line 2
 * line 3
'''

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para>line 1</para>'''
    }

    def ignoresLeadingAndTrailingEmptyLines() {
        _ * classMetaData.rawCommentText >> ''' *
 * line 1
 *
 * @tag line 2
'''

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para>line 1</para>'''
    }

    def commentIsEmptyWhenThereIsNoDescription() {
        _ * classMetaData.rawCommentText >> ''' *
 *
 * @tag line 2
'''
        
        when:
        def result = parser.parse(classMetaData)

        then:
        result.docbook == []
    }

    def commentCanContainHtmlEncodedCharacters() {
        _ * classMetaData.rawCommentText >> ''' * &lt;&gt;&amp; &#47;>'''

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para>&lt;&gt;&amp; /&gt;</para>'''
    }

    def convertsPElementsToParaElements() {
        _ * classMetaData.rawCommentText >> '<p>para 1</p><P>para 2</P>'

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para>para 1</para><para>para 2</para>'''
    }

    def addsImplicitParaElement() {
        _ * classMetaData.rawCommentText >> '<em>para 1</em><P>para 2</P>'

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para><emphasis>para 1</emphasis></para><para>para 2</para>'''
    }

    def ignoresEmptyPElements() {
        _ * classMetaData.rawCommentText >> 'para 1<p/><p></p>para 2<p></p>'

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para>para 1</para><para>para 2</para>'''
    }

    def convertsCodeTagsAndElementsToLiteralElements() {
        _ * classMetaData.rawCommentText >> 'This is <code>code</code>. So is {@code this}.'

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para>This is <literal>code</literal>. So is <literal>this</literal>.</para>'''
    }

    def doesNotInterpretContentsOfCodeTagAsHtml() {
        _ * classMetaData.rawCommentText >>'{@code List<String> && a < 9} <code>&amp;</code>'

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para><literal>List&lt;String&gt; &amp;&amp; a &lt; 9</literal> <literal>&amp;</literal></para>'''
    }

    def convertsPreElementsToProgramListingElements() {
        _ * classMetaData.rawCommentText >> ''' * <pre>this is some
 *
 * literal code</pre>
'''

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<programlisting>this is some

literal code</programlisting>'''
    }

    def implicitlyEndsCurrentParagraphAtPreElement() {
        _ * classMetaData.rawCommentText >> ''' * for example: <pre>this is some
 * literal code</pre> this is another para.
'''

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para>for example: </para><programlisting>this is some
literal code</programlisting><para> this is another para.</para>'''
    }

    def convertsUlAndLiElementsToItemizedListElements() {
        _ * classMetaData.rawCommentText >> '<ul><li>item1</li></ul>'

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<itemizedlist><listitem>item1</listitem></itemizedlist>'''
    }

    def convertsALinkTag() {
        _ * classMetaData.rawCommentText >> '{@link someClass} {@link otherClass label}'

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para><xref/> <xref/></para>'''
        1 * linkConverter.resolve('someClass', classMetaData) >> [document.createElement("xref")]
        1 * linkConverter.resolve('otherClass', classMetaData) >> [document.createElement("xref")]
        0 * linkConverter._
    }

    def convertsAnEmElementToAnEmphasisElement() {
        _ * classMetaData.rawCommentText >> '<em>text</em>'

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para><emphasis>text</emphasis></para>'''
    }

    def convertsBAndIElementToAnEmphasisElement() {
        _ * classMetaData.rawCommentText >> '<i>text</i> <b>other</b>'

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<para><emphasis>text</emphasis> <emphasis>other</emphasis></para>'''
    }

    def convertsHeadingsToSections() {
        _ * classMetaData.rawCommentText >> '''
<h2>section1</h2>
text1
<h3>section 1.1</h3>
text2
<h2>section 2</h2>
text3
'''

        when:
        def result = parser.parse(classMetaData)

        then:
        format(result.docbook) == '''<section><title>section1</title>
text1
<section><title>section 1.1</title>
text2
</section></section><section><title>section 2</title>
text3</section>'''
    }

    def convertsPropertyGetterMethodCommentToPropertyComment() {
        PropertyMetaData propertyMetaData = Mock()
        _ * propertyMetaData.rawCommentText >> 'returns the name of the thing.'

        when:
        def result = parser.parse(propertyMetaData)

        then:
        format(result.docbook) == '''<para>The name of the thing.</para>'''
    }

    def convertsInheritDocTag() {
        PropertyMetaData propertyMetaData = Mock()
        PropertyMetaData overriddenMetaData = Mock()

        when:
        def result = parser.parse(propertyMetaData)

        then:
        _ * propertyMetaData.rawCommentText >> 'before {@inheritDoc} after'
        _ * propertyMetaData.overriddenProperty >> overriddenMetaData
        _ * overriddenMetaData.rawCommentText >> ''' *
 * <em>inherited value</em>
 *
'''
        format(result.docbook) == '''<para>before </para><para><emphasis>inherited value</emphasis></para><para> after</para>'''
    }

    def convertsUnknownElementsAndTags() {
        PropertyMetaData propertyMetaData = Mock()
        _ * propertyMetaData.rawCommentText >> '<unknown>text</unknown><inheritDoc>{@unknown text}{@p text}{@ unknown}'

        when:
        def result = parser.parse(propertyMetaData)

        then:
        format(result.docbook) == '''<para><UNKNOWN-ELEMENT>unknown: text</UNKNOWN-ELEMENT><UNKNOWN-ELEMENT>inheritdoc: <UNKNOWN-TAG>unknown: text</UNKNOWN-TAG><UNKNOWN-TAG>p: text</UNKNOWN-TAG><UNKNOWN-TAG>: unknown</UNKNOWN-TAG></UNKNOWN-ELEMENT></para>'''
    }
}
