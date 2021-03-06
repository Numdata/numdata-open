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
 * Base class for enumeration types.
 *
 * @author Gerrit Meinders
 */
export default class Enum
{
	readonly name: string;

	constructor( name: string )
	{
		this.name = name;
	}

	/**
	 * Returns name of the enum constant.
	 *
	 * @return Enum constant name.
	 */
	valueOf(): string
	{
		return this.name;
	}

	/**
	 * Returns the qualified name of the enum constant.
	 *
	 * @return Qualified enum constant name (e.g. 'Enum.CONSTANT').
	 */
	toString(): string
	{
		return Object.getPrototypeOf( this ).constructor.name + "." + this.name;
	}

	static values: Enum[];

	/**
	 * Returns the enum constant with the specified value.
	 *
	 * @param value Value to find.
	 *
	 * @return Enum type.
	 *
	 * @throws TypeError if no matching enum constant is found.
	 */
	static valueOf( value: string ): Enum
	{
		const result = this.values.find( e => e.name === value );
		if ( !result )
		{
			throw new TypeError( value + " not found in " + this.name );
		}
		return result;
	}
}
