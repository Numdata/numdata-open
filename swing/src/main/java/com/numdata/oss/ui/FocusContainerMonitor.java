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

/**
 * This class monitors a component hierarchy and forwards any focus events from
 * that hierarchy to a given listener.
 *
 * @author  Peter S. Heijnen
 */
public class FocusContainerMonitor
	implements ContainerListener
{
	/**
	 * Listener for receiving keyboard focus events on the component hierarchy.
	 */
	private FocusListener _focusListener = null;

	/**
	 * Construct monitor.
	 *
	 * @param   component       Content component.
	 * @param   focusListener   Listener for keyboard focus events.
	 */
	public FocusContainerMonitor( final Component component, final FocusListener focusListener )
	{
		init( component, focusListener );
	}

	/**
	 * Construct uninitialized monitor.
	 */
	protected FocusContainerMonitor()
	{
	}

	/**
	 * Initialize monitor.
	 *
	 * @param   component       Content component.
	 * @param   focusListener   Listener for keyboard focus events.
	 */
	protected void init( final Component component, final FocusListener focusListener )
	{
		_focusListener = focusListener;
		attachTo( component );
	}

	/**
	 * Test whether a component or one of its descendants has focus.
	 *
	 * @param   component   Component to test.
	 *
	 * @return  {@code true} if the given component or one of its
	 *          descendants has focus.
	 */
	public static boolean containsFocus( final Component component )
	{
		final KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

		Component focusContainer = focusManager.getFocusOwner();

		while ( ( focusContainer != null ) && ( focusContainer != component ) && !( focusContainer instanceof Window ) )
		{
			focusContainer = focusContainer.getParent();
		}

		return ( focusContainer == component );
	}

	@Override
	public void componentAdded( final ContainerEvent e )
	{
		attachTo( e.getChild() );
	}

	@Override
	public void componentRemoved( final ContainerEvent e )
	{
		detachFrom( e.getChild() );
	}

	/**
	 * Attach listeners to the given component and its descendants (if any).
	 *
	 * @param   component   Component to attach listeners to.
	 */
	protected void attachTo( final Component component )
	{
		component.addFocusListener( _focusListener );

		if ( component instanceof Container )
		{
			final Container container = (Container)component;
			container.addContainerListener( this );

			for ( final Component child : container.getComponents() )
			{
				attachTo( child );
			}
		}
	}

	/**
	 * Detach listeners from the given component and its descendants (if
	 * any).
	 *
	 * @param   component   Component to detach listeners from.
	 */
	protected void detachFrom( final Component component )
	{
		component.removeFocusListener( _focusListener );

		if ( component instanceof Container )
		{
			final Container container = (Container)component;
			container.removeContainerListener( this );

			for ( final Component child : container.getComponents() )
			{
				detachFrom( child );
			}
		}
	}
}
