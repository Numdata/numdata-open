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

import java.math.*;

import org.junit.*;

/**
 * Provides tools for testing big decimal values.
 *
 * @author Gerrit Meinders
 */
public class BigDecimalTestTools
{
	/**
	 * Asserts that the two big decimals are equal.
	 *
	 * This method is called {@code assertDecimalEquals} instead of {@code
	 * assertEquals} to avoid clashes between {@link Assert#assertEquals(String,
	 * Object, Object)} and a static import of this method.
	 *
	 * @param message  Detail message.
	 * @param expected Expected value.
	 * @param actual   Actual value.
	 */
	public static void assertDecimalEquals( final String message, final BigDecimal expected, final BigDecimal actual )
	{
		if ( !BigDecimalTools.isEqual( expected, actual ) )
		{
			throw new ComparisonFailure( message, String.valueOf( expected ), String.valueOf( actual ) );
		}
	}

	/**
	 * This class must not be instantiated.
	 */
	private BigDecimalTestTools()
	{
		throw new AssertionError();
	}
}
