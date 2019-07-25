/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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

/**
 * Surrogate for 'java.util.Locale'.
 *
 * @author Gerrit Meinders
 */
export default class Locale
{
	/**
	 * Constant for language.
	 */
	static ENGLISH = new Locale( "en" );

	/**
	 * Constant for language.
	 */
	static FRENCH = new Locale( "fr" );

	/**
	 * Constant for language.
	 */
	static GERMAN = new Locale( "de" );

	/**
	 * Constant for language.
	 */
	static ITALIAN = new Locale( "it" );

	/**
	 * Constant for language.
	 */
	static JAPANESE = new Locale( "ja" );

	/**
	 * Constant for language.
	 */
	static KOREAN = new Locale( "ko" );

	/**
	 * Constant for language.
	 */
	static CHINESE = new Locale( "zh" );

	/**
	 * Constant for language.
	 */
	static SIMPLIFIED_CHINESE = new Locale( "zh", "CN" );

	/**
	 * Constant for language.
	 */
	static TRADITIONAL_CHINESE = new Locale( "zh", "TW" );

	/**
	 * Constant for country.
	 */
	static FRANCE = new Locale( "fr", "FR" );

	/**
	 * Constant for country.
	 */
	static GERMANY = new Locale( "de", "DE" );

	/**
	 * Constant for country.
	 */
	static ITALY = new Locale( "it", "IT" );

	/**
	 * Constant for country.
	 */
	static JAPAN = new Locale( "ja", "JP" );

	/**
	 * Constant for country.
	 */
	static KOREA = new Locale( "ko", "KR" );

	/**
	 * Constant for country.
	 */
	static CHINA = Locale.SIMPLIFIED_CHINESE;

	/**
	 * Constant for country.
	 */
	static PRC = Locale.SIMPLIFIED_CHINESE;

	/**
	 * Constant for country.
	 */
	static TAIWAN = Locale.TRADITIONAL_CHINESE;

	/**
	 * Constant for country.
	 */
	static UK = new Locale( "en", "GB" );

	/**
	 * Constant for country.
	 */
	static US = new Locale( "en", "US" );

	/**
	 * Constant for country.
	 */
	static CANADA = new Locale( "en", "CA" );

	/**
	 * Constant for country.
	 */
	static CANADA_FRENCH = new Locale( "fr", "CA" );

	/**
	 * Constant for the root locale.
	 */
	static ROOT = new Locale();

	/**
	 * @param [language] An ISO 639 alpha-2 language code.
	 * @param [country] An ISO 3166 alpha-2 country code.
	 * @param [variant] Any arbitrary value used to indicate a variation.
	 */
	constructor( language, country, variant )
	{
		this.language = language || '';
		this.country = country || '';
		this.variant = variant || '';
	}

	toString()
	{
		let result = this.language;
		if ( this.country || this.variant )
		{
			result += '_' + this.country;
			if ( this.variant )
			{
				result += '_' + this.variant;
			}
		}
		return result;
	}

	/**
	 * Returns the default locale.
	 *
	 * @returns {Locale} Default locale.
	 */
	static getDefault()
	{
		return Locale.ENGLISH;
	}
}
