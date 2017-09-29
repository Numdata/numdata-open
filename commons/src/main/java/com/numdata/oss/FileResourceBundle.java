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
package com.numdata.oss;

import java.io.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class offers similar functionality to PropertyResourceBundle, but with
 * the following added features:
 *
 * <ul>
 *
 * <li>Keeps a reference to the file from which the properties were read;</li>
 *
 * <li>Tracks the file modification time so it is possible to determine if the
 * data has been modified to support refresh-on-demand.</li>
 *
 * <li>Is serializable.</li>
 *
 * </ul>
 *
 * @author Peter S. Heijnen
 */
public class FileResourceBundle
extends ResourceBundle
implements Serializable
{
	/**
	 * Property resource file.
	 */
	private final File _file;

	/**
	 * Last known modification time of the resource file ({@code 0} if the file
	 * was not found).
	 */
	private long _lastModified;

	/**
	 * Properties in resource.
	 */
	private final Properties _properties;

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 7009086984893474043L;

	/**
	 * Construct new resource bundle for the specified property file.
	 *
	 * @param file Property resource file.
	 *
	 * @throws IOException if the resource file could not be read.
	 */
	@SuppressWarnings( "OverridableMethodCallDuringObjectConstruction" )
	public FileResourceBundle( final File file )
	throws IOException
	{
		_file = file;
		_lastModified = 0L;
		_properties = new Properties();
		refresh();
	}

	/**
	 * Get resource bundle for the specified locales and based on the specified
	 * path. Resource files are searched by stripping off the extension of the
	 * path (base) and appending a locale and '.properties' as illustrated in
	 * the following list:<ol>
	 *
	 * <li>{@code base + "_" + language + "_" + country + "_" + variant}
	 *
	 * <li>{@code base + "_" + language + "_" + country}
	 *
	 * <li>{@code base + "_" + language}
	 *
	 * <li>{@code base}
	 *
	 * </ol>
	 *
	 * Note that the last item in this list will only be used if the
	 * 'lastResort' argument is set.
	 *
	 * @param path       File path for which the bundle is requested.
	 * @param locale     Locale to try.
	 * @param lastResort Also try loading a bundle without locale info.
	 *
	 * @return FileResourceBundle for the specified path and locale; {@code
	 * null} if the bundle was not found.
	 */
	@Nullable
	public static FileResourceBundle getBundle( final String path, final Locale locale, final boolean lastResort )
	{
		FileResourceBundle result = null;

		if ( ( path == null ) || path.isEmpty() )
		{
			throw new IllegalArgumentException( "invalid path" );
		}

		if ( locale == null )
		{
			throw new IllegalArgumentException( "invalid locale" );
		}

		/*
		 * Setup buffer for path names based on the specified path. If the path
		 * contains a file extension, it will be removed.
		 */
		final StringBuilder pathBuffer = new StringBuilder( path );
		int j = path.lastIndexOf( '.' );
		final int baseLength = ( j < ( path.lastIndexOf( File.separatorChar ) + 1 ) ) ? path.length() : j;

		/*
		 * Try page locale, then the default locale
		 */
		final int nrTries = lastResort ? 4 : 3;
		for ( j = 0; j < nrTries; j++ )
		{
			/*
			 * Build path
			 */
			pathBuffer.setLength( baseLength );

			if ( j < 3 )
			{
				pathBuffer.append( '_' );
				pathBuffer.append( locale.getLanguage() );
			}

			if ( j < 2 )
			{
				pathBuffer.append( '_' );
				pathBuffer.append( locale.getCountry() );
			}

			if ( j < 1 )
			{
				final String variant = locale.getVariant();
				if ( TextTools.isEmpty( variant ) )
				{
					continue;
				}

				pathBuffer.append( '_' );
				pathBuffer.append( variant );
			}

			pathBuffer.append( ".properties" );

			/*
			 * Try to read resource bundle from file.
			 */
			try
			{
				final File file = new File( pathBuffer.toString() );
				result = new FileResourceBundle( file.getAbsoluteFile() );
				break;
			}
			catch ( final IOException e )
			{
				/* ignored, continue loop */
			}
		}

		return result;
	}

	/**
	 * Check if the resource file has been modified since the last time it was
	 * read and refresh if so.
	 *
	 * @throws IOException if the resource file could not be read.
	 */
	public void refreshIfModified()
	throws IOException
	{
		if ( isModified() )
		{
			refresh();
		}
	}

	/**
	 * Test if the resource file has been modified since the last time it was
	 * read.
	 *
	 * @return {@code true} if the resource file has been modified since the
	 * last time it was read; {@code false} if the contents of this class are
	 * still up to date.
	 */
	public boolean isModified()
	{
		return ( _file.lastModified() != _lastModified );
	}

	/**
	 * Refreshes the data contained in this bundle from the contents of the
	 * property resource file.
	 *
	 * @throws IOException if the resource file could not be read.
	 */
	public void refresh()
	throws IOException
	{
		_properties.clear();
		final InputStream is = new FileInputStream( _file );
		try
		{
			_lastModified = _file.lastModified();
			_properties.load( is );
		}
		catch ( final IOException ignored )
		{
			_lastModified = 0L;
			_properties.clear();
		}
		finally
		{
			is.close();
		}
	}

	/**
	 * Gets an object for the given key from this resource bundle. Returns
	 * {@code null} if this resource bundle does not contain an object for the
	 * given key.
	 *
	 * @param key Key for the desired object.
	 *
	 * @return Object for the given key; {@code null} if no object is defined
	 * for the given key.
	 */
	@Override
	public Object handleGetObject( @NotNull final String key )
	{
		return _properties.getProperty( key );
	}

	/**
	 * Returns an enumeration of the keys in this resource bundle.
	 *
	 * @return Enumeration of the keys in this resource bundle.
	 */
	@NotNull
	@Override
	public Enumeration<String> getKeys()
	{
		return new CombinedBundleEnumeration( parent, _properties.stringPropertyNames() );
	}
}
