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
import java.text.*;
import java.util.*;
import javax.servlet.http.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * Date field for web forms.
 *
 * @author G.B.M. Rupert
 */
public class FormDateField
extends FormField
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FormDateField.class );

	/**
	 * Locale to use for localizing messages.
	 */
	private final Locale _locale;

	/**
	 * Date format to use.
	 */
	@NotNull
	private SimpleDateFormat _dateFormat;

	/**
	 * Earliest date value allowed.
	 */
	private Date _earliestAllowedDate;

	/**
	 * Latest date value allowed.
	 */
	private Date _lastestAllowedDate;

	/**
	 * Construct new form date field.
	 *
	 * @param target     Target object for this field.
	 * @param locale     Locale to use for localizing messages.
	 * @param dateFormat Date format to use.
	 */
	public FormDateField( @NotNull final FieldTarget target, @NotNull final Locale locale, @NotNull final SimpleDateFormat dateFormat )
	{
		super( target );

		_locale = locale;
		_dateFormat = dateFormat;
		_earliestAllowedDate = null;
		_lastestAllowedDate = null;
	}

	/**
	 * Get data format used.
	 *
	 * @return Date format used.
	 */
	@NotNull
	public SimpleDateFormat getDateFormat()
	{
		return _dateFormat;
	}

	/**
	 * Set data format used.
	 *
	 * @param dateFormat Date format used.
	 */
	public void setDateFormat( @NotNull final SimpleDateFormat dateFormat )
	{
		_dateFormat = dateFormat;
	}

	/**
	 * Get earliest date value allowed.
	 *
	 * @return Earliest date value allowed; {@code null} if no limit is set.
	 */
	@Nullable
	public Date getEarliestAllowedDate()
	{
		return _earliestAllowedDate;
	}

	/**
	 * Get earliest date value allowed.
	 *
	 * @param date Earliest date value allowed ({@code null} to set no limit).
	 */
	public void setEarliestAllowedDate( @Nullable final Date date )
	{
		_earliestAllowedDate = ( date != null ) ? (Date)date.clone() : null;
	}

	/**
	 * Get latest date value allowed.
	 *
	 * @return Latest date value allowed; {@code null} if no limit is set.
	 */
	@Nullable
	public Date getLastestAllowedDate()
	{
		return _lastestAllowedDate;
	}

	/**
	 * Get latest date value allowed.
	 *
	 * @param date Latest date value allowed ({@code null} to set no limit).
	 */
	public void setLastestAllowedDate( @Nullable final Date date )
	{
		_lastestAllowedDate = ( date != null ) ? (Date)date.clone() : null;
	}

	/**
	 * Get value as {@link Date} object.
	 *
	 * @return Value as {@link Date} object.
	 */
	@Nullable
	public Date getDateValue()
	{
		Date result = null;

		final String value = getValue();
		if ( value != null )
		{
			try
			{
				result = new Date( Long.parseLong( value ) );
			}
			catch ( final NumberFormatException e )
			{
				/* ignored */
			}
		}

		return result;
	}

	/**
	 * Set value as {@link Date} object.
	 *
	 * @param value Value as {@link Date} object.
	 */
	public void setDateValue( @Nullable final Date value )
	{
		setValue( value != null ? Long.toString( value.getTime() ) : null );
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		final Date value = getDateValue();
		final SimpleDateFormat dateFormat = getDateFormat();
		final String stringValue = ( value == null ) ? "" : dateFormat.format( value );
		final String dateFormatPattern = dateFormat.toPattern();
		final int maxLength = dateFormatPattern.length();

		formFactory.writeTextField( iw, isEditable(), getName(), null, stringValue, maxLength, maxLength, false, false, false, null );
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
				if ( TextTools.isNonEmpty( value ) )
				{
					final Locale locale = _locale;
					final SimpleDateFormat dateFormat = _dateFormat;

					final Date date;
					try
					{
						date = dateFormat.parse( value );
					}
					catch ( final ParseException e )
					{
						final ResourceBundle bundle = ResourceBundleTools.getBundleHierarchy( getClass(), locale );
						//noinspection ThrowInsideCatchBlockWhichIgnoresCaughtException
						throw new InvalidFormDataException( ResourceBundleTools.format( bundle, "malformedDate", value, dateFormat.toPattern() ), InvalidFormDataException.Type.INVALID_DATE );
					}

					final Date earliestAllowedDate = _earliestAllowedDate;
					if ( ( earliestAllowedDate != null ) && date.before( earliestAllowedDate ) )
					{
						final ResourceBundle bundle = ResourceBundleTools.getBundleHierarchy( getClass(), locale );
						throw new InvalidFormDataException( ResourceBundleTools.format( bundle, "beforeEarliestAllowedDate", value, dateFormat.format( earliestAllowedDate ) ), InvalidFormDataException.Type.INVALID_DATE );
					}

					final Date lastestAllowedDate = _lastestAllowedDate;
					if ( ( lastestAllowedDate != null ) && date.after( lastestAllowedDate ) )
					{
						final ResourceBundle bundle = ResourceBundleTools.getBundleHierarchy( getClass(), locale );
						throw new InvalidFormDataException( ResourceBundleTools.format( bundle, "afterLatestAllowedDate", value, dateFormat.format( lastestAllowedDate ) ), InvalidFormDataException.Type.INVALID_DATE );
					}

					setDateValue( date );
				}
				else
				{
					setDateValue( null );
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
		final Date dateValue = getDateValue();
		if ( dateValue != null )
		{
			final SimpleDateFormat dateFormat = getDateFormat();
			out.append( dateFormat.format( dateValue ) );
		}
		out.append( ' ' );
	}
}
