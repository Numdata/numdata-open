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
package com.numdata.oss.io;

import java.io.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Test for {@link DataStreamTools} class.
 *
 * @author Peter S. Heijnen
 */
public class TestDataStreamTools
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestDataStreamTools.class.getName();

	/**
	 * Test {@link DataStreamTools#print(OutputStream, boolean)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testPrintBoolean()
	throws Exception
	{
		final OutputStream os = new ByteArrayOutputStream();

		DataStreamTools.print( os, true );
		DataStreamTools.print( os, false );
	}

	/**
	 * Test {@link DataStreamTools#print(OutputStream, char)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testPrintChar()
	throws Exception
	{
		final OutputStream os = new ByteArrayOutputStream();

		DataStreamTools.print( os, '\0' );
		DataStreamTools.print( os, 'a' );
	}

	/**
	 * Test {@link DataStreamTools#print(OutputStream, char[])} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testPrintCharArray()
	throws Exception
	{
		final OutputStream os = new ByteArrayOutputStream();

		DataStreamTools.print( os, new char[] { 'a' } );
	}

	/**
	 * Test {@link DataStreamTools#print(OutputStream, double)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testPrintDouble()
	throws Exception
	{
		final OutputStream os = new ByteArrayOutputStream();

		DataStreamTools.print( os, 0.0 );
	}

	/**
	 * Test {@link DataStreamTools#print(OutputStream, float)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testPrintFloat()
	throws Exception
	{
		final OutputStream os = new ByteArrayOutputStream();

		DataStreamTools.print( os, 0.0f );
		DataStreamTools.print( os, 0.5f );
		DataStreamTools.print( os, 1.0f );
	}

	/**
	 * Test {@link DataStreamTools#print(OutputStream, long)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testPrintLong()
	throws Exception
	{
		final OutputStream os = new ByteArrayOutputStream();

		DataStreamTools.print( os, 0L );
	}

	/**
	 * Test {@link DataStreamTools#print(OutputStream, Object)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testPrintObject()
	throws Exception
	{
		final OutputStream os = new ByteArrayOutputStream();

		final Object testObject = new Object()
		{
			public String toString()
			{
				return "test";
			}
		};

		DataStreamTools.print( os, testObject );
	}

	/**
	 * Test {@link DataStreamTools#print(OutputStream, CharSequence)} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testPrintString()
	throws Exception
	{
		final OutputStream os = new ByteArrayOutputStream();

		DataStreamTools.print( os, "hello world" );
	}

	/**
	 * Test {@link DataStreamTools#readAscii}  / {@link DataStreamTools#writeAscii}
	 * methods.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadWriteAscii()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testReadWriteAscii" );

		final String[] tests =
		{
		null,
		"",
		"a",
		"\0b"
		};

		for ( int i = 0; i < tests.length; i++ )
		{
			final String test = tests[ i ];

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			DataStreamTools.writeAscii( os, test );

			final byte[] bytes = os.toByteArray();

			final ByteArrayInputStream is = new ByteArrayInputStream( bytes );
			final String read = DataStreamTools.readAscii( is );

			assertEquals( "read/writeAscii test #" + i + " mismatch", test, read );
			assertEquals( "should be at end of stream after read/writeAscii test #" + i, -1, is.read() );
		}
	}

	/**
	 * Test {@link DataStreamTools#readBoolean}  / {@link
	 * DataStreamTools#writeBoolean} methods.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadWriteBoolean()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testReadWriteBoolean" );

		final boolean[] tests =
		{
		true,
		false,
		};

		for ( int i = 0; i < tests.length; i++ )
		{
			final boolean test = tests[ i ];

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			DataStreamTools.writeBoolean( os, test );

			final byte[] bytes = os.toByteArray();

			final ByteArrayInputStream is = new ByteArrayInputStream( bytes );
			final boolean read = DataStreamTools.readBoolean( is );

			assertEquals( "read/writeBoolean test #" + i + " mismatch", test, read );
			assertEquals( "should be at end of stream after read/writeBoolean test #" + i, -1, is.read() );
		}
	}

	/**
	 * Test {@link DataStreamTools#readByte} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadWriteByte()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testReadWriteByte" );

		for ( int test = -128; test < 127; test++ )
		{
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			os.write( test );

			final byte[] bytes = os.toByteArray();

			final ByteArrayInputStream is = new ByteArrayInputStream( bytes );
			final int read = DataStreamTools.readByte( is );

			assertEquals( "read/writeByte test #" + test + " mismatch", test, read );
			assertEquals( "should be at end of stream after read/writeByte test #" + test, -1, is.read() );
		}
	}

	/**
	 * Test {@link DataStreamTools#readDouble} / {@link DataStreamTools#writeDouble}
	 * method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadWriteDouble()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testReadWriteDouble" );

		final double[] tests =
		{
		0,
		-1,
		1,
		-0.0000125,
		0.0000125,
		Double.NaN,
		Double.MIN_VALUE,
		Double.MAX_VALUE,
		Double.POSITIVE_INFINITY,
		Double.NEGATIVE_INFINITY,
		};

		for ( int i = 0; i < tests.length; i++ )
		{
			final double test = tests[ i ];

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			DataStreamTools.writeDouble( os, test );

			final byte[] bytes = os.toByteArray();

			final ByteArrayInputStream is = new ByteArrayInputStream( bytes );
			final double read = DataStreamTools.readDouble( is );

			if ( Double.isNaN( test ) )
			{
				assertTrue( "read/writeDouble test #" + i + " mismatch", Double.isNaN( read ) );
			}
			else
			{
				assertEquals( "read/writeDouble test #" + i + " mismatch", test, read, 0 );
			}
			assertEquals( "should be at end of stream after read/writeDouble test #" + i, -1, is.read() );
		}
	}

	/**
	 * Test {@link DataStreamTools#readFloat} / {@link DataStreamTools#writeFloat}
	 * method..
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadWriteFloat()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testReadWriteFloat" );

		final float[] tests =
		{
		0,
		-1,
		1,
		Float.NaN,
		Float.MIN_VALUE,
		Float.MAX_VALUE,
		Float.POSITIVE_INFINITY,
		Float.NEGATIVE_INFINITY,
		};

		for ( int i = 0; i < tests.length; i++ )
		{
			final float test = tests[ i ];

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			DataStreamTools.writeFloat( os, test );

			final byte[] bytes = os.toByteArray();

			final ByteArrayInputStream is = new ByteArrayInputStream( bytes );
			final float read = DataStreamTools.readFloat( is );

			if ( Float.isNaN( test ) )
			{
				assertTrue( "read/writeDouble test #" + i + " mismatch", Float.isNaN( read ) );
			}
			else
			{
				assertEquals( "read/writeFloat test #" + i + " mismatch", test, read, 0.0f );
			}

			assertEquals( "should be at end of stream after read/writeFloat test #" + i, -1, is.read() );
		}
	}

	/**
	 * Test {@link DataStreamTools#readInt} / {@link DataStreamTools#writeInt}
	 * methods.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadWriteInt()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testReadWriteInt" );

		final int[] tests =
		{
		0,
		-1,
		1,
		Integer.MIN_VALUE,
		Integer.MAX_VALUE,
		};

		for ( int i = 0; i < tests.length; i++ )
		{
			final int test = tests[ i ];

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			DataStreamTools.writeInt( os, test );

			final byte[] bytes = os.toByteArray();

			final ByteArrayInputStream is = new ByteArrayInputStream( bytes );
			final int read = DataStreamTools.readInt( is );

			assertEquals( "read/writeInt test #" + i + " mismatch", test, read );
			assertEquals( "should be at end of stream after read/writeInt test #" + i, -1, is.read() );
		}
	}

	/**
	 * Test {@link DataStreamTools#readLong} / {@link DataStreamTools#writeLong}
	 * methods.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadWriteLong()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testReadWriteLong" );

		final long[] tests =
		{
		0,
		-1,
		1,
		Long.MIN_VALUE,
		Long.MAX_VALUE,
		};

		for ( int i = 0; i < tests.length; i++ )
		{
			final long test = tests[ i ];

			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			DataStreamTools.writeLong( os, test );

			final byte[] bytes = os.toByteArray();

			final ByteArrayInputStream is = new ByteArrayInputStream( bytes );
			final long read = DataStreamTools.readLong( is );

			assertEquals( "read/writeLong test #" + i + " mismatch", test, read );
			assertEquals( "should be at end of stream after read/writeLong test #" + i, -1, is.read() );
		}
	}
}
