/*
 * Copyright (c) 2020-2020, Numdata BV, The Netherlands.
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
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * {@link LogTarget} implementation for testing log output of a specific class.
 *
 * <p>Log output is send to the console and recorded for testing purposes.
 *
 * <p>This may typically be used in unit tests. JUnit example:
 * <pre>
 * LogTestTarget log = new LogTestTarget( MyClass.class );
 * try
 * {
 *     ClassLogger.addTarget( log );
 *
 *     .... do something involving MyClass
 *
 *     assertTrue( "Should have logged 'expected message'", log.getMessages().contains( "expected message" ) );
 * }
 * finally
 * {
 *     ClassLogger.removeTarget( log );
 * }
 * </pre>
 *
 * @author Peter S. Heijnen
 */
public class LogTestTarget
implements LogTarget
{
	/**
	 * Class to monitor.
	 */
	@NotNull
	private final Class<?> _clazz;

	/**
	 * Use this stream for console output (default: {@link System#out}).
	 */
	@SuppressWarnings( "UseOfSystemOutOrSystemErr" )
	private final PrintStream _console = System.out;

	/**
	 * Indent to use for console output.
	 */
	@NotNull
	private String _indent = "";

	/**
	 * Recorded logs.
	 */
	@NotNull
	private final List<LogMessage> _logs = new ArrayList<LogMessage>();

	/**
	 * Create instance.
	 *
	 * @param clazz Class to monitor.
	 */
	public LogTestTarget( @NotNull final Class<?> clazz )
	{
		_clazz = clazz;
	}

	@Override
	public void log( final String name, final int level, final String message, final Throwable throwable, final String threadName )
	{
		if ( isLevelEnabled( name, level ) )
		{
			_console.append( _indent ).append( ClassLogger.getLevelName( level ) ).append( ": " ).println( message );
			if ( throwable != null )
			{
				throwable.printStackTrace( _console );
			}
			_logs.add( new LogMessage( name, level, message, throwable, threadName ) );
		}
	}

	@Override
	public boolean isLevelEnabled( final String name, final int level )
	{
		return _clazz.getName().equals( name );
	}

	@NotNull
	public String getIndent()
	{
		return _indent;
	}

	public void setIndent( @NotNull final String indent )
	{
		_indent = indent;
	}

	/**
	 * Clears recorded logs.
	 */
	public void clear()
	{
		_logs.clear();
	}

	@NotNull
	public List<LogMessage> getLogs()
	{
		return _logs;
	}

	/**
	 * Returns list of recorded log messages.
	 *
	 * @return List of recorded log messages.
	 */
	@NotNull
	public List<String> getMessages()
	{
		final List<LogMessage> logMessages = getLogs();
		final List<String> result = new ArrayList<String>( logMessages.size() );
		for ( final LogMessage logMessage : logMessages )
		{
			result.add( logMessage.message );
		}
		return result;
	}
}
