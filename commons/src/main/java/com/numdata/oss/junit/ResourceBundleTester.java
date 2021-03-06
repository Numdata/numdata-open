/*
 * Copyright (c) 2004-2020, Numdata BV, The Netherlands.
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
@SuppressWarnings( { "unused", "WeakerAccess", "UseOfSystemOutOrSystemErr" } )
public class ResourceBundleTester
{
	/**
	 * Default locales to test.
	 */
	public static final List<Locale> LOCALES = Collections.unmodifiableList( Arrays.asList( Locale.US, new Locale( "nl", "NL" ), Locale.GERMANY, Locale.FRANCE, new Locale( "sv", "SE" ), Locale.ITALY ) );

	/**
	 * Class to test resource bundles for.
	 */
	@Nullable
	private Class<?> _forClass;

	/**
	 * {@code true} to include resource bundles for super-classes.
	 */
	private boolean _includeHierarchy = true;

	/**
	 * Base name of resource bundles to test.
	 */
	@Nullable
	private String _baseName;

	/**
	 * Locales to test bundles for.
	 */
	@NotNull
	private final Set<Locale> _locales = new LinkedHashSet<>( LOCALES );

	/**
	 * Allow differences between locales.
	 */
	private boolean _allowLocaleDiffs = false;

	/**
	 * List of keys that are required in all bundles.
	 */
	@NotNull
	private final Set<String> _expectedKeys = new LinkedHashSet<>();

	/**
	 * Allow unknown keys (not in expectedKeys) in bundles.
	 */
	private boolean _allowUnknown = false;

	/**
	 * Allow non-ASCII characters (>127) in values.
	 */
	private boolean _allowNonAscii = true;

	/**
	 * Allow HTML tags in values.
	 */
	private boolean _allowHTML = false;

	/**
	 * Whether to allow duplicate resource entries (resource entries that are
	 * defined in parent bundles).
	 */
	private boolean _allowDuplicates = false;

	/**
	 * Constructs a new instance.
	 *
	 * @param forClass Class to test resource bundles for, include resource
	 *                 bundles for super-classes.
	 */
	public ResourceBundleTester( @Nullable final Class<?> forClass )
	{
		_forClass = forClass;
		_baseName = null;
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

	/**
	 * Creates a resource bundle tester for the given class, including its
	 * bundle hierarchy.
	 *
	 * @param clazz Class to test bundles of.
	 *
	 * @return Resource bundle tester.
	 */
	@NotNull
	public static ResourceBundleTester forClass( @NotNull final Class<?> clazz )
	{
		return new ResourceBundleTester( clazz );
	}

	/**
	 * Creates a new tester for the given class, including its bundle hierarchy,
	 * with expected keys initialized to all of its bean properties.
	 *
	 * @param forClass Class to test resource bundles for.
	 *
	 * @return Created tester.
	 */
	public static ResourceBundleTester forBean( @NotNull final Class<?> forClass )
	{
		final ResourceBundleTester tester = forClass( forClass );
		tester.addExpectedKeys( BeanTools.getPropertyNames( forClass, false ) );
		return tester;
	}

	/**
	 * Creates a resource bundle tester for the given enum class, including its
	 * bundle hierarchy, * with expected keys initialized to all of its values.
	 *
	 * @param enumClass Class to test.
	 *
	 * @return Resource bundle tester.
	 */
	@NotNull
	public static ResourceBundleTester forEnum( @NotNull final Class<? extends Enum> enumClass )
	{
		final ResourceBundleTester tester = forClass( enumClass );
		tester.addEnumNames( enumClass );
		return tester;
	}

	/**
	 * Creates a resource bundle tester for the 'LocalStrings' bundle in the
	 * same package as the given class.
	 *
	 * @param clazzInPackage Class in package to test 'LocalStrings' bundle of.
	 *
	 * @return Resource bundle tester.
	 */
	@NotNull
	public static ResourceBundleTester forLocalStrings( @NotNull final Class<?> clazzInPackage )
	{
		final ResourceBundleTester tester = new ResourceBundleTester( clazzInPackage.getPackage().getName() + ".LocalStrings" );
		tester.setForClass( clazzInPackage );
		return tester;
	}

	public @Nullable Class<?> getForClass()
	{
		return _forClass;
	}

	public void setForClass( final @Nullable Class<?> forClass )
	{
		_forClass = forClass;
	}

	public @Nullable String getBaseName()
	{
		return _baseName;
	}

	public void setBaseName( final @Nullable String baseName )
	{
		_baseName = baseName;
	}

	public void addDeclaredEnums( @NotNull final Class<?> clazz )
	{
		if ( clazz.isEnum() )
		{
			for ( final Object enumConstant : clazz.getEnumConstants() )
			{
				_expectedKeys.add( enumConstant.toString() );
			}
		}

		for ( final Class<?> innerClass : clazz.getDeclaredClasses() )
		{
			addDeclaredEnums( innerClass );
		}
	}

	public void addEnumNames( @NotNull final Class<? extends Enum> enumClass )
	{
		addEnumNames( null, enumClass );
	}

	public void addEnumNames( @Nullable final String prefix, @NotNull final Class<? extends Enum> enumClass, @NotNull final String... suffices )
	{
		for ( final Enum enumConstant : enumClass.getEnumConstants() )
		{
			final String name = enumConstant.name();
			final String prefixed = ( prefix != null ) ? prefix + name : name;
			_expectedKeys.add( prefixed );
			for ( final String suffix : suffices )
			{
				_expectedKeys.add( prefixed + suffix );
			}
		}
	}

	public void addExpectedKey( @NotNull final String expectedKey )
	{
		assertTrue( "Unnecessary expected key '" + expectedKey + '\'', _expectedKeys.add( expectedKey ) );
	}

	public void addExpectedKeyUnchecked( @NotNull final String expectedKey )
	{
		_expectedKeys.add( expectedKey );
	}

	public void addExpectedKeyWithSuffix( @NotNull final String expectedKey, @NotNull final String suffix, @NotNull final String... additionalSuffices )
	{
		_expectedKeys.add( expectedKey );
		addExpectedKey( expectedKey + suffix );
		for ( final String additionalSuffix : additionalSuffices )
		{
			addExpectedKey( expectedKey + additionalSuffix );
		}
	}

	public void addExpectedKeys( @NotNull final String... expectedKeys )
	{
		for ( final String expectedKey : expectedKeys )
		{
			addExpectedKey( expectedKey );
		}
	}

	public void addExpectedKeys( @NotNull final Collection<String> expectedKeys )
	{
		_expectedKeys.addAll( expectedKeys );
	}

	public void addExpectedKeysWithSuffix( @NotNull final String suffix )
	{
		for ( final String expectedKey : new ArrayList<>( _expectedKeys ) )
		{
			addExpectedKey( expectedKey + suffix );
		}
	}

	public void addExpectedKeysWithSuffix( @SuppressWarnings( "TypeMayBeWeakened" ) @NotNull final Collection<String> expectedKeys, @NotNull final String suffix, @NotNull final String... additionalSuffices )
	{
		for ( final String expectedKey : expectedKeys )
		{
			addExpectedKeyWithSuffix( expectedKey, suffix, additionalSuffices );
		}
	}

	public void removeExpectedKey( @NotNull final String expectedKey )
	{
		assertTrue( "Missing expected key '" + expectedKey + '\'', _expectedKeys.remove( expectedKey ) );
	}

	public void removeExpectedKeys( @NotNull final String... expectedKeys )
	{
		for ( final String expectedKey : expectedKeys )
		{
			removeExpectedKey( expectedKey );
		}
	}

	public void removeExpectedKeys( @SuppressWarnings( "TypeMayBeWeakened" ) @NotNull final Collection<String> expectedKeys )
	{
		for ( final String expectedKey : expectedKeys )
		{
			removeExpectedKey( expectedKey );
		}
	}

	public void replaceExpectedKey( @NotNull final String oldExpectedKey, @NotNull final String newExpectedKey )
	{
		removeExpectedKey( oldExpectedKey );
		addExpectedKeyUnchecked( newExpectedKey );
	}

	public boolean isIncludeHierarchy()
	{
		return _includeHierarchy;
	}

	public void setIncludeHierarchy( final boolean includeHierarchy )
	{
		_includeHierarchy = includeHierarchy;
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

	public boolean isAllowDuplicates()
	{
		return _allowDuplicates;
	}

	public void setAllowDuplicates( final boolean allowDuplicates )
	{
		_allowDuplicates = allowDuplicates;
	}

	/**
	 * Tests the resource bundles.
	 *
	 * @return Map with bundles that were tested mapped by requested locale.
	 */
	public Map<Locale, ResourceBundle> run()
	{
		final Set<String> expectedKeys = getExpectedKeys();
		final String baseName = _baseName;
		final Class<?> forClass = _forClass;
		final boolean includeHierarchy = isIncludeHierarchy();
		final boolean allowUnknown = isAllowUnknown();
		final boolean allowLocaleDiffs = isAllowLocaleDiffs();
		final boolean allowNonAscii = isAllowNonAscii();
		final boolean allowHTML = isAllowHTML();
		final Set<Locale> tryLocales = getLocales();

		if ( forClass != null )
		{
			System.out.println( " - class         : " + forClass.getName() );
			System.out.println( " - hierarchy     : " + includeHierarchy );
		}
		else
		{
			System.out.println( " - baseName      : " + baseName );
		}

		System.out.println( " - test options  : includeHierarchy=" + isIncludeHierarchy() + ", allowLocaleDiffs=" + allowLocaleDiffs + ", allowUnknown=" + allowUnknown + ", allowNonAscii=" + allowNonAscii + ", allowHTML=" + allowHTML );
		System.out.println( " - expectedKeys  : " + expectedKeys );
		System.out.println( " - tried locales : " + tryLocales );
		if ( tryLocales.isEmpty() )
		{
			throw new Error( "error in test: no locales specified to test" );
		}

		final StringBuilder errors = new StringBuilder();
		final Map<Locale, ResourceBundle> bundlesByLocale = new LinkedHashMap<>();
		final Map<Locale, ResourceBundle> localBundleByLocale = new HashMap<>();

		for ( final Locale locale : tryLocales )
		{
			try
			{
				final ResourceBundle bundle;
				if ( baseName == null )
				{
					assert forClass != null;
					if ( includeHierarchy )
					{
						bundle = ResourceBundleTools.getBundleHierarchy( forClass, locale );
					}
					else
					{
						bundle = ResourceBundleTools.getBundle( forClass, locale );
					}

					try
					{
						localBundleByLocale.put( bundle.getLocale(), ResourceBundleTools.getBundle( forClass, locale ) );
					}
					catch ( final Exception ignored )
					{
					}
				}
				else
				{
					bundle = ResourceBundleTools.getBundle( baseName, locale, null );
				}
				bundlesByLocale.put( bundle.getLocale(), bundle );
			}
			catch ( final MissingResourceException ignored )
			{
				errors.append( "\nBundle '" ).append( baseName == null ? forClass.getName() : baseName ).append( "' not found for locale '" ).append( locale ).append( '\'' );
			}
		}

		if ( errors.length() > 0 )
		{
			fail( "Error(s):" + errors );
		}

		System.out.println( " - found locales : " + bundlesByLocale.keySet() );
		if ( bundlesByLocale.isEmpty() )
		{
			throw new AssertionError( "Found no " + ( ( baseName == null ) ? forClass.getName() : baseName ) + " bundle(s) to test" );
		}

		final Collection<ResourceBundle> bundles = bundlesByLocale.values();

		final Collection<Locale> seenLocales = new ArrayList<>( bundles.size() );
		final Collection<String> unknownKeys = new ArrayList<>();

		for ( final Map.Entry<Locale, ResourceBundle> bundleEntry : bundlesByLocale.entrySet() )
		{
			final ResourceBundle bundle = bundleEntry.getValue();
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
					errors.append( "\nMissing key '" ).append( key ).append( "' in " ).append( bundleDesc ).append( " bundle." );
				}
			}

			if ( !allowLocaleDiffs && allowUnknown )
			{
				for ( final String key : unknownKeys )
				{
					if ( ResourceBundleTools.getString( bundle, key, null ) == null )
					{
						errors.append( "\nMissing key '" ).append( key ).append( "' in " ).append( bundleDesc ).append( " bundle." );
					}
				}
			}

			/*
			 * Check for presence of unknown keys.
			 */
			final ResourceBundle localBundle = localBundleByLocale.get( bundleEntry.getKey() );
			final Set<String> keysToTest = ( localBundle != null ) ? localBundle.keySet() : allowUnknown ? bundle.keySet() : Collections.emptySet();
			for ( final String key : keysToTest )
			{
				final String keyDesc = "bundle '" + locale + ", key";
				final String value = bundle.getString( key );
				final String valueDesc = "resource '" + locale + '.' + key + "' value";

				if ( !expectedKeys.contains( key ) && !unknownKeys.contains( key ) )
				{
					if ( !allowUnknown )
					{
						errors.append( "\nUnknown key '" ).append( key ).append( "' was found in " ).append( bundleDesc ).append( " bundle" );
					}
					else if ( !seenLocales.isEmpty() && !allowLocaleDiffs )
					{
						errors.append( "\nKey '" ).append( key ).append( "' was found in " ).append( bundleDesc ).append( " bundle but not for previous locales (" ).append( seenLocales ).append( ')' );
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
			fail( "Error(s):" + errors );
		}

		if ( !allowUnknown && expectedKeys.isEmpty() )
		{
			throw new Error( "error in test: no expected keys and no unknown keys allowed!?" );
		}

		if ( !isAllowDuplicates() )
		{
			testDuplicates( bundlesByLocale.keySet() );
		}

		return bundlesByLocale;
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
	 */
	private static void testNonAscii( @NotNull final StringBuilder errors, @NotNull final String what, @Nullable final String s )
	{
		if ( s != null )
		{
			for ( int i = 0; i < s.length(); i++ )
			{
				final char c = s.charAt( i );
				//noinspection OverlyComplexBooleanExpression
				if ( ( ( c < '\040' ) && ( c != '\b' ) && ( c != '\t' ) && ( c != '\n' ) && ( c != '\f' ) && ( c != '\r' ) && ( c != '\033' ) ) || ( c > '\177' ) )
				{
					errors.append( '\n' ).append( what ).append( " '" ).append( s ).append( "' contains non-ASCII character '" ).append( c ).append( "' (" ).append( (int)c ).append( ')' );
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
					errors.append( '\n' ).append( what ).append( " '" ).append( s ).append( "' contains HTML text: " ).append( s, pos - 1, s.length() );
					break;
				}
			}
		}
	}

	/**
	 * Test for duplicate entries in resource bundles.
	 *
	 * @param locales Locales to test.
	 */
	private void testDuplicates( @NotNull final Collection<Locale> locales )
	{
		final Class<?> clazz = _forClass;
		if ( clazz != null )
		{
			for ( final Locale locale : getLocales() )
			{
				System.out.println( "Test for duplicates resource entries in '" + clazz.getName() + "' bundles for " + ( Locale.ROOT.equals( locale ) ? "ROOT" : "'" + locale + '\'' ) + " locale" );

				final List<ResourceBundle> bundles = new ArrayList<>( ResourceBundleTools.getHierarchyBundles( clazz, locale ) );

				// reverse order, recursively traverse hierarchy tree, and de-dupe bundles
				Collections.reverse( bundles );
//				bundles.forEach( bundle -> System.out.println( " - Initial: " + bundle.getBaseBundleName() + " bundle for locale " + bundle.getLocale() ) );
				int i = 0;
				while ( i < bundles.size() )
				{
					final ResourceBundle bundle = bundles.get( i );
					if ( bundles.indexOf( bundle ) < i )
					{
						// remove duplicate bundle
						bundles.remove( i );
					}
					else if ( bundle instanceof MergedResourceBundle )
					{
						// merged bundle is always for a super/containing class, so we can get that now
						final Class<?> bundleClass;
						try
						{
							bundleClass = Class.forName( bundle.getBaseBundleName() );
						}
						catch ( final ClassNotFoundException e )
						{
							throw new AssertionError( "Merged resource bundle '" + bundle.getBaseBundleName() + "' should be for class", e );
						}

						// replace the merged bundle with bundles further down from hierarchy
						bundles.remove( i );
						final List<ResourceBundle> replacements = ResourceBundleTools.getHierarchyBundles( bundleClass, locale );
						for ( int j = replacements.size(); --j >= 0; )
						{
							bundles.add( i, replacements.get( j ) );
						}
					}
					else
					{
						System.out.println( " - Bundle: " + bundle.getBaseBundleName() + " bundle for " + ( Locale.ROOT.equals( bundle.getLocale() ) ? "ROOT" : "'" + bundle.getLocale() + '\'' ) + " locale" );
						i++;
					}
				}

				if ( bundles.size() < 2 )
				{
					System.out.println( " - Can't have any duplicates with only " + bundles.size() + " bundle(s)" );
					continue;
				}

				final Map<String, ResourceBundle> bundleByKey = new HashMap<>();
				final List<List<?>> duplicates = new ArrayList<>();
				for ( final ResourceBundle previousBundle : bundles )
				{
					for ( final String key : previousBundle.keySet() )
					{
						final String previousValue = previousBundle.getString( key );
						final ResourceBundle bundle = bundleByKey.put( key, previousBundle );
						if ( bundle != null )
						{
							final String value = bundle.getString( key );
							if ( Objects.equals( previousValue, value ) )
							{
								duplicates.add( Arrays.asList( key, bundle.getBaseBundleName(), bundle.getLocale(), previousBundle.getBaseBundleName(), previousBundle.getLocale() ) );
							}
							else
							{
								System.err.println( "NOTE: Resource '" + key + "' value is overridden by " + bundle.getBaseBundleName() + " bundle for locale " + bundle.getLocale() + " with value '" + value + "', existing entry was in " + previousBundle.getBaseBundleName() + " bundle for locale " + previousBundle.getLocale() + " with value '" + previousValue + '\'' );
							}
						}
					}
				}

				if ( !duplicates.isEmpty() )
				{
					duplicates.sort( Comparator.comparing( row -> ( (String)row.get( 0 ) ) ) );
					TextTable.write( System.err, Arrays.asList( "Key", "Bundle", "Locale", "Bundle", "Locale" ), duplicates, "", "\n", "" );
					fail( "Found " + duplicates.size() + " duplicate resource entries for " + locale + " locale in " + bundles.get( bundles.size() - 1 ) );
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
	public static Set<String> getBeanPropertyNames( @NotNull final Class<?> beanClass, final boolean setterRequired, @NotNull final String... excludeProperties )
	{
		final Set<String> result = new HashSet<>();

		final Collection<String> excludeSet = new HashSet<>( Arrays.asList( excludeProperties ) );

		final Method[] methods = beanClass.getMethods();
		final Collection<String> methodNames = new HashSet<>();

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
