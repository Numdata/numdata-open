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
 * A combination of a linked list and a hash table. The main advantage of this
 * data structure is that it offers constant-time performance on the {@link
 * #remove(Object)} and {@link #contains(Object)} operations. As a trade-off,
 * operations that are dependent on the index of an element or the size of the
 * list offer only linear-time performance.
 *
 * @author G. Meinders
 */
public class LinkedHashList<E>
extends AbstractList<E>
implements Deque<E>
{
	/**
	 * First element in the linked list of entries. Note that internally, the
	 * linked list is actually cyclic.
	 */
	Entry<E> _first;

	/**
	 * Hash-table of entries.
	 */
	private Entry<E>[] _entries;

	/**
	 * Number of entries.
	 */
	private int _size;

	/**
	 * Size at which the current capacity of the list will be increased.
	 */
	private int _threshold;

	/**
	 * Size of the list, relative to its capacity, at which the capacity of the
	 * list is increased.
	 */
	private final double _loadFactor;

	/**
	 * Constructs a new linked hash list. The created list has the default
	 * initial capacity (16) and the default load factor (0.75).
	 */
	public LinkedHashList()
	{
		this( 16, 0.75 );
	}

	/**
	 * Constructs a new linked hash list. The created list has the default load
	 * factor (0.75) and an initial capacity sufficient to contain the elements
	 * in the given collection.
	 *
	 * @param collection Collection whose elements are to be placed in the
	 *                   list.
	 */
	@SuppressWarnings( "OverridableMethodCallDuringObjectConstruction" )
	public LinkedHashList( final Collection<? extends E> collection )
	{
		this( Math.max( (int)( (float)collection.size() / 0.75f ), 16 ), 0.75 );
		addAll( collection );
	}

	/**
	 * Constructs a new linked hash list.
	 *
	 * @param initialCapacity Initial capacity.
	 * @param loadFactor      Maximum ratio between size and capacity.
	 */
	public LinkedHashList( final int initialCapacity, final double loadFactor )
	{
		int capacity = 1;
		while ( capacity < initialCapacity )
		{
			capacity <<= 1;
		}

		_size = 0;
		_loadFactor = loadFactor;
		_threshold = (int)( (double)capacity * loadFactor );

		_first = null;
		//noinspection unchecked,rawtypes
		_entries = new Entry[ capacity ];
	}

	@Override
	public boolean add( final E value )
	{
		addLast( value );
		return true;
	}

	@Override
	public void add( final int index, final E value )
	{
		addEntry( index, value );
	}

	/**
	 * Adds the given value to the list at the specified index.
	 *
	 * @param index Index.
	 * @param value Value to be added.
	 *
	 * @return Added entry.
	 */
	private Entry<E> addEntry( final int index, final E value )
	{
		final Entry<E> result;

		if ( index == 0 )
		{
			result = addFirstEntry( value );
		}
		else if ( index == _size )
		{
			result = addLastEntry( value );
		}
		else
		{
			final Entry<E> entry = new Entry<E>( value );
			final int hash = entry._hash;

			addToHashTable( entry, indexFor( hash ) );
			addToLinkedList( entry, index );

			result = entry;
		}

		return result;
	}

	@Override
	public void addFirst( final E value )
	{
		addFirstEntry( value );
	}

	/**
	 * Adds the given value to the start of the list.
	 *
	 * @param value Value to be added.
	 *
	 * @return Added entry.
	 */
	private Entry<E> addFirstEntry( final E value )
	{
		final Entry<E> entry = new Entry<E>( value );
		final int hash = entry._hash;
		final int index = indexFor( hash );

		addToHashTable( entry, index );

		Entry<E> first = _first;
		if ( first == null )
		{
			first = entry;
		}
		else
		{
			final Entry<E> last = first._before;
			last.insert( entry );
			first = entry;
		}
		_first = first;

		return entry;
	}

	@Override
	public void addLast( final E value )
	{
		addLastEntry( value );
	}

	/**
	 * Adds the given value to the end of the list.
	 *
	 * @param value Value to be added.
	 *
	 * @return Added entry.
	 */
	private Entry<E> addLastEntry( final E value )
	{
		final Entry<E> entry = new Entry<E>( value );
		final int hash = entry._hash;
		final int index = indexFor( hash );

		addToHashTable( entry, index );

		final Entry<E> first = _first;
		if ( first == null )
		{
			_first = entry;
		}
		else
		{
			final Entry<E> last = first._before;
			last.insert( entry );
		}

		return entry;
	}

	@Override
	public boolean addAll( @NotNull final Collection<? extends E> collection )
	{
		final Iterator<? extends E> iterator = collection.iterator();
		final boolean result = iterator.hasNext();
		if ( result )
		{
			addAllAfterEntry( iterator, addLastEntry( iterator.next() ) );
		}
		return result;
	}

	@Override
	public boolean addAll( final int index, final Collection<? extends E> collection )
	{
		final Iterator<? extends E> iterator = collection.iterator();
		final boolean result = iterator.hasNext();
		if ( result )
		{
			addAllAfterEntry( iterator, addEntry( index, iterator.next() ) );
		}
		return result;
	}

	/**
	 * Adds all remaining values in the given iterator to the list, after the
	 * given entry.
	 *
	 * @param iterator Provides values to be added.
	 * @param first    Entries are added after this entry.
	 */
	private void addAllAfterEntry( final Iterator<? extends E> iterator, final Entry<E> first )
	{
		Entry<E> current = first;
		while ( iterator.hasNext() )
		{
			final Entry<E> next = new Entry<E>( iterator.next() );
			current.insert( next );
			addToHashTable( next, indexFor( next._hash ) );
			current = next;
		}
	}

	@Override
	public void clear()
	{
		final Entry<E>[] entries = _entries;
		for ( int i = 0; i < entries.length; i++ )
		{
			entries[ i ] = null;
		}
		_first = null;
		_size = 0;
	}

	@Override
	public boolean contains( final Object o )
	{
		boolean result = false;

		final int hash = hash( o );
		final int index = indexFor( hash );
		Entry<E> entry = _entries[ index ];

		while ( entry != null )
		{
			if ( ( o == null ) ? ( entry._value == null ) : o.equals( entry._value ) )
			{
				result = true;
				break;
			}
			entry = entry._next;
		}

		return result;
	}

	@Override
	public boolean containsAll( @NotNull final Collection<?> collection )
	{
		boolean result = true;
		for ( final Object o : collection )
		{
			if ( !contains( o ) )
			{
				result = false;
				break;
			}
		}
		return result;
	}

	@Override
	public E get( final int index )
	{
		return getFromLinkedList( index )._value;
	}

	@Override
	public int indexOf( final Object o )
	{
		int result = -1;

		final Entry<E> first = _first;
		if ( first != null )
		{
			int index = 0;
			Entry<E> element = first;
			//noinspection ObjectEquality
			do
			{
				if ( ( o == null ) ? ( element._value == null ) : o.equals( element._value ) )
				{
					result = index;
					break;
				}
				index++;
				element = element._after;
			}
			while ( element != first );
		}

		return result;
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@NotNull
	@Override
	public Iterator<E> iterator()
	{
		return listIterator();
	}

	@NotNull
	@Override
	public Iterator<E> descendingIterator()
	{
		return new DescendingIterator();
	}

	@Override
	public int lastIndexOf( final Object o )
	{
		int result = -1;

		final Entry<E> first = _first;
		if ( first != null )
		{
			final Entry<E> last = first._before;
			int index = _size - 1;
			Entry<E> element = last;
			//noinspection ObjectEquality
			do
			{
				if ( ( o == null ) ? ( element._value == null ) : o.equals( element._value ) )
				{
					result = index;
					break;
				}
				index--;
				element = element._before;
			}
			while ( element != last );
		}

		return result;
	}

	@NotNull
	@Override
	public ListIterator<E> listIterator()
	{
		return new ElementIterator( null, _first, 0 );
	}

	@NotNull
	@Override
	public ListIterator<E> listIterator( final int index )
	{
		final Entry<E> next = ( index == 0 ) ? _first : ( index == _size ) ? null : getFromLinkedList( index );
		final Entry<E> previous = ( index == 0 ) ? null : ( index == _size ) ? _first._before : next._before;
		return new ElementIterator( previous, next, index );
	}

	@Override
	public boolean offer( final E e )
	{
		add( e );
		return true;
	}

	@Override
	public E remove()
	{
		return removeFirst();
	}

	@Nullable
	@Override
	public E poll()
	{
		return pollFirst();
	}

	@Override
	public E element()
	{
		return getFirst();
	}

	@Nullable
	@Override
	public E peek()
	{
		return peekFirst();
	}

	@Override
	public void push( final E e )
	{
		addFirst( e );
	}

	@Override
	public E pop()
	{
		return removeFirst();
	}

	@Override
	public boolean offerFirst( final E e )
	{
		addFirst( e );
		return true;
	}

	@Override
	public boolean offerLast( final E e )
	{
		addLast( e );
		return true;
	}

	@Override
	public E removeFirst()
	{
		final Entry<E> first = _first;
		if ( first == null )
		{
			throw new NoSuchElementException( "empty" );
		}

		removeFromHashTable( first );
		removeFromLinkedList( first );

		return first._value;
	}

	@Override
	public E removeLast()
	{
		final Entry<E> first = _first;
		if ( first == null )
		{
			throw new NoSuchElementException( "empty" );
		}
		final Entry<E> last = first._before;

		removeFromHashTable( last );
		removeFromLinkedList( last );

		return last._value;
	}

	@Nullable
	@Override
	public E pollFirst()
	{
		return ( _first == null ) ? null : removeFirst();
	}

	@Nullable
	@Override
	public E pollLast()
	{
		return ( _first == null ) ? null : removeLast();
	}

	@Override
	public E getFirst()
	{
		if ( _first == null )
		{
			throw new NoSuchElementException( "empty" );
		}
		return _first._value;
	}

	@Override
	public E getLast()
	{
		if ( _first == null )
		{
			throw new NoSuchElementException( "empty" );
		}
		return _first._before._value;
	}

	@Nullable
	@Override
	public E peekFirst()
	{
		return ( _first == null ) ? null : _first._value;
	}

	@Nullable
	@Override
	public E peekLast()
	{
		return ( _first == null ) ? null : _first._before._value;
	}

	@Override
	public boolean removeFirstOccurrence( final Object o )
	{
		boolean result = false;

		final Entry<E> first = _first;
		if ( first != null )
		{
			Entry<E> current = first;
			//noinspection ObjectEquality
			do
			{
				if ( ( o == null ) ? ( current._value == null ) : o.equals( current._value ) )
				{
					removeFromHashTable( current );
					removeFromLinkedList( current );
					result = true;
					break;
				}
				current = current._after;
			}
			while ( current != first );
		}

		return result;
	}

	@Override
	public boolean removeLastOccurrence( final Object o )
	{
		boolean result = false;

		final Entry<E> first = _first;
		if ( first != null )
		{
			final Entry<E> last = first._before;
			Entry<E> current = last;
			//noinspection ObjectEquality
			do
			{
				if ( ( o == null ) ? ( current._value == null ) : o.equals( current._value ) )
				{
					removeFromHashTable( current );
					removeFromLinkedList( current );
					result = true;
					break;
				}
				current = current._before;
			}
			while ( current != last );
		}

		return result;
	}

	@Override
	public E remove( final int index )
	{
		final Entry<E> entry = getFromLinkedList( index );
		removeFromHashTable( entry );
		removeFromLinkedList( entry );
		return entry._value;
	}

	@Override
	public boolean remove( final Object o )
	{
		final Entry<E> entry = removeValueFromHashTable( o );
		final boolean removed = entry != null;
		if ( removed )
		{
			removeFromLinkedList( entry );
		}
		return removed;
	}

	/**
	 * Removes an entry for the given object, if any, from the hash table.
	 *
	 * @param o Object to be removed.
	 *
	 * @return Entry that was removed, if any.
	 */
	@Nullable
	private Entry<E> removeValueFromHashTable( final Object o )
	{
		Entry<E> result = null;

		final int index = indexFor( hash( o ) );
		Entry<E> current = _entries[ index ];
		Entry<E> previous = null;

		while ( current != null )
		{
			final E value = current._value;
			if ( ( o == null ) ? ( value == null ) : o.equals( value ) )
			{
				if ( previous == null )
				{
					_entries[ index ] = current._next;
				}
				else
				{
					previous._next = current._next;
				}
				_size--;
				result = current;
				break;
			}

			previous = current;
			current = current._next;
		}

		return result;
	}

	/**
	 * Removes the given entry from the hash table.
	 *
	 * @param entry Entry to be removed.
	 *
	 * @return Entry that was removed, if any.
	 */
	@Nullable
	@SuppressWarnings( "UnusedReturnValue" )
	private Entry<E> removeFromHashTable( final Entry<E> entry )
	{
		Entry<E> result = null;

		final int index = indexFor( entry._hash );
		Entry<E> current = _entries[ index ];
		Entry<E> previous = null;

		while ( current != null )
		{
			//noinspection ObjectEquality
			if ( current == entry )
			{
				if ( previous == null )
				{
					_entries[ index ] = current._next;
				}
				else
				{
					previous._next = current._next;
				}
				_size--;
				result = current;
				break;
			}

			previous = current;
			current = current._next;
		}

		return result;
	}

	/**
	 * Removes the given entry from the linked list.
	 *
	 * @param entry Entry to be removed.
	 */
	@SuppressWarnings( "ObjectEquality" )
	private void removeFromLinkedList( final Entry<E> entry )
	{
		final Entry<E> before = entry._before;
		final Entry<E> after = entry._after;

		before._after = after;
		after._before = before;

		if ( entry == _first )
		{
			_first = ( entry == after ) ? null : after;
		}
	}

	@Override
	public boolean removeAll( @NotNull final Collection<?> collection )
	{
		boolean result = false;
		for ( final Object o : collection )
		{
			result |= remove( o );
		}
		return result;
	}

	@Override
	public E set( final int index, final E element )
	{
		final Entry<E> entry = getFromLinkedList( index );
		return set( entry, element );
	}

	/**
	 * Sets the value of the given entry and updates the hash table if needed.
	 *
	 * @param entry Entry to set the value of.
	 * @param value Value to be set.
	 *
	 * @return Replaced value.
	 */
	private E set( final Entry<E> entry, final E value )
	{
		final E result = entry._value;

		//noinspection ObjectEquality
		if ( result != value )
		{
			final int oldHashTableIndex = indexFor( entry._hash );
			entry.set( value );
			final int newHashTableIndex = indexFor( entry._hash );

			if ( oldHashTableIndex != newHashTableIndex )
			{
				removeFromHashTable( entry );
				addToHashTable( entry, indexFor( entry._hash ) );
			}
		}

		return result;
	}

	@Override
	public int size()
	{
		return _size;
	}

	/**
	 * Returns the hashcode of the given object, or zero if the object is {@code
	 * null}.
	 *
	 * @param object Object to get the hashcode of.
	 *
	 * @return Hashcode of the given object.
	 */
	private static int hash( final Object object )
	{
		return ( object == null ) ? 0 : object.hashCode();
	}

	/**
	 * Adds the given entry to the hash-table.
	 *
	 * @param entry Entry to be added.
	 * @param index Index of the entry's hashcode in the hash-table.
	 */
	private void addToHashTable( final Entry<E> entry, final int index )
	{
		final Entry<E>[] entries = _entries;
		entry._next = entries[ index ];
		entries[ index ] = entry;

		if ( _size++ >= _threshold )
		{
			resize( 2 * entries.length );
		}
	}

	/**
	 * Adds the given entry to the linked list.
	 *
	 * @param entry Entry to be added.
	 * @param index Index to add the entry at.
	 */
	private void addToLinkedList( final Entry<E> entry, final int index )
	{
		final Entry<E> previous = getFromLinkedList( index - 1 );
		previous.insert( entry );
	}

	/**
	 * Resizes the hash-table to the given size.
	 *
	 * @param newSize New size of the hash-table. Must be a power of two.
	 */
	private void resize( final int newSize )
	{
		final Entry<E>[] entries = _entries;

		_size = 0;
		//noinspection unchecked,rawtypes
		_entries = new Entry[ newSize ];
		_threshold = (int)( (double)newSize * _loadFactor );

		for ( final Entry<E> entry : entries )
		{
			Entry<E> current = entry;
			while ( current != null )
			{
				final Entry<E> next = current._next;
				current._next = null;
				addToHashTable( current, indexFor( entry._hash ) );
				current = next;
			}
		}
	}

	/**
	 * Returns the index in the hash-table for objects with the given hashcode.
	 *
	 * @param hash Hashcode of the object(s).
	 *
	 * @return Index for values with the given hashcode.
	 */
	private int indexFor( final int hash )
	{
		return hash & ( _entries.length - 1 );
	}

	/**
	 * Returns the specified entry from the linked list.
	 *
	 * @param index Index of the entry.
	 *
	 * @return Entry at the given index.
	 */
	private Entry<E> getFromLinkedList( final int index )
	{
		if ( index < 0 )
		{
			throw new IndexOutOfBoundsException( "index " + index );
		}

		final int size = _size;
		if ( index >= size )
		{
			throw new IndexOutOfBoundsException( "index " + index + ", size " + size );
		}

		Entry<E> result;
		if ( index > size / 2 )
		{
			result = _first;
			for ( int i = size; i > index; i-- )
			{
				result = result._before;
			}
		}
		else
		{
			result = _first;
			for ( int i = 0; i < index; i++ )
			{
				result = result._after;
			}
		}
		return result;
	}

	/**
	 * Entry in the hash-table and linked list.
	 */
	@SuppressWarnings( "FinalClass" )
	private static final class Entry<E>
	{
		/**
		 * Value of the entry.
		 */
		private E _value;

		/**
		 * Hashcode of the value.
		 */
		private int _hash;

		/**
		 * Next element as the same position in the hash-table.
		 */
		private Entry<E> _next;

		/**
		 * Previous entry in the linked list.
		 */
		private Entry<E> _before;

		/**
		 * Next entry in the linked list.
		 */
		private Entry<E> _after;

		/**
		 * Constructs a new entry with the given value.
		 *
		 * @param value Value of the entry.
		 */
		private Entry( final E value )
		{
			_value = value;
			_hash = hash( value );
			_next = null;
			_before = this;
			_after = this;
		}

		/**
		 * Sets the value of the entry. This method does not update the hash
		 * table; see {@link LinkedHashList#set(Entry, Object)}.
		 *
		 * @param value New value.
		 *
		 * @return Old value.
		 */
		public E set( final E value )
		{
			final E result = _value;
			_value = value;
			_hash = hash( value );
			return result;
		}

		/**
		 * Inserts the given entry to the linked list after this entry.
		 *
		 * @param entry Entry to be inserted.
		 */
		public void insert( final Entry<E> entry )
		{
			final Entry<E> after = _after;
			entry._before = this;
			entry._after = after;
			after._before = entry;
			_after = entry;
		}

		/**
		 * Returns the value of the entry.
		 *
		 * @return Value of the entry.
		 */
		public E getValue()
		{
			return _value;
		}

		public String toString()
		{
			return "Entry[" + _value + "]@" + Integer.toHexString( System.identityHashCode( this ) );
		}
	}

	private class ElementIterator
	implements ListIterator<E>
	{
		/**
		 * Entry before the cursor of the iterator.
		 */
		private Entry<E> _previous;

		/**
		 * Entry after the cursor of the iterator.
		 */
		private Entry<E> _next;

		/**
		 * Index of {@link #_next} in the list.
		 */
		private int _nextIndex;

		/**
		 * Last entry returned by {@link #next()} or {@link #previous()}.
		 */
		private Entry<E> _lastIterated;

		/**
		 * Constructs a new iterator.
		 *
		 * @param previous  Entry before the cursor, if any.
		 * @param next      Entry after the cursor, if any.
		 * @param nextIndex Index of {@code next} in the list.
		 */
		private ElementIterator( @Nullable final Entry<E> previous, @Nullable final Entry<E> next, final int nextIndex )
		{
			_previous = previous;
			_next = next;
			_nextIndex = nextIndex;
			_lastIterated = null;
		}

		@Override
		public boolean hasNext()
		{
			return ( _next != null );
		}

		@Override
		public E next()
		{
			final Entry<E> current = _next;
			if ( current == null )
			{
				throw new NoSuchElementException( "finished" );
			}

			_previous = current;
			//noinspection ObjectEquality
			_next = ( current._after == _first ) ? null : current._after;
			_nextIndex++;
			_lastIterated = current;

			return current._value;
		}

		@Override
		public void remove()
		{
			final Entry<E> removable = _lastIterated;
			if ( removable == null )
			{
				throw new IllegalStateException( "no active element" );
			}
			_lastIterated = null;

			//noinspection ObjectEquality
			if ( removable == _previous )
			{
				//noinspection ObjectEquality
				_previous = ( removable == _first ) ? null : removable._before;
				_nextIndex--;
			}
			else //noinspection ObjectEquality
				if ( removable == _next )
				{
					//noinspection ObjectEquality
					_next = ( removable._after == _first ) ? null : removable._after;
				}

			removeFromHashTable( removable );
			removeFromLinkedList( removable );
		}

		@Override
		public boolean hasPrevious()
		{
			return ( _previous != null );
		}

		@Override
		public E previous()
		{
			final Entry<E> current = _previous;
			if ( current == null )
			{
				throw new NoSuchElementException( "finished" );
			}

			//noinspection ObjectEquality
			_previous = ( current == _first ) ? null : current._before;
			_next = current;
			_nextIndex--;
			_lastIterated = current;

			return current._value;
		}

		@Override
		public int nextIndex()
		{
			return _nextIndex;
		}

		@Override
		public int previousIndex()
		{
			return _nextIndex - 1;
		}

		@Override
		public void set( final E value )
		{
			final Entry<E> lastIterated = _lastIterated;
			if ( lastIterated == null )
			{
				throw new IllegalStateException( "no active element" );
			}
			LinkedHashList.this.set( lastIterated, value );
		}

		@Override
		public void add( final E value )
		{
			final Entry<E> previous = _previous;

			final Entry<E> added;
			if ( previous == null )
			{
				addFirst( value );
				added = _first;
			}
			else
			{
				added = new Entry<E>( value );
				previous.insert( added );
			}

			_previous = added;
			_nextIndex++;
			_lastIterated = null;
		}
	}

	/**
	 * Iterators over the elements in the deque in descending order.
	 */
	private class DescendingIterator
	implements Iterator<E>
	{
		/**
		 * Next entry that will be iterated.
		 */
		private Entry<E> _next;

		/**
		 * Last entry returned by {@link #next()}.
		 */
		private Entry<E> _removable;

		/**
		 * Constructs a new instance.
		 */
		DescendingIterator()
		{
			final Entry<E> first = _first;
			_next = ( first == null ) ? null : first._before;
			_removable = null;
		}

		@Override
		public boolean hasNext()
		{
			return ( _next != null );
		}

		@Override
		public E next()
		{
			final Entry<E> current = _next;
			if ( current == null )
			{
				throw new NoSuchElementException( "finished" );
			}

			_removable = current;
			//noinspection ObjectEquality
			_next = ( current == _first ) ? null : current._before;

			return current._value;
		}

		@Override
		public void remove()
		{
			final Entry<E> removable = _removable;
			if ( removable == null )
			{
				throw new IllegalStateException( "no active element" );
			}
			_removable = null;

			removeFromHashTable( removable );
			removeFromLinkedList( removable );
		}
	}
}
