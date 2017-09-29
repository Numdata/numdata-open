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
package com.numdata.oss.ui;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/**
 * Table model implemented on top of a {@link ListModel}. The list model
 * provides row objects. Columns of this table can be used to provide a view on
 * these row objects.
 *
 * @author  Peter S. Heijnen
 */
public class ListTableModel
	extends AbstractTableModel
	implements ListDataListener
{
	/**
	 * Underlying list model that provides row objects to this table.
	 */
	private ListModel _listModel = null;

	/**
	 * Table column definitions.
	 */
	private final List<ListTableColumn> _columns = new ArrayList<ListTableColumn>();

	/**
	 * Construct table model.
	 */
	public ListTableModel()
	{
		this( new DefaultListModel() );
	}

	/**
	 * Construct table model.
	 *
	 * @param   listModel   List model to provide row objects to this table.
	 */
	public ListTableModel( final ListModel listModel )
	{
		_listModel = listModel;
	}

	/**
	 * Get underlying list model that provides row objects to this table.
	 *
	 * @return  List model that is used.
	 */
	public ListModel getListModel()
	{
		return _listModel;
	}

	/**
	 * Set underlying list model that provides row objects to this table.
	 *
	 * @param   listModel   List model to use.
	 */
	public void setListModel( final ListModel listModel )
	{
		final ListModel oldListModel = _listModel;
		if ( listModel != oldListModel )
		{
			if ( oldListModel != null )
			{
				oldListModel.removeListDataListener( this );
			}

			_listModel = listModel;

			if ( listModel != null )
			{
				listModel.addListDataListener( this );
			}

			fireTableStructureChanged();
		}
	}

	@Override
	public Object getValueAt( final int rowIndex, final int columnIndex )
	{
		final ListTableColumn column = _columns.get( columnIndex );
		return column.getValue( this, getRowObject( rowIndex ), rowIndex, columnIndex );
	}

	@Override
	public void setValueAt( final Object value, final int rowIndex, final int columnIndex )
	{
		final ListTableColumn column = _columns.get( columnIndex );
		column.setValue( this, getRowObject( rowIndex ), rowIndex, columnIndex, value );
	}

	@Override
	public boolean isCellEditable( final int rowIndex, final int columnIndex )
	{
		final ListTableColumn column = _columns.get( columnIndex );
		return column.isEditable( this, getRowObject( rowIndex ), rowIndex, columnIndex );
	}

	@Override
	public int getRowCount()
	{
		return _listModel.getSize();
	}

	/**
	 * Get row object at the given row.
	 *
	 * @param   rowIndex    Table row index.
	 *
	 * @return  Row object.
	 */
	public Object getRowObject( final int rowIndex )
	{
		return _listModel.getElementAt( rowIndex );
	}

	/**
	 * Remove all columns from table model.
	 */
	public void clearColumns()
	{
		if ( !_columns.isEmpty() )
		{
			_columns.clear();
			fireTableStructureChanged();
		}
	}

	/**
	 * Add column to table model.
	 *
	 * @param   column  Column to add.
	 */
	public void addColumn( final ListTableColumn column )
	{
		_columns.add( column );
		fireTableStructureChanged();
	}

	/**
	 * Remove column from table model.
	 *
	 * @param   columnIndex     Column to remove.
	 */
	public void removeColumn( final int columnIndex )
	{
		_columns.remove( columnIndex );
		fireTableStructureChanged();
	}

	@Override
	public int getColumnCount()
	{
		return _columns.size();
	}

	/**
	 * Get column from table.
	 *
	 * @param   columnIndex     Index of column.
	 *
	 * @return  Data column.
	 */
	public ListTableColumn getColumn( final int columnIndex )
	{
		return _columns.get( columnIndex );
	}

	@Override
	public Class<?> getColumnClass( final int columnIndex )
	{
		final ListTableColumn column = getColumn( columnIndex );
		return column.getType( this, columnIndex );
	}

	@Override
	public String getColumnName( final int columnIndex )
	{
		final ListTableColumn column = getColumn( columnIndex );
		return column.getName( this, columnIndex );
	}

	@Override
	public void intervalAdded( final ListDataEvent e )
	{
		/* propagate list model events as table model events */
		fireTableRowsInserted( e.getIndex0(), e.getIndex1() );
	}

	@Override
	public void intervalRemoved( final ListDataEvent e )
	{
		/* propagate list model events as table model events */
		fireTableRowsDeleted( e.getIndex0(), e.getIndex1() );
	}

	@Override
	public void contentsChanged( final ListDataEvent e )
	{
		/* propagate list model events as table model events */
		fireTableRowsUpdated( e.getIndex0(), e.getIndex1() );
	}

	/**
	 * Table column interface.
	 */
	public interface ListTableColumn
	{
		/**
		 * Get column name.
		 *
		 * @param   model           Table model.
		 * @param   columnIndex     Column index in table model.
		 *
		 * @return  Column name.
		 */
		String getName( ListTableModel model, int columnIndex );

		/**
		 * Get cell value.
		 *
		 * @param   model           Table model.
		 * @param   rowObject       Row object.
		 * @param   rowIndex        Row index in table model.
		 * @param   columnIndex     Column index in table model.
		 *
		 * @return  Cell value.
		 */
		Object getValue( ListTableModel model, Object rowObject, int rowIndex, int columnIndex );

		/**
		 * Set cell value.
		 *
		 * @param   model           Table model.
		 * @param   rowObject       Row object.
		 * @param   rowIndex        Row index in table model.
		 * @param   columnIndex     Column index in table model.
		 * @param   value           Value to set.
		 */
		void setValue( ListTableModel model, Object rowObject, int rowIndex, int columnIndex, Object value );

		/**
		 * Test whether a cell is editable.
		 *
		 * @param   model           Table model.
		 * @param   rowObject       Row object.
		 * @param   rowIndex        Row index in table model.
		 * @param   columnIndex     Column index in table model.
		 *
		 * @return  {@code true} if cell is editable.
		 */
		boolean isEditable( ListTableModel model, Object rowObject, int rowIndex, int columnIndex );

		/**
		 * Get column type.
		 *
		 * @param   model           Table model.
		 * @param   columnIndex     Column index in table model.
		 *
		 * @return  Column type.
		 */
		Class<?> getType( ListTableModel model, int columnIndex );
	}

	/**
	 * Abstract base class for read-only table column.
	 */
	public abstract static class ReadOnlyListTableColumn
		implements ListTableColumn
	{
		@Override
		public void setValue( final ListTableModel model, final Object rowObject, final int rowIndex, final int columnIndex, final Object value )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isEditable( final ListTableModel model, final Object rowObject, final int rowIndex, final int columnIndex )
		{
			return false;
		}
	}

	/**
	 * Table column based on field reflection.
	 */
	public static class ListTableFieldColumn
		implements ListTableColumn
	{
		/**
		 * Field.
		 */
		private final Field _field;

		/**
		 * Construct column.
		 *
		 * @param   field   Java field.
		 */
		public ListTableFieldColumn( final Field field )
		{
			_field = field;
		}

		@Override
		public String getName( final ListTableModel model, final int columnIndex )
		{
			return _field.getName();
		}

		@Override
		public Object getValue( final ListTableModel model, final Object rowObject, final int rowIndex, final int columnIndex )
		{
			try
			{
				return ( rowObject != null ) ? _field.get( rowObject ) : null;
			}
			catch ( IllegalAccessException e )
			{
				throw new AssertionError( e );
			}
		}

		@Override
		public void setValue( final ListTableModel model, final Object rowObject, final int rowIndex, final int columnIndex, final Object value )
		{
			if ( rowObject == null )
			{
				throw new NullPointerException( "rowObject" );
			}

			try
			{
				_field.set( rowObject, value );
			}
			catch ( IllegalAccessException e )
			{
				throw new AssertionError( e );
			}
		}

		@Override
		public Class<?> getType( final ListTableModel model, final int columnIndex )
		{
			return _field.getType();
		}

		@Override
		public boolean isEditable( final ListTableModel model, final Object rowObject, final int rowIndex, final int columnIndex )
		{
			return !Modifier.isFinal( _field.getModifiers() );
		}
	}

	/**
	 * Table column based on bean property.
	 */
	public static class ListTableBeanColumn
		implements ListTableColumn
	{
		/**
		 * Bean property descriptor.
		 */
		private final PropertyDescriptor _propertyDescriptor;

		/**
		 * Construct column.
		 *
		 * @param   beanClass       Bean class.
		 * @param   propertyName    Name of bean property.
		 *
		 * @throws  IntrospectionException if an exception occurs during introspection.
		 */
		public ListTableBeanColumn( final Class<?> beanClass, final String propertyName )
			throws IntrospectionException
		{
			final BeanInfo beanInfo = Introspector.getBeanInfo( beanClass );
			PropertyDescriptor propertyDescriptor = null;
			for ( final PropertyDescriptor p : beanInfo.getPropertyDescriptors() )
			{
				if ( propertyName.equals( p.getName() ) )
				{
					propertyDescriptor = p;
					break;
				}
			}

			if ( propertyDescriptor == null )
			{
				throw new IntrospectionException( "Property '" + propertyName + "' not found in class '" + beanClass.getName() + '\'' );
			}

			_propertyDescriptor = propertyDescriptor;
		}

		@Override
		public String getName( final ListTableModel model, final int columnIndex )
		{
			return _propertyDescriptor.getDisplayName();
		}

		@Override
		public Object getValue( final ListTableModel model, final Object rowObject, final int rowIndex, final int columnIndex )
		{
			final Object result;

			if ( rowObject != null )
			{
				final Method readMethod = _propertyDescriptor.getReadMethod();
				if ( readMethod == null )
				{
					throw new AssertionError( "No read method available!" );
				}

				try
				{
					result = readMethod.invoke( rowObject );
				}
				catch ( IllegalAccessException e )
				{
					throw new AssertionError( e );
				}
				catch ( InvocationTargetException e )
				{
					throw new AssertionError( e );
				}
			}
			else
			{
				result = null;
			}

			return result;
		}

		@Override
		public void setValue( final ListTableModel model, final Object rowObject, final int rowIndex, final int columnIndex, final Object value )
		{
			if ( rowObject == null )
			{
				throw new NullPointerException( "rowObject" );
			}

			final Method writeMethod = _propertyDescriptor.getWriteMethod();
			if ( writeMethod == null )
			{
				throw new AssertionError( "No write method available!" );
			}

			try
			{
				writeMethod.invoke( rowObject, value );
			}
			catch ( IllegalAccessException e )
			{
				throw new AssertionError( e );
			}
			catch ( InvocationTargetException e )
			{
				throw new AssertionError( e );
			}
		}

		@Override
		public Class<?> getType( final ListTableModel model, final int columnIndex )
		{
			return _propertyDescriptor.getPropertyType();
		}

		@Override
		public boolean isEditable( final ListTableModel model, final Object rowObject, final int rowIndex, final int columnIndex )
		{
			return ( _propertyDescriptor.getWriteMethod() != null );
		}
	}
}
