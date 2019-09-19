/*
 * Copyright (c) 2004-2017, Numdata BV, The Netherlands.
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
import java.beans.*;
import javax.swing.*;

import org.intellij.lang.annotations.*;

/**
 * This class provides utility methods for {@link Action}s.
 *
 * @author  G.B.M. Rupert
 */
public class ActionTools
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private ActionTools()
	{
	}

	/**
	 * Add action(s) to a toolbar.
	 *
	 * @param   toolBar     Target tool bar.
	 * @param   actions     Actions to add.
	 */
	public static void addToToolBar( final JToolBar toolBar, final Action... actions )
	{
		if ( actions != null )
		{
			for ( final Action action : actions )
			{
				if ( action != null )
				{
					if ( action instanceof ChoiceAction )
					{
						final ChoiceAction choiceAction = (ChoiceAction)action;

						final JComboBox comboBox = new JComboBox( choiceAction.getValues() );
						comboBox.setSelectedItem( choiceAction.getSelectedValue() );
						comboBox.setMaximumSize( comboBox.getPreferredSize() );
						comboBox.setRenderer( new DefaultListCellRenderer()
						{
							@Override
							public Component getListCellRendererComponent( final JList list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus )
							{
								final Object label = choiceAction.getLabel( value );
								return super.getListCellRendererComponent( list, label, index, isSelected, cellHasFocus );
							}
						} );

						comboBox.addItemListener( new ItemListener()
						{
							public void itemStateChanged( final ItemEvent e )
							{
								if ( e.getStateChange() == ItemEvent.SELECTED )
								{
									choiceAction.setSelectedValue( e.getItem() );
								}
							}
						} );

						choiceAction.addPropertyChangeListener( new PropertyChangeListener()
						{
							public void propertyChange( final PropertyChangeEvent e )
							{
								if ( ChoiceAction.SELECTED_VALUE.equals( e.getPropertyName() ) )
								{
									comboBox.setSelectedItem( e.getNewValue() );
								}
							}
						} );

						toolBar.add( comboBox );
					}
					else if ( action instanceof ToggleAction )
					{
						final ToggleAction toggleAction = (ToggleAction)action;

						final JCheckBox checkBox = new JCheckBox( toggleAction );
						checkBox.setModel( new ToggleButtonModel( toggleAction ) );
						checkBox.setOpaque( false );
						toolBar.add( checkBox );
					}
					else
					{
						toolBar.add( action );
					}
				}
			}
		}
	}

	/**
	 * Create a menu item for the specified action.
	 *
	 * @param   action  Action to create menu item for.
	 *
	 * @return  {@link JMenuItem}.
	 */
	public static JMenuItem createMenuItem( final Action action )
	{
		final JMenuItem result;

		if ( action instanceof ToggleAction )
		{
			final ToggleAction toggleAction = (ToggleAction)action;

			final JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem( action );
			checkBoxMenuItem.setModel( new ToggleButtonModel( toggleAction ) );
			result = checkBoxMenuItem;
		}
		else
		{
			result = new JMenuItem( action );
		}

		return result;
	}

	/**
	 * This helper method is used to register a keyboard commands using a {@link
	 * Action} instance. The key stroke of the action is used to trigger the
	 * action.
	 *
	 * @param   target  Target component to register key command action with.
	 * @param   action  Action to register as key command.
	 */
	public static void registerKeyCommand( final JComponent target, final Action action )
	{
		registerKeyCommand( target, JComponent.WHEN_FOCUSED, action );
	}

	/**
	 * This helper method is used to register a keyboard commands using a {@link
	 * Action} instance. The key stroke of the action is used to trigger the
	 * action.
	 *
	 * @param   target      Target component to register key command action with.
	 * @param   condition   Focus condition for input map to use.
	 * @param   action      Action to register as key command.
	 */
	public static void registerKeyCommand( final JComponent target, @MagicConstant ( intValues = { JComponent.WHEN_FOCUSED, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, JComponent.WHEN_IN_FOCUSED_WINDOW } ) final int condition, final Action action )
	{
		if ( ( target != null ) && ( action != null ) )
		{
			final KeyStroke keyStroke = ( KeyStroke ) action.getValue( Action.ACCELERATOR_KEY );
			if ( keyStroke != null )
			{
				registerKeyCommand( target, condition, keyStroke, action );
			}
		}
	}

	/**
	 * This helper method is used to register a keyboard commands using an {@link
	 * Action} instance. The specified key stroke is used to trigger the action.
	 *
	 * @param   target      Target component to register key command action with.
	 * @param   keyStroke   Key stroke to trigger the action.
	 * @param   action      Action to perform when the key is pressed.
	 */
	public static void registerKeyCommand( final JComponent target, final KeyStroke keyStroke, final Action action )
	{
		registerKeyCommand( target, JComponent.WHEN_FOCUSED, keyStroke, action );
	}

	/**
	 * This helper method is used to register a keyboard commands using an {@link
	 * Action} instance. The specified key stroke is used to trigger the action.
	 *
	 * @param   target      Target component to register key command action with.
	 * @param   condition   Focus condition for input map to use.
	 * @param   keyStroke   Key stroke to trigger the action.
	 * @param   action      Action to perform when the key is pressed.
	 */
	public static void registerKeyCommand( final JComponent target, @MagicConstant( intValues = { JComponent.WHEN_FOCUSED, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, JComponent.WHEN_IN_FOCUSED_WINDOW } ) final int condition, final KeyStroke keyStroke, final Action action )
	{
		if ( ( target != null ) && ( keyStroke != null ) && ( action != null ) )
		{
			final String actionCommandKey = (String)action.getValue( Action.ACTION_COMMAND_KEY );
			if ( actionCommandKey != null )
			{
				final InputMap inputMap = target.getInputMap( condition );
				inputMap.put( keyStroke, actionCommandKey );

				final ActionMap actionMap = target.getActionMap();
				actionMap.put( actionCommandKey, action );
			}
		}
	}

	/**
	 * Inner class to use as button model for {@link ToggleAction}s.
	 */
	private static class ToggleButtonModel
		extends JToggleButton.ToggleButtonModel
	{
		/**
		 * Serialized data version.
		 */
		private static final long serialVersionUID = -5085825589585829490L;

		/**
		 * Toogle action this model belongs to.
		 */
		private final ToggleAction _action;

		/**
		 * Construct model.
		 *
		 * @param   action  Toggle action to create model for.
		 */
		ToggleButtonModel( final ToggleAction action )
		{
			_action = action;
		}

		@Override
		public boolean isSelected()
		{
			return _action.getValue();
		}
	}
}
