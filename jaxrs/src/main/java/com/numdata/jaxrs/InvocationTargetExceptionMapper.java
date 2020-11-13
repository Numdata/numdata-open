/*
 * Copyright (c) 2015-2020, Numdata BV, The Netherlands.
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

import java.lang.reflect.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import com.numdata.oss.log.*;

/**
 * Exception mapper for {@link InvocationTargetException}. The cause of the
 * exception is unwrapped and forwarded to the appropriate exception mapper.
 *
 * @author Gerrit Meinders
 */
@Provider
public class InvocationTargetExceptionMapper
implements ExceptionMapper<InvocationTargetException>
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( InvocationTargetExceptionMapper.class );

	/**
	 * Provides runtime lookup of provider instances.
	 */
	private final Providers _providers;

	/**
	 * Constructs a new instance.
	 *
	 * @param providers Provides runtime lookup of provider instances.
	 */
	public InvocationTargetExceptionMapper( @Context final Providers providers )
	{
		_providers = providers;
	}

	@Override
	public Response toResponse( final InvocationTargetException exception )
	{
		LOG.debug( "Invocation target exception thrown by web service", exception );

		final Throwable cause = exception.getCause();
		final Response result;
		if ( cause != null )
		{
			final ExceptionMapper<Throwable> mapper = (ExceptionMapper<Throwable>)_providers.getExceptionMapper( cause.getClass() );
			if ( mapper != null )
			{
				result = mapper.toResponse( cause );
			}
			else
			{
				result = Response.serverError().type( MediaType.TEXT_PLAIN_TYPE ).entity( exception.getMessage() ).build();
			}
		}
		else
		{
			result = Response.serverError().type( MediaType.TEXT_PLAIN_TYPE ).entity( exception.getMessage() ).build();
		}
		return result;
	}
}
