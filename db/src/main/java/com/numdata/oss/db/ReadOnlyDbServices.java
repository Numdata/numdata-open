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
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class wraps a {@link DbServices} object and makes it read-only.
 *
 * @author G.B.M. Rupert
 * @TODO Create a 'DbServices' interface and implement this wrapper the proper way!!
 */
public class ReadOnlyDbServices
extends DbServices
{
	/**
	 * Construct new {@link ReadOnlyDbServices}.
	 *
	 * @param target {@link DbServices} to wrap.
	 */
	public ReadOnlyDbServices( @NotNull final DbServices target )
	{
		super( target.getDataSource() );
		setSqlDialect( target.getSqlDialect() );
	}

	@Override
	public void createTable( @NotNull final Class<?> tableClass )
	throws SQLException
	{
		throw new AssertionError( "NOT ALLOWED IN READ-ONLY MODE" );
	}

	@Override
	public void dropTable( @NotNull final Class<?> tableClass )
	throws SQLException
	{
		throw new AssertionError( "NOT ALLOWED IN READ-ONLY MODE" );
	}

	@Override
	public int executeDelete( @NotNull final DeleteQuery<?> deleteQuery )
	throws SQLException
	{
		throw new AssertionError( "NOT ALLOWED IN READ-ONLY MODE" );
	}

	@Override
	public int executeUpdate( @NotNull final UpdateQuery<?> updateQuery )
	throws SQLException
	{
		throw new AssertionError( "NOT ALLOWED IN READ-ONLY MODE" );
	}

	@Override
	public void storeObject( @NotNull final Object object )
	throws SQLException
	{
		throw new AssertionError( "NOT ALLOWED IN READ-ONLY MODE" );
	}

	@Override
	protected void updateObjectImpl( @NotNull final Object object, @NotNull final List<FieldHandler> fields )
	throws SQLException
	{
		throw new AssertionError( "NOT ALLOWED IN READ-ONLY MODE" );
	}

	@Override
	protected void insertObjectImpl( @NotNull final Object object )
	throws SQLException
	{
		throw new AssertionError( "NOT ALLOWED IN READ-ONLY MODE" );
	}

	@Override
	public void startTransaction()
	throws SQLException
	{
		super.startTransaction();
		final Connection transactionConnection = acquireConnection( true );
		transactionConnection.setReadOnly( true );
	}
}
