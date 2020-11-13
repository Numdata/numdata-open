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
import org.json.*;

/**
 * Exception mapper for {@link JSONException}.
 *
 * @author Gerrit Meinders
 */
@Provider
public class JSONExceptionMapper
implements ExceptionMapper<JSONException>
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( JSONExceptionMapper.class );

	@Override
	public Response toResponse( final JSONException exception )
	{
		LOG.warn( "JSON exception thrown by web service", exception );
		return Response.serverError().type( MediaType.TEXT_PLAIN_TYPE ).entity( exception.getMessage() ).build();
	}
}
