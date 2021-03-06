/*
 * Copyright (c) 2011-2021, Unicon Creation BV, The Netherlands.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Numdata nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NUMDATA BV BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.numdata.oss.io;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import javax.xml.*;
import javax.xml.bind.*;
import javax.xml.parsers.*;
import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.Validator;
import javax.xml.validation.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;
import org.w3c.dom.Element;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This class provides some utility methods that may be helpful when processing
 * XML data.
 *
 * @author G. Meinders
 * @author Peter S. Heijnen
 */
public class XMLTools
{
	/**
	 * Cached document builder instance.
	 */
	private static DocumentBuilder documentBuilder = null;

	/**
	 * Cached XML input factory.
	 */
	private static XMLInputFactory xmlInputFactory = null;

	/**
	 * Create (secure) {@link SAXParserFactory}.
	 *
	 * @return {@link SAXParserFactory}.
	 */
	public static @NotNull SAXParserFactory createSAXParserFactory()
	{
		final SAXParserFactory spf = SAXParserFactory.newInstance();
		protectAgainstXXE( spf );
		return spf;
	}

	/**
	 * Protect {@link SAXParserFactory} against XML eXternal Entity
	 * injection (XXE).
	 *
	 * @param spf {@link SAXParserFactory} to be protected.
	 *
	 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity Prevention Cheat Sheet (at OWASP)</a>
	 */
	public static void protectAgainstXXE( final @NotNull SAXParserFactory spf )
	{
		setFeature( spf, "http://xerces.apache.org/xerces-j/features.html#external-general-entities", false ); // Xerces 1
		setFeature( spf, "http://xerces.apache.org/xerces2-j/features.html#external-general-entities", false ); // Xerces 2
		setFeature( spf, "http://xml.org/sax/features/external-general-entities", false ); // JDK 7+
		setFeature( spf, "http://apache.org/xml/features/disallow-doctype-decl", true );
	}

	/**
	 * Creates a {@link DocumentBuilderFactory}.
	 *
	 * @return {@link DocumentBuilderFactory}.
	 */
	public static @NotNull DocumentBuilderFactory createDocumentBuilderFactory()
	{
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		protectAgainstXXE( documentBuilderFactory );
		return documentBuilderFactory;
	}

	/**
	 * Protect {@link DocumentBuilderFactory} against XML eXternal Entity
	 * injection (XXE).
	 *
	 * @param dbf {@link DocumentBuilderFactory} to be protected.
	 *
	 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity Prevention Cheat Sheet (at OWASP)</a>
	 */
	public static void protectAgainstXXE( final @NotNull DocumentBuilderFactory dbf )
	{
		// XML entity attacks are prevented
//		setFeature( dbf, "http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl", true ); // Xerces 2 only
//		setFeature( dbf, "http://apache.org/xml/features/disallow-doctype-decl", true );

		// If you can't completely disable DTDs, then at least do the following:
		setFeature( dbf, "http://xerces.apache.org/xerces-j/features.html#external-general-entities", false ); // Xerces 1
		setFeature( dbf, "http://xerces.apache.org/xerces2-j/features.html#external-general-entities", false ); // Xerces 2
		setFeature( dbf, "http://xml.org/sax/features/external-general-entities", false ); // JDK7+

		//This feature has to be used together with the previous one, otherwise it will not protect you from XXE for sure
		setFeature( dbf, "http://xerces.apache.org/xerces-j/features.html#external-parameter-entities", false ); // Xerces 1
		setFeature( dbf, "http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities", false ); // Xerces 2
		setFeature( dbf, "http://xml.org/sax/features/external-parameter-entities", false ); // JDK7+

		setFeature( dbf, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false ); // Disable external DTDs as well

		// and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
		try
		{
			dbf.setXIncludeAware( false );
		}
		catch ( final UnsupportedOperationException ignored )
		{
			// XIncludeAware is not supported, so not necessary to disable it
		}

		dbf.setExpandEntityReferences( false );
	}

	/**
	 * Returns a namespace-aware document builder.
	 *
	 * @return Document builder.
	 */
	public static @NotNull DocumentBuilder getDocumentBuilder()
	{
		DocumentBuilder result = documentBuilder;
		if ( result == null )
		{
			final DocumentBuilderFactory documentBuilderFactory = createDocumentBuilderFactory();
			documentBuilderFactory.setNamespaceAware( true );
			try
			{
				result = documentBuilderFactory.newDocumentBuilder();
				documentBuilder = result;
			}
			catch ( final ParserConfigurationException e )
			{
				throw new RuntimeException( e );
			}
		}
		return result;
	}

	/**
	 * Create default DOM {@link Document} with support for XML namespaces.
	 *
	 * @return {@link Document} that was created.
	 */
	public static @NotNull Document createDocument()
	{
		return getDocumentBuilder().newDocument();
	}

	/**
	 * Creates a new {@link Transformer}.
	 *
	 * @return {@link Transformer}.
	 */
	public static @NotNull Transformer createTransformer()
	{
		final TransformerFactory transformerFactory = createTransformerFactory();
		try
		{
			return transformerFactory.newTransformer();
		}
		catch ( final TransformerConfigurationException e )
		{
			throw new RuntimeException( transformerFactory + " failed to create transformer: " + e, e );
		}
	}

	/**
	 * Creates a new {@link TransformerFactory}.
	 *
	 * @return {@link TransformerFactory}.
	 */
	public static @NotNull TransformerFactory createTransformerFactory()
	{
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		protectAgainstXXE( transformerFactory );
		return transformerFactory;
	}

	/**
	 * Protect {@link TransformerFactory} against XML eXternal Entity injection
	 * (XXE).
	 *
	 * @param tf {@link TransformerFactory} to be protected.
	 *
	 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity Prevention Cheat Sheet (at OWASP)</a>
	 */
	public static void protectAgainstXXE( final @NotNull TransformerFactory tf )
	{
		try
		{
			tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
		}
		catch ( final RuntimeException ignored )
		{
			// ignore unsupported property, it is probably a different implementation version than requested
		}

		try
		{
			tf.setAttribute( XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "" );
		}
		catch ( final RuntimeException ignored )
		{
			// ignore unsupported property, it is probably a different implementation version than requested
		}
	}

	/**
	 * Returns an XML input factory. Coalescing of character data is enabled.
	 *
	 * @return XML input factory.
	 */
	public static @NotNull XMLInputFactory getXMLInputFactory()
	{
		XMLInputFactory result = xmlInputFactory;
		if ( result == null )
		{
			result = XMLInputFactory.newInstance();
			protectAgainstXXE( result );
			result.setProperty( XMLInputFactory.IS_COALESCING, Boolean.TRUE );
			xmlInputFactory = result;
		}
		return result;
	}

	/**
	 * Protect {@link XMLInputFactory} against XML eXternal Entity injection
	 * (XXE).
	 *
	 * @param xif {@link XMLInputFactory} to be protected.
	 *
	 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity Prevention Cheat Sheet (at OWASP)</a>
	 */
	public static void protectAgainstXXE( final @NotNull XMLInputFactory xif )
	{
		// This disables DTDs entirely for that factory
		setProperty( xif, XMLInputFactory.SUPPORT_DTD, false );

		// disable external entities
		setProperty( xif, "javax.xml.stream.isSupportingExternalEntities", false );
	}

	/**
	 * Creates an XML stream reader. Coalescing of character data is enabled.
	 *
	 * @param in Stream to read from.
	 *
	 * @return XML stream reader.
	 */
	@NotNull
	public static XMLStreamReader createXMLStreamReader( final @NotNull InputStream in )
	{
		final XMLInputFactory xmlInputFactory = getXMLInputFactory();
		try
		{
			return xmlInputFactory.createXMLStreamReader( in );
		}
		catch ( final XMLStreamException e )
		{
			throw new RuntimeException( e );
		}
	}

	/**
	 * Creates an XML stream reader. Coalescing of character data is enabled.
	 *
	 * <strong>This method should only be used in very specific circumstances,
	 * e.g. when reading from a string. Otherwise use
	 * {@link #createXMLStreamReader(InputStream)}.</strong>
	 *
	 * @param reader Character stream to read from.
	 *
	 * @return XML stream reader.
	 */
	public static @NotNull XMLStreamReader createXMLStreamReader( final @NotNull Reader reader )
	{
		final XMLInputFactory xmlInputFactory = getXMLInputFactory();
		try
		{
			return xmlInputFactory.createXMLStreamReader( reader );
		}
		catch ( final XMLStreamException e )
		{
			throw new RuntimeException( e );
		}
	}

	/**
	 * Skips over the current element and all of its child nodes.
	 *
	 * @param reader XML reader to be used.
	 *
	 * @throws XMLStreamException if an error occurs while parsing the
	 * document.
	 */
	public static void skipElement( final @NotNull XMLStreamReader reader )
	throws XMLStreamException
	{
		reader.require( XMLStreamConstants.START_ELEMENT, null, null );

		int depth = 1;
		do
		{
			final int type = reader.next();
			if ( type == XMLStreamConstants.START_ELEMENT )
			{
				depth++;
			}
			else if ( type == XMLStreamConstants.END_ELEMENT )
			{
				depth--;
			}
		}
		while ( depth > 0 );
	}

	/**
	 * Reads the text content of the current element. This is the concatenation
	 * of the character nodes inside the element and any nested elements. Any
	 * other nodes found inside the element are ignored.
	 *
	 * @param reader Reader to read from.
	 *
	 * @return Text content.
	 *
	 * @throws XMLStreamException if an I/O exception occurs.
	 */
	public static @NotNull String readTextContent( final @NotNull XMLStreamReader reader )
	throws XMLStreamException
	{
		reader.require( XMLStreamConstants.START_ELEMENT, null, null );

		final StringBuilder result = new StringBuilder();

		int depth = 0;
		while ( reader.hasNext() )
		{
			final int type = reader.next();
			if ( type == XMLStreamConstants.START_ELEMENT )
			{
				depth++;
			}
			else if ( type == XMLStreamConstants.END_ELEMENT )
			{
				if ( depth-- == 0 )
				{
					break;
				}
			}
			else if ( type == XMLStreamConstants.CHARACTERS )
			{
				result.append( reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength() );
			}
		}

		return result.toString();
	}

	/**
	 * Read element from XML document and return it as an {@link Element}. On
	 * exit of this method, the stream will be positioned directly after the
	 * read element.
	 *
	 * @param reader XML reader to be used.
	 *
	 * @return {@link Element} that was read.
	 *
	 * @throws XMLStreamException if an error occurs while parsing the
	 * document.
	 */
	public static @NotNull Element readElementAsDOM( final @NotNull XMLStreamReader reader )
	throws XMLStreamException
	{
		final Document document = createDocument();
		readDocument( reader, document );
		return document.getDocumentElement();
	}

	/**
	 * Read a complete document or a single XML element (and any nested content)
	 * from an XML stream. In either case, reading stops if an initial element
	 * is closed.
	 *
	 * @param reader   XML reader.
	 * @param rootNode DOM node to read into.
	 *
	 * @throws XMLStreamException if an error occurs while parsing the
	 * document.
	 */
	public static void readDocument( final @NotNull XMLStreamReader reader, final @NotNull Node rootNode )
	throws XMLStreamException
	{
		final Object namespaceAwareValue = reader.getProperty( XMLInputFactory.IS_NAMESPACE_AWARE );
		final boolean namespaceAware = !( ( namespaceAwareValue instanceof Boolean ) && !(Boolean)namespaceAwareValue );
		boolean seenStartDocument = false;

		final Document document = ( rootNode instanceof Document ) ? (Document)rootNode : rootNode.getOwnerDocument();

		Node node = rootNode;
		while ( true )
		{
			boolean finished = false;

			switch ( reader.getEventType() )
			{
				case XMLStreamConstants.START_DOCUMENT:
					seenStartDocument = true;
					break;

				case XMLStreamConstants.END_DOCUMENT:
					finished = true;
					break;

				case XMLStreamConstants.START_ELEMENT:
				{
					final Element child;

					if ( !namespaceAware )
					{
						child = document.createElement( reader.getLocalName() );
					}
					else
					{
						final String prefix = reader.getPrefix();
						final String qualifiedName = ( ( prefix != null ) && !prefix.isEmpty() ) ? prefix + ':' + reader.getLocalName() : reader.getLocalName();
						child = document.createElementNS( reader.getNamespaceURI(), qualifiedName );
					}

					node.appendChild( child );
					node = child;

					final int namespaceCount = reader.getNamespaceCount();
					for ( int i = 0; i < namespaceCount; ++i )
					{
						final String prefix = reader.getNamespacePrefix( i );
						final String qualifiedName = ( prefix != null ) && !prefix.isEmpty() ? "xmlns:" + prefix : "xmlns";
						child.setAttributeNS( XMLConstants.XMLNS_ATTRIBUTE_NS_URI, qualifiedName, reader.getNamespaceURI( i ) );
					}

					final int attributeCount = reader.getAttributeCount();
					for ( int i = 0; i < attributeCount; ++i )
					{
						final String localName = reader.getAttributeLocalName( i );
						final String value = reader.getAttributeValue( i );

						if ( namespaceAware )
						{
							final String prefix = reader.getAttributePrefix( i );
							final String qualifiedName = ( prefix != null ) && !prefix.isEmpty() ? prefix + ':' + localName : localName;
							child.setAttributeNS( reader.getAttributeNamespace( i ), qualifiedName, value );
						}
						else
						{
							child.setAttribute( localName, value );
						}
					}
					break;
				}

				case XMLStreamConstants.END_ELEMENT:
					node = node.getParentNode();
					finished = ( node == null ) || ( !seenStartDocument && ( node == rootNode ) );
					break;

				case XMLStreamConstants.CDATA:
					node.appendChild( document.createCDATASection( reader.getText() ) );
					break;

				case XMLStreamConstants.CHARACTERS:
					node.appendChild( document.createTextNode( reader.getText() ) );
					break;

				case XMLStreamConstants.COMMENT:
					node.appendChild( document.createComment( reader.getText() ) );
					break;

				case XMLStreamConstants.SPACE:
					if ( node != rootNode )
					{
						node.appendChild( document.createTextNode( reader.getText() ) );
					}
					break;

				case XMLStreamConstants.ENTITY_REFERENCE:
					node.appendChild( document.createEntityReference( reader.getLocalName() ) );
					break;

				case XMLStreamConstants.PROCESSING_INSTRUCTION:
					node.appendChild( document.createProcessingInstruction( reader.getPITarget(), reader.getPIData() ) );
					break;

				case XMLStreamConstants.ENTITY_DECLARATION:
				case XMLStreamConstants.NOTATION_DECLARATION:
				case XMLStreamConstants.DTD:
					break;

				default:
					throw new XMLStreamException( "Unexpected event: " + reader.getEventType() );
			}

			if ( finished )
			{
				break;
			}

			reader.next();
		}
	}

	/**
	 * Pretty print an XML document.
	 *
	 * @param node DOM node to print.
	 * @param out  Stream to send the output to.
	 */
	public static void prettyPrint( final @NotNull Node node, final @NotNull OutputStream out )
	{
		try
		{
			final Transformer transformer = createTransformer();
			transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
			transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "2" );
			transformer.transform( new DOMSource( node ), new StreamResult( out ) );
		}
		catch ( final TransformerException e )
		{
			throw new RuntimeException( e );
		}
	}

	/**
	 * Get pretty-printed XML document for Java object marshalled using JAXB.
	 *
	 * @param jaxbObject Java object to be marshalled into XML.
	 *
	 * @return Pretty-printed XML-document.
	 *
	 * @see JAXB#marshal(Object, Result)
	 */
	public static @NotNull String getPrettyJaxb( final @NotNull Object jaxbObject )
	{
		final Document document = createDocument();
		JAXB.marshal( jaxbObject, new DOMResult( document ) );
		return getPretty( document );
	}

	/**
	 * Get pretty-printed XML document.
	 *
	 * @param node DOM node to print.
	 *
	 * @return Pretty-printed XML-document.
	 */
	public static @NotNull String getPretty( final @NotNull Node node )
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		prettyPrint( node, buffer );

		try
		{
			return buffer.toString( "UTF-8" );
		}
		catch ( final UnsupportedEncodingException e )
		{
			throw new AssertionError( e ); /* UTF-8 is always available */
		}
	}

	/**
	 * Write DOM node to the given XML stream.
	 *
	 * @param xmlWriter XML document writer.
	 * @param node      DOM node to write to XML document.
	 *
	 * @throws XMLStreamException if there a problem writing the XML document.
	 */
	public static void writeNode( final @NotNull XMLStreamWriter xmlWriter, final @NotNull Node node )
	throws XMLStreamException
	{
		switch ( node.getNodeType() )
		{
			case Node.ELEMENT_NODE:
				writeElement( xmlWriter, node );
				break;

			case Node.TEXT_NODE:
				xmlWriter.writeCharacters( node.getNodeValue() );
				break;

			case Node.CDATA_SECTION_NODE:
				xmlWriter.writeCData( node.getNodeValue() );
				break;

			case Node.ENTITY_REFERENCE_NODE:
				xmlWriter.writeEntityRef( node.getNodeName() );
				break;

			case Node.ENTITY_NODE:
				break;

			case Node.PROCESSING_INSTRUCTION_NODE:
				final String target = node.getNodeName();
				final String data = node.getNodeValue();
				if ( ( data == null ) || data.isEmpty() )
				{
					xmlWriter.writeProcessingInstruction( target );
				}
				else
				{
					xmlWriter.writeProcessingInstruction( target, data );
				}
				break;

			case Node.COMMENT_NODE:
				xmlWriter.writeComment( node.getNodeValue() );
				break;

			case Node.DOCUMENT_NODE:
				writeDocument( xmlWriter, node );
				break;

			case Node.DOCUMENT_TYPE_NODE:
				writeDTD( xmlWriter, (DocumentType)node );
				break;

			default:
				final Class<? extends Node> nodeClass = node.getClass();
				throw new XMLStreamException( "Unrecognized or unexpected node class: " + nodeClass.getName() );
		}
	}

	/**
	 * Write child nodes of a DOM node to the given XML stream.
	 *
	 * @param xmlWriter XML document writer.
	 * @param node      DOM nodes whose children to write to XML document.
	 *
	 * @throws XMLStreamException if there a problem writing the XML document.
	 */
	public static void writeChildren( final @NotNull XMLStreamWriter xmlWriter, final @NotNull Node node )
	throws XMLStreamException
	{
		for ( Node child = node.getFirstChild(); child != null; child = child.getNextSibling() )
		{
			writeNode( xmlWriter, child );
		}
	}

	/**
	 * Write DOM document to the given XML stream.
	 *
	 * @param xmlWriter XML document writer.
	 * @param document  DOM node to write as XML document.
	 *
	 * @throws XMLStreamException if there a problem writing the XML document.
	 */
	public static void writeDocument( final @NotNull XMLStreamWriter xmlWriter, final @NotNull Node document )
	throws XMLStreamException
	{
		xmlWriter.writeStartDocument();
		writeChildren( xmlWriter, document );
		xmlWriter.writeEndDocument();
		xmlWriter.flush();
	}

	/**
	 * Write DOM {@link Element} to the given XML stream.
	 *
	 * @param xmlWriter XML document writer.
	 * @param element   DOM {@link Element} to write to XML document.
	 *
	 * @throws XMLStreamException if there a problem writing the XML document.
	 */
	public static void writeElement( final @NotNull XMLStreamWriter xmlWriter, final @NotNull Node element )
	throws XMLStreamException
	{
		String elementPrefix = element.getPrefix();
		if ( elementPrefix == null )
		{
			elementPrefix = "";
		}

		final String elementName = element.getLocalName();

		String elementNsUri = element.getNamespaceURI();
		if ( elementNsUri == null )
		{
			elementNsUri = "";
		}

		final boolean hasChildren = ( element.getFirstChild() != null );
		if ( hasChildren )
		{
			xmlWriter.writeStartElement( elementPrefix, elementName, elementNsUri );
		}
		else
		{
			xmlWriter.writeEmptyElement( elementPrefix, elementName, elementNsUri );
		}

		final NamedNodeMap attributes = element.getAttributes();
		for ( int i = 0; i < attributes.getLength(); ++i )
		{
			final Attr attribute = (Attr)attributes.item( i );
			final String attributePrefix = attribute.getPrefix();
			final String attributeName = attribute.getLocalName();
			final String attributeValue = attribute.getValue();

			if ( TextTools.isEmpty( attributePrefix ) )
			{
				if ( "xmlns".equals( attributeName ) )
				{
					xmlWriter.writeDefaultNamespace( attributeValue );
				}
				else
				{
					xmlWriter.writeAttribute( attributeName, attributeValue );
				}
			}
			else
			{
				// Ok: is it a namespace declaration?
				if ( "xmlns".equals( attributePrefix ) )
				{
					xmlWriter.writeNamespace( attributeName, attributeValue );
				}
				else
				{
					xmlWriter.writeAttribute( attributePrefix, attribute.getNamespaceURI(), attributeName, attributeValue );
				}
			}
		}

		if ( hasChildren )
		{
			writeChildren( xmlWriter, element );

			xmlWriter.writeEndElement();
		}
	}

	/**
	 * Write DOM {@link DocumentType} (DTD) to the given XML stream.
	 *
	 * @param xmlWriter    XML document writer.
	 * @param documentType DOM {@link DocumentType}.
	 *
	 * @throws XMLStreamException if there a problem writing the XML document.
	 */
	public static void writeDTD( final @NotNull XMLStreamWriter xmlWriter, final @NotNull DocumentType documentType )
	throws XMLStreamException
	{
		final String name = documentType.getName();
		final String publicId = documentType.getPublicId();
		final String systemId = documentType.getSystemId();
		final String internalSubset = documentType.getInternalSubset();

		final StringBuilder sb = new StringBuilder();
		sb.append( "<!DOCTYPE " );
		sb.append( name );

		if ( !TextTools.isEmpty( publicId ) )
		{
			sb.append( "PUBLIC \"" );
			sb.append( publicId );
			sb.append( "\" \"" );
			sb.append( systemId );
			sb.append( '"' );
		}
		else if ( !TextTools.isEmpty( systemId ) )
		{
			sb.append( "SYSTEM \"" );
			sb.append( systemId );
			sb.append( '"' );
		}

		if ( !TextTools.isEmpty( internalSubset ) )
		{
			sb.append( " [" );
			sb.append( internalSubset );
			sb.append( ']' );
		}

		sb.append( '>' );

		xmlWriter.writeDTD( sb.toString() );
	}

	/**
	 * Set {@link DocumentBuilderFactory} feature safely (any exception will be
	 * caught and ignored).
	 *
	 * @param dbf     {@link DocumentBuilderFactory} to update.
	 * @param feature Feature name.
	 * @param state   Feature state.
	 */
	private static void setFeature( final @NotNull DocumentBuilderFactory dbf, final @NotNull String feature, final boolean state )
	{
		try
		{
			dbf.setFeature( feature, state );
		}
		catch ( final ParserConfigurationException | RuntimeException ignored )
		{
			// ignore unsupported feature, it is probably a different implementation version than requested
		}
	}

	/**
	 * Set {@link DocumentBuilderFactory} feature safely (any exception will be
	 * caught and ignored).
	 *
	 * @param spf     {@link DocumentBuilderFactory} to update.
	 * @param feature Feature name.
	 * @param state   Feature state.
	 */
	private static void setFeature( final @NotNull SAXParserFactory spf, final @NotNull String feature, final boolean state )
	{
		try
		{
			spf.setFeature( feature, state );
		}
		catch ( final ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException | RuntimeException ignored )
		{
			// ignore unsupported feature, it is probably a different implementation version than requested
		}
	}

	/**
	 * Set {@link XMLInputFactory} feature safely (any exception will be caught
	 * and ignored).
	 *
	 * @param xif   {@link XMLInputFactory} to update.
	 * @param name  Property name.
	 * @param value Property value.
	 */
	private static void setProperty( final @NotNull XMLInputFactory xif, final String name, final boolean value )
	{
		try
		{
			xif.setProperty( name, value );
		}
		catch ( final RuntimeException ignored )
		{
			// ignore unsupported property, it is probably a different implementation version than requested
		}
	}

	/**
	 * Protect {@link SchemaFactory} against XML eXternal Entity injection
	 * (XXE).
	 *
	 * @param sf {@link SchemaFactory} to be protected.
	 *
	 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity Prevention Cheat Sheet (at OWASP)</a>
	 */
	public static void protectAgainstXXE( final @NotNull SchemaFactory sf )
	{
		try
		{
			sf.setProperty( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
		}
		catch ( final RuntimeException | SAXNotSupportedException | SAXNotRecognizedException ignored )
		{
			// ignore unsupported property, it is probably a different implementation version than requested
		}

		try
		{
			sf.setProperty( XMLConstants.ACCESS_EXTERNAL_SCHEMA, "" );
		}
		catch ( final RuntimeException | SAXNotSupportedException | SAXNotRecognizedException ignored )
		{
			// ignore unsupported property, it is probably a different implementation version than requested
		}
	}

	/**
	 * Protect {@link Validator} against XML eXternal Entity injection
	 * (XXE).
	 *
	 * @param sf {@link Validator} to be protected.
	 *
	 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity Prevention Cheat Sheet (at OWASP)</a>
	 */
	public static void protectAgainstXXE( final @NotNull Validator sf )
	{
		try
		{
			sf.setProperty( XMLConstants.ACCESS_EXTERNAL_DTD, "" );
		}
		catch ( final RuntimeException | SAXNotSupportedException | SAXNotRecognizedException ignored )
		{
			// ignore unsupported property, it is probably a different implementation version than requested
		}

		try
		{
			sf.setProperty( XMLConstants.ACCESS_EXTERNAL_SCHEMA, "" );
		}
		catch ( final RuntimeException | SAXNotSupportedException | SAXNotRecognizedException ignored )
		{
			// ignore unsupported property, it is probably a different implementation version than requested
		}
	}

	/**
	 * Protect {@link SAXTransformerFactory} against XML eXternal Entity injection
	 * (XXE).
	 *
	 * @param stf {@link SAXTransformerFactory} to be protected.
	 *
	 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity Prevention Cheat Sheet (at OWASP)</a>
	 */
	public static void protectAgainstXXE( final @NotNull SAXTransformerFactory stf )
	{
		for ( final String name : Arrays.asList( XMLConstants.ACCESS_EXTERNAL_DTD,
		                                         XMLConstants.ACCESS_EXTERNAL_STYLESHEET ) )
		{
			try
			{
				stf.setAttribute( name, "" );
			}
			catch ( final RuntimeException ignored )
			{
				// ignore unsupported property, it is probably a different implementation version than requested
			}
		}
	}

	/**
	 * Protect {@link XMLReader} against XML eXternal Entity injection
	 * (XXE).
	 *
	 * @param xmlReader {@link XMLReader} to be protected.
	 *
	 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">XML External Entity Prevention Cheat Sheet (at OWASP)</a>
	 */
	public static void protectAgainstXXE( final @NotNull XMLReader xmlReader )
	{
		for ( final String name : Arrays.asList( "http://apache.org/xml/features/nonvalidating/load-external-dtd",
		                                         "http://xml.org/sax/features/external-general-entities",
		                                         "http://xml.org/sax/features/external-parameter-entities" ) )
		{
			try
			{
				xmlReader.setFeature( name, false );
			}
			catch ( final RuntimeException | SAXNotRecognizedException | SAXNotSupportedException ignored )
			{
				// ignore unsupported property, it is probably a different implementation version than requested
			}
		}
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private XMLTools()
	{
	}

	/**
	 * Create wrapper for {@link XMLStreamWriter} to create 'pretty' output
	 * (with indentations and new lines).
	 *
	 * @param xmlStreamWriter Target XML writer.
	 *
	 * @return Wrapped {@link XMLStreamWriter}.
	 */
	@NotNull
	public static XMLStreamWriter streamPretty( final @NotNull XMLStreamWriter xmlStreamWriter )
	{
		return (XMLStreamWriter)Proxy.newProxyInstance( XMLStreamWriter.class.getClassLoader(), new Class[] { XMLStreamWriter.class }, new PrettyXMLStreamWriterHandler( xmlStreamWriter ) );
	}

	/**
	 * Returns a list based on the given node list. The created list is simply
	 * an adapter wrapping the given node list, not a copy of it. As such, the
	 * list behaves exactly the same as the node list would.
	 *
	 * @param nodeList Node list.
	 * @param <T>      Node type.
	 *
	 * @return Created list.
	 */
	public static <T extends Node> List<T> asList( final NodeList nodeList )
	{
		return new NodeListAdapter<>( nodeList );
	}

	/**
	 * Wrapper for {@link XMLStreamWriter} to add indentation and net lines.
	 */
	public static class PrettyXMLStreamWriterHandler
	implements InvocationHandler
	{
		/**
		 * Initial value for {@link #_indent}.
		 */
		private static final char[] INITIAL_INDENT = { '\n', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };

		/**
		 * Target XML writer.
		 */
		@NotNull
		private final XMLStreamWriter _writer;

		/**
		 * Whether the element at the given depth has child elements.
		 */
		private final Set<Integer> _hasChild = new HashSet<>();

		/**
		 * Current indentation depth.
		 */
		private int _depth = 0;

		/**
		 * Indentation character buffer.
		 */
		private char[] _indent = INITIAL_INDENT;

		/**
		 * Construct {@link XMLStreamWriter} wrapper.
		 *
		 * @param target Target XML writer.
		 */
		public PrettyXMLStreamWriterHandler( final @NotNull XMLStreamWriter target )
		{
			_writer = target;
		}

		@Nullable
		@Override
		public Object invoke( final Object proxy, final Method method, final Object[] args )
		throws Throwable
		{
			final Set<Integer> hasChild = _hasChild;

			final String methodName = method.getName();
			switch ( methodName )
			{
				case "writeStartElement":
					hasChild.add( _depth - 1 );
					writeIndent();
					hasChild.remove( _depth++ );
					break;

				case "writeEndElement":
					if ( hasChild.contains( --_depth ) )
					{
						writeIndent();
					}
					break;

				case "writeEmptyElement":
					hasChild.add( _depth - 1 );
					writeIndent();
					break;
			}

			method.invoke( _writer, args );

			return null;
		}

		/**
		 * Write indenting characters to stream.
		 *
		 * @throws XMLStreamException if writing failed.
		 */
		private void writeIndent()
		throws XMLStreamException
		{
			final int length = 1 + _depth * 2;
			char[] indent = _indent;
			if ( length > indent.length )
			{
				final int capacity = 1 + ( ( length / 80 ) + 1 ) * 80;
				indent = new char[ capacity ];
				indent[ 0 ] = '\n';
				Arrays.fill( indent, 1, capacity, ' ' );
				_indent = indent;
			}

			_writer.writeCharacters( indent, 0, length );
		}
	}
}
