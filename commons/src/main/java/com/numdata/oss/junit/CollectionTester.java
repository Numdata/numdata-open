/*
 * Copyright (c) 2012-2019, Numdata BV, The Netherlands.
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

import java.util.*;

import org.jetbrains.annotations.*;
import org.junit.*;

/**
 * JUnit unit tool class to help with testing collections.
 *
 * @author Peter S. Heijnen
 */
public class CollectionTester
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private CollectionTester()
	{
	}

	/**
	 * Asserts that two arrays are equal. If they are not, an {@link
	 * AssertionError} is thrown with the given message.
	 *
	 * @param messagePrefix Prefix to failure messages.
	 * @param expected      Expected array value.
	 * @param actual        Actual array value.
	 * @param <T>           Element type.
	 */
	public static <T> void assertEquals( @Nullable final String messagePrefix, @Nullable final Collection<T> expected, @Nullable final Collection<T> actual )
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
	 * @param <T>           Element type.
	 */
	public static <T> void assertEquals( @Nullable final String messagePrefix, @Nullable final Collection<T> expected, @Nullable final Collection<T> actual, @NotNull final AssertEquals elementTester )
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
	 * @param <T>           Element type.
	 */
	public static <T> void assertEquals( @Nullable final String messagePrefix, @NotNull final String expectedName, @NotNull final String actualName, @Nullable final Collection<T> expected, @Nullable final Collection<T> actual )
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
	 * @param <T>           Element type.
	 */
	public static <T> void assertEquals( @Nullable final String messagePrefix, @NotNull final String expectedName, @NotNull final String actualName, @Nullable final Collection<T> expected, @Nullable final Collection<T> actual, @NotNull final AssertEquals elementTester )
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
			final Iterator<T> expectedIterator = expected.iterator();
			final Iterator<T> actualIterator = actual.iterator();

			int i = 0;
			while ( expectedIterator.hasNext() && actualIterator.hasNext() )
			{
				final String indexString = "[ " + i++ + " ]";
				final T expectedValue = expectedIterator.next();
				final T actualValue = actualIterator.next();

				elementTester.assertEquals( actualPrefix + "mismatch " + expectedName + indexString + " == " + actualName + indexString, expectedValue, actualValue );
			}

			Assert.assertEquals( actualPrefix + '\'' + expectedName + "' should have same length as '" + actualName + '\'', expected.size(), actual.size() );
		}
	}
}
