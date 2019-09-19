/*
 * Copyright (c) 2016-2017, Numdata BV, The Netherlands.
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

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import org.jetbrains.annotations.*;

/**
 * A vertical banner image used in dialogs.
 *
 * @author Gerrit Meinders
 */
public class VerticalBanner
extends ImagePanel
{
	/**
	 * Minimum height of the banner.
	 */
	private final int _minimumHeight;

	/**
	 * Constructs a new instance.
	 */
	public VerticalBanner()
	{
		this( null, 0 );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param image         Image to be shown.
	 */
	public VerticalBanner( @Nullable final BufferedImage image )
	{
		this( image, 0 );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param image         Image to be shown.
	 * @param minimumHeight Minimum height of the banner; {@code 0} for no
	 *                      minimum.
	 */
	public VerticalBanner( @Nullable final BufferedImage image, final int minimumHeight )
	{
		super( image );
		_minimumHeight = minimumHeight;
		setVerticalAlignment( 0 );
		setBorder( BorderFactory.createMatteBorder( 0, 0, 0, 1, Color.GRAY ) );
		setScaleMode( ImageTools.ScaleMode.COVER );
	}

	@Override
	public Dimension getPreferredSize()
	{
		final Dimension size = super.getPreferredSize();
		if ( _minimumHeight > 0 )
		{
			size.height = _minimumHeight;
		}
		return size;
	}

	@Override
	public Dimension getMinimumSize()
	{
		final Dimension size = super.getPreferredSize();
		if ( _minimumHeight > 0 )
		{
			size.height = _minimumHeight;
		}
		return size;
	}
}
