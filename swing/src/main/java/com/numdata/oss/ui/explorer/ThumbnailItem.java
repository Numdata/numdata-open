/*
 * Copyright (c) 2006-2017, Numdata BV, The Netherlands.
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
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import com.numdata.oss.*;
import com.numdata.oss.ui.*;
import org.jetbrains.annotations.*;

/**
 * Represents a single file in a {@link ThumbnailView}.
 *
 * @author  Peter S. Heijnen
 */
@SuppressWarnings ( { "rawtypes", "SerializableHasSerializationMethods", "NonSerializableFieldInSerializableClass" } )
public final class ThumbnailItem
	extends JComponent
	implements ListCellRenderer
{
	/**
	 * Insets inside border.
	 */
	public static final Insets INSETS = new Insets( 2, 14, 40, 14 );

	/**
	 * Explorer for which this item is used.
	 */
	private final Explorer _explorer;

	/**
	 * File being represented by the component.
	 */
	private VirtualFile _file = null;

	/**
	 * Image used to represent folders.
	 */
	private BufferedImage _folderImage = null;

	/**
	 * Thumbnail image, if already loaded.
	 */
	private BufferedImage _thumbnailImage;

	/**
	 * Create thumbnail item.
	 *
	 * @param   explorer        Explorer that the item belongs to.
	 */
	public ThumbnailItem( final Explorer explorer )
	{
		_explorer = explorer;
		_thumbnailImage = null;
		setOpaque( true );
		enableEvents( AWTEvent.MOUSE_EVENT_MASK );

		final int thumbnailSize = explorer.getThumbnailSize();
		final int width = INSETS.left + thumbnailSize + INSETS.right;
		final int height = INSETS.top + thumbnailSize + INSETS.bottom;
		final Dimension componentSize = new Dimension( width, height );

		setMinimumSize( componentSize );
		setPreferredSize( componentSize );
		setMaximumSize( componentSize );
	}

	/**
	 * Paints a representation of a folder to the given buffered image.
	 *
	 * @param   image   Image to paint to.
	 */
	static void createFolderImage( final BufferedImage image )
	{
		if ( image == null )
		{
			throw new NullPointerException( "image" );
		}

		final float width  = (float)image.getWidth();
		final float height = (float)image.getHeight();
		final float fold   = 11.0f + width / 6.0f;  // placement of fold in outer edge (scale with image)

		final Path2D.Float folderRearShape = new Path2D.Float( Path2D.WIND_NON_ZERO, 10 );
		folderRearShape.moveTo(         3.0f,  7.0f );
		folderRearShape.lineTo(         8.0f,  2.0f );
		folderRearShape.lineTo(         fold,  2.0f );
		folderRearShape.lineTo( fold  + 3.0f,  5.0f );
		folderRearShape.lineTo( width - 6.0f,  5.0f );
		folderRearShape.lineTo( width - 4.0f,  7.0f );
		folderRearShape.lineTo( width - 4.0f, 10.0f );
		folderRearShape.lineTo( width - 7.0f,  7.0f );
		folderRearShape.lineTo(         6.0f,  7.0f );
		folderRearShape.lineTo(         3.0f, 10.0f );
		folderRearShape.closePath();

		final Path2D.Float folderFrontShape = new Path2D.Float( Path2D.WIND_NON_ZERO, 9 );
		folderFrontShape.moveTo(         3.0f,         10.0f );
		folderFrontShape.lineTo(         6.0f,          7.0f );
		folderFrontShape.lineTo( width - 7.0f,          7.0f );
		folderFrontShape.lineTo( width - 4.0f,         10.0f );
		folderFrontShape.lineTo( width - 4.0f, height - 5.0f );
		folderFrontShape.lineTo( width - 5.0f, height - 4.0f );
		folderFrontShape.lineTo(         4.0f, height - 4.0f );
		folderFrontShape.lineTo(         3.0f, height - 5.0f );
		folderFrontShape.closePath();

		final Graphics2D g = (Graphics2D)image.getGraphics();

		g.setPaint( new GradientPaint( 0.0f, 0.0f, Explorer.FOLDER_REAR_LEFT_COLOR, fold, 0.0f, Explorer.FOLDER_REAR_RIGHT_COLOR, false ) );
		g.fill( folderRearShape );

		g.setPaint( new GradientPaint( 0.0f, 0.0f, Explorer.FOLDER_FRONT_TOP_COLOR, 0.0f, height - 14.0f, Explorer.FOLDER_FRONT_BOTTOM_COLOR, false ) );
		g.fill( folderFrontShape );

		g.setPaint( Explorer.FOLDER_OUTLINE_COLOR );
		g.draw( folderRearShape );
		g.draw( folderFrontShape );

		g.dispose();
	}

	/**
	 * Sets the file to be represented by the component.
	 *
	 * @param   file    File to be set.
	 */
	public void setFile( final VirtualFile file )
	{
		Image thumbnail = null;

		if ( ( file != null ) && !file.isDirectory() )
		{
			final int thumbnailSize = _explorer.getThumbnailSize();
			final ImageSource thumbnailSource = file.getThumbnail( thumbnailSize, thumbnailSize );
			if ( thumbnailSource != null )
			{
				try
				{
					thumbnail = thumbnailSource.getImage();
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
			}
		}

		setFile( file, thumbnail );
	}

	/**
	 * Sets the file to be represented by the component.
	 *
	 * @param   file        File to be set.
	 * @param   thumbnail   Thumbnail for the file.
	 */
	private void setFile( @Nullable final VirtualFile file, @Nullable final Image thumbnail )
	{
		_file = file;

		final Locale locale = _explorer.getLocale();

		final String description = file == null ? null : file.getDescription( locale );
		setToolTipText( TextTools.isEmpty( description ) ? null : description );

		if ( ( file != null ) && file.isDirectory() )
		{
			setBorder( BorderFactory.createEmptyBorder( INSETS.top, INSETS.left, INSETS.bottom, INSETS.right ) );
		}
		else
		{
			setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( INSETS.top, INSETS.left, INSETS.bottom, INSETS.right ),
			                                               BorderFactory.createLineBorder( Explorer.ITEM_BORDER_COLOR ) ) );
		}

		_thumbnailImage = (BufferedImage)thumbnail;
	}

	/**
	 * Returns the file represented by the component.
	 *
	 * @return  File for this item.
	 */
	public VirtualFile getFile()
	{
		return _file;
	}

	@Override
	protected void paintComponent( final Graphics g )
	{
		final int thumbnailSize = _explorer.getThumbnailSize();

		BufferedImage folderImage = _folderImage;
		if ( folderImage == null )
		{
			final GraphicsConfiguration configuration = getGraphicsConfiguration();
			folderImage = configuration.createCompatibleImage( thumbnailSize, thumbnailSize, Transparency.BITMASK );
			if ( folderImage == null )
			{
				folderImage = new BufferedImage( thumbnailSize, thumbnailSize, BufferedImage.TYPE_INT_ARGB );
			}
			createFolderImage( folderImage );
			_folderImage = folderImage;
		}

		final Graphics2D g2d = (Graphics2D)g;

		final int width  = getWidth();
		final int height = getHeight();

		g2d.setColor( getBackground() );
		g2d.fillRect( 0, 0, width, height );

		final Insets insets = INSETS;
		g2d.translate( insets.left, insets.top );

		final BufferedImage thumbnailImage = _thumbnailImage;

		final VirtualFile file = _file;
		if ( file.isDirectory() )
		{
			g2d.drawImage( folderImage, null, null );
		}
		else if ( thumbnailImage != null )
		{
			final Insets imageInsets = Explorer.THUMBNAIL_IMAGE_INSETS;

			final int thumbnailX      = imageInsets.left;
			final int thumbnailY      = imageInsets.top;
			final int thumbnailWidth  = thumbnailSize - imageInsets.left - imageInsets.right;
			final int thumbnailHeight = thumbnailSize - imageInsets.top  - imageInsets.bottom;

			final JList list = (JList)SwingUtilities.getAncestorOfClass( JList.class, this );

			final BufferedImage scaledImage = ImageTools.createScaledInstance( thumbnailImage, thumbnailWidth, thumbnailHeight, ImageTools.ScaleMode.CONTAIN, null );
			g2d.drawImage( scaledImage, thumbnailX + ( thumbnailWidth - scaledImage.getWidth() ) / 2, thumbnailY + ( thumbnailHeight - scaledImage.getHeight() ) / 2, list );
		}

		g2d.translate( -insets.left, -insets.top );

		String name = file.getDisplayName( _explorer.getLocale() );
		if ( TextTools.isEmpty( name ) )
		{
			name = file.getName();
		}

		if ( ( name != null ) && !name.isEmpty() )
		{
			g2d.setPaint( Explorer.TEXT_COLOR );
			g2d.setFont( _explorer.getTextFont() );

			final FontMetrics fm = g2d.getFontMetrics();

			final int textHorInset = 4;
			final int maxWidth     = width - insets.left - insets.right - 2 * textHorInset;

			String text = name;
			int textWidth = fm.stringWidth( text );

			final String dots = "...";
			int dotWidth = 0;

			while ( textWidth > maxWidth )
			{
				if ( dotWidth == 0 )
				{
					dotWidth = fm.stringWidth( dots );
				}

				text = text.substring( 0, text.length() - 1 );
				textWidth = fm.stringWidth( text ) + dotWidth;
			}

			if ( dotWidth > 0 )
			{
				text += dots;
			}

			final int textVerInset = 1;

			final int x = insets.left + textHorInset + ( maxWidth - textWidth ) / 2;
			final int y = height - insets.bottom + textVerInset + fm.getAscent();

			g2d.drawString( text, x, y );
		}
	}

	@Override
	public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
	{
		final ListView.Item item = (ListView.Item)value;
		setFile( item.getFile(), item.getThumbnail() );
		setForeground( isSelected ? list.getSelectionForeground() : list.getForeground() );
		setBackground( isSelected ? list.getSelectionBackground() : list.getBackground() );
		return this;
	}

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
