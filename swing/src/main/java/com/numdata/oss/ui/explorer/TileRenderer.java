/*
 * Copyright (c) 2012-2017, Numdata BV, The Netherlands.
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
import java.awt.Dimension;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

import com.numdata.oss.*;

/**
 * Represents an item as a tile with an image, name/label and description.
 *
 * @author G. Meinders
 */
@SuppressWarnings ( "rawtypes" )
public abstract class TileRenderer
	extends JComponent
	implements ListCellRenderer
{
	/**
	 * Insets around the thumbnail and text content of the tile.
	 */
	private static final Insets CONTENT_INSETS = new Insets( 5, 5, 5, 5 );

	/**
	 * Radius of the rounded corners of the thumbnail image.
	 */
	private static final double ROUND_CORNER_RADIUS = 3.0;

	/**
	 * Width of the text part of the tile.
	 */
	private static final int TEXT_WIDTH = 200;

	/**
	 * Name of the item.
	 */
	protected String _name = null;

	/**
	 * Description of the item.
	 */
	protected String _description = null;

	/**
	 * Size of the area reserved for a thumbnail image.
	 */
	protected final int _thumbnailSize;

	/**
	 * Whether a border should be painted around the thumbnail image.
	 */
	protected boolean _thumbnailBorderPainted = true;

	/**
	 * Whether a separator should be painted between tiles. (The separator is
	 * actually painted at the very bottom of each tile.)
	 */
	protected boolean _separatorPainted = true;

	/**
	 * Thumbnail image for the file, if available.
	 */
	protected Icon _thumbnailImage = null;

	/**
	 * Whether the tile is selected. Note that the appropriate foreground and
	 * background color need to be set separately.
	 */
	protected boolean _selected = false;

	/**
	 * Constructs a new instance.
	 *
	 * @param   thumbnailSize   Size of the area reserved for a thumbnail image.
	 */
	protected TileRenderer( final int thumbnailSize )
	{
		setOpaque( true );
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		final Insets borderInsets    = getInsets();
		final Insets thumbnailInsets = CONTENT_INSETS;

		_thumbnailSize = thumbnailSize;
		final int width  = borderInsets.left + borderInsets.right  + thumbnailSize + thumbnailInsets.left + thumbnailInsets.right + TEXT_WIDTH;
		final int height = borderInsets.top  + borderInsets.bottom + thumbnailSize + thumbnailInsets.top  + thumbnailInsets.bottom;

		final Dimension componentSize = new Dimension( width, height );
		setMinimumSize( componentSize );
		setPreferredSize( componentSize );
		setMaximumSize( componentSize );
	}

	@Override
	protected void paintComponent( final Graphics g )
	{
		final Graphics2D g2 = (Graphics2D)g;

		final Insets insets = getInsets();
		g2.translate( insets.left, insets.top );

		/*
		 * Paint the background of the component.
		 */
		final int width  = getWidth()  - insets.left - insets.right;
		final int height = getHeight() - insets.top  - insets.bottom;

		final Color background = getBackground();
		g2.setColor( background );
		g2.fillRect( 0, 0, width, height );

		/*
		 * Paint thumbnail image, with rounded corners and a border.
		 */
		final int thumbnailSize = _thumbnailSize;
		final Icon thumbnailImage  = _thumbnailImage;
		final Insets thumbnailInsets = CONTENT_INSETS;

		final Shape thumbnailBorder = new RoundRectangle2D.Double( (double)thumbnailInsets.left - 1.0, (double)thumbnailInsets.top - 1.0, (double)thumbnailSize + 1.0, (double)thumbnailSize + 1.0, ROUND_CORNER_RADIUS * 2.0, ROUND_CORNER_RADIUS * 2.0 );

		final Shape defaultClip = g2.getClip();
		g2.clip( thumbnailBorder );

		if ( thumbnailImage != null )
		{
			final JList list = (JList)SwingUtilities.getAncestorOfClass( JList.class, this );
			g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
			thumbnailImage.paintIcon( list, g2, thumbnailInsets.left, thumbnailInsets.top );
		}
		g2.setClip( defaultClip );

		if ( _thumbnailBorderPainted )
		{
			g2.setColor( _selected ? background.darker() : Explorer.ITEM_BORDER_COLOR );
			g2.draw( thumbnailBorder );
		}

		if ( _separatorPainted )
		{
			final Shape separator;
			final double separatorY = height + insets.bottom - 1.0;
			separator = new Line2D.Double( 0.0, separatorY, width, separatorY );
			g2.setColor( Explorer.ITEM_BORDER_COLOR );
			g2.draw( separator );
		}

		/*
		 * Paint the name and description of the item.
		 */
		final int textX      = thumbnailSize + CONTENT_INSETS.left * 2 + CONTENT_INSETS.right;
		final int textY      = CONTENT_INSETS.top;
		final int textWidth  = width  - thumbnailSize - CONTENT_INSETS.left * 2 - CONTENT_INSETS.right;
		final int textHeight = height - CONTENT_INSETS.top - CONTENT_INSETS.bottom;

		final TextArea textArea = new TextArea( textWidth, textHeight, SwingConstants.LEFT, SwingConstants.CENTER );

		final Locale locale = getLocale();

		final String name = _name;

		g2.setFont( getFont() );

		final Toolkit toolkit = getToolkit();

		final Object desktophints = toolkit.getDesktopProperty( "awt.font.desktophints" );
		if ( desktophints != null )
		{
			//noinspection rawtypes
			g2.addRenderingHints( (Map)desktophints );
		}
		else // desktop property is not available (seen on Linux/X11/xvfb)
		{
			g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		}

		final FontMetrics font = g2.getFontMetrics();

		final Color foreground = getForeground();
		if ( TextTools.isNonEmpty( name ) )
		{
			textArea.appendParagraph( name, locale, font, foreground );
		}

		final String description = _description;
		if ( ( description != null ) && TextTools.isNonEmpty( description ) )
		{
			textArea.appendVerticalGap( 5 );
			for ( final String line : description.split( "\r|\n|(\r\n)" ) )
			{
				final int detailsColorRed = ( foreground.getRed() + background.getRed() ) / 2;
				final int detailsColorGreen = ( foreground.getGreen() + background.getGreen() ) / 2;
				final int detailsColorBlue = ( foreground.getBlue() + background.getBlue() ) / 2;
				final Color detailsColor = new Color( detailsColorRed, detailsColorGreen, detailsColorBlue );
				textArea.appendParagraph( line, locale, font, detailsColor );
			}
		}

		textArea.paint( g2, textX, textY );

		g2.translate( -insets.left, -insets.top );
	}

	public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
	{
		setForeground( isSelected ? list.getSelectionForeground() : list.getForeground() );
		setBackground( isSelected ? list.getSelectionBackground() : list.getBackground() );
		_selected = isSelected;
		setValueInCellRenderer( list, value, index );
		return this;
	}

	/**
	 * Configures the cell renderer ({@code this}) to display the specified
	 * value.
	 *
	 * @param   list    List being rendered.
	 * @param   value   Value to be displayed.
	 * @param   index   Index in the list.
	 */
	protected abstract void setValueInCellRenderer( final JList list, final Object value, final int index );

	/**
	 * Overridden for performance reasons.
	 */
	@Override
	public void validate()
	{
	}

	/**
	 * Overridden for performance reasons.
	 */
	@Override
	public void invalidate()
	{
	}

	/**
	 * Overridden for performance reasons.
	 */
	@Override
	public void revalidate()
	{
	}
}
