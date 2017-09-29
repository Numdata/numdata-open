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
import java.util.*;
import javax.swing.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class provides functionality for creating a dialog with a standardized
 * layout and functionality.
 *
 * @author Peter S. Heijnen
 * @see StandardContentPane
 */
public class StandardDialog
extends JDialog
implements ActionListener
{
	/**
	 * Action command associated with the 'OK' button.
	 */
	public static final String OK_ACTION = "ok";

	/**
	 * Action command associated with the 'Cancel' button.
	 */
	public static final String CANCEL_ACTION = "cancel";

	/**
	 * Action command associated with the 'Apply' button.
	 */
	public static final String APPLY_ACTION = "apply";

	/**
	 * Button bit constant for 'OK' button. This can be combined with other
	 * button constants using logical OR's to select the buttons to include.
	 */
	public static final int OK_BUTTON = 1;

	/**
	 * Button bit constant for 'Cancel' button. This can be combined with other
	 * button constants using logical OR's to select the buttons to include.
	 */
	public static final int CANCEL_BUTTON = 2;

	/**
	 * Button bit constant for 'Apply' button. This can be combined with other
	 * button constants using logical OR's to select the buttons to include.
	 */
	public static final int APPLY_BUTTON = 4;

	/**
	 * Convenenience value for a button set containing the 'OK' and 'Cancel'
	 * buttons.
	 */
	public static final int OK_CANCEL_BUTTONS = OK_BUTTON | CANCEL_BUTTON;

	/**
	 * Convenenience value for a button set containing the 'OK', 'Apply', and
	 * 'Cancel' buttons.
	 */
	public static final int OK_APPLY_CANCEL_BUTTONS = OK_BUTTON | APPLY_BUTTON | CANCEL_BUTTON;

	/**
	 * Convenenience value for a button set containing the 'OK' and 'Apply'
	 * buttons.
	 */
	public static final int OK_APPLY_BUTTONS = OK_BUTTON | APPLY_BUTTON;

	/**
	 * Resource bundle for this dialog.
	 */
	protected final ResourceBundle _res;

	/**
	 * Flag to indicate that the {@link #doApply()} method was called.
	 *
	 * @see #doApply
	 */
	private boolean _applied;

	/**
	 * Flag to indicate that the {@link #doCancel()} method was called.
	 *
	 * @see #doCancel
	 */
	private boolean _cancelled;

	/**
	 * 'OK' button.
	 */
	private JButton _okButton = null;

	/**
	 * Construct dialog.
	 *
	 * @param owner        Parent component for this dialog.
	 * @param modalityType Modality type for this dialog.
	 * @param locale       Locale to use for internationalized messages.
	 * @param buttons      Buttons to include in dialog.
	 */
	public StandardDialog( @Nullable final Window owner, @NotNull final ModalityType modalityType, @NotNull final Locale locale, final int buttons )
	{
		super( owner, modalityType );

		setLocale( locale );
		_res = ResourceBundleTools.getBundleHierarchy( getClass(), locale );
		_applied = false;
		_cancelled = false;

		setLocale( locale );
		setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
		setTitle( ResourceBundleTools.getString( _res, "title", null ) );

		final StandardContentPane contentPane = new StandardContentPane( new JPanel( new BorderLayout() ) );
		addButtons( contentPane, buttons );
		super.setContentPane( contentPane );

		if ( _okButton != null )
		{
			final JRootPane rootPane = contentPane.getRootPane();
			rootPane.setDefaultButton( _okButton );
		}

		enableEvents( WindowEvent.WINDOW_EVENT_MASK | ComponentEvent.COMPONENT_EVENT_MASK );
	}

	/**
	 * Handle 'Apply' action.
	 *
	 * The default implementation simply sets the '{@code applied}' flag.
	 */
	protected void doApply()
	{
		_applied = true;
	}

	/**
	 * This method is called when the dialog is closed.
	 *
	 * The default implementation simply calls {@link #dispose()}.
	 *
	 * @see #doOk
	 * @see #doCancel
	 */
	protected void doClose()
	{
		dispose();
	}

	/**
	 * Handle 'Cancel' action (from button).
	 *
	 * The default implementation sets the '{@code cancelled}' flag and calls
	 * {@link #doClose()}.
	 */
	protected void doCancel()
	{
		_cancelled = true;
		doClose();
	}

	/**
	 * Handle 'OK' action (from button).
	 *
	 * The default implementation calls {@link #doApply()} and then, if {@link
	 * #isApplied()} returns {@code true}, {@link #doClose()}.
	 */
	protected void doOk()
	{
		doApply();
		if ( isApplied() )
		{
			doClose();
		}
	}

	/**
	 * Add buttons to content pane.
	 *
	 * @param contentPane Content pane to add buttons to.
	 * @param buttons     Buttons argument from constructor.
	 */
	private void addButtons( final StandardContentPane contentPane, final int buttons )
	{
		final ResourceBundle res = _res;

		if ( ( buttons & OK_BUTTON ) != 0 )
		{
			final JButton button = contentPane.addButton( res, OK_ACTION, this );
			_okButton = button;

			final InputMap inputMap = button.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
			inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK, false ), "pressed" );
			inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK, true ), "released" );
		}

		if ( ( buttons & CANCEL_BUTTON ) != 0 )
		{
			final JButton button = contentPane.addButton( res, CANCEL_ACTION, this );

			final InputMap inputMap = button.getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );
			inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0, false ), "pressed" );
			inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0, true ), "released" );
		}

		if ( ( buttons & APPLY_BUTTON ) != 0 )
		{
			contentPane.addButton( res, APPLY_ACTION, this );
		}
	}

	/**
	 * Get flag that indicates if the {@link #doApply()} method was called.
	 *
	 * This flag is reset when the dialog is (re)shown.
	 *
	 * @return {@code true} if the {@link #doApply()} method was called; {@code
	 * false} otherwise.
	 *
	 * @see #doApply
	 * @see #isCancelled
	 */
	public boolean isApplied()
	{
		return _applied;
	}

	/**
	 * Get flag that indicates if the {@link #doCancel()} method was called.
	 *
	 * This flag is reset when the dialog is (re)shown.
	 *
	 * @return {@code true} if the {@link #doCancel()} method was called; {@code
	 * false} otherwise.
	 *
	 * @see #doApply
	 * @see #isCancelled
	 */
	public boolean isCancelled()
	{
		return _cancelled;
	}

	/**
	 * Get (inner) content of dialog.
	 *
	 * The default implementation creates an empty panel with a {@link
	 * BorderLayout}.
	 *
	 * @return Content component.
	 *
	 * @see #getContent
	 */
	public JComponent getContent()
	{
		final StandardContentPane contentPane = getStandardContentPane();
		return contentPane.getContent();
	}

	/**
	 * Get standard content pane used by this dialog.
	 *
	 * @return Standard content pane of this dialog.
	 */
	public StandardContentPane getStandardContentPane()
	{
		return (StandardContentPane)getContentPane();
	}

	/**
	 * Reset state of dialog. This resets the '{@code applied}' and '{@code
	 * cancelled}' flags. It may also be used to revert the dialog content to a
	 * previous/pre-defined state.
	 */
	public void reset()
	{
		_cancelled = false;
		_applied = false;
	}

	/**
	 * Handle action events from buttons. This will call the {@code do...()}
	 * method corresponding to the button from which the event originated.
	 *
	 * @param event Action event.
	 */
	@Override
	public void actionPerformed( final ActionEvent event )
	{
		final String command = event.getActionCommand();

		if ( OK_ACTION.equals( command ) )
		{
			doOk();
		}
		else if ( CANCEL_ACTION.equals( command ) )
		{
			doCancel();
		}
		else if ( APPLY_ACTION.equals( command ) )
		{
			doApply();
		}
	}

	/**
	 * Reset state information when the dialog is shown.
	 *
	 * @param event Component event.
	 */
	@Override
	protected void processComponentEvent( final ComponentEvent event )
	{
		super.processComponentEvent( event );

		if ( event.getID() == ComponentEvent.COMPONENT_SHOWN )
		{
			reset();
		}
	}

	/**
	 * Clicking the 'close button' causes the {@link #doCancel()} method to be
	 * called.
	 *
	 * @param event Window event.
	 */
	@Override
	protected void processWindowEvent( final WindowEvent event )
	{
		super.processWindowEvent( event );

		if ( event.getID() == WindowEvent.WINDOW_CLOSING )
		{
			doCancel();
		}
	}

	/**
	 * This method should never be called. The standard content pane must be
	 * used. See {@link #getStandardContentPane()}.
	 *
	 * @param contentPane Dummy argument.
	 */
	@Override
	public void setContentPane( final Container contentPane )
	{
		throw new RuntimeException( "Use getStandardContentPane() instead." );
	}
}
