/*
 * Copyright (c) 2020-2020, Numdata BV, The Netherlands.
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

import java.util.concurrent.*;

import org.jetbrains.annotations.*;
import static org.junit.Assert.*;

/**
 * Some additional JUnit assertions.
 *
 * @author G. Meinders
 * @author Peter S. Heijnen
 */
public class JUnitTools
{
	/**
	 * Not supposed to be instantiated.
	 */
	private JUnitTools()
	{
	}

	/**
	 * Asserts that the given runnable throws the expected type of exception.
	 *
	 * @param message               Failure message.
	 * @param expectedExceptionType Exception type.
	 * @param runnable              Runnable to run.
	 */
	public static void assertException( @NotNull final String message, @NotNull final Class<? extends Exception> expectedExceptionType, @NotNull final Runnable runnable )
	{
		try
		{
			runnable.run();
			fail( message + ": Expected " + expectedExceptionType );
		}
		catch ( final Exception e )
		{
			if ( !expectedExceptionType.isInstance( e ) )
			{
				e.printStackTrace();
				fail( message + ": Expected " + expectedExceptionType + ", but was " + e );
			}
		}
	}

	/**
	 * Asserts that the given callable throws the expected type of exception.
	 *
	 * @param message               Failure message.
	 * @param expectedExceptionType Exception type.
	 * @param callable              Callable to run.
	 */
	public static void assertException( @NotNull final String message, @NotNull final Class<? extends Exception> expectedExceptionType, @NotNull final Callable<?> callable )
	{
		try
		{
			callable.call();
			fail( message + ": Expected " + expectedExceptionType );
		}
		catch ( final Exception e )
		{
			if ( !expectedExceptionType.isInstance( e ) )
			{
				e.printStackTrace();
				fail( message + ": Expected " + expectedExceptionType + ", but was " + e );
			}
		}
	}

	/**
	 * Asserts that the given runnable throws the expected exception.
	 *
	 * @param message           Failure message.
	 * @param expectedException Expected exception.
	 * @param runnable          Runnable to run.
	 */
	public static void assertException( @NotNull final String message, @NotNull final Exception expectedException, @NotNull final Runnable runnable )
	{
		try
		{
			runnable.run();
			fail( message + ": Expected " + expectedException );
		}
		catch ( final Exception e )
		{
			assertEquals( message, expectedException.getClass(), e.getClass() );
			final String expectedMessage = expectedException.getMessage();
			if ( ( expectedMessage != null ) && !expectedMessage.equals( e.getMessage() ) )
			{
				e.printStackTrace();
				assertEquals( message + ", unexpected message", expectedMessage, e.getMessage() );
			}
		}
	}

	/**
	 * Asserts that the given callable throws the expected exception.
	 *
	 * @param message           Failure message.
	 * @param expectedException Expected exception.
	 * @param callable          Callable to run.
	 */
	public static void assertException( @NotNull final String message, @NotNull final Exception expectedException, @NotNull final Callable<?> callable )
	{
		try
		{
			callable.call();
			fail( message + ": Expected " + expectedException );
		}
		catch ( final Exception e )
		{
			assertEquals( message, expectedException.getClass(), e.getClass() );
			final String expectedMessage = expectedException.getMessage();
			if ( ( expectedMessage != null ) && !expectedMessage.equals( e.getMessage() ) )
			{
				e.printStackTrace();
				assertEquals( message + ", unexpected message", expectedMessage, e.getMessage() );
			}
		}
	}
}
