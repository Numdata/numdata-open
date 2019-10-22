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
package com.numdata.servlets.test;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.jetbrains.annotations.*;

/**
 * Dummy implementation of {@link HttpServletRequest} for tests.
 *
 * @author G. Meinders
 */
public class HttpServletTestRequest
implements HttpServletRequest
{
	/**
	 * List of standard locales that are required.
	 */
	static final List<Locale> LOCALES;

	static
	{
		final List<Locale> locales = new ArrayList<Locale>( 3 );
		locales.add( new Locale( "nl", "NL" ) );
		locales.add( new Locale( "en", "US" ) );
		locales.add( new Locale( "de", "DE" ) );
		LOCALES = Collections.unmodifiableList( locales );
	}

	/**
	 * Servlet context.
	 */
	private final ServletContext _context;

	/**
	 * Session. Created on-demand if requested.
	 */
	private HttpSession _session = null;

	/**
	 * Internet Protocol (IP) address of the client or last proxy that sent the
	 * request.
	 */
	private final String _remoteAddr = "127.0.0.1";

	/**
	 * Fully qualified name of the client or the last proxy that sent the
	 * request.
	 */
	private final String _remoteHost = "localhost";

	/**
	 * Name of the HTTP method with which this request was made, for example,
	 * GET, POST, or PUT.
	 */
	private final String _method = "GET";

	/**
	 * Name and version of the protocol the request uses in the form
	 * protocol/majorVersion.minorVersion, for example, HTTP/1.1.
	 */
	private final String _protocol = "HTTP/1.1";

	/**
	 * Boolean indicating whether this request was made using a secure channel,
	 * such as HTTPS.
	 */
	private final boolean _isSecure = false;

	/**
	 * Name of the scheme used to make this request, for example, http, https,
	 * or ftp. Different schemes have different rules for constructing URLs, as
	 * noted in RFC 1738.
	 */
	private final String _scheme;

	/**
	 * Host name of the server to which the request was sent. It is the value of
	 * the part before ":" in the Host header value, if any, or the resolved
	 * server name, or the server IP address.
	 */
	private final String _serverName;

	/**
	 * Port number to which the request was sent. It is the value of the part
	 * after ":" in the Host header value, if any, or the server port where the
	 * client connection was accepted on.
	 */
	private final int _serverPort;

	/**
	 * Part of this request's URL from the protocol name up to the query string
	 * in the first line of the HTTP request. The web container does not decode
	 * this String.
	 */
	private final String _requestURI;

	/**
	 * The reconstructed URL the client used to make the request. The returned
	 * URL contains a protocol, server name, port number, and server path, but
	 * it does not include query string parameters.
	 */
	private final String _requestURL;

	/**
	 * Query string that is contained in the request URL after the path. This
	 * method returns null if the URL does not have a query string. Same as the
	 * value of the CGI variable QUERY_STRING.
	 */
	private final String _queryString;

	/**
	 * Part of this request's URL that calls the servlet. This path starts with
	 * a "/" character and includes either the servlet name or a path to the
	 * servlet, but does not include any extra path information or a query
	 * string. This will be the empty string ("") if the servlet used to process
	 * this request was matched using the "/*" pattern.
	 */
	private final String _servletPath;

	/**
	 * Extra path information associated with the URL the client sent when it
	 * made this request. The extra path information follows the servlet path
	 * but precedes the query string and will start with a "/" character. This
	 * is  null if there was no extra path information.
	 */
	private final String _pathInfo;

	/**
	 * Name of the authentication scheme used to protect the servlet.
	 */
	private final String _authType;

	/**
	 * Request headers.
	 */
	private final Map<String, List<String>> _headers = new LinkedHashMap<String, List<String>>();

	/**
	 * Request cookies received from client.
	 */
	private final List<Cookie> _cookies = new ArrayList<Cookie>();

	/**
	 * Request attributes from servlet container.
	 */
	private final Map<String, Object> _attributes = new LinkedHashMap<String, Object>();

	/**
	 * Request parameters from client.
	 */
	private final Map<String, String[]> _parameters = new LinkedHashMap<String, String[]>();

	/**
	 * Request content length.
	 */
	private int _contentLength = -1;

	/**
	 * Request content type.
	 */
	private String _contentType = null;

	/**
	 * Requested input stream.
	 */
	private ServletInputStream _inputStream = null;

	/**
	 * Requested reader.
	 */
	private BufferedReader _reader = null;

	/**
	 * User roles.
	 */
	private final Set<String> _roles = new HashSet<String>();

	/**
	 * A {@link Principal} object containing the name of the current
	 * authenticated user. If the user has not been authenticated, this is
	 * {@code null}.
	 */
	private Principal _userPrincipal = null;

	/**
	 * Constructs a new HTTP servlet request for the given URL.
	 *
	 * @param context Servlet context of the request.
	 * @param url     URL being requested.
	 */
	public HttpServletTestRequest( final ServletContext context, final URL url )
	{
		_context = context;
		_scheme = url.getProtocol();
		_serverName = url.getHost();
		_serverPort = ( url.getPort() < 0 ) ? url.getDefaultPort() : url.getPort();

		final String path = url.getPath();
		_requestURI = path;
		try
		{
			_requestURL = new URL( url, url.getPath() ).toString();
		}
		catch ( MalformedURLException e )
		{
			throw new AssertionError( "Failed to remove query and fragment from URL." );
		}

		final String contextPath = context.getContextPath();
		if ( !path.startsWith( contextPath ) )
		{
			throw new IllegalArgumentException( "Path '" + path + "' does not start with context path '" + contextPath + "' in URL '" + url + '\'' );
		}

		final String query = url.getQuery();
		if ( query != null && !query.isEmpty() )
		{
			try
			{
				for ( final String pair : query.split( "&" ) )
				{
					final int equals = pair.indexOf( '=' );
					final String key = URLDecoder.decode( ( equals < 0 ) ? pair : pair.substring( 0, equals ), "UTF-8" );
					final String value = ( equals < 0 ) ? "" : URLDecoder.decode( pair.substring( equals + 1 ), "UTF-8" );
					setParameter( key, value );
				}
			}
			catch ( UnsupportedEncodingException e )
			{
				throw new IllegalArgumentException( "Bad query: " + query, e );
			}
		}

		_queryString = query;
		_servletPath = path.substring( contextPath.length() );
		_pathInfo = null;

		final String userInfo = url.getUserInfo();
		if ( userInfo != null )
		{
			final int separatorInex = userInfo.indexOf( (int)':' );

			_authType = "BASIC";
			setRemoteUser( ( separatorInex < 0 ) ? userInfo : userInfo.substring( 0, separatorInex ) );
			//noinspection UnnecessaryFullyQualifiedName
			addHeader( "Authorization", "Basic " + com.numdata.oss.Base64.encodeBase64( userInfo.getBytes() ) );
		}
		else
		{
			_authType = null;
		}
	}

	@Nullable
	public RequestDispatcher getRequestDispatcher( @NotNull final String path )
	{
		final RequestDispatcher result;

		if ( !path.isEmpty() && ( path.charAt( 0 ) == '/' ) )
		{
			final ServletContext servletContext = getServletContext();
			result = servletContext.getRequestDispatcher( path );
		}
		else
		{
			result = null;
		}

		return result;
	}

	public String getProtocol()
	{
		return _protocol;
	}

	public String getMethod()
	{
		return _method;
	}

	public boolean isSecure()
	{
		return _isSecure;
	}

	public String getScheme()
	{
		return _scheme;
	}

	public String getServerName()
	{
		return _serverName;
	}

	public int getServerPort()
	{
		return _serverPort;
	}

	public String getRemoteHost()
	{
		return _remoteHost;
	}

	public String getRemoteAddr()
	{
		return _remoteAddr;
	}

	public int getRemotePort()
	{
		return 0;
	}

	public String getLocalName()
	{
		return "localhost";
	}

	public String getLocalAddr()
	{
		return "127.0.0.1";
	}

	public int getLocalPort()
	{
		return 0;
	}

	public Enumeration<String> getHeaderNames()
	{
		return Collections.enumeration( _headers.keySet() );
	}

	@Nullable
	public String getHeader( final String name )
	{
		final List<String> values = _headers.get( name );
		return ( ( values == null ) || values.isEmpty() ) ? null : values.get( 0 );
	}

	public Enumeration<String> getHeaders( final String key )
	{
		final List<String> values = _headers.get( key );
		return Collections.enumeration( ( values == null ) ? Collections.<String>emptySet() : values );
	}

	public long getDateHeader( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	public int getIntHeader( final String s )
	{
		throw new AssertionError( "not implemented" );
	}

	/**
	 * Adds a header with the specified name and value. Multiple values may be
	 * added for the same header; existing values are not overwritten.
	 *
	 * @param name  Name of the header.
	 * @param value Value to be set.
	 */
	public void addHeader( final String name, final String value )
	{
		final List<String> values = _headers.get( name );
		if ( values == null )
		{
			_headers.put( name, Collections.singletonList( value ) );
		}
		else
		{
			final List<String> newValues;
			if ( values instanceof ArrayList )
			{
				newValues = values;
			}
			else
			{
				newValues = new ArrayList<String>( values );
				newValues.add( value );
				_headers.put( name, newValues );
			}
			newValues.add( value );
		}
	}

	public Cookie[] getCookies()
	{
		return _cookies.toArray( new Cookie[ _cookies.size() ] );
	}

	/**
	 * Add cookie.
	 *
	 * @param cookie Cookie to add.
	 */
	public void addCookie( final Cookie cookie )
	{
		_cookies.add( cookie );
	}

	public String getAuthType()
	{
		return _authType;
	}

	@Nullable
	public String getRemoteUser()
	{
		final Principal principal = getUserPrincipal();
		return ( principal != null ) ? principal.getName() : null;
	}

	/**
	 * Set remote user. This is a convenience method that calls the {@link
	 * #setUserPrincipal} method with a {@link Principal} implementation or
	 * {@code null}.
	 *
	 * @param name Remote user name ({@code null} to unauthenticate).
	 */
	public void setRemoteUser( final String name )
	{
		_userPrincipal = ( name == null ) ? null : new Principal()
		{
			public String getName()
			{
				return name;
			}
		};
	}

	public boolean isUserInRole( final String s )
	{
		return _roles.contains( s );
	}

	/**
	 * Add roles for which the {@link #isUserInRole} method should return {@code
	 * true}.
	 *
	 * @param roles Roles for the user.
	 */
	public void addRoles( @NotNull final String... roles )
	{
		addRoles( Arrays.asList( roles ) );
	}

	/**
	 * Add roles for which the {@link #isUserInRole} method should return {@code
	 * true}.
	 *
	 * @param roles Roles for the user.
	 */
	public void addRoles( @NotNull final Collection<String> roles )
	{
		_roles.addAll( roles );
	}

	@Nullable
	public Principal getUserPrincipal()
	{
		return _userPrincipal;
	}

	/**
	 * Set user principal.
	 *
	 * @param userPrincipal User principal.
	 */
	public void setUserPrincipal( final Principal userPrincipal )
	{
		_userPrincipal = userPrincipal;
	}

	public String getRequestURI()
	{
		return _requestURI;
	}

	public StringBuffer getRequestURL()
	{
		return new StringBuffer( _requestURL );
	}

	public String getQueryString()
	{
		return _queryString;
	}

	public String getContextPath()
	{
		return _context.getContextPath();
	}

	public String getServletPath()
	{
		return _servletPath;
	}

	public String getPathInfo()
	{
		return _pathInfo;
	}

	@Nullable
	public String getPathTranslated()
	{
		return null;
	}

	@Deprecated
	@Nullable
	public String getRealPath( final String s )
	{
		return null;
	}

	@Nullable
	public String getParameter( final String s )
	{
		final String[] values = _parameters.get( s );
		return ( ( values == null ) || ( values.length == 0 ) ) ? null : values[ 0 ];
	}

	public Enumeration<String> getParameterNames()
	{
		return Collections.enumeration( _parameters.keySet() );
	}

	@Nullable
	public String[] getParameterValues( final String s )
	{
		return _parameters.get( s );
	}

	public Map<String, String[]> getParameterMap()
	{
		return Collections.unmodifiableMap( _parameters );
	}

	public Locale getLocale()
	{
		return LOCALES.get( 0 );
	}

	public Enumeration<Locale> getLocales()
	{
		return Collections.enumeration( LOCALES );
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

	@Nullable
	public HttpSession getSession()
	{
		return getSession( true );
	}

	@Nullable
	public String getRequestedSessionId()
	{
		return null;
	}

	@Deprecated
	public boolean isRequestedSessionIdValid()
	{
		return false;
	}

	public boolean isRequestedSessionIdFromCookie()
	{
		return false;
	}

	public boolean isRequestedSessionIdFromURL()
	{
		return false;
	}

	public boolean isRequestedSessionIdFromUrl()
	{
		return false;
	}

	/**
	 * Sets the session for this request.
	 *
	 * @param session Session.
	 */
	public void setSession( final HttpSession session )
	{
		_session = session;
	}

	@Contract( "true -> !null" )
	@Nullable
	public HttpSession getSession( final boolean create )
	{
		HttpSession result = _session;
		if ( ( result == null ) && create )
		{
			result = new HttpTestSession( _context );
			_session = result;
		}
		return result;
	}

	public int getContentLength()
	{
		return _contentLength;
	}

	/**
	 * Set content length.
	 *
	 * @param contentLength Content length.
	 */
	public void setContentLength( final int contentLength )
	{
		_contentLength = contentLength;
	}

	public String getContentType()
	{
		return _contentType;
	}

	/**
	 * Set content type.
	 *
	 * @param contentType Content type.
	 */
	public void setContentType( final String contentType )
	{
		_contentType = contentType;
	}

	@Nullable
	public String getCharacterEncoding()
	{
		return null;
	}

	public void setCharacterEncoding( final String s )
	{
	}

	public ServletInputStream getInputStream()
	{
		return _inputStream;
	}

	public BufferedReader getReader()
	{
		return _reader;
	}

	/**
	 * Sets the input stream from which the request's data may be read. This
	 * also sets the request's reader.
	 *
	 * @param inputStream Input stream to be set.
	 */
	@SuppressWarnings( "IOResourceOpenedButNotSafelyClosed" )
	public void setInputStream( final InputStream inputStream )
	{
		_inputStream = ( inputStream instanceof ServletInputStream ) ? (ServletInputStream)inputStream : new ServletInputStreamImpl( inputStream );
	}

	/**
	 * Sets the reader from which the request's data may be read. This also
	 * resets the request's input stream to {@code null}.
	 *
	 * @param reader Reader to be set.
	 */
	public void setReader( final Reader reader )
	{
		_reader = ( reader instanceof BufferedReader ) ? (BufferedReader)reader : new BufferedReader( reader );
		_inputStream = null;
	}

	/**
	 * Sets a request parameter with the given key and value. Any existing
	 * parameter with the same key will be replaced.
	 *
	 * @param key   Name of the parameter.
	 * @param value Value for the parameter.
	 */
	public void setParameter( final String key, final String value )
	{
		setParameter( key, new String[] { value } );
	}

	/**
	 * Sets a request parameter with the given key and value. Any existing
	 * parameter with the same key will be replaced.
	 *
	 * @param key    Name of the parameter.
	 * @param values Values for the parameter.
	 */
	public void setParameter( final String key, final String... values )
	{
		_parameters.put( key, values );
	}

	/**
	 * Sets request parameters for the key-value pairs in the given map. Any
	 * existing parameters with the same keys will be overwritten.
	 *
	 * @param parameters Request parameters to be set.
	 */
	public void setParameters( final Map<String, String[]> parameters )
	{
		_parameters.putAll( parameters );
	}

	public boolean authenticate( final HttpServletResponse response )
	throws IOException, ServletException
	{
		throw new AssertionError( "not implemented" );
	}

	public void login( final String username, final String password )
	throws ServletException
	{
		throw new AssertionError( "not implemented" );
	}

	public void logout()
	throws ServletException
	{
		throw new AssertionError( "not implemented" );
	}

	public Collection<Part> getParts()
	throws IOException, ServletException
	{
		throw new AssertionError( "not implemented" );
	}

	public Part getPart( final String name )
	throws IOException, ServletException
	{
		throw new AssertionError( "not implemented" );
	}

	public ServletContext getServletContext()
	{
		return _context;
	}

	public AsyncContext startAsync()
	{
		throw new AssertionError( "not implemented" );
	}

	public AsyncContext startAsync( final ServletRequest servletRequest, final ServletResponse servletResponse )
	{
		throw new AssertionError( "not implemented" );
	}

	public boolean isAsyncStarted()
	{
		throw new AssertionError( "not implemented" );
	}

	public boolean isAsyncSupported()
	{
		throw new AssertionError( "not implemented" );
	}

	public AsyncContext getAsyncContext()
	{
		throw new AssertionError( "not implemented" );
	}

	public DispatcherType getDispatcherType()
	{
		throw new AssertionError( "not implemented" );
	}

	public String changeSessionId()
	{
		throw new AssertionError( "not supported" );
	}

	public <T extends HttpUpgradeHandler> T upgrade( final Class<T> httpUpgradeHandlerClass )
	throws IOException, ServletException
	{
		throw new AssertionError( "not supported" );
	}

	public long getContentLengthLong()
	{
		throw new AssertionError( "not supported" );
	}

	/**
	 * Concrete implementation of a servlet input stream that wraps an
	 * underlying input stream, providing access to its data.
	 */
	private static class ServletInputStreamImpl
	extends ServletInputStream
	{
		/**
		 * Input stream being wrapped.
		 */
		private final InputStream _in;

		/**
		 * Construct stream.
		 *
		 * @param in Input stream to wrap.
		 */
		ServletInputStreamImpl( final InputStream in )
		{
			_in = in;
		}

		@Override
		public int read()
		throws IOException
		{
			return _in.read();
		}

		@Override
		public int read( final byte[] buffer )
		throws IOException
		{
			return _in.read( buffer );
		}

		@Override
		public int read( final byte[] buffer, final int off, final int len )
		throws IOException
		{
			return _in.read( buffer, off, len );
		}

		public boolean isFinished()
		{
			throw new AssertionError( "not implemented" );
		}

		public boolean isReady()
		{
			throw new AssertionError( "not implemented" );
		}

		public void setReadListener( final ReadListener listener )
		{
			throw new AssertionError( "not supported" );

		}
	}
}
