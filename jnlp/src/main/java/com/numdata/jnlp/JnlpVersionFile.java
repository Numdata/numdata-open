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

import java.io.*;
import java.util.*;
import javax.xml.stream.*;

import com.numdata.oss.io.*;
import org.jetbrains.annotations.*;

/**
 * Version file for the JNLP version-download protocol.
 *
 * The complete document type definition (DTD) for the version.xml is shown
 * in the following:
 *
 * <pre>
 * &lt;!ELEMENT jnlp-versions (resource*, platform*)&gt;
 * &lt;!ELEMENT resource (pattern, file)&gt;
 * &lt;!ELEMENT platform (pattern, file, product-version-id)&gt;
 * &lt;!ELEMENT pattern (name, version-id, os*, arch*, locale*)&gt;
 * &lt;!ELEMENT name (#PCDATA)&gt;
 * &lt;!ELEMENT version-id (#PCDATA)&gt;
 * &lt;!ELEMENT os (#PCDATA)&gt;
 * &lt;!ELEMENT arch (#PCDATA)&gt;
 * &lt;!ELEMENT locale (#PCDATA)&gt;
 * &lt;!ELEMENT file (#PCDATA)&gt;
 * &lt;!ELEMENT product-version-id (#PCDATA)&gt;
 * </pre>
 *
 * @author G. Meinders
 * @see <a href="http://download.oracle.com/javase/1.4.2/docs/guide/jws/downloadservletguide.html">Packaging
 * JNLP Applications in a Web Archive</a>
 */
public class JnlpVersionFile
{
	/**
	 * Resource entries.
	 */
	private final Collection<Resource> _resources = new ArrayList<Resource>();

	/**
	 * Adds a resource to the file.
	 *
	 * @param name      Name of the resource.
	 * @param versionID Version of the resource.
	 * @param file      Location of the file containing the resource.
	 */
	public void addResource( final String name, final String versionID, final String file )
	{
		addResource( new Resource( name, versionID, file ) );
	}

	/**
	 * Adds a resource to the file. If a resource with the same name already
	 * exists, it will be overwritten.
	 *
	 * @param resource Resource to be added.
	 */
	private void addResource( final Resource resource )
	{
		for ( final Iterator<Resource> it = _resources.iterator(); it.hasNext(); )
		{
			final Resource existing = it.next();
			if ( resource._name.equals( existing._name ) )
			{
				it.remove();
			}
		}

		_resources.add( resource );
	}

	/**
	 * Reads the version file from the given stream. The current content of this
	 * object is replaced.
	 *
	 * @param in Stream to read from.
	 *
	 * @throws XMLStreamException if an I/O error occurs.
	 */
	public void read( final InputStream in )
	throws XMLStreamException
	{
		final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();
		final XMLStreamReader reader = xmlInputFactory.createXMLStreamReader( in );
		reader.nextTag();

		final String tagName = reader.getLocalName();
		if ( "jnlp-versions".equals( tagName ) )
		{
			_resources.clear();
			readJnlpVersions( reader );
		}
		else
		{
			throw new XMLStreamException( "Unexpected root element: " + tagName );
		}

		reader.require( XMLStreamConstants.END_ELEMENT, null, tagName );
	}

	/**
	 * Reads a {@code &lt;jnlp-versions&gt;} element.
	 *
	 * @param reader Reader to read from.
	 *
	 * @throws XMLStreamException if an I/O error occurs.
	 */
	private void readJnlpVersions( final XMLStreamReader reader )
	throws XMLStreamException
	{
		while ( reader.nextTag() != XMLStreamConstants.END_ELEMENT )
		{
			final String tagName = reader.getLocalName();
			if ( "resource".equals( tagName ) )
			{
				final Resource resource = new Resource( "", "", "" );
				readResource( reader, resource );
				addResource( resource );
			}
			else
			{
				XMLTools.skipElement( reader );
			}
		}
	}

	/**
	 * Reads a {@code &lt;resource&gt;} element.
	 *
	 * @param reader   Reader to read from.
	 * @param resource Resource element being read.
	 *
	 * @throws XMLStreamException if an I/O error occurs.
	 */
	private void readResource( final XMLStreamReader reader, final Resource resource )
	throws XMLStreamException
	{
		while ( reader.nextTag() != XMLStreamConstants.END_ELEMENT )
		{
			final String tagName = reader.getLocalName();
			if ( "pattern".equals( tagName ) )
			{
				readPattern( reader, resource );
			}
			else if ( "file".equals( tagName ) )
			{
				resource._file = XMLTools.readTextContent( reader );
			}
			else
			{
				XMLTools.skipElement( reader );
			}
		}
	}

	/**
	 * Reads a {@code &lt;pattern&gt;} element.
	 *
	 * @param reader   Reader to read from.
	 * @param resource Resource element being read.
	 *
	 * @throws XMLStreamException if an I/O error occurs.
	 */
	private void readPattern( final XMLStreamReader reader, final Resource resource )
	throws XMLStreamException
	{
		while ( reader.nextTag() != XMLStreamConstants.END_ELEMENT )
		{
			final String tagName = reader.getLocalName();
			if ( "name".equals( tagName ) )
			{
				resource._name = XMLTools.readTextContent( reader );
			}
			else if ( "version-id".equals( tagName ) )
			{
				resource._versionID = XMLTools.readTextContent( reader );
			}
			else
			{
				XMLTools.skipElement( reader );
			}
		}
	}

	/**
	 * Writes the version file to the given stream.
	 *
	 * @param out Stream to write to.
	 *
	 * @throws XMLStreamException if an I/O error occurs.
	 */
	public void write( final OutputStream out )
	throws XMLStreamException
	{
		final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
		final XMLStreamWriter writer = new IndentingXMLStreamWriter( xmlOutputFactory.createXMLStreamWriter( out, "UTF-8" ) );

		writer.writeStartDocument();
		writer.writeStartElement( "jnlp-versions" );

		for ( final Resource resource : _resources )
		{
			/*
			<resource>
				<pattern>
					<name>...</name>
					<version-id>...</version-id>
				</pattern>
				<file>...</file>
			</resource>
			*/
			writer.writeStartElement( "resource" );
			writer.writeStartElement( "pattern" );
			writer.writeStartElement( "name" );
			writer.writeCharacters( resource._name );
			writer.writeEndElement();
			writer.writeStartElement( "version-id" );
			writer.writeCharacters( resource._versionID );
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeStartElement( "file" );
			writer.writeCharacters( resource._file );
			writer.writeEndElement();
			writer.writeEndElement();
		}

		writer.writeEndElement();
		writer.writeEndDocument();

		writer.flush();
	}

	/**
	 * Represents a {@code &lt;resource&gt;} element.
	 */
	private static class Resource
	{
		/**
		 * Name of the resource.
		 */
		@NotNull
		private String _name;

		/**
		 * Version of the resource.
		 */
		@NotNull
		private String _versionID;

		/**
		 * Location of the file containing the resource.
		 */
		@NotNull
		private String _file;

		/**
		 * Constructs a new instance.
		 *
		 * @param name      Name of the resource.
		 * @param versionID Version of the resource.
		 * @param file      Location of the file containing the resource.
		 */
		private Resource( @NotNull final String name, @NotNull final String versionID, @NotNull final String file )
		{
			_name = name;
			_versionID = versionID;
			_file = file;
		}

		@Override
		public boolean equals( final Object object )
		{
			boolean result = false;

			if ( object == this )
			{
				result = true;
			}
			else if ( object instanceof Resource )
			{
				final Resource resource = (Resource)object;
				result = _name.equals( resource._name ) &&
				         _versionID.equals( resource._versionID ) &&
				         _file.equals( resource._file );
			}

			return result;
		}

		@Override
		public int hashCode()
		{
			return _name.hashCode() ^ _versionID.hashCode() ^ _file.hashCode();
		}

		@Override
		public String toString()
		{
			return "name='" + _name + "', versionID='" + _versionID + "', file='" + _file + '\'';
		}
	}
}
