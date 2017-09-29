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
import java.beans.*;
import javax.swing.*;
import javax.swing.SwingWorker.*;

/**
 * This glass pane can be used to temporarily block user input to a window while
 * a process is runnning. The progress of the process is shown using a progress
 * bar.
 *
 * This class is based on the 'LockingGlassPane' class by Alexander Potochkin
 * (<a href="https://swinghelper.dev.java.net/">https://swinghelper.dev.java.net/</a>,
 * <a href="http://weblogs.java.net/blog/alexfromsun/">http://weblogs.java.net/blog/alexfromsun/</a>).
 *
 * @author  Peter S. Heijnen
 */
public class ProgressGlassPane
	extends JPanel
{
	/**
	 * Progress bar.
	 */
	protected JProgressBar _progressBar = null;

	/**
	 * Component that held keyboard focus before the pane was visible.
	 */
	private Component _oldKeyboardFocusOwner = null;

	/**
	 * Construct glass pane.
	 */
	public ProgressGlassPane()
	{
		super( null );

		initGui();
	}

	/**
	 * Execute swing worker when the given window is opened. This progress pane
	 * will overlay the window when the worker is executed and will be hidden
	 * and detached when the worker is finished. Opening the window a second
	 * time will not retrigger execution of the worker.
	 *
	 * @param   window  Target window for result of work.
	 * @param   worker  Worker to execute when window is opened.
	 * @param   delay   Delay in milliseconds before progress pane will be shown
	 *                  (0 or less => no delay).
	 */
	public void executeWhenOpened( final Window window, final SwingWorker<?, ?> worker, final int delay )
	{
		final RootPaneContainer rootPaneContainer = (RootPaneContainer)window;

		window.addComponentListener( new ComponentAdapter()
		{
			@Override
			public void componentShown( final ComponentEvent e )
			{
				window.removeComponentListener( this );

				if ( delay > 0 )
				{
					worker.execute();

					final Timer timer = new Timer( delay, new ActionListener()
					{
						@Override
						public void actionPerformed( final ActionEvent e )
						{
							if ( !worker.isDone() )
							{
								showWhileRunning( rootPaneContainer, worker );
							}
						}
					} );
					timer.setRepeats( false );
					timer.start();
				}
				else
				{
					showWhileRunning( rootPaneContainer, worker );
					worker.execute();
				}
			}
		} );
	}

	/**
	 * Show progress pane as overlay of the given root container while the given
	 * worker is active. The progress pane will be hidden and detached when the
	 * worker is finished.
	 *
	 * @param   rootPaneContainer   Root pane container (e.g. window/dialog).
	 * @param   worker              Swing worker to monitor.
	 */
	public void showWhileRunning( final RootPaneContainer rootPaneContainer, final SwingWorker<?,?> worker )
	{
		if ( worker.getState() != SwingWorker.StateValue.DONE )
		{
			final Component oldGlassPane = rootPaneContainer.getGlassPane();
			final boolean oldGlassPaneVisible = oldGlassPane.isVisible();
			rootPaneContainer.setGlassPane( this );

			worker.addPropertyChangeListener( new SwingWorkerMonitor()
			{
				@Override
				protected void done()
				{
					super.done();

					worker.removePropertyChangeListener( this );
					rootPaneContainer.setGlassPane( oldGlassPane );
					oldGlassPane.setVisible( oldGlassPaneVisible );
				}
			} );

			if ( !worker.isDone() )
			{
				setVisible( true );
			}
		}
	}

	/**
	 * Bind this pane to the specified swing worker to monitor its progress.
	 *
	 * @param   worker  Swing worker to monitor.
	 */
	public void monitorWorker( final SwingWorker<?, ?> worker )
	{
		worker.addPropertyChangeListener( new SwingWorkerMonitor() );
	}

	/**
	 * Initialize GUI of glass pane.
	 */
	protected void initGui()
	{
		setLayout( new GridBagLayout() );

		final Color background = getBackground();
		setBackground( ( background != null ) ? new Color( background.getRed() / 2, background.getGreen() / 2, background.getBlue() / 2, background.getAlpha() / 2 ) : new Color( 0, 0, 0, 128 ) );

		setCursor( Cursor.getPredefinedCursor( Cursor.WAIT_CURSOR ) );

		final JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate( true );
		_progressBar = progressBar;

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.ipadx = 20;
		gbc.ipady = 10;
		gbc.insets = new Insets( 8, 8, 8, 8 );
		add( progressBar, gbc );
	}

	/**
	 * This convenience method can be use to add a (cancel) button to the pane,
	 * but any component can be added this way.
	 *
	 * @param   component   Button component.
	 */
	public void addButton( final Component component )
	{
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 1;
		gbc.ipadx = 10;
		gbc.insets = new Insets( 8, 8, 8, 8 );

		add( component, gbc );
	}

	/**
	 * Set progress. The given percentage is limited to the 0 to 100 range. The
	 * 'indeterminate' state of the progress bar is set when the parameter is
	 * negative; it is disabled if the parameter is zero or greater.
	 *
	 * @param   progress    Progress percentage.
	 */
	public void setProgress( final int progress )
	{
		final JProgressBar progressBar = _progressBar;
		progressBar.setIndeterminate( ( progress < 0 ) );
		progressBar.setValue( Math.max( 0, Math.min( progress, 100 ) ) );
	}

	/**
	 * Get progress text. If set to {@code null}, no text is shown (this
	 * is the default).
	 *
	 * @param   string  Progress text ({@code null} to disable).
	 */
	public void setProgressString( final String string )
	{
		final JProgressBar progressBar = _progressBar;
		progressBar.setString( string );
		progressBar.setStringPainted( ( string != null ) );
	}

	@Override
	public void setVisible( final boolean visible )
	{
		final boolean wasVisible = isVisible();
		super.setVisible( visible );

		if ( isVisible() != wasVisible )
		{
			if ( visible )
			{
				activate();
			}
			else
			{
				deactivate();
			}
		}
	}

	/**
	 * Activate the glass pane. This is called by {@link #setVisible} when the
	 * glass pane is made visible.
	 */
	protected void activate()
	{
		final JRootPane rootPane = SwingUtilities.getRootPane( this );
		if ( rootPane != null )
		{
			final KeyboardFocusManager keyboardFocusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();

			final Component focusOwner = keyboardFocusManager.getPermanentFocusOwner();
			if ( ( focusOwner != null ) && SwingUtilities.isDescendingFrom( focusOwner, rootPane ) )
			{
				_oldKeyboardFocusOwner = focusOwner;
			}

			final JLayeredPane layeredPane = rootPane.getLayeredPane();
			layeredPane.setVisible( false );

			requestFocusInWindow();
		}
	}

	/**
	 * Deactivate the glass pane. This is called by {@link #setVisible} when the
	 * glass pane is hidden.
	 */
	protected void deactivate()
	{
		final JRootPane rootPane = SwingUtilities.getRootPane( this );
		if ( rootPane != null )
		{
			final JLayeredPane layeredPane = rootPane.getLayeredPane();
			layeredPane.setVisible( true );
		}

		final Component oldKeyboardFocusOwner = _oldKeyboardFocusOwner;
		if ( oldKeyboardFocusOwner != null )
		{
			oldKeyboardFocusOwner.requestFocusInWindow();
			_oldKeyboardFocusOwner = null;
		}
	}

	@Override
	protected void paintComponent( final Graphics g )
	{
		// Explicitly paint the layered pane, because it was made invisible.
		final JRootPane rootPane = SwingUtilities.getRootPane( this );
		if ( rootPane != null )
		{
			// It is important to call print() instead of paint() here
			// because print() doesn't affect the window's double buffer
			final JLayeredPane layeredPane = rootPane.getLayeredPane();
			layeredPane.print( g );
		}

		final Graphics2D g2d = (Graphics2D)g;
		final Paint oldPaint = g2d.getPaint();
		g2d.setColor( getBackground() );
		g2d.fillRect( 0, 0, getWidth(), getHeight() );
		g2d.setPaint( oldPaint );
	}

	/**
	 * Property listener that monitors the activity of a {@link SwingWorker}
	 * and shows its progress in the {@link ProgressGlassPane} to which it
	 * belongs.
	 */
	public class SwingWorkerMonitor
		implements PropertyChangeListener
	{
		@Override
		public void propertyChange( final PropertyChangeEvent evt )
		{
			final String propertyName = evt.getPropertyName();

			if ( "progress".equals( propertyName ) )
			{
				final int progress = (Integer)evt.getNewValue();

				setProgress( progress );
			}
			else if ( "state".equals( propertyName ) )
			{
				final StateValue state = (StateValue)evt.getNewValue();

				if ( state == StateValue.STARTED )
				{
					started();
				}
				else if ( state == StateValue.DONE )
				{
					done();
				}
			}
		}

		/**
		 * Called when work is started.
		 */
		protected void started()
		{
			setVisible( true );
		}

		/**
		 * Called when work is done.
		 */
		protected void done()
		{
			setVisible( false );
		}
	}
}
