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

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Defines input for the template.
 *
 * @author G. Meinders
 */
public class TemplateInput
{
	/**
	 * Name of the variable that stores the input.
	 */
	@NotNull
	private final String _variable;

	/**
	 * Message to be shown when requesting input.
	 */
	@Nullable
	private LocalizedString _message;

	/**
	 * Constructs a new input for the specified variable.
	 *
	 * @param   variable    Name of the variable that stores the input.
	 */
	public TemplateInput( @NotNull final String variable )
	{
		_variable = variable;
	}

	/**
	 * Returns the name of the variable that stores the input.
	 *
	 * @return  Name of the variable.
	 */
	@NotNull
	public String getVariable()
	{
		return _variable;
	}

	/**
	 * Returns the message to be shown when requesting input.
	 *
	 * @return  Message.
	 */
	@Nullable
	public LocalizedString getMessage()
	{
		return _message;
	}

	/**
	 * Sets the message to be shown when requesting input.
	 *
	 * @param   message     Message to be shown.
	 */
	public void setMessage( @Nullable final LocalizedString message )
	{
		_message = message;
	}
}
