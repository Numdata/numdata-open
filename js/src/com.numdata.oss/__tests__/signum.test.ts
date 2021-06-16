/*
 * (C) Copyright Unicon Creation BV 2021-2021 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Unicon Creation BV. Please contact
 * Unicon Creation BV for license information.
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
