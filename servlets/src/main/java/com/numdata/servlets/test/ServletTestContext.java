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

import java.io.*;
import java.net.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.descriptor.*;

import org.jetbrains.annotations.*;

/**
 * Dummy implementation of {@link ServletContext} for tests.
 *
 * @author Peter S. Heijnen
 */
public class ServletTestContext
implements ServletContext
{
	/**
	 * Servlet init parameters.
	 */
	private final Map<String, String> _initParameters = new HashMap<>();

	/**
	 * Servlet attributes.
	 */
	private final Map<String, Object> _attributes = new HashMap<>();

	{
		File cwd = new File( "." );
		cwd = cwd.getAbsoluteFile();
		//noinspection SpellCheckingInspection
		_attributes.put( "javax.servlet.context.tempdir", cwd );
	}

	/**
	 * Class loader used for loading resources.
	 */
	private final ClassLoader _classLoader;

	/**
	 * Context path. This is the empty string for the root context or a path name
	 * with a leading slash for a named context (e.g. '/context').
	 */
	private String _contextPath = "";

	/**
	 * Request dispatchers.
	 */
	final Map<String, RequestDispatcher> _requestDispatchers = new HashMap<>();

	/**
	 * Construct context.
	 */
	public ServletTestContext()
	{
		this( ServletTestContext.class );
	}

	/**
	 * Construct context.
	 *
	 * @param resourceClass Class whose class loader to use for resources.
	 */
	public ServletTestContext( final Class<?> resourceClass )
	{
		this( resourceClass.getClassLoader() );
	}

	/**
	 * Construct context.
	 *
	 * @param classLoader Class loaded used for resources.
	 */
	public ServletTestContext( final ClassLoader classLoader )
	{
		_classLoader = classLoader;
	}

	@Nullable
	public ServletContext getContext( final String string )
	{
		return null;
	}

	public String getContextPath()
	{
		return _contextPath;
	}

	/**
	 * Set context path. This is the empty string for the root context or a path
	 * name with a leading slash for a named context (e.g. '/context').
	 *
	 * @param contextPath Context path.
	 */
	public void setContextPath( final String contextPath )
	{
		_contextPath = contextPath;
	}

	public int getMajorVersion()
	{
		return 2;
	}

	public int getMinorVersion()
	{
		return 4;
	}

	@Nullable
	public String getMimeType( final String string )
	{
		return null;
	}

	@Nullable
	public Set<String> getResourcePaths( final String string )
	{
		return null;
	}

	public URL getResource( final String string )
	{
		return _classLoader.getResource( '.' + string );
	}

	public InputStream getResourceAsStream( final String string )
	{
		return _classLoader.getResourceAsStream( '.' + string );
	}

	@Nullable
	public RequestDispatcher getRequestDispatcher( @NotNull final String path )
	{
		if ( path.isEmpty() || ( path.charAt( 0 ) != '/' ) )
		{
			throw new IllegalArgumentException( "path: " + path );
		}

		final RequestDispatcher result;

		final Map<String, RequestDispatcher> requestDispatchers = _requestDispatchers;
		if ( requestDispatchers.isEmpty() )
		{
			result = new RequestTestDispatcher( path );
		}
		else
		{
			result = requestDispatchers.get( path );
			if ( result == null )
			{
				throw new IllegalArgumentException( "Request dispatcher not available: " + path );
			}
		}

		return result;
	}

	/**
	 * Set {@link RequestDispatcher} for the given path.
	 *
	 * @param path              Path to set {@link RequestDispatcher} for.
	 * @param requestDispatcher {@link RequestDispatcher} to set.
	 */
	public void setRequestDispatcher( @NotNull final String path, @NotNull final RequestDispatcher requestDispatcher )
	{
		if ( path.isEmpty() || ( path.charAt( 0 ) != '/' ) )
		{
			throw new IllegalArgumentException( "path: " + path );
		}

		_requestDispatchers.put( path, requestDispatcher );
	}

	@Nullable
	public RequestDispatcher getNamedDispatcher( final String string )
	{
		return null;
	}

	@Nullable
	public Servlet getServlet( final String string )
	{
		return null;
	}

	@Nullable
	public Enumeration<Servlet> getServlets()
	{
		return null;
	}

	@Nullable
	public Enumeration<String> getServletNames()
	{
		return null;
	}

	public void log( final String message )
	{
		System.err.println( message );
	}

	public void log( final Exception exception, final String message )
	{
		System.err.println( message );
		exception.printStackTrace( System.err );
	}

	public void log( final String message, final Throwable throwable )
	{
		System.err.println( message );
		throwable.printStackTrace( System.err );
	}

	@Nullable
	public String getRealPath( final String string )
	{
		return null;
	}

	public String getServerInfo()
	{
		return "TestServerInfo";
	}

	public String getInitParameter( final String name )
	{
		return _initParameters.get( name );
	}

	public Enumeration<String> getInitParameterNames()
	{
		return Collections.enumeration( _initParameters.keySet() );
	}

	public Object getAttribute( final String name )
	{
		return _attributes.get( name );
	}

	public Enumeration<String> getAttributeNames()
	{
		return Collections.enumeration( _attributes.keySet() );
	}

	public void setAttribute( final String name, final Object value )
	{
		_attributes.put( name, value );
	}

	public void removeAttribute( final String name )
	{
		_attributes.remove( name );
	}

	public String getServletContextName()
	{
		return "Test Servlet";
	}

	public int getEffectiveMajorVersion()
	{
		throw new AssertionError( "not implemented" );
	}

	public int getEffectiveMinorVersion()
	{
		throw new AssertionError( "not implemented" );
	}

	public boolean setInitParameter( final String name, final String value )
	{
		_initParameters.put( name, value );
		return true;
	}

	public ServletRegistration.Dynamic addServlet( final String servletName, final String className )
	{
		throw new AssertionError( "not implemented" );
	}

	public ServletRegistration.Dynamic addServlet( final String servletName, final Servlet servlet )
	{
		throw new AssertionError( "not implemented" );
	}

	public ServletRegistration.Dynamic addServlet( final String servletName, final Class<? extends Servlet> servletClass )
	{
		throw new AssertionError( "not implemented" );
	}

	public <T extends Servlet> T createServlet( final Class<T> clazz )
	{
		throw new AssertionError( "not implemented" );
	}

	public ServletRegistration getServletRegistration( final String servletName )
	{
		throw new AssertionError( "not implemented" );
	}

	public Map<String, ? extends ServletRegistration> getServletRegistrations()
	{
		throw new AssertionError( "not implemented" );
	}

	public FilterRegistration.Dynamic addFilter( final String filterName, final String className )
	{
		throw new AssertionError( "not implemented" );
	}

	public FilterRegistration.Dynamic addFilter( final String filterName, final Filter filter )
	{
		throw new AssertionError( "not implemented" );
	}

	public FilterRegistration.Dynamic addFilter( final String filterName, final Class<? extends Filter> filterClass )
	{
		throw new AssertionError( "not implemented" );
	}

	public <T extends Filter> T createFilter( final Class<T> clazz )
	{
		throw new AssertionError( "not implemented" );
	}

	public FilterRegistration getFilterRegistration( final String filterName )
	{
		throw new AssertionError( "not implemented" );
	}

	public Map<String, ? extends FilterRegistration> getFilterRegistrations()
	{
		throw new AssertionError( "not implemented" );
	}

	public SessionCookieConfig getSessionCookieConfig()
	{
		throw new AssertionError( "not implemented" );
	}

	public void setSessionTrackingModes( final Set<SessionTrackingMode> sessionTrackingModes )
	{
		throw new AssertionError( "not implemented" );
	}

	public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
	{
		throw new AssertionError( "not implemented" );
	}

	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
	{
		throw new AssertionError( "not implemented" );
	}

	public void addListener( final Class<? extends EventListener> listenerClass )
	{
		throw new AssertionError( "not implemented" );
	}

	public void addListener( final String className )
	{
		throw new AssertionError( "not implemented" );
	}

	public <T extends EventListener> void addListener( final T t )
	{
		throw new AssertionError( "not implemented" );
	}

	public <T extends EventListener> T createListener( final Class<T> clazz )
	{
		throw new AssertionError( "not implemented" );
	}

	public void declareRoles( final String... roleNames )
	{
		throw new AssertionError( "not implemented" );
	}

	public ClassLoader getClassLoader()
	{
		throw new AssertionError( "not implemented" );
	}

	public JspConfigDescriptor getJspConfigDescriptor()
	{
		throw new AssertionError( "not implemented" );
	}

	public String getVirtualServerName()
	{
		throw new AssertionError( "not implemented" );
	}

	public ServletRegistration.Dynamic addJspFile( final String s, final String s1 )
	{
		throw new AssertionError( "not implemented" );
	}

	public int getSessionTimeout()
	{
		return 30;
	}

	@Override
	public void setSessionTimeout( final int i )
	{
		throw new AssertionError( "not implemented" );
	}

	@Override
	public String getRequestCharacterEncoding()
	{
		return "UTF-8";
	}

	@Override
	public void setRequestCharacterEncoding( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	@Override
	public String getResponseCharacterEncoding()
	{
		return "UTF-8";
	}

	@Override
	public void setResponseCharacterEncoding( final String s )
	{
		throw new AssertionError( "not implemented" );
	}
}
