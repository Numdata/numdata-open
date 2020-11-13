/*
 * Copyright (c) 2008-2020, Numdata BV, The Netherlands.
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

import com.numdata.oss.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * This component represents a label on a web form. A label is placed in front
 * of any input components. It will also create a new row in the created result
 * table.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public class FormLabel
extends FormStaticText
{
	/**
	 * Associated form field.
	 */
	@Nullable
	private FormField _field;

	/**
	 * Constructs a form label with the given text.
	 *
	 * @param text Label text.
	 */
	public FormLabel( final String text )
	{
		this( text, null );
	}

	/**
	 * Constructs a form label with the given text.
	 *
	 * @param text  Label text.
	 * @param field Associated form field.
	 */
	public FormLabel( final String text, @Nullable final FormField field )
	{
		super( text );
		_field = field;
	}

	/**
	 * Returns the form field associated with this label.
	 *
	 * @return Associated form field; {@code null} if none.
	 */
	@Nullable
	public FormField getField()
	{
		return _field;
	}

	/**
	 * Set the form field associated with this label.
	 *
	 * @param field Associated form field; {@code null} if none.
	 */
	public void setField( @Nullable final FormField field )
	{
		_field = field;
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		formFactory.writeLabel( contextPath, table, iw, this );
	}

	/**
	 * Returns any error message for field associated with label.
	 *
	 * @return Error message from associated field, if any.
	 */
	@Nullable
	public String getErrorMessage()
	{
		final FormField field = getField();

		/*
		 * Show an error message if there is one. Look for errors starting with
		 * the associated form field and include any unlabeled fields after it.
		 */
		String errorMessage = null;
		for ( FormComponent sibling = field; ( sibling != null ) && !( sibling instanceof FormLabel ); sibling = sibling.getNext() )
		{
			if ( sibling instanceof FormField )
			{
				errorMessage = ( (FormField)sibling ).getErrorMessage();
				if ( errorMessage != null )
				{
					break;
				}
			}
		}
		return errorMessage;
	}

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		final String text = getText();
		int maxLength = text == null ? 0 : text.length();

		final FormContainer parent = getParent();
		if ( parent != null )
		{
			for ( final FormComponent component : parent.getComponents() )
			{
				if ( component instanceof FormLabel )
				{
					final String otherText = ( (FormLabel)component ).getText();
					if ( otherText != null )
					{
						maxLength = Math.max( maxLength, otherText.length() );
					}
				}
			}
		}

		out.append( "\n" ).append( indent );
		TextTools.appendFixed( out, text, maxLength, false, '.' );
		out.append( " : " );
	}
}
