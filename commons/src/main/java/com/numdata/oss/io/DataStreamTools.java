/*
 * Copyright (c) 2004-2018, Numdata BV, The Netherlands.
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
package com.numdata.oss.io;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class is a toolbox with methods to read data from InputStream's and
 * write data to OutputStream's.
 *
 * Methods exists to read/write Java primitives in a format compatible to a
 * DataInputStream or DataOutputStream, but without the need to encapsulate a
 * stream in such an instance.
 *
 * Also included, are methods to write data efficiently when the amount of data
 * should be kept to a minimum, but without using actual compression.
 *
 * Methods use big-endian byte order unless specified otherwise.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "NewExceptionWithoutArguments" )
public class DataStreamTools
{
	/**
	 * Empty byte array.
	 */
	private static final byte[] NO_BYTES = new byte[ 0 ];

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private DataStreamTools()
	{
	}

	/**
	 * Close a {@link InputStream}, ignoring any exception it will generate. A
	 * {@code null} is also silently ignored.
	 *
	 * @param in {@link InputStream} to close (may be {@code null}).
	 */
	public static void close( final InputStream in )
	{
		if ( in != null )
		{
			try
			{
				in.close();
			}
			catch ( Exception e )
			{
				/* silently ignore */
			}
		}
	}

	/**
	 * Close a {@link OutputStream}, ignoring any exception it will generate. A
	 * {@code null} is also silently ignored.
	 *
	 * @param out {@link OutputStream} to close (may be {@code null}).
	 */
	public static void close( final OutputStream out )
	{
		if ( out != null )
		{
			try
			{
				out.close();
			}
			catch ( Exception e )
			{
				/* silently ignore */
			}
		}
	}

	/**
	 * Close a {@link Reader}, ignoring any exception it will generate. A {@code
	 * null} is also silently ignored.
	 *
	 * @param reader {@link Reader} to close (may be {@code null}).
	 */
	public static void close( final Reader reader )
	{
		if ( reader != null )
		{
			try
			{
				reader.close();
			}
			catch ( Exception e )
			{
				/* silently ignore */
			}
		}
	}

	/**
	 * Close a {@link Writer}, ignoring any exception it will generate. A {@code
	 * null} is also silently ignored.
	 *
	 * @param writer {@link Writer} to close (may be {@code null}).
	 */
	public static void close( final Writer writer )
	{
		if ( writer != null )
		{
			try
			{
				writer.close();
			}
			catch ( Exception e )
			{
				/* silently ignore */
			}
		}
	}

	/**
	 * Test whether the specified object is serializable. Note that some objects
	 * may implement the {@link Serializable} interface, but actually reference
	 * non-serializable objects. This method verifies actual serializability.
	 *
	 * @param object Object to test.
	 *
	 * @return {@code true} if {@code object} is serializable; {@code false}
	 * otherwise.
	 */
	public static boolean isSerializable( final Object object )
	{
		boolean result;

		try
		{
			@SuppressWarnings( "IOResourceOpenedButNotSafelyClosed" ) final ObjectOutputStream objectOutputStream = new ObjectOutputStream( new NullOutputStream() );
			//noinspection NonSerializableObjectPassedToObjectStream
			objectOutputStream.writeObject( object );
			result = true;
		}
		catch ( IOException ignored )
		{
			result = false;
		}

		return result;
	}

	/**
	 * Prints the string representation of the {@code boolean} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 * @param value  {@code boolean} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final boolean value )
	throws IOException
	{
		print( target, Boolean.toString( value ) );
	}

	/**
	 * Prints the string representation of the {@code byte} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 * @param value  {@code byte} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final byte value )
	throws IOException
	{
		print( target, Byte.toString( value ) );
	}

	/**
	 * Prints the string representation of the {@code char} argument.
	 *
	 * Note that this method will only print character codes 0 to 255. An I/O
	 * exception is generated when any other character is specified.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 * @param c      {@code char} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final char c )
	throws IOException
	{
		target.append( c );
	}

	/**
	 * Prints the string representation of the {@code int} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 * @param i      {@code int} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final int i )
	throws IOException
	{
		print( target, Integer.toString( i ) );
	}

	/**
	 * Prints the string representation of the {@code long} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 * @param l      {@code long} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final long l )
	throws IOException
	{
		print( target, Long.toString( l ) );
	}

	/**
	 * Prints the string representation of the {@code float} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 * @param f      {@code float} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final float f )
	throws IOException
	{
		print( target, Float.toString( f ) );
	}

	/**
	 * Prints the string representation of the {@code double} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 * @param d      {@code double} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final double d )
	throws IOException
	{
		print( target, Double.toString( d ) );
	}

	/**
	 * Prints the contents of the {@code char} array argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 * @param chars  Character array to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final char[] chars )
	throws IOException
	{
		if ( chars == null )
		{
			print( target, (CharSequence)null );
		}
		else
		{
			for ( final char c : chars ) // TODO maybe wrap this in a 'CharSequence'
			{
				target.append( c );
			}
		}
	}

	/**
	 * Prints the {@link CharSequence} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link java.io.PrintWriter}.
	 *
	 * @param target   Character stream to print to.
	 * @param sequence {@link CharSequence} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final CharSequence sequence )
	throws IOException
	{
		target.append( sequence );
	}

	/**
	 * Prints the string representation of the {@link Object} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link java.io.PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 * @param object {@link Object} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final Appendable target, final Object object )
	throws IOException
	{
		print( target, ( object != null ) ? object.toString() : null );
	}

	/**
	 * Prints end-of-line sequence.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link java.io.PrintWriter}.
	 *
	 * @param target Character stream to print to.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void println( final Appendable target )
	throws IOException
	{
		print( target, TextTools.getLineSeparator() );
	}

	/**
	 * Prints the string representation of the {@code boolean} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os    Stream to print to.
	 * @param value {@code boolean} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final boolean value )
	throws IOException
	{
		print( os, Boolean.toString( value ) );
	}

	/**
	 * Prints the string representation of the {@code byte} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param b  {@code byte} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final byte b )
	throws IOException
	{
		print( os, Byte.toString( b ) );
	}

	/**
	 * Prints the string representation of the {@code char} argument.
	 *
	 * Note that this method will only print character codes 0 to 255. An I/O
	 * exception is generated when any other character is specified.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param ch {@code char} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final char ch )
	throws IOException
	{
		final int value = (int)ch;
		if ( value > 0xFF )
		{
			throw new IllegalArgumentException( "illegal char (" + ch + ')' );
		}

		os.write( value );
	}

	/**
	 * Prints the string representation of the {@code int} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param i  {@code int} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final int i )
	throws IOException
	{
		print( os, Integer.toString( i ) );
	}

	/**
	 * Prints the string representation of the {@code long} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param l  {@code long} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final long l )
	throws IOException
	{
		print( os, Long.toString( l ) );
	}

	/**
	 * Prints the string representation of the {@code float} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param f  {@code float} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final float f )
	throws IOException
	{
		print( os, Float.toString( f ) );
	}

	/**
	 * Prints the string representation of the {@code double} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param d  {@code double} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final double d )
	throws IOException
	{
		print( os, Double.toString( d ) );
	}

	/**
	 * Prints the contents of the {@code char} array argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os    Stream to print to.
	 * @param chars Character array to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final char[] chars )
	throws IOException
	{
		if ( chars == null )
		{
			print( os, (CharSequence)null );
		}
		else
		{
			for ( final char value : chars )
			{
				print( os, value );
			}
		}
	}

	/**
	 * Prints the {@link CharSequence} argument.
	 *
	 * Note that this method will only print character codes 0 to 255. An I/O
	 * exception is generated when any other character is specified.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link java.io.PrintStream}.
	 *
	 * @param os       Stream to print to.
	 * @param sequence {@link CharSequence} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final CharSequence sequence )
	throws IOException
	{
		if ( sequence == null )
		{
			os.write( (int)'n' );
			os.write( (int)'u' );
			os.write( (int)'l' );
			os.write( (int)'l' );
		}
		else
		{
			final int length = sequence.length();
			for ( int i = 0; i < length; i++ )
			{
				print( os, sequence.charAt( i ) );
			}
		}
	}

	/**
	 * Prints the string representation of the {@link Object} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link java.io.PrintStream}.
	 *
	 * @param os     Stream to print to.
	 * @param object {@link Object} to print.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void print( final OutputStream os, final Object object )
	throws IOException
	{
		print( os, ( object != null ) ? object.toString() : null );
	}

	/**
	 * Prints end-of-line sequence.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link java.io.PrintStream}.
	 *
	 * @param os Stream to print to.
	 *
	 * @throws IOException if there was a problem with writing to the stream.
	 */
	public static void println( final OutputStream os )
	throws IOException
	{
		print( os, TextTools.getLineSeparator() );
	}

	/**
	 * Prints the string representation of the {@code boolean} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os    Stream to print to.
	 * @param value {@code boolean} to print.
	 */
	public static void print( final ByteArrayOutputStream os, final boolean value )
	{
		print( os, Boolean.toString( value ) );
	}

	/**
	 * Prints the string representation of the {@code byte} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param b  {@code byte} to print.
	 */
	public static void print( final ByteArrayOutputStream os, final byte b )
	{
		print( os, Byte.toString( b ) );
	}

	/**
	 * Prints the string representation of the {@code char} argument.
	 *
	 * Note that this method will only print character codes 0 to 255. An I/O
	 * exception is generated when any other character is specified.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os    Stream to print to.
	 * @param value {@code char} to print.
	 */
	public static void print( final ByteArrayOutputStream os, final char value )
	{
		final int code = (int)value;
		if ( code > 0xFF )
		{
			throw new IllegalArgumentException( "illegal char (" + value + ')' );
		}

		os.write( code );
	}

	/**
	 * Prints the string representation of the {@code int} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param i  {@code int} to print.
	 */
	public static void print( final ByteArrayOutputStream os, final int i )
	{
		print( os, Integer.toString( i ) );
	}

	/**
	 * Prints the string representation of the {@code long} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param l  {@code long} to print.
	 */
	public static void print( final ByteArrayOutputStream os, final long l )
	{
		print( os, Long.toString( l ) );
	}

	/**
	 * Prints the string representation of the {@code float} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param f  {@code float} to print.
	 */
	public static void print( final ByteArrayOutputStream os, final float f )
	{
		print( os, Float.toString( f ) );
	}

	/**
	 * Prints the string representation of the {@code double} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os Stream to print to.
	 * @param d  {@code double} to print.
	 */
	public static void print( final ByteArrayOutputStream os, final double d )
	{
		print( os, Double.toString( d ) );
	}

	/**
	 * Prints the contents of the {@code char} array argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link PrintStream}.
	 *
	 * @param os    Stream to print to.
	 * @param chars Character array to print.
	 */
	public static void print( final ByteArrayOutputStream os, final char[] chars )
	{
		if ( chars == null )
		{
			print( os, (CharSequence)null );
		}
		else
		{
			for ( final char value : chars )
			{
				print( os, value );
			}
		}
	}

	/**
	 * Prints the {@link CharSequence} argument.
	 *
	 * Note that this method will only print character codes 0 to 255. An I/O
	 * exception is generated when any other character is specified.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link java.io.PrintStream}.
	 *
	 * @param os       Stream to print to.
	 * @param sequence {@link CharSequence} to print.
	 */
	public static void print( final ByteArrayOutputStream os, final CharSequence sequence )
	{
		if ( sequence == null )
		{
			os.write( (int)'n' );
			os.write( (int)'u' );
			os.write( (int)'l' );
			os.write( (int)'l' );
		}
		else
		{
			final int length = sequence.length();
			for ( int i = 0; i < length; i++ )
			{
				print( os, sequence.charAt( i ) );
			}
		}
	}

	/**
	 * Prints the string representation of the {@link Object} argument.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link java.io.PrintStream}.
	 *
	 * @param os     Stream to print to.
	 * @param object {@link Object} to print.
	 */
	public static void print( final ByteArrayOutputStream os, final Object object )
	{
		print( os, ( object != null ) ? object.toString() : null );
	}

	/**
	 * Prints end-of-line sequence.
	 *
	 * This method can be used to conveniently print values without requiring a
	 * {@link java.io.PrintStream}.
	 *
	 * @param os Stream to print to.
	 */
	public static void println( final ByteArrayOutputStream os )
	{
		print( os, TextTools.getLineSeparator() );
	}

	/**
	 * Writes an ASCII string to the specified output stream. First, the length
	 * is written, then all characters as bytes. If the string is {@code null},
	 * -1 is written as length (readAscii() recognizes this).
	 *
	 * An exception is thrown if the string contains character that require more
	 * than 8 bits using UTF-8 encoding.
	 *
	 * @param os    Output stream.
	 * @param value String to be written.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeAscii( final OutputStream os, final String value )
	throws IOException
	{
		if ( value == null )
		{
			/*
			 * Write -1 for null-string.
			 */
			writeVarInt( os, -1L );
		}
		else
		{
			/*
			 * Write string length followed by characters.
			 */
			final int len = value.length();
			writeVarInt( os, (long)len );

			for ( int i = 0; i < len; i++ )
			{
				int code = (int)value.charAt( i );
				if ( code < 0 )
				{
					code = -code;
				}

				if ( code > 0xFF )
				{
					throw new IOException( "illegal char" );
				}

				os.write( code );
			}
		}
	}

	/**
	 * Read an ASCII string to the underlying input stream. First, the length is
	 * read, then all characters as bytes. This method supports null-strings.
	 *
	 * @param is Input stream.
	 *
	 * @return ASCII string from input stream.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Nullable
	public static String readAscii( final InputStream is )
	throws IOException
	{
		final String result;

		/*
		 * Get string length and character following it. Interpret -1 as {@code null}.
		 */
		long len = readVarInt( is );
		if ( len == -1L )
		{
			result = null;
		}
		else if ( len == 0L )
		{
			result = "";
		}
		else if ( ( len > 0L ) && ( len < (long)Integer.MAX_VALUE ) )
		{
			final StringBuilder sb = new StringBuilder( (int)len );
			for ( ; len > 0L; len-- )
			{
				final int i = is.read();
				if ( i < 0 )
				{
					throw new EOFException();
				}

				sb.append( (char)i );
			}

			result = sb.toString();
		}
		else
		{
			throw new IOException( "got invalid string length (" + len + ')' );
		}

		return result;
	}

	/**
	 * Writes a {@code boolean} to an output stream.
	 *
	 * @param os    Output stream.
	 * @param value Value to write.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeBoolean( final OutputStream os, final boolean value )
	throws IOException
	{
		os.write( value ? 1 : 0 );
	}

	/**
	 * Reads a {@code boolean} from an input stream.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static boolean readBoolean( final InputStream is )
	throws IOException
	{
		final int i = is.read();
		if ( i < 0 )
		{
			throw new EOFException();
		}

		return ( i != 0 );
	}

	/**
	 * Reads a {@code byte} from an input stream.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static byte readByte( final InputStream is )
	throws IOException
	{
		final int i = is.read();
		if ( i < 0 )
		{
			throw new EOFException();
		}

		return (byte)i;
	}

	/**
	 * Read byte array of fixed size from an input stream. Unlike {@link
	 * InputStream#read(byte[])}, this method will continue reading until the
	 * entire byte array is read from the input stream.
	 *
	 * @param is   Input stream to read from.
	 * @param size Size of byte array to read.
	 *
	 * @return Byte array with all data that was read.
	 *
	 * @throws IOException when the reading from the stream fails.
	 */
	public static byte[] readByteArray( final InputStream is, final int size )
	throws IOException
	{
		final byte[] result = new byte[ size ];
		readByteArray( is, result );
		return result;
	}

	/**
	 * Read byte array of fixed size from an input stream. Unlike {@link
	 * InputStream#read(byte[])}, this method will continue reading until the
	 * entire byte array is read from the input stream.
	 *
	 * @param is          Input stream to read from.
	 * @param destination Byte array to store data in.
	 *
	 * @throws IOException when the reading from the stream fails.
	 */
	public static void readByteArray( final InputStream is, final byte[] destination )
	throws IOException
	{
		readByteArray( is, destination, 0, destination.length );
	}

	/**
	 * Read number of bytes into byte array from an input stream. Unlike the
	 * {@link InputStream#read(byte[], int, int)} methodm, this method will
	 * continue reading until the requested number of bytes are read from the
	 * input stream.
	 *
	 * @param is          Input stream to read from.
	 * @param destination Byte array to store data in.
	 * @param offset      Offset in {@code destination}.
	 * @param length      Number of bytes to read.
	 *
	 * @throws IOException when the reading from the stream fails.
	 */
	public static void readByteArray( final InputStream is, final byte[] destination, final int offset, final int length )
	throws IOException
	{
		int bytesRead = 0;
		while ( bytesRead < length )
		{
			final int readNow = is.read( destination, offset + bytesRead, length - bytesRead );
			if ( readNow < 0 )
			{
				throw new EOFException();
			}

			bytesRead += readNow;
		}
	}

	/**
	 * Read entire content from an input stream and store it in a byte array.
	 *
	 * @param is InputStream to read from.
	 *
	 * @return Byte array with all input stream data.
	 *
	 * @throws IOException when the reading from the stream fails.
	 */
	public static byte[] readByteArray( final InputStream is )
	throws IOException
	{
		/*
		 * We use a Fibonacci growing buffer in this algorithm to read as
		 * efficient as possible. We start with a relatively small buffer. After
		 * reading a block of data, we create a new array to fit the data we've
		 * read so far. The old array is then reused on the next iteration.
		 *
		 * NOTICE:
		 *    The algorithm has a deficiency: if we get a reliable available()
		 *    count (actually the correct number of bytes left in the stream), we
		 *    create one unnecessary buffer. We could actually return the buffer
		 *    on the first iteration without ever creating a new one.
		 */
		byte[] total = null;
		byte[] buffer = new byte[ Math.max( is.available(), 1024 ) ]; // initial buffer size

		boolean done = false;
		while ( !done )
		{
			/*
			 * Read data into current buffer until it's full.
			 */
			int totalRead = 0;
			while ( totalRead < buffer.length )
			{
				final int read = is.read( buffer, totalRead, buffer.length - totalRead );
				if ( read == -1 )
				{
					done = true;
					break;
				}
				totalRead += read;
			}

			/*
			 * Create next buffer
			 */
			if ( totalRead > 0 )
			{
				if ( total != null )
				{
					final byte[] newTotal = new byte[ total.length + totalRead ];

					System.arraycopy( total, 0, newTotal, 0, total.length );
					System.arraycopy( buffer, 0, newTotal, total.length, totalRead );

					buffer = total;
					total = newTotal;
				}
				else
				{
					total = new byte[ totalRead ];
					System.arraycopy( buffer, 0, total, 0, totalRead );
				}
			}
		}

		return total == null ? NO_BYTES : total;
	}

	/**
	 * Read entire content from a file and store it in a byte array.
	 *
	 * @param file File to read content from.
	 *
	 * @return Byte array with all file data.
	 *
	 * @throws IOException when the reading from the stream fails.
	 * @throws SecurityException if access is denied (e.g. in applet sandbox).
	 */
	public static byte[] readByteArray( final File file )
	throws IOException
	{
		final FileInputStream is = new FileInputStream( file );
		try
		{
			final int length = (int)file.length();
			final byte[] result = new byte[ length ];

			int todo = length;
			while ( todo > 0 )
			{
				final int read = is.read( result, length - todo, todo );
				if ( read <= 0 )
				{
					throw new EOFException();
				}

				todo -= read;
			}

			return result;
		}
		finally
		{
			is.close();
		}
	}

	/**
	 * Writes an {@code char} to an output stream.
	 *
	 * @param os    Output stream.
	 * @param value Value to write.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeChar( final OutputStream os, final char value )
	throws IOException
	{
		os.write( ( (int)value >>> 8 ) & 0xFF );
		os.write( (int)value & 0xFF );
	}

	/**
	 * Reads an unsigned 16-bit integer from an input stream as {@code char}.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static char readChar( final InputStream is )
	throws IOException
	{
		final int ch1 = is.read();
		final int ch2 = is.read();

		if ( ( ch1 | ch2 ) < 0 )
		{
			throw new EOFException();
		}

		return (char)( ( ch1 << 8 ) + ch2 );
	}

	/**
	 * Writes a {@code double} to an output stream.
	 *
	 * @param os    Output stream.
	 * @param value Value to write.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeDouble( final OutputStream os, final double value )
	throws IOException
	{
		writeLong( os, Double.doubleToLongBits( value ) );
	}

	/**
	 * Reads a {@code double} from an input stream.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static double readDouble( final InputStream is )
	throws IOException
	{
		return Double.longBitsToDouble( readLong( is ) );
	}

	/**
	 * Read an {@link Enum} value from an input stream.
	 *
	 * @param is        Input stream.
	 * @param enumClass Enumeration class to read.
	 *
	 * @return Enumeration value.
	 *
	 * @throws IllegalArgumentException if an unrecognized enumeration constant
	 * name was received.
	 * @throws IOException if an I/O error occurs.
	 * @throws NullPointerException if an argument is {@code null}, or a {@code
	 * null} string was received.
	 */
	public static <T extends Enum<T>> T readEnum( final InputStream is, final Class<T> enumClass )
	throws IOException
	{
		return Enum.valueOf( enumClass, readString( is ) );
	}

	/**
	 * Writes a {@code float} to an output stream.
	 *
	 * @param os    Output stream.
	 * @param value Value to write.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeFloat( final OutputStream os, final float value )
	throws IOException
	{
		writeInt( os, Float.floatToIntBits( value ) );
	}

	/**
	 * Reads a {@code float} from an input stream.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static float readFloat( final InputStream is )
	throws IOException
	{
		return Float.intBitsToFloat( readInt( is ) );
	}

	/**
	 * Reads a {@code float} from an input stream, with little-endian byte
	 * order.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static float readFloatLE( final InputStream is )
	throws IOException
	{
		final int ch1 = is.read();
		final int ch2 = is.read();
		final int ch3 = is.read();
		final int ch4 = is.read();

		if ( ( ch1 | ch2 | ch3 | ch4 ) < 0 )
		{
			throw new EOFException();
		}

		return Float.intBitsToFloat( ( ch4 << 24 ) + ( ch3 << 16 ) + ( ch2 << 8 ) + ch1 );
	}

	/**
	 * Write string to output stream using an indexed string list. This will
	 * potentially reduce the amount of data transferred, by only storing
	 * indices of previously used strings.
	 *
	 * It works as follows: if a string if written, there are three options:
	 *
	 * 1) The string is {@code null}; 2) The string is in the string list; 3)
	 * The string is not yet in the string list.
	 *
	 * For case 1, a single -1 is written.
	 *
	 * For case 2, only the index in the string list is written.
	 *
	 * For case 3, 0 is written, followed by the string itself written using the
	 * writeAscii() method.
	 *
	 * By examining the index received and keeping a private string list, the
	 * reader can re-produce the original strings by a simple look-up or reading
	 * the string from the stream and storing it in the string list for future
	 * reference.
	 *
	 * @param os         Output stream.
	 * @param stringList String list matching indices to strings.
	 * @param value      String to be written.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public static void writeIndexedString( final OutputStream os, final List<String> stringList, final String value )
	throws IOException
	{
		if ( value == null )
		{
			writeVarInt( os, -1L );
		}
		else
		{
			final int i = stringList.indexOf( value );
			writeVarInt( os, (long)( ( i < 0 ) ? stringList.size() : i ) );
			if ( i < 0 )
			{
				writeAscii( os, value );
				stringList.add( value );
			}
		}
	}

	/**
	 * Read string from input stream using an indexed string list. This will
	 * potentially reduce the amount of data transferred, by only using indices
	 * of previously used strings.
	 *
	 * It works as follows: a string is always prefixed by an integer. This
	 * integer can be one of the following:
	 *
	 * 1) less than zero: the string is {@code null}; 2) zero: the string is
	 * {@code null}; 3) greater than zero: the integer is the length of a string
	 * that is not yet in the string list.
	 *
	 * @param is         Input stream.
	 * @param stringList String list matching indices to strings.
	 *
	 * @return String that was retrieved.
	 *
	 * @throws IOException if an I/O error occurred while reading the string.
	 */
	public static String readIndexedString( final InputStream is, final List<String> stringList )
	throws IOException
	{
		final String result;

		final int i = (int)readVarInt( is );
		if ( i < 0 )
		{
			result = null;
		}
		else
		{
			if ( i == stringList.size() )
			{
				final String value = readAscii( is );
				stringList.add( value );
				result = value;
			}
			else
			{
				result = stringList.get( i );
			}
		}

		return result;
	}

	/**
	 * Writes an {@code int} to an output stream.
	 *
	 * @param os    Output stream.
	 * @param value Value to write.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeInt( final OutputStream os, final int value )
	throws IOException
	{
		os.write( ( value >>> 24 ) & 0xFF );
		os.write( ( value >>> 16 ) & 0xFF );
		os.write( ( value >>> 8 ) & 0xFF );
		os.write( value & 0xFF );
	}

	/**
	 * Reads a signed 32-bit integer from an input stream.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static int readInt( final InputStream is )
	throws IOException
	{
		final int ch1 = is.read();
		final int ch2 = is.read();
		final int ch3 = is.read();
		final int ch4 = is.read();

		if ( ( ch1 | ch2 | ch3 | ch4 ) < 0 )
		{
			throw new EOFException();
		}

		return ( ( ch1 << 24 ) + ( ch2 << 16 ) + ( ch3 << 8 ) + ch4 );
	}

	/**
	 * Reads an unsigned 32-bit integer from an input stream, with little-endian
	 * byte order.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static long readUnsignedIntLE( final InputStream is )
	throws IOException
	{
		final int ch1 = is.read();
		final int ch2 = is.read();
		final int ch3 = is.read();
		final int ch4 = is.read();

		if ( ( ch1 | ch2 | ch3 | ch4 ) < 0 )
		{
			throw new EOFException();
		}

		return ( (long)ch4 << 24 ) + ( (long)ch3 << 16 ) + ( (long)ch2 << 8 ) + (long)ch1;
	}

	/**
	 * Writes a {@code long} to an output stream.
	 *
	 * @param os    Output stream.
	 * @param value Value to write.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeLong( final OutputStream os, final long value )
	throws IOException
	{
		writeInt( os, (int)( value >> 32 ) );
		writeInt( os, (int)value );
	}

	/**
	 * Reads a signed 64-bit integer from an input stream.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static long readLong( final InputStream is )
	throws IOException
	{
		return ( (long)( readInt( is ) ) << 32 ) + ( (long)readInt( is ) & 0xFFFFFFFFL );
	}

	/**
	 * Writes an {@code short} to an output stream.
	 *
	 * @param os    Output stream.
	 * @param value Value to write.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeShort( final OutputStream os, final short value )
	throws IOException
	{
		os.write( ( value >>> 8 ) & 0xFF );
		os.write( (int)value & 0xFF );
	}

	/**
	 * Reads a signed 16-bit integer from an input stream.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static short readShort( final InputStream is )
	throws IOException
	{
		final int ch1 = is.read();
		final int ch2 = is.read();

		if ( ( ch1 | ch2 ) < 0 )
		{
			throw new EOFException();
		}

		return (short)( ( ch1 << 8 ) + ch2 );
	}

	/**
	 * Reads an unsigned 16-bit integer from an input stream, with little-endian
	 * byte order.
	 *
	 * @param is Input Stream
	 *
	 * @return Value that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static int readUnsignedShortLE( final InputStream is )
	throws IOException
	{
		final int ch1 = is.read();
		final int ch2 = is.read();

		if ( ( ch1 | ch2 ) < 0 )
		{
			throw new EOFException();
		}

		return ( ch2 << 8 ) + ch1;
	}

	/**
	 * Writes an STRING string to the specified output stream. First, the length
	 * is written, then all characters as bytes. If the string is {@code null},
	 * -1 is written as length (readString() recognizes this).
	 *
	 * An exception is thrown if the string contains character that require more
	 * than 8 bits using UTF-8 encoding.
	 *
	 * @param os    Output stream.
	 * @param value String to be written.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeString( final OutputStream os, final CharSequence value )
	throws IOException
	{
		if ( value == null )
		{
			writeInt( os, -1 );
		}
		else
		{
			final int len = value.length();
			writeInt( os, len );

			for ( int i = 0; i < len; i++ )
			{
				writeChar( os, value.charAt( i ) );
			}
		}
	}

	/**
	 * Read an STRING string to the underlying input stream. First, the length
	 * is read, then all characters as bytes. This method supports
	 * null-strings.
	 *
	 * @param is Input stream.
	 *
	 * @return STRING string from input stream.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Nullable
	public static String readString( final InputStream is )
	throws IOException
	{
		final String result;

		/*
		 * Get string length and character following it. Interpret -1 as {@code null}.
		 */
		int len = readInt( is );
		if ( len == -1 )
		{
			result = null;
		}
		else if ( len == 0 )
		{
			result = "";
		}
		else if ( len > 0 )
		{
			final StringBuilder sb = new StringBuilder( len );
			for ( ; len > 0; len-- )
			{
				sb.append( readChar( is ) );
			}

			result = sb.toString();
		}
		else
		{
			throw new IOException( "got invalid string length (" + len + ')' );
		}

		return result;
	}

	/**
	 * Writes a string to the specified output stream using UTF-8 encoding.
	 *
	 * First, the total number of bytes written as short, followed by the UTF-8
	 * encoded characters in the string.
	 *
	 * @param os    Output stream.
	 * @param value String to be written.
	 *
	 * @return Number of bytes written.
	 *
	 * @throws IOException if an I/O error occurs.
	 * @see java.io.DataOutputStream#writeUTF
	 */
	@SuppressWarnings( "StandardVariableNames" )
	public static int writeUTF( final OutputStream os, final CharSequence value )
	throws IOException
	{
		final int strlen = value.length();

		int utflen = 0;
		for ( int i = 0; i < strlen; i++ )
		{
			final int ch = (int)value.charAt( i );
			utflen += ( ( ch > 0 ) && ( ch < 0x80 ) ) ? 1 : ( ch < 0x800 ) ? 2 : 3;
		}

		final byte[] buffer = new byte[ utflen + 2 ];
		buffer[ 0 ] = (byte)( ( utflen >>> 8 ) & 0xFF );
		buffer[ 1 ] = (byte)( utflen & 0xFF );
		int count = 2;

		for ( int i = 0; i < strlen; i++ )
		{
			final int ch = (int)value.charAt( i );
			if ( ( ch > 0 ) && ( ch < 0x80 ) )
			{
				buffer[ count++ ] = (byte)ch;
			}
			else if ( ch < 0x800 )
			{
				buffer[ count++ ] = (byte)( 0xC0 | ( ( ch >> 6 ) & 0x1F ) );
				buffer[ count++ ] = (byte)( 0x80 | ( ch & 0x3F ) );
			}
			else
			{
				buffer[ count++ ] = (byte)( 0xE0 | ( ( ch >> 12 ) & 0x0F ) );
				buffer[ count++ ] = (byte)( 0x80 | ( ( ch >> 6 ) & 0x3F ) );
				buffer[ count++ ] = (byte)( 0x80 | ( ch & 0x3F ) );
			}
		}

		os.write( buffer, 0, utflen + 2 );
		return utflen + 2;
	}

	/**
	 * Reads from the stream {@code in} a representation of a Unicode character
	 * string encoded in <a href="DataInput.html#modified-utf-8">modified
	 * UTF-8</a> format; this string of characters is then returned as a {@code
	 * String}. The details of the modified UTF-8 representation are exactly the
	 * same as for the {@code readUTF} method of {@code DataInput}.
	 *
	 * @param in Stream to read UTF string from.
	 *
	 * @return String that was read.
	 *
	 * @throws IOException if an I/O error occurs.
	 * @see DataInputStream#readUTF
	 */
	public static String readUTF( final InputStream in )
	throws IOException
	{
		final int utflen = (int)readShort( in );
		int position = 2;

		final StringBuilder sb = new StringBuilder( utflen );

		while ( position < utflen )
		{
			final int b1 = (int)readByte( in ) & 0xFF;
			if ( b1 < 0x80 )
			{
				position++;
				sb.append( (char)b1 );
			}
			else if ( ( b1 >= 0xC0 ) && ( b1 < 0xE0 ) ) /* 2-byte */
			{
				position++;

				final int b2 = (int)readByte( in ) & 0xFF;
				if ( ( b2 & 0xC0 ) != 0x80 )
				{
					throw new UTFDataFormatException( "malformed input 0x" + Integer.toHexString( b2 ) + " at position " + position );
				}
				position++;

				sb.append( (char)( ( ( b1 & 0x1F ) << 6 ) | ( b2 & 0x3F ) ) );
			}
			else if ( ( b1 >= 0xE0 ) && ( b1 < 0xF0 ) ) /* 3-byte */
			{
				position++;

				final int b2 = (int)readByte( in ) & 0xFF;
				if ( ( b2 & 0xC0 ) != 0x80 )
				{
					throw new UTFDataFormatException( "malformed input 0x" + Integer.toHexString( b2 ) + " at position " + position );
				}
				position++;

				final int b3 = (int)readByte( in ) & 0xFF;
				if ( ( b3 & 0xC0 ) != 0x80 )
				{
					throw new UTFDataFormatException( "malformed input 0x" + Integer.toHexString( b3 ) + " at position " + position );
				}
				position++;

				sb.append( (char)( ( ( b1 & 0x0F ) << 12 ) | ( ( b2 & 0x3F ) << 6 ) | ( b3 & 0x3F ) ) );
			}
			else
			{
				throw new UTFDataFormatException( "malformed input 0x" + Integer.toHexString( b1 ) + " at position " + position );
			}
		}

		return sb.toString();
	}

	/**
	 * Write variable length integer value. This format is used to reduces the
	 * amount of data written.
	 *
	 * @param os    Stream to write to.
	 * @param value value to write.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public static void writeVarInt( final OutputStream os, final long value )
	throws IOException
	{
		if ( value >= -0x20L && value < 0x20L )
		{
			os.write( ( (int)value ) << 2 );
		}
		else if ( value >= -0x2000L && value < 0x2000L )
		{
			final int data = ( ( (int)value ) << 2 ) | 1;

			os.write( data & 0xFF );
			os.write( ( data >>> 8 ) & 0xFF );
		}
		else if ( value >= -0x20000000L && value < 0x20000000L )
		{
			final int data = ( ( (int)value ) << 2 ) | 2;

			os.write( data & 0xFF );
			os.write( ( data >>> 8 ) & 0xFF );
			os.write( ( data >>> 16 ) & 0xFF );
			os.write( ( data >>> 24 ) & 0xFF );
		}
		else if ( value >= -0x2000000000000000L && value < 0x2000000000000000L )
		{
			final long data = ( value << 2 ) | 3L;

			os.write( (int)data & 0xFF );
			os.write( (int)( data >>> 8 ) & 0xFF );
			os.write( (int)( data >>> 16 ) & 0xFF );
			os.write( (int)( data >>> 24 ) & 0xFF );
			os.write( (int)( data >>> 32 ) & 0xFF );
			os.write( (int)( data >>> 40 ) & 0xFF );
			os.write( (int)( data >>> 48 ) & 0xFF );
			os.write( (int)( data >>> 56 ) & 0xFF );
		}
		else
		{
			throw new IOException( "Value out of range for writeVar()!" );
		}
	}

	/**
	 * Read variable length integer value. This format is used to reduce the
	 * amount of data written for integer values.
	 *
	 * @param is Stream to read from.
	 *
	 * @return Integer value that was read.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	public static long readVarInt( final InputStream is )
	throws IOException
	{
		final int i1 = is.read();
		if ( i1 < 0 )
		{
			throw new EOFException();
		}

		final int size = i1 & 3;

		final long result;
		if ( size == 0 )
		{
			result = (long)( ( (byte)( i1 ) ) >> 2 );
		}
		else if ( size == 1 )
		{
			final int i2 = is.read();
			if ( i2 < 0 )
			{
				throw new EOFException();
			}

			result = (long)( ( (short)( ( i2 << 8 ) + i1 ) ) >> 2 );
		}
		else if ( size == 2 )
		{
			final int i2 = is.read();
			final int i3 = is.read();
			final int i4 = is.read();

			if ( ( i2 < 0 ) || ( i3 < 0 ) || ( i4 < 0 ) )
			{
				throw new EOFException();
			}

			result = (long)( ( ( i4 << 24 ) + ( i3 << 16 ) + ( i2 << 8 ) + i1 ) >> 2 );
		}
		else // size == 3
		{
			final long l1 = (long)i1;
			final long l2 = (long)is.read();
			final long l3 = (long)is.read();
			final long l4 = (long)is.read();
			final long l5 = (long)is.read();
			final long l6 = (long)is.read();
			final long l7 = (long)is.read();
			final long l8 = (long)is.read();

			if ( ( l2 < 0L ) || ( l3 < 0L ) || ( l4 < 0L ) || ( l5 < 0L ) || ( l6 < 0L ) || ( l7 < 0L ) || ( l8 < 0L ) )
			{
				throw new EOFException();
			}

			result = ( ( l8 << 56 ) + ( l7 << 48 ) + ( l6 << 40 ) + ( l5 << 32 ) + ( l4 << 24 ) + ( l3 << 16 ) + ( l2 << 8 ) + l1 ) >> 2;
		}

		return result;
	}

	/**
	 * Pipe from one stream to another stream.
	 *
	 * @param out Stream to write to.
	 * @param in  Stream to read to.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void pipe( final OutputStream out, final InputStream in )
	throws IOException
	{
		final byte[] buffer = new byte[ Math.max( 0x400, Math.min( in.available(), 0x10000 ) ) ]; // initial buffer size

		while ( true )
		{
			final int read = in.read( buffer );
			if ( read == -1 )
			{
				break;
			}

			if ( read > 0 )
			{
				out.write( buffer, 0, read );
			}
		}
	}

	/**
	 * Pipe from one stream to another stream.
	 *
	 * @param out Stream to write to.
	 * @param in  Stream to read to.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void pipe( final Appendable out, final Reader in )
	throws IOException
	{
		final char[] charArray = new char[ 0x400 ];
		final CharSequence charBuffer = new CharArray( charArray );

		while ( true )
		{
			final int read = in.read( charArray );
			if ( read == -1 )
			{
				break;
			}

			if ( read > 0 )
			{
				out.append( charBuffer, 0, read );
			}
		}
	}

	/**
	 * Validate whether the given file is a valid ZIP file.
	 *
	 * @param file File to validate.
	 *
	 * @return {@code true} if file is a valid ZIP file.
	 */
	public static boolean isValidZipFile( final File file )
	{
		boolean result;

		try
		{
			final FileInputStream fis = new FileInputStream( file );
			try
			{
				result = isValidZipFile( fis );
			}
			finally
			{
				fis.close();
			}
		}
		catch ( IOException ignored )
		{
			result = false;
		}

		return result;
	}

	/**
	 * Validate whether the given stream contains a valid ZIP file.
	 *
	 * @param in Stream containing ZIP file to validate.
	 *
	 * @return {@code true} if the stream contains a valid ZIP file.
	 */
	public static boolean isValidZipFile( final InputStream in )
	{
		boolean result;

		try
		{
			final ZipInputStream zis = new ZipInputStream( in );
			for ( ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry() )
			{
				entry.getCrc();
				entry.getCompressedSize();
				entry.getName();
			}

			result = true;
		}
		catch ( IOException ignored )
		{
			result = false;
		}

		return result;
	}
}
