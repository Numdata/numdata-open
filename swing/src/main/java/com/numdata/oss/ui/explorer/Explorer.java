/*
 * Copyright (c) 2006-2019, Numdata BV, The Netherlands.
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
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import com.numdata.oss.ui.*;
import org.jetbrains.annotations.*;

/**
 * This GUI component tries to imitate some of the behavior of the Microsoft
 * Windows XP explorer.
 *
 * This class is by no means meant to implement a complete file browsing
 * environment, just enough (GUI-only) to allow users to make a file selection
 * using a familiar interface.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "NonSerializableFieldInSerializableClass" )
public class Explorer
extends JPanel
implements ActionListener, ItemSelectable
{
	// Explorer style :

	/**
	 * Text color.
	 */
	public static final Color TEXT_COLOR = Color.BLACK;

	/**
	 * Text color.
	 */
	public static final Color DETAILS_COLOR = Color.GRAY;

	/**
	 * Item border color.
	 */
	public static final Color ITEM_BORDER_COLOR = new Color( 224, 223, 227 );

	/**
	 * Selection color.
	 */
	public static final Color SELECT_COLOR = new Color( 234, 233, 237 );

	/**
	 * Folder outline color.
	 */
	public static final Color FOLDER_OUTLINE_COLOR = new Color( 197, 140, 0 );

	/**
	 * Folder rear left color.
	 */
	public static final Color FOLDER_REAR_LEFT_COLOR = new Color( 255, 234, 127 );

	/**
	 * Folder rear right color.
	 */
	public static final Color FOLDER_REAR_RIGHT_COLOR = new Color( 254, 255, 133 );

	/**
	 * Folder front top color.
	 */
	public static final Color FOLDER_FRONT_TOP_COLOR = new Color( 255, 255, 173 );

	/**
	 * Folder front bottom color.
	 */
	public static final Color FOLDER_FRONT_BOTTOM_COLOR = new Color( 255, 210, 101 );

	/**
	 * Thumbnail insets.
	 */
	public static final Insets THUMBNAIL_IMAGE_INSETS = new Insets( 16, 14, 12, 14 );

	/**
	 * Default columns.
	 */
	private static final int DEFAULT_COLUMNS = 4;

	/**
	 * Default rows.
	 */
	private static final int DEFAULT_ROWS = 3;

	/**
	 * Property name: current folder, {@link #getCurrentFolder()}.
	 */
	public static final String CURRENT_FOLDER_PROPERTY = "currentFolder";

	/**
	 * Enum of all possible toolbar components.
	 */
	public enum ToolBarComponent
	{
		/**
		 * Label in front of FOLDER_LIST, like 'Looking in folder: '.
		 */
		FOLDER_LIST_LABEL,

		/**
		 * The {@link JComboBox} containing the list of folders.
		 */
		FOLDER_LIST,

		/**
		 * The button to create a new folder.
		 */
		CREATE_FOLDER,

		/**
		 * The button to go to the parent folder of the current folder.
		 */
		FOLDER_UP,

		/**
		 * The button to delete a folder or file.
		 */
		DELETE,

		/**
		 * Creates an invisible, fixed-width component of 3 px.
		 */
		HSTRUT,

		/**
		 * An invisible filler component.
		 */
		GLUE
	}

	/**
	 * Text font.
	 */
	private final Font _textFont;

	/**
	 * Number of rows to display.
	 */
	private int _rows = DEFAULT_ROWS;

	/**
	 * Number of columns to display.
	 */
	private int _columns = DEFAULT_COLUMNS;

	/**
	 * Scroll pane containing the view component.
	 */
	private JScrollPane _scrollPane;

	/**
	 * Tool bar at the top of the explorer, if any.
	 */
	private JToolBar _toolBar = null;

	/**
	 * Location of the tool bar.
	 */
	private int _toolBarSide;

	/**
	 * Current view.
	 */
	private View _view = null;

	/**
	 * Size of thumbnail images.
	 */
	private int _thumbnailSize;

	/**
	 * Action command associated with action events.
	 */
	@Nullable
	private String _actionCommand;

	/**
	 * Combo box to select folders.
	 */
	private JComboBox _folderComboBox;

	/**
	 * Filesystem the explorer was constructed with.
	 */
	@NotNull
	private VirtualFileSystem _fileSystem;

	/**
	 * Base folder of the explorer. ("root")
	 */
	@NotNull
	private String _baseFolder;

	/**
	 * Current active folder.
	 */
	@NotNull
	private String _currentFolder;

	/**
	 * File that is currently selected.
	 */
	@Nullable
	private VirtualFile _selectedFile;

	/**
	 * Construct explorer panel.
	 *
	 * @param virtualFileSystem {@link VirtualFileSystem} to use.
	 * @param baseFolder        Folder to show in explorer.
	 */
	public Explorer( @NotNull final VirtualFileSystem virtualFileSystem, @Nullable final String baseFolder )
	{
		this( 64, virtualFileSystem, baseFolder, new ToolBarComponent[] {
		Explorer.ToolBarComponent.FOLDER_LIST_LABEL,
		Explorer.ToolBarComponent.FOLDER_LIST,
		Explorer.ToolBarComponent.HSTRUT,
		Explorer.ToolBarComponent.FOLDER_UP,
		Explorer.ToolBarComponent.CREATE_FOLDER } );
	}

	/**
	 * Construct explorer panel.
	 *
	 * @param fileSystem        {@link VirtualFileSystem} to use.
	 * @param baseFolder        Folder to show in explorer. {@code null} for root.
	 * @param toolBarComponents Toolbar components to add to the toolbar.
	 */
	public Explorer( @NotNull final VirtualFileSystem fileSystem, @Nullable final String baseFolder, @Nullable final ToolBarComponent[] toolBarComponents )
	{
		this( 64, fileSystem, baseFolder, toolBarComponents );
	}

	/**
	 * Construct explorer panel.
	 *
	 * @param thumbnailSize     Size of thumbnail images.
	 * @param fileSystem        {@link VirtualFileSystem} to use.
	 * @param baseFolder        Folder to show in explorer. {@code null} for root.
	 * @param toolBarComponents Toolbar components to add to the toolbar.
	 */
	public Explorer( final int thumbnailSize, @NotNull final VirtualFileSystem fileSystem, @Nullable final String baseFolder, @Nullable final ToolBarComponent[] toolBarComponents )
	{
		super( new BorderLayout() );

		_fileSystem = fileSystem;
		_baseFolder = ( baseFolder == null ) ? "" : baseFolder;
		_currentFolder = ( baseFolder == null ) ? "" : baseFolder;
		_selectedFile = null;
		_actionCommand = null;
		_thumbnailSize = thumbnailSize;
		_toolBarSide = SwingConstants.TOP;

		_textFont = new Font( Font.SANS_SERIF, Font.PLAIN, 10 );

		/*
		 * Initialize GUI.
		 */
		initGUI( toolBarComponents );

		setView( new ThumbnailView( this, false ) );

		refresh();
	}

	/**
	 * Sets the size of thumbnail images used in the explorer.
	 *
	 * @param thumbnailSize Thumbnail image size, both width and height, in
	 *                      pixels.
	 */
	public void setThumbnailSize( final int thumbnailSize )
	{
		_thumbnailSize = thumbnailSize;
	}

	/**
	 * Returns the font to be used for regular text.
	 *
	 * @return Font to be used for text.
	 */
	public Font getTextFont()
	{
		return _textFont;
	}

	/**
	 * Initialize the GUI.
	 *
	 * @param toolBarComponents Initial tool bar components.
	 */
	private void initGUI( @Nullable final ToolBarComponent[] toolBarComponents )
	{
		removeAll();

		setLayout( new BorderLayout() );
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );

		_scrollPane = new JScrollPane( null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );

		final JComboBox folderComboBox = new JComboBox();
		folderComboBox.addActionListener( this );
		folderComboBox.setActionCommand( "folderChanged" );
		folderComboBox.setBorder( null );

		_folderComboBox = folderComboBox;

		if ( toolBarComponents != null )
		{
			for ( final ToolBarComponent component : toolBarComponents )
			{
				addToolbarComponent( component );
			}
		}

		add( _scrollPane, BorderLayout.CENTER );
	}

	/**
	 * Set view of explorer.
	 *
	 * @param view View to set.
	 */
	public void setView( final View view )
	{
		final View oldView = _view;
		if ( oldView != view )
		{
			if ( oldView != null )
			{
				removeItemListener( oldView );
			}

			_view = view;
			addItemListener( view ); // View should listen for selection changes.

			final Component viewComponent = view.getComponent();

			final JScrollPane scrollPane = _scrollPane;
			scrollPane.setViewportView( viewComponent );

			final JViewport viewport = scrollPane.getViewport();
			viewport.setBackground( viewComponent.getBackground() );
			viewport.setOpaque( true );

			setPreferredViewportSize();
		}
	}

	/**
	 * Returns the row sorter responsible for sorting and filtering the view, if
	 * supported by the view implementation.
	 *
	 * @return Row sorter; {@code null} if not supported.
	 */
	public DefaultRowSorter<?, ?> getRowSorter()
	{
		return _view.getRowSorter();
	}

	/**
	 * Add item listener for selection changes.
	 *
	 * @param listener Item listener to add.
	 */
	@Override
	public void addItemListener( final ItemListener listener )
	{
		listenerList.add( ItemListener.class, listener );
	}

	/**
	 * Removes the specified item listener.
	 *
	 * @param listener Item listener to remove.
	 */
	@Override
	public void removeItemListener( final ItemListener listener )
	{
		listenerList.remove( ItemListener.class, listener );
	}

	/**
	 * Fire {@link ItemEvent#ITEM_STATE_CHANGED} event.
	 *
	 * @param event Event to be fired.
	 */
	private void fireItemEvent( final ItemEvent event )
	{
		for ( final ItemListener listener : listenerList.getListeners( ItemListener.class ) )
		{
			listener.itemStateChanged( event );
		}
	}

	/**
	 * Add action listener invoked when a file is double clicked.
	 *
	 * @param listener Action listener.
	 */
	public void addActionListener( final ActionListener listener )
	{
		listenerList.add( ActionListener.class, listener );
	}

	/**
	 * Removes the specified action listener.
	 *
	 * @param listener Action listener to remove .
	 */
	public void removeActionListener( final ActionListener listener )
	{
		listenerList.remove( ActionListener.class, listener );
	}

	/**
	 * Fire {@link ActionEvent#ACTION_PERFORMED} event.
	 */
	private void fireActionPerformed()
	{
		for ( final ActionListener listener : listenerList.getListeners( ActionListener.class ) )
		{
			final ActionEvent actionEvent = new ActionEvent( this, ActionEvent.ACTION_PERFORMED, getActionCommand() );
			listener.actionPerformed( actionEvent );
		}
	}

	/**
	 * Get action command associated with action events.
	 *
	 * @return Action command associated with action events.
	 */
	@Nullable
	public String getActionCommand()
	{
		return _actionCommand;
	}

	/**
	 * Returns all selected files as array.
	 *
	 * @return the array of selected files.
	 */
	public VirtualFile[] getSelectedItems()
	{
		final VirtualFile selected = getSelectedFile();
		return ( selected != null ) ? new VirtualFile[] { selected } : new VirtualFile[ 0 ];
	}

	@Override
	public Object[] getSelectedObjects()
	{
		return getSelectedItems();
	}

	/**
	 * Get size of thumbnail images.
	 *
	 * @return Size of thumbnail images.
	 */
	public int getThumbnailSize()
	{
		return _thumbnailSize;
	}

	/**
	 * Check if the specified file is currently selected.
	 *
	 * @param file File to check.
	 *
	 * @return {@code true} if the specified file is selected; {@code false}
	 *         otherwise.
	 */
	protected boolean isSelected( final VirtualFile file )
	{
		return ( getSelectedFile() == file );
	}

	/**
	 * Set action command to associate with action events.
	 *
	 * @param actionCommand Action command to associate with action events.
	 */
	public void setActionCommand( @Nullable final String actionCommand )
	{
		_actionCommand = actionCommand;
	}

	/**
	 * Get the currently selected file.
	 *
	 * @return The currently selected item.
	 */
	@Nullable
	public VirtualFile getSelectedFile()
	{
		return _selectedFile;
	}

	/**
	 * Set currently selected file.
	 *
	 * @param file File to set select.
	 */
	public void setSelectedFile( @Nullable final VirtualFile file )
	{
		final VirtualFile oldValue = _selectedFile;
		if ( oldValue != file )
		{
			if ( oldValue != null )
			{
				fireItemEvent( new ItemEvent( this, ItemEvent.ITEM_STATE_CHANGED, oldValue, ItemEvent.DESELECTED ) );
			}

			_selectedFile = file;

			if ( file != null )
			{
				fireItemEvent( new ItemEvent( this, ItemEvent.ITEM_STATE_CHANGED, file, ItemEvent.SELECTED ) );
			}
		}
	}

	/**
	 * Handle double click on specified file.
	 *
	 * @param file The {@link VirtualFile} that is double clicked.
	 */
	public void doubleClickFile( final VirtualFile file )
	{
		if ( file.isDirectory() )
		{
			setCurrentFolder( file.getPath() );
		}
		else
		{
			setSelectedFile( file );
			fireActionPerformed();
		}
	}

	/**
	 * Sets number of rows to display.
	 *
	 * @param rowsToDisplay The number of rows to display.
	 */
	public void setRows( final int rowsToDisplay )
	{
		_rows = rowsToDisplay;
		setPreferredViewportSize();
	}

	/**
	 * Sets number of columns to display.
	 *
	 * @param columnsToDisplay The number of columns to display.
	 */
	public void setColumns( final int columnsToDisplay )
	{
		_columns = columnsToDisplay;
		setPreferredViewportSize();
	}

	/**
	 * Sets the preferred size of the viewport to fit the number of rows and
	 * columns specified by {@link #_rows} and {@link #_columns}.
	 */
	private void setPreferredViewportSize()
	{
		final Dimension itemSize = _view.getItemSize();
		final JScrollBar verticalScrollBar = _scrollPane.getVerticalScrollBar();
		final int verticalScrollBarWidth = verticalScrollBar.getPreferredSize().width;

		/*
		 * Somehow the calculated viewport width still causes wrapping in some
		 * cases. Not sure why, but a small extra margin fixes it without being
		 * really noticeable.
		 */
		final int extraWidth = 2;

		final Dimension preferredSize = new Dimension();
		preferredSize.width = itemSize.width * _columns + verticalScrollBarWidth + extraWidth;
		preferredSize.height = itemSize.height * _rows;

		if ( ( preferredSize.width != 0 ) && ( preferredSize.height != 0 ) )
		{
			final JViewport viewport = _scrollPane.getViewport();
			viewport.setPreferredSize( preferredSize );
		}
	}

	@Override
	public void actionPerformed( final ActionEvent event )
	{
		final JComboBox folderComboBox = _folderComboBox;
		final VirtualFileSystem fileSystem = _fileSystem;
		final String currentFolder = _currentFolder;

		if ( "folderChanged".equals( event.getActionCommand() ) )
		{
			final Object selectedItem = folderComboBox.getSelectedItem();
			setCurrentFolder( ( selectedItem == null ) ? getBaseFolder() : getBaseFolder() + selectedItem );
		}
		else if ( "parentFolder".equals( event.getActionCommand() ) )
		{
			// FIXME: The file system should provide a parent file, instead of using a hardcoded separator character.
			final int lastSeparator = currentFolder.lastIndexOf( (int)'/' );
			if ( lastSeparator > -1 )
			{
				setCurrentFolder( currentFolder.substring( 0, lastSeparator ) );
			}
		}
		else if ( "newFolderButton".equals( event.getActionCommand() ) )
		{
			final String filename = JOptionPane.showInputDialog( "Nieuwe map: " );
			if ( filename != null )
			{
				try
				{
					fileSystem.createFolder( fileSystem.getPath( currentFolder, filename ) );

					setCurrentFolder( currentFolder );
				}
				catch ( IOException ignored )
				{
					WindowTools.showErrorDialog( getTopLevelAncestor(), "Fout bij maken van map", "Er is een fout opgetreden bij het maken van de nieuwe map." );
				}
			}
		}
	}

	/**
	 * Sets the folder that is displayed. When set, the view is updated to show the
	 * current content of the specified folder.
	 *
	 * @param folderPath Path of the folder.
	 */
	public void setCurrentFolder( final String folderPath )
	{
		setCurrentFolder( folderPath, false );
	}

	/**
	 * Sets the folder that is displayed. When set, the view is updated to show the
	 * current content of the specified folder.
	 *
	 * @param folderPath Path of the folder.
	 * @param forceUpdate Update the UI regardless of whether {@link #_currentFolder} changed.
	 */
	private void setCurrentFolder( final String folderPath, final boolean forceUpdate )
	{
		if ( folderPath == null )
		{
			throw new NullPointerException( "folderPath" );
		}

		final String currentFolder = _currentFolder;
		final String baseFolder = getBaseFolder();
		final String newFolder = folderPath.startsWith( baseFolder ) ? folderPath : baseFolder;

		if ( forceUpdate || !newFolder.equals( currentFolder ) )
		{
			setSelectedFile( null );
			_currentFolder = newFolder;
			firePropertyChange( CURRENT_FOLDER_PROPERTY, currentFolder, newFolder );
			refresh();
		}
	}

	/**
	 * Updates the explorer to reflect the current state of the file system.
	 */
	public void refresh()
	{
		setSelectedFile( null );

		_view.update();

		/*
		 * Refresh the folderlist in the combo box to the current folder.
		 */
		final JComboBox folderComboBox = _folderComboBox;
		folderComboBox.removeActionListener( this ); // Temporarily stop explorer from listening to prevent "circular" events.
		folderComboBox.removeAllItems();

		final List<VirtualFile> folders;
		{
			try
			{
				folders = _fileSystem.getFolders( _currentFolder );
			}
			catch ( IOException e )
			{
				throw new RuntimeException( e );
			}
		}

		final String baseFolder = _baseFolder;

		for ( final VirtualFile file : folders )
		{
			if ( file.isDirectory() )
			{
				final String folderPath = file.getPath();
				if ( folderPath.startsWith( baseFolder ) )
				{
					final String pathSuffix = folderPath.substring( baseFolder.length() );
					final String item = pathSuffix.isEmpty() ? "(homeFolder)" : pathSuffix;

					folderComboBox.addItem( item );
					if ( folderPath.equals( _currentFolder ) )
					{
						folderComboBox.setSelectedItem( item );
					}
				}
			}
		}

		folderComboBox.addActionListener( this ); // Make explorer listen again..

		/*
		 * Reset the scrollbar.
		 */
		final JScrollBar scrollBar = _scrollPane.getVerticalScrollBar();
		final BoundedRangeModel rangeModel = scrollBar.getModel();
		rangeModel.setValue( 0 );

		repaint();
	}

	/**
	 * Returns the folder that is currently viewed by the explorer.
	 *
	 * @return the current folder.
	 */
	@NotNull
	public String getCurrentFolder()
	{
		return _currentFolder;
	}

	/**
	 * Get list of {@link VirtualFile}s in the current directory.
	 *
	 * @return List of virtual files in the current directory..
	 */
	public List<VirtualFile> getFileList()
	{
		try
		{
			return new ArrayList<VirtualFile>( _fileSystem.getFolderContents( _currentFolder ) );
		}
		catch ( IOException e )
		{
			throw new RuntimeException( e );
		}
	}

	/**
	 * Returns the base folder.
	 *
	 * @return the base folder.
	 */
	@NotNull
	public String getBaseFolder()
	{
		return _baseFolder;
	}

	/**
	 * Returns the current view.
	 *
	 * @return the current view.
	 */
	public View getView()
	{
		return _view;
	}

	/**
	 * Returns the current x value of the view port position from scrollpane.
	 *
	 * @return the x value of view port position.
	 */
	public int getViewPortPositionX()
	{
		final JViewport viewport = _scrollPane.getViewport();
		return viewport.getViewPosition().x;
	}

	/**
	 * Get location of the tool bar.
	 *
	 * @return Location of the tool bar.
	 */
	public int getToolBarSide()
	{
		return _toolBarSide;
	}

	/**
	 * Set location of the tool bar.
	 *
	 * @param toolBarSide Location of the tool bar {@link SwingConstants#LEFT},
	 *                    {@link SwingConstants#RIGHT}, {@link SwingConstants#LEFT},
	 *                    {@link SwingConstants#BOTTOM} or {@link
	 *                    SwingConstants#TOP}.
	 */
	public void setToolBarSide( final int toolBarSide )
	{
		if ( toolBarSide != SwingConstants.LEFT
		     && toolBarSide != SwingConstants.TOP
		     && toolBarSide != SwingConstants.RIGHT
		     && toolBarSide != SwingConstants.BOTTOM )
		{
			throw new IllegalArgumentException();
		}
		else
		{
			_toolBarSide = toolBarSide;

			final JToolBar toolBar = _toolBar;
			int orientation = SwingConstants.HORIZONTAL;
			String constraint = BorderLayout.NORTH;
			switch ( _toolBarSide )
			{
				case SwingConstants.LEFT:
					constraint = BorderLayout.WEST;
					orientation = SwingConstants.VERTICAL;
					break;

				case SwingConstants.RIGHT:
					constraint = BorderLayout.EAST;
					orientation = SwingConstants.VERTICAL;
					break;

				case SwingConstants.TOP:
					constraint = BorderLayout.NORTH;
					orientation = SwingConstants.HORIZONTAL;
					break;

				case SwingConstants.BOTTOM:
					constraint = BorderLayout.SOUTH;
					orientation = SwingConstants.HORIZONTAL;
					break;
			}

			toolBar.setOrientation( orientation );
			remove( toolBar );
			add( toolBar, constraint );
		}
	}

	/**
	 * Adds a {@link ToolBarComponent} to the tool bar.
	 *
	 * @param toolBarComponent {@link ToolBarComponent} to add.
	 */
	public void addToolbarComponent( final ToolBarComponent toolBarComponent )
	{
		final Component component;

		if ( toolBarComponent == ToolBarComponent.FOLDER_LIST_LABEL )
		{
			component = new JLabel( "Map: " );
		}
		else if ( toolBarComponent == ToolBarComponent.FOLDER_LIST )
		{
			component = _folderComboBox;
		}
		else if ( toolBarComponent == ToolBarComponent.FOLDER_UP )
		{
			final JButton parentFolderButton = new JButton();
			parentFolderButton.setIcon( new AsyncIcon( Explorer.class, "/icons/folderUp-16x16.gif", 16, 16, parentFolderButton ) );
			parentFolderButton.setActionCommand( "parentFolder" );
			parentFolderButton.addActionListener( this );
			parentFolderButton.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) ) );
			component = parentFolderButton;
		}
		else if ( toolBarComponent == ToolBarComponent.CREATE_FOLDER )
		{
			final JButton newFolderButton = new JButton();
			newFolderButton.setIcon( new AsyncIcon( Explorer.class, "/icons/folderNew-16x16.gif", 16, 16, newFolderButton ) );
			newFolderButton.setActionCommand( "newFolderButton" );
			newFolderButton.addActionListener( this );
			newFolderButton.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEtchedBorder(), BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) ) );
			component = newFolderButton;
		}
		else if ( toolBarComponent == ToolBarComponent.HSTRUT )
		{
			component = Box.createHorizontalStrut( 3 );
		}
		else if ( toolBarComponent == ToolBarComponent.GLUE )
		{
			component = Box.createHorizontalGlue();
		}
		else
		{
			throw new IllegalArgumentException( toolBarComponent.name() );
		}

		addToolbarComponent( component );
	}

	/**
	 * Adds a {@link Component} to the tool bar.
	 *
	 * @param component {@link Component} to add to tool bar.
	 */
	public void addToolbarComponent( @NotNull final Component component )
	{
		JToolBar toolBar = _toolBar;
		if ( toolBar == null )
		{
			toolBar = new JToolBar( SwingConstants.HORIZONTAL );
			toolBar.setFloatable( false );
			_toolBar = toolBar;

			add( toolBar, BorderLayout.NORTH );
		}

		toolBar.add( component );
	}

	/**
	 * Returns the {@link VirtualFileSystem} of the {@link Explorer}.
	 *
	 * @return the {@link VirtualFileSystem} of the {@link Explorer}.
	 */
	@NotNull
	public VirtualFileSystem getFileSystem()
	{
		return _fileSystem;
	}

	/**
	 * Sets the file system backing the explorer component.
	 *
	 * @param fileSystem File system to be set.
	 * @param baseFolder Folder to be used as root folder.
	 */
	public void setFileSystem( @NotNull final VirtualFileSystem fileSystem, @NotNull final String baseFolder )
	{
		_fileSystem = fileSystem;
		_baseFolder = baseFolder;

		setCurrentFolder( baseFolder, true );
	}
}
