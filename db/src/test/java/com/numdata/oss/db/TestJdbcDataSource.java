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

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link JdbcDataSource}.
 *
 * @author Gerrit Meinders
 */
public class TestJdbcDataSource
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestJdbcDataSource.class.getName();

	/**
	 * Data source to test.
	 */
	private JdbcDataSource _dataSource;

	@Before
	public void setUp()
	throws Exception
	{
		_dataSource = new JdbcDataSource( "org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:" + CLASS_NAME + ";shutdown=true", "sa", "" );
	}

	/**
	 * Tests that the read-only property of a connection is reset when the
	 * connection is returned to the pool and then borrowed again.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadOnly()
	throws Exception
	{
		final String where = CLASS_NAME + ".testReadOnly()";
		System.out.println( where );

		final JdbcDataSource dataSource = _dataSource;

		final Connection connection = dataSource.getConnection();
		assertFalse( "Connection should not be read-only.", connection.isReadOnly() );
		connection.setReadOnly( true );
		assertTrue( "Connection should be read-only.", connection.isReadOnly() );
		connection.close();

		final Connection connection2 = dataSource.getConnection();
		assertFalse( "Connection should not be read-only.", connection2.isReadOnly() );
		connection2.close();
	}
}
