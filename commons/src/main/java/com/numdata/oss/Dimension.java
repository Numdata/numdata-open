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

//import java.awt.geom.Dimension2D;
//import java.io.Serializable;

/**
 * Encapsulates a width and a height in a single object, in double precision.
 *
 * @author G. Meinders
 */
public class Dimension
//extends Dimension2D
//implements Serializable
{
	//private static final long serialVersionUID = 8906757656971850576L;

	/**
	 * Width.
	 */
	private double _width = 0.0;

	/**
	 * Height.
	 */
	private double _height = 0.0;

	/**
	 * Constructs a new dimension with zero width and height.
	 */
	public Dimension()
	{
	}

	/**
	 * Constructs a new dimension with the specified width and height.
	 *
	 * @param width  Width to be set.
	 * @param height Height to be set.
	 */
	public Dimension( final double width, final double height )
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
	public Dimension( final Dimension original )
	{
		this( original._width, original._height );
	}

	public double getWidth()
	{
		return _width;
	}

	public double getHeight()
	{
		return _height;
	}

	public boolean equals( final Object obj )
	{
		final boolean result;
		if ( obj instanceof Dimension )
		{
			final Dimension other = (Dimension)obj;
			result = ( _width == other.getWidth() ) && ( _height == other.getHeight() );
		}
		else
		{
			result = false;
		}
		return result;
	}

	public int hashCode()
	{
		final Double width = _width;
		final Double height = _height;
		return width.hashCode() ^ height.hashCode();
	}

	public String toString()
	{
		final Class<?> clazz = getClass();
		return clazz.getName() + '[' + _width + ',' + _height + ']';
	}
}
