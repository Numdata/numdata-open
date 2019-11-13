/*
 * Copyright (c) 2019-2019, Numdata BV, The Netherlands.
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
 * Internationalization tool class that can be used in expression libraries
 * template engines like Apache Velocity.
 *
 * @author Peter S. Heijnen
 */
public class I18NTool
{
	/**
	 * Locale.
	 */
	@NotNull
	private Locale _locale;

	/**
	 * Construct tool with default locale.
	 */
	public I18NTool()
	{
		this( Locale.getDefault() );
	}

	/**
	 * Construct tool with given locale.
	 *
	 * @param locale Locale to use.
	 */
	public I18NTool( @NotNull final Locale locale )
	{
		_locale = locale;
	}

	@NotNull
	public Locale getLocale()
	{
		return _locale;
	}

	public void setLocale( @NotNull final Locale locale )
	{
		_locale = locale;
	}

	/**
	 * Get resource bundle for the specified object's class hierarchy.
	 *
	 * @param object Object to get the bundle for.
	 *
	 * @return ResourceBundle for the specified source.
	 *
	 * @throws MissingResourceException if no resource bundle for the specified
	 * class can be found.
	 */
	@NotNull
	public ResourceBundle getBundle( @NotNull final Object object )
	{
		return getBundle( object.getClass() );
	}

	/**
	 * Get resource bundle for the specified class (hierarchy).
	 *
	 * @param clazz Class to get the bundle for.
	 *
	 * @return ResourceBundle for the specified source.
	 *
	 * @throws MissingResourceException if no resource bundle for the specified
	 * class can be found.
	 */
	@NotNull
	public ResourceBundle getBundle( @NotNull final Class<?> clazz )
	{
		return ResourceBundleTools.getBundleHierarchy( clazz, getLocale() );
	}

	/**
	 * Get string from resource bundle for the given object.
	 *
	 * @param object Object to get the translation for.
	 * @param key    Key in resource bundle.
	 *
	 * @return String from resource bundle.
	 *
	 * @throws MissingResourceException if translation was not found.
	 */
	@NotNull
	public String translate( @NotNull final Object object, @NotNull final String key )
	{
		return getBundle( object ).getString( key );
	}

	/**
	 * Get string from resource bundle for the given class.
	 *
	 * @param clazz Class to get the translation for.
	 * @param key   Key in resource bundle.
	 *
	 * @return String from resource bundle.
	 *
	 * @throws MissingResourceException if translation was not found.
	 */
	@NotNull
	public String translate( @NotNull final Class<?> clazz, @NotNull final String key )
	{
		return getBundle( clazz ).getString( key );
	}

	/**
	 * Get string from resource for an enumerate type.
	 *
	 * @param enumerate Enumerate value.
	 *
	 * @return String from resource bundle, or default value if undefined.
	 */
	@NotNull
	public String translate( @NotNull final Enum<?> enumerate )
	{
		return ResourceBundleTools.getString( getLocale(), enumerate );
	}

	/**
	 * Format number.
	 *
	 * @param number         Number to format.
	 * @param fractionDigits Number of fraction digits.
	 *
	 * @return Formatted number.
	 */
	@NotNull
	public String formatNumber( @NotNull final Number number, final int fractionDigits )
	{
		return formatNumber( number, fractionDigits, fractionDigits );
	}

	/**
	 * Format number.
	 *
	 * @param number                Number to format.
	 * @param minimumFractionDigits Minimum number of fraction digits.
	 * @param maximumFractionDigits Maximum number of fraction digits.
	 *
	 * @return Formatted number.
	 */
	@NotNull
	public String formatNumber( @NotNull final Number number, final int minimumFractionDigits, final int maximumFractionDigits )
	{
		return TextTools.getNumberFormat( getLocale(), minimumFractionDigits, maximumFractionDigits, false ).format( number );
	}

	/**
	 * Format date object in short date format.
	 *
	 * @param date Date object to format (may be a {@link Date} or {@link Calendar}).
	 *
	 * @return Formatted date; {@code null} if {@code date} is {@code null}.
	 */
	@Contract( "null -> null; !null -> !null" )
	@Nullable
	public String formatDate( @Nullable final Object date )
	{
		return ( date != null ) ? formatDate( DateFormat.getDateInstance( DateFormat.SHORT, getLocale() ), date ) : null;
	}

	/**
	 * Format date object in short date/time format.
	 *
	 * @param date Date object to format (may be a {@link Date} or {@link Calendar}).
	 *
	 * @return Formatted date; {@code null} if {@code date} is {@code null}.
	 */
	@Contract( "null -> null; !null -> !null" )
	@Nullable
	public String formatDateTime( @Nullable final Object date )
	{
		return ( date != null ) ? formatDate( DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, getLocale() ), date ) : null;
	}

	/**
	 * Format date object in short time format.
	 *
	 * @param date Date object to format (may be a {@link Date} or {@link Calendar}).
	 *
	 * @return Formatted date; {@code null} if {@code date} is {@code null}.
	 */
	@Contract( "null -> null; !null -> !null" )
	@Nullable
	public String formatTime( @Nullable final Object date )
	{
		return ( date != null ) ? formatDate( DateFormat.getTimeInstance( DateFormat.SHORT, getLocale() ), date ) : null;
	}

	/**
	 * Format date object using the given {@link SimpleDateFormat date format pattern}.
	 *
	 * @param pattern {@link SimpleDateFormat date format pattern}.
	 * @param date    Date object to format (may be a {@link Date} or {@link Calendar}).
	 *
	 * @return Formatted date; {@code null} if {@code date} is {@code null}.
	 */
	@Contract( "_,null -> null; _,!null -> !null" )
	@Nullable
	public String formatDate( @NotNull final String pattern, @Nullable final Object date )
	{
		return ( date != null ) ? formatDate( new SimpleDateFormat( pattern, getLocale() ), date ) : null;
	}

	/**
	 * Format date object using the given {@link DateFormat}.
	 *
	 * @param format {@link DateFormat} to use.
	 * @param date   Date object to format (may be a {@link Date} or {@link Calendar}).
	 *
	 * @return Formatted date; {@code null} if {@code date} is {@code null}.
	 */
	@Contract( "_,null -> null; _,!null -> !null" )
	@Nullable
	protected String formatDate( @NotNull final DateFormat format, @Nullable final Object date )
	{
		return ( date != null ) ? format.format( ( date instanceof Calendar ) ? ( (Calendar)date ).getTime() : date ) : null;
	}
}
