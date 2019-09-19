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
package com.numdata.oss.ui;

import java.awt.*;
import javax.swing.border.*;

import org.jetbrains.annotations.*;

/**
 * This class provides a border with a solid color background and border.
 *
 * @author  Peter S. Heijnen
 */
public class ColoredTitledBorder
	extends AbstractBorder
{
	/**
	 * Default font.
	 */
	public static final Font FONT = new Font( "SansSerif", Font.BOLD, 11 );

	/**
	 * Default test color.
	 */
	private static final Color TEXT_COLOR = Color.BLACK;

	/**
	 * Default background color.
	 */
	public static final Color BACKGROUND_COLOR = new Color( 166, 206, 246 );

	/**
	 * Default background color.
	 */
	public static final Color DISABLED_BACKGROUND_COLOR = new Color( 176, 176, 176 );

	/**
	 * Default border color.
	 */
	public static final Color BORDER_COLOR = Color.GRAY;

	/**
	 * Title text.
	 */
	@Nullable
	private String _text;

	/**
	 * Font to use for title text.
	 */
	@Nullable
	private Font _font;

	/**
	 * Title text color.
	 */
	@Nullable
	private Color _textColor;

	/**
	 * Text insets. These insets are used to offset the text from the border
	 * lines.
	 */
	@NotNull
	private final Insets _textInsets = new Insets( 4, 6, 4, 6 );

	/**
	 * Title content background paint.
	 */
	@Nullable
	private Paint _background;

	/**
	 * Disabled title content background paint.
	 */
	@Nullable
	private Paint _disabledBackground;

	/**
	 * Border color.
	 */
	@Nullable
	private Color _borderColor;

	/**
	 * Create border without title text.
	 */
	public ColoredTitledBorder()
	{
		this( null );
	}

	/**
	 * Create border with given title text.
	 *
	 * @param   text   Title text.
	 */
	public ColoredTitledBorder( @Nullable final String text )
	{
		_text = text;
		_font = FONT;
		_textColor = TEXT_COLOR;
		_background = BACKGROUND_COLOR;
		_disabledBackground = DISABLED_BACKGROUND_COLOR;
		_borderColor = BORDER_COLOR;
	}

	/**
	 * Get text insets. These insets are used to offset the text from the
	 * border lines.
	 *
	 * @return  Text insets.
	 */
	@NotNull
	public Insets getTextInsets()
	{
		return (Insets) _textInsets.clone();
	}

	/**
	 * Set text insets. These insets are used to offset the text from the
	 * border lines.
	 *
	 * @param   insets  Text insets.
	 */
	public void setTextInsets( @NotNull final Insets insets )
	{
		_textInsets.set( insets.top, insets.left, insets.bottom, insets.right );
	}

	/**
	 * Get title content background paint.
	 *
	 * @return  Title content background paint.
	 */
	@Nullable
	public Paint getBackground()
	{
		return _background;
	}

	/**
	 * Set title content background paint.
	 *
	 * @param   paint   Title content background paint.
	 */
	public void setBackground( @Nullable final Paint paint )
	{
		_background = paint;
	}

	/**
	 * Get disabled title content background paint.
	 *
	 * @return  Disabled title content background paint.
	 */
	@Nullable
	public Paint getDisabledBackground()
	{
		return _disabledBackground;
	}

	/**
	 * Set disabled title content background paint.
	 *
	 * @param   paint   Disabled title content background paint.
	 */
	public void setDisabledBackground( @Nullable final Paint paint )
	{
		_disabledBackground = paint;
	}

	/**
	 * Get border color.
	 *
	 * @return  Border color.
	 */
	@Nullable
	public Color getBorderColor()
	{
		return _borderColor;
	}

	/**
	 * Set border color.
	 *
	 * @param   color   Border color.
	 */
	public void setBorderColor( @Nullable final Color color )
	{
		_borderColor = color;
	}

	/**
	 * Get text color.
	 *
	 * @return  Text color.
	 */
	@Nullable
	public Color getTextColor()
	{
		return _textColor;
	}

	/**
	 * Set text color.
	 *
	 * @param   color   Text color.
	 */
	public void setTextColor( @Nullable final Color color )
	{
		_textColor = color;
	}

	/**
	 * Get font.
	 *
	 * @return  Used font;
	 *          {@code null} if inherited from component or using default.
	 */
	@Nullable
	public Font getFont()
	{
		return _font;
	}

	/**
	 * Set font.
	 *
	 * @param   font    Font to use; {@code null} to inherit from
	 *                  component or use default.
	 */
	public void setFont( @NotNull final Font font )
	{
		_font = font;
	}

	/**
	 * Get title text.
	 *
	 * @return  Title text.
	 */
	@Nullable
	public String getText()
	{
		return _text;
	}

	/**
	 * Set title text.
	 *
	 * @param   text    Title text.
	 */
	public void setText( @Nullable final String text )
	{
		_text = text;
	}

	@Override
	public void paintBorder( final Component component, final Graphics g, final int x, final int y, final int width, final int height )
	{
		final Graphics2D g2d = (Graphics2D) g;
		final Paint oldPaint = g2d.getPaint();
		g2d.translate( x, y );

		Font font = getFont();
		if ( font == null )
		{
			font = component.getFont();
			if ( font == null )
			{
				font = FONT;
			}
		}

		final FontMetrics fontMetrics = component.getFontMetrics( font );
		final Insets textInsets = _textInsets;
		final int textHeight = textInsets.top + fontMetrics.getHeight() + textInsets.bottom;

		final Color borderColor = getBorderColor();
		if ( borderColor != null )
		{
			g2d.setColor( borderColor );
			g2d.drawRect( 0,0, width - 1, height - 1 );
			g2d.drawLine( 1, textHeight + 1, width - 2, textHeight + 1 );
		}

		final Paint background = component.isEnabled() ? getBackground() : getDisabledBackground();
		if ( background != null )
		{
			g2d.setPaint( background );
			g2d.fillRect( 1, 1, width - 2, textHeight );
		}

		final String title = getText();
		if ( ( title != null ) && !title.isEmpty() )
		{
			g2d.setColor( getTextColor() );
			final Font oldFont = g2d.getFont();
			g2d.setFont( font );
			g2d.drawString( title, 1 + textInsets.left, 1 + textInsets.top + fontMetrics.getLeading() + fontMetrics.getAscent() );
			g2d.setFont( oldFont );
		}

		g2d.translate( -x, -y );
		g2d.setPaint( oldPaint );
	}

	@Override
	public Insets getBorderInsets( final Component component )
	{
		Font font = _font;
		if ( font == null )
		{
			font = component.getFont();
			if ( font == null )
			{
				font = FONT;
			}
		}

		final FontMetrics fontMetrics = component.getFontMetrics( font );
		final Insets textInsets = _textInsets;

		return new Insets( 1 + textInsets.top + fontMetrics.getHeight() + textInsets.bottom + 1, 1, 1, 1 );
	}

	@Override
	public Insets getBorderInsets( final Component component, final Insets insets )
	{
		Font font = _font;
		if ( font == null )
		{
			font = component.getFont();
			if ( font == null )
			{
				font = FONT;
			}
		}

		final FontMetrics fontMetrics = component.getFontMetrics( font );
		final Insets textInsets = _textInsets;

		insets.left = 1;
		insets.top = 1 + textInsets.top + fontMetrics.getHeight() + textInsets.bottom + 1;
		insets.right = 1;
		insets.bottom = 1;
		return insets;
	}

	@Override
	public boolean isBorderOpaque()
	{
		return ( ( getBorderColor() != null ) && ( getBackground() != null ) );
	}
}
