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
 * This element specifies a JAR file that contains native libraries.
 *
 * Notice that native libraries would typically be included in a {@code
 * resources} element that is intended for a particular operating system and
 * architecture.
 *
 * The JNLP Client must ensure that each file entry in the root directory of the
 * JAR file (i.e. {@code /}) can be loaded into the running process by the
 * {@link System#loadLibrary} method. It is up to the launched application to
 * actually cause the loading of the library (i.e. by calling {@link
 * System#loadLibrary}). Each entry must contain a platform-dependent shared
 * library with the correct naming convention, e.g. {@code *.dll} on Windows, or
 * {@code lib*.so} on Solaris.
 *
 * A JNLP Client ignores all manifest entries in a JAR file specified with the
 * {@code nativelib} element, except the entries used to sign the JAR file.
 *
 * @author Peter S. Heijnen
 */
public class JnlpNativelib
extends JnlpElement
{
	/**
	 * Location of native library file as a URL.
	 */
	private String _href = null;

	/**
	 * Version of a native library file that is requested.
	 */
	private String _version = null;

	/**
	 * Indicates if the resource may be lazily downloaded. If not, the resource
	 * must be downloaded before an application is launched (eager).
	 *
	 * NOTE: A JVM does not generate requests to the class loader when a native
	 * library is missing. Thus, the only way a native library can be triggered
	 * to be downloaded and loaded into the JVM process is by using the {@code
	 * part} attribute. For example, when the Java classes that implement the
	 * native wrappers for the native libraries are downloaded, that can also
	 * trigger the download of the native library.
	 */
	private boolean _lazyDownload = false;

	/**
	 * Size of native library file in bytes.
	 */
	private Integer _size = null;

	/**
	 * The name of the part the native library belongs to.
	 */
	private String _part = null;

	/**
	 * Get location of native library file as a URL.
	 *
	 * @return Location of native library file as a URL.
	 */
	public String getHref()
	{
		return _href;
	}

	/**
	 * Set location of native library file as a URL.
	 *
	 * @param href Location of native library file as a URL.
	 */
	public void setHref( final String href )
	{
		_href = href;
	}

	/**
	 * Get version of a particular native library file that is requested. Note
	 * that the version string is optional, may be a single version, but also a
	 * list of version ranges, please check out the class comment for details.
	 *
	 * @return Version of native library file that is requested; {@code null} if
	 * the version is undetermined.
	 */
	public String getVersion()
	{
		return _version;
	}

	/**
	 * Set version of a particular native library file that is requested. Note
	 * that the version string is optional, may be a single version, but also a
	 * list of version ranges, please check out the class comment for details.
	 *
	 * @param version Version of native library file to request; {@code null} if
	 *                this is undetermined.
	 */
	public void setVersion( final String version )
	{
		_version = version;
	}

	/**
	 * Check if the resource may be lazily downloaded. If not, the resource must
	 * be downloaded before an application is launched (eager).
	 *
	 * @return {@code true} if the resource may be lazily downloaded; {@code
	 * false} if the resource is eagerly downloaded.
	 */
	public boolean isLazyDownload()
	{
		return _lazyDownload;
	}

	/**
	 * Indicate if the resource may be lazily downloaded. If not, the resource
	 * must be downloaded before an application is launched (eager).
	 *
	 * @param enable {@code true} for lazy download; {@code false} for eager
	 *               download.
	 */
	public void setLazyDownload( final boolean enable )
	{
		_lazyDownload = enable;
	}

	/**
	 * Get size of native library file in bytes.
	 *
	 * @return Size of native library file in bytes; {@code null} if
	 * undetermined.
	 */
	public Integer getSize()
	{
		return _size;
	}

	/**
	 * Set size of native library file in bytes.
	 *
	 * @param size Size of native library file in bytes; {@code null} if
	 *             undetermined.
	 */
	public void setSize( final Integer size )
	{
		_size = size;
	}

	/**
	 * Get name of the group the native library belongs to.
	 *
	 * @return Name of the group the native library belongs to; {@code null} if
	 * the native library does not belong to any group.
	 */
	public String getPart()
	{
		return _part;
	}

	/**
	 * Set name of the group the native library belongs to.
	 *
	 * @param part Name of the group the native library belongs to; {@code null}
	 *             if it doesn't belong to any group.
	 */
	public void setPart( final String part )
	{
		_part = part;
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		out.writeEmptyElement( "nativelib" );
		writeMandatoryAttribute( out, "href", getHref() );
		writeOptionalAttribute( out, "version", getVersion() );
		if ( isLazyDownload() )
		{
			writeOptionalAttribute( out, "download", "lazy" );
		}
		writeOptionalAttribute( out, "size", getSize() );
		writeOptionalAttribute( out, "part", getPart() );
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		throw new UnsupportedOperationException( "Not implemented yet." );
	}
}
