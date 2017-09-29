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
import java.awt.image.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import com.numdata.oss.ui.ImageTools.*;
import org.jetbrains.annotations.*;

/**
 * A panel showing an image. The image is stretched or shrunk if needed to fit
 * the panel size. If desired, the image can be made clickable.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings ( "NonSerializableFieldInSerializableClass" )
public class ImagePanel
extends JPanel
{
	/**
	 * Image displayed by this panel.
	 */
	@Nullable
	private BufferedImage _image;

	/**
	 * Whether the image should be scaled to fit inside the component.
	 */
	private ScaleMode _scaleMode = ScaleMode.CONTAIN;

	/**
	 * Cached scaled instance of the article image.
	 */
	@Nullable
	private BufferedImage _scaledImage = null;

	/**
	 * Registered action listeners.
	 */
	@NotNull
	private final List<ActionListener> _actionListeners = new LinkedList<ActionListener>();

	/**
	 * Action command associated with action event fired by this component.
	 */
	@Nullable
	private String _actionCommand;

	/**
	 * Horizontal alignment of the image.
	 */
	private float _horizontalAlignment = 0.5f;

	/**
	 * Vertical alignment of the image.
	 */
	private float _verticalAlignment = 0.5f;

	/**
	 * Construct border-less image panel without an image.
	 */
	public ImagePanel()
	{
		this( (BufferedImage)null );
	}

	/**
	 * Construct border-less image panel with the specified image.
	 *
	 * @param imagePath Path of image to display.
	 */
	public ImagePanel( @Nullable final String imagePath )
	{
		this( imagePath == null ? null : ImageTools.getImage( imagePath ) );
	}

	/**
	 * Construct border-less image panel with the specified image.
	 *
	 * @param image Image to display.
	 */
	public ImagePanel( @Nullable final BufferedImage image )
	{
		_image = image;
		_actionCommand = null;

		addMouseListener( new MouseAdapter()
		{
			@Override
			public void mouseClicked( final MouseEvent e )
			{
				final List<ActionListener> actionListeners = _actionListeners;
				if ( !actionListeners.isEmpty() )
				{
					final ActionEvent actionEvent = new ActionEvent( this, ActionEvent.ACTION_PERFORMED, _actionCommand );
					for ( final ActionListener actionListener : actionListeners )
					{
						actionListener.actionPerformed( actionEvent );
					}
				}
			}
		} );
	}

	/**
	 * Get displayed image.
	 *
	 * @return Displayed image.
	 */
	@Nullable
	public Image getImage()
	{
		return _image;
	}

	/**
	 * Set image to display.
	 *
	 * @param image Image to display.
	 */
	public void setImage( @Nullable final BufferedImage image )
	{
		_image = image;
		_scaledImage = null;
		invalidate();
		repaint();
	}

	/**
	 * Set image to display.
	 *
	 * @param imagePath Path of image to display.
	 */
	public void setImage( final String imagePath )
	{
		setImage( ImageTools.getImage( imagePath ) );
	}

	/**
	 * Returns the scale mode for the image.
	 *
	 * @return Scale mode.
	 */
	public ScaleMode getScaleMode()
	{
		return _scaleMode;
	}

	/**
	 * Sets the scale mode for the image.
	 *
	 * @param scaleMode Scale mode.
	 */
	public void setScaleMode( final ScaleMode scaleMode )
	{
		_scaleMode = scaleMode;
	}

	/**
	 * Returns the horizontal alignment of the image.
	 *
	 * @return Horizontal alignment, between 0 (left) and 1 (right).
	 */
	public float getHorizontalAlignment()
	{
		return _horizontalAlignment;
	}

	/**
	 * Sets the horizontal alignment of the image.
	 *
	 * @param horizontalAlignment Horizontal alignment, between 0 (left) and 1
	 *                            (right).
	 */
	public void setHorizontalAlignment( final float horizontalAlignment )
	{
		_horizontalAlignment = horizontalAlignment;
	}

	/**
	 * Returns the vertical alignment of the image.
	 *
	 * @return Vertical alignment, between 0 (left) and 1 (right).
	 */
	public float getVerticalAlignment()
	{
		return _verticalAlignment;
	}

	/**
	 * Sets the vertical alignment of the image.
	 *
	 * @param verticalAlignment Vertical alignment, between 0 (left) and 1
	 *                          (right).
	 */
	public void setVerticalAlignment( final float verticalAlignment )
	{
		_verticalAlignment = verticalAlignment;
	}

	@Override
	public Dimension getPreferredSize()
	{
		final Dimension result;
		final Image image = _image;
		if ( !isPreferredSizeSet() && ( image != null ) )
		{
			final int width = image.getWidth( this );
			final int height = image.getHeight( this );

			if ( ( width >= 0 ) && ( height >= 0 ) )
			{
				final Insets insets = getInsets();
				result = new Dimension( width + insets.left + insets.right, height + insets.top + insets.bottom );
			}
			else
			{
				result = super.getPreferredSize();
			}
		}
		else
		{
			result = super.getPreferredSize();
		}

		return result;
	}

	/**
	 * Add action listeners. This listener will be notified when the image is
	 * clicked.
	 *
	 * @param actionListener Action listeners to add.
	 */
	public void addActionListener( @NotNull final ActionListener actionListener )
	{
		final List<ActionListener> listeners = _actionListeners;

		if ( listeners.isEmpty() )
		{
			setCursor( Cursor.getPredefinedCursor( Cursor.HAND_CURSOR ) );
		}

		listeners.add( actionListener );
	}

	/**
	 * Remove previously registered action listeners. If the last listener is
	 * removed, the image will no longer be clickable. Note that this method has
	 * no effect if the specified action listener was not previously
	 * registered.
	 *
	 * @param actionListener Action listeners to remove.
	 */
	public void removeActionListener( @Nullable final ActionListener actionListener )
	{
		final List<ActionListener> listeners = _actionListeners;
		if ( listeners.remove( actionListener ) && listeners.isEmpty() )
		{
			setCursor( null );
		}
	}

	/**
	 * Get action command associated with action events fired by this
	 * component.
	 *
	 * @return Action command.
	 */
	@Nullable
	public String getActionCommand()
	{
		return _actionCommand;
	}

	/**
	 * Set action command associated with action events fired by this
	 * component.
	 *
	 * @param actionCommand Action command.
	 */
	public void setActionCommand( @Nullable final String actionCommand )
	{
		_actionCommand = actionCommand;
	}

	@Override
	protected void paintComponent( final Graphics g )
	{
		super.paintComponent( g );

		if ( _image != null )
		{
			final Insets i = getInsets();
			final int width = getWidth() - i.left - i.right;
			final int height = getHeight() - i.top - i.bottom;

			final BufferedImage displayedImage;

			ScaleMode scaleMode = getScaleMode();
			if ( scaleMode != ScaleMode.NONE )
			{
				BufferedImage scaledImage = _scaledImage;
				if ( ( scaledImage == null ) || ( ( scaledImage.getWidth() != width ) && ( scaledImage.getHeight() != height ) ) )
				{
					scaledImage = ImageTools.createScaledInstance( _image, width, height, scaleMode, null );
					_scaledImage = scaledImage;
				}

				displayedImage = scaledImage;
			}
			else
			{
				displayedImage = _image;
			}

			final int x = i.left + Math.round( _horizontalAlignment * ( width - displayedImage.getWidth() ) );
			final int y = i.top + Math.round( _verticalAlignment * ( height - displayedImage.getHeight() ) );

			g.drawImage( displayedImage, x, y, this );
		}
	}
}
