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
import java.util.Date;
import java.util.concurrent.*;

import org.jetbrains.annotations.*;
import org.junit.*;

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
		Assert.assertEquals( "First record has wrong id", 0, firstRecord.ID );

		/* Test SELECT single object  - compare with inserted record */

		final SelectQuery<SampleRecord> selectFirst = new SelectQuery<SampleRecord>( SampleRecord.class );
		selectFirst.whereEqual( "ID", firstRecord.ID );

		{
			final SampleRecord firstRead = db.retrieveObject( selectFirst );
			Assert.assertNotNull( "Did not find back record", firstRead );
			assertEquals( "Data lost after read-back of first record", firstRecord, firstRead );
		}

		/* Test UPDATE */

		firstRecord.string = "UpdatedString";
		firstRecord.stringList.addAll( Arrays.asList( "een", "twee", "drie" ) );
		firstRecord.localizedString.set( "de", "Deutsch" );
		final int firstRecordID = firstRecord.ID;
		db.storeObject( firstRecord );
		Assert.assertEquals( "ID should not change after UPDATE", firstRecordID, firstRecord.ID );

		/* Test SELECT single object  - compare with updated record */

		{
			final SampleRecord secondRead = db.retrieveObject( selectFirst );
			Assert.assertNotNull( "Lost object after update", secondRead );
			assertEquals( "Data was not updated in the database", firstRecord, secondRead );
		}

		/* Test second INSERT */

		final SampleRecord secondRecord = new SampleRecord();
		db.storeObject( secondRecord );
		Assert.assertTrue( "Second record has wrong id: " + secondRecord.ID, secondRecord.ID > firstRecord.ID );

		/* Test SELECT list */

		final SelectQuery<SampleRecord> selectList = new SelectQuery<SampleRecord>( SampleRecord.class );
		selectList.orderBy( "ID" );
		final List<SampleRecord> list = db.retrieveList( selectList );
		Assert.assertEquals( "Unexpected number of records", 2, list.size() );

		assertEquals( "list[0] should be first recorD", firstRecord, list.get( 0 ) );
		assertEquals( "list[1] should be second recorD", secondRecord, list.get( 1 ) );
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
		Assert.assertNotNull( "Record 1 should have been committed.", otherRecord1 );

		System.out.println( " - Store record 2" );
		db.storeObject( record2 );
		final SampleRecord otherRecord2 = selectById( otherDb, record2.ID );
		Assert.assertNotNull( "Record 2 should have been committed.", otherRecord2 );

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
			Assert.assertNull( "Update of record 1 should not have been committed.", otherRecord1.string );
			Assert.assertNull( "Update of record 2 should not have been committed.", otherRecord2.string );

			System.out.println( " - Update record 2" );
			record2.string = "Commit";
			db.storeObject( record2 );

			System.out.println( " - Check records from other data source" );
			otherDb.refresh( otherRecord1 );
			otherDb.refresh( otherRecord2 );
			Assert.assertNull( "Update of record 1 should not have been committed.", otherRecord1.string );
			Assert.assertNull( "Update of record 2 should not have been committed.", otherRecord2.string );
		}
		finally
		{
			System.out.println( " - Commit" );
			db.commit();
		}

		System.out.println( " - Check records from other data source" );
		otherDb.refresh( otherRecord1 );
		otherDb.refresh( otherRecord2 );
		Assert.assertEquals( "Update of record 1 should have been committed.", "Hello", otherRecord1.string );
		Assert.assertEquals( "Update of record 2 should have been committed.", "Commit", otherRecord2.string );

		System.out.println( " - Check records from own data source" );
		db.refresh( record1 );
		db.refresh( record2 );
		Assert.assertEquals( "Update of record 1 should have been committed.", "Hello", record1.string );
		Assert.assertEquals( "Update of record 2 should have been committed.", "Commit", record2.string );

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
			Assert.assertEquals( "Update of record 1 should not have been committed.", "Hello", otherRecord1.string );
			Assert.assertEquals( "Update of record 2 should not have been committed.", "Commit", otherRecord2.string );

			System.out.println( " - Check records from own data source" );
			db.refresh( record1 );
			db.refresh( record2 );
			Assert.assertEquals( "Update of record 1 should have been committed.", "Bye", record1.string );
			Assert.assertEquals( "Update of record 2 should have been committed.", "Commit", record2.string );

			System.out.println( " - Update record 2" );
			record2.string = "Rollback";
			db.storeObject( record2 );

			System.out.println( " - Check records from other data source" );
			otherDb.refresh( otherRecord1 );
			otherDb.refresh( otherRecord2 );
			Assert.assertEquals( "Update of record 1 should not have been committed.", "Hello", otherRecord1.string );
			Assert.assertEquals( "Update of record 2 should not have been committed.", "Commit", otherRecord2.string );

			System.out.println( " - Check records from own data source" );
			db.refresh( record1 );
			db.refresh( record2 );
			Assert.assertEquals( "Update of record 1 should have been committed.", "Bye", record1.string );
			Assert.assertEquals( "Update of record 2 should have been committed.", "Rollback", record2.string );
		}
		finally
		{
			System.out.println( " - Rollback" );
			db.rollback();
		}

		System.out.println( " - Check records from other data source" );
		otherDb.refresh( otherRecord1 );
		otherDb.refresh( otherRecord2 );
		Assert.assertEquals( "Update of record 1 should have been committed.", "Hello", otherRecord1.string );
		Assert.assertEquals( "Update of record 2 should have been committed.", "Commit", otherRecord2.string );

		System.out.println( " - Check records from own data source" );
		db.refresh( record1 );
		db.refresh( record2 );
		Assert.assertEquals( "Update of record 1 should have been committed.", "Hello", record1.string );
		Assert.assertEquals( "Update of record 2 should have been committed.", "Commit", record2.string );
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
	 * Deletes the {@link SampleRecord} with the given id.
	 *
	 * @param db Database services.
	 * @param id Record id.
	 *
	 * @return Number of rows that were deleted.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	private static int deleteById( @NotNull final DbServices db, final int id )
	throws SQLException
	{
		final DeleteQuery<SampleRecord> deleteQuery = new DeleteQuery<SampleRecord>( SampleRecord.class );
		deleteQuery.whereEqual( SampleRecord.RECORD_ID, id );
		return db.executeDelete( deleteQuery );
	}

	private static void assertEquals( final String message, final SampleRecord expected, final SampleRecord actual )
	{
		Assert.assertEquals( message + ": string", expected.string, actual.string );
		Assert.assertNotSame( message + ": timestamp", expected.timestamp, actual.timestamp );
		assertEquals( message + ": date", expected.date, actual.date );
		Assert.assertEquals( message + ": localizedString", expected.localizedString, actual.localizedString );
		Assert.assertEquals( message + ": stringList", expected.stringList, actual.stringList );
		Assert.assertEquals( message + ": nestedProperties", expected.nestedProperties, actual.nestedProperties );
		Assert.assertEquals( message + ": nullableNestedProperties", expected.nullableNestedProperties, actual.nullableNestedProperties );
	}

	private static void assertEquals( final String message, final Date expected, final Date actual )
	{
		if ( ( expected == null ) || ( actual == null ) || ( ( actual.getTime() - expected.getTime() ) > 1000L ) )
		{
			/* the following will always fail */
			Assert.assertEquals( message, expected, actual );
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

						Assert.assertEquals( "Unexpected value.", "First", update1.string );
						Assert.assertEquals( "Unexpected value.", "First", update2.string );
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

						Assert.assertEquals( "Unexpected value.", "Second", update1.string );
						Assert.assertEquals( "Unexpected value.", "Second", update2.string );
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

		Assert.assertEquals( "Failed to delete record 1", 1, deleteById( db, record1.ID ) );
		Assert.assertEquals( "Failed to delete record 2", 1, deleteById( db, record2.ID ) );
	}
}
