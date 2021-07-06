/*
 * Copyright (c) 2021, Unicon Creation BV, The Netherlands.
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

import EventDispatcher from '../EventDispatcher';

test( 'listeners', () => {
	const eventDispatcher = new EventDispatcher();
	const listener1 = jest.fn();
	const listener2 = jest.fn();

	eventDispatcher.dispatch( 'a' );
	eventDispatcher.addListener( listener1 );
	eventDispatcher.dispatch( 'b' );
	eventDispatcher.addListener( listener2 );
	eventDispatcher.dispatch( 'c' );
	eventDispatcher.removeListener( listener1 );
	eventDispatcher.dispatch( 'd' );
	eventDispatcher.removeListener( listener2 );
	eventDispatcher.dispatch( 'e' );

	expect( listener1.mock.calls ).toEqual( [ [ 'b' ], [ 'c' ] ] );
	expect( listener2.mock.calls ).toEqual( [ [ 'c' ], [ 'd' ] ] );
} );

test( 'filters', () => {
	const eventDispatcher = new EventDispatcher();
	const listener = jest.fn();
	const filter1 = { filterEvent: jest.fn( event => event + event ) };
	const filter2 = { filterEvent: jest.fn( () => null ) };

	eventDispatcher.addListener( listener );
	eventDispatcher.dispatch( 'a' );
	eventDispatcher.appendFilter( filter1 );
	eventDispatcher.dispatch( 'b' );
	eventDispatcher.appendFilter( filter1 );
	eventDispatcher.dispatch( 'c' );
	eventDispatcher.removeFilter( filter1 );
	eventDispatcher.dispatch( 'd' );
	eventDispatcher.appendFilter( filter2 );
	eventDispatcher.dispatch( 'e' );
	eventDispatcher.removeFilter( filter2 );
	eventDispatcher.dispatch( 'f' );
	eventDispatcher.insertFilter( filter2 );
	eventDispatcher.dispatch( 'g' );

	expect( listener.mock.calls ).toEqual( [ [ 'a' ], [ 'bb' ], [ 'cccc' ], [ 'dd' ], [ 'ff' ] ] );
	expect( filter1.filterEvent.mock.calls ).toEqual( [ [ 'b' ], [ 'c' ], [ 'cc' ], [ 'd' ], [ 'e' ], [ 'f' ] ] );
	expect( filter2.filterEvent.mock.calls ).toEqual( [ [ 'ee' ], [ 'g' ] ] );
} );

test( 'focus', () => {
	const eventDispatcher = new EventDispatcher();
	const listener = jest.fn();
	const filter1 = { filterEvent: jest.fn( event => event + event ) };
	const filter2 = { filterEvent: jest.fn( () => null ) };

	eventDispatcher.addListener( listener );
	eventDispatcher.dispatch( 'a' );
	eventDispatcher.appendFilter( filter1 );
	eventDispatcher.dispatch( 'b' );
	eventDispatcher.appendFilter( filter2 );
	eventDispatcher.dispatch( 'c' );
	expect( eventDispatcher.hasFocus( filter1 ) ).toBe( false );
	expect( eventDispatcher.hasFocus( filter2 ) ).toBe( false );
	eventDispatcher.requestFocus( filter1 );
	expect( eventDispatcher.hasFocus( filter1 ) ).toBe( true );
	expect( eventDispatcher.hasFocus( filter2 ) ).toBe( false );
	eventDispatcher.dispatch( 'd' );
	eventDispatcher.releaseFocus();
	expect( eventDispatcher.hasFocus( filter1 ) ).toBe( false );
	expect( eventDispatcher.hasFocus( filter2 ) ).toBe( false );
	eventDispatcher.dispatch( 'e' );
	eventDispatcher.requestFocus( filter2 );
	expect( eventDispatcher.hasFocus( filter1 ) ).toBe( false );
	expect( eventDispatcher.hasFocus( filter2 ) ).toBe( true );
	eventDispatcher.dispatch( 'f' );

	expect( listener.mock.calls ).toEqual( [ [ 'a' ], [ 'bb' ] ] );
	expect( filter1.filterEvent.mock.calls ).toEqual( [ [ 'b' ], [ 'c' ], [ 'd' ], [ 'e' ] ] );
	expect( filter2.filterEvent.mock.calls ).toEqual( [ [ 'cc' ], [ 'dd' ], [ 'ee' ], [ 'f' ] ] );
} );

test( 'NullEventDispatcher', () => {
	const eventDispatcher: EventDispatcher = EventDispatcher.NULL;
	const listener = jest.fn();
	const filter = { filterEvent: jest.fn( event => event ) };

	expect( () => eventDispatcher.addListener( listener ) ).toThrow();
	expect( () => eventDispatcher.appendFilter( filter ) ).toThrow();
	expect( () => eventDispatcher.insertFilter( filter ) ).toThrow();
	eventDispatcher.dispatch( 'a' );
	eventDispatcher.requestFocus( filter );
	eventDispatcher.dispatch( 'b' );

	expect( listener.mock.calls ).toEqual( [] );
	expect( filter.filterEvent.mock.calls ).toEqual( [] );
} );
