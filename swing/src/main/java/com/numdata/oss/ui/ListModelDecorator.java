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
package com.numdata.oss.ui;

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import org.jetbrains.annotations.*;

/**
 * This decorator implements the {@link ListModel} interface on top of an
 * existing {@link List}.
 *
 * @param   <E>     Element type.
 *
 * @author  Peter S. Heijnen
 */
public class ListModelDecorator<E>
	extends AbstractList<E>
	implements ListModel
{
	/**
	 * {@link List} that is decorated by this class.
	 */
	private final List<E> _decoratedList;

	/**
	 * Registered {@link ListDataListener}s.
	 */
	protected final List<ListDataListener> _listDataListeners = new ArrayList<ListDataListener>();

	/**
	 * Constructs decorator for the given list.
	 *
	 * @param   decoratedList   {@link List} to decorate.
	 */
	public ListModelDecorator( @NotNull final List<E> decoratedList )
	{
		_decoratedList = decoratedList;
	}

	@Override
	public boolean add( final E t )
	{
		final int oldSize = size();
		_decoratedList.add( t );
		fireIntervalAdded( oldSize, oldSize );
		return true;
	}

	@Override
	public void add( final int index, final E element )
	{
		_decoratedList.add( index, element );
		fireIntervalAdded( index, index );
	}

	@Override
	public boolean addAll( final Collection<? extends E> collection )
	{
		final boolean result = _decoratedList.addAll( collection );
		if ( result )
		{
			fireIntervalAdded( size() - collection.size(), size() - 1 );
		}
		return result;
	}

	@Override
	public boolean addAll( final int index, final Collection<? extends E> collection )
	{
		final boolean result = _decoratedList.addAll( index, collection );
		if ( result )
		{
			fireIntervalAdded( index, index + collection.size() - 1 );
		}
		return result;
	}

	/**
	 * Sets all elements of the list, such that it is equal to the given list.
	 *
	 *
	 * @param   list    Elements to be set.
	 */
	public void setAll( final List<E> list )
	{
		if ( ( list != this ) && !equals( list ) )
		{
			final int oldSize = size();
			_decoratedList.clear();
			_decoratedList.addAll( list );

			final int newSize = size();
			final int minSize = Math.min( oldSize, newSize );

			if ( minSize > 0 )
			{
				fireContentsChanged( 0, minSize - 1 );
			}

			if ( oldSize > minSize )
			{
				fireIntervalRemoved( minSize, oldSize - 1 );
			}
			else if ( newSize > minSize )
			{
				fireIntervalAdded( minSize, newSize - 1 );
			}
		}
	}

	@Override
	public void clear()
	{
		final int oldSize = size();

		_decoratedList.clear();

		if ( oldSize > 0 )
		{
			fireIntervalRemoved( 0, oldSize - 1 );
		}
	}

	@Override
	public boolean contains( final Object o )
	{
		return _decoratedList.contains( o );
	}

	@Override
	public boolean containsAll( final Collection<?> c )
	{
		return _decoratedList.containsAll( c );
	}

	@Override
	public boolean equals( final Object o )
	{
		return _decoratedList.equals( o );
	}

	@Override
	public E get( final int index )
	{
		return _decoratedList.get( index );
	}

	@Override
	public int hashCode()
	{
		return _decoratedList.hashCode();
	}

	@Override
	public boolean isEmpty()
	{
		return _decoratedList.isEmpty();
	}

	@Override
	public int indexOf( final Object o )
	{
		return _decoratedList.indexOf( o );
	}

	@Override
	public int lastIndexOf( final Object o )
	{
		return _decoratedList.lastIndexOf( o );
	}

	@Override
	public E remove( final int index )
	{
		final E result = _decoratedList.remove( index );
		fireIntervalRemoved( index, index );
		return result;
	}

	@Override
	public boolean remove( final Object o )
	{
		final int index = indexOf( o );
		if ( index >= 0 )
		{
			remove( index );
		}
		return ( index >= 0 );
	}

	@Override
	protected void removeRange( final int fromIndex, final int toIndex )
	{
		super.removeRange( fromIndex, toIndex );
		fireIntervalRemoved( fromIndex, toIndex);
	}

	@Override
	public E set( final int index, final E element )
	{
		final E result = _decoratedList.set( index, element );
		fireContentsChanged( index, index );
		return result;
	}

	@Override
	public int size()
	{
		return _decoratedList.size();
	}

	@Override
	public Object[] toArray()
	{
		return _decoratedList.toArray();
	}

	@Override
	public <A> A[] toArray( final A[] array )
	{
		return _decoratedList.toArray( array );
	}

	@Override
	public int getSize()
	{
		return size();
	}

	@Override
	public E getElementAt( final int index )
	{
		return get( index );
	}

	@Override
	public void addListDataListener( @NotNull final ListDataListener listener )
	{
		_listDataListeners.add( listener );
	}

	@Override
	public void removeListDataListener( @NotNull final ListDataListener listener )
	{
		_listDataListeners.remove( listener );
	}

	/**
	 * This method must be called after one or more elements were added.
	 *
	 * @param   start   Start of internal (inclusive).
	 * @param   end     End of interval (inclusive).
	 *
	 * @throws  IllegalArgumentException if the specified range is invalid.
	 */
	protected void fireIntervalAdded( final int start, final int end )
	{
		if ( ( start > size() ) && ( end > size() ) )
		{
			throw new IllegalArgumentException( "invalid range: start=" + start + ", end=" + end );
		}

		if ( !_listDataListeners.isEmpty() )
		{
			final ListDataEvent event = new ListDataEvent( this, ListDataEvent.INTERVAL_ADDED, start, end );
			for ( final ListDataListener listener : _listDataListeners )
			{
				listener.intervalAdded( event );
			}
		}
	}

	/**
	 * This method must be called after one or more elements changed.
	 *
	 * @param   start   Start of internal (inclusive).
	 * @param   end     End of interval (inclusive).
	 *
	 * @throws  IllegalArgumentException if the specified range is invalid.
	 */
	protected void fireContentsChanged( final int start, final int end )
	{
		if ( ( start < 0 ) || ( start >= size() ) )
		{
			throw new IllegalArgumentException( "start index out of bounds: " + start );
		}

		if ( ( end < 0 ) || ( end >= size() ) )
		{
			throw new IllegalArgumentException( "end index out of bounds: " + end );
		}

		if ( !_listDataListeners.isEmpty() )
		{
			final ListDataEvent event = new ListDataEvent( this, ListDataEvent.CONTENTS_CHANGED, start, end );
			for ( final ListDataListener listener : _listDataListeners )
			{
				listener.contentsChanged( event );
			}
		}
	}

	/**
	 * This method must be called after one or more elements were removed.
	 *
	 * @param   start   Start of internal (inclusive).
	 * @param   end     End of interval (inclusive).
	 *
	 * @throws  IllegalArgumentException if the specified range is invalid.
	 */
	protected void fireIntervalRemoved( final int start, final int end )
	{
		if ( ( start > size() ) && ( end > size() ) )
		{
			throw new IllegalArgumentException( "invalid range: start=" + start + ", end=" + end );
		}

		if ( !_listDataListeners.isEmpty() )
		{
			final ListDataEvent event = new ListDataEvent( this, ListDataEvent.INTERVAL_REMOVED, start, end );
			for ( final ListDataListener listener : _listDataListeners )
			{
				listener.intervalRemoved( event );
			}
		}
	}
}
