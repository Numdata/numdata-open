/*
 * Copyright (c) 2004-2020, Numdata BV, The Netherlands.
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

import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.numdata.oss.*;
import com.numdata.oss.measurement.*;
import org.jetbrains.annotations.*;

/**
 * This implementation of the {@code Action} interface provides getters and
 * setters for the pre-defined action properties and a convenience constructor
 * to set these properties from a resource bundle.
 *
 * @author Peter S. Heijnen
 * @see Action
 */
public abstract class BasicAction
extends AbstractAction
implements Runnable
{
	/**
	 * Resource bundle.
	 */
	protected final ResourceBundle _bundle;

	/**
	 * Measurement recorder.
	 */
	private MeasurementRecorder _measurementRecorder = null;

	/**
	 * Construct basic action with settings from resource bundle using the
	 * specified key. The following properties are set:
	 *
	 * <table summary="Resource entries used for action">
	 * <tr><th>resource key</th><th>assigned to property</th><th>default</th></tr>
	 * <tr><td>{key}</td><td>name</td><td>{key}</td></tr>
	 * <tr><td>{key}Icon</td><td>smallIcon</td><td>none</td></tr>
	 * <tr><td>{key}Tip</td><td>shortDescription</td><td>none</td></tr>
	 * <tr><td>{key}KeyStroke</td><td>keyboardAccellerator</td><td>none</td></tr>
	 * <tr><td>{key}Mnemonic</td><td>mnemonicKey</td><td>none</td></tr>
	 * </table>
	 *
	 * @param bundle Resource bundle to get settings from.
	 * @param key    Resource key to use (also used as action command).
	 */
	protected BasicAction( @NotNull final ResourceBundle bundle, @NotNull final String key )
	{
		_bundle = bundle;
		setActionCommand( key );

		setName( ResourceBundleTools.getString( bundle, key, key ) );
		setDefaultIcon();
		setShortDescription( ResourceBundleTools.getString( bundle, key + "Tip", null ) );
		setAcceleratorKey( ResourceBundleTools.getString( bundle, key + "KeyStroke", null ) );
		setMnemonicKey( ResourceBundleTools.getString( bundle, key + "Mnemonic", null ) );
	}

	public MeasurementRecorder getMeasurementRecorder()
	{
		return _measurementRecorder;
	}

	public void setMeasurementRecorder( final MeasurementRecorder measurementRecorder )
	{
		_measurementRecorder = measurementRecorder;
	}

	public ResourceBundle getBundle()
	{
		return _bundle;
	}

	/**
	 * Set default icon for action.
	 */
	protected void setDefaultIcon()
	{
		final ResourceBundle bundle = _bundle;
		final String key = getActionCommand();

		final String iconPath = ResourceBundleTools.getString( bundle, key + "Icon", null );
		if ( iconPath != null )
		{
			final int iconSize = ResourceBundleTools.getInt( bundle, key + "IconSize", -1 );
			final int iconWidth = ResourceBundleTools.getInt( bundle, key + "IconWidth", iconSize );
			final int iconHeight = ResourceBundleTools.getInt( bundle, key + "IconHeight", iconSize );

			setSmallIcon( iconPath, iconWidth, iconHeight );
		}
	}

	/**
	 * Forward execution requests for action to {@link #run} method. This
	 * protects callers against exceptions thrown by this action. Any such
	 * exceptions will be caught and presented in a 'crash dialog' to the user.
	 *
	 * @see #run
	 */
	public void actionPerformed( final ActionEvent event )
	{
		if ( ( event != null ) && ( _measurementRecorder != null ) )
		{
			_measurementRecorder.recordEvent( getActionCommand(), SwingMeasurements.getInteractionType( event ) );
		}

		try
		{
			run();
		}
		catch ( final Throwable problem )
		{
			WindowTools.showErrorDialog( null, problem, getClass() );
		}
	}

	/**
	 * Get keystroke used as keyboard accelerator.
	 *
	 * @return Keystroke used as keyboard accelerator.
	 *
	 * @see #ACCELERATOR_KEY
	 * @see KeyStroke
	 */
	public KeyStroke getAcceleratorKey()
	{
		return (KeyStroke)getValue( ACCELERATOR_KEY );
	}

	/**
	 * Set key stroke to use as keyboard accelerator. If {@code null} is
	 * specified, any existing value is lost (removed).
	 *
	 * @param key Keystroke to use as keyboard accelerator.
	 *
	 * @see #ACCELERATOR_KEY
	 * @see KeyStroke
	 */
	public void setAcceleratorKey( final KeyStroke key )
	{
		putValue( ACCELERATOR_KEY, key );
	}

	/**
	 * Set key stroke to use as keyboard accelerator from a string in a format
	 * compatible with {@code KeyStroke#getKeyStroke(String)}. If {@code null}
	 * or an incorrectly formatted string is specified, any existing value is
	 * lost (removed).
	 *
	 * @param key String representation of key stroke.
	 *
	 * @see #ACCELERATOR_KEY
	 * @see KeyStroke
	 * @see KeyStroke#getKeyStroke(String)
	 */
	public void setAcceleratorKey( final String key )
	{
		if ( key != null )
		{
			final KeyStroke keyStroke = KeyStroke.getKeyStroke( key );
			if ( keyStroke == null )
			{
				throw new IllegalArgumentException( "Invalid accelerator for " + getName() + ": " + key );
			}
			setAcceleratorKey( keyStroke );
		}
	}

	/**
	 * Get command string associated with action.
	 *
	 * @return Command string associated with action.
	 *
	 * @see #ACTION_COMMAND_KEY
	 * @see ActionEvent
	 */
	public String getActionCommand()
	{
		return (String)getValue( ACTION_COMMAND_KEY );
	}

	/**
	 * Set command string to associate with action. If {@code null} is
	 * specified, any existing value is lost (removed).
	 *
	 * @param command Command string to associate with action.
	 *
	 * @see #ACTION_COMMAND_KEY
	 * @see ActionEvent
	 */
	private void setActionCommand( final String command )
	{
		putValue( ACTION_COMMAND_KEY, command );
	}

	/**
	 * Get name of action. Used for a menu or button text.
	 *
	 * @return Name of action.
	 *
	 * @see #NAME
	 */
	public String getName()
	{
		return (String)getValue( NAME );
	}

	/**
	 * Set name of action. Used for a menu or button text. If {@code null} is
	 * specified, any existing value is lost (removed).
	 *
	 * @param name Name of action.
	 *
	 * @see #NAME
	 */
	public void setName( final String name )
	{
		putValue( NAME, name );
	}

	/**
	 * Get long description of action. Could be used for context-sensitive
	 * help.
	 *
	 * @return Long description.
	 *
	 * @see #LONG_DESCRIPTION
	 */
	public String getLongDescription()
	{
		return (String)getValue( LONG_DESCRIPTION );
	}

	/**
	 * Set long description of action. Could be used for context-sensitive help.
	 * If {@code null} is specified, any existing value is lost (removed).
	 *
	 * @param description Long description.
	 *
	 * @see #LONG_DESCRIPTION
	 */
	public void setLongDescription( final String description )
	{
		putValue( LONG_DESCRIPTION, description );
	}

	/**
	 * Get integer key code used as mnemonic (e.g. key to select menu item).
	 *
	 * @return Integer key code used as mnemonic.
	 *
	 * @see #MNEMONIC_KEY
	 * @see java.awt.event.KeyEvent
	 * @see javax.swing.JMenuItem#setAction(javax.swing.Action)
	 */
	public int getMnemonicKey()
	{
		return ( (Number)getValue( MNEMONIC_KEY ) ).intValue();
	}

	/**
	 * Set integer key code to use as mnemonic (e.g. key to select menu item).
	 * If a negative code is specified, any existing value is lost (removed).
	 *
	 * @param key Integer key code to use as mnemonic.
	 *
	 * @see #MNEMONIC_KEY
	 * @see java.awt.event.KeyEvent
	 * @see javax.swing.JMenuItem#setAction(javax.swing.Action)
	 */
	public void setMnemonicKey( final int key )
	{
		putValue( MNEMONIC_KEY, ( key < 0 ) ? null : key );
	}

	/**
	 * Set mnemonic key from a string in a format compatible with {@code
	 * KeyStroke#getKeyStroke(String)}. Any specified modifiers will be ignored.
	 * If {@code null} or an incorrectly formatted string is specified, any
	 * existing value is lost (removed).
	 *
	 * @param key String representation of mnemonic key.
	 *
	 * @see #MNEMONIC_KEY
	 * @see KeyEvent
	 * @see JMenuItem#setAction(Action)
	 * @see KeyStroke#getKeyStroke(String)
	 */
	public void setMnemonicKey( final String key )
	{
		final KeyStroke keyStroke = KeyStroke.getKeyStroke( key );
		setMnemonicKey( ( keyStroke == null ) ? -1 : keyStroke.getKeyCode() );
	}

	/**
	 * Get short description of action. Used for tooltip text.
	 *
	 * @return Short description.
	 *
	 * @see #SHORT_DESCRIPTION
	 */
	public String getShortDescription()
	{
		return (String)getValue( SHORT_DESCRIPTION );
	}

	/**
	 * Set short description of action. Used for tooltip text. If {@code null}
	 * is specified, any existing value is lost (removed).
	 *
	 * @param description Short description.
	 *
	 * @see #SHORT_DESCRIPTION
	 */
	public void setShortDescription( final String description )
	{
		putValue( SHORT_DESCRIPTION, HTMLTools.plainTextToHTML( description ) );
	}

	/**
	 * Get small icon. Used for toolbar buttons.
	 *
	 * @return Small icon.
	 *
	 * @see #SMALL_ICON
	 * @see Icon
	 */
	public Icon getSmallIcon()
	{
		return (Icon)getValue( SMALL_ICON );
	}

	/**
	 * Set small icon. Used for toolbar buttons. If {@code null} is specified,
	 * any existing value is lost (removed).
	 *
	 * @param icon Small icon.
	 *
	 * @see #SMALL_ICON
	 * @see Icon
	 */
	public void setSmallIcon( final Icon icon )
	{
		putValue( SMALL_ICON, icon );
	}

	/**
	 * Set small icon. Used for toolbar buttons. If {@code null} is specified,
	 * any existing value is lost (removed).
	 *
	 * @param path Path to small icon.
	 *
	 * @see #SMALL_ICON
	 * @see Icon
	 */
	public void setSmallIcon( final String path )
	{
		setSmallIcon( path, -1, -1 );
	}

	/**
	 * Set small icon. Used for toolbar buttons. If {@code null} is specified,
	 * any existing value is lost (removed).
	 *
	 * @param path   Path to small icon.
	 * @param width  Icon width (<=0 to load from icon).
	 * @param height Icon height (<=0 to load from icon).
	 *
	 * @see #SMALL_ICON
	 * @see Icon
	 */
	public void setSmallIcon( final String path, final int width, final int height )
	{
		if ( path != null )
		{
			if ( ( width > 0 ) && ( height > 0 ) )
			{
				setSmallIcon( new AsyncIcon( path, width, height ) );
			}
			else
			{
				System.err.println( "TODO: Set icon dimensions for '" + getActionCommand() + "' action" );

				final Icon icon = ImageTools.getImageIcon( getClass(), path );
				if ( icon != null )
				{
					setSmallIcon( icon );
				}
			}
		}
	}
}
