/*
 * Copyright (c) 2011-2017, Numdata BV, The Netherlands.
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

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This interface describes how to handle a Java class in relation to the
 * database.
 *
 * @author Peter S. Heijnen
 */
public abstract class ClassHandler
{
	/**
	 * Java class being handled.
	 */
	protected final Class<?> _clazz;

	/**
	 * Case-insensitive map from column/field name to field handler.
	 */
	private final Map<String, FieldHandler> _fieldByName = new HashMap<String, FieldHandler>();

	/**
	 * List of all handled fields for this class.
	 */
	private final List<FieldHandler> _fields = new ArrayList<FieldHandler>();

	/**
	 * Create class handler.
	 *
	 * @param clazz Class to handle.
	 */
	protected ClassHandler( @NotNull final Class<?> clazz )
	{
		_clazz = clazz;
	}

	/**
	 * Add field handler.
	 *
	 * @param field Field handler to add.
	 */
	protected void addField( @NotNull final FieldHandler field )
	{
		_fieldByName.put( field.getName().toLowerCase(), field );
		_fields.add( field );
	}

	/**
	 * Get persistent fields of the class.
	 *
	 * @return List of persistent fields.
	 */
	@NotNull
	public List<FieldHandler> getFieldHandlers()
	{
		//noinspection ReturnOfCollectionOrArrayField
		return _fields;
	}

	/**
	 * Get field handler for the specified table column.
	 *
	 * @param name Column name.
	 *
	 * @return Field handler for column; {@code null} if no handler was found
	 * for the given column.
	 */
	@Nullable
	public FieldHandler getFieldHandlerForColumn( @NotNull final String name )
	{
		return _fieldByName.get( name.toLowerCase() );
	}

	/**
	 * Get name of SQL table to contain records of this class.
	 *
	 * @return Table name.
	 */
	@NotNull
	public abstract String getTableName();

	/**
	 * Get SQL statement that can be used to create the SQL table to contain
	 * records of this class.
	 *
	 * @return SQL statement.
	 */
	@NotNull
	public abstract String getCreateStatement();

	/**
	 * Test whether this class has a record id.
	 *
	 * @return {@code true} if this table has a record id.
	 */
	public abstract boolean hasRecordId();

	/**
	 * Get name of column that holds the record id. This may only be called if
	 * the {@link #hasRecordId} method returns {@code true}.
	 *
	 * @return Name of the column that holds the record id.
	 */
	@NotNull
	public abstract String getRecordIdColumn();

	/**
	 * Get record id of the specified record. This may only be called if the
	 * {@link #hasRecordId} method returns {@code true}.
	 *
	 * @param object Record object.
	 *
	 * @return Record id.
	 */
	public abstract long getRecordId( @NotNull Object object );

	/**
	 * Set record id of the specified record. This may only be called if the
	 * {@link #hasRecordId} method returns {@code true}.
	 *
	 * @param object Record object.
	 * @param id     Record id to set.
	 */
	public abstract void setRecordId( @NotNull Object object, long id );
}
