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
package com.numdata.jnlp;

import java.io.*;
import javax.xml.stream.*;

import com.numdata.oss.io.*;
import org.junit.*;

/**
 * Unit test for {@link JnlpVersionFile}.
 *
 * @author G. Meinders
 */
public class TestJnlpVersionFile
{
	/**
	 * Test file for {@link #testReadWrite()}.
	 */
	private static final String TEST_READ_WRITE;

	static
	{
		try
		{
			final XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newFactory();
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			final XMLStreamWriter writer = new IndentingXMLStreamWriter( xmlOutputFactory.createXMLStreamWriter( baos, "UTF-8" ) );
			writer.writeStartDocument();
			writer.writeStartElement( "jnlp-versions" );
			writer.writeStartElement( "resource" );
			writer.writeStartElement( "pattern" );
			writer.writeStartElement( "name" );
			writer.writeCharacters( "example.jar" );
			writer.writeEndElement(); // </name" );
			writer.writeStartElement( "version-id" );
			writer.writeCharacters( "1.2.3" );
			writer.writeEndElement(); // </version-id" );
			writer.writeEndElement(); // </pattern>
			writer.writeStartElement( "file" );
			writer.writeCharacters( "example-1.2.3.jar" );
			writer.writeEndElement(); // </file" );
			writer.writeEndElement(); // </resource>
			writer.writeStartElement( "resource" );
			writer.writeStartElement( "pattern" );
			writer.writeStartElement( "name" );
			writer.writeCharacters( "another.jar" );
			writer.writeEndElement(); // </name" );
			writer.writeStartElement( "version-id" );
			writer.writeCharacters( "2.3" );
			writer.writeEndElement(); // </version-id" );
			writer.writeEndElement(); // </pattern>
			writer.writeStartElement( "file" );
			writer.writeCharacters( "another-2.3.jar" );
			writer.writeEndElement(); // </file" );
			writer.writeEndElement(); // </resource>
			writer.writeEndElement(); // </jnlp-versions>
			writer.writeEndDocument();
			writer.flush();

			TEST_READ_WRITE = new String( baos.toByteArray(), "UTF-8" );
		}
		catch ( XMLStreamException e )
		{
			throw new AssertionError( e );
		}
		catch ( UnsupportedEncodingException e )
		{
			throw new AssertionError( e );
		}
	}

	/**
	 * Tests that the sample data ({@link #TEST_READ_WRITE}) can be read and
	 * then written, resulting in a file that exactly matches the input.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testReadWrite()
	throws Exception
	{
		final JnlpVersionFile file = new JnlpVersionFile();
		file.read( new ByteArrayInputStream( TEST_READ_WRITE.getBytes() ) );
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		file.write( out );

		final ByteArrayInputStream actualIn = new ByteArrayInputStream( out.toByteArray() );
		final ByteArrayInputStream expectedIn = new ByteArrayInputStream( TEST_READ_WRITE.getBytes( "UTF-8" ) );
		XMLTestTools.assertXMLEquals( expectedIn, actualIn );
	}
}
