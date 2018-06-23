/*
 * Copyright (c) 2018, Numdata BV, The Netherlands.
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
package com.numdata.uri;

import java.io.*;
import java.net.*;
import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.io.*;
import com.numdata.oss.log.*;
import com.numdata.serial.*;
import jcifs.smb.*;
import org.apache.commons.net.*;
import org.apache.commons.net.ftp.*;
import org.jetbrains.annotations.*;

/**
 * This class provides utility methods related to {@link URI}s. This class
 * supports all schemes supported by the JVM and explicitly supports the
 * schemes: file, ftp, keyboard, memory, serial, and smb.
 *
 * <h3>FTP</h3> The following parameters can be used:
 *
 * (Non-standard parameters are marked with a '*'.)
 *
 * <table>
 *
 * <tr><th></th><th>Key</th><th>Value</th><th>Description</th></tr>
 *
 * <tr><td></td><td>type</td><td>a</td><td>Set ASCII transfer type.</td></tr>
 *
 * <tr><td></td><td>type</td><td>i</td><td>Set binary transfer type.</td></tr>
 *
 * <tr><td>*</td><td>mode</td><td>passive</td><td>Use passive connection
 * mode.</td></tr>
 *
 * <tr><td>*</td><td>mode</td><td>active</td><td>Use active connection
 * mode.</td></tr>
 *
 * <tr><td>*</td><td>no-chdir</td><td></td><td> Write files without using
 * {@code chdir} commands. Qualified filenames are used instead. Note that <a
 * href="http://www.ietf.org/rfc/rfc1738.txt">RFC 1738: Uniform Resource
 * Locators (URL)</a> states that each path segment "is to be supplied,
 * sequentially, as the argument to a CWD (change working directory) command."
 * </td></tr>
 *
 * <tr><td>*</td><td>delete</td><td></td><td>Delete files before they are
 * written.</td></tr>
 *
 * </table>
 *
 * <h3>keyboard</h3>
 *
 * You may use the 'encoding' parameter to select character encoding other than
 * the default, UTF-8.
 *
 * @author Peter S. Heijnen
 * @see URI Java URI definition
 * @see Javacomm#openSerialPort Definition of 'serial' scheme
 * @see SmbFile Definition of the 'smb' scheme
 */
public class URITools
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( URITools.class );

	/**
	 * Extended version of {@link URI#resolve(String)} method. This also
	 * supports resolving against a 'jar:<url>!/<path>' URI.
	 *
	 * @param context URI context to resolve against.
	 * @param uri     URI to resolve against the context URI.
	 *
	 * @return The resulting URI.
	 *
	 * @see URI#resolve(URI, URI)
	 */
	public static URI resolve( final String context, final String uri )
	{
		return resolve( URI.create( context ), URI.create( uri ) );
	}

	/**
	 * Extended version of {@link URI#resolve(String)} method. This also
	 * supports resolving against a 'jar:<url>!/<path>' URI.
	 *
	 * @param context URI context to resolve against.
	 * @param uri     URI to resolve against the context URI.
	 *
	 * @return The resulting URI.
	 *
	 * @see URI#resolve(URI, URI)
	 */
	public static URI resolve( final URI context, final String uri )
	{
		return resolve( context, URI.create( uri ) );
	}

	/**
	 * Extended version of {@link URI#resolve(URI)} method. This also supports
	 * resolving against a 'jar:<url>!/<path>' URI.
	 *
	 * @param context URI context to resolve against.
	 * @param uri     URI to resolve against the context URI.
	 *
	 * @return The resulting URI.
	 *
	 * @see URI#resolve(URI, URI)
	 */
	public static URI resolve( final URI context, final URI uri )
	{
		URI result = context.resolve( uri );

		/*
		 * Implement resolving against 'jar:<url>!/<path>' URI.
		 */
		//noinspection ObjectEquality
		if ( ( result == uri ) && !uri.isOpaque() && context.isOpaque() && "jar".equals( context.getScheme() ) )
		{
			final String schemeSpecific = context.getRawSchemeSpecificPart();
			final int separator = schemeSpecific.lastIndexOf( "!/" );
			if ( separator > 0 )
			{
				final URI path = URI.create( schemeSpecific.substring( separator + 1 ) );

				final URI resolvedPath = resolve( path, uri );
				//noinspection ObjectEquality
				if ( resolvedPath != uri )
				{
					result = URI.create( context.getScheme() + ':' + schemeSpecific.substring( 0, separator + 1 ) + resolvedPath );
				}
			}
		}

		return result;
	}

	/**
	 * Read data from a source identified by an {@link URI}.
	 *
	 * @param uri URI for data source.
	 *
	 * @return File contents in a byte array.
	 *
	 * @throws IOException if the data could not be retrieved.
	 */
	public static byte[] readData( @NotNull final URI uri )
	throws IOException
	{
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "readData( " + TextTools.quote( uri ) + " )" );
		}

		final byte[] result;

		final String scheme = uri.getScheme();
		if ( scheme == null )
		{
			throw new IllegalArgumentException( "URI '" + uri + "' is invalid" );
		}

		if ( "file".equals( scheme ) )
		{
			final File file = new File( uri );
			if ( !file.isFile() || !file.canRead() )
			{
				throw new FileNotFoundException( uri.toString() );
			}

			try
			{
				final long fileSize = file.length();
				result = new byte[ (int)fileSize ];
				final InputStream in = new FileInputStream( file );
				try
				{
					int done = 0;
					while ( done < result.length )
					{
						done += in.read( result, done, result.length - done );
					}
				}
				finally
				{
					in.close();
				}
			}
			catch ( final SecurityException e )
			{
				throw new IOException( String.valueOf( e ) );
			}
		}
		else if ( "ftp".equals( scheme ) )
		{
			final URIPath path = new URIPath( uri );
			final Map<String, String> parameters = path.getParameters();

			String directory = path.getDirectoryWithoutSlash();
			String filename = path.getFile();

			if ( TextTools.isEmpty( filename ) )
			{
				throw new IOException( "Bad path in URI: " + uri );
			}

			if ( parameters.containsKey( "no-chdir" ) )
			{
				filename = path.getDirectory() + filename;
				directory = "";
			}

			final FTPClient ftpClient = openFtpConnection( uri );
			try
			{
				final String type = parameters.get( "type" );
				//noinspection Duplicates
				if ( type != null )
				{
					if ( "a".equals( type ) )
					{
						ftpClient.setFileType( FTP.ASCII_FILE_TYPE );
					}
					else if ( "i".equals( type ) )
					{
						ftpClient.setFileType( FTP.BINARY_FILE_TYPE );
					}
				}

				if ( !directory.isEmpty() && !ftpClient.changeWorkingDirectory( directory ) )
				{
					throw new FileNotFoundException( "Failed to access directory for URI: " + uri + " (" + ftpClient.getReplyCode() + ": " + ftpClient.getReplyString() + ')' );
				}

				final ByteArrayOutputStream byteOut = new ByteArrayOutputStream();

				if ( !ftpClient.retrieveFile( filename, byteOut ) )
				{
					throw new IOException( "Failed to retrieve file for URI (reply code " + ftpClient.getReplyCode() + ": " + ftpClient.getReplyString() + "): " + uri );
				}

				result = byteOut.toByteArray();
			}
			finally
			{
				if ( ftpClient.isConnected() )
				{
					ftpClient.disconnect();
				}
			}
		}
		else if ( "memory".equals( scheme ) )
		{
			result = MemoryScheme.getData( uri );
		}
		else if ( "smb".equals( scheme ) )
		{
			final SmbFile file = new SmbFile( "smb:" + uri.getSchemeSpecificPart() );
			try
			{
				if ( !file.isFile() )
				{
					throw new FileNotFoundException( uri.toString() );
				}
			}
			catch ( final SmbException e )
			{
				throw new IOException( uri + " => " + e.getMessage(), e );
			}
			catch ( final ExceptionInInitializerError e )
			{
				throw new IOException( uri + " => " + e.getMessage(), e );
			}

			final long fileSize = file.length();

			final InputStream in = file.getInputStream();
			result = new byte[ (int)fileSize ];
			try
			{
				int done = 0;
				while ( done < result.length )
				{
					final int read = in.read( result, done, result.length - done );
					if ( read < 0 )
					{
						throw new EOFException( uri.toString() );
					}

					done += read;
				}
			}
			finally
			{
				in.close();
			}
		}
		else
		{
			final URL url = uri.toURL();
			final URLConnection connection = url.openConnection();
			connection.setDoInput( true );
			connection.setDoOutput( false );
			final InputStream in = connection.getInputStream();
			try
			{
				result = DataStreamTools.readByteArray( in );
			}
			finally
			{
				in.close();
			}
		}

		return result;
	}

	/**
	 * Write data to a destination identified by an {@link URI}.
	 *
	 * @param uri    URI for data destination.
	 * @param data   Data to write.
	 * @param append Append to existing data vs. create new data..
	 *
	 * @throws IOException if the data could not be written.
	 */
	public static void writeData( @NotNull final URI uri, @NotNull final byte[] data, final boolean append )
	throws IOException
	{
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "writeData( " + TextTools.quote( uri ) + ", data[" + data.length + "], append=" + append + " )" );
		}

		final String scheme = uri.getScheme();
		if ( scheme == null )
		{
			throw new IllegalArgumentException( "URI '" + uri + "' is invalid" );
		}

		try
		{
			if ( "file".equals( scheme ) )
			{
				try
				{
					final File file = new File( uri );
					final File parent = file.getParentFile();
					if ( parent != null )
					{
						parent.mkdirs();
					}

					final OutputStream out = new FileOutputStream( file, append );
					try
					{
						out.write( data );
					}
					finally
					{
						out.close();
					}
				}
				catch ( final SecurityException e )
				{
					throw new IOException( String.valueOf( e ) );
				}
			}
			else if ( "ftp".equals( scheme ) )
			{
				final URIPath path = new URIPath( uri );
				final Map<String, String> parameters = path.getParameters();

				String directory = path.getDirectoryWithoutSlash();
				String filename = path.getFile();

				if ( TextTools.isEmpty( filename ) )
				{
					throw new IOException( "Bad path in URI: " + uri );
				}

				if ( parameters.containsKey( "no-chdir" ) )
				{
					filename = path.getDirectory() + filename;
					directory = "";
				}

				final FTPClient ftpClient = openFtpConnection( uri );
				try
				{
					if ( !directory.isEmpty() )
					{
						if ( LOG.isTraceEnabled() )
						{
							LOG.trace( "changeWorkingDirectory( " + directory + " )" );
						}

						if ( !ftpClient.changeWorkingDirectory( directory ) )
						{
							if ( LOG.isDebugEnabled() )
							{
								LOG.debug( "makeDirectory( " + directory + " )" );
							}

							if ( !ftpClient.makeDirectory( directory ) )
							{
								throw new IOException( "Failed to make directory for URI (reply code " + ftpClient.getReplyCode() + ": " + ftpClient.getReplyString() + "): " + uri );
							}

							if ( LOG.isTraceEnabled() )
							{
								LOG.trace( "changeWorkingDirectory( " + directory + " )" );
							}

							if ( !ftpClient.changeWorkingDirectory( directory ) )
							{
								throw new FileNotFoundException( "Failed to enter newly created directory for URI (reply code " + ftpClient.getReplyCode() + ": " + ftpClient.getReplyString() + "): " + uri );
							}
						}
					}

					if ( parameters.containsKey( "delete" ) )
					{
						if ( LOG.isTraceEnabled() )
						{
							LOG.trace( "deleteFile( '" + filename + "' )" );
						}

						try
						{
							ftpClient.deleteFile( filename );
						}
						catch ( final IOException e )
						{
							if ( LOG.isDebugEnabled() )
							{
								LOG.debug( "deleteFile( " + filename + " ) => " + e );
							}
							throw e;
						}
					}

					if ( LOG.isTraceEnabled() )
					{
						LOG.trace( "storeFile( '" + filename + "' )" );
					}

					if ( !ftpClient.storeFile( filename, new ByteArrayInputStream( data ) ) )
					{
						throw new IOException( "Failed to store file for URI (reply code " + ftpClient.getReplyCode() + ": " + ftpClient.getReplyString() + "): " + uri );
					}

					ftpClient.quit();
				}
				finally
				{
					if ( ftpClient.isConnected() )
					{
						ftpClient.disconnect();
					}
				}
			}
			else if ( "memory".equals( scheme ) )
			{
				final OutputStream out = MemoryScheme.getOutputStream( uri, append );
				out.write( data );
			}
			else if ( "serial".equals( scheme ) )
			{
				final Javacomm javacomm = Javacomm.getInstance();
				javacomm.sendToSerialPort( uri, data );
			}
			else if ( "keyboard".equals( scheme ) )
			{
				senTodKeyboard( uri, data );
			}
			else if ( "smb".equals( scheme ) )
			{
				final SmbFile file = new SmbFile( "smb:" + uri.getSchemeSpecificPart() );

				try
				{
					final SmbFile parent = new SmbFile( file.getParent(), (NtlmPasswordAuthentication)file.getPrincipal() );
					if ( !parent.exists() )
					{
						parent.mkdirs();
					}
				}
				catch ( final MalformedURLException e )
				{
					/*
					 * We couldn't get a parent, so we don't need to create one,
					 * and can safely ignore this exception.
					 */
				}
				catch ( final SmbException e )
				{
					/*
					 * We may not have been able to access the parent here, or
					 * the parent directory/ies could not be created.
					 *
					 * We ignore these situations, since either may be caused by
					 * various non-fatal problems.
					 */
				}

				final OutputStream out = new SmbFileOutputStream( file, append );
				try
				{
					out.write( data );
				}
				finally
				{
					out.close();
				}
			}
			else if ( "socket".equals( scheme ) )
			{
				final Socket socket = new Socket();
				try
				{
					socket.connect( new InetSocketAddress( uri.getHost(), uri.getPort() ), 10000 );
					final OutputStream out = socket.getOutputStream();
					out.write( data );
				}
				finally
				{
					socket.close();
				}
			}
			else
			{
				if ( LOG.isTraceEnabled() )
				{
					LOG.debug( "falling back to default protocol handler" );
				}

				final URL url = uri.toURL();
				final URLConnection connection = url.openConnection();
				connection.setDoInput( false );
				connection.setDoOutput( true );

				final OutputStream out = connection.getOutputStream();
				try
				{
					out.write( data );
				}
				finally
				{
					out.close();
				}
			}
		}
		catch ( final SecurityException e )
		{
			throw new IOException( String.valueOf( e ) );
		}
	}

	/**
	 * Send data to keyboard.
	 *
	 * @param uri  Keyboard URI.
	 * @param data Data to send.
	 *
	 * @throws IOException if keyboard can not be accessed.
	 */
	private static void senTodKeyboard( @NotNull final URI uri, @NotNull final byte[] data )
	throws IOException
	{
		final URIPath uriPath = new URIPath( uri );
		final Map<String, String> parameters = uriPath.getParameters();

		String encoding = parameters.get( "encoding" );
		if ( encoding == null )
		{
			encoding = "UTF-8";
		}

		final String string = new String( data, encoding );

		try
		{
			RobotTools.sendString( string );
		}
		catch ( final Throwable t )
		{
			throw new IOException( "Failed to send to keyboard: " + t, t );
		}
	}

	/**
	 * Open an FTP connection to the server specified in the given URI.
	 *
	 * @param uri FTP URI.
	 *
	 * @return {@link FTPClient} with open FTP connection.
	 *
	 * @throws IOException if it was not possible to connect through FTP.
	 */
	public static FTPClient openFtpConnection( final URI uri )
	throws IOException
	{
		/*
		 * Get FTP connection properties.
		 */
		final String user;
		final String password;

		final String userInfo = uri.getUserInfo();
		if ( userInfo != null )
		{
			final int colon = userInfo.indexOf( (int)':' );
			if ( colon >= 0 )
			{
				user = userInfo.substring( 0, colon );
				password = userInfo.substring( colon + 1 );
			}
			else
			{
				user = userInfo;
				password = "";
			}
		}
		else
		{
			user = "anonymous";
			password = "";
		}

		final String hostname = uri.getHost();
		if ( TextTools.isEmpty( hostname ) )
		{
			throw new IOException( "Missing hostname in  URI: " + uri );
		}

		final int port = ( uri.getPort() >= 0 ) ? uri.getPort() : 21;

		/*
		 * Setup FTP session.
		 */
		final FTPClient result = new FTPClient();
		boolean success = false;
		try
		{
			final URIPath path = new URIPath( uri );
			final Map<String, String> parameters = path.getParameters();
			final String connectMode = parameters.get( "mode" );

			result.setConnectTimeout( 10000 );
			result.connect( hostname, port );
			result.setSoTimeout( 10000 );
			result.setDataTimeout( 10000 );
			result.login( user, password );

			if ( "active".equals( connectMode ) )
			{
				result.enterLocalActiveMode();
			}
			else //if ( "passive".equals( connectMode ) )
			{
				result.enterLocalPassiveMode();
			}

			result.setFileType( FTP.BINARY_FILE_TYPE );

			if ( LOG.isTraceEnabled() )
			{
				result.addProtocolCommandListener( new ProtocolCommandListener()
				{
					@Override
					public void protocolCommandSent( @NotNull final ProtocolCommandEvent event )
					{
						LOG.trace( uri + ": client: " + event.getMessage() );
					}

					@Override
					public void protocolReplyReceived( @NotNull final ProtocolCommandEvent event )
					{
						LOG.trace( uri + ": server: " + event.getMessage() );
					}
				} );
			}

			success = true;
		}
		finally
		{
			if ( !success && result.isConnected() )
			{
				try
				{
					result.quit();
				}
				catch ( final Throwable t )
				{
					/* we don't care if we already have a problem */
				}

				try
				{
					result.disconnect();
				}
				catch ( final Throwable t )
				{
					/* we don't care if we already have a problem */
				}
			}
		}

		return result;
	}

	/**
	 * Opens a connection to the specified resource.
	 *
	 * @param uri Resource to connect to.
	 *
	 * @return Connection for the given URI.
	 */
	public static URIConnection openConnection( final URI uri )
	{
		// TODO: Provide more efficient implementations for specific schemes.
		return new BasicURIConnection( uri );
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private URITools()
	{
	}

	/**
	 * Represents the path component of a Uniform Resource Identifier (URI), as
	 * specified by <a href="http://www.ietf.org/rfc/rfc3986.txt">RFC 3986: URI
	 * Generic Syntax</a>. Path segments can be further parsed to simplify
	 * interpretation of common Internet schemes defined in <a
	 * href="http://www.ietf.org/rfc/rfc1738.txt"> RFC 1738: Uniform Resource
	 * Locators (URL)</a>.
	 */
	public static class URIPath
	{
		/**
		 * Whether the path is absolute. Absolute paths start with a slash.
		 *
		 * Note that a URI with authority and an empty path is defined to be
		 * absolute, but since the path is empty (and contains no slash), this
		 * method will return {@code false}.
		 */
		private final boolean _absolute;

		/**
		 * Whether the path is a directory. A path is understood to denote a
		 * directory if and only if it ends with a slash.
		 */
		private final boolean _directory;

		/**
		 * Percent-decoded path segments without scheme-specific parameters.
		 */
		private final List<String> _segments;

		/**
		 * Scheme-specific parameters, extracted from all path segments. Used by
		 * common Internet schemas such as FTP and HTTP. The following syntax is
		 * used:
		 * <pre>
		 * segment = name ( ";" key ( "=" value )? )*
		 * </pre>
		 */
		private final Map<String, String> _parameters;

		/**
		 * Constructs a new URI path.
		 *
		 * @param uri URI to get the path from.
		 *
		 * @throws IllegalArgumentException if the given URI is opaque.
		 */
		public URIPath( @NotNull final URI uri )
		{
			this( uri.getRawPath() /* null for opaque URLs */ );
		}

		/**
		 * Constructs a new URI path.
		 *
		 * @param rawPath URI path, percent-encoded.
		 */
		public URIPath( @NotNull final String rawPath )
		{
			_absolute = TextTools.startsWith( rawPath, '/' );
			_directory = TextTools.endsWith( rawPath, '/' );

			final List<String> segments;
			Map<String, String> parameters = null;

			if ( TextTools.isEmpty( rawPath ) )
			{
				segments = Collections.emptyList();
			}
			else
			{
				final String[] rawSplit = rawPath.split( "/" );
				segments = new ArrayList<String>( rawSplit.length );

				for ( int i = 0; i < rawSplit.length; i++ )
				{
					final String segment = rawSplit[ i ];
					if ( ( i != 0 ) || !segment.isEmpty() )
					{
						final String[] pathAndParameters = segment.split( ";" );
						if ( pathAndParameters.length > 0 )
						{
							segments.add( percentDecode( pathAndParameters[ 0 ] ) );

							if ( pathAndParameters.length > 1 )
							{
								if ( parameters == null )
								{
									parameters = new LinkedHashMap<String, String>();
								}

								for ( int j = 1; j < pathAndParameters.length; j++ )
								{
									final String entry = pathAndParameters[ j ];
									final int equals = entry.indexOf( '=' );
									final String key = ( equals == -1 ) ? entry : entry.substring( 0, equals );
									final String value = ( equals == -1 ) ? null : percentDecode( entry.substring( equals + 1 ) );
									parameters.put( percentDecode( key ), value );
								}
							}
						}
					}
				}
			}

			_segments = segments;
			_parameters = ( parameters == null ) ? Collections.<String, String>emptyMap() : parameters;
		}

		@NotNull
		public List<String> getSegments()
		{
			return Collections.unmodifiableList( _segments );
		}

		@NotNull
		public Map<String, String> getParameters()
		{
			return Collections.unmodifiableMap( _parameters );
		}

		public boolean isAbsolute()
		{
			return _absolute;
		}

		public boolean isDirectory()
		{
			return _directory;
		}

		/**
		 * Returns the directory part of the path, i.e. the path up to and
		 * including the last slash. Any parameters that the path may contain
		 * are not included in the result.
		 *
		 * @return Directory name.
		 */
		@NotNull
		public String getDirectory()
		{
			final String result;

			final List<String> segments = getSegments();
			final int segmentCount = isDirectory() ? segments.size() : segments.size() - 1;
			if ( segmentCount <= 0 )
			{
				result = isAbsolute() ? "/" : "";
			}
			else
			{
				final StringBuilder sb = new StringBuilder();

				for ( int i = 0; i < segmentCount; i++ )
				{
					if ( ( i > 0 ) || isAbsolute() )
					{
						sb.append( '/' );
					}
					sb.append( segments.get( i ) );
				}

				sb.append( '/' );
				result = sb.toString();
			}

			return result;
		}

		/**
		 * Returns the directory part of the path, i.e. the path up to and
		 * excluding the last slash. Any parameters that the path may contain
		 * are not included in the result.
		 *
		 * @return Directory name.
		 */
		@NotNull
		public String getDirectoryWithoutSlash()
		{
			final String result;

			final List<String> segments = getSegments();
			final int segmentCount = isDirectory() ? segments.size() : segments.size() - 1;
			if ( segmentCount <= 0 )
			{
				result = "";
			}
			else
			{
				final StringBuilder sb = new StringBuilder();

				for ( int i = 0; i < segmentCount; i++ )
				{
					if ( ( i > 0 ) || isAbsolute() )
					{
						sb.append( '/' );
					}
					sb.append( segments.get( i ) );
				}

				result = sb.toString();
			}

			return result;
		}

		/**
		 * Returns the file name part of the path. Any parameters that the path
		 * may contain are not included in the result.
		 *
		 * @return File name; empty for a path denoting a directory.
		 */
		@NotNull
		public String getFile()
		{
			final String result;

			if ( isDirectory() || isEmpty() )
			{
				result = "";
			}
			else
			{
				final List<String> segments = getSegments();
				result = segments.get( segments.size() - 1 );
			}

			return result;
		}

		/**
		 * Returns the path as string. Any parameters that the path may contain
		 * are not included in the result.
		 *
		 * @return Directory name.
		 */
		@NotNull
		public String getPath()
		{
			final String result;

			final List<String> segments = getSegments();
			final int segmentCount = segments.size();
			if ( segmentCount == 0 )
			{
				result = isDirectory() ? "/" : "";
			}
			else
			{
				final StringBuilder sb = new StringBuilder();

				for ( int i = 0; i < segmentCount; i++ )
				{
					if ( ( i > 0 ) || isAbsolute() )
					{
						sb.append( '/' );
					}
					sb.append( segments.get( i ) );
				}

				if ( isDirectory() )
				{
					sb.append( '/' );
				}

				result = sb.toString();
			}

			return result;
		}

		/**
		 * Returns whether the URL is empty.
		 *
		 * @return {@code true} if the URL is empty.
		 */
		private boolean isEmpty()
		{
			return _segments.isEmpty();
		}

		@Override
		public String toString()
		{
			final StringBuilder result = new StringBuilder();

			if ( isAbsolute() )
			{
				result.append( '/' );
			}

			final List<String> segments = getSegments();
			final Map<String, String> parameters = getParameters();

			boolean noColon = !isAbsolute();

			try
			{
				for ( final Iterator<String> it = segments.iterator(); it.hasNext(); )
				{
					percentEncode( result, it.next(), noColon ? PATH_DELIMS : PATH_DELIMS_COLON );
					if ( it.hasNext() )
					{
						result.append( '/' );
					}
					else
					{
						for ( final Map.Entry<String, String> entry : parameters.entrySet() )
						{
							result.append( ';' );
							percentEncode( result, entry.getKey() );
							if ( entry.getValue() != null )
							{
								result.append( '=' );
								percentEncode( result, entry.getValue() );
							}
						}
					}

					noColon = false;
				}
			}
			catch ( final IOException e )
			{
				// Never thrown by string builder.
				throw new AssertionError( e );
			}

			if ( isDirectory() )
			{
				if ( !segments.isEmpty() || !isAbsolute() )
				{
					result.append( '/' );
				}
			}

			return result.toString();
		}
	}

	/**
	 * RFC 3986 grammar: characters allowed in 'segment-nz-nc' production rule,
	 * excluding the semi-colon, which delimits the segment's parameters.
	 */
	private static final char[] PATH_DELIMS = { '@', '!', '$', '&', '\'', '(', ')', '*', '+', ',', '=' };

	/**
	 * RFC 3986 grammar: characters allowed in the 'pchar' production rule,
	 * excluding the semi-colon, which delimits the segment's parameters.
	 */
	private static final char[] PATH_DELIMS_COLON = { '@', ':', '!', '$', '&', '\'', '(', ')', '*', '+', ',', '=' };

	/**
	 * Returns whether the given character is unreserved.
	 *
	 * @param c Character.
	 *
	 * @return {@code true} for unreserved characters.
	 */
	private static boolean isUnreserved( final char c )
	{
		return ( c >= 'A' ) && ( c <= 'Z' ) ||
		       ( c >= 'a' ) && ( c <= 'z' ) ||
		       ( c >= '0' ) && ( c <= '9' ) ||
		       ( c == '-' ) ||
		       ( c == '.' ) ||
		       ( c == '_' ) ||
		       ( c == '~' );
	}

	/**
	 * Percent-encodes the given string and appends the result to the given
	 * appendable.
	 *
	 * @param out     Character sequence to append the result to.
	 * @param s       String to be encoded.
	 * @param allowed Characters that, in addition to unreserved characters,
	 *                must not be encoded.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private static void percentEncode( final Appendable out, final String s, final char... allowed )
	throws IOException
	{
		final char[] pair = new char[ 2 ];

		for ( int i = 0; i < s.length(); i = s.offsetByCodePoints( i, 1 ) )
		{
			final int codePoint = s.codePointAt( i );
			if ( Character.toChars( codePoint, pair, 0 ) == 1 )
			{
				final char c = pair[ 0 ];
				if ( isUnreserved( c ) )
				{
					out.append( c );
				}
				else
				{
					boolean encode = true;
					for ( final char allowedChar : allowed )
					{
						if ( c == allowedChar )
						{
							encode = false;
							break;
						}
					}

					if ( encode )
					{
						final String str = String.valueOf( c );
						for ( final byte b : str.getBytes( "UTF-8" ) )
						{
							appendPercentEncodedByte( out, b );
						}
					}
					else
					{
						out.append( c );
					}
				}
			}
			else
			{
				final String str = String.valueOf( pair );
				for ( final byte b : str.getBytes( "UTF-8" ) )
				{
					appendPercentEncodedByte( out, b );
				}
			}
		}
	}

	/**
	 * Percent-encodes the given byte and appends the result to the given
	 * appendable.
	 *
	 * @param out Character sequence to append the result to.
	 * @param b   Byte to be encoded.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private static void appendPercentEncodedByte( final Appendable out, final byte b )
	throws IOException
	{
		final char toUpper = (char)( (int)'A' - (int)'a' );

		out.append( '%' );

		{
			char c = Character.forDigit( ( b >> 4 ) & 0xf, 16 );
			if ( c >= 'a' )
			{
				c += toUpper;
			}
			out.append( c );
		}

		{
			char c = Character.forDigit( (int)b & 0xf, 16 );
			if ( c >= 'a' )
			{
				c += toUpper;
			}
			out.append( c );
		}
	}

	/**
	 * Percent-decodes the given string.
	 *
	 * @param s String to be decoded.
	 *
	 * @return Decoded string.
	 */
	private static String percentDecode( final String s )
	{
		try
		{
			return URLDecoder.decode( s, "UTF-8" );
		}
		catch ( final UnsupportedEncodingException e )
		{
			// Never thrown for UTF-8 encoding.
			throw new AssertionError( e );
		}
	}
}
