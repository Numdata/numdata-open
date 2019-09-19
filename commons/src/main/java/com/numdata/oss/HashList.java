/*
 * Copyright (c) 2009-2017, Numdata BV, The Netherlands.
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
 * Adds index hashing to a {@link ArrayList} to provide fast {@link #contains}
 * and {@link #indexOf} lookups. Modifications are rather costly, especially
 * adding/removing any element other than the element at the end of the list.
 *
 * @param <E> Element type.
 *
 * @author Peter S. Heijnen
 */
public class HashList<E>
extends ArrayList<E>
{
	/**
	 * Serialize data version.
	 */
	private static final long serialVersionUID = -2142544575701848415L;

	/**
	 * Maps {@link Object#hashCode()} to {@link TreeSet}s with element indices.
	 */
	private final Map<Integer, TreeSet<Integer>> _indexHashmap = new HashMap<Integer, TreeSet<Integer>>();

	/**
	 * Construct empty list.
	 */
	public HashList()
	{
	}

	/**
	 * Construct list will all contents from the specified collection.
	 *
	 * @param collection Collection with initial contents.
	 */
	@SuppressWarnings( "OverridableMethodCallDuringObjectConstruction" )
	public HashList( final Collection<? extends E> collection )
	{
		addAll( collection );
	}

	@Override
	public boolean add( final E element )
	{
		add( size(), element );
		return true;
	}

	/**
	 * Convenience method to implement common application of {@link HashList}:
	 * get index of {@code element} in list if it already exists or add it to
	 * the end of the list, and return the index of the {@code element} in the
	 * list.
	 *
	 * @param element Element to get index of or add to end of the list.
	 *
	 * @return Index of object in list.
	 */
	public int indexOfOrAdd( @NotNull final E element )
	{
		int result = -1;

		final Map<Integer, TreeSet<Integer>> indexHashmap = _indexHashmap;
		final Integer hashCode = element.hashCode();

		TreeSet<Integer> indices = indexHashmap.get( hashCode );
		if ( indices != null )
		{
			for ( final int index : indices )
			{
				if ( element.equals( get( index ) ) )
				{
					result = index;
					break;
				}
			}
		}

		if ( result < 0 )
		{
			/*
			 * Add index to map.
			 */
			if ( indices == null )
			{
				indices = new TreeSet<Integer>();
				indexHashmap.put( hashCode, indices );
			}

			final int size = size();
			indices.add( size );
			super.add( size, element );
			result = size;
		}

		return result;
	}

	@Override
	public void add( final int index, final E element )
	{
		final Map<Integer, TreeSet<Integer>> indexHashmap = _indexHashmap;

		/*
		 * Increment index of all trailing elements.
		 */
		if ( index < size() )
		{
			for ( final Map.Entry<Integer, TreeSet<Integer>> entry : indexHashmap.entrySet() )
			{
				final TreeSet<Integer> newIndices = new TreeSet<Integer>();

				for ( final Integer i : entry.getValue() )
				{
					if ( i >= index )
					{
						newIndices.add( i + 1 );
					}
					else
					{
						newIndices.add( i );
					}
				}

				entry.setValue( newIndices );
			}
		}

		/*
		 * Add index to map.
		 */
		final Integer hashCode = element.hashCode();

		TreeSet<Integer> indices = indexHashmap.get( hashCode );
		if ( indices == null )
		{
			indices = new TreeSet<Integer>();
			indexHashmap.put( hashCode, indices );
		}

		indices.add( index );
		super.add( index, element );
	}

	@Override
	public boolean addAll( final Collection<? extends E> collection )
	{
		for ( final E element : collection )
		{
			add( element );
		}

		return !collection.isEmpty();
	}

	@Override
	public boolean addAll( final int index, final Collection<? extends E> collection )
	{
		int i = index;

		for ( final E element : collection )
		{
			add( i++, element );
		}

		return !collection.isEmpty();
	}

	@Override
	public void clear()
	{
		super.clear();
		_indexHashmap.clear();
	}

	@Override
	public boolean contains( final Object object )
	{
		return ( indexOf( object ) >= 0 );
	}

	@Override
	public boolean containsAll( @NotNull final Collection<?> collection )
	{
		boolean result = true;

		for ( final Object element : collection )
		{
			if ( !contains( element ) )
			{
				result = false;
				break;
			}
		}

		return result;
	}

	@Override
	public int indexOf( final Object object )
	{
		int result = -1;

		final Iterable<Integer> indices = _indexHashmap.get( object.hashCode() );
		if ( indices != null )
		{
			for ( final int index : indices )
			{
				if ( object.equals( get( index ) ) )
				{
					result = index;
					break;
				}
			}
		}

		return result;
	}

	@Override
	public int lastIndexOf( final Object object )
	{
		int result = -1;

		final NavigableSet<Integer> indices = _indexHashmap.get( object.hashCode() );
		if ( indices != null )
		{
			for ( final int index : indices.descendingSet() )
			{
				if ( object.equals( get( index ) ) )
				{
					result = index;
					break;
				}
			}
		}

		return result;
	}

	@NotNull
	@Override
	public ListIterator<E> listIterator()
	{
		return new HashListIterator();
	}

	@Override
	public E remove( final int index )
	{
		final E element = super.remove( index );

		final Map<Integer, TreeSet<Integer>> indexHashmap = _indexHashmap;

		/*
		 * Remove index from map.
		 */
		final Integer hashCode = element.hashCode();

		final Set<Integer> indices = indexHashmap.get( hashCode );
		if ( indices.size() > 1 )
		{
			indices.remove( index );
		}
		else
		{
			indexHashmap.remove( hashCode );
		}

		/*
		 * Decrement index of all trailing elements.
		 */
		if ( index < size() - 1 )
		{
			for ( final Map.Entry<Integer, TreeSet<Integer>> entry : indexHashmap.entrySet() )
			{
				final TreeSet<Integer> newIndices = new TreeSet<Integer>();

				for ( final Integer i : entry.getValue() )
				{
					if ( i > index )
					{
						newIndices.add( i - 1 );
					}
					else
					{
						newIndices.add( i );
					}
				}

				entry.setValue( newIndices );
			}
		}

		return element;
	}

	@Override
	public boolean remove( final Object object )
	{
		final boolean result;

		final int index = indexOf( object );
		if ( index >= 0 )
		{
			remove( index );
			result = true;
		}
		else
		{
			result = false;
		}

		return result;
	}

	@Override
	public boolean removeAll( @NotNull final Collection<?> collection )
	{
		boolean result = false;

		for ( final Object element : collection )
		{
			if ( remove( element ) )
			{
				result = true;
			}
		}

		return result;
	}

	@Override
	public boolean retainAll( @NotNull final Collection<?> collection )
	{
		boolean result = false;

		int index = 0;
		while ( index < size() )
		{
			final E element = get( index );
			if ( !collection.contains( element ) )
			{
				remove( index );
				result = true;
			}
			else
			{
				index++;
			}
		}

		return result;
	}

	@Override
	protected void removeRange( final int fromIndex, final int toIndex )
	{
		for ( int toRemove = toIndex - fromIndex; toRemove > 0; toRemove-- )
		{
			remove( fromIndex );
		}
	}

	@Override
	public E set( final int index, final E element )
	{
		final E oldElement = get( index );

		//noinspection ObjectEquality
		if ( element != oldElement )
		{
			final Map<Integer, TreeSet<Integer>> indexHashmap = _indexHashmap;

			final Integer indexValue = index;

			Integer hashCode = oldElement.hashCode();

			TreeSet<Integer> indices = indexHashmap.get( hashCode );
			if ( indices.size() > 1 )
			{
				indices.remove( indexValue );
			}
			else
			{
				indexHashmap.remove( hashCode );
			}

			hashCode = element.hashCode();

			indices = indexHashmap.get( hashCode );
			if ( indices == null )
			{
				indices = new TreeSet<Integer>();
				indexHashmap.put( hashCode, indices );
			}

			indices.add( indexValue );
		}

		return super.set( index, element );
	}

	/**
	 * Iterator for {@link HashList}. This extends {@link ListIterator} to make
	 * sure {@link HashList#_indexHashmap} is updated when changes are made
	 * using the iterator's {@link #add}, {@link #set} or {@link #remove}
	 * methods.
	 */
	private class HashListIterator
	implements ListIterator<E>
	{
		/**
		 * Index of current element.
		 */
		int _index = -1;

		/**
		 * Flag to indicate that the current element can be removed.
		 */
		boolean _removable = false;

		/**
		 * Flag to indicate that the current element was removed.
		 */
		boolean _removed = false;

		@Override
		public void add( final E element )
		{
			HashList.this.add( _index++, element );
			_removable = false;
		}

		@Override
		public boolean hasNext()
		{
			return ( _index < size() - 1 );
		}

		@Override
		public boolean hasPrevious()
		{
			return ( _index > 0 );
		}

		@Override
		public E next()
		{
			if ( !hasNext() )
			{
				throw new NoSuchElementException( "finished" );
			}

			int index = _index;
			if ( !_removed )
			{
				_index = ++index;
			}

			_removable = true;
			_removed = false;

			return get( index );
		}

		@Override
		public int nextIndex()
		{
			return ( _removed ? _index : ( _index + 1 ) );
		}

		@Override
		public E previous()
		{
			if ( !hasPrevious() )
			{
				throw new NoSuchElementException( "finished" );
			}

			_removable = true;
			_removed = false;

			return get( --_index );
		}

		@Override
		public int previousIndex()
		{
			return hasPrevious() ? ( _index - 1 ) : -1;
		}

		@Override
		public void remove()
		{
			if ( !_removable )
			{
				throw new IllegalStateException( "can't remove twice" );
			}

			_removable = false;
			_removed = true;

			HashList.this.remove( _index );
		}

		@Override
		public void set( final E value )
		{
			HashList.this.set( _index, value );
		}
	}
}
