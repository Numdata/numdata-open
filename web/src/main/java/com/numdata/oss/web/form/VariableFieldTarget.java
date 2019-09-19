/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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

import org.jetbrains.annotations.*;

/**
 * Target type: variable. Value is stored within this target instance, since the
 * variable is not assigned to any real target.
 *
 * @author G.B.M. Rupert
 */
public class VariableFieldTarget
implements FieldTarget
{
	/**
	 * Name of form field.
	 */
	@NotNull
	private final String _name;

	/**
	 * Current target value.
	 */
	@Nullable
	private String _value;

	/**
	 * Construct new variable field target.
	 *
	 * @param name Name of variable.
	 */
	public VariableFieldTarget( @NotNull final String name )
	{
		_name = name;
		_value = null;
	}

	/**
	 * Construct new variable field target.
	 *
	 * @param name  Name of variable.
	 * @param value Initial value.
	 */
	public VariableFieldTarget( @NotNull final String name, @Nullable final String value )
	{
		_name = name;
		_value = value;
	}

	@Override
	@NotNull
	public String getName()
	{
		return _name;
	}

	@Override
	@Nullable
	public String getValue()
	{
		return _value;
	}

	@Override
	public void setValue( @Nullable final String value )
	{
		_value = value;
	}
}
