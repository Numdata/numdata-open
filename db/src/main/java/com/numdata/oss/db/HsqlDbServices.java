/*
 * Copyright (c) 2008-2021, Unicon Creation BV, The Netherlands.
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
import java.util.concurrent.atomic.*;
import javax.sql.*;

import org.jetbrains.annotations.*;

/**
 * In-memory implementation of {@link DbServices} for testing/stand-alone
 * application use based on HSQLDB
 * (see <a href='http://www.hsqldb.org/'>http://www.hsqldb.org/</a>). Each new
 * instance is backed by a newly created database.
 *
 * @author Peter S. Heijnen
 */
public class HsqlDbServices
extends DbServices
{
	/**
	 * Keeps track of the ID that is used to uniquely identify the database
	 * backing the next instance of this class.
	 */
	private static final AtomicInteger DATABASE_ID = new AtomicInteger( 0 );

	/**
	 * Construct in-memory database.
	 */
	public HsqlDbServices()
	{
		this( String.valueOf( DATABASE_ID.getAndIncrement() ), true );
	}

	/**
	 * Construct in-memory database.
	 *
	 * @param databaseName Database name.
	 * @param shutdown     Shutdown database after last client disconnects.
	 */
	public HsqlDbServices( @NotNull final String databaseName, final boolean shutdown )
	{
		super( createDataSource( databaseName, shutdown ), SqlDialect.HSQLDB );

	}

	/**
	 * Get JDBC memory database services (used for test purposes).
	 *
	 * @param databaseName Database name.
	 * @param shutdown     Shutdown database after last client disconnects.
	 *
	 * @return Database services (in memory) for JDBC database.
	 */
	private static DataSource createDataSource( @NotNull final String databaseName, final boolean shutdown )
	{
		try
		{
			return new JdbcDataSource( "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:" + databaseName + ";shutdown=" + shutdown, "sa", "" );
		}
		catch ( final SQLException e )
		{
			throw new RuntimeException( "Cannot create JDBC connection: " + e.getMessage(), e );
		}
	}

}
