/*
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2005  Santhosh Kumar T
 * <p/>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p/>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package com.numdata.oss.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * JList with CheckBoxes
 *
 * How to use this:
 *
 * Make your JList as check list
 * <pre>
 * CheckListManager checkListManager = new CheckListManager(yourList);
 *
 * // to get checked items
 * checkListManager.getSelectionModel().isSelectedIndex(index);
 * </pre>
 *
 * So your can convert JList with any model and renderer to CheckList with just
 * one line.
 *
 * We use DefaultListCellRenderer to store checkSelection. This allows us to
 * listen for changes in check selection very easily. We wrap the
 * ListCellRenderer to add CheckBox. This allows you to reuse existing
 * renderers.
 *
 * http://www.jroller.com/santhosh/entry/jlist_with_checkboxes
 *
 * @author Santhosh Kumar T - santhosh.tekuri@gmail.com
 */
@SuppressWarnings ("JavaDoc")
public class CheckListManager
extends MouseAdapter
implements ListSelectionListener, ActionListener
{
	private final ListSelectionModel _selectionModel = new DefaultListSelectionModel();

	private final JList _list;

	int _hotspot = new JCheckBox().getPreferredSize().width;

	public CheckListManager( final JList list )
	{
		_list = list;
		list.setCellRenderer( new CheckListCellRenderer( list.getCellRenderer(), _selectionModel ) );
		list.registerKeyboardAction( this, KeyStroke.getKeyStroke( KeyEvent.VK_SPACE, 0 ), JComponent.WHEN_FOCUSED );
		list.addMouseListener( this );
		_selectionModel.addListSelectionListener( this );
	}

	public ListSelectionModel getSelectionModel()
	{
		return _selectionModel;
	}

	private void toggleSelection( final int index )
	{
		if ( index >= 0 )
		{

			if ( _selectionModel.isSelectedIndex( index ) )
			{
				_selectionModel.removeSelectionInterval( index, index );
			}
			else
			{
				_selectionModel.addSelectionInterval( index, index );
			}
		}
	}

	/*------------------------------[ MouseListener ]-------------------------------------*/

	@Override
	public void mouseClicked( final MouseEvent me )
	{
		final int index = _list.locationToIndex( me.getPoint() );
		if ( index < 0 )
		{
			return;
		}
		if ( me.getX() > _list.getCellBounds( index, index ).x + _hotspot )
		{
			return;
		}
		toggleSelection( index );
	}

	/*-----------------------------[ ListSelectionListener ]---------------------------------*/

	public void valueChanged( final ListSelectionEvent e )
	{
		_list.repaint( _list.getCellBounds( e.getFirstIndex(), e.getLastIndex() ) );
	}

	/*-----------------------------[ ActionListener ]------------------------------*/

	public void actionPerformed( final ActionEvent e )
	{
		toggleSelection( _list.getSelectedIndex() );
	}

	public static class CheckListCellRenderer
	extends JPanel
	implements ListCellRenderer
	{
		private final ListCellRenderer _delegate;

		private final ListSelectionModel _selectionModel;

		private final JCheckBox _checkBox = new JCheckBox();

		public CheckListCellRenderer( final ListCellRenderer renderer, final ListSelectionModel selectionModel )
		{
			_delegate = renderer;
			_selectionModel = selectionModel;
			setLayout( new BorderLayout() );
			setOpaque( false );
			_checkBox.setOpaque( false );
		}

		public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
		{
			final Component renderer = _delegate.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );
			_checkBox.setSelected( _selectionModel.isSelectedIndex( index ) );
			removeAll();
			add( _checkBox, BorderLayout.WEST );
			add( renderer, BorderLayout.CENTER );
			return this;
		}
	}
}

