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

import javax.xml.stream.*;

/**
 * Describes an additional piece of related content, such as a readme
 * file, help pages, or links to registration pages. The application is
 * asking that this content be included in its desktop integration.
 *
 * @author  Peter S. Heijnen
 */
public class JnlpRelatedContent
	extends JnlpElement
{
	/**
	 * Reference to the related content.
	 */
	private String _href = null;

	/**
	 * Name of the related content.
	 */
	private String _title = null;

	/**
	 * A short description of the related content.
	 */
	private String _description = null;

	/**
	 * Icon that can be used to identify the related content to the user.
	 */
	private JnlpIcon _icon = null;

	/**
	 * Get reference to the related content.
	 *
	 * @return  Reference to the related content.
	 */
	public String getHref()
	{
		return _href;
	}

	/**
	 * Get reference to the related content.
	 *
 	 * @param   href    Reference to the related content.
	 */
	public void setHref( final String href )
	{
		_href = href;
	}

	/**
	 * Get name of related content.
	 *
	 * @return  Name of related content.
	 */
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Get name of related content.
	 *
 	 * @param   title   Name of related content.
	 */
	public void setTitle( final String title )
	{
		_title = title;
	}

	/**
	 * Get name of related content.
	 *
	 * @return  Name of related content.
	 */
	public String getDescription()
	{
		return _description;
	}

	/**
	 * Get name of related content.
	 *
	 * @param   description     Name of related content.
	 */
	public void setDescription( final String description )
	{
		_description = description;
	}

	/**
	 * Get icon to identify related content to the user.
	 *
	 * @return  Icon to identify related content to the user.
	 */
	public JnlpIcon getIcon()
	{
		return _icon;
	}

	/**
	 * Get icon to identify related content to the user.
	 *
	 * @param   icon    Icon to identify related content to the user.
	 */
	public void setIcon( final JnlpIcon icon )
	{
		_icon = icon;
	}

	@Override
	public void write( final XMLStreamWriter out )
		throws XMLStreamException
	{
		final String title = getTitle();
		final String description = getDescription();
		final JnlpIcon icon = getIcon();

		final boolean emptyRelatedContentElement = ( title == null ) && ( description == null ) && ( icon == null );

		if ( emptyRelatedContentElement )
		{
			out.writeEmptyElement( "related-content" );
		}
		else
		{
			out.writeStartElement( "related-content" );
		}

		writeMandatoryAttribute( out, "href", getHref() );

		writeOptionalTextElement( out, "title", title );
		writeOptionalTextElement( out, "description", description );

		if ( icon != null )
		{
			icon.write( out );
		}

		if ( !emptyRelatedContentElement )
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
