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
import java.util.regex.*;

import org.jetbrains.annotations.*;

/**
 * Support for glob patterns.
 *
 * <dl>
 *
 * <dt>*</dt><dd>Matches any substring.</dd>
 *
 * <dt>?</dt><dd>Matches exactly one character.</dd>
 *
 * <dt>|</dt><dd>Separates multiple patterns.</dd>
 *
 * </dl>
 *
 * @author Peter S. Heijnen
 */
public class Glob
{
	/**
	 * Cache for {@link #compilePattern(String)}.
	 */
	private final Map<String, Pattern> _patternCache = new HashMap<String, Pattern>();

	/**
	 * Match string value against glob pattern.
	 *
	 * An empty value or empty pattern always matches.
	 *
	 * @param value   String value.
	 * @param pattern Glob pattern.
	 *
	 * @return {@code true} if value matches pattern.
	 */
	public boolean matchPattern( @Nullable final String value, @Nullable final String pattern )
	{
		boolean result = true;

		if ( ( value != null ) && !value.isEmpty() )
		{
			final Pattern compiled = compilePattern( pattern );
			if ( compiled != null )
			{
				result = compiled.matcher( value ).matches();
			}
		}

		return result;
	}

	/**
	 * Compile glob pattern to {@link Pattern regular expression}.
	 *
	 * To speed up processing, results are cached.
	 *
	 * @param glob Glob pattern.
	 *
	 * @return {@link Pattern Regular expression}; {@code null} if {@code glob}
	 * is empty.
	 */
	@Nullable
	private Pattern compilePattern( @Nullable final String glob )
	{
		Pattern result;

		if ( ( glob == null ) || glob.isEmpty() )
		{
			result = null;
		}
		else
		{
			result = _patternCache.get( glob );
			if ( result == null )
			{
				final int len = glob.length();
				final StringBuilder sb = new StringBuilder( glob.length() * 11 / 10 ); // reserve 10% capacity for escaping etc

				int pos = 0;
				while ( pos < len )
				{
					final char ch = glob.charAt( pos++ );
					switch ( ch )
					{
						case '?':
							sb.append( '.' );
							break;

						case '*':
							sb.append( '.' ).append( ch );
							break;

						case '\\':
							if ( pos < len )
							{
								sb.append( ch ).append( glob.charAt( pos++ ) );
							}
							break;

						case '.':
						case '$':
						case '(':
						case ')':
						case '+':
						case '[':
						case ']':
							sb.append( '\\' ).append( ch );
							break;

						default:
							sb.append( ch );
					}
				}

				result = Pattern.compile( sb.toString() );
				_patternCache.put( glob, result );
			}
		}
		return result;
	}
}
