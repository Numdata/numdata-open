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
package com.numdata.oss.db;

import java.lang.reflect.*;
import java.math.*;
import java.sql.Date;
import java.sql.*;
import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * {@link FieldHandler} implementation based on reflection.
 *
 * @author Peter S. Heijnen
 */
public class ReflectedFieldHandler
implements FieldHandler
{
	/**
	 * {@link Field} that is handled.
	 */
	@NotNull
	private final Field _field;

	/**
	 * Construct handler for field.
	 *
	 * @param clazz     Class containing the field.
	 * @param fieldName Name of the field.
	 *
	 * @throws IllegalArgumentException if no such field is found.
	 */
	public ReflectedFieldHandler( @NotNull final Class<?> clazz, @NotNull final String fieldName )
	{
		try
		{
			_field = clazz.getField( fieldName );
		}
		catch ( NoSuchFieldException e )
		{
			throw new IllegalArgumentException( e );
		}
	}

	/**
	 * Construct handler for field.
	 *
	 * @param field {@link Field} to handle.
	 */
	public ReflectedFieldHandler( @NotNull final Field field )
	{
		_field = field;
	}

	/**
	 * Get {@link Field} that is handled.
	 *
	 * @return {@link Field} that is handled.
	 */
	@NotNull
	public Field getField()
	{
		return _field;
	}

	@NotNull
	public String getName()
	{
		return _field.getName();
	}

	@NotNull
	public Class<?> getJavaType()
	{
		return _field.getType();
	}

	@NotNull
	public Class<?> getSqlType()
	{
		final Class<?> result;

		final Class<?> javaType = getJavaType();

		if ( BigDecimal.class.isAssignableFrom( javaType ) )
		{
			result = BigDecimal.class;
		}
		else if ( Boolean.class.isAssignableFrom( javaType ) || boolean.class.isAssignableFrom( javaType ) )
		{
			result = Boolean.class;
		}
		else if ( Byte.class.isAssignableFrom( javaType ) || byte.class.isAssignableFrom( javaType ) )
		{
			result = Byte.class;
		}
		else if ( Character.class.isAssignableFrom( javaType ) || char.class.isAssignableFrom( javaType ) )
		{
			result = Character.class;
		}
		else if ( Double.class.isAssignableFrom( javaType ) || double.class.isAssignableFrom( javaType ) )
		{
			result = Double.class;
		}
		else if ( Float.class.isAssignableFrom( javaType ) || float.class.isAssignableFrom( javaType ) )
		{
			result = Float.class;
		}
		else if ( Integer.class.isAssignableFrom( javaType ) || int.class.isAssignableFrom( javaType ) )
		{
			result = Integer.class;
		}
		else if ( Long.class.isAssignableFrom( javaType ) || long.class.isAssignableFrom( javaType ) )
		{
			result = Long.class;
		}
		else if ( Short.class.isAssignableFrom( javaType ) || short.class.isAssignableFrom( javaType ) )
		{
			result = Short.class;
		}
		else if ( byte[].class.isAssignableFrom( javaType ) ||
		          java.util.Date.class.isAssignableFrom( javaType ) ||
		          Enum.class.isAssignableFrom( javaType ) )
		{
			result = javaType;
		}
		else
		{
			result = String.class;
		}

		return result;
	}

	public Object getFieldValue( @NotNull final Object object )
	{
		final Object value;
		try
		{
			value = _field.get( object );
		}
		catch ( IllegalAccessException e )
		{
			throw new IllegalArgumentException( e );
		}
		return value;
	}

	public void setColumnData( @NotNull final Object object, @NotNull final PreparedStatement ps, final int columnIndex )
	throws SQLException
	{
		final Object value = getFieldValue( object );

		if ( value == null )
		{
			final Field field = _field;
			if ( field.getAnnotation( NotNull.class ) != null )
			{
				throw new SQLException( "@NotNull " + field + "' set to null" );
			}

			ps.setString( columnIndex, null );
		}
		else if ( value instanceof Boolean )
		{
			ps.setBoolean( columnIndex, (Boolean)value );
		}
		else if ( value instanceof BigDecimal )
		{
			ps.setBigDecimal( columnIndex, (BigDecimal)value );
		}
		else if ( value instanceof Byte )
		{
			ps.setByte( columnIndex, (Byte)value );
		}
		else if ( value instanceof byte[] )
		{
			ps.setBytes( columnIndex, (byte[])value );
		}
		else if ( value instanceof Integer )
		{
			ps.setInt( columnIndex, (Integer)value );
		}
		else if ( value instanceof Long )
		{
			ps.setLong( columnIndex, (Long)value );
		}
		else if ( value instanceof Short )
		{
			ps.setShort( columnIndex, (Short)value );
		}
		else if ( value instanceof Timestamp )
		{
			ps.setTimestamp( columnIndex, (Timestamp)value );
		}
		else if ( value instanceof Time )
		{
			ps.setTime( columnIndex, (Time)value );
		}
		else if ( value instanceof Date )
		{
			ps.setDate( columnIndex, (Date)value );
		}
		else if ( value instanceof java.util.Date )
		{
			ps.setTimestamp( columnIndex, new Timestamp( ( (java.util.Date)value ).getTime() ) );
		}
		else if ( value instanceof LocalizedString )
		{
			ps.setString( columnIndex, value.toString() );
		}
		else if ( value instanceof Properties )
		{
			ps.setString( columnIndex, PropertyTools.toString( (Properties)value ) );
		}
		else if ( value instanceof Enum<?> )
		{
			ps.setString( columnIndex, ( (Enum<?>)value ).name() );
		}
		else
		{
			ps.setString( columnIndex, value.toString() );
		}
	}

	public void getColumnData( @NotNull final Object object, @NotNull final ResultSet resultSet, final int columnIndex )
	throws SQLException
	{
		final Field field = _field;
		final Class<?> type = field.getType();

		try
		{
			if ( BigDecimal.class.isAssignableFrom( type ) )
			{
				field.set( object, resultSet.getBigDecimal( columnIndex ) );
			}
			else if ( type == boolean.class )
			{
				field.setBoolean( object, resultSet.getBoolean( columnIndex ) );
			}
			else if ( type == Boolean.class )
			{
				final boolean value = resultSet.getBoolean( columnIndex );
				field.set( object, resultSet.wasNull() ? null : value ? Boolean.TRUE : Boolean.FALSE );
			}
			else if ( type == byte.class )
			{
				field.setByte( object, resultSet.getByte( columnIndex ) );
			}
			else if ( type == Byte.class )
			{
				final byte value = resultSet.getByte( columnIndex );
				field.set( object, resultSet.wasNull() ? null : Byte.valueOf( value ) );
			}
			else if ( type == char.class )
			{
				final String string = resultSet.getString( columnIndex );
				field.setChar( object, ( ( string == null ) || string.isEmpty() ) ? '\0' : string.charAt( 0 ) );
			}
			else if ( type == Character.class )
			{
				final String string = resultSet.getString( columnIndex );
				field.set( object, ( ( string == null ) || string.isEmpty() ) ? null : Character.valueOf( string.charAt( 0 ) ) );
			}
			else if ( type == short.class )
			{
				field.setShort( object, resultSet.getShort( columnIndex ) );
			}
			else if ( type == Short.class )
			{
				final short value = resultSet.getShort( columnIndex );
				field.set( object, resultSet.wasNull() ? null : Short.valueOf( value ) );
			}
			else if ( type == int.class )
			{
				field.setInt( object, resultSet.getInt( columnIndex ) );
			}
			else if ( type == Integer.class )
			{
				final int value = resultSet.getInt( columnIndex );
				field.set( object, resultSet.wasNull() ? null : Integer.valueOf( value ) );
			}
			else if ( type == long.class )
			{
				field.setLong( object, resultSet.getLong( columnIndex ) );
			}
			else if ( type == Long.class )
			{
				final long value = resultSet.getLong( columnIndex );
				field.set( object, resultSet.wasNull() ? null : Long.valueOf( value ) );
			}
			else if ( type == float.class )
			{
				field.setFloat( object, resultSet.getFloat( columnIndex ) );
			}
			else if ( type == Float.class )
			{
				final float value = resultSet.getFloat( columnIndex );
				field.set( object, resultSet.wasNull() ? null : Float.valueOf( value ) );
			}
			else if ( type == double.class )
			{
				field.setDouble( object, resultSet.getDouble( columnIndex ) );
			}
			else if ( type == Double.class )
			{
				final double value = resultSet.getDouble( columnIndex );
				field.set( object, resultSet.wasNull() ? null : Double.valueOf( value ) );
			}
			else if ( type == java.util.Date.class )
			{
				java.util.Date date = resultSet.getTimestamp( columnIndex );
				if ( date != null )
				{
					date = new java.util.Date( date.getTime() );
				}

				field.set( object, date );
			}
			else if ( type == String.class )
			{
				final Object obj = resultSet.getObject( columnIndex );
				field.set( object, ( obj == null ) ? null : String.valueOf( obj ) );
			}
			else if ( LocalizedString.class.isAssignableFrom( type ) )
			{
				LocalizedString localizedString = (LocalizedString)field.get( object );

				final String string = resultSet.getString( columnIndex );
				if ( string != null )
				{
					if ( localizedString == null )
					{
						localizedString = new LocalizedString();
						field.set( object, localizedString );
					}

					localizedString.set( LocalizedString.parse( string ) );
				}
				else if ( localizedString != null )
				{
					if ( Modifier.isFinal( field.getModifiers() ) ) /* can't set final field, but we can clear it */
					{
						localizedString.clear();
					}
					else
					{
						field.set( object, null );
					}
				}
			}
			else if ( Properties.class.isAssignableFrom( type ) )
			{
				Properties properties = (Properties)field.get( object );

				final String string = resultSet.getString( columnIndex );
				if ( string != null )
				{
					if ( properties == null )
					{
						properties = new Properties();
						field.set( object, properties );
					}
					else
					{
						properties.clear();
					}

					PropertyTools.fromString( properties, false, string );
				}
				else if ( properties != null )
				{
					if ( Modifier.isFinal( field.getModifiers() ) ) /* can't set final field, but we can clear it */
					{
						properties.clear();
					}
					else
					{
						field.set( object, null );
					}
				}
			}
			else if ( Enum.class.isAssignableFrom( type ) )
			{
				@SuppressWarnings ("rawtypes") final Class<? extends Enum> enumType = (Class<? extends Enum>)type;
				final String name = resultSet.getString( columnIndex );
				field.set( object, ( name == null ) ? null : Enum.valueOf( enumType, name ) );
			}
			else
			{
				field.set( object, resultSet.getObject( columnIndex ) );
			}
		}
		catch ( RuntimeException e )
		{
			throw new SQLException( e.toString(), e );
		}
		catch ( IllegalAccessException e )
		{
			throw new SQLException( e.toString(), e );
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + "[" + _field + "]";
	}
}
