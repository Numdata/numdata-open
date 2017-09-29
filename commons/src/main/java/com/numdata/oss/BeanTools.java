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
package com.numdata.oss;

import java.beans.*;
import java.lang.reflect.*;
import java.math.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class contains utility methods for working with Java beans.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "unused", "WeakerAccess" } )
public class BeanTools
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private BeanTools()
	{
	}

	/**
	 * Create instance of the given class using its default constructor.
	 *
	 * @param clazz Class to create instance of.
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
	 * Test whether the given class has a (public) field with the given name.
	 *
	 * @param clazz Class to test.
	 * @param name  Name of field to test.
	 *
	 * @return {@code true} if the class has the requested field.
	 */
	public static boolean isField( @NotNull final Class<?> clazz, @NotNull final String name )
	{
		return ( getField( clazz, name ) != null );
	}

	/**
	 * Gets a (public instance) field with the given name from the given class.
	 *
	 * @param clazz Class to get the field from.
	 * @param name  Name of field to get.
	 *
	 * @return Field from class; {@code null} if no such field was found.
	 */
	@Nullable
	public static Field getField( @NotNull final Class<?> clazz, @NotNull final String name )
	{
		Field result = null;
		try
		{
			final Field field = clazz.getField( name );
			final int modifiers = field.getModifiers();
			if ( Modifier.isPublic( modifiers ) && !Modifier.isStatic( modifiers ) )
			{
				result = field;
			}
		}
		catch ( final NoSuchFieldException ignored )
		{
		}

		return result;
	}

	/**
	 * Gets a property value from a bean.
	 *
	 * @param bean         Bean to get property from.
	 * @param propertyName Name of property whose value to get.
	 *
	 * @return Bean property value.
	 *
	 * @throws IllegalArgumentException if the bean does not have the property.
	 */
	public static Object getProperty( @NotNull final Object bean, @NotNull final String propertyName )
	{
		final Method getter = getGetter( bean.getClass(), propertyName );
		if ( getter == null )
		{
			throw new IllegalArgumentException( bean + " of " + bean.getClass() + " does not have '" + propertyName + "' property" );
		}

		return getValue( getter, bean );
	}

	/**
	 * Test whether the given class has a bean property with the given name.
	 *
	 * @param clazz Class to test.
	 * @param name  Name of bean property to test.
	 *
	 * @return {@code true} if the class has the requested bean property.
	 */
	public static boolean isProperty( @NotNull final Class<?> clazz, @NotNull final String name )
	{
		return ( getPropertyDescriptor( clazz, name ) != null );
	}

	/**
	 * Get properties from the given bean.
	 *
	 * @param bean            Bean to get properties from.
	 * @param includeReadOnly If true, include read-only properties; if false,
	 *                        only return read-write properties.
	 *
	 * @return Bean property names from class.
	 */
	@NotNull
	public static Map<String, Object> getProperties( @NotNull final Object bean, final boolean includeReadOnly )
	{
		final Map<String, Object> result = new HashMap<String, Object>();

		for ( final PropertyDescriptor propertyDescriptor : getPropertyDescriptors( bean.getClass() ) )
		{
			if ( !"class".equals( propertyDescriptor.getName() ) )
			{
				final Method getter = propertyDescriptor.getReadMethod();
				if ( getter != null )
				{
					if ( includeReadOnly || ( propertyDescriptor.getWriteMethod() != null ) )
					{
						result.put( propertyDescriptor.getName(), getValue( getter, bean ) );
					}
				}
			}
		}

		return result;
	}

	/**
	 * Get names of bean properties from the given class.
	 *
	 * @param clazz Class to get the bean properties from.
	 *
	 * @return Bean property names from class.
	 */
	@NotNull
	public static Set<String> getPropertyNames( @NotNull final Class<?> clazz )
	{
		return getPropertyNames( clazz, true );
	}

	/**
	 * Get names of bean properties from the given class.
	 *
	 * @param clazz           Class to get the bean properties from.
	 * @param includeReadOnly If true, include read-only properties; if false,
	 *                        only return read-write properties.
	 * @param exceptions      Names of properties to exclude from result.
	 *
	 * @return Bean property names from class.
	 */
	@NotNull
	public static Set<String> getPropertyNames( @NotNull final Class<?> clazz, final boolean includeReadOnly, final String... exceptions )
	{
		final List<String> exceptionList = Arrays.asList( exceptions );
		final Set<String> result = new LinkedHashSet<String>();

		for ( final PropertyDescriptor propertyDescriptor : getPropertyDescriptors( clazz ) )
		{
			if ( !"class".equals( propertyDescriptor.getName() ) && !exceptionList.contains( propertyDescriptor.getName() ) &&
			     ( propertyDescriptor.getReadMethod() != null ) &&
			     ( includeReadOnly || ( propertyDescriptor.getWriteMethod() != null ) ) )
			{
				result.add( propertyDescriptor.getName() );
			}
		}

		return result;
	}

	/**
	 * Get property accessors for the given class.
	 *
	 * @param clazz      Class to get the bean property accessors from.
	 * @param exceptions Names of properties to exclude from result.
	 *
	 * @return Bean property accessors for class.
	 */
	@NotNull
	public static List<Accessor> getPropertyAccessors( @NotNull final Class<?> clazz, final String... exceptions )
	{
		return getPropertyAccessors( clazz, true, exceptions );
	}

	/**
	 * Get property accessors for the given class.
	 *
	 * @param clazz           Class to get the bean property accessors from.
	 * @param includeReadOnly If true, include read-only properties; if false,
	 *                        only return read-write properties.
	 * @param exceptions      Names of properties to exclude from result.
	 *
	 * @return Bean property accessors for class.
	 */
	@NotNull
	public static List<Accessor> getPropertyAccessors( @NotNull final Class<?> clazz, final boolean includeReadOnly, final String... exceptions )
	{
		final List<String> exceptionList = Arrays.asList( exceptions );
		final List<PropertyDescriptor> propertyDescriptors = getPropertyDescriptors( clazz );
		final List<Accessor> result = new ArrayList<Accessor>( propertyDescriptors.size() - exceptionList.size() - 1 );

		for ( final PropertyDescriptor propertyDescriptor : propertyDescriptors )
		{
			if ( !"class".equals( propertyDescriptor.getName() ) && !exceptionList.contains( propertyDescriptor.getName() ) )
			{
				final Method getter = propertyDescriptor.getReadMethod();
				if ( getter != null )
				{
					if ( includeReadOnly || ( propertyDescriptor.getWriteMethod() != null ) )
					{
						result.add( new Accessor( clazz, propertyDescriptor ) );
					}
				}
			}
		}

		return result;
	}

	/**
	 * Gets type of bean property with the given name from the given class.
	 *
	 * @param clazz        Class to get the bean property from.
	 * @param propertyName Bean property name to get type of.
	 *
	 * @return Type for bean property from class.
	 *
	 * @throws IllegalArgumentException if the bean property was not found.
	 */
	@Nullable
	public static Class<?> getPropertyType( @NotNull final Class<?> clazz, @NotNull final String propertyName )
	{
		final PropertyDescriptor propertyDescriptor = getPropertyDescriptor( clazz, propertyName );
		if ( propertyDescriptor == null )
		{
			throw new IllegalArgumentException( clazz + " does not have '" + propertyName + "' property" );
		}

		return propertyDescriptor.getPropertyType();
	}

	/**
	 * Gets a getter method to access the bean property with the given name from
	 * the given class.
	 *
	 * @param clazz        Class to get the getter from.
	 * @param propertyName Bean property name to get getter for.
	 *
	 * @return Getter for bean property from class; {@code null} if no such
	 * getter was found.
	 */
	@Nullable
	public static Method getGetter( @NotNull final Class<?> clazz, @NotNull final String propertyName )
	{
		final PropertyDescriptor propertyDescriptor = getPropertyDescriptor( clazz, propertyName );
		return ( propertyDescriptor != null ) ? propertyDescriptor.getReadMethod() : null;
	}

	/**
	 * Gets a setter method to access the bean property with the given name and
	 * type from the given class.
	 *
	 * @param clazz        Class to get the setter from.
	 * @param propertyName Bean property name to get setter for.
	 *
	 * @return Setter for bean property from class; {@code null} if no such
	 * setter was found.
	 */
	@Nullable
	public static Method getSetter( @NotNull final Class<?> clazz, @NotNull final String propertyName )
	{
		final PropertyDescriptor propertyDescriptor = getPropertyDescriptor( clazz, propertyName );
		return ( propertyDescriptor != null ) ? propertyDescriptor.getWriteMethod() : null;
	}

	/**
	 * Get property value using field.
	 *
	 * @param field Field to get value from.
	 * @param bean  Bean to get value from.
	 *
	 * @return Property value.
	 */
	public static Object getValue( @NotNull final Field field, @NotNull final Object bean )
	{
		try
		{
			return field.get( bean );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( "access denied to '" + field.getName() + "' field of " + bean + " of " + bean.getClass(), e );
		}
	}

	/**
	 * Set property value using field.
	 *
	 * @param field Field to set value in.
	 * @param bean  Bean to set value in.
	 * @param value Property value.
	 */
	public static void setValue( @NotNull final Field field, @NotNull final Object bean, final Object value )
	{
		try
		{
			field.set( bean, value );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( "access denied to '" + field.getName() + "' field of " + bean + " of " + bean.getClass(), e );
		}
	}

	/**
	 * Get property value using getter.
	 *
	 * @param getter Getter method.
	 * @param bean   Bean to get value from.
	 *
	 * @return Property value.
	 */
	public static Object getValue( @NotNull final Method getter, @NotNull final Object bean )
	{
		try
		{
			return getter.invoke( bean );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( "access denied to '" + getter.getName() + "' method of " + bean + " of " + bean.getClass(), e );
		}
		catch ( final InvocationTargetException e )
		{
			throw new IllegalArgumentException( "exception occurred while getting '" + getter.getName() + "' method of " + bean + " of " + bean.getClass(), e );
		}
	}

	/**
	 * Set property value using setter.
	 *
	 * @param setter Setter method.
	 * @param bean   Bean to set value in.
	 * @param value  Property value.
	 */
	public static void setValue( @NotNull final Method setter, @NotNull final Object bean, @Nullable final Object value )
	{
		try
		{
			setter.invoke( bean, value );
		}
		catch ( final IllegalArgumentException e )
		{
			throw new IllegalArgumentException( "failed to call " + setter.toString() + " with argument " + ( value == null ? null : value.getClass() ), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( "access denied to '" + setter.getName() + "' method of " + bean + " of " + bean.getClass(), e );
		}
		catch ( final InvocationTargetException e )
		{
			throw new IllegalArgumentException( "exception occurred while getting '" + setter.getName() + "' method of " + bean + " of " + bean.getClass(), e );
		}
	}

	/**
	 * Get {@link PropertyDescriptor} for the given bean property.
	 *
	 * @param clazz        Bean class.
	 * @param propertyName Bean property name.
	 *
	 * @return {@link PropertyDescriptor}; {@code null} if property not found.
	 */
	@Nullable
	public static PropertyDescriptor getPropertyDescriptor( final @NotNull Class<?> clazz, final @NotNull String propertyName )
	{
		PropertyDescriptor propertyDescriptor = null;
		for ( final PropertyDescriptor candidate : getPropertyDescriptors( clazz ) )
		{
			if ( propertyName.equals( candidate.getName() ) )
			{
				propertyDescriptor = candidate;
				break;
			}
		}
		return propertyDescriptor;
	}

	/**
	 * Get all {@link PropertyDescriptor property descriptors} for the given
	 * bean class.
	 *
	 * @param clazz Bean class.
	 *
	 * @return {@link PropertyDescriptor} list.
	 */
	@NotNull
	public static List<PropertyDescriptor> getPropertyDescriptors( final @NotNull Class<?> clazz )
	{
		final BeanInfo info;
		try
		{
			info = Introspector.getBeanInfo( clazz );
		}
		catch ( final IntrospectionException e )
		{
			throw new IllegalStateException( e );
		}
		return Arrays.asList( info.getPropertyDescriptors() );
	}

	/**
	 * This is a bean property accessor. It encapsulates information about and
	 * provides access to a single bean property.
	 */
	public static class Accessor
	{
		/**
		 * Bean class.
		 */
		@NotNull
		private final Class<?> _beanClass;

		/**
		 * PGetter method.
		 */
		@NotNull
		private final PropertyDescriptor _propertyDescriptor;

		/**
		 * Create property accessor.
		 *
		 * @param beanClass    Bean class.
		 * @param propertyName Property name.
		 */
		public Accessor( @NotNull final Class<?> beanClass, @NotNull final String propertyName )
		{
			_beanClass = beanClass;

			final PropertyDescriptor propertyDescriptor = BeanTools.getPropertyDescriptor( beanClass, propertyName );
			if ( propertyDescriptor == null )
			{
				throw new IllegalArgumentException( "Class '" + beanClass.getName() + "' has no '" + propertyName + "' property" );
			}
			_propertyDescriptor = propertyDescriptor;
		}

		/**
		 * Create property accessor.
		 *
		 * @param beanClass          Bean class.
		 * @param propertyDescriptor Property descriptor.
		 */
		public Accessor( @NotNull final Class<?> beanClass, @NotNull final PropertyDescriptor propertyDescriptor )
		{
			_beanClass = beanClass;
			_propertyDescriptor = propertyDescriptor;
		}

		/**
		 * Get bean class.
		 *
		 * @return Bean class.
		 */
		@NotNull
		public Class<?> getBeanClass()
		{
			return _beanClass;
		}

		/**
		 * Get property name.
		 *
		 * @return Property name.
		 */
		@NotNull
		public String getPropertyName()
		{
			return getPropertyDescriptor().getName();
		}

		@NotNull
		public PropertyDescriptor getPropertyDescriptor()
		{
			return _propertyDescriptor;
		}

		/**
		 * Gets type of bean property.
		 *
		 * @return Type of bean property.
		 */
		@SuppressWarnings( "ObjectEquality" )
		@NotNull
		public Class<?> getType()
		{
			Class<?> result = getPropertyDescriptor().getPropertyType();
			if ( result.isPrimitive() )
			{
				//noinspection ChainOfInstanceofChecks
				if ( result == boolean.class )
				{
					result = Boolean.class;
				}
				else if ( result == char.class )
				{
					result = Character.class;
				}
				else if ( result == byte.class )
				{
					result = Byte.class;
				}
				else if ( result == short.class )
				{
					result = Short.class;
				}
				else if ( result == int.class )
				{
					result = Integer.class;
				}
				else if ( result == long.class )
				{
					result = Long.class;
				}
				else if ( result == float.class )
				{
					result = Float.class;
				}
				else if ( result == double.class )
				{
					result = Double.class;
				}
				else if ( result == void.class )
				{
					throw new IllegalStateException( "void getter is not allowed" );
				}
			}
			return result;
		}

		/**
		 * Test whether this property is read-only.
		 *
		 * @return {@code true} if property is read-only; {@code false} if
		 * property read-write.
		 */
		public boolean isReadOnly()
		{
			return ( getPropertyDescriptor().getWriteMethod() == null );
		}

		/**
		 * Gets property value from a bean.
		 *
		 * @param bean Bean to get property from.
		 *
		 * @return Bean property value.
		 */
		public Object getValue( @NotNull final Object bean )
		{
			return BeanTools.getValue( getGetter(), bean );
		}

		/**
		 * Set property value using setter.
		 *
		 * @param bean  Bean to set value in.
		 * @param value Property value.
		 */
		public void setValue( @NotNull final Object bean, @Nullable final Object value )
		{
			final Method setter = getSetter();
			if ( setter == null )
			{
				throw new UnsupportedOperationException( "Property '" + getPropertyName() + "' of '" + bean + "' is read-only, so we can not set value " + TextTools.quote( value ) );
			}

			BeanTools.setValue( setter, bean, value );
		}

		/**
		 * Set property from string value using setter. This method
		 * automatically parses the string value to the appropriate basic type
		 * (wrapper) or well known value class.
		 *
		 * @param bean  Bean to set value in.
		 * @param value String value.
		 */
		@SuppressWarnings( { "ObjectEquality", "ChainOfInstanceofChecks" } )
		public void setValueFromString( @NotNull final Object bean, final String value )
		{
			final Class<?> type = getType();

			if ( value == null )
			{
				setValue( bean, null );
			}
			else if ( type == boolean.class || type == Boolean.class )
			{
				setValue( bean, Boolean.parseBoolean( value ) );
			}
			else if ( type == BigDecimal.class )
			{
				setValue( bean, new BigDecimal( value ) );
			}
			else if ( type == BigInteger.class )
			{
				setValue( bean, new BigInteger( value ) );
			}
			else if ( type == byte.class || type == Byte.class )
			{
				setValue( bean, Byte.parseByte( value ) );
			}
			else if ( type == double.class || type == Double.class )
			{
				setValue( bean, Double.parseDouble( value ) );
			}
			else if ( type == float.class || type == Float.class )
			{
				setValue( bean, Float.parseFloat( value ) );
			}
			else if ( type == int.class || type == Integer.class )
			{
				setValue( bean, Integer.parseInt( value ) );
			}
			else if ( type == long.class || type == Long.class )
			{
				setValue( bean, Long.parseLong( value ) );
			}
			else if ( type == short.class || type == Short.class )
			{
				setValue( bean, Short.parseShort( value ) );
			}
			else
			{
				setValue( bean, value );
			}
		}

		/**
		 * Gets getter method to access the bean property.
		 *
		 * @return Getter for bean property.
		 */
		@NotNull
		public Method getGetter()
		{
			final Method result = getPropertyDescriptor().getReadMethod();
			if ( result == null )
			{
				throw new IllegalArgumentException( getBeanClass() + " has write-only '" + getPropertyDescriptor().getName() + "' property" );
			}

			return result;
		}

		/**
		 * Gets setter method to access the bean property.
		 *
		 * @return Setter for bean property; {@code null} if bean is read-only.
		 */
		@Nullable
		public Method getSetter()
		{
			final PropertyDescriptor propertyDescriptor = getPropertyDescriptor();
			final Method result = propertyDescriptor.getWriteMethod();
			if ( result == null )
			{
				throw new IllegalArgumentException( getBeanClass() + " has read-only '" + propertyDescriptor.getName() + "' property" );
			}

			return result;
		}
	}
}
