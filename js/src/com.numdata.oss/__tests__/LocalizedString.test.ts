/*
 * (C) Copyright Unicon Creation BV 2021-2021 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Unicon Creation BV. Please contact
 * Unicon Creation BV for license information.
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
