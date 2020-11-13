/*
 * Copyright (c) 2016-2020, Numdata BV, The Netherlands.
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
