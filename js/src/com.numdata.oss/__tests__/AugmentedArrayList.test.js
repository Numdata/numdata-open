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
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import expect from 'expect';
import AugmentedArrayList from '../AugmentedArrayList';

describe( 'AugmentedArrayList', function()
{
	test( 'constructor', function()
	{
		const list = new AugmentedArrayList();
		expect( list._elements ).toEqual( [] );
	} );

	test( 'clone constructor', function()
	{
		expect( new AugmentedArrayList( [ 'one', 'two', 'three' ] )._elements ).toEqual( [ 'one', 'two', 'three' ] );
		expect( new AugmentedArrayList( new AugmentedArrayList( [ 'one', 'two', 'three' ] ) )._elements ).toEqual( [ 'one', 'two', 'three' ] );
		expect( () => new AugmentedArrayList( 'one', 'two', 'three' ) ).toThrow( 'one' );
	} );

	test( 'iteration', () =>
	{
		const expected = [ 'one', 'two', 'three' ];
		const list = new AugmentedArrayList( expected );
		const actual = [];
		for ( let element of list )
		{
			actual.push( element );
		}
		expect( actual ).toEqual( expected );
	} );

	test( 'length', () =>
	{
		expect( new AugmentedArrayList().length ).toEqual( 0 );
		expect( new AugmentedArrayList( [ 'one', 'two', 'three' ] ).length ).toEqual( 3 );
	} );

	test( 'forEach', () =>
	{
		const consumer = jest.fn();
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		list.forEach( consumer );
		expect( consumer ).toHaveBeenNthCalledWith( 1, 'one', 0, list._elements );
		expect( consumer ).toHaveBeenNthCalledWith( 2, 'two', 1, list._elements );
		expect( consumer ).toHaveBeenNthCalledWith( 3, 'three', 2, list._elements );
		expect( consumer ).toHaveBeenCalledTimes( 3 );
	} );

	test( 'slice', () =>
	{
		expect( new AugmentedArrayList( [ 'one', 'two', 'three' ] ).slice() ).toEqual( [ 'one', 'two', 'three' ] );
		expect( new AugmentedArrayList( [ 'one', 'two', 'three' ] ).slice( 1 ) ).toEqual( [ 'two', 'three' ] );
		expect( new AugmentedArrayList( [ 'one', 'two', 'three' ] ).slice( 1, 2 ) ).toEqual( [ 'two' ] );
	} );

	test( 'map', () =>
	{
		const callback = jest.fn( ( element, index ) => index + ":" + element );
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.map( callback ) ).toEqual( [ '0:one', '1:two', '2:three' ] );
		expect( callback ).toHaveBeenNthCalledWith( 1, 'one', 0, list._elements );
		expect( callback ).toHaveBeenNthCalledWith( 2, 'two', 1, list._elements );
		expect( callback ).toHaveBeenNthCalledWith( 3, 'three', 2, list._elements );
		expect( callback ).toHaveBeenCalledTimes( 3 );
	} );

	test( 'filter', () =>
	{
		const callback = jest.fn( ( element, index ) => index === 1 || element === 'three' );
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.filter( callback ) ).toEqual( [ 'two', 'three' ] );
		expect( callback ).toHaveBeenNthCalledWith( 1, 'one', 0, list._elements );
		expect( callback ).toHaveBeenNthCalledWith( 2, 'two', 1, list._elements );
		expect( callback ).toHaveBeenNthCalledWith( 3, 'three', 2, list._elements );
		expect( callback ).toHaveBeenCalledTimes( 3 );
	} );

	test( 'find', () =>
	{
		const callback = jest.fn( element => element === 'two' );
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.find( callback ) ).toEqual( 'two' );
		expect( callback ).toHaveBeenNthCalledWith( 1, 'one', 0, list._elements );
		expect( callback ).toHaveBeenNthCalledWith( 2, 'two', 1, list._elements );
		expect( callback ).toHaveBeenCalledTimes( 2 );
	} );

	test( 'find (not found)', () =>
	{
		const callback = jest.fn( element => element === 'other' );
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.find( callback ) ).toEqual( undefined );
		expect( callback ).toHaveBeenNthCalledWith( 1, 'one', 0, list._elements );
		expect( callback ).toHaveBeenNthCalledWith( 2, 'two', 1, list._elements );
		expect( callback ).toHaveBeenNthCalledWith( 3, 'three', 2, list._elements );
		expect( callback ).toHaveBeenCalledTimes( 3 );
	} );

	test( 'every', () =>
	{
		const callback = jest.fn( ( element, index ) => index < 1 );
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.every( callback ) ).toEqual( false );
		expect( callback ).toHaveBeenNthCalledWith( 1, 'one', 0, list._elements );
		expect( callback ).toHaveBeenNthCalledWith( 2, 'two', 1, list._elements );
		expect( callback ).toHaveBeenCalledTimes( 2 );
	} );

	test( 'some', () =>
	{
		const callback = jest.fn( ( element, index ) => index === 1 );
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.some( callback ) ).toEqual( true );
		expect( callback ).toHaveBeenNthCalledWith( 1, 'one', 0, list._elements );
		expect( callback ).toHaveBeenNthCalledWith( 2, 'two', 1, list._elements );
		expect( callback ).toHaveBeenCalledTimes( 2 );
	} );

	test( 'isEmpty', () =>
	{
		expect( new AugmentedArrayList().isEmpty() ).toBe( true );
		expect( new AugmentedArrayList( [] ).isEmpty() ).toBe( true );
		expect( new AugmentedArrayList( [ 'one', 'two', 'three' ] ).isEmpty() ).toBe( false );
	} );

	test( 'size', () =>
	{
		expect( new AugmentedArrayList().size() ).toEqual( 0 );
		expect( new AugmentedArrayList( [ 'one', 'two', 'three' ] ).size() ).toEqual( 3 );
	} );

	test( 'clear', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		list.clear();
		expect( list._elements ).toEqual( [] );
	} );

	test( 'add( element: T ): boolean;', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.add( 'four' ) ).toBe( true );
		expect( list._elements ).toEqual( [ 'one', 'two', 'three', 'four' ] );
	} );

	test( 'add( index: number, element: T ): void;', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.add( 1, 'four' ) ).toBeUndefined();
		expect( list._elements ).toEqual( [ 'one', 'four', 'two', 'three' ] );
	} );

	test( 'addElement', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.addElement( 'four' ) ).toBe( true );
		expect( list._elements ).toEqual( [ 'one', 'two', 'three', 'four' ] );
	} );

	test( 'addIndex', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		list.addIndex( 1, 'four' );
		expect( list._elements ).toEqual( [ 'one', 'four', 'two', 'three' ] );
		list.addIndex( 4, 'five' );
		expect( list._elements ).toEqual( [ 'one', 'four', 'two', 'three', 'five' ] );
		expect( () => list.addIndex( 6, 'six' ) ).toThrow( 'Index: 6, Size: 5' );
	} );

	test( 'addAll', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.addAll( [] ) ).toBe( false );
		expect( list.addAll( [ 'four', 'five', 'six' ] ) ).toBe( true );
		expect( list._elements ).toEqual( [ 'one', 'two', 'three', 'four', 'five', 'six' ] );
	} );

	test( 'setLength', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		list.setLength( 1 );
		expect( list._elements ).toEqual( [ 'one' ] );
		list.setLength( 3 );
		expect( list._elements ).toEqual( [ 'one', null, null ] );
		expect( () => list.setLength( -1 ) ).toThrow( '-1' );
	} );

	test( 'contains', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.contains( 'two' ) ).toBe( true );
		expect( list.contains( 'four' ) ).toBe( false );
	} );

	test( 'containsAll', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.containsAll( [] ) ).toBe( true );
		expect( list.containsAll( [ 'two', 'three' ] ) ).toBe( true );
		expect( list.containsAll( [ 'two', 'four' ] ) ).toBe( false );
	} );

	test( 'indexOf', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.indexOf( 'two' ) ).toBe( 1 );
		expect( list.indexOf( 'four' ) ).toBe( -1 );
	} );

	test( 'lastIndexOf', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three', 'two', 'one' ] );
		expect( list.lastIndexOf( 'two' ) ).toBe( 3 );
		expect( list.lastIndexOf( 'four' ) ).toBe( -1 );
	} );

	test( 'get', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.get( 1 ) ).toEqual( 'two' );
		expect( () => list.get( 4 ) ).toThrow( 'Index: 4, Size: 3' );
	} );

	test( 'getFirst', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.getFirst() ).toEqual( 'one' );
		list.clear();
		expect( () => list.getFirst() ).toThrow( 'no such element' );
	} );

	test( 'getLast', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.getLast() ).toEqual( 'three' );
		list.clear();
		expect( () => list.getLast() ).toThrow( 'no such element' );
	} );

	test( 'setIndex', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.setIndex( 1, 'four' ) ).toEqual( 'two' );
		expect( list._elements ).toEqual( [ 'one', 'four', 'three' ] );
		expect( () => list.setIndex( 4, 'four' ) ).toThrow( 'Index: 4, Size: 3' );
	} );

	test( 'setAll', () =>
	{
		const list = new AugmentedArrayList( [ 'four', 'five' ] );
		list.setAll( [ 'one', 'two', 'three' ] );
		expect( list._elements ).toEqual( [ 'one', 'two', 'three' ] );
	} );

	test( 'remove( index: number ): T;', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.remove( 1 ) ).toBe( 'two' );
		expect( list._elements ).toEqual( [ 'one', 'three' ] );
	} );

	test( 'remove( element: T ): boolean;', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.remove( 'two' ) ).toBe( true );
		expect( list._elements ).toEqual( [ 'one', 'three' ] );
		expect( list.remove( 'two' ) ).toBe( false );
	} );

	test( 'removeIndex', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.removeIndex( 1 ) ).toBe( 'two' );
		expect( list._elements ).toEqual( [ 'one', 'three' ] );
		expect( () => list.removeIndex( 4 ) ).toThrow( 'Index: 4, Size: 2' );
	} );

	test( 'removeRange', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three', 'four' ] );
		list.removeRange( 1, 3 );
		expect( list._elements ).toEqual( [ 'one', 'four' ] );
		list.removeRange( 2, 1 ); // no-op
		expect( list._elements ).toEqual( [ 'one', 'four' ] );
		expect( () => list.removeRange( -1, 2 ) ).toThrow( '-1' );
		expect( () => list.removeRange( 2, -1 ) ).toThrow( '-1' );
		expect( () => list.removeRange( 4, 2 ) ).toThrow( '4' );
		expect( () => list.removeRange( 2, 4 ) ).toThrow( '4' );
	} );

	test( 'removeElement', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.removeElement( 'two' ) ).toBe( true );
		expect( list._elements ).toEqual( [ 'one', 'three' ] );
		expect( list.removeElement( 'two' ) ).toBe( false );
	} );

	test( 'removeFirst', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.removeFirst() ).toBe( 'one' );
		expect( list.removeFirst() ).toBe( 'two' );
		expect( list.removeFirst() ).toBe( 'three' );
		expect( list.isEmpty() ).toBe( true );
		expect( () => list.removeFirst() ).toThrow( 'no such element' );
	} );

	test( 'removeLast', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.removeLast() ).toBe( 'three' );
		expect( list.removeLast() ).toBe( 'two' );
		expect( list.removeLast() ).toBe( 'one' );
		expect( list.isEmpty() ).toBe( true );
		expect( () => list.removeLast() ).toThrow( 'no such element' );
	} );

	test( 'removeAll', () =>
	{
		const list = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		expect( list.removeAll( [ 'two', 'four' ] ) ).toBe( true );
		expect( list._elements ).toEqual( [ 'one', 'three' ] );
		expect( list.removeAll( [ 'five', 'six' ] ) ).toBe( false );
		expect( list._elements ).toEqual( [ 'one', 'three' ] );
	} );

	test( 'equals', () =>
	{
		const list1 = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		const list2 = new AugmentedArrayList( [ 'one', 'two', 'three' ] );
		const list3 = new AugmentedArrayList( [ 'one', 'two' ] );
		expect( list1.equals( list2 ) ).toBe( true );
		expect( list1.equals( list3 ) ).toBe( false );
		expect( list1.equals( list2._elements ) ).toBe( false );
		expect( list1.equals( list3._elements ) ).toBe( false );
		expect( list1.equals( 'banana' ) ).toBe( false );
	} );

	test( 'toString', () =>
	{
		expect( new AugmentedArrayList( [ 'one', 'two', 'three' ] ).toString() ).toBe( '[one,two,three]' );
	} );
} );
