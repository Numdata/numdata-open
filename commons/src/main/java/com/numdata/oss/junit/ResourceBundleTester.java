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
package com.numdata.oss.junit;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.*;
import java.util.regex.*;

import com.numdata.oss.*;
import com.numdata.oss.ensemble.*;
import com.numdata.oss.io.*;
import org.jetbrains.annotations.*;
import org.junit.*;

/**
 * JUnit unit tool class to help with testing resource bundles. Note that this
 * class can be used as a:
 *
 * <ul>
 *
 * <li><strong>Stand-alone application.</strong> Reads bundles from the
 * command-line arguments and tests them for errors. Command-line options can be
 * added to change the test behaviour.</li>
 *
 * <li><strong>JUnit test helper.</strong> Call {@code testBundles()} from your
 * test case to ensure resource bundle presence and content during the unit test
 * phase.</li>
 *
 * <li><strong>A utility class.</strong> Instances of this class provide the
 * same functionality as with the previous two uses to other classes.</li>
 *
 * </ul>
 *
 * Checks include resource bundle syntax and consistency amongst bundles.
 * Consistency checks include keys, whitespace, alignment, special characters,
 * and comments.
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
	 * Pattern for a resource bundle name.
	 */
	private static final Pattern BUNDLE_NAME_PATTERN = Pattern.compile( "^(.*?)(_([a-z][a-z])(_([A-Z][A-Z])(_([a-zA-Z_)]+))?)?)?\\.(?i)properties$" );

	/**
	 * Log used to keep track of errors that occurred when comparing the
	 * resource bundles.
	 */
	private final PrintWriter _log;

	/**
	 * Contents of each loaded resource bundle, mapped by (file) name.
	 */
	private final SortedMap<String, List<Line>> _bundlesByName = new TreeMap<String, List<Line>>();

	/**
	 * Generate warnings concirning comment lines in resource bundles.
	 */
	private boolean _commentWarnings = true;

	/**
	 * Generate warnings concerning whitespace in resource bundles.
	 */
	private boolean _whitespaceWarnings = true;

	/**
	 * Generate warnings concerning empty values in resource bundles.
	 */
	private boolean _emptyValueWarnings = true;

	/**
	 * Find differences between different locales of each bundle, i.e. missing
	 * keys.
	 */
	private boolean _findLocaleDifferences = false;

	/**
	 * When set, a warnings are given for entries that have the same value for
	 * different locales.
	 */
	private boolean _sameValueInDifferentLocale = false;

	/**
	 * Process input files recursively.
	 */
	private boolean _recursive = false;

	/**
	 * Input files to be processed.
	 */
	private final List<String> _inputFiles;

	/**
	 * Whether to generate verbose output.
	 */
	private boolean _verbose = false;

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		boolean ok = true;

		final PrintWriter log = new PrintWriter( System.out, true );
		final ResourceBundleTester tester = new ResourceBundleTester( log );

		if ( args.length == 0 )
		{
			ok = false;
		}

		try
		{
			final Queue<String> arguments = new LinkedList<String>( Arrays.asList( args ) );
			while ( !arguments.isEmpty() )
			{
				final String argument = arguments.peek();

				if ( TextTools.startsWith( argument, '-' ) )
				{
					arguments.remove();

					if ( argument.length() == 1 )
					{
						break;
					}

					for ( int i = 1; i < argument.length(); i++ )
					{
						switch ( argument.charAt( i ) )
						{
							case 'c':
								tester.setCommentWarnings( false );
								break;
							case 'l':
								tester.setFindLocaleDifferences( true );
								break;
							case 'd':
								tester.setFindLocaleDifferences( true );
								tester.setSameValueInDifferentLocale( true );
								break;
//							case 'm': // TODO Support merging of resource bundles or something useful like that. Need to figure out what is actually useful...
//								final String locale = arguments.poll();
//								if ( locale == null )
//								{
//									System.err.println( "Missing argument for option -m." );
//									ok = false;
//								}
//								else
//								{
//									tester.setMergeLocale( TextTools.parseLocale( locale ) );
//								}
//								break;
							case 'r':
								tester.setRecursive( true );
								break;
							case 'v':
								tester.setVerbose( true );
								break;
							case 'w':
								tester.setWhitespaceWarnings( false );
								break;
							case 'e':
								tester.setEmptyValueWarnings( false );
								break;
							default:
								System.err.println( "Unknown option: " + argument );
								ok = false;
								break;
						}
					}
				}
				else if ( argument.startsWith( "--" ) )
				{
					arguments.remove();
					final String argumentName = argument.substring( 2 );

					if ( "verbose".equals( argumentName ) )
					{
						tester.setVerbose( true );
					}
					else
					{
						System.err.println( "Unknown option: " + argument );
					}
				}
				else
				{
					break;
				}
			}

			tester.setInputFiles( arguments );

			if ( !ok )
			{
				// 80 characters:    12345678901234567890123456789012345678901234567890123456789012345678901234567890
				System.out.println( "Usage:" );
				System.out.println( "    [OPTION]... resource-bundle..." );
				System.out.println();
				System.out.println( "Options:" );
				System.out.println( "  -c               Ignore comments." );
				System.out.println( "  -e               Ignore empty values." );
				System.out.println( "  -w               Ignore whitespace." );
				System.out.println( "  -r               Process subdirectories recursively." );
				System.out.println( "  -l               Check for differences between locales." );
				System.out.println( "  -d               Check for identical values in different locales. Implies -l." );
				System.out.println( "  -m LOCALE        Merge, i.e. copy missing entries from specified locale." );
				System.out.println( "  -v, --verbose    Verbose output." );
			}
			else
			{
				System.out.println( "Finished with exit value: " + tester.call() );
			}
		}
		catch ( final FileNotFoundException e )
		{
			log.println( "error: resource bundle not found (" + e.getMessage() + ')' );
			ok = false;
		}
		catch ( final IOException e )
		{
			log.println( "error: I/O exception, message: " + e.getMessage() );
			ok = false;
		}

		if ( !ok )
		{
			System.out.println( "Failed!" );
		}
	}

	/**
	 * Test resource bundles for a class.
	 *
	 * @param forClass         Class to test resource bundles for.
	 * @param includeHierarchy {@code true} to include resource bundles for
	 *                         super-classes.
	 * @param minimumLocales   List of minimum required locales.
	 * @param allowLocaleDiffs Allow differences between locales;
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
	 * @param allowLocaleDiffs Allow differences between locales;
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
	 * @param allowLocaleDiffs Allow differences between locales;
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
		System.out.println( " - class         : " + forClass.getName() );
		System.out.println( " - hierarchy     : " + includeHierarchy );
		System.out.println( " - tried locales : " + locales );

		if ( !allowUnknown && expectedKeys.isEmpty() )
		{
			throw new Error( "error in test: no expected keys and no unknown keys allowed!?" );
		}

		final StringBuilder errors = new StringBuilder();
		final Map<Locale, ResourceBundle> bundles = new LinkedHashMap<Locale, ResourceBundle>();
		for ( final Locale locale : locales )
		{
			try
			{
				final ResourceBundle bundle = includeHierarchy ? ResourceBundleTools.getBundleHierarchy( forClass, locale ) : ResourceBundleTools.getBundle( forClass, locale );
				bundles.put( bundle.getLocale(), bundle );
			}
			catch ( final MissingResourceException ignored )
			{
				errors.append( "\nBundle '" );
				errors.append( forClass.getName() );
				errors.append( "' not found for locale '" );
				errors.append( locale );
				errors.append( '\'' );
			}
		}

		if ( errors.length() > 0 )
		{
			Assert.fail( "Error(s):" + errors );
		}

		System.out.println( " - found locales : " + bundles.keySet() );

		return testBundles( bundles.values(), allowLocaleDiffs, expectedKeys, allowUnknown, allowNonAscii, allowHTML );
	}

	/**
	 * Test resource bundles for a class.
	 *
	 * @param baseName         Base name of resource bundles to test.
	 * @param locales          List of locales to test.
	 * @param allowLocaleDiffs Allow differences between locales;
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
		System.out.println( " - baseName      : " + baseName );
		System.out.println( " - tried locales : " + locales );

		if ( !allowUnknown && expectedKeys.isEmpty() )
		{
			throw new Error( "error in test: no expected keys and no unknown keys allowed!?" );
		}

		final StringBuilder errors = new StringBuilder();
		final Map<Locale, ResourceBundle> bundles = new LinkedHashMap<Locale, ResourceBundle>();

		for ( final Locale locale : locales )
		{
			try
			{
				final ResourceBundle bundle = ResourceBundleTools.getBundle( baseName, locale, null );
				bundles.put( bundle.getLocale(), bundle );
			}
			catch ( final MissingResourceException ignored )
			{
				errors.append( "\nBundle '" );
				errors.append( baseName );
				errors.append( "' not found for locale '" );
				errors.append( locale );
				errors.append( '\'' );
			}
		}

		if ( errors.length() > 0 )
		{
			Assert.fail( "Error(s):" + errors );
		}

		System.out.println( " - found locales   : " + bundles.keySet() );

		return testBundles( bundles.values(), allowLocaleDiffs, expectedKeys, allowUnknown, allowNonAscii, allowHTML );
	}

	/**
	 * Test resource bundles for a class.
	 *
	 * @param bundles          Resource bundles to test.
	 * @param allowLocaleDiffs Allow differences between locales;
	 * @param expectedKeys     List of keys that are required in all bundles.
	 * @param allowUnknown     Allow unknown keys (not in expectedKeys) in
	 *                         bundles.
	 * @param allowNonAscii    Allow non-ASCII characters (>127) in values.
	 * @param allowHTML        Allow HTML tags in values.
	 *
	 * @return List with unknown keys that were found in the resource bundle.
	 */
	public static List<String> testBundles( @NotNull final Collection<ResourceBundle> bundles, final boolean allowLocaleDiffs, @NotNull final Collection<String> expectedKeys, final boolean allowUnknown, final boolean allowNonAscii, final boolean allowHTML )
	{
		System.out.println( " - test options  : allowLocaleDiffs=" + allowLocaleDiffs + ", allowUnknown=" + allowUnknown + ", allowNonAscii=" + allowNonAscii + ", allowHTML=" + allowHTML );
		System.out.println( " - expectedKeys  : " + expectedKeys );

		if ( !allowUnknown && expectedKeys.isEmpty() )
		{
			throw new Error( "error in test: no expected keys and no unknown keys allowed!?" );
		}

		final StringBuilder errors = new StringBuilder();

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
	public static void testNonAscii( @NotNull final StringBuilder errors, @NotNull final String what, @Nullable final String s )
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
	public static void testHTML( @NotNull final StringBuilder errors, @NotNull final String what, @Nullable final String s )
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
	 * Construct new ResourceBundleTester.
	 *
	 * @param log The used to write errors/warnings to.
	 */
	public ResourceBundleTester( final PrintWriter log )
	{
		_log = log;
		_inputFiles = new ArrayList<String>();
	}

	/**
	 * Sets the resource bundle files to be processed.
	 *
	 * @param inputFiles Resource bundle file names.
	 */
	public void setInputFiles( final Collection<String> inputFiles )
	{
		_inputFiles.clear();
		_inputFiles.addAll( inputFiles );
	}

	/**
	 * Returns whether warnings are given about inconsistent comments.
	 *
	 * @return {@code true} if comment warnings are given.
	 */
	public boolean isCommentWarnings()
	{
		return _commentWarnings;
	}

	/**
	 * Sets whether warnings are given about inconsistent comments.
	 *
	 * @param commentWarnings {@code true} to enable comment warnings.
	 */
	public void setCommentWarnings( final boolean commentWarnings )
	{
		_commentWarnings = commentWarnings;
	}

	/**
	 * Returns whether warnings are given about inconsistent whitespace.
	 *
	 * @return {@code true} if whitespace warnings are given.
	 */
	public boolean isWhitespaceWarnings()
	{
		return _whitespaceWarnings;
	}

	/**
	 * Sets whether warnings are given about inconsistent whitespace.
	 *
	 * @param whitespaceWarnings {@code true} to enable whitespace warnings.
	 */
	public void setWhitespaceWarnings( final boolean whitespaceWarnings )
	{
		_whitespaceWarnings = whitespaceWarnings;
	}

	/**
	 * Returns whether warnings are given about empty values.
	 *
	 * @return {@code true} if empty value warnings are given.
	 */
	public boolean isEmptyValueWarnings()
	{
		return _emptyValueWarnings;
	}

	/**
	 * Sets whether warnings are given about empty values.
	 *
	 * @param emptyValueWarnings {@code true} to enable empty value warnings.
	 */
	public void setEmptyValueWarnings( final boolean emptyValueWarnings )
	{
		_emptyValueWarnings = emptyValueWarnings;
	}

	/**
	 * Returns whether finding of differences between locales is enabled.
	 *
	 * @return {@code true} if finding differences between locales is enabled.
	 */
	public boolean isFindLocaleDifferences()
	{
		return _findLocaleDifferences;
	}

	/**
	 * Sets whether finding of differences between locales is enabled.
	 *
	 * @param findLocaleDifferences {@code true} to enable finding differences
	 *                              between locales.
	 */
	public void setFindLocaleDifferences( final boolean findLocaleDifferences )
	{
		_findLocaleDifferences = findLocaleDifferences;
	}

	/**
	 * Returns whether warnings are given for entries identical values in
	 * different locales.
	 *
	 * @return {@code true} if these warnings are enabled.
	 */
	public boolean isSameValueInDifferentLocale()
	{
		return _sameValueInDifferentLocale;
	}

	/**
	 * Sets whether warnings are given for entries identical values in different
	 * locales.
	 *
	 * @param sameValueInDifferentLocale {@code true} to enable these warnings.
	 */
	public void setSameValueInDifferentLocale( final boolean sameValueInDifferentLocale )
	{
		_sameValueInDifferentLocale = sameValueInDifferentLocale;
	}

	/**
	 * Returns whether directories are processed recursively.
	 *
	 * @return {@code true} if directories are processed recursively.
	 */
	public boolean isRecursive()
	{
		return _recursive;
	}

	/**
	 * Sets whether directories are processed recursively.
	 *
	 * @param recursive {@code true} to process directories recursively.
	 */
	public void setRecursive( final boolean recursive )
	{
		_recursive = recursive;
	}

	/**
	 * Returns whether verbose output will be generated.
	 *
	 * @return {@code true} if output is verbose.
	 */
	public boolean isVerbose()
	{
		return _verbose;
	}

	/**
	 * Sets whether verbose output will be generated.
	 *
	 * @param verbose {@code true} for verbose output.
	 */
	public void setVerbose( final boolean verbose )
	{
		_verbose = verbose;
	}

	/**
	 * Runs the resource bundle tester using currently set options and input
	 * files.
	 *
	 * @return Process exit value ({@code 0} when successful).
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public Integer call()
	throws IOException
	{
		final BasicSolo<Boolean> result = new BasicSolo<Boolean>( Boolean.TRUE );

		System.out.println( "Loading resource bundles..." );
		for ( final String argument : _inputFiles )
		{
			final FileVisitor visitor = new FileVisitor()
			{
				@Override
				public void visit( @NotNull final File file )
				throws IOException
				{
					final String path = file.toString();
					if ( isVerbose() )
					{
						_log.println( "Loading " + path + "..." );
					}

					final FileReader r = new FileReader( file );
					try
					{
						result.setValue( load( r, path ) );
					}
					finally
					{
						r.close();
					}
				}
			};

			FileTools.visitRecursively( new File( argument ), new FileExtensionFilter( ".properties", null, false ), visitor );
		}

		if ( result.getValue() )
		{
			if ( isFindLocaleDifferences() )
			{
				findLocaleDifferences();
			}

//			result.setValue( Boolean.valueOf( check() ) );
		}

		return result.getValue() ? 0 : -1;
	}

	/**
	 * Creates a model of the given resource bundle and checks for tabs.
	 *
	 * @param sourceReader The reader that will be used to read characters from
	 *                     the given resource bundle.
	 * @param bundleName   Identifies the bundle for the user.
	 *
	 * @return {@code true} if bundle was loaded successfully; {@code false} if
	 * the bundle contains errors.
	 *
	 * @throws IOException if the was an I/O problem while loading the bundle.
	 */
	public boolean load( final Reader sourceReader, final String bundleName )
	throws IOException
	{
		final StringBuilder sb = new StringBuilder();
		final List<Line> lines = new ArrayList<Line>();
		int lastLineBeginPosition = 0;
		int currentLine = 1;
		char lastChar = '\0';

		while ( true )
		{
			final int i = sourceReader.read();
			if ( i < 0 )
			{
				break;
			}

			final char c = (char)i;

			if ( c == '\t' )    // Tab encountered
			{
				if ( isWhitespaceWarnings() )
				{
					_log.println( bundleName + ": WARNING: Line " + currentLine + ": Tab character found." );
				}
			}
			else if ( c == '\n' )    // End of line encountered
			{
				boolean isContinuation = false;
				if ( !lines.isEmpty() )
				{
					final Line lastLine = lines.get( lines.size() - 1 );
					if ( TextTools.endsWith( lastLine.getValue(), '\\' ) )
					{
						isContinuation = true;
					}
				}

				final String lineString = sb.substring( lastLineBeginPosition, sb.length() );
				final Line line = new Line( bundleName, lineString, currentLine, isContinuation );

				currentLine++;
				lastLineBeginPosition = sb.length();
				lines.add( line );
//				System.out.println( "Line: " + lineString );
			}
			else if ( c < ' ' )
			{
				_log.println( bundleName + ": ERROR: Line " + currentLine + ": Control character(" + (int)c + ") found." );
			}
			else
			{
				sb.append( c );
			}

			lastChar = c;
		}

		/*
		 * Perform checks on single bundle.
		 */
		final boolean success;

		if ( lastChar != '\n' )
		{
			if ( isWhitespaceWarnings() )
			{
				_log.println( bundleName + ": WARNING: Line " + currentLine + ": No newline at end of file." );
			}

			boolean isContinuation = false;
			if ( !lines.isEmpty() )
			{
				final Line lastLine = lines.get( lines.size() - 1 );
				if ( TextTools.endsWith( lastLine.getValue(), '\\' ) )
				{
					isContinuation = true;
				}
			}

			final String lineString = sb.substring( lastLineBeginPosition, sb.length() );
			final Line line = new Line( bundleName, lineString, currentLine, isContinuation );
			lines.add( line );

			success = true;
		}
		else
		{
			success = checkAlignment( lines, bundleName );
		}

		if ( success )
		{
			_bundlesByName.put( bundleName, lines );
		}

		return success;
	}

	/**
	 * Finds and reports any differences between different localizations of the
	 * same resource bundle.
	 */
	private void findLocaleDifferences()
	{
		System.out.println( "Finding differences between locales..." );

		String currentBaseName = null;
		final Map<Locale, List<Line>> currentBundles = new LinkedHashMap<Locale, List<Line>>();

		for ( final Map.Entry<String, List<Line>> entry : _bundlesByName.entrySet() )
		{
			final Matcher matcher = BUNDLE_NAME_PATTERN.matcher( entry.getKey() );
			if ( matcher.matches() )
			{
				final String baseName = matcher.group( 1 );
				if ( currentBaseName == null )
				{
					currentBaseName = baseName;
				}
				else if ( !baseName.equals( currentBaseName ) )
				{
					findLocaleDifferences( currentBaseName, currentBundles );
					currentBundles.clear();
					currentBaseName = baseName;
				}

				final String language = matcher.group( 3 );
				final String country = matcher.group( 5 );
				final String variant = matcher.group( 7 );
				final Locale locale = new Locale( ( language == null ) ? "" : language, ( country == null ) ? "" : country, ( variant == null ) ? "" : variant );

				currentBundles.put( locale, entry.getValue() );
			}
		}

		if ( !currentBundles.isEmpty() && ( currentBaseName != null ) )
		{
			findLocaleDifferences( currentBaseName, currentBundles );
		}
	}

	/**
	 * Searches the given bundles for differences.
	 *
	 * @param baseName Base name of the bundles.
	 * @param bundles  Map with different translations of the same logical
	 *                 resource bundle, by locale.
	 */
	private void findLocaleDifferences( @NotNull final String baseName, @NotNull final Map<Locale, List<Line>> bundles )
	{
		final Set<Map.Entry<Locale, List<Line>>> bundleEntries = bundles.entrySet();
		final Iterator<Map.Entry<Locale, List<Line>>> bundleIterator = bundleEntries.iterator();

		final Map.Entry<Locale, List<Line>> firstBundle = bundleIterator.next();
		final Locale firstLocale = firstBundle.getKey();
		final List<Line> firstLines = firstBundle.getValue();
		final Map<String, BundleEntry> firstContent = getLogicalBundleContent( firstLines );

		while ( bundleIterator.hasNext() )
		{
			final Map.Entry<Locale, List<Line>> otherBundle = bundleIterator.next();
			final Locale otherLocale = otherBundle.getKey();
			final List<Line> otherLines = otherBundle.getValue();
			final Map<String, BundleEntry> otherContent = getLogicalBundleContent( otherLines );

			final Map<String, BundleEntry> difference = new LinkedHashMap<String, BundleEntry>( otherContent );
			final Set<String> differenceKeys = difference.keySet();
			for ( final Entry<String, BundleEntry> entry : firstContent.entrySet() )
			{
				final String key = entry.getKey();
				if ( differenceKeys.remove( key ) )
				{
					if ( isSameValueInDifferentLocale() )
					{
						// Common key
						final BundleEntry firstEntry = entry.getValue();
						final BundleEntry otherEntry = otherContent.get( firstEntry.getKey() );

						final String firstValue = firstEntry.getValue();
						final String otherValue = otherEntry.getValue();

						if ( firstValue.equals( otherValue ) )
						{
							System.out.println( baseName + ": Same value for '" + firstEntry.getKey() + "' in locales '" + firstLocale + "' and '" + otherLocale + '\'' );
						}
					}
				}
				else
				{
					// Only in first bundle
					System.out.println( baseName + ": Entry for '" + key + "' present in locale '" + firstLocale + "' but not in locale '" + otherLocale + '\'' );
				}
			}

			for ( final String key : difference.keySet() )
			{
				// Only in other bundle
				System.out.println( baseName + ": Entry for '" + key + "' present in locale '" + otherLocale + "' but not in locale '" + firstLocale + '\'' );
			}
		}
	}

	/**
	 * Processes the given resource bundle lines into resource bundle entries,
	 * which represent the parsed contents associated with a single key. By
	 * contrast, a {@code Line} represents a single line from the input file and
	 * may contain only part of a resource bundle entry, due to escaped line
	 * breaks.
	 *
	 * @param lines Lines read from the resource bundle file.
	 *
	 * @return Bundle entries derived from the given lines.
	 */
	private Map<String, BundleEntry> getLogicalBundleContent( final Iterable<Line> lines )
	{
		final Map<String, BundleEntry> result = new HashMap<String, BundleEntry>();

		BundleEntry currentEntry = null;

		for ( final Line line : lines )
		{
			if ( line.getType() == Line.CONTINUATION )
			{
				if ( currentEntry != null )
				{
					currentEntry.append( line.getValue() );
				}
			}
			else
			{
				if ( currentEntry != null )
				{
					result.put( currentEntry.getKey(), currentEntry );
					currentEntry = null;
				}

				if ( line.getType() == Line.RESOURCE )
				{
					final String key = line.getKey();
					currentEntry = new BundleEntry( line.getLineNumber(), key.trim(), line.getValue() );
				}
			}
		}

		if ( currentEntry != null )
		{
			result.put( currentEntry.getKey(), currentEntry );
		}

		return result;
	}

	/**
	 * Represents a single entry from a resource bundle.
	 */
	private static class BundleEntry
	{
		/**
		 * Line number where the entry starts.
		 */
		private final int _lineNumber;

		/**
		 * Key identifying the entry.
		 */
		private final String _key;

		/**
		 * Value of the entry.
		 */
		private String _value;

		/**
		 * Constructs a new resource bundle entry.
		 *
		 * @param lineNumber Line number where the entry starts.
		 * @param key        Key identifying the entry.
		 * @param value      Value of the entry.
		 */
		private BundleEntry( final int lineNumber, final String key, final String value )
		{
			_lineNumber = lineNumber;
			_key = key;
			_value = value;
		}

		/**
		 * Returns the entry's key.
		 *
		 * @return Key identifying the entry.
		 */
		public String getKey()
		{
			return _key;
		}

		/**
		 * Returns the entry's value.
		 *
		 * @return Value of the entry.
		 */
		public String getValue()
		{
			return _value;
		}

		/**
		 * Appends the given string to the entry's current value.
		 *
		 * @param value String to be appended.
		 */
		public void append( final String value )
		{
			_value += value;
		}

		/**
		 * Returns the line number where the entry starts.
		 *
		 * @return Entry's line number.
		 */
		public int getLineNumber()
		{
			return _lineNumber;
		}
	}

	/**
	 * Check all loaded resource bundles.
	 *
	 * @return {@code true} when all loaded bundles are correct; {@code false}
	 * when one or more bundles are inconsistent.
	 */
	public boolean check()
	{
		return checkKeys( _bundlesByName.entrySet() );
	}

	/**
	 * Compares the keys of the given resource bundles against the first
	 * resource bundle in the list.
	 *
	 * @param bundles Bundles to be checked.
	 *
	 * @return {@code True} when the keys (including spaces!) are identical.
	 */
	private boolean checkKeys( @NotNull final Iterable<Map.Entry<String, List<Line>>> bundles )
	{
		boolean result = true;

		final Iterator<Map.Entry<String, List<Line>>> bundleIterator = bundles.iterator();

		/*
		 * Iterate through all resource bundle models.
		 */
		final Map.Entry<String, List<Line>> sourceBundle = bundleIterator.next();

		while ( bundleIterator.hasNext() )
		{
			final Map.Entry<String, List<Line>> destBundle = bundleIterator.next();

			final String sourceName = sourceBundle.getKey();
			final String destName = destBundle.getKey();

			final List<Line> sourceList = sourceBundle.getValue();
			final List<Line> destList = destBundle.getValue();

			/*
			 * Check each line of the first resource bundle against the
			 * corresponding line in the other bundles.
			 */
			for ( int j = 0; j < sourceList.size() && j < destList.size(); j++ )
			{
				final Line sourceLine = sourceList.get( j );
				final Line destLine = destList.get( j );

				if ( sourceLine.getType() == Line.COMMENT )
				{
					if ( destLine.getType() != Line.COMMENT )
					{
						if ( isCommentWarnings() )
						{
							_log.println( destBundle + ": No corresponding comment line in resource bundle '" + destName + "' for comment: " + sourceLine.getText() + " at line " + sourceLine.getLineNumber() );
							result = false;
						}
					}
					else if ( !TextTools.equals( sourceLine.getText(), destLine.getText() ) )
					{
						if ( isCommentWarnings() )
						{
							_log.println( destBundle + ": Comment at line " + sourceLine.getLineNumber() + " doesn't match resource bundle " + sourceName );
							result = false;
						}
					}
				}
				else if ( sourceLine.getType() == Line.RESOURCE && destLine.getType() == Line.RESOURCE )
				{
					final String sourceKey = sourceLine.getKey();
					final String destKey = destLine.getKey();

					if ( !sourceKey.equals( destKey ) )
					{
						final String trimmedSourceKey = sourceKey.trim();
						if ( trimmedSourceKey.equals( destKey.trim() ) )
						{
							if ( isWhitespaceWarnings() )
							{
								_log.println( "warning: Whitespace around key: '" + trimmedSourceKey + "' at line " + sourceLine.getLineNumber() + " does not match with whitespace around corresponding key in resource bundle " + destName );
							}
						}
						else
						{
							_log.println( "error: Key '" + trimmedSourceKey + "' at line " + sourceLine.getLineNumber() + " does not match with '" + destKey.trim() + "' in resource bundle " + destName );
						}

						result = false;
					}
				}
			}   //  end for

		}   // end for

		return result;
	}

	/**
	 * Checks if lines are aligned correctly in a resource bundle. Multi-line
	 * values are checked as follows: A multi-line value should look like:
	 *
	 * <PRE>
	 *
	 * buttonText    = Click on this\
	 *
	 * &nbsp;               button!
	 *
	 * </PRE>
	 *
	 * The space before 'button!' should be the same as the position before
	 * 'Click'.
	 *
	 * The alignment of '=' characters in blocks of key/value pairs is checked
	 * as well. (In a block they should all be on the same position)
	 *
	 * @param lines      The model of a resource bundle.
	 * @param bundleName Identifies the bundle for the user.
	 *
	 * @return {@code true} when all lines (if any) are aligned correctly;
	 * otherwise {@code false}
	 */
	public boolean checkAlignment( final Iterable<Line> lines, final String bundleName )
	{
		final int[] equalsPositionInBlock = new int[ getNumberOfBlocks( lines ) ];
		boolean success = true;
		int blockNr = 0;
		int alignmentPosition = 0;

		for ( final Line line : lines )
		{
			if ( line.getType() == Line.EMPTY || line.getType() == Line.COMMENT )
			{
				blockNr++;
			}

			if ( line.getType() == Line.CONTINUATION )
			{
//				System.out.println( "ha: " + prevLine.getKey() + " ." );
				final String lineText = line.getValue();
				final String trimmedLineText = lineText.trim();
				final int startPosition = lineText.indexOf( trimmedLineText.charAt( 0 ) );
				if ( isWhitespaceWarnings() && ( alignmentPosition != startPosition ) )
				{
					_log.println( bundleName + ": WARNING: The continuation at " + line.getLineNumber() + " in resource bundle '" + bundleName + "' is not correctly aligned (pos=" + startPosition + ", should be " + alignmentPosition + ")." );
					success = false;
				}
//				System.out.println( "endcontinuation" );
			}

			if ( line.getType() == Line.RESOURCE )
			{
				final String lineText = line.getText();
				final int equalsPos = lineText.indexOf( '=' );

				if ( equalsPositionInBlock[ blockNr ] == 0 )
				{
					equalsPositionInBlock[ blockNr ] = equalsPos;
				}

				if ( equalsPositionInBlock[ blockNr ] != equalsPos )
				{
					if ( isWhitespaceWarnings() )
					{
						_log.println( bundleName + ": WARNING: '=' at position " + equalsPositionInBlock[ blockNr ] + " in block " + blockNr + " in resource bundle '" + bundleName + "' is not aligned correctly compared to the first line '" + equalsPos + "' of the block" );
					}
				}

				final String key = line.getKey();
				alignmentPosition = key.length() + 2;
			}
		}

		return success;
	}

	/**
	 * Get bean property names.
	 *
	 * @param beanClass         Bean class.
	 * @param setterRequired    Require setter for properties.
	 * @param excludeProperties Property name to exclude.
	 *
	 * @return Bean property names.
	 */
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

	/**
	 * Get the number of key/value blocks in a given resource bundle. A serie of
	 * key/values is considered a new block after an empty or a comment line.
	 *
	 * @param list The resource bundle that will be counted for blocks.
	 *
	 * @return The number of key/value blocks that appeared in the given
	 * resource bundle.
	 */
	public static int getNumberOfBlocks( final Iterable<Line> list )
	{
		int nr = 1;

		for ( final Line line : list )
		{
			if ( line.getType() == Line.EMPTY || line.getType() == Line.COMMENT )
			{
				nr++;
			}
		}

		return nr;
	}

	/**
	 * Represents one line of a resource bundle.
	 */
	private class Line
	{
		/**
		 * Constant indicating that a line is a comment line.
		 */
		public static final int COMMENT = 0;

		/**
		 * Constant indicating that a line is a resource line.
		 */
		public static final int RESOURCE = 1;

		/**
		 * Constant indicating that a line is empty ("").
		 */
		public static final int EMPTY = 2;

		/**
		 * Constant indicating that a line is a continuation of the previous
		 * line.
		 */
		public static final int CONTINUATION = 3;

		/**
		 * String that holds the line.
		 */
		private final String _line;

		/**
		 * The linenumber this line was found on.
		 */
		private final int _lineNumber;

		/**
		 * The key element of this line, including whitespace until the "="
		 * character! {@code null} when this line is not of type RESOURCE.
		 */
		private final String _key;

		/**
		 * The value element of this line. {@code null} when this line is of
		 * type COMMENT.
		 */
		private final String _value;

		/**
		 * The type of this line. (COMMENT or RESOURCE).
		 */
		private final int _type;

		/**
		 * Constructs a new line from the specified resource bundle.
		 *
		 * @param bundleName   Name of the resource bundle.
		 * @param line         The text that makes up this line.
		 * @param lineNumber   The number this line was found on.
		 * @param continuation Indicates whether this line is a continuation of
		 *                     a previous line ({@code True}) or not ({@code
		 *                     false}).
		 */
		private Line( final String bundleName, final String line, final int lineNumber, final boolean continuation )
		{
			_line = line;
			_lineNumber = lineNumber;

			final PrintWriter log = _log;

			final String trimmed = _line.trim();
			if ( trimmed.isEmpty() )
			{
				_type = EMPTY;
				_key = null;
				_value = _line;
			}
			else if ( trimmed.charAt( 0 ) == '#' )
			{
				_type = COMMENT;
				_key = null;
				_value = _line;
			}
			else if ( _line.contains( "=" ) )
			{
				_type = RESOURCE;
				_key = _line.substring( 0, _line.indexOf( '=' ) );
				_value = _line.substring( _line.indexOf( '=' ) + 1, _line.length() );
			}
			else if ( continuation )
			{
				_type = CONTINUATION;
				_key = null;
				_value = _line;
			}
			else
			{
				log.println( bundleName + ": ERROR: Text: '" + _line + "' at line " + lineNumber + " is illegal (forgot '\\' at the end of previous line?)" );

				_type = -1;
				_key = null;
				_value = _line;
			}

			if ( _type == RESOURCE )
			{
				/*
				 * Perform some checks
				 */
				if ( _value.isEmpty() )
				{
					if ( isEmptyValueWarnings() )
					{
						log.println( bundleName + ": WARNING: Empty value at line " + lineNumber );
					}
				}
				else if ( _value.charAt( 0 ) != ' ' )
				{
					if ( isWhitespaceWarnings() )
					{
						log.println( bundleName + ": WARNING: No space after '=' at line " + lineNumber );
					}
				}
				// @FIXME what is this supposed to do????
				//			else if ( _value.length() > 1 && _value.charAt( 1 ) == ' ' )
				//			{
				//				log.println( "warning: No space, followed by non-space detected after '=' for key: " + _key.trim() + " at line " + lineNumber );
				//			}

				if ( _line.contains( "?" ) && ( _line.indexOf( '?' ) != _line.length() - 1 ) )
				{
					log.println( bundleName + ": WARNING: ? detected inside line: " + lineNumber );
				}
			}
		}

		/**
		 * Get the {@code String} that represents this line.
		 *
		 * @return {@code String} representing this line.
		 */
		public String getText()
		{
			return _line;
		}

		/**
		 * Get the number this line is on.
		 *
		 * @return The number this line is on.
		 */
		public int getLineNumber()
		{
			return _lineNumber;
		}

		/**
		 * Get the key of this line.
		 *
		 * @return {@code String} containing the key of this line. Includins
		 * whitespace until the "=" character! {@code null} when this line is
		 * not of type RESOURCE.
		 */
		public String getKey()
		{
			return _key;
		}

		/**
		 * Get the value of this line.
		 *
		 * @return {@code String} containing the value of this line. When this
		 * line is op type RESOURCE, it contains the value after the '=' sign,
		 * otherwise it is the same as getLine().
		 */
		public String getValue()
		{
			return _value;
		}

		/**
		 * Returns the type of this line.
		 *
		 * @return The type of this line (COMMENT, RESOURCE, EMPTY or
		 * CONTINUATION).
		 */
		public int getType()
		{
			return _type;
		}
	}
}
