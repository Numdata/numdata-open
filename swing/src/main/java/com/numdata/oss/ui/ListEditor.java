/*
 * Copyright (c) 2010-2021, Numdata BV, The Netherlands.
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
import javax.swing.event.*;

import org.jetbrains.annotations.*;

/**
 * User-interface for editing a {@link List}.
 *
 * @param <E> list element type.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "UnusedReturnValue", "rawtypes", "unchecked", "WeakerAccess" } )
public class ListEditor<E>
{
	/**
	 * List that is being edited.
	 */
	@NotNull
	private final ListModelDecorator<E> _list;

	/**
	 * List component.
	 */
	@NotNull
	private final JList _listComponent;

	/**
	 * Panel that contains the list and tool bar.
	 */
	@NotNull
	private final JPanel _listPanel;

	/**
	 * Container used for tool components.
	 */
	@Nullable
	private JComponent _toolBar = null;

	/**
	 * Listener for events used/created by this editor.
	 */
	@NotNull
	private final CompoundListener _componentListener = new CompoundListener();

	/**
	 * Whether the list should be sorted.
	 */
	private boolean _sorted = false;

	/**
	 * Whether duplicated should be removed from the list.
	 */
	private boolean _removeDuplicates = false;

	/**
	 * Comparator used to sort the list. Note that the list will only be sorted
	 * if {@link #isSorted()} is {@code true}. Set to {@code null} for natural
	 * ordering.
	 */
	@Nullable
	private Comparator<E> _comparator = null;

	/**
	 * Action command for adding elements to the list.
	 */
	public static final String ADD = "add";

	/**
	 * Action command for removing elements from the list.
	 */
	public static final String REMOVE = "remove";

	/**
	 * Action command for copying elements in the list.
	 */
	public static final String COPY = "copy";

	/**
	 * Action command for moving elements up in the list.
	 */
	public static final String MOVE_UP = "moveUp";

	/**
	 * Action command for moving elements down in the list.
	 */
	public static final String MOVE_DOWN = "moveDown";

	/**
	 * Registered change listeners.
	 */
	@Nullable
	private List<ChangeListener> _changeListeners = null;

	/**
	 * Construct editor for a list.
	 *
	 * @param list List to edit.
	 */
	public ListEditor( @NotNull final List<E> list )
	{
		final ListModelDecorator<E> decoratedList = new ListModelDecorator<>( list );
		_list = decoratedList;

		//noinspection OverridableMethodCallDuringObjectConstruction
		final JList listComponent = createListComponent( decoratedList );
		listComponent.getModel().addListDataListener( new ListDataListener()
		{
			@Override
			public void intervalAdded( final ListDataEvent e )
			{
				fireChangeEvent();
			}

			@Override
			public void intervalRemoved( final ListDataEvent e )
			{
				fireChangeEvent();
			}

			@Override
			public void contentsChanged( final ListDataEvent e )
			{
				fireChangeEvent();
			}
		} );
		_listComponent = listComponent;

		final CompoundListener compoundListener = _componentListener;
		listComponent.addComponentListener( compoundListener );

		final ListSelectionModel selectionModel = listComponent.getSelectionModel();
		selectionModel.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
		selectionModel.addListSelectionListener( compoundListener );

		final JPanel listPanel = new JPanel( new BorderLayout() );
		final JScrollPane scrollPane = new JScrollPane( listComponent );
		scrollPane.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
		final JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
		verticalScrollBar.setBorder( BorderFactory.createEmptyBorder() );
		final JScrollBar horizontalScrollBar = scrollPane.getHorizontalScrollBar();
		horizontalScrollBar.setBorder( BorderFactory.createEmptyBorder() );
		listPanel.add( scrollPane, BorderLayout.CENTER );
		_listPanel = listPanel;
	}

	/**
	 * Add change listener. This listener will be called when the property page
	 * contents change.
	 *
	 * @param listener Listener to add.
	 */
	public void addChangeListener( final ChangeListener listener )
	{
		List<ChangeListener> changeListeners = _changeListeners;
		if ( changeListeners == null )
		{
			changeListeners = new LinkedList<>();
			_changeListeners = changeListeners;
		}
		changeListeners.add( listener );
	}

	/**
	 * Remove change listener.
	 *
	 * @param listener Listener to remove.
	 */
	public void removeChangeListener( final ChangeListener listener )
	{
		final List<ChangeListener> changeListeners = _changeListeners;
		if ( changeListeners != null )
		{
			changeListeners.remove( listener );
		}
	}

	/**
	 * Fire change event to all registered listeners.
	 */
	protected void fireChangeEvent()
	{
		final List<ChangeListener> changeListeners = _changeListeners;
		if ( ( changeListeners != null ) && !changeListeners.isEmpty() )
		{
			final ChangeEvent changeEvent = new ChangeEvent( this );

			for ( final ChangeListener listener : changeListeners )
			{
				listener.stateChanged( changeEvent );

			}
		}
	}

	/**
	 * Get component that provides the user-interface for the list editor.
	 *
	 * @return Component that provides the user-interface for the list editor.
	 */
	public JComponent getEditorComponent()
	{
		return getListPanel();
	}

	@SuppressWarnings( "ReturnOfCollectionOrArrayField" )
	public List<E> getList()
	{
		return _list;
	}

	public boolean isSorted()
	{
		return _sorted;
	}

	public void setSorted( final boolean sorted )
	{
		_sorted = sorted;
	}

	public boolean isRemoveDuplicates()
	{
		return _removeDuplicates;
	}

	public void setRemoveDuplicates( final boolean removeDuplicates )
	{
		_removeDuplicates = removeDuplicates;
	}

	@Nullable
	public Comparator<E> getComparator()
	{
		return _comparator;
	}

	public void setComparator( @Nullable final Comparator<E> comparator )
	{
		_comparator = comparator;
	}

	/**
	 * Sets the value (contents) of the edited list.
	 *
	 * @param value Value to be set.
	 */
	public void setValue( final Collection<E> value )
	{
		final ListModelDecorator<E> list = _list;
		if ( !list.equals( value ) )
		{
			final E selectedItem = getSelectedItem();

			list.setAll( value );

			if ( selectedItem != null )
			{
				setSelectedIndex( list.indexOf( selectedItem ) );
			}
		}
	}

	@NotNull
	public JList getListComponent()
	{
		return _listComponent;
	}

	@NotNull
	protected JPanel getListPanel()
	{
		return _listPanel;
	}

	/**
	 * Add element to end of the list and select it.
	 *
	 * @param element Element to add to the list.
	 */
	public void addElement( final E element )
	{
		addElement( _list.size(), element );
	}

	/**
	 * Add element at the specified index to the list and select it.
	 *
	 * @param index   Index in list to add element at.
	 * @param element Element to add to the list.
	 */
	public void addElement( final int index, final E element )
	{
		_list.add( index, element );
		setSelectedIndex( index );
	}

	/**
	 * This method should create an element to be added to the list.
	 *
	 * @return Element that was created; {@code null} if no element was created.
	 */
	@Nullable
	protected E createNewItem()
	{
		throw new UnsupportedOperationException( "createNewItem" );
	}

	/**
	 * Create a copy of the specified item to be added to the list.
	 *
	 * @param item Item to copy.
	 *
	 * @return Copy that was created; {@code null} if no element was created.
	 */
	protected E createItemCopy( final E item )
	{
		throw new UnsupportedOperationException( "createItemCopy" );
	}

	/**
	 * Move element down in the list.
	 *
	 * @param index Index of element to move down.
	 */
	protected void moveElementDown( final int index )
	{
		final List<E> list = _list;
		if ( ( index < 0 ) || ( index >= ( list.size() - 1 ) ) )
		{
			throw new IndexOutOfBoundsException( index + "/" + list.size() );
		}
		final int newIndex = index + 1;
		list.set( newIndex, list.set( index, list.get( newIndex ) ) );
		setSelectedIndex( newIndex );
	}

	/**
	 * Move element up in the list.
	 *
	 * @param index Index of element to move up.
	 */
	protected void moveElementUp( final int index )
	{
		final List<E> list = _list;
		if ( ( index < 1 ) || ( index >= list.size() ) )
		{
			throw new IndexOutOfBoundsException( index + "/" + list.size() );
		}
		final int newIndex = index - 1;
		list.set( newIndex, list.set( index, list.get( newIndex ) ) );
		setSelectedIndex( newIndex );
	}

	/**
	 * Remove element at the specified index from the list and select the new
	 * element at that index.
	 *
	 * @param index Index in list to remove element at.
	 */
	public void removeElement( final int index )
	{
		final List<E> list = _list;

		final int size = list.size();
		if ( ( index < 0 ) || ( index >= size ) )
		{
			throw new IndexOutOfBoundsException( index + "/" + size );
		}

		list.remove( index );

		setSelectedIndex( Math.min( index, size - 2 ) );
	}

	/**
	 * Get index of selected item in the list.
	 *
	 * @return Index of selected item; {@code -1} if no item is selected.
	 */
	public int getSelectedIndex()
	{
		final int minSelectionIndex = _listComponent.getSelectedIndex();
		return ( ( minSelectionIndex >= 0 ) && ( minSelectionIndex < _list.size() ) ) ? minSelectionIndex : -1;
	}

	/**
	 * Set index of selected item in the list.
	 *
	 * @param index Index of selected item; {@code -1} if no item is selected.
	 */
	protected void setSelectedIndex( final int index )
	{
		final JList listComponent = _listComponent;
		final int adjustedIndex = ( ( index < 0 ) || ( index >= _list.size() ) ) ? -1 : index;
		if ( adjustedIndex == -1 )
		{
			listComponent.clearSelection();
		}
		else
		{
			listComponent.setSelectedIndex( adjustedIndex );
			listComponent.ensureIndexIsVisible( adjustedIndex );
		}
	}

	/**
	 * Get selected item in list.
	 *
	 * @return Selected item; {@code null} if no item is selected (or the item
	 * is actually {@code null}).
	 */
	@Nullable
	public E getSelectedItem()
	{
		final List<E> list = _list;
		final int selectedIndex = getSelectedIndex();
		return ( selectedIndex >= 0 ) ? list.get( selectedIndex ) : null;
	}

	/**
	 * Add button for adding elements to the list.
	 *
	 * @return {@link JButton} that was created and added.
	 */
	public JButton addAddButton()
	{
		final JButton button = createButton( ADD, "Add", "/icons/silk/add.png", 16, 16 );
		button.addActionListener( _componentListener );
		addToolBarComponent( button );
		return button;
	}

	/**
	 * Add button for copying elements in the list.
	 *
	 * @return {@link JButton} that was created and added.
	 */
	public JButton addCopyButton()
	{
		final JButton button = createButton( COPY, "Copy", "/icons/silk/page_copy.png", 16, 16 );
		button.addActionListener( _componentListener );
		button.setEnabled( getSelectedIndex() >= 0 );
		addToolBarComponent( button );
		return button;
	}

	/**
	 * Add button for moving elements down in the list.
	 *
	 * @return {@link JButton} that was created and added.
	 */
	public JButton addMoveDownButton()
	{
		final JButton button = createButton( MOVE_DOWN, "MoveDown", "/icons/silk/arrow_down.png", 16, 16 );
		button.addActionListener( _componentListener );
		button.setEnabled( ( getSelectedIndex() >= 0 ) && ( getSelectedIndex() < ( _list.size() - 1 ) ) );
		addToolBarComponent( button );
		return button;
	}

	/**
	 * Add button for moving elements up in the list.
	 *
	 * @return {@link JButton} that was created and added.
	 */
	public JButton addMoveUpButton()
	{
		final JButton button = createButton( MOVE_UP, "MoveUp", "/icons/silk/arrow_up.png", 16, 16 );
		button.addActionListener( _componentListener );
		button.setEnabled( getSelectedIndex() > 0 );
		addToolBarComponent( button );
		return button;
	}

	/**
	 * Add button for deleting elements from the list.
	 *
	 * @return {@link JButton} that was created and added.
	 */
	public JButton addRemoveButton()
	{
		final JButton button = createButton( REMOVE, "Remove", "/icons/silk/delete.png", 16, 16 );
		button.addActionListener( _componentListener );
		button.setEnabled( getSelectedIndex() >= 0 );
		addToolBarComponent( button );
		return button;
	}

	/**
	 * Add component to the tool bar of the editor. The tool bar is
	 * automatically created when the first component is added. If no components
	 * are added, no tool bar is created.
	 *
	 * @param component Component to add to tool bar.
	 */
	public void addToolBarComponent( final Component component )
	{
		JComponent toolBar = _toolBar;
		if ( toolBar == null )
		{
			toolBar = createToolBar();
			_listPanel.add( toolBar, BorderLayout.NORTH );
			_toolBar = toolBar;
		}

		toolBar.add( component );
	}

	/**
	 * Create tool bar component to which buttons will be added.
	 *
	 * @return {@link JComponent} to use as tool bar.
	 */
	protected JComponent createToolBar()
	{
		final Box result = new Box( BoxLayout.X_AXIS );
		result.setBorder( BorderFactory.createMatteBorder( 1, 1, 0, 1, Color.GRAY ) );
		return result;
	}

	/**
	 * Create button to perform an action.
	 *
	 * @param actionCommand Action command to assign to button.
	 * @param text          Text to describe action.
	 * @param iconPath      Button icon path.
	 * @param iconWidth     Icon width.
	 * @param iconHeight    Icon height.
	 *
	 * @return {@link JButton} that was created.
	 */
	protected JButton createButton( @NotNull final String actionCommand, @Nullable final String text, @NotNull final String iconPath, final int iconWidth, final int iconHeight )
	{
		final JButton result = new JButton();
		result.setActionCommand( actionCommand );
		result.setToolTipText( text );
		SwingTools.makeIconButton( result, new AsyncIcon( ListEditor.class, iconPath, iconWidth, iconHeight, result ) );
		return result;
	}

	/**
	 * Creates list component.
	 *
	 * @param listModel List data model.
	 *
	 * @return {@link JList} component.
	 */
	protected JList createListComponent( final ListModel listModel )
	{
		final JList result = new JList( listModel );

		final ListCellRenderer defaultRenderer = result.getCellRenderer();
		result.setCellRenderer( ( list, value, index, isSelected, cellHasFocus ) -> defaultRenderer.getListCellRendererComponent( list, getDisplayValue( (E)value ), index, isSelected, cellHasFocus ) );

		return result;
	}

	/**
	 * Get value to display for the given item. The default implementation
	 * simply returns the given item.
	 *
	 * @param item Item to display.
	 *
	 * @return Display value.
	 */
	protected Object getDisplayValue( final E item )
	{
		return item;
	}

	/**
	 * Perform the specified action.
	 *
	 * @param actionCommand Action command (see class constants).
	 */
	protected void performAction( final String actionCommand )
	{
		final int selectedIndex = getSelectedIndex();
		final int listSize = _list.size();
		final boolean hasSelection = ( selectedIndex >= 0 ) && ( selectedIndex < listSize );

		if ( ADD.equals( actionCommand ) )
		{
			final E newItem = createNewItem();
			if ( newItem != null )
			{
				addElement( newItem );
			}
		}
		else if ( COPY.equals( actionCommand ) )
		{
			if ( hasSelection )
			{
				final E item = getSelectedItem();
				if ( item != null )
				{
					final E itemCopy = createItemCopy( item );
					if ( itemCopy != null )
					{
						addElement( selectedIndex + 1, itemCopy );
					}
				}
			}
		}
		else if ( MOVE_DOWN.equals( actionCommand ) )
		{
			if ( hasSelection && ( selectedIndex < ( listSize - 1 ) ) )
			{
				moveElementDown( selectedIndex );
			}
		}
		else if ( MOVE_UP.equals( actionCommand ) )
		{
			if ( hasSelection && ( selectedIndex > 0 ) )
			{
				moveElementUp( selectedIndex );
			}
		}
		else if ( REMOVE.equals( actionCommand ) )
		{
			if ( hasSelection )
			{
				removeElement( selectedIndex );
			}
		}

		cleanup();
	}

	/**
	 * Clean up list. This sorts and removes duplicates from the list.
	 */
	public void cleanup()
	{
		if ( isRemoveDuplicates() )
		{
			final List<E> list = getList();
			final Collection<E> tmp = isSorted() ? new TreeSet<>( _comparator ) : new LinkedHashSet<>( list.size() );
			tmp.addAll( list );
			setValue( tmp );
		}
		else if ( isSorted() )
		{
			final List<E> list = new ArrayList<>( getList() );
			list.sort( _comparator );
			setValue( list );
		}
	}

	/**
	 * Update editor state after selection change.
	 */
	protected void updateSelection()
	{
		final JComponent toolBar = _toolBar;
		if ( toolBar != null )
		{
			final int selectedIndex = getSelectedIndex();
			final int listSize = _list.size();
			final boolean hasSelection = ( selectedIndex >= 0 ) && ( selectedIndex < listSize );

			for ( final Component component : toolBar.getComponents() )
			{
				if ( component instanceof AbstractButton )
				{
					final String actionCommand = ( (AbstractButton)component ).getActionCommand();

					if ( COPY.equals( actionCommand ) )
					{
						component.setEnabled( hasSelection );
					}
					else if ( MOVE_DOWN.equals( actionCommand ) )
					{
						component.setEnabled( hasSelection && ( selectedIndex < ( listSize - 1 ) ) );
					}
					else if ( MOVE_UP.equals( actionCommand ) )
					{
						component.setEnabled( hasSelection && ( selectedIndex > 0 ) );
					}
					else if ( REMOVE.equals( actionCommand ) )
					{
						component.setEnabled( hasSelection );
					}
				}
			}
		}
	}

	/**
	 * Listener for events used/created by this editor.
	 */
	private class CompoundListener
	extends ComponentAdapter
	implements ActionListener, ListSelectionListener
	{
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			performAction( e.getActionCommand() );
		}

		@Override
		public void valueChanged( final ListSelectionEvent e )
		{
			if ( !e.getValueIsAdjusting() )
			{
				updateSelection();
			}
		}

		@Override
		public void componentShown( final ComponentEvent e )
		{
			System.out.println( "ListEditor$ComponentListener.componentShown" );
			updateSelection();
		}

		/**
		 * Invoked when the component has been made invisible.
		 */
		@Override
		public void componentHidden( final ComponentEvent e )
		{
			System.out.println( "ListEditor$ComponentListener.componentHidden" );
		}
	}
}
