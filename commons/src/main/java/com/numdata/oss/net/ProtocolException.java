/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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

import java.io.IOException;

/**
 * Indicates that a communication problem occurred between client and server
 * because either side didn't follow the required protocol properly.
 *
 * @author  G. Meinders
 */
public class ProtocolException
	extends IOException
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 5241537263089574383L;

	/**
	 * Constructs new protocol exception without a detail message.
	 */
	public ProtocolException()
	{
	}

	/**
	 * Constructs a new protocol exception with the specified detail message.
	 *
	 * @param   message     Detail message.
	 */
	public ProtocolException( final String message )
	{
		super( message );
	}

	/**
	 * Constructs a new protocol exception with the specified cause.
	 *
	 * @param   cause   Cause of the exception.
	 */
	public ProtocolException( final Throwable cause )
	{
		super( cause );
	}

	/**
	 * Constructs a new protocol exception with the specified detail message
	 * and cause.
	 *
	 * @param   message     Detail message.
	 * @param   cause   Cause of the exception.
	 */
	public ProtocolException( final String message , final Throwable cause )
	{
		super( message , cause );
	}
}
