/*
 * Copyright (c) 2014-2017, Numdata BV, The Netherlands.
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
import java.math.*;

import static com.numdata.oss.BigDecimalTools.*;

/**
 * Encapsulates a width and a height in a single object.
 *
 * @author Gerrit Meinders
 */
public class DecimalDimension
implements Serializable
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -2513384297645149434L;

	/**
	 * Width.
	 */
	private BigDecimal _width = BigDecimal.ZERO;

	/**
	 * Height.
	 */
	private BigDecimal _height = BigDecimal.ZERO;

	/**
	 * Constructs a new dimension with zero width and height.
	 */
	public DecimalDimension()
	{
	}

	/**
	 * Constructs a new dimension with the specified width and height.
	 *
	 * @param width  Width to be set.
	 * @param height Height to be set.
	 */
	public DecimalDimension( final BigDecimal width, final BigDecimal height )
	{
		_width = width;
		_height = height;
	}

	/**
	 * Constructs a new dimension with the same width and height as the given
	 * original.
	 *
	 * @param original Dimension to construct a duplicate of.
	 */
	public DecimalDimension( final DecimalDimension original )
	{
		this( original._width, original._height );
	}

	/**
	 * Returns the width.
	 *
	 * @return Width.
	 */
	public BigDecimal getWidth()
	{
		return _width;
	}

	/**
	 * Returns the height.
	 *
	 * @return Height.
	 */
	public BigDecimal getHeight()
	{
		return _height;
	}

	@Override
	public boolean equals( final Object obj )
	{
		final boolean result;
		if ( obj instanceof DecimalDimension )
		{
			final DecimalDimension other = (DecimalDimension)obj;
			result = isEqual( _width, other.getWidth() ) && isEqual( _height, other.getHeight() );
		}
		else
		{
			result = false;
		}
		return result;
	}

	@Override
	public int hashCode()
	{
		return _width.hashCode() ^ _height.hashCode();
	}

	@Override
	public String toString()
	{
		return "(" + _width + ',' + _height + ')';
	}
}
