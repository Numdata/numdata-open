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

import java.util.concurrent.*;

import org.junit.*;
import static org.junit.Assert.*;

@SuppressWarnings( "BusyWait" )
public class TestWaitingFutureTask
{
	private ExecutorService _executor;

	@Before
	public void setUp()
	{
		_executor = new ThreadPoolExecutor( 0, 100, 1L, TimeUnit.MINUTES, new SynchronousQueue<>() );
	}

	@After
	public void tearDown()
	throws Exception
	{
		System.out.println( "Shut down executor" );
		_executor.shutdownNow();
		_executor.awaitTermination( 20L, TimeUnit.SECONDS );
		assertTrue( "Executor failed to shutdown!?", _executor.isTerminated() );
	}

	@Test
	public void testCancel()
	throws Exception
	{
		final int[] state = { 0 };
		final boolean[] finishFast = { false };

		final WaitingFutureTask<Integer> task = new WaitingFutureTask<>( () -> {
			System.out.println( "task: Started" );
			state[ 0 ] = 1;
			try
			{
				Thread.sleep( 1000 );
				state[ 0 ] = 2;
				System.out.println( "task: Time out #1" );
			}
			catch ( final InterruptedException e )
			{
				state[ 0 ] = 3;
				System.out.println( "task: Interrupted #1" );
			}

			try
			{
				for ( int i = 0; i < 100 && !finishFast[ 0 ]; i++ )
				{
					Thread.sleep( 10 );
				}
				Thread.sleep( 20 );
				state[ 0 ] = 4;
				System.out.println( "task: Time out #2" );
			}
			catch ( final InterruptedException e )
			{
				state[ 0 ] = 5;
				System.out.println( "task: Interrupted #2" );
			}
		}, null );

		System.out.println( "Execute task" );
		_executor.execute( task );

		System.out.println( "Waiting for start" );
		for ( int i = 0; state[ 0 ] == 0 && i < 100; i++ )
		{
			Thread.sleep( 10 );
		}
		assertEquals( "Expected task to be started", 1, state[ 0 ] );

		System.out.println( "Cancel without waiting" );
		task.cancel( true );
		for ( int i = 0; state[ 0 ] == 1 && i < 100; i++ )
		{
			Thread.sleep( 10 );
		}
		assertEquals( "Expected task to be interrupted, but still running", 3, state[ 0 ] );
		Thread.sleep( 10 );
		assertEquals( "Expected task to remain running 10ms later", 3, state[ 0 ] );

		System.out.println( "Cancel and wait for timeout" );
		assertFalse( "Cancel should timeout after 10ms", task.cancelAndWait( true, 20, TimeUnit.MILLISECONDS ) );
		assertEquals( "Expected task to be interrupted, but still running", 3, state[ 0 ] );

		System.out.println( "Cancel and wait" );
		finishFast[ 0 ] = true;
		task.cancelAndWait( true );
		assertEquals( "Expected task to be interrupted, but still running", 4, state[ 0 ] );
	}

	@Test
	public void testGet()
	throws Exception
	{
		final int[] state = { 0 };
		final boolean[] finishFast = { false };

		final WaitingFutureTask<Integer> task = new WaitingFutureTask<>( () -> {
			System.out.println( "task: Started" );
			state[ 0 ] = 1;
			try
			{
				Thread.sleep( 1000 );
				state[ 0 ] = 2;
				System.out.println( "task: Time out #1" );
			}
			catch ( final InterruptedException e )
			{
				state[ 0 ] = 3;
				System.out.println( "task: Interrupted #1" );

				try
				{
					for ( int i = 0; i < 100 && !finishFast[ 0 ]; i++ )
					{
						Thread.sleep( 10 );
					}
					Thread.sleep( 20 );
					state[ 0 ] = 4;
					System.out.println( "task: Finished" );
				}
				catch ( final InterruptedException e2 )
				{
					state[ 0 ] = 5;
					System.out.println( "task: Interrupted #2" );
				}
			}
		}, null );

		System.out.println( "Execute task" );
		_executor.execute( task );

		System.out.println( "Waiting for start" );
		for ( int i = 0; state[ 0 ] == 0 && i < 100; i++ )
		{
			Thread.sleep( 10 );
		}

		System.out.println( "Cancel without waiting" );
		task.cancel( true );
		for ( int i = 0; state[ 0 ] == 1 && i < 100; i++ )
		{
			Thread.sleep( 10 );
		}
		assertEquals( "Expected task to be interrupted, but still running", 3, state[ 0 ] );

		System.out.println( "Request result using 'get()' with timeout" );
		finishFast[ 0 ] = true;
		try
		{
			task.get( 20, TimeUnit.MILLISECONDS );
			fail( "Expected CancellationException" );
		}
		catch ( final CancellationException e )
		{
			assertEquals( "Expected task to be finished", 4, state[ 0 ] );
		}

		System.out.println( "Request result using 'get()'" );
		try
		{
			task.get();
			fail( "Expected CancellationException" );
		}
		catch ( final CancellationException e )
		{
			assertEquals( "Expected task to be finished", 4, state[ 0 ] );
		}
	}

}
