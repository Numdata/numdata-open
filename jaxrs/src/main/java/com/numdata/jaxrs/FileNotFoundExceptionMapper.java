/*
 * (C) Copyright Numdata BV 2015-2015 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.jaxrs;

import java.io.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import com.numdata.oss.log.*;

/**
 * Exception mapper for {@link FileNotFoundException}.
 *
 * @author Gerrit Meinders
 */
@Provider
public class FileNotFoundExceptionMapper
implements ExceptionMapper<FileNotFoundException>
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FileNotFoundExceptionMapper.class );

	@Override
	public Response toResponse( final FileNotFoundException exception )
	{
		LOG.warn( "File not found exception thrown by web service", exception );
		return Response.status( Response.Status.NOT_FOUND ).type( MediaType.TEXT_PLAIN_TYPE ).entity( exception.getMessage() ).build();
	}
}
