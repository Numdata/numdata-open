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
package com.numdata.oss.db;

import java.io.IOException;

/**
 * Indicates that an attempt to store data in or retrieve data from an
 * underlying data storage system failed.
 *
 * @author  G. Meinders
 */
public class DatabaseException
	extends IOException
{
	/**
	 * Serialization data version.
	 */
	private static final long serialVersionUID = -6321055581722733626L;

	/**
	 * Constructs new database exception without a detail message or cause.
	 */
	public DatabaseException()
	{
	}

	/**
	 * Constructs new database exception with detail message, but no cause.
	 *
	 * @param   message     Detail message.
	 */
	public DatabaseException( final String message )
	{
		super( message );
	}

	/**
	 * Constructs new database exception with cause, but no detail message.
	 *
	 * @param   cause       Cause of exception.
	 */
	public DatabaseException( final Throwable cause )
	{
		super( cause );
	}

	/**
	 * Constructs new database exception with detail message and cause.
	 *
	 * @param   message     Detail message.
	 * @param   cause       Cause of exception.
	 */
	public DatabaseException( final String message , final Throwable cause )
	{
		super( message , cause );
	}
}
