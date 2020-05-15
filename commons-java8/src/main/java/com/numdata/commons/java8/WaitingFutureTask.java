/*
 * Original code: https://stackoverflow.com/questions/6040962/wait-for-cancel-on-futuretask?answertab=oldest#tab-top
 * Original authors: Aleksandr Dubinsky, Aleksey Otrubennikov, FooJBar
 */
package com.numdata.commons.java8;

import java.util.concurrent.*;

import org.jetbrains.annotations.*;

/**
 * This extension of the {@link FutureTask} class allows waiting for a cancelled
 * task.
 *
 * <p>Please check for details and code from Aleksandr Dubinsky at Stack Overflow article <a href="https://stackoverflow.com/questions/6040962/wait-for-cancel-on-futuretask">Wait for cancel() on FutureTask</a>.
 *
 * @param <T> Result type returned by {@code get} methods.
 *
 * @author Aleksandr Dubinsky, Aleksey Otrubennikov, FooJBar
 */
public class WaitingFutureTask<T>
extends FutureTask<T>
{
	/**
	 * Creates a {@code FutureTask} that will, upon running, execute the given {@code Runnable},
	 * and arrange that {@code get} will return the given result on successful completion.
	 *
	 * @param runnable the runnable task
	 * @param result   the result to return on successful completion.
	 *                 If you don't need a particular result, consider using constructions of the form:
	 *                 {@code Future<?> f = new FutureTask<Void>(runnable, null)}
	 *
	 * @throws NullPointerException if the runnable is null
	 */
	public WaitingFutureTask( @NotNull final Runnable runnable, @Nullable final T result )
	{
		this( Executors.callable( runnable, result ) );
	}

	/**
	 * Creates a {@code FutureTask} that will, upon running, execute the given {@code Callable}.
	 *
	 * @param callable the callable task
	 *
	 * @throws NullPointerException if the callable is null
	 */
	public WaitingFutureTask( @NotNull final Callable<T> callable )
	{
		this( new MyCallable<>( callable ) );
	}

	/**
	 * Some ugly code to work around the compiler's limitations on constructors.
	 *
	 * @param myCallable Wrapped callable.
	 */
	private WaitingFutureTask( @NotNull final MyCallable<T> myCallable )
	{
		super( myCallable );
		myCallable.task = this;
	}

	private final Semaphore semaphore = new Semaphore( 1 );

	private static class MyCallable<T>
	implements Callable<T>
	{
		MyCallable( final Callable<T> callable )
		{
			this.callable = callable;
		}

		final Callable<T> callable;

		WaitingFutureTask<T> task;

		@Override
		public T
		call()
		throws Exception
		{
			task.semaphore.acquire();
			try
			{
				if ( task.isCancelled() )
				{
					return null;
				}

				return callable.call();
			}
			finally
			{
				task.semaphore.release();
			}
		}
	}

	/**
	 * Waits if necessary for the computation to complete or finish cancelling, and then retrieves its result, if available.
	 *
	 * @return the computed result
	 *
	 * @throws CancellationException if the computation was cancelled
	 * @throws ExecutionException if the computation threw an exception
	 * @throws InterruptedException if the current thread was interrupted while waiting
	 */
	@Override
	public T get()
	throws InterruptedException, ExecutionException, CancellationException
	{

		try
		{
			return super.get();
		}
		catch ( final CancellationException e )
		{
			semaphore.acquire();
			semaphore.release();
			throw e;
		}
	}

	/**
	 * Waits if necessary for at most the given time for the computation to complete or finish cancelling, and then retrieves its result, if available.
	 *
	 * @param timeout the maximum time to wait
	 * @param unit    the time unit of the timeout argument
	 *
	 * @return the computed result
	 *
	 * @throws CancellationException if the computation was cancelled
	 * @throws ExecutionException if the computation threw an exception
	 * @throws InterruptedException if the current thread was interrupted while waiting
	 * @throws TimeoutException if the wait timed out
	 */
	@Override
	public T get( final long timeout, @NotNull final TimeUnit unit )
	throws InterruptedException, ExecutionException, CancellationException, TimeoutException
	{
		try
		{
			return super.get( timeout, unit );
		}
		catch ( final CancellationException e )
		{
			semaphore.acquire();
			semaphore.release();
			throw e;
		}
	}

	/**
	 * Attempts to cancel execution of this task and waits for the task to complete if it has been started.
	 * If the task has not started when {@code cancelAndWait} is called, this task should never run.
	 * If the task has already started, then the {@code mayInterruptIfRunning} parameter determines
	 * whether the thread executing this task should be interrupted in an attempt to stop the task.
	 *
	 * <p>After this method returns, subsequent calls to {@link #isDone} will
	 * always return {@code true}.  Subsequent calls to {@link #isCancelled}
	 * will always return {@code true}.
	 *
	 * @param mayInterruptIfRunning {@code true} if the thread executing this task should be interrupted;
	 *                              otherwise, in-progress tasks are allowed to complete
	 *
	 * @throws InterruptedException if the thread is interrupted
	 */
	public void cancelAndWait( final boolean mayInterruptIfRunning )
	throws InterruptedException
	{
		cancel( mayInterruptIfRunning );
		semaphore.acquire();
		semaphore.release();
	}

	/**
	 * Attempts to cancel execution of this task and waits for the task to complete if it has been started.
	 * If the task has not started when {@code cancelAndWait} is called, this task should never run.
	 * If the task has already started, then the {@code mayInterruptIfRunning} parameter determines
	 * whether the thread executing this task should be interrupted in an attempt to stop the task.
	 *
	 * If the task is completed, this return returns {@code true}. If the task
	 * is not completed within the given time limit, this method will return
	 * {@code false}.
	 *
	 * <p>After this method returns, subsequent calls to {@link #isDone} will
	 * always return {@code true}. Subsequent calls to {@link #isCancelled}
	 * will always return {@code true}.
	 *
	 * @param mayInterruptIfRunning {@code true} if the thread executing this task should be interrupted;
	 *                              otherwise, in-progress tasks are allowed to complete
	 * @param timeout               Maximum time to wait for the task to complete.
	 * @param timeUnit              Unit of the {@code timeout} argument
	 *
	 * @return {@code true} if execution was cancelled; {@code false} if timeout occurred.
	 *
	 * @throws InterruptedException if the thread is interrupted
	 */
	public boolean cancelAndWait( final boolean mayInterruptIfRunning, final int timeout, @NotNull final TimeUnit timeUnit )
	throws InterruptedException
	{
		cancel( mayInterruptIfRunning );
		final boolean result = semaphore.tryAcquire( timeout, timeUnit );
		if ( result )
		{
			semaphore.release();
		}
		return result;
	}
}
