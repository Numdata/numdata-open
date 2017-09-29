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
package com.numdata.jnlp;

import java.util.*;
import java.util.regex.*;

import org.jetbrains.annotations.*;

/**
 * Tools for working with JNLP files.
 *
 * @author G. Meinders
 */
public class JnlpTools
{
	/**
	 * Parses a whitespace-separated list.
	 *
	 * @param   value   Value to be parsed.
	 *
	 * @return  Parsed list.
	 */
	@NotNull
	public static List<String> parseList( final String value )
	{
		final String[] split = value.split( "\\s+" );
		return ( split == null ) ? Collections.<String>emptyList() : Arrays.asList( split );
	}

	/**
	 * Parses a whitespace-separated list, where the whitespace may be escaped
	 * using '\'.
	 *
	 * @param   value   Value to be parsed.
	 *
	 * @return  Parsed list.
	 */
	@NotNull
	public static List<String> parseEscapedWhitespaceList( final String value )
	{
		final Pattern pattern = Pattern.compile( "(\\S|(\\\\.))+" );
		final Matcher matcher = pattern.matcher( value );
		final List<String> result = new ArrayList<String>();
		while ( matcher.find() )
		{
			result.add( matcher.group() );
		}
		return result;
	}
}
