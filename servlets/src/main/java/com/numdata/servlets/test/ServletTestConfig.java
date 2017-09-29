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
package com.numdata.servlets.test;

import java.util.*;
import javax.servlet.*;

/**
 * Dummy implementation of {@link ServletContext} for tests.
 *
 * @author Peter S. Heijnen
 */
public class ServletTestConfig
implements ServletConfig
{
	/**
	 * Servlet name.
	 */
	private String _servletName = null;

	/**
	 * Init parameters.
	 */
	private final Map<String, String> _initParameters = new HashMap<String, String>();

	/**
	 * {@link ServletContext} in which the caller is executing.
	 */
	private ServletContext _servletContext = null;

	/**
	 * Create servlet configuration.
	 */
	public ServletTestConfig()
	{
	}

	/**
	 * Create servlet configuration for the given servlet context.
	 *
	 * @param servletContext Servlet context.
	 */
	public ServletTestConfig( final ServletContext servletContext )
	{
		_servletContext = servletContext;
	}

	public String getServletName()
	{
		return _servletName;
	}

	/**
	 * Set servlet name.
	 *
	 * @param servletName Servlet name.
	 */
	private void setServletName( final String servletName )
	{
		_servletName = servletName;
	}

	public ServletContext getServletContext()
	{
		return _servletContext;
	}

	/**
	 * Set {@link ServletContext} in which the caller is executing.
	 *
	 * @param servletContext {@link ServletContext} in which the caller is
	 *                       executing.
	 */
	private void setServletContext( final ServletContext servletContext )
	{
		_servletContext = servletContext;
	}

	public String getInitParameter( final String name )
	{
		return _initParameters.get( name );
	}

	/**
	 * Set initialization parameter.
	 *
	 * @param name  Parameter name.
	 * @param value Parameter value.
	 */
	public void setInitParameter( final String name, final String value )
	{
		_initParameters.put( name, value );
	}

	public Enumeration<String> getInitParameterNames()
	{
		return Collections.enumeration( _initParameters.keySet() );
	}
}
