/*
 * Copyright (c) 2017-2021, Unicon Creation BV, The Netherlands.
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
import equals from '../equals';
import ArrayTools from './ArrayTools';
import AugmentedList from './AugmentedList';

type Collection<T> = AugmentedArrayList<T> | T[];

/**
 * Javascript implementation of <code>com.numdata.oss.AugmentedArrayList</code>.
 *
 * @author Gerrit Meinders
 */
export default class AugmentedArrayList<T> implements AugmentedList<T>
{
	/**
	 * Array with elements in this list.
	 */
	_elements: T[];

	/**
	 * Constructs a new instance.
	 *
	 * @param {AugmentedArrayList|[]} [original] List or array to copy.
	 */
	constructor( original?: AugmentedArrayList<T> | T[] )
	{
		if ( original )
		{
			const elements = original instanceof AugmentedArrayList ? original._elements : original;
			if ( !Array.isArray( elements ) )
			{
				throw new TypeError( String( original ) );
			}
			this._elements = elements.slice();
		}
		else
		{
			this._elements = [];
		}
	}

	[ Symbol.iterator ]()
	{
		return this._elements[ Symbol.iterator ]();
	}

	get length()
	{
		return this._elements.length;
	}

	forEach( consumer: ( element: T, index: number, array: T[] ) => void, thisArg?: any )
	{
		this._elements.forEach( consumer, thisArg );
	}

	slice( start?: number, end?: number ): T[]
	{
		return this._elements.slice( start, end );
	}

	map<U>( callback: ( element: T, index: number, array: T[] ) => U, thisArg?: any ): U[]
	{
		return this._elements.map( callback, thisArg );
	}

	filter( condition: ( element: T, index: number, array: T[] ) => boolean, thisArg?: any ): T[]
	{
		return this._elements.filter( condition, thisArg );
	}

	find( condition: ( element: T, index: number, array: T[] ) => boolean, thisArg?: any ): T
	{
		return this._elements.find( condition, thisArg );
	}

	every( condition: ( element: T, index: number, array: T[] ) => boolean, thisArg?: any ): boolean
	{
		return this._elements.every( condition, thisArg );
	}

	some( condition: ( element: T, index: number, array: T[] ) => boolean, thisArg?: any ): boolean
	{
		return this._elements.some( condition, thisArg );
	}

	isEmpty()
	{
		return this.size() === 0;
	}

	size()
	{
		return this._elements.length;
	}

	clear()
	{
		this._elements.length = 0;
	}

	add( element: T ): boolean;
	add( index: number, element: T ): void;
	add( ...args: any[] )
	{
		if ( args.length === 1 )
		{
			return this.addElement( args[ 0 ] );
		}
		else
		{
			this.addIndex( args[ 0 ], args[ 1 ] );
		}
	}

	addElement( element: T ): boolean
	{
		this._elements.push( element );
		return true;
	}

	addIndex( index: number, element: T ): void
	{
		let elementCount = this.size();
		if ( ( index < 0 ) || ( index > elementCount ) )
		{
			throw new Error( "Index: " + index + ", Size: " + elementCount );
		}
		this._elements.splice( index, 0, element );
	}

	addAll( collection: Collection<T> ): boolean
	{
		let result = collection.length > 0;
		if ( result )
		{
			collection.forEach( element => this._elements.push( element ) );
		}
		return result;
	}

	setLength( length: number )
	{
		if ( length < 0 )
		{
			throw new TypeError( String( length ) );
		}

		if ( length < this._elements.length )
		{
			this._elements.length = length;
		}
		else if ( length > this._elements.length )
		{
			while ( length > this._elements.length )
			{
				this._elements.push( null );
			}
		}
	}

	contains( element: T )
	{
		return this._elements.indexOf( element ) !== -1;
	}

	containsAll( collection: Collection<T> )
	{
		return collection.every( element => this._elements.indexOf( element ) !== -1 );
	}

	indexOf( element: T )
	{
		return this._elements.indexOf( element );
	}

	lastIndexOf( element: T )
	{
		return this._elements.lastIndexOf( element );
	}

	get( index: number )
	{
		if ( ( index < 0 ) || ( index >= this._elements.length ) )
		{
			throw new Error( "Index: " + index + ", Size: " + this._elements.length );
		}

		return this._elements[ index ];
	}

	getFirst()
	{
		if ( this.isEmpty() )
		{
			throw new Error( "no such element" );
		}

		return this._elements[ 0 ];
	}

	getLast()
	{
		let size = this.size();
		if ( size === 0 )
		{
			throw new Error( "no such element" );
		}

		return this._elements[ size - 1 ];
	}

	setIndex( index: number, element: T )
	{
		if ( ( index < 0 ) || ( index >= this._elements.length ) )
		{
			throw new Error( "Index: " + index + ", Size: " + this._elements.length );
		}

		let oldElement = this._elements[ index ];

		this._elements[ index ] = element;

		return oldElement;
	}

	setAll( list: Collection<T> )
	{
		let changed = ( list.length !== this.length ) || !list.every( ( element: T, i: number ) => equals( this._elements[ i ], element ) );
		if ( changed )
		{
			let elements = ( list as any )._elements || list;
			this._elements = elements.slice();
		}
	}

	remove( index: number ): T;
	remove( element: T ): boolean;
	remove( arg: number | T )
	{
		// FIXME: But what happens for int<>?
		if ( typeof arg === 'number' )
		{
			return this.removeIndex( arg );
		}
		else
		{
			return this.removeElement( arg );
		}
	}

	removeIndex( index: number ): T
	{
		if ( ( index < 0 ) || ( index >= this.length ) )
		{
			throw new Error( "Index: " + index + ", Size: " + this.length );
		}

		let element = this._elements[ index ];
		this._elements.splice( index, 1 );
		return element;
	}

	removeRange( startIndex: number, endIndex: number ): void
	{
		let elementCount = this.length;
		if ( ( startIndex < 0 ) || ( startIndex > elementCount ) )
		{
			throw new Error( String( startIndex ) );
		}

		if ( ( endIndex < 0 ) || ( endIndex > elementCount ) )
		{
			throw new Error( String( endIndex ) );
		}

		let length = endIndex - startIndex;
		if ( length > 0 )
		{
			this._elements.splice( startIndex, length );
		}
	}

	removeElement( element: T ): boolean
	{
		const index = this._elements.indexOf( element );
		let result = index !== -1;
		if ( result )
		{
			this._elements.splice( index, 1 );
		}
		return result;
	}

	removeFirst(): T
	{
		if ( this.isEmpty() )
		{
			throw new Error( "no such element" );
		}

		let element = this._elements[ 0 ];
		this._elements.splice( 0, 1 );
		return element;
	}

	removeLast(): T
	{
		let size = this.size();
		if ( size === 0 )
		{
			throw new Error( "no such element" );
		}

		let element = this._elements[ size - 1 ];
		this._elements.length = size - 1;
		return element;
	}

	removeAll( collection: Collection<T> ): boolean
	{
		let result = false;
		collection.forEach( element =>
		{
			result = result || this.removeElement( element );
		} );
		return result;
	}

	equals( other: any )
	{
		return other._elements ? ( this._elements.length === other._elements.length ) && ArrayTools.equals( this._elements, other._elements ) : false;
	}

	toString()
	{
		return '[' + String( this._elements ) + ']';
	}
}
