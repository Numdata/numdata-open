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

import javax.xml.stream.*;

/**
 * Icon that can be used by JNLP clients.
 *
 * The image file can be either GIF or JPEG format, or other (possibly platform
 * dependant) formats. The JNLP Client may assume that a typical JNLP file will
 * have at least an icon in GIF or JPEG format of 32x32 pixels in 256 colors of
 * the default kind.
 *
 * @author Peter S. Heijnen
 */
public class JnlpIcon
extends JnlpElement
{
	/**
	 * Reference to icon image.
	 */
	private String _href = null;

	/**
	 * Version of the image that is requested.
	 */
	private String _version = null;

	/**
	 * Width of image in pixels.
	 */
	private Integer _width = null;

	/**
	 * Height of image in pixels.
	 */
	private Integer _height = null;

	/**
	 * Color depth of the image.
	 */
	private Integer _depth = null;

	/**
	 * Indicates the use of the icon, such as default, selected, disabled,
	 * rollover, splash, and shortcut.
	 */
	private String _kind = null;

	/**
	 * Specifies the download size of the icon in bytes.
	 */
	private Integer _size = null;

	public String getHref()
	{
		return _href;
	}

	public void setHref( final String href )
	{
		_href = href;
	}

	public String getVersion()
	{
		return _version;
	}

	public void setVersion( final String version )
	{
		_version = version;
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

	public Integer getDepth()
	{
		return _depth;
	}

	public void setDepth( final Integer depth )
	{
		_depth = depth;
	}

	public String getKind()
	{
		return _kind;
	}

	public void setKind( final String kind )
	{
		_kind = kind;
	}

	public Integer getSize()
	{
		return _size;
	}

	public void setSize( final Integer size )
	{
		_size = size;
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		out.writeEmptyElement( "icon" );
		writeMandatoryAttribute( out, "href", getHref() );
		writeMandatoryAttribute( out, "version", getVersion() );
		writeOptionalAttribute( out, "width", getWidth() );
		writeOptionalAttribute( out, "height", getHeight() );
		writeOptionalAttribute( out, "kind", getKind() );
		writeOptionalAttribute( out, "depth", getDepth() );
		writeOptionalAttribute( out, "size", getSize() );
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		throw new UnsupportedOperationException( "Not implemented yet." );
	}
}
