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
package com.numdata.oss.web.form;

import java.lang.reflect.*;
import java.math.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Target type: reflected. Value is a field/property of the target object
 * accessible through reflection. The field may be a class field, or a property
 * accessible using getter and setter methods.
 *
 * @author G.B.M. Rupert
 */
public class ReflectedFieldTarget
implements FieldTarget
{
	/**
	 * Name of object property or field.
	 */
	private final String _propertyName;

	/**
	 * Name of form field.
	 */
	private final String _fieldName;

	/**
	 * Target object.
	 */
	private final Object _target;

	/**
	 * Class field used for a reflection target with direct (public) access to
	 * the field. This supersedes the getter/setter method.
	 */
	private final Field _field;

	/**
	 * Class method used for a reflection target with getter/setter access to
	 * the field. This is the getter method.
	 */
	private final Method _getter;

	/**
	 * Construct new reflected field target.
	 *
	 * @param target Target object.
	 * @param name   Name of field .
	 */
	public ReflectedFieldTarget( final Object target, final String name )
	{
		this( target, name, name );
	}

	/**
	 * Construct new reflected field target.
	 *
	 * @param target       Target object.
	 * @param propertyName Name of object property or field.
	 * @param fieldName    Name of form field.
	 */
	public ReflectedFieldTarget( final Object target, final String propertyName, final String fieldName )
	{
		_propertyName = propertyName;
		_fieldName = fieldName;

		/*
		 * Try reflection to public field.
		 */
		final Class<?> targetClass = target.getClass();

		Field field;
		try
		{
			field = targetClass.getField( propertyName );
		}
		catch ( final NoSuchFieldException ignored )
		{
			field = null;
			/* Ignore. */
		}

		/*
		 * Use reflection to get getter method.
		 */
		Method getter = null;
		if ( field == null )
		{
			getter = getAccessor( targetClass, "get", propertyName, null );

			if ( getter == null )
			{
				getter = getAccessor( targetClass, "is", propertyName, null );
			}

			if ( getter == null )
			{
				getter = getAccessor( targetClass, "has", propertyName, null );
			}

			if ( getter == null )
			{
				throw new IllegalArgumentException( "Illegal target '" + target + "' for field '" + propertyName + '\'' );
			}
		}

		_target = target;
		_field = field;
		_getter = getter;
	}

	@Override
	@NotNull
	public String getName()
	{
		return _fieldName;
	}

	@Override
	@Nullable
	public String getValue()
	{
		try
		{
			final Object value;

			if ( _field != null )
			{
				value = _field.get( _target );
			}
			else
			{
				value = _getter.invoke( _target );
			}

			return toString( value );
		}
		catch ( final IllegalAccessException e )
		{
			throw new RuntimeException( "Access denied to accessor method while getting '" + getName() + '\'', e );
		}
		catch ( final InvocationTargetException e )
		{
			throw new RuntimeException( "Exception encountered while getting '" + getName() + '\'', e );
		}
	}

	@Override
	public void setValue( @Nullable final String value )
	{
		final Object target = _target;
		final Field field = _field;
		final Method getter = _getter;

		try
		{
			if ( field != null )
			{
				field.set( target, parse( value ) );
			}
			else if ( getter != null )
			{
				final Method setter = getAccessor( target.getClass(), "set", _propertyName, _getter.getReturnType() );
				if ( setter == null )
				{
					throw new RuntimeException( "property '" + _propertyName + "' is read-only" );
				}

				setter.invoke( target, parse( value ) );
			}
			else
			{
				throw new RuntimeException( "no field or getter defined" );
			}
		}
		catch ( final InvocationTargetException e )
		{
			throw new RuntimeException( "Exception encountered while setting '" + getName() + "' to " + TextTools.quote( value ), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new RuntimeException( "Access denied to accessor method while setting '" + getName() + "' to " + TextTools.quote( value ), e );
		}
	}

	/**
	 * Get accessor method for the specified property.
	 *
	 * @param aClass   Class to get the method from.
	 * @param prefix   Name prefix of method (e.g. "get", "set", "is").
	 * @param property Property name (e.g. "value").
	 * @param type     Argument type ({@code null} for none).
	 *
	 * @return Accessor method; {@code null} if the specified method was not
	 * found.
	 */
	@Nullable
	private static Method getAccessor( final Class<?> aClass, final String prefix, final String property, @Nullable final Class<?> type )
	{
		final String name = prefix + Character.toUpperCase( property.charAt( 0 ) ) + property.substring( 1 );
		@SuppressWarnings( "rawtypes" ) final Class<?>[] args = ( type == null ) ? new Class[] {} : new Class[] { type };

		Method result;

		try
		{
			result = aClass.getMethod( name, args );
		}
		catch ( final Exception ignored )
		{
			result = null;
		}

		return result;
	}

	/**
	 * Convert value to string.
	 *
	 * @param value Value to convert.
	 *
	 * @return String representation of value.
	 */
	@Nullable
	public static String toString( final Object value )
	{
		final String result;

		if ( value instanceof Calendar )
		{
			result = Long.toString( ( (Calendar)value ).getTime().getTime() );
		}
		else if ( value instanceof Date )
		{
			result = Long.toString( ( (Date)value ).getTime() );
		}
		else if ( value instanceof BigDecimal )
		{
			result = ( (BigDecimal)value ).toPlainString();
		}
		else if ( value instanceof Pattern )
		{
			result = ( (Pattern)value ).pattern();
		}
		else
		{
			result = ( value != null ) ? String.valueOf( value ) : null;
		}

		return result;
	}

	/**
	 * Check type and perform necessary type conversion. {@code null} is always
	 * accepted.
	 *
	 * @param value Value to check.
	 *
	 * @return Possibly type-converted value.
	 *
	 * @throws IllegalArgumentException if the value does not match the field
	 * type.
	 */
	@Nullable
	@SuppressWarnings( "ObjectEquality" )
	protected Object parse( @Nullable final String value )
	{
		Object result = value;

		final Class<?> type = ( _field != null ) ? _field.getType() : _getter.getReturnType();
		if ( ( value != null ) && ( type != String.class ) )
		{
			/**/
			if ( type == Boolean.class )
			{
				result = TextTools.isEmpty( value ) ? null : Boolean.valueOf( value );
			}
			else if ( type == BigDecimal.class )
			{
				result = TextTools.isEmpty( value ) ? null : new BigDecimal( value );
			}
			else if ( type == BigInteger.class )
			{
				result = TextTools.isEmpty( value ) ? null : new BigInteger( value );
			}
			else if ( type == boolean.class )
			{
				result = Boolean.valueOf( value );
			}
			else if ( type == Byte.class )
			{
				result = TextTools.isEmpty( value ) ? null : new Byte( value );
			}
			else if ( type == byte.class )
			{
				result = new Byte( value );
			}
			else if ( type == Short.class )
			{
				result = TextTools.isEmpty( value ) ? null : new Short( value );
			}
			else if ( type == short.class )
			{
				result = new Short( value );
			}
			else if ( type == Character.class )
			{
				result = TextTools.isEmpty( value ) ? null : Character.valueOf( value.charAt( 0 ) );
			}
			else if ( type == char.class )
			{
				result = new Character( !value.isEmpty() ? value.charAt( 0 ) : '\0' );
			}
			else if ( type == Integer.class )
			{
				result = TextTools.isEmpty( value ) ? null : new Integer( value );
			}
			else if ( type == int.class )
			{
				result = Integer.valueOf( value );
			}
			else if ( type == Long.class )
			{
				result = TextTools.isEmpty( value ) ? null : new Long( value );
			}
			else if ( type == long.class )
			{
				result = new Long( value );
			}
			else if ( type == Float.class )
			{
				result = TextTools.isEmpty( value ) ? null : new Float( value );
			}
			else if ( type == float.class )
			{
				result = new Float( value );
			}
			else if ( type == Double.class )
			{
				result = TextTools.isEmpty( value ) ? null : new Double( value );
			}
			else if ( type == double.class )
			{
				result = new Double( value );
			}
			else if ( type == Calendar.class )
			{
				result = TextTools.isEmpty( value ) ? null : CalendarTools.getInstance( new Date( Long.parseLong( value ) ) );
			}
			else if ( type == Currency.class )
			{
				result = TextTools.isEmpty( value ) ? null : Currency.getInstance( value.trim() );
			}
			else if ( type == Date.class )
			{
				result = TextTools.isEmpty( value ) ? null : new Date( Long.parseLong( value ) );
			}
			else if ( type == Locale.class )
			{
				result = TextTools.isEmpty( value ) ? null : LocaleTools.parseLocale( value );
			}
			else if ( type == Pattern.class )
			{
				result = TextTools.isEmpty( value ) ? null : Pattern.compile( value );
			}
			else if ( type == URL.class )
			{
				try
				{
					result = TextTools.isEmpty( value ) ? null : new URL( value );
				}
				catch ( final MalformedURLException e )
				{
					throw new IllegalArgumentException( "Malformed '" + getName() + "' URL: " + value, e );
				}
			}
			else if ( type.isEnum() )
			{
				//noinspection rawtypes
				result = TextTools.isEmpty( value ) ? null : Enum.valueOf( (Class)type, value );
			}
			else
			{
				throw new IllegalArgumentException( "Don't know how to convert '" + getName() + "' java.lang.String value to " + type );
			}
		}

		return result;
	}
}
