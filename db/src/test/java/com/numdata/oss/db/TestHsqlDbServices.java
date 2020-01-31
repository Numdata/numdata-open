/*
 * Copyright (c) 2008-2020, Numdata BV, The Netherlands.
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

import java.io.*;
import java.sql.*;
import java.util.Date;
import java.util.*;
import javax.sql.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class implements a unit test for the HSQL database services class.
 *
 * @author H.B.J. te Lintelo
 */
public class TestHsqlDbServices
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestHsqlDbServices.class.getName();

	/**
	 * Class DbObject for use in MemoryDbServices tests.
	 */
	@SuppressWarnings( { "PublicField", "InstanceVariableNamingConvention" } )
	public static class DbObject
	implements Serializable
	{
		/**
		 * Virtual database table name.
		 */
		public static final String TABLE_NAME = "DbObjects";

		/**
		 * SQL create statement (for MySQL).
		 */
		public static final String MYSQL_CREATE_STATEMENT = "CREATE TABLE `DbObjects` (\n" +
		                                                    "  `ID` int(11) NOT NULL auto_increment,\n" +
		                                                    "  `intField` int(11) NOT NULL,\n" +
		                                                    "  `doubleField` double NOT NULL,\n" +
		                                                    "  `floatField` float NOT NULL,\n" +
		                                                    "  `stringField` varchar(10) default NULL,\n" +
		                                                    "  `dateField` datetime default NULL,\n" +
		                                                    "  `booleanField` tinyint(1) NOT NULL,\n" +
		                                                    "  `byteField` tinyint NOT NULL,\n" +
		                                                    "  `shortField` smallint NOT NULL,\n" +
		                                                    "  `longField` bigint NOT NULL,\n" +
		                                                    "  `byteArrayField` tinyblob default NULL,\n" +
		                                                    "  PRIMARY KEY  (`ID`)\n" +
		                                                    ");";

		/** Unique ID of database record. */
		@SuppressWarnings( "NonConstantFieldWithUpperCaseName" )
		public int ID = -1;

		/** Data int field. */
		public int intField = 1;

		/** Data double field. */
		public double doubleField = 2.0;

		/** Data float field. */
		public float floatField = 3.0f;

		/** Data String field. */
		public String stringField = "4";

		/** Data Date field. */
		public Date dateField = DbServices.NOW;

		/** Data boolean field. */
		public boolean booleanField = true;

		/** Data byte field. */
		public byte byteField = (byte)7;

		/** Data short field. */
		public short shortField = (short)8;

		/** Data long field. */
		public long longField = 9L;

		/** Data byte array field. */
		public byte[] byteArrayField = {};
	}

	/**
	 * Test storage capability with hsqldb as jdbc driver.
	 *
	 * @throws Exception if error occurred.
	 */
	@Test
	public void testBasicSanity()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testBasicSanity()" );

		final HsqlDbServices db = new HsqlDbServices();
		db.createTable( DbObject.class );

		final DbObject o = new DbObject();
		db.storeObject( o );
		assertTrue( "Did not set ID to >= 0 in store method", o.ID >= 0 );

		final SelectQuery<DbObject> query = new SelectQuery<DbObject>( DbObject.class );
		final List<DbObject> dbObjectList = db.retrieveList( query );
		assertEquals( "Invalid result set length", 1, dbObjectList.size() );

		final DbObject received = dbObjectList.get( 0 );
		assertTrue( "ID field not updated", received.ID >= 0 );
		assertEquals( "Retrieved object data corrupt", o.intField, received.intField );
		assertEquals( "Retrieved object data corrupt", o.doubleField, received.doubleField, 0.001 );
		assertEquals( "Retrieved object data corrupt", o.floatField, received.floatField, 0.001f );
		assertEquals( "Retrieved object data corrupt", o.byteField, received.byteField );
		assertEquals( "Retrieved object data corrupt", o.shortField, received.shortField );
		assertEquals( "Retrieved object data corrupt", o.longField, received.longField );
	}

	/**
	 * Test {@code boolean} fields.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testBooleanField()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testBooleanField()" );

		final HsqlDbServices db = new HsqlDbServices();
		db.createTable( DbObject.class );

		for ( int i = 0; i < 2; i++ )
		{
			final boolean expected = ( i != 0 );

			final DbObject in = new DbObject();
			in.booleanField = expected;
			db.storeObject( in );

			final SelectQuery<DbObject> query = new SelectQuery<DbObject>( DbObject.class );
			query.whereEqual( "ID", in.ID );
			final DbObject out = db.retrieveObject( query );

			final boolean received = out.booleanField;
			assertEquals( "input/output mismatch for test #" + i, expected, received );
		}
	}

	/**
	 * Test {@code byte[]} fields.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testByteArrayField()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testByteArrayField()" );

		final byte[][] tests =
		{
		null,
		{},
		{ (byte)1 },
		{ (byte)1, (byte)2 },
		{ (byte)1, (byte)2, (byte)3 },
		};

		final HsqlDbServices db = new HsqlDbServices();
		db.createTable( DbObject.class );

		for ( int i = 0; i < tests.length; i++ )
		{
			final byte[] expected = tests[ i ];

			final DbObject in = new DbObject();
			in.byteArrayField = expected;
			db.storeObject( in );

			final SelectQuery<DbObject> query = new SelectQuery<DbObject>( DbObject.class );
			query.whereEqual( "ID", in.ID );
			final DbObject out = db.retrieveObject( query );

			final byte[] received = out.byteArrayField;
			if ( expected == null )
			{
				assertNull( "input/output mismatch for test #" + i, received );
			}
			else
			{
				assertNotNull( "input/output mismatch for test #" + i, received );
				assertEquals( "input/output mismatch for test #" + i + ".length", expected.length, received.length );
				for ( int r = 0; r < received.length; r++ )
				{
					assertEquals( "input/output mismatch for test #" + i + '[' + r + ']', expected[ r ], received[ r ] );
				}
			}
		}
	}

	/**
	 * Test {@code Date} fields.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testDateField()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testDateField()" );

		final Date[] tests =
		{
		null,
		new Date(),
		new Date( -1L ),
		DbServices.NOW
		};

		final HsqlDbServices db = new HsqlDbServices();
		db.createTable( DbObject.class );

		for ( int i = 0; i < tests.length; i++ )
		{
			final Date expected = tests[ i ];

			final DbObject in = new DbObject();
			in.dateField = expected;
			db.storeObject( in );

			final SelectQuery<DbObject> query = new SelectQuery<DbObject>( DbObject.class );
			query.whereEqual( "ID", in.ID );
			final DbObject out = db.retrieveObject( query );

			final Date received = out.dateField;

			if ( DbServices.NOW.equals( expected ) )
			{
				assertNotNull( "NOW() resulted in NULL (test #" + i + ')', received );
				assertTrue( "NOW() resulted incorrect timestamp " + received.getTime() + " (test #" + i + ')', received.getTime() > 0L );
			}
			else
			{
				assertEquals( "input/output mismatch for test #" + i, expected, received );
			}
		}
	}

	/**
	 * Test {@link JdbcTools#executeQuery} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testExecuteQuery()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testExecuteQuery()" );

		final HsqlDbServices db = new HsqlDbServices();
		final DataSource dataSource = db.getDataSource();

		db.createTable( DbObject.class );

		for ( int nrRows = 0; nrRows < 3; nrRows++ )
		{
			final boolean isEmpty = ( nrRows == 0 );
			final int last = nrRows - 1;
			final String what = "#" + nrRows + " - ";

			if ( !isEmpty )
			{
				db.storeObject( new DbObject() );
			}

			final ResultSetClone clone = JdbcTools.executeQuery( dataSource, "SELECT * FROM " + DbObject.TABLE_NAME );
			assertNotNull( what + "Raw query failed", clone );

			final ResultSetMetaData meta = clone.getMetaData();
			assertNotNull( what + "Clone result did not contain meta-data", meta );

			assertEquals( what + "Clone has wrong column count", DbObject.class.getFields().length - 2, meta.getColumnCount() );
			assertEquals( what + "Clone has wrong fetch size", nrRows, clone.getFetchSize() );
			assertEquals( what + "Clone has wrong row count", nrRows, clone.getData().length );

			for ( int pos = -1; pos <= nrRows; pos++ )
			{
				assertEquals( what + '(' + pos + ") isBeforeFirst()", ( pos < 0 ), clone.isBeforeFirst() );
				assertEquals( what + '(' + pos + ") isFirst()", !isEmpty && ( pos == 0 ), clone.isFirst() );
				assertEquals( what + '(' + pos + ") isLast()", !isEmpty && ( pos == last ), clone.isLast() );
				assertEquals( what + '(' + pos + ") isAfterLast()", ( pos > last ), clone.isAfterLast() );
				assertEquals( what + '(' + pos + ") next()", ( pos < last ), clone.next() );
			}
		}
	}

	/**
	 * Test {@code String} fields.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testStringField()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testStringField()" );

		final String[] tests =
		{
		null,
		"",
		"a",
		"ab",
		"ab" + 'c',
		};

		final HsqlDbServices db = new HsqlDbServices();
		db.createTable( DbObject.class );

		for ( int i = 0; i < tests.length; i++ )
		{
			final String expected = tests[ i ];

			final DbObject in = new DbObject();
			in.stringField = expected;
			db.storeObject( in );

			final SelectQuery<DbObject> query = new SelectQuery<DbObject>( DbObject.class );
			query.whereEqual( "ID", in.ID );
			final DbObject out = db.retrieveObject( query );

			final String received = out.stringField;

			if ( "null".equals( received ) )
			{
				fail( "received the string 'null' (not null), should never happen" );
			}

			assertEquals( "input/output mismatch for test #" + i, expected, received );
		}
	}

	/**
	 * Tests the implementation of {@link DbServices#tableExists} for HSQLDB.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testTableExists()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testTableExists()" );

		final HsqlDbServices db = new HsqlDbServices();
		final DataSource dataSource = db.getDataSource();

		assertFalse( "Table doesn't exist yet.", JdbcTools.tableExists( dataSource, DbObject.TABLE_NAME ) );

		db.createTable( DbObject.class );
		assertTrue( "Table does exist!", JdbcTools.tableExists( dataSource, DbObject.TABLE_NAME ) );

		db.storeObject( new DbObject() );
		assertTrue( "Table does exist!", JdbcTools.tableExists( dataSource, DbObject.TABLE_NAME ) );

		db.dropTable( DbObject.class );
		assertFalse( "Table doesn't exist anymore.", JdbcTools.tableExists( dataSource, DbObject.TABLE_NAME ) );
	}

	/**
	 * Tests the implementation of {@link JdbcTools#columnExists} for HSQLDB.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testColumnExists()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testColumnExists()" );

		final HsqlDbServices db = new HsqlDbServices();
		final DataSource dataSource = db.getDataSource();

		assertFalse( "Table doesn't exist yet.", JdbcTools.columnExists( dataSource, DbObject.TABLE_NAME, "intField" ) );
		assertFalse( "Table doesn't exist yet.", JdbcTools.columnExists( dataSource, DbObject.TABLE_NAME, "nonsense" ) );

		db.createTable( DbObject.class );
		assertTrue( "Empty table exists", JdbcTools.columnExists( dataSource, DbObject.TABLE_NAME, "intField" ) );
		assertFalse( "Empty table exists", JdbcTools.columnExists( dataSource, DbObject.TABLE_NAME, "unknown" ) );

		db.storeObject( new DbObject() );
		assertTrue( "Record exists", JdbcTools.columnExists( dataSource, DbObject.TABLE_NAME, "intField" ) );
		assertFalse( "Record exists", JdbcTools.columnExists( dataSource, DbObject.TABLE_NAME, "unknown" ) );

		db.dropTable( DbObject.class );
		assertFalse( "Table doesn't exist anymore.", JdbcTools.columnExists( dataSource, DbObject.TABLE_NAME, "intField" ) );
		assertFalse( "Table doesn't exist anymore.", JdbcTools.columnExists( dataSource, DbObject.TABLE_NAME, "nonsense" ) );
	}
}
