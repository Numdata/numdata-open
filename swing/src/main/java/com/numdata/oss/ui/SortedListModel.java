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

import javax.swing.AbstractListModel;
import javax.swing.ListModel;
import javax.swing.RowSorter;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;

/**
 * Decorates an underlying list model with sorting and filtering capabilities
 * using a {@link RowSorter}.
 *
 * @author G. Meinders
*/
public class SortedListModel<M extends ListModel>
	extends AbstractListModel
{
	/**
	 * Unsorted list model.
	 */
	private M _model = null;

	/**
	 * Row sorter that performs sorting and filtering on the model.
	 */
	private RowSorter<M> _sorter = null;

	/**
	 * Listens to events from the underlying model and row sorter, and notifies
	 * this model's listeners of the changes.
	 */
	private final EventTranslator _eventTranslator = new EventTranslator();

	/**
	 * Constructs an empty, unsorted list model.
	 */
	public SortedListModel()
	{
	}

	/**
	 * Constructs a list model that views the given underlying model after it's
	 * sorted using the given row sorter.
	 *
	 * @param   model   Model to be wrapped.
	 * @param   sorter  Row sorter responsible for sorting the model.
	 */
	public SortedListModel( final M model , final RowSorter<M> sorter )
	{
		setModel( model );
		setSorter( sorter );
	}

	public Object getElementAt( final int index )
	{
		final Object result;
		if ( ( index < 0 ) || ( index >= _sorter.getViewRowCount() ) )
		{
			result = null;
		}
		else
		{
			result = _model.getElementAt( _sorter.convertRowIndexToModel( index ) );
		}
		return result;
	}

	public int getSize()
	{
		return ( _sorter != null ) ? _sorter.getViewRowCount() :
		       ( _model  != null ) ? _model.getSize()          : 0;
	}

	/**
	 * Returns the underlying list model for which this model provides sorting.
	 *
	 * @return  Underlying (unsorted) list model.
	 */
	public M getModel()
	{
		return _model;
	}

	/**
	 * Sets the underlying list model.
	 *
	 * @param   model   List model to be set.
	 */
	public void setModel( final M model )
	{
		final M oldModel = _model;
		if ( oldModel != null )
		{
			oldModel.removeListDataListener( _eventTranslator );
		}

		setSorter( null );

		_model = model;
		if ( model != null )
		{
			model.addListDataListener( _eventTranslator );
		}
	}

	/**
	 * Returns the model's row sorter.
	 *
	 * @return  Row sorter.
	 */
	public RowSorter<M> getSorter()
	{
		return _sorter;
	}

	/**
	 * Sets the row sorter used to sort the underlying model.
	 *
	 * @param   sorter  Row sorter to be set.
	 */
	public void setSorter( final RowSorter<M> sorter )
	{
		if ( ( sorter != null ) && ( sorter.getModel() != _model ) )
		{
			throw new IllegalArgumentException( "sorter for different model" );
		}

		final RowSorter<M> oldSorter = _sorter;
		if ( oldSorter != null )
		{
			oldSorter.removeRowSorterListener( _eventTranslator );
		}

		_sorter = sorter;
		if ( sorter != null )
		{
			sorter.addRowSorterListener( _eventTranslator );
		}
	}

	/**
	 * Listens to events from the underlying model and row sorter, and notifies
	 * this model's listeners of the changes.
	 */
	private class EventTranslator
		implements ListDataListener , RowSorterListener
	{
		public void contentsChanged( final ListDataEvent e )
		{
			final int start = e.getIndex0();
			final int end   = e.getIndex1();

			final RowSorter<M> sorter = _sorter;
			if ( sorter == null )
			{
				fireContentsChanged( SortedListModel.this , start , end );
			}
			else
			{
				final int[] oldViewIndices = new int[ end - start + 1 ];
				for ( int modelIndex = start ; modelIndex <= end ; modelIndex++ )
				{
					final int viewIndex = sorter.convertRowIndexToView( modelIndex );
					oldViewIndices[ modelIndex - start ] = viewIndex;
				}

				sorter.rowsUpdated( start , end );

				for ( int modelIndex = start ; modelIndex <= end ; modelIndex++ )
				{
					final int oldViewIndex = oldViewIndices[ modelIndex - start ];
					final int viewIndex = sorter.convertRowIndexToView( modelIndex );

					if ( viewIndex == -1 )
					{
						if ( oldViewIndex != -1 )
						{
							fireIntervalRemoved( SortedListModel.this , oldViewIndex , oldViewIndex );
						}
					}
					else if ( oldViewIndex == -1 )
					{
						fireIntervalAdded( SortedListModel.this , viewIndex , viewIndex );
					}
					else
					{
						fireContentsChanged( SortedListModel.this , viewIndex , viewIndex );
					}
				}
			}
		}

		public void intervalAdded( final ListDataEvent e )
		{
			final int start = e.getIndex0();
			final int end   = e.getIndex1();

			final RowSorter<M> sorter = _sorter;
			if ( sorter == null )
			{
				fireIntervalAdded( SortedListModel.this , start , end );
			}
			else
			{
				final int oldRowCount = sorter.getViewRowCount();
				sorter.rowsInserted( start , end );
				final int newRowCount = sorter.getViewRowCount();

				if ( oldRowCount < newRowCount )
				{
					fireIntervalAdded( SortedListModel.this , oldRowCount , newRowCount - 1 );
				}

				if ( newRowCount > 0 )
				{
					fireContentsChanged( SortedListModel.this , 0 , newRowCount - 1 );
				}
			}
		}

		public void intervalRemoved( final ListDataEvent e )
		{
			final int start = e.getIndex0();
			final int end   = e.getIndex1();

			final RowSorter<M> sorter = _sorter;
			if ( sorter == null )
			{
				fireIntervalRemoved( SortedListModel.this , start , end );
			}
			else
			{
				final int oldRowCount = sorter.getViewRowCount();
				sorter.rowsDeleted( start , end );
				final int newRowCount = sorter.getViewRowCount();

				if ( oldRowCount > newRowCount )
				{
					fireIntervalRemoved( SortedListModel.this , newRowCount , oldRowCount - 1 );
				}

				if ( newRowCount > 0 )
				{
					fireContentsChanged( SortedListModel.this , 0 , newRowCount - 1 );
				}
			}
		}

		public void sorterChanged( final RowSorterEvent e )
		{
			final int oldRowCount = e.getPreviousRowCount();
			final int newRowCount = _sorter.getViewRowCount();

			if ( newRowCount < oldRowCount )
			{
				fireIntervalRemoved( SortedListModel.this , newRowCount , oldRowCount - 1 );
			}
			else if ( newRowCount > oldRowCount )
			{
				fireIntervalAdded( SortedListModel.this , oldRowCount , newRowCount - 1 );
			}

			if ( newRowCount > 0 )
			{
				fireContentsChanged( SortedListModel.this , 0 , newRowCount - 1 );
			}
		}
	}
}
