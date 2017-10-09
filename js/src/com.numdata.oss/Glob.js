/*
 * Copyright (c) 2017-2017 Numdata BV.  All rights reserved.
 *
 * Numdata Open Source Software License, Version 1.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Numdata BV (http://www.numdata.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Numdata" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission of Numdata BV. For written permission, please contact
 *    info@numdata.com.
 *
 * 5. Products derived from this software may not be called "Numdata",
 *    nor may "Numdata" appear in their name, without prior written
 *    permission of Numdata BV.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE NUMDATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Support for glob patterns.
 *
 * <dl>
 *
 * <dt>*</dt>
 * <dd>Matches any substring.</dd>
 *
 * <dt>?</dt>
 * <dd>Matches exactly one character.</dd>
 *
 * <dt>|</dt>
 * <dd>Separates multiple patterns.</dd>
 *
 * </dl>
 *
 * @author Peter S. Heijnen
 */
export default class Glob {

	/**
	 * Cache for {@link #compilePattern(String)}.
	 * @type object
	 */
	_patternCache = {};

	/**
	 * Match string value against glob pattern.
	 *
	 * An empty value or empty pattern always matches.
	 *
	 * @param {string} value String value.
	 * @param {string} pattern Glob pattern.
	 *
	 * @return {boolean} {@code true} if value matches pattern.
	 */
	matchPattern( value, pattern )
	{
		let result = true;
		if ( value )
		{
			const compiled = this.compilePattern( pattern );
			if ( compiled )
			{
				result = compiled.test( value );
			}
		}
		return result;
	}

	/**
	 * Compile glob pattern to regular expression.
	 *
	 * To speed up processing, results are cached.
	 *
	 * @param {string} glob Glob pattern.
	 *
	 * @return {RegExp} Regular expression; {@code null} if {@code glob}
	 * is empty.
	 */
	compilePattern( glob )
	{
		let result = null;
		if ( glob )
		{
			result = this._patternCache[ glob ];
			if ( !result )
			{
				const len = glob.length;
				// reserve 10% capacity for escaping etc
				let pattern = '^';
				let pos = 0;
				while ( pos < len )
				{
					const ch = glob.charAt( pos++ );
					switch ( ch )
					{
						case '?':
							pattern += '.';
							break;
						case '*':
							pattern += '.' + ch;
							break;
						case '\\':
							if ( pos < len )
							{
								pattern += ch + glob.charAt( pos++ );
							}
							break;
						case '.':
						case '$':
						case '(':
						case ')':
						case '+':
						case '[':
						case ']':
							pattern += '\\' + ch;
							break;
						case '|':
							pattern += '$|^';
							break;
						default:
							pattern += ch;
					}
				}
				pattern += '$';
				result = new RegExp( pattern );
				this._patternCache[ glob ] = result;
			}
		}
		return result;
	}
}
