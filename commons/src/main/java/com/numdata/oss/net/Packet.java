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
package com.numdata.oss.net;

import java.io.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Client/server request/response packet.
 *
 * @author Peter S. Heijnen
 */
public class Packet
{
	/**
	 * Message.
	 */
	@NotNull
	private final String _message;

	/**
	 * Attributes.
	 */
	@Nullable
	Map<String, Serializable> _attributes;

	/**
	 * Construct packet.
	 *
	 * @param message Message.
	 *
	 * @throws NullPointerException if {@code message} is {@code null}.
	 */
	public Packet( @NotNull final String message )
	{
		_message = message;
		_attributes = null;
	}

	/**
	 * Get message.
	 *
	 * @return Message.
	 */
	@NotNull
	public String getMessage()
	{
		return _message;
	}

	/**
	 * Get all attributes in this packet.
	 *
	 * @return Attributes defines in this packet; {@code null} if no attributes
	 * are available.
	 */
	@Nullable
	public Map<String, Serializable> getAttributes()
	{
		final Map<String, Serializable> attributes = _attributes;
		return ( ( attributes != null ) && !attributes.isEmpty() ) ? new HashMap<String, Serializable>( attributes ) : null;
	}

	/**
	 * Get attribute.
	 *
	 * @param name Name of attribute.
	 *
	 * @return Attribute value; {@code null} if attribute was not found.
	 *
	 * @throws NullPointerException if {@code name} is {@code null}.
	 */
	@Nullable
	public Serializable getAttribute( @NotNull final String name )
	{
		return ( _attributes != null ) ? _attributes.get( name ) : null;
	}

	/**
	 * Get attribute of the specified type.
	 *
	 * @param name Name of attribute.
	 * @param type Expected attribute type.
	 *
	 * @return Attribute value.
	 *
	 * @throws NullPointerException if {@code name} is {@code null}.
	 * @throws ProtocolException if the attribute is not set or has the wrong
	 * type.
	 */
	@NotNull
	public <T> T getAttribute( @NotNull final String name, @NotNull final Class<T> type )
	throws ProtocolException
	{
		final Serializable value = getAttribute( name );
		if ( value == null )
		{
			throw new ProtocolException( "got null for '" + name + '\'' );
		}

		if ( !type.isInstance( value ) )
		{
			throw new ProtocolException( "expected '" + type.getName() + "' for '" + name + "', but got " + value );
		}

		return (T)value;
	}

	/**
	 * Get attribute of the specified type.
	 *
	 * @param name         Name of attribute.
	 * @param type         Expected attribute type.
	 * @param defaultValue Default value.
	 *
	 * @return Attribute value.
	 *
	 * @throws NullPointerException if {@code name} is {@code null}.
	 * @throws ProtocolException if the attribute is not set or has the wrong
	 * type.
	 */
	public <T> T getAttribute( @NotNull final String name, @NotNull final Class<T> type, @Nullable final T defaultValue )
	throws ProtocolException
	{
		final T result;

		final Serializable value = getAttribute( name );
		if ( value != null )
		{
			if ( !type.isInstance( value ) )
			{
				throw new ProtocolException( "expected '" + type.getName() + "' for '" + name + "', but got " + value );
			}

			result = (T)value;
		}
		else
		{
			result = defaultValue;
		}

		//noinspection ConstantConditions
		return result;
	}

	/**
	 * Set attribute.
	 *
	 * @param name  Name of attribute.
	 * @param value Attribute value.
	 *
	 * @throws NullPointerException if {@code name} is {@code null}.
	 */
	public void setAttribute( @NotNull final String name, @Nullable final Serializable value )
	{
		Map<String, Serializable> attributes = _attributes;
		if ( attributes == null )
		{
			attributes = new HashMap<String, Serializable>();
			_attributes = attributes;
		}

		attributes.put( name, value );
	}

	public String toString()
	{
		return "Packet[message='" + _message + "',attributes=" + _attributes + ']';
	}
}
