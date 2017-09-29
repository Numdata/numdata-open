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

import java.util.*;

/**
 * Indicates that an operation could not be performed because the authenticated
 * user is lacking the proper authorization.
 *
 * @author G. Meinders
 */
public class AuthorizationException
extends RuntimeException
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 1287129873210938934L;

	/**
	 * Roles that were required, but not granted to the user.
	 */
	private final Collection<String> _missingRoles;

	/**
	 * Constructs a new authorization exception without a detail message.
	 */
	public AuthorizationException()
	{
		_missingRoles = Collections.emptySet();
	}

	/**
	 * Constructs a new authorization exception with detail message.
	 *
	 * @param message Detail message.
	 */
	public AuthorizationException( final String message )
	{
		super( message );
		_missingRoles = Collections.emptySet();
	}

	/**
	 * Constructs a new authorization exception with detail message and cause.
	 *
	 * @param message Detail message.
	 * @param cause   Cause of the exception.
	 */
	public AuthorizationException( final String message, final Throwable cause )
	{
		super( message, cause );
		_missingRoles = Collections.emptySet();
	}

	/**
	 * Constructs a new authorization exception with the given cause.
	 *
	 * @param cause Cause of the exception.
	 */
	public AuthorizationException( final Throwable cause )
	{
		super( cause );
		_missingRoles = Collections.emptySet();
	}

	/**
	 * Constructs a new authorization exception with detail message, missing roles,
	 * but no cause.
	 *
	 * @param message      Detail message.
	 * @param missingRoles Indicates which roles were required, but no granted to
	 *                     the user.
	 */
	public AuthorizationException( final String message, final String... missingRoles )
	{
		super( ( message != null ) ? message + ": " + Arrays.toString( missingRoles ) : Arrays.toString( missingRoles ) );
		_missingRoles = Arrays.asList( missingRoles );
	}

	/**
	 * Constructs a new authorization exception with the specified missing roles
	 * and cause.
	 *
	 * @param cause        Cause of the exception.
	 * @param missingRoles Indicates which roles were required, but no granted to
	 *                     the user.
	 */
	public AuthorizationException( final Throwable cause, final String... missingRoles )
	{
		super( Arrays.toString( missingRoles ), cause );
		_missingRoles = Arrays.asList( missingRoles );
	}

	/**
	 * Constructs a new authorization exception with detail message, missing roles,
	 * and cause.
	 *
	 * @param message      Detail message.
	 * @param cause        Cause of the exception.
	 * @param missingRoles Indicates which roles were required, but no granted to
	 *                     the user.
	 */
	public AuthorizationException( final String message, final Throwable cause, final String... missingRoles )
	{
		super( ( message != null ) ? message + ": " + Arrays.toString( missingRoles ) : Arrays.toString( missingRoles ), cause );
		_missingRoles = Arrays.asList( missingRoles );
	}

	/**
	 * Returns the role that was missing, causing this exception to be thrown.
	 *
	 * @return Missing roles.
	 */
	public Collection<String> getMissingRoles()
	{
		return Collections.unmodifiableCollection( _missingRoles );
	}
}
