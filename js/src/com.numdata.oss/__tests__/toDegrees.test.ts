/*
 * (C) Copyright Unicon Creation BV 2021-2021 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Unicon Creation BV. Please contact
 * Unicon Creation BV for license information.
 */

import toDegrees from '../toDegrees';

test( 'toDegrees', () => {
	expect( toDegrees( 0 ) ).toBe( 0 );
	expect( toDegrees( Math.PI / 2 ) ).toBe( 90 );
	expect( toDegrees( Math.PI ) ).toBe( 180 );
	expect( toDegrees( -Math.PI / 4 ) ).toBe( -45 );
} );
