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
import static org.junit.Assert.*;

/**
 * JUnit unit tool class to help with testing class field definitions.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "WeakerAccess" )
public class FieldTester
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private FieldTester()
	{
	}

	/**
	 * Get list of constant values from the specified class.
	 *
	 * @param forClass         Class to get the constants from.
	 * @param includeAncestors Include fields of ancestor classes.
	 * @param publicOnly       Return only public fields.
	 * @param typeFilter       Return constants of this type.
	 * @param ignoredFields    Names of fields to ignore.
	 * @param <T>              Constant type.
	 *
	 * @return List of constant values.
	 */
	@NotNull
	public static <T> List<T> getConstants( @NotNull final Class<?> forClass, final boolean includeAncestors, final boolean publicOnly, @NotNull final Class<T> typeFilter, @NotNull final String... ignoredFields )
	{
		return getConstants( forClass, includeAncestors, publicOnly, typeFilter, Arrays.asList( ignoredFields ) );
	}

	/**
	 * Get list of constant values from the specified class.
	 *
	 * @param forClass         Class to get the constants from.
	 * @param includeAncestors Include fields of ancestor classes.
	 * @param publicOnly       Return only public fields.
	 * @param typeFilter       Return constants of this type.
	 * @param ignoredFields    Names of fields to ignore.
	 * @param <T>              Constant type.
	 *
	 * @return List of constant values.
	 */
	@NotNull
	public static <T> List<T> getConstants( @NotNull final Class<?> forClass, final boolean includeAncestors, final boolean publicOnly, @NotNull final Class<T> typeFilter, @NotNull final Collection<String> ignoredFields )
	{
		final List<Field> fields = getFields( forClass, includeAncestors, true, false, publicOnly, typeFilter, ignoredFields );
		final List<T> result = new ArrayList<T>( fields.size() );
		for ( final Field field : fields )
		{
			try
			{
				field.setAccessible( true );
				//noinspection unchecked
				result.add( (T)field.get( null ) );
			}
			catch ( final IllegalAccessException e )
			{
				throw new AssertionError( e );
			}
		}
		return result;
	}

	/**
	 * Get list of constant fields from the specified class.
	 *
	 * @param forClass      Class to get the constants from.
	 * @param publicOnly    Return only public fields.
	 * @param ignoredFields Names of fields to ignore.
	 *
	 * @return List of constants.
	 */
	@NotNull
	public static List<Field> getConstantFields( @NotNull final Class<?> forClass, final boolean publicOnly, @NotNull final String... ignoredFields )
	{
		return getConstantFields( forClass, publicOnly, Arrays.asList( ignoredFields ) );
	}

	/**
	 * Get list of constant fields from the specified class.
	 *
	 * @param forClass      Class to get the constants from.
	 * @param publicOnly    Return only public fields.
	 * @param ignoredFields Names of fields to ignore.
	 *
	 * @return List of constants.
	 */
	@NotNull
	public static List<Field> getConstantFields( @NotNull final Class<?> forClass, final boolean publicOnly, @NotNull final Collection<String> ignoredFields )
	{
		return getFields( forClass, true, true, false, publicOnly, null, ignoredFields );
	}

	/**
	 * Get list of constant fields by their value. This is useful when trying to
	 * find constants without using explicit names.
	 *
	 * @param forClass Class to get the constants from.
	 * @param values   Constant values.
	 *
	 * @return List of constant values.
	 */
	@NotNull
	public static List<Field> getConstantFieldsByValue( @NotNull final Class<?> forClass, @NotNull final Collection<?> values )
	{
		final List<Field> fields = getFields( forClass, true, true, false, false, null );
		final List<Field> result = new ArrayList<Field>( values.size() );
		for ( final Object value : values )
		{
			boolean found = false;
			for ( final Field field : fields )
			{
				try
				{
					field.setAccessible( true );
					if ( value.equals( field.get( null ) ) )
					{
						result.add( field );
						found = true;
					}
				}
				catch ( final IllegalAccessException e )
				{
					throw new AssertionError( e );
				}
			}

			if ( !found )
			{
				throw new IllegalArgumentException( "Value not found: " + value );
			}
		}


		return result;
	}

	/**
	 * Get list of constant field names by their value. This is useful when
	 * trying to find constants without using explicit names.
	 *
	 * @param forClass Class to get the constants from.
	 * @param values   Constant values.
	 *
	 * @return List of constant names.
	 */
	@NotNull
	public static Set<String> getConstantNames( @NotNull final Class<?> forClass, @NotNull final Object... values )
	{
		return getConstantNames( forClass, Arrays.asList( values ) );
	}

	/**
	 * Get list of constant field names by their value. This is useful when
	 * trying to find constants without using explicit names.
	 *
	 * @param forClass Class to get the constants from.
	 * @param values   Constant values.
	 *
	 * @return List of constant names.
	 */
	@NotNull
	public static Set<String> getConstantNames( @NotNull final Class<?> forClass, @NotNull final Collection<?> values )
	{
		return getFieldNames( getConstantFieldsByValue( forClass, values ) );
	}

	/**
	 * Get list of instance fields from the specified class.
	 *
	 * @param forClass      Class to get the instances from.
	 * @param publicOnly    Return only public fields.
	 * @param ignoredFields Names of fields to ignore.
	 *
	 * @return List of instances.
	 */
	@NotNull
	public static List<Field> getInstanceFields( @NotNull final Class<?> forClass, final boolean publicOnly, @NotNull final String... ignoredFields )
	{
		return getFields( forClass, true, false, true, publicOnly, null, ignoredFields );
	}

	/**
	 * Get list of fields from the specified class.
	 *
	 * @param forClass         Class to get the constants from.
	 * @param includeAncestors Include fields of ancestor classes.
	 * @param includeStatic    If set, include static fields.
	 * @param includeInstance  If set, include non-static fields.
	 * @param publicOnly       Return only public fields.
	 * @param typeFilter       Return only fields of this type ({@code null} =
	 *                         any type).
	 * @param ignoredFields    Names of fields to ignore.
	 *
	 * @return List of constants.
	 */
	@NotNull
	public static List<Field> getFields( @NotNull final Class<?> forClass, final boolean includeAncestors, final boolean includeStatic, final boolean includeInstance, final boolean publicOnly, @Nullable final Class<?> typeFilter, @NotNull final String... ignoredFields )
	{
		return getFields( forClass, includeAncestors, includeStatic, includeInstance, publicOnly, typeFilter, Arrays.asList( ignoredFields ) );
	}

	/**
	 * Get list of fields from the specified class.
	 *
	 * @param forClass         Class to get the constants from.
	 * @param includeStatic    If set, include static fields.
	 * @param includeInstance  If set, include non-static fields.
	 * @param publicOnly       Return only public fields.
	 * @param includeAncestors Include fields of ancestor classes.
	 * @param typeFilter       Return only fields of this type ({@code null} =
	 *                         any type).
	 * @param ignoredFields    Names of fields to ignore.
	 *
	 * @return List of constants.
	 */
	@NotNull
	public static List<Field> getFields( @NotNull final Class<?> forClass, final boolean includeAncestors, final boolean includeStatic, final boolean includeInstance, final boolean publicOnly, @Nullable final Class<?> typeFilter, @NotNull final Collection<String> ignoredFields )
	{
		int ignoreCount = 0;

		List<Field> fields = Arrays.asList( forClass.getDeclaredFields() );
		if ( includeAncestors )
		{
			fields = new ArrayList<Field>( fields );
			for ( Class<?> ancestor = forClass.getSuperclass(); ancestor != null; ancestor = ancestor.getSuperclass() )
			{
				fields.addAll( Arrays.asList( ancestor.getDeclaredFields() ) );
			}
		}

		final List<Field> result = new ArrayList<Field>( fields.size() - ignoredFields.size() );

		for ( final Field field : fields )
		{
			//noinspection ObjectEquality
			if ( !includeAncestors && ( forClass != field.getDeclaringClass() ) )
			{
				continue;
			}

			final String name = field.getName();
			if ( ignoredFields.contains( name ) )
			{
				ignoreCount++;
			}
			else
			{
				final int modifiers = field.getModifiers();

				if ( ( Modifier.isStatic( modifiers ) ? includeStatic : includeInstance ) &&
				     ( !publicOnly || Modifier.isPublic( modifiers ) ) &&
				     ( ( typeFilter == null ) || typeFilter.isAssignableFrom( field.getType() ) ) )
				{
					result.add( field );
				}
			}
		}

		if ( ignoredFields.size() != ignoreCount )
		{
			throw new IllegalArgumentException( "Not all ignored fields were found: " + ignoredFields );
		}

		return result;
	}

	/**
	 * Get fields names from a collection of fields.
	 *
	 * @param fields Fields to get names from.
	 *
	 * @return Field names.
	 */
	public static Set<String> getFieldNames( final Iterable<Field> fields )
	{
		final Set<String> result = new HashSet<String>();

		for ( final Field field : fields )
		{
			result.add( field.getName() );
		}

		return result;
	}

	/**
	 * Test constants defined by the specified class. String constants should
	 * always have the same value as their name. Public static fields should
	 * always be final (we hate globals!) and non-transient (which makes no
	 * sense for static fields).
	 *
	 * @param forClass       Class to test the constants for.
	 * @param allowNonString If set, allow constants of other types than {@code
	 *                       String}.
	 * @param ignoredFields  Names of fields to ignore.
	 */
	public static void testConstants( @NotNull final Class<?> forClass, final boolean allowNonString, @NotNull final String... ignoredFields )
	{
		testConstants( forClass, allowNonString, Arrays.asList( ignoredFields ) );
	}

	/**
	 * Test constants defined by the specified class. String constants should
	 * always have the same value as their name. Public static fields should
	 * always be final (we hate globals!) and non-transient (which makes no
	 * sense for static fields).
	 *
	 * @param forClass       Class to test the constants for.
	 * @param allowNonString If set, allow constants of other types than {@code
	 *                       String}.
	 * @param ignoredFields  Names of fields to ignore.
	 */
	public static void testConstants( @NotNull final Class<?> forClass, final boolean allowNonString, @NotNull final Collection<String> ignoredFields )
	{
		final List<Field> fields = getConstantFields( forClass, true, ignoredFields );
		for ( final Field field : fields )
		{
			final String name = field.getName();
			final int modifiers = field.getModifiers();
			final Class<?> type = field.getType();

			if ( Modifier.isVolatile( modifiers ) || Modifier.isTransient( modifiers ) || !Modifier.isFinal( modifiers ) )
			{
				fail( "Invalid modifiers '" + Modifier.toString( modifiers ) + "' for constant: " + forClass.getName() + '.' + name );
			}

			if ( String.class.equals( type ) )
			{
				try
				{
					//noinspection TypeMayBeWeakened
					final String value = (String)field.get( null );
					if ( !value.equals( name ) )
					{
						final String constantNameForValue = TextTools.camelToUpperCase( value );
						if ( !constantNameForValue.equals( name ) )
						{
							fail( "String constant " + forClass.getName() + '.' + name + "' does not match value '" + value + '\'' + ( !constantNameForValue.equals( value ) ? " (could also have been '" + constantNameForValue + "')" : "" ) );
						}
					}
				}
				catch ( final Exception e )
				{
					fail( "failed to get value of '" + forClass.getName() + '.' + name + "' constant (" + e + ')' );
				}
			}
			else
			{
				if ( !allowNonString )
				{
					fail( "non-String constant '" + forClass.getName() + '.' + name + "' detected" );
				}
			}
		}
	}

	/**
	 * Get list of public non-transient fields of the specified class.
	 *
	 * @param forClass      Class to test the fields for.
	 * @param ignoredFields Names of fields to ignore.
	 *
	 * @return List of public non-transient fields of the specified class.
	 *
	 * @deprecated This method is does some magic exceptions. Invoker should do
	 * this.
	 */
	@Deprecated
	public static List<String> getFieldList( final Class<?> forClass, @NotNull final String... ignoredFields )
	{
		final List<Field> fields = getFields( forClass, true, true, true, true, null, ignoredFields );

		final List<String> result = new ArrayList<String>( fields.size() );

		for ( final Field field : fields )
		{
			final int modifiers = field.getModifiers();
			final String name = field.getName();
			final Class<?> type = field.getType();

			try
			{
				if ( !Modifier.isTransient( modifiers ) )
				{
					if ( Modifier.isStatic( modifiers ) )
					{
						if ( String.class.equals( type ) && !"TABLE_NAME".equals( name ) && !name.endsWith( "_CREATE_STATEMENT" ) && !"LIGHTWEIGHT_FIELDS".equals( name ) )
						{
							result.add( (String)field.get( null ) );
						}
					}
					else
					{
						result.add( name );
					}
				}
			}
			catch ( final Exception e )
			{ /* ignore invalid fields */ }
		}

		return result;
	}
}
