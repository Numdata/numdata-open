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

import org.jetbrains.annotations.*;

/**
 * Provides access to the underlying augmented list but prevents modifications.
 *
 * @author Gerrit Meinders
 */
@SuppressWarnings( "NewExceptionWithoutArguments" )
public class UnmodifiableAugmentedList<E>
implements AugmentedList<E>
{
	/**
	 * Wrapped list.
	 */
	private final AugmentedList<? extends E> _list;

	/**
	 * Constructs a new instance.
	 *
	 * @param list List to be wrapped.
	 */
	public UnmodifiableAugmentedList( final AugmentedList<? extends E> list )
	{
		_list = list;
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

	@Override
	@NotNull
	public Object[] toArray()
	{
		return _list.toArray();
	}

	@Override
	@NotNull
	public <T> T[] toArray( @NotNull final T[] a )
	{
		//noinspection SuspiciousToArrayCall
		return _list.toArray( a );
	}

	public String toString()
	{
		return _list.toString();
	}

	@Override
	@NotNull
	public Iterator<E> iterator()
	{
		return new Iterator<E>()
		{
			@SuppressWarnings( "JavaDoc" )
			private final Iterator<? extends E> _iterator = _list.iterator();

			@Override
			public boolean hasNext()
			{
				return _iterator.hasNext();
			}

			@Override
			public E next()
			{
				return _iterator.next();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public boolean add( final E e )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove( final Object o )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll( @NotNull final Collection<?> coll )
	{
		return _list.containsAll( coll );
	}

	@Override
	public boolean addAll( @NotNull final Collection<? extends E> coll )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll( @NotNull final Collection<?> coll )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll( @NotNull final Collection<?> coll )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear()
	{
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings( { "EqualsWhichDoesntCheckParameterClass" } )
	public boolean equals( final Object o )
	{
		return o == this || _list.equals( o );
	}

	public int hashCode()
	{
		return _list.hashCode();
	}

	@Override
	public E get( final int index )
	{
		return _list.get( index );
	}

	@Override
	public E set( final int index, final E element )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void add( final int index, final E element )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove( final int index )
	{
		throw new UnsupportedOperationException();
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

	@Override
	public boolean addAll( final int index, @NotNull final Collection<? extends E> collection )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	@NotNull
	public ListIterator<E> listIterator()
	{
		return listIterator( 0 );
	}

	@Override
	@NotNull
	public ListIterator<E> listIterator( final int index )
	{
		return new ListIterator<E>()
		{
			@SuppressWarnings( "JavaDoc" )
			private final ListIterator<? extends E> _iterator = _list.listIterator( index );

			@Override
			public boolean hasNext()
			{
				return _iterator.hasNext();
			}

			@Override
			public E next()
			{
				return _iterator.next();
			}

			@Override
			public boolean hasPrevious()
			{
				return _iterator.hasPrevious();
			}

			@Override
			public E previous()
			{
				return _iterator.previous();
			}

			@Override
			public int nextIndex()
			{
				return _iterator.nextIndex();
			}

			@Override
			public int previousIndex()
			{
				return _iterator.previousIndex();
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void set( final E e )
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void add( final E e )
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	@NotNull
	public List<E> subList( final int fromIndex, final int toIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void ensureCapacity( final int capacity )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeRange( final int startIndex, final int endIndex )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setLength( final int length )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAll( final List<? extends E> list )
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E getFirst()
	{
		return _list.getFirst();
	}

	@Override
	public E getLast()
	{
		return _list.getLast();
	}

	@Override
	public E removeFirst()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public E removeLast()
	{
		throw new UnsupportedOperationException();
	}
}
