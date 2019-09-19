/*
 * MySwing: Advanced Swing Utilites
 * Copyright (C) 2014-2017  Santhosh Kumar T
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
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * CheckTree with/without Selection Digging
 *
 * Now it is easy to add check-boxes to any JTree very easily using MySwing.
 *
 * It also comes with two options whether to dig the selection or not.
 *
 * http://jroller.com/santhosh/entry/checktree_with_without_selection_digging
 *
 * @author Santhosh Kumar T - santhosh.tekuri@gmail.com
 */
public class CheckTreeManager
extends MouseAdapter
implements TreeSelectionListener
{
	private final CheckTreeSelectionModel _selectionModel;

	private JTree _tree = new JTree();

	int _hotspot = new JCheckBox().getPreferredSize().width;

	public CheckTreeManager( final JTree tree )
	{
		this( tree, new CheckTreeSelectionModel( tree.getModel() ) );
	}

	public CheckTreeManager( final JTree tree, final CheckTreeSelectionModel selectionModel )
	{
		_tree = tree;
		_selectionModel = selectionModel;
		tree.setCellRenderer( new CheckTreeCellRenderer( tree.getCellRenderer(), selectionModel ) );
		tree.addMouseListener( this );
		selectionModel.addTreeSelectionListener( this );
	}

	@Override
	public void mouseClicked( final MouseEvent me )
	{
		final JTree tree = _tree;

		final TreePath path = tree.getPathForLocation( me.getX(), me.getY() );

		if ( path != null )
		{
			final Rectangle pathBounds = tree.getPathBounds( path );
			if ( ( pathBounds != null ) && ( me.getX() <= pathBounds.x + _hotspot ) )
			{
				toggleSelection( path );
			}
		}
	}

	public void toggleSelection( final TreePath path )
	{
		final CheckTreeSelectionModel selectionModel = getSelectionModel();
		setSelection( path, !selectionModel.isPathSelected( path, true ) );
	}

	public void setSelection( final TreePath path, final boolean selected )
	{
		final CheckTreeSelectionModel selectionModel = getSelectionModel();
		if ( selected != selectionModel.isPathSelected( path, true ) )
		{
			selectionModel.removeTreeSelectionListener( this );
			try
			{
				selectionModel.setSelection( path, selected );
			}
			finally
			{
				selectionModel.addTreeSelectionListener( this );
				_tree.treeDidChange();
			}
		}
	}

	public CheckTreeSelectionModel getSelectionModel()
	{
		return _selectionModel;
	}

	public void valueChanged( final TreeSelectionEvent e )
	{
		_tree.treeDidChange();
	}

	public static class CheckTreeSelectionModel
	extends DefaultTreeSelectionModel
	{
		private TreeModel _model;

		public CheckTreeSelectionModel( final TreeModel model )
		{
			_model = model;
			setSelectionMode( TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION );
		}

		public void setSelection( final TreePath path, final boolean selected )
		{
			if ( selected != isPathSelected( path, true ) )
			{
				if ( !selected )
				{
					removeSelectionPath( path );
				}
				else
				{
					addSelectionPath( path );
				}
			}
		}

		// tests whether there is any unselected node in the subtree of given path
		public boolean isPartiallySelected( final TreePath path )
		{
			if ( isPathSelected( path, true ) )
			{
				return false;
			}
			final TreePath[] selectionPaths = getSelectionPaths();
			if ( selectionPaths == null )
			{
				return false;
			}
			for ( final TreePath selectionPath : selectionPaths )
			{
				if ( isDescendant( selectionPath, path ) )
				{
					return true;
				}
			}
			return false;
		}

		// tells whether given path is selected.
		// if dig is true, then a path is assumed to be selected, if
		// one of its ancestor is selected.
		public boolean isPathSelected( final TreePath path, final boolean dig )
		{
			boolean result = isPathSelected( path );
			if ( !result && dig )
			{
				for ( TreePath ancestor = path.getParentPath(); ancestor != null; ancestor = ancestor.getParentPath() )
				{
					if ( isPathSelected( ancestor ) )
					{
						result = true;
						break;
					}
				}
			}
			return result;
		}

		// is path1 descendant of path2
		private boolean isDescendant( final TreePath path1, final TreePath path2 )
		{
			boolean result = true;

			final Object[] obj1 = path1.getPath();
			final Object[] obj2 = path2.getPath();
			for ( int i = 0; i < obj2.length; i++ )
			{
				//noinspection ObjectEquality
				if ( obj1[ i ] != obj2[ i ] )
				{
					result = false;
					break;
				}
			}

			return result;
		}

		@Override
		public void setSelectionPaths( final TreePath[] pPaths )
		{
			throw new UnsupportedOperationException( "not implemented yet!!!" );
		}

		@Override
		public void addSelectionPaths( final TreePath[] paths )
		{
			// unselect all descendants of paths[]
			for ( final TreePath path : paths )
			{
				final TreePath[] selectionPaths = getSelectionPaths();
				if ( selectionPaths == null )
				{
					break;
				}
				final ArrayList<TreePath> toBeRemoved = new ArrayList<TreePath>();
				for ( final TreePath selectionPath : selectionPaths )
				{
					if ( isDescendant( selectionPath, path ) )
					{
						toBeRemoved.add( selectionPath );
					}
				}
				super.removeSelectionPaths( toBeRemoved.toArray( new TreePath[ toBeRemoved.size() ] ) );
			}

			// if all siblings are selected then unselect them and select parent recursively
			// otherwize just select that path.
			for ( final TreePath path1 : paths )
			{
				TreePath path = path1;
				TreePath temp = null;
				while ( areSiblingsSelected( path ) )
				{
					temp = path;
					if ( path.getParentPath() == null )
					{
						break;
					}
					path = path.getParentPath();
				}
				if ( temp != null )
				{
					if ( temp.getParentPath() != null )
					{
						addSelectionPath( temp.getParentPath() );
					}
					else
					{
						if ( !isSelectionEmpty() )
						{
							removeSelectionPaths( getSelectionPaths() );
						}
						super.addSelectionPaths( new TreePath[] { temp } );
					}
				}
				else
				{
					super.addSelectionPaths( new TreePath[] { path } );
				}
			}
		}

		// tells whether all siblings of given path are selected.
		private boolean areSiblingsSelected( final TreePath path )
		{
			final TreePath parent = path.getParentPath();
			if ( parent == null )
			{
				return true;
			}
			final Object node = path.getLastPathComponent();
			final Object parentNode = parent.getLastPathComponent();

			final TreeModel model = _model;
			final int childCount = model.getChildCount( parentNode );
			for ( int i = 0; i < childCount; i++ )
			{
				final Object childNode = model.getChild( parentNode, i );
				//noinspection ObjectEquality
				if ( ( childNode != node ) && !isPathSelected( parent.pathByAddingChild( childNode ) ) )
				{
					return false;
				}
			}
			return true;
		}

		@Override
		public void removeSelectionPaths( final TreePath[] paths )
		{
			for ( final TreePath path : paths )
			{
				if ( path.getPathCount() == 1 )
				{
					super.removeSelectionPaths( new TreePath[] { path } );
				}
				else
				{
					toggleRemoveSelection( path );
				}
			}
		}

		// if any ancestor node of given path is selected then unselect it
		//  and selection all its descendants except given path and descendants.
		// otherwise just unselect the given path
		private void toggleRemoveSelection( final TreePath path )
		{
			final Deque<TreePath> stack = new ArrayDeque<TreePath>();

			TreePath parent = path.getParentPath();
			while ( parent != null && !isPathSelected( parent ) )
			{
				stack.push( parent );
				parent = parent.getParentPath();
			}

			if ( parent == null )
			{
				super.removeSelectionPaths( new TreePath[] { path } );
			}
			else
			{
				final TreeModel model = _model;

				stack.push( parent );
				while ( !stack.isEmpty() )
				{
					final TreePath temp = stack.pop();
					final TreePath peekPath = stack.isEmpty() ? path : stack.peek();
					final Object node = temp.getLastPathComponent();
					final Object peekNode = peekPath.getLastPathComponent();
					final int childCount = model.getChildCount( node );
					for ( int i = 0; i < childCount; i++ )
					{
						final Object childNode = model.getChild( node, i );
						//noinspection ObjectEquality
						if ( childNode != peekNode )
						{
							super.addSelectionPaths( new TreePath[] { temp.pathByAddingChild( childNode ) } );
						}
					}
				}
				super.removeSelectionPaths( new TreePath[] { parent } );
			}

		}
	}

	private static class CheckTreeCellRenderer
	extends JPanel
	implements TreeCellRenderer
	{
		private final CheckTreeSelectionModel _selectionModel;

		private final TreeCellRenderer _delegate;

		private final TristateCheckBox _checkBox = new TristateCheckBox();

		CheckTreeCellRenderer( final TreeCellRenderer delegate, final CheckTreeSelectionModel selectionModel )
		{
			_delegate = delegate;
			_selectionModel = selectionModel;
			setLayout( new BorderLayout() );
			setOpaque( false );
			_checkBox.setOpaque( false );
		}


		public Component getTreeCellRendererComponent( final JTree tree, final Object value, final boolean selected, final boolean expanded, final boolean leaf, final int row, final boolean hasFocus )
		{
			final Component renderer = _delegate.getTreeCellRendererComponent( tree, value, selected, expanded, leaf, row, hasFocus );

			final TreePath path = tree.getPathForRow( row );
			if ( path != null )
			{
				if ( _selectionModel.isPathSelected( path, true ) )
				{
					_checkBox.setSelected( true );
				}
				else if ( _selectionModel.isPartiallySelected( path ) )
				{
					_checkBox.setIndeterminate();
				}
				else
				{
					_checkBox.setSelected( false );
				}
			}
			removeAll();
			add( _checkBox, BorderLayout.WEST );
			add( renderer, BorderLayout.CENTER );
			return this;
		}
	}

}
