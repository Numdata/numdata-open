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

import java.io.*;

import org.jetbrains.annotations.*;

/**
 * This exception is thrown whenever a specific resource (typically a database
 * record) can not be found.
 *
 * @author Peter S. Heijnen
 */
public class ObjectNotFoundException
extends IOException
{
	/**
	 * Type of missing object.
	 */
	private final Class<?> _objectType;

	/**
	 * ID of missing object (<0 if not applicable).
	 */
	private final int _id;

	/**
	 * Message identifying the object that was not found.
	 */
	private final String _message;

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -6804917100329226433L;

	/**
	 * Construct new exception.
	 *
	 * @param objectType Type of missing object.
	 * @param id         ID of missing object (<0 if not applicable).
	 */
	public ObjectNotFoundException( final Class<?> objectType, final int id )
	{
		this( objectType, id, null );
	}

	/**
	 * Construct new exception.
	 *
	 * @param objectType Type of missing object.
	 * @param message    Message identifying the site that was not found.
	 */
	public ObjectNotFoundException( final Class<?> objectType, final String message )
	{
		this( objectType, -1, message );
	}

	/**
	 * Construct new exception.
	 *
	 * @param objectType Type of missing object.
	 * @param id         ID of missing object (<0 if not applicable).
	 * @param message    Message identifying the object that was not found.
	 */
	public ObjectNotFoundException( final Class<?> objectType, final int id, @Nullable final String message )
	{
		super( message );

		_objectType = objectType;
		_id = id;
		_message = message;
	}

	/**
	 * The type of object that could not be found.
	 *
	 * @return Type of object.
	 */
	public Class<?> getObjectType()
	{
		return _objectType;
	}

	@Override
	public String getMessage()
	{
		final String message = super.getMessage();

		return message == null ? toString() : message;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();

		if ( _objectType != null )
		{
			sb.append( _objectType.getSimpleName() );
		}

		if ( _id >= 0 )
		{
			sb.append( ' ' );
			sb.append( _id );
		}

		if ( _message != null )
		{
			if ( sb.length() > 0 )
			{
				sb.append( ' ' );
			}

			sb.append( _message );
		}

		return sb.toString();
	}
}
