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
import { assert } from 'chai';
import Enum from '../../lib/java.lang/Enum';

class Enum1 extends Enum
{
}
Enum1.A = new Enum1( "A" );
Enum1.B = new Enum1( "B" );
Enum1.C = new Enum1( "C" );
Enum1.values = [ Enum1.A, Enum1.B, Enum1.C ];

class Enum2 extends Enum
{
}
Enum2.A = new Enum2( "A" );
Enum2.B = new Enum2( "B" );
Enum2.C = new Enum2( "C" );
Enum2.values = [ Enum2.A, Enum2.B, Enum2.C ];

describe( "Enum", function()
{
	it( "should have enum constants", function()
	{
		assert.instanceOf( Enum1.A, Enum1 );
		assert.instanceOf( Enum1.B, Enum1 );
		assert.instanceOf( Enum1.C, Enum1 );
	} );

	it( "should extends Enum", function()
	{
		assert.instanceOf( Enum1.A, Enum );
		assert.instanceOf( Enum1.B, Enum );
		assert.instanceOf( Enum1.C, Enum );
	} );

	it( "should have values property", function()
	{
		console.info( Enum1.prototype );
		assert.deepEqual( Enum1.values, [ Enum1.A, Enum1.B, Enum1.C ] );
	} );

	it( "should be type-safe", function()
	{
		assert.notEqual( Enum1.A, Enum2.A );
		assert.notEqual( Enum1.B, Enum2.B );
		assert.notEqual( Enum1.C, Enum2.C );
		assert.notInstanceOf( Enum1.A, Enum2 );
		assert.notInstanceOf( Enum2.B, Enum1 );
	} );

	it( "should match strings", function()
	{
		assert.equal( Enum1.A, "A" );
		assert.equal( Enum1.B, "B" );
		assert.equal( Enum1.C, "C" );
	} );
} );
