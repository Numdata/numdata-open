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
package com.numdata.jaxrs;

import java.io.*;
import java.nio.charset.*;
import javax.ws.rs.core.*;

import org.json.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link JSONObjectMessageBodyReader}.
 *
 * @author Gerrit Meinders
 */
public class TestJSONObjectMessageBodyReader
{
	@Test
	public void testIsWriteable()
	{
		@SuppressWarnings( "TypeMayBeWeakened" )
		final JSONObjectMessageBodyReader reader = new JSONObjectMessageBodyReader();

		assertTrue( "Supported class and media type", reader.isReadable( JSONObject.class, JSONObject.class, JSONObject.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE ) );
		assertFalse( "Unsupported class", reader.isReadable( Object.class, Object.class, Object.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE ) );
		assertFalse( "Unsupported media type", reader.isReadable( JSONObject.class, JSONObject.class, JSONObject.class.getAnnotations(), MediaType.TEXT_HTML_TYPE ) );
	}

	@Test
	public void testReadFrom()
	throws IOException
	{
		final JSONObjectMessageBodyReader reader = new JSONObjectMessageBodyReader();

		{
			final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
			final ByteArrayInputStream in = new ByteArrayInputStream( "{\"ydia-alpha\":\"ÿα\"}".getBytes( StandardCharsets.UTF_8 ) );
			final JSONObject jsonObject = reader.readFrom( JSONObject.class, JSONObject.class, JSONObject.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE, headers, in );
			assertEquals( "Unexpected result", new JSONObject().put( "ydia-alpha", "ÿα" ).toString(), jsonObject.toString() );
		}

		{
			final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
			final ByteArrayInputStream in = new ByteArrayInputStream( "{\"ydia-alpha\":\"ÿα\"}".getBytes( StandardCharsets.ISO_8859_1 ) );
			final JSONObject jsonObject = reader.readFrom( JSONObject.class, JSONObject.class, JSONObject.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE.withCharset( StandardCharsets.ISO_8859_1.name() ), headers, in );
			assertEquals( "Unexpected result", new JSONObject().put( "ydia-alpha", "ÿ?" ).toString(), jsonObject.toString() );
		}
	}
}
