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
 * Unit test for {@link CRC16}.
 *
 * @author Gerrit Meinders
 * @see <a href="http://web.archive.org/web/20130528094312/http://www.zorc.breitbandkatze.de/crc.html">Generic
 * CRC calculator</a> (used to calculate reference values)
 */
public class TestCRC16
{
	/**
	 * Tests the checksum using some examples found online as a reference.
	 */
	@Test
	public void test()
	{
		assertEquals( "Unexpected value.", 14785, getValue( "hello world" ) );
		assertEquals( "Unexpected value.", 22462, getValue( "Hello World!" ) );
	}

	private long getValue( final String s )
	{
		try
		{
			final byte[] bytes = s.getBytes( "US-ASCII" );
			for ( final byte aByte : bytes )
			{
				System.out.print( Integer.toHexString( aByte & 0xff ) );
			}
			System.out.println();
			final CRC16 checksum = new CRC16();
			checksum.update( bytes, 0, bytes.length );
			return checksum.getValue();
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new AssertionError( e );
		}
	}
}
