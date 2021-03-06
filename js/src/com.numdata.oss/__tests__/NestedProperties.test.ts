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
import NestedProperties from '../NestedProperties';

test( 'constructor', () => {
	expect( new NestedProperties() ).toEqual( { _values: {} } );
} );

test( 'copyFrom', () => {
	const a = new NestedProperties();
	a.set( 'a', 1 );
	a.set( 'b', '2' );
	a.set( 'c', [ 3 ] );

	const b = new NestedProperties( a );
	expect( b ).toEqual( a );
} );

test( 'getBoolean', () => {
	const properties = new NestedProperties();
	properties.set( "true", true );
	properties.set( "false", false );
	properties.set( "positive", 1.23 );
	properties.set( "zero", 0 );
	properties.set( "negative", -1.23 );
	properties.set( "trueString1", "True" );
	properties.set( "trueString2", "1" );
	properties.set( "trueString3", "y" );
	properties.set( "trueString4", "yeS" );
	properties.set( "falseString1", "any" );
	properties.set( "falseString2", "" );

	expect( properties.getBoolean( "true", false ) ).toBe( true );
	expect( properties.getBoolean( "false", true ) ).toBe( false );
	expect( properties.getBoolean( "positive", false ) ).toBe( true );
	expect( properties.getBoolean( "zero", true ) ).toBe( false );
	expect( properties.getBoolean( "negative", true ) ).toBe( false );
	expect( properties.getBoolean( "trueString1", false ) ).toBe( true );
	expect( properties.getBoolean( "trueString2", false ) ).toBe( true );
	expect( properties.getBoolean( "trueString3", false ) ).toBe( true );
	expect( properties.getBoolean( "trueString4", false ) ).toBe( true );
	expect( properties.getBoolean( "falseString1", true ) ).toBe( false );
	expect( properties.getBoolean( "falseString2", true ) ).toBe( false );
	expect( properties.getBoolean( "other", true ) ).toBe( true );
	expect( properties.getBoolean( "other", false ) ).toBe( false );
	expect( properties.getBoolean( "other" ) ).toBe( false );
} );

test( 'getFloat', () => {
	const properties = new NestedProperties();
	properties.set( "true", true );
	properties.set( "false", false );
	properties.set( "positive", 1.23 );
	properties.set( "zero", 0 );
	properties.set( "negative", -1.23 );
	properties.set( "numberString1", "1.23" );
	properties.set( "numberString2", "NaN" );
	properties.set( "invalidString1", "any" );
	properties.set( "invalidString2", "" );

	expect( () => properties.getFloat( 'true', 13.37 ) ).toThrow( new TypeError( 'true=true' ) );
	expect( () => properties.getFloat( 'false', 13.37 ) ).toThrow( new TypeError( 'false=false' ) );
	expect( properties.getFloat( 'positive', 13.37 ) ).toBe( 1.23 );
	expect( properties.getFloat( 'zero', 13.37 ) ).toBe( 0 );
	expect( properties.getFloat( 'negative', 13.37 ) ).toBe( -1.23 );
	expect( properties.getFloat( 'numberString1', 13.37 ) ).toBe( 1.23 );
	expect( properties.getFloat( 'numberString2', 13.37 ) ).toBe( NaN );
	expect( () => properties.getFloat( 'invalidString1' ) ).toThrow( new TypeError( 'invalidString1=any' ) );
	expect( () => properties.getFloat( 'invalidString2' ) ).toThrow( new TypeError( 'invalidString2=' ) );
	expect( properties.getFloat( 'other', 13.37 ) ).toBe( 13.37 );
	expect( properties.getFloat( 'other' ) ).toBe( 0.0 );
} );

test( 'getInt', () => {
	const properties = new NestedProperties();
	properties.set( "true", true );
	properties.set( "false", false );
	properties.set( "positive", 1.23 );
	properties.set( "zero", 0 );
	properties.set( "negative", -1.23 );
	properties.set( "numberString1", "123" );
	properties.set( "invalidString1", "1.23" );
	properties.set( "invalidString2", "NaN" );
	properties.set( "invalidString3", "any" );
	properties.set( "invalidString4", "" );

	expect( () => properties.getInt( "true", 1337 ) ).toThrow( new TypeError( 'true=true' ) );
	expect( () => properties.getInt( "false", 1337 ) ).toThrow( new TypeError( 'false=false' ) );
	expect( properties.getInt( "positive", 1337 ) ).toBe( 1 );
	expect( properties.getInt( "zero", 1337 ) ).toBe( 0 );
	expect( properties.getInt( "negative", 1337 ) ).toBe( -1 );
	expect( properties.getInt( "numberString1", 1337 ) ).toBe( 123 );
	expect( () => properties.getInt( "invalidString1", 1337 ) ).toThrow( new TypeError( `invalidString1=${properties.getProperty( 'invalidString1' )}` ) );
	expect( () => properties.getInt( "invalidString2", 1337 ) ).toThrow( new TypeError( `invalidString2=${properties.getProperty( 'invalidString2' )}` ) );
	expect( () => properties.getInt( "invalidString3", 1337 ) ).toThrow( new TypeError( `invalidString3=${properties.getProperty( 'invalidString3' )}` ) );
	expect( () => properties.getInt( "invalidString4", 1337 ) ).toThrow( new TypeError( `invalidString4=${properties.getProperty( 'invalidString4' )}` ) );
	expect( properties.getInt( "other", 1337 ) ).toBe( 1337 );
	expect( properties.getInt( "other" ) ).toBe( 0 );
} );

test( 'getNested', () => {

} );

test( 'getProperty', () => {
	const properties = new NestedProperties();
	properties.set( "true", true );
	properties.set( "false", false );
	properties.set( "positive", 1.23 );
	properties.set( "zero", 0 );
	properties.set( "negative", -1.23 );
	properties.set( "string1", "123" );
	properties.set( "string2", "any" );
	properties.set( "string3", "" );

	expect( properties.getProperty( "true", "default" ) ).toBe( "true" );
	expect( properties.getProperty( "false", "default" ) ).toBe( "false" );
	expect( properties.getProperty( "positive", "default" ) ).toBe( "1.23" );
	expect( properties.getProperty( "zero", "default" ) ).toBe( "0" );
	expect( properties.getProperty( "negative", "default" ) ).toBe( "-1.23" );
	expect( properties.getProperty( "string1", "default" ) ).toBe( "123" );
	expect( properties.getProperty( "string2", "default" ) ).toBe( "any" );
	expect( properties.getProperty( "string3", "default" ) ).toBe( "" );
	expect( properties.getProperty( "other", "default" ) ).toBe( "default" );
	expect( properties.getProperty( "other" ) ).toBe( null );
} );

test( 'set', () => {

} );

test( 'clear', () => {

} );

test( 'equals', () => {

} );
