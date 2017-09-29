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

import com.numdata.oss.io.*;

/**
 * Description of a JAR file that is part of the application's classpath. The
 * JAR file will be loaded into the JVM using a {@link ClassLoader} object. The
 * JAR file will typically contain Java classes that contain the code for the
 * particular application, but can also contain other resources, such as icons
 * and configuration files, that are available through the {@link
 * ClassLoader#getResource} mechanism.
 *
 * A JNLP Client ignores all manifest entries in a JAR file specified with the
 * {@code jar} element, except the following:
 *
 * <ul>
 *
 * <li>The manifest entries used to sign a JAR file are recognized and
 * validated.</li>
 *
 * <li>The {@code Main-Class} entry in the JAR file specified as main is used to
 * determine the main class of an application (if it is not specified explicitly
 * in the JNLP file).</li>
 *
 * <li>The manifest entries used to seal a package are recognized, and the
 * sealing of packages are verified. These are the {@code name} and {@code
 * sealed} entries.</li>
 *
 * <li>The following manifest entries described by the Optional Package
 * Versioning documentation: {@code Extension-Name}, {@code
 * Specification-Vendor}, {@code Specification-Version}, {@code
 * Implementation-Vendor-Id}, {@code Implementation-Vendor}, and {@code
 * Implementation-Version} are recognized and will be available through the
 * {@link Package} class. They are otherwise not used by a JNLP Client.</li>
 *
 * </ul>
 */
public class JnlpJar
extends JnlpElement
{
	/**
	 * Location of a jar file as a URL.
	 */
	private String _href = null;

	/**
	 * Version of a particular JAR file that is requested.
	 */
	private String _version = null;

	/**
	 * Indicates whether this element contains the main class of the
	 * application/applet (or installer for an extension). There must be at most
	 * one jar element in a JNLP file that is specified as main. If no jar
	 * element is specified as main, then the first JAR element will be
	 * considered the main JAR file.
	 */
	private Boolean _main = null;

	/**
	 * Indicates if the resource may be lazily downloaded. If not, the resource
	 * must be downloaded before an application is launched (eager).
	 */
	private boolean _lazyDownload = false;

	/**
	 * Size of a JAR file in bytes.
	 */
	private Integer _size = null;

	/**
	 * The name of the group the jar belongs to.
	 */
	private String _part = null;

	/**
	 * Get location of a jar file as a URL.
	 *
	 * @return Location of a jar file as a URL.
	 */
	public String getHref()
	{
		return _href;
	}

	/**
	 * Set location of a jar file as a URL.
	 *
	 * @param href Location of a jar file as a URL.
	 */
	public void setHref( final String href )
	{
		_href = href;
	}

	/**
	 * Get version of a particular JAR file that is requested. Note that the
	 * version string is optional, may be a single version, but also a list of
	 * version ranges, please check out the class comment for details.
	 *
	 * @return Version of a JAR file that is requested; {@code null} if the
	 * version is undetermined.
	 */
	public String getVersion()
	{
		return _version;
	}

	/**
	 * Set version of a particular JAR file that is requested. Note that the
	 * version string is optional, may be a single version, but also a list of
	 * version ranges, please check out the class comment for details.
	 *
	 * @param version Version of a JAR file that is requested; {@code null} if
	 *                this is undetermined.
	 */
	public void setVersion( final String version )
	{
		_version = version;
	}

	/**
	 * Check whether this element contains the main class of the
	 * application/applet (or installer for an extension). There must be at most
	 * one jar element in a JNLP file that is specified as main. If no jar
	 * element is specified as main, then the first JAR element will be
	 * considered the main JAR file.
	 *
	 * @return {@link Boolean#TRUE} if the JAR contains the main class; {@link
	 * Boolean#FALSE} if the JAR does not the main class; {@code null} if this
	 * is undetermined.
	 */
	public Boolean getMain()
	{
		return _main;
	}

	/**
	 * Indicate whether this element contains the main class of the
	 * application/applet (or installer for an extension). There must be at most
	 * one jar element in a JNLP file that is specified as main. If no jar
	 * element is specified as main, then the first JAR element will be
	 * considered the main JAR file.
	 *
	 * @param main {@link Boolean#TRUE} if the JAR contains the main class;
	 *             {@link Boolean#FALSE} if the JAR does not the main class;
	 *             {@code null} if this is undetermined.
	 */
	public void setMain( final Boolean main )
	{
		_main = main;
	}

	/**
	 * Check if the resource may be lazily downloaded. If not, the resource must
	 * be downloaded before an application is launched (eager).
	 *
	 * @return {@code true} if the file may be lazily downloaded; {@code false}
	 * if the file is eagerly downloaded.
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
	 * Get size of a JAR file in bytes.
	 *
	 * @return Size of a JAR file in bytes; {@code null} if undetermined.
	 */
	public Integer getSize()
	{
		return _size;
	}

	/**
	 * Set size of a JAR file in bytes.
	 *
	 * @param size Size of a JAR file in bytes; {@code null} if undetermined.
	 */
	public void setSize( final Integer size )
	{
		_size = size;
	}

	/**
	 * Get name of the group the jar belongs to.
	 *
	 * @return Name of the group the JAR belongs to; {@code null} if the JAR
	 * does not belong to any group.
	 */
	public String getPart()
	{
		return _part;
	}

	/**
	 * Set name of the group the jar belongs to.
	 *
	 * @param part Name of the group the JAR belongs to; {@code null} if it
	 *             doesn't belong to any group.
	 */
	public void setPart( final String part )
	{
		_part = part;
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		out.writeEmptyElement( "jar" );
		writeOptionalAttribute( out, "part", getPart() );
		writeMandatoryAttribute( out, "href", getHref() );
		writeOptionalAttribute( out, "version", getVersion() );
		writeOptionalAttribute( out, "main", getMain() );
		writeOptionalAttribute( out, "size", getSize() );
		if ( isLazyDownload() )
		{
			writeOptionalAttribute( out, "download", "lazy" );
		}
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		setHref( in.getAttributeValue( null, "href" ) );
		setVersion( in.getAttributeValue( null, "version" ) );

		final String main = in.getAttributeValue( null, "main" );
		if ( main != null )
		{
			setMain( Boolean.valueOf( main ) );
		}

		setLazyDownload( "lazy".equals( in.getAttributeValue( null, "download" ) ) );

		final String size = in.getAttributeValue( null, "size" );
		if ( size != null )
		{
			setSize( Integer.valueOf( size ) );
		}

		setPart( in.getAttributeValue( null, "part" ) );

		XMLTools.skipElement( in );
	}
}
