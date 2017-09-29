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

import java.sql.*;

import org.jetbrains.annotations.*;

/**
 * This interface describes how to handle a Java field in relation to the
 * database.
 *
 * @author  Peter S. Heijnen
 */
public interface FieldHandler
{
	/**
	 * Get name of field.
	 *
	 * @return  Name of field.
	 */
	@NotNull
	String getName();

	/**
	 * Get Java field type.
	 *
	 * @return  Field type.
	 */
	@NotNull
	Class<?> getJavaType();

	/**
	 * Get database field type.
	 *
	 * @return  Field type.
	 */
	@NotNull
	Class<?> getSqlType();

	/**
	 * Get column data from a {@link ResultSet} into the given record object.
	 *
	 * @param   object          Record object.
	 * @param   resultSet       {@link ResultSet}.
	 * @param   columnIndex     Index of column in {@link ResultSet}.
	 *
	 * @throws  SQLException if an error occurs while accessing the database.
	 */
	void getColumnData( @NotNull Object object, @NotNull ResultSet resultSet, int columnIndex )
		throws SQLException;

	/**
	 * Set column data from the given record object into a {@link PreparedStatement}.
	 *
	 * @param   object              Record object.
	 * @param   preparedStatement   {@link PreparedStatement}.
	 * @param   columnIndex         Index of field in {@link PreparedStatement}.
	 *
	 * @throws  SQLException if an error occurs while accessing the database.
	 */
	void setColumnData( @NotNull Object object, @NotNull PreparedStatement preparedStatement, int columnIndex )
		throws SQLException;

	/**
	 * Get value of field from the given record object.
	 *
	 * @param   object  Record object.
	 *
	 * @return  Field value.
	 */
	Object getFieldValue( @NotNull Object object );
}
