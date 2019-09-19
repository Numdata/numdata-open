/*
 * Copyright (c) 2013-2017, Numdata BV, The Netherlands.
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
package com.numdata.printer;

import java.awt.print.*;
import java.io.*;
import java.net.*;

import com.numdata.oss.*;

/**
 * Client for the Line Printer Daemon Protocol.
 *
 * @author Gerrit Meinders
 * @see <a href="http://tools.ietf.org/html/rfc1179">RFC 1179: Line Printer
 * Daemon Protocol</a>
 */
public class LPDClient
{
	/**
	 * The default LPD port number.
	 */
	public static final int DEFAULT_PORT = 515;

	/**
	 * Host name of the printer.
	 */
	private final String _hostname;

	/**
	 * Network port used by the LPD service.
	 */
	private final int _networkPort;

	/**
	 * Queue name to be used.
	 */
	private final String _queueName;

	/**
	 * Username to provide to the printer.
	 */
	private final String _userName;

	/**
	 * Constructs a new instance.
	 *
	 * @param printerSettings Printer settings to use.
	 */
	public LPDClient( final PrinterSettings printerSettings )
	{
		this( printerSettings.getHostname(), printerSettings.getNetworkPort(), printerSettings.getQueueName() );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param hostname    Host name or IP address of server.
	 * @param networkPort Port to use on host (0 or less to use default).
	 * @param queueName   Name of remote printer queue or port.
	 */
	public LPDClient( final String hostname, final int networkPort, final String queueName )
	{
		_hostname = hostname;
		_networkPort = ( networkPort <= 0 ) ? DEFAULT_PORT : networkPort;
		_queueName = queueName;
		_userName = "Unknown";
	}

	/**
	 * Requests information about the printer queue.
	 *
	 * @param longFormat {@code false} for short format; {@code true} for long
	 *                   format.
	 *
	 * @return Printer queue information.
	 *
	 * @throws PrinterException if an error occurs while accessing the printer.
	 */
	public String requestQueueState( final boolean longFormat )
	throws PrinterException
	{
		try
		{
			final Socket socket = connect();
			try
			{
				socket.setSoTimeout( 30000 );

				final InputStream in = socket.getInputStream();
				final OutputStream out = socket.getOutputStream();
				final Writer writer = new OutputStreamWriter( out, "US-ASCII" );

				writer.write( longFormat ? 0x04 : 0x03 );
				writer.write( _queueName );
				writer.write( '\n' );
				writer.flush();

				return TextTools.loadText( in );
			}
			finally
			{
				try
				{
					socket.close();
				}
				catch ( final IOException ignored )
				{
				}
			}
		}
		catch ( final IOException e )
		{
			throw new PrinterIOException( e );
		}
	}

	/**
	 * Removes the currently active job.
	 *
	 * @throws PrinterException if an error occurs while accessing the printer.
	 */
	public void removeCurrentJob()
	throws PrinterException
	{
		try
		{
			final Socket socket = connect();
			try
			{
				socket.setSoTimeout( 30000 );

				final InputStream in = socket.getInputStream();
				final OutputStream out = socket.getOutputStream();
				final Writer writer = new OutputStreamWriter( out, "US-ASCII" );

				writer.write( 0x05 );
				writer.write( _queueName );
				writer.write( ' ' );
				writer.write( _userName );
				writer.write( '\n' );
				writer.flush();

				if ( in.read() != 0 )
				{
					throw new PrinterException( "LPD print server '" + _hostname + "' refused to remove jobs." );
				}
			}
			finally
			{
				try
				{
					socket.close();
				}
				catch ( final IOException ignored )
				{
				}
			}
		}
		catch ( final IOException e )
		{
			throw new PrinterIOException( e );
		}
	}

	/**
	 * Print a document to a LPD print server on a network host.
	 *
	 * This implementation conforms to RFC1179, except for the fact that it does
	 * not use local port numbers 721 - 731, which may not always be available.
	 *
	 * @param documentName Name of document to display in queue.
	 * @param documentData Document data to print.
	 * @param printRaw     Print raw vs. cooked data.
	 *
	 * @throws PrinterException if there was a problem sending the document to
	 * the printer.
	 */
	public void print( final String documentName, final byte[] documentData, final boolean printRaw )
	throws PrinterException
	{
		System.out.println( "LPD.print( " + documentName + ", " + documentData.length + " bytes of data, " + printRaw + " )" );
		try
		{
			final Socket socket = connect();
			try
			{
				final String jobNumber = "001"; // 001 to 999 (should cycle?)

				socket.setSoTimeout( 30000 );
				final String localHostName = "localhost";

				final InputStream in = socket.getInputStream();
				final OutputStream out = socket.getOutputStream();
				final Writer writer = new OutputStreamWriter( out, "US-ASCII" );
				final String hostname = _hostname;

				// Open printer

				writer.write( 0x02 );
				writer.write( _queueName );
				writer.write( (int)'\n' );
				writer.flush();

				if ( in.read() != 0 )
				{
					throw new FatalPrinterException( "LPD print server '" + hostname + "' did not recognize printer queue name '" + _queueName + '\'' );
				}

				// Send control file

				final StringBuilder controlFile = new StringBuilder( 80 );
				controlFile.append( 'H' );
				controlFile.append( localHostName );
				controlFile.append( '\n' );

				controlFile.append( 'P' );
				controlFile.append( _userName );
				controlFile.append( '\n' );

				controlFile.append( printRaw ? "o" : "p" );
				controlFile.append( "dfA" );
				controlFile.append( jobNumber );
				controlFile.append( localHostName );
				controlFile.append( '\n' );

				controlFile.append( "UdfA" );
				controlFile.append( jobNumber );
				controlFile.append( localHostName );
				controlFile.append( '\n' );
				controlFile.append( 'N' );
				controlFile.append( documentName );
				controlFile.append( '\n' );

				writer.write( 0x02 );
				writer.write( String.valueOf( controlFile.length() ) );
				writer.write( " cfA" );
				writer.write( jobNumber );
				writer.write( localHostName );
				writer.write( (int)'\n' );
				writer.flush();

				if ( in.read() != 0 )
				{
					throw new PrinterException( "LPD print server '" + hostname + "' refused print job '" + documentName + '\'' );
				}

				writer.write( controlFile.toString() );
				writer.write( 0 );
				writer.flush();

				if ( in.read() != 0 )
				{
					throw new PrinterException( "LPD print server '" + hostname + "' refused print job '" + documentName + "' control file" );
				}

				// Send print file

				writer.write( 0x03 );
				writer.write( String.valueOf( documentData.length ) );
				writer.write( " dfA" );
				writer.write( jobNumber );
				writer.write( localHostName );
				writer.write( (int)'\n' );
				writer.flush();

				if ( in.read() != 0 )
				{
					throw new PrinterException( "LPD print server '" + hostname + "' refused print job '" + documentName + "' document header" );
				}

				out.write( documentData );
				out.write( 0 );
				out.flush();

				if ( in.read() != 0 )
				{
					throw new PrinterException( "LPD print server '" + hostname + "' refused print job '" + documentName + "' document data" );
				}
			}
			finally
			{
				try
				{
					socket.close();
				}
				catch ( final IOException ignored )
				{
				}
			}
		}
		catch ( final IOException e )
		{
			throw new PrinterIOException( e );
		}
	}

	/**
	 * Connects to the LPD print server. This method will retry a few times if
	 * needed, because some printers occasionally refuse incoming connections.
	 *
	 * @return Connected socket.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private Socket connect()
	throws IOException
	{
		Socket result = null;

		int attempts = 3;
		while ( attempts > 0 )
		{
			try
			{
				result = new Socket( _hostname, _networkPort );
				break;
			}
			catch ( final ConnectException e )
			{
				if ( --attempts == 0 )
				{
					throw e;
				}

				try
				{
					Thread.sleep( 10L );
				}
				catch ( final InterruptedException ignored )
				{
					Thread.currentThread().interrupt();
					break;
				}
			}
		}

		if ( result == null )
		{
			throw new AssertionError();
		}

		return result;
	}
}
