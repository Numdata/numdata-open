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

import org.jetbrains.annotations.*;

/**
 * This class provides an enhanced API to the {@link Properties} class. It
 * allows properties other than {@link String}s to be defined, even nesting of
 * properties possible. However, whenever string values are required by the
 * original API, values will be converted on-demand.
 *
 * @author Peter S. Heijnen
 */
public class NestedProperties
extends Properties
{
	/**
	 * Serialization version.
	 */
	private static final long serialVersionUID = 7198127381954175103L;

	/**
	 * Construct empty nested field.
	 */
	public NestedProperties()
	{
	}

	/**
	 * Create clone of specified original nested properties.
	 *
	 * @param original Original object to clone.
	 */
	@SuppressWarnings( "OverridableMethodCallDuringObjectConstruction" )
	public NestedProperties( @Nullable final Properties original )
	{
		if ( original != null )
		{
			copyFrom( original );
		}
	}

	/**
	 * Copy properties from other {@code NestedProperties}.
	 *
	 * @param other Nested properties to copy.
	 */
	public void set( @NotNull final Properties other )
	{
		//noinspection ObjectEquality
		if ( other != this )
		{
			clear();
			copyFrom( other );
		}
	}

	/**
	 * Copy properties from other {@code NestedProperties}.
	 *
	 * @param other Nested properties to copy.
	 */
	public void copyFrom( final Properties other )
	{
		if ( other == null )
		{
			throw new NullPointerException( "other" );
		}

		//noinspection ObjectEquality
		if ( other != this )
		{
			for ( @NotNull final String propertyName : other.stringPropertyNames() )
			{
				final Object value = ( (Map<?, ?>)other ).get( propertyName );
				put( propertyName, ( value == null ) ? other.getProperty( propertyName ) : ( value instanceof Properties ) ? new NestedProperties( (Properties)value ) : value );
			}
		}
	}

	@NotNull
	@Override
	public Set<String> stringPropertyNames()
	{
		final Set<String> result = new HashSet<String>( size() );

		for ( final Enumeration<?> e = propertyNames(); e.hasMoreElements(); )
		{
			final Object key = e.nextElement();
			if ( key instanceof String )
			{
				result.add( (String)key );
			}
		}

		return result;
	}

	/**
	 * Get {@link BigDecimal} property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return {@link BigDecimal} value; default value if the property is not found
	 *         or malformed.
	 */
	public BigDecimal getBigDecimal( @NotNull final String name, @Nullable final BigDecimal defaultValue )
	{
		final BigDecimal result;

		final Object value = get( name );

		if ( value == null )
		{
			result = defaultValue;
		}
		else if ( value instanceof BigDecimal )
		{
			result = (BigDecimal)value;
		}
		else if ( value instanceof BigInteger )
		{
			result = new BigDecimal( (BigInteger)value );
			put( name, result );
		}
		else if ( ( value instanceof Short ) || ( value instanceof  Integer ) || ( value instanceof Long ) )
		{
			result = new BigDecimal( ((Number)value).longValue() );
			put( name, result );
		}
		else if ( value instanceof Number )
		{
			result =  BigDecimal.valueOf( ((Number)value).doubleValue() );
			put( name, result );
		}
		else if ( value instanceof String )
		{
			result = new BigDecimal( (String)value );
			put( name, result );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		//noinspection ConstantConditions
		return result;
	}

	/**
	 * Get {@link BigInteger} property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default return value.
	 *
	 * @return {@link BigInteger} value; default value if the property is not found
	 *         or malformed.
	 */
	public BigInteger getBigInteger( @NotNull final String name, @Nullable final BigInteger defaultValue )
	{
		final BigInteger result;

		final Object value = get( name );

		if ( value == null )
		{
			result = defaultValue;
		}
		else if ( value instanceof BigInteger )
		{
			result = (BigInteger)value;
		}
		else if ( value instanceof Number )
		{
			result = BigInteger.valueOf( ( (Number)value ).longValue() );
			put( name, result );
		}
		else if ( value instanceof String )
		{
			result = new BigInteger( (String)value );
			put( name, result );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		//noinspection ConstantConditions
		return result;
	}

	/**
	 * Get boolean property with the specified name.
	 *
	 * @param name Property name.
	 *
	 * @return Boolean value; {@code false} if the property is not found or
	 *         malformed.
	 *
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public boolean getBoolean( @NotNull final String name )
	{
		return getBoolean( name, false );
	}

	/**
	 * Get boolean property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 *
	 * @return Boolean value; {@code defaultValue} if property is not found.
	 *
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public boolean getBoolean( @NotNull final String name, final boolean defaultValue )
	{
		final boolean result;

		final Object value = get( name );

		if ( value == null )
		{
			result = defaultValue;
		}
		else if ( value instanceof Boolean )
		{
			result = (Boolean)value;
		}
		else if ( value instanceof Number )
		{
			result = ( ( (Number)value ).intValue() > 0 );

			put( name, result ? Boolean.TRUE : Boolean.FALSE );
		}
		else if ( value instanceof String )
		{
			final String s = (String)value;

			result = "true".equalsIgnoreCase( s )
			         || "1".equals( s )
			         || "y".equalsIgnoreCase( s )
			         || "yes".equalsIgnoreCase( s );

			put( name, result ? Boolean.TRUE : Boolean.FALSE );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		return result;
	}

	/**
	 * Get character property with the specified name.
	 *
	 * @param name Property name.
	 *
	 * @return Character; '\0' if property is not found.
	 *
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public char getChar( @NotNull final String name )
	{
		return getChar( name, '\0' );
	}

	/**
	 * Get character property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 *
	 * @return Character; {@code defaultValue} if property is not found.
	 *
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public char getChar( @NotNull final String name, final char defaultValue )
	{
		final char result;

		final Object value = get( name );

		if ( value == null )
		{
			result = defaultValue;
		}
		else if ( value instanceof Character )
		{
			result = (Character)value;
		}
		else if ( value instanceof Number )
		{
			result = (char)( ( (Number)value ).intValue() );

			put( name, result );
		}
		else if ( value instanceof String )
		{
			final String s = (String)value;
			result = !s.isEmpty() ? s.charAt( 0 ) : '\0';
			put( name, result );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		return result;
	}

	/**
	 * Get double property with the specified name.
	 *
	 * @param name Property name.
	 *
	 * @return Double value; 0 if property is not found.
	 *
	 * @throws NumberFormatException if the stored string can't be parsed.
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public double getDouble( @NotNull final String name )
	{
		return getDouble( name, 0.0 );
	}

	/**
	 * Get double property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 *
	 * @return Double value; {@code defaultValue} if property is not found.
	 *
	 * @throws NumberFormatException if the stored string can't be parsed.
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public double getDouble( @NotNull final String name, final double defaultValue )
	{
		final double result;

		final Object value = get( name );

		if ( value == null )
		{
			result = defaultValue;
		}
		else if ( value instanceof Double )
		{
			result = (Double)value;
		}
		else if ( value instanceof Number )
		{
			result = ( (Number)value ).doubleValue();
			put( name, result );
		}
		else if ( value instanceof String )
		{
			result = Double.parseDouble( (String)value );
			put( name, result );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		return result;
	}

	/**
	 * Get enumeration property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 * @param enumType     Type of enumeration to get.
	 *
	 * @return Enumeration constant; {@code defaultValue} if property is not
	 *         found.
	 *
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 * @throws ArrayIndexOutOfBoundsException if an out-of-bounds integer value was
	 * set.
	 */
	public <T extends Enum<T>> T getEnum( @NotNull final String name, @Nullable final T defaultValue, @NotNull final Class<T> enumType )
	{
		T result;

		final Object value = get( name );

		if ( value == null )
		{
			result = defaultValue;
		}
		else if ( enumType.isInstance( value ) )
		{
			result = enumType.cast( value );
		}
		else if ( value instanceof Number )
		{
			result = enumType.getEnumConstants()[ ( (Number)value ).intValue() ];
			put( name, result );
		}
		else if ( value instanceof String )
		{
			try
			{
				result = Enum.valueOf( enumType, (String)value );
			}
			catch ( final IllegalArgumentException e )
			{
				try
				{
					result = enumType.getEnumConstants()[ Integer.parseInt( (String)value ) ];
				}
				catch ( final NumberFormatException ignored )
				{
					throw e;
				}
			}

			put( name, result );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		//noinspection ConstantConditions
		return result;
	}

	/**
	 * Get float property with the specified name.
	 *
	 * @param name Property name.
	 *
	 * @return Float value; 0 if property is not found.
	 *
	 * @throws NumberFormatException if the stored string can't be parsed.
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public float getFloat( @NotNull final String name )
	{
		return getFloat( name, 0.0f );
	}

	/**
	 * Get float property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 *
	 * @return Long value; {@code defaultValue} if property is not found.
	 *
	 * @throws NumberFormatException if the stored string can't be parsed.
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public float getFloat( @NotNull final String name, final float defaultValue )
	{
		final float result;

		final Object value = get( name );

		if ( value == null )
		{
			result = defaultValue;
		}
		else if ( value instanceof Float )
		{
			result = (Float)value;
		}
		else if ( value instanceof Number )
		{
			result = ( (Number)value ).floatValue();
			put( name, result );
		}
		else if ( value instanceof String )
		{
			result = Float.parseFloat( (String)value );
			put( name, result );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		return result;
	}

	/**
	 * Get integer property with the specified name.
	 *
	 * @param name Property name.
	 *
	 * @return Integer value; 0 if property is not found.
	 */
	public int getInt( @NotNull final String name )
	{
		return getInt( name, 0, null );
	}

	/**
	 * Get integer property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 *
	 * @return Integer value; {@code defaultValue} if property is not found.
	 */
	public int getInt( @NotNull final String name, final int defaultValue )
	{
		return getInt( name, defaultValue, null );
	}

	/**
	 * Get integer property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 * @param values       String representation of integer values.
	 *
	 * @return Integer value; 0 if property is not found.
	 *
	 * @throws NumberFormatException if the stored string can't be parsed.
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public int getInt( @NotNull final String name, final int defaultValue, @Nullable final String[] values )
	{
		int result;

		final Object value = get( name );

		if ( value == null )
		{
			result = defaultValue;
		}
		else if ( value instanceof Integer )
		{
			result = (Integer)value;
		}
		else if ( value instanceof Number )
		{
			result = ( (Number)value ).intValue();
			put( name, result );
		}
		else if ( value instanceof String )
		{
			result = ( values != null ) ? ArrayTools.indexOf( value, values ) : -1;
			if ( result < 0 )
			{
				result = Integer.parseInt( (String)value );
			}

			put( name, result );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		return result;
	}

	/**
	 * Get long integer property with the specified name.
	 *
	 * @param name Property name.
	 *
	 * @return Long value; 0 if property is not found.
	 *
	 * @throws NumberFormatException if the stored string can't be parsed.
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public long getLong( @NotNull final String name )
	{
		return getLong( name, 0L );
	}

	/**
	 * Get long integer property with the specified name.
	 *
	 * @param name         Property name.
	 * @param defaultValue Default value for property.
	 *
	 * @return Long value; {@code defaultValue} if property is not found.
	 *
	 * @throws NumberFormatException if the stored string can't be parsed.
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public long getLong( @NotNull final String name, final long defaultValue )
	{
		final long result;

		final Object value = get( name );

		if ( value == null )
		{
			result = defaultValue;
		}
		else if ( value instanceof Long )
		{
			result = (Long)value;
		}
		else if ( value instanceof Number )
		{
			result = ( (Number)value ).longValue();
			put( name, result );
		}
		else if ( value instanceof String )
		{
			result = Long.parseLong( (String)value );
			put( name, result );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		return result;
	}

	/**
	 * Get nested properties from these properties.
	 *
	 * @param name Property name.
	 *
	 * @return {@code NestedProperties} instance.
	 *
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public NestedProperties getNested( @NotNull final String name )
	{
		return getNested( name, true );
	}

	/**
	 * Get nested properties from these properties.
	 *
	 * @param name              Property name.
	 * @param createIfNecessary Create nested properties if not present; return
	 *                          {@code null} otherwise.
	 *
	 * @return {@code NestedProperties} instance; or {@code null} if the property
	 *         was not set and {@code createIfNecessary} is {@code false}.
	 *
	 * @throws IllegalArgumentException if the stored value can not be converted.
	 */
	public NestedProperties getNested( @NotNull final String name, final boolean createIfNecessary )
	{
		final NestedProperties result;

		final Object value = get( name );

		if ( value == null )
		{
			if ( createIfNecessary )
			{
				result = new NestedProperties();
				put( name, result );
			}
			else
			{
				result = null;
			}
		}
		else if ( value instanceof NestedProperties )
		{
			result = (NestedProperties)value;
		}
		else if ( value instanceof String )
		{
			final String string = (String)value;

			result = new NestedProperties();
			if ( !string.isEmpty() && !PropertyTools.fromString( result, string ) )
			{
				throw new IllegalArgumentException( "can't parse properties: " + string );
			}

			put( name, result );
		}
		else
		{
			throw new IllegalArgumentException( name + '=' + value );
		}

		//noinspection ConstantConditions
		return result;
	}

	@Override
	public String getProperty( @NotNull final String name )
	{
		final String result;

		final Object value = get( name );

		if ( value == null )
		{
			result = ( defaults != null ) ? defaults.getProperty( name ) : null;
		}
		else if ( value instanceof String )
		{
			result = (String)value;
		}
		else
		{
			final Class<?> valueClass = value.getClass();
			if ( value instanceof Properties )
			{
				final Properties properties = (Properties)value;
				if ( properties.isEmpty() )
				{
					result = "";
				}
				else
				{
					result = PropertyTools.toString( properties );
//					put( name, result ); /* should we write back string values or retain the original? */
				}
			}
			else if ( valueClass.isArray() )
			{
				final int length = Array.getLength( value );

				final StringBuilder sb = new StringBuilder();

				for ( int i = 0; i < length; i++ )
				{
					final Object element = Array.get( value, i );

					if ( i > 0 )
					{
						sb.append( ',' );
					}

					if ( element != null )
					{
						sb.append( element );
					}
					else if ( length == 1 )
					{
						/*
						 * 'null' must be explicitly written; otherwise the property
						 * value would represent an empty list.
						 */
						sb.append( "null" );
					}
//					else
//					{
//						/* 'null' may be omitted */
//					}
				}

				result = sb.toString();
			}
			else
			{
				result = String.valueOf( value );
//				put( name, result ); /* should we write back string values or retain the original? */
			}
		}

		//noinspection ConstantConditions
		return result;
	}

	/**
	 * Set boolean property with the specified name in the 'properties' field.
	 *
	 * @param name  Property name.
	 * @param value Value to set.
	 */
	public void set( @NotNull final String name, @NotNull final BigDecimal value )
	{
		put( name, value.toPlainString() );
	}

	/**
	 * Set boolean property with the specified name in the 'properties' field.
	 *
	 * @param name  Property name.
	 * @param value Value to set.
	 */
	public void set( @NotNull final String name, final boolean value )
	{
		put( name, value ? Boolean.TRUE : Boolean.FALSE );
	}

	/**
	 * Set character property with the specified name in the 'properties' field.
	 *
	 * @param name  Property name.
	 * @param value Value to set.
	 */
	public void set( @NotNull final String name, final char value )
	{
		put( name, value );
	}

	/**
	 * Set enumeration property with the specified name in the 'properties' field.
	 *
	 * @param name  Property name.
	 * @param value Value to set.
	 */
	public void set( @NotNull final String name, @NotNull final Enum<?> value )
	{
		put( name, value );
	}

	/**
	 * Set enumeration list property with the specified name in the 'properties'
	 * field.
	 *
	 * @param name   Property name.
	 * @param values Values to set.
	 */
	public void set( @NotNull final String name, @NotNull final Enum<?>... values )
	{
		put( name, values );
	}

	/**
	 * Set integer property with the specified name in the 'properties' field.
	 *
	 * @param name  Property name.
	 * @param value Value to set.
	 */
	public void set( @NotNull final String name, final int value )
	{
		put( name, value );
	}

	/**
	 * Set long property with the specified name in the 'properties' field.
	 *
	 * @param name  Property name.
	 * @param value Value to set.
	 */
	public void set( @NotNull final String name, final long value )
	{
		put( name, value );
	}

	/**
	 * Set float property with the specified name in the 'properties' field.
	 *
	 * @param name  Property name.
	 * @param value Value to set.
	 */
	public void set( @NotNull final String name, final float value )
	{
		put( name, value );
	}

	/**
	 * Set double property with the specified name in the 'properties' field.
	 *
	 * @param name  Property name.
	 * @param value Value to set.
	 */
	public void set( @NotNull final String name, final double value )
	{
		put( name, value );
	}

	/**
	 * Set nested properties.
	 *
	 * @param name   Property name.
	 * @param nested Nested properties to set.
	 *
	 * @throws NullPointerException if {@code nested} is {@code null}.
	 */
	public void set( @NotNull final String name, @NotNull final NestedProperties nested )
	{
		put( name, nested );
	}

	/**
	 * Set String property with the specified name in the 'properties' field.
	 *
	 * @param name  Property name.
	 * @param value Value to set ({@code null} to remove key).
	 */
	public void set( @NotNull final String name, @Nullable final String value )
	{
		if ( value == null )
		{
			remove( name );
		}
		else
		{
			put( name, value );
		}
	}
}
