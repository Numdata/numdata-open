/*
 * Copyright (c) 2020, Numdata BV, The Netherlands.
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
package com.numdata.commons.java8;

import java.util.function.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link FunctionTools}.
 */
@SuppressWarnings( "ErrorNotRethrown" )
public class TestFunctionTools
{
	@Test
	public void testWrapFunctionException()
	{
		final Function<Integer, Integer> wrappedFunction = FunctionTools.wrapFunctionException( TestFunctionTools::throwingFunction );

		System.out.println( " - Test #1: no exception" );
		assertEquals( "Test #1 result", Integer.valueOf( 0 ), wrappedFunction.apply( 0 ) );

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			wrappedFunction.apply( 1 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			wrappedFunction.apply( 2 );
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			wrappedFunction.apply( 3 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertNotNull( "Expect wrapped exception with original cause", e.getCause() );
			assertEquals( "Unexpected cause type", Exception.class, e.getCause().getClass() );
		}
	}

	@Test
	public void testLiftFunctionException()
	throws Exception
	{
		final Function<Integer, Integer> wrappedFunction = FunctionTools.liftFunctionException( TestFunctionTools::throwingFunction );

		System.out.println( " - Test #1: no exception" );
		assertEquals( "Test #1 result", Integer.valueOf( 0 ), wrappedFunction.apply( 0 ) );

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			wrappedFunction.apply( 1 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			wrappedFunction.apply( 2 );
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			wrappedFunction.apply( 3 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final Exception e )
		{
			assertEquals( "Unexpected exception message", "3", e.getMessage() );
		}
	}

	@Test
	public void testWrapBiFunctionException()
	{
		final BiFunction<Integer, Integer, Integer> wrappedBiFunction = FunctionTools.wrapBiFunctionException( TestFunctionTools::throwingBiFunction );

		System.out.println( " - Test #1: no exception" );
		assertEquals( "Test #1 result", Integer.valueOf( 1 ), wrappedBiFunction.apply( 0, 1 ) );

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			wrappedBiFunction.apply( 1, 0 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			wrappedBiFunction.apply( 2, 0 );
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			wrappedBiFunction.apply( 3, 0 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertNotNull( "Expect wrapped exception with original cause", e.getCause() );
			assertEquals( "Unexpected cause type", Exception.class, e.getCause().getClass() );
		}
	}

	@Test
	public void testLiftBiFunctionException()
	throws Exception
	{
		final BiFunction<Integer, Integer, Integer> wrappedBiFunction = FunctionTools.liftBiFunctionException( TestFunctionTools::throwingBiFunction );

		System.out.println( " - Test #1: no exception" );
		assertEquals( "Test #1 result", Integer.valueOf( 1 ), wrappedBiFunction.apply( 0, 1 ) );

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			wrappedBiFunction.apply( 1, 0 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			wrappedBiFunction.apply( 2, 0 );
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			wrappedBiFunction.apply( 3, 0 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final Exception e )
		{
			assertEquals( "Unexpected exception message", "3", e.getMessage() );
		}
	}

	@Test
	public void testWrapConsumerException()
	{
		final Consumer<Integer> wrappedConsumer = FunctionTools.wrapConsumerException( TestFunctionTools::throwingFunction );

		System.out.println( " - Test #1: no exception" );
		wrappedConsumer.accept( 0 );

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			wrappedConsumer.accept( 1 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			wrappedConsumer.accept( 2 );
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			wrappedConsumer.accept( 3 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertNotNull( "Expect wrapped exception with original cause", e.getCause() );
			assertEquals( "Unexpected cause type", Exception.class, e.getCause().getClass() );
		}
	}

	@Test
	public void testLiftConsumerException()
	throws Exception
	{
		final Consumer<Integer> wrappedConsumer = FunctionTools.liftConsumerException( TestFunctionTools::throwingFunction );

		System.out.println( " - Test #1: no exception" );
		wrappedConsumer.accept( 0 );

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			wrappedConsumer.accept( 1 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			wrappedConsumer.accept( 2 );
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			wrappedConsumer.accept( 3 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final Exception e )
		{
			assertEquals( "Unexpected exception message", "3", e.getMessage() );
		}
	}

	@Test
	public void testWrapIntConsumerException()
	{
		final IntConsumer wrappedIntConsumer = FunctionTools.wrapIntConsumerException( TestFunctionTools::throwingFunction );

		System.out.println( " - Test #1: no exception" );
		wrappedIntConsumer.accept( 0 );

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			wrappedIntConsumer.accept( 1 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			wrappedIntConsumer.accept( 2 );
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			wrappedIntConsumer.accept( 3 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertNotNull( "Expect wrapped exception with original cause", e.getCause() );
			assertEquals( "Unexpected cause type", Exception.class, e.getCause().getClass() );
		}
	}

	@Test
	public void testLiftIntConsumerException()
	throws Exception
	{
		final IntConsumer wrappedIntConsumer = FunctionTools.liftIntConsumerException( TestFunctionTools::throwingFunction );

		System.out.println( " - Test #1: no exception" );
		wrappedIntConsumer.accept( 0 );

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			wrappedIntConsumer.accept( 1 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			wrappedIntConsumer.accept( 2 );
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			wrappedIntConsumer.accept( 3 );
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final Exception e )
		{
			assertEquals( "Unexpected exception message", "3", e.getMessage() );
		}
	}

	@Test
	public void testWrapSupplierException()
	{
		System.out.println( " - Test #1: no exception" );
		FunctionTools.wrapSupplierException( () -> 0 ).get();

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			FunctionTools.wrapSupplierException( () -> {
				throw new RuntimeException( "1" );
			} ).get();
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			FunctionTools.wrapSupplierException( () -> {
				throw new Error( "2" );
			} ).get();
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			FunctionTools.wrapSupplierException( () -> {
				throw new Exception( "3" );
			} ).get();
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertNotNull( "Expect wrapped exception with original cause", e.getCause() );
			assertEquals( "Unexpected cause type", Exception.class, e.getCause().getClass() );
		}
	}

	@Test
	public void testLiftSupplierException()
	{
		System.out.println( " - Test #1: no exception" );
		FunctionTools.liftSupplierException( () -> 0 ).get();

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			FunctionTools.liftSupplierException( () -> {
				throw new RuntimeException( "1" );
			} ).get();
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			FunctionTools.liftSupplierException( () -> {
				throw new Error( "2" );
			} ).get();
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			FunctionTools.liftSupplierException( () -> {
				throw new Exception( "3" );
			} ).get();
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final Exception e )
		{
			assertEquals( "Unexpected exception message", "3", e.getMessage() );
		}
	}

	@Test
	public void testWrapIntSupplierException()
	{
		System.out.println( " - Test #1: no exception" );
		FunctionTools.wrapIntSupplierException( () -> 0 ).getAsInt();

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			FunctionTools.wrapIntSupplierException( () -> {
				throw new RuntimeException( "1" );
			} ).getAsInt();
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			FunctionTools.wrapIntSupplierException( () -> {
				throw new Error( "2" );
			} ).getAsInt();
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			FunctionTools.wrapIntSupplierException( () -> {
				throw new Exception( "3" );
			} ).getAsInt();
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertNotNull( "Expect wrapped exception with original cause", e.getCause() );
			assertEquals( "Unexpected cause type", Exception.class, e.getCause().getClass() );
		}
	}

	@Test
	public void testLiftIntSupplierException()
	{
		System.out.println( " - Test #1: no exception" );
		FunctionTools.liftIntSupplierException( () -> 0 ).getAsInt();

		System.out.println( " - Test #2: RuntimeException" );
		try
		{
			FunctionTools.liftIntSupplierException( () -> {
				throw new RuntimeException( "1" );
			} ).getAsInt();
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final RuntimeException e )
		{
			assertEquals( "Unexpected exception message", "1", e.getMessage() );
		}

		System.out.println( " - Test #3: Error" );
		try
		{
			FunctionTools.liftIntSupplierException( () -> {
				throw new Error( "2" );
			} ).getAsInt();
			fail( "Should have thrown Error" );
		}
		catch ( final Error e )
		{
			assertEquals( "Unexpected exception message", "2", e.getMessage() );
		}

		System.out.println( " - Test #4: Exception" );
		try
		{
			FunctionTools.liftIntSupplierException( () -> {
				throw new Exception( "3" );
			} ).getAsInt();
			fail( "Should have thrown RuntimeException" );
		}
		catch ( final Exception e )
		{
			assertEquals( "Unexpected exception message", "3", e.getMessage() );
		}
	}

	public static int throwingFunction( final int input )
	throws Exception
	{
		switch ( input )
		{
			case 1:
				throw new RuntimeException( "1" );

			case 2:
				throw new Error( "2" );

			case 3:
				throw new Exception( "3" );

			default:
				return input;
		}
	}

	public static int throwingBiFunction( final int input1, final int input2 )
	throws Exception
	{
		switch ( input1 )
		{
			case 1:
				throw new RuntimeException( "1" );

			case 2:
				throw new Error( "2" );

			case 3:
				throw new Exception( "3" );

			default:
				return input2;
		}
	}
}
