/*
 * Copyright (c) 2009-2017, Numdata BV, The Netherlands.
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
import java.awt.image.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

import com.numdata.oss.ui.*;

/**
 * Represents a single file in a {@link ThumbnailView}.
 *
 * @author  G. Meinders
 */
public class Tile
	extends TileRenderer
{
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
	private Icon _folderIcon = null;

	/**
	 * Create thumbnail item.
	 *
	 * @param   explorer    Explorer that the tile is used for.
	 */
	public Tile( final Explorer explorer )
	{
		super( explorer.getThumbnailSize() );
		setLocale( explorer.getLocale() );
		setFont( explorer.getTextFont() );
		_explorer = explorer;
	}

	/**
	 * Sets the file to be represented by the component.
	 *
	 * @param   file    File to be set.
	 */
	public void setFile( final VirtualFile file )
	{
		_file = file;

		if ( !file.isDirectory() )
		{
			final int thumbnailSize = _explorer.getThumbnailSize();
			final ImageSource thumbnailSource = file.getThumbnail( thumbnailSize, thumbnailSize );
			Image thumbnailImage = null;
			if ( thumbnailSource != null )
			{
				try
				{
					thumbnailImage = thumbnailSource.getImage();
				}
				catch ( IOException e )
				{
					e.printStackTrace();
				}
			}
			_thumbnailImage = new ImageIcon( thumbnailImage );
		}
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

	/**
	 * Returns the icon used for folders.
	 *
	 * @return  Folder icon.
	 */
	private Icon getFolderIcon()
	{
		Icon folderIcon = _folderIcon;

		final int thumbnailSize = _thumbnailSize;
		if ( folderIcon == null )
		{
			final GraphicsConfiguration configuration = getGraphicsConfiguration();

			BufferedImage folderImage = ( configuration == null ) ? null : configuration.createCompatibleImage( thumbnailSize, thumbnailSize, Transparency.BITMASK );
			if ( folderImage == null )
			{
				folderImage = new BufferedImage( thumbnailSize, thumbnailSize, BufferedImage.TYPE_INT_ARGB );
			}

			ThumbnailItem.createFolderImage( folderImage );
			folderIcon = new ImageIcon( folderImage );

			_folderIcon = folderIcon;
		}

		return folderIcon;
	}

	@Override
	protected void setValueInCellRenderer( final JList list, final Object value, final int index )
	{
		final ListView.Item item = (ListView.Item)value;
		final VirtualFile file = item.getFile();
		_file = file;

		final Locale locale = _explorer.getLocale();

		String name = file.getDisplayName( locale );
		if ( name == null )
		{
			name = file.getName();
		}

		_name = name;
		_description = file.getDescription( locale );

		if ( file.isDirectory() )
		{
			_thumbnailImage = getFolderIcon();
		}
		else
		{
			Icon thumbnailIcon = null;

			final Image thumbnailImage = item.getThumbnail();
			if ( thumbnailImage != null )
			{
				if ( ( thumbnailImage instanceof RenderedImage ) &&
				     ( thumbnailImage.getWidth( null ) == 1 ) &&
				     ( thumbnailImage.getHeight( null ) == 1 ) )
				{
					final BufferedImage bufferedImage = (BufferedImage)thumbnailImage;
					thumbnailIcon = new PaintIcon( _thumbnailSize, _thumbnailSize, new Color( bufferedImage.getRGB( 0, 0 ) ) );
				}
				else
				{
					thumbnailIcon = new ImageIcon( thumbnailImage );
				}
			}

			_thumbnailImage = thumbnailIcon;
		}

		_thumbnailBorderPainted = !file.isDirectory();
	}
}
