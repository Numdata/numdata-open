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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 * Implements a console similar to the Java Console that is available for
 * applets and Java Web Start applications.
 *
 * @author  G. Meinders
 */
public class Console
	extends JFrame
{
	private final StyledDocument _document;

	/**
	 * Creates a new console.
	 */
	public Console()
	{
		super( "Java Console" );

		final StyledDocument document = new DefaultStyledDocument();
		_document = document;

		final Style errorStyle = document.addStyle( "error" , null );
		StyleConstants.setForeground( errorStyle , Color.RED );

		final JTextPane console = new JTextPane( document )
		{
			// Hack to disable word wrapping.
			public boolean getScrollableTracksViewportWidth()
			{
				return false;
			}
		};
		console.setFont( new Font( Font.MONOSPACED , Font.PLAIN , 12 ) );

		final JScrollPane consoleScroller = new JScrollPane( console , ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS , ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS );

		// The following is part of the above hack to disable word wrapping.
		final JViewport consoleScrollerViewport = consoleScroller.getViewport();
		consoleScrollerViewport.setBackground( Color.WHITE );

		setLayout( new BorderLayout() );
		add( consoleScroller , BorderLayout.CENTER );
		setDefaultCloseOperation( HIDE_ON_CLOSE );
		setSize( new Dimension( 640 , 320 ) );
	}

	/**
	 * Redirects the standard output and error streams to the console.
	 */
	public void redirectStandardStreams()
	{
		System.setOut( new PrintStream( new ConsoleStream( null ) ) );
		System.setErr( new PrintStream( new ConsoleStream( _document.getStyle( "error" ) ) ) );
	}

	/**
	 * Adds a system tray icon that shows/hides the console.
	 *
	 * @throws  AWTException if no icon can be added to the system tray.
	 */
	public void addToSystemTray()
		throws AWTException
	{
		if ( !SystemTray.isSupported() )
		{
			throw new AWTException( "System tray not supported." );
		}

		final SystemTray systemTray   = SystemTray.getSystemTray();
		final Dimension trayIconSize = systemTray.getTrayIconSize();

		final BufferedImage consoleIconImage = new BufferedImage( trayIconSize.width , trayIconSize.height , BufferedImage.TYPE_INT_ARGB );
		{
			final Graphics2D g = consoleIconImage.createGraphics();
			g.setColor( Color.WHITE );
			final int width = consoleIconImage.getWidth();
			final int height = width * 3 / 4;
			final int offset = ( width - height ) / 2;
			g.fillRect( 0 , offset , width - 1 , height - 1 );
			g.setColor( Color.BLACK );
			g.drawRect( 0 , offset , width - 1 , height - 1 );
			g.dispose();
		}

		final TrayIcon consoleTrayIcon = new TrayIcon( consoleIconImage , "Open Java Console" );
		consoleTrayIcon.addMouseListener( new MouseAdapter()
		{
			public void mouseClicked( final MouseEvent e )
			{
				setVisible( !isVisible() );
			}
		} );

		systemTray.add( consoleTrayIcon );
	}

	private class ConsoleStream extends OutputStream
	{
		private AttributeSet _style;

		ConsoleStream( final AttributeSet style )
		{
			_style = style;
		}

		public void write( final int b )
			throws IOException
		{
			SwingUtilities.invokeLater( new AddToConsole( String.valueOf( (char)b ) ) );
		}

		public void write( final byte[] b )
			throws IOException
		{
			SwingUtilities.invokeLater( new AddToConsole( new String( b ) ) );
		}

		public void write( final byte[] b , final int offset , final int length )
			throws IOException
		{
			SwingUtilities.invokeLater( new AddToConsole( new String( b , offset , length ) ) );
		}

		private class AddToConsole
			implements Runnable
		{
			private String _text;

			private AddToConsole( final String text )
			{
				_text = text;
			}

			public void run()
			{
				try
				{
					_document.insertString( _document.getLength() , _text , _style );
				}
				catch ( BadLocationException e )
				{
					e.printStackTrace();
				}
			}
		}
	}
}
