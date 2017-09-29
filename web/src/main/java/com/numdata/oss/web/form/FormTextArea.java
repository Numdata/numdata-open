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

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * Text area for web forms.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public class FormTextArea
extends FormField
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FormTextArea.class );

	/**
	 * Number of columns in text area.
	 */
	private int _columns;

	/**
	 * Number of rows in text area.
	 */
	private int _minimumRows;

	/**
	 * Maximum number of rows in text area (-1 => unbound).
	 */
	private int _maximumRows = -1;

	/**
	 * Set value to {@code null} if it is empty.
	 *
	 * @see TextTools#isEmpty
	 */
	private boolean _nullIfEmpty = false;

	/**
	 * Construct text area.
	 *
	 * @param target Target object of field.
	 */
	public FormTextArea( final FieldTarget target )
	{
		this( target, -1, -1 );
	}

	/**
	 * Construct text area with the specified number of rows and columns.
	 *
	 * @param target      Target object of field.
	 * @param minimumRows Minimum number of rows (-1 => unbound).
	 * @param columns     Number of columns (-1 => unspecified).
	 */
	public FormTextArea( final FieldTarget target, final int minimumRows, final int columns )
	{
		super( target );
		_minimumRows = minimumRows;
		_columns = columns;
		_columns = columns;
	}

	/**
	 * Set the number of columns in text area (-1 => unspecified).
	 *
	 * @param columns Number of columns
	 */
	public void setCols( final int columns )
	{
		_columns = columns;
	}

	/**
	 * Get minimum number of rows in text area (-1 => unbound).
	 *
	 * @return Minimum number of rows
	 */
	public int getRows()
	{
		return _minimumRows;
	}

	/**
	 * Set the minimum number of rows in text area (-1 => unbound).
	 *
	 * @param rows Minimum number of rows
	 */
	public void setRows( final int rows )
	{
		_minimumRows = rows;
	}

	/**
	 * Get maximum number of rows in text area (zero or less => unbound).
	 *
	 * @return Maximum number of rows.
	 */
	public int getMaximumRows()
	{
		return _maximumRows;
	}

	/**
	 * Set maximum number of rows in text area (zero or less => unbound).
	 *
	 * @param rows Maximum number of rows.
	 */
	public void setMaximumRows( final int rows )
	{
		_maximumRows = rows;
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

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		final String value = getValue();

		int rows = Math.max( _minimumRows, getLineCount( value ) );

		final int maximumRows = _maximumRows;
		if ( maximumRows > 0 )
		{
			rows = Math.min( rows, maximumRows );
		}

		formFactory.writeTextArea( iw, isEditable(), getName(), value, _columns, rows, null );
	}

	/**
	 * Get number of lines in the given string.
	 *
	 * @param value String value.
	 *
	 * @return Number of lines in string (0 if {@code null}; >=1 otherwise).
	 */
	public static int getLineCount( @Nullable final String value )
	{
		int result = 0;

		if ( value != null )
		{
			int pos = 0;
			do
			{
				result++;
				pos = value.indexOf( '\n', pos ) + 1;
			}
			while ( pos > 0 );
		}

		return result;
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
				setValue( ( isNullIfEmpty() && TextTools.isEmpty( value ) ) ? null : value );

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
