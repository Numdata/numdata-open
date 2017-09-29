/*
 * Copyright (c) 2017, Numdata BV, The Netherlands.
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
package com.numdata.jnlp;

import java.util.*;
import javax.xml.stream.*;

import com.numdata.oss.io.*;
import com.numdata.oss.log.*;

/**
 * Application descriptor for an application. This contains all information
 * needed to launch an application, given the resources described by the {@code
 * resources} element.
 *
 * @author Peter S. Heijnen
 */
public class JnlpApplicationDesc
extends JnlpElement
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JnlpApplicationDesc.class );

	/**
	 * The name of the class containing the {@code main} method of the
	 * application. This attribute can be omitted if the {@code main} class can
	 * be found from the {@code Main-Class} manifest entry in the main JAR
	 * file.
	 */
	private String _mainClass = null;

	/**
	 * Ordered list of arguments for the application.
	 */
	private List<String> _argumentList = new ArrayList<String>();

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		setMainClass( in.getAttributeValue( null, "main-class" ) );

		while ( in.nextTag() != XMLStreamConstants.END_ELEMENT )
		{
			final String tagName = in.getLocalName();
			if ( "argument".equals( tagName ) )
			{
				addArgument( XMLTools.readTextContent( in ) );
			}
			else
			{
				LOG.warn( "Unsupported element: " + tagName );
				XMLTools.skipElement( in );
			}
		}
	}

	/**
	 * Get name of the class containing the {@code main} method of the
	 * application. This attribute can be omitted if the {@code main} class can
	 * be found from the {@code Main-Class} manifest entry in the main JAR
	 * file.
	 *
	 * @return Name of class containing the {@code main} method; {@code null} if
	 * derived from the JAR file manifest.
	 */
	public String getMainClass()
	{
		return _mainClass;
	}

	/**
	 * Set name of the class containing the {@code main} method of the
	 * application. This attribute can be omitted if the {@code main} class can
	 * be found from the {@code Main-Class} manifest entry in the main JAR
	 * file.
	 *
	 * @param mainClass Name of class containing the {@code main} method; {@code
	 *                  null} to derive from the JAR file manifest.
	 */
	public void setMainClass( final String mainClass )
	{
		_mainClass = mainClass;
	}

	/**
	 * Get list of arguments for the application.
	 *
	 * @return Arguments for the application.
	 */
	public List<String> getArguments()
	{
		return Collections.unmodifiableList( _argumentList );
	}

	/**
	 * Add argument to the application.
	 *
	 * @param argument Argument to add.
	 */
	public void addArgument( final String argument )
	{
		_argumentList.add( argument );
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		final List<String> arguments = getArguments();

		final boolean writeEmptyElement = arguments.isEmpty();
		if ( writeEmptyElement )
		{
			out.writeEmptyElement( "application-desc" );
		}
		else
		{
			out.writeStartElement( "application-desc" );
		}

		writeOptionalAttribute( out, "main-class", getMainClass() );

		for ( final String argument : arguments )
		{
			out.writeStartElement( "argument" );
			out.writeCharacters( argument );
			out.writeEndElement();
		}

		if ( !writeEmptyElement )
		{
			out.writeEndElement();
		}
	}
}
