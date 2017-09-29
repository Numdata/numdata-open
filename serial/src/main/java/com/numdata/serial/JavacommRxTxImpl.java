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
import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import gnu.io.*;
import org.jetbrains.annotations.*;

/**
 * This is an implementation of the {@link Javacomm} interface based on the RXTX
 * package.
 *
 * @author  Peter S. Heijnen
 */
class JavacommRxTxImpl
	extends Javacomm
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JavacommRxTxImpl.class );

	/**
	 * Construct {@link Javacomm} implementation for RXTX implementation.
	 */
	JavacommRxTxImpl()
	{
		CommPortIdentifier.getPortIdentifiers();
	}

	@NotNull
	@Override
	public List<String> getSerialPortNames()
	{
		final List<String> result = new ArrayList<String>();

		for ( final Enumeration<?> e = CommPortIdentifier.getPortIdentifiers(); e.hasMoreElements(); )
		{
			final CommPortIdentifier id = (CommPortIdentifier)e.nextElement();
			if ( id.getPortType() == CommPortIdentifier.PORT_SERIAL )
			{
				result.add( id.getName() );
			}
		}

		return result;
	}

	/**
	 * Get serial port by name.
	 *
	 * @param   name    Serial port name.
	 *
	 * @return  Port with given name;
	 *          {@code null} if the port was not found.
	 */
	@Nullable
	public CommPortIdentifier getPortByName( @NotNull final String name )
	{
		CommPortIdentifier result = null;

		for ( final Enumeration<?> e = CommPortIdentifier.getPortIdentifiers(); e.hasMoreElements(); )
		{
			final CommPortIdentifier id = (CommPortIdentifier)e.nextElement();
			if ( ( id.getPortType() == CommPortIdentifier.PORT_SERIAL ) && name.equalsIgnoreCase( id.getName() ) )
			{
				result = id;
				break;
			}
		}

		return result;
	}

	@Override
	public SerialPort openSerialPort( @NotNull final String portName, final int baudrate, @NotNull final DataBits databits, @NotNull final Parity parity, @NotNull final StopBits stopbits )
		throws IOException
	{
		/*
		 * Search the requested port.
		 */
		CommPortIdentifier commPortIdentifier = getPortByName( portName );
		if ( commPortIdentifier == null )
		{
			/*
			 * Remove trailing ":" if it was specified.
			 */
			String filteredPortName = portName;
			if ( TextTools.endsWith( filteredPortName, ':' ) )
			{
				filteredPortName = filteredPortName.substring( 0, filteredPortName.length() - 1 );
			}

			/*
			 * Remove leading "/dev/" if it was specified.
			 */
			if ( filteredPortName.startsWith( "/dev/" ) )
			{
				filteredPortName = filteredPortName.substring( 5 );
			}

			commPortIdentifier = getPortByName( filteredPortName );
		}

		if ( commPortIdentifier == null )
		{
			throw new IOException( "Could not find serial port '" + portName + "'!" );
		}

		final int rxtxDatabits;
		switch ( databits )
		{

			case FIVE:
				rxtxDatabits = gnu.io.SerialPort.DATABITS_5;
				break;
			case SIX:
				rxtxDatabits = gnu.io.SerialPort.DATABITS_6;
				break;
			case SEVEN:
				rxtxDatabits = gnu.io.SerialPort.DATABITS_7;
				break;
			case EIGHT:
			default:
				rxtxDatabits = gnu.io.SerialPort.DATABITS_8;
				break;
		}

		final int rxtxParity;
		switch ( parity )
		{
			case ODD:
				rxtxParity = gnu.io.SerialPort.PARITY_ODD;
				break;
			case EVEN:
				rxtxParity = gnu.io.SerialPort.PARITY_EVEN;
				break;
			case MARK:
				rxtxParity = gnu.io.SerialPort.PARITY_MARK;
				break;
			case SPACE:
				rxtxParity = gnu.io.SerialPort.PARITY_SPACE;
				break;
			case NONE:
			default:
				rxtxParity = gnu.io.SerialPort.PARITY_NONE;
				break;
		}

		final int rxtxStopbits;
		switch ( stopbits )
		{
			case TWO:
				rxtxStopbits = gnu.io.SerialPort.STOPBITS_2;
				break;
			case ONE_AND_A_HALF:
				rxtxStopbits = gnu.io.SerialPort.STOPBITS_1_5;
				break;
			case ONE:
			default:
				rxtxStopbits = gnu.io.SerialPort.STOPBITS_1;
				break;
		}


		/*
		 * Open the port.
		 */
		final gnu.io.SerialPort port;
		try
		{
			final Method openMethod = getMethod( commPortIdentifier, "open", String.class, int.class );
			port = (gnu.io.SerialPort)callMethod( commPortIdentifier, openMethod, getClass().getName(), Integer.valueOf( 2000 ) );
		}
		catch ( InvocationTargetException e )
		{
			final Throwable cause = e.getCause();
			if ( cause instanceof PortInUseException )
			{
				final String message = "Serial port '" + portName + "' already in use";
				LOG.error( message, cause );
				throw new IOException( message, cause );
			}
			else
			{
				final String message = "Could not open port '" + portName + "': " + e;
				LOG.error( message, cause );
				throw new IOException( message, cause );
			}
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

	/**
	 * Call method on object.
	 *
	 * @param object     Object to call method on.
	 * @param methodName Name of the method.
	 * @param args       Method arguments.
	 *
	 * @return Method result value, if any.
	 */
	public static Object callMethod( final Object object, final String methodName, final Object... args )
	throws InvocationTargetException
	{
		final Class<?>[] parameterTypes = new Class<?>[ args.length ];
		for ( int i = 0; i < args.length; i++ )
		{
			final Object arg = args[ i ];
			parameterTypes[ i ] = ( arg == null ) ? Object.class : arg.getClass();

		}
		final Method method = getMethod( object, methodName, parameterTypes );
		return callMethod( object, method, args );
	}

	/**
	 * Call method on object.
	 *
	 * @param object Object to call method on.
	 * @param method Method of object.
	 * @param args   Method arguments.
	 *
	 * @return Method result value, if any.
	 */
	public static Object callMethod( final Object object, final Method method, final Object... args )
	throws InvocationTargetException
	{
		try
		{
			return method.invoke( object, args );
		}
		catch ( IllegalAccessException e )
		{
			throw new IllegalArgumentException( method + " not accessible for " + object, e );
		}
	}

	/**
	 * Get public {@code Method} from the given object with the given parameter
	 * types.
	 *
	 * @param object         Object to get method from.
	 * @param methodName     Name of the method.
	 * @param parameterTypes List of parameters type.
	 *
	 * @return {@code Method} that matches the specified parameters.
	 */
	public static Method getMethod( final Object object, final String methodName, final Class<?>... parameterTypes )
	{
		final Class<?> aClass = object.getClass();
		try
		{
			return aClass.getMethod( methodName, parameterTypes );
		}
		catch ( RuntimeException e )
		{
			throw e;
		}
		catch ( Exception e )
		{
			throw new RuntimeException( e );
		}

	}

}
