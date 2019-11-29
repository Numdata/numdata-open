/*
 * Copyright (c) 2019-2019, Numdata BV, The Netherlands.
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
		return element -> {
			try
			{
				return checkedFunction.apply( element );
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
		return element -> {
			try
			{
				return checkedFunction.apply( element );
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
		return element -> {
			try
			{
				checkedConsumer.apply( element );
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
		return element -> {
			try
			{
				checkedConsumer.apply( element );
			}
			catch ( final Exception ex )
			{
				sneakyThrow( ex );
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
