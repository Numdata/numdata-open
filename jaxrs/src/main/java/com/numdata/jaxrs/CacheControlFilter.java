/*
 * (C) Copyright Numdata BV 2016-2020 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.jaxrs;

import java.lang.annotation.*;
import java.lang.reflect.*;
import javax.annotation.*;
import javax.ws.rs.*;
import javax.ws.rs.container.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

/**
 * Filter that handles {@link CacheControl} annotations.
 *
 * @author Gerrit Meinders
 */
@Provider
@CacheControl
@Priority( Priorities.HEADER_DECORATOR )
public class CacheControlFilter
implements ContainerResponseFilter
{
	/**
	 * Resource class/method that matched the current request.
	 */
	@Context
	private ResourceInfo _resourceInfo;

	@Override
	public void filter( final ContainerRequestContext requestContext, final ContainerResponseContext responseContext )
	{
		CacheControl annotation = null;

		for ( final Annotation candidate : responseContext.getEntityAnnotations() )
		{
			if ( candidate instanceof CacheControl )
			{
				annotation = (CacheControl)candidate;
				break;
			}
		}

		if ( ( annotation == null ) && ( _resourceInfo != null ) )
		{
			final Method resourceMethod = _resourceInfo.getResourceMethod();
			if ( resourceMethod != null )
			{
				annotation = resourceMethod.getAnnotation( CacheControl.class );
			}

			if ( annotation == null )
			{
				final Class<?> resourceClass = _resourceInfo.getResourceClass();
				if ( resourceClass != null )
				{
					annotation = resourceClass.getAnnotation( CacheControl.class );
				}
			}

		}

		if ( annotation != null )
		{
			final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
			final javax.ws.rs.core.CacheControl cacheControl = new javax.ws.rs.core.CacheControl();
			if ( annotation.noCache() )
			{
				cacheControl.setNoCache( true );
				cacheControl.setMaxAge( 0 ); // to prevent something else from being set by 'ExpiresFilter'
			}
			else
			{
				cacheControl.setMustRevalidate( annotation.mustRevalidate() );
				cacheControl.setMaxAge( annotation.maxAge() );
			}
			headers.putSingle( HttpHeaders.CACHE_CONTROL, cacheControl );
		}
	}
}
