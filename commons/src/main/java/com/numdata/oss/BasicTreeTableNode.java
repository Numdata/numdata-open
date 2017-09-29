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

/**
 * Basic implementation of a node of a tree table.
 *
 * @author  G. Meinders
 */
public class BasicTreeTableNode<V,C extends TreeNode>
	extends BasicTreeNode<V,C>
	implements TreeTableNode<V>
{
	/**
	 * Attributes associated with this node.
	 */
	private final Map<String,Object> _attributes;

	/**
	 * Construct tree node.
	 *
	 * @param   parent  Parent of this node.
	 * @param   value   Associated value.
	 */
	public BasicTreeTableNode( final TreeNode parent, final V value )
	{
		super( parent, value );
		_attributes = new HashMap<String,Object>();
	}

	@Override
	public Set<String> getAttributeNames()
	{
		return _attributes.keySet();
	}

	@Override
	public Object getAttributeValue( final String attribute )
	{
		return _attributes.get( attribute );
	}

	/**
	 * Set attribute value.
	 *
	 * @param   name    Attribute name.
	 * @param   value   Attribute value.
	 */
	public void setAttribute( final String name, final Object value )
	{
		if ( name == null )
		{
			throw new NullPointerException( "name" );
		}

		_attributes.put( name, value );
	}
}
