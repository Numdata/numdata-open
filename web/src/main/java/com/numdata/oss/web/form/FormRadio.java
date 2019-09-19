/*
 * Copyright (c) 2015-2017, Numdata BV, The Netherlands.
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

import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * Radio button for web forms.
 *
 * @author Peter S. Heijnen
 */
public class FormRadio
extends FormField
{
	/**
	 * Radio value.
	 */
	@NotNull
	private String _value;

	/**
	 * Whether this radio is checked or not.
	 */
	private boolean _checked = false;

	/**
	 * Constructor for radio button.
	 *
	 * @param target  Target object for this field.
	 * @param value   Radio value.
	 * @param checked Whether this radio is checked or not.
	 */
	public FormRadio( @NotNull final FieldTarget target, @NotNull final String value, final boolean checked )
	{
		super( target );
		_value = value;
		_checked = checked;
	}

	@NotNull
	@Override
	public String getValue()
	{
		return _value;
	}

	@Override
	public void setValue( @Nullable final String value )
	{
		if ( value == null )
		{
			throw new IllegalArgumentException( "null value is not allowed" );
		}

		_value = value;
	}

	/**
	 * Returns whether this radio is checked or not.
	 *
	 * @return Whether this radio is checked or not.
	 */
	public boolean isChecked()
	{
		return _checked;
	}

	/**
	 * Sets whether this radio is checked or not.
	 *
	 * @param checked Whether this radio is checked or not.
	 */
	public void setChecked( final boolean checked )
	{
		_checked = checked;
	}

	/**
	 * Set field value as boolean.
	 *
	 * @param value Boolean field value.
	 */
	public void setBooleanValue( final boolean value )
	{
		setValue( Boolean.toString( value ) );
	}

	/**
	 * Set field value as boolean.
	 *
	 * @param value Boolean field value.
	 */
	public void setBooleanValue( final Boolean value )
	{
		setValue( ( value != null ) ? Boolean.toString( value ) : null );
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		formFactory.writeRadioButton( contextPath, iw, isEditable(), getName(), getValue(), isChecked(), null );
	}

	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	{
		final SubmitStatus result;

		if ( !isEditable() )
		{
			result = SubmitStatus.UNKNOWN;
		}
		else if ( request.getParameter( getName() ) != null )
		{
			final String value = getValue();
			final boolean checked = value.equals( request.getParameter( getName() ) );
			setChecked( checked );
			if ( checked )
			{
				super.setValue( value );
			}
			result = SubmitStatus.SUBMITTED;
		}
		else
		{
			result = SubmitStatus.NOT_SUBMITTED;
		}

		return result;
	}

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		out.append( '(' ).append( isChecked() ? 'O' : ' ' ).append( ") " );
	}
}
