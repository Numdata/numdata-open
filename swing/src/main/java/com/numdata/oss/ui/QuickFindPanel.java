/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.*;
import javax.swing.text.*;

import com.numdata.oss.*;

/**
 * Quick find component that can be used to filter the contents of any component
 * that accepts a {@link DefaultRowSorter}.
 *
 * @author G. Meinders
 */
public class QuickFindPanel
	extends JPanel
	implements DocumentListener, ActionListener
{
	/**
	 * Row sorter to perform quick find with.
	 */
	private DefaultRowSorter<?,?> _sorter;

	/**
	 * Original row filter that was assigned to the current row sorter.
	 */
	private RowFilter<?,?> _originalRowFilter;

	/**
	 * Label that is put in front of the text field.
	 */
	private final JLabel _label;

	/**
	 * Text field that the quick find query is entered into.
	 */
	private final JTextField _field;

	/**
	 * Performs delayed updates of the row filters.
	 */
	private final Timer _timer;

	/**
	 * Constructs a new quick find panel that operates on the given table
	 * row sorter.
	 *
	 * @param   locale  Locale to use.
	 * @param   sorter  Table row sorter to perform quick find on.
	 */
	public QuickFindPanel( final Locale locale, final DefaultRowSorter<?,?> sorter )
	{
		_sorter = sorter;
		_originalRowFilter = ( sorter != null ) ? sorter.getRowFilter() : null;

		_timer = new Timer( 500, this );
		_timer.setRepeats( false );

		final JLabel label = new JLabel( ResourceBundleTools.getString( locale, QuickFindPanel.class, "findLabel" ) );
		_label = label;

		final JTextField field = new KeyConsumingTextField( 20 );
		_field = field;

		setLayout( new FlowLayout( FlowLayout.RIGHT ) );
		add( label );
		add( field );

		final Document document = field.getDocument();
		document.addDocumentListener( this );

		field.addActionListener( this );
	}

	/**
	 * Get label that is put in front of the text field.
	 *
	 * @return  Label that is put in front of the text field.
	 */
	public JLabel getLabel()
	{
		return _label;
	}

	/**
	 * Get search text field.
	 *
	 * @return  Search text field.
	 */
	public JTextField getTextField()
	{
		return _field;
	}

	@Override
	public void insertUpdate( final DocumentEvent e )
	{
		updateRowFilterLater();
	}

	@Override
	public void removeUpdate( final DocumentEvent e )
	{
		updateRowFilterLater();
	}

	@Override
	public void changedUpdate( final DocumentEvent e )
	{
		updateRowFilterLater();
	}

	/**
	 * Updates the row filters of the associated table row sorter after a delay.
	 */
	private void updateRowFilterLater()
	{
		_timer.restart();
	}

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		updateRowFilterNow();
	}

	/**
	 * Updates the row filters of the associated table row sorter immediately.
	 */
	private void updateRowFilterNow()
	{
		final JTextField field = _field;

		final Container topLevelAncestor = getTopLevelAncestor();
		final Cursor    topLevelCursor   = ( topLevelAncestor == null ) ? null : topLevelAncestor.getCursor();
		final Cursor    fieldCursor      = field.getCursor();
		final Cursor    waitCursor       = Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR );

		if ( topLevelAncestor != null )
		{
			topLevelAncestor.setCursor( waitCursor );
		}
		field.setCursor( waitCursor );

		try
		{
			final DefaultRowSorter<?,?> sorter = _sorter;
			final RowFilter originalRowFilter = _originalRowFilter;

			final String text = field.getText();
			if ( TextTools.isEmpty( text ) )
			{
				sorter.setRowFilter( originalRowFilter );
			}
			else
			{
				final Collection<RowFilter<Object,Object>> filters = new ArrayList<RowFilter<Object,Object>>();
				if ( originalRowFilter != null )
				{
					filters.add( originalRowFilter );
				}

				for ( final String word : text.split( "\\s+" ) )
				{
					filters.add( RowFilter.regexFilter( "(?i)" + Pattern.quote( word ) ) );
				}

				sorter.setRowFilter( RowFilter.andFilter( filters ) );
			}
		}
		finally
		{
			if ( topLevelAncestor != null )
			{
				topLevelAncestor.setCursor( topLevelCursor );
			}
			field.setCursor( fieldCursor );
		}
	}

	/**
	 * Clears the search query, such that no rows are filtered.
	 */
	public void clear()
	{
		_field.setText( "" );
		updateRowFilterNow();
	}

	/**
	 * Sets the row sorter to perform quick find on.
	 *
	 * @param   sorter  Row sorter to perform quick find on.
	 */
	public void setRowSorter( final DefaultRowSorter<?, ?> sorter )
	{
		final DefaultRowSorter oldSorter = _sorter;
		if ( sorter != oldSorter )
		{
			if ( oldSorter != null )
			{
				oldSorter.setRowFilter( _originalRowFilter );
			}

			_sorter = sorter;
			_originalRowFilter = ( sorter != null ) ? sorter.getRowFilter() : null;

			updateRowFilterNow();
		}
	}
}
