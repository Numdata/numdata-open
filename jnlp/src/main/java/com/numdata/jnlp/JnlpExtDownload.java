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
 * Defines when part of an extension is downloaded.
 *
 * @see     JnlpExtension
 *
 * @author  Peter S. Heijnen
 */
public class JnlpExtDownload
	extends JnlpElement
{
	/**
	 * Name of a part in the extension.
	 */
	private String _extPart = null;

	/**
	 * Indicates if the resource may be lazily downloaded. If not, the
	 * resource must be downloaded before an application is launched
	 * (eager).
	 */
	private boolean _lazyDownload = false;

	/**
	 * Name of the part it belongs to in the current JNLP file.
	 */
	private String _part = null;

	/**
	 * Get name of extension part.
	 *
	 * @return  Name of extension part.
	 */
	public String getExtPart()
	{
		return _extPart;
	}

	/**
	 * Set name of extension part.
	 *
	 * @param   extPart     Name of extension part.
	 */
	public void setExtPart( final String extPart )
	{
		_extPart = extPart;
	}

	/**
	 * Check if the resource may be lazily downloaded. If not, the resource
	 * must be downloaded before an application is launched (eager).
	 *
	 * @return  {@code true} if the file may be lazily downloaded;
	 *          {@code false} if the file is eagerly downloaded.
	 */
	public boolean isLazyDownload()
	{
		return _lazyDownload;
	}

	/**
	 * Indicate if the resource may be lazily downloaded. If not, the
	 * resource must be downloaded before an application is launched
	 * (eager).
	 *
	 * @param   enable  {@code true} for lazy download;
	 *                  {@code false} for eager download.
	 */
	public void setLazyDownload( final boolean enable )
	{
		_lazyDownload = enable;
	}

	/**
	 * Get name of part in the current JNLP file the extension part belongs to.
	 *
	 * @return  Name of part in the current JNLP file.
	 */
	public String getPart()
	{
		return _part;
	}

	/**
	 * Set name of part in the current JNLP file the extension part belongs to.
	 *
	 * @param   part    Name of part in the current JNLP file.
	 */
	public void setPart( final String part )
	{
		_part = part;
	}

	@Override
	public void write( final XMLStreamWriter out )
		throws XMLStreamException
	{
		out.writeEmptyElement( "ext-download" );
		writeMandatoryAttribute( out, "ext-part", getExtPart() );
		if ( isLazyDownload() )
		{
			writeOptionalAttribute( out, "download", "lazy" );
		}
		writeOptionalAttribute( out, "part", getPart() );
	}

	@Override
	public void read( final XMLStreamReader in )
		throws XMLStreamException
	{
		throw new UnsupportedOperationException( "Not implemented yet." );
	}
}
