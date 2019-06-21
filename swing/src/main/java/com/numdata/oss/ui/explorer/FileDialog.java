/*
 * Copyright (c) 2001-2019, Numdata BV, The Netherlands.
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
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

import com.numdata.oss.*;
import com.numdata.oss.ui.*;

/**
 * This class implements a save dialog that mimics the classic windows "Save As"
 * dialog for a {@link VirtualFileSystem}.
 *
 * This class also handles related UI stuff, like producing error messages on
 * bad input, confirmation dialog when overwriting files, etc.
 *
 * @author Peter S. Heijnen
 */
public final class FileDialog
extends JDialog
implements ActionListener
{
	/**
	 * Resource bundle for internationalization.
	 */
	private final ResourceBundle _res;

	private ImageIcon _folderIcon;

	private ImageIcon _cabinetIcon;

	private ImageIcon _cabinetIconLocked;

	private ImageIcon _layoutIcon;

	private ImageIcon _layoutIconLocked;

	private ImageIcon _shelvingIcon;

	private ImageIcon _shelvingIconLocked;

	private ImageIcon _binaryIcon;

	/**
	 * Flag to indicate that this is a 'SAVE' dialog ({@code true}) vs. an
	 * 'OPEN' dialog ({@code false}).
	 */
	private final boolean _isSave;

	/**
	 * Database services.
	 */
	private final VirtualFileSystem _fileSystem;

	/**
	 * Currently selected filename.
	 */
	private final String _filename;

	/**
	 * List of user's folders.
	 */
	private final List<VirtualFile> _folders;

	/**
	 * Path to the current folder.
	 */
	private String _currentFolder;

	/**
	 * List of entries in the current folder.
	 */
	private List<VirtualFile> _folderContent;

	/**
	 * Choice box with folder choices upto the current folder.
	 */
	private JComboBox _folderChoice;

	/**
	 * Table component used to list contents of the current folder.
	 */
	private JTable _fileTable;

	/**
	 * Table model for folder contents.
	 */
	private final AbstractTableModel _contentModel = new AbstractTableModel()
	{
		public int getColumnCount()
		{
			return 3;
		}

		public int getRowCount()
		{
			return ( _folderContent == null ) ? 0 : _folderContent.size();
		}

		public Class<?> getColumnClass( final int columnIndex )
		{
			return ( columnIndex == 0 ) ? ImageIcon.class : String.class;
		}

		public String getColumnName( final int columnIndex )
		{
			final String result;
			switch ( columnIndex )
			{
				case 0:
					result = "";
					break;
				case 1:
					result = _res.getString( "name" );
					break;
				case 2:
					result = _res.getString( "type" );
					break;
				default:
					result = null;
			}
			return result;
		}

		public Object getValueAt( final int rowIndex, final int columnIndex )
		{
			final VirtualFile d = _folderContent.get( rowIndex );
			final String name = d.getName();
			final String type = d.getType();

			final Object result;
			switch ( columnIndex )
			{
				case 0: /* icon */
					if ( d.isDirectory() )
					{
						result = _folderIcon;
					}
					else if ( "binary".equalsIgnoreCase( type ) )
					{
						result = _binaryIcon;
					}
					else if ( "cabinet".equalsIgnoreCase( type ) )
					{
						result = d.canWrite() ? _cabinetIcon : _cabinetIconLocked;
					}
					else if ( "shelving".equalsIgnoreCase( type ) )
					{
						result = d.canWrite() ? _shelvingIcon : _shelvingIconLocked;
					}
					else if ( "layout".equalsIgnoreCase( type ) )
					{
						result = d.canWrite() ? _layoutIcon : _layoutIconLocked;
					}
					else
					{
						result = null;
					}
					break;

				case 1: /* name */
					result = ( d.isDirectory() || d.canWrite() ) ? name : name + ' ' + _res.getString( "readOnly" );
					break;


				case 2: /* type */
					result = ResourceBundleTools.getString( _res, d.isDirectory() ? "folder" : type, _res.getString( "unknownType" ) );
					break;

				default:
					result = null;
			}
			return result;
		}
	};

	/**
	 * Text field to allow user to enter a filename.
	 */
	private JTextField _filenameField;

	/**
	 * Accessory component (used for previews).
	 */
	private Component _accessory;

	/**
	 * Flag to indicate that the save operation was cancelled.
	 */
	private boolean _isCancelled;

	/**
	 * If not {@code null}, then only files of this type are listed, and an
	 * opened file should be of this type.
	 */
	private final String _typeFilter;

	/**
	 * Construct file dialog.
	 *
	 * @param owner         Owner dialog for dialog.
	 * @param locale        Locale for internationalization.
	 * @param isSave        {@code true} => this is a 'SAVE' dialog; {@code
	 *                      false} => this is an 'OPEN' dialog.
	 * @param fileSystem    File system to use.
	 * @param rootFolder    Root folder to list.
	 * @param initialFolder Initial folder to open.
	 * @param filename      Initial filename to select.
	 * @param typeFilter    If not {@code null}, then only files of this type
	 *                      are listed, and an opened file should be of this
	 *                      type.
	 * @param accessory     Accessory component (optional, used for previews).
	 *
	 * @throws IOException if the file dialog could not be opened due to an I/O
	 * error.
	 */
	public FileDialog( final Window owner, final Locale locale, final boolean isSave, final VirtualFileSystem fileSystem, final String rootFolder, final String initialFolder, final String filename, final String typeFilter, final Component accessory )
	throws IOException
	{
		super( owner, DEFAULT_MODALITY_TYPE );

		_res = ResourceBundleTools.getBundle( FileDialog.class, locale );
		_isSave = isSave;
		_fileSystem = fileSystem;

		final List<VirtualFile> folders = fileSystem.getFolders( rootFolder );
		if ( TextTools.endsWith( initialFolder, '/' ) && !initialFolder.startsWith( rootFolder ) )
		{
			for ( int slash = initialFolder.indexOf( '/' ); slash >= 0; slash = initialFolder.indexOf( '/', slash + 1 ) )
			{
				folders.add( fileSystem.getFile( initialFolder.substring( 0, slash + 1 ) ) );
			}
		}

		_folders = folders;
		_currentFolder = TextTools.isEmpty( initialFolder ) && !folders.isEmpty() ? folders.get( 0 ).getPath() : initialFolder;
		_filename = ( filename == null ) ? "" : filename;
		_typeFilter = typeFilter;
		_folderContent = null;

		_isCancelled = true;

		_accessory = accessory;

		initGui();
	}

	/**
	 * Construct file dialog.
	 *
	 * @param owner         Owner frame for dialog.
	 * @param locale        Locale for internationalization.
	 * @param isSave        {@code true} => this is a 'SAVE' dialog; {@code
	 *                      false} => this is an 'OPEN' dialog.
	 * @param fileSystem    File system to use.
	 * @param rootFolder    Root folder to list.
	 * @param initialFolder Initial folder to open.
	 * @param filename      Initial filename to select.
	 * @param typeFilter    If not {@code null}, then only files of this type
	 *                      are listed, and an opened file should be of this
	 *                      type.
	 * @param accessory     Accessory component (optional, used for previews).
	 *
	 * @throws IOException if the file dialog could not be opened due to an I/O
	 * error.
	 */
	public FileDialog( final Frame owner, final Locale locale, final boolean isSave, final VirtualFileSystem fileSystem, final String rootFolder, final String initialFolder, final String filename, final String typeFilter, final Component accessory )
	throws IOException
	{
		this( (Window)owner, locale, isSave, fileSystem, rootFolder, initialFolder, filename, typeFilter, accessory );
	}

	/**
	 * Initialize GUI of dialog.
	 */
	private void initGui()
	{
		_folderIcon = ImageTools.getImageIcon( getClass(), "/icons/folderClosed-16x16.gif" );
		_cabinetIcon = ImageTools.getImageIcon( getClass(), "/product-configurator/cabinet-16x16.gif" );
		_cabinetIconLocked = ImageTools.getImageIcon( getClass(), "/product-configurator/cabinetLocked-16x16.gif" );
		_layoutIcon = ImageTools.getImageIcon( getClass(), "/product-configurator/layout-16x16.gif" );
		_layoutIconLocked = ImageTools.getImageIcon( getClass(), "/product-configurator/layoutLocked-16x16.gif" );
		_shelvingIcon = ImageTools.getImageIcon( getClass(), "/product-configurator/shelving-16x16.gif" );
		_shelvingIconLocked = ImageTools.getImageIcon( getClass(), "/product-configurator/shelvingLocked-16x16.gif" );
		_binaryIcon = ImageTools.getImageIcon( getClass(), "/product-configurator/binary-16x16.gif" );

		setTitle( _res.getString( _isSave ? "saveTitle" : "openTitle" ) );

//		setBackground( Color.getColor( "Dialog" ) );

		final JPanel content = new JPanel( new BorderLayout() );
		content.setBorder( BorderFactory.createEmptyBorder( 0, 6, 0, 6 ) );
		setContentPane( content );

		/*
		 * Create 'folder context panel'
		 */
		final JPanel contextPanel = new JPanel( new GridBagLayout() );
		content.add( contextPanel, BorderLayout.NORTH );
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets.top = 4;
		gbc.insets.bottom = 4;

		final JLabel saveInLabel = new JLabel( _res.getString( _isSave ? "saveIn" : "openFrom" ) );
		contextPanel.add( saveInLabel, gbc );

		final JComboBox folderChoice = new JComboBox();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		contextPanel.add( folderChoice, gbc );
		gbc.weightx = 0.0;
		gbc.fill = GridBagConstraints.NONE;

		for ( int i = 0; i < _folders.size(); i++ )
		{
			final VirtualFile folder = _folders.get( i );
			folderChoice.addItem( ( i == 0 ) ? ( folder.getPath() + " (" + _res.getString( "homeFolder" ) + ')' ) : ( folder.getPath() ) );
		}

		folderChoice.setActionCommand( "folder" );
		folderChoice.addActionListener( this );
		_folderChoice = folderChoice;

		final JButton folderUpButton = new JButton( ImageTools.getImageIcon( getClass(), "/icons/folderUp-16x16.gif" ) );
		folderUpButton.setBorder( null );
		folderUpButton.setActionCommand( "folderUp" );
		folderUpButton.addActionListener( this );
		contextPanel.add( folderUpButton, gbc );

		/*
		 * Create 'folder listing' (containing a list of files)
		 */
		final JTable fileTable = new JTable( _contentModel );
		fileTable.addMouseListener( new MouseAdapter()
		{
			public void mouseClicked( final MouseEvent e )
			{
				final VirtualFile file = getFile();
				if ( file != null )
				{
					final int clickCount = e.getClickCount();
					if ( file.isDirectory() )
					{
						if ( clickCount == 2 )
						{
							final String oldPath = null; // Too late to get it now.
							updateFiles( file.getPath() );
							firePropertyChange( "path", oldPath, getPath() );
						}
					}
					else
					{
						final String oldPath = null; // Too late to get it now.
						_filenameField.setText( file.getName() );
						firePropertyChange( "path", oldPath, getPath() );

						if ( !_isSave && ( clickCount == 2 ) )
						{
							confirm();
						}
					}
				}
			}
		} );

		final ListSelectionModel selectionModel = fileTable.getSelectionModel();
		selectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		fileTable.setCellSelectionEnabled( false );
		fileTable.setRowSelectionAllowed( true );
		fileTable.setFocusable( false );
		fileTable.setShowGrid( false );
		_fileTable = fileTable;

		final JScrollPane scrollPane = new JScrollPane( fileTable );
		scrollPane.setBackground( fileTable.getBackground() );
		content.add( scrollPane, BorderLayout.CENTER );

		/*
		 * Add optional accessory component.
		 */
		final Component accessory = _accessory;
		if ( accessory != null )
		{
			content.add( accessory, BorderLayout.EAST );
			if ( accessory instanceof PropertyChangeListener )
			{
				addPropertyChangeListener( "path", (PropertyChangeListener)accessory );
			}
		}

		/*
		 * Create 'filename panel' (containing the filename input field and save/cancel buttons).
		 */
		final JPanel filenamePanel = new JPanel( new GridBagLayout() );
		content.add( filenamePanel, BorderLayout.SOUTH );

		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.weightx = 0.0;
		gbc.insets.left = 0;
		gbc.insets.right = 0;
		gbc.insets.top = 8;
		gbc.insets.bottom = 8;

		final JLabel filenameLabel = new JLabel( _res.getString( "filename" ) );
		filenamePanel.add( filenameLabel, gbc );

		gbc.anchor = GridBagConstraints.NORTH;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 2;
		gbc.weightx = 1.0;
		gbc.insets.left = 0;
		gbc.insets.right = 4;
		gbc.insets.top = 8;
		gbc.insets.bottom = 8;

		final JTextField filenameField = new JTextField( Math.max( _filename.length(), 40 ) );
		filenamePanel.add( filenameField, gbc );
		filenameField.addActionListener( this );
		_filenameField = filenameField;

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weightx = 0.0;
		gbc.insets.left = 4;
		gbc.insets.right = 0;
		gbc.insets.top = 8;
		gbc.insets.bottom = 2;

		final JButton confirmButton = new JButton( "  " + _res.getString( _isSave ? "save" : "open" ) + "  " );
		filenamePanel.add( confirmButton, gbc );
		confirmButton.setActionCommand( "confirm" );
		confirmButton.addActionListener( this );

		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets.left = 4;
		gbc.insets.right = 0;
		gbc.insets.top = 2;
		gbc.insets.bottom = 8;

		final JButton cancelButton = new JButton( "  " + _res.getString( "cancel" ) + "  " );
		filenamePanel.add( cancelButton, gbc );
		cancelButton.setActionCommand( "cancel" );
		cancelButton.addActionListener( this );

		WindowTools.packAndCenter( this, 600, 400 );
		addWindowListener( new WindowAdapter()
		{
			public void windowOpened( final WindowEvent event )
			{
				filenameField.setText( _filename );
				updateFiles( _currentFolder );
				firePropertyChange( "path", null, getPath() );
				invalidate();
			}
		} );
	}

	/**
	 * Get parent folder for the specified path.
	 *
	 * @param path Path to file or directory whose parent folder to get.
	 *
	 * @return Parent folder from path.
	 */
	public static String getParent( final String path )
	{
		final int i1 = path.indexOf( (int)'/' );
		final int i2 = path.lastIndexOf( (int)'/', path.length() - 2 );

		return path.substring( 0, ( i2 <= i1 ) ? i1 + 1 : i2 );
	}

	/**
	 * Test if dialog was 'cancelled'. This value can not be relied upon until
	 * the dialog has been disposed.
	 *
	 * @return {@code true} if dialog was cancelled; {@code false} otherwise.
	 */
	public boolean isCancelled()
	{
		return _isCancelled;
	}

	/**
	 * Returns the currently selected {@link VirtualFile}.
	 *
	 * @return Currently selected {@link VirtualFile}; {@code null} if no file
	 * is selected.
	 */
	public VirtualFile getFile()
	{
		final VirtualFile result;

		final int selectedRow = _fileTable.getSelectedRow();
		final List<VirtualFile> folderContent = _folderContent;

		if ( ( folderContent != null ) && ( ( selectedRow >= 0 ) && ( selectedRow < folderContent.size() ) ) )
		{
			result = folderContent.get( selectedRow );
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * Get path to the selected file.
	 *
	 * @return Path to the selected file; {@code null} after dialog has been
	 * cancelled.
	 *
	 * @see #getFileType
	 */
	public String getPath()
	{
		final String result;
		if ( !isVisible() && isCancelled() )
		{
			result = null;
		}
		else
		{
			result = _fileSystem.getPath( _currentFolder, _filenameField.getText() );
		}
		return result;
	}

	/**
	 * Returns the type of the selected file.
	 *
	 * @return Type of the selected file; {@code null} if no file is selected.
	 *
	 * @see VirtualFile#getType()
	 * @see #getPath
	 */
	public String getFileType()
	{
		final VirtualFile file = getFile();
		return ( file != null ) ? file.getType() : null;
	}

	/**
	 * Handle action events from various components of the save dialog.
	 *
	 * @param event Action event.
	 */
	public void actionPerformed( final ActionEvent event )
	{
		final String ac = event.getActionCommand();

		if ( "folder".equals( ac ) )
		{
			final int folderIndex = _folderChoice.getSelectedIndex();
			final VirtualFile folder = _folders.get( folderIndex );

			String path = folder.getPath();
			if ( !TextTools.endsWith( path, '/' ) )
			{
				path += '/';
			}

			if ( !path.equals( _currentFolder ) )
			{
				updateFiles( path );
			}
		}
		else if ( "folderUp".equals( ac ) )
		{
			updateFiles( getParent( _currentFolder ) );
		}
		else if ( "newFolder".equals( ac ) )
		{
		}
		else if ( "cancel".equals( ac ) )
		{
			_isCancelled = true;
			dispose();
		}
		else if ( "confirm".equals( ac ) || ( event.getSource() == _filenameField ) )
		{
			confirm();
		}
	}

	private void confirm()
	{
		if ( validateFilename() )
		{
			_isCancelled = false;
			dispose();
		}
	}

	/**
	 * Validate the entered file name. Possible errors:
	 *
	 * <ul>
	 *
	 * <li>Common: no filename entered ==> errorEmptyFilename</li>
	 *
	 * <li>Common: filename has bad characters: / \ : * ? " ' < > | ==>
	 * errorBadFilename</li>
	 *
	 * <li>Open: file not found ==> errorNotFound</li>
	 *
	 * <li>Save: existing locked file ==> errorLocked</li>
	 *
	 * <li>Save: existing file with other ID ==> confirmOverwrite</li>
	 *
	 * </ul>
	 *
	 * @return {@code true} if successful; {@code false} otherwise.
	 */
	private boolean validateFilename()
	{
		final boolean result;

		final ResourceBundle res = _res;
		final boolean isSave = _isSave;

		/*
		 * Check filename
		 */
		final String text = _filenameField.getText();
		final String filename = text.trim();

		if ( filename.length() == 0 )
		{
			final VirtualFile file = getFile();

			if ( ( file != null ) && ( file.isDirectory() ) )
			{
				final String oldPath = getPath();
				updateFiles( file.getPath() );
				firePropertyChange( "path", oldPath, getPath() );
			}
			else
			{
				JOptionPane.showMessageDialog( getParent(),
				                               res.getString( "errorEmptyFilename" + filename ),
				                               res.getString( isSave ? "saveErrorTitle" : "openErrorTitle" ),
				                               JOptionPane.ERROR_MESSAGE );
			}

			result = false;
		}

		else if ( filename.indexOf( (int)'/' ) >= 0
		          || filename.indexOf( (int)'\\' ) >= 0
		          || filename.indexOf( (int)':' ) >= 0
		          || filename.indexOf( (int)'*' ) >= 0
		          || filename.indexOf( (int)'?' ) >= 0
		          || filename.indexOf( (int)'"' ) >= 0
		          || filename.indexOf( (int)'\'' ) >= 0
		          || filename.indexOf( (int)'<' ) >= 0
		          || filename.indexOf( (int)'>' ) >= 0
		          || filename.indexOf( (int)'|' ) >= 0 )
		{
			JOptionPane.showMessageDialog( getParent(),
			                               MessageFormat.format( res.getString( "errorBadFilename" ), filename ),
			                               res.getString( isSave ? "saveErrorTitle" : "openErrorTitle" ),
			                               JOptionPane.ERROR_MESSAGE );

			result = false;
		}
		else
		{
			/*
			 * Find existing entry (may be same as we had)
			 */
			VirtualFile existing = null;
			final List<VirtualFile> folderContent = _folderContent;
			if ( folderContent != null )
			{
				for ( final VirtualFile file : folderContent )
				{
					if ( filename.equalsIgnoreCase( file.getName() ) )
					{
						existing = file;
						break;
					}
				}
			}

			if ( isSave )
			{
				/*
				 * Check if we have an existing locked file.
				 */
				if ( ( existing != null ) && !existing.canWrite() )
				{
					JOptionPane.showMessageDialog( getParent(),
					                               MessageFormat.format( res.getString( "errorLocked" ), filename ),
					                               res.getString( "saveErrorTitle" ),
					                               JOptionPane.ERROR_MESSAGE );

					result = false;
				}

				/*
				 * Check if we have an existing file with a different ID. If so, ask user for overwrite confirmation.
				 */
				else
				{
					result = ( existing == null ) || ( JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog( getParent(),
					                                                                                            MessageFormat.format( res.getString( "confirmOverwriteMessage" ), filename ),
					                                                                                            res.getString( "confirmOverwriteTitle" ),
					                                                                                            JOptionPane.YES_NO_OPTION ) );
				}
			}
			else /* open */
			{
				if ( existing == null )
				{
					/*
					 * @FIXME Should (should we?) handle relative filenames here
					 */
					JOptionPane.showMessageDialog( getParent(),
					                               MessageFormat.format( res.getString( "errorNotFound" ), filename ),
					                               res.getString( "openErrorTitle" ),
					                               JOptionPane.ERROR_MESSAGE );

					result = false;
				}
				else
				{
					result = true;
				}
			}
		}

		return result;
	}

	/**
	 * This method updates the contents of the folder choice box and file list
	 * based on the currently selected folder.
	 *
	 * @param path Path to folder.
	 */
	private void updateFiles( final String path )
	{
		final VirtualFileSystem fileSystem = _fileSystem;

		if ( ( _folderContent == null ) || !TextTools.equals( fileSystem.getPath( _currentFolder, "x" ), fileSystem.getPath( path, "x" ) ) )
		{
			try
			{
				final JComboBox folderChoice = _folderChoice;
				final JTable fileTable = _fileTable;
				final JTextField filenameField = _filenameField;

				final List<VirtualFile> folderContent = fileSystem.getFolderContents( path );

				for ( Iterator<VirtualFile> i = folderContent.iterator(); i.hasNext(); )
				{
					final VirtualFile file = i.next();
					if ( !file.isDirectory() && ( _typeFilter != null ) && !_typeFilter.equalsIgnoreCase( file.getType() ) )
					{
						i.remove();
					}
				}

				_currentFolder = path;
				_folderContent = folderContent;

				/*
				 * Add files in the selected folder to the table.
				 */
				final AbstractTableModel contentModel = _contentModel;

				int nameWidth = 100;
				int typeWidth = 100;

				final FontMetrics fm = getFontMetrics( fileTable.getFont() );
				if ( fm != null )
				{
					for ( int i = 0; i < folderContent.size(); i++ )
					{
						nameWidth = Math.max( nameWidth, fm.stringWidth( (String)contentModel.getValueAt( i, 1 ) ) );
						typeWidth = Math.max( typeWidth, fm.stringWidth( (String)contentModel.getValueAt( i, 2 ) ) );
					}
				}

				final TableColumnModel columnModel = fileTable.getColumnModel();
				final TableColumn column1 = columnModel.getColumn( 1 );
				final TableColumn column2 = columnModel.getColumn( 2 );
				final TableColumn column0 = columnModel.getColumn( 0 );

				column0.setPreferredWidth( 16 + 8 );
				column1.setPreferredWidth( nameWidth + 8 );
				column2.setPreferredWidth( typeWidth + 8 );
				fileTable.setRowHeight( 20 );

				/*
				 * Request focus for filename field.
				 */
				fileTable.invalidate();
				contentModel.fireTableDataChanged();
				fileTable.repaint();

				int index = 0;
				for ( final VirtualFile aFolder : _folders )
				{
					if ( TextTools.equals( fileSystem.getPath( path, "x" ), fileSystem.getPath( aFolder.getPath(), "x" ) ) )
					{
						folderChoice.setSelectedIndex( index );
						break;
					}
					index++;
				}

				filenameField.requestFocus();
			}
			catch ( IOException e )
			{
				e.printStackTrace();
			}
		}
	}
}
