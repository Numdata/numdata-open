/*
 * Copyright (c) 2017-2018, Numdata BV, The Netherlands.
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
import ArrayTools from './ArrayTools';

/**
 * Tests if two objects are equal, using an equals method if available or
 * the strict equality operator (===) otherwise.
 *
 * @param o1 First value.
 * @param o2 Second value.
 *
 * @returns {boolean} true if the values are equal; false otherwise.
 */
function equals( o1, o2 )
{
	return o1 && o1.equals ? o1.equals( o2 ) : o1 === o2;
}

/**
 * Javascript implementation of <code>com.numdata.oss.AugmentedArrayList</code>.
 *
 * @author Gerrit Meinders
 */
export default class AugmentedArrayList
{
	/**
	 * Array with elements in this list.
	 */
	_elements;

	/**
	 * Constructs a new instance.
	 *
	 * @param {AugmentedArrayList|[]} [original] List or array to copy.
	 */
	constructor( original )
	{
		if ( original )
		{
			const elements = original._elements || original;
			if ( Array.isArray( elements ) )
			{
				this._elements = elements.slice();
			}
			else
			{
				throw new TypeError( original );
			}
		}
		else
		{
			this._elements = [];
		}
	}

	[Symbol.iterator]()
	{
		return this._elements[ Symbol.iterator ]();
	}

	/**
	 * Length of the list.
	 * @returns {number}
	 */
	get length()
	{
		return this._elements.length;
	}

	/**
	 * Calls the given function for each element of the aggregation.
	 *
	 * @param {function} consumer Consumer function.
	 */
	forEach( consumer )
	{
		this._elements.forEach( consumer );
	}

	/**
	 * Creates a new array with the results of calling a provided function on
	 * every element in this list.
	 *
	 * @param {function} callback Callback that produces the element of the new array.
	 *
	 * @return {Array} Created array.
	 */
	map( callback )
	{
		return this._elements.map( callback );
	}

	/**
	 * Returns a new array containing only the elements from this list that
	 * match the given condition.
	 *
	 * @param {function} condition Condition function.
	 *
	 * @return {Array}
	 */
	filter( condition )
	{
		return this._elements.filter( condition );
	}

	/**
	 * Finds the first element in the list for which the given condition holds.
	 *
	 * @param {function} condition Condition function.
	 *
	 * @return {*}
	 */
	find( condition )
	{
		return this._elements.find( condition );
	}

	/**
	 * Tests whether all elements pass the given test.
	 * @param condition Function implementing the test.
	 */
	every( condition )
	{
		return this._elements.every( condition );
	}

	/**
	 * Tests whether some element passes the given test.
	 *
	 * @param condition Function implementing the test.
	 */
	some( condition )
	{
		return this._elements.some( condition );
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

	add()
	{
		if ( arguments.length === 1 )
		{
			return this.addElement.apply( this, arguments );
		}
		else
		{
			this.addIndex.apply( this, arguments );
		}
	}

	addElement( element )
	{
		this._elements.push( element );
		return true;
	}

	addIndex( index, element )
	{
		let elementCount = this.size();
		if ( ( index < 0 ) || ( index > elementCount ) )
		{
			throw new Error( "Index: " + index + ", Size: " + elementCount );
		}
		this._elements.splice( index, 0, element );
	}

	addAll( collection )
	{
		let result = collection.length > 0;
		if ( result )
		{
			collection.forEach( element => this._elements.push( element ) );
		}
		return result;
	}

	setLength( length )
	{
		if ( length < 0 )
		{
			throw new TypeError( length );
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

	contains( element )
	{
		return this._elements.indexOf( element ) !== -1;
	}

	containsAll( collection )
	{
		return collection.every( element => this._elements.indexOf( element ) !== -1 );
	}

	indexOf( element )
	{
		return this._elements.indexOf( element );
	}

	lastIndexOf( element )
	{
		return this._elements.lastIndexOf( element );
	}

	get( index )
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

	setIndex( index, element )
	{
		if ( ( index < 0 ) || ( index >= this._elements.length ) )
		{
			throw new Error( "Index: " + index + ", Size: " + this._elements.length );
		}

		let oldElement = this._elements[ index ];

		this._elements[ index ] = element;

		return oldElement;
	}

	setAll( list )
	{
		let changed = ( list.length !== this.length ) || !list.every( ( element, i ) => equals( this._elements[ i ], element ) );
		if ( changed )
		{
			let elements = list._elements || list;
			this._elements = elements.slice();
		}
	}

	remove()
	{
		// FIXME: But what happens for int<>?
		if ( typeof arguments[ 0 ] === 'number' )
		{
			return this.removeIndex.apply( this, arguments );
		}
		else
		{
			return this.removeElement.apply( this, arguments );
		}
	}

	removeIndex( index )
	{
		if ( ( index < 0 ) || ( index >= this.length ) )
		{
			throw new Error( "Index: " + index + ", Size: " + this.length );
		}

		let element = this._elements[ index ];
		this._elements.splice( index, 1 );
		return element;
	}

	removeRange( startIndex, endIndex )
	{
		let elementCount = this.length;
		if ( ( startIndex < 0 ) || ( startIndex > elementCount ) )
		{
			throw new Error( startIndex );
		}

		if ( ( endIndex < 0 ) || ( endIndex > elementCount ) )
		{
			throw new Error( endIndex );
		}

		let length = endIndex - startIndex;
		if ( length > 0 )
		{
			this._elements.splice( startIndex, length );
		}
	}

	removeElement( element )
	{
		var index = this._elements.indexOf( element );
		let result = index !== -1;
		if ( result )
		{
			this._elements.splice( index, 1 );
		}
		return result;
	}

	removeFirst()
	{
		if ( this.isEmpty() )
		{
			throw new Error( "no such element" );
		}

		let element = this._elements[ 0 ];
		this._elements.splice( 0, 1 );
		return element;
	}

	removeLast()
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

	removeAll( collection )
	{
		let result = false;
		collection.forEach( element =>
		{
			result |= this.removeElement( element );
		} );
		return result;
	}

	equals( other )
	{
		return other._elements && ( this._elements.length === other._elements.length ) && ArrayTools.equals( this._elements, other._elements );
	}

	toString()
	{
		return '[' + String( this._elements ) + ']';
	}
}
