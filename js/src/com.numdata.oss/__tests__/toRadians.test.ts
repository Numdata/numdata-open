/*
 * (C) Copyright Unicon Creation BV 2021-2021 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Unicon Creation BV. Please contact
 * Unicon Creation BV for license information.
 */

import toRadians from '../toRadians';

test( 'toRadians', () => {
	expect( toRadians( 0 ) ).toBe( 0 );
	expect( toRadians( 90 ) ).toBe( Math.PI / 2 );
	expect( toRadians( 180 ) ).toBe( Math.PI );
	expect( toRadians( -45 ) ).toBe( -Math.PI / 4 );
} );
