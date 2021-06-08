/*
 * Copyright (c) 2021-2021, Unicon Creation BV, The Netherlands.
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
import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link CSVParser}.
 *
 * @author Peter S. Heijnen
 */
public class TestCSVParser
{
	@Test
	public void testOptions()
	{
		final CSVParser csvParser = new CSVParser();
		assertEquals( "Unexpected 'separator' value", ',', csvParser.getSeparator() );
		assertFalse( "Unexpected 'skipComments' value", csvParser.isSkipComments() );
		assertFalse( "Unexpected 'skipEmptyRows' value", csvParser.isSkipEmptyRows() );
		assertFalse( "Unexpected 'emptyNull' value", csvParser.isEmptyNull() );
		csvParser.setSeparator( ';' );
		csvParser.setSkipComments( true );
		csvParser.setSkipEmptyRows( true );
		csvParser.setEmptyNull( true );
		assertEquals( "Unexpected 'separator' value", ';', csvParser.getSeparator() );
		assertTrue( "Unexpected 'skipComments' value", csvParser.isSkipComments() );
		assertTrue( "Unexpected 'skipEmptyRows' value", csvParser.isSkipEmptyRows() );
		assertTrue( "Unexpected 'emptyNull' value", csvParser.isEmptyNull() );
	}

	@Test
	public void testReadAll()
	throws Exception
	{
		final CSVParser csvParser = new CSVParser();
		final List<List<String>> expected = Arrays.asList( Arrays.asList( "a", "b" ), Arrays.asList( "1", "2", "3" ) );
		final List<List<String>> actual = csvParser.readAll( new StringReader( "\n\na,b\n1,2,3\n\n" ) );
		assertEquals( "Unexpected output", expected, actual );
	}

	@Test
	public void testReadRow()
	throws Exception
	{
		final CSVParser csvParser = new CSVParser();
		final List<String> expected = Arrays.asList( "a", "b" );
		final List<String> actual = csvParser.readRow( new BufferedReader( new StringReader( "\n\na,b\n1,2,3\n\n" ) ) );
		assertEquals( "Unexpected output", expected, actual );
	}

	@Test
	public void testParseLines()
	{
		final CSVParser csvParser = new CSVParser();
		csvParser.setSkipComments( true );
		csvParser.setSkipEmptyRows( true );
		csvParser.setEmptyNull( true );

		final List<String> input = Arrays.asList( "#",
		                                          "    # hello",
		                                          "1,2,\"3\"",
		                                          "\" \", \"Hi\"\"\",\"Hi\"\"there\",\",\" ",
		                                          "\t ",
		                                          "1,2,,4",
		                                          "  1 ,  2 , \"3\" , 4  " );
		final List<List<String>> expected = Arrays.asList( Arrays.asList( "1", "2", "3" ),
		                                                   Arrays.asList( " ", "Hi\"", "Hi\"there", "," ),
		                                                   Arrays.asList( "1", "2", null, "4" ),
		                                                   Arrays.asList( "1", "2", "3", "4" ) );
		final List<List<String>> actual = csvParser.parseLines( input );
		assertEquals( "Unexpected result", expected, actual );
	}
}
