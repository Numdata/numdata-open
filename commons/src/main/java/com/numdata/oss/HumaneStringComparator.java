/*
 * Copyright (c) 2009-2018, Numdata BV, The Netherlands.
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

/**
 * This code was borrowed from the following blog:
 *
 * http://www.jroller.com/tfenne/entry/humanestringcomparator_sorting_strings_for_people
 *
 * Written by Tim Fennell
 */
package com.numdata.oss;

import java.util.*;
import java.util.regex.*;

/**
 * A comparator for Strings that imposes a more "human" order on Strings.
 * Modeled on the way the Mac OS X Finder orders file names, it breaks the
 * Strings into segments of non-numbers and numbers (e.g. Foo123Bar into 'Foo',
 * 123, 'Bar') and compares the segments from each String one at a time.
 *
 * The following rules are applied when comparing segments:<ul>
 *
 * <li>A null segment sorts before a non-null segment (e.g. Foo < Foo123)</li>
 *
 * <li>String segments are compared case-insensitively</li>
 *
 * <li>Number segments are converted to java.lang.Doubles and compared</li>
 *
 * </ul>
 *
 * If two Strings are identical after analyzing them in a segment-at-time manner
 * then lhs.compareTo(rhs) will be invoked to ensure that the result provided by
 * the comparator is reflexive (i.e. compare(x,y) = -compare(y,x)) and agrees
 * with String.equals() in all cases.
 *
 * As an example, the following list of Strings is presented in the order in
 * which the comparator will sort them:
 *
 * <ul>
 *
 * <li>Foo</li>
 *
 * <li>Foo1Bar2</li>
 *
 * <li>Foo77.7Boo</li>
 *
 * <li>Foo123</li>
 *
 * <li>Foo777Boo</li>
 *
 * <li>Foo1234</li>
 *
 * <li>FooBar</li>
 *
 * <li>Fooey123</li>
 *
 * <li>Splat</li>
 *
 * </ul>
 *
 * @author Tim Fennell (oss@tfenne.com)
 */
public class HumaneStringComparator
implements Comparator<String>
{
	/**
	 * The Regular Expression used to match out segments of each String. Will
	 * incrementally match a number segment defined as one or more digits
	 * followed optionally by a decimal point and more digits, or a non-number
	 * segment consisting of any non-digit characters.
	 */
	Pattern SEGMENT_PATTERN = Pattern.compile( "(\\d+(\\.\\d+)?|\\D+)" );

	/**
	 * A default instance of the comparator that can be used without
	 * instantiating a new copy every time one is needed.
	 */
	public static final HumaneStringComparator DEFAULT = new HumaneStringComparator();

	/**
	 * The implementation of the Comparable interface method that compares two
	 * Strings. Implements the algorithm described in the class level javadoc.
	 *
	 * @param lhs the first of two Strings to compare
	 * @param rhs the second of two Strings to compare
	 *
	 * @return int -1, 0 or 1 respectively if the first String (lhs) sorts
	 * before equally or after the second String
	 */
	public int compare( final String lhs, final String rhs )
	{
		// Take care of nulls first
		if ( lhs == null && rhs == null )
		{
			return 0;
		}
		else if ( lhs == null )
		{
			return -1;
		}
		else if ( rhs == null ) return 1;

		final Matcher lhsMatcher = SEGMENT_PATTERN.matcher( lhs );
		final Matcher rhsMatcher = SEGMENT_PATTERN.matcher( rhs );

		int result = 0;
		while ( result == 0 )
		{
			final boolean lhsFound = lhsMatcher.find();
			final boolean rhsFound = rhsMatcher.find();

			if ( !lhsFound && !rhsFound )
			{
				// Both Strings ran out and they matched so far! Return a full compareTo
				// of the same Strings so that we don't violate equality checks
				return lhs.compareTo( rhs );
			}
			else if ( !lhsFound )
			{
				result = -1;
			}
			else if ( !rhsFound )
			{
				result = 1;
			}
			else
			{
				final String lhsSegment = lhsMatcher.group();
				final String rhsSegment = rhsMatcher.group();

				if ( Character.isDigit( lhsSegment.charAt( 0 ) ) && Character.isDigit( rhsSegment.charAt( 0 ) ) )
				{
					result = compareNumberSegments( lhsSegment, rhsSegment );
				}
				else
				{
					result = compareStringSegments( lhsSegment, rhsSegment );
				}
			}
		}

		return result;
	}

	/**
	 * Converts the two Strings to doubles and then compares then numerically by
	 * invoking Double.compareTo()
	 */
	protected int compareNumberSegments( final String lhs, final String rhs )
	{
		return new Double( lhs ).compareTo( new Double( rhs ) );
	}

	/**
	 * Compares the left hand String to the right hand String case-insensitively
	 * by invoking lhs.compareToIgnoreCase(rhs).
	 */
	protected int compareStringSegments( final String lhs, final String rhs )
	{
		return lhs.compareToIgnoreCase( rhs );
	}
}
