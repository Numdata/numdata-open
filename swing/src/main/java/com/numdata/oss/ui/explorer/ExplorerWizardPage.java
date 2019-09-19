/*
 * Copyright (c) 2008-2019, Numdata BV, The Netherlands.
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
import java.util.*;
import javax.swing.*;

import com.numdata.oss.ui.*;
import com.numdata.oss.ui.explorer.Explorer.*;
import org.jetbrains.annotations.*;

/**
 * Wizard page that uses an {@link Explorer} component to allow the user to
 * select a file from a {@link VirtualFileSystem}.
 *
 * @author Gerrit Meinders
 */
public class ExplorerWizardPage
extends WizardPage
{
	/**
	 * Explorer component provided to select a file.
	 */
	@NotNull
	private final Explorer _explorer;

	/**
	 * Quick find component for filtering the explorer.
	 */
	@Nullable
	private QuickFindPanel _quickFind;

	/**
	 * Constructs an explorer wizard page based on a new in-memory file system
	 * with no current selection and the default tool bar components.
	 *
	 * @param wizard Wizard that controls this page.
	 * @param res    Resource bundle for this page (optional).
	 * @param name   Name and resource key of page (optional).
	 */
	public ExplorerWizardPage( @NotNull final Wizard wizard, final ResourceBundle res, final String name )
	{
		this( wizard, res, name, new MemoryFileSystem(), null );
	}

	/**
	 * Construct page.
	 *
	 * @param wizard            Wizard that controls this page.
	 * @param res               Resource bundle for this page (optional).
	 * @param name              Name and resource key of page (optional).
	 * @param fileSystem        File system to select a file from.
	 * @param initialSelection  Initially selected file, if any.
	 * @param toolBarComponents Indicates which components should be shown
	 *                          in the tool bar of the explorer component.
	 */
	public ExplorerWizardPage( @NotNull final Wizard wizard, final ResourceBundle res, final String name, @NotNull final VirtualFileSystem fileSystem, @Nullable final VirtualFile initialSelection, final ToolBarComponent... toolBarComponents )
	{
		super( wizard, res, name, new BorderLayout() );

		final Explorer explorer = new Explorer( fileSystem, "/", toolBarComponents );
		explorer.setLocale( wizard.getLocale() );
		_explorer = explorer;

		setSelectedFile( initialSelection );

		/*
		 * Update state of next and finish buttons.
		 */
		explorer.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				final StandardContentPane contentPane = wizard.getStandardContentPane();

				if ( e.getStateChange() == ItemEvent.SELECTED )
				{
					final VirtualFile selectedFile = getSelectedFile();
					final boolean fileSelected = ( selectedFile != null );
					final boolean lastPage = wizard.isLastPage( ExplorerWizardPage.this );

					contentPane.setButtonState( Wizard.NEXT_ACTION, fileSelected && !lastPage ? StandardContentPane.BUTTON_ENABLED : StandardContentPane.BUTTON_DISABLED );
					contentPane.setButtonState( Wizard.FINISH_ACTION, fileSelected ? StandardContentPane.BUTTON_ENABLED : StandardContentPane.BUTTON_DISABLED );
				}
				else
				{
					contentPane.setButtonState( Wizard.NEXT_ACTION, StandardContentPane.BUTTON_DISABLED );
					contentPane.setButtonState( Wizard.FINISH_ACTION, StandardContentPane.BUTTON_DISABLED );
				}
			}
		} );

		/*
		 * Allow for selection by means of a double-click.
		 */
		explorer.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final Wizard wizard = getWizard();
				final int pageCount = wizard.getPageCount();
				final WizardPage lastPage = wizard.getPage( pageCount - 1 );

				if ( ExplorerWizardPage.this == lastPage )
				{
					wizard.doFinish();
				}
				else
				{
					wizard.doNext();
				}
			}
		} );

		/*
		 * Update wizard title when current folder changes.
		 */
		explorer.addPropertyChangeListener( new PropertyChangeListener()
		{
			@Override
			public void propertyChange( final PropertyChangeEvent evt )
			{
				if ( Explorer.CURRENT_FOLDER_PROPERTY.equals( evt.getPropertyName() ) )
				{
					final Wizard wizard = getWizard();
					wizard.setTitle( getTitle() );
				}
			}
		} );

		final QuickFindPanel quickFind;
		final RowSorter<?> rowSorter = explorer.getRowSorter();
		if ( rowSorter instanceof DefaultRowSorter )
		{
			quickFind = new QuickFindPanel( wizard.getLocale(), (DefaultRowSorter<?, ?>)rowSorter );
			add( quickFind, BorderLayout.NORTH );
		}
		else
		{
			quickFind = null;
		}
		_quickFind = quickFind;

		add( explorer, BorderLayout.CENTER );
	}

	@Override
	protected void shown()
	{
		if ( _quickFind != null )
		{
			_quickFind.clear();
		}
		super.shown();
	}

	/**
	 * Returns the explorer component shown on the page.
	 *
	 * @return Explorer component.
	 */
	@NotNull
	protected Explorer getExplorer()
	{
		return _explorer;
	}

	/**
	 * Returns the quick find panel.
	 *
	 * @return Quick find panel.
	 */
	@Nullable
	protected QuickFindPanel getQuickFind()
	{
		return _quickFind;
	}

	/**
	 * Selects the specified file. If necessary, the current folder of the
	 * explorer is changed to the parent folder of the given file.
	 *
	 * @param selectedFile File to be selected.
	 */
	public void setSelectedFile( @Nullable final VirtualFile selectedFile )
	{
		final Explorer explorer = _explorer;

		// FIXME: The file system should provide a parent file, instead of using a hardcoded separator character.
		if ( selectedFile != null )
		{
			final String path = selectedFile.getPath();
			final int lastSeparator = path.lastIndexOf( (int)'/' );
			if ( lastSeparator > -1 )
			{
				explorer.setCurrentFolder( path.substring( 0, lastSeparator ) );
			}
		}

		explorer.setSelectedFile( selectedFile );
	}

	/**
	 * Returns the file that was selected by the user.
	 *
	 * @return Selected file.
	 */
	@Nullable
	public VirtualFile getSelectedFile()
	{
		return _explorer.getSelectedFile();
	}

	@Override
	public void doNext()
	{
		final VirtualFile selectedFile = getSelectedFile();

		if ( selectedFile != null )
		{
			if ( selectedFile.isDirectory() )
			{
				final Explorer explorer = getExplorer();
				explorer.setCurrentFolder( selectedFile.getPath() );
			}
			else
			{
				super.doNext();
			}
		}
	}
}
