/*
 * (C) Copyright Numdata BV 2015-2018 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
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
