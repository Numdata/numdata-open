/*
 * Copyright (c) 2011-2021, Unicon Creation BV, The Netherlands.
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
package com.numdata.jnlp.ant;

import java.io.*;
import java.util.*;

import com.numdata.jnlp.*;
import com.numdata.oss.*;
import com.numdata.oss.io.*;
import org.apache.tools.ant.*;

/**
 * Provides generation of JNLP and related files for Apache Ant.
 *
 * @author G. Meinders
 */
@SuppressWarnings( { "ReturnOfCollectionOrArrayField", "unused", "UseOfSystemOutOrSystemErr" } )
public class JnlpTask
extends Task
{
	/**
	 * Root directory of the web application.
	 */
	private File _webappDir = null;

	/**
	 * The '/WEB-INF/lib' directory of the web application.
	 */
	private File _libraryDir = null;

	/**
	 * Location of the JNLP file, relative to the web application root.
	 */
	private String _jnlpFile = null;

	/**
	 * Codebase attribute for the JNLP file.
	 */
	private String _codebase = null;

	/**
	 * Href attribute for the JNLP file.
	 */
	private String _href = null;

	/**
	 * Location of the {@code version.xml} file, relative to the web application
	 * root.
	 */
	private String _versionFile = null;

	/**
	 * Whether JARs in this JNLP file should be downloaded lazily. This applies
	 * to any (library) JAR files used in the JNLP.
	 */
	private boolean _lazyDownload = false;

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
	 * Nested JNLP information elements.
	 */
	private final List<InformationElement> _information = new ArrayList<>();

	/**
	 * Implicit resources element.
	 */
	private final ResourcesElement _defaultResources;

	/**
	 * Resources elements, including {@link #_defaultResources}.
	 */
	private final List<ResourcesElement> _resources;

	/**
	 * Descriptor element of the JNLP.
	 */
	private DescriptorElement _descriptor = null;

	/**
	 * Constructs a new instance.
	 */
	public JnlpTask()
	{
		final List<ResourcesElement> resources = new ArrayList<>();
		_resources = resources;

		final ResourcesElement defaultResources = new ResourcesElement();
		resources.add( defaultResources );
		_defaultResources = defaultResources;
	}

	public File getWebappDir()
	{
		return _webappDir;
	}

	public void setWebappDir( final File dir )
	{
		_webappDir = dir;
	}

	public File getLibraryDir()
	{
		return _libraryDir;
	}

	public void setLibraryDir( final File dir )
	{
		_libraryDir = dir;
	}

	public String getJnlpFile()
	{
		return _jnlpFile;
	}

	public void setJnlpFile( final String jnlpFile )
	{
		_jnlpFile = jnlpFile;
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

	public String getVersionFile()
	{
		return _versionFile;
	}

	public void setVersionFile( final String versionFile )
	{
		_versionFile = versionFile;
	}

	public boolean isLazyDownload()
	{
		return _lazyDownload;
	}

	public void setLazyDownload( final boolean lazyDownload )
	{
		_lazyDownload = lazyDownload;
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

	public List<InformationElement> getInformation()
	{
		return _information;
	}

	/**
	 * Provides support for nested {@code information} elements.
	 *
	 * @return Information element.
	 */
	public InformationElement createInformation()
	{
		final InformationElement result = new InformationElement();
		_information.add( result );
		return result;
	}

	public List<ResourcesElement> getResources()
	{
		return _resources;
	}

	/**
	 * Provides support for nested {@code resources} elements.
	 *
	 * @return Resources element.
	 */
	public ResourcesElement createResources()
	{
		final ResourcesElement result = new ResourcesElement();
		_resources.add( result );
		return result;
	}

	public ResourcesElement getDefaultResources()
	{
		return _defaultResources;
	}

	/**
	 * Provides support for nested {@code lib} (library JAR) elements.
	 *
	 * @return Library JAR element.
	 */
	public LibraryJarElement createLib()
	{
		return _defaultResources.createLib();
	}

	/**
	 * Provides support for nested {@code nativelib} elements.
	 *
	 * @return Native library element.
	 */
	public NativeLibraryElement createNativelib()
	{
		return _defaultResources.createNativelib();
	}

	/**
	 * Provides support for nested {@code jar} elements.
	 *
	 * @return JAR element.
	 */
	public JarElement createJar()
	{
		return _defaultResources.createJar();
	}

	/**
	 * Provides support for nested {@code extension} elements.
	 *
	 * @return Extension element.
	 */
	public ExtensionElement createExtension()
	{
		return _defaultResources.createExtension();
	}

	/**
	 * Provides support for nested {@code j2se} elements.
	 *
	 * @return Extension element.
	 */
	public JreElement createJ2se()
	{
		return _defaultResources.createJ2se();
	}

	public DescriptorElement getDescriptor()
	{
		return _descriptor;
	}

	/**
	 * Provides support for nested {@code applicationdesc} elements.
	 *
	 * @return Information element.
	 */
	public ApplicationDescElement createApplicationDesc()
	{
		final ApplicationDescElement result = new ApplicationDescElement();
		if ( _descriptor != null )
		{
			throw new UnsupportedElementException( "Can't specify multiple descriptor elements.", "applicationdesc" );
		}
		_descriptor = result;
		return result;
	}

	/**
	 * Provides support for nested {@code appletdesc} elements.
	 *
	 * @return Information element.
	 */
	public AppletDescElement createAppletDesc()
	{
		final AppletDescElement result = new AppletDescElement();
		if ( _descriptor != null )
		{
			throw new UnsupportedElementException( "Can't specify multiple descriptor elements.", "appletdesc" );
		}
		_descriptor = result;
		return result;
	}

	/**
	 * Provides support for nested {@code componentdesc} elements.
	 *
	 * @return Information element.
	 */
	public ComponentDescElement createComponentDesc()
	{
		final ComponentDescElement result = new ComponentDescElement();
		if ( _descriptor != null )
		{
			throw new UnsupportedElementException( "Can't specify multiple descriptor elements.", "componentdesc" );
		}
		_descriptor = result;
		return result;
	}

	/**
	 * Specifies an information element to be added to the JNLP file.
	 */
	public static class InformationElement
	{
		/**
		 * Locale of the information element.
		 */
		private String _locale = null;

		/**
		 * Title of the JNLP file.
		 */
		private String _title = null;

		/**
		 * Vendor of the JNLP file.
		 */
		private String _vendor = null;

		public String getLocale()
		{
			return _locale;
		}

		public void setLocale( final String locale )
		{
			_locale = locale;
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
	}

	/**
	 * Container for resources. May be specific to a platform/architecture.
	 */
	public static class ResourcesElement
	{
		/**
		 * Operating systems to which the resources apply. If a value is a
		 * prefix of the {@code os.name} system property, then the resources can
		 * be used. If no operating system is specified, all operating systems
		 * are matched.
		 */
		private String _os = null;

		/**
		 * Architectures to which the resources apply. If a value is a prefix of
		 * the {@code os.arch} system property, then the resources can be used.
		 * If no architecture is specified, all architectures are matched.
		 */
		private String _arch = null;

		/**
		 * Locales to which the resources apply.
		 */
		private String _locale = null;

		/**
		 * Nested library JAR elements.
		 */
		private final List<LibraryJarElement> _libs = new ArrayList<>();

		/**
		 * Nested library JAR elements.
		 */
		private final List<NativeLibraryElement> _nativeLibs = new ArrayList<>();

		/**
		 * Nested JAR elements.
		 */
		private final List<JarElement> _jars = new ArrayList<>();

		/**
		 * Nested extension elements.
		 */
		private final List<ExtensionElement> _extensions = new ArrayList<>();

		/**
		 * Nested j2se elements.
		 */
		private final List<JreElement> _jres = new ArrayList<>();

		/**
		 * Constructs a new instance.
		 */
		private ResourcesElement()
		{
		}

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

		public String getLocale()
		{
			return _locale;
		}

		public void setLocale( final String locale )
		{
			_locale = locale;
		}

		public List<LibraryJarElement> getLibs()
		{
			return _libs;
		}

		/**
		 * Provides support for nested {@code lib} (library JAR) elements.
		 *
		 * @return Library JAR element.
		 */
		public LibraryJarElement createLib()
		{
			final LibraryJarElement result = new LibraryJarElement();
			_libs.add( result );
			return result;
		}

		public List<NativeLibraryElement> getNativeLibs()
		{
			return _nativeLibs;
		}

		/**
		 * Provides support for nested {@code nativelib} elements.
		 *
		 * @return Native library element.
		 */
		public NativeLibraryElement createNativelib()
		{
			final NativeLibraryElement result = new NativeLibraryElement();
			_nativeLibs.add( result );
			return result;
		}

		public List<JarElement> getJars()
		{
			return _jars;
		}

		/**
		 * Provides support for nested {@code jar} elements.
		 *
		 * @return JAR element.
		 */
		public JarElement createJar()
		{
			final JarElement result = new JarElement();
			_jars.add( result );
			return result;
		}

		public List<ExtensionElement> getExtensions()
		{
			return _extensions;
		}

		/**
		 * Provides support for nested {@code extension} elements.
		 *
		 * @return Extension element.
		 */
		public ExtensionElement createExtension()
		{
			final ExtensionElement result = new ExtensionElement();
			_extensions.add( result );
			return result;
		}

		public List<JreElement> getJres()
		{
			return _jres;
		}

		/**
		 * Provides support for nested {@code j2se} elements.
		 *
		 * @return Extension element.
		 */
		public JreElement createJ2se()
		{
			final JreElement result = new JreElement();
			_jres.add( result );
			return result;
		}
	}

	/**
	 * Specifies a library JAR (from 'WEB-INF/lib') to be included in the JNLP
	 * and {@code version.xml} files.
	 */
	public static class LibraryJarElement
	{
		/**
		 * Name of the library JAR, relative to the 'WEB-INF/lib' directory.
		 */
		private String _name = null;

		/**
		 * Version of the JAR.
		 */
		private String _version = null;

		/**
		 * Specifies whether this JAR contains the main class.
		 */
		private Boolean _main = null;

		public String getName()
		{
			return _name;
		}

		public void setName( final String name )
		{
			_name = name;
		}

		public String getVersion()
		{
			return _version;
		}

		public void setVersion( final String version )
		{
			_version = version;
		}

		public Boolean getMain()
		{
			return _main;
		}

		public void setMain( final Boolean main )
		{
			_main = main;
		}
	}

	/**
	 * Specifies a JAR to be included in the JNLP and {@code version.xml}
	 * files.
	 */
	public static class JarElement
	{
		/**
		 * Location of the JAR.
		 */
		private String _href = null;

		/**
		 * Version of the JAR.
		 */
		private String _version = null;

		/**
		 * Specifies whether this JAR contains the main class.
		 */
		private Boolean _main = null;

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

		public Boolean getMain()
		{
			return _main;
		}

		public void setMain( final Boolean main )
		{
			_main = main;
		}
	}

	/**
	 * Specifies a native library to be included in the JNLP and {@code
	 * version.xml} files.
	 */
	public static class NativeLibraryElement
	{
		/**
		 * Location of the JAR.
		 */
		private String _href = null;

		/**
		 * Name of the JAR in 'WEB-INF/lib'.
		 */
		private String _lib = null;

		/**
		 * Version of the JAR.
		 */
		private String _version = null;

		public String getHref()
		{
			return _href;
		}

		public void setHref( final String href )
		{
			_href = href;
		}

		public String getLib()
		{
			return _lib;
		}

		public void setLib( final String lib )
		{
			_lib = lib;
		}

		public String getVersion()
		{
			return _version;
		}

		public void setVersion( final String version )
		{
			_version = version;
		}
	}

	/**
	 * Specifies an extension JNLP (i.e. containing a 'component-desc').
	 */
	public static class ExtensionElement
	{
		/**
		 * Name of the extension.
		 */
		private String _name = null;

		/**
		 * Location of the JNLP file that defines the extension.
		 */
		private String _href = null;

		/**
		 * Version of the extension requested.
		 */
		private String _version = null;

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
	}

	/**
	 * Specifies a Java runtime environment.
	 */
	public static class JreElement
	{
		/**
		 * Version.
		 */
		private String _version = null;

		/**
		 * Download link.
		 */
		private String _href = null;

		/**
		 * Maximum heap size.
		 */
		private String _maxheapsize = null;

		public String getVersion()
		{
			return _version;
		}

		public void setVersion( final String version )
		{
			_version = version;
		}

		public String getHref()
		{
			return _href;
		}

		public void setHref( final String href )
		{
			_href = href;
		}

		public String getMaxheapsize()
		{
			return _maxheapsize;
		}

		public void setMaxheapsize( final String maxheapsize )
		{
			_maxheapsize = maxheapsize;
		}
	}

	/**
	 * Common base class for JNLP descriptor elements.
	 */
	public abstract static class DescriptorElement
	{
		/**
		 * Add element to {@link JnlpFile}.
		 *
		 * @param jnlp {@link JnlpFile}.
		 */
		protected abstract void addToJnlpFile( JnlpFile jnlp );
	}

	/**
	 * Specifies a JNLP application descriptor.
	 */
	public static class ApplicationDescElement
	extends DescriptorElement
	{
		/**
		 * Name of the main class.
		 */
		private String _mainClass = null;

		/**
		 * Application arguments.
		 */
		private final List<ArgumentElement> _arguments = new ArrayList<>();

		public String getMainClass()
		{
			return _mainClass;
		}

		public void setMainClass( final String mainClass )
		{
			_mainClass = mainClass;
		}

		public List<ArgumentElement> getArguments()
		{
			return _arguments;
		}

		/**
		 * Provides support for nested {@code argument} elements.
		 *
		 * @return {@link ArgumentElement}.
		 */
		public ArgumentElement createArgument()
		{
			final ArgumentElement result = new ArgumentElement();
			_arguments.add( result );
			return result;
		}

		@Override
		protected void addToJnlpFile( final JnlpFile jnlp )
		{
			final JnlpApplicationDesc application = jnlp.createApplication();
			application.setMainClass( getMainClass() );
			for ( final ArgumentElement argument : getArguments() )
			{
				application.addArgument( argument.getValue() );
			}
		}
	}

	/**
	 * Specifies an application argument.
	 */
	@SuppressWarnings( "JavaDoc" )
	public static class ArgumentElement
	{
		private String _value = null;

		public String getValue()
		{
			return _value;
		}

		public void setValue( final String value )
		{
			_value = value;
		}
	}

	/**
	 * Specifies a JNLP applet descriptor.
	 */
	@SuppressWarnings( "JavaDoc" )
	public static class AppletDescElement
	extends DescriptorElement
	{
		private final Collection<ParameterElement> _parameters = new ArrayList<>();

		private String _documentbase = null;

		private String _mainClass = null;

		private String _name = null;

		private Integer _width = null;

		private Integer _height = null;

		public String getDocumentbase()
		{
			return _documentbase;
		}

		public void setDocumentbase( final String documentbase )
		{
			_documentbase = documentbase;
		}

		public String getMainClass()
		{
			return _mainClass;
		}

		public void setMainClass( final String mainClass )
		{
			_mainClass = mainClass;
		}

		public String getName()
		{
			return _name;
		}

		public void setName( final String name )
		{
			_name = name;
		}

		public Integer getWidth()
		{
			return _width;
		}

		public void setWidth( final int width )
		{
			_width = width;
		}

		public Integer getHeight()
		{
			return _height;
		}

		public void setHeight( final int height )
		{
			_height = height;
		}

		public Collection<ParameterElement> getParameters()
		{
			return _parameters;
		}

		/**
		 * Provides support for nested {@code parameter} elements.
		 *
		 * @return {@link ParameterElement}.
		 */
		public ParameterElement createParameter()
		{
			final ParameterElement result = new ParameterElement();
			_parameters.add( result );
			return result;
		}

		@Override
		protected void addToJnlpFile( final JnlpFile jnlp )
		{
			if ( getName() == null )
			{
				throw new BuildException( "Missing required attribute 'name'." );
			}

			if ( getWidth() == null )
			{
				throw new BuildException( "Missing required attribute 'width'." );
			}

			if ( getHeight() == null )
			{
				throw new BuildException( "Missing required attribute 'height'." );
			}

			final JnlpAppletDesc applet = jnlp.createApplet();

			applet.setDocumentbase( getDocumentbase() );
			applet.setMainClass( getMainClass() );
			applet.setName( getName() );
			applet.setWidth( getWidth() );
			applet.setHeight( getHeight() );

			for ( final ParameterElement parameter : getParameters() )
			{
				applet.setParameter( parameter.getName(), parameter.getValue() );
			}
		}
	}

	/**
	 * Specifies a JNLP applet descriptor.
	 */
	public static class ComponentDescElement
	extends DescriptorElement
	{
		@Override
		protected void addToJnlpFile( final JnlpFile jnlp )
		{
			jnlp.createComponent();
		}
	}

	/**
	 * Specifies an applet parameter.
	 */
	@SuppressWarnings( "JavaDoc" )
	public static class ParameterElement
	{
		private String _name = null;

		private String _value = null;

		public String getName()
		{
			return _name;
		}

		public void setName( final String name )
		{
			_name = name;
		}

		public String getValue()
		{
			return _value;
		}

		public void setValue( final String value )
		{
			_value = value;
		}
	}

	@Override
	public void execute()
	{
		final File webappDir = getWebappDir();
		if ( webappDir == null )
		{
			throw new BuildException( "Missing required attribute 'webappDir'." );
		}

		final String jnlpFilePath = getJnlpFile();
		if ( jnlpFilePath == null )
		{
			throw new BuildException( "Missing required attribute 'jnlpFile'." );
		}

		File libraryDir = getLibraryDir();
		if ( libraryDir == null )
		{
			libraryDir = new File( webappDir, "WEB-INF/lib" );
		}

		final File jnlpFile = new File( webappDir, jnlpFilePath );
		final File jarDir = jnlpFile.getParentFile();

		final String versionFilePath = getVersionFile();
		if ( versionFilePath != null )
		{
			final File versionFile = new File( webappDir, versionFilePath );
			System.out.println( "Generating version.xml: " + versionFile );

			final JnlpVersionFile jnlpVersionFile = new JnlpVersionFile();

			if ( versionFile.exists() )
			{
				try
				{
					try ( final FileInputStream in = new FileInputStream( versionFile ) )
					{
						jnlpVersionFile.read( in );
					}
				}
				catch ( final Exception e )
				{
					throw new BuildException( "Failed to read existing version file: " + e.getMessage(), e );
				}
			}

			final File versionFileDir = versionFile.getParentFile();
			final File libraryDirRelative = FileTools.relativize( versionFileDir, new File( webappDir, "WEB-INF/lib" ) );


			for ( final ResourcesElement resourcesElement : getResources() )
			{
				for ( final LibraryJarElement lib : resourcesElement.getLibs() )
				{
					final File relativeLibraryFile = new File( libraryDirRelative, lib.getName() );
					try
					{
						jnlpVersionFile.addResource( lib.getName(), lib.getVersion(), relativeLibraryFile.toString() );
					}
					catch ( final IllegalArgumentException e )
					{
						throw new BuildException( e.getMessage() );
					}
				}

				for ( final NativeLibraryElement jar : resourcesElement.getNativeLibs() )
				{
					try
					{
						if ( jar.getLib() != null )
						{
							final File relativeLibraryFile = new File( libraryDirRelative, jar.getLib() );
							jnlpVersionFile.addResource( jar.getLib(), jar.getVersion(), relativeLibraryFile.toString() );
						}
						else
						{
							jnlpVersionFile.addResource( jar.getHref(), jar.getVersion(), jar.getHref() );
						}
					}
					catch ( final IllegalArgumentException e )
					{
						throw new BuildException( e.getMessage() );
					}
				}

				for ( final JarElement jar : resourcesElement.getJars() )
				{
					try
					{
						jnlpVersionFile.addResource( jar.getHref(), jar.getVersion(), jar.getHref() );
					}
					catch ( final IllegalArgumentException e )
					{
						throw new BuildException( e.getMessage() );
					}
				}
			}

			try
			{
				versionFileDir.mkdirs();

				final FileOutputStream out = new FileOutputStream( versionFile );
				try
				{
					jnlpVersionFile.write( out );
				}
				finally
				{
					out.close();
				}
			}
			catch ( final Exception e )
			{
				throw new BuildException( e );
			}
		}

		System.out.println( "Generating JNLP: " + jnlpFile );

		final JnlpFileGenerator jnlpFileGenerator = new JnlpFileGenerator();
		final JnlpFile jnlp = jnlpFileGenerator.getJnlpFile();

		for ( final ResourcesElement resourcesElement : getResources() )
		{
			final JnlpResources resources = jnlp.addResources();

			final String osList = resourcesElement.getOs();
			if ( osList != null )
			{
				for ( final String os : JnlpTools.parseEscapedWhitespaceList( osList ) )
				{
					resources.addOs( os );
				}
			}

			final String archList = resourcesElement.getArch();
			if ( archList != null )
			{
				for ( final String arch : JnlpTools.parseList( archList ) )
				{
					resources.addArch( arch );
				}
			}

			final String localeList = resourcesElement.getLocale();
			if ( localeList != null )
			{
				for ( final String locale : JnlpTools.parseList( localeList ) )
				{
					resources.addLocale( LocaleTools.parseLocale( locale ) );
				}
			}

			for ( final LibraryJarElement element : resourcesElement.getLibs() )
			{
				final File jarFile = new File( libraryDir, element.getName() );
				final String groupName = jarFile.getName();
				try
				{
					final JnlpJar jar = jnlpFileGenerator.addJar( resources, groupName, jarFile, isLazyDownload(), element.getVersion() );
					if ( element.getMain() != null )
					{
						jar.setMain( element.getMain() );
					}
				}
				catch ( final IOException e )
				{
					throw new BuildException( e );
				}
			}

			for ( final NativeLibraryElement element : resourcesElement.getNativeLibs() )
			{
				final File jarFile;
				if ( element.getLib() != null )
				{
					jarFile = new File( libraryDir, element.getLib() );
				}
				else
				{
					jarFile = new File( jarDir, element.getHref() );
				}

				final String groupName = jarFile.getName();
				jnlpFileGenerator.addNativelib( resources, groupName, jarFile, isLazyDownload(), element.getVersion() );
			}

			for ( final JarElement element : resourcesElement.getJars() )
			{
				final File jarFile = new File( jarDir, element.getHref() );
				final String groupName = jarFile.getName();
				try
				{
					final JnlpJar jar = jnlpFileGenerator.addJar( resources, groupName, jarFile, isLazyDownload(), element.getVersion() );
					if ( element.getMain() != null )
					{
						jar.setMain( element.getMain() );
					}
				}
				catch ( final IOException e )
				{
					throw new BuildException( e );
				}
			}
		}

		for ( final InformationElement informationElement : getInformation() )
		{
			final JnlpInformation information = jnlp.addInformation();

			if ( informationElement.getLocale() != null )
			{
				final Locale locale = LocaleTools.parseLocale( informationElement.getLocale() );
				information.addLocale( locale );
			}

			if ( informationElement.getTitle() != null )
			{
				information.setTitle( informationElement.getTitle() );
			}
			else
			{
				System.out.println( "WARNING: 'information' section without 'title' may not work in Java Web Start." );
			}

			if ( informationElement.getVendor() != null )
			{
				information.setVendor( informationElement.getVendor() );
			}
			else
			{
				System.out.println( "WARNING: 'information' section without 'vendor' may not work in Java Web Start." );
			}
		}

		final String codebase = getCodebase();
		if ( codebase != null )
		{
			jnlp.setCodebase( codebase );
		}

		final String href = getHref();
		if ( href != null )
		{
			jnlp.setHref( href );
		}

		final List<JnlpResources> resourcesList = jnlp.getResourcesList();
		final JnlpResources resources = resourcesList.get( 0 );
		final ResourcesElement resourcesElement = getResources().get( 0 );

		for ( final JreElement jreElement : resourcesElement.getJres() )
		{
			final JnlpJreElement jre = resources.addJ2se();
			jre.setHref( jreElement.getHref() );
			jre.setMaxHeapSize( jreElement.getMaxheapsize() );
			jre.addVersion( jreElement.getVersion() );
		}

		for ( final ExtensionElement extensionElement : resourcesElement.getExtensions() )
		{
			final JnlpExtension extension = resources.addExtension();
			extension.setName( extensionElement.getName() );
			extension.setHref( extensionElement.getHref() );
			extension.setVersion( extensionElement.getVersion() );
		}

		if ( isAllPermissions() )
		{
			jnlp.setAllPermissions( true );
		}
		else if ( isApplicationPermissions() )
		{
			jnlp.setApplicationPermissions( true );
		}

		final DescriptorElement descriptor = getDescriptor();
		if ( descriptor == null )
		{
			jnlp.createComponent();
		}
		else
		{
			descriptor.addToJnlpFile( jnlp );
		}

		try
		{
			final File directory = jnlpFile.getParentFile();
			directory.mkdirs();

			try ( final OutputStream out = new FileOutputStream( jnlpFile ) )
			{
				jnlpFileGenerator.writeFormatted( out );
			}
		}
		catch ( final Exception e )
		{
			throw new BuildException( e );
		}
	}
}
