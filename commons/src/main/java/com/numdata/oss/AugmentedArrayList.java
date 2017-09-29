/*
 * Copyright (c) 2017, Numdata BV, The Netherlands.
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
package com.numdata.oss;

import java.util.*;

/**
 * This class extends the {@link ArrayList} class to implement the features of
 * the {@link AugmentedList} interface.
 *
 * @param <E> Element type.
 *
 * @author Peter S. Heijnen
 */
public class AugmentedArrayList<E>
extends ArrayList<E>
implements AugmentedList<E>
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -3207470177194124202L;

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */
	public AugmentedArrayList()
	{
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 *
	 * @param initialCapacity Initial capacity of the list.
	 *
	 * @throws IllegalArgumentException an invalid capacity is specified.
	 */
	public AugmentedArrayList( final int initialCapacity )
	{
		super( initialCapacity );
	}

	/**
	 * Constructs a list containing the elements of the specified collection, in
	 * the order they are returned by the {@code collection}'s iterator. The
	 * initial capacity will be 110% the size of the specified {@code
	 * collection}.
	 *
	 * @param collection Collection with elements to place in this list.
	 *
	 * @throws NullPointerException if the specified {@code collection} is
	 * {@code null}.
	 */
	public AugmentedArrayList( final Collection<? extends E> collection )
	{
		super( collection );
	}

	/**
	 * Constructs a list containing the specified elements. The initial capacity
	 * will be the same as the specified number of elements.
	 *
	 * @param elements Elements of list to construct.
	 *
	 * @throws NullPointerException if {@code elements} is {@code null}.
	 */
	public AugmentedArrayList( final E... elements )
	{
		super( Arrays.asList( elements ) );
	}

	@Override
	public void setLength( final int length )
	{
		if ( length < 0 )
		{
			throw new IllegalArgumentException( String.valueOf( length ) );
		}

		final int oldLength = size();
		if ( length > oldLength )
		{
			ensureCapacity( length );
			for ( int i = oldLength; i < length; i++ )
			{
				add( null );
			}
		}
		else if ( length < oldLength )
		{
			removeRange( length, oldLength );
		}
	}

	@Override
	public void removeRange( final int startIndex, final int endIndex )
	{
		super.removeRange( startIndex, endIndex );
	}

	@Override
	public void setAll( final List<? extends E> list )
	{
		final int oldSize = size();
		final int newSize = list.size();

		final int common = Math.min( oldSize, newSize );
		for ( int i = 0; i < common; i++ )
		{
			set( i, list.get( i ) );
		}

		if ( newSize > oldSize )
		{
			ensureCapacity( newSize );

			for ( int i = oldSize; i < newSize; i++ )
			{
				add( list.get( i ) );
			}
		}
		else if ( newSize < oldSize )
		{
			removeRange( newSize, oldSize );
		}
	}

	@Override
	public E getFirst()
	{
		if ( isEmpty() )
		{
			throw new NoSuchElementException( "empty" );
		}

		return get( 0 );
	}

	@Override
	public E getLast()
	{
		final int size = size();
		if ( size == 0 )
		{
			throw new NoSuchElementException( "empty" );
		}

		return get( size - 1 );
	}

	@Override
	public E removeFirst()
	{
		if ( isEmpty() )
		{
			throw new NoSuchElementException( "empty" );
		}

		return remove( 0 );
	}

	@Override
	public E removeLast()
	{
		final int size = size();
		if ( size == 0 )
		{
			throw new NoSuchElementException( "empty" );
		}

		return remove( size - 1 );
	}
}
