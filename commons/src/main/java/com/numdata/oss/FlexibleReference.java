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

import java.lang.ref.*;

/**
 * A reference that may alternate between soft and hard reference behavior in
 * response to invocations of its {@link #soften()} and {@link #harden()}
 * methods. A flexible reference is initially a hard reference.
 *
 * A reference that is cleared by the garbage collector (while behaving as a
 * soft reference), is from that point on considered to be neither a hard
 * reference nor a soft reference. Any subsequent calls to {@link #soften()} or
 * {@link #harden()} will return {@code false} and have no effect.
 *
 * @author G. Meinders
 */
public class FlexibleReference<T>
extends SoftReference<T>
{
	/**
	 * Hard reference to the value, or {@code null} if the reference is
	 * currently soft.
	 */
	T _referent;

	/**
	 * Constructs a new hard reference to the given object.
	 *
	 * @param referent Object to refer to.
	 *
	 * @throws NullPointerException if {@code referent} is {@code null}.
	 */
	public FlexibleReference( final T referent )
	{
		super( referent );
		if ( referent == null )
		{
			throw new NullPointerException( "referent" );
		}
		_referent = referent;
	}

	/**
	 * Constructs a new hard reference to the given object.
	 *
	 * @param referent Object to refer to.
	 * @param queue    Reference queue to register with; {@code null} if not
	 *                 needed.
	 *
	 * @throws NullPointerException if {@code referent} is {@code null}.
	 */
	public FlexibleReference( final T referent, final ReferenceQueue<? super T> queue )
	{
		super( referent, queue );
		if ( referent == null )
		{
			throw new NullPointerException( "referent" );
		}
		_referent = referent;
	}

	/**
	 * Switches to the behavior of a soft reference.
	 *
	 * @return {@code true} if this was previously a hard reference; {@code
	 * false} otherwise.
	 */
	public boolean soften()
	{
		final boolean result = ( _referent != null ) && ( get() != null );
		if ( result )
		{
			_referent = null;
		}
		return result;
	}

	/**
	 * Switches to the behavior of a hard reference.
	 *
	 * @return {@code true} if this was previously a soft reference; {@code
	 * false} otherwise.
	 */
	public boolean harden()
	{
		final T value = get();

		final boolean result = ( _referent == null ) && ( value != null );
		if ( result )
		{
			_referent = value;
		}
		return result;
	}

	/**
	 * Returns whether the reference is currently a soft reference. A reference
	 * with a {@code null} value, either due to being cleared or otherwise, is
	 * soft nor hard.
	 *
	 * @return {@code true} if this is a soft reference; {@code false}
	 * otherwise.
	 */
	public boolean isSoft()
	{
		return ( ( _referent == null ) && ( get() != null ) );
	}

	/**
	 * Returns whether the reference is currently a soft reference. A reference
	 * with a {@code null} value, either due to being cleared or otherwise, is
	 * soft nor hard.
	 *
	 * @return {@code true} if this is a hard reference; {@code false}
	 * otherwise.
	 */
	public boolean isHard()
	{
		return ( ( _referent != null ) && ( get() != null ) );
	}
}
