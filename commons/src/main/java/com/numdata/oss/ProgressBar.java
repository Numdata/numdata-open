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

import java.io.*;
import java.util.concurrent.*;

/**
 * Text-based progress bar.
 *
 * @author Gerrit Meinders
 */
public class ProgressBar
{
	/**
	 * Delay until a progress bar is shown, in milliseconds.
	 */
	private int _delay = 1000;

	/**
	 * Maximum progress.
	 */
	private long _total = 1L;

	/**
	 * Current progress.
	 */
	private long _progress = 0L;

	/**
	 * Current value, in percent. {@code -1} if not shown yet.
	 */
	private int _value = -1;

	/**
	 * Time when {@link #begin} was last called.
	 */
	private long _startTime = -1L;

	/**
	 * Time when {@link #end} was called after the last call to {@link #begin}.
	 * This is {@code -1} until {@link #end} is called.
	 */
	private long _endTime = -1;

	/**
	 * Stream to write the progress bar to.
	 */
	private final PrintStream _out;

	/**
	 * Width of progress bar in characters.
	 */
	private int _width = 50;

	/**
	 * Constructs a new instance.
	 */
	public ProgressBar()
	{
		_out = System.out;
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param out Stream to write the progress bar to.
	 */
	public ProgressBar( final PrintStream out )
	{
		_out = out;
	}

	public int getDelay()
	{
		return _delay;
	}

	public void setDelay( final int delay )
	{
		_delay = delay;
	}

	public int getWidth()
	{
		return _width;
	}

	public void setWidth( final int width )
	{
		_width = width;
	}

	/**
	 * Called at the begin of an operation.
	 *
	 * @param total Total length of the operation.
	 */
	public void begin( final int total )
	{
		begin( (long)total );
	}

	/**
	 * Called at the begin of an operation.
	 *
	 * @param total Total length of the operation.
	 */
	public void begin( final long total )
	{
		_total = total;
		_progress = 0L;
		_value = -1;
		_startTime = System.nanoTime();
		_endTime = -1;
	}

	/**
	 * Prints the header for the progress bar.
	 */
	private void printHeader()
	{
		final int indent = 14;
		final int width = getWidth();
		final StringBuilder sb = new StringBuilder( indent + width );
		TextTools.appendSpace( sb, indent + width );

		setTextAt( sb, indent - 4, "0% |" );
		setTextAt( sb, indent + width / 4 - 5, "25% |" );
		setTextAt( sb, indent + width / 2 - 5, "50% |" );
		setTextAt( sb, indent + width * 3 / 4 - 5, "75% |" );
		setTextAt( sb, indent + width - 6, "100% |" );

		_out.println( sb );
		_out.print( "   Progress: ." );
	}

	/**
	 * Set text at given position in {@link StringBuilder}.
	 *
	 * @param sb   {@link StringBuilder}.
	 * @param pos  Position in string to set text at.
	 * @param text Text to set.
	 */
	private static void setTextAt( final StringBuilder sb, final int pos, final CharSequence text )
	{
		for ( int i = 0; i < text.length(); i++ )
		{
			sb.setCharAt( pos + i, text.charAt( i ) );
		}
	}

	/**
	 * Increments the progress bar's value.
	 */
	public void increment()
	{
		increment( 1L );
	}

	/**
	 * Increments the progress bar's value.
	 *
	 * @param amount Amount to increment.
	 *
	 * @throws IllegalArgumentException if {@code amount < 0}.
	 */
	public void increment( final int amount )
	{
		increment( (long)amount );
	}

	/**
	 * Increments the progress bar's value.
	 *
	 * @param amount Amount to increment.
	 *
	 * @throws IllegalArgumentException if {@code amount < 0}.
	 */
	public void increment( final long amount )
	{
		if ( amount < 0L )
		{
			throw new IllegalArgumentException( "amount < 0: " + amount );
		}

		_progress += amount;

		int value = _value;
		final int width = getWidth();
		final int newValue = (int)( _progress * (long)width / _total );

		/*
		 * Show progress bar after delay milliseconds.
		 */
		if ( ( value == -1 ) && ( newValue < width / 2 ) && ( getRunningTime() >= getDelay() ) )
		{
			value = 0;
			printHeader();
		}

		if ( value != -1 )
		{
			final PrintStream out = _out;
			while ( value < newValue )
			{
				out.print( '.' );
				value++;
			}
			_value = value;
		}
	}

	public long getProgress()
	{
		return _progress;
	}

	public long getTotal()
	{
		return _total;
	}

	/**
	 * Get current value, in percent.
	 *
	 * @return Current value, in percent.
	 */
	public int getValue()
	{
		return Math.max( 0, _value );
	}

	/**
	 * Get running time in milliseconds since {@link #begin} was called.. This
	 * is updated until {@link #end} is called.
	 *
	 * @return Running time in milliseconds.
	 */
	public int getRunningTime()
	{
		final long startTime = _startTime;

		long endTime = _endTime;
		if ( endTime < 0 )
		{
			endTime = System.nanoTime();
		}

		return ( ( startTime >= 0 ) && ( startTime < endTime ) ) ? (int)TimeUnit.NANOSECONDS.toMillis( endTime - startTime ) : 0;
	}

	/**
	 * Get speed.
	 *
	 * @return Speed (progress per second).
	 */
	public double getSpeed()
	{
		return (double)( getProgress() * 1000 ) / getRunningTime();
	}

	/**
	 * Ends the progress bar.
	 */
	public void end()
	{
		_endTime = System.nanoTime();

		if ( _value != -1 )
		{
			_out.println( " done." );
		}
	}
}
