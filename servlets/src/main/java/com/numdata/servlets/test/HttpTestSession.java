/*
 * Copyright (c) 2011-2017, Numdata BV, The Netherlands.
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
import javax.servlet.http.*;

import org.jetbrains.annotations.*;

/**
 * Dummy implementation of {@link HttpSession} for tests.
 *
 * @author  Peter S. Heijnen
 */
public class HttpTestSession
	implements HttpSession
{
	/**
	 * 'new' flag.
	 */
	private final boolean _isNew = true;

	/**
	 * Time when session was created.
	 */
	private final long _creationTime = System.currentTimeMillis();

	/**
	 * Time when session was last accessed.
	 */
	private final long _lastAccessed = System.currentTimeMillis();

	/**
	 * Session ID.
	 */
	private final String _id;

	/**
	 * Servlet context.
	 */
	private final ServletContext _context;

	/**
	 * Session attributes.
	 */
	private final Map<String, Object> _attributes = new HashMap<String, Object>();

	/**
	 * Construct session.
	 *
	 * @param   context     Servlet context for session.
	 */
	public HttpTestSession( final ServletContext context )
	{
		_context = context;
		_id = "TEST_SESSION_" + getCreationTime();
	}

	public String getId()
	{
		return _id;
	}

	public boolean isNew()
	{
		return _isNew;
	}

	public long getCreationTime()
	{
		return _creationTime;
	}

	public long getLastAccessedTime()
	{
		return _lastAccessed;
	}

	public void setMaxInactiveInterval( final int i )
	{
	}

	public int getMaxInactiveInterval()
	{
		return 0;
	}

	public void invalidate()
	{
	}

	public ServletContext getServletContext()
	{
		return _context;
	}

	public Object getAttribute( final String s )
	{
		return _attributes.get( s );
	}

	public Enumeration<String> getAttributeNames()
	{
		return Collections.enumeration( _attributes.keySet() );
	}

	public void setAttribute( final String s, final Object o )
	{
		_attributes.put( s, o );
	}

	public void removeAttribute( final String s )
	{
		_attributes.remove( s );
	}

	@Deprecated
	@Nullable
	public HttpSessionContext getSessionContext()
	{
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Nullable
	public Object getValue( final String s )
	{
		throw new UnsupportedOperationException();
	}

	@Deprecated
	@Nullable
	public String[] getValueNames()
	{
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public void putValue( final String s, final Object o )
	{
		throw new UnsupportedOperationException();
	}

	@Deprecated
	public void removeValue( final String s )
	{
		throw new UnsupportedOperationException();
	}
}
