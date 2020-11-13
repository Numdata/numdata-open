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
 * Provider for reading a {@link JSONArray}.
 *
 * @author Gerrit Meinders
 */
@Provider
@Consumes( MediaType.APPLICATION_JSON )
public class JSONArrayMessageBodyReader
	implements MessageBodyReader<JSONArray>
{
	@Override
	public boolean isReadable( final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType )
	{
		return JSONArray.class.isAssignableFrom( type ) && mediaType.isCompatible( MediaType.APPLICATION_JSON_TYPE );
	}

	@Override
	public JSONArray readFrom( final Class<JSONArray> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream )
	throws IOException, WebApplicationException
	{
		final Map<String, String> parameters = mediaType.getParameters();
		final String charset = parameters.get( MediaType.CHARSET_PARAMETER );
		final InputStreamReader reader = new InputStreamReader( entityStream, ( charset != null ) ? charset : "UTF-8" );
		return new JSONArray( new JSONTokener( reader ) );
	}
}
