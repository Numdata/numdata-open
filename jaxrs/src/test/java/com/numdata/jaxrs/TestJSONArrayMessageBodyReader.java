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

import org.json.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link JSONArrayMessageBodyReader}.
 *
 * @author Gerrit Meinders
 */
public class TestJSONArrayMessageBodyReader
{
	@Test
	public void testIsWriteable()
	{
		@SuppressWarnings( "TypeMayBeWeakened" )
		final JSONArrayMessageBodyReader reader = new JSONArrayMessageBodyReader();

		assertTrue( "Supported class and media type", reader.isReadable( JSONArray.class, JSONArray.class, JSONArray.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE ) );
		assertFalse( "Unsupported class", reader.isReadable( Object.class, Object.class, Object.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE ) );
		assertFalse( "Unsupported media type", reader.isReadable( JSONArray.class, JSONArray.class, JSONArray.class.getAnnotations(), MediaType.TEXT_HTML_TYPE ) );
	}

	@Test
	public void testReadFrom()
	throws IOException
	{
		final JSONArrayMessageBodyReader reader = new JSONArrayMessageBodyReader();

		{
			final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
			final ByteArrayInputStream in = new ByteArrayInputStream( "[123,\"ÿ\",\"α\"]".getBytes( StandardCharsets.UTF_8 ) );
			final JSONArray jsonArray = reader.readFrom( JSONArray.class, JSONArray.class, JSONArray.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE, headers, in );
			assertEquals( "Unexpected result", new JSONArray().put( 123 ).put( "ÿ" ).put( "α" ).toString(), jsonArray.toString() );
		}

		{
			final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
			final ByteArrayInputStream in = new ByteArrayInputStream( "[123,\"ÿ\",\"α\"]".getBytes( StandardCharsets.ISO_8859_1 ) );
			final JSONArray jsonArray = reader.readFrom( JSONArray.class, JSONArray.class, JSONArray.class.getAnnotations(), MediaType.APPLICATION_JSON_TYPE.withCharset( StandardCharsets.ISO_8859_1.name() ), headers, in );
			assertEquals( "Unexpected result", new JSONArray().put( 123 ).put( "ÿ" ).put( "?" ).toString(), jsonArray.toString() );
		}
	}
}
