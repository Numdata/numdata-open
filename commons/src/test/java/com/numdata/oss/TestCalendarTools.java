/*
 * Copyright (c) 2010-2020, Numdata BV, The Netherlands.
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
package com.numdata.oss;

import java.text.*;
import java.util.*;

import static com.numdata.oss.CalendarTools.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link CalendarTools}.
 *
 * @author G. Meinders
 */
public class TestCalendarTools
{
	/**
	 * Unit test for {@link CalendarTools#getEasterSunday(int)}. Verifies the
	 * dates returned for all years between 1990 and 2050, based on a table
	 * found at <a href="http://www.gmarts.org/index.php?go=413">http://www.gmarts.org/index.php?go=413</a>.
	 */
	@Test
	public void testGetEasterSunday()
	{
		assertEquals( "Incorrect date.", gregorianDate( 1990, Calendar.APRIL, 15 ), CalendarTools.getEasterSunday( 1990 ) );
		assertEquals( "Incorrect date.", gregorianDate( 1991, Calendar.MARCH, 31 ), CalendarTools.getEasterSunday( 1991 ) );
		assertEquals( "Incorrect date.", gregorianDate( 1992, Calendar.APRIL, 19 ), CalendarTools.getEasterSunday( 1992 ) );
		assertEquals( "Incorrect date.", gregorianDate( 1993, Calendar.APRIL, 11 ), CalendarTools.getEasterSunday( 1993 ) );
		assertEquals( "Incorrect date.", gregorianDate( 1994, Calendar.APRIL, 3 ), CalendarTools.getEasterSunday( 1994 ) );
		assertEquals( "Incorrect date.", gregorianDate( 1995, Calendar.APRIL, 16 ), CalendarTools.getEasterSunday( 1995 ) );
		assertEquals( "Incorrect date.", gregorianDate( 1996, Calendar.APRIL, 7 ), CalendarTools.getEasterSunday( 1996 ) );
		assertEquals( "Incorrect date.", gregorianDate( 1997, Calendar.MARCH, 30 ), CalendarTools.getEasterSunday( 1997 ) );
		assertEquals( "Incorrect date.", gregorianDate( 1998, Calendar.APRIL, 12 ), CalendarTools.getEasterSunday( 1998 ) );
		assertEquals( "Incorrect date.", gregorianDate( 1999, Calendar.APRIL, 4 ), CalendarTools.getEasterSunday( 1999 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2000, Calendar.APRIL, 23 ), CalendarTools.getEasterSunday( 2000 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2001, Calendar.APRIL, 15 ), CalendarTools.getEasterSunday( 2001 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2002, Calendar.MARCH, 31 ), CalendarTools.getEasterSunday( 2002 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2003, Calendar.APRIL, 20 ), CalendarTools.getEasterSunday( 2003 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2004, Calendar.APRIL, 11 ), CalendarTools.getEasterSunday( 2004 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2005, Calendar.MARCH, 27 ), CalendarTools.getEasterSunday( 2005 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2006, Calendar.APRIL, 16 ), CalendarTools.getEasterSunday( 2006 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2007, Calendar.APRIL, 8 ), CalendarTools.getEasterSunday( 2007 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2008, Calendar.MARCH, 23 ), CalendarTools.getEasterSunday( 2008 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2009, Calendar.APRIL, 12 ), CalendarTools.getEasterSunday( 2009 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2010, Calendar.APRIL, 4 ), CalendarTools.getEasterSunday( 2010 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2011, Calendar.APRIL, 24 ), CalendarTools.getEasterSunday( 2011 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2012, Calendar.APRIL, 8 ), CalendarTools.getEasterSunday( 2012 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2013, Calendar.MARCH, 31 ), CalendarTools.getEasterSunday( 2013 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2014, Calendar.APRIL, 20 ), CalendarTools.getEasterSunday( 2014 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2015, Calendar.APRIL, 5 ), CalendarTools.getEasterSunday( 2015 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2016, Calendar.MARCH, 27 ), CalendarTools.getEasterSunday( 2016 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2017, Calendar.APRIL, 16 ), CalendarTools.getEasterSunday( 2017 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2018, Calendar.APRIL, 1 ), CalendarTools.getEasterSunday( 2018 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2019, Calendar.APRIL, 21 ), CalendarTools.getEasterSunday( 2019 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2020, Calendar.APRIL, 12 ), CalendarTools.getEasterSunday( 2020 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2021, Calendar.APRIL, 4 ), CalendarTools.getEasterSunday( 2021 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2022, Calendar.APRIL, 17 ), CalendarTools.getEasterSunday( 2022 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2023, Calendar.APRIL, 9 ), CalendarTools.getEasterSunday( 2023 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2024, Calendar.MARCH, 31 ), CalendarTools.getEasterSunday( 2024 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2025, Calendar.APRIL, 20 ), CalendarTools.getEasterSunday( 2025 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2026, Calendar.APRIL, 5 ), CalendarTools.getEasterSunday( 2026 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2027, Calendar.MARCH, 28 ), CalendarTools.getEasterSunday( 2027 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2028, Calendar.APRIL, 16 ), CalendarTools.getEasterSunday( 2028 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2029, Calendar.APRIL, 1 ), CalendarTools.getEasterSunday( 2029 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2030, Calendar.APRIL, 21 ), CalendarTools.getEasterSunday( 2030 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2031, Calendar.APRIL, 13 ), CalendarTools.getEasterSunday( 2031 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2032, Calendar.MARCH, 28 ), CalendarTools.getEasterSunday( 2032 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2033, Calendar.APRIL, 17 ), CalendarTools.getEasterSunday( 2033 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2034, Calendar.APRIL, 9 ), CalendarTools.getEasterSunday( 2034 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2035, Calendar.MARCH, 25 ), CalendarTools.getEasterSunday( 2035 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2036, Calendar.APRIL, 13 ), CalendarTools.getEasterSunday( 2036 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2037, Calendar.APRIL, 5 ), CalendarTools.getEasterSunday( 2037 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2038, Calendar.APRIL, 25 ), CalendarTools.getEasterSunday( 2038 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2039, Calendar.APRIL, 10 ), CalendarTools.getEasterSunday( 2039 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2040, Calendar.APRIL, 1 ), CalendarTools.getEasterSunday( 2040 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2041, Calendar.APRIL, 21 ), CalendarTools.getEasterSunday( 2041 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2042, Calendar.APRIL, 6 ), CalendarTools.getEasterSunday( 2042 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2043, Calendar.MARCH, 29 ), CalendarTools.getEasterSunday( 2043 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2044, Calendar.APRIL, 17 ), CalendarTools.getEasterSunday( 2044 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2045, Calendar.APRIL, 9 ), CalendarTools.getEasterSunday( 2045 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2046, Calendar.MARCH, 25 ), CalendarTools.getEasterSunday( 2046 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2047, Calendar.APRIL, 14 ), CalendarTools.getEasterSunday( 2047 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2048, Calendar.APRIL, 5 ), CalendarTools.getEasterSunday( 2048 ) );
		assertEquals( "Incorrect date.", gregorianDate( 2049, Calendar.APRIL, 18 ), CalendarTools.getEasterSunday( 2049 ) );
	}

	/**
	 * Tests {@link CalendarTools#ISO8601_DATE_FORMAT} and the other ISO8601
	 * constants in {@link CalendarTools}.
	 */
	@Test
	public void testDateTimeFormats()
	{
		final Calendar calendar = Calendar.getInstance( TimeZone.getTimeZone( "UTC" ) );
		calendar.set( 2020, Calendar.JULY, 2, 13, 37, 0 );
		calendar.set( Calendar.MILLISECOND, 0 );
		final Date zeroSeconds = calendar.getTime();
		calendar.set( Calendar.SECOND, 12 );
		final Date nonZeroSeconds = calendar.getTime();
		calendar.set( Calendar.MILLISECOND, 345 );
		final Date nonZeroMilliseconds = calendar.getTime();

		/*
		 * The non-UTC formats use the default time zone. To make this test
		 * system-independent, we clone these formats and set a predefined
		 * time zone.
		 */
		final TimeZone testedTimeZone = TimeZone.getTimeZone( "Europe/Amsterdam" );
		final SimpleDateFormat iso8601DateFormat = ( SimpleDateFormat)CalendarTools.ISO8601_DATE_FORMAT.clone();
		iso8601DateFormat.setTimeZone( testedTimeZone );
		final SimpleDateFormat iso8601DatetimeFormat = (SimpleDateFormat)CalendarTools.ISO8601_DATETIME_FORMAT.clone();
		iso8601DatetimeFormat.setTimeZone( testedTimeZone );
		final SimpleDateFormat iso8601DatetimeSecondsFormat = (SimpleDateFormat)CalendarTools.ISO8601_DATETIME_SECONDS_FORMAT.clone();
		iso8601DatetimeSecondsFormat.setTimeZone( testedTimeZone );
		final SimpleDateFormat iso8601DatetimeMillisecondsFormat = (SimpleDateFormat)CalendarTools.ISO8601_DATETIME_MILLISECONDS_FORMAT.clone();
		iso8601DatetimeMillisecondsFormat.setTimeZone( testedTimeZone );

		assertEquals( "Unexpected formatted date.", "2020-07-02", iso8601DateFormat.format( zeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02", CalendarTools.ISO8601_UTC_DATE_FORMAT.format( zeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T15:37+0200", iso8601DatetimeFormat.format( zeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T13:37Z", CalendarTools.ISO8601_UTC_DATETIME_FORMAT.format( zeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T15:37:00+0200", iso8601DatetimeSecondsFormat.format( zeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T13:37:00Z", CalendarTools.ISO8601_UTC_DATETIME_SECONDS_FORMAT.format( zeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T15:37:00.000+0200", iso8601DatetimeMillisecondsFormat.format( zeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T13:37:00.000Z", CalendarTools.ISO8601_UTC_DATETIME_MILLISECONDS_FORMAT.format( zeroSeconds ) );

		assertEquals( "Unexpected formatted date.", "2020-07-02", iso8601DateFormat.format( nonZeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02", CalendarTools.ISO8601_UTC_DATE_FORMAT.format( nonZeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T15:37+0200", iso8601DatetimeFormat.format( nonZeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T13:37Z", CalendarTools.ISO8601_UTC_DATETIME_FORMAT.format( nonZeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T15:37:12+0200", iso8601DatetimeSecondsFormat.format( nonZeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T13:37:12Z", CalendarTools.ISO8601_UTC_DATETIME_SECONDS_FORMAT.format( nonZeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T15:37:12.000+0200", iso8601DatetimeMillisecondsFormat.format( nonZeroSeconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T13:37:12.000Z", CalendarTools.ISO8601_UTC_DATETIME_MILLISECONDS_FORMAT.format( nonZeroSeconds ) );

		assertEquals( "Unexpected formatted date.", "2020-07-02", iso8601DateFormat.format( nonZeroMilliseconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02", CalendarTools.ISO8601_UTC_DATE_FORMAT.format( nonZeroMilliseconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T15:37+0200", iso8601DatetimeFormat.format( nonZeroMilliseconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T13:37Z", CalendarTools.ISO8601_UTC_DATETIME_FORMAT.format( nonZeroMilliseconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T15:37:12+0200", iso8601DatetimeSecondsFormat.format( nonZeroMilliseconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T13:37:12Z", CalendarTools.ISO8601_UTC_DATETIME_SECONDS_FORMAT.format( nonZeroMilliseconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T15:37:12.345+0200", iso8601DatetimeMillisecondsFormat.format( nonZeroMilliseconds ) );
		assertEquals( "Unexpected formatted date.", "2020-07-02T13:37:12.345Z", CalendarTools.ISO8601_UTC_DATETIME_MILLISECONDS_FORMAT.format( nonZeroMilliseconds ) );
	}
}
