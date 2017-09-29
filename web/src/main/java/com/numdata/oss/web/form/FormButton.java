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
import java.util.*;
import javax.servlet.http.*;

import com.numdata.oss.*;
import com.numdata.oss.web.*;
import org.intellij.lang.annotations.*;
import org.jetbrains.annotations.*;

/**
 * This class represents a button that can be placed on a form.
 *
 * Buttons can be used to submit forms, allow the user to go back, reset a form,
 * link to resources, etc.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public class FormButton
extends FormComponent
{
	/**
	 * Button type: normal button (link).
	 */
	private static final int NORMAL = 0;

	/**
	 * Button type: form submit button.
	 */
	public static final int SUBMIT = 1;

	/**
	 * Button type: back button (history[-1]).
	 */
	public static final int BACK = 2;

	/**
	 * Button type: form upload button.
	 */
	public static final int BROWSE = 3;

	/**
	 * Button type: form image button.
	 */
	public static final int IMAGE = 4;

	/**
	 * Type of button ({@link #NORMAL}, {@link #SUBMIT}, {@link #BACK}, {@link
	 * #BROWSE}, or {@link #IMAGE}).
	 */
	private final int _type;

	/**
	 * Action when button pressed (link).
	 */
	private final String _action;

	/**
	 * Text on the button.
	 */
	private final String _text;

	/**
	 * Styles applied to the button. May be {@code null}.
	 */
	private final List<String> _styles;

	/**
	 * Create special button.
	 *
	 * @param type   Type of button ({@link #NORMAL}, {@link #SUBMIT}, {@link
	 *               #BACK}, {@link #BROWSE}, or {@link #IMAGE}).
	 * @param text   Text shown on the button.
	 * @param styles Styles applied to the button. May be empty/{@code null}.
	 */
	public FormButton( final int type, final String text, final String... styles )
	{
		this( null, type, text, styles );
	}

	/**
	 * Create special button.
	 *
	 * @param name   Name identifying the button.
	 * @param type   Type of button ({@link #NORMAL}, {@link #SUBMIT}, {@link
	 *               #BACK}, {@link #BROWSE}, or {@link #IMAGE}).
	 * @param text   Text shown on the button.
	 * @param styles Styles applied to the button. May be empty/{@code null}.
	 */
	public FormButton( @Nullable final String name, final int type, final String text, final String... styles )
	{
		super( name );

		_type = type;
		_action = null;
		_text = text;
		_styles = ( ( styles != null ) && ( styles.length > 0 ) ) ? Arrays.asList( styles ) : null;
	}

	/**
	 * Create normal button.
	 *
	 * @param action Action when button pressed (link).
	 * @param text   Text shown on the button.
	 * @param styles Styles applied to the button. May be empty/{@code null}.
	 */
	public FormButton( final String action, final String text, final String... styles )
	{
		super( null );
		_type = NORMAL;
		_action = action;
		_text = text;
		_styles = ( ( styles != null ) && ( styles.length > 0 ) ) ? Arrays.asList( styles ) : null;
	}

	/**
	 * Returns the button type.
	 *
	 * @return Button type.
	 */
	@MagicConstant( intValues = { NORMAL, SUBMIT, BACK, BROWSE, IMAGE } )
	public int getType()
	{
		return _type;
	}

	/**
	 * Returns the action (link) when the button is pressed.
	 *
	 * @return Button action link.
	 */
	public String getAction()
	{
		return _action;
	}

	/**
	 * Returns the text shown on the button.
	 *
	 * @return Button text.
	 */
	public String getText()
	{
		return _text;
	}

	/**
	 * Generate output for button.
	 *
	 * @param contextPath Context path associated with servlet (may be empty or
	 *                    {@code null}).
	 * @param iw          Writer used for output.
	 * @param formFactory HTML form factory.
	 *
	 * @throws IOException if an error occurs while generating output.
	 */
	public void generate( final String contextPath, final IndentingJspWriter iw, final HTMLFormFactory formFactory )
	throws IOException
	{
		final String action = _action;
		final String name = getName();
		final String text = _text;
		final List<String> styles = _styles;
		final boolean hasStyles = ( styles != null ) && !styles.isEmpty();
		final Map<String, String> attributes = hasStyles ? Collections.singletonMap( "class", TextTools.getList( styles, ' ' ) ) : null;

		switch ( _type )
		{
			case NORMAL:
				formFactory.writeButton( iw, action, text, attributes );
				break;

			case SUBMIT:
				if ( TextTools.isNonEmpty( action ) )
				{
					throw new IOException( "Can't set action for submit button" );
				}

				formFactory.writeSubmitButton( iw, name, text, attributes );
				break;

			case BACK:
				if ( TextTools.isNonEmpty( action ) )
				{
					throw new IOException( "Can't set action for back button" );
				}

				if ( hasStyles )
				{
					throw new IOException( "Can't apply styles to back button" );
				}

				formFactory.writeBackButton( contextPath, iw, text );
				break;

			case BROWSE:
				if ( TextTools.isNonEmpty( action ) )
				{
					throw new IOException( "Can't set action for browse button" );
				}

				if ( hasStyles )
				{
					throw new IOException( "Can't apply styles to browse button" );
				}

				formFactory.writeBrowseButton( iw, text );
				break;

			case IMAGE:
				if ( TextTools.isNonEmpty( action ) )
				{
					throw new IOException( "Can't set action for image button" );
				}

				if ( hasStyles )
				{
					throw new IOException( "Can't apply styles to image button" );
				}

				formFactory.writeSubmitImage( contextPath, iw, name, text, null );
				break;
		}
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		generate( contextPath, iw, formFactory );
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
		out.append( "<<" ).append( getText() ).append( ">> " );
	}
}
