/*
 * Copyright (c) 2011-2020, Numdata BV, The Netherlands.
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

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Tool class that can be used in template engines like Apache Velocity.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "JavaDoc" )
public final class TextTool
{
	public String getFixed( final int length, final char fillChar )
	{
		return TextTools.getFixed( length, fillChar );
	}

	public String getFixed( final String source, final int length, final boolean rightAligned, final char fillChar )
	{
		return TextTools.getFixed( source, length, rightAligned, fillChar );
	}

	public String getTrimmedSubstring( final String source, final int start, final int end )
	{
		return TextTools.getTrimmedSubstring( source, start, end );
	}

	public boolean isEmpty( final CharSequence string )
	{
		return TextTools.isEmpty( string );
	}

	public boolean isNonEmpty( final CharSequence string )
	{
		return TextTools.isNonEmpty( string );
	}

	public boolean startsWithIgnoreCase( @Nullable final String string, @Nullable final String prefix )
	{
		return TextTools.startsWithIgnoreCase( string, prefix );
	}

	public boolean endsWithIgnoreCase( @Nullable final String string, @Nullable final String suffix )
	{
		return TextTools.endsWithIgnoreCase( string, suffix );
	}

	public String escape( final CharSequence source )
	{
		final StringBuilder sb = new StringBuilder();
		TextTools.escape( sb, source );
		return sb.toString();
	}

	public NumberFormat getCurrencyFormat( final Locale locale, final String symbol )
	{
		return TextTools.getCurrencyFormat( locale, symbol );
	}

	public DateFormat getDateFormat( final Locale locale )
	{
		return TextTools.getDateFormat( locale );
	}

	public DateFormat getDateTimeFormat( final Locale locale )
	{
		return TextTools.getDateTimeFormat( locale );
	}

	public String getLineSeparator()
	{
		return TextTools.getLineSeparator();
	}

	public String getList( final char separator, @NotNull final Object... objects )
	{
		return TextTools.getList( separator, objects );
	}

	public String getList( @Nullable final Iterable<?> list, final char separator )
	{
		return TextTools.getList( list, separator );
	}

	public String getList( final CharSequence separator, @NotNull final Object... objects )
	{
		return TextTools.getList( separator, objects );
	}

	public String getList( @Nullable final Iterable<?> list, @NotNull final CharSequence separator )
	{
		return TextTools.getList( list, separator );
	}

	public NumberFormat getNumberFormat( final Locale locale )
	{
		return TextTools.getNumberFormat( locale );
	}

	public NumberFormat getNumberFormat( final Locale locale, final int minimumFractionDigits, final int maximumFractionDigits, final boolean groupingUsed )
	{
		return TextTools.getNumberFormat( locale, minimumFractionDigits, maximumFractionDigits, groupingUsed );
	}

	public NumberFormat getPercentFormat( final Locale locale )
	{
		return TextTools.getPercentFormat( locale );
	}

	public List<String> readLines( @NotNull final URL url )
	throws IOException
	{
		return TextTools.readLines( url );
	}

	public List<String> readLines( @NotNull final File file )
	throws IOException
	{
		return TextTools.readLines( file );
	}

	public List<String> readLines( @NotNull final InputStream is, @NotNull final String charsetName )
	throws IOException
	{
		return TextTools.readLines( is, charsetName );
	}

	public List<String> readLines( final Reader reader )
	throws IOException
	{
		return TextTools.readLines( reader );
	}

	public String loadText( final URL url )
	{
		return TextTools.loadText( url );
	}

	public String loadText( @NotNull final File file )
	{
		return TextTools.loadText( file );
	}

	public String loadText( final InputStream is )
	throws IOException
	{
		return TextTools.loadText( is );
	}

	public String loadText( final Reader reader )
	throws IOException
	{
		return TextTools.loadText( reader );
	}

	public String quote( final Object source )
	{
		return TextTools.quote( source );
	}

	public String replace( @Nullable final String source, final char find, @Nullable final String replace )
	{
		return TextTools.replace( source, find, replace );
	}

	public String toHexString( final int value, final int digits, final boolean upperCase )
	{
		return TextTools.toHexString( value, digits, upperCase );
	}

	public String[] tokenize( final String source, final char separator )
	{
		return TextTools.tokenize( source, separator );
	}

	public List<String> tokenize( final String source, final char separator, final boolean trim )
	{
		return TextTools.tokenize( source, separator, trim );
	}

	public String unescape( final CharSequence source )
	{
		return TextTools.unescape( source );
	}

	public String capitalize( final CharSequence cs )
	{
		return TextTools.capitalize( cs );
	}

	public String decapitalize( final CharSequence cs )
	{
		return TextTools.decapitalize( cs );
	}

	public String wildcardPatternToRegex( @NotNull final String wildcardPattern )
	{
		return TextTools.wildcardPatternToRegex( wildcardPattern );
	}

	public String camelToUpperCase( final String value )
	{
		return TextTools.camelToUpperCase( value );
	}

	public String truncate( @Nullable final String string, final int length )
	{
		return TextTools.truncate( string, length );
	}
}
