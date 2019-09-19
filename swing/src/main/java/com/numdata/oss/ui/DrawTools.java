/*
 * Copyright (c) 2005-2017, Numdata BV, The Netherlands.
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

/**
 * This class contains various utility methods that help with rendering 2D
 * images.
 *
 * @author Peter S. Heijnen
 */
public class DrawTools
{
	/** Alignment/orientation option: center. */
	public static final int CENTER = 1;

	/** Alignment/orientation option: north. */
	public static final int NORTH = 2;

	/** Alignment/orientation option: north-east. */
	public static final int NORTHEAST = 3;

	/** Alignment/orientation option: east. */
	public static final int EAST = 4;

	/** Alignment/orientation option: south-east. */
	public static final int SOUTHEAST = 5;

	/** Alignment/orientation option: south. */
	public static final int SOUTH = 6;

	/** Alignment/orientation option: south-west. */
	public static final int SOUTHWEST = 7;

	/** Alignment/orientation option: west. */
	public static final int WEST = 8;

	/** Alignment/orientation option: north-west. */
	public static final int NORTHWEST = 9;

	/**
	 * Length of drawn arrows.
	 *
	 * @see #drawDimensionHorizontal
	 */
	public static final int ARROW_LENGTH = 6;

	/**
	 * Width of drawn arrows.
	 *
	 * @see #drawDimensionHorizontal
	 */
	public static final int ARROW_WIDTH = 6;

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private DrawTools()
	{
	}

	/**
	 * Draw horizontal dimension using an horizontal line with arrow heads on
	 * both sides and the specified text at the requested position.
	 *
	 * @param g              Graphics context.
	 * @param x1             X coordinate for left side of dimension line.
	 * @param x2             X coordinate for right side of dimension line.
	 * @param y1             Y coordinate for end point of 'limit' lines.
	 * @param y2             Y coordinate for dimension line.
	 * @param text           Dimension text ({@code null} => none).
	 * @param textAlignment  Position of text relative to reference point
	 *                       ({@link #NORTH}, {@link #NORTHEAST}, {@link #EAST},
	 *                       {@link #SOUTHEAST}, {@link #SOUTH}, {@link
	 *                       #SOUTHWEST}, {@link #WEST}, {@link #NORTHWEST}, or
	 *                       {@link #CENTER}).
	 * @param textBackground Background color for text ({@code null} => none).
	 *                       This paints a opaque box behind the text with a
	 *                       line border using the current foreground color.
	 *
	 * @return Bounds of the drawn dimension text box; {@code null} if no box is
	 * drawn surrounding the text or no text is specified.
	 */
	public static Rectangle drawDimensionHorizontal( final Graphics g, final int x1, final int x2, final int y1, final int y2, final String text, final int textAlignment, final Color textBackground )
	{
		if ( y1 != y2 )
		{
			final int extendedY2 = y2 + ( ( y2 > y1 ) ? ARROW_WIDTH / 2 : -ARROW_WIDTH / 2 );
			g.drawLine( x1, y1, x1, extendedY2 );
			g.drawLine( x2, y1, x2, extendedY2 );
		}

		return drawDimensionHorizontal( g, Math.min( x1, x2 ), Math.max( x1, x2 ), y2, text, textAlignment, textBackground );
	}

	/**
	 * Draw vertical dimension using an vertical line with arrow heads on both
	 * sides and the specified text at the requested position.
	 *
	 * @param g              Graphics context.
	 * @param x1             X coordinate for end point of 'limit' lines.
	 * @param x2             X coordinate for dimension line.
	 * @param y1             Y coordinate for top side of dimension line.
	 * @param y2             Y coordinate for bottom side of dimension line.
	 * @param text           Dimension text ({@code null} => none).
	 * @param textAlignment  Position of text relative to reference point
	 *                       ({@link #NORTH}, {@link #NORTHEAST}, {@link #EAST},
	 *                       {@link #SOUTHEAST}, {@link #SOUTH}, {@link
	 *                       #SOUTHWEST}, {@link #WEST}, {@link #NORTHWEST}, or
	 *                       {@link #CENTER}).
	 * @param textBackground Background color for text ({@code null} => none).
	 *                       This paints a opaque box behind the text with a
	 *                       line border using the current foreground color.
	 *
	 * @return Bounds of the drawn dimension text box; {@code null} if no box is
	 * drawn surrounding the text or no text is specified.
	 */
	public static Rectangle drawDimensionVertical( final Graphics g, final int x1, final int x2, final int y1, final int y2, final String text, final int textAlignment, final Color textBackground )
	{
		if ( x1 != x2 )
		{
			final int extendedX2 = x2 + ( ( x2 > x1 ) ? ARROW_WIDTH / 2 : -ARROW_WIDTH / 2 );
			g.drawLine( x1, y1, extendedX2, y1 );
			g.drawLine( x1, y2, extendedX2, y2 );
		}

		return drawDimensionVertical( g, x2, Math.min( y1, y2 ), Math.max( y1, y2 ), text, textAlignment, textBackground );
	}

	/**
	 * Draw horizontal dimension using an horizontal line with arrow heads on
	 * both sides and the specified text at the requested position.
	 *
	 * @param g              Graphics context.
	 * @param x1             X coordinate for left side of dimension line.
	 * @param x2             X coordinate for right side of dimension line.
	 * @param y              Y coordinate for dimension line.
	 * @param text           Dimension text ({@code null} => none).
	 * @param textAlignment  Position of text relative to reference point
	 *                       ({@link #NORTH}, {@link #NORTHEAST}, {@link #EAST},
	 *                       {@link #SOUTHEAST}, {@link #SOUTH}, {@link
	 *                       #SOUTHWEST}, {@link #WEST}, {@link #NORTHWEST}, or
	 *                       {@link #CENTER}).
	 * @param textBackground Background color for text ({@code null} => none).
	 *                       This paints a opaque box behind the text with a
	 *                       line border using the current foreground color.
	 *
	 * @return Bounds of the drawn dimension text box; {@code null} if no box is
	 * drawn surrounding the text or no text is specified.
	 */
	public static Rectangle drawDimensionHorizontal( final Graphics g, final int x1, final int x2, final int y, final String text, final int textAlignment, final Color textBackground )
	{
		final int x1Arrow;
		final int x2Arrow;

		if ( ( x2 - x1 ) > 20 )
		{
			x1Arrow = ( x1 + ARROW_LENGTH );
			x2Arrow = ( x2 - ARROW_LENGTH );
		}
		else
		{
			x1Arrow = ( x1 - ARROW_LENGTH );
			x2Arrow = ( x2 + ARROW_LENGTH );
		}

		g.drawLine( x1, y, x2, y );

		final int[] xs = { x1, x1Arrow, x1Arrow };
		final int[] ys = { y, y - ARROW_WIDTH / 2, y + ARROW_WIDTH / 2 };
		g.fillPolygon( xs, ys, 3 );

		xs[ 0 ] = x2;
		xs[ 1 ] = x2Arrow;
		xs[ 2 ] = x2Arrow;
		g.fillPolygon( xs, ys, 3 );

		Rectangle result = null;
		if ( ( text != null ) && ( text.length() > 0 ) )
		{
			final int textX;

			switch ( textAlignment )
			{
				case WEST:
				case NORTHWEST:
				case SOUTHWEST:
					textX = Math.min( x1, x1Arrow );
					break;

				default:
				case CENTER:
				case NORTH:
				case SOUTH:
					textX = ( x1 + x2 ) / 2;
					break;

				case EAST:
				case NORTHEAST:
				case SOUTHEAST:
					textX = Math.max( x2, x2Arrow );
					break;
			}

			final int textY;

			final FontMetrics metrics = g.getFontMetrics();
			if ( ( x2 - x1 ) < ( metrics.stringWidth( text ) + 2 ) )
			{
				switch ( textAlignment )
				{
					case NORTH:
					case NORTHWEST:
					case NORTHEAST:
						textY = y - ARROW_WIDTH / 2;
						break;

					default:
					case CENTER:
					case WEST:
					case EAST:
						textY = y;
						break;

					case SOUTH:
					case SOUTHWEST:
					case SOUTHEAST:
						textY = y + ARROW_WIDTH / 2;
						break;
				}
			}
			else
			{
				textY = y;
			}

			result = drawDimensionText( g, textX, textY, text, textAlignment, textBackground );
		}

		return result;
	}

	/**
	 * Draw vertical dimension using an vertical line with arrow heads on both
	 * sides and the specified text at the requested position.
	 *
	 * @param g              Graphics context.
	 * @param x              X coordinate for dimension line.
	 * @param y1             Y coordinate for top side of dimension line.
	 * @param y2             Y coordinate for bottom side of dimension line.
	 * @param text           Dimension text ({@code null} => none).
	 * @param textAlignment  Position of text relative to reference point
	 *                       ({@link #NORTH}, {@link #NORTHEAST}, {@link #EAST},
	 *                       {@link #SOUTHEAST}, {@link #SOUTH}, {@link
	 *                       #SOUTHWEST}, {@link #WEST}, {@link #NORTHWEST}, or
	 *                       {@link #CENTER}).
	 * @param textBackground Background color for text ({@code null} => none).
	 *                       This paints a opaque box behind the text with a
	 *                       line border using the current foreground color.
	 *
	 * @return Bounds of the drawn dimension text box; {@code null} if no box is
	 * drawn surrounding the text or no text is specified.
	 */
	public static Rectangle drawDimensionVertical( final Graphics g, final int x, final int y1, final int y2, final String text, final int textAlignment, final Color textBackground )
	{
		final int y1Arrow;
		final int y2Arrow;

		if ( ( y2 - y1 ) > 20 )
		{
			y1Arrow = ( y1 + ARROW_LENGTH );
			y2Arrow = ( y2 - ARROW_LENGTH );
		}
		else
		{
			y1Arrow = ( y1 - ARROW_LENGTH );
			y2Arrow = ( y2 + ARROW_LENGTH );
		}

		g.drawLine( x, y1, x, y2 );

		final int[] xs = { x, x - ARROW_WIDTH / 2, x + ARROW_WIDTH / 2 };
		final int[] ys = { y1, y1Arrow, y1Arrow };
		g.fillPolygon( xs, ys, 3 );

		ys[ 0 ] = y2;
		ys[ 1 ] = y2Arrow;
		ys[ 2 ] = y2Arrow;
		g.fillPolygon( xs, ys, 3 );

		Rectangle result = null;
		if ( ( text != null ) && ( text.length() > 0 ) )
		{
			final int textX;

			final FontMetrics metrics = g.getFontMetrics();
			if ( ( y2 - y1 ) < ( metrics.getAscent() - metrics.getDescent() + 4 ) )
			{
				switch ( textAlignment )
				{
					case WEST:
					case NORTHWEST:
					case SOUTHWEST:
						textX = x - ARROW_WIDTH / 2;
						break;

					default:
					case CENTER:
					case NORTH:
					case SOUTH:
						textX = x;
						break;

					case EAST:
					case NORTHEAST:
					case SOUTHEAST:
						textX = x + ARROW_WIDTH / 2;
						break;
				}
			}
			else
			{
				textX = x;
			}

			final int textY;
			switch ( textAlignment )
			{
				case NORTH:
				case NORTHWEST:
				case NORTHEAST:
					textY = Math.min( y1, y1Arrow );
					break;

				default:
				case CENTER:
				case WEST:
				case EAST:
					textY = ( y1 + y2 ) / 2;
					break;

				case SOUTH:
				case SOUTHWEST:
				case SOUTHEAST:
					textY = Math.max( y2, y2Arrow );
					break;
			}

			result = drawDimensionText( g, textX, textY, text, textAlignment, textBackground );
		}

		return result;
	}

	/**
	 * Draw text a the specified location. The alignment of the text can be
	 * specified relative to the supplied coordinates. If a {@code
	 * textBackground} is specified, the text is placed in a box that fits
	 * nicely around the text.
	 *
	 * @param g              Graphics context.
	 * @param x              X coordinate of reference point to draw text at.
	 * @param y              Y coordinate of reference point to draw text at.
	 * @param text           Dimension text ({@code null} => none).
	 * @param textAlignment  Position of text relative to reference point
	 *                       ({@link #NORTH}, {@link #NORTHEAST}, {@link #EAST},
	 *                       {@link #SOUTHEAST}, {@link #SOUTH}, {@link
	 *                       #SOUTHWEST}, {@link #WEST}, {@link #NORTHWEST}, or
	 *                       {@link #CENTER}).
	 * @param textBackground Background color for text ({@code null} => none).
	 *                       This paints a opaque box behind the text with a
	 *                       line border using the current foreground color.
	 *
	 * @return Bounds of the drawn dimension text box; {@code null} if no box is
	 * drawn surrounding the text.
	 */
	private static Rectangle drawDimensionText( final Graphics g, final int x, final int y, final String text, final int textAlignment, final Color textBackground )
	{
		final int inset = ( textBackground != null ) ? 3 : 0;
		final FontMetrics metrics = g.getFontMetrics();
		final int width = metrics.stringWidth( text ) + inset * 3;
		final int height = metrics.getAscent() - metrics.getDescent();

		final int textX;

		switch ( textAlignment )
		{
			case WEST:
			case NORTHWEST:
			case SOUTHWEST:
				textX = x - width - inset;
				break;

			default:
			case CENTER:
			case NORTH:
			case SOUTH:
				textX = x - width / 2;
				break;

			case EAST:
			case NORTHEAST:
			case SOUTHEAST:
				textX = x + inset + 1;
				break;
		}

		final int textY;

		switch ( textAlignment )
		{
			case NORTH:
			case NORTHEAST:
			case NORTHWEST:
				textY = y - height - 6 - inset;
				break;

			default:
			case CENTER:
			case EAST:
			case WEST:
				textY = y - height / 2 - inset;
				break;

			case SOUTH:
			case SOUTHEAST:
			case SOUTHWEST:
				textY = y + 3;
				break;
		}

		Rectangle result = null;
		if ( textBackground != null )
		{
			final Color color = g.getColor();
			g.setColor( textBackground );
			g.fillRect( textX + 1, textY + 1, width - 2, height + 4 );
			g.setColor( color );
			g.drawRect( textX, textY, width - 1, height + 5 );

			result = new Rectangle( textX, textY, width - 1, height + 5 );
		}

		g.drawString( text, textX + inset, textY + inset + height );

		return result;
	}

	/**
	 * Draw horizontal etched line.
	 *
	 * @param g     Graphics context.
	 * @param x     Horizontal position of line.
	 * @param y     Vertical position of line.
	 * @param width Width (length) of line.
	 */
	public static void drawEtchHorizontal( final Graphics g, final int x, final int y, final int width )
	{
		g.setColor( Color.gray );
		g.drawLine( x, y, x + width - 1, y );

		g.setColor( Color.white );
		g.drawLine( x, y + 1, x + width - 1, y + 1 );
	}

	/**
	 * Draw vertical etched line.
	 *
	 * @param g      Graphics context.
	 * @param x      Horizontal position of line.
	 * @param y      Vertical position of line.
	 * @param height Height (length) of line.
	 */
	public static void drawEtchVertical( final Graphics g, final int x, final int y, final int height )
	{
		g.setColor( Color.gray );
		g.drawLine( x, y, x, y + height - 1 );

		g.setColor( Color.white );
		g.drawLine( x + 1, y, x + 1, y + height - 1 );
	}
}
