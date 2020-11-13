/*
 * (C) Copyright Numdata BV 2020-2020 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.jaxrs;

import java.io.*;
import java.nio.charset.*;
import javax.ws.rs.core.*;

import static java.util.Collections.*;
import org.json.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link JSONArrayMessageBodyWriter}.
 *
 * @author Gerrit Meinders
 */
public class TestJSONArrayMessageBodyWriter
{
	@Test
	public void testIsWriteable()
	{
		@SuppressWarnings( "TypeMayBeWeakened" )
		final JSONArrayMessageBodyWriter writer = new JSONArrayMessageBodyWriter();

		assertTrue( "Supported class and media type", writer.isWriteable( JSONArray.class, JSONArray.class, JSONArray.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE ) );
		assertFalse( "Unsupported class", writer.isWriteable( Object.class, Object.class, Object.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE ) );
		assertFalse( "Unsupported media type", writer.isWriteable( JSONArray.class, JSONArray.class, JSONArray.class.getAnnotations(), MediaType.TEXT_HTML_TYPE ) );
	}

	@Test
	public void testWriteTo()
	throws IOException
	{
		@SuppressWarnings( "TypeMayBeWeakened" )
		final JSONArrayMessageBodyWriter writer = new JSONArrayMessageBodyWriter();

		{
			final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			writer.writeTo( new JSONArray().put( 123 ).put( "ÿ" ).put( "α" ), JSONArray.class, JSONArray.class, JSONArray.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE, headers, out );
			assertEquals( "Unexpected output", "[123,\"ÿ\",\"α\"]", out.toString( StandardCharsets.UTF_8.name() ) );
			assertEquals( "Unexpected headers", singletonList( MediaType.APPLICATION_JSON_TYPE.withCharset( StandardCharsets.UTF_8.name() ) ), headers.remove( "Content-Type" ) );
			assertTrue( "Unexpected headers: " + headers, headers.isEmpty() );
		}

		{
			final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			writer.writeTo( new JSONArray().put( 123 ).put( "ÿ" ).put( "α" ), JSONArray.class, JSONArray.class, JSONArray.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE.withCharset( StandardCharsets.ISO_8859_1.name() ), headers, out );
			assertEquals( "Unexpected output", "[123,\"ÿ\",\"?\"]", out.toString( StandardCharsets.ISO_8859_1.name() ) );
			assertEquals( "Unexpected headers", singletonList( MediaType.APPLICATION_JSON_TYPE.withCharset( StandardCharsets.ISO_8859_1.name() ) ), headers.remove( "Content-Type" ) );
			assertTrue( "Unexpected headers: " + headers, headers.isEmpty() );
		}
	}
}
