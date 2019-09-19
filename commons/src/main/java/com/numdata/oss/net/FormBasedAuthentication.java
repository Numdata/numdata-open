/*
 * Copyright (c) 2014-2017, Numdata BV, The Netherlands.
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

import java.io.*;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;

import com.numdata.oss.Base64;
import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * Provides support for form-based authentication used by servlets.
 *
 * @author Peter S. Heijnen
 * @author Gerrit Meinders
 */
public class FormBasedAuthentication
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FormBasedAuthentication.class );

	/**
	 * Cookie manager to be used.
	 */
	private final CookieManager _cookieManager;

	/**
	 * User name.
	 *
	 * @see #setUserCredentials
	 */
	private String _loginName = null;

	/**
	 * Password.
	 *
	 * @see #setUserCredentials
	 */
	private String _password = null;

	/**
	 * Constructs a new instance.
	 */
	public FormBasedAuthentication()
	{
		/*
		 * NOTE: The exception to use 'CookiePolicy.ACCEPT_ALL' below is needed
		 * to support connections to 'localhost', which according to
		 * 'HttpCookie.domainMatches' does not match itself.
		 */
//		final CookiePolicy cookiePolicy = _serverURL.getHost().contains( "." ) ? CookiePolicy.ACCEPT_ORIGINAL_SERVER : CookiePolicy.ACCEPT_ALL;
		final CookiePolicy cookiePolicy = CookiePolicy.ACCEPT_ALL;
		_cookieManager = new CookieManager( null, cookiePolicy );
	}

	/**
	 * Get login name.
	 *
	 * @return Login name.
	 */
	public String getLoginName()
	{
		return _loginName;
	}

	/**
	 * Set login name to use.
	 *
	 * @param loginName Login name to use.
	 */
	public void setLoginName( final String loginName )
	{
		_loginName = loginName;
	}

	/**
	 * Get password.
	 *
	 * @return Password.
	 */
	public String getPassword()
	{
		return _password;
	}

	/**
	 * Set password to use for login.
	 *
	 * @param password Password to use for login.
	 */
	public void setPassword( final String password )
	{
		_password = password;
	}

	/**
	 * Set user credentials to use when the server requires a login.
	 *
	 * @param loginName Login name to use.
	 * @param password  Password to use.
	 */
	public void setUserCredentials( final String loginName, final String password )
	{
		setLoginName( loginName );
		setPassword( password );
	}

	/**
	 * Opens a connection to the given URL. This method will attempt to log in
	 * using basic authentication and form-based authentication.
	 *
	 * @param url URL to connect to.
	 *
	 * @return URL connection.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@NotNull
	public URLConnection openConnection( @NotNull final URL url )
	throws IOException
	{
		URLConnection connection = url.openConnection();
		connection.setUseCaches( false );

		final String loginName = getLoginName();
		if ( TextTools.isNonEmpty( loginName ) )
		{
			final String password = getPassword();
			final String credentials = loginName + ':' + ( ( password != null ) ? password : "" );
			connection.setRequestProperty( "Authorization", "Basic " + Base64.encodeBase64( credentials.getBytes() ) );
		}

		includeCookiesInRequest( connection );
		connection.connect();
		acceptCookiesFromResponse( connection );

		/*
		 * Handle error responses.
		 */
		if ( connection instanceof HttpURLConnection )
		{
			final int responseCode = ( (HttpURLConnection)connection ).getResponseCode();
			switch ( responseCode )
			{
				case HttpURLConnection.HTTP_OK:
					break;

				case HttpURLConnection.HTTP_UNAUTHORIZED:
					throw new AuthenticationException( connection.getHeaderField( 0 ) );

				case HttpURLConnection.HTTP_FORBIDDEN:
					throw new AuthorizationException( connection.getHeaderField( 0 ) );

				case HttpURLConnection.HTTP_NOT_FOUND:
					throw new FileNotFoundException( connection.getHeaderField( 0 ) );

				default:
					throw new IOException( connection.getHeaderField( 0 ) );
			}
		}

		/*
		 * Handle form-based login.
		 */
		if ( isHtmlResponse( connection ) )
		{
			final String content = getHtmlContent( connection );

			final Pattern pattern = Pattern.compile( "action=\"([^\"]*/j_security_check)\"" );
			final Matcher matcher = pattern.matcher( content );

			if ( matcher.find() )
			{
				LOG.info( "Authenticating using form-based login" );

				if ( TextTools.isEmpty( loginName ) )
				{
					throw new AuthenticationException( "No credentials" );
				}

				final URL loginURL = new URL( connection.getURL(), matcher.group( 1 ) + "?j_username=" + URLEncoder.encode( loginName, "UTF-8" ) + "&j_password=" + URLEncoder.encode( getPassword(), "UTF-8" ) );
				LOG.trace( "loginURL: " + loginURL );

				connection = loginURL.openConnection();
				connection.setUseCaches( false );
				includeCookiesInRequest( connection );
				connection.connect();
				acceptCookiesFromResponse( connection );

				if ( ( connection instanceof HttpURLConnection ) && ( ( (HttpURLConnection)connection ).getResponseCode() != HttpURLConnection.HTTP_OK ) )
				{
					throw new AuthenticationException( connection.getHeaderField( 0 ) );
				}

				if ( isHtmlResponse( connection ) && pattern.matcher( getHtmlContent( connection ) ).matches() )
				{
					final String title = getTitleFromHtmlContent( getHtmlContent( connection ) );
					throw new AuthenticationException( TextTools.isNonEmpty( title ) ? title : "Login failed" );
				}
			}
			else
			{
				throw new IOException( "Unrecognized response: " + getTitleFromHtmlContent( content ) );
			}
		}

		return connection;
	}

	/**
	 * Include cookies in HTTP request.
	 *
	 * @param connection Connection to send request through.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void includeCookiesInRequest( final URLConnection connection )
	throws IOException
	{
		try
		{
			final Map<String, List<String>> requestProperties = _cookieManager.get( connection.getURL().toURI(), connection.getRequestProperties() );
			for ( final Map.Entry<String, List<String>> entry : requestProperties.entrySet() )
			{
				for ( final String value : entry.getValue() )
				{
					connection.addRequestProperty( entry.getKey(), value );
				}
			}
		}
		catch ( URISyntaxException e )
		{
			throw new IllegalArgumentException( e.toString(), e );
		}
	}

	/**
	 * Accept cookies from HTTP response.
	 *
	 * @param connection Connection to get response from.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void acceptCookiesFromResponse( final URLConnection connection )
	throws IOException
	{
		try
		{
			_cookieManager.put( connection.getURL().toURI(), connection.getHeaderFields() );
		}
		catch ( URISyntaxException e )
		{
			throw new IllegalArgumentException( e.toString(), e );
		}
	}

	/**
	 * Sets the specified cookie.
	 *
	 * @param url    URL to associate the cookie with; {@code null} to have no
	 *               associations with an URL.
	 * @param header Cookie to be set; see {@link HttpCookie#parse}.
	 */
	public void setCookie( final URL url, final String header )
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "setCookie( '" + header + "' )" );
		}

		final CookieStore cookieStore = _cookieManager.getCookieStore();
		for ( final HttpCookie cookie : HttpCookie.parse( header ) )
		{
			try
			{
				cookieStore.add( url.toURI(), cookie );
			}
			catch ( URISyntaxException e )
			{
				throw new IllegalArgumentException( e.toString(), e );
			}
		}
	}

	/**
	 * Test if the HTTP response contains HTML content.
	 *
	 * @param connection URL connection to get response from.
	 *
	 * @return {@code true} if the HTTP response specifies HTML content; {@code
	 *         false} otherwise.
	 */
	private static boolean isHtmlResponse( final URLConnection connection )
	{
		final String contentType = connection.getContentType();
		return ( contentType != null ) && contentType.contains( "text/html" );
	}

	/**
	 * Get HTML content from HTTP response.
	 *
	 * @param connection URL connection to get response from.
	 *
	 * @return HTML content.
	 *
	 * @throws IOException if there was a problem retrieving the content.
	 */
	private static String getHtmlContent( final URLConnection connection )
	throws IOException
	{
		final String result;

		final InputStream in = connection.getInputStream();
		try
		{
			final InputStreamReader reader = new InputStreamReader( in, getCharsetFromContentType( connection.getContentType() ) );
			result = TextTools.loadText( reader );
		}
		finally
		{
			in.close();
		}

		return result;
	}

	/**
	 * Get HTML content from HTTP response.
	 *
	 * @param content HTML content to get title from.
	 *
	 * @return Title from HTML content; {@code null} if no title was found.
	 */
	private static String getTitleFromHtmlContent( final CharSequence content )
	{
		String result = null;

		if ( content != null )
		{
			final Pattern pattern = Pattern.compile( "(?i)<title\\s*>\\s*(.*[^\\s])\\s*</title\\s*>" );

			final Matcher matcher = pattern.matcher( content );
			if ( matcher.find() )
			{
				result = matcher.group( 1 );
			}
		}

		return result;
	}

	/**
	 * This method derives the charset from a "Content-Type" response header. A
	 * typical header is:
	 * <pre>Content-Type: text/html;charset=ISO-8859-1</pre>
	 *
	 * @param contentType Content type.
	 *
	 * @return Chararacter set (never {@code null}).
	 */
	@Nullable
	private static Charset getCharsetFromContentType( final CharSequence contentType )
	{
		Charset result = null;

		if ( contentType != null )
		{
			final Pattern pattern = Pattern.compile( ";\\s*charset\\s*=\\s*([^;\\s]+)" );

			final Matcher matcher = pattern.matcher( contentType );
			if ( matcher.find() )
			{
				final String charsetName = matcher.group( 1 );

				try
				{
					result = Charset.forName( charsetName );
				}
				catch ( UnsupportedCharsetException ignored )
				{
					System.err.println( "Unknown charset: " + charsetName );
				}
			}

			if ( result == null )
			{
				result = Charset.forName( "ISO-8859-1" );
			}
		}

		return result;
	}
}
