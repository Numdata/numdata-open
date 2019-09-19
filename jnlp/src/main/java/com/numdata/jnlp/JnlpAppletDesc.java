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
package com.numdata.jnlp;

import java.util.*;
import javax.xml.stream.*;

import com.numdata.oss.io.*;
import com.numdata.oss.log.*;

/**
 * Application descriptor for an applet. This contains all information needed to
 * launch an Applet, given the resources described by the {@code resources}
 * elements.
 *
 * The JNLP Client may override the applet parameters, or complete a relative
 * codebase, by using information available in its environment. For example, a
 * JNLP Client that is implemented as a browser plugin can complete a relative
 * codebase using the address of the page the applet is on, or override applet
 * parameters with applet parameters specified on the page.
 *
 * @author Peter S. Heijnen
 */
public class JnlpAppletDesc
extends JnlpElement
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JnlpAppletDesc.class );

	/**
	 * The document base for the applet as a URL. This is available to the
	 * applet through the {@link java.applet.AppletContext}. The document base
	 * can be provided explicitly since an applet launched with a JNLP Client
	 * may not be embedded in a web page.
	 */
	private String _documentbase = null;

	/**
	 * Name of the {@code main} applet class.
	 */
	private String _mainClass = null;

	/**
	 * Name of the applet. This is available to the applet through the {@link
	 * java.applet.AppletContext}.
	 */
	private String _name = null;

	/**
	 * Width of the applet in pixels. The JNLP Client may set the actual size to
	 * be more or less than the specified size, to fit within the minimum and
	 * maximum dimensions for a Java Frame on each platform.
	 */
	private Integer _width = null;

	/**
	 * Height of the applet in pixels. The JNLP Client may set the actual size
	 * to be more or less than the specified size, to fit within the minimum and
	 * maximum dimensions for a Java Frame on each platform.
	 */
	private Integer _height = null;

	/**
	 * Parameters to the applet.
	 */
	private final Map<String, String> _parameters = new LinkedHashMap<String, String>();

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		setMainClass( in.getAttributeValue( null, "main-class" ) );
		setName( in.getAttributeValue( null, "name" ) );

		final String width = in.getAttributeValue( null, "width" );
		if ( width != null )
		{
			setWidth( Integer.valueOf( width ) );
		}

		final String height = in.getAttributeValue( null, "height" );
		if ( height != null )
		{
			setHeight( Integer.valueOf( height ) );
		}

		setDocumentbase( in.getAttributeValue( null, "documentbase" ) );

		while ( in.nextTag() != XMLStreamConstants.END_ELEMENT )
		{
			final String tagName = in.getLocalName();
			if ( "param".equals( tagName ) )
			{
				final String name = in.getAttributeValue( null, "name" );
				final String value = in.getAttributeValue( null, "value" );
				setParameter( name, value );
			}
			else
			{
				LOG.warn( "Unsupported element: " + tagName );
				XMLTools.skipElement( in );
			}
		}
	}

	public String getDocumentbase()
	{
		return _documentbase;
	}

	public void setDocumentbase( final String documentbase )
	{
		_documentbase = documentbase;
	}

	public String getMainClass()
	{
		return _mainClass;
	}

	public void setMainClass( final String mainClass )
	{
		_mainClass = mainClass;
	}

	public String getName()
	{
		return _name;
	}

	public void setName( final String name )
	{
		_name = name;
	}

	public Integer getWidth()
	{
		return _width;
	}

	public void setWidth( final Integer width )
	{
		_width = width;
	}

	public Integer getHeight()
	{
		return _height;
	}

	public void setHeight( final Integer height )
	{
		_height = height;
	}

	/**
	 * Get applet parameter names.
	 *
	 * @return Applet parameter names.
	 */
	public Set<String> getParameterNames()
	{
		return Collections.unmodifiableSet( _parameters.keySet() );
	}

	/**
	 * Get applet parameter.
	 *
	 * @param name Parameter name.
	 *
	 * @return Parameter value; {@code null} if parameter is undefined.
	 */
	public String getParameter( final String name )
	{
		return _parameters.get( name );
	}

	/**
	 * Set applet parameter.
	 *
	 * @param name  Parameter name.
	 * @param value Parameter value.
	 */
	public void setParameter( final String name, final String value )
	{
		_parameters.put( name, value );
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		final String mainClass = getMainClass();
		final Map<String, String> parameters = _parameters;

		out.writeStartElement( "applet-desc" );
		writeOptionalAttribute( out, "documentbase", getDocumentbase() );
		writeOptionalAttribute( out, "main-class", mainClass );
		writeOptionalAttribute( out, "name", getName() );
		writeOptionalAttribute( out, "width", getWidth() );
		writeOptionalAttribute( out, "height", getHeight() );

		for ( final Map.Entry<String, String> parameter : parameters.entrySet() )
		{
			out.writeEmptyElement( "parameter" );
			out.writeAttribute( "name", parameter.getKey() );
			out.writeAttribute( "value", parameter.getValue() );
		}

		out.writeEndElement();
	}
}
