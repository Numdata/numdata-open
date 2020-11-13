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
