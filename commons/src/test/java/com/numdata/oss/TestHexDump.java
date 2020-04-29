/*
 * Copyright (c) 2020, Numdata BV, The Netherlands.
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

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link HexDump}.
 *
 * @author Gerrit Meinders
 */
public class TestHexDump
{
	/**
	 * Tests {@link HexDump#write}.
	 *
	 * @throws IOException if the test fails.
	 */
	@Test
	public void testHexDump()
	throws IOException
	{
		final StringBuilder sb = new StringBuilder();
		HexDump.write( sb, "Hello world!\n".getBytes() );
		assertEquals( "Unexpected output.", "00000: 48 65 6C 6C 6F 20 77 6F 72 6C 64 21 0A           |Hello world!.   |\n", sb.toString() );
		sb.setLength( 0 );
		HexDump.write( sb, "##Hello world!\n##".getBytes(), 2, 13 );
		assertEquals( "Unexpected output.", "00000: 48 65 6C 6C 6F 20 77 6F 72 6C 64 21 0A           |Hello world!.   |\n", sb.toString() );
		sb.setLength( 0 );
		HexDump.write( sb, "##Hello hexdump(..) world!\n##".getBytes(), 2, 25 );
		assertEquals( "Unexpected output.", "00000: 48 65 6C 6C 6F 20 68 65 78 64 75 6D 70 28 2E 2E  |Hello hexdump(..|\n" +
		                                    "00010: 29 20 77 6F 72 6C 64 21 0A                       |) world!.       |\n", sb.toString() );
	}
}