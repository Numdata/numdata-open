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
import com.numdata.oss.log.*;

/**
 * Data model of a JNLP file.
 *
 * All references to external resources in a JNLP file are specified as URLs
 * using the href attribute. An {@code href} can be relative or absolute. A
 * relative URL is relative to the URL given in the codebase attribute of the
 * jnlp root element. A relative URL cannot contain parent directory notations,
 * such as '..'. It must denote a file that is stored in a subdirectory of the
 * codebase. URLs in a JNLP file should always be properly encoded (also known
 * as 'escaped' form in RFC 2396 Section 2.4.2), e.g. a space should be
 * represented as {@code %20} in a HTTP URL.
 *
 * All resources can also be specified using a URL and version string pair.
 * Thus, all elements that support the href attribute also support the version
 * attribute, which specifies the version of the given resource that is
 * required. The version attribute can not only specify an exact version, but
 * can also a space-separated list of version ranges. A version range is either
 * a version-id, a version-id followed by a star (*), a version-id followed by a
 * plus sign (+), or two version-ranges combined using an ampersand (&amp;). The
 * star means prefix match, the plus sign means this version or greater, and the
 * ampersand means the logical and-ing of the two version-ranges. Example:
 * <pre>
 * &lt;jar href='classes/MyApp.jar' version='1.4.0_04 1.4*&amp;1.4.1_02+'/&gt;
 * </pre>
 * The meaning of the above is: the JAR file at the given URL that either has
 * the version-id 1.4.0_04, or has a version-id with 1.4 as a prefix and that is
 * not less than 1.4.1_02.
 *
 * @author Peter S. Heijnen
 */
public class JnlpFile
extends JnlpElement
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JnlpFile.class );

	/**
	 * When to check for application updates.
	 *
	 * @see JnlpFile#getUpdateCheck
	 * @see JnlpFile#setUpdateCheck
	 */
	public enum UpdateCheck
	{
		/**
		 * Always check for updates before launching the application.
		 */
		ALWAYS,

		/**
		 * Check for updates until timeout before launching the application
		 * (default). If the update check is not completed before the timeout,
		 * the application is launched, and the update check will continue in
		 * the background.
		 */
		TIMEOUT,

		/**
		 * Launch the application while checking for updates in the background.
		 */
		BACKGROUND
	}

	/**
	 * How to perform application updates.
	 *
	 * @see JnlpFile#getUpdatePolicy
	 * @see JnlpFile#setUpdatePolicy
	 */
	public enum UpdatePolicy
	{
		/**
		 * Always download updates without any prompt.
		 */
		ALWAYS,

		/**
		 * Ask the user if he/she wants to download and run the updated version,
		 * or launch the cached version (this is the default).
		 */
		PROMPT_UPDATE,

		/**
		 * Ask the user if he/she wants to download and run the updated version,
		 * or cancel and abort running the application.
		 */
		PROMPT_RUN
	}

	/**
	 * What versions of the JNLP specification a particular JNLP file works
	 * with. The default value is "1.0+".
	 *
	 * If the JNLP Client does not implement this version, then the launch will
	 * be aborted. If the attribute is not explicitly defined, it must be
	 * assumed to be '1.0+', i.e., it works with all JNLP Clients.
	 */
	private String _spec = null;

	/**
	 * Version of the application being launched, as well as the version of the
	 * JNLP file itself.
	 */
	private String _version = null;

	/**
	 * codebase for the application. This is also used as the base URL for all
	 * relative URLs in href attributes.  -->
	 */
	private String _codebase = null;

	/**
	 * Location of the JNLP file as a URL.
	 */
	private String _href = null;

	/**
	 * List of meta-information for desktop integration and user feedback.
	 */
	private final List<JnlpInformation> _informationList = new ArrayList<JnlpInformation>();

	/**
	 * Indicates that the application needs full access the the local system and
	 * network.
	 */
	private boolean _allPermissions = false;

	/**
	 * Indicates that the application needs the set of permissions defined for a
	 * J2EE application client.
	 */
	private boolean _applicationPermissions = false;

	/**
	 * When to check for application updates.
	 */
	private UpdateCheck _updateCheck = null;

	/**
	 * What to do when application updates are available.
	 */
	private UpdatePolicy _updatePolicy = null;

	/**
	 * Resources that are part of the application, such as Java class files,
	 * native libraries, and system properties.
	 */
	private final List<JnlpResources> _resourcesList = new ArrayList<JnlpResources>();

	/**
	 * Descriptor for application/applet/component/installer.
	 */
	private JnlpElement _descriptor = null;

	public String getSpec()
	{
		return _spec;
	}

	public void setSpec( final String spec )
	{
		_spec = spec;
	}

	public String getVersion()
	{
		return _version;
	}

	public void setVersion( final String version )
	{
		_version = version;
	}

	public String getCodebase()
	{
		return _codebase;
	}

	public void setCodebase( final String codebase )
	{
		_codebase = codebase;
	}

	public String getHref()
	{
		return _href;
	}

	public void setHref( final String href )
	{
		_href = href;
	}

	public boolean isAllPermissions()
	{
		return _allPermissions;
	}

	public void setAllPermissions( final boolean enabled )
	{
		_allPermissions = enabled;
	}

	public boolean isApplicationPermissions()
	{
		return _applicationPermissions;
	}

	public void setApplicationPermissions( final boolean enabled )
	{
		_applicationPermissions = enabled;
	}

	public UpdateCheck getUpdateCheck()
	{
		return _updateCheck;
	}

	public void setUpdateCheck( final UpdateCheck check )
	{
		_updateCheck = check;
	}

	public UpdatePolicy getUpdatePolicy()
	{
		return _updatePolicy;
	}

	public void setUpdatePolicy( final UpdatePolicy policy )
	{
		_updatePolicy = policy;
	}

	/**
	 * Add information section to the JNLP file. This information is used to
	 * integrate the application into the desktop, provide user feedback, etc.
	 * This information may be restricted to a specific operating system,
	 * architecture, platform, or locale. By default, no restrictions are set.
	 *
	 * @return {@link JnlpInformation} that was added to the JNLP file.
	 */
	public JnlpInformation addInformation()
	{
		final JnlpInformation result = new JnlpInformation();
		_informationList.add( result );
		return result;
	}

	public List<JnlpInformation> getInformationList()
	{
		return Collections.unmodifiableList( _informationList );
	}

	/**
	 * Add resources section to the JNLP file. The resources may be restricted
	 * to a specific operating system, architecture, or locale. By default, no
	 * restrictions are set.
	 *
	 * @return {@link JnlpResources} section that was added to the JNLP file.
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

	/**
	 * Create application descriptor.
	 *
	 * @return {@link JnlpApplicationDesc} that was created.
	 */
	public JnlpApplicationDesc createApplication()
	{
		if ( _descriptor != null )
		{
			throw new IllegalStateException( "Can create only one application, applet, component, or installer descriptor" );
		}

		final JnlpApplicationDesc result = new JnlpApplicationDesc();
		_descriptor = result;
		return result;
	}

	/**
	 * Create applet descriptor.
	 *
	 * @return {@link JnlpAppletDesc} that was created.
	 */
	public JnlpAppletDesc createApplet()
	{
		if ( _descriptor != null )
		{
			throw new IllegalStateException( "Can create only one application, applet, component, or installer descriptor" );
		}

		final JnlpAppletDesc result = new JnlpAppletDesc();
		_descriptor = result;
		return result;
	}

	/**
	 * Create component descriptor.
	 *
	 * @return {@link JnlpComponentDesc} that was created.
	 */
	public JnlpComponentDesc createComponent()
	{
		if ( _descriptor != null )
		{
			throw new IllegalStateException( "Can create only one application, applet, component, or installer descriptor" );
		}

		final JnlpComponentDesc result = new JnlpComponentDesc();
		_descriptor = result;
		return result;
	}

	/**
	 * Create installer descriptor.
	 *
	 * @return {@link JnlpInstallerDesc} that was created.
	 */
	public JnlpInstallerDesc createInstaller()
	{
		if ( _descriptor != null )
		{
			throw new IllegalStateException( "Can create only one application, applet, component, or installer descriptor" );
		}

		final JnlpInstallerDesc result = new JnlpInstallerDesc();
		_descriptor = result;
		return result;
	}

	public JnlpElement getDescriptor()
	{
		return _descriptor;
	}

	/**
	 * Writes the JNLP file to the given character stream.
	 *
	 * @param out Character stream to write to.
	 *
	 * @throws XMLStreamException if an I/O error occurs.
	 */
	public void write( final Writer out )
	throws XMLStreamException
	{
		final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
		final XMLStreamWriter xmlStreamWriter = new IndentingXMLStreamWriter( xmlOutputFactory.createXMLStreamWriter( out ) );
		write( xmlStreamWriter );
		xmlStreamWriter.flush();
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		out.writeStartDocument();
		out.writeStartElement( "jnlp" );
		writeOptionalAttribute( out, "codebase", getCodebase() );
		writeOptionalAttribute( out, "href", getHref() );
		writeOptionalAttribute( out, "version", getVersion() );
		writeOptionalAttribute( out, "spec", getSpec() );

		final List<JnlpInformation> informationList = getInformationList();
		if ( informationList.isEmpty() )
		{
			throw new XMLStreamException( "No <information> specified" );
		}

		for ( final JnlpInformation information : informationList )
		{
			information.write( out );
		}

		if ( isAllPermissions() || isApplicationPermissions() )
		{
			out.writeStartElement( "security" );
			writeOptionalBooleanElement( out, "all-permissions", isAllPermissions() );
			writeOptionalBooleanElement( out, "j2ee-application-client-permissions", isApplicationPermissions() );
			out.writeEndElement();
		}

		final UpdateCheck updateCheck = getUpdateCheck();
		final UpdatePolicy updatePolicy = getUpdatePolicy();
		if ( ( updateCheck != null ) || ( updatePolicy != null ) )
		{
			out.writeEmptyElement( "update" );

			if ( updateCheck != null )
			{
				switch ( updateCheck )
				{
					case ALWAYS:
						out.writeAttribute( "check", "always" );
						break;

					case TIMEOUT:
						out.writeAttribute( "check", "timeout" );
						break;

					case BACKGROUND:
						out.writeAttribute( "check", "background" );
						break;
				}

			}

			if ( updatePolicy != null )
			{
				switch ( updatePolicy )
				{
					case ALWAYS:
						out.writeAttribute( "policy", "always" );
						break;

					case PROMPT_UPDATE:
						out.writeAttribute( "policy", "prompt-update" );
						break;

					case PROMPT_RUN:
						out.writeAttribute( "policy", "prompt-run" );
						break;
				}
			}
		}

		for ( final JnlpResources resources : getResourcesList() )
		{
			resources.write( out );
		}

		final JnlpElement descriptor = getDescriptor();
		if ( descriptor == null )
		{
			throw new XMLStreamException( "Must specify a application/applet/component/installer descriptor" );
		}

		descriptor.write( out );

		out.writeEndElement();
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		in.require( XMLStreamConstants.START_DOCUMENT, null, null );
		in.nextTag();

		in.require( XMLStreamConstants.START_ELEMENT, null, "jnlp" );

		setCodebase( in.getAttributeValue( null, "codebase" ) );
		setHref( in.getAttributeValue( null, "href" ) );
		setSpec( in.getAttributeValue( null, "spec" ) );
		setVersion( in.getAttributeValue( null, "version" ) );

		while ( in.nextTag() != XMLStreamConstants.END_ELEMENT )
		{
			final String tagName = in.getLocalName();
			if ( "information".equals( tagName ) )
			{
				final JnlpInformation information = addInformation();
				information.read( in );
			}
			else if ( "resources".equals( tagName ) )
			{
				final JnlpResources resources = addResources();
				resources.read( in );
			}
			else if ( "security".equals( tagName ) )
			{
				readSecurityElement( in );
			}
			else if ( "application-desc".equals( tagName ) )
			{
				final JnlpApplicationDesc application = createApplication();
				application.read( in );
			}
			else if ( "applet-desc".equals( tagName ) )
			{
				final JnlpAppletDesc applet = createApplet();
				applet.read( in );
			}
			else if ( "component-desc".equals( tagName ) )
			{
				final JnlpComponentDesc component = createComponent();
				component.read( in );
			}
			else if ( "installer-desc".equals( tagName ) )
			{
				final JnlpInstallerDesc installer = createInstaller();
				installer.read( in );
			}
			else
			{
				LOG.debug( "Unsupported element: " + tagName );
				XMLTools.skipElement( in );
			}
		}

		in.require( XMLStreamConstants.END_ELEMENT, null, "jnlp" );
		in.next();
		in.require( XMLStreamConstants.END_DOCUMENT, null, null );
	}

	/**
	 * Reads a JNLP security element.
	 *
	 * @param in XML stream reader.
	 *
	 * @throws XMLStreamException if a parse error occurs.
	 */
	private void readSecurityElement( final XMLStreamReader in )
	throws XMLStreamException
	{
		while ( in.nextTag() != XMLStreamConstants.END_ELEMENT )
		{
			final String tagName = in.getLocalName();
			if ( "all-permissions".equals( tagName ) )
			{
				setAllPermissions( true );
				XMLTools.skipElement( in );
			}
			else if ( "j2ee-application-client-permissions".equals( tagName ) )
			{
				setApplicationPermissions( true );
				XMLTools.skipElement( in );
			}
			else
			{
				LOG.debug( "Unsupported element: " + tagName );
				XMLTools.skipElement( in );
			}
		}
	}
}
