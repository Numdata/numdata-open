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

import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * A choice action can be used for actions that should be performed when a
 * choice is made. In a UI such a choice is typically shown as a combo box.
 *
 * @author  G.B.M. Rupert
 */
public abstract class EnumChoiceAction
	extends ChoiceAction
	implements Runnable
{
	/**
	 * Label for each choice.
	 */
	private final String[] _labels;

	/**
	 * Value for each choice.
	 */
	private final Object[] _values;

	/**
	 * Value of the currently selected choice.
	 */
	private Object _selectedValue;

	/**
	 * Construct a new choice action.
	 *
	 * @param   bundle      Resource bundle to get choice labels from.
	 * @param   enumClass   Available choices.
	 */
	protected EnumChoiceAction( @Nullable final ResourceBundle bundle , @NotNull final Class<? extends Enum<?>> enumClass )
	{
		final Enum<? extends Enum<?>>[] values = enumClass.getEnumConstants();
		_values = values;

		if( bundle != null )
		{
			_labels = new String[ values.length ];
			for ( int i = 0 ; i < values.length ; i++ )
			{
				_labels[ i ] = bundle.getString( values[ i ].name() );
			}
		}
		else
		{
			_labels = new String[ values.length ];
			for ( int i = 0 ; i < values.length ; i++ )
			{
				_labels[ i ] = values[ i ].name();
			}
		}

		_selectedValue = ( values.length == 0 ) ? null : values[ 0 ];
	}

	@Override
	public Object getLabel( final Object value )
	{
		final int index = ArrayTools.indexOf( value , _values );
		if ( index < 0 )
		{
			throw new IllegalArgumentException( "value: " + value );
		}
		return _labels[ index ];
	}

	@Override
	public Object[] getValues()
	{
		return _values;
	}

	@Override
	public Object getSelectedValue()
	{
		return _selectedValue;
	}

	@Override
	public void setSelectedValue( final Object selectedValue )
	{
		final Object oldValue = _selectedValue;
		_selectedValue = selectedValue;
		firePropertyChange( SELECTED_VALUE , oldValue , selectedValue );
		run();
	}
}
