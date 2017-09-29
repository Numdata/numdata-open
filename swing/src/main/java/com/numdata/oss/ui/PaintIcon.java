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

import java.awt.*;
import javax.swing.*;

/**
 * This icon paints the icon area with a single paint. The icon size and paint
 * can be set at will.
 *
 * @author  Peter S. Heijnen
 */
public class PaintIcon
	implements Icon
{
	/**
	 * Icon width.
	 */
	private int _iconWidth;

	/**
	 * Icon height.
	 */
	private int _iconHeight;

	/**
	 * Paint that is used for the icon.
	 */
	private Paint _paint;

	/**
	 * Create icon.
	 */
	public PaintIcon()
	{
		this( 0, 0, null );
	}

	/**
	 * Create icon.
	 *
	 * @param   width   Icon width.
	 * @param   height  Icon height.
	 * @param   paint   Paint to use for the icon.
	 */
	public PaintIcon( final int width, final int height, final Paint paint )
	{
		_paint = paint;
		_iconHeight = height;
		_iconWidth = width;
	}

	@Override
	public int getIconWidth()
	{
		return _iconWidth;
	}

	/**
	 * Set icon width.
	 *
	 * @param   width   Icon width.
	 */
	public void setIconWidth( final int width )
	{
		_iconWidth = width;
	}

	@Override
	public int getIconHeight()
	{
		return _iconHeight;
	}

	/**
	 * Set icon height.
	 *
	 * @param   height  Icon height.
	 */
	public void setIconHeight( final int height )
	{
		_iconHeight = height;
	}

	/**
	 * Get paint that is used for the icon.
	 *
	 * @return  Paint that is used for the icon.
	 */
	public Paint getPaint()
	{
		return _paint;
	}

	/**
	 * Set paint to use for the icon.
	 *
	 * @param   paint   Paint to use for the icon.
	 */
	public void setPaint( final Paint paint )
	{
		_paint = paint;
	}

	@Override
	public void paintIcon( final Component component, final Graphics g, final int x, final int y )
	{
		final Paint paint = getPaint();
		if ( paint != null )
		{
			final Graphics2D g2d = (Graphics2D)g;
			final Paint oldPaint = g2d.getPaint();
			g2d.setPaint( paint );
			g2d.fillRect( x, y, getIconWidth(), getIconHeight() );
			g2d.setPaint( oldPaint );
		}
	}
}
