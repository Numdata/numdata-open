/*
 * Copyright (c) 2009-2017, Numdata BV, The Netherlands.
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
 * This class provides a localized string value.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "OverridableMethodCallDuringObjectConstruction" )
public class LocalizedString
extends AbstractLocalizableString
implements Serializable
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 2325449239984073136L;

	/**
	 * Map's locale to string value.
	 */
	private final Properties _values = new Properties();

	/**
	 * Construct empty nested field.
	 */
	public LocalizedString()
	{
	}

	/**
	 * Construct localized string with the given default value.
	 *
	 * @param defaultString String to set as default.
	 */
	public LocalizedString( @NotNull final String defaultString )
	{
		set( defaultString );
	}

	/**
	 * Create clone of specified original localized strings.
	 *
	 * @param original Original object to clone.
	 */
	public LocalizedString( @NotNull final LocalizedString original )
	{
		set( original );
	}

	/**
	 * Creates a {@link LocalizedString} using a vararg. The vararg part may not
	 * be empty and should always contain an even number of elements. Each
	 * variable entry consists of a locale name followed by a string value for
	 * that locale. Example use:
	 * <pre>
	 * create( "Dog" , "de" , "Hund" , "nl" , "Hond" )
	 * </pre>
	 *
	 * @param defaultString         String to set as default.
	 * @param localeSpecificStrings Pairs of locale names and string values.
	 *
	 * @return {@link LocalizedString}
	 */
	@SuppressWarnings( "SpellCheckingInspection" )
	@NotNull
	public static LocalizedString create( @NotNull final String defaultString, @NotNull final String... localeSpecificStrings )
	{
		final LocalizedString result = new LocalizedString( defaultString );

		if ( ( localeSpecificStrings.length % 2 ) != 0 )
		{
			throw new IllegalArgumentException( "Bad number of arguments: " + ( 1 + localeSpecificStrings.length ) );
		}

		for ( int i = 0; i < localeSpecificStrings.length; i += 2 )
		{
			final String locale = localeSpecificStrings[ i ];
			if ( TextTools.isEmpty( locale ) )
			{
				throw new IllegalArgumentException( i + ": " + locale );
			}

			final String string = localeSpecificStrings[ i + 1 ];
			if ( TextTools.isEmpty( string ) )
			{
				throw new IllegalArgumentException( ( i + 1 ) + ": " + string );
			}

			result.set( locale, string );
		}

		return result;
	}

	/**
	 * This helper-method converts a {@link LocalizableString} to a {@link
	 * LocalizedString}.
	 *
	 * If the {@link LocalizableString} is already a {@link LocalizedString}
	 * instance, that instance is returned; otherwise a {@link LocalizedString}
	 * if created using the {@code locales} parameter.
	 *
	 * @param locales           Locales to convert.
	 * @param localizableString {@link LocalizableString}.
	 *
	 * @return {@link LocalizedString}.
	 */
	@NotNull
	public static LocalizedString convert( @NotNull final Iterable<Locale> locales, @NotNull final LocalizableString localizableString )
	{
		final LocalizedString result;

		if ( localizableString instanceof LocalizedString )
		{
			result = (LocalizedString)localizableString;
		}
		else
		{
			result = new LocalizedString();

			for ( final Locale locale : locales )
			{
				final String value = localizableString.get( locale );
				if ( value != null )
				{
					if ( result.isEmpty() )
					{
						result.set( value );
					}
					else if ( !value.equals( result.get( locale ) ) )
					{
						result.set( locale, value );
					}
				}
			}
		}

		return result;
	}

	/**
	 * Parse string that was generated by {@link #toString}.
	 *
	 * @param string String to parse.
	 *
	 * @return Localized string.
	 *
	 * @throws IllegalArgumentException if the string was malformed.
	 */
	@NotNull
	public static LocalizedString parse( @Nullable final String string )
	{
		final LocalizedString result = new LocalizedString();

		if ( string != null )
		{
			if ( !PropertyTools.fromString( result._values, true, string ) )
			{
				result._values.clear();
				result.set( string );
			}
		}

		return result;
	}

	/**
	 * Clear all values.
	 */
	public void clear()
	{
		_values.clear();
	}

	/**
	 * Clear all properties and copy from {@link LocalizedString}. This is a
	 * no-op method if the specified {@link LocalizedString} is the same as this
	 * one.
	 *
	 * @param other {@link LocalizedString} to copy.
	 */
	public void set( @NotNull final LocalizedString other )
	{
		//noinspection ObjectEquality
		if ( other != this )
		{
			clear();

			for ( final String locale : other.getLocales() )
			{
				final String string = other.get( locale );
				if ( string != null )
				{
					set( locale, string );
				}
			}
		}
	}

	/**
	 * Get any available string.
	 *
	 * @return String; {@code null} if no suitable string was found.
	 */
	@Nullable
	public String get()
	{
		return get( (String)null );
	}

	/**
	 * Get string for the specified locale.
	 *
	 * @param locale Locale to get string for ({@code null} to get any string).
	 *
	 * @return String for the specified locale; {@code null} if no suitable
	 * string was found.
	 */
	@Override
	@Nullable
	public String get( @Nullable final Locale locale )
	{
		return get( locale, null );
	}

	/**
	 * Get string for the specified locale.
	 *
	 * @param locale       Locale to get string for; {@code null} for any.
	 * @param defaultValue Value to return if no string is available.
	 *
	 * @return String for the specified locale; {@code defaultValue} if no
	 * suitable string was found.
	 */
	@Contract( value = "_, !null -> !null", pure = true )
	@Nullable
	public String get( @Nullable final Locale locale, @Nullable final String defaultValue )
	{
		return get( ( locale == null ) ? null : locale.toString(), defaultValue );
	}

	/**
	 * Get string for the specified locale.
	 *
	 * @param locale Locale to get string for; {@code null} for any.
	 *
	 * @return String for the specified locale; {@code null} if no suitable
	 * string was found.
	 */
	@Override
	@Nullable
	public String get( @Nullable final String locale )
	{
		return get( locale, null );
	}

	/**
	 * Get string for the specified locale.
	 *
	 * @param locale       Locale to get string for; {@code null} for any.
	 * @param defaultValue Value to return if no string is available.
	 *
	 * @return String for the specified locale; {@code defaultValue} if no
	 * suitable string was found.
	 */
	@Contract( value = "_, !null -> !null", pure = true )
	@Nullable
	public String get( @Nullable final String locale, @Nullable final String defaultValue )
	{
		String result;

		String key = ( locale == null ) ? "" : locale;
		while ( true )
		{
			result = getSpecific( key );
			if ( ( result != null ) || key.isEmpty() )
			{
				break;
			}

			final int end = key.lastIndexOf( '_' );
			key = ( end < 0 ) ? "" : key.substring( 0, end );
		}

		if ( ( result == null ) && ( locale == null ) )
		{
			result = getSpecific( "en" );

			if ( result == null )
			{
				final Enumeration<?> names = _values.propertyNames();
				if ( names.hasMoreElements() )
				{
					result = getSpecific( (String)names.nextElement() );
				}
			}
		}

		if ( result == null )
		{
			result = defaultValue;
		}

		return result;
	}

	/**
	 * Get string for the specified locale and only that locale.
	 *
	 * @param locale Locale to get string for; {@code null} or empty string for
	 *               the default locale.
	 *
	 * @return String for the specified locale; {@code null} if no entry was
	 * found.
	 */
	@Nullable
	public String getSpecific( @Nullable final String locale )
	{
		return _values.getProperty( ( locale == null ) ? "" : locale );
	}

	/**
	 * Set the default string.
	 *
	 * @param string String to set.
	 *
	 * @throws IllegalArgumentException if invalid arguments are specified.
	 */
	public void set( @NotNull final String string )
	{
		set( "", string );
	}

	/**
	 * Set string for the specified locale.
	 *
	 * @param locale Locale to set string for; {@code null} to set the default.
	 * @param string String to set.
	 *
	 * @throws IllegalArgumentException if invalid arguments are specified.
	 */
	public void set( @Nullable final Locale locale, @NotNull final String string )
	{
		set( ( locale == null ) ? "" : locale.toString(), string );
	}

	/**
	 * Set string for the specified locale.
	 *
	 * @param locale Locale to set string for; {@code null} or empty string to
	 *               set the default.
	 * @param string String to set.
	 *
	 * @throws IllegalArgumentException if {@code string} is empty.
	 */
	public void set( @Nullable final String locale, @NotNull final String string )
	{
		_values.setProperty( ( locale == null ) ? "" : locale, string );
	}

	/**
	 * Get locales for which a value is provided.
	 *
	 * @return Set of locale strings for which values are provided (may be
	 * empty).
	 */
	@NotNull
	public Set<String> getLocales()
	{
		return _values.stringPropertyNames();
	}

	/**
	 * Test whether at least one string value is available.
	 *
	 * @return {@code true} if no string values are available; {@code false} if
	 * at least one string value is available.
	 */
	public boolean isEmpty()
	{
		return _values.isEmpty();
	}

	/**
	 * Remove string for the specified locale.
	 *
	 * @param locale Locale to remove string for; {@code null} to remove the
	 *               default.
	 */
	public void remove( @Nullable final Locale locale )
	{
		remove( ( locale == null ) ? "" : locale.toString() );
	}

	/**
	 * Remove string for the specified locale.
	 *
	 * @param locale Locale to remove string for; {@code null} or empty string
	 *               to remove the default.
	 */
	public void remove( @Nullable final String locale )
	{
		_values.remove( ( locale == null ) ? "" : locale );
	}

	@Override
	public boolean equals( final Object obj )
	{
		return ( obj instanceof LocalizedString ) && _values.equals( ( (LocalizedString)obj )._values );
	}

	@Override
	public int hashCode()
	{
		return _values.hashCode();
	}

	/**
	 * Create a string containing all localized string contents that may be
	 * parsed using the {@link #parse} method.
	 *
	 * @return String with localized string contents.
	 *
	 * @see #parse
	 */
	@Override
	public String toString()
	{
		return PropertyTools.toString( _values );
	}

	/**
	 * Create a string containing all localized string contents that may be
	 * parsed using the {@link #parse} method.
	 *
	 * @param localizedString {@link LocalizedString} to convert; {@code null}
	 *                        to return {@code null}.
	 *
	 * @return String with localized string contents; {@code null} if parameter
	 * was {@code null} or empty.
	 *
	 * @see #parse
	 */
	@Nullable
	public static String toString( @Nullable final LocalizedString localizedString )
	{
		return ( localizedString != null ) && !localizedString.isEmpty() ? localizedString.toString() : null;
	}

	/**
	 * Compare another {@link LocalizedString} to this one.
	 *
	 * @param locale Locale to compare strings for.
	 * @param other  Other {@link LocalizedString} to compare with.
	 *
	 * @return {@code -1} if {@code this} comes before {@code other}; {@code 1}
	 * if {@code this} comes after {@code other}; {@code 0} if {@code this} and
	 * {@code other} have equal ranking.
	 */
	public int compareTo( @NotNull final Locale locale, @NotNull final LocalizableString other )
	{
		final int result;

		//noinspection ObjectEquality
		if ( other == this )
		{
			result = 0;
		}
		else
		{
			final String string1 = get( locale );
			final String string2 = other.get( locale );

			//noinspection StringEquality
			result = ( string1 == string2 ) ? 0 : ( string1 == null ) ? 1 : ( string2 == null ) ? -1 : HumaneStringComparator.DEFAULT.compare( string1, string2 );
		}

		return result;
	}

	/**
	 * Convert {@link LocalizedString} to a map.
	 *
	 * @return Map strings by locale.
	 */
	@NotNull
	public Map<String, String> toMap()
	{
		return PropertyTools.toMap( _values );
	}

	/**
	 * Set localized strings from map. The map maps locales to strings. The
	 * reverse is done using the {@link #toMap} method.
	 *
	 * @param map Map to get localized strings from.
	 */
	public void fromMap( @NotNull final Map<String, String> map )
	{
		for ( final Map.Entry<String, String> entry : map.entrySet() )
		{
			set( entry.getKey(), entry.getValue() );
		}
	}

	/**
	 * Comparator implementation for {@link LocalizedString}s.
	 */
	@SuppressWarnings( "ClassNameSameAsAncestorName" )
	public static class Comparator
	implements java.util.Comparator<LocalizedString>
	{
		/**
		 * Locale to compare strings for.
		 */
		@NotNull
		private final Locale _locale;

		/**
		 * Create comparator.
		 *
		 * @param locale Locale to compare strings for.
		 */
		public Comparator( @NotNull final Locale locale )
		{
			_locale = locale;
		}

		@Override
		public int compare( final LocalizedString o1, final LocalizedString o2 )
		{
			//noinspection ObjectEquality
			return ( o1 == o2 ) ? 0 : ( o1 == null ) ? 1 : ( o2 == null ) ? -1 : o1.compareTo( _locale, o2 );
		}
	}
}
