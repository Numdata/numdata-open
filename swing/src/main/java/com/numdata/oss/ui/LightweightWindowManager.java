/*
 * Copyright (c) 2014-2017, Numdata BV, The Netherlands.
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
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Window manager for lightweight windows, based on {@link JDesktopPane} and
 * {@link JInternalFrame}. The desktop pane is installed in the glass pane and
 * given a transparent background. The internal frames then appear on top of
 * other components, just like regular windows would.
 *
 * Since JDK 7 update 19, mixing of lightweight and heavyweight components
 * should work automatically. But there are still issues with that at this time,
 * so hiding heavyweights (such as certain 3D views) is recommended. To do this,
 * simply write a {@link InternalFrameListener} and add it to this window
 * manager.
 *
 * @author Gerrit Meinders
 */
public class LightweightWindowManager
{
	/**
	 * Root pane that contains the desktop pane.
	 */
	private JRootPane _rootPane;

	/**
	 * Desktop pane that contains the windows.
	 */
	private final JDesktopPane _desktop;

	/**
	 * Manages visibility of the desktop pane and forwards events to registered
	 * listeners.
	 */
	private final InternalFrameListener _internalFrameListener;

	/**
	 * Listeners that receive events from all windows.
	 */
	private List<InternalFrameListener> _listeners = new ArrayList<InternalFrameListener>();

	/**
	 * Constructs a new context.
	 *
	 * @param rootPane Root pane that will contain the window.
	 */
	public LightweightWindowManager( final JRootPane rootPane )
	{
		_rootPane = rootPane;

		final JDesktopPane desktop = new JDesktopPane()
		{
			@Override
			protected void paintComponent( final Graphics g )
			{
				g.setColor( new Color( 0, true ) );
				g.fillRect( 0, 0, getWidth(), getHeight() );
			}
		};
		desktop.setOpaque( false );

		/*
		 * Block mouse events to components beneath the glass pane.
		 */
		final MouseAdapter mouseListener = new MouseAdapter()
		{
		};
		desktop.addMouseListener( mouseListener );
		desktop.addMouseWheelListener( mouseListener );
		desktop.addMouseMotionListener( mouseListener );

		_desktop = desktop;

		_internalFrameListener = new FrameListener();
	}

	/**
	 * Returns the number of visible windows.
	 *
	 * @return Number of visible windows.
	 */
	public int getVisibleWindowCount()
	{
		int result = 0;
		for ( int i = 0, componentCount = _desktop.getComponentCount(); i < componentCount; i++ )
		{
			final Component component = _desktop.getComponent( i );
			if ( component instanceof JInternalFrame )
			{
				final JInternalFrame frame = (JInternalFrame)component;
				if ( frame.isVisible() )
				{
					result++;
				}
			}
		}
		return result;
	}

	/**
	 * Returns the width of the desktop containing the lightweight windows.
	 *
	 * @return Width of the desktop.
	 */
	public int getWidth()
	{
		return _rootPane.getWidth();
	}

	/**
	 * Returns the height of the desktop containing the lightweight windows.
	 *
	 * @return Height of the desktop.
	 */
	public int getHeight()
	{
		return _rootPane.getHeight();
	}

	/**
	 * Adds a window. The window is automatically removed when closed.
	 *
	 * @param window Window to add.
	 */
	public void addWindow( final JInternalFrame window )
	{
		window.addInternalFrameListener( _internalFrameListener );
		_desktop.add( window );
	}

	/**
	 * Removes a window.
	 *
	 * @param window Window to remove.
	 */
	private void removeWindow( final JInternalFrame window )
	{
		_desktop.remove( window );
		window.removeInternalFrameListener( _internalFrameListener );
	}

	/**
	 * Adds a listener for all managed windows.
	 *
	 * @param listener Listener to add.
	 */
	public void addListener( final InternalFrameListener listener )
	{
		_listeners.add( listener );
	}

	/**
	 * Removes a listener for all managed windows.
	 *
	 * @param listener Listener to remove.
	 */
	public void removeListener( final InternalFrameListener listener )
	{
		_listeners.remove( listener );
	}

	/**
	 * Manages visibility of the desktop pane and forwards events to registered
	 * listeners.
	 */
	private class FrameListener
	implements InternalFrameListener
	{
		public void internalFrameOpened( final InternalFrameEvent e )
		{
			final JInternalFrame window = e.getInternalFrame();

			final JRootPane rootPane = _rootPane;
			rootPane.setGlassPane( _desktop );

			window.pack();
			window.setLocation( ( rootPane.getWidth() - window.getWidth() ) / 2, ( rootPane.getHeight() - window.getHeight() ) / 2 );
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					window.pack();
				}
			} );

			_desktop.setVisible( true );

			window.setFocusCycleRoot( true );
			window.setFocusTraversalPolicy( new LayoutFocusTraversalPolicy() );
			try
			{
				window.setSelected( true );
			}
			catch ( PropertyVetoException ignored )
			{
			}

			final FocusManager focusManager = FocusManager.getCurrentManager();
			focusManager.focusNextComponent( window );

			for ( final InternalFrameListener listener : _listeners )
			{
				listener.internalFrameOpened( e );
			}
		}

		public void internalFrameClosing( final InternalFrameEvent e )
		{
			for ( final InternalFrameListener listener : _listeners )
			{
				listener.internalFrameClosing( e );
			}
		}

		public void internalFrameClosed( final InternalFrameEvent e )
		{
			removeWindow( e.getInternalFrame() );

			if ( getVisibleWindowCount() == 0 )
			{
				_desktop.setVisible( false );
			}

			for ( final InternalFrameListener listener : _listeners )
			{
				listener.internalFrameClosed( e );
			}
		}

		public void internalFrameIconified( final InternalFrameEvent e )
		{
			for ( final InternalFrameListener listener : _listeners )
			{
				listener.internalFrameIconified( e );
			}
		}

		public void internalFrameDeiconified( final InternalFrameEvent e )
		{
			for ( final InternalFrameListener listener : _listeners )
			{
				listener.internalFrameDeiconified( e );
			}
		}

		public void internalFrameActivated( final InternalFrameEvent e )
		{
			for ( final InternalFrameListener listener : _listeners )
			{
				listener.internalFrameActivated( e );
			}
		}

		public void internalFrameDeactivated( final InternalFrameEvent e )
		{
			for ( final InternalFrameListener listener : _listeners )
			{
				listener.internalFrameDeactivated( e );
			}
		}
	}
}
