/*
 * Copyright (c) 2007-2017, Numdata BV, The Netherlands.
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

import java.io.*;

/**
 * Wraps an object using a transient field. An object that can't or shouldn't be
 * serialized can be wrapped to effectively make it disappear when the wrapper
 * is deserialized.
 *
 * @author G. Meinders
 */
public class TransientWrapper<T>
implements Serializable
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -973764791399450812L;

	/**
	 * Object being wrapped.
	 */
	private transient T _content;

	/**
	 * Construct new transient wrapper, initially without any content.
	 */
	public TransientWrapper()
	{
		this( null );
	}

	/**
	 * Construct new transient wrapper with the specified content.
	 *
	 * @param content Content to be wrapped.
	 */
	public TransientWrapper( final T content )
	{
		_content = content;
	}

	/**
	 * Returns the content wrapped by the object.
	 *
	 * @return Wrapped content.
	 */
	public T getContent()
	{
		return _content;
	}

	/**
	 * Sets the content wrapped by the object.
	 *
	 * @param content Content to be wrapped.
	 *
	 * @return Previously wrapped content.
	 */
	public Object setContent( final T content )
	{
		final Object result = _content;
		_content = content;
		return result;
	}

	public String toString()
	{
		return super.toString() + ": " + _content;
	}
}
