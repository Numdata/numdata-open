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
