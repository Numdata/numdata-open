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

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class provides functionality to check user authorization.
 *
 * @author Peter S. Heijnen
 */
public class AuthorizationChecker
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( AuthorizationChecker.class );

	/**
	 * Informant about the authorized user.
	 */
	private final AuthenticationInformant _informant;

	/**
	 * Construct checker for the specified informant.
	 *
	 * @param informant Informant about the authorized user.
	 */
	public AuthorizationChecker( @NotNull final AuthenticationInformant informant )
	{
		_informant = informant;
	}

	/**
	 * Check if a user is authenticated and has at least one of the specified
	 * roles.
	 *
	 * @param roles Roles the user may have to pass the check.
	 *
	 * @throws AuthenticationException if the user is not authenticated.
	 * @throws AuthorizationException if the user has none of the specified
	 * roles.
	 */
	public void checkAuthorization( @Nullable final String... roles )
	{
		checkAuthorization( _informant, roles );
	}

	/**
	 * Check if a user is authenticated and has at least one of the specified
	 * roles.
	 *
	 * @param informant Informant about the authorized user.
	 * @param roles     Roles the user may have to pass the check.
	 *
	 * @throws AuthenticationException if the user is not authenticated.
	 * @throws AuthorizationException if the user has none of the specified
	 * roles.
	 */
	public static void checkAuthorization( @NotNull final AuthenticationInformant informant, @Nullable final String... roles )
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "checkAuthorization( " + informant + " , " + ArrayTools.toString( roles ) + " )" );
		}

		final String loginName = informant.getUserName();
		if ( TextTools.isEmpty( loginName ) )
		{
			throw new AuthenticationException( "No user" );
		}

		if ( ( roles != null ) && ( roles.length > 0 ) )
		{
			boolean hasRole = false;
			for ( final String role : roles )
			{
				hasRole = informant.isUserInRole( role );
				if ( hasRole )
				{
					break;
				}
			}

			if ( !hasRole )
			{
				throw new AuthorizationException( "user '" + loginName + "' at domain '" + informant.getDomainName() + "' is missing user role(s)", roles );
			}
		}
	}
}
