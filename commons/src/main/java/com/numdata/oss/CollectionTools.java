/*
 * Copyright (c) 2009-2019, Numdata BV, The Netherlands.
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
import java.math.*;
import java.util.*;

import com.numdata.oss.ensemble.*;
import org.jetbrains.annotations.*;

/**
 * This class contains utility methods for working with collections.
 *
 * @author Peter S. Heijnen
 */
public class CollectionTools
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private CollectionTools()
	{
	}

	/**
	 * Add element to grouped collections.
	 *
	 * @param result     Resulting groups mapped by key.
	 * @param groupKey   Element group.
	 * @param element    Element to add.
	 * @param groupClass Type of newly created groups.
	 * @param <K>        Group key type.
	 * @param <E>        Element type.
	 * @param <G>        Group type.
	 *
	 * @return Group to which the element was added.
	 */
	@NotNull
	public static <K, E, G extends Collection<? super E>> G addToGroupedCollection( @NotNull final Map<K, G> result, final K groupKey, final E element, @SuppressWarnings( "rawtypes" ) @NotNull final Class<? extends Collection> groupClass )
	{
		G group = result.get( groupKey );
		if ( group == null )
		{
			try
			{
				group = (G)groupClass.getConstructor().newInstance();
			}
			catch ( final InstantiationException e )
			{
				throw new IllegalArgumentException( "Bad group class: " + groupClass, e );
			}
			catch ( final IllegalAccessException e )
			{
				throw new IllegalArgumentException( "Bad group class: " + groupClass, e );
			}
			catch ( final NoSuchMethodException e )
			{
				throw new IllegalArgumentException( "Bad group class: " + groupClass, e );
			}
			catch ( final InvocationTargetException e )
			{
				throw new IllegalArgumentException( "Bad group class: " + groupClass, e );
			}

			result.put( groupKey, group );
		}

		group.add( element );

		return group;
	}

	/**
	 * Add element to grouped list.
	 *
	 * @param result   Resulting list mapped by key.
	 * @param groupKey Element group.
	 * @param element  Element to add.
	 * @param <K>      Group key type.
	 * @param <E>      Element type.
	 *
	 * @return List to which the element was added.
	 */
	@NotNull
	public static <K, E> List<E> addToGroupedList( @NotNull final Map<K, List<E>> result, final K groupKey, final E element )
	{
		return addToGroupedCollection( result, groupKey, element, ArrayList.class );
	}

	/**
	 * Add element to grouped set.
	 *
	 * @param result   Resulting set mapped by key.
	 * @param groupKey Element group.
	 * @param element  Element to add.
	 * @param <K>      Group key type.
	 * @param <E>      Element type.
	 *
	 * @return Set to which the element was added.
	 */
	@NotNull
	public static <K, E> Set<E> addToGroupedSet( @NotNull final Map<K, Set<E>> result, final K groupKey, final E element )
	{
		return addToGroupedCollection( result, groupKey, element, HashSet.class );
	}

	/**
	 * Adds the given value to the map's current value of the given key. If the
	 * map contains no entry for the given key, the implicit value of the key is
	 * zero.
	 *
	 * @param map   Map to be modified.
	 * @param key   Key to change the value of.
	 * @param value Amount to be added to the current value of the key.
	 *
	 * @return The value that is stored in the map.
	 */
	public static <K> int addToValue( final Map<K, Integer> map, final K key, final int value )
	{
		final Integer oldValue = map.get( key );
		final int result = ( oldValue != null ) ? value + oldValue : value;
		map.put( key, result );
		return result;
	}

	/**
	 * Adds the given value to the map's current value of the given key. If the
	 * map contains no entry for the given key, the implicit value of the key is
	 * zero.
	 *
	 * @param map   Map to be modified.
	 * @param key   Key to change the value of.
	 * @param value Amount to be added to the current value of the key.
	 *
	 * @return The value that is stored in the map.
	 */
	public static <K> float addToValue( final Map<K, Float> map, final K key, final float value )
	{
		final Float oldValue = map.get( key );
		final float result = ( oldValue != null ) ? value + oldValue : value;
		map.put( key, result );
		return result;
	}

	/**
	 * Adds the given value to the map's current value of the given key. If the
	 * map contains no entry for the given key, the implicit value of the key is
	 * zero.
	 *
	 * @param map   Map to be modified.
	 * @param key   Key to change the value of.
	 * @param value Amount to be added to the current value of the key.
	 *
	 * @return The value that is stored in the map.
	 */
	public static <K> BigDecimal addToValue( final Map<K, BigDecimal> map, final K key, final BigDecimal value )
	{
		final BigDecimal oldValue = map.get( key );
		final BigDecimal result = ( oldValue != null ) ? oldValue.add( value ) : value;
		map.put( key, result );
		return result;
	}

	/**
	 * Adds the given value to the map's current value of the given key. If the
	 * map contains no entry for the given key, the implicit value of the key is
	 * zero.
	 *
	 * @param map   Map to be modified.
	 * @param key   Key to change the value of.
	 * @param value Amount to be added to the current value of the key.
	 *
	 * @return The value that is stored in the map.
	 */
	public static <K> double addToValue( final Map<K, Double> map, final K key, final double value )
	{
		final Double oldValue = map.get( key );
		final double result = ( oldValue != null ) ? value + oldValue : value;
		map.put( key, result );
		return result;
	}

	/**
	 * Get values of the specified field from a collection.
	 *
	 * @param collection Collection of objects to get values from.
	 * @param fieldName  Name of field whose values to get.
	 * @param fieldType  Field type.
	 *
	 * @return List of values from collection.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <T> List<T> getFieldValues( @NotNull final Collection<?> collection, @NotNull final String fieldName, @NotNull final Class<T> fieldType )
	{
		return getFieldValues( new ArrayList<T>( collection.size() ), collection, fieldName, fieldType );
	}

	/**
	 * Get values of the specified field from a collection into another
	 * collection.
	 *
	 * @param result     Collection to store result in.
	 * @param collection Collection of objects to get values from.
	 * @param fieldName  Name of field whose values to get.
	 * @param fieldType  Field type.
	 *
	 * @return {@code result} argument value.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <T, C extends Collection<? super T>> C getFieldValues( @NotNull final C result, @NotNull final Iterable<?> collection, @NotNull final String fieldName, @NotNull final Class<T> fieldType )
	{
		try
		{
			Class<?> lastClass = null;
			Field field = null;

			for ( final Object element : collection )
			{
				if ( element == null )
				{
					throw new IllegalArgumentException( "null element" );
				}

				final Class<?> elementClass = element.getClass();
				//noinspection ObjectEquality
				if ( elementClass != lastClass )
				{
					field = elementClass.getField( fieldName );
					lastClass = elementClass;
				}

				if ( field != null )
				{
					result.add( fieldType.cast( field.get( element ) ) );
				}
			}

			return result;
		}
		catch ( final NoSuchFieldException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	/**
	 * Get values of the specified bean property from a collection.
	 *
	 * @param collection   Collection of objects to get values from.
	 * @param propertyName Name of property whose values to get.
	 * @param propertyType Property type.
	 *
	 * @return New collection with property values.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <T> List<T> getPropertyValues( @NotNull final Collection<?> collection, @NotNull final String propertyName, @NotNull final Class<T> propertyType )
	{
		return getPropertyValues( new ArrayList<T>( collection.size() ), collection, propertyName, propertyType );
	}

	/**
	 * Get values of the specified bean property from a collection into another
	 * collection.
	 *
	 * @param result       Collection to store result in.
	 * @param collection   Collection of objects to get values from.
	 * @param propertyName Name of property whose values to get.
	 * @param propertyType Property type.
	 *
	 * @return {@code result} argument value.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <T, C extends Collection<? super T>> C getPropertyValues( @NotNull final C result, @NotNull final Iterable<?> collection, @NotNull final String propertyName, @NotNull final Class<T> propertyType )
	{
		//noinspection ObjectEquality
		final String methodName = ( ( propertyType == Boolean.class ) ? "is" : "get" ) + Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 );

		try
		{
			Class<?> lastClass = null;
			Method getter = null;

			for ( final Object element : collection )
			{
				if ( element == null )
				{
					throw new IllegalArgumentException( "null element" );
				}

				final Class<?> elementClass = element.getClass();
				//noinspection ObjectEquality
				if ( elementClass != lastClass )
				{
					lastClass = elementClass;
					getter = elementClass.getMethod( methodName );
				}

				if ( getter != null )
				{
					result.add( propertyType.cast( getter.invoke( element ) ) );
				}
			}

			return result;
		}
		catch ( final InvocationTargetException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	/**
	 * Get integer values of the specified field from a collection.
	 *
	 * @param collection Collection of objects to get values from.
	 * @param fieldName  Name of field whose values to get.
	 *
	 * @return Arrays with integer values from collection.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static int[] getIntFieldValues( @NotNull final Collection<?> collection, @NotNull final String fieldName )
	{
		try
		{
			final int[] result = new int[ collection.size() ];

			Class<?> lastClass = null;
			Field field = null;
			int resultIndex = 0;

			for ( final Object element : collection )
			{
				if ( element == null )
				{
					throw new IllegalArgumentException( "element" );
				}

				final Class<?> elementClass = element.getClass();
				//noinspection ObjectEquality
				if ( ( field == null ) || ( elementClass != lastClass ) )
				{
					field = elementClass.getField( fieldName );
					lastClass = elementClass;
				}

				result[ resultIndex++ ] = ( (Number)field.get( element ) ).intValue();
			}

			return result;
		}
		catch ( final NoSuchFieldException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	/**
	 * Get {@link Collection} class for the specified element types.
	 *
	 * @param <E> Element type.
	 *
	 * @return {@link Class} for {@link Collection}
	 */
	public static <E> Class<Collection<E>> getCollectionClass()
	{
		final Class<?> collectionClass = Collection.class;
		return (Class<Collection<E>>)collectionClass;
	}

	/**
	 * Get first element from {@link Iterable}.
	 *
	 * @param iterable Iterable to get first element from.
	 *
	 * @return First element by iterator of {@code iterable}.
	 *
	 * @throws NoSuchElementException if the iterable has no elements.
	 */
	public static <T> T getFirst( @NotNull final Iterable<? extends T> iterable )
	{
		final Iterator<? extends T> itereator = iterable.iterator();
		return itereator.next();
	}

	/**
	 * Get {@link List} class for the specified element types.
	 *
	 * @param <E> Element type.
	 *
	 * @return {@link Class} for {@link List}
	 */
	public static <E> Class<List<E>> getListClass()
	{
		final Class<?> listClass = List.class;
		return (Class<List<E>>)listClass;
	}

	/**
	 * Get {@link Map} class for the specified key and value types.
	 *
	 * @param <K> Key type.
	 * @param <V> Value type.
	 *
	 * @return {@link Class} for {@link Map}
	 */
	public static <K, V> Class<Map<K, V>> getMapClass()
	{
		final Class<?> mapClass = Map.class;
		return (Class<Map<K, V>>)mapClass;
	}

	/**
	 * Get {@link Set} class for the specified element types.
	 *
	 * @param <E> Element type.
	 *
	 * @return {@link Class} for {@link Set}
	 */
	public static <E> Class<Set<E>> getSetClass()
	{
		final Class<?> setClass = Set.class;
		return (Class<Set<E>>)setClass;
	}

	/**
	 * Get elements in a collections mapped by the value of the specified field
	 * for each element. The original collection's element order remains intact
	 * as far as possible.
	 *
	 * @param collection Collection of objects to get values from.
	 * @param fieldName  Name of field whose values to get.
	 * @param fieldType  Field type.
	 *
	 * @return Map with elements mapped by field value.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <K, V> Map<K, V> getMappedByField( @NotNull final Collection<V> collection, @NotNull final String fieldName, @NotNull final Class<K> fieldType )
	{
		try
		{
			final Map<K, V> result = new LinkedHashMap<K, V>( collection.size() );

			Class<?> lastClass = null;
			Field field = null;

			for ( final V element : collection )
			{
				if ( element == null )
				{
					throw new IllegalArgumentException( "element" );
				}

				final Class<?> elementClass = element.getClass();
				//noinspection ObjectEquality
				if ( ( field == null ) || ( elementClass != lastClass ) )
				{
					field = elementClass.getField( fieldName );
					lastClass = elementClass;
				}

				result.put( fieldType.cast( field.get( element ) ), element );
			}

			return result;
		}
		catch ( final NoSuchFieldException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	/**
	 * Get elements in a collections mapped by the value of the specified field
	 * for each element. The original collection's element order remains intact
	 * as far as possible.
	 *
	 * @param collection Collection of objects to get values from.
	 * @param fieldName  Name of field whose values to get.
	 * @param fieldType  Field type.
	 *
	 * @return Map with elements mapped by field value.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <K, V> Map<K, List<V>> getGroupByField( @NotNull final Collection<V> collection, @NotNull final String fieldName, @NotNull final Class<K> fieldType )
	{
		return getGroupByField( new LinkedHashMap<K, List<V>>( collection.size() ), collection, fieldName, fieldType );
	}

	/**
	 * Get elements in a collections mapped by the value of the specified field
	 * for each element. The original collection's element order remains intact
	 * as far as possible.
	 *
	 * @param result     Collection to store result in.
	 * @param collection Collection of objects to get values from.
	 * @param fieldName  Name of field whose values to get.
	 * @param fieldType  Field type.
	 *
	 * @return Map with elements mapped by field value.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <K, V> Map<K, List<V>> getGroupByField( @NotNull final Map<K, List<V>> result, @NotNull final Iterable<V> collection, @NotNull final String fieldName, @NotNull final Class<K> fieldType )
	{
		try
		{
			Class<?> lastClass = null;
			Field field = null;

			for ( final V element : collection )
			{
				if ( element == null )
				{
					throw new IllegalArgumentException( "element" );
				}

				final Class<?> elementClass = element.getClass();
				//noinspection ObjectEquality
				if ( ( field == null ) || ( elementClass != lastClass ) )
				{
					field = elementClass.getField( fieldName );
					lastClass = elementClass;
				}

				final K key = fieldType.cast( field.get( element ) );

				List<V> group = result.get( key );
				if ( group == null )
				{
					group = new ArrayList<V>();
					result.put( key, group );
				}

				group.add( element );
			}

			return result;
		}
		catch ( final NoSuchFieldException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	/**
	 * Get element from a collections whose specified bean property has the
	 * given value. The first match is returned.
	 *
	 * @param collection   Collection of objects.
	 * @param propertyName Name of property whose values to match.
	 * @param value        Required property value.
	 *
	 * @return Element with the given value; {@code null} if element was not
	 * found.
	 */
	public static <T> T getByProperty( @NotNull final Iterable<? extends T> collection, @NotNull final String propertyName, @Nullable final Object value )
	{
		try
		{
			T result = null;

			Class<?> lastClass = null;
			Method getter = null;

			for ( final T element : collection )
			{
				if ( element == null )
				{
					throw new IllegalArgumentException( "element" );
				}

				final Class<?> elementClass = element.getClass();
				//noinspection ObjectEquality
				if ( ( getter == null ) || ( elementClass != lastClass ) )
				{
					getter = elementClass.getMethod( ( ( value instanceof Boolean ) ? "is" : "get" ) + Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 ) );
					lastClass = elementClass;
				}

				final Object elementValue = getter.invoke( element );
				if ( ( value == null ) ? ( elementValue == null ) : value.equals( elementValue ) )
				{
					result = element;
					break;
				}
			}

			//noinspection ConstantConditions
			return result;
		}
		catch ( final NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final InvocationTargetException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	/**
	 * Get elements in a collections mapped by the value of the specified bean
	 * property for each element. The original collection's element order
	 * remains intact as far as possible.
	 *
	 * @param collection   Collection of objects to get values from.
	 * @param propertyName Name of property whose values to get.
	 * @param propertyType Property type.
	 *
	 * @return Map with elements mapped by property value.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <K, V> Map<K, V> getMappedByProperty( @NotNull final Collection<V> collection, @NotNull final String propertyName, @NotNull final Class<K> propertyType )
	{
		return getMappedByProperty( new LinkedHashMap<K, V>( collection.size() ), collection, propertyName, propertyType );
	}

	/**
	 * Get elements in a collections mapped by the value of the specified bean
	 * property for each element. The original collection's element order
	 * remains intact as far as possible.
	 *
	 * @param result       Collection to store result in.
	 * @param collection   Collection of objects to get values from.
	 * @param propertyName Name of property whose values to get.
	 * @param propertyType Property type.
	 *
	 * @return Map with elements mapped by property value.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <K, V> Map<K, V> getMappedByProperty( final Map<K, V> result, @NotNull final Iterable<V> collection, @NotNull final String propertyName, @NotNull final Class<K> propertyType )
	{
		try
		{
			Class<?> lastClass = null;
			Method getter = null;

			for ( final V element : collection )
			{
				if ( element == null )
				{
					throw new IllegalArgumentException( "element" );
				}

				final Class<?> elementClass = element.getClass();
				//noinspection ObjectEquality
				if ( ( getter == null ) || ( elementClass != lastClass ) )
				{
					//noinspection ObjectEquality
					getter = elementClass.getMethod( ( ( propertyType == Boolean.class ) ? "is" : "get" ) + Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 ) );
					lastClass = elementClass;
				}

				result.put( propertyType.cast( getter.invoke( element ) ), element );
			}

			return result;
		}
		catch ( final NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final InvocationTargetException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	/**
	 * Get elements in a collections mapped by the value of the specified
	 * property for each element. The original collection's element order
	 * remains intact as far as possible.
	 *
	 * @param collection   Collection of objects to get values from.
	 * @param propertyName Name of property whose values to get.
	 * @param propertyType Property type.
	 *
	 * @return Map with elements mapped by property value.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <K, V> Map<K, List<V>> getGroupByProperty( @NotNull final Collection<V> collection, @NotNull final String propertyName, @NotNull final Class<K> propertyType )
	{
		return getGroupByProperty( new LinkedHashMap<K, List<V>>( collection.size() ), collection, propertyName, propertyType );
	}

	/**
	 * Get elements in a collections mapped by the value of the specified
	 * property for each element. The original collection's element order
	 * remains intact as far as possible.
	 *
	 * @param result       Collection to store result in.
	 * @param collection   Collection of objects to get values from.
	 * @param propertyName Name of property whose values to get.
	 * @param propertyType Property type.
	 *
	 * @return Map with elements mapped by property value.
	 *
	 * @throws IllegalArgumentException if an argument or element is {@code
	 * null}.
	 */
	public static <K, V> Map<K, List<V>> getGroupByProperty( @NotNull final Map<K, List<V>> result, @NotNull final Iterable<? extends V> collection, @NotNull final String propertyName, @NotNull final Class<K> propertyType )
	{
		try
		{
			Class<?> lastClass = null;
			Method getter = null;

			for ( final V element : collection )
			{
				if ( element == null )
				{
					throw new IllegalArgumentException( "element" );
				}

				final Class<?> elementClass = element.getClass();
				//noinspection ObjectEquality
				if ( ( getter == null ) || ( elementClass != lastClass ) )
				{
					//noinspection ObjectEquality
					getter = elementClass.getMethod( ( ( propertyType == Boolean.class ) ? "is" : "get" ) + Character.toUpperCase( propertyName.charAt( 0 ) ) + propertyName.substring( 1 ) );
					lastClass = elementClass;
				}

				final K key = propertyType.cast( getter.invoke( element ) );

				List<V> group = result.get( key );
				if ( group == null )
				{
					group = new ArrayList<V>();
					result.put( key, group );
				}

				group.add( element );
			}

			return result;
		}
		catch ( final NoSuchMethodException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final IllegalAccessException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
		catch ( final InvocationTargetException e )
		{
			throw new IllegalArgumentException( e.getMessage(), e );
		}
	}

	/**
	 * Replace first occurrence of a given element in a list with elements in
	 * another list. This has no effect if the element is not part of the list.
	 *
	 * @param list        List to replace element in.
	 * @param element     Element to replace.
	 * @param replacement Replacement of element.
	 * @param <E>         element type.
	 *
	 * @return {@code true} if the list contained the element.
	 */
	public static <E> boolean replace( @NotNull final List<E> list, @Nullable final E element, @NotNull final List<? extends E> replacement )
	{
		final boolean result;

		final int replacementSize = replacement.size();
		if ( replacementSize == 0 )
		{
			result = list.remove( element );
		}
		else
		{
			final int index = list.indexOf( element );
			result = ( index >= 0 );

			if ( result )
			{
				if ( replacementSize == 1 )
				{
					list.set( index, replacement.get( 0 ) );
				}
				else
				{
					list.remove( index );
					list.addAll( index, replacement );
				}
			}
		}

		return result;
	}

	/**
	 * Sorts a list while preserving the index-based relation with another list.
	 * In other words, {@code sorted} will be sorted according to the natural
	 * ordering of its elements, while preserving the set of all pairs {@code
	 * (sorted[i], related[i])}.
	 *
	 * This sort is guaranteed to be <em>stable</em>: equal elements will not be
	 * reordered as a result of the sort.
	 *
	 * @param sorted  List to be sorted.
	 * @param related List of related values to be re-ordered in exactly the
	 *                same way as {@code sorted}.
	 */
	public static <E extends Comparable<? super E>, R> void sortRelated( @NotNull final List<E> sorted, @NotNull final List<R> related )
	{
		sortRelatedImpl( sorted, related, new Comparator<Duet<E, R>>()
		{
			@Override
			public int compare( final Duet<E, R> o1, final Duet<E, R> o2 )
			{
				final E value1 = o1.getValue1();
				final E value2 = o2.getValue1();
				return value1.compareTo( value2 );
			}
		} );
	}

	/**
	 * Sorts a list while preserving the index-based relation with another list.
	 * In other words, {@code sorted} will be sorted according to the given
	 * comparator, while preserving the set of all pairs {@code (sorted[i],
	 * related[i])}.
	 *
	 * This sort is guaranteed to be <em>stable</em>: equal elements will not be
	 * reordered as a result of the sort.
	 *
	 * @param sorted     List to be sorted.
	 * @param related    List of related values to be re-ordered in exactly the
	 *                   same way as {@code sorted}.
	 * @param comparator Specifies the ordering of elements to be sorted.
	 */
	public static <E, R> void sortRelated( @NotNull final List<E> sorted, @NotNull final List<R> related, @NotNull final Comparator<? super E> comparator )
	{
		sortRelatedImpl( sorted, related, new Comparator<Duet<E, R>>()
		{
			@Override
			public int compare( final Duet<E, R> o1, final Duet<E, R> o2 )
			{
				return comparator.compare( o1.getValue1(), o2.getValue1() );
			}
		} );
	}

	/**
	 * Sorts a list while preserving the index-based relation with another list.
	 * In other words, {@code sorted} will be sorted while preserving the set of
	 * all pairs {@code (sorted[i], related[i])}.
	 *
	 * This sort is guaranteed to be <em>stable</em>: equal elements will not be
	 * reordered as a result of the sort.
	 *
	 * @param sorted          List to be sorted.
	 * @param related         List of related values to be re-ordered in exactly
	 *                        the same way as {@code sorted}.
	 * @param entryComparator Specifies the ordering of pairs.
	 */
	private static <E, R> void sortRelatedImpl( @NotNull final List<E> sorted, @NotNull final List<R> related, @NotNull final Comparator<Duet<E, R>> entryComparator )
	{
		if ( sorted.size() != related.size() )
		{
			throw new IllegalArgumentException( "sorted.size() != related.size()" );
		}

		//noinspection unchecked,rawtypes
		final Duet<E, R>[] entries = new Duet[ sorted.size() ];

		{
			final Iterator<E> sortedIterator = sorted.iterator();
			final Iterator<R> relatedIterator = related.iterator();

			for ( int i = 0; i < entries.length; i++ )
			{
				entries[ i ] = new BasicDuet<E, R>( sortedIterator.next(), relatedIterator.next() );
			}
		}

		Arrays.sort( entries, entryComparator );

		{
			final ListIterator<E> sortedIterator = sorted.listIterator();
			final ListIterator<R> relatedIterator = related.listIterator();

			for ( final Duet<E, R> entry : entries )
			{
				sortedIterator.next();
				relatedIterator.next();
				sortedIterator.set( entry.getValue1() );
				relatedIterator.set( entry.getValue2() );
			}
		}
	}

	/**
	 * Concatenates two collections, appending the elements in {@code source} to
	 * {@code destination}.
	 *
	 * Both arguments are optional. If no destination collection is given, a new
	 * destination collection is created to add elements to. If no source
	 * collection is given, the destination collection is returned as-is. If
	 * both collections are {@code null}, the result is also {@code null}.
	 *
	 * @param destination Collection to add elements to.
	 * @param source      Collection get elements from.
	 *
	 * @return Concatenation of the two collections.
	 */
	@Nullable
	public static <T> List<T> concatenate( @Nullable final List<T> destination, @Nullable final Collection<? extends T> source )
	{
		final List<T> result;
		if ( destination == null )
		{
			result = ( source == null ) ? null : new ArrayList<T>( source );
		}
		else
		{
			result = destination;
			if ( source != null )
			{
				destination.addAll( source );
			}
		}
		return result;
	}

	/**
	 * Creates a map from the given array of key-value pairs.
	 *
	 * @param entries Contents of the map, as key-value pairs.
	 *
	 * @return Map for the given entries.
	 *
	 * @throws IllegalArgumentException if {@code entries} has an odd number of
	 * elements.
	 */
	public static <T> Map<T, T> createMap( @Nullable final T... entries )
	{
		final Map<T, T> result;
		if ( ( entries == null ) || ( entries.length == 0 ) )
		{
			result = Collections.emptyMap();
		}
		else if ( ( entries.length & 1 ) == 1 )
		{
			throw new IllegalArgumentException( "entries: length = " + entries.length );
		}
		else
		{
			result = createMap( Arrays.asList( entries ) );
		}
		return result;
	}

	/**
	 * Creates a map from the given list of key-value pairs.
	 *
	 * @param entries Contents of the map, as key-value pairs.
	 *
	 * @return Map for the given entries.
	 *
	 * @throws IllegalArgumentException if {@code entries} has an odd number of
	 * elements.
	 */
	public static <T> Map<T, T> createMap( @NotNull final List<T> entries )
	{
		final Map<T, T> result;
		if ( entries.isEmpty() )
		{
			result = Collections.emptyMap();
		}
		else if ( ( entries.size() & 1 ) == 1 )
		{
			throw new IllegalArgumentException( "entries: size() = " + entries.size() );
		}
		else
		{
			result = new HashMap<T, T>();
			for ( final Iterator<T> it = entries.iterator(); it.hasNext(); )
			{
				result.put( it.next(), it.next() );
			}
		}
		return result;
	}

	/**
	 * Wrap {@link Iterator} as {@link Iterable} whose {@link
	 * Iterable#iterator()} method can only be called once.
	 *
	 * @param iterator {@link Iterator} to wrap as {@link Iterable}.
	 * @param <T>      Element type.
	 *
	 * @return {@link Iterable}.
	 */
	public <T> Iterable<T> iterable( @NotNull final Iterator<T> iterator )
	{
		return new Iterable<T>()
		{
			boolean retrieved = false;

			@NotNull
			@Override
			public Iterator<T> iterator()
			{
				if ( retrieved )
				{
					throw new IllegalStateException( "Can only retrieve iterator once from 'CollectionTools.iterable(" + iterator + " )'" );
				}
				retrieved = true;
				return iterator;
			}
		};
	}
}
