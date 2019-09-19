/*
 * Copyright (c) 2005-2018, Numdata BV, The Netherlands.
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
import java.text.*;
import java.util.*;
import javax.swing.*;

import com.numdata.oss.*;
import static javax.swing.ScrollPaneConstants.*;
import org.jetbrains.annotations.*;

/**
 * This wizard page is used for selecting a value using radio buttons.
 *
 * @author Peter S. Heijnen
 */
public abstract class AbstractRadioWizardPage
extends WizardPage
implements ItemListener
{
	/**
	 * Values from which a selection is made.
	 */
	private Object[] _values;

	/**
	 * Panel containing radio buttons.
	 */
	private JPanel _radioPanel;

	/**
	 * Radio buttons used to make selection.
	 */
	private JRadioButton[] _radios;

	/**
	 * Construct page.
	 *
	 * @param wizard      Wizard that controls this page.
	 * @param res         Resource bundle for this page (optional).
	 * @param resourceKey Resource key (name) of page (optional).
	 */
	protected AbstractRadioWizardPage( final Wizard wizard, final ResourceBundle res, final String resourceKey )
	{
		super( wizard, res, resourceKey, new BorderLayout() );

		_values = null;
		_radioPanel = null;
		_radios = null;
	}

	@Override
	protected void initGui()
	{
		final JPanel radioPanel = createRadioPanel();
		add( new JScrollPane( radioPanel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER ), BorderLayout.CENTER );
	}

	@Override
	protected void shown()
	{
		updateRadios();
		updateValue();
		setEnabled( ( _values != null ) && ( _values.length > 1 ) );
	}

	/**
	 * Create panel on which the radio buttons will be placed.
	 *
	 * @return {@link JPanel} on which the radio buttons will be placed.
	 */
	protected JPanel createRadioPanel()
	{
		final JPanel result = new RadioPanel( _res.getString( getName() + ".message" ), this );
		_radioPanel = result;
		return result;
	}

	/**
	 * Create radio button that will be placed on the radio panel.
	 *
	 * @param name Name of the radio button.
	 * @param text Text of the radio button.
	 *
	 * @return The created {@link JRadioButton} (never {@code null}).
	 */
	protected JRadioButton createRadioButton( final String name, final String text )
	{
		final JRadioButton result = new JRadioButton( text );
		result.setFocusPainted( false );
		result.setName( name );
		return result;
	}

	/**
	 * Update contents of radio panel based on the currently available choices.
	 */
	protected void updateRadios()
	{
		final Wizard wizard = getWizard();
		final Locale locale = wizard.getLocale();
		final ResourceBundle res = _res;
		final String resourceKey = getName();

		final JPanel radioPanel = _radioPanel;
		final Object[] values = getValues();
		final int selectedIndex = Math.max( 0, ArrayTools.indexOf( getValue(), values ) );
		final JRadioButton[] radios = new JRadioButton[ values.length ];

		final String valuePattern = ResourceBundleTools.getString( res, resourceKey + ".value", null );
		final MessageFormat valueFormat = ( valuePattern != null ) ? new MessageFormat( valuePattern, locale ) : null;

		radioPanel.removeAll();

		for ( int i = 0; i < values.length; i++ )
		{
			Object value = values[ i ];
			final String name;

			if ( value instanceof Object[] )
			{
				final Object[] valueObjects = (Object[])value;

				name = ( valueObjects.length > 0 ) ? String.valueOf( valueObjects[ 0 ] ) : null;

				if ( valueObjects.length == 1 )
				{
					value = getTranslatedValue( valueObjects[ 0 ] );
				}
				else
				{
					final Object[] translatedValues = new Object[ valueObjects.length ];

					for ( int j = 0; j < translatedValues.length; j++ )
					{
						translatedValues[ j ] = getTranslatedValue( valueObjects[ j ] );
					}

					value = translatedValues;
				}
			}
			else
			{
				name = ( value != null ) ? String.valueOf( value ) : null;
				value = getTranslatedValue( value );

			}

			final String text = getText( value, locale, valueFormat );

			final JRadioButton radio = createRadioButton( name, text );
			radioPanel.add( radio );
			radio.setSelected( i == selectedIndex );
			radios[ i ] = radio;
		}

		_values = values;
		_radios = radios;
	}

	/**
	 * Returns a text representation of the given value.
	 *
	 * @param value  Value to create a representation of.
	 * @param locale Locale to be used.
	 * @param format Suggested message format for the text representation;
	 *               {@code null} if not available.
	 *
	 * @return Text representation of the given value.
	 */
	protected String getText( final Object value, final Locale locale, final MessageFormat format )
	{
		return ( format != null ) ? format.format( ( value instanceof Object[] ) ? value : new Object[] { value } )
		                          : ArrayTools.isValidType( value ) ? ArrayTools.toString( value )
		                                                            : String.valueOf( value );
	}

	/**
	 * Update selection using the currently selected radio button.
	 *
	 * @see #getRadioIndex
	 * @see #setValue
	 */
	protected void updateValue()
	{
		final int radioIndex = getRadioIndex();
		setValue( ( radioIndex < 0 ) ? null : _values[ radioIndex ] );
	}

	/**
	 * Get index of currently selected radio button.
	 *
	 * @return Index of radio button; {@code -1} if no radio button is currently
	 * selected.
	 */
	protected final int getRadioIndex()
	{
		int result = -1;

		final JRadioButton[] radios = _radios;
		if ( radios != null )
		{
			for ( int i = 0; i < radios.length; i++ )
			{
				final JRadioButton radio = radios[ i ];
				if ( ( radio != null ) && radio.isSelected() )
				{
					result = i;
					break;
				}
			}
		}

		return result;
	}

	@Override
	public final void itemStateChanged( final ItemEvent event )
	{
		if ( ( event.getID() == ItemEvent.ITEM_STATE_CHANGED ) && ( event.getStateChange() == ItemEvent.SELECTED ) )
		{
			updateValue();
		}
	}


	/**
	 * Get currently selected value.
	 *
	 * @return Currently selected value; {@code null} if no value has been
	 * selected.
	 */
	protected abstract Object getValue();

	/**
	 * Set currently selected value.
	 *
	 * @param value Value to select ({@code null} to clear the selection).
	 */
	protected abstract void setValue( final Object value );

	/**
	 * Get list of values to make the selection from.
	 *
	 * @return List of values (never {@code null}).
	 */
	protected abstract Object[] getValues();

	/**
	 * Panel with optional title that automatically lays out (radio) buttons and
	 * registers an optional listener to all (radio) buttons on the panel.
	 */
	private static class RadioPanel
	extends JPanel
	{
		/**
		 * Button group for all (radio) buttons.
		 */
		final ButtonGroup _buttonGroup;

		/**
		 * Default constraints used for buttons.
		 */
		final GridBagConstraints _constraints;

		/**
		 * Listener that is registered to all buttons.
		 */
		@Nullable
		private final ItemListener _itemListener;

		/**
		 * Construct panel.
		 *
		 * @param title        Title for panel.
		 * @param itemListener Listener that is registered to all buttons.
		 */
		RadioPanel( @Nullable final String title, @Nullable final ItemListener itemListener )
		{
			super( new GridBagLayout() );

			_itemListener = itemListener;
			_buttonGroup = new ButtonGroup();

			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.weightx = 1.0;
			constraints.anchor = GridBagConstraints.WEST;
			_constraints = constraints;

			if ( title != null )
			{
				constraints.insets.left = 16;
				constraints.insets.right = 16;
				constraints.fill = GridBagConstraints.HORIZONTAL;

				final JLabel label = new JLabel( title );
				label.setBorder( new EtchedBorder( 0, 0, 8, 0 ) );
				add( label, constraints );

				constraints.insets.left = 30;
				constraints.insets.right = 30;
				constraints.fill = GridBagConstraints.NONE;
			}
		}

		@Override
		protected void addImpl( final Component component, final Object constraints, final int index )
		{
			if ( component instanceof AbstractButton )
			{
				final AbstractButton button = (AbstractButton)component;
				button.setSelected( true );
				_buttonGroup.add( button );
				if ( _itemListener != null )
				{
					button.addItemListener( _itemListener );
				}
			}

			super.addImpl( component, ( constraints == null ) ? _constraints : constraints, index );
		}

		@Override
		public void remove( final int index )
		{
			final Component component = getComponent( index );
			if ( component instanceof AbstractButton )
			{
				final AbstractButton button = (AbstractButton)component;
				if ( _itemListener != null )
				{
					button.removeItemListener( _itemListener );
				}
				_buttonGroup.remove( button );
			}

			super.remove( index );
		}

		@Override
		public void removeAll()
		{
			synchronized ( getTreeLock() )
			{
				for ( int i = getComponentCount(); --i >= 0; )
				{
					final Component component = getComponent( i );
					if ( component instanceof AbstractButton )
					{
						remove( component );
					}
				}
			}
		}
	}
}
