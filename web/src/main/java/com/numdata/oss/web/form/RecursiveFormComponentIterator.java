/*
 * Copyright (c) 2016-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.web.form;

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This iterator performs a recursive pre-order tree walk in a form component
 * tree.
 *
 * @author Peter S. Heijnen
 */
public class RecursiveFormComponentIterator
implements Iterator<FormComponent>
{
	/**
	 * Root note to start iteration at.
	 */
	@NotNull
	private final FormContainer _root;

	/**
	 * Current container node. This container contains the next element, if
	 * any.
	 */
	@NotNull
	private FormContainer _container;

	/**
	 * Index of next element in the current container node, if any. If this
	 * index is not valid, the iteration is completed.
	 */
	private int _childIndex;

	/**
	 * Construct iterator.
	 *
	 * @param root Root node to start iteration at.
	 */
	public RecursiveFormComponentIterator( @NotNull final FormContainer root )
	{
		_root = root;
		_container = root;
		_childIndex = 0;
	}

	@Override
	public boolean hasNext()
	{
		return ( _childIndex < _container.getComponentCount() );
	}

	@Override
	public void remove()
	{

	}

	@Override
	public FormComponent next()
	{
		FormContainer container = _container;
		int childIndex = _childIndex;

		if ( childIndex >= container.getComponentCount() )
		{
			//noinspection NewExceptionWithoutArguments
			throw new NoSuchElementException();
		}

		final FormComponent result = container.getComponent( childIndex );

		if ( ( result instanceof FormContainer ) && ( (FormContainer)result ).getComponentCount() > 0 )
		{
			container = (FormContainer)result;
			childIndex = 0;
		}
		else
		{
			//noinspection ObjectEquality
			while ( ( ++childIndex >= container.getComponentCount() ) && ( container != _root ) )
			{
				final FormContainer parent = container.getParent();
				childIndex = parent.getComponentIndex( container );
				container = parent;
			}
		}

		_container = container;
		_childIndex = childIndex;
		return result;
	}
}
