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
import javax.xml.stream.*;
import javax.xml.stream.events.*;

import static org.junit.Assert.*;

/**
 * Tools for testing XML-related code.
 *
 * @author G. Meinders
 */
public class XMLTestTools
{
	/**
	 * Asserts that the given stream contain an equivalent XML document.
	 *
	 * @param expectedIn Expected XML document.
	 * @param actualIn   Actual XML document.
	 *
	 * @throws XMLStreamException if there is an error with the underlying XML.
	 */
	public static void assertXMLEquals( final InputStream expectedIn, final InputStream actualIn )
	throws XMLStreamException
	{
		final XMLInputFactory xmlInputFactory = XMLInputFactory.newFactory();

		final XMLEventReader actualReader = xmlInputFactory.createXMLEventReader( actualIn );
		final XMLEventReader expectedReader = xmlInputFactory.createXMLEventReader( expectedIn );

		while ( expectedReader.hasNext() && actualReader.hasNext() )
		{
			final XMLEvent expectedEvent = expectedReader.nextEvent();
			final XMLEvent actualEvent = actualReader.nextEvent();

			final StringWriter expectedWriter = new StringWriter();
			expectedEvent.writeAsEncodedUnicode( expectedWriter );
			final String expected = expectedWriter.toString();

			final StringWriter actualWriter = new StringWriter();
			actualEvent.writeAsEncodedUnicode( actualWriter );
			final String actual = actualWriter.toString();

			assertEquals( "Unexpected event.", expected, actual );
			assertEquals( "Unexpected event type.", expectedEvent.getEventType(), actualEvent.getEventType() );
		}

		assertFalse( "Expected more elements.", expectedReader.hasNext() );
		assertFalse( "Expected no more elements.", actualReader.hasNext() );
	}

	/**
	 * Utility class should not be instantiated.
	 */
	private XMLTestTools()
	{
	}
}
