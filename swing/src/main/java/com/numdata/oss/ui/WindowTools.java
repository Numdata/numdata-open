/*
 * Copyright (c) 2004-2017, Numdata BV, The Netherlands.
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
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class provides utility methods for windows/dialogs.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "MagicConstant", "unused" } )
public class WindowTools
{
	/**
	 * Constant for 'yes' response from user.
	 *
	 * @see #askConfirmationDialog
	 */
	public static final int YES = 1;

	/**
	 * Constant for 'no' response from user.
	 *
	 * @see #askConfirmationDialog
	 */
	public static final int NO = 0;

	/**
	 * Constant for 'cancel' response from user.
	 *
	 * @see #askConfirmationDialog
	 */
	public static final int CANCEL = -1;

	/**
	 * Format of gemeotry argument used by {@link #setGeometry} method.
	 */
	private static final Pattern GEOMETRY_ARGUMENT = Pattern.compile( "^((-?[0-9]+)x(-?[0-9]+))?(([+-])(\\-?[0-9]+)([+-])(\\-?[0-9]+))?$" );
	//                                                              0   12          3           45     6           7     8

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private WindowTools()
	{
	}

	/**
	 * Request confirmation from user using a dialog offering 'yes' and 'no'
	 * options.
	 *
	 * The message can contain HTML formatting codes.
	 *
	 * @param owner   Parent component (if {@code null}, a default {@link Frame}
	 *                is used).
	 * @param title   Dialog title.
	 * @param message Content message.
	 *
	 * @return {@link #YES} ({@code 1}) if the user selected 'yes'; {@link #NO}
	 * ({@code 0}) if the user selected 'no'; {@link #CANCEL} ({@code -1}) if
	 * the user closed the dialog without making a choice, or an exception
	 * occurred internally.
	 *
	 * @deprecated Use {@link #showConfirmDialog} instead.
	 */
	@Deprecated
	public static int askConfirmationDialog( @Nullable final Component owner, @Nullable final String title, @Nullable final String message )
	{
		int result;

		try
		{
			final int response = JOptionPane.showConfirmDialog( owner, TextTools.plainTextToHTML( message ), title, JOptionPane.YES_NO_CANCEL_OPTION );
			switch ( response )
			{
				case JOptionPane.YES_OPTION:
					result = YES;
					break;

				case JOptionPane.NO_OPTION:
					result = NO;
					break;

				default:
					result = CANCEL;
					break;
			}
		}
		catch ( final Throwable ignored )
		{
			result = CANCEL;
		}

		return result;
	}

	/**
	 * Center the specified window on screen (don't modify size).
	 *
	 * @param window Target window.
	 */
	public static void center( @NotNull final Window window )
	{
		center( window, window.getWidth(), window.getHeight() );
	}

	/**
	 * Center the specified internal frame on it's container (don't modify
	 * size).
	 *
	 * @param window Target window.
	 */
	public static void center( @NotNull final JInternalFrame window )
	{
		final Container parent = window.getParent();
		if ( parent != null )
		{
			final Insets parentInsets = parent.getInsets();

			final int x = parentInsets.left + ( parent.getWidth() - parentInsets.left - parentInsets.right - window.getWidth() ) / 2;
			final int y = parentInsets.top + ( parent.getHeight() - parentInsets.top - parentInsets.bottom - window.getHeight() ) / 2;

			window.setLocation( x, y );
		}
	}

	/**
	 * Center the specified window on screen and give it the specified width and
	 * height.
	 *
	 * Negative values can be used to define a width and/or height relative to
	 * the screen size, e.g. specifying {@code 600} for the width, and {@code
	 * -200} for the height, will set the window width to 600 pixels, and the
	 * height to the screen height minus 200 pixels.
	 *
	 * @param window Target window.
	 * @param width  Set width of target window to this value.
	 * @param height Set height of target window to this value.
	 */
	public static void center( @NotNull final Window window, final int width, final int height )
	{
		final Rectangle screenBounds = getScreenBounds( window );

		final int acualWidth = ( width < 0 ) ? screenBounds.width + width : width;
		final int actualHeight = ( height < 0 ) ? screenBounds.height + height : height;

		window.setBounds( screenBounds.x + ( screenBounds.width - acualWidth ) / 2, screenBounds.y + ( screenBounds.height - actualHeight ) / 2, acualWidth, actualHeight );
	}

	/**
	 * Centers the given window on its parent window or, if it has no parent, on
	 * the screen.
	 *
	 * @param window Window to be centered.
	 */
	public static void centerOnParent( @NotNull final Window window )
	{
		centerOnComponent( window, window.getParent() );
	}

	/**
	 * Centers the given window on the given component. If no component is
	 * given, the window is centered on the screen instead.
	 *
	 * @param window    Window to be centered.
	 * @param component Component to center the window on.
	 */
	public static void centerOnComponent( @NotNull final Window window, @Nullable final Component component )
	{
		final int x;
		final int y;

		if ( component == null )
		{
			final Rectangle screenBounds = getScreenBounds( window );
			x = screenBounds.x + ( screenBounds.width - window.getWidth() ) / 2;
			y = screenBounds.y + ( screenBounds.height - window.getHeight() ) / 2;
		}
		else
		{
			final Point locationOnScreen = component.getLocationOnScreen();
			final int preferredX = locationOnScreen.x + ( component.getWidth() - window.getWidth() ) / 2;
			final int preferredY = locationOnScreen.y + ( component.getHeight() - window.getHeight() ) / 2;

			final Rectangle screenBounds = getScreenBounds( component );
			x = Math.max( screenBounds.x, Math.min( preferredX, screenBounds.x + screenBounds.width - window.getWidth() ) );
			y = Math.max( screenBounds.y, Math.min( preferredY, screenBounds.y + screenBounds.height - window.getHeight() ) );
		}

		window.setLocation( x, y );
	}

	/**
	 * Do our best to close the specified window. There always seem to be issues
	 * with windows not closing reliably, especially when running from a
	 * browser. This method tries to get the window to close in various ways,
	 * hoping that at least one of them works.
	 *
	 * @param window Window to close.
	 */
	public static void close( @NotNull final Window window )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					window.dispose();
				}
				catch ( final Throwable t )
				{ /* ignore */ }

				try
				{
					window.setVisible( false );
				}
				catch ( final Throwable t )
				{ /* ignore */ }
			}
		} );
	}

	/**
	 * Construct (Swing) frame with the specified title, size, and content. This
	 * method will:
	 *
	 * <ul>
	 *
	 * <li>Set the default close operation to {@link JFrame#EXIT_ON_CLOSE}.</li>
	 *
	 * <li>Resize the frame to the specified width and height.</li>
	 *
	 * <li>Place frame in the center of the screen.</li>
	 *
	 * <li>Use the specified component as content (optional). This does not need
	 * to be a container; if it is not, the component is added to the default
	 * content pane.</li>
	 *
	 * </ul>
	 *
	 * @param title   Frame title ({@code null} => none).
	 * @param width   Frame width.
	 * @param height  Frame height.
	 * @param content Content component.
	 *
	 * @return Constructed frame (not yet visible).
	 */
	public static JFrame createFrame( @Nullable final String title, final int width, final int height, @Nullable final Component content )
	{
		return createFrame( title, -1, -1, width, height, content );
	}

	/**
	 * Construct (Swing) frame with the specified title, size, and content. This
	 * method will:
	 *
	 * <ul>
	 *
	 * <li>Set the default close operation to {@link JFrame#EXIT_ON_CLOSE}.</li>
	 *
	 * <li>Resize the frame to the specified width and height.<br /><br />
	 * Negative values can be used to define a width and/or height relative to
	 * the screen size, e.g. specifying {@code 600} for the width, and {@code
	 * -200} for the height, will set the window width to 600 pixels, and the
	 * height to the screen height minus 200 pixels.</li>
	 *
	 * <li>Place frame in the frame at the requested location. If either
	 * coordinate of this location is set to a negative value, it will be
	 * centered on screen.</li>
	 *
	 * <li>Use the specified component as content (optional). This does not need
	 * to be a container; if it is not, the component is added to the default
	 * content pane.</li>
	 *
	 * </ul>
	 *
	 * @param title   Frame title ({@code null} => none).
	 * @param x       X location of frame ({@code -1} => center on screen).
	 * @param y       Y location of frame ({@code -1} => center on screen).
	 * @param width   Frame width.
	 * @param height  Frame height.
	 * @param content Content component.
	 *
	 * @return Constructed frame (not yet visible).
	 */
	public static JFrame createFrame( @Nullable final String title, final int x, final int y, final int width, final int height, @Nullable final Component content )
	{
		final JFrame result = new JFrame( title );
		result.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		if ( content instanceof Container )
		{
			result.setContentPane( (Container)content );
		}
		else if ( content != null )
		{
			final Container contentPane = result.getContentPane();
			contentPane.add( content, BorderLayout.CENTER );
		}

		final Rectangle screenBounds = getScreenBounds( result );

		final int actualWidth = ( width < 0 ) ? screenBounds.width + width : width;
		final int actualHeight = ( height < 0 ) ? screenBounds.height + height : height;
		final int actualX = ( x < 0 ) ? screenBounds.x + ( screenBounds.width - actualWidth ) / 2 : x;
		final int actualY = ( y < 0 ) ? screenBounds.y + ( screenBounds.height - actualHeight ) / 2 : y;

		result.setBounds( actualX, actualY, actualWidth, actualHeight );

		return result;
	}

	/**
	 * Create a progress window. The window centered and made visible. The
	 * caller should close the dialog when appropriate (typically in a {@code
	 * try}...{@code finally} block).
	 *
	 * @param owner   Owner component of child window.
	 * @param title   Dialog title.
	 * @param message Message to display in content of dialog.
	 *
	 * @return Progress dialog (already visible), caller needs to close it.
	 */
	public static JDialog createProgressWindow( @Nullable final Window owner, @Nullable final String title, @Nullable final String message )
	{
		final JPanel contentPane = new JPanel( new BorderLayout() );
		contentPane.setBorder( BorderFactory.createEmptyBorder( 16, 16, 16, 16 ) );
		contentPane.add( new JLabel( TextTools.plainTextToHTML( message ), JLabel.CENTER ), BorderLayout.CENTER );

		final JDialog result;
		if ( owner instanceof Frame )
		{
			result = new JDialog( (Frame)owner, title, false );
		}
		else if ( owner instanceof Dialog )
		{
			result = new JDialog( (Dialog)owner, title, false );
		}
		else
		{
			result = new JDialog( (Frame)null, title, false );
		}

		result.setContentPane( contentPane );
		result.setResizable( false );
		packAndCenter( result, 250, 100 );
		result.setVisible( true );

		if ( SwingUtilities.isEventDispatchThread() )
		{
			final RepaintManager repaintManager = RepaintManager.currentManager( result );
			repaintManager.addDirtyRegion( result, 0, 0, result.getWidth(), result.getHeight() );
			repaintManager.paintDirtyRegions();
		}

		return result;
	}

	/**
	 * Pack and center the specified window on screen.
	 *
	 * Note that a small value will be added to the packed window size (8
	 * pixels) to improve the window appearance in general and fix small layout
	 * errors (which often cause visual disturbance).
	 *
	 * @param window Target window.
	 *
	 * @see Window#pack()
	 */
	public static void packAndCenter( @NotNull final Window window )
	{
		packAndCenter( window, 0, 0, 0, 0 );
	}

	/**
	 * Pack and center the specified window on screen. A minimum width and
	 * height can specified to ensure proper window dimensions.
	 *
	 * Note that a small value will be added to the packed window size (8
	 * pixels) to improve the window appearance in general and fix small layout
	 * errors (which often cause visual disturbance).
	 *
	 * @param window    Target window.
	 * @param minWidth  Minimum window width.
	 * @param minHeight Minimum window height.
	 *
	 * @see Window#pack()
	 */
	public static void packAndCenter( @NotNull final Window window, final int minWidth, final int minHeight )
	{
		packAndCenter( window, minWidth, minHeight, 0, 0 );
	}

	/**
	 * Pack and center the specified window on screen. A minimum width and
	 * height can specified to ensure proper window dimensions. In addition to
	 * this, values to be added to the packed window size can be specified to
	 * improve the window appearance in general and fix small layout errors
	 * (which often cause visual disturbance).
	 *
	 * Negative values can be used to define a width and/or height relative to
	 * the screen size, e.g. specifying {@code 600} for the width, and {@code
	 * -200} for the height, will set the window width to 600 pixels, and the
	 * height to the screen height minus 200 pixels.
	 *
	 * @param window    Target window.
	 * @param minWidth  Minimum window width.
	 * @param minHeight Minimum window height.
	 * @param maxWidth  Maximum window width.
	 * @param maxHeight Maximum window height.
	 *
	 * @see Window#pack()
	 */
	public static void packAndCenter( @NotNull final Window window, final int minWidth, final int minHeight, final int maxWidth, final int maxHeight )
	{
		final Rectangle screenBounds = getScreenBounds( window );
		window.pack();

		int width = window.getWidth();

		if ( minWidth != 0 )
		{
			width = Math.max( ( minWidth < 0 ) ? screenBounds.width + minWidth : minWidth, width );
		}

		width = Math.min( ( maxWidth <= 0 ) ? screenBounds.width + maxWidth : maxWidth, width );

		int height = window.getHeight();

		if ( minHeight != 0 )
		{
			height = Math.max( ( minHeight < 0 ) ? screenBounds.height + minHeight : minHeight, height );
		}

		height = Math.min( ( maxHeight <= 0 ) ? screenBounds.height + maxHeight : maxHeight, height );

		window.setBounds( screenBounds.x + ( screenBounds.width - width ) / 2, screenBounds.y + ( screenBounds.height - height ) / 2, width, height );
	}

	/**
	 * Pack and center the specified window on screen when it is shown. This
	 * delayed operation is often useful when dialog content is changed after
	 * calling this method (typically a constructor), or when a dialog may be
	 * shown multiple times with different contents.
	 *
	 * Note that a small value will be added to the packed window size (8
	 * pixels) to improve the window appearance in general and fix small layout
	 * errors (which often cause visual disturbance).
	 *
	 * @param window Target window.
	 *
	 * @see Window#pack()
	 */
	public static void packAndCenterOnShow( @NotNull final Window window )
	{
		packAndCenterOnShow( window, 0, 0 );
	}

	/**
	 * Pack and center the specified window on screen when it is shown. This
	 * delayed operation is often useful when dialog content is changed after
	 * calling this method (typically a constructor), or when a dialog may be
	 * shown multiple times with different contents.
	 *
	 * A minimum width and height can specified to ensure proper window
	 * dimensions.
	 *
	 * Note that a small value will be added to the packed window size (8
	 * pixels) to improve the window appearance in general and fix small layout
	 * errors (which often cause visual disturbance).
	 *
	 * @param window    Target window.
	 * @param minWidth  Minimum window width.
	 * @param minHeight Minimum window height.
	 *
	 * @see Window#pack()
	 */
	public static void packAndCenterOnShow( @NotNull final Window window, final int minWidth, final int minHeight )
	{
		window.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowOpened( final WindowEvent e )
			{
				packAndCenter( window, minWidth, minHeight, 0, 0 );
			}
		} );
	}

	/**
	 * Get bounds of screen associated with the specified component. If
	 * possible, screen insets are applied.
	 *
	 * @param component Component to get screen bounds for.
	 *
	 * @return Screen bounds.
	 */
	@NotNull
	public static Rectangle getScreenBounds( @NotNull final Component component )
	{
		final Toolkit toolkit = component.getToolkit();

		final Rectangle bounds;

		final GraphicsConfiguration graphicsConfiguration = component.getGraphicsConfiguration();
		if ( graphicsConfiguration != null )
		{
			bounds = graphicsConfiguration.getBounds();
//			System.out.println( "GraphicsConfiguration.bounds=" + bounds );

			final Insets insets = toolkit.getScreenInsets( graphicsConfiguration );
//			System.out.println( "GraphicsConfiguration.insets=" + insets );
			bounds.x += insets.left;
			bounds.y += insets.top;
			bounds.width -= insets.left + insets.right;
			bounds.height -= insets.top + insets.bottom;
		}
		else
		{
			final Dimension screenSize = toolkit.getScreenSize();
			System.out.println( "Toolkit.screenSize=" + screenSize );
			bounds = new Rectangle( 0, 0, screenSize.width, screenSize.height - 5 );
		}

		return bounds;
	}

	/**
	 * Get {@link Window} component containing the specified component or the
	 * component itself, if it is a {@link Window}.
	 *
	 * @param component Component to get window of.
	 *
	 * @return {@link Window} component ({@code component} or ancestor); {@code
	 * null} if component is not (in) a window.
	 */
	@Nullable
	public static Window getWindow( @Nullable final Component component )
	{
		Window result = null;

		for ( Component comp = component; comp != null; comp = comp.getParent() )
		{
			if ( comp instanceof Window )
			{
				result = (Window)comp;
				break;
			}
		}

		return result;
	}

	/**
	 * Maximize the specified frame.
	 *
	 * @param frame Frame to maximize.
	 */
	public static void maximize( @NotNull final Frame frame )
	{
		maximize( frame, getScreenBounds( frame ) );
	}

	/**
	 * Maximize the specified frame.
	 *
	 * @param frame         Frame to maximize.
	 * @param regularBounds Bounds of frame in non-maximized state (used as
	 *                      fallback if the current platform does not support
	 *                      native frame maximization).
	 */
	public static void maximize( @NotNull final Frame frame, @NotNull final Rectangle regularBounds )
	{
		// NOTE: This is needed to maximize the frame while it's not 'realized' yet.
		// See also: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4464714
		if ( !frame.isVisible() )
		{
			frame.pack();
		}

		frame.setBounds( regularBounds );
		frame.setExtendedState( Frame.MAXIMIZED_BOTH );
		System.out.println( "frame bounds after maximize: " + frame.getBounds() );
	}

	/**
	 * Handle '{@code -geometry}' argument according to X specifications. This
	 * argument has the form:
	 * <pre>
	 *   -geometry WIDTHxHEIGHT+XOFF+YOFF
	 * </pre>
	 * Where {@code WIDTH} and {@code HEIGHT} specify the preferred size of the
	 * application's main window in pixels.
	 *
	 * {@code XOFF} and {@code YOFF} parts are measured in pixels and are used
	 * to specify the distance of the window from the left or right and top and
	 * bottom edges of the screen, respectively. Both types of offsets are
	 * measured from the indicated edge of the screen to the corresponding edge
	 * of the window. The X offset may be specified in the following ways:
	 *
	 * <dl>
	 *
	 * <dt>{@code +XOFF}</dt><dd>The left edge of the window is to be placed
	 * {@code XOFF} pixels in from the left edge of the screen (i.e., the X
	 * coordinate of the window's origin will be {@code XOFF}). {@code XOFF} may
	 * be negative, in which case the window's left edge will be off the screen.
	 * </dd>
	 *
	 * <dt>{@code -XOFF}</dt><dd>The right edge of the window is to be placed
	 * {@code XOFF} pixels in from the right edge of the screen. {@code XOFF}
	 * may be negative, in which case the window's right edge will be off the
	 * screen.</dd></dl>
	 *
	 * The Y offset has similar meanings:<dl>
	 *
	 * <dt>{@code +YOFF}</dt><dd> The top edge of the window is to be {@code
	 * YOFF} pixels below the top edge of the screen (i.e., the Y coordinate of
	 * the window's origin will be {@code YOFF}). {@code YOFF} may be negative,
	 * in which case the window's top edge will be off the screen.</dd>
	 *
	 * <dt>{@code -YOFF}</dt><dd> The bottom edge of the window is to be {@code
	 * YOFF} pixels above the bottom edge of the screen. {@code YOFF} may be
	 * negative, in which case the window's bottom edge will be off the screen.
	 * </dd>
	 *
	 * </dl>
	 *
	 * Offsets must be given as pairs; in other words, in order to specify
	 * either {@code XOFF} or {@code YOFF} both must be present. Windows can be
	 * placed in the four corners of the screen using the following
	 * specifications:
	 *
	 * <table>
	 *
	 * <tr><th>{@code +0+0}</th><td>upper left hand corner.</td></tr>
	 *
	 * <tr><th>{@code -0+0}</th><td>upper right hand corner.</td></tr>
	 *
	 * <tr><th>{@code -0-0}</th><td>right hand corner.</td></tr>
	 *
	 * <tr><th>{@code +0-0}</th><td>lower left hand corner.</td></tr>
	 *
	 * </table>
	 *
	 * @param geometry     Geometry specification ({@code null} or 0-length
	 *                     string is ignored).
	 * @param windowBounds Window bounds to set according to geometry (should be
	 *                     initialized to screen bounds).
	 *
	 * @return {@code true} if geometry was specified and applied; {@code false}
	 * if no geometry was specified;
	 *
	 * @throws IllegalArgumentException if the goemetry was badly formatted.
	 * @throws NullPointerException if {@code windowBounds} is {@code null}.
	 */
	public static boolean setGeometry( @Nullable final String geometry, @NotNull final Rectangle windowBounds )
	{
		final boolean result;

		if ( ( geometry != null ) && !geometry.isEmpty() )
		{
			final Matcher matcher = GEOMETRY_ARGUMENT.matcher( geometry );
			if ( !matcher.matches() )
			{
				throw new IllegalArgumentException( "badly formatted geometry argument '" + geometry + '\'' );
			}

			if ( matcher.group( 4 ) != null )
			{
				final boolean xNeg = "-".equals( matcher.group( 5 ) );
				final int xOff = Integer.parseInt( matcher.group( 6 ) );
				final boolean yNeg = "-".equals( matcher.group( 7 ) );
				final int yOff = Integer.parseInt( matcher.group( 8 ) );

				windowBounds.x = xNeg ? windowBounds.width - xOff : xOff;
				windowBounds.y = yNeg ? windowBounds.height - yOff : yOff;
			}

			if ( ( matcher.group( 1 ) != null ) )
			{
				windowBounds.width = Integer.parseInt( matcher.group( 2 ) );
				windowBounds.height = Integer.parseInt( matcher.group( 3 ) );
			}

			result = true;
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Show information message dialog.
	 *
	 * The message can contain HTML formatting codes.
	 *
	 * @param owner   Parent component (if {@code null}, a default {@link Frame}
	 *                is used).
	 * @param title   Dialog title.
	 * @param message Content message.
	 */
	public static void showInfoDialog( @Nullable final Component owner, @Nullable final String title, @Nullable final String message )
	{
		showMessageDialog( owner, title, message, JOptionPane.INFORMATION_MESSAGE, 0 );
	}

	/**
	 * Show error message dialog based on the specified exception. The error is
	 * also printed on {@link System#err}.
	 *
	 * @param owner       Parent component (if {@code null}, a default {@link
	 *                    Frame} is used).
	 * @param throwable   Throwable to get title and content (stack trace)
	 *                    from.
	 * @param stopTraceAt Stop stack trace when this class is encountered
	 *                    ({@code null} to always show whole stack).
	 */
	public static void showErrorDialog( @Nullable final Component owner, @NotNull final Throwable throwable, @Nullable final Class<?> stopTraceAt )
	{
		showErrorDialog( owner, null, throwable, stopTraceAt );
	}

	/**
	 * Show error message dialog based on the specified exception. The error is
	 * also printed on {@link System#err}.
	 *
	 * @param owner       Parent component (if {@code null}, a default {@link
	 *                    Frame} is used).
	 * @param title       Dialog title ({@code null} to use exception message,
	 *                    if any).
	 * @param throwable   Throwable to get content (stack trace) from (may be
	 *                    {@code null} to use title).
	 * @param stopTraceAt Stop stack trace when this class is encountered
	 *                    ({@code null} to always show whole stack).
	 */
	public static void showErrorDialog( @Nullable final Component owner, @Nullable final String title, @Nullable final Throwable throwable, @Nullable final Class<?> stopTraceAt )
	{
		String actualTitle = title;
		if ( ( actualTitle == null ) && ( throwable != null ) )
		{
			actualTitle = throwable.getLocalizedMessage();
			if ( actualTitle == null )
			{
				actualTitle = throwable.toString();
			}
		}

		final String message = ( throwable == null ) ? actualTitle : ( "<pre>" + StackTools.getStackTrace( throwable, stopTraceAt, true ) );

		showErrorDialog( owner, actualTitle, message );
	}

	/**
	 * Show error message dialog. The error is also printed on {@link
	 * System#err}.
	 *
	 * The message can contain HTML formatting codes.
	 *
	 * @param owner   Parent component (if {@code null}, a default {@link Frame}
	 *                is used).
	 * @param title   Dialog title.
	 * @param message Content message.
	 */
	public static void showErrorDialog( @Nullable final Component owner, @Nullable final String title, @Nullable final String message )
	{
		System.err.println( TextTools.getFixed( "----[ " + title + ']', 80, false, '-' ) );
		System.err.println( message );
		System.err.println( TextTools.getFixed( 80, '-' ) );

		showMessageDialog( owner, title, TextTools.plainTextToHTML( message ), JOptionPane.ERROR_MESSAGE, 0 );
	}

	/**
	 * Show warning message dialog.
	 *
	 * The message can contain HTML formatting codes.
	 *
	 * @param owner   Parent component (if {@code null}, a default {@link Frame}
	 *                is used).
	 * @param title   Dialog title.
	 * @param message Content message.
	 */
	public static void showWarningDialog( @Nullable final Component owner, @Nullable final String title, @Nullable final String message )
	{
		showMessageDialog( owner, title, TextTools.plainTextToHTML( message ), JOptionPane.WARNING_MESSAGE, 0 );
	}

	/**
	 * Show message dialog with optional timeout after which the message dialog
	 * is closed automatically.
	 *
	 * The message can contain HTML formatting codes.
	 *
	 * @param owner       Parent component (if {@code null}, a default {@link
	 *                    Frame} is used).
	 * @param title       Dialog title.
	 * @param message     Content message.
	 * @param messageType Type of message (see {@link JOptionPane}).
	 * @param timeout     Time in milliseconds after which the dialog should be
	 *                    closed automatically (<= 0 to disable this feature).
	 */
	public static void showMessageDialog( @Nullable final Component owner, @Nullable final String title, @Nullable final String message, final int messageType, final int timeout )
	{
		try
		{
			if ( timeout > 0 )
			{
				showTimedMessageDialog( owner, null, null, null, message, timeout );

			}
			else
			{
				JOptionPane.showMessageDialog( owner, message, title, messageType );
			}
		}
		catch ( final Throwable t )
		{
			/* ignore exceptions */
		}
	}

	/**
	 * Show message dialog that is closed automatically after the specified
	 * timeout.
	 *
	 * The message can contain HTML formatting codes.
	 *
	 * @param owner      Parent component (if {@code null}, a default {@link
	 *                   Frame} is used).
	 * @param font       Font to use ({@code null} => default).
	 * @param foreground Foreground color to use ({@code null} => default).
	 * @param background Background color to use ({@code null} => default).
	 * @param message    Content message.
	 * @param timeout    Time in milliseconds after which the dialog should be
	 *                   closed automatically (<= 0 never show dialog).
	 */
	public static void showTimedMessageDialog( @Nullable final Component owner, @Nullable final Font font, @Nullable final Color foreground, @Nullable final Color background, @Nullable final String message, final int timeout )
	{
		if ( timeout > 0 )
		{
			final JDialog dialog = createUndecoratedMessageDialog( owner, true, font, foreground, background, message );

			final Timer timer = new Timer( timeout, new ActionListener()
			{
				@Override
				public void actionPerformed( final ActionEvent event )
				{
					dialog.dispose();
				}
			} );

			timer.start();
			dialog.setVisible( true );
			timer.stop();
			dialog.dispose();
		}
	}

	/**
	 * Create an undecorated message dialog. The returned dialog is not yet
	 * visible.
	 *
	 * The message can contain HTML formatting codes.
	 *
	 * @param owner      Parent component (if {@code null}, a default {@link
	 *                   Frame} is used).
	 * @param modal      Create modal dialog vs. dialog that allows access to
	 *                   other windows.
	 * @param font       Font to use ({@code null} => default).
	 * @param foreground Foreground color to use ({@code null} => default).
	 * @param background Background color to use ({@code null} => default).
	 * @param message    Content message.
	 *
	 * @return Dialog that was created.
	 */
	public static JDialog createUndecoratedMessageDialog( @Nullable final Component owner, final boolean modal, @Nullable final Font font, @Nullable final Color foreground, @Nullable final Color background, @Nullable final String message )
	{
		final JDialog dialog = ( owner instanceof Dialog ) ? new JDialog( (Dialog)owner, modal ) : new JDialog( (Frame)owner, modal );
		dialog.setUndecorated( true );
		dialog.setResizable( false );

		final JLabel label = new JLabel( TextTools.plainTextToHTML( message ) );
		if ( font != null )
		{
			label.setFont( font );
		}

		if ( foreground != null )
		{
			label.setForeground( foreground );
		}

		final JPanel contentPane = new JPanel( new BorderLayout() );
		if ( background != null )
		{
			contentPane.setBackground( background );
		}

		contentPane.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ), BorderFactory.createEmptyBorder( 16, 16, 16, 16 ) ) );
		contentPane.setOpaque( true );
		contentPane.add( label, BorderLayout.CENTER );
		dialog.setContentPane( contentPane );

		packAndCenter( dialog, 250, 100 );
		return dialog;
	}

	/**
	 * Shows a dialog with the specified attributes, using {@link JOptionPane}.
	 * Contrary to the equivalent method in {@code JOptionPane}, this method
	 * performs localization of button texts using its own resource bundle. By
	 * providing a locale, the dialog's buttons can be properly localized.
	 *
	 * @param locale      Locale to be used.
	 * @param parent      Parent component.
	 * @param message     Message to be shown.
	 * @param title       Title of the dialog.
	 * @param optionType  Options that the user may choose from.
	 * @param messageType Kind of message.
	 *
	 * @return Option selected by the user.
	 *
	 * @see JOptionPane#showConfirmDialog(Component, Object, String, int, int)
	 */
	public static int showConfirmDialog( @NotNull final Locale locale, @Nullable final Component parent, @Nullable final String message, @Nullable final String title, final int optionType, final int messageType )
	{
		final ResourceBundle res = ResourceBundleTools.getBundle( WindowTools.class, locale );

		final String[] options;
		switch ( optionType )
		{
			case JOptionPane.YES_NO_OPTION:
				options = new String[] { res.getString( "yes" ), res.getString( "no" ) };
				break;

			case JOptionPane.YES_NO_CANCEL_OPTION:
				options = new String[] { res.getString( "yes" ), res.getString( "no" ), res.getString( "cancel" ) };
				break;

			case JOptionPane.OK_CANCEL_OPTION:
				options = new String[] { res.getString( "ok" ), res.getString( "cancel" ) };
				break;

			default:
				options = null;
		}

		return JOptionPane.showOptionDialog( parent, message, title, optionType, messageType, null, options, null );
	}
}
