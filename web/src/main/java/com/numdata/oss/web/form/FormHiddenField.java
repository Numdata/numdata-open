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

import java.io.*;
import javax.servlet.http.*;

import com.numdata.oss.log.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * A hidden field can be used to define static parameters to include when the
 * form is submitted. This is typically used to include a record ID or some
 * other information that is needed to process the form.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public class FormHiddenField
extends FormField
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FormHiddenField.class );

	/**
	 * Construct hidden field.
	 *
	 * @param target Target object of field.
	 */
	public FormHiddenField( final FieldTarget target )
	{
		super( target );
		setEditable( false );
	}

	/**
	 * Construct hidden field.
	 *
	 * @param target Target object of field.
	 * @param value  Value to set the field to.
	 */
	public FormHiddenField( final FieldTarget target, final String value )
	{
		this( target );
		setValue( value );
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		formFactory.writeHiddenField( iw, getName(), getValue() );
	}


	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	throws InvalidFormDataException
	{
		final SubmitStatus result;

		if ( isEditable() )
		{
			final String value = request.getParameter( getName() );
			if ( value != null )
			{
				setValue( value );

				result = SubmitStatus.SUBMITTED;
			}
			else
			{
				LOG.trace( "Not submitted, because '" + getName() + "' parameter is not set" );
				result = SubmitStatus.NOT_SUBMITTED;
			}
		}
		else
		{
			result = SubmitStatus.UNKNOWN;
		}

		return result;
	}
}