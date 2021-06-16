/*
 * Copyright (c) 2017-2021, Unicon Creation BV, The Netherlands.
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
export default class NestedProperties
{
	/**
	 * Properties.
	 */
	private _values: { [ key: string ]: any } = {};

	/**
	 * Constructs a new instance.
	 *
	 * @param [original] Original properties to copy.
	 */
	constructor( original?: NestedProperties )
	{
		if ( original )
		{
			this.copyFrom( original );
		}
	}

	/**
	 * Copy properties from other {@code NestedProperties}.
	 *
	 * @param other Nested properties to copy.
	 */
	copyFrom( other: NestedProperties ): void
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
	 * @param name Property name.
	 * @param [defaultValue] Default value.
	 *
	 * @return Boolean value.
	 */
	getBoolean( name: string, defaultValue = false ): boolean
	{
		let result;
		const value = this._values.hasOwnProperty( name ) ? this._values[ name ] : undefined;
		if ( ( value === null ) || ( value === undefined ) )
		{
			result = defaultValue;
		}
		else if ( typeof value === 'boolean' )
		{
			result = value;
		}
		else if ( typeof value === 'number' )
		{
			result = value > 0;
		}
		else if ( typeof value === 'string' )
		{
			const lower = value.toLowerCase();
			result = ( "true" === lower )
			         || ( "1" === value )
			         || ( "y" === lower )
			         || ( "yes" === lower );
		}
		else
		{
			throw new TypeError( `${name}=${value}` );
		}
		return result;
	}

	/**
	 * Get float property with the specified name.
	 *
	 * @param name Property name.
	 * @param [defaultValue] Default value.
	 *
	 * @return Float value.
	 */
	getFloat( name: string, defaultValue = 0 ): number
	{
		let result;
		const value = this._values.hasOwnProperty( name ) ? this._values[ name ] : undefined;
		if ( ( value === null ) || ( value === undefined ) )
		{
			result = defaultValue;
		}
		else if ( typeof value === 'number' )
		{
			result = value;
		}
		else if ( typeof value === 'string' )
		{
			result = Number.parseFloat( value );
			if ( Number.isNaN( result ) && ( value !== 'NaN' ) )
			{
				throw new TypeError( `${name}=${value}` );
			}
		}
		else
		{
			throw new TypeError( `${name}=${value}` );
		}
		return result;
	}

	/**
	 * Get int property with the specified name.
	 *
	 * @param name Property name.
	 * @param [defaultValue] Default value.
	 *
	 * @return Int value.
	 */
	getInt( name: string, defaultValue = 0 ): number
	{
		let result;
		const value = this._values.hasOwnProperty( name ) ? this._values[ name ] : undefined;
		if ( ( value === null ) || ( value === undefined ) )
		{
			result = defaultValue;
		}
		else if ( typeof value === 'number' )
		{
			result = Math.trunc( value );
		}
		else if ( typeof value === 'string' )
		{
			result = Number.parseInt( value );
			if ( Number.isNaN( result ) || ( String( Math.trunc( result ) ) !== value ) ) // Strict integer parsing
			{
				throw new TypeError( `${name}=${value}` );
			}
		}
		else
		{
			throw new TypeError( `${name}=${value}` );
		}
		return result;
	}

	/**
	 * Get nested properties with the specified name.
	 *
	 * @param name Property name.
	 * @param createIfNecessary Create nested properties if not present; return {@code null} otherwise.
	 *
	 * @return Nested properties.
	 */
	getNested( name: string, createIfNecessary = false ): NestedProperties | null
	{
		let result;
		const value = this._values.hasOwnProperty( name ) ? this._values[ name ] : undefined;
		if ( ( value === null ) || ( value === undefined ) )
		{
			result = createIfNecessary ? new NestedProperties() : null;
		}
		else if ( value instanceof NestedProperties )
		{
			result = value;
		}
		else if ( typeof value === 'string' )
		{
			throw new Error( "not implemented" );
		}
		else
		{
			throw new TypeError( `${name}=${value}` );
		}
		return result;
	}

	/**
	 * Get string property with the specified name.
	 *
	 * @param name Property name.
	 *
	 * @return Property value.
	 */
	getProperty( name: string, defaultValue: string = null ): string
	{
		const value = this._values[ name ];
		return ( ( value === null ) || ( value === undefined ) ) ? defaultValue : String( value );
	}

	/**
	 * Clear and set all properties from the given nested properties.
	 *
	 * @param properties Nested properties to copy.
	 */
	set( properties: NestedProperties ): void;

	/**
	 * Sets property with the specified name, or clear and set all properties
	 * from the given nested properties.
	 *
	 * @param name Property name; or nested properties to copy.
	 * @param propertyValue Value to set.
	 */
	set( name: string, propertyValue: any ): void;

	set( nameOrProperties: NestedProperties | string, propertyValue?: any )
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
			throw new TypeError( 'invalid argument: ' + nameOrProperties );
		}
	}

	clear()
	{
		this._values = {};
	}

	/**
	 * Returns whether the given properties are equal to this.
	 *
	 * @param other Properties to compare with.
	 *
	 * @return Whether both objects are equal.
	 */
	equals( other: NestedProperties ): boolean
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
