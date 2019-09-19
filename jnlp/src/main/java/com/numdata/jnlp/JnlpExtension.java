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
 * An extension that is required in order to run the application.
 *
 * The inclusion of an extension element in a resources element has the
 * following effect:
 *
 * <ul>
 *
 * <li>If it points to a component extension (i.e. a JNLP file with a {@code
 * component-desc} element), then the resources described in the resources
 * element in that JNLP file become part of the application's resources. The
 * included resources will have the permissions specified in the component
 * extension.</li>
 *
 * <li>If the extension points to an extension installer (i.e. a JNLP file with
 * an {@code installer-desc} element), then the installer application will be
 * executed, if it has not already been executed on the local machine.</li>
 *
 * </ul>
 *
 * @author Peter S. Heijnen
 */
public class JnlpExtension
extends JnlpElement
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JnlpExtension.class );

	/**
	 * Name of the extension. This can be used by the JNLP Client to inform the
	 * user about the particular extension that is required, while the extension
	 * descriptor (i.e., the JNLP file) is being downloaded.
	 */
	private String _name = null;

	/**
	 * Location of the extension.
	 */
	private String _href = null;

	/**
	 * The version of the extension requested.
	 */
	private String _version = null;

	/**
	 * Defines when parts of the extension are downloaded. Note that a JNLP
	 * client is always allowed to eagerly download all parts if it chooses.
	 */
	private final List<JnlpExtDownload> _extDownloadList = new ArrayList<JnlpExtDownload>();

	public String getName()
	{
		return _name;
	}

	public void setName( final String name )
	{
		_name = name;
	}

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

	/**
	 * Add extension part download definition.
	 *
	 * @return Extension part download definition that was added.
	 */
	public JnlpExtDownload addExtDownload()
	{
		final JnlpExtDownload result = new JnlpExtDownload();
		_extDownloadList.add( result );
		return result;
	}

	public List<JnlpExtDownload> getExtDownloadList()
	{
		return Collections.unmodifiableList( _extDownloadList );
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		final List<JnlpExtDownload> extDownloads = getExtDownloadList();
		final boolean writeEmptyElement = extDownloads.isEmpty();

		if ( writeEmptyElement )
		{
			out.writeEmptyElement( "extension" );
		}
		else
		{
			out.writeStartElement( "extension" );
		}

		writeOptionalAttribute( out, "name", getName() );
		writeMandatoryAttribute( out, "href", getHref() );
		writeOptionalAttribute( out, "version", getVersion() );

		for ( final JnlpExtDownload extDownload : extDownloads )
		{
			extDownload.write( out );
		}

		if ( !writeEmptyElement )
		{
			out.writeEndElement();
		}
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		setName( in.getAttributeValue( null, "name" ) );
		setHref( in.getAttributeValue( null, "href" ) );
		setVersion( in.getAttributeValue( null, "version" ) );

		while ( in.nextTag() != XMLStreamConstants.END_ELEMENT )
		{
			final String tagName = in.getLocalName();
			if ( "ext-download".equals( tagName ) )
			{
				final JnlpExtDownload extDownload = addExtDownload();
				extDownload.setExtPart( in.getAttributeValue( null, "ext-part" ) );
				extDownload.setLazyDownload( "lazy".equals( in.getAttributeValue( null, "download" ) ) );
				extDownload.setPart( in.getAttributeValue( null, "part" ) );
			}
			else
			{
				LOG.warn( "Unsupported element: " + tagName );
				XMLTools.skipElement( in );
			}
		}
	}
}
