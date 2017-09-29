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

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class contains utility methods for working with arrays. It offers some
 * of the essential functions of the {@link List} interface, handles {@code
 * null} values and can work on real arrays as well as {@link List} instances.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "ChainOfInstanceofChecks", "ObjectEquality" } )
public final class ArrayTools
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private ArrayTools()
	{
	}

	/**
	 * Check if the specified argument is {@code null}, an array, or a {@link
	 * List} instance. If not, generate a {@link IllegalArgumentException}.
	 *
	 * @param array Array or {@link List} to check.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	private static void checkType( @Nullable final Object array )
	{
		if ( ( array != null ) && !isValidType( array ) )
		{
			throw new IllegalArgumentException( "not array or list" );
		}
	}

	/**
	 * Check if the specified argument is an array.
	 *
	 * @param object Object to check.
	 *
	 * @return {@code true} if the {@code object} argument is an array; {@code
	 * false} otherwise.
	 */
	public static boolean isArray( final Object object )
	{
		final boolean result;

		if ( object == null )
		{
			result = false;
		}
		else
		{
			final Class<?> arrayClass = object.getClass();
			result = arrayClass.isArray();
		}

		return result;
	}

	/**
	 * Check if the specified argument is an array or a {@link List} instance.
	 *
	 * @param object Object to check.
	 *
	 * @return {@code true} if the {@code object} argument is an array or a
	 * {@link List} instance; {@code false} otherwise.
	 */
	public static boolean isValidType( final Object object )
	{
		return ( object instanceof List ) || isArray( object );
	}

	/**
	 * Clear contents of an array. This will reset all elements of an array to
	 * their default value ({@code 0}, {@code false}, {@code null}). If the
	 * array is {@code null} or has no elements, calling this method has no
	 * effect.
	 *
	 * IMPORTANT: The size of a {@link List} remains constant.
	 *
	 * @param array Array or {@link List} whose contents should be cleared.
	 */
	public static void clear( final Object array )
	{
		clear( array, 0, -1 );
	}

	/**
	 * Clear contents of an array starting in a specific range. This will reset
	 * all elements of an array in the specific range to their default value
	 * ({@code 0}, {@code false}, {@code null}). If the array is {@code null} or
	 * has no elements in the specified range, then calling this method has no
	 * effect.
	 *
	 * IMPORTANT: The size of a {@link List} remains constant.
	 *
	 * @param array      Array or {@link List} whose contents should be
	 *                   cleared.
	 * @param startIndex Index of first element to clear.
	 * @param endIndex   Index of element after last cleared element
	 *                   (out-of-range => use array length, e.g. -1).
	 *
	 * @throws ArrayIndexOutOfBoundsException if {@code startIndex} is
	 * negative.
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static void clear( final Object array, final int startIndex, final int endIndex )
	{
		checkType( array );

		if ( startIndex < 0 )
		{
			throw new ArrayIndexOutOfBoundsException( startIndex );
		}

		int index = getElementCount( array, endIndex );
		if ( index > startIndex )
		{
			if ( array instanceof List )
			{
				final List<Object> list = (List<Object>)array;
				final Object value = null;

				while ( --index >= startIndex )
				{
					list.set( index, value );
				}
			}
			else
			{
				final Class<?> arrayClass = array.getClass();
				final Class<?> componentType = arrayClass.getComponentType();

				if ( componentType == boolean.class )
				{
					final boolean value = false;
					while ( --index >= startIndex )
					{
						Array.setBoolean( array, index, value );
					}
				}
				else if ( componentType == byte.class )
				{
					final byte value = (byte)0;
					while ( --index >= startIndex )
					{
						Array.setByte( array, index, value );
					}
				}
				else if ( componentType == char.class )
				{
					final char value = '\0';
					while ( --index >= startIndex )
					{
						Array.setChar( array, index, value );
					}
				}
				else if ( componentType == double.class )
				{
					final double value = 0.0;
					while ( --index >= startIndex )
					{
						Array.setDouble( array, index, value );
					}
				}
				else if ( componentType == float.class )
				{
					final float value = 0.0f;
					while ( --index >= startIndex )
					{
						Array.setFloat( array, index, value );
					}
				}
				else if ( componentType == int.class )
				{
					final int value = 0;
					while ( --index >= startIndex )
					{
						Array.setInt( array, index, value );
					}
				}
				else if ( componentType == long.class )
				{
					final long value = 0L;
					while ( --index >= startIndex )
					{
						Array.setLong( array, index, value );
					}
				}
				else if ( componentType == short.class )
				{
					final short value = (short)0;
					while ( --index >= startIndex )
					{
						Array.setShort( array, index, value );
					}
				}
				else
				{
					final Object value = null;
					while ( --index >= startIndex )
					{
						Array.set( array, index, value );
					}
				}
			}
		}
	}

	/**
	 * Clone the specified array. Multi-dimensional arrays are cloned
	 * recursively (deep clone). If the array is {@code null}, then {@code null}
	 * will be returned aswell.
	 *
	 * @param array Array or {@link List} to clone.
	 *
	 * @return Cloned array or {@link List}; {@code null} if the specified array
	 * was {@code null}.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object clone( final Object array )
	{
		return clone( array, -1 );
	}

	/**
	 * Clone the specified array. Multi-dimensional arrays are cloned
	 * recursively (deep clone). If the array is {@code null}, then {@code null}
	 * will be returned as well.
	 *
	 * @param array        Array or {@link List} to clone.
	 * @param elementCount Current number of elements used (out-of-range => use
	 *                     array length, e.g. -1).
	 *
	 * @return Cloned array or {@link List}; {@code null} if the specified array
	 * was {@code null}.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	@Nullable
	public static Object clone( final Object array, final int elementCount )
	{
		final Object result;

		checkType( array );

		if ( array == null )
		{
			result = null;
		}
		else
		{
			final int length = getElementCount( array, elementCount );

			if ( array instanceof List )
			{
				final List<Object> oldList = (List<Object>)array;
				final List<Object> newList = new ArrayList<Object>( length );

				for ( int i = 0; i < length; i++ )
				{
					Object element = oldList.get( i );
					if ( isValidType( element ) )
					{
						element = clone( element, -1 );
					}

					newList.add( element );
				}

				result = newList;
			}
			else
			{
				final Class<?> componentType = getComponentType( array, null );

				result = Array.newInstance( componentType, length );
				if ( length > 0 )
				{
					if ( componentType.isArray() )
					{
						for ( int i = 0; i < length; i++ )
						{
							Array.set( result, i, clone( Array.get( array, i ), -1 ) );
						}
					}
					else
					{
						copy( array, 0, result, 0, length );
					}
				}
			}
		}

		return result;
	}

	/**
	 * Test if array contains the specified element.
	 *
	 * @param element Element to get index of.
	 * @param array   Array or {@link List} to find element in (may be {@code
	 *                null}).
	 *
	 * @return {@code true} if {@code array} contains {@code element}; {@code
	 * false} otherwise.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static boolean contains( final Object element, final Object array )
	{
		return ( indexOf( element, array ) >= 0 );
	}

	/**
	 * Copies an array from the specified source array, beginning at the
	 * specified position, to the specified position of the destination array.
	 *
	 * This method offers is a little more flexible type conversion than the
	 * {@link System#arraycopy} method, including: <ul>
	 *
	 * <li>any-to-{@link String}</li>
	 *
	 * <li>any-to-{@link Object}</li>
	 *
	 * <li>{@link Number} to numeric primitive</li>
	 *
	 * <li>{@link String} to boolean primitive</li>
	 *
	 * <li>{@link String} to numeric primitive</li></ul>
	 *
	 * During conversion, {@code null}-elements are handled appropriately
	 * (remain {@code null} for reference type, generate {@link
	 * NullPointerException} if trying to convert to primitive).
	 *
	 * @param src    Source array.
	 * @param srcPos Starting position in the source array.
	 * @param dst    Destination array.
	 * @param dstPos Starting position in the destination data.
	 * @param length Number of array elements to be copied.
	 *
	 * @throws IndexOutOfBoundsException if copying would cause access of data
	 * outside array bounds.
	 * @throws ArrayStoreException if an element in the {@code src} array could
	 * not be stored into the {@code dst} array because of a type mismatch.
	 * @throws NullPointerException if either {@code src} or {@code dst} is
	 * {@code null}.
	 * @see System#arraycopy
	 */
	public static void copy( @Nullable final Object src, final int srcPos, final Object dst, final int dstPos, final int length )
	{
		/*
		 * NOTE: Most calls to the original {@link System#arraycopy}
		 *       method below are simply used to generate the appropriate
		 *       exception (so that consistency is assured).
		 */
		if ( ( src != dst ) && ( src != null ) && ( srcPos >= 0 ) && ( dst != null ) && ( dstPos >= 0 ) && ( length > 0 ) )
		{
			final Class<?> srcClass = src.getClass();
			final Class<?> dstClass = dst.getClass();

			if ( srcClass.isArray() && dstClass.isArray() )
			{
				final Class<?> srcType = srcClass.getComponentType();
				final Class<?> dstType = dstClass.getComponentType();

				if ( !dstType.isAssignableFrom( srcType ) )
				{
					final int srcLen = Array.getLength( src );
					final int dstLen = Array.getLength( dst );

					if ( ( ( srcPos + length ) <= srcLen ) && ( ( dstPos + length ) <= dstLen ) )
					{
						if ( dstType == Object.class )
						{
							for ( int i = 0; i < length; i++ )
							{
								Array.set( dst, i, Array.get( src, i ) );
							}
						}
						else if ( dstType == String.class )
						{
							for ( int i = 0; i < length; i++ )
							{
								final Object element = Array.get( src, i );
								Array.set( dst, i, ( element == null ) ? null : String.valueOf( element ) );
							}
						}
						else if ( dstType == boolean.class )
						{
							if ( String.class == srcType )
							{
								for ( int i = 0; i < length; i++ )
								{
									final String element = (String)Array.get( src, srcPos + i );
									Array.setBoolean( dst, dstPos + i, "true".equalsIgnoreCase( element ) );
								}
							}
							else
							{
								System.arraycopy( src, srcPos, dst, dstPos, length );
							}
						}
						else if ( dstType == byte.class )
						{
							if ( String.class == srcType )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setByte( dst, dstPos + i, Byte.parseByte( (String)Array.get( src, i ) ) );
								}
							}
							else if ( Number.class.isAssignableFrom( srcType ) )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setByte( dst, dstPos + i, ( (Number)Array.get( src, srcPos + i ) ).byteValue() );
								}
							}
							else
							{
								System.arraycopy( src, srcPos, dst, dstPos, length );
							}
						}
						else if ( dstType == double.class )
						{
							if ( String.class == srcType )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setDouble( dst, dstPos + i, Double.parseDouble( (String)Array.get( src, i ) ) );
								}
							}
							else if ( Number.class.isAssignableFrom( srcType ) )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setDouble( dst, dstPos + i, ( (Number)Array.get( src, srcPos + i ) ).doubleValue() );
								}
							}
							else
							{
								System.arraycopy( src, srcPos, dst, dstPos, length );
							}
						}
						else if ( dstType == float.class )
						{
							if ( String.class == srcType )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setFloat( dst, dstPos + i, Float.parseFloat( (String)Array.get( src, i ) ) );
								}
							}
							else if ( Number.class.isAssignableFrom( srcType ) )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setFloat( dst, dstPos + i, ( (Number)Array.get( src, srcPos + i ) ).floatValue() );
								}
							}
							else
							{
								System.arraycopy( src, srcPos, dst, dstPos, length );
							}
						}
						else if ( dstType == int.class )
						{
							if ( String.class == srcType )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setInt( dst, dstPos + i, Integer.parseInt( (String)Array.get( src, i ) ) );
								}
							}
							else if ( Number.class.isAssignableFrom( srcType ) )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setInt( dst, dstPos + i, ( (Number)Array.get( src, srcPos + i ) ).intValue() );
								}
							}
							else
							{
								System.arraycopy( src, srcPos, dst, dstPos, length );
							}
						}
						else if ( dstType == long.class )
						{
							if ( String.class == srcType )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setLong( dst, dstPos + i, Long.parseLong( (String)Array.get( src, i ) ) );
								}
							}
							else if ( Number.class.isAssignableFrom( srcType ) )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setLong( dst, dstPos + i, ( (Number)Array.get( src, srcPos + i ) ).longValue() );
								}
							}
							else
							{
								System.arraycopy( src, srcPos, dst, dstPos, length );
							}
						}
						else if ( dstType == short.class )
						{
							if ( String.class == srcType )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setShort( dst, dstPos + i, Short.parseShort( (String)Array.get( src, i ) ) );
								}
							}
							else if ( Number.class.isAssignableFrom( srcType ) )
							{
								for ( int i = 0; i < length; i++ )
								{
									Array.setShort( dst, dstPos + i, ( (Number)Array.get( src, srcPos + i ) ).shortValue() );
								}
							}
							else
							{
								System.arraycopy( src, srcPos, dst, dstPos, length );
							}
						}
						else
						{
							System.arraycopy( src, srcPos, dst, dstPos, length );
						}
					}
					else
					{
						System.arraycopy( src, srcPos, dst, dstPos, length );
					}
				}
				else
				{
					System.arraycopy( src, srcPos, dst, dstPos, length );
				}
			}
			else
			{
				System.arraycopy( src, srcPos, dst, dstPos, length );
			}
		}
		else
		{
			System.arraycopy( src, srcPos, dst, dstPos, length );
		}
	}

	/**
	 * Test equality between arrays. This returns:<ul>
	 *
	 * <li>{@code true} if both arguments are {@code null}.</li>
	 *
	 * <li>{@code false} if one of the arguments is {@code null}.</li>
	 *
	 * <li>{@code true} if both arguments refer to the same object.</li>
	 *
	 * <li>{@code true} if both arguments are arrays of equal type, length, and
	 * content (tested recursively).</li>
	 *
	 * <li>{@code true} if either argument is not an array or if the argument
	 * types are unequal and {@code array1.equals( array2 )} returns {@code
	 * true}.</li>
	 *
	 *
	 * </ul>
	 *
	 * @param array1 First array to compare for equality.
	 * @param array2 Second array to compare for equality.
	 *
	 * @return {@code true} if the two arrays are equal or both {@code null}
	 * (see method comment for details); {@code false} otherwise.
	 */
	public static boolean equals( final Object array1, final Object array2 )
	{
		boolean result = false;

		if ( array1 == array2 )
		{
			result = true;
		}
		else if ( ( array1 != null ) && ( array2 != null ) )
		{
			final Class<?> type = array1.getClass();
			if ( !type.isArray() || !type.equals( array2.getClass() ) )
			{
				result = array1.equals( array2 );
			}
			else
			{
				final int length = Array.getLength( array1 );
				if ( length == Array.getLength( array2 ) )
				{
					result = true;
					for ( int i = 0; i < length; i++ )
					{
						final Object value1 = Array.get( array1, i );
						final Object value2 = Array.get( array2, i );

						if ( !equals( value1, value2 ) )
						{
							result = false;
							break;
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Return the length of the specified array or {@link List} object. If the
	 * array is {@code null} or is actually not a {@link List} or array, zero
	 * will be returned.
	 *
	 * @param array Array or {@link List} to get the length from (may be {@code
	 *              null} or unsuitable object).
	 *
	 * @return Length of array or {@link List}; {@code 0} if array is {@code
	 * null} or is not an array.
	 *
	 * @see Array#getLength
	 */
	public static int getLength( @Nullable final Object array )
	{
		final int result;

		if ( array == null )
		{
			result = 0;
		}
		else if ( array instanceof List )
		{
			result = ( (Collection<?>)array ).size();
		}
		else
		{
			final Class<?> arrayClass = array.getClass();
			result = arrayClass.isArray() ? Array.getLength( array ) : 0;
		}

		return result;
	}

	/**
	 * Set length of the specified array. If the array is {@code null} or has a
	 * different length, a new array is created with the specified length.
	 *
	 * @param array         Array or {@link List} object ({@code null} to create
	 *                      array).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param length        Length of resulting array.
	 *
	 * @return Array with the specified length; may return the original {@code
	 * array} argument.
	 *
	 * @throws IllegalArgumentException if {@code length} argument is negative
	 * or {@code array} is not an array or list.
	 */
	public static Object setLength( final Object array, final Class<?> componentType, final int length )
	{
		final Object result;

		checkType( array );

		if ( length < 0 )
		{
			throw new IllegalArgumentException( "length<0 (" + length + ')' );
		}

		if ( array instanceof List )
		{
			final List<?> list = (List<?>)array;

			int size = list.size();

			while ( size > length )
			{
				size--;
				list.remove( size );

			}

			while ( size < length )
			{
				list.add( null );
				size++;
			}

			result = list;
		}
		else
		{
			final int oldLength = getLength( array );
			if ( ( array == null ) || ( oldLength != length ) )
			{
				result = Array.newInstance( getComponentType( array, componentType ), length );
				final int toCopy = Math.min( length, oldLength );
				if ( toCopy > 0 )
				{
					copy( array, 0, result, 0, toCopy );
				}
			}
			else
			{
				result = array;
			}
		}

		return result;
	}

	/**
	 * Set length of the specified array. If the array is {@code null} and a
	 * positive length is specified, a new array will be created; if the array
	 * is {@code null} and the specified length is 0, {@code null} will be
	 * returned; in other cases, the array will be adjusted to the specified
	 * length if needed.
	 *
	 * @param array         Array object ({@code null} to create new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param length        Length of resulting array.
	 *
	 * @return Array with the specified length; may return the original {@code
	 * array} argument.
	 *
	 * @throws IllegalArgumentException if {@code length} argument is negative
	 * or {@code array} is not an array or list.
	 */
	public static Object setLength( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final int length )
	{
		final Object result;

		checkType( array );

		if ( length < 0 )
		{
			throw new IllegalArgumentException( "length<0 (" + length + ')' );
		}

		final int arrayLength = getLength( array );
		if ( length > arrayLength )
		{
			result = ensureLength( array, componentType, incrementSize, length );
		}
		else
		{
			final int count = ( ( elementCount < 0 ) || ( elementCount > arrayLength ) ) ? arrayLength : elementCount;
			if ( length > count )
			{
				clear( array, count, length );
			}
			else if ( length < count )
			{
				clear( array, length, count );
			}

			result = array;
		}

		return result;
	}

	/**
	 * Ensure that the specified array has at least the requested length. If the
	 * array is has a shorter length, a new array is created with the specified
	 * minimum length. The increment size determines how the array length is
	 * incremented to meet the requirement (exact, linear, exponential).
	 *
	 * @param array         Array object ({@code null} to create new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param minimumLength Minimum length of resulting array.
	 *
	 * @return Array with at least the specified minimum length; may return the
	 * original {@code array} argument.
	 *
	 * @throws IllegalArgumentException if {@code length} argument is negative
	 * or {@code array} is not an array or list.
	 */
	public static Object ensureLength( @Nullable final Object array, @Nullable final Class<?> componentType, final int incrementSize, final int minimumLength )
	{
		final Object result;

		checkType( array );

		if ( minimumLength < 0 )
		{
			throw new IllegalArgumentException( "minimumLength<0 (" + minimumLength + ')' );
		}

		final int oldLength = getLength( array );
		if ( minimumLength > oldLength )
		{
			int newLength;
			if ( incrementSize < -1 ) /* round up to exponents of 'incrementSize' (exponential growth) */
			{
				newLength = ( oldLength < 1 ) ? 1 : oldLength;
				while ( newLength < minimumLength )
				{
					newLength *= -incrementSize;
				}
			}
			else if ( incrementSize <= 1 ) /* set length exactly */
			{
				newLength = minimumLength;
			}
			else /* round up to multiple of 'incrementSize' (linear growth) */
			{
				newLength = minimumLength - ( minimumLength - 1 ) % incrementSize - 1 + incrementSize;
			}

			final Class<?> newComponentType = getComponentType( array, componentType );
			result = Array.newInstance( newComponentType, newLength );
			if ( oldLength > 0 )
			{
				final Class<?> arrayClass = array.getClass();
				final Class<?> oldcomponentType = arrayClass.getComponentType();

				if ( ( newComponentType == Object.class ) && oldcomponentType.isPrimitive() )
				{
					for ( int i = 0; i < oldLength; i++ )
					{
						Array.set( result, i, Array.get( array, i ) );
					}
				}
				else if ( ( newComponentType == String.class ) && !newComponentType.isAssignableFrom( oldcomponentType ) )
				{
					for ( int i = 0; i < oldLength; i++ )
					{
						final Object element = Array.get( array, i );
						Array.set( result, i, ( element == null ) ? null : String.valueOf( element ) );
					}
				}
				else
				{
					copy( array, 0, result, 0, oldLength );
				}

			}
		}
		else
		{
			result = array;
		}

		return result;
	}

	/**
	 * Get element with the specified index from an array. This method also
	 * considers the specified element count, meaning that an {@link
	 * ArrayIndexOutOfBoundsException} is also thrown when the index exceeds the
	 * specified element count.
	 *
	 * @param array        Array or {@link List} to get element from.
	 * @param elementCount Current number of elements used (out-of-range => use
	 *                     array length, e.g. -1).
	 * @param index        Index of element to get.
	 *
	 * @return Element at the specified index.
	 *
	 * @throws ArrayIndexOutOfBoundsException if the index is out of range.
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 * @see Array#get
	 */
	public static Object get( final Object array, final int elementCount, final int index )
	{
		final Object result;

		checkType( array );

		if ( ( index < 0 ) || ( index >= getElementCount( array, elementCount ) ) )
		{
			throw new ArrayIndexOutOfBoundsException( index );
		}

		if ( array instanceof List )
		{
			result = ( (List<?>)array ).get( index );
		}
		else
		{
			result = Array.get( array, index );
		}

		return result;
	}

	/**
	 * Get values of the specified field from an object array.
	 *
	 * @param array     Array or {@link List} to get objects from.
	 * @param fieldName Name of field whose values to get.
	 * @param distinct  Only return distinct field values.
	 * @param sort      Sort result (only applies to numeric basic type).
	 *
	 * @return Array with values from the object array; {@code null} if invalid
	 * arguments were specified.
	 */
	@Nullable
	public static Object getFieldValues( final Object[] array, final String fieldName, final boolean distinct, final boolean sort )
	{
		Object result = null;

		if ( array != null )
		{
			try
			{
				final Class<?> arrayClass = array.getClass();
				final int arrayLength = array.length;
				final Class<?> arrayComponentType = arrayClass.getComponentType();
				final Field field = arrayComponentType.getField( fieldName );
				final Class<?> fieldType = field.getType();


				if ( distinct && ( arrayLength > 0 ) )
				{
					final Collection<Object> distinctValues = new HashSet<Object>( Math.min( 16, arrayLength ) );

					for ( final Object object : array )
					{
						if ( object != null )
						{
							distinctValues.add( field.get( object ) );
						}
					}

					result = Array.newInstance( fieldType, distinctValues.size() );
					int j = 0;
					for ( final Object distinctValue : distinctValues )
					{
						Array.set( result, j++, distinctValue );
					}
				}
				else
				{
					result = Array.newInstance( fieldType, arrayLength );

					for ( int i = 0; i < arrayLength; i++ )
					{
						final Object object = array[ i ];
						if ( object != null )
						{
							Array.set( result, i, field.get( object ) );
						}
					}
				}

				if ( sort )
				{
					//noinspection ChainOfInstanceofChecks
					if ( result instanceof byte[] )
					{
						Arrays.sort( (byte[])result );
					}
					else if ( result instanceof char[] )
					{
						Arrays.sort( (char[])result );
					}
					else if ( result instanceof double[] )
					{
						Arrays.sort( (double[])result );
					}
					else if ( result instanceof float[] )
					{
						Arrays.sort( (float[])result );
					}
					else if ( result instanceof int[] )
					{
						Arrays.sort( (int[])result );
					}
					else if ( result instanceof long[] )
					{
						Arrays.sort( (long[])result );
					}
					else if ( result instanceof short[] )
					{
						Arrays.sort( (short[])result );
					}
				}
			}
			catch ( final NoSuchFieldException e )
			{
				/* no ID field */
			}
			catch ( final IllegalAccessException e )
			{
				/* field is not public */
			}
			catch ( final RuntimeException e )
			{
				/* various caused having to do with reflection */
			}
		}

		return result;
	}

	/**
	 * Safely get element with the specified index from an array. This method
	 * will never throw an exception, but simply return {@code null} if the
	 * index is out of range.
	 *
	 * @param array Array or {@link List} to get element from.
	 * @param index Index of element to get.
	 *
	 * @return Element at the specified index; {@code null} if the index is out
	 * of range.
	 *
	 * @see Array#get
	 */
	public static Object getSafely( final Object array, final int index )
	{
		return getSafely( array, -1, index );
	}

	/**
	 * Safely get element with the specified index from an array. This method
	 * will never throw an exception, but simply return {@code null} if the
	 * index is out of range.
	 *
	 * @param array        Array or {@link List} to get element from.
	 * @param elementCount Current number of elements used (out-of-range => use
	 *                     array length, e.g. -1).
	 * @param index        Index of element to get.
	 *
	 * @return Element at the specified index; {@code null} if the element could
	 * not be retrieved (invalid argument, index out of range).
	 *
	 * @see Array#get
	 */
	@Nullable
	public static Object getSafely( final Object array, final int elementCount, final int index )
	{
		final Object result;

		if ( ( index < 0 ) || ( index >= getElementCount( array, elementCount ) ) )
		{
			result = null;
		}
		else if ( array instanceof List )
		{
			result = ( (List<?>)array ).get( index );
		}
		else
		{
			result = Array.get( array, index );
		}

		return result;
	}

	/**
	 * Get actual component type of array based on the specified array object
	 * and component type. If the component type is non-{@code null}, then it
	 * will be used as-is; if the component type is {@code null}, the component
	 * type is derived automatically from the array object (if possible);
	 * finally, if the component type could not be determined automatically,
	 * {@link Object} will be used as component type.
	 *
	 * @param array         Array object.
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 *
	 * @return Element type.
	 */
	public static Class<?> getComponentType( @Nullable final Object array, @Nullable final Class<?> componentType )
	{
		final Class<?> result;

		if ( componentType != null )
		{
			result = componentType;
		}
		else if ( array == null )
		{
			result = Object.class;
		}
		else
		{
			final Class<?> arrayClass = array.getClass();
			result = arrayClass.isArray() ? arrayClass.getComponentType() : Object.class;
		}

		return result;
	}

	/**
	 * Return the number of elements in the specified array object. A preferred
	 * result value may be specified (useful when not all elements in an array
	 * are actually used). If the specified element count does not match the
	 * array length (e.g. -1), then the array length will be returned.
	 *
	 * @param array        Array or {@link List} to get element count from (may
	 *                     be {@code null}).
	 * @param elementCount Preferred result value (use any out-of-range value to
	 *                     use the array length, e.g. -1).
	 *
	 * @return Element count of array; {@code 0} if array is {@code null} or is
	 * not an array.
	 *
	 * @see #getLength
	 */
	public static int getElementCount( @Nullable final Object array, final int elementCount )
	{
		final int length = getLength( array );
		return ( ( elementCount < 0 ) || ( elementCount > length ) ) ? length : elementCount;
	}

	/**
	 * Get (first) index of element in array.
	 *
	 * @param element Element to get index of.
	 * @param array   Array or {@link List} to get element count from (may be
	 *                {@code null}).
	 *
	 * @return Index of the specified element in the array; {@code -1} if no
	 * match is found in the array or the array is {@code null}.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static int indexOf( final Object element, final Object array )
	{
		int result = -1;

		if ( array instanceof List )
		{
			result = ( (List<?>)array ).indexOf( element );
		}
		else if ( array != null )
		{
			final Class<?> aClass = array.getClass();
			if ( aClass.isArray() )
			{
				final int length = Array.getLength( array );
				for ( result = length; --result >= 0; )
				{
					if ( equals( element, Array.get( array, result ) ) )
					{
						break;
					}
				}
			}
			else
			{
				checkType( array );
			}
		}

		return result;
	}

	/**
	 * Add all elements from {@code array2} to {@code array1}. When using array
	 * types, a new result array will be allocated unless {@code array2} is
	 * empty; otherwise, {@code array1} will be returned as result.
	 *
	 * @param array1 Array to which elements are added.
	 * @param array2 Array containing element(s) to add.
	 *
	 * @return Resulting array or {@link List} (depending on input arguments;
	 * may be the same as {@code array1}, or {@code null} if both input
	 * arguments are {@code null}).
	 *
	 * @see List#addAll
	 */
	public static Object addAll( final Object array1, final Object array2 )
	{
		final Object result;

		if ( array1 instanceof List )
		{
			result = array1;

			final List<Object> list = ( (List<Object>)array1 );
			if ( array2 instanceof List )
			{
				list.addAll( (Collection<?>)array2 );
			}
			else
			{
				final int l2 = getLength( array2 );
				for ( int i = 0; i < l2; i++ )
				{
					list.add( Array.get( array2, i ) );
				}
			}
		}
		else
		{
			final int l2 = getLength( array2 );
			if ( l2 > 0 )
			{
				final int l1 = getLength( array1 );
				if ( l1 > 0 )
				{
					result = setLength( array1, Object.class, l1 + l2 );
					copy( array2, 0, result, l1, l2 );
				}
				else
				{
					result = clone( array2 );
				}
			}
			else
			{
				result = array1;
			}
		}

		return result;
	}

	/**
	 * Append an element to an array. The result is a newly created array, which
	 * is increased in size by one, with the specified element appended at the
	 * end.
	 *
	 * @param array   Array to appent element to ({@code null} to create new).
	 * @param element Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final Object element )
	{
		return append( array, null, -1, -1, element );
	}

	/**
	 * Append element to an array. The result is an array with the element
	 * appened. If needed, a new array will be created.
	 *
	 * @param array         Array to appent element to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param element       Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, @Nullable final Class<?> componentType, final int elementCount, final int incrementSize, final Object element )
	{
		final int index = getElementCount( array, elementCount );
		final Object result = ensureLength( array, componentType, incrementSize, index + 1 );
		Array.set( result, index, element );
		return result;
	}

	/**
	 * Append an element to an array. The result is a newly created array, which
	 * is increased in size by one, with the specified element appended at the
	 * end.
	 *
	 * @param array   Array to appent element to ({@code null} to create new).
	 * @param element Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final boolean element )
	{
		return append( array, boolean.class, -1, -1, element );
	}

	/**
	 * Append element to an array. The result is an array with the element
	 * appened. If needed, a new array will be created.
	 *
	 * @param array         Array to appent element to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param element       Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final boolean element )
	{
		final int index = getElementCount( array, elementCount );
		final Object result = ensureLength( array, componentType, incrementSize, index + 1 );
		Array.setBoolean( result, index, element );
		return result;
	}

	/**
	 * Append an element to an array. The result is a newly created array, which
	 * is increased in size by one, with the specified element appended at the
	 * end.
	 *
	 * @param array   Array to appent element to ({@code null} to create new).
	 * @param element Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final byte element )
	{
		return append( array, byte.class, -1, -1, element );
	}

	/**
	 * Append element to an array. The result is an array with the element
	 * appened. If needed, a new array will be created.
	 *
	 * @param array         Array to appent element to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param element       Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final byte element )
	{
		final int index = getElementCount( array, elementCount );
		final Object result = ensureLength( array, componentType, incrementSize, index + 1 );
		Array.setByte( result, index, element );
		return result;
	}

	/**
	 * Append an element to an array. The result is a newly created array, which
	 * is increased in size by one, with the specified element appended at the
	 * end.
	 *
	 * @param array   Array to appent element to ({@code null} to create new).
	 * @param element Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final char element )
	{
		return append( array, char.class, -1, -1, element );
	}

	/**
	 * Append element to an array. The result is an array with the element
	 * appened. If needed, a new array will be created.
	 *
	 * @param array         Array to appent element to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param element       Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final char element )
	{
		final int index = getElementCount( array, elementCount );
		final Object result = ensureLength( array, componentType, incrementSize, index + 1 );
		Array.setChar( result, index, element );
		return result;
	}

	/**
	 * Append an element to an array. The result is a newly created array, which
	 * is increased in size by one, with the specified element appended at the
	 * end.
	 *
	 * @param array   Array to appent element to ({@code null} to create new).
	 * @param element Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final double element )
	{
		return append( array, double.class, -1, -1, element );
	}

	/**
	 * Append element to an array. The result is an array with the element
	 * appened. If needed, a new array will be created.
	 *
	 * @param array         Array to appent element to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param element       Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final double element )
	{
		final int index = getElementCount( array, elementCount );
		final Object result = ensureLength( array, componentType, incrementSize, index + 1 );
		Array.setDouble( result, index, element );
		return result;
	}

	/**
	 * Append an element to an array. The result is a newly created array, which
	 * is increased in size by one, with the specified element appended at the
	 * end.
	 *
	 * @param array   Array to appent element to ({@code null} to create new).
	 * @param element Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final float element )
	{
		return append( array, float.class, -1, -1, element );
	}

	/**
	 * Append element to an array. The result is an array with the element
	 * appened. If needed, a new array will be created.
	 *
	 * @param array         Array to appent element to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param element       Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final float element )
	{
		final int index = getElementCount( array, elementCount );
		final Object result = ensureLength( array, componentType, incrementSize, index + 1 );
		Array.setFloat( result, index, element );
		return result;
	}

	/**
	 * Append an element to an array. The result is a newly created array, which
	 * is increased in size by one, with the specified element appended at the
	 * end.
	 *
	 * @param array   Array to appent element to ({@code null} to create new).
	 * @param element Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final int element )
	{
		return append( array, int.class, -1, -1, element );
	}

	/**
	 * Append element to an array. The result is an array with the element
	 * appened. If needed, a new array will be created.
	 *
	 * @param array         Array to appent element to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param element       Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final int element )
	{
		final int index = getElementCount( array, elementCount );
		final Object result = ensureLength( array, componentType, incrementSize, index + 1 );
		Array.setInt( result, index, element );
		return result;
	}

	/**
	 * Append an element to an array. The result is a newly created array, which
	 * is increased in size by one, with the specified element appended at the
	 * end.
	 *
	 * @param array   Array to appent element to ({@code null} to create new).
	 * @param element Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final long element )
	{
		return append( array, long.class, -1, -1, element );
	}

	/**
	 * Append element to an array. The result is an array with the element
	 * appened. If needed, a new array will be created.
	 *
	 * @param array         Array to appent element to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param element       Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final long element )
	{
		final int index = getElementCount( array, elementCount );
		final Object result = ensureLength( array, componentType, incrementSize, index + 1 );
		Array.setLong( result, index, element );
		return result;
	}

	/**
	 * Append an element to an array. The result is a newly created array, which
	 * is increased in size by one, with the specified element appended at the
	 * end.
	 *
	 * @param array   Array to appent element to ({@code null} to create new).
	 * @param element Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final short element )
	{
		return append( array, short.class, -1, -1, element );
	}

	/**
	 * Append element to an array. The result is an array with the element
	 * appened. If needed, a new array will be created.
	 *
	 * @param array         Array to appent element to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param element       Element to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object append( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final short element )
	{
		final int index = getElementCount( array, elementCount );
		final Object result = ensureLength( array, componentType, incrementSize, index + 1 );
		Array.setShort( result, index, element );
		return result;
	}

	/**
	 * Append multiple elements to an array. The result is an array with the
	 * elements appened. If needed, a new array will be created.
	 *
	 * @param array    Array to append elements to ({@code null} to create
	 *                 new).
	 * @param elements Array or {@link List} with elements to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object appendMultiple( final Object array, final Object elements )
	{
		return insertMultiple( array, null, -1, -1, -1, elements );
	}

	/**
	 * Append multiple elements to an array. The result is an array with the
	 * elements appened. If needed, a new array will be created.
	 *
	 * @param array         Array to append elements to ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param elements      Array or {@link List} with elements to append.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object appendMultiple( final Object array, final Class<?> componentType, final int elementCount, final int incrementSize, final Object elements )
	{
		return insertMultiple( array, componentType, elementCount, incrementSize, -1, elements );
	}

	/**
	 * Insert an element into an array. The result is a newly created array
	 * which is increased in size by one, with the element inserted at the
	 * specified index.
	 *
	 * @param array    Array to insert element in ({@code null} to create new).
	 * @param insertAt Index to insert element at (use any out-of-range index to
	 *                 append, e.g. -1).
	 * @param element  Element to insert.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object insert( final Object array, final int insertAt, final Object element )
	{
		return insert( array, null, -1, -1, insertAt, element );
	}

	/**
	 * Insert an element into an array. The result is an array with the element
	 * inserted at the specified index. If needed, a new array will be created.
	 *
	 * @param array         Array to insert element in ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param insertAt      Index to insert element at (use any out-of-range
	 *                      index to append, e.g. -1).
	 * @param element       Element to insert.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object insert( final Object array, @Nullable final Class<?> componentType, final int elementCount, final int incrementSize, final int insertAt, final Object element )
	{
		final int oldCount = getElementCount( array, elementCount );
		final int index = ( ( insertAt < 0 ) || ( insertAt > oldCount ) ) ? oldCount : insertAt;

		final Object result = ensureLength( array, componentType, incrementSize, oldCount + 1 );

		/* move tail elements */
		if ( index < oldCount )
		{
			copy( result, index, result, index + 1, oldCount - index );
		}

		/* insert element */
		Array.set( result, insertAt, element );

		return result;
	}

	/**
	 * Insert multiple elements into an array. The result is an array with the
	 * elements inserted at the specified index. If needed, a new array will be
	 * created.
	 *
	 * @param array    Array to insert elements in ({@code null} to create
	 *                 new).
	 * @param insertAt Index to insert element at (use any out-of-range index to
	 *                 append, e.g. -1).
	 * @param elements Array or {@link List} with elements to insert.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} or {@code elements} is
	 * not an array or list.
	 */
	public static Object insertMultiple( final Object array, final int insertAt, final Object elements )
	{
		return insertMultiple( array, null, -1, -1, insertAt, elements );
	}

	/**
	 * Insert multiple elements into an array. The result is an array with the
	 * elements inserted at the specified index. If needed, a new array will be
	 * created.
	 *
	 * @param array         Array to insert elements in ({@code null} to create
	 *                      new).
	 * @param componentType Component type ({@code null} to derive
	 *                      automatically).
	 * @param elementCount  Current number of elements used (out-of-range => use
	 *                      array length, e.g. -1).
	 * @param incrementSize Increment size: &lt;-1 : use exponential size (-2 to
	 *                      use powers of 2); -1..1  : set length exactly; &gt;1
	 *                      : grow by this amount.
	 * @param insertAt      Index to insert element at (use any out-of-range
	 *                      index to append, e.g. -1).
	 * @param elements      Array or {@link List} with elements to insert.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} or {@code elements} is
	 * not an array or list.
	 */
	public static Object insertMultiple( final Object array, @Nullable final Class<?> componentType, final int elementCount, final int incrementSize, final int insertAt, final Object elements )
	{
		checkType( array );
		checkType( elements );
		final int insertCount = getLength( elements );
		checkType( elements );

		final Object result;
		if ( ( array != null ) && ( insertCount == 0 ) )
		{
			result = array;
		}
		else
		{
			final int oldCount = getElementCount( array, elementCount );
			final int index = ( ( insertAt < 0 ) || ( insertAt > oldCount ) ) ? oldCount : insertAt;

			result = ensureLength( array, componentType, incrementSize, oldCount + insertCount );
			if ( insertCount > 0 )
			{
				/* move tail elements */
				if ( index < oldCount )
				{
					copy( result, index, result, index + insertCount, oldCount - index );
				}

				/* insert elements */
				if ( elements instanceof List )
				{
					final List<?> list = (List<?>)elements;
					for ( int i = 0; i < insertCount; i++ )
					{
						Array.set( result, index + i, list.get( i ) );
					}
				}
				else
				{
					copy( elements, 0, result, index, insertCount );
				}
			}
		}

		return result;
	}

	/**
	 * Remove element from an array. The result is a newly created array with
	 * the specified element removed. If the element does not exist within the
	 * array, calling this method has no effect.
	 *
	 * @param array   Array to remove element from.
	 * @param element Element to remove.
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object remove( final Object array, final Object element )
	{
		return remove( array, indexOf( element, array ) );
	}

	/**
	 * Remove element from an array. The result is a newly created array with
	 * the element at the specified index removed. If the index is out of range,
	 * then calling this method has no effect.
	 *
	 * @param array    Array to remove element from.
	 * @param removeAt Index to remove element at (use any out-of-range index to
	 *                 append, e.g. -1).
	 *
	 * @return Resulting array.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object remove( final Object array, final int removeAt )
	{
		return ( removeAt >= 0 ) ? removeRange( array, removeAt, removeAt + 1 ) : array;
	}

	/**
	 * Remove element from an array. All elements (beyond the specified index
	 * (if any), are moved to the specified index and the empty space at the end
	 * of the array is cleared. The new element count will be returned. If the
	 * index is out of range, then calling this method has no effect.
	 *
	 * @param array        Array to remove element from.
	 * @param elementCount Current number of elements used (out-of-range => use
	 *                     array length, e.g. -1).
	 * @param removeAt     Index to remove element at (use any out-of-range
	 *                     index to append, e.g. -1).
	 *
	 * @return New element count.
	 *
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static int remove( final Object array, final int elementCount, final int removeAt )
	{
		return removeRange( array, elementCount, removeAt, removeAt + 1 );
	}

	/**
	 * Remove elements from an array. The result is a newly created array with
	 * the elements in the specified range removed. If the specified range is
	 * void, then calling this method has no effect.
	 *
	 * @param array      Array to remove elements from.
	 * @param startIndex Index of first element to remove.
	 * @param endIndex   Index of element after last remove element
	 *                   (out-of-range => use array length, e.g. -1).
	 *
	 * @return Resulting array.
	 *
	 * @throws ArrayIndexOutOfBoundsException if {@code startIndex} is
	 * negative.
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static Object removeRange( final Object array, final int startIndex, final int endIndex )
	{
		final Object result;

		if ( startIndex < 0 )
		{
			throw new ArrayIndexOutOfBoundsException( startIndex );
		}

		final int oldLength = getLength( array );
		final int actualEnd = ( ( endIndex < 0 ) || ( endIndex > oldLength ) ) ? oldLength : endIndex;
		final int nrRemoved = actualEnd - startIndex;

		if ( nrRemoved > 0 )
		{
			result = Array.newInstance( getComponentType( array, null ), oldLength - nrRemoved );

			/* copy head */
			if ( startIndex > 0 )
			{
				copy( array, 0, result, 0, startIndex );
			}

			/* copy tail */
			if ( actualEnd < oldLength )
			{
				copy( array, actualEnd, result, startIndex, oldLength - actualEnd );
			}
		}
		else
		{
			result = array;
		}

		return result;
	}

	/**
	 * Remove elements from an array. All elements (beyond the specified range
	 * (if any), are moved to the start of the specified range and the empty
	 * space at the end of the array is cleared. The new element count will be
	 * returned. If the specified range is void, then calling this method has no
	 * effect.
	 *
	 * @param array        Array to remove element from.
	 * @param elementCount Current number of elements used (out-of-range => use
	 *                     array length, e.g. -1).
	 * @param startIndex   Index of first element to remove.
	 * @param endIndex     Index of element after last remove element
	 *                     (out-of-range => use array length, e.g. -1).
	 *
	 * @return New element count.
	 *
	 * @throws ArrayIndexOutOfBoundsException if {@code startIndex} is
	 * negative.
	 * @throws IllegalArgumentException if {@code array} is not an array or
	 * list.
	 */
	public static int removeRange( final Object array, final int elementCount, final int startIndex, final int endIndex )
	{
		final int newLength;

		if ( startIndex < 0 )
		{
			throw new ArrayIndexOutOfBoundsException( startIndex );
		}

		final int oldLength = getElementCount( array, elementCount );
		final int actualEnd = ( ( endIndex < 0 ) || ( endIndex > oldLength ) ) ? oldLength : endIndex;
		final int nrRemoved = actualEnd - startIndex;

		if ( nrRemoved > 0 )
		{
			newLength = oldLength - nrRemoved;

			/* move tail */
			if ( actualEnd < oldLength )
			{
				copy( array, actualEnd, array, startIndex, oldLength - actualEnd );
			}

			/* clear trailing element */
			clear( array, oldLength - nrRemoved, oldLength );
		}
		else
		{
			newLength = oldLength;
		}

		return newLength;
	}

	/**
	 * Subtract the contents of {@code array2} from {@code array1} and return it
	 * as a new array of the specified {@code componentType}.
	 *
	 * @param componentType Component type of result.
	 * @param array1        Array to subtract from.
	 * @param array2        Array with elements to subtract.
	 *
	 * @return Array with elements from {@code array1} that are not in {@code
	 * array2}.
	 */
	public static Object subtract( final Class<?> componentType, final Object array1, final Object array2 )
	{
		final int length1 = getLength( array1 );

		Object result = Array.newInstance( componentType, length1 );

		if ( length1 > 0 ) /* anything to subtract from? */
		{
			if ( getLength( array2 ) > 0 ) /* anything to subtract? */
			{
				int resultCount = 0;

				for ( int i = 0; i < length1; i++ )
				{
					final Object element = get( array1, -1, i );
					if ( indexOf( element, array2 ) < 0 )
					{
						Array.set( result, resultCount++, element );
					}
				}

				result = setLength( result, componentType, resultCount );
			}
			else
			{
				copy( array1, 0, result, 0, length1 );
			}
		}

		return result;
	}

	/**
	 * Produce string representation of the specified array or {@link List}.
	 * Recursive array/list structures and {@code null} values are supported.
	 *
	 * If the specified object is {@code null}, or not actually an array or
	 * {@link List}, then result will be the same as if {@link String#valueOf(
	 *Object)} was called directly.
	 *
	 * @param array Array or {@link List} to convert to string.
	 *
	 * @return User-friendly string representation of the specified value.
	 */
	public static String toString( final Object array )
	{
		final String result;

		if ( isValidType( array ) )
		{
			if ( getLength( array ) == 0 )
			{
				result = "{}";
			}
			else
			{
				final StringBuffer sb = new StringBuffer();
				appendString( sb, array );
				result = sb.toString();
			}
		}
		else if ( array instanceof String )
		{
			result = "'" + array + '\'';
		}
		else
		{
			result = String.valueOf( array );
		}

		return result;
	}

	/**
	 * Produce string representation of the specified array or {@link List} and
	 * append it to the specified {@link StringBuffer}. Recursive array/list
	 * structures and {@code null} values are supported.
	 *
	 * If the specified object is {@code null}, or not actually an array or
	 * {@link List}, then result will be the same as if {@link
	 * StringBuffer#append(Object)} was called directly.
	 *
	 * @param dest  Destination buffer for string.
	 * @param array Array or {@link List} to convert to string.
	 */
	public static void appendString( final StringBuffer dest, final Object array )
	{
		if ( isValidType( array ) )
		{
			final int length = getLength( array );
			if ( length == 0 )
			{
				dest.append( "{}" );
			}
			else
			{
				dest.append( "{ " );

				for ( int i = 0; i < length; i++ )
				{
					if ( i > 0 )
					{
						dest.append( ", " );
					}

					appendString( dest, getSafely( array, i ) );
				}

				dest.append( " }" );
			}
		}
		else if ( array instanceof String )
		{
			dest.append( '\'' );
			dest.append( array );
			dest.append( '\'' );
		}
		else
		{
			dest.append( array );
		}
	}

	/**
	 * Concatenates the given arrays into a single array of the same type.
	 *
	 * @param arrays Arrays to be concatenated.
	 *
	 * @return Concatenation of the given arrays.
	 *
	 * @throws NullPointerException if {@code arrays} contains {@code null}.
	 */
	public static <T> T[] concatenate( final T[]... arrays )
	{
		return concatenate( (Class<T>)arrays.getClass().getComponentType().getComponentType(), Arrays.asList( arrays ) );
	}

	/**
	 * Concatenates the given arrays into a single array of the same type.
	 *
	 * @param componentType Component type of the given arrays.
	 * @param arrays        Arrays to be concatenated.
	 *
	 * @return Concatenation of the given arrays.
	 *
	 * @throws NullPointerException if any argument is {@code null}, or if
	 * {@code arrays} contains {@code null}.
	 */
	public static <T> T[] concatenate( final Class<T> componentType, final Collection<T[]> arrays )
	{
		int totalLength = 0;
		for ( final T[] array : arrays )
		{
			totalLength += array.length;
		}

		final T[] result = (T[])Array.newInstance( componentType, totalLength );
		int destPos = 0;
		for ( final T[] array : arrays )
		{
			System.arraycopy( array, 0, result, destPos, array.length );
			destPos += array.length;
		}

		return result;
	}

	/**
	 * Return the specified collection as an array.
	 *
	 * @param collection Collection to return as array.
	 *
	 * @return Array.
	 *
	 * @throws NullPointerException if the parameter is {@code null}, or if the
	 * given collection contains any {@code null}s.
	 */
	public static boolean[] asBooleanArray( final Collection<Boolean> collection )
	{
		final int size = collection.size();
		final boolean[] result = new boolean[ size ];

		final Iterator<Boolean> iterator = collection.iterator();
		for ( int index = 0; index < size; index++ )
		{
			result[ index ] = iterator.next();
		}

		return result;
	}

	/**
	 * Return the specified collection as an array.
	 *
	 * @param collection Collection to return as array.
	 *
	 * @return Array.
	 *
	 * @throws NullPointerException if the parameter is {@code null}, or if the
	 * given collection contains any {@code null}s.
	 */
	public static byte[] asByteArray( final Collection<Byte> collection )
	{
		final int size = collection.size();
		final byte[] result = new byte[ size ];

		final Iterator<Byte> iterator = collection.iterator();
		for ( int index = 0; index < size; index++ )
		{
			result[ index ] = iterator.next();
		}

		return result;
	}

	/**
	 * Return the specified collection as an array.
	 *
	 * @param collection Collection to return as array.
	 *
	 * @return Array.
	 *
	 * @throws NullPointerException if the parameter is {@code null}, or if the
	 * given collection contains any {@code null}s.
	 */
	public static char[] asCharArray( final Collection<Character> collection )
	{
		final int size = collection.size();
		final char[] result = new char[ size ];

		final Iterator<Character> iterator = collection.iterator();
		for ( int index = 0; index < size; index++ )
		{
			result[ index ] = iterator.next();
		}

		return result;
	}

	/**
	 * Return the specified collection as an array. Any {@code null}s that the
	 * array contains are replaced with {@link Double#NaN}.
	 *
	 * @param collection Collection to return as array.
	 *
	 * @return Array.
	 *
	 * @throws NullPointerException if the parameter is {@code null}.
	 */
	public static double[] asDoubleArray( final Collection<Double> collection )
	{
		final int size = collection.size();
		final double[] result = new double[ size ];

		final Iterator<Double> iterator = collection.iterator();
		for ( int index = 0; index < size; index++ )
		{
			final Double value = iterator.next();
			result[ index ] = value == null ? Double.NaN : value;
		}

		return result;
	}

	/**
	 * Return the specified collection as an array. Any {@code null}s that the
	 * array contains are replaced with {@link Float#NaN}.
	 *
	 * @param collection Collection to return as array.
	 *
	 * @return Array.
	 *
	 * @throws NullPointerException if the parameter is {@code null}.
	 */
	public static float[] asFloatArray( final Collection<Float> collection )
	{
		final int size = collection.size();
		final float[] result = new float[ size ];

		final Iterator<Float> iterator = collection.iterator();
		for ( int index = 0; index < size; index++ )
		{
			final Float value = iterator.next();
			result[ index ] = value == null ? Float.NaN : value;
		}

		return result;
	}

	/**
	 * Return the specified collection as an array.
	 *
	 * @param collection Collection to return as array.
	 *
	 * @return Array.
	 *
	 * @throws NullPointerException if the parameter is {@code null}, or if the
	 * given collection contains any {@code null}s.
	 */
	public static int[] asIntArray( final Collection<Integer> collection )
	{
		final int size = collection.size();
		final int[] result = new int[ size ];

		final Iterator<Integer> iterator = collection.iterator();
		for ( int index = 0; index < size; index++ )
		{
			result[ index ] = iterator.next();
		}

		return result;
	}

	/**
	 * Return the specified collection as an array.
	 *
	 * @param collection Collection to return as array.
	 *
	 * @return Array.
	 *
	 * @throws NullPointerException if the parameter is {@code null}, or if the
	 * given collection contains any {@code null}s.
	 */
	public static long[] asLongArray( final Collection<Long> collection )
	{
		final int size = collection.size();
		final long[] result = new long[ size ];

		final Iterator<Long> iterator = collection.iterator();
		for ( int index = 0; index < size; index++ )
		{
			result[ index ] = iterator.next();
		}

		return result;
	}

	/**
	 * Return the specified collection as an array.
	 *
	 * @param collection Collection to return as array.
	 *
	 * @return Array.
	 *
	 * @throws NullPointerException if the parameter is {@code null}, or if the
	 * given collection contains any {@code null}s.
	 */
	public static short[] asShortArray( final Collection<Short> collection )
	{
		final int size = collection.size();
		final short[] result = new short[ size ];

		final Iterator<Short> iterator = collection.iterator();
		for ( int index = 0; index < size; index++ )
		{
			result[ index ] = iterator.next();
		}

		return result;
	}

	/**
	 * Implementation a la {@link Arrays#asList(Object[])} for integer array.
	 *
	 * @param ints Array by which the list will be backed
	 *
	 * @return List view of the specified array.
	 */
	public static List<Integer> asList( final int... ints )
	{
		if ( ints == null )
		{
			throw new NullPointerException( "ints" );
		}

		final int length = ints.length;

		return new AbstractList<Integer>()
		{
			@Override
			public int size()
			{
				return length;
			}

			@NotNull
			@Override
			public Integer[] toArray()
			{
				final Integer[] result = new Integer[ length ];
				for ( int i = 0; i < length; i++ )
				{
					result[ i ] = ints[ i ];
				}
				return result;
			}

			@NotNull
			@Override
			public <T> T[] toArray( @NotNull final T[] a )
			{
				final T[] result;

				final int aLength = a.length;
				if ( aLength < length )
				{
					final Class<? extends Object[]> aClass = a.getClass();
					result = (T[])Array.newInstance( aClass.getComponentType(), length );
				}
				else
				{
					result = a;

					for ( int i = length; i < aLength; i++ )
					{
						a[ i ] = null;
					}
				}

				for ( int i = 0; i < length; i++ )
				{
					a[ i ] = (T)Integer.valueOf( ints[ i ] );
				}

				return result;
			}

			@Override
			public Integer get( final int index )
			{
				return ints[ index ];
			}

			@Override
			public Integer set( final int index, final Integer element )
			{
				final Integer oldValue = ints[ index ];
				ints[ index ] = element;
				return oldValue;
			}

			@Override
			public int indexOf( final Object o )
			{
				int result = -1;

				if ( o instanceof Integer )
				{
					final int value = (Integer)o;

					for ( int i = 0; i < ints.length; i++ )
					{
						if ( ints[ i ] == value )
						{
							result = i;
						}
					}
				}
				return result;
			}

			@Override
			public boolean contains( final Object o )
			{
				return ( indexOf( o ) != -1 );
			}
		};
	}

	/**
	 * Return collection of {@link Integer}s as {@code int}-array.
	 *
	 * @param collection Collection of {@link Integer}s.
	 *
	 * @return Array containing contents of collection.
	 */
	@NotNull
	public static int[] toArray( @NotNull final Collection<Integer> collection )
	{
		final int[] result = new int[ collection.size() ];

		int i = 0;
		for ( final Integer integer : collection )
		{
			result[ i++ ] = integer;
		}

		return result;
	}

	/**
	 * Writes a dump of binary data in hexadecimal and printable ASCII to the
	 * standard output. If multiple byte arrays are given, the dump contains
	 * multiple columns showing the data in each array.
	 *
	 * @param appendable  Appendable to print the dump to.
	 * @param columnWidth Number of bytes per line for each column.
	 * @param columns     One or more byte arrays to be dumped.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void hexDump( final Appendable appendable, final int columnWidth, final byte[]... columns )
	throws IOException
	{
		hexDump( appendable, columnWidth, false, columns );
	}

	/**
	 * Writes a dump of binary data in hexadecimal and printable ASCII to the
	 * standard output. If multiple byte arrays are given, the dump contains
	 * multiple columns showing the data in each array.
	 *
	 * @param appendable  Appendable to print the dump to.
	 * @param columnWidth Number of bytes per line for each column.
	 * @param markDiffs   Mark differences between columns.
	 * @param columns     One or more byte arrays to be dumped.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void hexDump( final Appendable appendable, final int columnWidth, final boolean markDiffs, final byte[]... columns )
	throws IOException
	{
		final int byteIndexRadix = ( ( columnWidth == 8 ) || ( columnWidth == 16 ) ) ? columnWidth : 10;

		int byteIndex = 0;
		while ( true )
		{
			appendable.append( TextTools.getFixed( Integer.toString( byteIndex, byteIndexRadix ), 8, true, '0' ) );
			final int lineStart = byteIndex;

			int endOfData = 0;

			for ( final byte[] column : columns )
			{
				appendable.append( " " );

				if ( ( lineStart + columnWidth ) >= column.length )
				{
					endOfData++;
				}

				boolean lastWasDifferent = false;

				int byteOffset = 0;
				byteIndex = lineStart;

				while ( byteOffset < columnWidth )
				{
					final boolean isDifferent = markDiffs && isElementDifferent( byteIndex, columns );
					appendable.append( isDifferent ? !lastWasDifferent ? '[' : '|' : lastWasDifferent ? ']' : ' ' );
					lastWasDifferent = isDifferent;

					if ( byteIndex >= column.length )
					{
						appendable.append( "  " );
					}
					else
					{
						appendable.append( TextTools.toHexString( (int)column[ byteIndex ], 2, isDifferent ) );
					}
					byteOffset++;
					byteIndex++;
				}

				appendable.append( lastWasDifferent ? ']' : ' ' );

				if ( column.length > lineStart )
				{
					appendable.append( " |" );

					for ( byteOffset = 0, byteIndex = lineStart; ( byteOffset < columnWidth ); byteOffset++, byteIndex++ )
					{
						if ( column.length <= byteIndex )
						{
							appendable.append( ' ' );
						}
						else
						{

							final char c = (char)column[ byteIndex ];
							if ( ( c >= ' ' ) && ( c <= '~' ) )
							{
								appendable.append( c );
							}
							else
							{
								appendable.append( '.' );
							}
						}
					}

					appendable.append( '|' );
				}
				else
				{
					appendable.append( "   " );
					for ( int padding = 0; padding < columnWidth; padding++ )
					{
						appendable.append( ' ' );
					}
				}
			}

			byteIndex = lineStart + columnWidth;

			appendable.append( '\n' );

			if ( endOfData == columns.length )
			{
				break;
			}
		}
	}

	/**
	 * Compares an element in multiple arrays. If the elements are not equal in
	 * all specified arrays, this will return {@code true}. This is also handles
	 * situations where the specified index is beyond the end of one or more of
	 * the specified arrays.
	 *
	 * @param byteIndex  Byte index.
	 * @param byteArrays Byte arrays to compare.
	 *
	 * @return {@code true} if the element with the given index as different for
	 * one or more of the given arrays; {@code false} if the element is the same
	 * for all arrays.
	 */
	public static boolean isElementDifferent( final int byteIndex, final byte[]... byteArrays )
	{
		boolean result = false;

		if ( byteArrays.length > 1 )
		{
			final byte[] column0 = byteArrays[ 0 ];
			final boolean end = ( byteIndex >= column0.length );
			final byte value = end ? (byte)0 : column0[ byteIndex ];

			for ( int columnIndex = 1; !result && columnIndex < byteArrays.length; columnIndex++ )
			{
				final byte[] column = byteArrays[ columnIndex ];

				if ( byteIndex < column.length )
				{
					result = ( column[ byteIndex ] != value );
				}
				else
				{
					result = !end;
				}
			}
		}

		return result;
	}

	/**
	 * Reverse the contents of the given array.
	 *
	 * @param a Array to reverse.
	 */
	public static void reverse( final int[] a )
	{
		reverse( a, 0, a.length );
	}

	/**
	 * Reverse the contents of the given array.
	 *
	 * @param a      Array to reverse.
	 * @param offset Offset of range in array.
	 * @param length Length of array range.
	 */
	public static void reverse( final int[] a, final int offset, final int length )
	{
		int h = offset;
		int t = offset + length - 1;

		while ( h < t )
		{
			final int tmp = a[ h ];
			a[ h++ ] = a[ t ];
			a[ t-- ] = tmp;
		}
	}
}
