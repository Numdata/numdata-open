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
import java.math.*;
import java.text.*;
import javax.servlet.http.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * Field for numbers on web forms.
 *
 * @author Peter S. Heijnen
 */
public class FormNumberField
extends FormField
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FormNumberField.class );

	/**
	 * Maximum number of fraction digits (0 =&gt; integer only).
	 */
	private final int _maximumFractionDigits;

	/**
	 * Minimum value ({@code null} =&gt; don't care, 0 =&gt; positive only).
	 */
	private BigDecimal _minValue;

	/**
	 * Maximum value ({@code null} =&gt; don't care).
	 */
	private BigDecimal _maxValue;

	/**
	 * Set value to {@code null} if it is empty.
	 *
	 * @see TextTools#isEmpty
	 */
	private boolean _nullIfEmpty = false;

	/**
	 * Construct text field.
	 *
	 * @param target                Target object of field.
	 * @param maximumFractionDigits Maximum number of fraction digits (0 =&gt;
	 *                              integer only).
	 * @param minValue              Minimum value ({@code null} =&gt; don't care, 0
	 *                              =&gt; positive only).
	 * @param maxValue              Maximum value ({@code null} =&gt; don't care).
	 */
	public FormNumberField( @NotNull final FieldTarget target, final int maximumFractionDigits, @Nullable final BigDecimal minValue, @Nullable final BigDecimal maxValue )
	{
		super( target );

		_maximumFractionDigits = maximumFractionDigits;
		_minValue = minValue;
		_maxValue = maxValue;
	}

	/**
	 * Get minimum value.
	 *
	 * @return Minimum value; {@code null} =&gt; don't care; {@code 0.0} =&gt; positive
	 * only.
	 */
	public BigDecimal getMinValue()
	{
		return _minValue;
	}

	/**
	 * Set minimum value.
	 *
	 * @param minValue Minimum value ({@code null} =&gt; don't care; {@code 0.0} =&gt;
	 *                 positive only).
	 */
	public void setMinValue( final BigDecimal minValue )
	{
		_minValue = minValue;
	}

	/**
	 * Get maximum value.
	 *
	 * @return Maximum value; {@code null} =&gt; don't care.
	 */
	public BigDecimal getMaxValue()
	{
		return _maxValue;
	}

	/**
	 * Set maximum value.
	 *
	 * @param maxValue Maximum value ({@code null} =&gt; don't care).
	 */
	public void setMaxValue( final BigDecimal maxValue )
	{
		_maxValue = maxValue;
	}

	@Nullable
	@Override
	public String getValue()
	{
		return format( getNumberValue() );
	}

	@Override
	public void setValue( @Nullable final String value )
	{
		final BigDecimal number = parse( value );
		if ( number != null )
		{
			final BigDecimal minValue = getMinValue();
			if ( ( minValue != null ) && BigDecimalTools.isLess( number, minValue ) )
			{
				throw new IllegalArgumentException( "Value (" + number + ") exceeds minimum (" + minValue + ')' );
			}

			final BigDecimal maxValue = getMaxValue();
			if ( ( maxValue != null ) && BigDecimalTools.isGreater( number, maxValue ) )
			{
				throw new IllegalArgumentException( "Value (" + number + ") exceeds maximum (" + maxValue + ')' );
			}
		}

		super.setValue( ( number != null ) ? number.toPlainString() : null );
	}

	/**
	 * Get value as {@link Number}.
	 *
	 * @return Value as {@link Number}.
	 */
	@Nullable
	public BigDecimal getNumberValue()
	{
		return parse( super.getValue() );
	}


	/**
	 * Set value as {@link Number}.
	 *
	 * @param number Value as {@link Number}.
	 */
	public void setNumberValue( @Nullable final Number number )
	{
		setValue( ( number != null ) ? number.toString() : null );
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
		final String name = getName();

		@SuppressWarnings( "ThrowableResultOfMethodCallIgnored" ) final boolean invalid = getException() != null;

		formFactory.writeTextField( iw, isEditable(), name, name, getValue(), 10, 20, false, false, invalid, null );
	}


	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	throws InvalidFormDataException
	{
		final SubmitStatus result;

		if ( isEditable() )
		{
			final String stringValue = request.getParameter( getName() );
			if ( stringValue != null )
			{
				if ( TextTools.isNonEmpty( stringValue ) )
				{
					final BigDecimal value;
					try
					{
						value = parse( stringValue );
					}
					catch ( final IllegalArgumentException ignored )
					{
						throw new InvalidFormDataException( stringValue, InvalidFormDataException.Type.INVALID_NUMBER );
					}

					final BigDecimal minValue = getMinValue();
					if ( ( minValue != null ) && BigDecimalTools.isLess( value, minValue ) )
					{
						throw new InvalidFormDataException( value + " < " + minValue, InvalidFormDataException.Type.INVALID_NUMBER );
					}

					final BigDecimal maxValue = getMaxValue();
					if ( ( maxValue != null ) && BigDecimalTools.isGreater( value, maxValue ) )
					{
						throw new InvalidFormDataException( value + " > " + maxValue, InvalidFormDataException.Type.INVALID_NUMBER );
					}

					setNumberValue( value );
				}
				else if ( isNullIfEmpty() )
				{
					setNumberValue( null );
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

	/**
	 * Get number format for this field.
	 *
	 * @return Number format for this field.
	 */
	@NotNull
	protected NumberFormat getNumberFormat()
	{
		return TextTools.getNumberFormat( getLocale(), 0, _maximumFractionDigits, false );
	}

	/**
	 * Format number.
	 *
	 * @param number Number to format.
	 *
	 * @return String with formatted number; empty string if {@code number} is
	 * {@code null}.
	 */
	protected String format( @Nullable final Number number )
	{
		return ( number != null ) ? getNumberFormat().format( number ) : "";
	}

	/**
	 * Parse string and return as {@link BigDecimal} instance.
	 *
	 * @param value String to parse.
	 *
	 * @return {@link BigDecimal instance}; {@code null} if string is {@code
	 * null} or empty.
	 *
	 * @throws IllegalArgumentException if the string could not be parsed.
	 */
	@Nullable
	protected BigDecimal parse( @Nullable final String value )
	{
		BigDecimal result;

		if ( value == null )
		{
			result = null;
		}
		else
		{
			final String trimmedValue = value.trim();
			if ( trimmedValue.isEmpty() )
			{
				result = null;
			}
			else
			{
				try
				{
					result = BigDecimalTools.parse( getNumberFormat(), trimmedValue );
				}
				catch ( final NumberFormatException e )
				{
					try
					{
						result = new BigDecimal( trimmedValue );
					}
					catch ( final NumberFormatException ignored )
					{
						throw e;
					}
				}

				result = BigDecimalTools.limitScale( result, _maximumFractionDigits );
			}
		}

		return result;
	}

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		out.append( format( getNumberValue() ) ).append( ' ' );
	}
}
