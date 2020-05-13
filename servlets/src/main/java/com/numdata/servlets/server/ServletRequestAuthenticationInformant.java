/*
 * Copyright (c) 2008-2020, Numdata BV, The Netherlands.
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
package com.numdata.servlets.server;

import javax.servlet.http.*;

import com.numdata.oss.net.*;
import org.jetbrains.annotations.*;

/**
 * This class implements the {@link AuthenticationInformant} interface for a
 * servlet request.
 *
 * @author Peter S. Heijnen
 */
public class ServletRequestAuthenticationInformant
implements AuthenticationInformant
{
	/**
	 * Servlet request providing authentication info.
	 */
	private final HttpServletRequest _servletRequest;

	/**
	 * Construct informant.
	 *
	 * @param servletRequest Servlet request providing authentication info.
	 */
	public ServletRequestAuthenticationInformant( @NotNull final HttpServletRequest servletRequest )
	{
		_servletRequest = servletRequest;
	}

	@Override
	@NotNull
	public String getDomainName()
	{
		return _servletRequest.getServerName();
	}

	@Override
	public String getUserName()
	{
		return _servletRequest.getRemoteUser();
	}

	@Override
	public boolean isUserInRole( @NotNull final String role )
	{
		return _servletRequest.isUserInRole( role );
	}
}
