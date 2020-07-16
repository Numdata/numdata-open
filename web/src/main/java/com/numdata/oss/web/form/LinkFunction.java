/*
 * (C) Copyright Numdata BV 2017-2020 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.oss.web.form;

import org.jetbrains.annotations.*;

/**
 * Function to get link for a field value.
 *
 * @author Peter S. Heijnen
 */
public interface LinkFunction<T>
{
	/**
	 * Returns a link for the given value.
	 *
	 * @param contextPath Web application context path.
	 * @param value       Form field value.
	 *
	 * @return Link for the given value; {@code null} if no link is available.
	 */
	@Nullable
	String getLink( @NotNull final String contextPath, @NotNull final T value );
}
