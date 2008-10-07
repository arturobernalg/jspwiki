package com.ecyrd.jspwiki.ui.stripes;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class StripesJspTransformerTest extends TestCase
{
    public static Test suite()
    {
        return new TestSuite( StripesJspTransformerTest.class );
    }

    protected Map<String, Object> m_sharedState = new HashMap<String, Object>();

    protected JspTransformer m_transformer = new StripesJspTransformer();

    protected JspDocument m_doc = new JspDocument();

    public StripesJspTransformerTest( String s )
    {
        super( s );
    }

    public void testAddStripesTaglib() throws Exception
    {
        String s = "<form/>";
        JspDocument doc = new JspParser().parse( s );
        assertEquals( 1, doc.getNodes().size() );
        m_transformer.transform( m_sharedState, doc );
        Node node;
        Attribute attribute;

        // Verify Stripes taglib + linebreak were added
        assertEquals( 3, doc.getNodes().size() );

        // Verify Stripes taglib
        node = doc.getNodes().get( 0 );
        assertEquals( NodeType.JSP_DIRECTIVE, node.getType() );
        assertEquals( "taglib", node.getName() );
        attribute = ((Tag) node).getAttribute( "prefix" );
        assertEquals( "stripes", attribute.getValue() );
        attribute = ((Tag) node).getAttribute( "uri" );
        assertEquals( "/WEB-INF/stripes.tld", attribute.getValue() );

        // Verify linebreak
        node = doc.getNodes().get( 1 );
        assertEquals( NodeType.TEXT, node.getType() );

        // Verify old tag is still there too
        node = doc.getNodes().get( 2 );
        assertEquals( NodeType.EMPTY_ELEMENT_TAG, node.getType() );
        assertEquals( "stripes:form", node.getName() );
        assertEquals( 0, ((Tag) node).getAttributes().size() );
    }

    public void testFormEmptyElementTag() throws Exception
    {
        String s = "<form accept-charset=\"UTF-8\" method=\"POST\" />";
        JspDocument doc = new JspParser().parse( s );
        m_transformer.transform( m_sharedState, doc );

        assertEquals( 3, doc.getNodes().size() ); // Added Stripes taglib + linebreak
        Node node = doc.getNodes().get( 2 ); // First 2 are injected Stripes taglib+ llinebreak
        assertEquals( "stripes:form", node.getName() );

        assertEquals( 2, ((Tag) node).getAttributes().size() );
        Node attribute = ((Tag) node).getAttributes().get( 0 );
        assertEquals( "acceptcharset", attribute.getName() );
    }

    public void testFormWithParams() throws Exception
    {
        String s = "<form action=\"Login.jsp?tab=profile\"/>";
        JspDocument doc = new JspParser().parse( s );
        m_transformer.transform( m_sharedState, doc );

        assertEquals( 5, doc.getNodes().size() );
        Node node = doc.getNodes().get( 2 ); // First 2 are injected Stripes taglib+ llinebreak
        assertEquals( "stripes:form", node.getName() );
        assertEquals( 1, ((Tag) node).getAttributes().size() );
        Node attribute = ((Tag) node).getAttributes().get( 0 );
        assertEquals( "Login.jsp", attribute.getValue() );
    }

    public void testNoAddStripesTaglib() throws Exception
    {
        String s = "<%@ taglib uri=\"/WEB-INF/stripes.tld\" prefix=\"stripes\" %>\n<foo/>";
        JspDocument doc = new JspParser().parse( s );
        assertEquals( 3, doc.getNodes().size() );
        m_transformer.transform( m_sharedState, doc );

        // Verify Stripes taglib was NOT added
        assertEquals( 3, doc.getNodes().size() );
    }

    public void testPasswordTag() throws Exception
    {
        String s = "<input type=\"password\" size=\"24\" value=\"\" name=\"j_username\" id=\"j_username\" />";
        JspDocument doc = new JspParser().parse( s );
        Node node;

        // Before transformation, 1 node, with 5 attributes
        node = doc.getNodes().get( 0 );
        assertEquals( 1, doc.getNodes().size() );
        assertEquals( 5, ((Tag) node).getAttributes().size() );
        m_transformer.transform( m_sharedState, doc );

        // After transformation, the "type" attribute is deleted
        assertEquals( 3, doc.getNodes().size() ); // Added Stripes taglib + linebreak
        node = doc.getNodes().get( 2 ); // First 2 are injected Stripes taglib+llinebreak
        assertEquals( NodeType.EMPTY_ELEMENT_TAG, node.getType() );
        assertEquals( "stripes:password", node.getName() );
        assertEquals( 4, ((Tag) node).getAttributes().size() );
        assertEquals( 0, node.getChildren().size() );
    }

    public void testPasswordTagComplex() throws Exception
    {
        String s = "<input type=\"password\" size=\"24\" value=\"<wiki:Variable var='uid' default='' />\" name=\"j_username\" id=\"j_username\" />";
        JspDocument doc = new JspParser().parse( s );
        assertEquals( 1, doc.getNodes().size() );
        m_transformer.transform( m_sharedState, doc );

        // After transformation, the "type" and "value" attributes are deleted;
        // value becomes child node
        assertEquals( 5, doc.getNodes().size() ); // Added Stripes taglib +linebreak
        Node node = doc.getNodes().get( 2 ); // First 2 are injected Stripes taglib+ llinebreak
        assertEquals( NodeType.START_TAG, node.getType() );
        assertEquals( "stripes:password", node.getName() );
        assertEquals( 3, ((Tag) node).getAttributes().size() );

        // The value attribute should show up as a child node
        assertEquals( 1, node.getChildren().size() );
        node = node.getChildren().get( 0 );
        assertEquals( NodeType.EMPTY_ELEMENT_TAG, node.getType() );
        assertEquals( "wiki:Variable", node.getName() );
    }

    public void testTextArea() throws Exception
    {
        String s = "<textarea id=\"members\" name=\"members\" rows=\"10\" cols=\"30\" value=\"Foo\" />";
        JspDocument doc = new JspParser().parse( s );
        assertEquals( 1, doc.getNodes().size() );
        m_transformer.transform( m_sharedState, doc );

        // After transformation, the tag name is renamed
        assertEquals( 3, doc.getNodes().size() ); // Added Stripes taglib + linebreak
        Node node = doc.getNodes().get( 2 ); // First 2 are injected Stripes taglib+ llinebreak
        assertEquals( NodeType.EMPTY_ELEMENT_TAG, node.getType() );
        assertEquals( "stripes:textarea", node.getName() );
        assertEquals( 5, ((Tag) node).getAttributes().size() );

        // The value attribute should have stayed as an attribute
        Attribute attribute = ((Tag) node).getAttribute( "value" );
        assertNotNull( attribute );
        assertEquals( 1, attribute.getChildren().size() );
        node = attribute.getChildren().get( 0 );
        assertEquals( NodeType.TEXT, node.getType() );
        assertEquals( "Foo", node.getValue() );
    }

    public void testTextAreaComplex() throws Exception
    {
        String s = "<textarea id=\"members\" name=\"members\" rows=\"10\" cols=\"30\" value=\"<%=foo%>\" />";
        JspDocument doc = new JspParser().parse( s );
        assertEquals( 1, doc.getNodes().size() );
        m_transformer.transform( m_sharedState, doc );

        // After transformation, the tag name is renamed & tag is split
        assertEquals( 5, doc.getNodes().size() ); // Added Stripes taglib + linebreak
        Node node = doc.getNodes().get( 2 ); // First 2 are injected Stripes taglib+ llinebreak
        assertEquals( NodeType.START_TAG, node.getType() );
        assertEquals( "stripes:textarea", node.getName() );
        assertEquals( 4, ((Tag) node).getAttributes().size() ); // Value attribute vanishes...

        // Verify newly created end tag
        node = doc.getNodes().get( 4 );
        assertEquals( NodeType.END_TAG, node.getType() );
        assertEquals( "stripes:textarea", node.getName() );

        // The value attribute should have moved to child nodes
        node = doc.getNodes().get( 2 );
        Attribute attribute = ((Tag) node).getAttribute( "value" );
        assertNull( attribute );
        
        assertEquals( 1, node.getChildren().size() );
        node = node.getChildren().get( 0 );
        assertEquals( NodeType.JSP_EXPRESSION, node.getType() );
        assertEquals( "foo", node.getValue() );
        assertEquals( "<%=foo%>", node.toString() );
    }

    public void testTextAreaNoNameAttribute() throws Exception
    {
        String s = "<textarea id=\"members\" rows=\"10\" cols=\"30\" value=\"Foo\" />";
        JspDocument doc = new JspParser().parse( s );
        assertEquals( 1, doc.getNodes().size() );
        m_transformer.transform( m_sharedState, doc );

        // After transformation, the tag name is stays the same (no name attribute...)
        assertEquals( 1, doc.getNodes().size() ); // NO Stripes taglib or  linebreak
        Node node = doc.getNodes().get( 0 );
        assertEquals( NodeType.EMPTY_ELEMENT_TAG, node.getType() );
        assertEquals( "textarea", node.getName() );
        assertEquals( 4, ((Tag) node).getAttributes().size() );
    }
}