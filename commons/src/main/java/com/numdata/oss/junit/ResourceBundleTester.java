/*
 * Copyright (c) 2004-2019, Numdata BV, The Netherlands.
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
package com.numdata.oss.junit;

import java.lang.reflect.*;
import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * JUnit unit tool class to help with testing resource bundles. It can be used
 * by calling {@code testBundles()} or (if needed) an instance can be created
 * and customized before calling {@link #run()}.
 *
 * Checks include resource bundle syntax and consistency amongst bundles.
 * Consistency checks include keys and special characters.
 *
 * TODO Check if values in different bundles are duplicates. Difficulty would be
 * finding out when duplicates are acceptable...
 *
 * @author D. van 't Oever
 * @author Peter S. Heijnen
 */
public class ResourceBundleTester
{
	/**
	 * Class to test resource bundles for.
	 */
	@Nullable
	private final Class<?> _forClass;

	/**
	 * {@code true} to include resource bundles for super-classes.
	 */
	private final boolean _includeHierarchy;

	/**
	 * Base name of resource bundles to test.
	 */
	@Nullable
	private final String _baseName;

	/**
	 * Minimum required locales.
	 */
	@NotNull
	private final Set<Locale> _locales = new LinkedHashSet<Locale>();

	/**
	 * Allow differences between locales.
	 */
	private boolean _allowLocaleDiffs = false;

	/**
	 * List of keys that are required in all bundles.
	 */
	@NotNull
	private final Set<String> _expectedKeys = new LinkedHashSet<String>();

	/**
	 * Allow unknown keys (not in expectedKeys) in bundles.
	 */
	private boolean _allowUnknown = true;

	/**
	 * Allow non-ASCII characters (>127) in values.
	 */
	private boolean _allowNonAscii = true;

	/**
	 * Allow HTML tags in values.
	 */
	private boolean _allowHTML = false;

	/**
	 * Constructs a new instance.
	 *
	 * @param forClass Class to test resource bundles for, include resource
	 *                 bundles for super-classes.
	 */
	public ResourceBundleTester( @Nullable final Class<?> forClass )
	{
		this( forClass, true );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param forClass         Class to test resource bundles for.
	 * @param includeHierarchy {@code true} to include resource bundles for
	 *                         super-classes.
	 */
	public ResourceBundleTester( @Nullable final Class<?> forClass, final boolean includeHierarchy )
	{
		_forClass = forClass;
		_includeHierarchy = includeHierarchy;
		_baseName = null;
	}

	/**
	 * Creates a new tester for the given class, including its class hierarchy,
	 * with expected keys initialized to all of its bean properties.
	 *
	 * @param forClass Class to test resource bundles for.
	 *
	 * @return Created tester.
	 */
	public static ResourceBundleTester forBeanClass( final Class<?> forClass )
	{
		final ResourceBundleTester result = new ResourceBundleTester( forClass, true );
		for ( final String expectedKey : BeanTools.getPropertyNames( forClass, false ) )
		{
			result.addExpectedKey( expectedKey );
		}
		return result;
	}

	public void addExpectedKey( final String expectedKey )
	{
		assertTrue( "Unnecessary expected key '" + expectedKey + '\'', _expectedKeys.add( expectedKey ) );
	}

	public void addExpectedKeysWithSuffix( final String suffix )
	{
		for ( final String expectedKey : new ArrayList<String>( _expectedKeys ) )
		{
			addExpectedKey( expectedKey + suffix );
		}
	}

	public void removeExpectedKey( final String expectedKey )
	{
		assertTrue( "Missing expected key '" + expectedKey + '\'', _expectedKeys.remove( expectedKey ) );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param baseName Base name of resource bundles to test.
	 */
	public ResourceBundleTester( @Nullable final String baseName )
	{
		_forClass = null;
		_includeHierarchy = false;
		_baseName = baseName;
	}

	@NotNull
	public Set<Locale> getLocales()
	{
		return Collections.unmodifiableSet( _locales );
	}

	public void setLocales( @NotNull final Collection<Locale> locales )
	{
		_locales.clear();
		_locales.addAll( locales );
	}

	public boolean isAllowLocaleDiffs()
	{
		return _allowLocaleDiffs;
	}

	public void setAllowLocaleDiffs( final boolean allowLocaleDiffs )
	{
		_allowLocaleDiffs = allowLocaleDiffs;
	}

	@NotNull
	public Set<String> getExpectedKeys()
	{
		return Collections.unmodifiableSet( _expectedKeys );
	}

	public void setExpectedKeys( @NotNull final Collection<String> expectedKeys )
	{
		_expectedKeys.clear();
		_expectedKeys.addAll( expectedKeys );
	}

	public boolean isAllowUnknown()
	{
		return _allowUnknown;
	}

	public void setAllowUnknown( final boolean allowUnknown )
	{
		_allowUnknown = allowUnknown;
	}

	public boolean isAllowNonAscii()
	{
		return _allowNonAscii;
	}

	public void setAllowNonAscii( final boolean allowNonAscii )
	{
		_allowNonAscii = allowNonAscii;
	}

	public boolean isAllowHTML()
	{
		return _allowHTML;
	}

	public void setAllowHTML( final boolean allowHTML )
	{
		_allowHTML = allowHTML;
	}

	/**
	 * Test resource bundles for a class.
	 *
	 * @param forClass         Class to test resource bundles for.
	 * @param includeHierarchy {@code true} to include resource bundles for
	 *                         super-classes.
	 * @param minimumLocales   List of minimum required locales.
	 * @param allowLocaleDiffs Allow differences between locales.
	 * @param expectedKeys     List of keys that are required in all bundles.
	 * @param allowUnknown     Allow unknown keys (not in expectedKeys) in
	 *                         bundles.
	 *
	 * @return List with unknown keys that were found in the resource bundle.
	 */
	public static List<String> testBundles( @NotNull final Class<?> forClass, final boolean includeHierarchy, @NotNull final Collection<Locale> minimumLocales, final boolean allowLocaleDiffs, @NotNull final Collection<String> expectedKeys, final boolean allowUnknown )
	{
		return testBundles( forClass, includeHierarchy, minimumLocales.toArray( new Locale[ minimumLocales.size() ] ), allowLocaleDiffs, expectedKeys, allowUnknown, true, false );
	}

	/**
	 * Test resource bundles for a class.
	 *
	 * @param forClass         Class to test resource bundles for.
	 * @param includeHierarchy {@code true} to include resource bundles for
	 *                         super-classes.
	 * @param minimumLocales   List of minimum required locales.
	 * @param allowLocaleDiffs Allow differences between locales.
	 * @param expectedKeys     List of keys that are required in all bundles.
	 * @param allowUnknown     Allow unknown keys (not in expectedKeys) in
	 *                         bundles.
	 * @param allowNonAscii    Allow non-ASCII characters (>127) in values.
	 * @param allowHTML        Allow HTML tags in values.
	 *
	 * @return List with unknown keys that were found in the resource bundle.
	 */
	public static List<String> testBundles( @NotNull final Class<?> forClass, final boolean includeHierarchy, @NotNull final Locale[] minimumLocales, final boolean allowLocaleDiffs, @Nullable final Collection<String> expectedKeys, final boolean allowUnknown, final boolean allowNonAscii, final boolean allowHTML )
	{
		return testBundles( forClass, includeHierarchy, Arrays.asList( minimumLocales ), allowLocaleDiffs, ( expectedKeys != null ) ? expectedKeys : Collections.<String>emptySet(), allowUnknown, allowNonAscii, allowHTML );
	}

	/**
	 * Test resource bundles for a class.
	 *
	 * @param forClass         Class to test resource bundles for.
	 * @param includeHierarchy {@code true} to include resource bundles for
	 *                         super-classes.
	 * @param locales          List of minimum required locales.
	 * @param allowLocaleDiffs Allow differences between locales.
	 * @param expectedKeys     List of keys that are required in all bundles.
	 * @param allowUnknown     Allow unknown keys (not in expectedKeys) in
	 *                         bundles.
	 * @param allowNonAscii    Allow non-ASCII characters (>127) in values.
	 * @param allowHTML        Allow HTML tags in values.
	 *
	 * @return List with unknown keys that were found in the resource bundle.
	 */
	public static List<String> testBundles( @NotNull final Class<?> forClass, final boolean includeHierarchy, @NotNull final Collection<Locale> locales, final boolean allowLocaleDiffs, @NotNull final Collection<String> expectedKeys, final boolean allowUnknown, final boolean allowNonAscii, final boolean allowHTML )
	{
		final ResourceBundleTester tester = new ResourceBundleTester( forClass, includeHierarchy );
		tester.setLocales( locales );
		tester.setAllowLocaleDiffs( allowLocaleDiffs );
		tester.setExpectedKeys( expectedKeys );
		tester.setAllowUnknown( allowUnknown );
		tester.setAllowNonAscii( allowNonAscii );
		tester.setAllowHTML( allowHTML );
		return tester.run();
	}

	/**
	 * Test resource bundles for a class.
	 *
	 * @param baseName         Base name of resource bundles to test.
	 * @param locales          List of locales to test.
	 * @param allowLocaleDiffs Allow differences between locales.
	 * @param expectedKeys     List of keys that are required in all bundles.
	 * @param allowUnknown     Allow unknown keys (not in expectedKeys) in
	 *                         bundles.
	 * @param allowNonAscii    Allow non-ASCII characters (>127) in values.
	 * @param allowHTML        Allow HTML tags in values.
	 *
	 * @return List with unknown keys that were found in the resource bundle.
	 */
	public static List<String> testBundles( @NotNull final String baseName, @NotNull final Collection<Locale> locales, final boolean allowLocaleDiffs, @NotNull final Collection<String> expectedKeys, final boolean allowUnknown, final boolean allowNonAscii, final boolean allowHTML )
	{
		final ResourceBundleTester tester = new ResourceBundleTester( baseName );
		tester.setLocales( locales );
		tester.setAllowLocaleDiffs( allowLocaleDiffs );
		tester.setExpectedKeys( expectedKeys );
		tester.setAllowUnknown( allowUnknown );
		tester.setAllowNonAscii( allowNonAscii );
		tester.setAllowHTML( allowHTML );
		return tester.run();
	}

	/**
	 * Tests the resource bundles.
	 *
	 * @return List with unknown keys that were found in the resource bundles.
	 */
	public List<String> run()
	{
		if ( _forClass != null )
		{
			System.out.println( " - class         : " + _forClass.getName() );
			System.out.println( " - hierarchy     : " + _includeHierarchy );
		}
		else
		{
			System.out.println( " - baseName      : " + _baseName );
		}

		System.out.println( " - tried locales : " + _locales );
		if ( _locales.isEmpty() )
		{
			throw new Error( "error in test: no locales specified to test" );
		}

		final Set<String> expectedKeys = _expectedKeys;
		final boolean allowUnknown = _allowUnknown;
		if ( !allowUnknown && expectedKeys.isEmpty() )
		{
			throw new Error( "error in test: no expected keys and no unknown keys allowed!?" );
		}

		final StringBuilder errors = new StringBuilder();
		final Map<Locale, ResourceBundle> bundlesByLocale = new LinkedHashMap<Locale, ResourceBundle>();

		for ( final Locale locale : _locales )
		{
			try
			{
				final ResourceBundle bundle;
				if ( _forClass != null )
				{
					if ( _includeHierarchy )
					{
						bundle = ResourceBundleTools.getBundleHierarchy( _forClass, locale );
					}
					else
					{
						bundle = ResourceBundleTools.getBundle( _forClass, locale );
					}
				}
				else
				{
					assert _baseName != null;
					bundle = ResourceBundleTools.getBundle( _baseName, locale, null );
				}
				bundlesByLocale.put( bundle.getLocale(), bundle );
			}
			catch ( final MissingResourceException ignored )
			{
				errors.append( "\nBundle '" );
				errors.append( _forClass != null ? _forClass.getName() : _baseName );
				errors.append( "' not found for locale '" );
				errors.append( locale );
				errors.append( '\'' );
			}
		}

		if ( errors.length() > 0 )
		{
			Assert.fail( "Error(s):" + errors );
		}

		System.out.println( " - found locales : " + bundlesByLocale.keySet() );

		final Collection<ResourceBundle> bundles = bundlesByLocale.values();
		final boolean allowLocaleDiffs = _allowLocaleDiffs;
		final boolean allowNonAscii = _allowNonAscii;
		final boolean allowHTML = _allowHTML;

		System.out.println( " - test options  : allowLocaleDiffs=" + allowLocaleDiffs + ", allowUnknown=" + allowUnknown + ", allowNonAscii=" + allowNonAscii + ", allowHTML=" + allowHTML );
		System.out.println( " - expectedKeys  : " + expectedKeys );

		if ( !allowUnknown && expectedKeys.isEmpty() )
		{
			throw new Error( "error in test: no expected keys and no unknown keys allowed!?" );
		}

		final Collection<Locale> seenLocales = new ArrayList<Locale>( bundles.size() );
		final List<String> unknownKeys = new ArrayList<String>();

		for ( final ResourceBundle bundle : bundles )
		{
			Locale locale = bundle.getLocale();
			if ( locale == null )
			{
				locale = Locale.ROOT;
			}
			final String bundleDesc = Locale.ROOT.equals( locale ) ? "default" : "'" + locale + '\'';

			/*
			 * Check presence of expected keys
			 */
			for ( final String key : expectedKeys )
			{
				try
				{
					bundle.getString( key );
				}
				catch ( final MissingResourceException ignored )
				{
					errors.append( "\nMissing key '" );
					errors.append( key );
					errors.append( "' in " );
					errors.append( bundleDesc );
					errors.append( " bundle." );
				}
			}

			if ( !allowLocaleDiffs && allowUnknown )
			{
				for ( final String key : unknownKeys )
				{
					if ( ResourceBundleTools.getString( bundle, key, null ) == null )
					{
						errors.append( "\nMissing key '" );
						errors.append( key );
						errors.append( "' in " );
						errors.append( bundleDesc );
						errors.append( " bundle." );
					}
				}
			}

			/*
			 * Check for presence of unknown keys.
			 */
			for ( final Enumeration<String> keys = bundle.getKeys(); keys.hasMoreElements(); )
			{
				final String key = keys.nextElement();
				final String keyDesc = "bundle '" + locale + ", key";
				final String value = bundle.getString( key );
				final String valueDesc = "resource '" + locale + '.' + key + "' value";

				if ( !expectedKeys.contains( key ) && !unknownKeys.contains( key ) )
				{
					if ( !allowUnknown )
					{
						errors.append( "\nUnknown key '" );
						errors.append( key );
						errors.append( "' was found in " );
						errors.append( bundleDesc );
						errors.append( " bundle" );
					}
					else if ( !seenLocales.isEmpty() && !allowLocaleDiffs )
					{
						errors.append( "\nKey '" );
						errors.append( key );
						errors.append( "' was found in " );
						errors.append( bundleDesc );
						errors.append( " bundle but not for previous locales (" );
						errors.append( seenLocales );
						errors.append( ')' );
					}

					unknownKeys.add( key );
				}

				testNonAscii( errors, keyDesc, key );

				if ( !allowNonAscii )
				{
					testNonAscii( errors, valueDesc, value );
				}

				if ( !allowHTML )
				{
					testHTML( errors, valueDesc, value );
				}
			}

			seenLocales.add( locale );
		}

		if ( errors.length() > 0 )
		{
			Assert.fail( "Error(s):" + errors );
		}

		return unknownKeys;
	}

	/**
	 * Test if the supplied string contains any non-ASCII characters
	 * (8,9,10,12,13,27,32-127). Throws an exception if so.
	 *
	 * @param errors Buffer to write errors to.
	 * @param what   Description of string being tested (used for error
	 *               message).
	 * @param s      String to test.
	 *
	 * @throws AssertionError if test fails.
	 * @see #testBundles
	 */
	private static void testNonAscii( @NotNull final StringBuilder errors, @NotNull final String what, @Nullable final String s )
	{
		if ( s != null )
		{
			for ( int i = 0; i < s.length(); i++ )
			{
				final char c = s.charAt( i );
				if ( ( ( c < '\040' ) && ( c != '\b' ) && ( c != '\t' ) && ( c != '\n' ) && ( c != '\f' ) && ( c != '\r' ) && ( c != '\033' ) ) || ( c > '\177' ) )
				{
					errors.append( '\n' );
					errors.append( what );
					errors.append( " '" );
					errors.append( s );
					errors.append( "' contains non-ASCII character '" );
					errors.append( c );
					errors.append( "' (" );
					errors.append( (int)c );
					errors.append( ')' );
					break;
				}
			}
		}
	}

	/**
	 * Test if the supplied string contains any HTML characters (&....).
	 *
	 * @param errors Buffer to write errors to.
	 * @param what   Description of string being tested (used for error
	 *               message).
	 * @param s      String to test.
	 *
	 * @throws AssertionError if test fails.
	 * @see #testBundles
	 */
	private static void testHTML( @NotNull final StringBuilder errors, @NotNull final String what, @Nullable final String s )
	{
		if ( s != null )
		{
			final int length = s.length();

			int pos = 0;
			while ( true )
			{
				pos = s.indexOf( '&', pos ) + 1;
				if ( ( pos <= 0 ) || ( pos >= length ) )
				{
					break;
				}

				final char next = s.charAt( pos );
				if ( Character.isLetterOrDigit( next ) )
				{
					errors.append( '\n' );
					errors.append( what );
					errors.append( " '" );
					errors.append( s );
					errors.append( "' contains HTML text: " );
					errors.append( s, pos - 1, s.length() );
					break;
				}
			}
		}
	}

	/**
	 * Get bean property names.
	 *
	 * @param beanClass         Bean class.
	 * @param setterRequired    Require setter for properties.
	 * @param excludeProperties Property name to exclude.
	 *
	 * @return Bean property names.
	 *
	 * @deprecated Use {@link BeanTools#getPropertyNames(Class, boolean,
	 * String...)} instead. (Note that the boolean parameter is reversed!)
	 */
	@Deprecated
	public static Set<String> getBeanPropertyNames( final Class<?> beanClass, final boolean setterRequired, final String... excludeProperties )
	{
		final Set<String> result = new HashSet<String>();

		final Collection<String> excludeSet = new HashSet<String>( Arrays.asList( excludeProperties ) );

		final Method[] methods = beanClass.getMethods();
		final Collection<String> methodNames = new HashSet<String>();

		for ( final Method method : methods )
		{
			methodNames.add( method.getName() );
		}

		for ( final Method method : methods )
		{
			final String methodName = method.getName();

			final String basename;
			if ( methodName.startsWith( "get" ) )
			{
				basename = methodName.substring( 3 );
			}
			else if ( methodName.startsWith( "is" ) )
			{
				basename = methodName.substring( 2 );
			}
			else
			{
				basename = null;
			}

			if ( ( basename != null ) && ( !setterRequired || methodNames.contains( "set" + basename ) ) )
			{
				final String propertyName = TextTools.decapitalize( basename );
				if ( !excludeSet.contains( propertyName ) )
				{
					result.add( propertyName );
				}
			}
		}
		return result;
	}
}
