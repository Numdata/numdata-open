/*
 * Copyright (c) 2015-2020, Numdata BV, The Netherlands.
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
package com.numdata.jaxrs;

import java.io.*;
import java.util.*;
import javax.annotation.*;
import javax.naming.*;
import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * Filter that implements (partial) support for <a href="http://www.w3.org/TR/cors/">Cross-Origin
 * Resource Sharing</a>.
 *
 * <p>The filter can be configured from the web.xml using init-params, or in
 * JNDI at {@code "java:comp/env/com.numdata.jaxrs.CrossOriginFilter/..."}. Any
 * properties set in JNDI override the init-params in web.xml.
 *
 * @author Gerrit Meinders
 */
@Provider
@CrossOrigin
@Priority( Priorities.HEADER_DECORATOR )
@WebFilter( initParams = {
@WebInitParam( name = "origins", value = "*", description = "Origins that are allowed to access the resources, or the wildcard '*' to allow any origin." ),
@WebInitParam( name = "allowCredentials", value = "false", description = "Whether the browser may expose credentials." )
} )
public class CrossOriginFilter
implements ContainerResponseFilter, Filter
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( CrossOriginFilter.class );

	/**
	 * Origins that are allowed to access the resources, or the wildcard '*' to
	 * allow any origin.
	 */
	@SuppressWarnings( "FieldAccessedSynchronizedAndUnsynchronized" )
	private Set<String> _origins = Collections.emptySet();

	/**
	 * Whether the set of origins contains a wildcard.
	 */
	private boolean _wildcard = true;

	/**
	 * Whether the browser may expose credentials.
	 */
	@SuppressWarnings( "FieldAccessedSynchronizedAndUnsynchronized" )
	private boolean _allowCredentials = false;

	/**
	 * Servlet config injected by Jersey.
	 */
	@Context
	ServletConfig _jerseyServletConfig;

	/**
	 * Whether the filter is initialized from Jersey.
	 */
	private boolean _jerseyInit = false;

	/**
	 * Whether the filter is initialized from JNDI.
	 */
	private boolean _jndiInit = false;

	/**
	 * Initializes the filter.
	 *
	 * @param origins          Allowed origins.
	 * @param allowCredentials Whether credentials may be exposed.
	 */
	private void init( @Nullable final String origins, @Nullable final String allowCredentials )
	{
		if ( origins != null )
		{
			final Set<String> originsSet = new LinkedHashSet<>( Arrays.asList( origins.split( "\\s+" ) ) );
			_wildcard = originsSet.remove( "*" );
			_origins = originsSet;
		}

		if ( allowCredentials != null )
		{
			_allowCredentials = Boolean.parseBoolean( allowCredentials );
		}
	}

	public Set<String> getOrigins()
	{
		return Collections.unmodifiableSet( _origins );
	}

	public boolean isWildcard()
	{
		return _wildcard;
	}

	public boolean isAllowCredentials()
	{
		return _allowCredentials;
	}

	/**
	 * Initializes the filter using settings from JNDI.
	 */
	private void initFromJNDI()
	{
		try
		{
			final InitialContext initialContext = new InitialContext();
			try
			{
				final javax.naming.Context context = (javax.naming.Context)initialContext.lookup( "java:comp/env/" + getClass().getName() );
				init( lookup( context, "origins" ),
				      lookup( context, "allowCredentials" ) );
				LOG.debug( () -> "Initialized from JNDI with origins " + getOrigins() + ", Allow-Credentials=" + isAllowCredentials() );
			}
			finally
			{
				initialContext.close();
			}
		}
		catch ( final NoInitialContextException | NameNotFoundException ignored )
		{
			LOG.debug( "Not running with JNDI context. This is no problem, just a notice." );
		}
		catch ( final NamingException e )
		{
			LOG.warn( "Failed to read settings from JNDI.", e );
		}
	}

	/**
	 * Retrieves the named object, or {@code null} if not found.
	 *
	 * @param context Context to retrieve the object from.
	 * @param name    Name of the object.
	 *
	 * @return Retrieved object.
	 *
	 * @throws NamingException if a naming exception other than {@link
	 * NameNotFoundException} occurs.
	 */
	@Nullable
	private static String lookup( @NotNull final javax.naming.Context context, @NotNull final String name )
	throws NamingException
	{
		String result = null;
		try
		{
			result = (String)context.lookup( name );
		}
		catch ( final NameNotFoundException ignored )
		{
			LOG.debug( () -> {
				String nameInNamespace;
				try
				{
					nameInNamespace = context.getNameInNamespace();
				}
				catch ( NamingException e )
				{
					nameInNamespace = e.toString();
				}
				return "The name '" + name + "' is not available in the JNDI context '" + nameInNamespace + "'. This is no problem.";
			} );
		}
		return result;
	}

	/**
	 * Handles cross-origin resource sharing.
	 *
	 * @param request Request to handle.
	 */
	private void handleCrossOrigin( final HttpAdapter request )
	{
		synchronized ( this )
		{
			if ( !_jndiInit )
			{
				initFromJNDI();
				_jndiInit = true;
			}
		}

		final LogChannel trace = LOG.getTraceChannel();
		trace.log( () -> "Handling request to " + request.getRequestURL() + " (" + request.getAPI() + ")" );

		final String origin = request.getRequestHeader( "Origin" );
		if ( ( origin == null ) || "null".equals( origin ) || "*".equals( origin ) )
		{
			if ( _wildcard )
			{
				trace.log( () -> " - Origin no specified by request, but allowed by wildcard in filter configuration." );
				request.setResponseHeader( "Access-Control-Allow-Origin", "*" );
				trace.log( () -> " - Access-Control-Allow-Origin: " + origin );
			}
			else
			{
				trace.log( " - Origin not specified by request. Cross-origin resource sharing is disabled." );
			}
		}
		else if ( !_origins.contains( origin ) )
		{
			trace.log( () -> " - Origin " + origin + " is not allowed by filter configuration. Cross-origin resource sharing is disabled." );
		}
		else
		{
			trace.log( () -> " - Origin " + origin + " is allowed explicitly in filter configuration." );

			request.setResponseHeader( "Access-Control-Allow-Origin", origin );
			request.setResponseHeader( "Access-Control-Allow-Credentials", String.valueOf( _allowCredentials ) );

			trace.log( () -> " - Access-Control-Allow-Origin: " + origin );
			trace.log( () -> " - Access-Control-Allow-Credentials: " + _allowCredentials );
		}
	}

	/**
	 * Adapter to unify different HTTP request/response classes.
	 */
	private abstract static class HttpAdapter
	{
		/**
		 * Name of the underlying API.
		 */
		private final String _api;

		/**
		 * Constructs a new instance.
		 *
		 * @param api Name of the underlying API.
		 */
		protected HttpAdapter( final String api )
		{
			_api = api;
		}

		/**
		 * Returns the name of the underlying API.
		 *
		 * @return Name of the underlying API.
		 */
		public String getAPI()
		{
			return _api;
		}

		/**
		 * Returns the URL requested by the client.
		 *
		 * @return Request URL.
		 */
		public abstract String getRequestURL();

		/**
		 * Returns the value of the specified request header.
		 *
		 * @param name Name of the request header.
		 *
		 * @return Value of the request header.
		 */
		public abstract String getRequestHeader( String name );

		/**
		 * Sets the value of the specified response header.
		 *
		 * @param name  Name of the response header.
		 * @param value Value of the response header.
		 */
		public abstract void setResponseHeader( String name, String value );
	}

	/*
	 * Filter implementation: Servlet API
	 */

	@Override
	public void init( final FilterConfig filterConfig )
	{
		init( filterConfig.getInitParameter( "origins" ),
		      filterConfig.getInitParameter( "allowCredentials" ) );
		LOG.debug( () -> "Initialized from Servlet API with origins " + _origins + ", Allow-Credentials=" + _allowCredentials );
	}

	@Override
	public void doFilter( final ServletRequest request, final ServletResponse response, final FilterChain chain )
	throws IOException, ServletException
	{
		if ( request instanceof HttpServletRequest )
		{
			final HttpServletRequest httpServletRequest = (HttpServletRequest)request;
			final HttpServletResponse httpServletResponse = (HttpServletResponse)response;

			handleCrossOrigin( new HttpAdapter( "Servlet" )
			{
				@Override
				public String getRequestURL()
				{
					return httpServletRequest.getRequestURL().toString();
				}

				@Override
				public String getRequestHeader( final String name )
				{
					return httpServletRequest.getHeader( name );
				}

				@Override
				public void setResponseHeader( final String name, final String value )
				{
					httpServletResponse.setHeader( name, value );
				}
			} );
		}

		chain.doFilter( request, response );
	}

	@Override
	public void destroy()
	{
	}

	/*
	 * Filter implementation: Jersey
	 */

	@Override
	public void filter( final ContainerRequestContext requestContext, final ContainerResponseContext responseContext )
	{
		synchronized ( this )
		{
			if ( !_jerseyInit )
			{
				final ServletConfig jerseyServletConfig = _jerseyServletConfig;
				init( jerseyServletConfig.getInitParameter( getClass().getName() + ".origins" ),
				      jerseyServletConfig.getInitParameter( getClass().getName() + ".allowCredentials" ) );
				LOG.debug( () -> "Initialized from Jersey with origins " + _origins + ", Allow-Credentials=" + _allowCredentials );
				_jerseyInit = true;
			}
		}

		handleCrossOrigin( new HttpAdapter( "Jersey" )
		{
			@Override
			public String getRequestURL()
			{
				return requestContext.getUriInfo().getRequestUri().toString();
			}

			@Override
			public String getRequestHeader( final String name )
			{
				return requestContext.getHeaderString( name );
			}

			@Override
			public void setResponseHeader( final String name, final String value )
			{
				final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
				headers.putSingle( name, value );
			}
		} );
	}
}
