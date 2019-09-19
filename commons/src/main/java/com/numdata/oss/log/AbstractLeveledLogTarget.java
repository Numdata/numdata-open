/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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

import java.util.*;
import java.util.regex.*;

/**
 * Abstract implementation of the {@link LogTarget} implementation, that
 * provides a log level setting to determine what log levels are
 * enabled/logged.
 *
 * @author Peter S. Heijnen
 * @see LogTarget
 */
public abstract class AbstractLeveledLogTarget
implements LogTarget
{
	/**
	 * Log level ({@link ClassLogger#FATAL}, {@link ClassLogger#ERROR}, {@link
	 * ClassLogger#WARN}, {@link ClassLogger#INFO}, {@link ClassLogger#DEBUG},
	 * {@link ClassLogger#TRACE}, {@link ClassLogger#NONE}, or {@link
	 * ClassLogger#ALL}).
	 */
	private int _level;

	/**
	 * Log level for logs matched by name according to some pattern.
	 */
	private final List<LogLevelPattern> _levelPatterns;

	/**
	 * Cached log level by log name, for log names that were previously matched
	 * against the {@link #_levelPatterns}.
	 */
	private final Map<String, Integer> _levelByName;

	/**
	 * Construct logger with the specifiedl default log level or the a log level
	 * that is set using the specified system property.
	 *
	 * @param defaultLogLevel     Default log level.
	 * @param levelSystemProperty System property through which the default log
	 *                            level may be changed.
	 */
	protected AbstractLeveledLogTarget( final int defaultLogLevel, final String levelSystemProperty )
	{
		_level = defaultLogLevel;
		_levelPatterns = new ArrayList<LogLevelPattern>();
		_levelByName = new HashMap<String, Integer>();

		if ( levelSystemProperty != null )
		{
			try
			{
				final String levelOption = System.getProperty( levelSystemProperty );
				if ( levelOption != null )
				{
					setLevels( levelOption );
				}
			}
			catch ( SecurityException e )
			{
				/* ignore no access to system property */
			}
		}
	}

	/**
	 * Get log level.
	 *
	 * @return Log level ({@link ClassLogger#FATAL}, {@link ClassLogger#ERROR},
	 * {@link ClassLogger#WARN}, {@link ClassLogger#INFO}, {@link
	 * ClassLogger#DEBUG}, {@link ClassLogger#TRACE}, {@link ClassLogger#NONE},
	 * or {@link ClassLogger#ALL}).
	 */
	public int getLevel()
	{
		return _level;
	}

	/**
	 * Set log level.
	 *
	 * @param level Log level ({@link ClassLogger#FATAL}, {@link
	 *              ClassLogger#ERROR}, {@link ClassLogger#WARN}, {@link
	 *              ClassLogger#INFO}, {@link ClassLogger#DEBUG}, {@link
	 *              ClassLogger#TRACE}, {@link ClassLogger#NONE}, or {@link
	 *              ClassLogger#ALL}).
	 */
	public void setLevel( final int level )
	{
		_level = level;
		_levelByName.clear();
	}

	/**
	 * Set log levels from a comma separated list, containing a log level for
	 * one or more wildcard patterns. The pattern may be omitted to set the
	 * default log level. If a log matches multiple patterns, the last pattern
	 * applies. For example: {@code INFO,my.package.*=DEBUG,*.critical.*=TRACE}.
	 *
	 * @param levels Specifies log levels to be set.
	 */
	public void setLevels( final String levels )
	{
		_levelPatterns.clear();

		final String[] levelEntries = levels.split( "," );
		for ( final String levelEntry : levelEntries )
		{
			final int separatorIndex = levelEntry.indexOf( '=' );
			if ( separatorIndex == -1 )
			{
				try
				{
					setLevel( ClassLogger.parseLevel( levelEntry ) );
				}
				catch ( Exception e )
				{
					/* ignore bad strings */
				}
			}
			else
			{
				final String pattern = levelEntry.substring( 0, separatorIndex );
				final String patternLevel = levelEntry.substring( separatorIndex + 1 );
				try
				{
					setLevel( pattern, ClassLogger.parseLevel( patternLevel ) );
				}
				catch ( Exception e )
				{
					/* ignore bad strings */
				}
			}
		}
	}

	/**
	 * Set log level from string (any incorrect string is silently ignored).
	 *
	 * @param level Log level as string.
	 */
	public void setLevel( final String level )
	{
		try
		{
			setLevel( ClassLogger.parseLevel( level ) );
		}
		catch ( Exception e )
		{
			/* ignore bad strings */
		}
	}

	/**
	 * Set log level from string (any incorrect string is silently ignored).
	 *
	 * @param pattern Wildcard pattern matching the names of logs to set level
	 *                of.
	 * @param level   Log level as string.
	 */
	public void setLevel( final String pattern, final int level )
	{
		_levelPatterns.add( new LogLevelPattern( pattern, level ) );
		_levelByName.clear();
	}

	public boolean isLevelEnabled( final String name, final int level )
	{
		final boolean result;

		if ( _levelPatterns.isEmpty() )
		{
			result = ( level <= _level );
		}
		else
		{
			Integer enabledLevel = _levelByName.get( name );

			if ( enabledLevel == null )
			{
				/*
				 * Match against patterns. The last pattern to match determines the
				 * actual level.
				 */
				for ( final ListIterator<LogLevelPattern> i = _levelPatterns.listIterator( _levelPatterns.size() ); i.hasPrevious(); )
				{
					final LogLevelPattern levelPattern = i.previous();
					if ( levelPattern.matches( name ) )
					{
						enabledLevel = Integer.valueOf( levelPattern.getLevel() );
						break;
					}
				}

				/*
				 * If no pattern matched, use the default log level.
				 */
				if ( enabledLevel == null )
				{
					enabledLevel = Integer.valueOf( _level );
				}

				/*
				 * Cache the resulting log level.
				 */
				_levelByName.put( name, enabledLevel );
			}

			result = ( level <= enabledLevel );
		}

		return result;
	}

	/**
	 * Defines a log level for all logs with a name matching a given pattern.
	 */
	private static class LogLevelPattern
	{
		/**
		 * Pattern to be matched against the log name.
		 */
		private final Pattern _pattern;

		/**
		 * Level to be used for matching logs.
		 */
		private final int _level;

		/**
		 * Constructs a log level definition for the given pattern.
		 *
		 * @param pattern Wildcard pattern.
		 * @param level   Label for logs matching the given pattern.
		 */
		private LogLevelPattern( final String pattern, final int level )
		{
			final StringBuffer patternRegex = new StringBuffer();

			/*
			 * Quote the given pattern and then replace all wildcards with the
			 * appropriate regular expression.
			 */
			final Pattern wildcardPattern = Pattern.compile( "[*?]" );
			final Matcher matcher = wildcardPattern.matcher( Pattern.quote( pattern ) );
			while ( matcher.find() )
			{
				final String match = matcher.group();
				if ( match.charAt( 0 ) == '*' )
				{
					matcher.appendReplacement( patternRegex, "\\\\E.*\\\\Q" );
				}
				else
				{
					matcher.appendReplacement( patternRegex, "\\\\E.\\\\Q" );
				}
			}
			matcher.appendTail( patternRegex );

			_pattern = Pattern.compile( patternRegex.toString() );
			_level = level;
		}

		/**
		 * Returns whether the given log name matches this pattern.
		 *
		 * @param name Log name.
		 *
		 * @return {@code true} if the log name matches.
		 */
		public boolean matches( final String name )
		{
			final boolean result;

			if ( name != null )
			{
				final Matcher matcher = _pattern.matcher( name );
				result = matcher.matches();
			}
			else
			{
				result = false;
			}

			return result;
		}

		/**
		 * Returns the log level for matching logs.
		 *
		 * @return Log level.
		 */
		public int getLevel()
		{
			return _level;
		}
	}
}
