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
 * This class extends {@link AbstractLocalizableString} with caching of
 * previously localized results.
 *
 * @author Peter S. Heijnen
 */
public abstract class AbstractCachingLocalizableString
extends AbstractLocalizableString
{
	/**
	 * Cached results.
	 */
	private final Map<String, String> _cache = new HashMap<String, String>();

	@Override
	@Nullable
	public String get( @Nullable final Locale locale )
	{
		final Locale actualLocale = ( locale == null ) ? Locale.ROOT : locale;

		String result;

		synchronized ( _cache )
		{
			final String cacheKey = actualLocale.toString();
			result = _cache.get( cacheKey );
			if ( ( result == null ) && !_cache.containsKey( cacheKey ) )
			{
				result = getNewString( actualLocale );
				_cache.put( cacheKey, result );
			}
		}

		return result;
	}

	/**
	 * Clears cache.
	 */
	public void clearCache()
	{
		synchronized ( _cache )
		{
			_cache.clear();
		}
	}

	/**
	 * Get new localized string for the given locale. This method is called when
	 * no cached result is available.
	 *
	 * @param locale Locale to be used.
	 *
	 * @return String for the given locale.
	 */
	@Nullable
	protected abstract String getNewString( @NotNull final Locale locale );
}
