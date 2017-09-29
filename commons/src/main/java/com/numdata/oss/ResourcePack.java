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
import java.util.zip.*;

import org.jetbrains.annotations.*;

/**
 * This class manages a set of resource bundles. It is meant as a replacement of
 * the {@link ResourceBundle#getBundle} method using centralized storage. Its
 * main purposes are:
 *
 * <ul>
 *
 * <li>Provide resource access by class name to obfuscated applications.</li>
 *
 * <li>Reduce amount of resource files.</li>
 *
 * </ul>
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "FinalClass" )
public final class ResourcePack
{
	/**
	 * List of locales in this resource pack.
	 */
	@NotNull
	private Locale[] _locales;

	/**
	 * Central storage of resource data, mapped by bundle base name (outer map)
	 * and entry key (inner map). The index in the {@code String[]} matches the
	 * index in the locale list ({@code _locales}). However, the array may be
	 * smaller/larger than the list of locales, and {@code null} values may
	 * exist.
	 */
	@NotNull
	final Map<String, Map<String, String[]>> _bundles = new HashMap<String, Map<String, String[]>>();

	/**
	 * Each bundle in this map is a view on the central storage, so if any data
	 * is added, this will appear in the cached bundles as well. This means we
	 * don't need to clear the cache, unless the locale entries are replaced or
	 * removed.
	 */
	@NotNull
	private final Map<Locale, Map<String, ResourceBundle>> _bundleCache = new HashMap<Locale, Map<String, ResourceBundle>>();

	/**
	 * Fallback locale index (-1 if unknown).
	 */
	private int _rootLocaleIndex;

	/**
	 * Construct new resource pack.
	 */
	public ResourcePack()
	{
		_locales = new Locale[ 0 ];
		_rootLocaleIndex = -1;
	}

	/**
	 * Construct resource pack data from the specified stream.
	 *
	 * @param is Stream to read from.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public ResourcePack( @NotNull final DataInputStream is )
	throws IOException
	{
		this();

		/*
		 * Load locales.
		 */
		final int nrLocales = (int)is.readShort();
		final Locale[] locales = new Locale[ nrLocales ];
		for ( int i = 0; i < nrLocales; i++ )
		{
			locales[ i ] = LocaleTools.parseLocale( is.readUTF() );
		}

		/*
		 * Load bundles.
		 */
		final int nrBundles = (int)is.readShort();
		final Map<String, Map<String, String[]>> bundles = new HashMap<String, Map<String, String[]>>( nrBundles );

		for ( int i = 0; i < nrBundles; i++ )
		{
			final String name = is.readUTF();
			final int nrEntries = (int)is.readShort();
			final Map<String, String[]> entries = new HashMap<String, String[]>( nrEntries );

			for ( int j = 0; j < nrEntries; j++ )
			{
				final String key = is.readUTF();
				final String[] values = new String[ nrLocales ];

				for ( int k = 0; k < nrLocales; k++ )
				{
					values[ k ] = is.readBoolean() ? is.readUTF() : null;
				}

				entries.put( key, values );
			}

			bundles.put( name, entries );
		}

		/*
		 * If everything worked out fine, activate the new data.
		 */
		_locales = locales;
		_bundles.clear();
		_bundles.putAll( bundles );

		_bundleCache.clear();
		updateLocaleIndices();
	}

	/**
	 * Replacement of {@code ResourceBundle.getBundle()} method using the data
	 * in this pack.
	 *
	 * @param name   Base name of bundle, fully qualified class name.
	 * @param locale Locale to get the bundle for.
	 *
	 * @return The requested resource bundle.
	 *
	 * @throws MissingResourceException if the requested bundle is unavailable.
	 */
	@NotNull
	public ResourceBundle getBundle( @NotNull final String name, @NotNull final Locale locale )
	{
		ResourceBundle result = null;

		Map<String, ResourceBundle> localeCache = _bundleCache.get( locale );
		if ( localeCache != null )
		{
			result = localeCache.get( name );
		}

		if ( result == null )
		{
			final Map<String, String[]> entries = _bundles.get( name );
			if ( entries != null )
			{
				final int localeIndex = getBestLocaleIndex( locale );

				final int rootLocaleIndex = _rootLocaleIndex;

				if ( ( rootLocaleIndex >= 0 ) && ( localeIndex != rootLocaleIndex ) )
				{
					result = new ResourceBundleImpl( null, entries, rootLocaleIndex );
				}

				if ( localeIndex >= 0 )
				{
					result = new ResourceBundleImpl( result, entries, localeIndex );
				}

				if ( result != null )
				{
					if ( localeCache == null )
					{
						localeCache = new HashMap<String, ResourceBundle>();
						_bundleCache.put( locale, localeCache );
					}

					localeCache.put( name, result );
				}
			}
		}

		if ( result == null )
		{
			throw new MissingResourceException( name, name, null );
		}

		return result;
	}

	/**
	 * Add bundle data for the specified locale from an existing resource
	 * bundle.
	 *
	 * @param locale Locale to which data is added.
	 * @param name   Name of bundle.
	 * @param bundle Resource bundle to read data from.
	 */
	public void insertBundleData( @NotNull final Locale locale, @NotNull final String name, @NotNull final ResourceBundle bundle )
	{
		final int index = getOrCreateLocaleIndex( locale );
		final Map<String, String[]> bundleData = getOrCreateBundleData( name );

		/*
		 * Read entries from the input bundle and store them in the bundle data.
		 */
		for ( final Enumeration<String> e = bundle.getKeys(); e.hasMoreElements(); )
		{
			final String key = e.nextElement();

			final String[] cache = bundleData.get( key );
			final String[] entry = (String[])ArrayTools.ensureLength( cache, String.class, -1, _locales.length );
			//noinspection ArrayEquality
			if ( entry != cache )
			{
				bundleData.put( key, entry );
			}

			entry[ index ] = bundle.getString( key );
		}
	}

	/**
	 * Add bundle data for the specified locale from an two-dimensional string
	 * array.
	 *
	 * The {@code data} array is organized as follows: each row (first index)
	 * defines an entry; column #0 (second index) defines the key, the rest of
	 * the columns define the entry data in order of the {@code locales}
	 * argument.
	 *
	 * @param name    Name of bundle.
	 * @param locales List of locales in data.
	 * @param data    Two-dimensional array with data.
	 */
	public void insertBundleData( @NotNull final String name, @NotNull final Locale[] locales, @NotNull final String[][] data )
	{
		/*
		 * Determine index for each specified locale.
		 */
		final int nrLocales = locales.length;
		int maxIndex = -1;

		final int[] indices = new int[ nrLocales ];
		for ( int i = 0; i < nrLocales; i++ )
		{
			final int index = getOrCreateLocaleIndex( locales[ i ] );
			indices[ i ] = index;
			maxIndex = Math.max( maxIndex, index );
		}

		/*
		 * Get bundle data.
		 */
		final Map<String, String[]> bundleData = getOrCreateBundleData( name );

		/*
		 * Get entries from data.
		 */
		for ( final String[] row : data )
		{
			if ( row != null )
			{
				final String key = row[ 0 ];
				if ( key != null )
				{
					final Object cache = bundleData.get( key );
					final String[] entry = (String[])ArrayTools.ensureLength( cache, String.class, -1, _locales.length );
					if ( entry != cache )
					{
						bundleData.put( key, entry );
					}

					final int length = Math.min( nrLocales, row.length - 1 );
					for ( int j = 0; j < length; j++ )
					{
						entry[ indices[ j ] ] = row[ j + 1 ];
					}
				}
			}
		}
	}

	/**
	 * Get locales defined in this resource pack.
	 *
	 * @return Array of locales in this resource pack.
	 */
	@NotNull
	public Locale[] getLocales()
	{
		return (Locale[])ArrayTools.clone( _locales );
	}

	/**
	 * Get index of locale in this pack. This tries to make a <b>best match</b>
	 * between the specified locale and the locales in this pack.
	 *
	 * This will try to make a match using the locale's language, country,
	 * and variant; then it will try to match only the language and country; and
	 * finally, it will try to match only the language.
	 *
	 * @param locale Locale to match.
	 *
	 * @return Index of locale; -1 if the locale was not found.
	 */
	public int getBestLocaleIndex( @Nullable final Locale locale )
	{
		int result = -1;

		if ( ( locale != null ) && ( _locales.length > 0 ) )
		{
			final String language = locale.getLanguage();
			if ( language != null )
			{
				final String country = locale.getCountry();
				final String variant = locale.getVariant();

				if ( variant != null )
				{
					result = getExactLocaleIndex( language, country, variant );
				}

				if ( ( result < 0 ) && ( country != null ) )
				{
					result = getExactLocaleIndex( language, country, null );
				}

				if ( result < 0 )
				{
					result = getExactLocaleIndex( language, null, null );
				}
			}
		}

		return result;
	}

	/**
	 * Get index of locale in this pack. This requires an <b>exact match</b>
	 * between the argument values and a locale entry in this pack.
	 *
	 * @param language Locale's language ({@code null} => ignored).
	 * @param country  Locale's country ({@code null} => ignored).
	 * @param variant  Locale's variant ({@code null} => ignored).
	 *
	 * @return Index of locale; -1 if the locale was not found.
	 */
	public int getExactLocaleIndex( @Nullable final String language, @Nullable final String country, @Nullable final String variant )
	{
		int result = -1;

		for ( int i = 0; i < _locales.length; i++ )
		{
			final Locale locale = _locales[ i ];

			if ( ( ( language == null ) || language.equalsIgnoreCase( locale.getLanguage() ) ) &&
			     ( ( country == null ) || country.equalsIgnoreCase( locale.getCountry() ) ) &&
			     ( ( variant == null ) || variant.equalsIgnoreCase( locale.getVariant() ) ) )
			{
				result = i;
				break;
			}
		}

		return result;
	}

	/**
	 * Get index of locale in this pack or create a new locale entry and return
	 * its index. Note that the index is always refers to an <b>exact match</b>
	 * between the specified locale and a locale entry in this pack.
	 *
	 * @param locale Locale to get or create the index for.
	 *
	 * @return Index of locale.
	 */
	private int getOrCreateLocaleIndex( @NotNull final Locale locale )
	{
		int index = getExactLocaleIndex( locale.getLanguage(), locale.getCountry(), locale.getVariant() );
		if ( index < 0 )
		{
			final Locale[] oldLocales = _locales;
			_locales = (Locale[])ArrayTools.append( oldLocales, Locale.class, -1, -1, locale );
			updateLocaleIndices();
			index = oldLocales.length;
		}
		return index;
	}

	/**
	 * This method updates the {@link #_rootLocaleIndex} field.
	 */
	private void updateLocaleIndices()
	{
		_rootLocaleIndex = getBestLocaleIndex( Locale.ROOT );
	}

	/**
	 * Get names of bundles in this resource pack.
	 *
	 * @return Array with names of bundles in this resource pack.
	 */
	@NotNull
	public String[] getBundleNames()
	{
		final Set<String> keys = _bundles.keySet();

		final String[] result = keys.toArray( new String[ keys.size() ] );
		Arrays.sort( result );
		return result;
	}

	/**
	 * Remove bundle with the given name from this resource pack. This has no
	 * effect if no bundle exists with the specified name.
	 *
	 * @param name Name of bundle to remove.
	 */
	public void removeBundle( @NotNull final String name )
	{
		_bundles.remove( name );
	}

	/**
	 * Rename a bundle in this resource pack. This is typically used as a post
	 * process for class name obfuscation. This has no effect if the old and new
	 * name are the same, or if no bundle exists with the old name.
	 *
	 * @param oldName Old bundle name.
	 * @param newName New bundle name.
	 */
	public void renameBundle( @NotNull final String oldName, @NotNull final String newName )
	{
		if ( !oldName.equals( newName ) )
		{
			final Map<String, String[]> bundle = _bundles.get( oldName );
			if ( bundle != null )
			{
				_bundles.remove( oldName );
				_bundles.put( newName, bundle );
			}
		}
	}

	/**
	 * Get bundle data in a two-dimensional string array.
	 *
	 * The {@code data} array is organized as follows: each row (first index)
	 * defines an entry; column #0 (second index) defines the key, the rest of
	 * the columns define the entry data in order of the result of the {@code
	 * getLocales()} method.
	 *
	 * @param name Name of bundle.
	 *
	 * @return ResourceBundleImpl data in a two-dimensional string array (see
	 * comments).
	 */
	@NotNull
	public String[][] getBundleData( @NotNull final String name )
	{
		final Map<String, String[]> bundleData = _bundles.get( name );

		final int nrLocales = _locales.length;
		final int nrKeys = bundleData.size();

		final String[][] result = new String[ nrKeys ][];

		int rowIndex = 0;
		final Set<String> keys = bundleData.keySet();
		for ( final String key : keys )
		{
			final String[] row = new String[ nrLocales + 1 ];
			final String[] values = bundleData.get( key );

			row[ 0 ] = key;
			for ( int i = 0; i < nrLocales; i++ )
			{
				row[ i + 1 ] = ( i >= values.length ) ? null : values[ i ];
			}

			result[ rowIndex++ ] = row;
		}

		return result;
	}

	/**
	 * Get the named bundle data. Create new data if needed.
	 *
	 * @param name ResourceBundleImpl name.
	 *
	 * @return ResourceBundleImpl data.
	 */
	@NotNull
	private Map<String, String[]> getOrCreateBundleData( @NotNull final String name )
	{
		Map<String, String[]> bundleData = _bundles.get( name );
		if ( bundleData == null )
		{
			bundleData = new HashMap<String, String[]>();
			_bundles.put( name, bundleData );
		}
		return bundleData;
	}

	/**
	 * Write resource pack data to the specified stream.
	 *
	 * @param out Stream to write to.
	 *
	 * @throws IOException if there was a problem writing the data.
	 */
	public void write( @NotNull final OutputStream out )
	throws IOException
	{
		final Locale[] locales = _locales;
		final int nrLocales = locales.length;
		final String[] bundleNames = getBundleNames();
		final int nrBundles = bundleNames.length;

		final GZIPOutputStream gzos = new GZIPOutputStream( out );
		final DataOutput dataOut = new DataOutputStream( gzos );

		dataOut.writeShort( nrLocales );

		for ( final Locale locale : locales )
		{
			dataOut.writeUTF( locale.toString() );
		}

		dataOut.writeShort( nrBundles );

		for ( final String name : bundleNames )
		{
			final String[][] entries = getBundleData( name );
			final int nrEntries = entries.length;

			dataOut.writeUTF( name );
			dataOut.writeShort( nrEntries );

			for ( final String[] entry : entries )
			{
				dataOut.writeUTF( entry[ 0 ] );
				for ( int k = 1; k <= nrLocales; k++ )
				{
					final String value = entry[ k ];
					dataOut.writeBoolean( value != null );
					if ( value != null )
					{
						dataOut.writeUTF( value );
					}
				}
			}
		}

		gzos.finish();
	}

	/**
	 * This class implements a {@code ResourceBundle} using the central storage
	 * facility in this pack.
	 */
	private final class ResourceBundleImpl
	extends ResourceBundle
	{
		/**
		 * Resource entries in this bundle (store to prevent bundle lookup on
		 * each query).
		 */
		@NotNull
		final Map<String, String> _entries;

		/**
		 * Construct bundle.
		 *
		 * @param parent     Parent bundle.
		 * @param entries    Entries of resource bundle.
		 * @param valueIndex Index of locale, used as index in values.
		 *
		 * @throws MissingResourceException if the requested bundle is
		 * unavailable.
		 */
		private ResourceBundleImpl( @Nullable final ResourceBundle parent, @NotNull final Map<String, String[]> entries, final int valueIndex )
		{
			this.parent = parent;
			_entries = new HashMap<String, String>();

			for ( final Map.Entry<String, String[]> entry : entries.entrySet() )
			{
				final String[] values = entry.getValue();

				if ( values != null )
				{
					final int length = values.length;

					String value = null;
					int i;
					if ( ( ( i = valueIndex ) < 0 ) || ( i >= length ) || ( ( value = values[ i ] ) == null ) )
					{
						if ( ( ( i = _rootLocaleIndex ) >= 0 ) && ( i < length ) )
						{
							value = values[ i ];
						}
					}

					if ( value != null )
					{
						_entries.put( entry.getKey(), value );
					}
				}
			}
		}

		@Nullable
		@Override
		protected Object handleGetObject( @NotNull final String key )
		{
			return _entries.get( key );
		}

		@NotNull
		@Override
		public Enumeration<String> getKeys()
		{
			return new CombinedBundleEnumeration( parent, _entries.keySet() );
		}
	}
}
