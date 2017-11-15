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

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Class to represent well-formed (ordered) non-overlapping integer intervals.
 *
 * @author Peter S. Heijnen
 */
public class IntIntervals
{
	/**
	 * Ordered non-overlapping intervals.
	 */
	private final List<Interval> _intervals = new ArrayList<Interval>();

	@NotNull
	public List<Interval> getIntervals()
	{
		return Collections.unmodifiableList( _intervals );
	}

	/**
	 * Add interval.
	 *
	 * @param start Start of interval (inclusive).
	 * @param end   End of interval (inclusive).
	 */
	public void add( final int start, final int end )
	{
		if ( start < end )
		{
			Interval mergedInterval = null;
			int mergedStart = start;
			int mergedEnd = end;
			final List<Interval> intervals = _intervals;

			int i = 0;
			while ( i < intervals.size() )
			{
				final Interval interval = intervals.get( i );
				if ( mergedEnd < interval.getStart() )
				{
					// added/merged interval is before existing interval => done
					break;
				}

				if ( mergedStart > interval.getEnd() )
				{
					// no overlap => next
					i++;
				}
				else
				{
					// merge with overlapping interval
					mergedStart = Math.min( mergedStart, interval.getStart() );
					mergedEnd = Math.max( mergedEnd, interval.getEnd() );

					if ( mergedInterval == null )
					{
						mergedInterval = interval;
						i++;
					}
					else
					{
						intervals.remove( i );
					}
				}
			}

			if ( mergedInterval == null )
			{
				// add new interval if no intersection with an existing interval is found
				intervals.add( i, new Interval( mergedStart, mergedEnd ) );
			}
			else
			{
				// update merged interval
				mergedInterval._start = mergedStart;
				mergedInterval._end = mergedEnd;
			}
		}
	}

	/**
	 * Remove interval.
	 *
	 * @param start Start of interval (exclusive).
	 * @param end   End of interval (exclusive).
	 */
	public void remove( final int start, final int end )
	{
		if ( start < end )
		{
			final List<Interval> intervals = _intervals;

			int i = 0;
			while ( i < intervals.size() )
			{
				final Interval interval = intervals.get( i );

				if ( interval.getStart() >= end )
				{
					// interval is after removed interval => done!
					break;
				}

				if ( start >= interval.getEnd() )
				{
					// no overlap => next
					i++;
				}
				else if ( start <= interval.getStart() )
				{
					if ( end < interval.getEnd() )
					{
						// removed interval overlaps start of interval => done!
						interval._start = end;
						break;
					}
					else
					{
						// removed interval covers entire interval => delete
						intervals.remove( i );
					}
				}
				else
				{
					if ( end >= interval.getEnd() )
					{
						// removed interval overlaps end of interval
						interval._end = start;
						i++;
					}
					else
					{
						// removed interval splits existing interval
						intervals.add( i + 1, new Interval( end, interval.getEnd() ) );
						interval._end = start;
						break;
					}
				}
			}
		}
	}

	/**
	 * Returns whether any interval is present.
	 *
	 * @return {@code true} if at least one interval is present.
	 */
	public boolean isEmpty()
	{
		return _intervals.isEmpty();
	}

	/**
	 * Returns total length of intervals.
	 *
	 * @return Total length of intervals.
	 */
	public int getTotalLength()
	{
		int result = 0;

		for ( final Interval interval : _intervals )
		{
			result += interval.getLength();
		}

		return result;
	}

	/**
	 * Integer interval.
	 */
	public static class Interval
	{
		/**
		 * Start of interval (inclusive).
		 */
		private int _start;

		/**
		 * End of interval (inclusive).
		 */
		private int _end;

		/**
		 * Create interval.
		 *
		 * @param start Start of interval (inclusive).
		 * @param end   End of interval (inclusive).
		 */
		public Interval( final int start, final int end )
		{
			_start = start;
			_end = end;
		}

		public int getStart()
		{
			return _start;
		}

		public int getEnd()
		{
			return _end;
		}

		public int getLength()
		{
			return _end - _start;
		}
	}
}
