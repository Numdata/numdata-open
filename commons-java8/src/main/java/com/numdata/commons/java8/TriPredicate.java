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
package com.numdata.commons.java8;

import org.jetbrains.annotations.*;

/**
 * Represents a predicate that accepts three arguments.
 *
 * @param <A> First argument type.
 * @param <B> Second argument type.
 * @param <C> Third argument type.
 *
 * @author Peter S. Heijnen
 */
@FunctionalInterface
public interface TriPredicate<A, B, C>
{
	/**
	 * Evaluates this predicate with the given arguments.
	 *
	 * @param a First argument.
	 * @param b Second argument.
	 * @param c Third argument.
	 *
	 * @return {@code true} if predicate is matched.
	 */
	boolean test( A a, B b, C c );

	/**
	 * Returns {@link TriPredicate} that negates this predicate.
	 *
	 * @return {@link TriPredicate} that negates this predicate.
	 */
	@NotNull
	default TriPredicate<A, B, C> negate()
	{
		return ( A a, B b, C c ) -> !test( a, b, c );
	}

	/**
	 * Returns {@link TriPredicate} that is a logical AND between this and the
	 * given predicate.
	 *
	 * NOTE: The other predicate is not evaluated if this predicate evaluates to
	 * {@code false} (short-circuit).
	 *
	 * @param other Predicate to combine with this predicate.
	 *
	 * @return {@link TriPredicate} that is a logical AND between this and the
	 * given predicate.
	 */
	@NotNull
	default TriPredicate<A, B, C> and( @NotNull final TriPredicate<? super A, ? super B, ? super C> other )
	{
		return ( A a, B b, C c ) -> test( a, b, c ) && other.test( a, b, c );
	}

	/**
	 * Returns {@link TriPredicate} that is a logical OR between this and the
	 * given predicate.
	 *
	 * NOTE: The other predicate is not evaluated if this predicate evaluates to
	 * {@code true} (short-circuit).
	 *
	 * @param other Predicate to combine with this predicate.
	 *
	 * @return {@link TriPredicate} that is a logical OR between this and the
	 * given predicate.
	 */
	@NotNull
	default TriPredicate<A, B, C> or( @NotNull final TriPredicate<? super A, ? super B, ? super C> other )
	{
		return ( A a, B b, C c ) -> test( a, b, c ) || other.test( a, b, c );
	}
}
