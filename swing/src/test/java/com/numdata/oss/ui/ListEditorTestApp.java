/*
 * Copyright (c) 2010-2017, Numdata BV, The Netherlands.
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
import java.util.List;
import javax.swing.*;

/**
 * Test application for {@link ListEditor} class.
 *
 * @author  Peter S. Heijnen
 */
public class ListEditorTestApp
	extends ListEditor<String>
{
	/**
	 * Main editor component.
	 */
	private final JSplitPane _editorComponent;

	/**
	 * Title border.
	 */
	private final ColoredTitledBorder _titledBorder;

	/**
	 * Container for selected item.
	 */
	private final JPanel _listItemContainer;

	/**
	 * Label for {@link #_textField}.
	 */
	private final JLabel _textLabel;

	/**
	 * Text field containing the currently selected item. Can be used for
	 * renaming an item.
	 */
	private final JTextField _textField;

	/**
	 * Run application.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		final ListEditorTestApp listEditor = new ListEditorTestApp();

		final JFrame frame = new JFrame( "List Editor Test Application" );
		frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
		frame.setContentPane( listEditor.getEditorComponent() );
		frame.pack();
		WindowTools.center( frame, -300, -200 );
		frame.setVisible( true );
	}

	/**
	 * Construct test editor.
	 */
	private ListEditorTestApp()
	{
		super( new ArrayList<String>( Arrays.asList( "Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Iota", "Kappa", "Lambda", "Mu", "Nu", "Xi", "Omicron", "Pi", "Rho", "Sigma", "Tau", "Upsilon", "Phi", "Chi", "Psi", "Omega" ) ) );

		addAddButton();
		addRemoveButton();
		addCopyButton();
		addMoveUpButton();
		addMoveDownButton();

		final ColoredTitledBorder titledBorder = new ColoredTitledBorder( "" );
		_titledBorder = titledBorder;

		final JLabel textLabel = new JLabel( "Value:" );
		textLabel.setEnabled( false );
		_textLabel = textLabel;

		final JTextField textField = new JTextField( 20 );
		textField.addActionListener( new ActionListener()
		{
			@Override
			public void actionPerformed( final ActionEvent e )
			{
				final int selectedIndex = getSelectedIndex();
				if ( selectedIndex >= 0 )
				{
					final List<String> list = getList();
					list.set( selectedIndex, textField.getText() );
				}
			}
		} );
		textField.setEnabled( false );
		_textField = textField;

		final JPanel listItemContainer = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		listItemContainer.setBorder( titledBorder );
		listItemContainer.add( textLabel );
		listItemContainer.add( textField );
		_listItemContainer = listItemContainer;

		final JSplitPane splitPane = SwingTools.createInvisibleSplitPane( getListPanel(), listItemContainer );
		splitPane.setDividerLocation( 200 );
		_editorComponent = splitPane;
	}

	@Override
	protected String createNewItem()
	{
		return "<new item>";
	}

	@Override
	protected String createItemCopy( final String item )
	{
		return item;
	}

	@Override
	public JComponent getEditorComponent()
	{
		return _editorComponent;
	}

	@Override
	protected void updateSelection()
	{
		super.updateSelection();
		final String selectedItem = getSelectedItem();
		_titledBorder.setText( ( selectedItem != null ) ? selectedItem : "Nothing selected" );
		_listItemContainer.repaint();
		_textLabel.setEnabled( ( selectedItem != null ) );
		_textField.setText( ( selectedItem != null ) ? selectedItem : "" );
		_textField.setEnabled( ( selectedItem != null ) );
	}
}
