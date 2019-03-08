/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.concurrent.ExecutionException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import com.numdata.oss.TextTools;


/**
 * Demo application that searches images using Flickr. It is used to demonstrate
 * the {@link BackgroundProcessingListModel}.
 *
 * @author Peter S. Heijnen
 * @author John O'Conner (original Sun example code)
 */
public class BackgroundProcessingListModelDemoApp
extends JFrame
{
	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName() );
				}
				catch ( Exception ignore )
				{
				}

				final JFrame frame = new BackgroundProcessingListModelDemoApp();
				WindowTools.center( frame, -200, -100 );
				frame.setVisible( true );
			}
		} );
	}

	/**
	 * Construct application frame.
	 */
	private BackgroundProcessingListModelDemoApp()
	{
		super( "Image Search" );
		setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

		final JTextField searchField = new JTextField();

		final JProgressBar progressBar = new JProgressBar();

		final BackgroundProcessingListModel<FlickrWebClient.ImageInfo> listModel = new BackgroundProcessingListModel<FlickrWebClient.ImageInfo>()
		{
			/** Serialized data version. */
			private static final long serialVersionUID = 4598738474171628782L;

			@Override
			protected void process( final FlickrWebClient.ImageInfo info )
			{
				info.setThumbnail( FlickrWebClient.retrieveThumbNail( info ) );
			}

			@Override
			protected void processed( final List<FlickrWebClient.ImageInfo> elements, final int progress )
			{
				super.processed( elements, progress );
				progressBar.setIndeterminate( false );
				progressBar.setValue( progress );
			}
		};

		final SortedListModel<ListModel> sortedModel = new SortedListModel<ListModel>();

		final ListRowSorter<ListModel> sorter = new ListRowSorter<ListModel>( listModel );
		sorter.setSortKeys( Collections.singletonList( new RowSorter.SortKey( 0, SortOrder.ASCENDING ) ) );
		sorter.setStringConverter( new ListStringConverter()
		{
			@Override
			public String toString( final ListModel model, final int row )
			{
				final FlickrWebClient.ImageInfo imageInfo = (FlickrWebClient.ImageInfo)model.getElementAt( row );
				return imageInfo.getTitle();
			}
		} );
		sortedModel.setModel( listModel );
		sortedModel.setSorter( sorter );

		searchField.addKeyListener( new KeyAdapter()
		{
			/** Active searcher. */
			private Searcher _searcher = null;

			@Override
			public void keyPressed( final KeyEvent evt )
			{
				if ( evt.getKeyCode() == KeyEvent.VK_ENTER )
				{
					final String text = searchField.getText();
					if ( TextTools.isNonEmpty( text ) )
					{
						final Searcher previousSearcher = _searcher;
						if ( ( previousSearcher != null ) && !previousSearcher.isDone() )
						{
							previousSearcher.cancel( true );
						}

						listModel.clear();
						progressBar.setIndeterminate( true );

						final Searcher searcher = new Searcher( listModel, text );
						_searcher = searcher;
						searcher.execute();
					}
				}
			}
		} );

		final JList imageList = new JList( sortedModel );
		imageList.setLayoutOrientation( JList.HORIZONTAL_WRAP );
		imageList.setVisibleRowCount( 0 );
		imageList.setCellRenderer( new CellRenderer() );
		imageList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

		final Box searchBox = new Box( BoxLayout.LINE_AXIS );
		searchBox.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		searchBox.add( new JLabel( "Search" ) );
		searchBox.add( searchField );
		searchBox.add( Box.createHorizontalGlue() );

		final Box progressBox = new Box( BoxLayout.LINE_AXIS );
		progressBox.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		progressBox.add( new JLabel( "Matched Images" ) );
		progressBox.add( progressBar );

		final JPanel topPanel = new JPanel( new BorderLayout() );
		topPanel.add( searchBox, BorderLayout.NORTH );
		topPanel.add( progressBox, BorderLayout.SOUTH );

		final JPanel contentPane = new JPanel( new BorderLayout() );
		contentPane.add( topPanel, BorderLayout.NORTH );
		contentPane.add( new JScrollPane( imageList ) );
		setContentPane( contentPane );
	}

	/**
	 * Worker that performs the search in the background.
	 */
	public static class Searcher
	extends SwingWorker<List<FlickrWebClient.ImageInfo>, Void>
	{
		/**
		 * List to add images to.
		 */
		final List<FlickrWebClient.ImageInfo> _list;

		/**
		 * Text to search.
		 */
		private final String _text;

		/**
		 * Construct worker.
		 *
		 * @param list List to add images to.
		 * @param text Text to search.
		 */
		public Searcher( final List<FlickrWebClient.ImageInfo> list, final String text )
		{
			_list = new ArrayList<FlickrWebClient.ImageInfo>( list );
			_text = text;
		}

		@Override
		protected List<FlickrWebClient.ImageInfo> doInBackground()
		{
			return FlickrWebClient.searchImages( _text, 50 );
		}

		@Override
		protected void done()
		{
			if ( !isCancelled() )
			{
				List<FlickrWebClient.ImageInfo> infoList = null;
				try
				{
					infoList = get();
				}
				catch ( ExecutionException ex )
				{
					ex.printStackTrace();
				}
				catch ( InterruptedException ex )
				{
					ex.printStackTrace();
				}

				final List<FlickrWebClient.ImageInfo> list = _list;
				if ( infoList == null )
				{
					//noinspection ConstantConditions
					list.add( new FlickrWebClient.ImageInfo( null, null, null, null, "Error occurred!", false, false, false ) );
				}
				else if ( infoList.isEmpty() )
				{
					//noinspection ConstantConditions
					list.add( new FlickrWebClient.ImageInfo( null, null, null, null, "No images available", false, false, false ) );
				}
				else
				{
					list.addAll( infoList );
				}
			}
		}
	}

	/**
	 * Cell renderer for image list.
	 */
	public static class CellRenderer
	extends DefaultListCellRenderer
	{
		/**
		 * Current image info to render.
		 */
		@SuppressWarnings( "NonSerializableFieldInSerializableClass" )
		private FlickrWebClient.ImageInfo _info = null;

		@Override
		public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
		{
			_info = (FlickrWebClient.ImageInfo)value;
			return super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
		}

		@Override
		public void setIcon( final Icon icon )
		{
			super.setIcon( _info != null ? _info.getThumbnail() : null );
		}

		@Override
		public void setText( final String text )
		{
			super.setText( _info != null ? _info.getTitle() : null );
		}
	}
}
