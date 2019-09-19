/*
 * Copyright (c) 2013-2017, Numdata BV, The Netherlands.
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
 * This class provides a localized string value from a string map.
 *
 * @author Peter S. Heijnen
 */
public class StringMapLocalizedString
extends AbstractLocalizableString
{
	/**
	 * Class whose string map is used.
	 */
	private final Map<String, String> _stringMap;

	/**
	 * Construct localizable string map string.
	 */
	public StringMapLocalizedString()
	{
		_stringMap = Collections.emptyMap();
	}

	/**
	 * Construct localizable string map string.
	 *
	 * @param stringMap String map to get localized string values from.
	 */
	public StringMapLocalizedString( @NotNull final Map<String, String> stringMap )
	{
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_stringMap = stringMap;
	}

	@Override
	public String get( @Nullable final String locale )
	{
		return getValue( _stringMap, locale );
	}

	@Override
	@Nullable
	public String get( @Nullable final Locale locale )
	{
		return getValue( _stringMap, locale );
	}

	/**
	 * Get string for a given locale from string map.
	 *
	 * @param stringMap String map to get string value from.
	 * @param locale    Locale to get string for ({@code null} => any).
	 *
	 * @return String for the specified or a fallback locale; {@code null} if no
	 *         string value is available.
	 */
	@Nullable
	public static String getValue( @NotNull final Map<String, String> stringMap, @Nullable final Locale locale )
	{
		return getValue( stringMap, ( locale != null ) ? locale.toString() : null );
	}

	/**
	 * Get string for the specified locale from string map.
	 *
	 * @param stringMap String map to get string value from.
	 * @param locale    Locale to get string for ({@code null} => any).
	 *
	 * @return String for the specified or a fallback locale; {@code null} if no
	 *         string value is available.
	 */
	@Nullable
	public static String getValue( @NotNull final Map<String, String> stringMap, @Nullable final String locale )
	{
		String result = null;

		if ( !stringMap.isEmpty() )
		{
			String localeKey = ( locale == null ) ? "" : locale;
			while ( true )
			{
				result = stringMap.get( localeKey );
				if ( ( result != null ) || localeKey.isEmpty() )
				{
					break;
				}

				final int end = localeKey.lastIndexOf( '_' );
				localeKey = ( end < 0 ) ? "" : localeKey.substring( 0, end );
			}

			if ( result == null )
			{
				result = CollectionTools.getFirst( stringMap.values() );
			}
		}
		return result;
	}

}
