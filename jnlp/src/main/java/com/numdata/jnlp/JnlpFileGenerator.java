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
package com.numdata.jnlp;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;
import javax.xml.parsers.*;
import javax.xml.stream.*;

import com.numdata.jnlp.JnlpFile.*;
import com.numdata.oss.*;
import com.numdata.oss.io.*;
import org.jetbrains.annotations.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This application helps to create a JNLP file for lazy downloading of JAR
 * files based on contents of JAR files.
 * <pre>
 * Syntax: [{group-name}:]{jar-file} ...
 * </pre>
 * A set of JAR files is taken as input and a JNLP file is written to the
 * standard output channel. Optionally JAR files can be prefixed with a group
 * name followed by a colon character, this is useful for grouping several JAR
 * files into a single group or to use more descriptive names for JAR files.
 *
 * @author Peter S. Heijnen
 */
public class JnlpFileGenerator
{
	/**
	 * Root/default package.
	 */
	private final PackageInfo _root = new PackageInfo( "" );

	/**
	 * JNLP file being generated.
	 */
	private final JnlpFile _jnlpFile;

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 *
	 * @throws Exception if the application crashes.
	 */
	public static void main( final String[] args )
	throws Exception
	{
		final JnlpFileGenerator jnlpFileGenerator = new JnlpFileGenerator();

		final JnlpFile jnlpFile = jnlpFileGenerator.getJnlpFile();
		final JnlpResources resources = jnlpFile.addResources();

		final Pattern groupPrefix = Pattern.compile( "([^:]{2,}):(.*)" );
		for ( final String arg : args )
		{
			final File jarFile;
			final String groupName;

			final Matcher groupMatcher = groupPrefix.matcher( arg );
			if ( groupMatcher.matches() )
			{
				groupName = groupMatcher.group( 1 );
				jarFile = new File( groupMatcher.group( 2 ) );
			}
			else
			{
				jarFile = new File( arg );
				groupName = jarFile.getName();
			}

			jnlpFileGenerator.addJar( resources, groupName, jarFile, true, null );
		}

		jnlpFile.createComponent();

		jnlpFileGenerator.writeFormatted( System.out );
	}

	/**
	 * Create JNLP file generator.
	 */
	public JnlpFileGenerator()
	{
		final JnlpFile jnlpFile = new JnlpFile();
		jnlpFile.setUpdateCheck( UpdateCheck.ALWAYS );
		jnlpFile.setUpdatePolicy( UpdatePolicy.ALWAYS );

		_jnlpFile = jnlpFile;
	}

	/**
	 * Write formatted XML document to the given stream.
	 *
	 * @param out Stream to write JNLP file to.
	 *
	 * @throws XMLStreamException if the JNLP file could not be written.
	 * @throws ParserConfigurationException if we could not create a DOM.
	 * @throws SAXException if the written JNLP file could not be parsed.
	 * @throws IOException if there was a problem writing to the stream.
	 */
	public void writeFormatted( final OutputStream out )
	throws XMLStreamException, ParserConfigurationException, SAXException, IOException
	{
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write( baos );
		final DocumentBuilderFactory builderFactory = XMLTools.createDocumentBuilderFactory();
		final DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();
		final Document document = documentBuilder.parse( new ByteArrayInputStream( baos.toByteArray() ) );
		XMLTools.prettyPrint( document.getDocumentElement(), out );
	}

	/**
	 * Write JNLP file to the given stream.
	 *
	 * @param out Stream to write JNLP file to.
	 *
	 * @throws XMLStreamException if the JNLP file could not be written.
	 */
	public void write( final @NotNull OutputStream out )
	throws XMLStreamException
	{
		final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
		final XMLStreamWriter xmlStreamWriter = xmlOutputFactory.createXMLStreamWriter( out, "UTF-8" );
		write( xmlStreamWriter );
		xmlStreamWriter.close();
	}

	/**
	 * Write JNLP file to the given stream.
	 *
	 * @param out Stream to write JNLP file to.
	 *
	 * @throws XMLStreamException if the JNLP file could not be written.
	 */
	public void write( final @NotNull XMLStreamWriter out )
	throws XMLStreamException
	{
		for ( final JnlpResources resources : _jnlpFile.getResourcesList() )
		{
			determinePackageSources( resources, _root, "" );
		}
		_jnlpFile.write( out );
	}

	/**
	 * Add JAR file.
	 *
	 * @param resources    JNLP resources.
	 * @param source       Name of JAR file source.
	 * @param jarFile      JAR file to add.
	 * @param lazyDownload Whether the resource may be lazily downloaded.
	 * @param version      JAR file version.
	 *
	 * @return Added JAR file.
	 *
	 * @throws IOException if there was a problem reading the JAR file.
	 */
	public JnlpJar addJar( final @NotNull JnlpResources resources, final @NotNull String source, final @NotNull File jarFile, final boolean lazyDownload, final @Nullable String version )
	throws IOException
	{
		final JnlpJar jar = resources.addJar();
		jar.setHref( jarFile.isAbsolute() ? jarFile.getName() : jarFile.getPath() );
		jar.setPart( source );
		jar.setLazyDownload( lazyDownload );
		jar.setSize( (int)jarFile.length() );

		if ( version != null )
		{
			jar.setVersion( version );
		}

		try ( final FileInputStream fis = new FileInputStream( jarFile ) )
		{
			try ( final JarInputStream jis = new JarInputStream( fis ) )
			{
				while ( true )
				{
					final JarEntry jarEntry = jis.getNextJarEntry();
					if ( jarEntry == null )
					{
						break;
					}

					final String entryName = jarEntry.getName();
					if ( entryName.endsWith( ".class" ) )
					{
						PackageInfo currentPackage = _root;

						final List<String> path = TextTools.tokenize( entryName, '/', false );
						for ( int i = 0; i < path.size() - 1; i++ )
						{
							currentPackage = currentPackage.getOrAddChild( path.get( i ) );
						}

						currentPackage.addSource( source );
					}
				}
			}
		}

		return jar;
	}

	/**
	 * Add native library JAR file.
	 *
	 * @param resources    JNLP resources.
	 * @param source       Name of JAR file source.
	 * @param jarFile      JAR file to add.
	 * @param lazyDownload Whether the resource may be lazily downloaded.
	 * @param version      JAR file version.
	 *
	 * @return Added native library JAR file.
	 */
	public @NotNull JnlpNativelib addNativelib( final @NotNull JnlpResources resources, final @NotNull String source, final @NotNull File jarFile, final boolean lazyDownload, final @Nullable String version )
	{
		final JnlpNativelib jar = resources.addNativelib();
		jar.setHref( jarFile.isAbsolute() ? jarFile.getName() : jarFile.getPath() );
		jar.setPart( source );
		jar.setLazyDownload( lazyDownload );
		jar.setSize( (int)jarFile.length() );

		if ( version != null )
		{
			jar.setVersion( version );
		}

		return jar;
	}

	/**
	 * Determine the which packages are in which groups/sources.
	 *
	 * @param resources JNLP resources.
	 * @param node      Root node.
	 * @param prefix    Package name prefix (empty for root).
	 */
	private void determinePackageSources( final @NotNull JnlpResources resources, final @NotNull PackageInfo node, final @NotNull String prefix )
	{
		final String fullName = prefix + node._name;
		final String childPrefix = fullName.isEmpty() ? "" : fullName + '.';
		final Set<String> sources = node._sources;
		final Collection<PackageInfo> children = node.getChildren();

		boolean equalChildren = true;
		if ( children.size() > 1 )
		{
			Set<String> childSources = null;

			for ( final PackageInfo child : children )
			{
				if ( childSources == null )
				{
					childSources = child.getCombinedSources();
				}
				else if ( !childSources.equals( child.getCombinedSources() ) )
				{
					equalChildren = false;
					break;
				}
			}
		}

		if ( ( ( children.size() > 1 ) || !sources.isEmpty() ) && equalChildren )
		{
			for ( final String source : node.getCombinedSources() )
			{
				resources.addPackage( childPrefix + "*", source, true );
			}
		}
		else
		{
			for ( final String source : sources )
			{
				resources.addPackage( childPrefix + "*", source, false );
			}

			for ( final PackageInfo child : children )
			{
				determinePackageSources( resources, child, childPrefix );
			}
		}
	}

	/**
	 * Returns the JNLP file being generated. The returned instance may be used
	 * to apply custom modifications to the JNLP file.
	 *
	 * @return JNLP file.
	 */
	public JnlpFile getJnlpFile()
	{
		return _jnlpFile;
	}

	/**
	 * Container for some properties of a package.
	 */
	private static class PackageInfo
	{
		/**
		 * Unqualified name of package.
		 */
		final @NotNull String _name;

		/**
		 * Child nodes of this package (sub-packages).
		 */
		final @NotNull Map<String, PackageInfo> _childNodes = new TreeMap<>();

		/**
		 * Sources that define classes in this package.
		 */
		final @NotNull Set<String> _sources = new TreeSet<>();

		/**
		 * Create package property container.
		 *
		 * @param name Unqualified name of package.
		 */
		PackageInfo( final @NotNull String name )
		{
			_name = name;
		}

		/**
		 * Get or add child node (sub-package).
		 *
		 * @param name Name of child node (unqualified name of sub-package).
		 *
		 * @return Child node.
		 */
		public PackageInfo getOrAddChild( final @NotNull String name )
		{
			PackageInfo result = _childNodes.get( name );
			if ( result == null )
			{
				result = new PackageInfo( name );
				_childNodes.put( name, result );
			}
			return result;
		}

		/**
		 * Add source from which this package is referenced.
		 *
		 * @param source Source from which this package is referenced.
		 */
		public void addSource( final String source )
		{
			_sources.add( source );
		}

		/**
		 * Test whether this is a leaf node.
		 *
		 * @return {@code true} if this is a leaf node;
		 * {@code false} if this node has at least one descendant.
		 */
		public boolean isLeaf()
		{
			return _childNodes.isEmpty();
		}


		/**
		 * Get child nodes (sub-packages).
		 *
		 * @return Child nodes.
		 */
		public Collection<PackageInfo> getChildren()
		{
			return _childNodes.values();
		}

		/**
		 * Get combined sources for this node and all its descendants.
		 *
		 * @return Combined sources.
		 */
		public Set<String> getCombinedSources()
		{
			final Set<String> result = new TreeSet<>( _sources );
			getChildren().stream().map( PackageInfo::getCombinedSources ).forEach( result::addAll );
			return result;
		}
	}
}
