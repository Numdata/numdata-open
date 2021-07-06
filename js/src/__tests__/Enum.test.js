/*
 * Copyright (c) 2017-2020, Numdata BV, The Netherlands.
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

import Enum from '../Enum';

/**
 * Enum for testing.
 */
class Enum1 extends Enum
{
}

Enum1.values = [
	Enum1.A = new Enum1( "A" ),
	Enum1.B = new Enum1( "B" ),
	Enum1.C = new Enum1( "C" )
];

/**
 * Enum for testing.
 */
class Enum2 extends Enum
{
}

Enum2.values = [
	Enum2.A = new Enum2( "A" ),
	Enum2.B = new Enum2( "B" ),
	Enum2.C = new Enum2( "C" )
];

test( "type-safety", () =>
{
	expect( Enum1.A ).toBeInstanceOf( Enum );
	expect( Enum2.B ).toBeInstanceOf( Enum );
	expect( Enum1.C ).toBeInstanceOf( Enum );

	expect( Enum1.A ).toBeInstanceOf( Enum1 );
	expect( Enum2.B ).toBeInstanceOf( Enum2 );
	expect( Enum2.C ).toBeInstanceOf( Enum2 );

	expect( Enum1.A ).not.toBeInstanceOf( Enum2 );
	expect( Enum2.B ).not.toBeInstanceOf( Enum1 );
} )
test( "should have values property", () =>
{
	expect( Enum1.values ).toEqual( [ Enum1.A, Enum1.B, Enum1.C ] );
} );

test( 'valueOf', () =>
{
	expect( Enum1.A.valueOf() ).toEqual( "A" );
	expect( Enum1.B.valueOf() ).toEqual( "B" );
	expect( Enum1.C.valueOf() ).toEqual( "C" );
} );

test( 'toString', () =>
{
	expect( Enum1.A.toString() ).toEqual( "Enum1.A" );
	expect( Enum2.B.toString() ).toEqual( "Enum2.B" );
} );

test( 'static valueOf', () =>
{
	expect( Enum1.valueOf( 'A' ) ).toBe( Enum1.A );
	expect( Enum2.valueOf( 'B' ) ).toBe( Enum2.B );
	expect( () => Enum1.valueOf( 'D' ) ).toThrow();
} );
