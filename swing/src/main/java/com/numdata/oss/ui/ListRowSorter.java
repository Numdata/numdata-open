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

import javax.swing.DefaultRowSorter;
import javax.swing.ListModel;

/**
 * Provides sorting and filtering using a {@link ListModel}.
 *
 * @author G. Meinders
 */
public class ListRowSorter<M extends ListModel>
	extends DefaultRowSorter<M,Integer>
{
	/**
	 * Model to be sorted.
	 */
	private M _model;

	/**
	 * Converts values to strings for sorting.
	 */
	private ListStringConverter _stringConverter;

	/**
	 * Constructs a new sorted with no model set yet.
	 */
	public ListRowSorter()
	{
		this( null );
	}

	/**
	 * Constructs a new sorter for the given model.
	 *
	 * @param   model   Model to be sorted.
	 */
	public ListRowSorter( final M model )
	{
		_model           = model;
		_stringConverter = null;

		setModelWrapper( new ListRowSorterModelWrapper() );
	}

	/**
	 * Sets the object responsible for converting list values to strings to
	 * allow for sorting.
	 *
	 * @param   stringConverter     String converter to be set.
	 */
	public void setStringConverter( final ListStringConverter stringConverter )
	{
		_stringConverter = stringConverter;
	}

	/**
	 * Wraps a list model for use with a row sorter.
	 */
	private class ListRowSorterModelWrapper
		extends ModelWrapper<M,Integer>
	{
		public M getModel()
		{
			return _model;
		}

		public int getColumnCount()
		{
			return 1;
		}

		public int getRowCount()
		{
			return ( _model == null ) ? 0 : _model.getSize();
		}

		public Object getValueAt( final int row , final int column )
		{
			return _model.getElementAt( row );
		}

		@Override
		public String getStringValueAt( final int row , final int column )
		{
			final String result;

			final Object value = getValueAt( row , column );
			if ( value == null )
			{
				result = "";
			}
			else if ( _stringConverter != null )
			{
				result = _stringConverter.toString( _model , row );
			}
			else
			{
				result = String.valueOf( value );
			}

			return result;
		}

		public Integer getIdentifier( final int row )
		{
			return Integer.valueOf( row );
		}
	}
}
