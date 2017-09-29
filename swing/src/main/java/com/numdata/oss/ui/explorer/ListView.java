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
import java.awt.Dimension;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

import com.numdata.oss.*;
import com.numdata.oss.ui.*;
import org.jetbrains.annotations.*;

/**
 * A view based on a {@link JList}, supporting sorting and filtering. Many
 * common views can easily be derived from this class by replacing the list's
 * cell renderer.
 *
 * @author  G. Meinders
 */
public abstract class ListView
	extends JList
	implements View
{
	/**
	 * {@link Explorer} that holds the model for this view.
	 */
	protected final Explorer _explorer;

	/**
	 * Construct new thumbnail view.
	 *
	 * @param   explorer        Explorer that holds the model for this view.
	 * @param   sorted          {@code true} to sort items in the view.
	 */
	protected ListView( final Explorer explorer, final boolean sorted )
	{
		_explorer = explorer;

		setBorder( BorderFactory.createEmptyBorder( 1 , 1 , 1 , 1 ) );
		setCellRenderer( new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent( final JList list , final Object value , final int index , final boolean isSelected , final boolean cellHasFocus )
			{
				final Object displayValue;
				final String toolTipText;
				if ( value instanceof Item )
				{
					final Item item = (Item)value;
					final VirtualFile file = item.getFile();

					final Locale locale = explorer.getLocale();
					displayValue = file.getDisplayName( locale );

					final String description = file.getDescription( locale );
					toolTipText  = TextTools.isNonEmpty( description ) ? description : null;
				}
				else
				{
					displayValue = value;
					toolTipText  = null;
				}

				final Component result = super.getListCellRendererComponent( list , displayValue , index , isSelected , cellHasFocus );
				setToolTipText( toolTipText );
				return result;
			}
		} );

		/*
		 * Create a list model that supports sorting and filtering.
		 */
		final ListModel model = new BackgroundProcessingListModel<Item>()
		{
			@Override
			protected void process( final Item item )
			{
				final int thumbnailSize = explorer.getThumbnailSize();
				item.loadThumbnail( thumbnailSize, thumbnailSize );
			}
		};

		final ListRowSorter<ListModel> sorter = new ListRowSorter<ListModel>( model );
		if ( sorted )
		{
			sorter.setSortKeys( Arrays.asList( new RowSorter.SortKey( 0 , SortOrder.ASCENDING ) ) );
		}
		sorter.setComparator( 0 , new DefaultItemComparator( explorer.getLocale() ) );

		/*
		 * Provide string converter to support quick find.
		 */
		sorter.setStringConverter( new ListStringConverter()
		{
			@Override
			public String toString( final ListModel model , final int row )
			{
				final Item item = (Item)model.getElementAt( row );
				final VirtualFile file = item.getFile();
				final Locale locale = explorer.getLocale();
				return file.getDisplayName( locale ) + " " + file .getDescription( locale );
			}
		} );

		setModel( new SortedListModel<ListModel>( model , sorter ) );

		/*
		 * Update the explorer when a file is selected.
		 */
		addListSelectionListener( new ListSelectionListener()
		{
			@Override
			public void valueChanged( final ListSelectionEvent e )
			{
				final Item item = (Item)getSelectedValue();
				explorer.setSelectedFile( ( item == null ) ? null : item.getFile() );
			}
		} );

		/*
		 * Allow for the user to open folders by double-clicking.
		 */
		addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent e )
			{
				if ( SwingUtilities.isLeftMouseButton( e ) && ( e.getClickCount() == 2 ) )
				{
					final Item item = (Item)getSelectedValue();
					if ( item != null )
					{
						final VirtualFile file = item.getFile();
						if ( file != null )
						{
							explorer.doubleClickFile( file );
						}
					}
				}
			}
		} );

		/*
		 * Provide prototype for improved performance for long lists.
		 */
		setPrototypeCellValue( new Item( new PrototypeVirtualFile() ) );

		/*
		 * Ensure that the current selection is visible after a resize.
		 */
		addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentResized( final ComponentEvent e )
			{
				ensureIndexIsVisible( getLeadSelectionIndex() );
			}
		} );
	}

	/**
	 * Returns the default list model backing the view.
	 *
	 * @return  Default list model.
	 */
	private BackgroundProcessingListModel<Item> getDefaultListModel()
	{
		final SortedListModel<BackgroundProcessingListModel<Item>> sortedListModel = (SortedListModel<BackgroundProcessingListModel<Item>>)getModel();
		return sortedListModel.getModel();
	}

	@Override
	public void update()
	{
		final BackgroundProcessingListModel<Item> model = getDefaultListModel();

		final List<Item> items = new ArrayList<Item>();
		for ( final VirtualFile file : _explorer.getFileList() )
		{
			items.add( new Item( file ) );
		}

		if ( model.equals( items ) )
		{
			repaint();
		}
		else
		{
			model.clear();
			model.addAll( items );
		}

		clearSelection();
		for ( final VirtualFile selected : _explorer.getSelectedItems() )
		{
			setSelectedValue( selected , true );
		}
	}

	@Override
	public DefaultRowSorter<?,?> getRowSorter()
	{
		final SortedListModel<ListModel> sortedListModel = (SortedListModel<ListModel>)getModel();
		return (DefaultRowSorter<?,?>)sortedListModel.getSorter();
	}

	@Override
	public void itemStateChanged( final ItemEvent e )
	{
		final boolean selected = e.getStateChange() == ItemEvent.SELECTED;
		final VirtualFile file = (VirtualFile)e.getItem();

		final ListModel listModel = getModel();
		for ( int i = 0 ; i < listModel.getSize() ; i++ )
		{
			final Item item = (Item)listModel.getElementAt( i );
			if ( file.equals( item.getFile() ) )
			{
				if ( selected )
				{
					addSelectionInterval( i, i );
				}
				else
				{
					removeSelectionInterval( i, i );
				}
			}
		}

		final int leadSelectionIndex = getLeadSelectionIndex();
		if ( leadSelectionIndex >= 0 )
		{
			ensureIndexIsVisible( leadSelectionIndex );
		}
	}

	@Override
	public Component getComponent()
	{
		return this;
	}

	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		return ( getVisibleRowCount() == 0 ) || super.getScrollableTracksViewportWidth();
	}

	@Override
	public boolean getScrollableTracksViewportHeight()
	{
		return false;
	}

	@Override
	public Dimension getItemSize()
	{
		final ListCellRenderer renderer = getCellRenderer();
		final Component rendererComponent = renderer.getListCellRendererComponent( this, getPrototypeCellValue(), 0, false, false );
		return rendererComponent.getPreferredSize();
	}

	/**
	 * List item, with a cached thumbnail image that's fetched asynchronously.
	 */
	public static class Item
	{
		/**
		 * Virtual file represented by the item.
		 */
		private final VirtualFile _file;

		/**
		 * Cached thumbnail image.
		 */
		private Image _thumbnail;

		/**
		 * Constructs a list view item for the given virtual file.
		 *
		 * @param   file    Virtual file.
		 */
		public Item( final VirtualFile file )
		{
			_file = file;
			_thumbnail = null;
		}

		/**
		 * Returns the virtual file represented by the item.
		 *
		 * @return  Virtual file.
		 */
		public VirtualFile getFile()
		{
			return _file;
		}

		/**
		 * Loads the file's thumbnail image.
		 *
		 * @param   width   Thumbnail width to be requested.
		 * @param   height  Thumbnail height to be requested.
		 */
		public void loadThumbnail( final int width , final int height )
		{
			Image thumbnail = null;

			final ImageSource thumbnailSource = _file.getThumbnail( width , height );
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

			if ( thumbnail == null )
			{
				thumbnail = ImageTools.getImage( "product-configurator/unknown.jpg" );
			}

			_thumbnail = thumbnail;
		}

		/**
		 * Returns the (cached) thumbnail image for the item's virtual file.
		 *
		 * @return  Thumbnail image; or {@code null} if not yet loaded.
		 */
		public Image getThumbnail()
		{
			return _thumbnail;
		}
	}

	/**
	 * Implements the default sorting behavior for items, sorting directories
	 * before files and then sorting by name and finally by description.
	 */
	public static class DefaultItemComparator
		implements Comparator<Item>
	{
		/**
		 * Locale to be used.
		 */
		private Locale _locale;

		/**
		 * Constructs a new comparator for the given locale.
		 *
		 * @param   locale  Locale to be used.
		 */
		public DefaultItemComparator( @NotNull final Locale locale )
		{
			_locale = locale;
		}

		@Override
		public int compare( final Item item , final Item other )
		{
			int result = 0;

			final VirtualFile file = item.getFile();
			final VirtualFile otherFile = other.getFile();

			final boolean directory = file.isDirectory();
			if ( directory != otherFile.isDirectory() )
			{
				result = directory ? -1 : 1;
			}

			final Locale locale = _locale;

			if ( result == 0 )
			{
				result = TextTools.compare( file.getDisplayName( locale ), otherFile.getDisplayName( locale ) );
			}

			if ( result == 0 )
			{
				result = TextTools.compare( file.getDescription( locale ), otherFile.getDescription( locale ) );
			}

			return result;
		}
	}
}
