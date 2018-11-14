/*
 * (C) Copyright Numdata BV 2018-2018 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.oss.io;

import java.util.*;

import org.w3c.dom.*;

/**
 * Adapts a {@link NodeList} to a regular {@link List}.
 *
 * @author Gerrit Meinders
 */
class NodeListAdapter<T extends Node>
extends AbstractList<T>
implements RandomAccess
{
	/**
	 * Adapted node list.
	 */
	private final NodeList _nodeList;

	/**
	 * Constructs a new instance.
	 *
	 * @param nodeList Node list to adapt.
	 */
	public NodeListAdapter( final NodeList nodeList )
	{
		_nodeList = nodeList;
	}

	@Override
	public T get( final int index )
	{
		return (T)_nodeList.item( index );
	}

	@Override
	public int size()
	{
		return _nodeList.getLength();
	}
}
