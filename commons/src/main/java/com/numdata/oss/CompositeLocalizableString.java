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

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Localizable string made up of components, which can in turn be localizable
 * strings.
 *
 * @author G. Meinders
 */
public class CompositeLocalizableString
extends AbstractCachingLocalizableString
{
	/**
	 * Components that make up the string.
	 */
	private final Collection<Object> _components = new ArrayList<Object>();

	@NotNull
	@Override
	protected String getNewString( @NotNull final Locale locale )
	{
		final StringBuilder builder = new StringBuilder();
		for ( final Object component : _components )
		{
			if ( component instanceof LocalizableString )
			{
				final LocalizableString localizableString = (LocalizableString)component;
				builder.append( localizableString.get( locale ) );
			}
			else
			{
				builder.append( component );
			}
		}
		return builder.toString();
	}

	/**
	 * Test whether this composition contains any components.
	 *
	 * @return {@code true} if this composition is empty; {@code false} if at
	 * least one component is present.
	 */
	public boolean isEmpty()
	{
		return _components.isEmpty();
	}

	/**
	 * Appends the given object to the string.
	 *
	 * @param component Component to be appended.
	 */
	public void append( final Object component )
	{
		_components.add( component );
	}
}
