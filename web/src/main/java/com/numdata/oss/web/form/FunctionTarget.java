/*
 * Copyright (c) 2020, Numdata BV, The Netherlands.
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

import java.util.function.*;

import org.jetbrains.annotations.*;

/**
 * Field target based on a given getter and setter.
 *
 * @author Gerrit Meinders
 */
public class FunctionTarget
implements FieldTarget
{
	/**
	 * Field name.
	 */
	private final @NotNull String _name;

	/**
	 * Getter for the field.
	 */
	private final @NotNull Supplier<@Nullable String> _getter;

	/**
	 * Setter for the field.
	 */
	private final @NotNull Consumer<@Nullable String> _setter;

	/**
	 * Constructs a new instance.
	 *
	 * @param name   Name of the field.
	 * @param getter Getter for the field.
	 * @param setter Setter for the field.
	 */
	public FunctionTarget( final @NotNull String name, final @NotNull Supplier<@Nullable String> getter, final @NotNull Consumer<@Nullable String> setter )
	{
		_name = name;
		_getter = getter;
		_setter = setter;
	}

	@Override
	public @NotNull String getName()
	{
		return _name;
	}

	@Override
	public @Nullable String getValue()
	{
		return _getter.get();
	}

	@Override
	public void setValue( final @Nullable String value )
	{
		_setter.accept( value );
	}
}
