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
import java.util.*;
import javax.swing.*;

import com.numdata.oss.*;
import com.numdata.oss.net.*;

/**
 * This class can be used to monitor the connection between a {@link Client} and
 * a {@link Server}. An error message is displayed when the connection fails for
 * some reason.
 *
 * @author Peter S. Heijnen
 */
public class ClientConnectionMonitor
implements Runnable
{
	/**
	 * Poll time in milliseconds.
	 */
	private static final long POLL_TIME = 30000L;

	/**
	 * Resource bundle with localized messages.
	 */
	private final ResourceBundle _res;

	/**
	 * Parent component to use for dialogs.
	 */
	private final Component _owner;

	/**
	 * {@link Client} to monitor.
	 */
	private final Client _client;

	/**
	 * Construct new monitor.
	 *
	 * @param locale Locale to use for internationalization.
	 * @param owner  Parent component to use for dialogs.
	 * @param client {@link Client} to monitor.
	 */
	private ClientConnectionMonitor( final Locale locale, final Component owner, final Client client )
	{
		_res = ResourceBundleTools.getBundle( ClientConnectionMonitor.class, locale );
		_client = client;
		_owner = owner;
	}

	/**
	 * Create a monitor thread and start it.
	 *
	 * @param locale Locale to use for internationalization.
	 * @param owner  Parent component to use for dialogs.
	 * @param client {@link Client} to monitor.
	 *
	 * @return Thread that was started.
	 */
	public static Thread startMonitor( final Locale locale, final Component owner, final Client client )
	{
		final Thread thread = new Thread( new ClientConnectionMonitor( locale, owner, client ), ClientConnectionMonitor.class.getName() );
		thread.setPriority( Thread.MIN_PRIORITY );
		thread.setDaemon( true );
		thread.start();
		return thread;
	}

	public void run()
	{
		int failures = 0; // Number of failures (reset when connection is restored).

		while ( true )
		{
			try
			{
				Thread.sleep( POLL_TIME );
			}
			catch ( InterruptedException ignored )
			{
				break;
			}

			boolean success = false;
			try
			{
				_client.ping();
				success = true;
			}
			catch ( Exception e )
			{
				e.printStackTrace();
			}

			if ( success )
			{
				if ( failures > 0 )
				{
					showMessage( "reconnected" );
				}

				failures = 0;
			}
			else
			{
				if ( failures == 0 )
				{
					showMessage( "disconnected" );
				}

				failures++;
			}
		}
	}

	/**
	 * Show message dialog.
	 *
	 * @param message Message to show.
	 */
	private void showMessage( final String message )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				WindowTools.showTimedMessageDialog( _owner, null, null, null, _res.getString( message ), 5000 );
			}
		} );
	}
}
