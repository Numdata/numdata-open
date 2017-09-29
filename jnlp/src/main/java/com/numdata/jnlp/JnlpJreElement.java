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
 * Description of a supported JRE version, standard parameters to the Java
 * Virtual Machine, and an optional {@code resources} element to be used by a
 * particular JRE.
 *
 * @author Peter S. Heijnen
 */
public class JnlpJreElement
extends JnlpElement
{
	/**
	 * Name of XML element for JRE ('j2se' [old] or 'java' [new]).
	 */
	private final String _xmlName;

	/**
	 * Versions of the JRE that this application is supported on.
	 *
	 * Versions are typically specified using a prefix or a greater-than
	 * notation, i.e. be post-fixed with either a plus ({@code +}) or an
	 * asterisk ({@code *}).
	 *
	 * If no {@code href} is specified, the version refers to a platform version
	 * of the Java 2 platform (e.g. '1.6+'); if an {@code href} is specified,
	 * the versions refers to a vendor-specific product version.
	 */
	private final List<String> _versions = new ArrayList<String>();

	/**
	 * Get location where the JRE should be downloaded from. If this is set, a
	 *
	 * If no {@code href} is specified, the JRE version refers to a Java
	 * platform version; if an {@code href} is specified, a vendor-specific JRE
	 * is requested, uniquely named using the {@code href} and version.
	 */
	private String _href = null;

	/**
	 * Initial size of the Java heap. The modifiers {@code m} and {@code k} (not
	 * case-sensitive) can be used for megabytes and kilobytes, respectively
	 * (e.g. '128m').
	 */
	private String _initialHeapSize = null;

	/**
	 * Maximum size of the Java heap. The modifiers {@code m} and {@code k} (not
	 * case-sensitive) can be used for megabytes and kilobytes, respectively
	 * (e.g. '128m').
	 */
	private String _maxHeapSize = null;

	/**
	 * Set of virtual machine arguments to use when launching java.
	 */
	private final List<String> _javaVmArgs = new ArrayList<String>();

	/**
	 * Resources for this JRE version. These resources are all ignored, except
	 * the ones in the JRE element that are used to launch the application.
	 */
	private final List<JnlpResources> _resourcesList = new ArrayList<JnlpResources>();

	/**
	 * Create {@code java} element.
	 */
	public JnlpJreElement()
	{
		this( "java" );
	}

	/**
	 * Create JRE element.
	 *
	 * @param xmlName Name of XML element for JRE ('j2se' or 'java').
	 */
	public JnlpJreElement( final String xmlName )
	{
		_xmlName = xmlName;
	}

	public String getXmlName()
	{
		return _xmlName;
	}

	/**
	 * Add version of the JRE that this application is supported on.
	 *
	 * Versions are typically specified using a prefix or a greater-than
	 * notation, i.e. be post-fixed with either a plus ({@code +}) or an
	 * asterisk ({@code *}).
	 *
	 * If no {@code href} is specified, the version refers to a platform version
	 * of the Java 2 platform (e.g. '1.6+'); if an {@code href} is specified,
	 * the versions refers to a vendor-specific product version.
	 *
	 * @param version Version JRE that this application is supported on.
	 */
	public void addVersion( final String version )
	{
		_versions.add( version );
	}

	public List<String> getVersions()
	{
		return Collections.unmodifiableList( _versions );
	}

	public String getHref()
	{
		return _href;
	}

	public void setHref( final String href )
	{
		_href = href;
	}

	public String getInitialHeapSize()
	{
		return _initialHeapSize;
	}

	public void setInitialHeapSize( final String heapSize )
	{
		_initialHeapSize = heapSize;
	}

	public String getMaxHeapSize()
	{
		return _maxHeapSize;
	}

	public void setMaxHeapSize( final String heapSize )
	{
		_maxHeapSize = heapSize;
	}

	/**
	 * Add Java virtual machine argument to use when launching java.
	 *
	 * @param javaVmArg Java virtual machine argument to add.
	 */
	public void addJavaVmArg( final String javaVmArg )
	{
		_javaVmArgs.add( javaVmArg );
	}

	public List<String> getJavaVmArgs()
	{
		return Collections.unmodifiableList( _javaVmArgs );
	}

	/**
	 * Add resources section for this JRE version. These resources are ignored
	 * if this JRE is not used.
	 *
	 * @return Resources section that was added.
	 */
	public JnlpResources addResources()
	{
		final JnlpResources result = new JnlpResources();
		_resourcesList.add( result );
		return result;
	}

	public List<JnlpResources> getResourcesList()
	{
		return Collections.unmodifiableList( _resourcesList );
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		final List<JnlpResources> resourcesList = getResourcesList();
		final boolean writeEmptyElement = resourcesList.isEmpty();

		if ( writeEmptyElement )
		{
			out.writeEmptyElement( getXmlName() );
		}
		else
		{
			out.writeStartElement( getXmlName() );
		}

		final List<String> versions = getVersions();
		if ( !versions.isEmpty() )
		{
			final StringBuilder sb = new StringBuilder();

			for ( final String version : versions )
			{
				if ( sb.length() > 0 )
				{
					sb.append( ' ' );
				}

				sb.append( version );
			}

			out.writeAttribute( "version", sb.toString() );
		}
		else
		{
			throw new XMLStreamException( "No version set" );
		}

		writeOptionalAttribute( out, "href", getHref() );
		writeOptionalAttribute( out, "initial-heap-size", getInitialHeapSize() );
		writeOptionalAttribute( out, "max-heap-size", getMaxHeapSize() );

		final List<String> javaVmArgs = getJavaVmArgs();
		if ( !javaVmArgs.isEmpty() )
		{
			final StringBuilder sb = new StringBuilder();

			for ( final String javaVmArg : javaVmArgs )
			{
				if ( sb.length() > 0 )
				{
					sb.append( ' ' );
				}

				sb.append( javaVmArg );
			}

			out.writeAttribute( "java-vm-args", sb.toString() );
		}

		for ( final JnlpResources resources : resourcesList )
		{
			resources.write( out );
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
		throw new UnsupportedOperationException( "Not implemented yet." );
	}
}
