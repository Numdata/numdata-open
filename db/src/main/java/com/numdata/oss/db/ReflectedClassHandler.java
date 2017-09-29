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

import org.jetbrains.annotations.*;

/**
 * {@link ClassHandler} implementation based on reflection.
 *
 * @author  Peter S. Heijnen
 */
public class ReflectedClassHandler
	extends ClassHandler
{
	/**
	 * Cache for {@link #getTableName} result.
	 */
	private String _tableName = null;

	/**
	 * Cache for {@link #getCreateStatement} result.
	 */
	private String _createStatement = null;

	/**
	 * Java field used for record id.
	 */
	private final Field _idField;

	/**
	 * Create class handler.
	 *
	 * @param   clazz   Class to handle.
	 */
	protected ReflectedClassHandler( @NotNull final Class<?> clazz )
	{
		super( clazz );

		Field idField = null;

		for ( final Field field : clazz.getFields() )
		{
			final FieldHandler fieldHandler = createFieldHandler( field );
			if ( fieldHandler != null )
			{
				addField( fieldHandler );

				if ( ( idField == null ) && isRecordIdField( field ) )
				{
					idField = field;
				}
			}
		}

		_idField = idField;
	}

	/**
	 * Test whether the specified field is the 'record id' field.
	 *
	 * @param   field   Field to test.
	 *
	 * @return  {@code true} if field is 'record id' field;
	 *          {@code false} otherwise.
	 */
	protected boolean isRecordIdField( @NotNull final Field field )
	{
		final TableRecord tableRecord = _clazz.getAnnotation( TableRecord.class );
		final String recordId = ( tableRecord != null ) ? tableRecord.recordId() : "ID";

		return recordId.equals( field.getName() );
	}

	/**
	 * Create {@link FieldHandler} for a {@link Field}. This may be overridden
	 * to implement special handlers. This may also return {@code null} to
	 * indicate that the field is not handled (i.e. it is ignored).
	 *
	 * @param   field   Field to test.
	 *
	 * @return  {@link FieldHandler} for field;
	 *          {@code null} if field is not handled.
	 */
	@Nullable
	protected FieldHandler createFieldHandler( @NotNull final Field field )
	{
		final FieldHandler result;

		final Class<?> type = field.getType();
		final int modifiers = field.getModifiers();

		if ( Modifier.isPublic( modifiers ) &&
		     !Modifier.isStatic( modifiers ) &&
		     !Modifier.isTransient( modifiers ) &&
		     !Modifier.isVolatile( modifiers ) &&
		     ( !Modifier.isFinal( modifiers ) || !type.isPrimitive() ) ) // primitives must be final
		{
			result = new ReflectedFieldHandler( field );
		}
		else
		{
			result = null;
		}

		return result;
	}

	@NotNull
	@Override
	public String getTableName()
	{
		String result = _tableName;
		if ( result == null )
		{
			final Class<?> clazz = _clazz;

			final TableRecord tableRecord = clazz.getAnnotation( TableRecord.class );
			if ( tableRecord != null )
			{
				result = tableRecord.tableName();
			}
			else /* fall back to 'old-school' field constant using reflection */
			{
				final String fieldName = "TABLE_NAME";

				try
				{
					final Field tableNameField = clazz.getField( fieldName );
					result = (String)tableNameField.get( null );
				}
				catch ( IllegalAccessException e )
				{
					throw new RuntimeException( "Access denied to '" + fieldName + "' constant in class '" + clazz.getName() + '\'', e );
				}
				catch ( NoSuchFieldException e )
				{
					throw new RuntimeException( "Missing '" + fieldName + "' constant in class '" + clazz.getName() + '\'', e );
				}
				catch ( NullPointerException e )
				{
					throw new RuntimeException( "The '" + fieldName + "' field of '" + clazz.getName() + "' is not static", e );
				}
				catch ( SecurityException e )
				{
					throw new RuntimeException( "Reflection access to '" + fieldName + "' field of '" + clazz.getName() + "' denied (" + e + ')', e );
				}
			}

			_tableName = result;
		}

		return result;
	}

	@NotNull
	@Override
	public String getCreateStatement()
	{
		String result = _createStatement;
		if ( result == null )
		{
			final Class<?> clazz = _clazz;

			final String fieldName = "MYSQL_CREATE_STATEMENT";

			try
			{
				final Field createStatementField = clazz.getField( fieldName );
				result = (String)createStatementField.get( null );
			}
			catch ( NoSuchFieldException e )
			{
				throw new RuntimeException( "No create '" + fieldName + " constant in class '" + clazz.getName() + '\'', e );
			}
			catch ( IllegalAccessException e )
			{
				throw new RuntimeException( "Access denied to '" + fieldName + " constant in class '" + clazz.getName() + '\'', e );
			}

			_createStatement = result;
		}

		return result;
	}

	@Override
	public boolean hasRecordId()
	{
		return ( _idField != null );
	}

	@NotNull
	@Override
	public String getRecordIdColumn()
	{
		final Field idField = _idField;
		if ( idField == null )
		{
			throw new IllegalArgumentException( _clazz.getName() + " class has no record ID" );
		}

		return idField.getName();
	}

	@Override
	public long getRecordId( @NotNull final Object object )
	{
		final Field idField = _idField;
		if ( idField == null )
		{
			throw new IllegalArgumentException( _clazz.getName() + " class has no record ID" );
		}

		try
		{
			return idField.getLong( object );
		}
		catch ( IllegalAccessException e )
		{
			throw new IllegalArgumentException( "Error getting " + idField.getName() + " field from " + object, e );
		}
	}

	@Override
	public void setRecordId( @NotNull final Object object, final long id )
	{
		final Field idField = _idField;
		if ( idField == null )
		{
			throw new IllegalArgumentException( _clazz.getName() + " class has no record ID" );
		}

		try
		{
			final Class<?> idType = idField.getType();
			if ( ( idType == int.class ) && ( (long)(int)id == id ) )
			{
				idField.setInt( object, (int)id );
			}
			else
			{
				idField.setLong( object, id );
			}
		}
		catch ( IllegalAccessException e )
		{
			throw new IllegalArgumentException( "Error getting " + idField.getName() + " field from " + object, e );
		}
	}

}
