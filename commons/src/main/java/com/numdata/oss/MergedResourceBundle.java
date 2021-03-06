/*
 * Copyright (c) 2006-2020, Numdata BV, The Netherlands.
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

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This implementation of {@link ResourceBundle} merges the contents of existing
 * bundles into a new one.
 *
 * @author Peter S. Heijnen
 * @see ResourceBundleTools#getBundleHierarchy
 */
public class MergedResourceBundle
extends ResourceBundle
{
	/**
	 * Locale for which the bundle is created.
	 */
	@NotNull
	private final Locale _locale;

	/**
	 * Resources in this bundle.
	 */
	@NotNull
	private final Map<String, Object> _contents = new HashMap<>();

	/**
	 * Base name of this bundle, if known, or {@code null} to use {@link ResourceBundle#getBaseBundleName()}.
	 */
	@Nullable
	private final String _baseBundleName;

	/**
	 * Create merged bundle.
	 *
	 * @param baseBundleName Base name of this bundle (optional).
	 * @param locale         Locale for which the bundle is created.
	 */
	public MergedResourceBundle( @Nullable final String baseBundleName, @NotNull final Locale locale )
	{
		_baseBundleName = baseBundleName;
		_locale = locale;
	}

	/**
	 * Create merged bundle.
	 *
	 * @param locale Locale for which the bundle is created.
	 */
	public MergedResourceBundle( @NotNull final Locale locale )
	{
		this( null, locale );
	}

	/**
	 * Create merged bundle.
	 *
	 * @param baseBundleName Base name of this bundle (optional).
	 * @param locale         Locale for which the bundle is created.
	 * @param bundles        Source bundles.
	 */
	@SuppressWarnings( "OverridableMethodCallDuringObjectConstruction" )
	public MergedResourceBundle( @Nullable final String baseBundleName, @NotNull final Locale locale, @NotNull final ResourceBundle... bundles )
	{
		this( baseBundleName, locale );

		for ( final ResourceBundle bundle : bundles )
		{
			addBundle( bundle );
		}
	}

	/**
	 * Create merged bundle.
	 *
	 * @param locale  Locale for which the bundle is created.
	 * @param bundles Source bundles.
	 */
	@SuppressWarnings( "OverridableMethodCallDuringObjectConstruction" )
	public MergedResourceBundle( @NotNull final Locale locale, @NotNull final ResourceBundle... bundles )
	{
		this( null, locale, bundles );
	}

	@Override
	@Nullable
	public String getBaseBundleName()
	{
		return ( _baseBundleName != null ) ? _baseBundleName : super.getBaseBundleName();
	}

	/**
	 * Add bundle to this bundle.
	 *
	 * @param bundle Bundle to add.
	 */
	public void addBundle( @NotNull final ResourceBundle bundle )
	{
		final Map<String, Object> contents = _contents;

		for ( final Enumeration<String> en = bundle.getKeys(); en.hasMoreElements(); )
		{
			final String key = en.nextElement();
			contents.put( key, bundle.getObject( key ) );
		}
	}

	@Override
	public Object handleGetObject( @NotNull final String key )
	{
		return _contents.get( key );
	}

	@NotNull
	@Override
	public Enumeration<String> getKeys()
	{
		return new CombinedBundleEnumeration( parent, _contents.keySet() );
	}

	@Override
	public @NotNull Locale getLocale()
	{
		return _locale;
	}
}
