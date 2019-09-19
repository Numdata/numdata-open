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

import com.numdata.oss.*;
import com.numdata.oss.io.*;
import com.numdata.oss.log.*;

/**
 * Contains an ordered set of resources that constitutes an application.
 * Resources can be restricted to a specific operating system, architecture, or
 * locale.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "UnusedReturnValue" )
public class JnlpResources
extends JnlpElement
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JnlpResources.class );

	/**
	 * Operating systems to which the resources apply. If a value is a prefix of
	 * the {@code os.name} system property, then the resources can be used. If
	 * no operating system is specified, all operating systems are matched.
	 */
	private final List<String> _osList = new ArrayList<String>();

	/**
	 * Architectures to which the resources apply. If a value is a prefix of the
	 * {@code os.arch} system property, then the resources can be used. If no
	 * architecture is specified, all architectures are matched.
	 */
	private final List<String> _archList = new ArrayList<String>();

	/**
	 * Locales to which the resources apply.
	 */
	private final List<Locale> _localeList = new ArrayList<Locale>();

	/**
	 * System properties that will be available through the {@link
	 * System#getProperty} and {@link System#getProperties} methods.
	 */
	private final Map<String, String> _properties = new LinkedHashMap<String, String>();

	/**
	 * JAR file code resources.
	 */
	private final List<JnlpJar> _jarList = new ArrayList<JnlpJar>();

	/**
	 * Native library JAR file code resources.
	 */
	private final List<JnlpNativelib> _nativelibList = new ArrayList<JnlpNativelib>();

	/**
	 * Prioritized list of supported JREs (most preferred first ).
	 */
	private final List<JnlpJreElement> _jreList = new ArrayList<JnlpJreElement>();

	/**
	 * Extensions that are required in order to run the application.
	 */
	private final List<JnlpExtension> _extensionList = new ArrayList<JnlpExtension>();

	/**
	 * Lists which packages or classes are implemented in which JAR files.
	 */
	private final List<JnlpPackage> _packageList = new ArrayList<JnlpPackage>();

	/**
	 * Add operating systems for which the resources should be considered. The
	 * value matches if it is a prefix of the {@code os.name} system property.
	 * If no operating system is specified, any operating system matches.
	 *
	 * @param os Operating system to add.
	 */
	public void addOs( final String os )
	{
		_osList.add( os );
	}

	public List<String> getOsList()
	{
		return Collections.unmodifiableList( _osList );
	}

	/**
	 * Add architecture for which the resources should be considered. The value
	 * matches if it is a prefix of the {@code os.arch} system property. If no
	 * architecture is specified, any architecture matches.
	 *
	 * @param arch Architecture to add.
	 */
	public void addArch( final String arch )
	{
		_archList.add( arch );
	}

	public List<String> getArchList()
	{
		return Collections.unmodifiableList( _archList );
	}

	/**
	 * Add locale for which the resources should be used. The given locale is
	 * matched against the default locale for the JNLP client. If no locale is
	 * specified, any locale matches.
	 *
	 * @param locale Locale to add.
	 */
	public void addLocale( final Locale locale )
	{
		_localeList.add( locale );
	}

	public List<Locale> getLocaleList()
	{
		return Collections.unmodifiableList( _localeList );
	}

	/**
	 * Set system properties that will be available to the application.
	 *
	 * Properties must be processed in the order specified in the JNLP file.
	 * Thus, if two properties define different values for the same property,
	 * then the last value specified in the JNLP file is used.
	 *
	 * For an untrusted application system properties set in the JNLP file may
	 * only be set by the JNLP Client if they are considered secure. At a
	 * minimum a JNLP Client will allow setting system properties whose name
	 * starts with "jnlp".
	 *
	 * Properties set in the JNLP file may be set by the JNLP Client at any time
	 * before the application's code is executed; thus, it cannot be assumed
	 * that they will be set during the initialization of the virtual machine.
	 *
	 * Properties set in an extension JNLP file may be set any time before code
	 * from that extension is executed.
	 *
	 * @param name  Property name.
	 * @param value Property value.
	 */
	public void setProperty( final String name, final String value )
	{
		_properties.put( name, value );
	}

	public Map<String, String> getProperties()
	{
		return Collections.unmodifiableMap( _properties );
	}

	/**
	 * Add JAR file code resource.
	 *
	 * @return {@link JnlpJar} that was added.
	 */
	public JnlpJar addJar()
	{
		final JnlpJar result = new JnlpJar();
		_jarList.add( result );
		return result;
	}

	public List<JnlpJar> getJarList()
	{
		return Collections.unmodifiableList( _jarList );
	}

	/**
	 * Add native library JAR file code resource.
	 *
	 * @return {@link JnlpNativelib} that was added.
	 */
	public JnlpNativelib addNativelib()
	{
		final JnlpNativelib result = new JnlpNativelib();
		_nativelibList.add( result );
		return result;
	}

	public List<JnlpNativelib> getNativelibList()
	{
		return Collections.unmodifiableList( _nativelibList );
	}

	/**
	 * Add entry to specify which packages or classes are implemented in which
	 * JAR files.
	 *
	 * @return {@link JnlpPackage} that was added.
	 */
	public JnlpPackage addPackage()
	{
		final JnlpPackage result = new JnlpPackage();
		_packageList.add( result );
		return result;
	}

	/**
	 * Convenience method to add an entry to specify which packages or classes
	 * are implemented in which JAR files.
	 *
	 * @param name      Package or class name (e.g. 'org.example.*',
	 *                  'org.example.MyClass').
	 * @param part      Part that must be downloaded for the package.
	 * @param recursive If set, also match sub-packages.
	 *
	 * @return {@link JnlpPackage} that was added.
	 */
	public JnlpPackage addPackage( final String name, final String part, final boolean recursive )
	{
		final JnlpPackage result = addPackage();
		result.setName( name );
		result.setPart( part );
		if ( recursive )
		{
			result.setRecursive( Boolean.TRUE );
		}
		return result;
	}

	public List<JnlpPackage> getPackageList()
	{
		return Collections.unmodifiableList( _packageList );
	}

	/**
	 * Add supported java runtime environment (JRE). JRE entries are
	 * prioritized, most preferred JRE's should be added first.
	 *
	 * @return {@link JnlpJreElement} that was added.
	 */
	public JnlpJreElement addJava()
	{
		final JnlpJreElement result = new JnlpJreElement();
		_jreList.add( result );
		return result;
	}

	/**
	 * Add supported java runtime environment (JRE). JRE entries are
	 * prioritized, most preferred JRE's should be added first.
	 *
	 * This method creates an element named {@code j2se}, which is
	 * interchangeable with the {@code java} element. The {@code j2se} element
	 * is kept for backward compatibility.
	 *
	 * @return {@link JnlpJreElement} that was added.
	 */
	public JnlpJreElement addJ2se()
	{
		final JnlpJreElement result = new JnlpJreElement( "j2se" );
		_jreList.add( result );
		return result;
	}

	public List<JnlpJreElement> getJreList()
	{
		return Collections.unmodifiableList( _jreList );
	}

	/**
	 * Add extensions that is required in order to run the application.
	 *
	 * @return {@link JnlpExtension} that was added.
	 */
	public JnlpExtension addExtension()
	{
		final JnlpExtension result = new JnlpExtension();
		_extensionList.add( result );
		return result;
	}

	public List<JnlpExtension> getExtensionList()
	{
		return Collections.unmodifiableList( _extensionList );
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		out.writeStartElement( "resources" );

		if ( !_osList.isEmpty() )
		{
			final StringBuilder sb = new StringBuilder();

			for ( final String os : _osList )
			{
				if ( sb.length() > 0 )
				{
					sb.append( ' ' );
				}

				sb.append( os.replace( " ", "\\ " ) );
			}

			out.writeAttribute( "os", sb.toString() );
		}

		if ( !_archList.isEmpty() )
		{
			final StringBuilder sb = new StringBuilder();

			for ( final String arch : _archList )
			{
				if ( sb.length() > 0 )
				{
					sb.append( ' ' );
				}

				sb.append( arch.replace( " ", "\\ " ) );
			}

			out.writeAttribute( "arch", sb.toString() );
		}

		if ( !_localeList.isEmpty() )
		{
			final StringBuilder sb = new StringBuilder();

			for ( final Locale locale : _localeList )
			{
				if ( sb.length() > 0 )
				{
					sb.append( ' ' );
				}

				sb.append( locale );
			}

			out.writeAttribute( "locale", sb.toString() );
		}

		for ( final Map.Entry<String, String> property : _properties.entrySet() )
		{
			out.writeEmptyElement( "property" );
			out.writeAttribute( "name", property.getKey() );
			out.writeAttribute( "value", property.getValue() );
		}

		for ( final JnlpJar jar : _jarList )
		{
			jar.write( out );
		}

		for ( final JnlpNativelib nativelib : _nativelibList )
		{
			nativelib.write( out );
		}

		for ( final JnlpPackage packageElement : _packageList )
		{
			packageElement.write( out );
		}

		for ( final JnlpJreElement jre : _jreList )
		{
			jre.write( out );
		}

		for ( final JnlpExtension extension : _extensionList )
		{
			extension.write( out );
		}

		out.writeEndElement(); // </resources>
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		final String osAttribute = in.getAttributeValue( null, "os" );
		if ( osAttribute != null )
		{
			for ( final String os : JnlpTools.parseEscapedWhitespaceList( osAttribute ) )
			{
				addOs( os );
			}
		}

		final String archAttribute = in.getAttributeValue( null, "arch" );
		if ( archAttribute != null )
		{
			for ( final String arch : JnlpTools.parseList( archAttribute ) )
			{
				addArch( arch );
			}
		}

		final String localeAttribute = in.getAttributeValue( null, "locale" );
		if ( localeAttribute != null )
		{
			for ( final String locale : JnlpTools.parseList( localeAttribute ) )
			{
				addLocale( LocaleTools.parseLocale( locale ) );
			}
		}

		while ( in.nextTag() != XMLStreamConstants.END_ELEMENT )
		{
			// TODO: (j2se | nativelib | property | package)*
			final String tagName = in.getLocalName();
			if ( "jar".equals( tagName ) )
			{
				final JnlpJar jar = addJar();
				jar.read( in );
			}
			else if ( "extension".equals( tagName ) )
			{
				final JnlpExtension extension = addExtension();
				extension.read( in );
			}
			else
			{
				LOG.debug( "Unsupported element: " + tagName );
				XMLTools.skipElement( in );
			}
		}
	}
}
