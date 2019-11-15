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

import java.lang.reflect.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class contains utility methods for working with
 * {@link java.lang.reflect Java reflection}.
 *
 * @author Peter S. Heijnen
 */
public class ReflectionTools
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private ReflectionTools()
	{
	}

	/**
	 * Create instance of the given class using its default constructor.
	 *
	 * @param clazz Class to create instance of.
	 * @param <T>   Bean class.
	 *
	 * @return Instance of requested class.
	 */
	@NotNull
	public static <T> T newInstance( @NotNull final Class<? extends T> clazz )
	{
		try
		{
			final Constructor<? extends T> defaultConstructor = clazz.getConstructor();
			return defaultConstructor.newInstance();
		}
		catch ( final InstantiationException e )
		{
			throw new IllegalArgumentException( "The '" + clazz.getSimpleName() + "' class can not be instantiated", e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( "Default constructor of '" + clazz.getSimpleName() + "' is not public", e );
		}
		catch ( final NoSuchMethodException e )
		{
			throw new IllegalArgumentException( "The '" + clazz.getSimpleName() + "' class has no default constructor", e );
		}
		catch ( final InvocationTargetException e )
		{
			throw new IllegalArgumentException( "Default constructor of '" + clazz.getSimpleName() + "' class threw an exception", e );
		}
	}

	/**
	 * Create instance of object using the given constructor.
	 *
	 * @param constructor Object constructor.
	 * @param arguments   Constructor arguments.
	 * @param <T>         Bean class.
	 *
	 * @return Instance of requested class.
	 */
	@NotNull
	public static <T> T newInstance( @NotNull final Constructor<T> constructor, @NotNull final Object... arguments )
	{
		try
		{
			return constructor.newInstance( arguments );
		}
		catch ( final InstantiationException e )
		{
			throw new IllegalArgumentException( "The '" + constructor.getDeclaringClass() + "' class can not be instantiated", e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( "The '" + constructor + "' constructor is not accessible", e );
		}
		catch ( final InvocationTargetException e )
		{
			final Throwable cause = e.getCause();
			if ( cause instanceof RuntimeException )
			{
				throw (RuntimeException)cause;
			}
			if ( cause instanceof Error )
			{
				throw (Error)cause;
			}
			throw new RuntimeException( "exception thrown by '" + constructor + "' constructor", e );
		}
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
	@NotNull
	public static Set<String> getFieldNames( @NotNull final Iterable<Field> fields )
	{
		final Set<String> result = new HashSet<String>();

		for ( final Field field : fields )
		{
			result.add( field.getName() );
		}

		return result;
	}

	/**
	 * Call {@link Field#get(Object)} method, but replace checked exceptions
	 * with unchecked exceptions.
	 *
	 * @param field  Field to get value from.
	 * @param object Object to get field value from ({@code null} for static field).
	 *
	 * @return Field value.
	 *
	 * @see Field#get(Object)
	 */
	public static Object getField( @NotNull final Field field, @Nullable final Object object )
	{
		try
		{
			return field.get( object );
		}
		catch ( final IllegalAccessException e )
		{
			if ( Modifier.isStatic( field.getModifiers() ) )
			{
				throw new IllegalArgumentException( "get access denied to static '" + field.getDeclaringClass().getName() + "." + field.getName() + "' field", e );
			}
			else
			{
				throw new IllegalArgumentException( "get access denied to '" + field.getDeclaringClass().getName() + "." + field.getName() + "' field for " + ( ( object != null ) ? object.getClass().getName() + '@' + Integer.toHexString( System.identityHashCode( object ) ) : "null" ) + " object", e );
			}
		}
	}

	/**
	 * Call {@link Field#set(Object, Object)} method, but replace checked
	 * exceptions with unchecked exceptions.
	 *
	 * @param field  Field to set value of.
	 * @param object Object to set field value of ({@code null} for static field).
	 * @param value  Value to set.
	 *
	 * @see Field#set(Object, Object)
	 */
	public static void setField( @NotNull final Field field, @Nullable final Object object, @Nullable final Object value )
	{
		try
		{
			field.set( object, value );
		}
		catch ( final IllegalAccessException e )
		{
			if ( Modifier.isStatic( field.getModifiers() ) )
			{
				throw new IllegalArgumentException( "set access denied to static '" + field.getDeclaringClass().getName() + "." + field.getName() + "' field and value " + value, e );
			}
			else
			{
				throw new IllegalArgumentException( "set access denied to '" + field.getDeclaringClass().getName() + "." + field.getName() + "' field for " + ( ( object != null ) ? object.getClass().getName() + '@' + Integer.toHexString( System.identityHashCode( object ) ) : "null" ) + " object and value " + value, e );
			}
		}
	}

	/**
	 * Call {@link Method#invoke(Object, Object...)} method, but replace checked
	 * exceptions with unchecked exceptions.
	 *
	 * @param method    Method to invoke.
	 * @param object    Object to invoke method of ({@code null} for static method).
	 * @param arguments Method arguments.
	 *
	 * @return Method return value.
	 *
	 * @see Method#invoke(Object, Object...)
	 */
	@SuppressWarnings( "ThrowInsideCatchBlockWhichIgnoresCaughtException" )
	public static Object invokeMethod( @NotNull final Method method, @Nullable final Object object, @NotNull final Object... arguments )
	{
		try
		{
			return method.invoke( object, arguments );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( "access denied to '" + method.getDeclaringClass().getName() + "." + method.getName() + "' method of " + ( ( object != null ) ? object.getClass().getName() + '@' + Integer.toHexString( System.identityHashCode( object ) ) : "null" ) + " object", e );
		}
		catch ( final InvocationTargetException e )
		{
			final Throwable cause = e.getCause();
			if ( cause instanceof RuntimeException )
			{
				throw (RuntimeException)cause;
			}
			if ( cause instanceof Error )
			{
				throw (Error)cause;
			}
			throw new RuntimeException( "exception thrown by '" + method.getDeclaringClass().getName() + "." + method.getName() + "' method of " + ( ( object != null ) ? object.getClass().getName() + '@' + Integer.toHexString( System.identityHashCode( object ) ) : "null" ) + " object", cause );
		}
	}
}
