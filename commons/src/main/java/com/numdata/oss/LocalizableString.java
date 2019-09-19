/*
 * Copyright (c) 2010-2017, Numdata BV, The Netherlands.
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
 * This interface provides a localizable string.
 *
 * @author Peter S. Heijnen
 */
public interface LocalizableString
{
	/**
	 * Get string for a given locale.
	 *
	 * @param locale Locale to get string for ({@code null} => any).
	 *
	 * @return String for the specified or a fallback locale; {@code null} if no
	 * string value is available.
	 */
	@Nullable
	String get( @Nullable Locale locale );

	/**
	 * Get string for the specified locale.
	 *
	 * @param locale Locale to get string for ({@code null} => any).
	 *
	 * @return String for the specified or a fallback locale; {@code null} if no
	 * string value is available.
	 */
	@Nullable
	String get( @Nullable String locale );

	/**
	 * Returns a localizable string that is the concatenation of this string and
	 * the given string.
	 *
	 * @param s String.
	 *
	 * @return Concatenation of the strings.
	 */
	LocalizableString concat( String s );

	/**
	 * Returns a localizable string that is the concatenation of this string and
	 * the given string.
	 *
	 * @param string String.
	 *
	 * @return Concatenation of the strings.
	 */
	LocalizableString concat( LocalizableString string );
}
