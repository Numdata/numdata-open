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

/**
 * This class provides a localized string value.
 *
 * @author Peter S. Heijnen
 */
export default class LocalizedString
{
	/**
	 * Map's locale to string value.
	 * @type {Object.<string, string>}
	 */
	_values = {};

	/**
	 * Construct localized string based on the given (localized) string.
	 *
	 * @param {string|LocalizedString} [original] (Localized) string to copy.
	 */
	constructor( original )
	{
		if ( original )
		{
			this.set( original );
		}
	}

	/**
	 * Creates a {@link LocalizedString} using a vararg. The vararg part may not
	 * be empty and should always contain an even number of elements. Each
	 * variable entry consists of a locale name followed by a string value for
	 * that locale. Example use:
	 * <pre>
	 * create( "Dog" , "de" , "Hund" , "nl" , "Hond" )
	 * </pre>
	 *
	 * @param {string} defaultString String to set as default.
	 * @param {...string} localeSpecificStrings Pairs of locale names and string values.
	 *
	 * @return {LocalizedString} Created localized string.
	 */
	static create( defaultString, localeSpecificStrings )
	{
		let result = new LocalizedString( defaultString );

		if ( localeSpecificStrings )
		{
			if ( ( localeSpecificStrings.length % 2 ) != 0 )
			{
				throw new TypeError( "Bad number of arguments: " + ( 1 + localeSpecificStrings.length ) );
			}

			for ( let i = 0; i < localeSpecificStrings.length; i += 2 )
			{
				const locale = localeSpecificStrings[ i ];
				if ( !locale )
				{
					throw new TypeError( i + ": " + locale );
				}

				const string = localeSpecificStrings[ i + 1 ];
				if ( !string )
				{
					throw new TypeError( ( i + 1 ) + ": " + string );
				}

				result.set( string, locale );
			}
		}

		return result;
	}

	/**
	 * Clear all values.
	 */
	clear()
	{
		this._values = {};
	}

	/**
	 * Get string for the specified locale.
	 *
	 * @param {string} [locale]       Locale to get string for; {@code null} for any.
	 * @param {string} [defaultValue] Value to return if no string is available.
	 *
	 * @return {?string} String for the specified locale; {@code defaultValue} if no suitable string was found.
	 */
	get( locale, defaultValue )
	{
		let result;

		let key = ( locale == null ) ? "" : locale;
		while ( true ) // eslint-disable-line no-constant-condition
		{
			result = this.getSpecific( key );
			if ( ( result != null ) || !key )
			{
				break;
			}

			let end = key.lastIndexOf( '_' );
			key = ( end < 0 ) ? "" : key.substring( 0, end );
		}

		if ( ( result == null ) && ( locale == null ) )
		{
			result = this.getSpecific( "en" );

			if ( result == null )
			{
				let names = Object.keys( this._values );
				if ( names.length )
				{
					result = this.getSpecific( names[ 0 ] );
				}
			}
		}

		if ( result == null )
		{
			result = defaultValue;
		}

		return result;
	}

	/**
	 * Get string for the specified locale and only that locale.
	 *
	 * @param {string} [locale] Locale to get string for; {@code null} for any.
	 *
	 * @return {?string} String for the specified locale; {@code null} if no entry was
	 *         found.
	 */
	getSpecific( locale )
	{
		return this._values[ ( locale == null ) ? "" : locale ];
	}

	/**
	 * Set string for the specified locale or copy all entries from another
	 * localized string.
	 *
	 * @param {LocalizedString|string} value (Localized) string to set.
	 * @param {string} [locale] Locale to set string for; {@code null} or empty string to set the default.
	 */
	set( value, locale )
	{
		if ( value instanceof LocalizedString )
		{
			this._values = Object.assign( {}, value._values );
		}
		else if ( locale && !locale.match( /^[a-z][a-z](_[A-Z][A-Z](_.*)?)?/ ) )
		{
			throw new TypeError( 'Invalid locale: ' + locale );
		}
		else
		{
			this._values[ ( locale == null ) ? "" : locale ] = value;
		}
	}

	/**
	 * Sets all values based on the given source.
	 *
	 * @param {LocalizedString|{}} source
	 */
	setAll( source )
	{
		this._values = Object.assign( {}, source instanceof LocalizedString ? source._values : source );
	}

	/**
	 * Test whether at least one string value is available.
	 *
	 * @return {boolean} {@code true} if no string values are available; {@code false} if at
	 *         least one string value is available.
	 */
	isEmpty()
	{
		return Object.keys( this._values ).length == 0;
	}

	/**
	 * Remove string for the specified locale.
	 *
	 * @param {string} [locale] Locale to remove string for; {@code null} or empty string to remove the default.
	 */
	remove( locale )
	{
		delete this._values[ ( locale == null ) ? "" : locale ];
	}

	/**
	 * Returns whether the given localized string is equal to this.
	 *
	 * @param {*} other Localized string to compare with.
	 *
	 * @return {boolean} true if the localized strings are equal.
	 */
	equals( other )
	{
		let result = false;
		if ( other == this )
		{
			result = true;
		}
		else if ( other instanceof LocalizedString )
		{
			var keys = Object.keys( this._values );
			result = ( keys.length == Object.keys( other._values ).length );
			if ( result )
			{
				for ( let key of keys )
				{
					if ( this._values[ key ] != other._values[ key ] )
					{
						result = false;
						break;
					}
				}
			}
		}
		return result;
	}
}
