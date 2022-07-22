/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */
package org.apache.wiki.htmltowiki.syntax;

import org.apache.commons.lang3.StringUtils;
import org.apache.wiki.htmltowiki.XHtmlElementToWikiTranslator;
import org.apache.wiki.util.XmlUtil;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.PrintWriter;


/**
 * Translates to wiki syntax from a {@code FORM} element.
 */
public abstract class FormDecorator {

    final protected PrintWriter out;
    final protected XHtmlElementToWikiTranslator chain;

    protected FormDecorator( final PrintWriter out, final XHtmlElementToWikiTranslator chain ) {
        this.out = out;
        this.chain = chain;
    }

    /**
     * Translates the given XHTML element into wiki markup.
     *
     * @param e XHTML element being translated.
     */
    public void decorate( final Element e ) throws JDOMException {
        // remove the hidden input where name="formname" since a new one will be generated again when the xhtml is rendered.
        final Element formName = XmlUtil.getXPathElement( e, "INPUT[@name='formname']" );
        if( formName != null ) {
            formName.detach();
        }

        final String name = e.getAttributeValue( "name" );
        out.print( StringUtils.LF + markupFormOpen( name ) + StringUtils.LF );
        chain.translate( e );
        out.print( StringUtils.LF + markupFormClose() + StringUtils.LF );
    }

    /**
     * Opening wiki markup for a {@code Form} element.
     *
     * @param name {@code Form}'s {@code name} attribute - may be {@code null}.
     * @return Opening wiki markup for a {@code Form} element.
     */
    protected abstract String markupFormOpen( String name );

    /**
     * Closing wiki markup for a {@code Form} element.
     *
     * @return Closing wiki markup for a {@code Form} element.
     */
    protected abstract String markupFormClose();

}
