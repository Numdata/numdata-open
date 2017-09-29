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
import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class provides an implementation-independent wrapper around the Java
 * Communications API. It only supports communication using serial ports.
 *
 * Since the RXTX people decided that they should not use the 'javax.comm'
 * namespace but 'gnu.io' instead (which is correct, but cumbersome for
 * applications), this class was created to decouple the implementation from
 * application code.
 *
 * @author Peter S. Heijnen
 */
public abstract class Javacomm
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( Javacomm.class );

	/**
	 * Get {@link Javacomm} implementation.
	 *
	 * @return {@link Javacomm} instance.
	 */
	public static Javacomm getInstance()
	{
		LOG.trace( "JavaComm.getInstance" );

		Javacomm result = null;

		try
		{
			final Class<? extends Javacomm> implClass = JavacommSunImpl.class;
			final Constructor<? extends Javacomm> defaultConstructor = implClass.getDeclaredConstructor();
			result = defaultConstructor.newInstance();
		}
		catch ( Throwable e )
		{
			LOG.trace( "Sun implementation is not available" );
		}

		if ( result == null )
		{
			try
			{
				final Class<? extends Javacomm> implClass = JavacommRxTxImpl.class;
				final Constructor<? extends Javacomm> defaultConstructor = implClass.getDeclaredConstructor();
				result = defaultConstructor.newInstance();
			}
			catch ( Throwable e )
			{
				LOG.trace( "RXTX implementation is not available" );
			}
		}

		if ( result == null )
		{
			result = new JavacommNullImpl();
		}

		return result;
	}

	/**
	 * Get list of available serial ports.
	 *
	 * @return List of available serial ports.
	 */
	@NotNull
	public abstract List<String> getSerialPortNames();

	/**
	 * Write data to serial port identified by an {@link URI}.
	 *
	 * @param uri  URI for data destination.
	 * @param data Data to write.
	 *
	 * @throws IOException if the data could not be written.
	 */
	public void sendToSerialPort( final URI uri, final byte[] data )
	throws IOException
	{
		final SerialPort port = openSerialPort( uri );
		try
		{
			final OutputStream out = port.getOutputStream();
			out.write( data );
		}
		finally
		{
			port.close();
		}
	}

	/**
	 * Open a serial port for communications using a URI. URI syntax:
	 * <pre>
	 * serial:&lt;portname&gt;[;baudrate;=&lt;baudrate&gt;][;databits=5|6|7|8][;parity=even|mark|none|odd|space][;stopbits=1|1.5|2]
	 * </pre>
	 * Default communication parameters are: 9600 baud, 8 databits, no parity, 1
	 * stop bit (aka '8N1'), <dl>
	 *
	 * <dt>NOTE 1:</dt><dd> The caller must close the port to make sure the
	 * port does not remain locked.</dd>
	 *
	 * <dt>NOTE 2:</dt><dd> There is no platform-independent naming scheme for
	 * communication ports. Some drivers may include duplicate and/or unsorted
	 * port entries, so there is no easy solution for this. Just try to use
	 * common sense here (e.g. 'COM1:' on Windows systems and 'ttyS0' or 'cua0'
	 * on Linux and similar systems).</dd>
	 *
	 * </dl>
	 *
	 * @param uri Serial URI.
	 *
	 * @return Open {@link SerialPort}.
	 *
	 * @throws IOException if it was not possible to open the specified port.
	 */
	public SerialPort openSerialPort( final URI uri )
	throws IOException
	{
		final String[] schemeSpecificParts = TextTools.tokenize( uri.getSchemeSpecificPart(), ';' );

		final String portName = schemeSpecificParts[ 0 ];
		if ( TextTools.isEmpty( portName ) )
		{
			throw new IOException( "Missing port name in URI: " + uri );
		}

		int baudrate = 9600;
		DataBits databits = DataBits.EIGHT;
		Parity parity = Parity.NONE;
		StopBits stopbits = StopBits.ONE;

		for ( int i = 1; i < schemeSpecificParts.length; i++ )
		{
			final String parameter = schemeSpecificParts[ i ];

			final int equals = parameter.indexOf( (int)'=' );
			final String name = ( equals >= 0 ) ? parameter.substring( 0, equals ) : parameter;
			final String value = ( equals >= 0 ) ? parameter.substring( equals + 1 ) : null;

			if ( "baudrate".equals( name ) )
			{
				try
				{
					baudrate = Integer.parseInt( value );
				}
				catch ( NumberFormatException nfe )
				{
					throw new IOException( "Bad '" + name + "' in URI: " + uri, nfe );
				}
			}
			else if ( "databits".equals( name ) )
			{
				if ( "5".equals( value ) )
				{
					databits = DataBits.FIVE;
				}
				else if ( "6".equals( value ) )
				{
					databits = DataBits.SIX;
				}
				else if ( "7".equals( value ) )
				{
					databits = DataBits.SEVEN;
				}
				else if ( "8".equals( value ) )
				{
					databits = DataBits.EIGHT;
				}
				else
				{
					throw new IOException( "Bad '" + name + "' in URI: " + uri );
				}
			}
			else if ( "parity".equals( name ) )
			{
				if ( "even".equals( value ) )
				{
					parity = Parity.EVEN;
				}
				else if ( "mark".equals( value ) )
				{
					parity = Parity.MARK;
				}
				else if ( "none".equals( value ) )
				{
					parity = Parity.NONE;
				}
				else if ( "odd".equals( value ) )
				{
					parity = Parity.ODD;
				}
				else if ( "space".equals( value ) )
				{
					parity = Parity.SPACE;
				}
				else
				{
					throw new IOException( "Bad '" + name + "' in URI: " + uri );
				}
			}
			else if ( "stopbits".equals( name ) )
			{
				if ( "1".equals( value ) )
				{
					stopbits = StopBits.ONE;
				}
				else if ( "1.5".equals( value ) )
				{
					stopbits = StopBits.ONE_AND_A_HALF;
				}
				else if ( "2".equals( value ) )
				{
					stopbits = StopBits.TWO;
				}
				else
				{
					throw new IOException( "Bad '" + name + "' in URI: " + uri );
				}
			}
			else
			{
				throw new IOException( "Unrecognized '" + parameter + "' in URI: " + uri );
			}
		}

		return openSerialPort( portName, baudrate, databits, parity, stopbits );
	}

	/**
	 * Open serial port.
	 *
	 * @param portName Port name (platform-dependent).
	 * @param baudRate Baud rate.
	 * @param dataBits Data bits.
	 * @param parity   Parity.
	 * @param stopBits Stop bits.
	 *
	 * @return Open serial port.
	 *
	 * @throws IOException if the port could not be opened.
	 */
	public abstract SerialPort openSerialPort( @NotNull String portName, int baudRate, @NotNull DataBits dataBits, @NotNull Parity parity, @NotNull StopBits stopBits )
	throws IOException;

	/**
	 * Data bits options.
	 */
	public enum DataBits
	{
		/**
		 * Five data bits.
		 */
		FIVE,

		/**
		 * Six data bits.
		 */
		SIX,

		/**
		 * Seven data bits.
		 */
		SEVEN,

		/**
		 * Eight data bits.
		 */
		EIGHT
	}

	/**
	 * Parity options.
	 */
	public enum Parity
	{
		/**
		 * No pairty.
		 */
		NONE,

		/**
		 * Odd parity.
		 */
		ODD,

		/**
		 * Even parity.
		 */
		EVEN,

		/**
		 * Mark parity.
		 */
		MARK,

		/**
		 * Space parity.
		 */
		SPACE
	}

	/**
	 * Stop bits options.
	 */
	public enum StopBits
	{
		/**
		 * One stop bit.
		 */
		ONE,

		/**
		 * One and a half stop bit.
		 */
		ONE_AND_A_HALF,

		/**
		 * Two stop bits.
		 */
		TWO
	}

	/**
	 * Wrapper class around native serial port implementation.
	 */
	public interface SerialPort
	{
		/**
		 * Get native port name.
		 *
		 * @return Native port name.
		 */
		String getName();

		/**
		 * Close the port.
		 */
		void close();

		/**
		 * Get input stream.
		 *
		 * @return Input stream.
		 *
		 * @throws IOException if an I/O error occurs.
		 */
		InputStream getInputStream()
		throws IOException;

		/**
		 * Get output stream.
		 *
		 * @return Output stream.
		 *
		 * @throws IOException if an I/O error occurs.
		 */
		OutputStream getOutputStream()
		throws IOException;

		/**
		 * Add listener for available data from the serial port. Only one
		 * listener can be added to a port.
		 *
		 * @param listener Listener to add.
		 *
		 * @throws TooManyListenersException if no more listeners can be added.
		 */
		void addEventListener( SerialPortDataAvailableEventListener listener )
		throws TooManyListenersException;

		/**
		 * Remove event listener that was previously added.
		 */
		void removeEventListener();
	}

	/**
	 * Listener for data from a serial port.
	 */
	public interface SerialPortDataAvailableEventListener
	extends EventListener
	{
		/**
		 * Notifies listener that data from the serial port is available.
		 *
		 * @param event Event object.
		 */
		void serialPortDataAvailable( SerialPortDataAvailableEvent event );
	}

	/**
	 * Event to notify listeners about available database from the serial port.
	 */
	public static class SerialPortDataAvailableEvent
	extends EventObject
	{
		/**
		 * Serialized data version.
		 */
		private static final long serialVersionUID = -5000071576142762147L;

		/**
		 * Construct event object.
		 *
		 * @param source Source of event.
		 */
		public SerialPortDataAvailableEvent( final Object source )
		{
			super( source );
		}
	}
}
