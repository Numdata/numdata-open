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
import javax.swing.tree.*;

import org.jetbrains.annotations.*;

/**
 * Iterates depth-first through a tree starting at a given {@link TreeNode}.
 *
 * @author Peter S. Heijnen
 */
public class TreeNodeIterator<T extends TreeNode>
implements Iterator<T>
{
	/**
	 * Node to start iteration at.
	 */
	@NotNull
	private final T _node;

	/**
	 * Current child index.
	 */
	private int _childIndex;

	/**
	 * Iterator for child nodes.
	 */
	@Nullable
	private TreeNodeIterator<T> _childIterator;

	/**
	 * Create iterator.
	 *
	 * @param node Node to start iteration at.
	 */
	public TreeNodeIterator( @NotNull final T node )
	{
		this( node, -1 );
	}

	/**
	 * Create iterator.
	 *
	 * @param node       Node to start iteration at.
	 * @param childIndex Index of first child (-1 to start at node).
	 */
	private TreeNodeIterator( @NotNull final T node, final int childIndex )
	{
		_node = node;
		_childIndex = childIndex;
		_childIterator = null;
	}

	@Override
	public boolean hasNext()
	{
		return ( ( _childIterator != null ) && ( _childIterator.hasNext() ) ) || ( _childIndex < _node.getChildCount() );
	}

	@Override
	public T next()
	{
		final T result;

		final T node = _node;

		TreeNodeIterator<T> childIterator = _childIterator;
		if ( ( childIterator != null ) && childIterator.hasNext() )
		{
			result = childIterator.next();
		}
		else
		{
			int childIndex = _childIndex;
			if ( childIndex >= node.getChildCount() )
			{
				throw new NoSuchElementException();
			}

			if ( childIndex < 0 )
			{
				result = node;
				childIndex = 0;
			}
			else
			{
				result = (T)node.getChildAt( childIndex++ );
				childIterator = result.isLeaf() ? null : new TreeNodeIterator<T>( result, 0 );
			}

			_childIndex = childIndex;
			_childIterator = childIterator;
		}

		return result;
	}

	@Override
	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
