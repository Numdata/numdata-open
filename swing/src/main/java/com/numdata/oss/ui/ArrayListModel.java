/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Implementation of {@link ListModel} on top of an {@link AugmentedArrayList}.
 *
 * @param   <T>     Element type.
 *
 * @author  Peter S. Heijnen
 */
public class ArrayListModel<T>
	extends AugmentedArrayList<T>
	implements ListModel
{
	/**
	 * Registered {@link ListDataListener}s.
	 */
	protected final List<ListDataListener> _listeners = new ArrayList<ListDataListener>();

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 987413958702398754L;

	/**
	 * Constructs an empty list with an initial capacity of ten.
	 */
	public ArrayListModel()
	{
	}

	/**
	 * Constructs an empty list with the specified initial capacity.
	 *
	 * @param   capacity    Initial capacity of the list
	 *
	 * @throws  IllegalArgumentException if the specified capacity is negative
	 */
	public ArrayListModel( final int capacity )
	{
		super( capacity );
	}

	/**
	 * Constructs a list containing the elements of the specified collection.
	 *
	 * @param   collection  Collection whose elements to place into this list
	 */
	public ArrayListModel( @NotNull final Collection<? extends T> collection )
	{
		super( collection );
	}

	@Override
	public boolean add( @Nullable final T t )
	{
		final int oldSize = size();
		super.add( t );
		fireIntervalAdded( oldSize, oldSize );
		return true;
	}

	@Override
	public void add( final int index, @Nullable final T element )
	{
		super.add( index, element );
		fireIntervalAdded( index, index );
	}

	@Override
	public boolean addAll( final Collection<? extends T> collection )
	{
		final boolean result = super.addAll( collection );
		if ( result )
		{
			fireIntervalAdded( size() - collection.size(), size() - 1 );
		}
		return result;
	}

	@Override
	public boolean addAll( final int index, final Collection<? extends T> collection )
	{
		final boolean result = super.addAll( index, collection );
		if ( result )
		{
			fireIntervalAdded( index, index + collection.size() - 1 );
		}
		return result;
	}

	@Override
	public void setAll( final List<? extends T> ts )
	{
		final int removed = size();

		/*
		 * Avoid calling 'super.setAll', because that method calls 'set', 'add'
		 * and 'remove', which are overridden to fire events. Use bulk methods
		 * from 'ArrayList' instead.
		 */
		super.clear();
		super.addAll( ts );

		final int added = size();

		final int changed = Math.min( added, removed );
		if ( changed > 0 )
		{
			fireContentsChanged( 0, changed - 1 );
		}

		if ( added > removed )
		{
			fireIntervalAdded( removed, added - 1 );
		}
		else if ( removed > added )
		{
			fireIntervalRemoved( added, removed - 1 );
		}
	}

	@Override
	public void clear()
	{
		final int oldSize = size();

		super.clear();

		if ( oldSize > 0 )
		{
			fireIntervalRemoved( 0, oldSize - 1 );
		}
	}

	@Override
	public T remove( final int index )
	{
		final T result = super.remove( index );
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
	public void removeRange( final int fromIndex, final int toIndex )
	{
		super.removeRange( fromIndex, toIndex );
		fireIntervalRemoved( fromIndex, toIndex);
	}

	@Override
	public T set( final int index, final T element )
	{
		final T result = super.set( index, element );
		fireContentsChanged( index, index );
		return result;
	}

	@Override
	public int getSize()
	{
		return size();
	}

	@Override
	public T getElementAt( final int index )
	{
		return get( index );
	}

	@Override
	public void addListDataListener( @NotNull final ListDataListener listener )
	{
		_listeners.add( listener );
	}

	@Override
	public void removeListDataListener( @NotNull final ListDataListener listener )
	{
		_listeners.remove( listener );
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

		if ( !_listeners.isEmpty() )
		{
			final ListDataEvent event = new ListDataEvent( this, ListDataEvent.INTERVAL_ADDED, start, end );
			for ( final ListDataListener listener : _listeners )
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

		if ( !_listeners.isEmpty() )
		{
			final ListDataEvent event = new ListDataEvent( this, ListDataEvent.CONTENTS_CHANGED, start, end );
			for ( final ListDataListener listener : _listeners )
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

		if ( !_listeners.isEmpty() )
		{
			final ListDataEvent event = new ListDataEvent( this, ListDataEvent.INTERVAL_REMOVED, start, end );
			for ( final ListDataListener listener : _listeners )
			{
				listener.intervalRemoved( event );
			}
		}
	}
}
