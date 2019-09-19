/*
 * Copyright (c) 2008-2018, Numdata BV, The Netherlands.
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
import com.numdata.oss.log.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * Text field for web forms.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public class FormTextField
extends FormField
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FormTextField.class );

	/**
	 * Visible size of field (-1 =&gt; unspecified).
	 */
	private int _size = -1;

	/**
	 * Maximum number of characters in field (-1 =&gt; unspecified).
	 */
	private int _maxlength = -1;

	/**
	 * Field is a password vs. regular text field.
	 */
	private boolean _password = false;

	/**
	 * Disable autocomplete function in user agent.
	 */
	private boolean _disableAutocomplete = false;

	/**
	 * Allow or disallow empty values to be sumitted.
	 *
	 * @see TextTools#isEmpty
	 */
	private boolean _allowEmpty = true;

	/**
	 * Set value to {@code null} if it is empty.
	 *
	 * @see TextTools#isEmpty
	 */
	private boolean _nullIfEmpty = false;

	/**
	 * Whether whitespace characters are removed from the start and end of the
	 * value.
	 */
	private boolean _trimWhitespace = true;

	/**
	 * Placeholder text.
	 */
	@Nullable
	private String _placeholder = null;

	/**
	 * Construct text field.
	 *
	 * @param target Target object of field.
	 */
	public FormTextField( final FieldTarget target )
	{
		this( target, -1, -1, false );
	}

	/**
	 * Construct text field.
	 *
	 * @param target Target object of field.
	 * @param size   Visable size of field (-1 =&gt; unspecified).
	 */
	public FormTextField( final FieldTarget target, final int size )
	{
		this( target, size, -1, false );
	}

	/**
	 * Construct text field.
	 *
	 * @param target    Target object of field.
	 * @param size      Visable size of field (-1 =&gt; unspecified).
	 * @param maxLength Maximum number of characters in field (-1 =&gt;
	 *                  unspecified).
	 */
	public FormTextField( final FieldTarget target, final int size, final int maxLength )
	{
		this( target, size, maxLength, false );
	}

	/**
	 * Construct text field.
	 *
	 * @param target    Target object of field.
	 * @param size      Visable size of field (-1 =&gt; unspecified).
	 * @param maxLength Maximum number of characters in field (-1 =&gt;
	 *                  unspecified).
	 * @param password  Field is a password vs. regular text field.
	 */
	public FormTextField( final FieldTarget target, final int size, final int maxLength, final boolean password )
	{
		super( target );
		setSize( size );
		setMaxLength( maxLength );
		setPassword( password );
	}

	/**
	 * Get password option.
	 *
	 * @return {@code true} if field is a password field; {@code false} if field
	 * is a regular text field.
	 */
	public boolean isPassword()
	{
		return _password;
	}

	/**
	 * Set password option.
	 *
	 * @param password Field is a password vs. regular text field.
	 */
	public void setPassword( final boolean password )
	{
		_password = password;
	}

	/**
	 * Get the visible size of field.
	 *
	 * @return Visible size of field; -1 if undefined.
	 */
	public int getSize()
	{
		return _size;
	}

	/**
	 * Set the visible size of field.
	 *
	 * @param size Visible size of field; -1 if undefined.
	 */
	public void setSize( final int size )
	{
		_size = size;
	}

	/**
	 * Get the maximum number of characters in field.
	 *
	 * @return Maximum number of characters in field.
	 */
	public int getMaxLength()
	{
		return _maxlength;
	}

	/**
	 * Set the maximum number of characters in field.
	 *
	 * @param length Maximum number of characters in field.
	 */
	public void setMaxLength( final int length )
	{
		_maxlength = length;
	}

	/**
	 * Test whether autocomplete is disabled (enabled by default).
	 *
	 * @return {@code true} if autocomplete is disabled; {@code false} if
	 * autocomplete is enabled (default).
	 */
	public boolean isDisableAutocomplete()
	{
		return _disableAutocomplete;
	}

	/**
	 * Set autocomplete to disabled (enabled by default).
	 *
	 * @param disabled disable vs. enable autocomplete.
	 */
	public void setDisableAutocomplete( final boolean disabled )
	{
		_disableAutocomplete = disabled;
	}

	/**
	 * Returns whether empty values are allowed.
	 *
	 * @return {@code true} if empty values are allowed (default); {@code false}
	 * if empty values are disallowed;
	 *
	 * @see TextTools#isEmpty
	 */
	public boolean isAllowEmpty()
	{
		return _allowEmpty;
	}

	/**
	 * Allow empty value to be sumitted (this is allowed by default).
	 *
	 * @param allowEmpty {@code true} to allow empty values (default); {@code
	 *                   false} to disallow empty values.
	 *
	 * @see TextTools#isEmpty
	 */
	public void setAllowEmpty( final boolean allowEmpty )
	{
		_allowEmpty = allowEmpty;
	}

	/**
	 * Returns whether empty values are parsed as {@code null}.
	 *
	 * @return {@code true} if empty values result in {@code null}.
	 */
	public boolean isNullIfEmpty()
	{
		return _nullIfEmpty;
	}

	/**
	 * Sets whether empty values are parsed as {@code null}.
	 *
	 * @param nullIfEmpty {@code true} to parse empty values as {@code null}.
	 */
	public void setNullIfEmpty( final boolean nullIfEmpty )
	{
		_nullIfEmpty = nullIfEmpty;
	}

	/**
	 * Returns whether whitespace characters are removed from the start and end
	 * of the value.
	 *
	 * @return {@code true} if whitespaces are trimmed.
	 */
	public boolean isTrimWhitespace()
	{
		return _trimWhitespace;
	}

	/**
	 * Sets whether whitespace characters are removed from the start and end of
	 * the value.
	 *
	 * @param trimWhitespace {@code true} to trim whitespaces.
	 */
	public void setTrimWhitespace( final boolean trimWhitespace )
	{
		_trimWhitespace = trimWhitespace;
	}

	@Nullable
	public String getPlaceholder()
	{
		return _placeholder;
	}

	public void setPlaceholder( @Nullable final String placeholder )
	{
		_placeholder = placeholder;
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		final String placeholder = getPlaceholder();
		final Map<String, String> attributes = TextTools.isEmpty( placeholder ) ? null : Collections.singletonMap( "placeholder", placeholder );

		//noinspection ThrowableResultOfMethodCallIgnored
		formFactory.writeTextField( iw, isEditable(), getName(), getName(), getValue(), getSize(), getMaxLength(), isPassword(), isDisableAutocomplete(), getException() != null, attributes );
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
				if ( isTrimWhitespace() ? TextTools.isEmpty( value ) : value.isEmpty() )
				{
					if ( !isAllowEmpty() )
					{
						throw new InvalidFormDataException( InvalidFormDataException.Type.INFO_MISSING );
					}

					setValue( isNullIfEmpty() ? null : isTrimWhitespace() ? value.trim() : value );
				}
				else
				{
					setValue( isTrimWhitespace() ? value.trim() : value );
				}

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

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		final String value = getValue();
		if ( value != null )
		{
			out.append( value );
		}
		out.append( ' ' );
	}
}
