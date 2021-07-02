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

import LocalizedString from '../LocalizedString';
import Locale from '../Locale';

test( 'construct', () => {
	expect( new LocalizedString()._values ).toEqual( {} );
} );

test( 'construct from JSON', () => {
	const json = {
		'': 'Greetings Earthlings',
		en: 'Hello world!',
		nl: 'Hallo wereld!'
	}
	expect( new LocalizedString( json )._values ).toEqual( json );
	expect( () => new LocalizedString( { 'nl_NL_Achterhoeks': 'This is perfectly fine.' } )._values ).not.toThrow();
	expect( () => new LocalizedString( { invalid: 'This is unacceptable!' } )._values ).toThrow();
} );

test( 'construct from LocalizedString', () => {
	const json = {
		'': 'Greetings Earthlings',
		en: 'Hello world!',
		nl: 'Hallo wereld!'
	}
	expect( new LocalizedString( new LocalizedString( json ) )._values ).toEqual( json );
} );

test( 'create', () => {
	const defaultOnly = LocalizedString.create( "Dog" );
	expect( defaultOnly.get() ).toEqual( "Dog" );
	expect( defaultOnly.get( Locale.US ) ).toEqual( "Dog" );
	expect( defaultOnly.get( Locale.GERMAN ) ).toEqual( "Dog" );

	expect( () => LocalizedString.create( "Dog", "de" ) ).toThrow();
	expect( () => LocalizedString.create( "Dog", "de", "Hund", "nl" ) ).toThrow();

	const localizedString = LocalizedString.create( "Dog", "de", "Hund", "nl", "Hond" );
	expect( localizedString.get() ).toEqual( "Dog" );
	expect( localizedString.get( Locale.ENGLISH ) ).toEqual( "Dog" );
	expect( localizedString.get( Locale.GERMAN ) ).toEqual( "Hund" );
	expect( localizedString.get( "nl" ) ).toEqual( "Hond" );
} );

test( 'get with defaultValue', () => {
	const withExplicitDefault = new LocalizedString( {
		'': 'Greetings Earthlings',
		en: 'Hello world!',
		nl: 'Hallo wereld!'
	} );
	expect( withExplicitDefault.get( 'zh', 'Default greeting' ) ).toEqual( 'Greetings Earthlings' );

	const withoutExplicitDefault = new LocalizedString( {
		en: 'Hello world!',
		nl: 'Hallo wereld!'
	} );
	expect( withoutExplicitDefault.get( 'zh', 'Default greeting' ) ).toEqual( 'Default greeting' );
} );

test( 'get any locale', () => {
	const withEnglish = new LocalizedString( {
		en: 'Hello world!',
		nl: 'Hallo wereld!'
	} );
	expect( withEnglish.get( null, 'Default greeting' ) ).toEqual( 'Hello world!' );

	const withoutEnglish = new LocalizedString( {
		de: 'Hallo Welt!',
		nl: 'Hallo wereld!'
	} );
	expect( withoutEnglish.get( null, 'Default greeting' ) ).toEqual( 'Hallo Welt!' );
} );

test( 'equals', () => {
	const localizedString = new LocalizedString( { en: 'Hello world!', nl: 'Hallo wereld!' } );
	expect( localizedString.equals( localizedString ) ).toBe( true );
	expect( localizedString.equals( new LocalizedString( { en: 'Hello world!', nl: 'Hallo wereld!' } ) ) ).toBe( true );
	expect( localizedString.equals( new LocalizedString( { en: 'Hello world!', nl: 'Hallo andere wereld!' } ) ) ).toBe( false );
	expect( localizedString.equals( new LocalizedString( { en: 'Hello world!', nl: 'Hallo wereld!', de: 'Hallo Welt!' } ) ) ).toBe( false );
} );
