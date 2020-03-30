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

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.jetbrains.annotations.*;

/**
 * This class contains utilities for working the Java 8 {@code java.util.stream}
 * package.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "unused", "FinalClass" } )
public final class StreamTools
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private StreamTools()
	{
	}

	/**
	 * Creates a {@code Stream} for an {@code Iterable}.
	 *
	 * @param <T>      Element type.
	 * @param iterable {@link Iterable}.
	 *
	 * @return {@link Stream}.
	 */
	@NotNull
	public static <T> Stream<T> stream( @NotNull final Iterable<T> iterable )
	{
		return stream( iterable.iterator() );
	}

	/**
	 * Creates a {@code Stream} for an {@code Iterator}.
	 *
	 * @param <T>      Element type.
	 * @param iterator {@link Iterator}.
	 *
	 * @return {@link Stream}.
	 */
	@NotNull
	public static <T> Stream<T> stream( @NotNull final Iterator<T> iterator )
	{
		return StreamSupport.stream( Spliterators.spliteratorUnknownSize( iterator, 0 ), false );
	}

	/**
	 * {@link Collectors#toMap(Function, Function, BinaryOperator, Supplier)}
	 * with throwing merge function.
	 *
	 * @param <T>         the type of the input elements
	 * @param <K>         the output type of the key mapping function
	 * @param <U>         the output type of the value mapping function
	 * @param <M>         the type of the resulting {@code Map}
	 * @param keyMapper   a mapping function to produce keys
	 * @param valueMapper a mapping function to produce values
	 * @param mapSupplier a function which returns a new, empty {@code Map} into
	 *                    which the results will be inserted
	 *
	 * @return {@code Collector}.
	 *
	 * @see Collectors#toMap(Function, Function, BinaryOperator, Supplier)
	 */
	@NotNull
	public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap( final Function<? super T, ? extends K> keyMapper, @NotNull final Function<? super T, ? extends U> valueMapper, @NotNull final Supplier<M> mapSupplier )
	{
		return Collectors.toMap( keyMapper, valueMapper, ( u, v ) -> {
			throw new IllegalStateException( String.format( "Duplicate key %s", u ) );
		}, mapSupplier );
	}

	/**
	 * Collects the elements of a stream in a {@link HashSet}, but returns
	 * {@link Collections#emptySet} if the stream is empty.
	 *
	 * @param <T>    Element type.
	 * @param stream Stream to reduce.
	 *
	 * @return {@link Set}.
	 */
	@NotNull
	public static <T> Set<T> toSet( @NotNull final Stream<T> stream )
	{
		return toSet( stream, HashSet::new );
	}

	/**
	 * Collects the elements of a stream in a {@link Set}, but returns
	 * {@link Collections#emptySet} if the stream is empty.
	 *
	 * This is equivalent to:
	 * <pre>{@code
	 *     Set<T> result = Collections.emptySet();
	 *     for ( T element : stream )
	 *     {
	 *         if ( result.isEmpty() )
	 *         {
	 *             result = setSupplier.get();
	 *         }
	 *         result.add( element );
	 *     }
	 *     return result;
	 * }</pre>
	 * but is not constrained to execute sequentially.
	 *
	 * @param <T>         Element type.
	 * @param stream      Stream to reduce.
	 * @param setSupplier Supplier for non-empty result.
	 *
	 * @return {@link Set}.
	 */
	@NotNull
	public static <T> Set<T> toSet( @NotNull final Stream<T> stream, @NotNull final Supplier<Set<T>> setSupplier )
	{
		final BiFunction<Set<T>, T, Set<T>> accumulator = ( set, i ) -> {
			final Set<T> result = set.isEmpty() ? setSupplier.get() : set;
			result.add( i );
			return result;
		};

		final BinaryOperator<Set<T>> combiner = ( set1, set2 ) -> {
			final Set<T> result;
			if ( set1.isEmpty() )
			{
				result = set2;
			}
			else
			{
				set1.addAll( set2 );
				result = set1;
			}
			return result;
		};

		return stream.reduce( Collections.emptySet(), accumulator, combiner );
	}
}
