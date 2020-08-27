/*
 * Copyright (c) 2019-2020, Numdata BV, The Netherlands.
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

import org.jetbrains.annotations.*;

/**
 * This class contains utilities for working the Java 8 {@code
 * java.util.function} package.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "unused", "FinalClass" } )
public final class FunctionTools
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private FunctionTools()
	{
	}

	/**
	 * Wrap a {@link CheckedFunction} with an unchecked {@link Function}. Any
	 * checked exception thrown by the {@link CheckedFunction} will be wrapped
	 * by a {@link RuntimeException}.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedFunction {@link CheckedFunction} to wrap.
	 * @param <T>             Input type of the function.
	 * @param <R>             Result type of the function.
	 *
	 * @return {@link Function}.
	 */
	@NotNull
	@Contract( pure = true )
	public static <T, R> Function<T, R> wrapFunctionException( @NotNull final CheckedFunction<T, R, ?> checkedFunction )
	{
		return t -> {
			try
			{
				return checkedFunction.apply( t );
			}
			catch ( final RuntimeException | Error e )
			{
				throw e;
			}
			catch ( final Exception e )
			{
				throw new RuntimeException( e );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedFunction} with an unchecked {@link Function}, but
	 * still declare the checked exception that might abort the stream and be
	 * thrown.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedFunction {@link CheckedFunction} to wrap.
	 * @param <EX>            Checked exception type.
	 * @param <T>             Input type of the function.
	 * @param <R>             Result type of the function.
	 *
	 * @return {@link Function}.
	 *
	 * @throws EX checked exception from {@code checkedFunction}.
	 */
	@SuppressWarnings( "RedundantThrows" )
	@NotNull
	@Contract( pure = true )
	public static <T, R, EX extends Exception> Function<T, R> liftFunctionException( @NotNull final CheckedFunction<T, R, EX> checkedFunction )
	throws EX
	{
		return hideFunctionException( checkedFunction );
	}

	/**
	 * Wrap a {@link CheckedFunction} with an unchecked {@link Function}.
	 *
	 * IMPORTANT: Never use this method directly, because it will cause checked
	 * exceptions to appear where they are not declared. Please use the {@link
	 * #liftFunctionException(CheckedFunction)} method instead to declare the
	 * checked exception.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedFunction {@link CheckedFunction} to wrap.
	 * @param <EX>            Checked exception type.
	 * @param <T>             Input type of the function.
	 * @param <R>             Result type of the function.
	 *
	 * @return {@link Function}.
	 */
	@NotNull
	@Contract( pure = true )
	private static <T, R, EX extends Exception> Function<T, R> hideFunctionException( @NotNull final CheckedFunction<T, R, EX> checkedFunction )
	{
		return t -> {
			try
			{
				return checkedFunction.apply( t );
			}
			catch ( final Exception ex )
			{
				return sneakyThrow( ex );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedBiFunction} with an unchecked {@link BiFunction}. Any
	 * checked exception thrown by the {@link CheckedBiFunction} will be wrapped
	 * by a {@link RuntimeException}.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedBiFunction {@link CheckedBiFunction} to wrap.
	 * @param <A>               First input type of the biFunction.
	 * @param <B>               Second input type of the biFunction.
	 * @param <R>               Result type of the biFunction.
	 *
	 * @return {@link BiFunction}.
	 */
	@NotNull
	@Contract( pure = true )
	public static <A, B, R> BiFunction<A, B, R> wrapBiFunctionException( @NotNull final CheckedBiFunction<A, B, R, ?> checkedBiFunction )
	{
		return ( a, b ) -> {
			try
			{
				return checkedBiFunction.apply( a, b );
			}
			catch ( final RuntimeException | Error e )
			{
				throw e;
			}
			catch ( final Exception e )
			{
				throw new RuntimeException( e );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedBiFunction} with an unchecked {@link BiFunction}, but
	 * still declare the checked exception that might abort the stream and be
	 * thrown.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedBiFunction {@link CheckedBiFunction} to wrap.
	 * @param <EX>              Checked exception type.
	 * @param <A>               First input type of the biFunction.
	 * @param <B>               Second input type of the biFunction.
	 * @param <R>               Result type of the biFunction.
	 *
	 * @return {@link BiFunction}.
	 *
	 * @throws EX checked exception from {@code checkedBiFunction}.
	 */
	@SuppressWarnings( "RedundantThrows" )
	@NotNull
	@Contract( pure = true )
	public static <A, B, R, EX extends Exception> BiFunction<A, B, R> liftBiFunctionException( @NotNull final CheckedBiFunction<A, B, R, EX> checkedBiFunction )
	throws EX
	{
		return hideBiFunctionException( checkedBiFunction );
	}

	/**
	 * Wrap a {@link CheckedBiFunction} with an unchecked {@link BiFunction}.
	 *
	 * IMPORTANT: Never use this method directly, because it will cause checked
	 * exceptions to appear where they are not declared. Please use the {@link
	 * #liftBiFunctionException(CheckedBiFunction)} method instead to declare the
	 * checked exception.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedBiFunction {@link CheckedBiFunction} to wrap.
	 * @param <EX>              Checked exception type.
	 * @param <A>               First input type of the biFunction.
	 * @param <B>               Second input type of the biFunction.
	 * @param <R>               Result type of the biFunction.
	 *
	 * @return {@link BiFunction}.
	 */
	@NotNull
	@Contract( pure = true )
	private static <A, B, R, EX extends Exception> BiFunction<A, B, R> hideBiFunctionException( @NotNull final CheckedBiFunction<A, B, R, EX> checkedBiFunction )
	{
		return ( a, b ) -> {
			try
			{
				return checkedBiFunction.apply( a, b );
			}
			catch ( final Exception ex )
			{
				return sneakyThrow( ex );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedConsumer} with an unchecked {@link Consumer}. Any
	 * checked exception thrown by the {@link CheckedConsumer} will be wrapped
	 * by a {@link RuntimeException}.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedConsumer {@link CheckedConsumer} to wrap.
	 * @param <T>             Input type of the consumer.
	 *
	 * @return {@link Consumer}.
	 */
	@NotNull
	@Contract( pure = true )
	public static <T> Consumer<T> wrapConsumerException( @NotNull final CheckedConsumer<T, ?> checkedConsumer )
	{
		return t -> {
			try
			{
				checkedConsumer.apply( t );
			}
			catch ( final RuntimeException | Error e )
			{
				throw e;
			}
			catch ( final Exception e )
			{
				throw new RuntimeException( e );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedConsumer} with an unchecked {@link Consumer}, but
	 * still declare the checked exception that might abort the stream and be
	 * thrown.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedConsumer {@link CheckedConsumer} to wrap.
	 * @param <EX>            Checked exception type.
	 * @param <T>             Input type of the consumer.
	 *
	 * @return {@link Consumer}.
	 *
	 * @throws EX checked exception from {@code checkedConsumer}.
	 */
	@SuppressWarnings( "RedundantThrows" )
	@NotNull
	@Contract( pure = true )
	public static <T, EX extends Exception> Consumer<T> liftConsumerException( @NotNull final CheckedConsumer<T, EX> checkedConsumer )
	throws EX
	{
		return hideConsumerException( checkedConsumer );
	}

	/**
	 * Wrap a {@link CheckedConsumer} with an unchecked {@link Consumer}.
	 *
	 * IMPORTANT: Never use this method directly, because it will cause checked
	 * exceptions to appear where they are not declared. Please use the {@link
	 * #liftConsumerException(CheckedConsumer)} method instead to declare the
	 * checked exception.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedConsumer {@link CheckedConsumer} to wrap.
	 * @param <EX>            Checked exception type.
	 * @param <T>             Input type of the consumer.
	 *
	 * @return {@link Consumer}.
	 */
	@NotNull
	@Contract( pure = true )
	private static <T, EX extends Exception> Consumer<T> hideConsumerException( @NotNull final CheckedConsumer<T, EX> checkedConsumer )
	{
		return t -> {
			try
			{
				checkedConsumer.apply( t );
			}
			catch ( final Exception ex )
			{
				sneakyThrow( ex );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedIntConsumer} with an unchecked {@link IntConsumer}.
	 * Any checked exception thrown by the {@link CheckedIntConsumer} will be
	 * wrapped by a {@link RuntimeException}.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedIntConsumer {@link CheckedIntConsumer} to wrap.
	 *
	 * @return {@link IntConsumer}.
	 */
	@NotNull
	@Contract( pure = true )
	public static IntConsumer wrapIntConsumerException( @NotNull final CheckedIntConsumer<?> checkedIntConsumer )
	{
		return t -> {
			try
			{
				checkedIntConsumer.apply( t );
			}
			catch ( final RuntimeException | Error e )
			{
				throw e;
			}
			catch ( final Exception e )
			{
				throw new RuntimeException( e );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedIntConsumer} with an unchecked {@link IntConsumer},
	 * but still declare the checked exception that might abort the stream and
	 * be thrown.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedIntConsumer {@link CheckedIntConsumer} to wrap.
	 * @param <EX>               Checked exception type.
	 *
	 * @return {@link IntConsumer}.
	 *
	 * @throws EX checked exception from {@code checkedIntConsumer}.
	 */
	@SuppressWarnings( "RedundantThrows" )
	@NotNull
	@Contract( pure = true )
	public static <EX extends Exception> IntConsumer liftIntConsumerException( @NotNull final CheckedIntConsumer<EX> checkedIntConsumer )
	throws EX
	{
		return hideIntConsumerException( checkedIntConsumer );
	}

	/**
	 * Wrap a {@link CheckedIntConsumer} with an unchecked {@link IntConsumer}.
	 *
	 * IMPORTANT: Never use this method directly, because it will cause checked
	 * exceptions to appear where they are not declared. Please use the {@link
	 * #liftIntConsumerException(CheckedIntConsumer)} method instead to declare
	 * the checked exception.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedIntConsumer {@link CheckedIntConsumer} to wrap.
	 * @param <EX>               Checked exception type.
	 *
	 * @return {@link IntConsumer}.
	 */
	@NotNull
	@Contract( pure = true )
	private static <EX extends Exception> IntConsumer hideIntConsumerException( @NotNull final CheckedIntConsumer<EX> checkedIntConsumer )
	{
		return t -> {
			try
			{
				checkedIntConsumer.apply( t );
			}
			catch ( final Exception ex )
			{
				sneakyThrow( ex );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedSupplier} with an unchecked {@link Supplier}. Any
	 * checked exception thrown by the {@link CheckedSupplier} will be wrapped
	 * by a {@link RuntimeException}.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedSupplier {@link CheckedSupplier} to wrap.
	 * @param <T>             Result type of supplier.
	 *
	 * @return {@link Supplier}.
	 */
	@NotNull
	@Contract( pure = true )
	public static <T> Supplier<T> wrapSupplierException( @NotNull final CheckedSupplier<T, ?> checkedSupplier )
	{
		return () -> {
			try
			{
				return checkedSupplier.get();
			}
			catch ( final RuntimeException | Error e )
			{
				throw e;
			}
			catch ( final Exception e )
			{
				throw new RuntimeException( e );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedSupplier} with an unchecked {@link Supplier}, but
	 * still declare the checked exception that might abort the stream and be
	 * thrown.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedSupplier {@link CheckedSupplier} to wrap.
	 * @param <EX>            Checked exception type.
	 * @param <T>             Result type of supplier.
	 *
	 * @return {@link Supplier}.
	 *
	 * @throws EX checked exception from {@code checkedSupplier}.
	 */
	@SuppressWarnings( "RedundantThrows" )
	@NotNull
	@Contract( pure = true )
	public static <T, EX extends Exception> Supplier<T> liftSupplierException( @NotNull final CheckedSupplier<T, EX> checkedSupplier )
	throws EX
	{
		return hideSupplierException( checkedSupplier );
	}

	/**
	 * Wrap a {@link CheckedSupplier} with an unchecked {@link Supplier}.
	 *
	 * IMPORTANT: Never use this method directly, because it will cause checked
	 * exceptions to appear where they are not declared. Please use the {@link
	 * #liftSupplierException(CheckedSupplier)} method instead to declare the
	 * checked exception.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedSupplier {@link CheckedSupplier} to wrap.
	 * @param <EX>            Checked exception type.
	 * @param <T>             Result type of supplier.
	 *
	 * @return {@link Supplier}.
	 */
	@NotNull
	@Contract( pure = true )
	private static <T, EX extends Exception> Supplier<T> hideSupplierException( @NotNull final CheckedSupplier<T, EX> checkedSupplier )
	{
		return () -> {
			try
			{
				return checkedSupplier.get();
			}
			catch ( final Exception ex )
			{
				return sneakyThrow( ex );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedIntSupplier} with an unchecked {@link IntSupplier}.
	 * Any checked exception thrown by the {@link CheckedIntSupplier} will be
	 * wrapped by a {@link RuntimeException}.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedIntSupplier {@link CheckedIntSupplier} to wrap.
	 *
	 * @return {@link IntSupplier}.
	 */
	@NotNull
	@Contract( pure = true )
	public static IntSupplier wrapIntSupplierException( @NotNull final CheckedIntSupplier<?> checkedIntSupplier )
	{
		return () -> {
			try
			{
				return checkedIntSupplier.get();
			}
			catch ( final RuntimeException | Error e )
			{
				throw e;
			}
			catch ( final Exception e )
			{
				throw new RuntimeException( e );
			}
		};
	}

	/**
	 * Wrap a {@link CheckedIntSupplier} with an unchecked {@link IntSupplier},
	 * but still declare the checked exception that might abort the stream and
	 * be thrown.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedIntSupplier {@link CheckedIntSupplier} to wrap.
	 * @param <EX>               Checked exception type.
	 *
	 * @return {@link IntSupplier}.
	 *
	 * @throws EX checked exception from {@code checkedIntSupplier}.
	 */
	@SuppressWarnings( "RedundantThrows" )
	@NotNull
	@Contract( pure = true )
	public static <EX extends Exception> IntSupplier liftIntSupplierException( @NotNull final CheckedIntSupplier<EX> checkedIntSupplier )
	throws EX
	{
		return hideIntSupplierException( checkedIntSupplier );
	}

	/**
	 * Wrap a {@link CheckedIntSupplier} with an unchecked {@link IntSupplier}.
	 *
	 * IMPORTANT: Never use this method directly, because it will cause checked
	 * exceptions to appear where they are not declared. Please use the {@link
	 * #liftIntSupplierException(CheckedIntSupplier)} method instead to declare
	 * the checked exception.
	 *
	 * See: <a href='https://blog.codefx.org/java/repackaging-exceptions-streams/'>Repackaging
	 * Exceptions In Streams</a>.
	 *
	 * @param checkedIntSupplier {@link CheckedIntSupplier} to wrap.
	 * @param <EX>               Checked exception type.
	 *
	 * @return {@link IntSupplier}.
	 */
	@NotNull
	@Contract( pure = true )
	private static <EX extends Exception> IntSupplier hideIntSupplierException( @NotNull final CheckedIntSupplier<EX> checkedIntSupplier )
	{
		return () -> {
			try
			{
				return checkedIntSupplier.get();
			}
			catch ( final Exception ex )
			{
				return sneakyThrow( ex );
			}
		};
	}

	/**
	 * Internal method to hide an exception that is thrown.
	 *
	 * @param throwable What to throw.
	 * @param <E>       Bogus un-checked exception type.
	 * @param <T>       Bogus method return type (never returns).
	 *
	 * @return Nothing (never returns).
	 *
	 * @throws E The throwable, but no longer checked.
	 */
	private static <E extends Throwable, T> T sneakyThrow( @NotNull final Throwable throwable )
	throws E
	{
		throw (E)throwable;
	}
}
