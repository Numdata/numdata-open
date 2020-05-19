/*
 * Copyright (c) 2008-2020, Numdata BV, The Netherlands.
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

import org.jetbrains.annotations.*;

/**
 * This class implements {@link LogTarget} to log messages to a {@link
 * PrintStream}, which is the the standard error output stream by default.
 *
 * @author Peter S. Heijnen
 * @see ClassLogger
 * @see LogTarget
 */
public class ConsoleTarget
extends AbstractStreamTarget
{
	/**
	 * Name of system property through which the default log level can be set.
	 */
	public static final String LEVEL_SYSTEM_PROPERTY = "console.logger.level";

	/**
	 * Name of system property to enable logging to 'stdout' instead of
	 * 'stderr'.
	 */
	public static final String STDOUT_SYSTEM_PROPERTY = "console.logger.stdout";

	/**
	 * Print stream to write log messages to.
	 */
	@NotNull
	private final PrintStream _out;

	/**
	 * Construct logger with the default log level, that sends its output to the
	 * standard error channel.
	 *
	 * The default log level ({@link ClassLogger#INFO}) can be changed using the
	 * '{@code console.logger.level}' system property.
	 */
	public ConsoleTarget()
	{
		this( ClassLogger.INFO, getDefaultStream() );
	}

	/**
	 * Get default stream to use for output to console. This is normally {@link
	 * System#err stderr}, but if the {@code console.logger.stdout} system
	 * property is set to {@code true}, {@link System#out stdout} is used.
	 *
	 * @return Default stream to use for console output.
	 */
	@SuppressWarnings( { "AccessOfSystemProperties", "UseOfSystemOutOrSystemErr" } )
	protected static PrintStream getDefaultStream()
	{
		PrintStream result = System.err;
		try
		{
			if ( Boolean.getBoolean( STDOUT_SYSTEM_PROPERTY ) )
			{
				result = System.out;
			}
		}
		catch ( final SecurityException ignored )
		{
			/* ignore denied access to system property */
		}
		return result;
	}

	/**
	 * Construct logger with the default log level, that sends its output to the
	 * specified stream.
	 *
	 * The default log level ({@link ClassLogger#INFO}) can be changed using the
	 * '{@code console.logger.level}' system property.
	 *
	 * @param defaultLogLevel Default log level.
	 * @param out             Print stream to write log messages to.
	 */
	public ConsoleTarget( final int defaultLogLevel, @NotNull final PrintStream out )
	{
		super( defaultLogLevel, LEVEL_SYSTEM_PROPERTY );

		_out = out;
	}

	/**
	 * Construct logger with the given levels that sends its output to the
	 * specified stream.
	 *
	 * NOTE: This constructor does not support the
	 * '{@code console.logger.level}' system property.
	 *
	 * @param out             Print stream to write log messages to.
	 * @param defaultLogLevel Default log level.
	 * @param levels          Specifies log levels to be set (see {@link #setLevels(String)}).
	 */
	public ConsoleTarget( @NotNull final PrintStream out, final int defaultLogLevel, @NotNull final String levels )
	{
		super( defaultLogLevel, null );
		_out = out;
		setLevels( levels );
	}

	@NotNull
	@Override
	public PrintStream getLogStream()
	{
		return _out;
	}
}
