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
 * This implementation of {@link Enumeration} is used to combine the keys
 * contained within an optional parent {@link ResourceBundle} with the keys
 * contained within the specified {@link Set}}.
 *
 * @author Peter S. Heijnen
 */
public class CombinedBundleEnumeration
implements Enumeration<String>
{
	/**
	 * Enumeration of keys from parent (may be {@code null}; will be set to
	 * {@code null} when enumeration is complete).
	 */
	Enumeration<String> _parentKeys;

	/**
	 * Set of keys to return from this enumeration (may be {@code null}; will be
	 * set to {@code null} when parent enumeration is complete).
	 */
	Collection<String> _keys;

	/**
	 * Iterator  of keys to return from this enumeration (may be {@code null};
	 * will be set to {@code null} when iterator is complete).
	 */
	Iterator<String> _keyIterator;

	/**
	 * Pre-fetched next element ({@code null} if not yet pre-fetched, or no next
	 * element is available).
	 */
	String _next;

	/**
	 * Create combined resource bundle key enumeration.
	 *
	 * @param parent Parent bundle.
	 * @param keys   Collection of bundle keys.
	 */
	@SuppressWarnings( "AssignmentToCollectionOrArrayFieldFromParameter" )
	CombinedBundleEnumeration( @Nullable final ResourceBundle parent, @NotNull final Collection<String> keys )
	{
		_parentKeys = ( parent != null ) ? parent.getKeys() : null;
		_keys = keys;
		_keyIterator = keys.iterator();
		_next = null;
	}

	@Override
	public boolean hasMoreElements()
	{
		String next = _next;
		if ( next == null )
		{
			final Enumeration<String> parentKeys = _parentKeys;
			if ( parentKeys != null )
			{
				final Collection<String> keys = _keys;

				while ( ( next == null ) && parentKeys.hasMoreElements() )
				{
					next = parentKeys.nextElement();
					if ( ( keys != null ) && keys.contains( next ) )
					{
						next = null;
					}
				}

				if ( next == null )
				{
					_parentKeys = null;
					_keys = null;
				}
			}

			if ( next == null )
			{
				final Iterator<String> keyIterator = _keyIterator;
				if ( keyIterator != null )
				{
					if ( keyIterator.hasNext() )
					{
						next = keyIterator.next();
					}
					else
					{
						_keyIterator = null;
					}
				}
			}

			_next = next;
		}

		return ( next != null );
	}

	@Override
	public String nextElement()
	{
		if ( !hasMoreElements() )
		{
			throw new NoSuchElementException( "finished" );
		}

		final String result = _next;
		_next = null;
		return result;
	}
}
