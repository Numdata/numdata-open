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
package com.numdata.oss;

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This decorator adds {@link AugmentedList} functionality to an existing
 * {@link List} implementation.
 *
 * @param <E> Element type.
 *
 * @author Peter S. Heijnen
 */
public class AugmentedListDecorator<E>
implements AugmentedList<E>
{
	/**
	 * List that is decorated.
	 */
	private final @NotNull List<E> _list;

	/**
	 * Returns {@link AugmentedList} for the given list. This can be the same
	 * instance, if it is already in instance of {@link AugmentedList}; or a
	 * new {@link AugmentedListDecorator} instance that wraps the given list.
	 *
	 * @param list List to decorate.
	 *
	 * @return {@link AugmentedList}.
	 */
	public static <E> @NotNull AugmentedList<E> wrap( final @NotNull List<E> list )
	{
		return ( list instanceof AugmentedList ) ? (AugmentedList<E>)list : new AugmentedListDecorator<>( list );
	}

	/**
	 * Decorate list with {@link AugmentedList} interface functionality.
	 *
	 * @param list List to decorate.
	 */
	public AugmentedListDecorator( final @NotNull List<E> list )
	{
		_list = list;
	}

	@Override
	public void ensureCapacity( final int capacity )
	{
		if ( _list instanceof ArrayList )
		{
			( (ArrayList<E>)_list ).ensureCapacity( capacity );
		}
	}

	@Override
	public void removeRange( final int startIndex, final int endIndex )
	{
		for ( int i = endIndex; --i >= startIndex; )
		{
			_list.remove( i );
		}
	}

	@Override
	public void setLength( final int length )
	{
		final int oldLength = _list.size();
		if ( length < oldLength )
		{
			removeRange( length, oldLength );
		}
		else
		{
			for ( int i = oldLength; i < length; i++ )
			{
				_list.add( null );
			}
		}
	}

	@Override
	public void setAll( final List<? extends E> list )
	{
		final List<E> dest = _list;
		final int oldLength = dest.size();
		final int length = list.size();

		final int common = Math.min( oldLength, length );
		for ( int i = 0; i < common; i++ )
		{
			dest.set( i, list.get( i ) );
		}

		if ( length < oldLength )
		{
			removeRange( length, oldLength );
		}
		else
		{
			for ( int i = oldLength; i < length; i++ )
			{
				dest.add( list.get( i ) );
			}
		}
	}

	@Override
	public E getFirst()
	{
		final List<E> list = _list;
		return ( list instanceof Deque ) ? ( (Deque<E>)list ).getFirst() : list.get( 0 );
	}

	@Override
	public E getLast()
	{
		final List<E> list = _list;
		return ( list instanceof Deque ) ? ( (Deque<E>)list ).getLast() : list.get( list.size() - 1 );
	}

	@Override
	public E removeFirst()
	{
		final List<E> list = _list;
		return ( list instanceof Deque ) ? ( (Deque<E>)list ).removeFirst() : list.remove( 0 );
	}

	@Override
	public E removeLast()
	{
		final List<E> list = _list;
		return ( list instanceof Deque ) ? ( (Deque<E>)list ).removeLast() : list.remove( list.size() - 1 );
	}

	@Override
	public int size()
	{
		return _list.size();
	}

	@Override
	public boolean isEmpty()
	{
		return _list.isEmpty();
	}

	@Override
	public boolean contains( final Object o )
	{
		return _list.contains( o );
	}

	@NotNull
	@Override
	public Iterator<E> iterator()
	{
		return _list.iterator();
	}

	@NotNull
	@Override
	public Object[] toArray()
	{
		return _list.toArray();
	}

	@NotNull
	@Override
	public <T> T[] toArray( @NotNull final T[] a )
	{
		return _list.toArray( a );
	}

	@Override
	public boolean add( final E e )
	{
		return _list.add( e );
	}

	@Override
	public boolean remove( final Object o )
	{
		return _list.remove( o );
	}

	@Override
	public boolean containsAll( @NotNull final Collection<?> c )
	{
		return _list.containsAll( c );
	}

	@Override
	public boolean addAll( @NotNull final Collection<? extends E> c )
	{
		return _list.addAll( c );
	}

	@Override
	public boolean addAll( final int index, @NotNull final Collection<? extends E> c )
	{
		return _list.addAll( index, c );
	}

	@Override
	public boolean removeAll( @NotNull final Collection<?> c )
	{
		return _list.removeAll( c );
	}

	@Override
	public boolean retainAll( @NotNull final Collection<?> c )
	{
		return _list.retainAll( c );
	}

	@Override
	public void clear()
	{
		_list.clear();
	}

	@Override
	public E get( final int index )
	{
		return _list.get( index );
	}

	@Override
	public E set( final int index, final E element )
	{
		return _list.set( index, element );
	}

	@Override
	public void add( final int index, final E element )
	{
		_list.add( index, element );
	}

	@Override
	public E remove( final int index )
	{
		return _list.remove( index );
	}

	@Override
	public int indexOf( final Object o )
	{
		return _list.indexOf( o );
	}

	@Override
	public int lastIndexOf( final Object o )
	{
		return _list.lastIndexOf( o );
	}

	@NotNull
	@Override
	public ListIterator<E> listIterator()
	{
		return _list.listIterator();
	}

	@NotNull
	@Override
	public ListIterator<E> listIterator( final int index )
	{
		return _list.listIterator( index );
	}

	@NotNull
	@Override
	public List<E> subList( final int fromIndex, final int toIndex )
	{
		return new AugmentedListDecorator<E>( _list.subList( fromIndex, toIndex ) );
	}
}
