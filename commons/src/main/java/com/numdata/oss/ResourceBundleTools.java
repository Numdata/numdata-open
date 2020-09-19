/*
 * Copyright (c) 2003-2020, Numdata BV, The Netherlands.
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
import java.text.*;
import java.util.*;
import java.util.zip.*;

import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This utility class contains utility methods to access resource bundles.
 *
 * @author Peter S. Heijnen
 */
public class ResourceBundleTools
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( ResourceBundleTools.class );

	/**
	 * This is used to support pre-loaded resource bundle data.
	 */
	@SuppressWarnings( "ConstantNamingConvention" )
	private static final ResourcePack _resourceData;

	/**
	 * Recursive bundle cache.
	 */
	@SuppressWarnings( "ConstantNamingConvention" )
	private static final Map<Locale, Map<String, ResourceBundle>> _recursiveBundleCache = new HashMap<>();

	/*
	 * Try to load resource data from 'res.dat' resource. This should be as
	 * forgiving to errors as possible.
	 */
	static
	{
		ResourcePack result = null;
		try
		{
			final ClassLoader classLoader = ResourceBundleTools.class.getClassLoader();

			try ( final InputStream is = classLoader.getResourceAsStream( "res.dat" ) )
			{
				if ( is != null )
				{
					result = new ResourcePack( new DataInputStream( new GZIPInputStream( is ) ) );
				}
			}
		}
		catch ( final Throwable ignored )
		{
		}
		_resourceData = result;
	}

	/**
	 * Utility class is not to be instantiated.
	 */
	private ResourceBundleTools()
	{
	}

	/**
	 * Recursive bundle cache.
	 */
	@SuppressWarnings( "ConstantNamingConvention" )
	public static void clearCache()
	{
		final Map<Locale, Map<String, ResourceBundle>> recursiveBundleCache = _recursiveBundleCache;
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized ( recursiveBundleCache )
		{
			recursiveBundleCache.clear();
		}
	}

	/**
	 * Get resource bundle for the specified class hierarchy and locale. If no
	 * locale is specified, the default locale is used.
	 *
	 * This method gets a bundle that contains resources for the specified class
	 * and all its parent classes (if any of them have bundles).
	 *
	 * @param clazz  Class to get the bundle for.
	 * @param locale Locale to get the bundle for.
	 *
	 * @return ResourceBundle for specified source and locale.
	 *
	 * @throws MissingResourceException if no resource bundle for the specified
	 * class can be found.
	 */
	@NotNull
	public static ResourceBundle getBundleHierarchy( @NotNull final Class<?> clazz, @Nullable final Locale locale )
	{
		LOG.debug( () -> "getBundleHierarchy( " + clazz + ", " + locale + " )" );
		final ResourceBundle result;

		final String className = clazz.getName();

		final Locale usedLocale = ( locale == null ) ? Locale.getDefault() : locale;

		final Map<Locale, Map<String, ResourceBundle>> recursiveBundleCache = _recursiveBundleCache;
		//noinspection SynchronizationOnLocalVariableOrMethodParameter
		synchronized ( recursiveBundleCache )
		{
			final Map<String, ResourceBundle> localeCache = recursiveBundleCache.computeIfAbsent( usedLocale, k -> new HashMap<>( 1 ) );
			if ( localeCache.containsKey( className ) )
			{
				result = localeCache.get( className );
			}
			else
			{
				final List<ResourceBundle> bundles = getHierarchyBundles( clazz, usedLocale );
				LOG.trace( () -> "getBundleHierarchy() combine " + bundles.size() + " bundle(s) for class  " + clazz.getName() );
				result = bundles.isEmpty() ? null : bundles.size() == 1 ? bundles.get( 0 ) : new MergedResourceBundle( className, usedLocale, bundles.toArray( new ResourceBundle[ 0 ] ) );
				LOG.trace( () -> "getBundleHierarchy() result for " + clazz.getName() + " => " + ( ( result != null ) ? "bundle with keys " + new TreeSet<>( result.keySet() ) : "null" ) );
				localeCache.put( className, result );
			}
		}

		if ( result == null )
		{
			throw new MissingResourceException( className, className, null );
		}

		return result;
	}

	/**
	 * Get resource bundles for the specified class hierarchy and locale. If no
	 * locale is specified, the default locale is used.
	 *
	 * <p>This method gets a bundle that contains resources for the specified
	 * class and all its parent classes (if any of them have bundles).
	 *
	 * <p>If a key exists in multiple returned bundles the entry from the last
	 * bundle must be used.
	 *
	 * @param clazz  Class to get the bundle for.
	 * @param locale Locale to get the bundle for.
	 *
	 * @return Resource bundles for the specified class hierarchy.
	 */
	@NotNull
	public static List<ResourceBundle> getHierarchyBundles( final @NotNull Class<?> clazz, @NotNull final Locale locale )
	{
		final List<ResourceBundle> bundles = new ArrayList<>();

		final Class<?> superclass = clazz.getSuperclass();
		ResourceBundle superBundle = null;
		if ( ( superclass != null ) && ( superclass != Object.class ) && !superclass.getName().startsWith( "java." ) )
		{
			try
			{
				final ResourceBundle bundle = getBundleHierarchy( superclass, locale );
				superBundle = bundle;
				LOG.trace( () -> "getHierarchyBundles: got super-bundle for super-class " + superclass.getName() + " with keys " + new TreeSet<>( bundle.keySet() ) );
				bundles.add( superBundle );
			}
			catch ( final MissingResourceException ignored )
			{
			}
		}

		final Package localPackage = clazz.getPackage();
		LOG.trace( () -> "getHierarchyBundles: localPackage=" + localPackage );
		if ( ( localPackage != null ) && ( ( superBundle == null ) || !localPackage.equals( superclass.getPackage() ) ) )
		{
			final int packageBundleIndex = bundles.size();
			String packageName = localPackage.getName();
			do
			{
				final String packageBundleName = packageName + ".LocalStrings";
				LOG.trace( () -> "getHierarchyBundles: packageBundleName=" + packageBundleName );
				try
				{
					final ResourceBundle bundle = getBundle( packageBundleName, locale, clazz.getClassLoader() );
					bundles.add( packageBundleIndex, bundle );
					LOG.trace( () -> "getHierarchyBundles: got " + packageBundleName + " bundle with keys " + new TreeSet<>( bundle.keySet() ) );
				}
				catch ( final MissingResourceException ignored )
				{
				}

				final int dot = packageName.lastIndexOf( '.' );
				packageName = ( dot > 0 ) ? packageName.substring( 0, dot ) : null;
			}
			while ( packageName != null );
		}

		final Class<?> declaringClass = getDeclaringClass( clazz );
		if ( declaringClass != null )
		{
			try
			{
				final ResourceBundle bundle = getBundleHierarchy( declaringClass, locale );
				bundles.add( bundle );
				LOG.trace( () -> "getHierarchyBundles: got bundle for declaring class " + declaringClass.getName() + " with keys " + new TreeSet<>( bundle.keySet() ) );
			}
			catch ( final MissingResourceException ignored )
			{
			}
		}

		try
		{
			final ResourceBundle bundle = getBundle( clazz, locale );
			bundles.add( bundle );
			LOG.trace( () -> "getHierarchyBundles: got bundle for requested class " + clazz.getName() + " with keys " + new TreeSet<>( bundle.keySet() ) );
		}
		catch ( final MissingResourceException ignored )
		{
		}
		return bundles;
	}

	@SuppressWarnings( "JavaDoc" )
	@Nullable
	private static Class<?> getDeclaringClass( @NotNull final Class<?> clazz )
	{
		Class<?> result = clazz.getDeclaringClass();
		if ( result == null )
		{
			final String className = clazz.getName();
			final int pos = className.indexOf( '$', className.lastIndexOf( '.' ) + 1 );
			if ( pos >= 0 )
			{
				try
				{
					final String outerClassName = className.substring( 0, pos );
					result = Class.forName( outerClassName );
				}
				catch ( final Throwable t )
				{
					/* did not work, too bad! */
				}
			}
		}

		//noinspection ObjectEquality
		if ( result == Object.class )
		{
			result = null;
		}

		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "getDeclaringClass() => " + result );
		}


		return result;
	}

	/**
	 * Get resource bundle for the specified class and locale.  If no locale is
	 * specified, the default locale is used.
	 *
	 * This method provides a central point for loading class resource bundles.
	 * It provides a hook for changing the default load behavior of bundles.
	 *
	 * @param clazz  Class to get the bundle for.
	 * @param locale Locale to get the bundle for.
	 *
	 * @return ResourceBundle for specified source and locale.
	 *
	 * @throws MissingResourceException if no resource bundle for the specified
	 * class can be found.
	 */
	@NotNull
	public static ResourceBundle getBundle( @NotNull final Class<?> clazz, @Nullable final Locale locale )
	{
		return getBundle( clazz.getName(), locale, clazz.getClassLoader() );
	}

	/**
	 * Get resource bundle for the specified class and locale.  If no locale is
	 * specified, the default locale is used.
	 *
	 * This method provides a central point for loading class resource bundles.
	 * It provides a hook for changing the default load behavior of bundles.
	 *
	 * @param baseName Base name of bundle (typically fully qualified class name
	 *                 ).
	 * @param locale   Locale to get the bundle for.
	 * @param loader   Class loader from which to load the resource bundle
	 *                 ({@code null} to use system class loader).
	 *
	 * @return ResourceBundle for specified source and locale.
	 *
	 * @throws MissingResourceException if no resource bundle for the specified
	 * class can be found.
	 */
	@NotNull
	public static ResourceBundle getBundle( @NotNull final String baseName, @Nullable final Locale locale, @Nullable final ClassLoader loader )
	{
		final Locale usedLocale = ( locale == null ) ? Locale.getDefault() : locale;

		/*
		 * Use resource bundle from pre-loaded data if possible.
		 */
		ResourceBundle result = null;
		if ( _resourceData != null )
		{
			try
			{
				result = _resourceData.getBundle( baseName, usedLocale );
				if ( LOG.isDebugEnabled() )
				{
					LOG.debug( "Retrieved bundle from resource data: " + baseName + ", " + usedLocale );
				}
			}
			catch ( final MissingResourceException e )
			{
				// Fall back to regular resource bundle loading.
			}
		}

		/*
		 * Use regular resource bundle loading (not pre-loaded).
		 */
		if ( result == null )
		{
			if ( loader == null ) /* indicates void, basic type, or bootstrap class loader */
			{
				if ( LOG.isDebugEnabled() )
				{
					LOG.debug( "getBundle( '" + baseName + ", " + usedLocale + ", " + CONTROL + " )" );
				}
				result = ResourceBundle.getBundle( baseName, usedLocale, CONTROL ); /* uses system class loader, which extends the bootstrap class loader... */
			}
			else
			{
				if ( LOG.isDebugEnabled() )
				{
					LOG.debug( "getBundle( '" + baseName + ", " + usedLocale + ", " + loader + ", " + CONTROL + " )" );
				}
				result = ResourceBundle.getBundle( baseName, usedLocale, loader, CONTROL );
			}
		}

		return result;
	}

	/**
	 * Create {@link ResourceBundle} from the properties stored in the specified
	 * {@link Properties} object.
	 *
	 * @param properties {@link Properties} to create bundle from (may be {@code
	 *                   null}).
	 *
	 * @return {@link ResourceBundle} instance.
	 */
	@NotNull
	public static ResourceBundle getBundle( @Nullable final Properties properties )
	{
		final Map<String, String> map;

		if ( properties != null )
		{
			map = new HashMap<>( properties.size() );
			for ( final Enumeration<String> e = (Enumeration<String>)properties.propertyNames(); e.hasMoreElements(); )
			{
				final String key = e.nextElement();
				final String value = properties.getProperty( key );

				map.put( key, value );
			}
		}
		else
		{
			map = new HashMap<>( 0 );
		}

		return new ResourceBundle()
		{
			@Override
			protected Object handleGetObject( @NotNull final String key )
			{
				return map.get( key );
			}

			@NotNull
			@Override
			public Enumeration<String> getKeys()
			{
				return new CombinedBundleEnumeration( parent, map.keySet() );
			}
		};
	}

	/**
	 * Create {@link Properties} object from the string resources stored in the
	 * specified {@link ResourceBundle}.
	 *
	 * @param bundle {@link ResourceBundle} to get properties from (may be
	 *               {@code null}).
	 *
	 * @return {@link ResourceBundle} instance.
	 */
	@NotNull
	public static Properties getProperties( @Nullable final ResourceBundle bundle )
	{
		final Properties result = new Properties();

		if ( bundle != null )
		{
			for ( final Enumeration<String> e = bundle.getKeys(); e.hasMoreElements(); )
			{
				final String key = e.nextElement();
				final Object value = bundle.getObject( key );

				if ( value instanceof String )
				{
					result.setProperty( key, (String)value );
				}
			}
		}

		return result;
	}

	/**
	 * Get list of strings from resource bundle with the specified key. Elements
	 * are separated by pipes (|). Leading and trailing whitespace of each
	 * element is removed.
	 *
	 * @param bundle Bundle to get the list from.
	 * @param key    Resource key of string list.
	 *
	 * @return List of strings from resource bundle.
	 *
	 * @throws MissingResourceException if no object for the given key can be
	 * found
	 */
	@NotNull
	public static String[] getStringList( @NotNull final ResourceBundle bundle, @NotNull final String key )
	{
		final String[] result = TextTools.tokenize( bundle.getString( key ), '|' );

		for ( int i = result.length; --i >= 0; )
		{
			result[ i ] = result[ i ].trim();
		}

		return result;
	}

	/**
	 * Get property choices from resource bundle for the specified property
	 * name. To make this work, the following resources must be defined:
	 *
	 * <ul>
	 *
	 * <li>'{propertyName}List' is a list of possible values</li>
	 *
	 * <li>Optionally, a label for each value in the list</li>
	 *
	 * </ul>
	 *
	 * @param bundle       Bundle to get the list from.
	 * @param propertyName Name of property for which to get the choices.
	 *
	 * @return Array with value/label pairs for all available choices.
	 */
	@NotNull
	public static String[] getChoices( @NotNull final ResourceBundle bundle, @Nullable final String propertyName )
	{
		return getChoices( bundle, propertyName + '.', getStringList( bundle, propertyName + "List" ) );
	}

	/**
	 * Get property choices from resource bundle for the specified property name
	 * based on a list of allowed property values. The choice labels are
	 * determined for each value in the following order:
	 *
	 * <ol>
	 *
	 * <li>Resource entry with the prefix concatenated with value as key;
	 *
	 * <li>Resource entry with the value as key;
	 *
	 * <li>The value itself.
	 *
	 * </ol>
	 *
	 * @param bundle Bundle to get value labels from.
	 * @param prefix Prefix to keys in bundle (optional).
	 * @param values List of values to include in choices.
	 *
	 * @return Array with value/label pairs for all available choices.
	 */
	@NotNull
	public static String[] getChoices( @Nullable final ResourceBundle bundle, @Nullable final String prefix, @NotNull final String[] values )
	{
		final String[] result = new String[ values.length * 2 ];

		int resultIndex = 0;
		for ( final String value : values )
		{
			String label = null;

			if ( ( prefix != null ) && !prefix.isEmpty() )
			{
				label = getString( bundle, prefix + value, null );
			}

			if ( label == null )
			{
				label = getString( bundle, value, value );
			}

			result[ resultIndex++ ] = value;
			result[ resultIndex++ ] = label;
		}

		return result;
	}

	/**
	 * Get integer from resource bundle. If the requested integer is not or
	 * incorrectly defined the bundle (or if {@code null} is specified for the
	 * bundle or key), the specified default value is returned.
	 *
	 * @param bundle       Bundle to get integer from.
	 * @param key          Key of integer to get from bundle.
	 * @param defaultValue Value to return when the resource is not found.
	 *
	 * @return Integer from resource bundle; default value if undefined or
	 * invalid.
	 */
	public static int getInt( @Nullable final ResourceBundle bundle, @NotNull final String key, final int defaultValue )
	{
		int result = defaultValue;

		final String string = getString( bundle, key, null );
		if ( string != null )
		{
			try
			{
				result = Integer.parseInt( string );
			}
			catch ( final NumberFormatException e )
			{
				/* ignored, will return default value */
			}
		}

		return result;
	}

	/**
	 * Get string from resource for an enumerate type.
	 *
	 * @param locale    Locale to get resource string for.
	 * @param enumerate Enumerate value.
	 *
	 * @return String from resource bundle, or default value if undefined.
	 */
	@NotNull
	public static String getString( @Nullable final Locale locale, @NotNull final Enum<?> enumerate )
	{
		String result;

		try
		{
			result = getString( getBundleHierarchy( enumerate.getClass(), locale ), enumerate );
		}
		catch ( final MissingResourceException ignored )
		{
			result = enumerate.toString();
		}

		return result;
	}

	/**
	 * Get string from resource for an enumerate type.
	 *
	 * @param bundle    Bundle to get string from.
	 * @param enumerate Enumerate value.
	 *
	 * @return String from resource bundle, or default value if undefined.
	 */
	@NotNull
	public static String getString( @Nullable final ResourceBundle bundle, @NotNull final Enum<?> enumerate )
	{
		String result;

		final String name = enumerate.name();

		if ( bundle != null )
		{
			try
			{
				final Class<?> enumClass = enumerate.getClass();
				String enumName = enumClass.getSimpleName();
				enumName = enumName.substring( enumName.lastIndexOf( '$' ) + 1 );

				result = bundle.getString( enumName + '.' + name );
			}
			catch ( final MissingResourceException ignored )
			{
				result = getString( bundle, name, enumerate.toString() );
			}
		}
		else
		{
			result = enumerate.toString();
		}

		return result;
	}

	/**
	 * Get string from resource bundle for the specified class and locale. If no
	 * locale is specified, the default locale is used.
	 *
	 * @param locale Locale to get the bundle for.
	 * @param clazz  Class to get the bundle for.
	 * @param key    Key of string to get from bundle.
	 *
	 * @return String from resource bundle.
	 *
	 * @throws MissingResourceException if no resource bundle or resource entry
	 * for the specified class can be found.
	 */
	@NotNull
	public static String getString( @Nullable final Locale locale, @NotNull final Class<?> clazz, @NotNull final String key )
	{
		final ResourceBundle bundle;
		try
		{
			bundle = getBundleHierarchy( clazz, locale );
		}
		catch ( final MissingResourceException e )
		{
			final MissingResourceException mre = new MissingResourceException( "Resource bundle '" + clazz.getName() + "' missing (to get '" + key + "' resource) for locale " + locale, clazz.getName(), key );
			mre.initCause( e );
			throw mre;

		}

		try
		{
			return bundle.getString( key );
		}
		catch ( final MissingResourceException ignored )
		{
			throw new MissingResourceException( "Resource '" + key + "' not found in '" + clazz.getName() + "' bundle for locale " + locale, bundle.getClass().getName(), key );
		}
	}

	/**
	 * Get string from resource bundle. If the requested string is not defined
	 * in the bundle (or if {@code null} is specified for the bundle or key),
	 * the specified default value is returned.
	 *
	 * @param bundle       Bundle to get string from.
	 * @param key          Key of string to get from bundle.
	 * @param defaultValue Value to return when the string is not found.
	 *
	 * @return String from resource bundle, or default value if undefined.
	 */
	@Contract( "_, _, !null -> !null" )
	@Nullable
	public static String getString( @Nullable final ResourceBundle bundle, @NotNull final String key, @Nullable final String defaultValue )
	{
		String result = defaultValue;
		if ( bundle != null )
		{
			try
			{
				result = bundle.getString( key );
			}
			catch ( final MissingResourceException e )
			{
				/* ignored, will return default value */
			}
		}
		return result;
	}

	/**
	 * Get message pattern with the specified key from the specified bundle and
	 * format the message by substituting the specified argument.
	 *
	 * @param bundle   Bundle to get message from.
	 * @param key      Key of message pattern to get from bundle.
	 * @param argument Argument {@code {0}} to format and substitute.
	 *
	 * @return Formatted message.
	 *
	 * @throws IllegalArgumentException if the message pattern or arguments are
	 * invalid.
	 * @see MessageFormat#format(String, Object[])
	 */
	@NotNull
	public static String format( @Nullable final ResourceBundle bundle, @NotNull final String key, final long argument )
	{
		return MessageFormat.format( getString( bundle, key, key ), argument );
	}

	/**
	 * Get message pattern with the specified key from the specified bundle and
	 * format the message by substituting the specified argument.
	 *
	 * @param bundle   Bundle to get message from.
	 * @param key      Key of message pattern to get from bundle.
	 * @param argument Argument {@code {0}} to format and substitute.
	 *
	 * @return Formatted message.
	 *
	 * @throws IllegalArgumentException if the message pattern or arguments are
	 * invalid.
	 * @see MessageFormat#format(String, Object[])
	 */
	@NotNull
	public static String format( @Nullable final ResourceBundle bundle, @NotNull final String key, final double argument )
	{
		return MessageFormat.format( getString( bundle, key, key ), argument );
	}

	/**
	 * Get message pattern with the specified key from the specified bundle and
	 * format the message by substituting the specified arguments.
	 *
	 * @param bundle    Bundle to get message from.
	 * @param key       Key of message pattern to get from bundle.
	 * @param arguments Array of objects to be formatted and substituted.
	 *
	 * @return Formatted message.
	 *
	 * @throws IllegalArgumentException if the message pattern or arguments are
	 * invalid.
	 * @see MessageFormat#format(String, Object[])
	 */
	@NotNull
	public static String format( @Nullable final ResourceBundle bundle, @NotNull final String key, final Object... arguments )
	{
		return MessageFormat.format( getString( bundle, key, key ), arguments );
	}

	/**
	 * Get message pattern with the specified key from the specified bundle and
	 * format the message by substituting the specified arguments.
	 *
	 * @param locale    Locale used to format the pattern.
	 * @param bundle    Bundle to get message from.
	 * @param key       Key of message pattern to get from bundle.
	 * @param arguments Array of objects to be formatted and substituted.
	 *
	 * @return Formatted message.
	 *
	 * @throws IllegalArgumentException if the message pattern or arguments are
	 * invalid.
	 * @see MessageFormat#format(String, Object[])
	 */
	@NotNull
	public static String format( @Nullable final Locale locale, @Nullable final ResourceBundle bundle, @NotNull final String key, final Object... arguments )
	{
		return new MessageFormat( getString( bundle, key, key ), locale ).format( arguments );
	}

	/**
	 * Get message pattern with the specified key from the specified bundle and
	 * format the message by substituting the specified arguments.
	 *
	 * @param locale    Locale used to format the pattern.
	 * @param clazz     Class from whose bundle to get message from.
	 * @param key       Key of message pattern to get from bundle.
	 * @param arguments Array of objects to be formatted and substituted.
	 *
	 * @return Formatted message.
	 *
	 * @throws IllegalArgumentException if the message pattern or arguments are
	 * invalid.
	 * @see MessageFormat#format(String, Object[])
	 */
	@NotNull
	public static String format( @Nullable final Locale locale, @NotNull final Class<?> clazz, @NotNull final String key, final Object... arguments )
	{
		final ResourceBundle bundle;
		try
		{
			bundle = getBundleHierarchy( clazz, locale );
		}
		catch ( final MissingResourceException e )
		{
			final MissingResourceException mre = new MissingResourceException( "Resource bundle '" + clazz.getName() + "' missing (to get '" + key + "' resource and format with arguments " + Arrays.toString( arguments ) + ") for locale " + locale, clazz.getName(), key );
			mre.initCause( e );
			throw mre;

		}

		return format( locale, bundle, key, arguments );
	}


	/**
	 * Get {@link LocalizableString} that provides a translation of a class'
	 * name. The translation is retrieved from the class resource bundle
	 * hierarchy, and may be the class name or the name of one of its ancestor
	 * classes.
	 *
	 * @param clazz Class whose name to translate.
	 *
	 * @return {@link LocalizableString} that provides a class name translation
	 * (returns {@link Class#getSimpleName() simple class name} if no
	 * translation is found).
	 */
	@NotNull
	public static LocalizableString getClassNameTranslation( @NotNull final Class<?> clazz )
	{
		return new AbstractLocalizableString()
		{
			@NotNull
			@Override
			public String get( @Nullable final Locale locale )
			{
				return getClassNameTranslation( locale, clazz );
			}
		};
	}

	/**
	 * Get translation of a class' name. The translation is retrieved from the
	 * class resource bundle hierarchy, and may be the class name or the name of
	 * one of its ancestor classes.
	 *
	 * @param locale Locale to get class name for.
	 * @param clazz  Class whose name to translate.
	 *
	 * @return Translated class name; returns {@link Class#getSimpleName()
	 * simple class name} if no translation was found.
	 */
	@NotNull
	public static String getClassNameTranslation( @Nullable final Locale locale, @NotNull final Class<?> clazz )
	{
		final ResourceBundle bundle = getBundleHierarchy( clazz, locale );

		String result = null;
		for ( Class<?> ancestor = clazz; ( result == null ) && ( ancestor != null ); ancestor = ancestor.getSuperclass() )
		{
			final String className = ancestor.getSimpleName();
			result = getString( bundle, className, null );
			if ( result == null )
			{
				result = getString( bundle, TextTools.decapitalize( className ), null );
			}
		}

		if ( result == null )
		{
			result = clazz.getSimpleName();
		}

		return result;
	}

	/**
	 * {@link ControlWithoutFallback} used by {@link #getBundle(Class,
	 * Locale)}.
	 */
	private static final ResourceBundle.Control CONTROL = new ControlWithoutFallback();

	/**
	 * Implementation of {@link ResourceBundle.Control} that does not fallback
	 * to the default locale.
	 */
	private static class ControlWithoutFallback
	extends ResourceBundle.Control
	{
		@Nullable
		@Override
		public Locale getFallbackLocale( @Nullable final String baseName, @Nullable final Locale locale )
		{
			return null;
		}
	}
}
