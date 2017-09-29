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

import java.lang.reflect.*;
import java.util.*;

import org.junit.*;

/**
 * JUnit unit tool class to help with testing array values.
 *
 * @author Peter S. Heijnen
 */
public class ArrayTester
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private ArrayTester()
	{
	}

	/**
	 * Asserts that two arrays are equal. If they are not, an {@link
	 * AssertionError} is thrown with the given message.
	 *
	 * @param messagePrefix Prefix to failure messages.
	 * @param expected      Expected array value.
	 * @param actual        Actual array value.
	 */
	public static void assertEquals( final String messagePrefix, final Object expected, final Object actual )
	{
		assertEquals( messagePrefix, "expected", "actual", expected, actual );
	}

	/**
	 * Asserts that two arrays are equal. If they are not, an {@link
	 * AssertionError} is thrown with the given message.
	 *
	 * @param messagePrefix Prefix to failure messages.
	 * @param expected      Expected array value.
	 * @param actual        Actual array value.
	 * @param elementTester Tests equality of elements.
	 */
	public static void assertEquals( final String messagePrefix, final Object expected, final Object actual, final AssertEquals elementTester )
	{
		assertEquals( messagePrefix, "expected", "actual", expected, actual, elementTester );
	}

	/**
	 * Asserts that two arrays are equal. If they are not, an {@link
	 * AssertionError} is thrown with the given message.
	 *
	 * @param messagePrefix Prefix to failure messages.
	 * @param expectedName  Name of expected array.
	 * @param actualName    Name of actual array.
	 * @param expected      Expected array value.
	 * @param actual        Actual array value.
	 */
	public static void assertEquals( final String messagePrefix, final String expectedName, final String actualName, final Object expected, final Object actual )
	{
		assertEquals( messagePrefix, expectedName, actualName, expected, actual, new AssertEquals() );
	}

	/**
	 * Asserts that two arrays are equal. If they are not, an {@link
	 * AssertionError} is thrown with the given message.
	 *
	 * @param messagePrefix Prefix to failure messages.
	 * @param expectedName  Name of expected array.
	 * @param actualName    Name of actual array.
	 * @param expected      Expected array value.
	 * @param actual        Actual array value.
	 * @param elementTester Tests equality of elements.
	 */
	public static void assertEquals( final String messagePrefix, final String expectedName, final String actualName, final Object expected, final Object actual, final AssertEquals elementTester )
	{
		final String actualPrefix = ( ( messagePrefix != null ) ? messagePrefix + " - " : "" );

		if ( expected == null )
		{
			if ( actual != null )
			{
				Assert.fail( actualPrefix + expectedName + " is 'null', '" + actualName + "' is not" );
			}
		}
		else if ( ( actual == null ) )
		{
			Assert.fail( actualPrefix + actualName + " is 'null', '" + expectedName + "' is not" );
		}
		else
		{
			final Class<?> expectedType = expected.getClass();
			Assert.assertTrue( actualPrefix + '\'' + expectedName + "' should be an array", expectedType.isArray() );
			final int expectedLength = Array.getLength( expected );

			final Class<?> actualType = actual.getClass();
			Assert.assertTrue( actualPrefix + '\'' + actualName + "' should be an array", actualType.isArray() );
			final int actualLength = Array.getLength( actual );

			Assert.assertEquals( actualPrefix + '\'' + expectedName + "' should be of same type as '" + actualName + '\'', expectedType, actualType );
			Assert.assertEquals( actualPrefix + '\'' + expectedName + "' should have same length as '" + actualName + '\'', expectedLength, actualLength );

			final Class<?> componentType = expectedType.getComponentType();
			final boolean isMultiDimensional = componentType.isArray();

			for ( int i = 0; i < expectedLength; i++ )
			{
				final String expectedValueName = expectedName + "[ " + i + " ]";
				final String actualValueName = actualName + "[ " + i + " ]";
				final Object expectedValue = Array.get( expected, i );
				final Object actualValue = Array.get( actual, i );

				if ( isMultiDimensional )
				{
					assertEquals( messagePrefix, expectedValueName, actualValueName, expectedValue, actualValue );
				}
				else
				{
					elementTester.assertEquals( actualPrefix + "mismatch " + expectedValueName + " == " + actualValueName, expectedValue, actualValue );
				}
			}
		}
	}

	/**
	 * Give string representation of an array. This method tries to provide a
	 * user-friendly representation of an array, and properly handles {@code
	 * null}-arrays and {@code null}-elements.
	 *
	 * @param array Array to give string representation of.
	 *
	 * @return String representation of array.
	 */
	public static String toString( final Object array )
	{
		final String result;

		if ( array == null )
		{
			result = "null";
		}
		else
		{
			final Class<?> aClass = array.getClass();
			if ( !aClass.isArray() )
			{
				throw new IllegalArgumentException( array + " is not an array!" );
			}

			final int aLength = Array.getLength( array );
			final Class<?> eClass = aClass.getComponentType();
			final String eClassName = eClass.getName();
			final String eShortName = eClassName.substring( eClassName.lastIndexOf( (int)'.' ) + 1 );

			final StringBuilder sb = new StringBuilder();
			sb.append( eShortName );
			sb.append( '[' );
			sb.append( aLength );
			sb.append( ']' );

			//noinspection ObjectEquality
			if ( ( aLength < 10 )
			     && ( eClass.isPrimitive()
			          || Number.class.isAssignableFrom( eClass )
			          || ( eClass == Boolean.class )
			          || ( eClass == Class.class )
			          || ( eClass == Locale.class )
			          || ( eClass == String.class )
			     ) )
			{
				sb.append( "={" );
				for ( int i = 0; i < aLength; i++ )
				{
					if ( i > 0 )
					{
						sb.append( ',' );
					}

					final Object element = Array.get( array, i );
					if ( element == null )
					{
						sb.append( " null " );
					}
					else
					{
						//noinspection ObjectEquality
						final String s = ( eClass == Class.class ) ? ( (Class<?>)element ).getName() : element.toString();

						if ( s == null )
						{
							sb.append( " ?null? " ); // toString() should not return null....
						}
						else if ( ( s.length() > 40 ) )
						{
							sb.append( " ... " );
						}
						else
						{
							sb.append( " '" );
							sb.append( s );
							sb.append( "' " );
						}
					}
				}
				sb.append( '}' );
			}

			result = sb.toString();
		}

		return result;
	}

	/**
	 * Helper for {@link ArrayTester#assertEquals} methods to determine whether
	 * two elements of an array are equal.
	 */
	public static class AssertEquals
	{
		/**
		 * Assert equality of {@code actualValue} to {@code expectedValue}.
		 *
		 * @param message       Message to display if assertion fails.
		 * @param expectedValue Expected value.
		 * @param actualValue   Actual value.
		 */
		public void assertEquals( final String message, final Object expectedValue, final Object actualValue )
		{
			if ( ( expectedValue instanceof Double ) && ( actualValue instanceof Double ) )
			{
				final double expectedDouble = (Double)expectedValue;
				final double actualDouble = (Double)actualValue;

				Assert.assertEquals( message, expectedDouble, actualDouble, 0.0001 );
			}
			else if ( ( expectedValue instanceof Float ) && ( actualValue instanceof Float ) )
			{
				final float expectedFloat = (Float)expectedValue;
				final float actualFloat = (Float)actualValue;

				Assert.assertEquals( message, expectedFloat, actualFloat, 0.0001f );
			}
			else
			{
				Assert.assertEquals( message, expectedValue, actualValue );
			}
		}
	}
}
