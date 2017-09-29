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
import javax.swing.border.*;

import org.jetbrains.annotations.*;

/**
 * A horizontal of vertical banner image.
 *
 * @author G. Meinders
 */
public class Banner
	extends JComponent
{
	/**
	 * Image shown in the banner.
	 */
	@Nullable
	private Image _image;

	/**
	 * Orientation of the banner.
	 */
	private int _orientation;

	/**
	 * Horizontal image alignment, ranging from {@code 0.0} (left-aligned) to
	 * {@code 1.0} (right-aligned).
	 */
	private double _horizontalImageAlignment = 0.0;

	/**
	 * Constructs a new instance.
	 *
	 * @param   orientation     Orientation to be used;
	 *                          {@link SwingConstants#HORIZONTAL} or
	 *                          {@link SwingConstants#VERTICAL}.
	 */
	public Banner( final int orientation )
	{
		_image = null;
		_orientation = orientation;

		if ( orientation == SwingConstants.VERTICAL )
		{
			setBorder( BorderFactory.createMatteBorder( 0, 0, 0, 1, Color.GRAY ) );
		}
		else
		{
			setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.GRAY ) );
		}
	}

	/**
	 * Returns the image that is shown.
	 *
	 * @return  Banner image.
	 */
	@Nullable
	public Image getImage()
	{
		return _image;
	}

	/**
	 * Sets the image to be shown.
	 *
	 * @param   image   Banner image.
	 */
	public void setImage( @Nullable final Image image )
	{
		if ( _image != image )
		{
			_image = image;

			if ( image == null )
			{
				setMinimumSize( null );
				setPreferredSize( null );
				setMaximumSize( null );
			}
			else
			{
				final Insets insets = getInsets();

				final int width = image.getWidth( this ) + insets.left + insets.right;
				final int height = image.getHeight( this ) + insets.bottom + insets.top;
				final int orientation = _orientation;

				setMinimumSize( ( orientation == SwingConstants.HORIZONTAL ) ? new Dimension( 0, height ) : new Dimension( width, 0 ) );
				setPreferredSize( new Dimension( width, height ) );
				setMaximumSize( ( orientation == SwingConstants.HORIZONTAL ) ? new Dimension( Integer.MAX_VALUE, height ) : new Dimension( width, Integer.MAX_VALUE ) );
			}

			revalidate();
		}
	}

	/**
	 * Returns the horizontal alignment of the image.
	 *
	 * @return  Horizontal image alignment, ranging from {@code 0.0}
	 *          (left-aligned) to {@code 1.0} (right-aligned).
	 */
	public double getHorizontalImageAlignment()
	{
		return _horizontalImageAlignment;
	}

	/**
	 * Sets the horizontal alignment of the image.
	 *
	 * @param   horizontalImageAlignment    Horizontal image alignment, ranging
	 *                                      from {@code 0.0} (left-aligned) to
	 *                                      {@code 1.0} (right-aligned).
	 */
	public void setHorizontalImageAlignment( final double horizontalImageAlignment )
	{
		_horizontalImageAlignment = horizontalImageAlignment;
	}

	@Override
	public Border getBorder()
	{
		return ( _image == null ) ? null : super.getBorder();
	}

	@Override
	public Insets getInsets()
	{
		return getInsets( null );
	}

	@Override
	public Insets getInsets( final Insets insets )
	{
		final Insets result = super.getInsets( insets );
		if ( _image == null )
		{
			result.set( 0, 0, 0, 0 );
		}
		return result;
	}

	@Override
	public void paintComponent( final Graphics g )
	{
		final Insets insets = getInsets();

		final int componentWidth = getWidth() - insets.left - insets.right;
		final int componentHeight = getHeight() - insets.bottom - insets.top;

		final Image image = _image;
		if ( image == null )
		{
			if ( isOpaque() )
			{
				g.setColor( getBackground() );
				g.fillRect( insets.left, insets.top, componentWidth, componentHeight );
			}
		}
		else
		{
			final int imageWidth = image.getWidth( this );
			final int imageHeight = image.getHeight( this );
			final int minHeight = Math.min( imageHeight, componentHeight );
			final int imageLeft = (int)(_horizontalImageAlignment * ( componentWidth - imageWidth ));

			g.drawImage( image, insets.left + imageLeft, insets.top, insets.left + imageLeft + imageWidth, insets.top + componentHeight, 0, 0, imageWidth, minHeight, null );

			if ( imageLeft > 0 )
			{
				g.drawImage( image, insets.left, insets.top, insets.left + imageLeft, insets.top + componentHeight, 0, 0, 1, minHeight, null );
			}

			if ( imageLeft + imageWidth < componentWidth )
			{
				g.drawImage( image, insets.left + imageLeft + imageWidth, insets.top, insets.left + componentWidth, insets.top + componentHeight, imageWidth - 1, 0, imageWidth, minHeight, null );
			}
		}
	}
}
