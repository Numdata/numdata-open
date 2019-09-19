/*
 * Copyright (c) 2013-2018, Numdata BV, The Netherlands.
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
import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.stream.*;
import javax.swing.*;
import javax.swing.table.*;

import com.numdata.oss.collections.*;
import org.jetbrains.annotations.*;

/**
 * An {@link Icon} implementation that loads icon images asynchronously.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings ( "NonSerializableFieldInSerializableClass" )
public class AsyncIcon
implements Icon
{
	/**
	 * Icon width.
	 */
	private int _width;

	/**
	 * Icon height.
	 */
	private int _height;

	/**
	 * Icon image ({@code null} until loaded).
	 */
	private Image _image = null;

	/**
	 * Components that will be updated after the image is loaded.
	 */
	private Collection<Component> _componentsToUpdate = new IdentityHashSet<Component>();

	/**
	 * Creates an icon from the specified file.
	 *
	 * @param path               Image path.
	 * @param width              Initial icon width.
	 * @param height             Initial icon height.
	 * @param componentsToUpdate Components to update after image is loaded.
	 */
	public AsyncIcon( @NotNull final String path, final int width, final int height, final Component... componentsToUpdate )
	{
		this( new Callable<Image>()
		{
			@Override
			@Nullable
			public Image call()
			throws Exception
			{
				final BufferedImage image = ImageTools.getImage( path );
				if ( ( image != null ) && ( ( ( width != 0 ) && ( image.getWidth() != width ) ) || ( ( height != 0 ) && ( image.getHeight() != height ) ) ) )
				{
					System.err.println( "note: Icon '" + path + "' initialized at " + width + 'x' + height + ", but image is " + image.getWidth() + 'x' + image.getHeight() + " pixels" );
				}
				return image;
			}
		}, width, height, componentsToUpdate );
	}

	/**
	 * Creates an icon from the specified file.
	 *
	 * @param file               Image file.
	 * @param width              Initial icon width.
	 * @param height             Initial icon height.
	 * @param componentsToUpdate Components to update after image is loaded.
	 */
	public AsyncIcon( @NotNull final File file, final int width, final int height, final Component... componentsToUpdate )
	{
		this( new Callable<Image>()
		{
			@Override
			public Image call()
			throws Exception
			{
				if ( !file.canRead() )
				{
					throw new FileNotFoundException( file.toString() );
				}

				final BufferedImage image = ImageTools.load( file );

				if ( ( ( width != 0 ) && ( image.getWidth() != width ) ) || ( ( height != 0 ) && ( image.getHeight() != height ) ) )
				{
					System.err.println( "note: Icon '" + file + "' initialized at " + width + 'x' + height + ", but image is " + image.getWidth() + 'x' + image.getHeight() + " pixels" );
				}
				return image;
			}
		}, width, height, componentsToUpdate );
	}

	/**
	 * Creates an icon from an the specified URL.
	 *
	 * @param url                URL for the image.
	 * @param width              Initial icon width.
	 * @param height             Initial icon height.
	 * @param componentsToUpdate Components to update after image is loaded.
	 */
	public AsyncIcon( @NotNull final URL url, final int width, final int height, final Component... componentsToUpdate )
	{
		this( new Callable<Image>()
		{
			@Override
			public Image call()
			throws Exception
			{
				final BufferedImage image = ImageTools.load( url );

				if ( ( ( width != 0 ) && ( image.getWidth() != width ) ) || ( ( height != 0 ) && ( image.getHeight() != height ) ) )
				{
					System.err.println( "note: Icon '" + url + "' initialized at " + width + 'x' + height + ", but image is " + image.getWidth() + 'x' + image.getHeight() + " pixels" );
				}
				return image;
			}
		}, width, height, componentsToUpdate );
	}

	/**
	 * Create icon from image file data.
	 *
	 * @param imageData          Image file data.
	 * @param width              Initial icon width.
	 * @param height             Initial icon height.
	 * @param componentsToUpdate Components to update after image is loaded.
	 */
	public AsyncIcon( @NotNull final byte[] imageData, final int width, final int height, final Component... componentsToUpdate )
	{
		this( new Callable<Image>()
		{
			@Override
			public Image call()
			throws Exception
			{
				return ImageTools.load( imageData );
			}
		}, width, height, componentsToUpdate );
	}

	/**
	 * Create icon from image.
	 *
	 * @param image              Image.
	 * @param width              Initial icon width.
	 * @param height             Initial icon height.
	 * @param componentsToUpdate Components to update after image is loaded.
	 */
	public AsyncIcon( @NotNull final Image image, final int width, final int height, final Component... componentsToUpdate )
	{
		this( new Callable<Image>()
		{
			@Override
			public Image call()
			throws Exception
			{
				return new ImageIcon( image ).getImage();
			}
		}, width, height, componentsToUpdate );
	}

	/**
	 * Create icon from an input stream.
	 *
	 * @param in                 Stream to read the image from.
	 * @param width              Initial icon width.
	 * @param height             Initial icon height.
	 * @param componentsToUpdate Components to update after image is loaded.
	 */
	public AsyncIcon( @NotNull final InputStream in, final int width, final int height, final Component... componentsToUpdate )
	{
		this( new Callable<Image>()
		{
			@Override
			public Image call()
			throws Exception
			{
				return ImageTools.load( in );
			}
		}, width, height, componentsToUpdate );
	}

	/**
	 * Create icon from an image stream.
	 *
	 * @param imageStream        Image input stream to read the image from.
	 * @param width              Initial icon width.
	 * @param height             Initial icon height.
	 * @param componentsToUpdate Components to update after image is loaded.
	 */
	public AsyncIcon( @NotNull final ImageInputStream imageStream, final int width, final int height, final Component... componentsToUpdate )
	{
		this( new Callable<Image>()
		{
			@Override
			public Image call()
			throws Exception
			{
				return ImageTools.load( imageStream );
			}
		}, width, height, componentsToUpdate );
	}

	/**
	 * Create icon from resource.  Resources are loaded against the specified
	 * {@code referenceClass}.
	 *
	 * @param referenceClass     Class to resolve path against to and whose
	 *                           class loader to use.
	 * @param resourcePath       Image resource path.
	 * @param width              Initial icon width.
	 * @param height             Initial icon height.
	 * @param componentsToUpdate Components to update after image is loaded.
	 */
	public AsyncIcon( @NotNull final Class<?> referenceClass, @NotNull final String resourcePath, final int width, final int height, final Component... componentsToUpdate )
	{
		this( new Callable<Image>()
		{
			@Override
			@Nullable
			public Image call()
			throws Exception
			{
				BufferedImage image = ImageTools.getImage( referenceClass, resourcePath );
				if ( ( image != null ) && ( ( ( width != 0 ) && ( image.getWidth() != width ) ) || ( ( height != 0 ) && ( image.getHeight() != height ) ) ) )
				{
					System.err.println( "note: Icon '" + resourcePath + "' initialized at " + width + 'x' + height + ", but image is " + image.getWidth() + 'x' + image.getHeight() + " pixels; it will be scaled down" );
					image = ImageTools.createScaledInstance( image, width, height, ImageTools.ScaleMode.CONTAIN, null );
				}
				return image;
			}
		}, width, height, componentsToUpdate );
	}

	/**
	 * Create icon from resource.  Resources are loaded against the specified
	 * {@code referenceClass}.
	 *
	 * @param imageLoader        Callable that loads the image (will be called
	 *                           on worker thread).
	 * @param width              Initial icon width.
	 * @param height             Initial icon height.
	 * @param componentsToUpdate Components to update after image is loaded.
	 */
	public AsyncIcon( @NotNull final Callable<Image> imageLoader, final int width, final int height, final Component... componentsToUpdate )
	{
		_width = width;
		_height = height;
		_componentsToUpdate.addAll( Arrays.asList( componentsToUpdate ) );

		new Worker( imageLoader ).execute();
	}

	@Override
	public int getIconWidth()
	{
		return _width;
	}

	@Override
	public int getIconHeight()
	{
		return _height;
	}

	@Override
	public void paintIcon( final Component component, final Graphics g, final int x, final int y )
	{
		final int width = _width;
		final int height = _height;

		final Image image = _image;
		if ( image != null )
		{
			g.drawImage( image, x, y, width, height, null );
		}
		else
		{
			registerComponent( component );

			final Graphics nested = g.create();
			nested.setColor( Color.GRAY );
			nested.fillRect( x, y, width, height );
			nested.dispose();
		}
	}

	/**
	 * Register component that intends to paint the icon.
	 *
	 * This method is a no-op if the icon image is loaded; if the icon image is
	 * not loaded (yet), the component will be updated when the image has been
	 * loaded.
	 *
	 * @param component Target component on which icon will be painted.
	 */
	public void registerComponent( @NotNull final Component component )
	{
		if ( ( _image == null ) && ( _componentsToUpdate != null ) && !( component instanceof ListCellRenderer ) && !( component instanceof TableCellRenderer ) )
		{
			_componentsToUpdate.add( component );
		}
	}

	/**
	 * Called when image is loaded. This method is called on the EDT.
	 *
	 * @param image Image that was loaded.
	 */
	protected void imageLoaded( @NotNull final Image image )
	{
		_image = image;

		final int width = image.getWidth( null );
		final int height = image.getHeight( null );
		final boolean resized = ( width != _width || height != _height );
		_width = width;
		_height = height;

		// repaint components that called 'paintIcon' while the image was still loading
		final Collection<Component> componentsToUpdate = _componentsToUpdate;
		_componentsToUpdate = null;
		for ( final Component component : componentsToUpdate )
		{
			if ( resized )
			{
				SwingTools.revalidate( component );
			}
			component.repaint();
		}
	}

	/**
	 * Called when failed to load image. This method is called on the EDT.
	 *
	 * @param exception Exception that occurred while loading the image.
	 */
	private void failedToLoad( final Exception exception )
	{
		// print error
		System.err.println( "ERROR: Failed to load icon (" + ( ( exception.getCause() != null ) ? exception.getCause() : exception ) + ')' );
		exception.printStackTrace();

		// no more need to collection components to paint
		_componentsToUpdate = null;
	}

	/**
	 * A worker used to load an image.
	 */
	private class Worker
	extends SwingWorker<Image, Void>
	{
		/**
		 * Callable that loads the image.
		 */
		private final Callable<Image> _imageLoader;

		/**
		 * Create worker for the given job.
		 *
		 * @param imageLoader Callable that loads the image.
		 */
		private Worker( @NotNull final Callable<Image> imageLoader )
		{
			if ( ImageTools.serverSideWarningsEnabled )
			{
				new Throwable( "WARNING: Server-side creation of AsyncIcon.Worker should be avoided." ).printStackTrace();
			}
			_imageLoader = imageLoader;
		}

		@Override
		protected Image doInBackground()
		throws Exception
		{
			return _imageLoader.call();
		}

		@Override
		protected void done()
		{
			try
			{
				final Image image = get();
				if ( image != null )
				{
					imageLoaded( image );
				}
			}
			catch ( final Exception e )
			{
				failedToLoad( e );
			}
		}
	}
}
