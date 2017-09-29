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
import java.awt.Dimension;
import java.awt.event.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class provides functionality for creating a content pane with a
 * standardized layout and functionality.
 *
 * @author Peter S. Heijnen
 * @see StandardDialog
 */
public class StandardContentPane
extends JPanel
{
	/**
	 * Button state: enabled.
	 */
	public static final int BUTTON_ENABLED = 0;

	/**
	 * Button state: disabled.
	 */
	public static final int BUTTON_DISABLED = 1;

	/**
	 * Button state: hidden.
	 */
	public static final int BUTTON_HIDDEN = 2;

	/**
	 * Default inset to use around the content component.
	 */
	public static final int DEFAULT_CONTENT_INSET = 8;

	/**
	 * Inset to use around the button bar.
	 */
	public static final int BUTTON_BAR_INSET = 8;

	/**
	 * Gap to use between buttons on the button bar.
	 */
	public static final int BUTTON_BAR_GAP = 8;

	/**
	 * Panel for image on left side of content.
	 */
	@Nullable
	private JComponent _imagePanel;

	/**
	 * Panel for buttons.
	 */
	@Nullable
	private Container _buttonBar;

	/**
	 * Inner content component of this pane.
	 */
	@NotNull
	private JComponent _content;

	/**
	 * Construct new content pane,.
	 *
	 * @param content Main component with inner content of pane.
	 */
	public StandardContentPane( @NotNull final JComponent content )
	{
		super( new BorderLayout() );

		if ( ( content instanceof JPanel ) && ( content.getBorder() == null ) )
		{
			content.setBorder( BorderFactory.createEmptyBorder( DEFAULT_CONTENT_INSET, DEFAULT_CONTENT_INSET, DEFAULT_CONTENT_INSET, DEFAULT_CONTENT_INSET ) );
		}

		_content = content;
		add( content, BorderLayout.CENTER );

		_imagePanel = null;
		_buttonBar = null;
	}

	/**
	 * Add button to content pane using the specified button properties.
	 *
	 * @param res      Resource bundle to get button label and tooltip from.
	 * @param key      Resource key, name, and action command to use.
	 * @param listener Action listener to perform button action.
	 *
	 * @return Button component.
	 */
	@NotNull
	public JButton addButton( @NotNull final ResourceBundle res, @NotNull final String key, @NotNull final ActionListener listener )
	{
		final JButton result;

		result = new JButton( ' ' + ResourceBundleTools.getString( res, key, key ) + ' ' );
		result.setName( key );
		result.setActionCommand( key );
		result.addActionListener( listener );

		final String tooltip = ResourceBundleTools.getString( res, key + "Tip", null );
		if ( tooltip != null )
		{
			result.setToolTipText( tooltip );
		}

		addButton( result );

		return result;
	}

	/**
	 * Add button to content pane using the specified button properties.
	 *
	 * @param res      Resource bundle to get button label and tooltip from.
	 * @param afterKey The button will be placed after the button with this
	 *                 resource key.
	 * @param key      Resource key, name, and action command to use ({@code
	 *                 null} => don't create button).
	 * @param listener Action listener to perform button action ({@code null} =>
	 *                 don't create button).
	 *
	 * @return Button component; {@code null} if no button that was created.
	 */
	@Nullable
	public JButton addButton( final ResourceBundle res, final String afterKey, final String key, final ActionListener listener )
	{
		final JButton result;

		if ( ( key != null ) && ( listener != null ) )
		{
			result = new JButton( ' ' + ResourceBundleTools.getString( res, key, key ) + ' ' );
			result.setName( key );
			result.setActionCommand( key );
			result.addActionListener( listener );

			final String tooltip = ResourceBundleTools.getString( res, key + "Tip", null );
			if ( tooltip != null )
			{
				result.setToolTipText( tooltip );
			}

			addButton( afterKey, result );
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * Add button to content pane. The button may actually be any component to
	 * be placed on the button bar.
	 *
	 * @param button Button component to add ({@code null} => don't add
	 *               button).
	 */
	public void addButton( @Nullable final Component button )
	{
		if ( button != null )
		{
			final Container buttonBar = getButtonBar();
			buttonBar.add( ( buttonBar.getComponentCount() == 0 ) ? Box.createGlue() : Box.createRigidArea( new Dimension( BUTTON_BAR_GAP, 0 ) ) );
			buttonBar.add( button );
		}
	}

	/**
	 * Add button to content pane. The button may actually be any component to
	 * be placed on the button bar.
	 *
	 * @param afterName The button will be placed after the component with this
	 *                  name.
	 * @param button    Button component to add ({@code null} => don't add
	 *                  button).
	 */
	public void addButton( @Nullable final String afterName, @Nullable final Component button )
	{
		if ( button != null )
		{
			if ( afterName != null )
			{
				final Container buttonBar = getButtonBar();
				final Component[] components = buttonBar.getComponents();

				int insertAt = -1;
				for ( int i = 0; i < components.length; i++ )
				{
					if ( afterName.equals( components[ i ].getName() ) )
					{
						insertAt = i + 1;
						break;
					}
				}

				buttonBar.add( ( buttonBar.getComponentCount() == 0 ) ? Box.createGlue() : Box.createRigidArea( new Dimension( BUTTON_BAR_GAP, 0 ) ), insertAt++ );
				buttonBar.add( button, insertAt );
			}
			else
			{
				addButton( button );
			}
		}
	}

	/**
	 * Get button with the specified name from the content pane.
	 *
	 * @param name Name of button component.
	 *
	 * @return Button component; {@code null} if the requested button was not
	 * found.
	 */
	@Nullable
	public Component getButton( @Nullable final String name )
	{
		Component result = null;

		final Container buttonBar = _buttonBar;
		if ( ( name != null ) && ( buttonBar != null ) )
		{
			synchronized ( buttonBar.getTreeLock() )
			{
				final int ncomponents = buttonBar.getComponentCount();
				for ( int i = 0; i < ncomponents; i++ )
				{
					final Component component = buttonBar.getComponent( i );
					if ( name.equals( component.getName() ) )
					{
						result = component;
						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Remove button from content pane. The component must already exist on the
	 * button bar (if it does not exist, this request is silently ignored), but
	 * the button bar will not disappear.
	 *
	 * @param key Name of button component.
	 */
	public void removeButton( @Nullable final String key )
	{
		removeButton( getButton( key ) );
	}

	/**
	 * Remove button from content pane. The component must already exist on the
	 * button bar (if it does not exist, this request is silently ignored), but
	 * the button bar will not disappear.
	 *
	 * @param button Button component to remove ({@code null} => don't remove
	 *               button).
	 */
	public void removeButton( @Nullable final Component button )
	{
		if ( button != null )
		{
			final Container buttonBar = _buttonBar;
			if ( buttonBar != null )
			{
				buttonBar.remove( button );
			}
		}
	}

	/**
	 * Set state of button (enabled/disabled/hidden).
	 *
	 * @param key   Name of button component.
	 * @param state State to set button to ({@link #BUTTON_ENABLED}, {@link
	 *              #BUTTON_DISABLED}, {@link #BUTTON_HIDDEN}).
	 *
	 * @throws IllegalArgumentException if an invalid state was requested.
	 */
	public void setButtonState( @Nullable final String key, final int state )
	{
		final Component button = getButton( key );
		if ( button != null )
		{
			switch ( state )
			{
				case BUTTON_ENABLED:
					button.setEnabled( true );
					button.setVisible( true );
					break;

				case BUTTON_DISABLED:
					button.setEnabled( false );
					button.setVisible( true );
					break;

				case BUTTON_HIDDEN:
					button.setVisible( false );
					break;

				default:
					throw new IllegalArgumentException( "invalid state: " + state );
			}
		}
	}

	/**
	 * Get button bar of dialog, or create one if necessary.
	 *
	 * @return Button bar.
	 */
	@NotNull
	public Container getButtonBar()
	{
		Container buttonBar = _buttonBar;
		if ( buttonBar == null )
		{
			buttonBar = createButtonBar();
			_buttonBar = buttonBar;
		}
		return buttonBar;
	}

	/**
	 * Construct panel to place buttons on.
	 *
	 * @return Button panel component.
	 */
	@NotNull
	protected Container createButtonBar()
	{
		final Box result = new Box( BoxLayout.X_AXIS );
		result.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createMatteBorder( 1, 0, 0, 0, Color.GRAY ), BorderFactory.createEmptyBorder( BUTTON_BAR_INSET, BUTTON_BAR_INSET, BUTTON_BAR_INSET, BUTTON_BAR_INSET ) ) );
		result.add( Box.createGlue() );
		add( result, BorderLayout.SOUTH );
		return result;
	}

	/**
	 * Get inner content component of this pane.
	 *
	 * @return Inner content component of this pane.
	 */
	@NotNull
	public JComponent getContent()
	{
		return _content;
	}

	/**
	 * Set inner content component of this pane.
	 *
	 * @param content Inner content component to set.
	 */
	public void setContent( @NotNull final JComponent content )
	{
		final JComponent oldContent = _content;
		//noinspection ObjectEquality
		if ( oldContent != content )
		{
			remove( oldContent );
			_content = content;
			add( content, BorderLayout.CENTER );
		}
	}

	/**
	 * Set image to display on the left side of the content pane. This is
	 * typically used as 'wizard image'. A thin gray vertical line is displayed
	 * on the right size of the image to separate the image from the content.
	 *
	 * @param path Path to image to place on left side ({@code null} => none).
	 */
	public void setImage( @Nullable final String path )
	{
		setImage( ImageTools.getImage( path ), 0 );
	}

	/**
	 * Set image to display on the left side of the content pane. This is
	 * typically used as 'wizard image'. A thin gray vertical line is displayed
	 * on the right size of the image to separate the image from the content.
	 *
	 * @param path          Path to image to place on left side ({@code null} =>
	 *                      none).
	 * @param minimumHeight Minimum image height to use (0 = no minimum).
	 */
	public void setImage( @Nullable final String path, final int minimumHeight )
	{
		setImage( ImageTools.getImage( path ), minimumHeight );
	}

	/**
	 * Set image to display on the left side of the content pane. This is
	 * typically used as 'wizard image'. A thin gray vertical line is displayed
	 * on the right size of the image to separate the image from the content.
	 *
	 * @param image Image to place on left side ({@code null} => none).
	 */
	public void setImage( @Nullable final BufferedImage image )
	{
		setImage( image, 0 );
	}

	/**
	 * Set image to display on the left side of the content pane. This is
	 * typically used as 'wizard image'. A thin gray vertical line is displayed
	 * on the right size of the image to separate the image from the content.
	 *
	 * @param image         Image to place on left side ({@code null} => none).
	 * @param minimumHeight Minimum image height to use (0 = no minimum).
	 */
	public void setImage( @Nullable final BufferedImage image, final int minimumHeight )
	{
		if ( image != null )
		{
			if ( _imagePanel != null )
			{
				throw new IllegalStateException( "already have image" );
			}

			final VerticalBanner imagePanel = new VerticalBanner( image, minimumHeight );
			add( imagePanel, BorderLayout.WEST );
			_imagePanel = imagePanel;
		}
	}
}
