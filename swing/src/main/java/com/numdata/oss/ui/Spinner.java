/*
 * Copyright (c) 2006-2017, Numdata BV, The Netherlands.
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
import javax.swing.*;

/**
 * Spinner component. This can be used to increment/decrement a value in a text
 * field.
 *
 * @author Peter S. Heijnen
 */
public class Spinner
extends JPanel
{
	/**
	 * Text field with value to control.
	 */
	private final JTextField _textField;

	/**
	 * Minimum value allowed.
	 */
	private final int _minValue;

	/**
	 * Maximum value allowed.
	 */
	private final int _maxValue;

	/**
	 * Inner class with button component for increment and decrement
	 * operations.
	 */
	private final class Button
	extends JButton
	implements ActionListener
	{
		/**
		 * Set if this is the increment button; cleared if this is the decrement
		 * button.
		 */
		private final boolean _isIncrement;

		/**
		 * Insets cache.
		 */
		private Insets _insets;

		/**
		 * Construct button.
		 *
		 * @param isIncrement Set if this is the increment button; cleared if
		 *                    this is the decrement button.
		 */
		private Button( final boolean isIncrement )
		{
			super( "" );

			setFocusable( false );
			addActionListener( this );

			_isIncrement = isIncrement;
			_insets = null;
		}

		@Override
		public void actionPerformed( final ActionEvent e )
		{
			if ( _isIncrement )
			{
				increment();
			}
			else // decrement
			{
				decrement();
			}

			_textField.requestFocusInWindow();
		}

		@Override
		protected void paintComponent( final Graphics g )
		{
			super.paintComponent( g );

			final Insets insets = getInsets( _insets );
			final int x = ( getWidth() + insets.left - insets.right ) / 2;
			final int y = ( getHeight() + insets.top - insets.bottom ) / 2;

			g.drawLine( x - 3, y, x + 3, y );
			if ( _isIncrement )
			{
				g.drawLine( x, y - 3, x, y + 3 );
			}

			_insets = insets;
		}

		@Override
		public boolean isVisible()
		{
			final JTextField textField = _textField;
			return super.isVisible() &&
//			       ( _isIncrement ? ( getTextFieldValue() < _limit ) : ( getTextFieldValue() > _limit ) ) &&
                   ( ( textField == null ) || ( textField.isVisible() && textField.isEnabled() ) );
		}
	}

	/**
	 * Construct spinner component.
	 *
	 * @param textField Text field with value to control.
	 */
	public Spinner( final JTextField textField )
	{
		this( textField, Integer.MIN_VALUE, Integer.MAX_VALUE );
	}

	/**
	 * Construct spinner component.
	 *
	 * @param textField Text field with value to control.
	 * @param minValue  Minimum value allowed.
	 * @param maxValue  Maximum value allowed.
	 */
	public Spinner( final JTextField textField, final int minValue, final int maxValue )
	{
		super( new GridLayout( 1, 2, 0, 0 ) );
		setBorder( null );

		_textField = textField;
		_minValue = minValue;
		_maxValue = maxValue;

		add( new Button( false ) );
		add( new Button( true ) );


		final InputMap inputMap = textField.getInputMap();
		inputMap.put( KeyStroke.getKeyStroke( '+' ), "incrementValue" );
		inputMap.put( KeyStroke.getKeyStroke( '-' ), "decrementValue" );

		final ActionMap actionMap = textField.getActionMap();
		actionMap.put( "incrementValue", new IncrementAction() );
		actionMap.put( "decrementValue", new DecrementAction() );

	}

	/**
	 * Increment value.
	 */
	public void increment()
	{
		final int value = getValue();
		if ( value < _maxValue )
		{
			setValue( value + 1 );
		}
	}

	/**
	 * Decrement value.
	 */
	public void decrement()
	{
		final int value = getValue();
		if ( value > _minValue )
		{
			setValue( value - 1 );
		}
	}

	/**
	 * Get integer value from text field, if any. If the text field contains no
	 * valid integer value, this will return 0. Any errors are absorbed.
	 *
	 * @return Integer value from text field; {@code 0} if no valid integer
	 * value is set.
	 */
	public int getValue()
	{
		int result = 0;

		String text = _textField.getText();

		final int dot = text.indexOf( (int)'.' );
		if ( dot >= 0 )
		{
			text = text.substring( 0, dot );
		}

		try
		{
			result = Integer.parseInt( text.trim() );
		}
		catch ( NumberFormatException e )
		{
			/* ignored */
		}

		return result;
	}

	/**
	 * Set integer value in text field. This will also trigger an action event
	 * from the text field, so text field listeners are notified.
	 *
	 * @param value Integer value to set in text field.
	 */
	public void setValue( final int value )
	{
		final JTextField textField = _textField;
		textField.setText( String.valueOf( value ) );
		textField.postActionEvent();
	}

	@Override
	public Dimension getMinimumSize()
	{
		final Dimension fieldMinimum = _textField.getMinimumSize();
		return new Dimension( fieldMinimum.height * 2, fieldMinimum.height );
	}

	@Override
	public Dimension getPreferredSize()
	{
		final Dimension fieldPreferred = _textField.getPreferredSize();
		return new Dimension( fieldPreferred.height * 2, fieldPreferred.height );
	}

	/**
	 * Increment action.
	 */
	private final class IncrementAction
	extends AbstractAction
	{
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			increment();
		}
	}

	/**
	 * Decrement action.
	 */
	private final class DecrementAction
	extends AbstractAction
	{
		@Override
		public void actionPerformed( final ActionEvent e )
		{
			decrement();
		}
	}
}
