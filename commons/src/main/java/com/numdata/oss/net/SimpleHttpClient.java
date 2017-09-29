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

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.charset.*;
import java.security.*;
import java.security.cert.*;
import java.util.*;
import java.util.regex.*;
import javax.net.ssl.*;

import com.numdata.oss.Base64;
import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class provides a simple HTTP client mostly based around the {@link
 * HttpURLConnection} class.
 *
 * @author Peter S. Heijnen
 */
public class SimpleHttpClient
{
	/**
	 * HTTP request method.
	 */
	public static final String GET = "GET";

	/**
	 * HTTP request method.
	 */
	public static final String POST = "POST";

	/**
	 * HTTP request method.
	 */
	public static final String PUT = "PUT";

	/**
	 * HTTP request method.
	 */
	public static final String DELETE = "DELETE";

	/**
	 * Media type for HTML text.
	 */
	public static final String TEXT_HTML = "text/html";

	/**
	 * Media type for plaint text.
	 */
	public static final String TEXT_PLAIN = "text/plain";

	/**
	 * Media type for XML data.
	 */
	public static final String APPLICATION_XML = "application/xml";

	/**
	 * Media type for binary data.
	 */
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( SimpleHttpClient.class );

	/**
	 * Regex to detect form-based login.
	 */
	private static final Pattern LOGIN_PAGE_PATTERN = Pattern.compile( "action=\"([^\"]*/j_security_check)\"" );

	/**
	 * Regex to detect title content in HTML page.
	 */
	private static final Pattern HTML_TITLE_PATTERN = Pattern.compile( "(?i)<title\\s*>\\s*(.*[^\\s])\\s*</title\\s*>" );

	/**
	 * Cookie manager to be used.
	 */
	@NotNull
	private final CookieManager _cookieManager;

	/**
	 * User name to use when a web page requires authentication.
	 */
	@Nullable
	private String _user = null;

	/**
	 * Password to use when a web page requires authentication.
	 */
	@Nullable
	private String _password = null;

	/**
	 * Construct client.
	 */
	public SimpleHttpClient()
	{
		_cookieManager = new CookieManager( null, CookiePolicy.ACCEPT_ALL );
	}

	/**
	 * Get login name.
	 *
	 * @return Login name.
	 */
	@Nullable
	public String getUser()
	{
		return _user;
	}

	/**
	 * Set login name to use.
	 *
	 * @param user Login name to use.
	 */
	public void setUser( @Nullable final String user )
	{
		_user = user;
	}

	/**
	 * Get password.
	 *
	 * @return Password.
	 */
	@Nullable
	public String getPassword()
	{
		return _password;
	}

	/**
	 * Set password to use for login.
	 *
	 * @param password Password to use for login.
	 */
	public void setPassword( @Nullable final String password )
	{
		_password = password;
	}

	/**
	 * Get HTML page from the given URL using HTTP.
	 *
	 * @param url URL for HTML page to get.
	 *
	 * @return HTML page context.
	 *
	 * @throws IOException if the page could not be retrieved.
	 */
	@NotNull
	public String getHtmlPage( @NotNull final URL url )
	throws IOException
	{
		return getHtmlPage( url, true );
	}

	/**
	 * Get HTML page from the given URL using HTTP.
	 *
	 * @param url       URL for HTML page to get.
	 * @param useCaches Whether the use of caches is allowed or not.
	 *
	 * @return HTML page context.
	 *
	 * @throws IOException if the page could not be retrieved.
	 */
	@NotNull
	public String getHtmlPage( @NotNull final URL url, final boolean useCaches )
	throws IOException
	{
		final Connection connection = createConnection( GET, url, useCaches, false, true );
		connection.requireSuccessfulResponse();
		connection.requireResponseContentType( TEXT_HTML );
		return connection.getTextContent();
	}

	/**
	 * Upload file to the given web URL.
	 *
	 * @param url            Upload web URL.
	 * @param fieldName      Field name for file on upload form.
	 * @param file           File to upload.
	 * @param formFields     Additional form fields to set in upload request.
	 * @param barCertIgnored Whether to ignore bad SSL certificates.
	 *
	 * @throws IOException if the upload failed.
	 */
	public void uploadFile( @NotNull final URL url, @NotNull final String fieldName, @NotNull final File file, @NotNull final Map<String, String> formFields, final boolean barCertIgnored )
	throws IOException
	{
		final Connection connection = createConnection( POST, url, false, barCertIgnored, true );
		final MultiPartPostRequest postRequest = new MultiPartPostRequest( connection.getUrlConnection(), "UTF-8" );
		for ( final Map.Entry<String, String> entry : formFields.entrySet() )
		{
			postRequest.addFormField( entry.getKey(), entry.getValue() );
		}
		postRequest.addFilePart( fieldName, file );
		postRequest.finish();
	}

	/**
	 * Get cookies for the given URL. This may be used after calling {@link
	 * #getHtmlPage} to get any cookies that were set.
	 *
	 * @param url URL to get cookies for.
	 *
	 * @return Cookies.
	 *
	 * @throws IOException if the default cookie handler encounters an I/O
	 * error.
	 */
	@NotNull
	public List<HttpCookie> getCookies( @NotNull final URL url )
	throws IOException
	{
		final Map<String, HttpCookie> result = new LinkedHashMap<String, HttpCookie>();
		final URI uri = UrlTools.toURI( url );

		final CookieHandler defaultCookieHandler = CookieHandler.getDefault();
		if ( defaultCookieHandler != null )
		{
			final Map<String, List<String>> cookies = defaultCookieHandler.get( uri, new HashMap<String, List<String>>() );
			for ( final Map.Entry<String, List<String>> entry : cookies.entrySet() )
			{
				for ( final String value : entry.getValue() )
				{
					/*
					 * NOTE: 'value' is a 'Cookie' header, which splits multiple
					 * cookies with a semi-colon. 'HttpCookie' on the other hand
					 * parses a 'Set-Cookie' header, which separates multiple
					 * cookies with a comma. Manually split by semi-colon to
					 * compensate.
					 */
					for ( final String part : value.split( ";" ) )
					{
						for ( final HttpCookie cookie : HttpCookie.parse( part ) )
						{
							result.put( cookie.getName(), cookie );
						}
					}
				}
			}
		}

		for ( final HttpCookie cookie : _cookieManager.getCookieStore().get( uri ) )
		{
			result.put( cookie.getName(), cookie );
		}

		return new ArrayList<HttpCookie>( result.values() );
	}

	/**
	 * Sets the specified cookie.
	 *
	 * @param uri    URI to associated cookie with; {@code null} to not
	 *               associate the cookie with an URI.
	 * @param header Cookie to be set; see {@link HttpCookie#parse}.
	 *
	 * @throws IllegalArgumentException if the header is malformed.
	 * @see HttpCookie#parse
	 * @see CookieStore#add
	 */
	public void setCookie( @NotNull final URI uri, @NotNull final String header )
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "setCookie( '" + header + "' )" );
		}

		final CookieStore cookieStore = _cookieManager.getCookieStore();
		for ( final HttpCookie cookie : HttpCookie.parse( header ) )
		{
			cookieStore.add( uri, cookie );
		}
	}

	/**
	 * Open HTTP / HTTPS connection.
	 *
	 * When the {@code barCertIgnored} flag is set and a HTTPS connection is
	 * made, bad SSL certificates will be ignored. This disabled any protection
	 * from spoofing or man-in-the-middle attacks, however, it may be required
	 * when security authorities are inaccessible or when accessing servers with
	 * invalid/useless (self-signed) certificates (i.e. many embedded devices or
	 * private web services).
	 *
	 * @param method HTTP request method to use.
	 * @param url    URL to connect to.
	 *
	 * @return {@link HttpURLConnection}.
	 *
	 * @throws IOException if the connection could not be opened.
	 * @throws IllegalArgumentException if URL is no a HTTP or HTTPS url.
	 */
	@NotNull
	public Connection createConnection( @NotNull final String method, @NotNull final URL url )
	throws IOException
	{
		final Connection result = new Connection( url );
		result.setMethod( method );
		return result;
	}

	/**
	 * Open HTTP / HTTPS connection.
	 *
	 * When the {@code barCertIgnored} flag is set and a HTTPS connection is
	 * made, bad SSL certificates will be ignored. This disabled any protection
	 * from spoofing or man-in-the-middle attacks, however, it may be required
	 * when security authorities are inaccessible or when accessing servers with
	 * invalid/useless (self-signed) certificates (i.e. many embedded devices or
	 * private web services).
	 *
	 * @param method                HTTP request method to use.
	 * @param url                   URL to connect to.
	 * @param useCaches             Whether the use of caches is allowed or
	 *                              not.
	 * @param barCertIgnored        Whether to ignore bad SSL certificates.
	 * @param authorizationIncluded Include 'Authorization' header in request if
	 *                              credentials are available.
	 *
	 * @return {@link HttpURLConnection}.
	 *
	 * @throws IOException if the connection could not be opened.
	 * @throws IllegalArgumentException if URL is no a HTTP or HTTPS url.
	 */
	@NotNull
	public Connection createConnection( @NotNull final String method, @NotNull final URL url, final boolean useCaches, final boolean barCertIgnored, final boolean authorizationIncluded )
	throws IOException
	{
		final Connection result = new Connection( url );
		result.setMethod( method );
		result.setUseCaches( useCaches );
		result.setBadCertIgnored( barCertIgnored );
		result.setAuthorizationIncluded( authorizationIncluded );
		return result;
	}

	/**
	 * This method derives the charset from a "Content-Type" response header. A
	 * typical header is:
	 * <pre>Content-Type: text/html;charset=ISO-8859-1</pre>
	 *
	 * @param contentType Content type.
	 *
	 * @return Character set (never {@code null}).
	 */
	@NotNull
	public static Charset getCharsetFromContentType( @Nullable final CharSequence contentType )
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
				catch ( final UnsupportedCharsetException ignored )
				{
					System.err.println( "Unknown charset: " + charsetName );
				}
			}
		}

		if ( result == null )
		{
			result = Charset.forName( "ISO-8859-1" );
		}

		return result;
	}

	/**
	 * Simple pipe method that read data from one stream and writes it to
	 * another. This uses a 4KiB buffer.
	 *
	 * @param in  Input stream to read from.
	 * @param out Output stream to write to.
	 *
	 * @throws IOException if there was a problem with one of the streams.
	 */
	private static void pipe( @NotNull final InputStream in, @NotNull final OutputStream out )
	throws IOException
	{
		final byte[] buffer = new byte[ 4096 ];
		for ( int read; ( read = in.read( buffer ) ) >= 0; )
		{
			out.write( buffer, 0, read );
		}
	}

	/**
	 * This utility class provides an abstraction layer for sending multi-part
	 * HTTP POST requests to a web server.
	 *
	 * @author www.codejava.net
	 */
	public static class MultiPartPostRequest
	{
		/**
		 * HTTP connection.
		 */
		private final HttpURLConnection _connection;

		/**
		 * Binary output stream.
		 */
		private final OutputStream _out;

		/**
		 * Character output stream (encodes text and writes to binary output
		 * stream).
		 */
		private final PrintWriter _writer;

		/**
		 * Character set used by writer.
		 */
		private final String _charset;

		/**
		 * Part boundary.
		 */
		private final String _boundary;

		/**
		 * Required line separator in multipart request.
		 */
		private static final String EOL = "\r\n";

		/**
		 * Initialize HTTP POST request with content type 'multipart/form-data'.
		 *
		 * @param connection Unopened HTTP connection.
		 * @param encoding   Character encoding used for form data.
		 *
		 * @throws IOException if the connection could not be opened.
		 */
		public MultiPartPostRequest( final HttpURLConnection connection, final String encoding )
		throws IOException
		{
			_connection = connection;
			_charset = encoding;

			// creates a unique boundary based on time stamp
			final String boundary = "===" + Long.toHexString( System.currentTimeMillis() ) + "===";
			connection.setRequestProperty( "Content-Type", "multipart/form-data; boundary=" + boundary );
			_boundary = boundary;

			connection.setDoOutput( true ); // use POST method
			connection.setDoInput( true );

			final OutputStream out = connection.getOutputStream();
			_out = out;
			_writer = new PrintWriter( new OutputStreamWriter( out, encoding ), true );
		}

		/**
		 * Adds a form field to the request.
		 *
		 * @param name  field name
		 * @param value field value
		 */
		public void addFormField( @SuppressWarnings( "TypeMayBeWeakened" ) final String name, @SuppressWarnings( "TypeMayBeWeakened" ) final String value )
		{
			final PrintWriter writer = _writer;
			writer.append( "--" ).append( _boundary ).append( EOL );
			writer.append( "Content-Disposition: form-data; name=\"" ).append( name ).append( "\"" ).append( EOL );
			writer.append( "Content-Type: " ).append( TEXT_PLAIN ).append( "; charset=" ).append( _charset ).append( EOL );
			writer.append( EOL );
			writer.append( value ).append( EOL );
			writer.flush();
		}

		/**
		 * Adds a upload file section to the request.
		 *
		 * @param fieldName name attribute in <input type="file" name="..." />
		 * @param file      a File to be uploaded
		 *
		 * @throws IOException if the part could not be written.
		 */
		public void addFilePart( @SuppressWarnings( "TypeMayBeWeakened" ) final String fieldName, final File file )
		throws IOException
		{
			final String fileName = file.getName();

			final PrintWriter writer = _writer;
			writer.append( "--" ).append( _boundary ).append( EOL );
			writer.append( "Content-Disposition: form-data; name=\"" ).append( fieldName ).append( "\"; filename=\"" ).append( fileName ).append( "\"" ).append( EOL );
			writer.append( "Content-Type: " ).append( URLConnection.guessContentTypeFromName( fileName ) ).append( EOL );
			writer.append( "Content-Transfer-Encoding: binary" ).append( EOL );
			writer.append( EOL );
			writer.flush();

			final OutputStream out = _out;

			final FileInputStream in = new FileInputStream( file );
			try
			{
				pipe( in, out );
			}
			finally
			{
				in.close();
			}
			out.flush();

			writer.append( EOL );
			writer.flush();
		}

		/**
		 * Completes the request and receives response from the server.
		 *
		 * @return Content from server.
		 *
		 * @throws IOException if an error occurred while processing the
		 * request.
		 */
		public Object finish()
		throws IOException
		{
			final PrintWriter writer = _writer;
			writer.append( EOL ).flush();
			writer.append( "--" ).append( _boundary ).append( "--" ).append( EOL );
			writer.close();

			final HttpURLConnection connection = _connection;
			try
			{
				final Object result;

				final Object content = connection.getContent();
				if ( content instanceof InputStream )
				{
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					pipe( (InputStream)content, baos );
					result = baos.toByteArray();
				}
				else
				{
					result = content;
				}

				return result;
			}
			finally
			{
				connection.disconnect();
			}
		}
	}

	private static class DummyHostnameVerifier
	implements HostnameVerifier
	{
		@Override
		public boolean verify( final String hostname, final SSLSession sslSession )
		{
			// any host is correct
			return true;
		}
	}

	/**
	 * HTTP/HTTPS connection wrapper.
	 */
	public class Connection
	{
		/**
		 * HTTP request method to use.
		 */
		@NotNull
		private String _method = GET;

		/**
		 * URL to connect to.
		 */
		@NotNull
		private URL _url;

		/**
		 * Whether the use of caches is allowed or not.
		 */
		private boolean _useCaches = false;

		/**
		 * Whether to ignore bad SSL certificates.
		 *
		 * When this flag is set and a HTTPS connection is made, bad SSL
		 * certificates will be ignored. This disabled any protection from
		 * spoofing or man-in-the-middle attacks, however, it may be required
		 * when security authorities are inaccessible or when accessing servers
		 * with invalid/useless (self-signed) certificates (i.e. many embedded
		 * devices or private web services).
		 */
		private boolean _barCertIgnored = false;

		/**
		 * whether 'Authorization' header is included in request if credentials
		 * are available.
		 */
		private boolean _authorizationIncluded = true;

		/**
		 * {@link HttpURLConnection} used as delegate for the actual work.
		 */
		private HttpURLConnection _urlConnection = null;

		/**
		 * Whether the {@link #connect()} method was called or not.
		 */
		private boolean _connected = false;

		/**
		 * Text content that was received.
		 */
		private String _textContent = null;

		/**
		 * Create HTTP / HTTPS connection.
		 *
		 * @param url URL to connect to.
		 */
		public Connection( @NotNull final URL url )
		{
			LOG.debug( "Connection( " + url + " )" );

			_url = url;
		}

		/**
		 * G Set HTTP request method to use.
		 *
		 * @return HTTP request method to use.
		 */
		@NotNull
		public String getMethod()
		{
			return _method;
		}

		/**
		 * Set HTTP request method to use.
		 *
		 * @param method HTTP request method to use.
		 */
		public void setMethod( @NotNull final String method )
		{
			_method = method;
		}

		/**
		 * Get URL to connect to.
		 *
		 * @return URL to connect to.
		 */
		@NotNull
		public URL getUrl()
		{
			return _url;
		}

		/**
		 * Set URL to connect to.
		 *
		 * @param url URL to connect to.
		 */
		public void setUrl( @NotNull final URL url )
		{
			_url = url;
		}

		/**
		 * Get whether the use of caches is allowed or not.
		 *
		 * @return {@code true} if the use of caches is allowed or not.
		 */
		public boolean isUseCaches()
		{
			return _useCaches;
		}

		/**
		 * Set whether the use of caches is allowed or not.
		 *
		 * @param useCaches Whether the use of caches is allowed or not.
		 */
		public void setUseCaches( final boolean useCaches )
		{
			_useCaches = useCaches;
		}

		/**
		 * Set whether 'Authorization' header is included in request if
		 * credentials are available.
		 *
		 * @return {@code true} if 'Authorization' header is included in request
		 * if credentials are available.
		 */
		public boolean isAuthorizationIncluded()
		{
			return _authorizationIncluded;
		}

		/**
		 * Set whether 'Authorization' header is included in request if
		 * credentials are available.
		 *
		 * @param authorizationIncluded 'Authorization' header is included in
		 *                              request if credentials are available.
		 */
		public void setAuthorizationIncluded( final boolean authorizationIncluded )
		{
			_authorizationIncluded = authorizationIncluded;
		}

		/**
		 * Test whether to ignore bad SSL certificates.
		 *
		 * When this flag is set and a HTTPS connection is made, bad SSL
		 * certificates will be ignored. This disabled any protection from
		 * spoofing or man-in-the-middle attacks, however, it may be required
		 * when security authorities are inaccessible or when accessing servers
		 * with invalid/useless (self-signed) certificates (i.e. many embedded
		 * devices or private web services).
		 *
		 * @return {@code true} to ignore bad SSL certificates.
		 */
		public boolean isBadCertIgnored()
		{
			return _barCertIgnored;
		}

		/**
		 * Set whether to ignore bad SSL certificates.
		 *
		 * When this flag is set and a HTTPS connection is made, bad SSL
		 * certificates will be ignored. This disabled any protection from
		 * spoofing or man-in-the-middle attacks, however, it may be required
		 * when security authorities are inaccessible or when accessing servers
		 * with invalid/useless (self-signed) certificates (i.e. many embedded
		 * devices or private web services).
		 *
		 * @param barCertIgnored {@code true} to ignore bad SSL certificates.
		 */
		public void setBadCertIgnored( final boolean barCertIgnored )
		{
			_barCertIgnored = barCertIgnored;
		}

		/**
		 * Send content to server.
		 *
		 * NOTE: This should be called before the network connection is made
		 * (using the {@link #connect()} method).
		 *
		 * @param contentType Content-type of data ({@code null} if no data or
		 *                    {@link SimpleHttpClient#APPLICATION_OCTET_STREAM}).
		 * @param content     Request content ({@code null} to send no
		 *                    content).
		 *
		 * @throws IOException if there was a communications error.
		 */
		public void sendContent( @NotNull final String contentType, @NotNull final byte[] content )
		throws IOException
		{
			final HttpURLConnection connection = getUrlConnection();
			connection.setDoOutput( true );
			connection.setRequestMethod( getMethod() );
			connection.setRequestProperty( "Content-Type", contentType );
			connection.setRequestProperty( "Content-Length", String.valueOf( content.length ) );

			final OutputStream out = connection.getOutputStream();
			try
			{
				out.write( content );
			}
			finally
			{
				out.close();
			}

			acceptCookiesFromResponse( connection );
		}

		/**
		 * Open link to server and perform authorization if necessary.
		 *
		 * @throws IOException if a communications error occurs.
		 * @throws IllegalArgumentException the connection parameters are
		 * invalid.
		 */
		public void connect()
		throws IOException
		{
			if ( !_connected )
			{
				HttpURLConnection connection = getUrlConnection();
				connection.connect();
				acceptCookiesFromResponse( connection );

				int responseCode = connection.getResponseCode();
				if ( responseCode == HttpURLConnection.HTTP_UNAUTHORIZED ||
				     responseCode == HttpURLConnection.HTTP_FORBIDDEN )
				{
					String textContent = getTextContent();
					final Matcher loginAction = LOGIN_PAGE_PATTERN.matcher( textContent );
					if ( loginAction.find() )
					{
						LOG.debug( "Detected form-based login page" );

						final String user = getUser();
						final String password = getPassword();
						if ( ( user == null ) || ( password == null ) )
						{
							throw new AuthenticationException( "Page requires login, but no user credentials are set" );
						}

						if ( LOG.isTraceEnabled() )
						{
							LOG.trace( "Performing form-based login using login name '" + user + '\'' );
						}

						final URL loginUrl = UrlTools.buildUrl( connection.getURL(), loginAction.group( 1 ), "j_username", user, "j_password", password );

						connection = createUrlConnection( GET, loginUrl, isUseCaches(), isBadCertIgnored(), false );
						try
						{
							acceptCookiesFromResponse( connection );

							responseCode = connection.getResponseCode();
							if ( responseCode == HttpURLConnection.HTTP_UNAUTHORIZED ||
							     responseCode == HttpURLConnection.HTTP_FORBIDDEN )
							{
								textContent = getTextContent();
								if ( LOGIN_PAGE_PATTERN.matcher( textContent ).find() )
								{
									final Matcher matcher = HTML_TITLE_PATTERN.matcher( textContent );
									final String message = matcher.find() ? matcher.group( 1 ) : connection.getHeaderField( 0 );
									throw new AuthenticationException( message );
								}
							}
						}
						catch ( final IOException e )
						{
							// authentication failed
							String errorContent = null;

							final InputStream errorStream = connection.getErrorStream();
							if ( errorStream != null )
							{
								try
								{
									final InputStreamReader reader = new InputStreamReader( errorStream, getCharsetFromContentType( connection.getContentType() ) );
									errorContent = TextTools.loadText( reader );
								}
								catch ( final IOException ignored )
								{
									// don't hide original cause
								}
							}

							if ( errorContent != null )
							{
								final Matcher matcher = HTML_TITLE_PATTERN.matcher( errorContent );
								if ( matcher.find() )
								{
									throw new AuthenticationException( matcher.group( 1 ), e );
								}
							}

							throw e;
						}
					}
				}

				_connected = true;
			}
		}

		/**
		 * Create {@link HttpURLConnection} delegate.
		 *
		 * @param method                HTTP request method to use.
		 * @param url                   URL to connect to.
		 * @param useCaches             Whether the use of caches is allowed or
		 *                              not.
		 * @param badCertIgnored        Whether to ignore bad SSL certificates.
		 * @param authorizationIncluded Include 'Authorization' header in
		 *                              request if credentials are available.
		 *
		 * @return {@link HttpURLConnection}.
		 *
		 * @throws IOException if the connection could not be opened.
		 * @throws IllegalArgumentException if URL is no a HTTP or HTTPS url.
		 */
		@NotNull
		protected HttpURLConnection createUrlConnection( @NotNull final String method, @NotNull final URL url, final boolean useCaches, final boolean badCertIgnored, final boolean authorizationIncluded )
		throws IOException
		{
			final boolean trace = LOG.isTraceEnabled();
			if ( trace )
			{
				LOG.trace( "createUrlConnection( method:" + method + ", url:" + url + ", useCaches:" + useCaches + ", badCertIgnored: " + badCertIgnored + ", authorizationIncluded:" + authorizationIncluded + " )" );
			}

			final String protocol = url.getProtocol();
			if ( !"http".equals( protocol ) && !"https".equals( protocol ) )
			{
				throw new IllegalArgumentException( "Not a 'http' or 'https' URL: " + url );
			}

			final HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			if ( badCertIgnored && ( connection instanceof HttpsURLConnection ) )
			{
				if ( trace )
				{
					LOG.trace( "Disable all SSL certificate validation for this request" );
				}

				// Crap! HP printers (and probably many others) have bad/useless self-signed SSL certificates, so we disable all validations here
				final HttpsURLConnection httpsConnection = (HttpsURLConnection)connection;

				try
				{
					final SSLContext sslContext = SSLContext.getInstance( "SSL" );
					sslContext.init( null, new TrustManager[] { new X509TrustManager()
					{
						@Override
						public void checkClientTrusted( final X509Certificate[] x509Certificates, final String s )
						{
							// all clients are trusted
						}

						@Override
						public void checkServerTrusted( final X509Certificate[] x509Certificates, final String s )
						{
							// all servers all trusted.
						}

						@Override
						public X509Certificate[] getAcceptedIssuers()
						{
							// there are not issuers
							return new X509Certificate[ 0 ];
						}
					} }, new SecureRandom() );

					httpsConnection.setSSLSocketFactory( sslContext.getSocketFactory() );
				}
				catch ( final NoSuchAlgorithmException e )
				{
					// should not happen, SSL is part of standard platform SDK
					throw new IOException( e );
				}
				catch ( final KeyManagementException e )
				{
					// should not happen since everything is void
					throw new IOException( e );
				}

				httpsConnection.setHostnameVerifier( new DummyHostnameVerifier() );
			}

			connection.setRequestMethod( method );
			connection.setUseCaches( useCaches );

			if ( authorizationIncluded )
			{
				final String user = getUser();
				if ( user != null )
				{
					if ( trace )
					{
						LOG.trace( "Include 'Authorization' header for '" + user + '\'' );
					}

					final String password = getPassword();
					final String credentials = ( ( password != null ) ? user + ':' + password : user );
					connection.setRequestProperty( "Authorization", "Basic " + Base64.encodeBase64( credentials.getBytes( "UTF-8" ) ) );
				}
			}

			includeCookiesInRequest( connection );

			_urlConnection = connection;
			_textContent = null;

			return connection;
		}

		/**
		 * Include cookies in a HTTP request.
		 *
		 * @param connection URL connection to set cookies in.
		 *
		 * @throws IOException if the URL connection failed.
		 */
		protected void includeCookiesInRequest( @NotNull final URLConnection connection )
		throws IOException
		{
			final boolean trace = LOG.isTraceEnabled();
			if ( trace )
			{
				LOG.trace( "includeCookiesInRequest()" );
			}

			final URI uri = UrlTools.toURI( connection.getURL() );

			final CookieManager cookieManager = _cookieManager;
			for ( final Map.Entry<String, List<String>> cookie : cookieManager.get( uri, connection.getRequestProperties() ).entrySet() )
			{
				for ( final String value : cookie.getValue() )
				{
					connection.addRequestProperty( cookie.getKey(), value );
				}
			}

			if ( trace )
			{
				for ( final Map.Entry<String, List<String>> entry : connection.getRequestProperties().entrySet() )
				{
					final String key = entry.getKey();
					for ( final String value : entry.getValue() )
					{
						LOG.trace( "Send: " + key + ": " + value );
					}
				}
			}
		}

		/**
		 * Accept cookies from HTTP response.
		 *
		 * This method implies opening the connection and reading the response
		 * header from the server.
		 *
		 * @param connection Connection to get response from.
		 *
		 * @throws IOException if an I/O error occurs.
		 */
		public void acceptCookiesFromResponse( @NotNull final URLConnection connection )
		throws IOException
		{
			connection.connect();

			if ( LOG.isTraceEnabled() )
			{
				for ( final Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet() )
				{
					final String key = entry.getKey();
					for ( final String value : entry.getValue() )
					{
						LOG.trace( "Received: " + ( ( key != null ) ? key + ": " + value : value ) );
					}
				}
			}

			try
			{
				_cookieManager.put( connection.getURL().toURI(), connection.getHeaderFields() );
			}
			catch ( final URISyntaxException e )
			{
				throw new IllegalArgumentException( e.toString(), e );
			}
		}

		/**
		 * Get {@link HttpURLConnection} used as delegate for this connection.
		 *
		 * IMPORTANT: This connection may not be connected and unauthorized. Use
		 * the {@link #connect()} to get an established connection that is
		 * authorized if applicable.
		 *
		 * @return {@link HttpURLConnection}
		 *
		 * @throws IOException if the connection could not be opened.
		 * @throws IllegalArgumentException the connection parameters are
		 * invalid.
		 */
		@NotNull
		public HttpURLConnection getUrlConnection()
		throws IOException
		{
			HttpURLConnection connection = _urlConnection;
			if ( connection == null )
			{
				connection = createUrlConnection( getMethod(), getUrl(), isUseCaches(), isBadCertIgnored(), isAuthorizationIncluded() );
			}
			return connection;
		}

		/**
		 * Require a successful response from the server.
		 *
		 * @throws IOException if the connection did not produce a successful
		 * response.
		 */
		public void requireSuccessfulResponse()
		throws IOException
		{
			connect();
			final HttpURLConnection connection = getUrlConnection();
			final int responseCode = connection.getResponseCode();
			if ( responseCode / 100 != 2 )
			{
				if ( LOG.isTraceEnabled() )
				{
					for ( final Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet() )
					{
						final String name = entry.getKey();
						for ( final String value : entry.getValue() )
						{
							LOG.trace( ( name != null ) ? "Received: " + name + ": " + value : "Received: " + value );
						}
					}
				}

				switch ( responseCode )
				{
					case HttpURLConnection.HTTP_UNAUTHORIZED:
						throw new AuthenticationException( connection.getHeaderField( 0 ) );

					case HttpURLConnection.HTTP_FORBIDDEN:
						throw new AuthorizationException( connection.getHeaderField( 0 ) );

					case HttpURLConnection.HTTP_NOT_FOUND:
						throw new FileNotFoundException( connection.getHeaderField( 0 ) );

					default:
						throw new IOException( "Received " + connection.getHeaderField( 0 ) );
				}
			}
		}

		/**
		 * Require a response with the given content type from the server.
		 *
		 * @param requiredContentType Required content type; {@code null} to
		 *                            accept any or no content-type.
		 *
		 * @throws IOException if the connection did not produce a successful
		 * response.
		 */
		public void requireResponseContentType( @Nullable final String requiredContentType )
		throws IOException
		{
			connect();
			final HttpURLConnection connection = getUrlConnection();
			final String contentType = connection.getContentType();
			if ( ( requiredContentType != null ) && ( ( contentType == null ) || !contentType.startsWith( requiredContentType ) ) )
			{
				throw new IOException( "Response from server should have content-type " + requiredContentType + ", but was " + contentType );
			}
		}

		/**
		 * Gets the status code from an HTTP response message.
		 *
		 * @return the HTTP Status-Code, or -1
		 *
		 * @throws IOException if a communications error occurs.
		 * @see HttpURLConnection#getResponseCode()
		 */
		public int getResponseCode()
		throws IOException
		{
			connect();
			return getUrlConnection().getResponseCode();
		}

		/**
		 * Returns the value of the {@code content-length} header field.
		 *
		 * @return the content length of the resource that this connection's URL
		 * references, or {@code -1} if the content length is not known.
		 *
		 * @throws IOException if a communications error occurs.
		 * @see URLConnection#getContentLength()
		 */
		public int getContentLength()
		throws IOException
		{
			connect();
			return getUrlConnection().getContentLength();
		}

		/**
		 * Returns the value of the {@code content-type} header field.
		 *
		 * @return the content type of the resource that the URL references, or
		 * {@code null} if not known.
		 *
		 * @throws IOException if a communications error occurs.
		 * @see URLConnection#getContentType()
		 */
		public String getContentType()
		throws IOException
		{
			connect();
			return getUrlConnection().getContentType();
		}

		/**
		 * Returns the value of the {@code content-encoding} header field.
		 *
		 * @return the content encoding of the resource that the URL references,
		 * or {@code null} if not known.
		 *
		 * @throws IOException if a communications error occurs.
		 * @see URLConnection#getContentEncoding()
		 */
		public String getContentEncoding()
		throws IOException
		{
			connect();
			return getUrlConnection().getContentEncoding();
		}

		/**
		 * Returns an input stream that reads from this connection.
		 *
		 * @return Input stream to read from the connection.
		 *
		 * @throws IOException if a communications error occurs.
		 * @see URLConnection#getInputStream()
		 */
		public InputStream getInputStream()
		throws IOException
		{
			final InputStream result;

			connect();

			final String textContent = _textContent;
			if ( textContent != null )
			{
				result = new InputStream()
				{
					/**
					 * ByteBuffer that encodes a string into bytes.
					 */
					final ByteBuffer _byteBuffer = getCharsetFromContentType( getContentType() ).encode( textContent );

					@Override
					public int read()
					{
						final ByteBuffer byteBuffer = _byteBuffer;
						return byteBuffer.hasRemaining() ? byteBuffer.get() & 0xFF : -1;
					}

					@Override
					public int read( @NotNull final byte[] bytes, final int offset, final int len )
					{
						final int result;

						final ByteBuffer byteBuffer = _byteBuffer;
						if ( byteBuffer.hasRemaining() )
						{
							result = Math.min( len, byteBuffer.remaining() );
							byteBuffer.get( bytes, offset, result );
						}
						else
						{
							result = -1;
						}

						return result;
					}
				};
			}
			else
			{
				final HttpURLConnection connection = getUrlConnection();
				result = connection.getInputStream();
			}

			return result;
		}

		/**
		 * Get text content from the given URL using HTTP.
		 *
		 * @return Text content.
		 *
		 * @throws IOException if a communications error occurs.
		 */
		@NotNull
		public String getTextContent()
		throws IOException
		{
			String result = _textContent;
			if ( result == null )
			{
				final HttpURLConnection connection = getUrlConnection();
				final int responseCode = connection.getResponseCode();
				final InputStream in = responseCode < 400 ? connection.getInputStream() : connection.getErrorStream();
				try
				{
					final String contentType = connection.getContentType();
					final Charset charset = getCharsetFromContentType( contentType );
					final InputStreamReader reader = new InputStreamReader( in, charset );
					result = TextTools.loadText( reader );
					_textContent = result;
				}
				finally
				{
					in.close();
				}
			}
			return result;
		}
	}
}
