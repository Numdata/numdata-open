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

package com.numdata.oss.template;

import java.io.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * A template for text content.
 *
 * @author G. Meinders
 */
public class Template
{
	/**
	 * Content of the template.
	 */
	private final List<TemplateContent> _content;

	/**
	 * Defines user input needed to fill in the template.
	 */
	private final List<TemplateInput> _inputs;

	/**
	 * Value for each known variable.
	 */
	private final Map<String, String> _variables;

	/**
	 * Constructs a new empty template.
	 */
	public Template()
	{
		_content = new ArrayList<TemplateContent>();
		_inputs = new ArrayList<TemplateInput>();
		_variables = new HashMap<String, String>();
	}

	/**
	 * Adds the given content to the template.
	 *
	 * @param   content     Content to be added.
	 */
	public void addContent( final TemplateContent content )
	{
		_content.add( content );
	}

	/**
	 * Returns the content definitions that make up the template.
	 *
	 * @return  Content of the template.
	 *
	 * @see     #generate(TemplateOutput)
	 */
	List<TemplateContent> getContent()
	{
		return Collections.unmodifiableList( _content );
	}

	/**
	 * Generates output for the template.
	 *
	 * @param   out     Receives template output.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	public void generate( @NotNull final TemplateOutput out )
		throws IOException
	{
		if ( out.getContext() != this )
		{
			throw new IllegalArgumentException( "Given output object must have this template as context." );
		}

		for ( final TemplateContent content : _content )
		{
			content.write( out );
		}
	}

	/**
	 * Adds an input definition to the template.
	 *
	 * @param   input   Input definition.
	 */
	public void addInput( final TemplateInput input )
	{
		_inputs.add( input );
	}

	/**
	 * Returns the template's inputs.
	 *
	 * @return  Template inputs.
	 */
	public List<TemplateInput> getInputs()
	{
		return Collections.unmodifiableList( _inputs );
	}

	/**
	 * Sets the value of a variable.
	 *
	 * @param   variable    Name of the variable.
	 * @param   value       Value to be set.
	 */
	public void setVariable( @NotNull final String variable, @Nullable final String value )
	{
		_variables.put( variable, value );
	}

	/**
	 * Returns the value of a variable.
	 *
	 * @param   variable    Name of the variable.
	 *
	 * @return  Variable value.
	 */
	@Nullable
	public String getVariable( @NotNull final String variable )
	{
		return _variables.get( variable );
	}
}
