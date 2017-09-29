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
package com.numdata.serial;

import java.io.*;
import java.util.*;
import javax.comm.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This is an implementation of the {@link Javacomm} interface based on the
 * official Sun-based implementation.
 *
 * @author  Peter S. Heijnen
 */
class JavacommSunImpl
	extends Javacomm
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JavacommSunImpl.class );

	/**
	 * Construct {@link Javacomm} implementation for RXTX implementation.
	 */
	JavacommSunImpl()
	{
		CommPortIdentifier.getPortIdentifiers();
	}

	@NotNull
	@Override
	public List<String> getSerialPortNames()
	{
		final List<String> result = new ArrayList<String>();

		for ( final Enumeration<?> e = CommPortIdentifier.getPortIdentifiers() ; e.hasMoreElements() ; )
		{
			final CommPortIdentifier id = (CommPortIdentifier)e.nextElement();
			if ( id.getPortType() == CommPortIdentifier.PORT_SERIAL )
			{
				result.add( id.getName() );
			}
		}

		return result;
	}

	@Override
	public SerialPort openSerialPort( @NotNull final String portName, final int baudrate, @NotNull final DataBits databits, @NotNull final Parity parity, @NotNull final StopBits stopbits )
		throws IOException
	{
		/*
		 * Remove trailing ":" if it was specified.
		 */
		String filteredPorTName = portName;
		if ( TextTools.endsWith( filteredPorTName, ':' ) )
		{
			filteredPorTName = filteredPorTName.substring( 0, filteredPorTName.length() - 1 );
		}

		/*
		 * Remove leading "/dev/" if it was specified.
		 */
		if ( filteredPorTName.startsWith( "/dev/" ) )
		{
			filteredPorTName = filteredPorTName.substring( 5 );
		}

		/*
		 * Search the requested port.
		 */
		CommPortIdentifier commPortIdentifier = null;

		for ( final Enumeration<?> e = CommPortIdentifier.getPortIdentifiers() ; e.hasMoreElements() ; )
		{
			final CommPortIdentifier id = (CommPortIdentifier)e.nextElement();
			if ( ( id.getPortType() == CommPortIdentifier.PORT_SERIAL ) && filteredPorTName.equalsIgnoreCase( id.getName() ) )
			{
				commPortIdentifier = id;
				break;
			}
		}

		if ( commPortIdentifier == null )
		{
			throw new IOException( "Could not find serial port '" + portName + "'!" );
		}

		final int rxtxDatabits;
		switch ( databits )
		{

			case FIVE:
				rxtxDatabits = javax.comm.SerialPort.DATABITS_5;
				break;
			case SIX:
				rxtxDatabits = javax.comm.SerialPort.DATABITS_6;
				break;
			case SEVEN:
				rxtxDatabits = javax.comm.SerialPort.DATABITS_7;
				break;
			case EIGHT:
			default:
				rxtxDatabits = javax.comm.SerialPort.DATABITS_8;
				break;
		}

		final int rxtxParity;
		switch ( parity )
		{
			case ODD:
				rxtxParity = javax.comm.SerialPort.PARITY_ODD;
				break;
			case EVEN:
				rxtxParity = javax.comm.SerialPort.PARITY_EVEN;
				break;
			case MARK:
				rxtxParity = javax.comm.SerialPort.PARITY_MARK;
				break;
			case SPACE:
				rxtxParity = javax.comm.SerialPort.PARITY_SPACE;
				break;
			case NONE:
			default:
				rxtxParity = javax.comm.SerialPort.PARITY_NONE;
				break;
		}

		final int rxtxStopbits;
		switch ( stopbits )
		{
			case TWO:
				rxtxStopbits = javax.comm.SerialPort.STOPBITS_2;
				break;
			case ONE_AND_A_HALF:
				rxtxStopbits = javax.comm.SerialPort.STOPBITS_1_5;
				break;
			case ONE:
			default:
				rxtxStopbits = javax.comm.SerialPort.STOPBITS_1;
				break;
		}


		/*
		 * Open the port.
		 */
		final javax.comm.SerialPort port;
		try
		{
			port = (javax.comm.SerialPort)commPortIdentifier.open( getClass().getName(), 2000 );
		}
		catch ( PortInUseException e )
		{
			final String message = "Serial port '" + portName + "' already in use";
			LOG.error( message, e );
			throw new IOException( message, e );
		}

		/*
		 * Set communication parameters.
		 */
		try
		{
			port.setSerialPortParams( baudrate, rxtxDatabits, rxtxStopbits, rxtxParity );
		}
		catch ( UnsupportedCommOperationException e )
		{
			final String message = "Serial port '" + portName + "' does not accept specified settings (" + baudrate + ',' + databits + ',' + stopbits + ',' + parity + ')';
			LOG.error( message, e );
			throw new IOException( message, e );
		}

		/*
		 * Create wrapper around implementation class.
		 */
		return new SerialPort()
		{
			@Override
			public void close()
			{
				port.close();
			}

			@Override
			public InputStream getInputStream() throws IOException
			{
				return port.getInputStream();
			}

			@Override
			public OutputStream getOutputStream() throws IOException
			{
				return port.getOutputStream();
			}

			@Override
			public String getName()
			{
				return port.getName();
			}

			@Override
			public void addEventListener( final SerialPortDataAvailableEventListener listener ) throws TooManyListenersException
			{
				port.addEventListener( new SerialPortEventListener()
				{
					@Override
					public void serialEvent( final SerialPortEvent serialPortEvent )
					{
						listener.serialPortDataAvailable( new SerialPortDataAvailableEvent( serialPortEvent.getSource() ) );
					}
				} );

				port.notifyOnDataAvailable( true );
			}

			@Override
			public void removeEventListener()
			{
				port.removeEventListener();
			}
		};
	}
}
