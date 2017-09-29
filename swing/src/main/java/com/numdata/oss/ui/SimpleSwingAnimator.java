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
package com.numdata.oss.ui;

import java.awt.event.*;
import javax.swing.*;

/**
 * This is a simple Swing animator based on a {@link Timer}.
 *
 * @author  Peter S. Heijnen
 */
public abstract class SimpleSwingAnimator
{
	/**
	 * Update interval of timer in milliseconds.
	 */
	private int _updateInterval = 50;

	/**
	 * The running timer.
	 */
	private Timer _timer = null;

	/**
	 * Length of runnning animation is milliseconds.
	 */
	private int _animationLength = -1;

	/**
	 * Value of {@link System#currentTimeMillis()} when the animation was
	 * started.
	 */
	private long _startTime = System.currentTimeMillis();

	/**
	 * Construct animator.
	 */
	protected SimpleSwingAnimator()
	{
	}

	/**
	 * Start animating.
	 */
	public void start()
	{
		stop();

		final int animationLength = startAnimating();
		_animationLength = animationLength;
		if ( animationLength > 0 )
		{
			final Timer timer = new Timer( getUpdateInterval(), new TimerListener() );
			_timer = timer;
			_startTime = System.currentTimeMillis();
			timer.start();
		}
	}

	/**
	 * Stop animating.
	 */
	public void stop()
	{
		final Timer timer = _timer;
		if ( timer != null )
		{
			timer.stop();
			_timer = null;
			stopAnimating();
		}
	}

	/**
	 * Get animation update interval in milliseconds.
	 *
	 * @return  Animation update interval in milliseconds.
	 */
	public int getUpdateInterval()
	{
		return _updateInterval;
	}

	/**
	 * Set animation update interval in milliseconds.
	 *
	 * @param   millis  Animation update interval in milliseconds.
	 */
	public void setUpdateInterval( final int millis )
	{
		_updateInterval = millis;
	}

	/**
	 * This method is called to start animating. This should indicate how
	 * long the animation will run for.
	 *
	 * @return  Length of animation in milliseconds;
	 *          0 to run indefinitely.
	 */
	protected abstract int startAnimating();

	/**
	 * This method is called to update the animation.
	 *
	 * @param   timePassed  Time passed since start in milliseconds.
	 * @param   progress    Progress for finite animation (0.0-1.0).
	 */
	protected abstract void updateAnimation( int timePassed, double progress );

	/**
	 * This method is called to stop animating.
	 */
	protected abstract void stopAnimating();

	/**
	 * This listener is attached to the {@link Timer}.
	 */
	private class TimerListener
		implements ActionListener
	{
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			final int animationLength = _animationLength;
			final boolean indefinite = ( animationLength <= 0 );
			final int timePassed = (int)Math.min( System.currentTimeMillis() - _startTime, (long)Integer.MAX_VALUE );

			final boolean finished = !indefinite && ( timePassed >= animationLength );
			final double progress = indefinite ? 0.0 : finished ? 1.0 : ( (double)timePassed / (double)animationLength );

			updateAnimation( timePassed, progress );

			if ( finished )
			{
				stop();
			}
		}
	}
}
