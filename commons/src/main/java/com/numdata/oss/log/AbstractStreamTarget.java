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
package com.numdata.oss.log;

import java.io.*;
import java.text.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Abstract implementation of the {@link LogTarget} implementation, that
 * provides logging to an {@link OutputStream} and a log level setting to
 * determine what log levels are enabled/logged.
 *
 * @author Peter S. Heijnen
 */
public abstract class AbstractStreamTarget
extends AbstractLeveledLogTarget
{
	/**
	 * Format to use timestamps.
	 */
	protected final DateFormat _timestampFormat;

	/**
	 * Construct logger with the specified default log level or the a log level
	 * that is set using the specified system property.
	 *
	 * @param defaultLogLevel     Default log level.
	 * @param levelSystemProperty System property through which the default log
	 *                            level may be changed.
	 */
	protected AbstractStreamTarget( final int defaultLogLevel, final String levelSystemProperty )
	{
		super( defaultLogLevel, levelSystemProperty );
		_timestampFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss.SSS" );
	}

	/**
	 * Get stream to which log messages are written.
	 *
	 * @return Stream to which log messages are written; {@code null} if no
	 * stream is available.
	 */
	protected abstract PrintStream getLogStream();

	@Override
	public void log( final String name, final int level, final String message, final Throwable throwable, final String threadName )
	{
		if ( ( ( ( message != null ) && !message.isEmpty() ) || ( throwable != null ) ) && isLevelEnabled( name, level ) )
		{
			final PrintStream out = getLogStream();
			if ( out != null )
			{
				final String threadNameNotNull = ( threadName == null ) ? "unknown" : threadName;
				try
				{
					write( out, name, message, throwable, threadNameNotNull );
				}
				catch ( final IOException e )
				{
					/*
					 * If we can't write the message to the log stream, fallback
					 * to the standard error stream.
					 */
					try
					{
						write( System.err, name, message, throwable, threadNameNotNull );
					}
					catch ( final IOException e2 )
					{
						/* should not happen on 'err', nothing to do about it */
					}

					out.close();
				}
			}
		}
	}

	/**
	 * Write log message to the specified stream.
	 *
	 * @param out        Stream to write output to.
	 * @param name       Name of log (e.g. class name).
	 * @param message    Log message.
	 * @param throwable  Throwable associated with log message.
	 * @param threadName Identifies the thread that produced the log message.
	 *
	 * @throws IOException if there was a problem writing the output.
	 */
	protected void write( final PrintStream out, final String name, @Nullable final String message, @Nullable final Throwable throwable, @NotNull final String threadName )
	throws IOException
	{
		final String timestamp = _timestampFormat.format( new Date() );
		final String text = String.valueOf( message );

		writeText( out, name, timestamp, threadName, null, null, text );

		for ( Throwable cause = throwable; cause != null; cause = cause.getCause() )
		{
			@SuppressWarnings ( "ObjectEquality" )
			final String firstLinePrefix = ( ( cause != throwable ) || !text.isEmpty() ) ? "Caused by: " : null;

			String throwableText = cause.toString();
			if ( throwableText == null )
			{
				final String localizedMessage = cause.getLocalizedMessage();
				throwableText = ( localizedMessage != null ) ? localizedMessage + ": " + cause.getClass().getName() : cause.getClass().getName();
			}

			writeText( out, name, timestamp, threadName, firstLinePrefix, null, throwableText );

			for ( final StackTraceElement element : cause.getStackTrace() )
			{
				writeText( out, name, timestamp, threadName, "\tat ", "\tat ", String.valueOf( element ) );
			}
		}

		out.flush();
	}

	/**
	 * Write log text to the specified stream. If the text contains multiple
	 * lines (separated by newline characters), each line is logged as a
	 * separate, prefixed, line.
	 *
	 * @param out                  Stream to write to.
	 * @param name                 Name of log (e.g. class name).
	 * @param timestamp            Message timestamp.
	 * @param threadName           Name of current thread.
	 * @param firstLinePrefix      Prefix to put in front of first text line.
	 * @param subsequentLinePrefix Prefix to put in front of subsequent text
	 *                             line(s).
	 * @param text                 Text to write.
	 */
	protected static void writeText( @NotNull final PrintStream out, @NotNull final String name, @NotNull final String timestamp, @NotNull final String threadName, @Nullable final String firstLinePrefix, @Nullable final String subsequentLinePrefix, @NotNull final String text )
	{
		String linePrefix = firstLinePrefix;
		int start = 0;
		while ( start < text.length() )
		{
			int end = text.indexOf( (int)'\n', start );
			if ( end < 0 )
			{
				end = text.length();
			}

			final String line = text.substring( start, end );
			start = end + 1;

			writeLinePrefix( out, name, timestamp, threadName );
			if ( linePrefix != null )
			{
				out.print( linePrefix );
			}

			out.print( line );
			out.println();

			linePrefix = subsequentLinePrefix;
		}
	}

	/**
	 * Write prefix of log line to the specified stream.
	 *
	 * @param out        Stream to write to.
	 * @param name       Name of log (e.g. class name).
	 * @param timestamp  Message timestamp.
	 * @param threadName Name of current thread.
	 */
	protected static void writeLinePrefix( final PrintStream out, final String name, final String timestamp, final String threadName )
	{
		out.print( timestamp );
		out.print( " (" );
		out.print( threadName );
		out.print( ") " );

		int pos = 0;
		int dot;
		while ( ( dot = name.indexOf( '.', pos ) ) > pos )
		{
			out.append( name.charAt( pos ) );
			out.append( '.' );
			pos = dot + 1;
		}
		out.append( name, pos, name.length() );

		out.print( ": " );
	}
}
