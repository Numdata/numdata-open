/*
 * (C) Copyright Numdata BV 2016-2016 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.jaxrs;

import java.io.*;
import java.lang.annotation.*;
import java.lang.reflect.*;
import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import org.json.*;

/**
 * Provider for writing a {@link JSONArray}.
 *
 * @author Gerrit Meinders
 */
@Provider
@Produces( MediaType.APPLICATION_JSON )
public class JSONArrayMessageBodyWriter
	implements MessageBodyWriter<JSONArray>
{
	@Override
	public boolean isWriteable( final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType )
	{
		return JSONArray.class.isAssignableFrom( type ) && mediaType.isCompatible( MediaType.APPLICATION_JSON_TYPE );
	}

	@Override
	public void writeTo( final JSONArray jsonArray, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream )
	throws IOException, WebApplicationException
	{
		final Map<String, String> parameters = mediaType.getParameters();
		final String mediaTypeCharset = parameters.get( MediaType.CHARSET_PARAMETER );
		final String charset = ( mediaTypeCharset != null ) ? mediaTypeCharset : "UTF-8";
		httpHeaders.putSingle( "Content-Type", mediaType.withCharset( charset ) );

		final OutputStreamWriter writer = new OutputStreamWriter( entityStream, charset );
		try
		{
			jsonArray.write( writer );
		}
		finally
		{
			writer.flush();
		}
	}
}
