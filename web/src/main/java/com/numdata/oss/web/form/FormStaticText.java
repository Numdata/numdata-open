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

import java.io.*;
import javax.servlet.http.*;

import com.numdata.oss.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * This component is a static piece of text that can be used on a web form. It
 * may contain instructions to the user, information about units, separator
 * text, etc.
 *
 * @author Peter S. Heijnen
 */
public class FormStaticText
extends FormComponent
{
	/**
	 * Label text.
	 */
	@Nullable
	private String _text;

	/**
	 * Create static text component with the specified text.
	 *
	 * @param text Label text.
	 */
	public FormStaticText( @Nullable final String text )
	{
		_text = text;
	}

	/**
	 * Set text of this label.
	 *
	 * @param text Text of this label.
	 */
	public void setText( @Nullable final String text )
	{
		_text = text;
	}

	/**
	 * Get text of this label.
	 *
	 * @return Text of this label.
	 */
	@Nullable
	public String getText()
	{
		return _text;
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		final String text = getText();
		iw.print( ( text != null ) && TextTools.isNonEmpty( text ) ? text : "&nbsp;" );
	}

	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	{
		return SubmitStatus.UNKNOWN;
	}

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		final Object text = getText();
		if ( text != null )
		{
			out.append( String.valueOf( text ).replaceAll( "&nbsp;", " " ).replaceAll( "<br\\s*/?>", "\n" + indent ) );
		}
	}
}
