/*
 * (C) Copyright Numdata BV 2016-2017 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.jaxrs;

import java.lang.annotation.*;
import javax.ws.rs.*;

/**
 * Annotation to add a Cache-Control header.
 *
 * @author Gerrit Meinders
 * @noinspection JavaDoc
 */
@Target ( { ElementType.TYPE, ElementType.METHOD } )
@Retention ( RetentionPolicy.RUNTIME )
@NameBinding
public @interface CacheControl
{
	/**
	 * Value of the no-cache cache-control directive.
	 */
	boolean noCache() default false;

	/**
	 * Value of the must-revalidate cache-control directive.
	 */
	boolean mustRevalidate() default false;

	/**
	 * Value of the max-age cache-control directive (in seconds); {@code -1} to disable.
	 */
	int maxAge() default -1;
}
