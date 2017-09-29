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

import java.lang.reflect.*;
import java.math.*;
import java.util.*;
import java.util.regex.*;

import org.jetbrains.annotations.*;

/**
 * This class contains functionality to work with {@link Properties}.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "unused" )
public class PropertyTools
{
	/**
	 * Create {@link Properties} from key/value arguments.
	 *
	 * @param keysAndValues Key and value strings.
	 *
	 * @return Resulting {@link Properties}.
	 *
	 * @throws IllegalArgumentException if argument list is {@code null} or its
	 * length is not a multiple of 2, or contains {@code null} elements.
	 */
	public static Properties create( @NotNull final String... keysAndValues )
	{
		final Properties result = new Properties();

		for ( int i = 0; i < keysAndValues.length; i += 2 )
		{
			final String key = keysAndValues[ i ];
			if ( key == null )
			{
				throw new IllegalArgumentException( "arg[" + i + "] = null" );
			}

			final String value = keysAndValues[ i + 1 ];
			if ( value == null )
			{
				throw new IllegalArgumentException( "arg[" + ( i + 1 ) + "] = null" );
			}

			result.setProperty( key, value );
		}

		return result;
	}

	/**
	 * Get properties from a string with format "{key}={value}[{separator}...]".
	 * The result is returned as a Properties object. Whitespace is ignored, the
	 * string may contain standard escapes like "\n", "\t", etc. and values may
	 * be surrounded by single or double quotes (allowing a value to contain
	 * leading or trailing whitespace).
	 *
	 * @param propertyString String with properties (may be {@code null}).
	 *
	 * @return Properties object, or {@code null} if a syntax error was
	 * encountered (property without or with a malformed value).
	 */
	public static Properties fromString( @Nullable final CharSequence propertyString )
	{
		return fromString( null, propertyString, false );
	}

	/**
	 * Get properties from a string with format "{key}={value}[{separator}...]".
	 * The result is returned as a {@link Properties} object. Whitespace is
	 * ignored, the string may contain standard escapes like "\n", "\t", etc.
	 * and values may be surrounded by single or double quotes (allowing a value
	 * to contain leading or trailing whitespace and separators).
	 *
	 * If specified, {@code defaults} will be used for default property values.
	 * If the property string contains errors, it will be returned unaltered.
	 *
	 * If {@code createIfNeeded} is set, then a new {@link Properties} will be
	 * created when the property string contained syntax errors, and no {@code
	 * defaults} are specified (set to {@code null}),
	 *
	 * @param defaults       Default properties (may be {@code null}).
	 * @param propertyString String with properties (may be {@code null})..
	 * @param createIfNeeded Create new {@link Properties} if needed.
	 *
	 * @return {@link Properties} instance (see comments).
	 */
	@SuppressWarnings( "ConstantConditions" )
	public static Properties fromString( @Nullable final Properties defaults, @Nullable final CharSequence propertyString, final boolean createIfNeeded )
	{
		final Properties result;
		if ( propertyString == null )
		{
			if ( createIfNeeded && ( defaults == null ) )
			{
				result = new Properties();
			}
			else
			{
				result = defaults;
			}
		}
		else
		{
			final Properties properties = new Properties( defaults );
			final boolean ok = fromString( properties, propertyString );
			if ( ok )
			{
				result = properties;
			}
			else if ( createIfNeeded && ( defaults == null ) )
			{
				result = properties;
				properties.clear();
			}
			else
			{
				result = defaults;
			}
		}
		return result;
	}

	/**
	 * Get properties from a string with format "{key}['='|':']{value}[{separator}...]"
	 * into the specified {@link Properties} object. Whitespace is ignored, the
	 * value may contain standard escapes like "\n", "\t", etc. and values may
	 * be surrounded by single or double quotes (allowing a value to contain
	 * leading or trailing whitespace and separators).
	 *
	 * The result is {@code true} if all properties in the string were parsed
	 * successfully. If a problem occurs, the result will be {@code false}, but
	 * the result {@link Properties} object may be altered for properties that
	 * have been parsed.
	 *
	 * @param properties     Target {@link Properties} object.
	 * @param propertyString String with properties (may be {@code null})..
	 *
	 * @return {@code true} if the properties were parsed successfully; {@code
	 * false} if the property string was malformed.
	 */
	public static boolean fromString( @NotNull final Properties properties, @Nullable final CharSequence propertyString )
	{
		return fromString( properties, false, propertyString );
	}

	/**
	 * Get properties from a string with format "{key}['='|':']{value}[{separator}...]"
	 * into the specified {@link Properties} object. Whitespace is ignored, the
	 * value may contain standard escapes like "\n", "\t", etc. and values may
	 * be surrounded by single or double quotes (allowing a value to contain
	 * leading or trailing whitespace and separators).
	 *
	 * The result is {@code true} if all properties in the string were parsed
	 * successfully. If a problem occurs, the result will be {@code false}, but
	 * the result {@link Properties} object may be altered for properties that
	 * have been parsed.
	 *
	 * @param properties      Target {@link Properties} object.
	 * @param allowEmptyNames {@code true} to allow empty keys.
	 * @param propertyString  String with properties (may be {@code null})..
	 *
	 * @return {@code true} if the properties were parsed successfully; {@code
	 * false} if the property string was malformed.
	 */
	@SuppressWarnings( "MethodWithMultipleReturnPoints" )
	public static boolean fromString( @NotNull final Properties properties, final boolean allowEmptyNames, @Nullable final CharSequence propertyString )
	{
		final String separators = ",\r\n";
		if ( propertyString == null )
		{
			return false;
		}

		final int len = propertyString.length();
		final StringBuffer sb = new StringBuffer();

		int pos = 0;

		while ( ( pos >= 0 ) && ( pos < len ) )
		{
			/*
			 * Skip whitespace
			 */
			while ( Character.isWhitespace( propertyString.charAt( pos ) ) )
			{
				if ( ++pos == len )
				{
					return true;
				}
			}

			/*
			 * Get property name
			 */
			final int namePos = pos;
			final StringBuilder nameBuffer = new StringBuilder();

			boolean escaped = false;
			char ch;
			while ( true )
			{
				ch = propertyString.charAt( pos );
				if ( !escaped && ( ch == '\\' ) )
				{
					escaped = true;
				}
				else
				{
					if ( escaped )
					{
						final char unescaped;
						switch ( ch )
						{
							case 'r':
								unescaped = '\r';
								break;
							case 'n':
								unescaped = '\n';
								break;
							default:
								unescaped = ch;
						}
						nameBuffer.append( unescaped );
						escaped = false;
					}
					else if ( Character.isWhitespace( ch ) || ( ch == '=' ) || ( ch == ':' ) )
					{
						break;
					}
					else
					{
						nameBuffer.append( ch );
					}
				}

				if ( ++pos == len )
				{
					return false;
				}
			}

			if ( !allowEmptyNames && ( pos == namePos ) )
			{
				return false;
			}

			final String name = nameBuffer.toString();

			/*
			 * Skip whitespace
			 */
			while ( Character.isWhitespace( propertyString.charAt( pos ) ) )
			{
				if ( ++pos == len )
				{
					return false;
				}
			}

			/*
			 * Require '=' or ':' character.
			 */
			ch = propertyString.charAt( pos );
			if ( ( ( ch != ':' ) && ( ch != '=' ) ) || ( ++pos == len ) )
			{
				return false;
			}

			/*
			 * Skip whitespace
			 */
			while ( Character.isWhitespace( propertyString.charAt( pos ) ) )
			{
				if ( ++pos == len )
				{
					return false;
				}
			}

			/*
			 * Get value
			 */
			final char quote = propertyString.charAt( pos );
			if ( ( quote == '\'' ) || ( quote == '"' ) )
			{
				/*
				 * Skip quote
				 */
				if ( pos++ == len )
				{
					return false;
				}

				/*
				 * Get quoted value
				 */
				sb.setLength( 0 );
				pos = TextTools.unescape( propertyString, pos, sb, String.valueOf( quote ) );
				if ( pos < 0 )
				{
					return false;
				}

				properties.setProperty( name, sb.toString() );

				/*
				 * Skip whitespace
				 */
				while ( pos < len && Character.isWhitespace( propertyString.charAt( pos ) ) )
				{
					pos++;
				}

				/*
				 * Require separator or end of line
				 */
				if ( pos < len && separators.indexOf( (int)propertyString.charAt( pos++ ) ) < 0 )
				{
					return false;
				}
			}
			else
			{
				/*
				 * Get unquoted value, remove whitespace
				 */
				sb.setLength( 0 );
				pos = TextTools.unescape( propertyString, pos, sb, separators );
				final String value = sb.toString();

				properties.setProperty( name, value.trim() );
			}
		}

		return true;
	}

	/**
	 * Create a string with format "property=value,..." based on the specified
	 * {@link Properties} object.
	 *
	 * @param properties {@link Properties} object.
	 *
	 * @return String with properties, may be empty string if invalid or empty
	 * Properties object was provided.
	 */
	public static String toString( @Nullable final Properties properties )
	{
		return toString( properties, false );
	}

	/**
	 * Create a string with format "property=value,..." based on the specified
	 * {@link Properties} object. If desired, empty elements (properties with a
	 * zero-length value) will be filtered out.
	 *
	 * @param properties          {@link Properties} object.
	 * @param filterEmptyElements If set, properties with zero-length values
	 *                            will not be included in the result.
	 *
	 * @return String with properties, may be empty string if invalid or empty
	 * Properties object was provided.
	 */
	public static String toString( @Nullable final Properties properties, final boolean filterEmptyElements )
	{
		StringBuffer sb = null;
		if ( ( properties != null ) && !properties.isEmpty() )
		{
			for ( final Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); )
			{
				final String name = (String)e.nextElement();
				final String value = properties.getProperty( name );
				final int len = value.length();

				if ( !filterEmptyElements || ( len > 0 ) )
				{
					if ( sb == null )
					{
						sb = new StringBuffer( name.length() + 2 + len + 1 );
					}
					else
					{
						sb.append( ',' );
					}

					escapeKey( sb, name );
					sb.append( '=' );
					escapeValue( sb, value );
				}
			}
		}

		return ( sb == null ) ? "" : sb.toString();
	}

	/**
	 * Copy properties from one {@link Properties} object to another.
	 *
	 * @param src Source properties.
	 * @param dst Destination properties
	 */
	public static void copy( @NotNull final Properties src, @NotNull final Properties dst )
	{
		//noinspection ObjectEquality
		if ( src != dst )
		{
			for ( final Enumeration<?> e = src.propertyNames(); e.hasMoreElements(); )
			{
				final String propertyName = (String)e.nextElement();
				dst.setProperty( propertyName, src.getProperty( propertyName ) );
			}
		}
	}

	/**
	 * Appends the given key to the buffer, adding escape sequences as specified
	 * by {@link Properties#load}.
	 *
	 * @param buffer Buffer to add the escaped key to.
	 * @param key    Key to be appended.
	 */
	private static void escapeKey( @NotNull final StringBuffer buffer, @NotNull final CharSequence key )
	{
		final Pattern pattern = Pattern.compile( "[=:\\s\\\\]" );
		final Matcher matcher = pattern.matcher( key );

		while ( matcher.find() )
		{
			final String group = matcher.group();
			matcher.appendReplacement( buffer, "" );
			buffer.append( '\\' );

			switch ( group.charAt( 0 ) )
			{
				case '\r':
					buffer.append( 'r' );
					break;

				case '\n':
					buffer.append( 'n' );
					break;

				default:
					buffer.append( group );
			}
		}

		matcher.appendTail( buffer );
	}

	/**
	 * Appends the given value to the buffer, adding escape sequences where
	 * needed.
	 *
	 * @param buffer Buffer to add the escaped value to.
	 * @param value  Value to be appended.
	 */
	private static void escapeValue( final StringBuffer buffer, final String value )
	{
		final int quotePos = buffer.length();
		char quote = '\0';

		final int length = value.length();

		int index = 0;
		do
		{
			final char ch = ( index < length ) ? value.charAt( index ) : '\0';
			switch ( ch )
			{
				case 0: /* empty string */
					//noinspection fallthrough
				case ' ':
					//noinspection fallthrough
				case ',':
					//noinspection fallthrough
				case '=':
					if ( quote == '\0' )
					{
						// prefer " as quote, but use ' when value contains ", but no '
						quote = ( ( value.indexOf( (int)'"', index ) >= 0 ) && ( value.indexOf( (int)'\'', index ) < 0 ) ) ? '\'' : '"';
						buffer.insert( quotePos, quote );
					}

					if ( ch != '\0' )
					{
						TextTools.escape( buffer, ch );
					}
					break;

				case '\'':
					if ( quote == '\0' )
					{
						buffer.insert( quotePos, quote = '"' );
					}
					else if ( quote == '\'' )
					{
						buffer.append( '\\' );
					}

					buffer.append( ch );
					break;

				case '"':
					if ( quote == '\0' )
					{
						buffer.insert( quotePos, quote = '\'' );
					}
					else if ( quote == '"' )
					{
						buffer.append( '\\' );
					}

					buffer.append( ch );
					break;

				default:
					TextTools.escape( buffer, ch );
			}

			index++;
		}
		while ( index < length );

		if ( quote != '\0' )
		{
			buffer.append( quote );
		}
	}

	/**
	 * Get {@link BigDecimal} property with the specified name from the
	 * specified properties.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return {@link BigDecimal} value.
	 *
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 * @throws NumberFormatException if the property value was malformed.
	 */
	public static BigDecimal getBigDecimal( @Nullable final Properties properties, @NotNull final String name )
	{
		return new BigDecimal( getString( properties, name ) );
	}

	/**
	 * Get double property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Double value; Default value if the property is not found or
	 * malformed.
	 */
	public static BigDecimal getBigDecimal( @Nullable final Properties properties, @NotNull final String name, @Nullable final BigDecimal defaultValue )
	{
		BigDecimal result = defaultValue;

		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( ( string != null ) && !string.isEmpty() )
		{
			try
			{
				result = new BigDecimal( string );
			}
			catch ( final NumberFormatException nfe )
			{
				/* ignored, will return default value */
			}
		}

		//noinspection ConstantConditions
		return result;
	}

	/**
	 * Get {@link BigInteger} property with the specified name from the
	 * specified properties.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return {@link BigInteger} value.
	 *
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 * @throws NumberFormatException if the property value was malformed.
	 */
	public static BigInteger getBigInteger( @Nullable final Properties properties, @NotNull final String name )
	{
		return new BigInteger( getString( properties, name ) );
	}

	/**
	 * Get double property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Double value; Default value if the property is not found or
	 * malformed.
	 */
	public static BigInteger getBigInteger( @Nullable final Properties properties, @NotNull final String name, final BigInteger defaultValue )
	{
		BigInteger result = defaultValue;

		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( ( string != null ) && !string.isEmpty() )
		{
			try
			{
				result = new BigInteger( string );
			}
			catch ( final NumberFormatException nfe )
			{
				/* ignored, will return default value */
			}
		}

		return result;
	}

	/**
	 * Get boolean property with the specified name from the specified
	 * properties.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return Boolean value.
	 *
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 * @throws IllegalArgumentException if the property value was malformed.
	 */
	public static boolean getBoolean( @Nullable final Properties properties, @NotNull final String name )
	{
		final boolean result;

		final String value = getString( properties, name );

		if ( "true".equalsIgnoreCase( value ) ||
		     "1".equals( value ) ||
		     "y".equalsIgnoreCase( value ) ||
		     "yes".equalsIgnoreCase( value ) ||
		     "on".equalsIgnoreCase( value ) )
		{
			result = true;
		}
		else if ( "false".equalsIgnoreCase( value ) ||
		          "0".equals( value ) ||
		          "n".equalsIgnoreCase( value ) ||
		          "no".equalsIgnoreCase( value ) ||
		          "off".equalsIgnoreCase( value ) )
		{
			result = false;
		}
		else
		{
			throw new IllegalArgumentException( '\'' + value + "' is not a boolean" );
		}

		return result;
	}

	/**
	 * Get boolean property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Boolean value; Default value if the property is not found or
	 * malformed.
	 */
	public static boolean getBoolean( @Nullable final Properties properties, @NotNull final String name, final boolean defaultValue )
	{
		final String s = ( properties != null ) ? properties.getProperty( name ) : null;

		return "true".equalsIgnoreCase( s ) ||
		       "1".equals( s ) ||
		       "y".equalsIgnoreCase( s ) ||
		       "yes".equalsIgnoreCase( s ) ||
		       ( defaultValue && !( "false".equalsIgnoreCase( s ) ||
		                            "0".equals( s ) ||
		                            "n".equalsIgnoreCase( s ) ||
		                            "no".equalsIgnoreCase( s ) ) );
	}

	/**
	 * Get boolean property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Boolean value; Default value if the property is not found or
	 * malformed.
	 */
	@Nullable
	public static Boolean getBoolean( @Nullable final Properties properties, @NotNull final String name, @Nullable final Boolean defaultValue )
	{
		final Boolean result;

		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( string == null )
		{
			result = defaultValue;
		}
		else if ( "true".equalsIgnoreCase( string ) ||
		          "1".equals( string ) ||
		          "y".equalsIgnoreCase( string ) ||
		          "yes".equalsIgnoreCase( string ) )
		{
			result = Boolean.TRUE;
		}
		else if ( "false".equalsIgnoreCase( string ) ||
		          "0".equals( string ) ||
		          "n".equalsIgnoreCase( string ) ||
		          "no".equalsIgnoreCase( string ) )
		{
			result = Boolean.FALSE;
		}
		else
		{
			result = defaultValue;
		}

		return result;
	}

	/**
	 * Get character property with the specified name from the specified
	 * properties.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return Character value.
	 *
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 */
	public static char getChar( @Nullable final Properties properties, @NotNull final String name )
	{
		final String value = getString( properties, name );
		return value.isEmpty() ? '\0' : value.charAt( 0 );
	}

	/**
	 * Get character property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Character value; Default value if the property is not found or
	 * malformed.
	 */
	public static char getChar( @Nullable final Properties properties, @NotNull final String name, final char defaultValue )
	{
		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		return ( string != null ) ? string.isEmpty() ? '\0' : string.charAt( 0 ) : defaultValue;
	}

	/**
	 * Get double property with the specified name from the specified
	 * properties.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return Double value.
	 *
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 * @throws NumberFormatException if the property value was malformed.
	 */
	public static double getDouble( @Nullable final Properties properties, @NotNull final String name )
	{
		return Double.parseDouble( getString( properties, name ) );
	}

	/**
	 * Get double property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Double value; Default value if the property is not found or
	 * malformed.
	 */
	public static double getDouble( @Nullable final Properties properties, @NotNull final String name, final double defaultValue )
	{
		double result = defaultValue;

		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( ( string != null ) && !string.isEmpty() )
		{
			try
			{
				result = Double.parseDouble( string );
			}
			catch ( final NumberFormatException nfe )
			{
				/* ignored, will return default value */
			}
		}

		return result;
	}

	/**
	 * Get enumeration property with the specified name.
	 *
	 * @param properties Properties to read from.
	 * @param name       Property name.
	 * @param enumType   Type of enumeration.
	 *
	 * @return Enumeration value.
	 *
	 * @throws IllegalArgumentException if the stored value can not be
	 * converted.
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 */
	public static <T extends Enum<T>> T getEnum( @Nullable final Properties properties, @NotNull final String name, final Class<T> enumType )
	{
		T result;

		final String string = getString( properties, name );
		try
		{
			result = Enum.valueOf( enumType, string );
		}
		catch ( final IllegalArgumentException e )
		{
			try
			{
				final int intValue = Integer.parseInt( string );
				result = enumType.getEnumConstants()[ intValue ];
			}
			catch ( final NumberFormatException ignored )
			{
				throw e;
			}
			catch ( final IndexOutOfBoundsException ignored )
			{
				throw e;
			}
		}

		return result;
	}

	/**
	 * Get integer property with the specified name.
	 *
	 * @param properties   Properties to read from.
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 *
	 * @return Enumeration value; {@code defaultValue} if property is not found.
	 *
	 * @throws IllegalArgumentException if the stored value can not be
	 * converted.
	 * @throws ArrayIndexOutOfBoundsException if an out-of-bounds integer value
	 * was set.
	 */
	public static <T extends Enum<T>> T getEnum( @Nullable final Properties properties, @NotNull final String name, final T defaultValue )
	{
		T result = defaultValue;

		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( ( string != null ) && !string.isEmpty() )
		{
			final Class<T> enumType = (Class<T>)defaultValue.getClass();

			try
			{
				result = Enum.valueOf( enumType, string );
			}
			catch ( final IllegalArgumentException ignored )
			{
				try
				{
					final int intValue = Integer.parseInt( string );
					result = enumType.getEnumConstants()[ intValue ];
				}
				catch ( final NumberFormatException nfe )
				{
					/* ignored, will return default value */
				}
				catch ( final IndexOutOfBoundsException ioobe )
				{
					/* ignored, will return default value */
				}
			}
		}

		return result;
	}

	/**
	 * Get enumeration list property with the specified name. The property value
	 * must be a comma separated list of enumeration constant names or indices
	 * of the same type as the component type of the given default value. The
	 * list may contain {@code null} values either as the string {@code "null"}
	 * or the empty string. If the property value is the empty string, an empty
	 * array with the appropriate component type is returned.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 *
	 * @return Array of enumeration values; {@code defaultValue} if property is
	 * not found.
	 *
	 * @throws IllegalArgumentException if the stored value can not be
	 * converted.
	 * @throws ArrayIndexOutOfBoundsException if an out-of-bounds integer value
	 * was set.
	 */
	public static <T extends Enum<T>> T[] getEnumList( @Nullable final Properties properties, @NotNull final String name, @NotNull final T[] defaultValue )
	{
		T[] result = defaultValue;

		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( string != null )
		{
			final Class<T[]> enumArrayType = (Class<T[]>)defaultValue.getClass();
			final Class<T> enumType = (Class<T>)enumArrayType.getComponentType();

			if ( string.isEmpty() )
			{
				result = (T[])Array.newInstance( enumType, 0 );
			}
			else
			{
				final String[] elements = string.split( ",", -1 );
				result = (T[])Array.newInstance( enumType, elements.length );

				for ( int i = 0; i < elements.length; i++ )
				{
					final String element = elements[ i ];
					if ( ( element == null ) || element.isEmpty() || "null".equals( element ) )
					{
						result[ i ] = null;
					}
					else
					{
						try
						{
							result[ i ] = Enum.valueOf( enumType, element );
						}
						catch ( final IllegalArgumentException e )
						{
							try
							{
								final int intValue = Integer.parseInt( element );
								result[ i ] = enumType.getEnumConstants()[ intValue ];
							}
							catch ( final NumberFormatException ignored )
							{
								throw e;
							}
							catch ( final IndexOutOfBoundsException ioobe )
							{
								throw new IllegalArgumentException( ioobe );
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Get float property with the specified name from the specified
	 * properties.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return Float value.
	 *
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 * @throws NumberFormatException if the property value was malformed.
	 */
	public static float getFloat( @Nullable final Properties properties, @NotNull final String name )
	{
		return Float.parseFloat( getString( properties, name ) );
	}

	/**
	 * Get float property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Float value; Default value if the property is not found or
	 * malformed.
	 */
	public static float getFloat( @Nullable final Properties properties, @NotNull final String name, final float defaultValue )
	{
		float result = defaultValue;

		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( ( string != null ) && !string.isEmpty() )
		{
			try
			{
				result = Float.parseFloat( string );
			}
			catch ( final NumberFormatException e )
			{
				/* ignored, will return default value */
			}
		}

		return result;
	}

	/**
	 * Get integer property with the specified name from the specified
	 * properties.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return Integer value.
	 *
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 * @throws NumberFormatException if the property value was malformed.
	 */
	public static int getInt( @Nullable final Properties properties, @NotNull final String name )
	{
		return Integer.parseInt( getString( properties, name ) );
	}

	/**
	 * Get integer property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Integer value; Default value if the property is not found or
	 * malformed.
	 */
	public static int getInt( @Nullable final Properties properties, @NotNull final String name, final int defaultValue )
	{
		return getInt( properties, name, defaultValue, 10 );
	}

	/**
	 * Get integer property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 * @param radix        Radix to be used to parse the property.
	 *
	 * @return Integer value; Default value if the property is not found or
	 * malformed.
	 */
	public static int getInt( @Nullable final Properties properties, @NotNull final String name, final int defaultValue, final int radix )
	{
		int result = defaultValue;

		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( ( string != null ) && !string.isEmpty() )
		{
			try
			{
				result = Integer.parseInt( string, radix );
			}
			catch ( final NumberFormatException e )
			{
				/* ignored, will return default value */
			}
		}

		return result;
	}

	/**
	 * Get long integer property with the specified name from the specified
	 * properties.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return Long integer value.
	 *
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 * @throws NumberFormatException if the property value was malformed.
	 */
	public static long getLong( @Nullable final Properties properties, @NotNull final String name )
	{
		return Long.parseLong( getString( properties, name ) );
	}

	/**
	 * Get long integer property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Long integer value; Default value if the property is not found or
	 * malformed.
	 */
	public static long getLong( @Nullable final Properties properties, @NotNull final String name, final long defaultValue )
	{
		long result = defaultValue;

		final String string = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( ( string != null ) && !string.isEmpty() )
		{
			try
			{
				result = Long.parseLong( string );
			}
			catch ( final NumberFormatException e )
			{
				/* ignored, will return default value */
			}
		}

		return result;
	}

	/**
	 * Get nested Properties with the specified name from the specified
	 * properties.
	 *
	 * @param properties     Properties object to get property from.
	 * @param name           Property name.
	 * @param createIfNeeded Create new {@link Properties} if needed.
	 *
	 * @return Properties object ({@code null} if property is not found and
	 * {@code createIfNeeded} is {@code false}).
	 */
	public static Properties getProperties( @Nullable final Properties properties, @NotNull final String name, final boolean createIfNeeded )
	{
		return fromString( null, ( properties == null ) ? null : getString( properties, name, null ), createIfNeeded );
	}

	/**
	 * Get string property with the specified name from the specified
	 * properties.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return String value.
	 *
	 * @throws NoSuchElementException if no property exists with the specified
	 * name.
	 */
	@NotNull
	public static String getString( @Nullable final Properties properties, @NotNull final String name )
	{
		final String result = ( properties != null ) ? properties.getProperty( name ) : null;
		if ( result == null )
		{
			throw new NoSuchElementException( name );
		}

		return result;
	}

	/**
	 * Get string property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return String value; default value if the property is not found.
	 */
	public static String getString( @NotNull final Properties properties, @NotNull final String name, @Nullable final String defaultValue )
	{
		return properties.getProperty( name, defaultValue );
	}

	/**
	 * Retrieves a list of strings with specified name.
	 *
	 * @param properties Properties object to get property from.
	 * @param name       Property name.
	 *
	 * @return List of strings.
	 */
	@NotNull
	public static List<String> getStringList( @Nullable final Properties properties, @NotNull final String name )
	{
		final List<String> result;

		final Properties nested = getProperties( properties, name, false );
		if ( nested == null )
		{
			final String value = ( properties != null ) ? properties.getProperty( name ) : null;
			result = ( value == null ) ? Collections.<String>emptyList() : Collections.singletonList( value );
		}
		else
		{
			result = new ArrayList<String>();
			for ( int i = 0; ; i++ )
			{
				final String value = nested.getProperty( String.valueOf( i ) );
				if ( value != null )
				{
					result.add( value );
				}
				else
				{
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Get localized string property with the specified name from the specified
	 * properties.
	 *
	 * @param properties   Properties object to get property from.
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return Localized string value; default value if the property is absent.
	 *
	 * @throws IllegalArgumentException if the property value was malformed.
	 * @throws NoSuchElementException if the value was missing and no default
	 * was provided.
	 */
	@NotNull
	public static LocalizedString getLocalizedString( @NotNull final Properties properties, @NotNull final String name, @Nullable final LocalizedString defaultValue )
	{
		final String value = getString( properties, name, null );

		final LocalizedString result;
		if ( value == null )
		{
			result = defaultValue;
		}
		else
		{
			result = LocalizedString.parse( value );
		}

		if ( result == null )
		{
			throw new NoSuchElementException( name );
		}

		return result;
	}

	/**
	 * Set double property with the specified name in the 'properties' field.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, final BigDecimal value )
	{
		final String string = ( value != null ) ? value.toPlainString() : null;
		set( properties, name, string );
	}

	/**
	 * Set boolean property with the specified name in the 'properties' field.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, final boolean value )
	{
		set( properties, name, String.valueOf( value ) );
	}

	/**
	 * Set character property with the specified name in the 'properties'
	 * field.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, final char value )
	{
		set( properties, name, ( value == '\0' ) ? null : String.valueOf( value ) );
	}

	/**
	 * Set integer property with the specified name in the 'properties' field.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, final int value )
	{
		set( properties, name, String.valueOf( value ) );
	}

	/**
	 * Set long property with the specified name in the 'properties' field.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, final long value )
	{
		set( properties, name, String.valueOf( value ) );
	}

	/**
	 * Set float property with the specified name in the 'properties' field.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, final float value )
	{
		set( properties, name, String.valueOf( value ) );
	}

	/**
	 * Set double property with the specified name in the 'properties' field.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, final double value )
	{
		set( properties, name, String.valueOf( value ) );
	}

	/**
	 * Set enumeration property with the specified name in the 'properties'
	 * field.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, final Enum<?> value )
	{
		set( properties, name, ( value == null ) ? null : value.name() );
	}

	/**
	 * Set enumeration list property with the specified name in the 'properties'
	 * field.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param values     Values to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, final Enum<?>... values )
	{
		final StringBuilder sb = new StringBuilder();
		for ( final Enum<?> value : values )
		{
			if ( sb.length() > 0 )
			{
				sb.append( ',' );
			}

			if ( value == null )
			{
				if ( values.length == 1 )
				{
					/*
					 * 'null' must be explicitly written; otherwise the property
					 * value would represent an empty list.
					 */
					sb.append( "null" );
				}
//				else
//				{
//					/* 'null' may be omitted */
//				}
			}
			else
			{
				sb.append( value.name() );
			}
		}
		set( properties, name, sb.toString() );
	}

	/**
	 * Set nested {@link Properties} with the specified name in the 'properties'
	 * field.
	 *
	 * @param properties       Target properties.
	 * @param name             Property name.
	 * @param nestedProperties Nested properties to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, @Nullable final Properties nestedProperties )
	{
		set( properties, name, ( nestedProperties == null ) ? null : toString( nestedProperties ) );
	}

	/**
	 * Set property with the specified name in the 'properties' field, if the
	 * value is {@code null}, the property is removed.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, @Nullable final String value )
	{
		if ( value == null )
		{
			properties.remove( name );
		}
		else
		{
			properties.setProperty( name, String.valueOf( value ) );
		}
	}

	/**
	 * Sets the property with the specified name in the given properties
	 * instance to a list of strings. The list must not contain any {@code null}
	 * values.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 *
	 * @throws IllegalArgumentException if the list of strings contains a {@code
	 * null} value.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, @Nullable final List<String> value )
	{
		if ( value == null )
		{
			properties.remove( name );
		}
		else
		{
			final Properties nested = new Properties();
			for ( int i = 0; i < value.size(); i++ )
			{
				final String string = value.get( i );
				if ( string == null )
				{
					throw new IllegalArgumentException( "list contains a null at index " + i );
				}
				nested.setProperty( String.valueOf( i ), string );
			}
			set( properties, name, nested );
		}
	}

	/**
	 * Set localized string property with the specified name in the given
	 * properties instance. If the value is {@code null}, the property is
	 * removed.
	 *
	 * @param properties Target properties.
	 * @param name       Property name.
	 * @param value      Value to set.
	 */
	public static void set( @NotNull final Properties properties, @NotNull final String name, @Nullable final LocalizedString value )
	{
		if ( value == null )
		{
			properties.remove( name );
		}
		else
		{
			properties.setProperty( name, value.toString() );
		}
	}

	/**
	 * Get array with sorted property keys from a {@link Properties} object.
	 *
	 * @param properties Properties object to get keys from.
	 *
	 * @return Sorted keys.
	 */
	@NotNull
	public static List<String> getSortedKeys( @NotNull final Properties properties )
	{
		final List<String> result = new ArrayList<String>();

		for ( final Enumeration<String> e = (Enumeration<String>)properties.propertyNames(); e.hasMoreElements(); )
		{
			result.add( e.nextElement() );
		}

		Collections.sort( result );

		return result;
	}

	/**
	 * Get human-friendly string representation of a properties object. This
	 * will align keys and values, and try to automatically detect nested
	 * properties and indent them for improved readability.
	 *
	 * @param properties Properties to create a string representation of.
	 *
	 * @return Human-friendly string representation of the properties.
	 */
	public static String toFriendlyString( @NotNull final Properties properties )
	{
		final String result;

		if ( properties.isEmpty() )
		{
			result = "(no properties)";
		}
		else
		{
			final StringBuffer sb = new StringBuffer();
			toFriendlyString( sb, 0, properties );
			result = sb.toString();
		}

		return result;
	}

	/**
	 * Get human-friendly string representation of a properties object. This
	 * will align keys and values, and try to automatically detect nested
	 * properties and indent them for improved readability.
	 *
	 * This the internal (recursive) helper method that will write the result to
	 * a {@code StringBuffer} and allow indentation to be specified.
	 *
	 * @param sb           Buffer to write string to.
	 * @param indentSpaces Number of spaces to prepend to each line.
	 * @param properties   Properties to create a string representation of.
	 */
	private static void toFriendlyString( @NotNull final StringBuffer sb, final int indentSpaces, @NotNull final Properties properties )
	{
		final List<String> namesList = new ArrayList<String>();
		int maxKeyLength = 0;

		for ( final Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); )
		{
			final String name = ( (String)e.nextElement() );

			namesList.add( name );
			maxKeyLength = Math.max( maxKeyLength, name.length() );
		}

		final String[] names = namesList.toArray( new String[ namesList.size() ] );
		Arrays.sort( names );

		for ( final String name : names )
		{
			final String value = properties.getProperty( name );

			TextTools.appendSpace( sb, indentSpaces );

			final Properties nested = ( value.indexOf( (int)'=' ) > 0 ) ? fromString( value ) : null;
			if ( nested != null )
			{
				sb.append( name );
				sb.append( ":\n" );
				toFriendlyString( sb, indentSpaces + 4, nested );
			}
			else
			{
				TextTools.appendFixed( sb, name, maxKeyLength );
				sb.append( " = " );
				sb.append( value );
				sb.append( '\n' );
			}
		}
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private PropertyTools()
	{
	}

	/**
	 * Convert map to {@link Properties}.
	 *
	 * @param map Map to convert with properties.
	 *
	 * @return {@link Properties}.
	 */
	@NotNull
	public static Properties fromMap( @Nullable final Map<String, String> map )
	{
		final Properties result = new Properties();
		fromMap( result, map );
		return result;
	}

	/**
	 * Convert map to {@link Properties}.
	 *
	 * @param result Result properties.
	 * @param map    Map to convert with properties.
	 *
	 * @return Result properties.
	 */
	public static Properties fromMap( @NotNull final Properties result, @Nullable final Map<String, String> map )
	{
		if ( map != null )
		{
			for ( final Map.Entry<String, String> entry : map.entrySet() )
			{
				result.setProperty( entry.getKey(), entry.getValue() );
			}
		}

		return result;
	}

	/**
	 * Convert {@link Properties} to a map.
	 *
	 * @param properties Properties to convert.
	 *
	 * @return Map with properties.
	 */
	@NotNull
	public static Map<String, String> toMap( @Nullable final Properties properties )
	{
		final Map<String, String> result = new HashMap<String, String>();
		toMap( result, properties );
		return result;
	}

	/**
	 * Convert {@link Properties} to a map.
	 *
	 * @param result     Result map.
	 * @param properties Properties to convert.
	 *
	 * @return Result map.
	 */
	@NotNull
	public static Map<String, String> toMap( @NotNull final Map<String, String> result, @Nullable final Properties properties )
	{
		if ( properties != null )
		{
			for ( final String propertyName : properties.stringPropertyNames() )
			{
				result.put( propertyName, properties.getProperty( propertyName ) );
			}
		}

		return result;
	}
}
