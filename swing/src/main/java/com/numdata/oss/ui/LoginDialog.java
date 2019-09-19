/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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

/**
 * This dialog is used to query the user for credentials to use for logging in.
 *
 * @author Sjoerd Bouwman
 * @author Peter S. Heijnen
 */
public final class LoginDialog
extends JDialog
implements ActionListener
{
	/**
	 * Text field with name of user to log in.
	 */
	private final JTextField _usernameField;

	/**
	 * Text field with password for the user being logged in.
	 */
	private final JPasswordField _passwordField;

	/**
	 * Success flag. This is set when authentication succeeded.
	 */
	private boolean _cancelled;

	private static final String ABORT = "abort";

	/**
	 * Construct login dialog.
	 * <pre>
	 *           0                  1
	 *    +-----------------+------------------+
	 *  0 |         Domain: | [______________] |
	 *    +-----------------+------------------+
	 *  1 |       Username: | [________]       |
	 *    +-----------------+------------------+
	 *  2 |       Password: | [________]       |
	 *    +-----------------+------------------+
	 *  3 |       [ Abort ]   [ Login ]        |
	 *    +------------------------------------+
	 * </pre>
	 *
	 * @param parent     Parent frame of dialog.
	 * @param locale     Locale to use for internationalization.
	 * @param domainName Default domainname.
	 * @param username   Default username.
	 * @param password   Default password.
	 */
	public LoginDialog( final Frame parent, final Locale locale, final String domainName, final String username, final String password )
	{
		super( parent, true );

		_cancelled = false;

		final ResourceBundle res = ResourceBundleTools.getBundleHierarchy( getClass(), locale );

		setTitle( res.getString( "title" ) );

		final JPanel content = new JPanel( new GridBagLayout() );
		setContentPane( content );

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets( 8, 8, 8, 8 );
		gbc.weightx = 0.0;
		gbc.weighty = 1.0;

		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.fill = GridBagConstraints.NONE;
		content.add( new JLabel( domainName ), gbc );
		gbc.gridwidth = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		content.add( new JLabel( res.getString( "username" ) ), gbc );

		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		_usernameField = new JTextField( username );
		_usernameField.addActionListener( this );
		content.add( _usernameField, gbc );

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.EAST;
		content.add( new JLabel( res.getString( "password" ) ), gbc );

		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		_passwordField = new JPasswordField( password );
		_passwordField.addActionListener( this );
		content.add( _passwordField, gbc );

		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.EAST;
		final JPanel buttonBar = new JPanel( new FlowLayout( FlowLayout.RIGHT, 8, 4 ) );
		content.add( buttonBar, gbc );

		final JButton abortButton = new JButton( "   " + res.getString( "abort" ) + "   " );
		abortButton.addActionListener( this );
		abortButton.setActionCommand( ABORT );
		buttonBar.add( abortButton );

		final JButton loginButton = new JButton( "   " + res.getString( "login" ) + "   " );
		loginButton.addActionListener( this );
		buttonBar.add( loginButton );

		WindowTools.packAndCenterOnShow( this, 250, 100 );
		setVisible( true );

		final Font f = loginButton.getFont();
		loginButton.setFont( new Font( f.getName(), f.getStyle() | Font.BOLD, f.getSize() ) );
	}

	public void actionPerformed( final ActionEvent event )
	{
		final String command = event.getActionCommand();

		_cancelled = ABORT.equals( command );
		dispose();
	}

	/**
	 * Name of user that was logged in (if {@code isSuccessful()} returns {@code
	 * true}).
	 *
	 * @return Name of user that was logged in.
	 */
	public String getUsername()
	{
		return _usernameField.getText();
	}

	/**
	 * Get password of user that logged in. +
	 *
	 * @return Password of user that logged in.
	 */
	public String getPassword()
	{
		return new String( _passwordField.getPassword() );
	}

	/**
	 * Get cancellation flag. This is set when the dialog is cancelled by the
	 * user.
	 *
	 * @return {@code true} if dialog was cancelled; {@code false} otherwise.
	 */
	public boolean isCancelled()
	{
		return _cancelled;
	}
}

