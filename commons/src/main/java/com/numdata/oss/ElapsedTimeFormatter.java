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

/**
 * This class implements functionality to format elapsed time in a user-friendly
 * and internationalized form.
 *
 * @author Peter S. Heijnen
 */
public class ElapsedTimeFormatter
{
	/**
	 * Short format to use for elapsed times.
	 */
	private final MessageFormat _shortFormat;

	/**
	 * Format to use for each unit when using long format for elapsed times.
	 */
	private final MessageFormat[] _longFormat;

	/**
	 * Number of milliseconds per second.
	 */
	public static final long MILLIS_PER_SECOND = 1000L;

	/**
	 * Number of milliseconds per minute.
	 */
	public static final long MILLIS_PER_MINUTE = 60L * MILLIS_PER_SECOND;

	/**
	 * Number of milliseconds per uur.
	 */
	public static final long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;

	/**
	 * Number of milliseconds per dag.
	 */
	public static final long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;

	/**
	 * Construct formatted.
	 *
	 * @param locale Locale to use for internationalization.
	 */
	public ElapsedTimeFormatter( final Locale locale )
	{
		final ResourceBundle res = ResourceBundleTools.getBundle( ElapsedTimeFormatter.class, locale );

		_shortFormat = new MessageFormat( res.getString( "shortFormat" ) );

		_longFormat = new MessageFormat[]
		{
		new MessageFormat( res.getString( "longDaysFormat" ) ),
		new MessageFormat( res.getString( "longHoursFormat" ) ),
		new MessageFormat( res.getString( "longMinutesFormat" ) ),
		new MessageFormat( res.getString( "longSecondsFormat" ) ),
		new MessageFormat( res.getString( "longMillisecondsFormat" ) ),
		};
	}

	/**
	 * Format the specified elapsed time using a short form. This writes out the
	 * time in a comprehensive form (examples: "23 days, 1:12:39" or
	 * "0:00:01.298" ).
	 *
	 * @param elapsedMillis Elapsed time in milliseconds.
	 *
	 * @return Formatted time string.
	 */
	public String formatElapsedTimeShort( final long elapsedMillis )
	{
		return _shortFormat.format( getUnitArguments( elapsedMillis ) );
	}

	/**
	 * Format the specified elapsed time using a long form. This writes out the
	 * time in complete unit names (examples: "23 days, 1 hour, 12 minutes" or
	 * "19 seconds, 10ms").
	 *
	 * This convenience method uses generally acceptable defaults for the more
	 * details arguments defined by the overloaded version of this method.
	 *
	 * @param elapsedMillis Elapsed time in milliseconds.
	 *
	 * @return Formatted time string.
	 */
	public String formatElapsedTimeLong( final long elapsedMillis )
	{
		return formatElapsedTimeLong( elapsedMillis, 1, 3 );
	}

	/**
	 * Format the specified elapsed time using a long form. This writes out the
	 * time in complete unit names (examples: "23 days, 1 hour, 12 minutes" or
	 * "19 seconds, 10ms").
	 *
	 * @param elapsedMillis Elapsed time in milliseconds.
	 * @param minimumUnits  Include at least this number of units in the
	 *                      result.
	 * @param maximumUnits  Include at most this number of units in the result.
	 *
	 * @return Formatted time string.
	 */
	public String formatElapsedTimeLong( final long elapsedMillis, final int minimumUnits, final int maximumUnits )
	{
		final StringBuffer sb = new StringBuffer();

		final Integer[] unitValues = getUnitArguments( elapsedMillis );
		final int unitCount = unitValues.length;
		final MessageFormat[] unitFormats = _longFormat;

		int insertedUnitCount = 0;

		for ( int unitIndex = 0; unitIndex < unitCount; unitIndex++ )
		{
			final Integer value = unitValues[ unitIndex ];

			if ( ( value > 0 ) || ( insertedUnitCount > 0 ) || ( ( unitCount - unitIndex ) <= minimumUnits ) )
			{
				if ( insertedUnitCount > 0 )
				{
					sb.append( ", " );
				}

				unitFormats[ unitIndex ].format( value, sb, new FieldPosition( 0 ) );

				if ( ++insertedUnitCount >= maximumUnits )
				{
					break;
				}
			}
		}

		return sb.toString();
	}

	/**
	 * This internal helper method is used to calculate the various time unit
	 * values from an elapsed time given in milliseconds.
	 *
	 * @param elapsedMillis Elapsed time in milliseconds.
	 *
	 * @return Array containing unit values as {@link Integer} objects.
	 */
	private static Integer[] getUnitArguments( final long elapsedMillis )
	{
		return new Integer[]
		{
		(int)( elapsedMillis / MILLIS_PER_DAY ),
		(int)( elapsedMillis / MILLIS_PER_HOUR ) % 24,
		(int)( elapsedMillis / MILLIS_PER_MINUTE ) % 60,
		(int)( elapsedMillis / MILLIS_PER_SECOND ) % 60,
		(int)( elapsedMillis % MILLIS_PER_SECOND )
		};
	}
}
