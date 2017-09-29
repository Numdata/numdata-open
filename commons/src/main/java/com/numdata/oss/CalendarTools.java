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
package com.numdata.oss;

import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import org.jetbrains.annotations.*;

/**
 * Provides calendar-related utility methods.
 *
 * @author G. Meinders
 */
public class CalendarTools
{
	/**
	 * UTC timezone.
	 */
	public static final TimeZone UTC = TimeZone.getTimeZone( "UTC" );

	/**
	 * <a href='http://en.wikipedia.org/wiki/ISO_8601'>ISO 8601</a> date
	 * format.
	 */
	public static final SimpleDateFormat ISO8601_DATE_FORMAT;

	/**
	 * <a href='http://en.wikipedia.org/wiki/ISO_8601'>ISO 8601</a> date
	 * format.
	 */
	public static final SimpleDateFormat ISO8601_UTC_DATE_FORMAT;

	/**
	 * <a href='http://en.wikipedia.org/wiki/ISO_8601'>ISO 8601</a> date/time
	 * format with precision set to minutes.
	 */
	public static final SimpleDateFormat ISO8601_DATETIME_FORMAT;

	/**
	 * <a href='http://en.wikipedia.org/wiki/ISO_8601'>ISO 8601</a> date/time
	 * format with precision set to minutes.
	 */
	public static final SimpleDateFormat ISO8601_UTC_DATETIME_FORMAT;

	/**
	 * <a href='http://en.wikipedia.org/wiki/ISO_8601'>ISO 8601</a> date/time
	 * format with precision set to seconds.
	 */
	public static final SimpleDateFormat ISO8601_DATETIME_SECONDS_FORMAT;

	/**
	 * <a href='http://en.wikipedia.org/wiki/ISO_8601'>ISO 8601</a> date/time
	 * format with precision set to seconds.
	 */
	public static final SimpleDateFormat ISO8601_UTC_DATETIME_SECONDS_FORMAT;

	/**
	 * <a href='http://en.wikipedia.org/wiki/ISO_8601'>ISO 8601</a> date/time
	 * format with precision set to milliseconds.
	 */
	public static final SimpleDateFormat ISO8601_DATETIME_MILLISECONDS_FORMAT;

	/**
	 * <a href='http://en.wikipedia.org/wiki/ISO_8601'>ISO 8601</a> date/time
	 * format with precision set to milliseconds.
	 */
	public static final SimpleDateFormat ISO8601_UTC_DATETIME_MILLISECONDS_FORMAT;

	static
	{
		//noinspection SpellCheckingInspection
		ISO8601_DATE_FORMAT = new SimpleDateFormat( "yyyy-MM-dd" );

		//noinspection SpellCheckingInspection
		final SimpleDateFormat iso8601UtcDateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
		iso8601UtcDateFormat.setTimeZone( UTC );
		ISO8601_UTC_DATE_FORMAT = iso8601UtcDateFormat;

		//noinspection SpellCheckingInspection
		ISO8601_DATETIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mmZ" );

		//noinspection SpellCheckingInspection
		final SimpleDateFormat iso8601UtcDateTimeFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm'Z'" );
		iso8601UtcDateTimeFormat.setTimeZone( UTC );
		ISO8601_UTC_DATETIME_FORMAT = iso8601UtcDateTimeFormat;

		//noinspection SpellCheckingInspection
		ISO8601_DATETIME_SECONDS_FORMAT = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );

		//noinspection SpellCheckingInspection
		final SimpleDateFormat iso8601UtcDateTimeSecondsFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
		iso8601UtcDateTimeSecondsFormat.setTimeZone( UTC );
		ISO8601_UTC_DATETIME_SECONDS_FORMAT = iso8601UtcDateTimeSecondsFormat;

		//noinspection SpellCheckingInspection
		ISO8601_DATETIME_MILLISECONDS_FORMAT = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSSZ" );

		//noinspection SpellCheckingInspection
		final SimpleDateFormat iso8601UtcDateTimeMillisecondsFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'" );
		iso8601UtcDateTimeMillisecondsFormat.setTimeZone( UTC );
		ISO8601_UTC_DATETIME_MILLISECONDS_FORMAT = iso8601UtcDateTimeMillisecondsFormat;
	}

	/**
	 * Returns a calendar for the given date.
	 *
	 * @param date Date to get calendar for.
	 *
	 * @return Calendar for the given date.
	 */
	public static Calendar getInstance( @NotNull final Date date )
	{
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime( date );
		return calendar;
	}

	/**
	 * Returns a calendar instance for the current date. The time fields of the
	 * calendar are all set to zero.
	 *
	 * @return Calendar for the current date, with time fields set to zero.
	 */
	public static Calendar getDateInstance()
	{
		final Calendar calendar = Calendar.getInstance();
		resetTimeFields( calendar );
		return calendar;
	}

	/**
	 * Returns a calendar instance for the given date. The time fields of the
	 * calendar are all set to zero.
	 *
	 * @param date Date to get calendar for.
	 *
	 * @return Calendar for the given date, with time fields set to zero.
	 */
	public static Calendar getDateInstance( @NotNull final Date date )
	{
		final Calendar calendar = getInstance( date );
		resetTimeFields( calendar );
		return calendar;
	}

	/**
	 * Returns integer representing date.
	 *
	 * The result is calculated as follows: years * 10000 + month * 100 + day.
	 *
	 * Example: august 10th of 2049 would be 20490810.
	 *
	 * @param date Date to get date integer for.
	 *
	 * @return Integer date.
	 */
	public static int getDateInt( @NotNull final Date date )
	{
		return getDateInt( getInstance( date ) );
	}

	/**
	 * Returns integer representing date.
	 *
	 * The result is calculated as follows: years * 10000 + month * 100 + day.
	 *
	 * Example: august 10th of 2049 would be 20490810.
	 *
	 * @param calendar Calendar to get date integer for.
	 *
	 * @return Integer date.
	 */
	public static int getDateInt( @NotNull final Calendar calendar )
	{
		return calendar.get( Calendar.YEAR ) * 10000 + ( calendar.get( Calendar.MONTH ) + 1 ) * 100 + calendar.get( Calendar.DAY_OF_MONTH );
	}

	/**
	 * Returns a calendar instance for the current time. The date fields of the
	 * calendar are all set to the Epoch (January 1, 1970, Gregorian calendar).
	 *
	 * @return Calendar for the current date, with time fields set to zero.
	 */
	public static Calendar getTimeInstance()
	{
		final Calendar calendar = Calendar.getInstance();
		resetDateFields( calendar );
		return calendar;
	}

	/**
	 * Returns a calendar instance for the given date. The date fields of the
	 * calendar are all set to the Epoch (January 1, 1970, Gregorian calendar).
	 *
	 * @param date Date to get calendar for.
	 *
	 * @return Calendar for the given date, with time fields set to zero.
	 */
	public static Calendar getTimeInstance( @NotNull final Date date )
	{
		final Calendar calendar = getInstance( date );
		resetDateFields( calendar );
		return calendar;
	}

	/**
	 * Sets the time fields of the given calendar to the Epoch (January 1, 1970,
	 * Gregorian calendar).
	 *
	 * @param calendar Calendar to adjust.
	 */
	public static void resetDateFields( @NotNull final Calendar calendar )
	{
		final int hour = calendar.get( Calendar.HOUR_OF_DAY );
		final int minute = calendar.get( Calendar.MINUTE );
		final int second = calendar.get( Calendar.SECOND );
		final int millisecond = calendar.get( Calendar.MILLISECOND );
		calendar.setTimeInMillis( 0L );
		calendar.set( Calendar.HOUR_OF_DAY, hour );
		calendar.set( Calendar.MINUTE, minute );
		calendar.set( Calendar.SECOND, second );
		calendar.set( Calendar.MILLISECOND, millisecond );
	}

	/**
	 * Sets the time fields of the given calendar to zero.
	 *
	 * @param calendar Calendar to adjust.
	 */
	public static void resetTimeFields( final Calendar calendar )
	{
		calendar.set( Calendar.HOUR_OF_DAY, 0 );
		calendar.set( Calendar.MINUTE, 0 );
		calendar.set( Calendar.SECOND, 0 );
		calendar.set( Calendar.MILLISECOND, 0 );
	}

	/**
	 * Returns the date of Easter Sunday in a given year.
	 *
	 * @param year Year to calculate the date for.
	 *
	 * @return Date of Easter Sunday.
	 *
	 * @throws IllegalArgumentException if the year is before 1583 (since the
	 * algorithm only works on the Gregorian calendar).
	 * @see #getEasterSundayCalendar(int)
	 */
	public static Date getEasterSunday( final int year )
	{
		final Calendar calendar = getEasterSundayCalendar( year );
		return calendar.getTime();
	}

	/**
	 * Returns a calendar set to the date of Easter Sunday in a given year. The
	 * algorithm used is from <cite>Donald E. Knuth, The Art of Computer
	 * Programming, 1.3.2</cite>. The algorithm only works for the Gregorian
	 * calendar. As such, the earliest supported year is 1583.
	 *
	 * A lot of information about the computation of the date of Easter can
	 * be found here: <a href="http://en.wikipedia.org/wiki/Computus">http://en.wikipedia.org/wiki/Computus</a>
	 *
	 * A (slightly different) Java implementation of the algorithm used is
	 * provided here: <a href="http://www.java2s.com/Code/Java/Development-Class/EastercomputethedayonwhichEasterfalls.htm">http://www.java2s.com/Code/Java/Development-Class/EastercomputethedayonwhichEasterfalls.htm</a>
	 *
	 * Additional information about the algorithm used, alongside an
	 * implementation in some obscure language, can be found here: <a
	 * href="http://www.forth.org.ru/~mlg/mirror/home.earthlink.net/~neilbawd/easter.html">http://www.forth.org.ru/~mlg/mirror/home.earthlink.net/~neilbawd/easter.html</a>
	 *
	 * @param year Year to calculate the date for.
	 *
	 * @return Calendar set to midnight on Easter Sunday in the given year.
	 *
	 * @throws IllegalArgumentException if the year is before 1583 (since the
	 * algorithm only works on the Gregorian calendar).
	 */
	public static GregorianCalendar getEasterSundayCalendar( final int year )
	{
		if ( year < 1583 )
		{
			throw new IllegalArgumentException( "algorithm invalid before 1583" );
		}

		// E1: Golden number
		final int golden = ( year % 19 ) + 1;

		// E2: Century
		final int century = ( year / 100 ) + 1;

		// E3: Corrections
		final int x = ( 3 * century / 4 ) - 12; // leap year correction
		final int z = ( ( 8 * century + 5 ) / 25 ) - 5; // sync with moon's orbit

		// E4: Find Sunday
		final int d = ( 5 * year / 4 ) - x - 10;

		// E5: Epact (specifies when a full moon occurs)
		int epact = ( 11 * golden + 20 + z - x ) % 30;
		if ( ( ( epact == 25 ) && ( golden > 11 ) ) || ( epact == 24 ) )
		{
			epact++;
		}

		// E6: Find full moon
		final int fullMoon = ( 44 - epact ) + 30 * ( epact > 23 ? 1 : 0 );

		// E7: Advance to Sunday
		final int sunday = fullMoon + 7 - ( ( d + fullMoon ) % 7 );

		// E8: Get month
		final int month = Calendar.MARCH + sunday / 31;
		final int day = sunday % 31;

		return new GregorianCalendar( year, month, day );
	}

	/**
	 * Returns the date of Ascension Day in a given year. Ascension Day is
	 * celebrated on a Thursday, 39 days after Easter Sunday.
	 *
	 * @param year Year to calculate the date for.
	 *
	 * @return Date of Ascension day.
	 */
	public static Date getAscensionDay( final int year )
	{
		final Calendar calendar = getEasterSundayCalendar( year );
		calendar.add( Calendar.DATE, 39 );
		return calendar.getTime();
	}

	/**
	 * Returns the date of Pentecost Sunday in a given year. Pentecost Sunday is
	 * celebrated 49 days after Easter Sunday.
	 *
	 * @param year Year to calculate the date for.
	 *
	 * @return Date of Pentecost Sunday.
	 *
	 * @throws IllegalArgumentException if the year is before 1583 (since the
	 * algorithm only works on the Gregorian calendar).
	 * @see #getPentecostSundayCalendar(int)
	 */
	public static Date getPentecostSunday( final int year )
	{
		final Calendar calendar = getPentecostSundayCalendar( year );
		return calendar.getTime();
	}

	/**
	 * Returns a calendar set to the date of Pentecost Sunday in a given year.
	 * Pentecost Sunday is celebrated 49 days after Easter Sunday.
	 *
	 * @param year Year to calculate the date for.
	 *
	 * @return Calendar set to midnight on Pentecost Sunday in the given year.
	 *
	 * @throws IllegalArgumentException if the year is before 1583 (since the
	 * algorithm only works on the Gregorian calendar).
	 * @see #getEasterSundayCalendar(int)
	 */
	public static GregorianCalendar getPentecostSundayCalendar( final int year )
	{
		final GregorianCalendar calendar = getEasterSundayCalendar( year );
		calendar.add( Calendar.DATE, 49 );
		return calendar;
	}

	/**
	 * Returns the specified Gregorian date.
	 *
	 * @param year  Year.
	 * @param month Month (see {@link Calendar} constants).
	 * @param day   Day of month.
	 *
	 * @return Specified date.
	 */
	public static Date gregorianDate( final int year, final int month, final int day )
	{
		if ( month > Calendar.DECEMBER )
		{
			throw new IllegalArgumentException( "Invalid month: " + month + "; use (zero-based) constants Calendar.JANUARY, etc." );
		}
		final GregorianCalendar calendar = new GregorianCalendar( year, month, day );
		return calendar.getTime();
	}

	/**
	 * Returns the specified Gregorian date and time.
	 *
	 * @param year   Year.
	 * @param month  Month (see {@link Calendar} constants).
	 * @param day    Day of month.
	 * @param hour   Hour of day
	 * @param minute Minute.
	 * @param second Second.
	 *
	 * @return Specified date and time.
	 */
	public static Date gregorianDate( final int year, final int month, final int day, final int hour, final int minute, final int second )
	{
		if ( month > Calendar.DECEMBER )
		{
			throw new IllegalArgumentException( "Invalid month: " + month + "; use (zero-based) constants Calendar.JANUARY, etc." );
		}
		final GregorianCalendar calendar = new GregorianCalendar( year, month, day, hour, minute, second );
		return calendar.getTime();
	}

	/**
	 * Returns whether the given instants in time occur on the same date.
	 *
	 * @param first  First instant in time.
	 * @param second Second instant in time.
	 *
	 * @return Whether the instants in time occur on the same date.
	 */
	public static boolean isSameDate( @NotNull final Date first, @NotNull final Date second )
	{
		return isSameDate( getInstance( first ), getInstance( second ) );
	}

	/**
	 * Returns whether the given instants in time occur on the same date.
	 *
	 * @param first    First instant in time.
	 * @param second   Second instant in time.
	 * @param timeUnit Granularity for time comparison.
	 *
	 * @return Whether the instants in time occur on the same date.
	 */
	public static boolean isSameDate( @NotNull final Date first, @NotNull final Date second, @NotNull final TimeUnit timeUnit )
	{
		return isSameDate( getInstance( first ), getInstance( second ), timeUnit );
	}

	/**
	 * Returns whether the given calendars share the same date.
	 *
	 * @param first  First instant in time.
	 * @param second Second instant in time.
	 *
	 * @return Whether the instants in time occur on the same date.
	 */
	public static boolean isSameDate( @NotNull final Calendar first, @NotNull final Calendar second )
	{
		return ( first.get( Calendar.DAY_OF_YEAR ) == second.get( Calendar.DAY_OF_YEAR ) ) &&
		       ( first.get( Calendar.YEAR ) == second.get( Calendar.YEAR ) ) &&
		       ( first.get( Calendar.ERA ) == second.get( Calendar.ERA ) );
	}

	/**
	 * Returns whether the given calendars share the same date.
	 *
	 * @param first    First instant in time.
	 * @param second   Second instant in time.
	 * @param timeUnit Granularity for time comparison.
	 *
	 * @return Whether the instants in time occur on the same date.
	 */
	public static boolean isSameDate( @NotNull final Calendar first, @NotNull final Calendar second, @NotNull final TimeUnit timeUnit )
	{
		return ( compareDate( first, second, timeUnit ) == 0 );
	}

	/**
	 * Compares two calendars by date only.
	 *
	 * @param first  First calendar to compare.
	 * @param second Second calendar to compare.
	 *
	 * @return -1 if the first date is before the second date; 0 if both dates
	 * are equal; 1 if the first date later date than second.
	 */
	public static int compareDate( @NotNull final Date first, @NotNull final Date second )
	{
		return compareDate( getInstance( first ), getInstance( second ) );
	}

	/**
	 * Compares two calendars by date only.
	 *
	 * @param first    First calendar to compare.
	 * @param second   Second calendar to compare.
	 * @param timeUnit Granularity for time comparison.
	 *
	 * @return -1 if the first date is before the second date; 0 if both dates
	 * are equal; 1 if the first date later date than second.
	 */
	public static int compareDate( @NotNull final Date first, @NotNull final Date second, @NotNull final TimeUnit timeUnit )
	{
		return compareDate( getInstance( first ), getInstance( second ), timeUnit );
	}

	/**
	 * Compares two calendars by date only.
	 *
	 * @param first  First calendar to compare.
	 * @param second Second calendar to compare.
	 *
	 * @return -1 if the first date is before the second date; 0 if both dates
	 * are equal; 1 if the first date later date than second.
	 */
	public static int compareDate( @NotNull final Calendar first, @NotNull final Calendar second )
	{
		return compareDate( first, second, TimeUnit.DAYS );
	}

	/**
	 * Compares two calendars by date only.
	 *
	 * @param first    First calendar to compare.
	 * @param second   Second calendar to compare.
	 * @param timeUnit Granularity for time comparison.
	 *
	 * @return -1 if the first date is before the second date; 0 if both dates
	 * are equal; 1 if the first date later date than second.
	 */
	public static int compareDate( @NotNull final Calendar first, @NotNull final Calendar second, @NotNull final TimeUnit timeUnit )
	{
		final int result;

		final int era1 = first.get( Calendar.ERA );
		final int era2 = second.get( Calendar.ERA );
		if ( era1 != era2 )
		{
			result = ( era1 < era2 ) ? -1 : 1;
		}
		else
		{
			final int year1 = first.get( Calendar.YEAR );
			final int year2 = second.get( Calendar.YEAR );
			if ( year1 != year2 )
			{
				result = ( year1 < year2 ) ? -1 : 1;
			}
			else
			{
				final int day1 = first.get( Calendar.DAY_OF_YEAR );
				final int day2 = second.get( Calendar.DAY_OF_YEAR );
				if ( day1 != day2 )
				{
					result = ( day1 < day2 ) ? -1 : 1;
				}
				else if ( timeUnit.ordinal() >= TimeUnit.DAYS.ordinal() )
				{
					result = 0;
				}
				else
				{
					final int hour1 = first.get( Calendar.HOUR_OF_DAY );
					final int hour2 = second.get( Calendar.HOUR_OF_DAY );
					if ( hour1 != hour2 )
					{
						result = ( hour1 < hour2 ) ? -1 : 1;
					}
					else if ( timeUnit.ordinal() >= TimeUnit.HOURS.ordinal() )
					{
						result = 0;
					}
					else
					{
						final int minute1 = first.get( Calendar.MINUTE );
						final int minute2 = second.get( Calendar.MINUTE );
						if ( minute1 != minute2 )
						{
							result = ( minute1 < minute2 ) ? -1 : 1;
						}
						else if ( timeUnit.ordinal() >= TimeUnit.MINUTES.ordinal() )
						{
							result = 0;
						}
						else
						{
							final int second1 = first.get( Calendar.SECOND );
							final int second2 = second.get( Calendar.SECOND );
							if ( second1 != second2 )
							{
								result = ( second1 < second2 ) ? -1 : 1;
							}
							else if ( timeUnit.ordinal() >= TimeUnit.SECONDS.ordinal() )
							{
								result = 0;
							}
							else
							{
								final int millis1 = first.get( Calendar.MILLISECOND );
								final int millis2 = second.get( Calendar.MILLISECOND );
								if ( millis1 != millis2 )
								{
									result = ( millis1 < millis2 ) ? -1 : 1;
								}
								else
								{
									result = 0;
								}
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Determine the number of days between two dates.
	 *
	 * @param first  First date to compare.
	 * @param second Second date to compare.
	 *
	 * @return Difference in number of days between the two dates. Negative if
	 * first is before second; zero is the date are the same; positive if the
	 * first is after the second.
	 */
	public static int getDateDifference( @NotNull final Date first, @NotNull final Date second )
	{
		return getDateDifference( getInstance( first ), getInstance( second ) );
	}

	/**
	 * Tests whether a given date matches the specified date range. The {@code
	 * startDate} and {@code endDate} parameter can be set to {@code null} to
	 * set no start and/or end limit. If both parameters are set to {@code
	 * null}, this method is a no-op.
	 *
	 * @param date      Date to consider.
	 * @param startDate Start date, inclusive. Time is ignored.
	 * @param endDate   End date, inclusive. Time is ignored.
	 *
	 * @return {@code true} if {@code date} is between the given start and end
	 * dates.
	 */
	public static boolean isBetweenDates( @Nullable final Date date, @Nullable final Date startDate, @Nullable final Date endDate )
	{
		return ( ( startDate == null ) && ( endDate == null ) ) ||
		       ( ( date != null ) &&
		         ( ( startDate == null ) || ( compareDate( date, startDate ) >= 0 ) ) &&
		         ( ( endDate == null ) || ( compareDate( date, endDate ) <= 0 ) ) );
	}

	/**
	 * Determine the number of days between two dates.
	 *
	 * @param first  First calendar to compare.
	 * @param second Second calendar to compare.
	 *
	 * @return Difference in number of days between the two calendars. Negative
	 * if first is before second; zero is the date are the same; positive if the
	 * first is after the second.
	 */
	public static int getDateDifference( @NotNull final Calendar first, @NotNull final Calendar second )
	{
		int result = 0;

		final int direction = compareDate( first, second );
		if ( direction != 0 )
		{
			final Calendar calendar = (Calendar)first.clone();
			do
			{
				calendar.add( Calendar.DATE, -direction );
				result += direction;
			}
			while ( !isSameDate( calendar, second ) );
		}

		return result;
	}

	/**
	 * Should not be instantiated.
	 */
	private CalendarTools()
	{
	}
}
