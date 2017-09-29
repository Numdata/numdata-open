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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.*;

/**
 * The Tristate Checkbox is widely used to represent an undetermined state of a
 * check box.
 *
 * See: http://www.javaspecialists.co.za/archive/Issue145.html
 *
 * @author Dr. Heinz M. Kabutz
 */
@SuppressWarnings ( "JavaDoc" )
public class TristateCheckBox
extends JCheckBox
{
	public enum TristateState
	{
		SELECTED
		{
			@Override
			public TristateState next()
			{
				return INDETERMINATE;
			}
		},

		INDETERMINATE
		{
			@Override
			public TristateState next()
			{
				return DESELECTED;
			}
		},

		DESELECTED
		{
			@Override
			public TristateState next()
			{
				return SELECTED;
			}
		};

		public abstract TristateState next();
	}

	// Listener on model changes to maintain correct focusability
	private final ChangeListener _enableListener = new ChangeListener()
	{
		public void stateChanged( final ChangeEvent e )
		{
			setFocusable( getModel().isEnabled() );
		}
	};

	public TristateCheckBox()
	{
		this( null, null, TristateState.DESELECTED );
	}

	public TristateCheckBox( final String text )
	{
		this( text, null, TristateState.DESELECTED );
	}

	public TristateCheckBox( final String text, final Icon icon, final TristateState initial )
	{
		super( text, icon );

		//Set default single model
		setModel( new TristateButtonModel( initial ) );

		// override action behaviour
		super.addMouseListener( new MouseAdapter()
		{
			@Override
			public void mousePressed( final MouseEvent e )
			{
				iterateState();
			}
		} );

		final ActionMap actions = new ActionMapUIResource();
		actions.put( "pressed", new AbstractAction()
		{
			public void actionPerformed( final ActionEvent e )
			{
				iterateState();
			}
		} );
		actions.put( "released", null );

		SwingUtilities.replaceUIActionMap( this, actions );
	}

	// Next two methods implement new API by delegation to model
	public void setIndeterminate()
	{
		getTristateModel().setIndeterminate();
	}

	public boolean isIndeterminate()
	{
		return getTristateModel().isIndeterminate();
	}

	public TristateState getState()
	{
		return getTristateModel().getState();
	}

	//Overrides superclass method
	@Override
	public void setModel( final ButtonModel newModel )
	{
		super.setModel( newModel );

		//Listen for enable changes
		if ( model instanceof TristateButtonModel )
		{
			model.addChangeListener( _enableListener );
		}
	}

	//Empty override of superclass method
	@Override
	public void addMouseListener( final MouseListener listener )
	{
	}

	// Mostly delegates to model
	private void iterateState()
	{
		//Maybe do nothing at all?
		if ( getModel().isEnabled() )
		{
			grabFocus();

			// Iterate state
			getTristateModel().iterateState();

			// Fire ActionEvent
			int modifiers = 0;
			final AWTEvent currentEvent = EventQueue.getCurrentEvent();
			if ( currentEvent instanceof InputEvent )
			{
				modifiers = ( (InputEvent)currentEvent ).getModifiers();
			}
			else if ( currentEvent instanceof ActionEvent )
			{
				modifiers = ( (ActionEvent)currentEvent ).getModifiers();
			}
			fireActionPerformed( new ActionEvent( this, ActionEvent.ACTION_PERFORMED, getText(), System.currentTimeMillis(), modifiers ) );
		}
	}

	//Convenience cast
	public TristateButtonModel getTristateModel()
	{
		return (TristateButtonModel)getModel();
	}

	public class TristateButtonModel
	extends ToggleButtonModel
	{
		private TristateState _state = TristateState.DESELECTED;

		public TristateButtonModel()
		{
			this( TristateState.DESELECTED );
		}

		public TristateButtonModel( final TristateState state )
		{
			setState( state );
		}

		public void setIndeterminate()
		{
			setState( TristateState.INDETERMINATE );
		}

		public boolean isIndeterminate()
		{
			return ( _state == TristateState.INDETERMINATE );
		}

		// Overrides of superclass methods
		@Override
		public void setEnabled( final boolean enabled )
		{
			super.setEnabled( enabled );
			// Restore state display
			displayState();
		}

		@Override
		public void setSelected( final boolean selected )
		{
			setState( selected ? TristateState.SELECTED : TristateState.DESELECTED );
		}

		// Empty overrides of superclass methods
		@Override
		public void setArmed( final boolean armed )
		{
		}

		@Override
		public void setPressed( final boolean pressed )
		{
		}

		void iterateState()
		{
			setState( _state.next() );
		}

		private void setState( final TristateState state )
		{
			//Set internal state
			_state = state;
			displayState();
			if ( state == TristateState.INDETERMINATE && isEnabled() )
			{
				// force the events to fire

				// Send ChangeEvent
				fireStateChanged();

				// Send ItemEvent
				final int indeterminate = 3;
				fireItemStateChanged( new ItemEvent( this, ItemEvent.ITEM_STATE_CHANGED, this, indeterminate ) );
			}
		}

		private void displayState()
		{
			final TristateState state = _state;
			super.setSelected( state != TristateState.DESELECTED );
			super.setArmed( state == TristateState.INDETERMINATE );
			super.setPressed( state == TristateState.INDETERMINATE );

		}

		public TristateState getState()
		{
			return _state;
		}
	}
}


