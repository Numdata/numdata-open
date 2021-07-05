/*
 * Copyright (c) 2021-2021, Unicon Creation BV, The Netherlands.
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

import equals from '../equals';

test( 'without equals method', () => {
	expect( equals( 1, 1 ) ).toBe( true );
	expect( equals( 1, 2 ) ).toBe( false );
	expect( equals( 1, '1' ) ).toBe( false );
	expect( equals( 'abc', 'abc' ) ).toBe( true );
	expect( equals( 'abc', 'ab' ) ).toBe( false );
	expect( equals( NaN, NaN ) ).toBe( false );
	expect( equals( { a: 1 }, { a: 1 } ) ).toBe( false );
	const object = { a: 2 };
	expect( equals( object, object ) ).toBe( true );
} );

test( 'without equals method', () => {
	expect( equals( new Example( 1 ), new Example( 1 ) ) ).toBe( true );
	expect( equals( new Example( 1 ), { value: 1 } ) ).toBe( false );
} );

/**
 * Example type with equals.
 */
class Example
{
	value: number;

	constructor( value: number )
	{
		this.value = value;
	}

	/**
	 * Returns whether this object and the other object are equal.
	 *
	 * @param other Other object.
	 *
	 * @return 'true' if equal, otherwise 'false'.
	 */
	equals( other: any ): boolean
	{
		return this === other || other instanceof Example && this.value === other.value;
	}
}
