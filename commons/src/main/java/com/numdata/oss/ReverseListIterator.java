/*
 * Copyright (c) 2013-2017, Numdata BV, The Netherlands.
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
 * A list iterator that returns elements in reverse order.
 *
 * @author Gerrit Meinders
 */
public class ReverseListIterator<T>
implements ListIterator<T>
{
	/**
	 * Size of the list.
	 */
	private final int _size;

	/**
	 * List iterator (in forward order).
	 */
	private final ListIterator<T> _iterator;

	/**
	 * Constructs a new instance.
	 *
	 * @param list List to iterate over.
	 */
	public ReverseListIterator( final List<T> list )
	{
		final int size = list.size();
		_size = size;
		_iterator = list.listIterator( size );
	}

	@Override
	public boolean hasNext()
	{
		return _iterator.hasPrevious();
	}

	@Override
	public T next()
	{
		return _iterator.previous();
	}

	@Override
	public boolean hasPrevious()
	{
		return _iterator.hasNext();
	}

	@Override
	public T previous()
	{
		return _iterator.next();
	}

	@Override
	public int nextIndex()
	{
		return _size - _iterator.previousIndex() - 1;
	}

	@Override
	public int previousIndex()
	{
		return _size - _iterator.nextIndex();
	}

	@Override
	public void remove()
	{
		_iterator.remove();
	}

	@Override
	public void set( final T t )
	{
		_iterator.set( t );
	}

	@Override
	public void add( final T t )
	{
		_iterator.add( t );
	}
}
