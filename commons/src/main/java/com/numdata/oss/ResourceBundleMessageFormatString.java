/*
 * Copyright (c) 2017, Numdata BV, The Netherlands.
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
package com.numdata.oss;

import java.text.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class provides a localized string value from a message format in a
 * resource bundle and some message format arguments.
 *
 * @author Peter S. Heijnen
 */
public class ResourceBundleMessageFormatString
extends AbstractLocalizableString
{
	/**
	 * Class whose resource bundle is used.
	 */
	private final Class<?> _resourceClass;

	/**
	 * Name of message format resource in bundle.
	 */
	private final String _resourceName;

	/**
	 * Message format arguments.
	 */
	private final Object[] _arguments;

	/**
	 * Construct localizable resource bundle string.
	 *
	 * @param resourceClass Class whose resource bundle is used.
	 * @param resourceName  Name of resource in bundle.
	 * @param arguments     Message format arguments.
	 */
	public ResourceBundleMessageFormatString( @NotNull final Class<?> resourceClass, @NotNull final String resourceName, @NotNull final Object... arguments )
	{
		_resourceClass = resourceClass;
		_resourceName = resourceName;
		//noinspection AssignmentToCollectionOrArrayFieldFromParameter
		_arguments = arguments;
	}

	@Override
	public String get( @Nullable final Locale locale )
	{
		final ResourceBundle bundle = ResourceBundleTools.getBundleHierarchy( _resourceClass, locale );
		return MessageFormat.format( bundle.getString( _resourceName ), _arguments );
	}
}
