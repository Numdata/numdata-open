/*
 * Copyright (c) 2004-2019, Numdata BV, The Netherlands.
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
import java.util.*;

import com.numdata.oss.event.EventListener;

/**
 * This class implements various (AWT) event listener interfaces and protects
 * their handlers against causing havoc when they throw an exception. The thrown
 * exception is caught and an error dialog will be shown to the user.
 *
 * @see     ActionListener
 * @see     ComponentListener
 * @see     FocusListener
 * @see     KeyListener
 * @see     MouseListener
 * @see     MouseMotionListener
 *
 * @author  Peter S. Heijnen
 */
public class ProtectedEventListener
	implements ActionListener, ComponentListener, EventListener, FocusListener, KeyListener, MouseListener, MouseMotionListener
{
	/**
	 * This method is called when an exception occurs in one of the protected
	 * event handlers.
	 *
	 * @param   event       Event that was being handled.
	 * @param   problem     Exception that occurred during the one of the handlers.
	 */
	protected void handleProblem( final EventObject event , final Throwable problem )
	{
		WindowTools.showErrorDialog( null , problem , ProtectedEventListener.class );
	}

	public final void actionPerformed( final ActionEvent event )
	{
		try
		{
			protectedActionPerformed( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedActionPerformed( final ActionEvent event )
		throws Exception
	{
	}

	public final void componentResized( final ComponentEvent event )
	{
		try
		{
			protectedComponentResized( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedComponentResized( final ComponentEvent event )
		throws Exception
	{
	}

	public final void componentMoved( final ComponentEvent event )
	{
		try
		{
			protectedComponentMoved( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedComponentMoved( final ComponentEvent event )
		throws Exception
	{
	}

	public final void componentShown( final ComponentEvent event )
	{
		try
		{
			protectedComponentShown( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedComponentShown( final ComponentEvent event )
		throws Exception
	{
	}

	public final void componentHidden( final ComponentEvent event )
	{
		try
		{
			protectedComponentHidden( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedComponentHidden( final ComponentEvent event )
		throws Exception
	{
	}

	public final void focusGained( final FocusEvent event )
	{
		try
		{
			protectedFocusGained( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedFocusGained( final FocusEvent event )
		throws Exception
	{
	}

	public final void focusLost( final FocusEvent event )
	{
		try
		{
			protectedFocusLost( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedFocusLost( final FocusEvent event )
		throws Exception
	{
	}

	public final void handleEvent( final EventObject event )
	{
		try
		{
			protectedHandleEvent( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	@SuppressWarnings( { "CastConflictsWithInstanceof", "JavaDoc" } )
	protected void protectedHandleEvent( final EventObject event )
		throws Exception
	{
		if ( event instanceof AWTEvent )
		{
			switch ( ((AWTEvent)event).getID() )
			{
				case ActionEvent.ACTION_PERFORMED :
					protectedActionPerformed( (ActionEvent)event );
					break;

				case ComponentEvent.COMPONENT_HIDDEN :
					protectedComponentHidden( (ComponentEvent)event );
					break;

				case ComponentEvent.COMPONENT_MOVED :
					protectedComponentMoved( (ComponentEvent)event );
					break;

				case ComponentEvent.COMPONENT_RESIZED :
					protectedComponentResized( (ComponentEvent)event );
					break;

				case ComponentEvent.COMPONENT_SHOWN :
					protectedComponentShown( (ComponentEvent)event );
					break;

				case FocusEvent.FOCUS_GAINED :
					protectedFocusGained( (FocusEvent)event );
					break;

				case FocusEvent.FOCUS_LOST :
					protectedFocusLost( (FocusEvent)event );
					break;

				case KeyEvent.KEY_PRESSED :
					protectedKeyPressed( (KeyEvent)event );
					break;

				case KeyEvent.KEY_RELEASED :
					protectedKeyReleased( (KeyEvent)event );
					break;

				case KeyEvent.KEY_TYPED :
					protectedKeyTyped( (KeyEvent)event );
					break;

				case MouseEvent.MOUSE_CLICKED :
					protectedMouseClicked( (MouseEvent)event );
					break;

				case MouseEvent.MOUSE_DRAGGED :
					protectedMouseDragged( (MouseEvent)event );
					break;

				case MouseEvent.MOUSE_ENTERED :
					protectedMouseEntered( (MouseEvent)event );
					break;

				case MouseEvent.MOUSE_EXITED :
					protectedMouseExited( (MouseEvent)event );
					break;

				case MouseEvent.MOUSE_MOVED :
					protectedMouseMoved( (MouseEvent)event );
					break;

				case MouseEvent.MOUSE_PRESSED :
					protectedMousePressed( (MouseEvent)event );
					break;

				case MouseEvent.MOUSE_RELEASED :
					protectedMousePressed( (MouseEvent)event );
					break;
			}
		}
	}

	public final void mouseClicked( final MouseEvent event )
	{
		try
		{
			protectedMouseClicked( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedMouseClicked( final MouseEvent event )
		throws Exception
	{
	}

	public final void mousePressed( final MouseEvent event )
	{
		try
		{
			protectedMousePressed( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedMousePressed( final MouseEvent event )
		throws Exception
	{
	}

	public final void mouseReleased( final MouseEvent event )
	{
		try
		{
			protectedMouseReleased( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedMouseReleased( final MouseEvent event )
		throws Exception
	{
	}

	public final void mouseEntered( final MouseEvent event )
	{
		try
		{
			protectedMouseEntered( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedMouseEntered( final MouseEvent event )
		throws Exception
	{
	}

	public final void mouseExited( final MouseEvent event )
	{
		try
		{
			protectedMouseExited( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedMouseExited( final MouseEvent event )
		throws Exception
	{
	}

	public final void mouseDragged( final MouseEvent event )
	{
		try
		{
			protectedMouseDragged( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedMouseDragged( final MouseEvent event )
		throws Exception
	{
	}

	public final void mouseMoved( final MouseEvent event )
	{
		try
		{
			protectedMouseMoved( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedMouseMoved( final MouseEvent event )
		throws Exception
	{
	}

	public final void keyTyped( final KeyEvent event )
	{
		try
		{
			protectedKeyTyped( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedKeyTyped( final KeyEvent event )
		throws Exception
	{
	}

	public final void keyPressed( final KeyEvent event )
	{
		try
		{
			protectedKeyPressed( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedKeyPressed( final KeyEvent event )
		throws Exception
	{
	}

	public final void keyReleased( final KeyEvent event )
	{
		try
		{
			protectedKeyReleased( event );
		}
		catch ( final Throwable problem )
		{
			handleProblem( event , problem );
		}
	}

	protected void protectedKeyReleased( final KeyEvent event )
		throws Exception
	{
	}
}
