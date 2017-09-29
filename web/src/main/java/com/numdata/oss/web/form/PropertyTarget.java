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
package com.numdata.oss.web.form;

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Provides access to a property stored in a {@link Properties} object.
 *
 * @author Peter S. Heijnen
 */
public class PropertyTarget
implements FieldTarget
{
	/**
	 * Target {@link Properties}.
	 */
	private final Properties _properties;

	/**
	 * Name of form field.
	 */
	private final String _name;

	/**
	 * Construct new map field target.
	 *
	 * @param properties {@link Properties} object.
	 * @param name       Name of property.
	 */
	@SuppressWarnings ( "AssignmentToCollectionOrArrayFieldFromParameter" )
	public PropertyTarget( final Properties properties, final String name )
	{
		_properties = properties;
		_name = name;
	}

	@NotNull
	@Override
	public String getName()
	{
		return _name;
	}

	@Nullable
	@Override
	public String getValue()
	{
		return _properties.getProperty( _name );
	}

	@Override
	public void setValue( @Nullable final String value )
	{
		if ( value == null )
		{
			_properties.remove( _name );
		}
		else
		{
			_properties.setProperty( _name, value );
		}
	}
}