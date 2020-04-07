/*
 * Copyright (c) 2003-2020, Numdata BV, The Netherlands.
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
package com.numdata.oss;

import java.io.*;

import org.jetbrains.annotations.*;

/**
 * This class provides functionality to create a "hex dump" in plain text.
 *
 * <p>Typical output would be:
 * <pre>
 * 00000: 04 d2 29 00 00 01 00 00 00 00 00 01 20 45 47 |..)......... EG|
 * 00010: 43 45 46 45 45 43 41 43 41 43 41 43 41 43 41 |CEFEECACACACACA|
 * 00020: 41 43 41 43 41 43 41 43 41 43 41 41 44 00 00 |ACACACACACAAD..|
 * 00030: 00 01 c0 0c 00 20 00 01 00 00 00 00 00 06 20 |..... ........ |
 * 00040: ac 22 22 e1                                  |."".           |
 * </pre>
 *
 * This is especially suitable for console applications and tests.
 *
 * @author Peter S. Heijnen
 */
public class HexDump
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private HexDump()
	{
	}

	/**
	 * Writes a "hex dump" to the given target.
	 *
	 * @param out Target character stream.
	 * @param src Data to dump.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void write( @NotNull final Appendable out, @NotNull final byte[] src )
	throws IOException
	{
		write( out, src, 0, src.length );
	}

	/**
	 * Writes a "hex dump" to the given target.
	 *
	 * @param out      Target character stream.
	 * @param src      Data to dump.
	 * @param srcIndex Index of first byte to dump.
	 * @param length   Number of bytes to dump.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void write( @NotNull final Appendable out, @NotNull final byte[] src, final int srcIndex, final int length )
	throws IOException
	{
		for ( int pointer = 0; pointer < length; pointer += 16 )
		{
			TextTools.appendHexString( out, pointer, 5, true );
			out.append( ": " );

			final int rowLength = Math.min( 16, length - pointer );

			for ( int i = 0; i < 16; i++ )
			{
				if ( i < rowLength )
				{
					TextTools.appendHexString( out, src[ srcIndex + pointer + i ], 2, true );
					out.append( ' ' );
				}
				else
				{
					out.append( "   " );
				}
			}

			out.append( " |" );

			for ( int i = 0; i < 16; i++ )
			{
				if ( i < rowLength )
				{
					final byte b = src[ srcIndex + pointer + i ];
					out.append( ( b < 31 ) ? '.' : (char)b );
				}
				else
				{
					out.append( ' ' );
				}
			}

			out.append( "|\n" );
		}
	}
}
