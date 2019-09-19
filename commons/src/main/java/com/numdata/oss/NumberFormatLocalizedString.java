/*
 * Copyright (c) 2013-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss;

import java.text.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Number formatted in a locale-specific manner according to a specified format.
 *
 * @author G. Meinders
 */
public class NumberFormatLocalizedString
extends AbstractLocalizableString
{
	/**
	 * Number to be formatted.
	 */
	private final Object _value;

	/**
	 * Minimum number of fraction digits.
	 */
	private final int _minimumFractionDigits;

	/**
	 * Maximum number of fraction digits.
	 */
	private final int _maximumFractionDigits;

	/**
	 * Use grouping of digits or not.
	 */
	private final boolean _groupingUsed;

	/**
	 * Constructs a new instance.
	 *
	 * @param value                 Number to be formatted.
	 * @param minimumFractionDigits Minimum number of fraction digits.
	 * @param maximumFractionDigits Maximum number of fraction digits.
	 * @param groupingUsed          Use grouping of digits or not.
	 */
	public NumberFormatLocalizedString( final Number value, final int minimumFractionDigits, final int maximumFractionDigits, final boolean groupingUsed )
	{
		_value = value;
		_minimumFractionDigits = minimumFractionDigits;
		_maximumFractionDigits = maximumFractionDigits;
		_groupingUsed = groupingUsed;
	}

	@Nullable
	@Override
	public String get( @Nullable final Locale locale )
	{
		final Locale actualLocale = locale == null ? Locale.ROOT : locale;
		final NumberFormat numberFormat = TextTools.getNumberFormat( actualLocale, _minimumFractionDigits, _maximumFractionDigits, _groupingUsed );
		return numberFormat.format( _value );
	}
}
