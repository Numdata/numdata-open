/*
 * Copyright (c) 2010-2017, Numdata BV, The Netherlands.
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

/**
 * Calculates the moving average within a fixed size window. When an instance is
 * first created, its window is filled with all zeros. With each {@link
 * #update}, the window fills up until the window size is exceeded. At that
 * point, the window remains filled ({@link #isFilled()}) and for each added
 * value, the oldest value in the window will be removed.
 *
 * @author G. Meinders
 */
public class MovingAverage
{
	/**
	 * Sum of all values in the window.
	 */
	private double _total;

	/**
	 * Values in the current window.
	 */
	private final double[] _window;

	/**
	 * Start of the current window.
	 */
	private int _index;

	/**
	 * Whether each value in the window has been set.
	 */
	private boolean _filled;

	/**
	 * Constructs a new moving average with the specified window size.
	 *
	 * @param window Size of the window.
	 */
	public MovingAverage( final int window )
	{
		_total = 0.0;
		_window = new double[ window ];
		_index = 0;
		_filled = false;
	}

	/**
	 * Updates the moving average with the given value.
	 *
	 * @param value Value to update the average with.
	 */
	public void update( final double value )
	{
		int index = _index;
		final double[] window = _window;

		_total += value - window[ index ];
		window[ index ] = value;

		index = ( index + 1 ) % window.length;
		_index = index;

		if ( index == 0 )
		{
			_filled = true;
		}
	}

	/**
	 * Returns the moving average for the current window.
	 *
	 * @return Moving average.
	 */
	public double get()
	{
		return _total / (double)_window.length;
	}

	/**
	 * Returns whether each value in the current window has been set, i.e. the
	 * window is filled.
	 *
	 * @return {@code true} if the window is filled.
	 */
	public boolean isFilled()
	{
		return _filled;
	}
}
