/*
 * Copyright (c) 2009-2017, Numdata BV, The Netherlands.
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

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * A variant of {@link DataInputStream} for reading little-endian data.
 *
 * @author  Peter S. Heijnen
 */
public class LittleEndianDataInputStream
	extends FilterInputStream
	implements DataInput
{
	/**
	 * Creates data input from input stream.
	 *
	 * @param in The input stream to read from.
	 */
	public LittleEndianDataInputStream( final InputStream in )
	{
		super( in );
	}

	public final boolean readBoolean()
		throws IOException
	{
		final int b = in.read();
		if ( b < 0 )
			throw new EOFException();

		return ( b != 0 );
	}

	public final byte readByte()
		throws IOException
	{
		final int b = in.read();
		if ( b < 0 )
			throw new EOFException();

		return (byte)b;
	}

	public final char readChar()
		throws IOException
	{
		return (char)readUnsignedShort();
	}

	public final double readDouble()
		throws IOException
	{
		return Double.longBitsToDouble( readLong() );
	}

	public final float readFloat()
		throws IOException
	{
		return Float.intBitsToFloat( readInt() );
	}

	public final void readFully( final byte[] dest )
		throws IOException
	{
		readFully( dest , 0 , dest.length );
	}

	public final void readFully( final byte[] dest , final int offset , final int length )
		throws IOException
	{
		int currentOffset = offset;
		int todo = length;

		while ( todo > 0 )
		{
			final int read = in.read( dest , currentOffset , todo );
			if ( read <= 0 )
				throw new EOFException();

			currentOffset += read;
			todo -= read;
		}
	}

	public final int readInt()
		throws IOException
	{
		final int s0 = readUnsignedShort();
		final int s1 = readUnsignedShort();
		return s0 | ( s1 << 16 );
	}

	public final String readLine()
		throws IOException
	{
		final StringBuilder sb = new StringBuilder();
		final InputStream in = this.in;

		int i;
		while ( true )
		{
			i = in.read();
			if ( ( i < 0 ) || ( i == (int)'\n' ) )
				break;

			if ( i == (int)'\r' )
			{
				final int c2 = in.read();
				if ( ( c2 != (int)'\n' ) && ( c2 >= 0 ) )
				{
					final PushbackInputStream pis;
					if ( in instanceof PushbackInputStream )
					{
						pis = (PushbackInputStream)in;
					}
					else
					{
						pis = new PushbackInputStream( in );
						this.in = pis;
					}
					pis.unread( c2 );
				}
				break;
			}

			sb.append( (char)i );
		}

		return ( ( i >= 0 ) || ( sb.length() > 0 ) ) ? sb.toString() : null;
	}

	public final long readLong()
		throws IOException
	{
		final int s0 = readUnsignedShort();
		final int s1 = readUnsignedShort();
		final int s2 = readUnsignedShort();
		final int s3 = readUnsignedShort();
		return (long)s0 | ( (long)s1 << 16 ) | ( (long)s2 << 24 ) | ( (long)s3 << 32 );
	}

	public final short readShort()
		throws IOException
	{
		return (short)readUnsignedShort();
	}

	public final int readUnsignedByte()
		throws IOException
	{
		final int b = in.read();
		if ( b < 0 )
			throw new EOFException();

		return b;
	}

	public final int readUnsignedShort()
		throws IOException
	{
		final int b0 = readUnsignedByte();
		final int b1 = readUnsignedByte();
		return b0 | ( b1 << 8 );
	}

	public final String readUTF()
		throws IOException
	{
		return DataInputStream.readUTF( this );
	}

	public final int skipBytes( final int length )
		throws IOException
	{
		return (int)skip( (long)length );
	}
}
