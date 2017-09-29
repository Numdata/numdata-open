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
package com.numdata.microeditor;

import java.awt.Dimension;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

import com.numdata.oss.*;
import com.numdata.oss.ui.*;
import com.numdata.printer.*;

/**
 * Minimalistic text editor implementation.
 *
 * It is currently used to edit resource bundles.
 *
 * @author Peter S. Heijnen
 */
public final class MicroTextEditor
extends JDialog
{
	/**
	 * Name of this program.
	 */
	private static final String _programTitle = "Text Editor";

	/**
	 * Currently edited file.
	 */
	private File _file;

	/**
	 * Last known contents of file.
	 */
	private String _fileContent;

	/**
	 * The text area actually implements all the editing functionality.
	 */
	private final JTextArea _textArea;

	/**
	 * Search string used by {@link #doFind} and {@link #doFindNext}.
	 */
	private String _searchFor;

	/**
	 * Manager for undo/redo actions.
	 */
	private final UndoManager _undoManager;

	/**
	 * Action that represents the 'Undo' function.
	 */
	private final UndoAction _undoAction;

	/**
	 * Action that represents the 'Redo' function.
	 */
	private final RedoAction _redoAction;

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		new MicroTextEditor( null, null, null, true );
	}

	/**
	 * Construct editor.
	 *
	 * @param fixedFile If set, open editor just for this file.
	 */
	public MicroTextEditor( final File fixedFile, final String fixedText, final Font font, final boolean editable )
	{
		super( JOptionPane.getRootFrame(), _programTitle, true );

		final boolean isFixed = ( fixedFile != null ) || ( fixedText != null );

		_file = fixedFile;
		_fileContent = null;
		_searchFor = null;

		_undoManager = new UndoManager();
		_undoAction = new UndoAction();
		_redoAction = new RedoAction();

		initMenu( ( fixedText == null ), !isFixed, editable );

		final JTextArea textArea = new JTextArea();
		textArea.setLineWrap( false );
		textArea.setEditable( editable );
		_textArea = textArea;
		if ( font != null )
		{
			textArea.setFont( font );
		}

		final Document document = textArea.getDocument();
		document.addUndoableEditListener( new UndoableEditListener()
		{
			public void undoableEditHappened( final UndoableEditEvent e )
			{
				_undoManager.addEdit( e.getEdit() );
				_undoAction.update();
				_redoAction.update();
			}
		} );

		if ( fixedText != null )
		{
			setText( fixedFile, fixedText );
		}
		else if ( fixedFile != null )
		{
			loadText( fixedFile );
		}

		setContentPane( new JScrollPane( textArea ) );

		setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			public void windowClosing( final WindowEvent e )
			{
				doExit();
			}
		} );

		final Toolkit toolkit = getToolkit();
		final Dimension ss = toolkit.getScreenSize();
		setBounds( 30, 75, ss.width - 60, ss.height - 150 );

		setVisible( true );
	}

	/**
	 * Initialize the editor menu.
	 *
	 * @param allowOtherFile If set (normal operation), allow files to be opened
	 *                       and saved under a different name; if not set (fixed
	 *                       file), only a single file can be edited.
	 */
	private void initMenu( final boolean allowFile, final boolean allowOtherFile, final boolean editable )
	{
		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar( menuBar );

		JMenu menu;
		JMenuItem item;

		menuBar.add( menu = new JMenu( "File" ) );
		menu.setMnemonic( 'f' );

		if ( allowFile )
		{
			if ( allowOtherFile )
			{
				menu.add( item = new JMenuItem( "doNew" ) );
				item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_N, ActionEvent.CTRL_MASK ) );
				item.addActionListener( new ActionListener()
				{
					public void actionPerformed( final ActionEvent ae )
					{
						doNew();
					}
				} );

				menu.add( item = new JMenuItem( "Open" ) );
				item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_O, ActionEvent.CTRL_MASK ) );
				item.addActionListener( new ActionListener()
				{
					public void actionPerformed( final ActionEvent ae )
					{
						doOpen();
					}
				} );
			}

			menu.add( item = new JMenuItem( "Save" ) );
			item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, ActionEvent.CTRL_MASK ) );
			item.addActionListener( new ActionListener()
			{
				public void actionPerformed( final ActionEvent ae )
				{
					doSave();
				}
			} );

			if ( allowOtherFile )
			{
				menu.add( item = new JMenuItem( "Save As" ) );
				item.addActionListener( new ActionListener()
				{
					public void actionPerformed( final ActionEvent ae )
					{
						doSaveAs();
					}
				} );
			}

			menu.addSeparator();
		}

		menu.add( item = new JMenuItem( "Print" ) );
		item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_P, ActionEvent.CTRL_MASK ) );
		item.addActionListener( new ActionListener()
		{
			public void actionPerformed( final ActionEvent ae )
			{
				doPrint();
			}
		} );

		menu.addSeparator();

		menu.add( item = new JMenuItem( "Exit" ) );
		item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F4, ActionEvent.CTRL_MASK ) );
		item.addActionListener( new ActionListener()
		{
			public void actionPerformed( final ActionEvent ae )
			{
				doExit();
			}
		} );

		if ( editable )
		{
			menuBar.add( menu = new JMenu( "Edit" ) );
		}
		menu.setMnemonic( 'e' );

		menu.add( _undoAction );
		_undoAction.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK ) );

		menu.add( _redoAction );
		_redoAction.putValue( Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke( KeyEvent.VK_Z, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK ) );

		menu.addSeparator();

		if ( editable )
		{
			menu.add( item = new JMenuItem( "Cut" ) );
			item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK ) );
			item.addActionListener( new ActionListener()
			{
				public void actionPerformed( final ActionEvent ae )
				{
					doCut();
				}
			} );
		}

		menu.add( item = new JMenuItem( "doCopy" ) );
		item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK ) );
		item.addActionListener( new ActionListener()
		{
			public void actionPerformed( final ActionEvent ae )
			{
				doCopy();
			}
		} );

		if ( editable )
		{
			menu.add( item = new JMenuItem( "Paste" ) );
			item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK ) );
			item.addActionListener( new ActionListener()
			{
				public void actionPerformed( final ActionEvent ae )
				{
					doPaste();
				}
			} );
		}

		menu.addSeparator();

		menu.add( item = new JMenuItem( "Find" ) );
		item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F, ActionEvent.CTRL_MASK ) );
		item.addActionListener( new ActionListener()
		{
			public void actionPerformed( final ActionEvent ae )
			{
				doFind();
			}
		} );

		menu.add( item = new JMenuItem( "Find Next" ) );
		item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_F3, ActionEvent.CTRL_MASK ) );
		item.addActionListener( new ActionListener()
		{
			public void actionPerformed( final ActionEvent ae )
			{
				doFindNext();
			}
		} );

		menu.addSeparator();

		menu.add( item = new JMenuItem( "Select All" ) );
		item.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_A, ActionEvent.CTRL_MASK ) );
		item.addActionListener( new ActionListener()
		{
			public void actionPerformed( final ActionEvent ae )
			{
				doSelectAll();
			}
		} );
	}

	/**
	 * Load text from file into editor.
	 *
	 * @param file File to load.
	 */
	private void loadText( final File file )
	{
		final String text = TextTools.loadText( file );
		if ( text == null )
		{
			WindowTools.showErrorDialog( null, "Error", "An I/O failure occurred during the operation." );
		}

		setText( file, text );

	}

	private void setText( final File file, final String text )
	{
		_textArea.setText( text );

		_file = file;
		_fileContent = text;

		setTitle( ( ( file == null ) ? "Untitled" : file.getName() ) + " - " + _programTitle );
	}

	/**
	 * Save text from editor into file.
	 *
	 * @param file File to save.
	 */
	private void saveText( final File file )
	{
		File actualFile = _file;
		if ( actualFile != null )
		{
			try
			{
				final FileWriter os = new FileWriter( file );
				final String text = _textArea.getText();
				try
				{
					os.write( text );

					_file = file;
					_fileContent = text;

					actualFile = file;
				}
				finally
				{
					os.close();
				}
			}
			catch ( IOException e )
			{
				WindowTools.showErrorDialog( null, "Error", "An I/O failure occurred during the operation.\n\n" + e );
			}
		}

		setTitle( ( ( actualFile == null ) ? "Untitled" : actualFile.getName() ) + " - " + _programTitle );
	}

	/**
	 * Action implementation for 'Undo' action.
	 */
	final class UndoAction
	extends AbstractAction
	{
		/**
		 * Create 'Redo' action.
		 */
		UndoAction()
		{
			super( "Undo" );
			setEnabled( false );
		}

		public void actionPerformed( final ActionEvent e )
		{
			try
			{
				_undoManager.undo();
			}
			catch ( CannotUndoException ex )
			{
				WindowTools.showErrorDialog( null, "Undo failed", "Could not undo the last action." );
			}
			update();
			_redoAction.update();
		}

		/**
		 * Update state of 'Undo' action.
		 */
		void update()
		{
			if ( _undoManager.canUndo() )
			{
				setEnabled( true );
				putValue( "Undo", _undoManager.getUndoPresentationName() );
			}
			else
			{
				setEnabled( false );
				putValue( Action.NAME, "Undo" );
			}
		}
	}

	/**
	 * Action implementation for 'Redo' action.
	 */
	final class RedoAction
	extends AbstractAction
	{
		/**
		 * Create 'Redo' action.
		 */
		RedoAction()
		{
			super( "Redo" );
			setEnabled( false );
		}

		public void actionPerformed( final ActionEvent e )
		{
			try
			{
				_undoManager.redo();
			}
			catch ( CannotRedoException ex )
			{
				WindowTools.showErrorDialog( null, "Redo failed", "Could not redo the last action." );
			}
			update();
			_undoAction.update();
		}

		/**
		 * Update state of 'Redo' action.
		 */
		void update()
		{
			if ( _undoManager.canRedo() )
			{
				setEnabled( true );
				putValue( "Redo", _undoManager.getRedoPresentationName() );
			}
			else
			{
				setEnabled( false );
				putValue( Action.NAME, "Redo" );
			}
		}
	}

	/**
	 * Do 'New' action.
	 */
	public void doNew()
	{
		final String text = _textArea.getText();
		if ( ( text.length() > 0 ) && !text.equals( _fileContent ) )
		{
			final String message = "Do you want to save the changes ?";
			switch ( WindowTools.askConfirmationDialog( null, message, message ) )
			{
				case WindowTools.YES:
					doSave();
					_textArea.setText( "" );
					break;

				case WindowTools.NO:
					_textArea.setText( "" );
					break;

				default:
					break;
			}
		}
		else
		{
			_textArea.setText( "" );
		}

		setTitle( "Untitled - " + _programTitle );
	}

	/**
	 * Do 'Open..' action.
	 */
	public void doOpen()
	{
		boolean perform = true;

		final String text = _textArea.getText();
		if ( ( text.length() > 0 ) && !text.equals( _fileContent ) )
		{
			final String message = "Do you want to save the changes?";
			switch ( WindowTools.askConfirmationDialog( null, message, message ) )
			{
				case WindowTools.YES:
					doSave();
					break;

				case WindowTools.NO:
					break;

				default:
					perform = false;
					break;
			}
		}

		if ( perform )
		{
			final JFileChooser jfc = new JFileChooser( _file );
			if ( jfc.showOpenDialog( this ) == JFileChooser.APPROVE_OPTION )
			{
				loadText( jfc.getSelectedFile() );
			}
		}
	}

	/**
	 * Do 'Save' action.
	 */
	public void doSave()
	{
		if ( _file != null )
		{
			saveText( _file );
		}
		else
		{
			doSaveAs();
		}
	}

	/**
	 * Do 'Save As...' action.
	 */
	public void doSaveAs()
	{
		final JFileChooser jfc = new JFileChooser( _file );
		if ( jfc.showOpenDialog( this ) == JFileChooser.APPROVE_OPTION )
		{
			saveText( jfc.getSelectedFile() );
		}
	}

	/**
	 * Do 'Print' action.
	 */
	public void doPrint()
	{
		PrintTools.printText( _textArea.getText() );
	}

	/**
	 * Do 'Exit' action.
	 */
	public void doExit()
	{
		boolean perform = true;
		final String text = _textArea.getText();
		if ( ( text.length() > 0 ) && !text.equals( _fileContent ) )
		{
			final String message = "Do you want to save the changes?";
			switch ( WindowTools.askConfirmationDialog( null, message, message ) )
			{
				case WindowTools.YES:
					doSave();
					break;

				case WindowTools.NO:
					break;

				default:
					perform = false;
					break;
			}
		}

		if ( perform )
		{
			dispose();
			// System.exit( 0 );
		}
	}

	/**
	 * Do 'Cut' action.
	 */
	public void doCut()
	{
		_textArea.cut();
	}

	/**
	 * Do 'Copy' action.
	 */
	public void doCopy()
	{
		_textArea.copy();
	}

	/**
	 * Do 'Paste' action.
	 */
	public void doPaste()
	{
		_textArea.paste();
	}

	/**
	 * Do 'Select All' action.
	 */
	public void doSelectAll()
	{
		_textArea.selectAll();
	}

	/**
	 * Do 'Find' action.
	 */
	public void doFind()
	{
		String searchFor = _searchFor;
		while ( true )
		{
			searchFor = JOptionPane.showInputDialog( "Type the text to find", searchFor );
			if ( ( searchFor == null ) || ( searchFor.length() == 0 ) )
			{
				break;
			}

			_searchFor = searchFor;

			final String text = _textArea.getText();
			final int foundAt = text.indexOf( searchFor );
			if ( foundAt < 0 )
			{
				WindowTools.showWarningDialog( null, "Not found", "Could not find the entered text!" );
			}
			else
			{
				_textArea.select( foundAt, foundAt + searchFor.length() );
				break;
			}
		}
	}

	/**
	 * Do 'Find Next' action.
	 */
	public void doFindNext()
	{
		final String searchFor = _searchFor;
		if ( ( searchFor != null ) && ( searchFor.length() > 0 ) )
		{
			final String text = _textArea.getText();

			int foundAt = text.indexOf( searchFor, _textArea.getSelectionEnd() );
			if ( foundAt < 0 )
			{
				foundAt = text.indexOf( searchFor );
			}

			_textArea.select( foundAt, foundAt + searchFor.length() );
		}
	}
}
