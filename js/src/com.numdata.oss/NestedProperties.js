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
 * Implements external 'NestedProperties' for ROM.
 *
 * @author  Gerrit Meinders
 */
export default class NestedProperties {
	/**
	 * Properties.
	 *
	 * @type {{}}
	 * @private
	 */
	_values = {};

	/**
	 * Constructs a new instance.
	 *
	 * @param {NestedProperties} [original] Original properties to copy.
	 */
	constructor( original )
	{
		if ( original )
		{
			this.copyFrom( original );
		}
	}

	/**
	 * Copy properties from other {@code NestedProperties}.
	 *
	 * @param {NestedProperties} other Nested properties to copy.
	 */
	copyFrom( other )
	{
		let target = this._values;
		let source = other._values;
		for ( let property in source )
		{
			if ( source.hasOwnProperty( property ) )
			{
				let value = source[ property ];
				if ( value instanceof NestedProperties )
				{
					value = new NestedProperties( value );
				}
				target[ property ] = value;
			}
		}
	}

	/**
	 * Get boolean property with the specified name.
	 *
	 * @param {string} name Property name.
	 * @param {boolean} [defaultValue] Default value.
	 *
	 * @return {boolean} Boolean value.
	 */
	getBoolean( name, defaultValue )
	{
		return this._values.hasOwnProperty( name ) ? !!this._values[ name ] : defaultValue;
	}

	/**
	 * Get float property with the specified name.
	 *
	 * @param {string} name Property name.
	 * @param {number} [defaultValue] Default value.
	 *
	 * @return {number} Float value.
	 */
	getFloat( name, defaultValue )
	{
		return this._values.hasOwnProperty( name ) ? replaceNaN( Number.parseFloat( this._values[ name ] ), defaultValue ) : defaultValue;
	}

	/**
	 * Get int property with the specified name.
	 *
	 * @param {string} name Property name.
	 * @param {number} [defaultValue] Default value.
	 *
	 * @return {number} Int value.
	 */
	getInt( name, defaultValue )
	{
		return this._values.hasOwnProperty( name ) ? replaceNaN( Number.parseInt( this._values[ name ] ), defaultValue ) : defaultValue;
	}

	/**
	 * Get nested properties with the specified name.
	 *
	 * @param {string} name Property name.
	 * @param {boolean} createIfNecessary Create nested properties if not present; return {@code null} otherwise.
	 *
	 * @return {?NestedProperties} Nested properties.
	 */
	getNested( name, createIfNecessary )
	{
		return this._values[ name ] || createIfNecessary ? {} : null;
	}

	/**
	 * Get string property with the specified name.
	 *
	 * @param {string} name Property name.
	 *
	 * @return {string} Property value.
	 */
	getProperty( name )
	{
		const value = this._values[ name ];
		return typeof value === 'string' ? value : null;
	}

	/**
	 * Sets property with the specified name, or clear and set all properties
	 * from the given nested properties.
	 *
	 * @param {NestedProperties|string} nameOrProperties Property name; or nested properties to copy.
	 * @param {*} [propertyValue] Value to set.
	 */
	set( nameOrProperties, propertyValue )
	{
		if ( typeof nameOrProperties === 'string' )
		{
			this._values[ nameOrProperties ] = propertyValue;
		}
		else if ( nameOrProperties instanceof NestedProperties )
		{
			this.clear();
			this.copyFrom( nameOrProperties );
		}
		else
		{
			throw new TypeError( nameOrProperties );
		}
	}

	clear()
	{
		this._values = {};
	}

	/**
	 * Returns whether the given properties are equal to this.
	 *
	 * @param {NestedProperties} other Properties to compare with.
	 *
	 * @return {boolean} Whether both objects are equal.
	 */
	equals( other )
	{
		let result = other instanceof NestedProperties;

		if ( result )
		{
			let thisValues = this._values;
			let otherValues = other._values;

			for ( let property in thisValues )
			{
				if ( thisValues.hasOwnProperty( property ) )
				{
					let thisValue = thisValues[ property ];
					let otherValue = otherValues[ property ];

					if ( thisValue instanceof NestedProperties )
					{
						result = thisValue.equals( otherValue );
					}
					else
					{
						result = thisValue === otherValue;
					}

					if ( !result )
					{
						break;
					}
				}
			}
		}

		return result;
	}
}

function replaceNaN( maybeNaN, replacement )
{
	return Number.isNaN( maybeNaN ) ? replacement : maybeNaN;
}
