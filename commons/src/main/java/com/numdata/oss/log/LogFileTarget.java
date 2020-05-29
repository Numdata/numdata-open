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
import java.text.*;
import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class implements {@link LogTarget} to log messages to a file.
 *
 * @author Peter S. Heijnen
 * @see ClassLogger
 */
public final class LogFileTarget
extends AbstractStreamTarget
{
	/**
	 * Name of system property through which the file path can be set. A
	 * timestamp will be insterted into this path before any file extension that
	 * may be included in the path.
	 */
	public static final String PATH_SYSTEM_PROPERTY = "file.logger.path";

	/**
	 * Name of system property through which the default log level can be set.
	 */
	public static final String LEVEL_SYSTEM_PROPERTY = "file.logger.level";

	/**
	 * Name of system property through which the maximum log file size can be
	 * set. This value may contain a unit suffix (e.g. 'k', 'm' or 'g' to
	 * specify kilobytes, megabytes, gigabytes respectively). If this property
	 * is omitted, the log file size is not limited.
	 */
	public static final String MAXSIZE_SYSTEM_PROPERTY = "file.logger.maxsize";

	/**
	 * Format use to generate timestamp in path.
	 */
	private final SimpleDateFormat _logFileTimestampFormat = new SimpleDateFormat( "yyyyMMdd'T'HHmmss", Locale.US );

	/**
	 * Path for log files.
	 */
	private final String _path;

	/**
	 * Current log file.
	 */
	private File _file;

	/**
	 * Print stream to write log messages to.
	 */
	private PrintStream _stream;

	/**
	 * Maximum log file size. A new log file will be created when the log file
	 * size is equal or greater than this value. A negative value disabled this
	 * limit.
	 */
	private final long _maximumFileSize;

	/**
	 * Get default {@link LogFileTarget} if the {@link #PATH_SYSTEM_PROPERTY} is
	 * set.
	 *
	 * @return Default {@link LogFileTarget} instance; {@code null} if no
	 * default instance is available.
	 */
	@Nullable
	@SuppressWarnings( "AccessOfSystemProperties" )
	static LogFileTarget getDefaultInstance()
	{
		LogFileTarget result = null;

		try
		{
			final String path = System.getProperty( PATH_SYSTEM_PROPERTY );
			if ( TextTools.isNonEmpty( path ) )
			{
				final String maxSizeString = System.getProperty( MAXSIZE_SYSTEM_PROPERTY );
				final long maxsize = TextTools.isNonEmpty( maxSizeString ) ? parseByteAmount( maxSizeString ) : -1L;

				result = new LogFileTarget( path, maxsize );
			}
		}
		catch ( final SecurityException e )
		{
			/* ignore no access to system property */
		}
		catch ( final Throwable t )
		{
			/* ignore all other problems, but do print them on the console */
			t.printStackTrace();
		}

		return result;
	}

	/**
	 * Parse amount in bytes. The default unit is bytes, but other units may be
	 * specified using a suffix.
	 *
	 * @param value Value to parse.
	 *
	 * @return Amount in bytes.
	 *
	 * @throws NumberFormatException if the value could not be parsed.
	 */
	private static long parseByteAmount( final String value )
	{
		/* parse number */
		final NumberFormat numberFormat = TextTools.getNumberFormat( Locale.US, 0, 0, false );

		final ParsePosition parsePosition = new ParsePosition( 0 );
		final Long number = (Long)numberFormat.parse( value, parsePosition );

		final int suffixPos = parsePosition.getIndex();
		if ( suffixPos == 0 )
		{
			throw new NumberFormatException( value );
		}

		/* handle suffix, default to bytes */
		long unit = 1L;
		if ( suffixPos < value.length() )
		{
			final String suffix = value.substring( suffixPos );

			if ( "k".equalsIgnoreCase( suffix ) )
			{
				unit = 1024L;
			}
			else if ( "m".equalsIgnoreCase( suffix ) )
			{
				unit = 1024L * 1024L;
			}
			else if ( "g".equalsIgnoreCase( suffix ) )
			{
				unit = 1024L * 1024L * 1024L;
			}
			else
			{
				throw new NumberFormatException( value );
			}
		}

		return number * unit;
	}

	/**
	 * Construct log file target.
	 *
	 * The log file path must be set using the '{@code file.logger.path}' system
	 * property.
	 *
	 * The default log level ({@link ClassLogger#INFO}) can be changed using the
	 * '{@code file.logger.level}' system property.
	 *
	 * @param path            Path to log file location.
	 * @param maximumFileSize Maximum log file size (negative = no limit).
	 */
	public LogFileTarget( final String path, final long maximumFileSize )
	{
		super( ClassLogger.INFO, LEVEL_SYSTEM_PROPERTY );

		_path = path;
		_maximumFileSize = maximumFileSize;
		_file = null;
		_stream = null;

		if ( TextTools.isEmpty( path ) )
		{
			setLevel( ClassLogger.NONE );
			throw new IllegalArgumentException( "empty path" );
		}
	}

	@Override
	public PrintStream getLogStream()
	{
		File file = _file;
		PrintStream stream = _stream;

		_stream = System.err; /* temporary setting to prevent re-entering */

		/*
		 * Close existing stream if the log file disappeared or the log file
		 * size exceeds the maximum.
		 */
		if ( ( stream != null ) && ( file != null ) && ( !file.exists() || ( ( _maximumFileSize > 0L ) && ( file.length() >= _maximumFileSize ) ) ) )
		{
			stream.close();
			file = null;
			stream = null;
		}

		/*
		 * Create log file and output stream on-demand
		 */
		if ( stream == null )
		{
			/*
			 * Output file output stream.
			 */
			final File logFile = getLogFile();
			try
			{
				stream = new PrintStream( logFile, "UTF-8" );
				file = logFile;
			}
			catch ( final Exception e )
			{
				/*
				 * If we can't create/open the log file, log this problem
				 * and reduce our log level to 'NONE'.
				 */
				final ClassLogger logger = ClassLogger.getFor( getClass() );
				logger.error( "Failed to create/open log file: " + file, e );

				setLevel( ClassLogger.NONE );
			}
		}

		/*
		 * Set stream to the system's standard error output stream. This is
		 * done to prevent log file creation retries (which will most likely
		 * fail anyway).
		 */
		if ( stream == null )
		{
			file = null;
			stream = System.err;
		}

		_file = file;
		_stream = stream;

		return stream;
	}

	/**
	 * Get log file. This inserts '-yyMMddHHmmss' in the configured path before
	 * the filename extension or at the end of the configured path if it does
	 * ont contain a filename extension.
	 *
	 * @return Log file.
	 */
	private File getLogFile()
	{
		String path = _path;
		final String timestamp = _logFileTimestampFormat.format( new Date() );

		final int dot = path.lastIndexOf( (int)'.' );
		if ( dot > Math.max( path.lastIndexOf( (int)'/' ), path.lastIndexOf( (int)'\\' ) ) )
		{
			path = path.substring( 0, dot ) + '-' + timestamp + path.substring( dot );
		}
		else
		{
			path = path + '-' + timestamp;
		}

		return new File( path );
	}
}
