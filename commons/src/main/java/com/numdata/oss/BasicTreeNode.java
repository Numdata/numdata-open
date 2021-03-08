/*
 * Copyright (c) 2008-2021, Unicon Creation BV, The Netherlands.
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
 * Basic implementation of {@link TreeNode} interface.
 *
 * @param <C> Child type.
 * @param <V> Value type.
 *
 * @author Peter S. Heijnen
 * @see TreeNode
 */
public class BasicTreeNode<V, C extends TreeNode>
implements TreeNode, Iterable<BasicTreeNode<?, ?>>
{
	/**
	 * Parent of this node.
	 */
	private TreeNode _parent;

	/**
	 * Children of this node.
	 */
	private final List<C> _children = new ArrayList<>();

	/**
	 * Associated value of this node.
	 */
	private V _value;

	/**
	 * Construct tree node.
	 *
	 * @param parent Parent of this node.
	 * @param value  Associated value.
	 */
	public BasicTreeNode( final TreeNode parent, final V value )
	{
		_parent = parent;
		_value = value;
	}

	@Override
	public TreeNode getParent()
	{
		return _parent;
	}

	public void setParent( final TreeNode parent )
	{
		_parent = parent;
	}

	@Override
	public Enumeration<C> children()
	{
		return Collections.enumeration( _children );
	}

	/**
	 * Add child to this node.
	 *
	 * @param child Child to add.
	 */
	public void addChild( final C child )
	{
		_children.add( child );
	}

	@Override
	public boolean getAllowsChildren()
	{
		return true;
	}

	@Override
	public C getChildAt( final int childIndex )
	{
		return _children.get( childIndex );
	}

	@Override
	public int getChildCount()
	{
		return _children.size();
	}

	/**
	 * Get all {@link TreeNode child nodes} of this node.
	 *
	 * @return All {@link TreeNode child nodes} of this node.
	 */
	public List<C> getChildren()
	{
		return Collections.unmodifiableList( _children );
	}

	@Override
	public int getIndex( final TreeNode node )
	{
		//noinspection SuspiciousMethodCalls
		return _children.indexOf( node );
	}

	@Override
	public boolean isLeaf()
	{
		return _children.isEmpty();
	}

	public V getValue()
	{
		return _value;
	}

	public void setValue( final V value )
	{
		_value = value;
	}

	@NotNull
	@Override
	public TreeNodeIterator<BasicTreeNode<?, ?>> iterator()
	{
		return new TreeNodeIterator<>( this );
	}

	@Override
	public boolean equals( final Object obj )
	{
		return ( obj == this ) || ( obj instanceof BasicTreeNode ) && Objects.equals( getValue(), ( (BasicTreeNode<?, ?>)obj ).getValue() );
	}

	@Override
	public int hashCode()
	{
		final V value = getValue();
		return ( value != null ) ? value.hashCode() : 0;
	}
}
