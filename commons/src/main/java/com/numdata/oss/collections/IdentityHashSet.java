/*
 * Copyright (c) 2012-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.collections;

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class is a {@link Set} implementation based on object identity instead
 * of equality.
 *
 * Just like {@link IdentityHashMap} (which is actually used as backing store),
 * this is not a general-purpose set, because it intentionally violates the
 * general contract, which mandates the use of the {@link Object#equals} method
 * when comparing objects.
 *
 * @param <E> the type of elements maintained by this set
 *
 * @author Peter S. Heijnen
 */
public class IdentityHashSet<E>
	extends AbstractSet<E>
	implements Cloneable
{
	/**
	 * The backing {@link IdentityHashMap}.
	 */
	private IdentityHashMap<E,Object> _map;

	/**
	 * Dummy value to associate with all keys.
	 */
	private static final Object PRESENT = new Object();

	/**
	 * Constructs a new, empty identity hash set with a default expected
	 * maximum size (21).
	 */
	public IdentityHashSet()
	{
		_map = new IdentityHashMap<E,Object>();
	}

	/**
	 * Constructs a new set containing the elements in the specified collection.
	 *
	 * @param   collection  Collection to place in set.
	 */
	public IdentityHashSet( @NotNull final Collection<? extends E> collection )
	{
		_map = new IdentityHashMap<E,Object>( collection.size() );
		addAll( collection );
	}

	/**
	 * Constructs a new, empty map with the specified expected maximum size.
	 *
	 * @param   expectedMaxSize     Expected maximum size of the map
	 */
	public IdentityHashSet( final int expectedMaxSize )
	{
		_map = new IdentityHashMap<E,Object>( expectedMaxSize );
	}

	@NotNull
	@Override
	public Iterator<E> iterator()
	{
		return _map.keySet().iterator();
	}

	@Override
	public int size()
	{
		return _map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return _map.isEmpty();
	}

	@Override
	public boolean contains( final Object object )
	{
		return _map.containsKey( object );
	}

	@Override
	public boolean add( final E object )
	{
		return ( _map.put( object, PRESENT ) == null );
	}

	@Override
	public boolean remove( final Object object )
	{
		return ( _map.remove( object ) == PRESENT );
	}

	@Override
	public void clear()
	{
		_map.clear();
	}

	@Override
	public IdentityHashSet<E> clone()
	{
		try
		{
			final IdentityHashSet<E> clone = (IdentityHashSet<E>)super.clone();
			clone._map = (IdentityHashMap<E, Object>)_map.clone();
			return clone;
		}
		catch ( final CloneNotSupportedException e )
		{
			throw new AssertionError( e );
		}
	}
}
