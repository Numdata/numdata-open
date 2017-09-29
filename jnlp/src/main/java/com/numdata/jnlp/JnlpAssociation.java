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

/**
 * Hint to the JNLP client that it wishes to be registered with the operating
 * system as the primary handler of certain extensions and a certain mime-type.
 *
 * An application making such a request should be prepared to have its {@code
 * main} method invoked with the arguments {@code -open filename} and {@code
 * -print filename} instead of any arguments listed with the {@code
 * application-desc} element.
 *
 * @author Peter S. Heijnen
 */
public class JnlpAssociation
extends JnlpElement
{
	/**
	 * List of file extensions that the application requests it be registered to
	 * handle.
	 */
	private final List<String> _extensions = new ArrayList<String>();

	/**
	 * A mime-type that the application requests it be registered to handle.
	 */
	private String _mimeType = null;

	/**
	 * Icon that can be used for items of this type.
	 */
	private JnlpIcon _icon = null;

	/**
	 * A short description of the association.
	 */
	private String _description = null;

	/**
	 * Add file extension that the application can handle.
	 *
	 * @param fileExtension File extension to handle.
	 */
	public void addExtension( final String fileExtension )
	{
		_extensions.add( fileExtension );
	}

	/**
	 * Get file extensions that the application can handle.
	 *
	 * @return List of file extensions.
	 */
	public List<String> getExtensions()
	{
		return Collections.unmodifiableList( _extensions );
	}

	/**
	 * Get mime-type that the application requests it be registered to handle.
	 *
	 * @return Mime-type that the application handles.
	 */
	public String getMimeType()
	{
		return _mimeType;
	}

	/**
	 * Set mime-type that the application requests it be registered to handle.
	 *
	 * @param mimeType Mime-type that the application handles.
	 */
	public void setMimeType( final String mimeType )
	{
		_mimeType = mimeType;
	}

	/**
	 * Get icon that can be used for items of this type.
	 *
	 * @return Icon that can be used for items of this type.
	 */
	public JnlpIcon getIcon()
	{
		return _icon;
	}

	/**
	 * Set icon that can be used for items of this type.
	 *
	 * @param icon Icon to use for items of this type.
	 */
	public void setIcon( final JnlpIcon icon )
	{
		_icon = icon;
	}

	/**
	 * Get short description of the association.
	 *
	 * @return Short description of the association.
	 */
	public String getDescription()
	{
		return _description;
	}

	/**
	 * Set short description of the association.
	 *
	 * @param description Short description of the association.
	 */
	public void setDescription( final String description )
	{
		_description = description;
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		final JnlpIcon icon = getIcon();
		final String description = getDescription();

		final boolean emptyAssociationElement = ( icon == null ) && ( description == null );

		if ( emptyAssociationElement )
		{
			out.writeEmptyElement( "association" );
		}
		else
		{
			out.writeStartElement( "association" );
		}

		final List<String> extensions = getExtensions();
		if ( !extensions.isEmpty() )
		{
			final StringBuilder sb = new StringBuilder();

			for ( final String extension : extensions )
			{
				if ( sb.length() > 0 )
				{
					sb.append( ' ' );
				}

				sb.append( extension );
			}

			out.writeAttribute( "extensions", sb.toString() );
		}
		else
		{
			throw new XMLStreamException( "No extensions set" );
		}

		writeMandatoryAttribute( out, "mime-type", getMimeType() );

		if ( icon != null )
		{
			icon.write( out );
		}

		if ( description != null )
		{
			writeOptionalTextElement( out, "description", description );
		}

		if ( !emptyAssociationElement )
		{
			out.writeEndElement();
		}
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		throw new UnsupportedOperationException( "Not implemented yet." );
	}
}
