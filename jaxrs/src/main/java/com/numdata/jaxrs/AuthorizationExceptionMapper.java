/*
 * (C) Copyright Numdata BV 2015-2015 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.jaxrs;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import com.numdata.oss.log.*;
import com.numdata.oss.net.*;

/**
 * Exception mapper for {@link AuthorizationException}.
 *
 * @author Gerrit Meinders
 */
@Provider
public class AuthorizationExceptionMapper
implements ExceptionMapper<AuthorizationException>
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( AuthorizationExceptionMapper.class );

	@Override
	public Response toResponse( final AuthorizationException exception )
	{
		LOG.warn( "Authorization exception thrown by web service", exception );
		return Response.status( Response.Status.FORBIDDEN ).type( MediaType.TEXT_PLAIN_TYPE ).entity( exception.getMessage() ).build();
	}
}
