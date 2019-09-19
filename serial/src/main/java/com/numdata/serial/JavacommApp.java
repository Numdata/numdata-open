/*
 * Copyright (c) 2010-2017, Numdata BV, The Netherlands.
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
package com.numdata.serial;

import java.io.*;
import java.net.*;
import java.util.*;

import com.numdata.oss.*;
import com.numdata.serial.Javacomm.*;

/**
 * Small application to diagnose basic {@link Javacomm} functionality.
 *
 * @author  Peter S. Heijnen
 */
public class JavacommApp
{
	/**
	 * If enabled, all transmitted bytes are printed to the standard output
	 * in hexadecimal notation.
	 */
	private boolean _hexdump;

	/**
	 * Number of bytes written to hex dump so far.
	 */
	private int _hexDumpBytesWritten = 0;

	/**
	 * Width of a hex dump row.
	 */
	private int _hexDumpWidth = 20;

	/**
	 * If enabled, no carriage return characters are transmitted.
	 */
	private boolean _noCarriageReturn;

	/**
	 * Constructs a new instance.
	 */
	public JavacommApp()
	{
	}

	public void setHexdump( final boolean hexdump )
	{
		_hexdump = hexdump;
	}

	public boolean isHexdump()
	{
		return _hexdump;
	}

	public int getHexDumpWidth()
	{
		return _hexDumpWidth;
	}

	public void setHexDumpWidth( final int hexDumpWidth )
	{
		_hexDumpWidth = hexDumpWidth;
	}

	public void setNoCarriageReturn( final boolean noCarriageReturn )
	{
		_noCarriageReturn = noCarriageReturn;
	}

	public boolean isNoCarriageReturn()
	{
		return _noCarriageReturn;
	}

	/**
	 * List available serial ports.
	 */
	private void listSerialPorts()
	{
		final Javacomm javacomm = Javacomm.getInstance();
		final List<String> serialPortNames = javacomm.getSerialPortNames();

		if ( serialPortNames.isEmpty() )
		{
			System.out.println( "No serial ports available." );
		}
		else
		{
			System.out.println( "Available serial ports:" );

			for ( final String name : serialPortNames )
			{
				System.out.println( "  " + name );
			}
		}
	}

	/**
	 * Listen to a port and dump incoming data.
	 *
	 * @param   portName    Name of port to listen to.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	private void listenToPort( final String portName )
		throws IOException
	{
		final Javacomm jc = Javacomm.getInstance();

		System.out.println( "Opening port '" + portName + '\'' );
		final SerialPort port = jc.openSerialPort( portName, 9600, DataBits.EIGHT, Parity.NONE, StopBits.ONE );

		System.out.println( "Listening" );

		final InputStream is = port.getInputStream();

		while ( true )
		{
			final int b = is.read();
			if ( b < 0 )
			{
				break;
			}

			System.out.println( TextTools.toHexString( b, 2, true ) + ' ' + TextTools.getFixed( String.valueOf( b ), 3, true, ' ' ) + " '" + (char)b + '\'' );
		}

		System.out.println( "Port closed" );
	}

	/**
	 * Sends data from {@link System#in} to the given port.
	 *
	 * @param port Name of the port.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void sendToPort( final String port )
	throws IOException
	{

		final Javacomm jc = Javacomm.getInstance();
		final SerialPort serialPort = jc.openSerialPort( URI.create( "serial:" + port ) );
		try
		{
			final OutputStream out = serialPort.getOutputStream();
			int b;
			while ( ( b = System.in.read() ) != -1 )
			{
				if ( !isNoCarriageReturn() || ( b != '\r' ) )
				{
					out.write( b );

					if ( isHexdump() )
					{
						hexDump( b );
					}
				}
			}
			out.close();
		}
		finally
		{
			serialPort.close();
		}
	}

	/**
	 * Prints the hexadecimal representation of the given byte to the standard
	 * output.
	 *
	 * @param b Byte to dump.
	 */
	private void hexDump( final int b )
	{
		System.out.print( TextTools.toHexString( b, 2, false ) );
		if ( ++_hexDumpBytesWritten % _hexDumpWidth == 0 )
		{
			System.out.println();
		}
		else
		{
			System.out.print( ' ' );
		}
	}

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 *
	 * @throws Exception if the application crashes.
	 */
	public static void main( final String[] args )
	throws Exception
	{
		final JavacommApp app = new JavacommApp();

		final List<String> arguments = new ArrayList<String>( Arrays.asList( args ) );
		for ( Iterator<String> iterator = arguments.iterator(); iterator.hasNext(); )
		{
			final String argument = iterator.next();
			if ( argument.startsWith( "--" ) )
			{
				if ( "--hexdump".equals( argument ) )
				{
					app.setHexdump( true );
				}
				else if ( "--nocr".equals( argument ) )
				{
					app.setNoCarriageReturn( true );
				}
				else
				{
					System.err.println( "Unknown option: " + args );
				}
				iterator.remove();
			}
		}

		if ( arguments.isEmpty() )
		{
			System.err.println( "Usage:" );
			System.err.println( "  list           List all serial ports." );
			System.err.println( "  listen PORT    Print incoming data from specified PORT on standard output." );
			System.err.println( "  send PORT      Send data from standard input to specified PORT." );
			System.err.println();
			System.err.println( "Options:" );
			System.err.println( "  --hexdump      Print transmitted bytes in hexadecimal format." );
			System.err.println( "  --nocr         Send no carriage return characters." );
		}
		else
		{
			final String command = arguments.remove( 0 );
			if ( "list".equals( command ) )
			{
				app.listSerialPorts();
			}
			else if ( "listen".equals( command ) )
			{
				final String port = arguments.remove( 0 );
				app.listenToPort( port );
			}
			else if ( "send".equals( command ) )
			{
				final String port = arguments.remove( 0 );
				app.sendToPort( port );
			}
			else
			{
				System.err.println( "Unsupported command: " + command );
			}
		}
	}
}
