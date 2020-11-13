/*
 * Copyright (c) 2014-2020, Numdata BV, The Netherlands.
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
 * Field target that represents the presence of a given element in a
 * collection.
 *
 * @param <E> Element type.
 *
 * @author Peter S. Heijnen
 */
public class CollectionPresenceTarget<E>
implements FieldTarget
{
	/**
	 * Name of field (element).
	 */
	private final String _name;

	/**
	 * Element whose presence is represented.
	 */
	private final E _element;

	/**
	 * Collection to get/set element presence from/in.
	 */
	private final Collection<E> _collection;

	/**
	 * Name of field.
	 *
	 * @param name       Name of field (element).
	 * @param element    Element whose presence is represented.
	 * @param collection Collection to get/set element presence from/in.
	 */
	@SuppressWarnings ( "AssignmentToCollectionOrArrayFieldFromParameter" )
	public CollectionPresenceTarget( @NotNull final String name, @NotNull final E element, @NotNull final Collection<E> collection )
	{
		_name = name;
		_element = element;
		_collection = collection;
	}

	@Override
	@NotNull
	public String getName()
	{
		return _name;
	}

	@Override
	@Nullable
	public String getValue()
	{
		return String.valueOf( _collection.contains( _element ) );
	}

	@Override
	public void setValue( @Nullable final String value )
	{
		if ( Boolean.parseBoolean( value ) )
		{
			_collection.add( _element );
		}
		else
		{
			_collection.remove( _element );
		}
	}
}
