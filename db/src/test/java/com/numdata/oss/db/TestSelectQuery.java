/*
 * Copyright (c) 2014-2017, Numdata BV, The Netherlands.
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

import static com.numdata.oss.CalendarTools.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test for {@link SelectQuery}.
 *
 * @author Gerrit Meinders
 */
public class TestSelectQuery
{
	/**
	 * Database services to use.
	 */
	private HsqlDbServices _db;

	@Before
	public void setUp()
	throws Exception
	{
		final HsqlDbServices db = new HsqlDbServices();
		_db = db;
	}

	@After
	public void tearDown()
	throws Exception
	{
		_db.shutdown();
		_db = null;
	}

	/**
	 * Tests {@link AbstractQuery#andWhereBetweenDates(String, Date, Date)}.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testAndWhereBetweenDates()
	throws Exception
	{
		System.out.println( "TestSelectQuery.testAndWhereBetweenDates()" );

		final HsqlDbServices db = _db;
		db.createTable( SampleRecord.class );

		final List<Date> dates = new ArrayList<Date>();
		dates.add( gregorianDate( 2014, Calendar.DECEMBER, 1, 12, 34, 56 ) );
		dates.add( gregorianDate( 2014, Calendar.DECEMBER, 1, 23, 59, 59 ) );
		dates.add( gregorianDate( 2014, Calendar.DECEMBER, 2, 0, 0, 0 ) );
		dates.add( gregorianDate( 2014, Calendar.DECEMBER, 2, 11, 15, 12 ) );
		dates.add( gregorianDate( 2014, Calendar.DECEMBER, 2, 23, 59, 59 ) );
		dates.add( gregorianDate( 2014, Calendar.DECEMBER, 3, 0, 0, 0 ) );
		dates.add( gregorianDate( 2014, Calendar.DECEMBER, 3, 12, 34, 56 ) );
		dates.add( gregorianDate( 2014, Calendar.DECEMBER, 3, 23, 59, 59 ) );
		dates.add( gregorianDate( 2014, Calendar.DECEMBER, 4, 0, 0, 0 ) );

		for ( final Date date : dates )
		{
			final SampleRecord record = new SampleRecord();
			record.string = date.toString();
			record.date = date;
			db.storeObject( record );
		}

		class Test
		{
			private final Date _startDate;

			private final Date _endDate;

			private final int _expectedCount;

			private Test( final Date startDate, final Date endDate, final int expectedCount )
			{
				_startDate = startDate;
				_endDate = endDate;
				_expectedCount = expectedCount;
			}
		}

		final List<Test> tests = new ArrayList<Test>();
		tests.add( new Test( gregorianDate( 2014, Calendar.NOVEMBER, 1, 11, 31, 15 ), gregorianDate( 2014, Calendar.DECEMBER, 1, 13, 30, 0 ), 2 ) );
		tests.add( new Test( gregorianDate( 2014, Calendar.NOVEMBER, 1, 11, 31, 15 ), gregorianDate( 2014, Calendar.DECEMBER, 2, 11, 30, 0 ), 5 ) );
		tests.add( new Test( gregorianDate( 2014, Calendar.DECEMBER, 2, 11, 31, 15 ), gregorianDate( 2014, Calendar.DECEMBER, 2, 7, 8, 9 ), 3 ) );
		tests.add( new Test( gregorianDate( 2014, Calendar.DECEMBER, 2, 13, 31, 15 ), gregorianDate( 2014, Calendar.DECEMBER, 3, 7, 8, 9 ), 6 ) );
		tests.add( new Test( gregorianDate( 2014, Calendar.DECEMBER, 3, 11, 31, 15 ), gregorianDate( 2014, Calendar.DECEMBER, 3, 7, 8, 9 ), 3 ) );
		tests.add( new Test( gregorianDate( 2014, Calendar.DECEMBER, 3, 11, 31, 15 ), gregorianDate( 2014, Calendar.DECEMBER, 4, 0, 0, 0 ), 4 ) );

		for ( final Test test : tests )
		{
			System.out.println( " - " + test._startDate + " .. " + test._endDate );
			final SelectQuery<SampleRecord> select = new SelectQuery<SampleRecord>( SampleRecord.class );
			select.select( "COUNT(*)" );
			select.andWhereBetweenDates( SampleRecord.DATE, test._startDate, test._endDate );
			final Number count = _db.selectNumber( select );
			assertNotNull( "Failed to perform select query.", count );
			assertEquals( "Unexpected number of records.", test._expectedCount, count.intValue() );
		}
	}
}
