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
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import signum from '../signum';

test( 'negative', () => {
	expect( signum( -1 ) ).toBe( -1 );
	expect( signum( -2 ) ).toBe( -1 );
	expect( signum( -Infinity ) ).toBe( -1 );
} );

test( 'zero', () => {
	expect( signum( -0 ) ).toBe( -0 );
	expect( signum( 0 ) ).toBe( 0 );
} );

test( 'positive', () => {
	expect( signum( 1 ) ).toBe( 1 );
	expect( signum( 2 ) ).toBe( 1 );
	expect( signum( Infinity ) ).toBe( 1 );
} );

test( 'invalid', () => {
	expect( signum( NaN ) ).toBe( NaN );
	expect( () => signum( null ) ).toThrow( "expected a number, but was: object (null)" );
	// @ts-ignore
	expect( () => signum( '123' ) ).toThrow( "expected a number, but was: string (123)" );
	// @ts-ignore
	expect( () => signum( {} ) ).toThrow( "expected a number, but was: object ([object Object])" );
} );
