/*
 * Copyright (c) 2011-2019, Numdata BV, The Netherlands.
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
import java.util.Date;
import java.util.*;
import java.util.concurrent.*;

import org.jetbrains.annotations.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * This class tests the {@link DbServices} class.
 *
 * @author Peter S. Heijnen
 */
public class TestDbServices
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestDbServices.class.getName();

	/**
	 * Database services to test.
	 */
	private DbServices _db = null;

	/**
	 * Another database services instance connected to the same data source.
	 */
	private DbServices _otherDb = null;

	@SuppressWarnings( "JavaDoc" )
	@Before
	public void setUp()
	throws Exception
	{

		final DbServices db;
		final DbServices otherDb;

		if ( true ) // HSQLDB
		{
			final HsqlDbServices hsql = new HsqlDbServices( CLASS_NAME, true );
			hsql.setDatabaseTransactionControl( HsqlDbServices.TransactionControl.MVCC );
			db = hsql;
			otherDb = new HsqlDbServices( CLASS_NAME, true );
		}
		else // MySQL
		{
			db = new DbServices( new JdbcDataSource( "com.mysql.jdbc.Driver", "jdbc:mysql://dbserver/test", "ivenza", "ivenza" ) );
			otherDb = new DbServices( new JdbcDataSource( "com.mysql.jdbc.Driver", "jdbc:mysql://dbserver/test", "ivenza", "ivenza" ) );
		}

		_db = db;
		_otherDb = otherDb;

		if ( !db.tableExists( SampleRecord.class ) )
		{
			db.createTable( SampleRecord.class );
		}
	}

	@SuppressWarnings( "JavaDoc" )
	@After
	public void tearDown()
	throws Exception
	{
		final DbServices db = _db;
		if ( db instanceof HsqlDbServices )
		{
			( (HsqlDbServices)db ).shutdown();
		}
	}

	/**
	 * Test database persistence using the {@link SampleRecord}.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testPersistence()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testPersistence" );

		final DbServices db = _db;

		/* Test INSERT */

		final SampleRecord firstRecord = new SampleRecord();
		firstRecord.string = "TestString";
		firstRecord.stringList.addAll( Arrays.asList( "one", "two", "three" ) );
		firstRecord.localizedString.set( "defaultString" );
		firstRecord.localizedString.set( "nl", "Nederlands" );
		db.storeObject( firstRecord );
		assertEquals( "First record has wrong id", 0, firstRecord.ID );

		/* Test SELECT single object  - compare with inserted record */

		final SelectQuery<SampleRecord> selectFirst = new SelectQuery<SampleRecord>( SampleRecord.class );
		selectFirst.whereEqual( "ID", firstRecord.ID );

		{
			final SampleRecord firstRead = db.retrieveObject( selectFirst );
			assertNotNull( "Did not find back record", firstRead );
			assertRecordEquals( "Data lost after read-back of first record", firstRecord, firstRead );
		}

		/* Test UPDATE */

		firstRecord.string = "UpdatedString";
		firstRecord.stringList.addAll( Arrays.asList( "een", "twee", "drie" ) );
		firstRecord.localizedString.set( "de", "Deutsch" );
		final int firstRecordID = firstRecord.ID;
		db.storeObject( firstRecord );
		assertEquals( "ID should not change after UPDATE", firstRecordID, firstRecord.ID );

		/* Test SELECT single object  - compare with updated record */

		{
			final SampleRecord secondRead = db.retrieveObject( selectFirst );
			assertNotNull( "Lost object after update", secondRead );
			assertRecordEquals( "Data was not updated in the database", firstRecord, secondRead );
		}

		/* Test second INSERT */

		final SampleRecord secondRecord = new SampleRecord();
		db.storeObject( secondRecord );
		assertTrue( "Second record has wrong id: " + secondRecord.ID, secondRecord.ID > firstRecord.ID );

		/* Test SELECT list */

		final SelectQuery<SampleRecord> selectList = new SelectQuery<SampleRecord>( SampleRecord.class );
		selectList.orderBy( "ID" );
		final List<SampleRecord> list = db.retrieveList( selectList );
		assertEquals( "Unexpected number of records", 2, list.size() );

		assertRecordEquals( "list[0] should be first recorD", firstRecord, list.get( 0 ) );
		assertRecordEquals( "list[1] should be second recorD", secondRecord, list.get( 1 ) );
	}

	/**
	 * Tests {@link DbServices#deleteObject}.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	@Test
	public void testDelete()
	throws SQLException
	{
		final String where = CLASS_NAME + ".testDelete()";
		System.out.println( where );

		final DbServices db = _db;

		final SampleRecord record1 = new SampleRecord();
		assertEquals( "Unexpected record ID before store.", -1, record1.ID );
		db.storeObject( record1 );
		assertTrue( "Unexpected record ID after store.", record1.ID >= 0 );

		final SampleRecord record2 = new SampleRecord();
		assertEquals( "Unexpected record ID before store.", -1, record2.ID );
		db.storeObject( record2 );
		assertTrue( "Unexpected record ID after store.", record2.ID >= 0 );

		final int record1Id = record1.ID;
		final int record2Id = record2.ID;

		db.deleteObject( record1 );
		assertEquals( "Unexpected record ID after delete.", -1, record1.ID );
		assertNull( "Failed to delete record.", selectById( db, record1Id ) );

		db.deleteObject( record2 );
		assertEquals( "Unexpected record ID after delete.", -1, record2.ID );
		assertNull( "Failed to delete record.", selectById( db, record2Id ) );
	}

	/**
	 * Tests transaction support.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	@Test
	public void testTransactions()
	throws SQLException
	{
		final String where = CLASS_NAME + ".testTransactions()";
		System.out.println( where );

		final DbServices db = _db;
		final DbServices otherDb = _otherDb;

		final SampleRecord record1 = new SampleRecord();
		final SampleRecord record2 = new SampleRecord();

		System.out.println( " - Store record 1" );
		db.storeObject( record1 );
		final SampleRecord otherRecord1 = selectById( otherDb, record1.ID );
		assertNotNull( "Record 1 should have been committed.", otherRecord1 );

		System.out.println( " - Store record 2" );
		db.storeObject( record2 );
		final SampleRecord otherRecord2 = selectById( otherDb, record2.ID );
		assertNotNull( "Record 2 should have been committed.", otherRecord2 );

		System.out.println( " - Start transaction" );
		db.startTransaction( Connection.TRANSACTION_SERIALIZABLE );
		try
		{
			System.out.println( " - Update record 1" );
			record1.string = "Hello";
			db.storeObject( record1 );

			System.out.println( " - Check records from other data source" );
			otherDb.refresh( otherRecord1 );
			otherDb.refresh( otherRecord2 );
			assertNull( "Update of record 1 should not have been committed.", otherRecord1.string );
			assertNull( "Update of record 2 should not have been committed.", otherRecord2.string );

			System.out.println( " - Update record 2" );
			record2.string = "Commit";
			db.storeObject( record2 );

			System.out.println( " - Check records from other data source" );
			otherDb.refresh( otherRecord1 );
			otherDb.refresh( otherRecord2 );
			assertNull( "Update of record 1 should not have been committed.", otherRecord1.string );
			assertNull( "Update of record 2 should not have been committed.", otherRecord2.string );
		}
		finally
		{
			System.out.println( " - Commit" );
			db.commit();
		}

		System.out.println( " - Check records from other data source" );
		otherDb.refresh( otherRecord1 );
		otherDb.refresh( otherRecord2 );
		assertEquals( "Update of record 1 should have been committed.", "Hello", otherRecord1.string );
		assertEquals( "Update of record 2 should have been committed.", "Commit", otherRecord2.string );

		System.out.println( " - Check records from own data source" );
		db.refresh( record1 );
		db.refresh( record2 );
		assertEquals( "Update of record 1 should have been committed.", "Hello", record1.string );
		assertEquals( "Update of record 2 should have been committed.", "Commit", record2.string );

		System.out.println( " - Start transaction" );
		db.startTransaction( Connection.TRANSACTION_SERIALIZABLE );
		try
		{
			System.out.println( " - Update record 1" );
			record1.string = "Bye";
			db.storeObject( record1 );

			System.out.println( " - Check records from other data source" );
			otherDb.refresh( otherRecord1 );
			otherDb.refresh( otherRecord2 );
			assertEquals( "Update of record 1 should not have been committed.", "Hello", otherRecord1.string );
			assertEquals( "Update of record 2 should not have been committed.", "Commit", otherRecord2.string );

			System.out.println( " - Check records from own data source" );
			db.refresh( record1 );
			db.refresh( record2 );
			assertEquals( "Update of record 1 should have been committed.", "Bye", record1.string );
			assertEquals( "Update of record 2 should have been committed.", "Commit", record2.string );

			System.out.println( " - Update record 2" );
			record2.string = "Rollback";
			db.storeObject( record2 );

			System.out.println( " - Check records from other data source" );
			otherDb.refresh( otherRecord1 );
			otherDb.refresh( otherRecord2 );
			assertEquals( "Update of record 1 should not have been committed.", "Hello", otherRecord1.string );
			assertEquals( "Update of record 2 should not have been committed.", "Commit", otherRecord2.string );

			System.out.println( " - Check records from own data source" );
			db.refresh( record1 );
			db.refresh( record2 );
			assertEquals( "Update of record 1 should have been committed.", "Bye", record1.string );
			assertEquals( "Update of record 2 should have been committed.", "Rollback", record2.string );
		}
		finally
		{
			System.out.println( " - Rollback" );
			db.rollback();
		}

		System.out.println( " - Check records from other data source" );
		otherDb.refresh( otherRecord1 );
		otherDb.refresh( otherRecord2 );
		assertEquals( "Update of record 1 should have been committed.", "Hello", otherRecord1.string );
		assertEquals( "Update of record 2 should have been committed.", "Commit", otherRecord2.string );

		System.out.println( " - Check records from own data source" );
		db.refresh( record1 );
		db.refresh( record2 );
		assertEquals( "Update of record 1 should have been committed.", "Hello", record1.string );
		assertEquals( "Update of record 2 should have been committed.", "Commit", record2.string );
	}

	/**
	 * Selects the {@link SampleRecord} with the given id.
	 *
	 * @param db Database services.
	 * @param id Record id.
	 *
	 * @return Found sample record.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	@Nullable
	private SampleRecord selectById( @NotNull final DbServices db, final int id )
	throws SQLException
	{
		final SelectQuery<SampleRecord> select = new SelectQuery<SampleRecord>( SampleRecord.class );
		select.whereEqual( SampleRecord.RECORD_ID, id );
		return db.retrieveObject( select );
	}

	/**
	 * Asserts that the given sample records are equal.
	 *
	 * @param message  Failure message prefix.
	 * @param expected Expected value.
	 * @param actual   Actual value.
	 */
	private static void assertRecordEquals( @NotNull final String message, @NotNull final SampleRecord expected, @NotNull final SampleRecord actual )
	{
		assertEquals( message + ": string", expected.string, actual.string );
		assertNotSame( message + ": timestamp", expected.timestamp, actual.timestamp );
		assertDateEquals( message + ": date", expected.date, actual.date );
		assertEquals( message + ": localizedString", expected.localizedString, actual.localizedString );
		assertEquals( message + ": stringList", expected.stringList, actual.stringList );
		assertEquals( message + ": nestedProperties", expected.nestedProperties, actual.nestedProperties );
		assertEquals( message + ": nullableNestedProperties", expected.nullableNestedProperties, actual.nullableNestedProperties );
	}

	/**
	 * Asserts that the given dates are (almost) equal.
	 *
	 * @param message  Failure message.
	 * @param expected Expected value.
	 * @param actual   Actual value.
	 */
	private static void assertDateEquals( @Nullable final String message, @Nullable final Date expected, @Nullable final Date actual )
	{
		if ( ( expected == null ) || ( actual == null ) || ( ( actual.getTime() - expected.getTime() ) > 1000L ) )
		{
			assertEquals( message, expected, actual );
		}
	}

	/**
	 * Tests automatic transaction retry behavior by creating a deadlock
	 * situation. To create a deadlock, two threads concurrently modify the same
	 * two records in opposite order.
	 *
	 * @throws Exception if the test fails unexpectedly.
	 */
	@Test
	public void testTransactionDeadlock()
	throws Exception
	{
		final String where = CLASS_NAME + ".testTransactionDeadlock()";
		System.out.println( where );

		final DbServices db = _db;

		final SampleRecord record1 = new SampleRecord();
		final SampleRecord record2 = new SampleRecord();
		db.storeObject( record1 );
		db.storeObject( record2 );

		final ExecutorService executorService = Executors.newFixedThreadPool( 2 );

		final Future<Void> future1 = executorService.submit( new Callable<Void>()
		{
			@Override
			public Void call()
			throws Exception
			{
				final TransactionBody transactionBody = new TransactionBody()
				{
					@Override
					public void execute()
					throws SQLException
					{
						System.out.println( " - Transaction 1: update record 1" );
						final SampleRecord update1 = new SampleRecord();
						update1.ID = record1.ID;
						update1.string = "First";
						db.storeObject( update1 );

						try
						{
							Thread.sleep( 50L );
						}
						catch ( final InterruptedException e )
						{
							throw new SQLException( "Transaction 1 interrupted", e );
						}

						System.out.println( " - Transaction 1: update record 2" );
						final SampleRecord update2 = new SampleRecord();
						update2.ID = record2.ID;
						update2.string = "First";
						db.storeObject( update2 );

						db.refresh( update1 );
						db.refresh( update2 );

						assertEquals( "Unexpected value.", "First", update1.string );
						assertEquals( "Unexpected value.", "First", update2.string );
					}
				};

				for ( int i = 0; i < 10; i++ )
				{
					db.transactionWithRetry( transactionBody );
				}

				return null;
			}
		} );

		final Future<Void> future2 = executorService.submit( new Callable<Void>()
		{
			@Override
			public Void call()
			throws Exception
			{
				final TransactionBody transactionBody = new TransactionBody()
				{
					@Override
					public void execute()
					throws SQLException
					{
						System.out.println( " - Transaction 2: update record 2" );
						final SampleRecord update2 = new SampleRecord();
						update2.ID = record2.ID;
						update2.string = "Second";
						db.storeObject( update2 );

						try
						{
							Thread.sleep( 50L );
						}
						catch ( final InterruptedException e )
						{
							throw new SQLException( "Transaction 2 interrupted", e );
						}

						System.out.println( " - Transaction 2: update record 1" );
						final SampleRecord update1 = new SampleRecord();
						update1.ID = record1.ID;
						update1.string = "Second";
						db.storeObject( update1 );

						assertEquals( "Unexpected value.", "Second", update1.string );
						assertEquals( "Unexpected value.", "Second", update2.string );
					}
				};

				for ( int i = 0; i < 10; i++ )
				{
					db.transactionWithRetry( transactionBody );
				}

				return null;
			}
		} );

		executorService.shutdown();

		final long start = System.nanoTime();

		future1.get();
		future2.get();

		final long end = System.nanoTime();
		System.out.println( " - Test completed in " + (double)( ( end - start ) / 1000000L ) / 1000.0 + " s" );

		final int record1Id = record1.ID;
		db.deleteObject( record1 );
		assertEquals( "Failed to delete record 1", -1, record1.ID );
		assertNull( "Failed to delete record 1", selectById( db, record1Id ) );

		final int record2Id = record2.ID;
		db.deleteObject( record2 );
		assertEquals( "Failed to delete record 2", -1, record2.ID );
		assertNull( "Failed to delete record 2", selectById( db, record2Id ) );
	}
}
