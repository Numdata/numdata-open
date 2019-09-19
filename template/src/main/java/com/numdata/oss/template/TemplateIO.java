/*
 * Copyright (c) 2011-2017, Numdata BV, The Netherlands.
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

package com.numdata.oss.template;

import java.io.*;
import javax.xml.*;
import javax.xml.namespace.*;
import javax.xml.stream.*;

import com.numdata.oss.*;
import com.numdata.oss.io.*;
import org.jetbrains.annotations.*;

/**
 * Reads and writes {@link Template}s as XML.
 *
 * @author G. Meinders
 */
public class TemplateIO
{
	/**
	 * Namespace URI for templates.
	 */
	public static final String TEMPLATE_NS_URI = "http://www.numdata.com/2011/template-0.1";

	/**
	 * Reads a template from the given stream.
	 *
	 * @param in Stream to be read.
	 *
	 * @return Template.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static Template read( final InputStream in )
	throws IOException
	{
		final Template result = new Template();

		final XMLInputFactory factory = XMLInputFactory.newFactory();
		try
		{
			final XMLStreamReader reader = factory.createXMLStreamReader( in );

			while ( reader.hasNext() )
			{
				reader.next();
				if ( reader.isStartElement() )
				{
					final QName name = reader.getName();
					if ( equals( name, TEMPLATE_NS_URI, "content" ) )
					{
						readContent( result, reader );
					}
					else if ( equals( name, TEMPLATE_NS_URI, "input" ) )
					{
						readInput( result, reader );
					}
				}
			}
		}
		catch ( XMLStreamException e )
		{
			throw new IOException( e );
		}

		return result;
	}

	/**
	 * Reads a {@code content} element.
	 *
	 * @param template Template to update.
	 * @param reader   Source to read from.
	 *
	 * @throws XMLStreamException if the source document can't be parsed.
	 */
	private static void readContent( final Template template, final XMLStreamReader reader )
	throws XMLStreamException
	{
		reader.require( XMLStreamConstants.START_ELEMENT, TEMPLATE_NS_URI, "content" );
		while ( reader.hasNext() )
		{
			final int type = reader.next();
			if ( type == XMLStreamConstants.END_ELEMENT )
			{
				break;
			}
			else if ( type == XMLStreamConstants.CHARACTERS )
			{
				template.addContent( new CharacterContent( reader.getText() ) );
			}
			else if ( type == XMLStreamConstants.START_ELEMENT )
			{
				final QName name = reader.getName();
				if ( equals( name, TEMPLATE_NS_URI, "cursor" ) )
				{
					template.addContent( new CursorPosition() );
					XMLTools.skipElement( reader );
					reader.require( XMLStreamConstants.END_ELEMENT, TEMPLATE_NS_URI, "cursor" );
				}
				else if ( equals( name, TEMPLATE_NS_URI, "variable" ) )
				{
					final String variable = reader.getAttributeValue( XMLConstants.NULL_NS_URI, "name" );
					template.addContent( new VariableContent( variable ) );
					XMLTools.skipElement( reader );
					reader.require( XMLStreamConstants.END_ELEMENT, TEMPLATE_NS_URI, "variable" );
				}
				else
				{
					XMLTools.skipElement( reader );
				}
			}
		}
	}

	/**
	 * Reads an {@code input} element.
	 *
	 * @param template Template to update.
	 * @param reader   Source to read from.
	 *
	 * @throws XMLStreamException if the source document can't be parsed.
	 */
	private static void readInput( final Template template, final XMLStreamReader reader )
	throws XMLStreamException
	{
		reader.require( XMLStreamConstants.START_ELEMENT, TEMPLATE_NS_URI, "input" );

		final String variable = reader.getAttributeValue( XMLConstants.NULL_NS_URI, "variable" );
		// TODO: reader.getAttributeValue( XMLConstants.NULL_NS_URI, "type" );

		final TemplateInput templateInput = new TemplateInput( variable );
		final LocalizedString message = new LocalizedString();

		while ( reader.hasNext() )
		{
			final int type = reader.next();
			if ( type == XMLStreamConstants.END_ELEMENT )
			{
				break;
			}
			else if ( type == XMLStreamConstants.START_ELEMENT )
			{
				final QName name = reader.getName();
				if ( equals( name, TEMPLATE_NS_URI, "message" ) )
				{
					message.set( XMLTools.readTextContent( reader ) );
					reader.require( XMLStreamConstants.END_ELEMENT, TEMPLATE_NS_URI, "message" );
				}
			}
		}

		if ( !message.isEmpty() )
		{
			templateInput.setMessage( message );
		}

		template.addInput( templateInput );
	}

	/**
	 * Returns whether the given qualified name matches the specified namespace
	 * URI and local part.
	 *
	 * @param qName        Qualified name.
	 * @param namespaceURI Namespace URI to compare to.
	 * @param localPart    Local part to compare to.
	 *
	 * @return {@code true} if the qualified name matches.
	 */
	private static boolean equals( @NotNull final QName qName, @Nullable final String namespaceURI, @NotNull final String localPart )
	{
		return TextTools.equals( namespaceURI, qName.getNamespaceURI() ) &&
		       localPart.equals( qName.getLocalPart() );
	}
}
