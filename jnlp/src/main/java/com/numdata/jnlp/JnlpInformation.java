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
import org.jetbrains.annotations.*;

/**
 * Information for JNLP Client to integrate the application into the desktop,
 * provide user feedback, etc.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "unused", "WeakerAccess" } )
public class JnlpInformation
extends JnlpElement
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JnlpInformation.class );

	/**
	 * Kind of description.
	 */
	enum KindOfDescription
	{
		/**
		 * Default description. This is used for all other kinds of descriptions
		 * if they are not explicitly specified.
		 */
		DEFAULT,

		/**
		 * If a reference to the application is going to appear in one row in a
		 * list or a table, this description will be used.
		 */
		ONE_LINE,

		/**
		 * If a reference to the application is going to be displayed in a
		 * situation where there is room for a paragraph, this description is
		 * used.
		 */
		SHORT,

		/**
		 * A description of the application intended to be used as a tooltip.
		 */
		TOOLTIP
	}

	/**
	 * Specifies the operating system for which the {@code information} element
	 * should be considered. If the value is a prefix of the {@code os.name}
	 * system property, then the {@code information} element can be used. If the
	 * attribute is not specified, it matches all operating systems.
	 */
	private String _os = null;

	/**
	 * Specifies the architecture for which the {@code information} element
	 * should be considered. If the value is a prefix of the {@code os.arch}
	 * system property, then the {@code information} element can be used. If the
	 * attribute is not specified, it matches all architectures.
	 */
	private String _arch = null;

	/**
	 * Specifies the platform for which the {@code information} element should
	 * be considered. If the value is a prefix of the {@code os.platform} system
	 * property, then the {@code information} element can be used. If the
	 * attribute is not specified, it matches all platforms.
	 */
	private String _platform = null;

	/**
	 * Specifies the locales for which the {@code information} element should be
	 * used. Several locales can be specified.
	 *
	 * The JNLP Client searches through the {@code information} elements in the
	 * order specified in the JNLP file. For each {@code information} element,
	 * it checks if the value specified in the locale attribute matches the
	 * current locale. If a match is found, the values specified in that {@code
	 * information} element will be used, possibly overriding values found in
	 * previous {@code information} elements.
	 */
	private final List<Locale> _locales = new ArrayList<Locale>();

	/**
	 * Name of the application.
	 */
	private String _title = null;

	/**
	 * Vendor of the application.
	 */
	private String _vendor = null;

	/**
	 * Home page URL for the application. It can be used by the JNLP client to
	 * point the user to a web page where they can find more information about
	 * the application.
	 */
	private String _homepage = null;

	/**
	 * Descriptions of application. Descriptions can be given for different
	 * kinds of messages, but at most one per kind. All descriptions contain
	 * plain text. No formatting, such as HTML tags is supported.
	 */
	private final Map<KindOfDescription, String> _descriptions = new EnumMap<KindOfDescription, String>( KindOfDescription.class );

	/**
	 * Icons that can be used by a JNLP client to identify the application to
	 * the user.
	 */
	private final List<JnlpIcon> _icons = new ArrayList<JnlpIcon>();

	/**
	 * Indicates if the application can work while the client system is
	 * disconnected from the network. The default is that an application only
	 * works if the client system is online.
	 *
	 * This can be use by a JNLP Client to provide a better user experience. For
	 * example, the offline allowed/disallowed information can be communicated
	 * to the user, it can be used to prevent launching an application that is
	 * known not to work when the system is offline, or it can be completely
	 * ignored by the JNLP Client.
	 *
	 * An application cannot assume that it will never be launched offline, even
	 * if this element is not specified.
	 */
	private boolean _offlineAllowed = false;

	/**
	 * Application's preferences for desktop integration.
	 */
	private final List<JnlpShortcut> _shortcuts = new ArrayList<JnlpShortcut>();

	/**
	 * Hints to register application as the primary handler of certain
	 * extensions and a certain mime-type.
	 */
	private final List<JnlpAssociation> _associations = new ArrayList<JnlpAssociation>();

	/**
	 * Additional pieces of related content, such as a readme files, help pages,
	 * or links to registration pages.
	 */
	private final List<JnlpRelatedContent> _relatedContentList = new ArrayList<JnlpRelatedContent>();

	public String getOs()
	{
		return _os;
	}

	public void setOs( final String os )
	{
		_os = os;
	}

	public String getArch()
	{
		return _arch;
	}

	public void setArch( final String arch )
	{
		_arch = arch;
	}

	public String getPlatform()
	{
		return _platform;
	}

	public void setPlatform( final String platform )
	{
		_platform = platform;
	}

	/**
	 * Add locale for which the information should be used. Several locales can
	 * be specified.
	 *
	 * The JNLP Client searches through the information elements in the order
	 * specified in the JNLP file. For each information element, it checks if
	 * any of the specified locales matches the JNLP client locale. If a match
	 * is found, the information is used and any previously set values will be
	 * overridden.
	 *
	 * @param locale Locale to apply information to.
	 */
	public void addLocale( final Locale locale )
	{
		_locales.add( locale );
	}

	public List<Locale> getLocales()
	{
		return Collections.unmodifiableList( _locales );
	}

	public String getTitle()
	{
		return _title;
	}

	public void setTitle( final String title )
	{
		_title = title;
	}

	public String getVendor()
	{
		return _vendor;
	}

	public void setVendor( final String vendor )
	{
		_vendor = vendor;
	}

	public String getHomepage()
	{
		return _homepage;
	}

	public void setHomepage( final String homepage )
	{
		_homepage = homepage;
	}

	/**
	 * Get description of a specific kind of the application. Descriptions
	 * contain plain text. No formatting, such as HTML tags is supported.
	 *
	 * @param kind Kind of description.
	 *
	 * @return Description of requested kind (may fall back to the {@link
	 * KindOfDescription#DEFAULT DEFAULT} description if no description of the
	 * specific type is available); {@code null} if no description is
	 * available.
	 */
	@Nullable
	public String getDescription( final KindOfDescription kind )
	{
		String result = _descriptions.get( kind );
		if ( ( result == null ) && ( kind != KindOfDescription.DEFAULT ) )
		{
			result = _descriptions.get( KindOfDescription.DEFAULT );
		}
		return result;
	}

	/**
	 * Get descriptions of application. Descriptions can be given for different
	 * kinds of messages, but at most one per kind. All descriptions contain
	 * plain text. No formatting, such as HTML tags is supported.
	 *
	 * @param kind        Kind of description.
	 * @param description Description of application.
	 */
	public void setDescription( final KindOfDescription kind, final String description )
	{
		_descriptions.put( kind, description );
	}

	public Map<KindOfDescription, String> getDescriptions()
	{
		return Collections.unmodifiableMap( _descriptions );
	}

	public boolean isOfflineAllowed()
	{
		return _offlineAllowed;
	}

	public void setOfflineAllowed( final boolean allow )
	{
		_offlineAllowed = allow;
	}

	/**
	 * Add icons that can be used by a JNLP client to identify the application
	 * to the user.
	 *
	 * @return {@link JnlpIcon} that was added.
	 */
	public JnlpIcon addIcon()
	{
		final JnlpIcon result = new JnlpIcon();
		_icons.add( result );
		return result;
	}

	public List<JnlpIcon> getIcons()
	{
		return Collections.unmodifiableList( _icons );
	}

	/**
	 * Add preference for desktop integration.
	 *
	 * @return {@link JnlpShortcut} that was added.
	 */
	public JnlpShortcut addShortcut()
	{
		final JnlpShortcut result = new JnlpShortcut();
		_shortcuts.add( result );
		return result;
	}

	public List<JnlpShortcut> getShortcuts()
	{
		return Collections.unmodifiableList( _shortcuts );
	}

	/**
	 * Add hint to register the application as the primary handler of certain
	 * extensions and a certain mime-type.
	 *
	 * @return {@link JnlpAssociation} that was added.
	 */
	public JnlpAssociation addAssocation()
	{
		final JnlpAssociation result = new JnlpAssociation();
		_associations.add( result );
		return result;
	}

	public List<JnlpAssociation> getAssociations()
	{
		return Collections.unmodifiableList( _associations );
	}

	/**
	 * Add related content, such as a readme file, help page, or a link to a
	 * registration page.
	 *
	 * @return {@link JnlpRelatedContent} that was added.
	 */
	public JnlpRelatedContent addRelatedContent()
	{
		final JnlpRelatedContent result = new JnlpRelatedContent();
		_relatedContentList.add( result );
		return result;
	}

	public List<JnlpRelatedContent> getRelatedContentList()
	{
		return Collections.unmodifiableList( _relatedContentList );
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		out.writeStartElement( "information" );
		writeOptionalAttribute( out, "os", getOs() );
		writeOptionalAttribute( out, "arch", getArch() );
		writeOptionalAttribute( out, "platform", getPlatform() );

		final List<Locale> locales = getLocales();
		if ( !locales.isEmpty() )
		{
			final StringBuilder sb = new StringBuilder();

			for ( final Locale locale : locales )
			{
				if ( sb.length() > 0 )
				{
					sb.append( ' ' );
				}

				sb.append( locale );
			}

			out.writeAttribute( "locale", sb.toString() );
		}

		writeOptionalTextElement( out, "title", getTitle() );
		writeOptionalTextElement( out, "vendor", getVendor() );
		writeOptionalTextElement( out, "homepage", getHomepage() );

		final Map<KindOfDescription, String> descriptions = getDescriptions();
		for ( final Map.Entry<KindOfDescription, String> entry : descriptions.entrySet() )
		{
			out.writeStartElement( "description" );

			switch ( entry.getKey() )
			{
				case ONE_LINE:
					out.writeAttribute( "kind", "one-line" );
					break;

				case SHORT:
					out.writeAttribute( "kind", "short" );
					break;

				case TOOLTIP:
					out.writeAttribute( "kind", "tooltip" );
					break;

				case DEFAULT:
					/* don't specify 'kind' */
					break;
			}

			out.writeCharacters( entry.getValue() );
			out.writeEndElement();
		}

		for ( final JnlpIcon icon : getIcons() )
		{
			icon.write( out );
		}

		for ( final JnlpShortcut shortcut : getShortcuts() )
		{
			shortcut.write( out );
		}

		writeOptionalBooleanElement( out, "offline-allowed", isOfflineAllowed() );

		for ( final JnlpAssociation association : getAssociations() )
		{
			association.write( out );
		}

		for ( final JnlpRelatedContent relatedContent : getRelatedContentList() )
		{
			relatedContent.write( out );
		}

		out.writeEndElement();
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
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
			// TODO: description*, icon*
			final String tagName = in.getLocalName();
			if ( "title".equals( tagName ) )
			{
				setTitle( XMLTools.readTextContent( in ) );
			}
			else if ( "vendor".equals( tagName ) )
			{
				setVendor( XMLTools.readTextContent( in ) );
			}
			else if ( "homepage".equals( tagName ) )
			{
				setHomepage( XMLTools.readTextContent( in ) );
			}
			else if ( "offline-allowed".equals( tagName ) )
			{
				setOfflineAllowed( true );
				in.nextTag();
				in.require( XMLStreamConstants.END_ELEMENT, null, "offline-allowed" );
			}
			else
			{
				LOG.warn( "Unsupported element: " + tagName );
				XMLTools.skipElement( in );
			}
		}
	}
}
