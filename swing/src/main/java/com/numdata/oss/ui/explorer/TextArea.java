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
package com.numdata.oss.ui.explorer;

import java.awt.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import com.numdata.oss.*;

/**
 * A rectangular area in which text is laid out.
 *
 * @author G. Meinders
 */
public class TextArea
{
	/**
	 * The ellipsis indicates truncated text.
	 */
	private static final String ELLIPSIS = "...";

	/**
	 * Width of text area.
	 */
	private int _width;

	/**
	 * Height of text area.
	 */
	private int _height;

	/**
	 * Horizontal alignment of the text in the area ({@link
	 * SwingConstants#LEFT}, {@link SwingConstants#CENTER}, or {@link
	 * SwingConstants#RIGHT}).
	 */
	private int _horizontalAlignment = SwingConstants.LEFT;

	/**
	 * Vertical alignment of the text in the area  ({@link SwingConstants#TOP},
	 * {@link SwingConstants#CENTER}, or {@link SwingConstants#BOTTOM}).
	 */
	private int _verticalAlignment = SwingConstants.TOP;

	/**
	 * Lines of text in the text area.
	 */
	private final List<Line> _lines = new ArrayList<Line>();

	/**
	 * Height of the text currently in the area.
	 */
	private int _textHeight = 0;

	/**
	 * Closed.
	 */
	private boolean _closed = false;

	/**
	 * Constructs a new text area.
	 *
	 * @param width  Width of text area.
	 * @param height Height of text area.
	 */
	public TextArea( final int width, final int height )
	{
		_width = width;
		_height = height;
	}

	/**
	 * Constructs a new text area.
	 *
	 * @param width               Width of text area.
	 * @param height              Height of text area.
	 * @param horizontalAlignment Horizontal alignment.
	 * @param verticalAlignment   Vertical alignment.
	 */
	public TextArea( final int width, final int height, final int horizontalAlignment, final int verticalAlignment )
	{
		this( width, height );
		setHorizontalAlignment( horizontalAlignment );
		setVerticalAlignment( verticalAlignment );
	}

	/**
	 * Appends the given text paragraph to the contents of the text area.  The
	 * text if word-wrapped if the text is wider than the text area; if more
	 * text is appended than can be fit inside the area, the text is clipped,
	 * indicated by an ellipsis ('...') at the end of the text.
	 *
	 * @param g      Graphics context.
	 * @param text   Text to be appended.
	 * @param locale Locale of the text; used to find line breaks.
	 * @param font   Font to render the text with.
	 * @param color  Color of the text.
	 *
	 * @return {@code true} if the paragraph was (partially) appended; {@code
	 * false} if the paragraph not be appended.
	 */
	public boolean appendParagraph( final Graphics g, final String text, final Locale locale, final Font font, final Color color )
	{
		return appendParagraph( text, locale, g.getFontMetrics( font ), color );
	}

	/**
	 * Appends the given text paragraph to the contents of the text area.  The
	 * text if word-wrapped if the text is wider than the text area; if more
	 * text is appended than can be fit inside the area, the text is clipped,
	 * indicated by an ellipsis ('...') at the end of the text.
	 *
	 * @param text        Text to be appended.
	 * @param locale      Locale of the text; used to find line breaks.
	 * @param fontMetrics Font including rendering information.
	 * @param color       Color of the text.
	 *
	 * @return {@code true} if the paragraph was (partially) appended; {@code
	 * false} if the paragraph not be appended.
	 */
	public boolean appendParagraph( final String text, final Locale locale, final FontMetrics fontMetrics, final Color color )
	{
		final boolean result = !_closed && ( getNumberOfNewLinesThatFit( fontMetrics ) > 0 );

		if ( !result )
		{
			close();
		}
		else
		{
			final BreakIterator breakIterator = BreakIterator.getLineInstance( locale );
			breakIterator.setText( text );

			int lineStart = 0;

			while ( ( lineStart >= 0 ) && ( lineStart < text.length() ) )
			{
				final boolean lastLine = ( getNumberOfNewLinesThatFit( fontMetrics ) < 2 );
				lineStart = appendBreakableLine( text, lineStart, breakIterator, lastLine, fontMetrics, color );
				if ( lastLine )
				{
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Appends the given text paragraph to the contents of the text area.  The
	 * text if word-wrapped if the text is wider than the text area; if more
	 * text is appended than can be fit inside the area, the text is clipped,
	 * indicated by an ellipsis ('...') at the end of the text.
	 *
	 * @param g      Graphics context.
	 * @param text   Text to be appended.
	 * @param locale Locale of the text; used to find line breaks.
	 * @param font   Font to render the text with.
	 * @param color  Color of the text.
	 *
	 * @return {@code true} if a line was appended; {@code false} if the line
	 * could not be appended.
	 */
	public boolean appendLine( final Graphics g, final String text, final Locale locale, final Font font, final Color color )
	{
		return appendLine( text, locale, g.getFontMetrics( font ), color );
	}

	/**
	 * Appends a line of text to the contents of the text area. If the text does
	 * not fit on the line, it will be clipped. Clipping is indicated by an
	 * ellipsis ('...') at the end of the line.
	 *
	 * @param text        Text to be appended.
	 * @param locale      Locale of the text; used to find line breaks.
	 * @param fontMetrics Font including rendering information.
	 * @param color       Color of the text.
	 *
	 * @return {@code true} if a line was appended; {@code false} if the line
	 * could not be appended.
	 */
	public boolean appendLine( final String text, final Locale locale, final FontMetrics fontMetrics, final Color color )
	{
		final boolean result = !_closed && ( getNumberOfNewLinesThatFit( fontMetrics ) > 0 );

		if ( !result )
		{
			close();
		}
		else
		{
			final BreakIterator breakIterator = BreakIterator.getLineInstance( locale );
			breakIterator.setText( text );

			appendBreakableLine( text, 0, breakIterator, true, fontMetrics, color );
		}

		return result;
	}

	/**
	 * Appends the specified amount of vertical whitespace.
	 *
	 * @param height Height of the gap.
	 *
	 * @return {@code true} if the vertical gap was appended; {@code false} if
	 * the vertical gap could not be appended.
	 */
	public boolean appendVerticalGap( final int height )
	{
		final int newTextHeight = _textHeight + height;
		final boolean result = !_closed && ( newTextHeight <= _height );

		if ( !result )
		{
			close();
		}
		else
		{
			_textHeight = newTextHeight;
		}

		return result;
	}

	/**
	 * Appends a line of text to the contents of the text area. If the text does
	 * not fit on the line, it will be word-wrapped or clipped. Clipping is
	 * indicated by an ellipsis ('...') at the end of the line.
	 *
	 * @param text          Text to be appended.
	 * @param lineStart     Offset in text with line to append.
	 * @param breakIterator Finds boundaries for wrapping.
	 * @param clip          Clip line if it does not fit.
	 * @param fontMetrics   Font including rendering information.
	 * @param color         Color of the text.
	 *
	 * @return Offset in text after last character on the line; length of text
	 * if end of text was reached; -1 if the text was clipped (may occur even if
	 * {@code clip} is not set, but text would appear outside the area bounds).
	 */
	private int appendBreakableLine( final String text, final int lineStart, final BreakIterator breakIterator, final boolean clip, final FontMetrics fontMetrics, final Color color )
	{
		final int result;

		final int areaWidth = _width;
		if ( ( areaWidth > 0 ) && ( getNumberOfNewLinesThatFit( fontMetrics ) > 0 ) )
		{
			final int minimumCharactersPerLine = areaWidth / fontMetrics.getMaxAdvance();

			int lineEnd = breakIterator.preceding( Math.min( lineStart + minimumCharactersPerLine, text.length() ) );
			if ( ( lineEnd == BreakIterator.DONE ) || ( lineEnd < lineStart ) )
			{
				lineEnd = text.length();
			}

			boolean lineClipped = clip && ( lineEnd < text.length() );
			String line = text.substring( lineStart, lineEnd );
			if ( lineClipped )
			{
				line += ELLIPSIS;
			}

			while ( lineEnd < text.length() )
			{
				int longerLineEnd = breakIterator.following( lineEnd + 1 );
				if ( longerLineEnd == BreakIterator.DONE )
				{
					longerLineEnd = text.length();
				}

				final boolean longerLineClipped = clip && ( longerLineEnd < text.length() );
				String longerLine = text.substring( lineStart, longerLineEnd );
				if ( longerLineClipped )
				{
					longerLine += ELLIPSIS;
				}

				final boolean longerLineDoesNotFit = ( fontMetrics.stringWidth( longerLine ) > areaWidth );
				if ( longerLineDoesNotFit )
				{
					break;
				}

				lineEnd = longerLineEnd;
				lineClipped = longerLineClipped;
				line = longerLine;
			}

			addLine( line, fontMetrics, color );
			result = lineClipped ? -1 : lineEnd;
		}
		else
		{
			close();
			result = -1;
		}

		return result;
	}

	/**
	 * Internal method to add a line of text to this area. This is not intended
	 * for public use, because it performs no checks whatsoever whether the line
	 * will fit.
	 *
	 * @param text        Text on the line.
	 * @param fontMetrics Font including rendering information.
	 * @param color       Color of the text.
	 */
	protected void addLine( final String text, final FontMetrics fontMetrics, final Color color )
	{
		if ( _closed )
		{
			throw new IllegalStateException( "closed!" );
		}

		_lines.add( new Line( text, fontMetrics.getFont(), color, _textHeight + fontMetrics.getAscent() ) );
		_textHeight += fontMetrics.getHeight();
	}

	/**
	 * Get number of new lines that will fit with the given font in this area.
	 *
	 * @param metrics Metrics of font to test.
	 *
	 * @return Number of lines.
	 */
	private int getNumberOfNewLinesThatFit( final FontMetrics metrics )
	{
		final int i = _height - _textHeight - metrics.getAscent() - metrics.getDescent();
		return ( i < 0 ) ? 0 : 1 + i / metrics.getHeight();
	}

	/**
	 * Clear contents of the text area.
	 */
	public void clear()
	{
		_lines.clear();
		_textHeight = 0;
		_closed = false;
	}

	/**
	 * Close the area. Once this is called, no more content can be added.
	 */
	public void close()
	{
		_closed = true;
	}

	/**
	 * Changes the size of the text area, while clearing its contents.
	 *
	 * @param width  Width of text area.
	 * @param height Height of text area.
	 */
	public void clearAndResize( final int width, final int height )
	{
		clear();
		_width = width;
		_height = height;
	}

	/**
	 * Paint the text area.
	 *
	 * @param g Graphics context.
	 * @param x X coordinate of upper-left corner of text area to paint.
	 * @param y Y coordinate of upper-left corner of text area to paint.
	 */
	public void paint( final Graphics g, final int x, final int y )
	{
		final int textHeight = _textHeight;
		final int areaWidth = _width;
		final int areaHeight = _height;

		final int topY;

		switch ( getVerticalAlignment() )
		{
			default:
			case SwingConstants.TOP:
				topY = y;
				break;

			case SwingConstants.CENTER:
				topY = y + ( areaHeight - textHeight ) / 2;
				break;

			case SwingConstants.BOTTOM:
				topY = y + ( areaHeight - textHeight );
				break;
		}

		for ( final Line line : getLines() )
		{
			final String text = line.getText();
			if ( ( text != null ) && !TextTools.isEmpty( text ) )
			{
				g.setFont( line.getFont() );

				final int alignedX;

				switch ( getHorizontalAlignment() )
				{
					default:
					case SwingConstants.LEFT:
						alignedX = x;
						break;

					case SwingConstants.CENTER:
					{
						final FontMetrics fontMetrics = g.getFontMetrics();
						alignedX = x + ( areaWidth - fontMetrics.stringWidth( text ) ) / 2;
						break;
					}

					case SwingConstants.RIGHT:
					{
						final FontMetrics fontMetrics = g.getFontMetrics();
						alignedX = x + ( areaWidth - fontMetrics.stringWidth( text ) );
						break;
					}
				}

				g.setColor( line.getColor() );
				g.drawString( text, alignedX, topY + line.getBaseline() );
			}
		}
	}

	/**
	 * Get horizontal alignment of the text in the area.
	 *
	 * @return Horizontal alignment of the text in the area ({@link
	 * SwingConstants#LEFT}, {@link SwingConstants#CENTER}, or {@link
	 * SwingConstants#RIGHT}).
	 */
	public int getHorizontalAlignment()
	{
		return _horizontalAlignment;
	}

	/**
	 * Get horizontal alignment of the text in the area.
	 *
	 * @param alignment Horizontal alignment of the text in the area ({@link
	 *                  SwingConstants#LEFT}, {@link SwingConstants#CENTER}, or
	 *                  {@link SwingConstants#RIGHT}).
	 */
	public void setHorizontalAlignment( final int alignment )
	{
		_horizontalAlignment = alignment;
	}

	/**
	 * Get vertical alignment of the text in the area.
	 *
	 * @return Vertical alignment of the text in the area ({@link
	 * SwingConstants#TOP}, {@link SwingConstants#CENTER}, or {@link
	 * SwingConstants#BOTTOM}).
	 */
	public int getVerticalAlignment()
	{
		return _verticalAlignment;
	}

	/**
	 * Set vertical alignment of the text in the area.
	 *
	 * @param alignment Vertical alignment of the text in the area ({@link
	 *                  SwingConstants#TOP}, {@link SwingConstants#CENTER}, or
	 *                  {@link SwingConstants#BOTTOM}).
	 */
	public void setVerticalAlignment( final int alignment )
	{
		_verticalAlignment = alignment;
	}

	/**
	 * Get lines of text in this area.
	 *
	 * @return List of text lines.
	 */
	public List<Line> getLines()
	{
		return Collections.unmodifiableList( _lines );
	}

	/**
	 * Represents a single line of text in the same font and color.
	 */
	public static class Line
	{
		/**
		 * Text on the line.
		 */
		private String _text;

		/**
		 * Font of the text.
		 */
		private Font _font;

		/**
		 * Color of the text.
		 */
		private Color _color;

		/**
		 * Baseline offset of the line, relative to the start of the text.
		 */
		private final int _baseline;

		/**
		 * Constructs a new line of text.
		 *
		 * @param text     Text on the line.
		 * @param font     Font of the text.
		 * @param color    Color of the text.
		 * @param baseline Baseline offset of the line, relative to the
		 */
		public Line( final String text, final Font font, final Color color, final int baseline )
		{
			_text = text;
			_font = font;
			_color = color;
			_baseline = baseline;
		}

		/**
		 * Get text on the line.
		 *
		 * @return Text on the line.
		 */
		public String getText()
		{
			return _text;
		}

		/**
		 * Set text on the line.
		 *
		 * @param text Text on the line.
		 */
		public void setText( final String text )
		{
			_text = text;
		}

		/**
		 * Get font of the text.
		 *
		 * @return Font of the text.
		 */
		public Font getFont()
		{
			return _font;
		}

		/**
		 * Set font of the text.
		 *
		 * @param font Font of the text.
		 */
		public void setFont( final Font font )
		{
			_font = font;
		}

		/**
		 * Get color of the text.
		 *
		 * @return Color of the text.
		 */
		public Color getColor()
		{
			return _color;
		}

		/**
		 * Set color of the text.
		 *
		 * @param color Color of the text.
		 */
		public void setColor( final Color color )
		{
			_color = color;
		}

		/**
		 * Get baseline offset of the line, relative to the start of the text.
		 *
		 * @return Baseline offset of the line.
		 */
		public int getBaseline()
		{
			return _baseline;
		}
	}
}
