/*
 * Copyright (c) 2011-2017, Numdata BV, The Netherlands.
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

package com.numdata.oss.template;

import java.io.*;

import org.jetbrains.annotations.*;

/**
 * Template content consisting of the value of a named variable.
 *
 * @author G. Meinders
 */
public class VariableContent
	implements TemplateContent
{
	/**
	 * Name of the variable.
	 */
	@NotNull
	private final String _variable;

	/**
	 * Constructs a new instance.
	 *
	 * @param   variable    Name of the variable.
	 */
	public VariableContent( @NotNull final String variable )
	{
		_variable = variable;
	}

	/**
	 * Returns the name of the variable.
	 *
	 * @return  Name of the variable.
	 */
	@NotNull
	public String getVariable()
	{
		return _variable;
	}

	@Override
	public void write( @NotNull final TemplateOutput out )
		throws IOException
	{
		final Template context = out.getContext();
		final String value = context.getVariable( _variable );
		if ( value != null )
		{
			out.append( value );
		}
	}
}
