/*
 * Copyright (c) 2004-2018, Numdata BV, The Netherlands.
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
import java.util.List;
import javax.imageio.*;
import javax.imageio.stream.*;
import javax.swing.*;

import com.numdata.oss.io.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class provides an (AWT) image toolbox. It also provides a central point
 * for accessing shared image resources.
 *
 * This code does not depend on any API outside this project or the Java2
 * standard.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "PublicField", "ConstantNamingConvention", "StaticNonFinalField" } )
public class ImageTools
{
	/**
	 * Debug flag to delay image loading operations to simulate low bandwidth
	 * network connections.
	 */
	public static final long LOAD_DELAY = 0L;

	/**
	 * Mode for scaling an image.
	 */
	public enum ScaleMode
	{
		/**
		 * Use the original image size.
		 */
		NONE,

		/**
		 * Scale the image to cover the entire component, while preserving the
		 * image's aspect ratio.
		 */
		COVER,

		/**
		 * Scale the image to fit inside the component, while preserving the
		 * image's aspect ratio.
		 */
		CONTAIN,

		/**
		 * Scale the image to fit the entire component, without preserving the
		 * image's aspect ratio.
		 */
		STRETCH
	}

	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( ImageTools.class );

	/**
	 * Switch to enable warning messages (on {@link System#err}) when images are
	 * not found. This may be very useful for tracing program bugs or deployment
	 * errors.
	 *
	 * This option can be set using the {@code ImageTools.missingImageWarnings}
	 * system property.
	 */
	public static boolean missingImageWarningsEnabled = true;

	/**
	 * Show warning is image is loaded on EDT. Since image loading can be time
	 * consuming, loading them on the EDT may disrupt responsiveness of the
	 * application. Loading should be deferred to worker threads instead.
	 *
	 * This option can be set using the {@code ImageTools.edtWarnings} system
	 * property.
	 */
	public static boolean edtWarningsEnabled = true;

	/**
	 * Show warning if icon is loaded without preset dimensions; if using preset
	 * dimensions, icons may be loaded asynchronously using the {@link
	 * AsyncIcon} class.
	 *
	 * This option can be set using the {@code ImageTools.presetIconSizeWarnings}
	 * system property.
	 */
	public static boolean presetIconSizeWarningsEnabled = true;

	/**
	 * Show warnings when using methods that are not suitable for server-side
	 * use, e.g. due to permanent caching.
	 */
	public static boolean serverSideWarningsEnabled = false;

	/**
	 * Search path to use for loading image resources.
	 */
	private static final List<String> _searchPath = new LinkedList<String>();

	/**
	 * Class loader to load image resources.
	 */
	private static ClassLoader _classLoader = ImageTools.class.getClassLoader();

	/**
	 * Cache for previously loaded images.
	 *
	 * @see #getImage
	 */
	private static final Map<String, BufferedImage> _imageCache = new HashMap<String, BufferedImage>();

	/**
	 * Cache for resources relative to classes.
	 *
	 * @see #getClassResource(Class, String)
	 * @see #getImageIcon(Class, String)
	 */
	private static final Map<Class<?>, Map<String, URL>> _classResourceCache = new HashMap<Class<?>, Map<String, URL>>();

	static
	{
		missingImageWarningsEnabled = getSystemSetting( "missingImageWarnings", true );
		edtWarningsEnabled = getSystemSetting( "edtWarnings", true );
		presetIconSizeWarningsEnabled = getSystemSetting( "presetIconSizeWarnings", true );

	}

	/**
	 * Helper-method to get boolean system property. This supports 'yes'/'no';
	 * 'y'/'n'; '0'/'1'; 'on'/'off', and 'true'/'false' values. If none of these
	 * values is set or the system property is not set at all, the default value
	 * is returned.
	 *
	 * NOTE: This method is similar to {@link Boolean#getBoolean(String)}, but
	 * allows more value formats and supports a default value.
	 *
	 * @param name          Name of system property.
	 * @param defaultResult Default boolean value if no valid value is set.
	 *
	 * @return Boolean value.
	 */
	private static boolean getSystemSetting( final String name, final boolean defaultResult )
	{
		boolean result = defaultResult;

		try
		{
			final String value = System.getProperty( "ImageTools." + name );

			if ( "true".equalsIgnoreCase( value ) ||
			     "on".equalsIgnoreCase( value ) ||
			     "yes".equalsIgnoreCase( value ) ||
			     "y".equalsIgnoreCase( value ) ||
			     "1".equals( value ) )
			{
				result = true;
			}
			else if ( "false".equalsIgnoreCase( value ) ||
			          "off".equalsIgnoreCase( value ) ||
			          "no".equalsIgnoreCase( value ) ||
			          "n".equalsIgnoreCase( value ) ||
			          "0".equals( value ) )
			{
				result = false;
			}
		}
		catch ( final Throwable ignored )
		{
			/* access to system property denied */
		}

		return result;
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private ImageTools()
	{
	}

	/**
	 * Disable warning messages about requested but missing images (on* {@link
	 * System#err}. These messages can be very useful when tracing program bugs
	 * or deployment errors, but can be annoying in test environments.
	 */
	public static void disableMissingImageWarnings()
	{
		missingImageWarningsEnabled = false;
	}

	/**
	 * Clear search path.
	 */
	public static void clearSearchPath()
	{
		synchronized ( _searchPath )
		{
			_searchPath.clear();
		}
	}

	/**
	 * Add entry to image search path. If the entry already exists, it will be
	 * ignored.
	 *
	 * @param path Path entry to add to image search path.
	 */
	public static void addToSearchPath( final String path )
	{
		final String actualEntry;

		if ( path == null )
		{
			throw new NullPointerException( "path" );
		}

		final int len = path.length();
		if ( len == 0 )
		{
			throw new IllegalArgumentException( "0-length string is not allowed in search path" );
		}

		/*
		 * Add proper separator to path entry if needed. This can't be determined
		 * 100% sure, but should work well with Windows, Unix(-like), and URL's.
		 */
		int sepPos;
		if ( ( sepPos = path.lastIndexOf( (int)File.separatorChar ) ) >= 0 )
		{
			actualEntry = ( sepPos < len - 1 ) ? path + File.separatorChar : path;
		}
		else if ( ( sepPos = path.lastIndexOf( (int)'/' ) ) >= 0 )
		{
			actualEntry = ( sepPos != len - 1 ) ? path + '/' : path;
		}
		else
		{
			actualEntry = path + File.separatorChar;
		}

		final List<String> searchPath = _searchPath;
		synchronized ( searchPath )
		{
			if ( !searchPath.contains( actualEntry ) )
			{
				searchPath.add( actualEntry );
			}
		}
	}

	/**
	 * Set class loader to load image resources.
	 *
	 * @param classLoader Class loader to load image resources.
	 */
	public static void setClassLoader( final ClassLoader classLoader )
	{
		_classLoader = classLoader;
	}


	/**
	 * Get {@link ImageIcon} for the given path. Resources are loaded against
	 * the specified {@code referenceClass}.
	 *
	 * @param referenceClass Class to resolve path against to and whose class
	 *                       loader to use.
	 * @param path           Image path.
	 *
	 * @return Icon object; {@code null} if image is not found.
	 */
	@Nullable
	public static ImageIcon getImageIcon( @NotNull final Class<?> referenceClass, @Nullable final String path )
	{
		final ImageIcon result;

		final BufferedImage image = getImage( referenceClass, path );
		if ( image != null )
		{
			result = new ImageIcon( image );
			if ( presetIconSizeWarningsEnabled )
			{
				new Throwable( "[ImageTools: TODO: Load '" + path + "' icon with preset dimensions: " + result.getIconWidth() + " x " + result.getIconHeight() + ']' ).printStackTrace();
			}
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * Get {@link ImageIcon} for the given path. Resources are loaded against
	 * the specified {@code referenceClass}.
	 *
	 * @param referenceClass Class to resolve path against to and whose class
	 *                       loader to use.
	 * @param path           Image path.
	 *
	 * @return Icon object; {@code null} if image is not found.
	 */
	@Nullable
	public static BufferedImage getImage( @NotNull final Class<?> referenceClass, @Nullable final String path )
	{
		BufferedImage result = null;

		if ( path != null )
		{
			final URL url = getClassResource( referenceClass, path );
			if ( url != null )
			{
				result = getImage( url );
			}
			else
			{
				result = getImage( path );
			}
		}

		return result;
	}

	/**
	 * Get URL to resource relative to the given class.
	 *
	 * This method uses a permanent cache to speed up future requests.
	 *
	 * @param referenceClass Class to resolve path against to and whose class
	 *                       loader to use.
	 * @param path           Resource path.
	 *
	 * @return {@link URL} to resource; {@code null} if the resource was not
	 * found.
	 */
	@Nullable
	static URL getClassResource( @NotNull final Class<?> referenceClass, @NotNull final String path )
	{
		URL result;

		final Map<Class<?>, Map<String, URL>> cache = _classResourceCache;
		synchronized ( cache )
		{
			final Map<Class<?>, Map<String, URL>> classResourceCache = cache;

			Map<String, URL> resources = classResourceCache.get( referenceClass );
			if ( resources == null )
			{
				resources = new HashMap<String, URL>();
				classResourceCache.put( referenceClass, resources );
				result = referenceClass.getResource( path );
				resources.put( path, result );
			}
			else
			{
				result = resources.get( path );
				if ( ( result == null ) && !resources.containsKey( path ) )
				{
					result = referenceClass.getResource( path );
					resources.put( path, result );
				}
			}
		}

		return result;
	}

	/**
	 * Get image for the specified URL.
	 *
	 * This method stores all returned images in a permanent cache! Please do
	 * not use this method for loading images that are rarely used.
	 *
	 * @param url URL to image file.
	 *
	 * @return Image object.
	 */
	@Nullable
	public static BufferedImage getImage( @Nullable final URL url )
	{
		BufferedImage result;

		if ( url == null )
		{
			result = null;
		}
		else
		{
			final String key = url.toExternalForm();
			final Map<String, BufferedImage> cache = _imageCache;

			final boolean needToLoad;

			synchronized ( cache )
			{
				result = cache.get( key );
				needToLoad = ( result == null ) && !cache.containsKey( key );
			}

			if ( needToLoad )
			{
				if ( serverSideWarningsEnabled )
				{
					new Throwable( "[ImageTools.getImage] Unsafe use of 'getImage'; '" + url + "' will be cached permanently." ).printStackTrace();
				}

				try
				{
					result = load( url );
				}
				catch ( final IOException e )
				{
					if ( missingImageWarningsEnabled )
					{
						new Throwable( "[Image not found: " + url + "] (" + e + ')' ).printStackTrace();
					}
					result = null;
				}

				synchronized ( cache )
				{
					cache.put( key, result );
				}
			}
		}

		return result;
	}

	/**
	 * Get image for the specified path.
	 *
	 * This method stores all returned images in a permanent cache! Please do
	 * not use this method for loading images that are rarely used (use the
	 * {@link #load(String)} method for that purpose).
	 *
	 * @param path Path to image file.
	 *
	 * @return Image object.
	 */
	@Nullable
	public static BufferedImage getImage( @Nullable final String path )
	{
		BufferedImage result;

		if ( ( path == null ) || path.isEmpty() )
		{
			result = null;
		}
		else
		{
			final Map<String, BufferedImage> cache = _imageCache;

			final boolean needToLoad;
			synchronized ( cache )
			{
				result = cache.get( path );
				needToLoad = ( result == null ) && !cache.containsKey( path );
			}

			if ( needToLoad )
			{
				if ( serverSideWarningsEnabled )
				{
					new Throwable( "[ImageTools.getImage] Unsafe use of 'getImage'; '" + path + "' will be cached permanently." ).printStackTrace();
				}

				result = load( path );

				synchronized ( cache )
				{
					cache.put( path, result );
				}
			}
		}
		return result;
	}

	/**
	 * Load the image with the specified path. This method will not cache
	 * images, use the {@link #getImage} method for that purpose.
	 *
	 * @param path Path to image file.
	 *
	 * @return Image object.
	 */
	public static BufferedImage load( final String path )
	{
		LOG.debug( "load( " + path + " )" );
		final boolean trace = LOG.isTraceEnabled();

		BufferedImage result = null;

		if ( ( path != null ) && !path.isEmpty() )
		{
			final boolean mightBeRelative = ( path.indexOf( ':' ) < 0 );
			final String relativePath = ( mightBeRelative && ( path.charAt( 0 ) == '/' ) || ( path.charAt( 0 ) == '\\' ) ) ? path.substring( 1 ) : path;

			{
				InputStream stream = null;
				try
				{
					if ( trace )
					{
						LOG.trace( " - try loading resource " + relativePath );
					}
					stream = _classLoader.getResourceAsStream( relativePath );
					if ( stream != null )
					{
						result = load( stream );
					}
				}
				catch ( final IOException e )
				{
					LOG.trace( "failed to retrieve: " + relativePath, e );
				}
				catch ( final SecurityException e )
				{
					LOG.trace( "failed to retrieve: " + relativePath, e );
				}
				finally
				{
					if ( stream != null )
					{
						if ( trace )
						{
							LOG.trace( "      > found resource " + relativePath );
						}
						try
						{
							stream.close();
						}
						catch ( final IOException e )
						{
							LOG.trace( "failed to close: " + relativePath, e );
						}
					}
				}
			}

			if ( result == null )
			{
				final List<String> searchPath = _searchPath;
				synchronized ( searchPath )
				{
					final int last = mightBeRelative ? searchPath.size() : 0;

					for ( int i = 0; ( result == null ) && ( i <= last ); i++ )
					{
						final String actualPath = ( i < last ) ? searchPath.get( i ) + relativePath : path;
						if ( trace )
						{
							LOG.trace( " - try loading from " + actualPath );
						}

						try
						{
							if ( actualPath.contains( ":/" ) )
							{
								result = load( new URL( actualPath ) );
							}
							else
							{
								result = load( new File( actualPath ) );
							}
							if ( trace )
							{
								LOG.trace( "      > found  " + actualPath );
							}
						}
						catch ( final FileNotFoundException e )
						{
							LOG.trace( "failed to load: " + actualPath, e );
						}
						catch ( final IOException e )
						{
							LOG.trace( "failed to load: " + actualPath, e );
						}
						catch ( final SecurityException e )
						{
							LOG.trace( "failed to load: " + actualPath, e );
						}
					}
				}
			}

		}

		/*
		 * Provide clue to problems with loading the image.
		 */
		if ( result == null )
		{
			if ( missingImageWarningsEnabled )
			{
				new Throwable( "[Image not found: " + path + ']' ).printStackTrace();
			}
		}

		if ( ( result != null ) && edtWarningsEnabled && SwingUtilities.isEventDispatchThread() )
		{
			new Throwable( "[ImageTools: TODO: Should load '" + path + "' from worker thread, not the EDT!" + ( ( result != null ) ? " (loaded images size = " + result.getWidth() + " x " + result.getHeight() + ')' : "" ) + ']' ).printStackTrace();
		}

		if ( LOAD_DELAY > 0L )
		{
			System.err.println( "[ImageTools: load( '" + path + "') " + ( ( result != null ) ? "successful" : "failed" ) + ']' );
			try
			{
				Thread.sleep( ( result == null ) ? LOAD_DELAY / 3 : LOAD_DELAY );
			}
			catch ( final InterruptedException e )
			{
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Read image from file.
	 *
	 * @param file File.
	 *
	 * @return {@link BufferedImage} that was loaded.
	 *
	 * @throws IOException if the image could not be loaded.
	 */
	@NotNull
	public static BufferedImage load( @NotNull final File file )
	throws IOException
	{
		return load( DataStreamTools.readByteArray( file ) );
	}

	/**
	 * Read image from input stream.
	 *
	 * @param in Input stream.
	 *
	 * @return {@link BufferedImage} that was loaded.
	 *
	 * @throws IOException if the image could not be loaded.
	 */
	@NotNull
	public static BufferedImage load( @NotNull final InputStream in )
	throws IOException
	{
		final byte[] imageData;
		try
		{
			imageData = DataStreamTools.readByteArray( in );
		}
		finally
		{
			in.close();
		}

		return load( imageData );
	}

	/**
	 * Read image from URL.
	 *
	 * @param url URL for image.
	 *
	 * @return {@link BufferedImage} that was loaded.
	 *
	 * @throws IOException if the image could not be loaded.
	 */
	@NotNull
	public static BufferedImage load( @NotNull final URL url )
	throws IOException
	{
		final byte[] imageData;

		final InputStream in = url.openStream();
		try
		{
			imageData = DataStreamTools.readByteArray( in );

		}
		finally
		{
			in.close();
		}

		return load( imageData );
	}

	/**
	 * Read image from image stream.
	 *
	 * @param imageInputStream Image stream.
	 *
	 * @return {@link BufferedImage} that was loaded.
	 *
	 * @throws IOException if the image could not be loaded.
	 */
	@NotNull
	public static BufferedImage load( @NotNull final ImageInputStream imageInputStream )
	throws IOException
	{
		byte[] imageData;
		try
		{
			imageData = null;
			final int length = (int)imageInputStream.length();
			byte[] buffer = new byte[ ( length >= 0 ) ? length : 1024 ]; // initial buffer size

			while ( true )
			{
				final int numberRead = imageInputStream.read( buffer );
				if ( numberRead == length )
				{
					imageData = buffer;
					break;
				}
				else if ( numberRead <= 0 )
				{
					if ( imageData == null )
					{
						throw new IOException( "Zero length image data" );
					}
					break;
				}
				else if ( imageData == null ) // create first result
				{
					imageData = new byte[ numberRead ];
					System.arraycopy( buffer, 0, imageData, 0, numberRead );
				}
				else // Create next buffer
				{
					final byte[] newTotal = new byte[ imageData.length + numberRead ];

					System.arraycopy( imageData, 0, newTotal, 0, imageData.length );
					System.arraycopy( buffer, 0, newTotal, imageData.length, numberRead );

					buffer = imageData;
					imageData = newTotal;
				}
			}
		}
		finally
		{
			imageInputStream.close();
		}

		return load( imageData );
	}

	/**
	 * Read image from image data.
	 *
	 * @param imageData Image data.
	 *
	 * @return {@link BufferedImage} that was loaded.
	 *
	 * @throws IOException if the image could not be loaded.
	 */
	@NotNull
	public static BufferedImage load( @NotNull final byte[] imageData )
	throws IOException
	{

		// Try to work around bugs in the JDK:
		//  - Bug 6986863: ProfileDeferralMgr throwing ConcurrentModificationException (https://bugs.openjdk.java.net/browse/JDK-6986863)
		//  - Bug 8032243: LCMS error 13: Couldn't link the profiles  (https://bugs.openjdk.java.net/browse/JDK-8032243)
		//
		// We try to work around these bugs by synchronizing on ImageIO (dirty!), basically making the JPEG decoding single-threaded.
		synchronized ( ImageIO.class )
		{
			final BufferedImage result = ImageIO.read( new ByteArrayInputStream( imageData ) );
			if ( result == null )
			{
				throw new IOException( "ImageIO failed to load image (" + imageData.length + " bytes)" );
			}

			return result;
		}
	}

	/**
	 * Returns a scaled instance of the given image. When the image size is
	 * reduced, a multi-step bilinear scaling method is used. This method avoids
	 * glitches that appear when using traditional bilinear or bicubic when an
	 * image is scaled to less than 50% its original size. When the image size
	 * is increased, bicubic interpolation is used instead.
	 *
	 * Based on code from an article published at 'java.net': <a
	 * href="http://today.java.net/pub/a/today/2007/04/03/perils-of-image-getscaledinstance.html">
	 * The Perils of Image.getScaledInstance()</a>.
	 *
	 * @param source       Image to be scaled.
	 * @param targetWidth  Width of the result, in pixels.
	 * @param targetHeight Height of the result, in pixels.
	 * @param scaleMode    Scale mode to use.
	 * @param background   Fill paint to use for background if keeping the
	 *                     aspect ratio requires 'empty' area; {@code null} will
	 *                     result in a smaller result image (empty areas are
	 *                     removed).
	 *
	 * @return Scaled version of the given image. If the source image already
	 * had the specified target size, the source image is returned.
	 */
	public static BufferedImage createScaledInstance( @NotNull final BufferedImage source, final int targetWidth, final int targetHeight, @NotNull final ScaleMode scaleMode, @Nullable final Paint background )
	{
		if ( ( source.getWidth() <= 0 ) || ( source.getHeight() <= 0 ) )
		{
			throw new IllegalArgumentException( "source image has invalid size: " + source.getWidth() + " x " + source.getHeight() );
		}

		if ( ( targetWidth <= 0 ) || ( targetHeight <= 0 ) )
		{
			throw new IllegalArgumentException( "invalid target size specified: " + targetWidth + " x " + targetHeight );
		}

		final boolean haveBackground = ( background != null );

		/*
		 * Determine appropriate image type.
		 */
		final ColorModel sourceColorModel = source.getColorModel();
		final int scaledType = sourceColorModel.hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;

		int scaledImageWidth = source.getWidth();
		int scaledImageHeight = source.getHeight();

		/*
		 * Determine width and height of the target image, taking the
		 * 'keepAspect' parameter into account.
		 */
		final int imageTargetWidth;
		final int imageTargetHeight;

		if ( scaleMode == ScaleMode.COVER )
		{
			final double targetScale = Math.max( (double)targetWidth / (double)scaledImageWidth,
			                                     (double)targetHeight / (double)scaledImageHeight );

			imageTargetWidth = (int)Math.round( (double)scaledImageWidth * targetScale );
			imageTargetHeight = (int)Math.round( (double)scaledImageHeight * targetScale );
		}
		else if ( scaleMode == ScaleMode.CONTAIN )
		{
			final double targetScale = Math.min( (double)targetWidth / (double)scaledImageWidth,
			                                     (double)targetHeight / (double)scaledImageHeight );

			imageTargetWidth = (int)Math.round( (double)scaledImageWidth * targetScale );
			imageTargetHeight = (int)Math.round( (double)scaledImageHeight * targetScale );
		}
		else if ( scaleMode == ScaleMode.STRETCH )
		{
			imageTargetWidth = targetWidth;
			imageTargetHeight = targetHeight;
		}
		else
		{
			throw new IllegalArgumentException( "Unsupported scale mode: " + scaleMode );
		}

		/*
		 * Choose interpolation method.
		 */
		final Object interpolation;
		if ( ( imageTargetWidth < scaledImageWidth ) || ( imageTargetHeight < scaledImageHeight ) )
		{
			interpolation = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		}
		else
		{
			interpolation = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
		}

		/*
		 * Perform scaling, using multiple steps when reducing to less than 50%.
		 */
		BufferedImage result = source;
		while ( ( imageTargetWidth != scaledImageWidth ) || ( imageTargetHeight != scaledImageHeight ) )
		{
			scaledImageWidth = Math.max( scaledImageWidth / 2, imageTargetWidth );
			scaledImageHeight = Math.max( scaledImageHeight / 2, imageTargetHeight );

			/*
			 * Use specified target size instead of the aspect-correct size in
			 * the final scaling step.
			 */
			final boolean finalStep = ( scaledImageWidth == imageTargetWidth ) && ( scaledImageHeight == imageTargetHeight );
			final int scaledImageX = finalStep && haveBackground ? ( targetWidth - imageTargetWidth ) / 2 : 0;
			final int scaledImageY = finalStep && haveBackground ? ( targetHeight - imageTargetHeight ) / 2 : 0;
			final int scaledCanvasWidth = finalStep ? haveBackground ? targetWidth : imageTargetWidth : scaledImageWidth;
			final int scaledCanvasHeight = finalStep ? haveBackground ? targetHeight : imageTargetHeight : scaledImageHeight;
			/*
			 * Perform the scaling step.
			 */
			final BufferedImage scaledImage = new BufferedImage( scaledCanvasWidth, scaledCanvasHeight, scaledType );

			final Graphics2D g2 = scaledImage.createGraphics();
			if ( finalStep && haveBackground )
			{
				g2.setPaint( background );
				g2.fillRect( 0, 0, scaledCanvasWidth, scaledCanvasHeight );
			}
			g2.setRenderingHint( RenderingHints.KEY_INTERPOLATION, interpolation );
			g2.drawImage( result, scaledImageX, scaledImageY, haveBackground ? scaledImageWidth : scaledCanvasWidth, haveBackground ? scaledImageHeight : scaledCanvasHeight, null );
			g2.dispose();

			result = scaledImage;
		}

		return result;
	}

	/**
	 * Creates a grayscaled image from the given source. If no background color
	 * is specified, transparent areas of the source image will appear black in
	 * the result.
	 *
	 * @param source     Source image.
	 * @param background Background color; may be {@code null}.
	 *
	 * @return Grayscaled image.
	 */
	public static BufferedImage createGrayscaleInstance( final BufferedImage source, final Color background )
	{
		if ( source == null )
		{
			throw new NullPointerException( "source" );
		}

		if ( ( source.getWidth() <= 0 ) || ( source.getHeight() <= 0 ) )
		{
			throw new IllegalArgumentException( "source image has invalid size: " + source.getWidth() + " x " + source.getHeight() );
		}

		final int width = source.getWidth();
		final int height = source.getHeight();

		final BufferedImage grayscale = new BufferedImage( width, height, BufferedImage.TYPE_BYTE_GRAY );

		final Graphics2D g = grayscale.createGraphics();
		if ( background != null )
		{
			g.setColor( Color.WHITE );
			g.fillRect( 0, 0, width, height );
		}
		g.setRenderingHint( RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY );
		g.setRenderingHint( RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE );
		g.drawImage( source, 0, 0, null );
		g.dispose();
		return grayscale;
	}

	/**
	 * Bayer's optimal dither matrix.
	 *
	 * @see <a href="http://scien.stanford.edu/class/psych221/projects/02/mdeleon/htbasics.html">Halftoning
	 * Basics</a>
	 */
	private static final int[][] BAYER_DITHER_MATRIX =
	{
	{ 1, 17, 5, 21, 2, 18, 6, 22 },
	{ 25, 9, 29, 13, 26, 10, 30, 14 },
	{ 7, 23, 3, 19, 8, 24, 4, 20 },
	{ 31, 15, 27, 11, 32, 16, 28, 12 },
	{ 2, 18, 6, 22, 1, 17, 5, 21 },
	{ 26, 10, 30, 14, 25, 9, 29, 13 },
	{ 8, 24, 4, 20, 7, 23, 3, 19 },
	{ 32, 16, 28, 12, 31, 15, 27, 11 }
	};

	/**
	 * Creates a halftoned black and white image from the given source. If no
	 * background color is specified, transparent areas of the source image will
	 * appear black in the result.
	 *
	 * @param source     Source image.
	 * @param background Background color; may be {@code null}.
	 *
	 * @return Black and white image.
	 *
	 * @see <a href="http://www.tud.ttu.ee/~t992514/programming/java.html">Image
	 * dithering using a dither matrix</a>
	 */
	public static BufferedImage createBlackAndWhiteInstance( final BufferedImage source, final Color background )
	{
		if ( source == null )
		{
			throw new NullPointerException( "source" );
		}

		if ( ( source.getWidth() <= 0 ) || ( source.getHeight() <= 0 ) )
		{
			throw new IllegalArgumentException( "source image has invalid size: " + source.getWidth() + " x " + source.getHeight() );
		}

		/*
		 * For some reason Java2D doesn't perform dithering on 1-bit images,
		 * so we'll just do it ourselves.
		 */

		final int[][] ditherMatrix = BAYER_DITHER_MATRIX;

		final int width = source.getWidth();
		final int height = source.getHeight();
		final int n = ditherMatrix.length;

		final BufferedImage result = new BufferedImage( width, height, BufferedImage.TYPE_BYTE_BINARY );
		final WritableRaster resultRaster = result.getRaster();

		final boolean isIntRGB = ( source.getType() == BufferedImage.TYPE_INT_ARGB ) ||
		                         ( source.getType() == BufferedImage.TYPE_INT_RGB );

		final ColorModel colorModel = source.getColorModel();
		final boolean sourceHasAlpha = colorModel.hasAlpha();

		final WritableRaster sourceRaster = source.getRaster();
		final DataBuffer sourceDataBuffer = sourceRaster.getDataBuffer();

		final int backgroundRed = ( background == null ) ? 0 : background.getRed();
		final int backgroundGreen = ( background == null ) ? 0 : background.getGreen();
		final int backgroundBlue = ( background == null ) ? 0 : background.getBlue();

		for ( int y = 0; y < height; ++y )
		{
			for ( int x = 0; x < width; ++x )
			{
				/*
				 * Get source ARGB components for the pixel.
				 */
				final int argb = isIntRGB ? sourceDataBuffer.getElem( y * width + x ) : source.getRGB( x, y );
				int red = ( argb & 0xff0000 ) >>> 16;
				int green = ( argb & 0xff00 ) >>> 8;
				int blue = ( argb & 0xff );

				if ( sourceHasAlpha )
				{
					/*
					 * Apply background color.
					 */
					final int alpha = ( argb & 0xff000000 ) >>> 24;
					red = ( red * alpha + backgroundRed * ( 0xff - alpha ) ) / 0xff;
					green = ( green * alpha + backgroundGreen * ( 0xff - alpha ) ) / 0xff;
					blue = ( blue * alpha + backgroundBlue * ( 0xff - alpha ) ) / 0xff;
				}

				/*
				 * Grayscale and threshold according to dither matrix.
				 */
				final int gray = ( red * 30 + green * 59 + blue * 11 ) / 100;
				final int threshold = 0xff * ditherMatrix[ y % n ][ x % n ] / 32;
				if ( gray >= threshold )
				{
					resultRaster.setSample( x, y, 0, 1 );
				}
			}
		}

		return result;
	}

	/**
	 * Convenience method for writing image files.
	 *
	 * @param file     File to write image to.
	 * @param image    Image to write to file.
	 * @param mimeType Mime type to encode image with.
	 *
	 * @throws IOException if there was a problem writing the image file.
	 */
	public static void writeImage( final File file, final BufferedImage image, final String mimeType )
	throws IOException
	{
		if ( file == null )
		{
			throw new NullPointerException( "file" );
		}

		if ( image == null )
		{
			throw new NullPointerException( "image" );
		}

		if ( mimeType == null )
		{
			throw new NullPointerException( "mimeType" );
		}

		final ImageWriter imageWriter;
		{
			final Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType( mimeType );
			if ( !writers.hasNext() )
			{
				throw new IOException( "Unknown image MIME type '" + mimeType + '\'' );
			}

			imageWriter = writers.next();
		}

		final File parent = file.getParentFile();
		if ( parent != null )
		{
			parent.mkdirs();
		}

		final ImageOutputStream os = new FileImageOutputStream( file );
		try
		{
			imageWriter.setOutput( os );
			imageWriter.write( image );
		}
		finally
		{
			os.close();
		}
	}

	/**
	 * Writes the given image to the given file as a JPEG image. This method
	 * provides a convenient {@code quality} parameter to set the compression
	 * quality, which can be quite a hassle using the API directly.
	 *
	 * @param file    File to write to.
	 * @param image   Image to be written.
	 * @param quality Compression quality between 0 and 1.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void writeImageAsJpeg( @NotNull final File file, @NotNull final BufferedImage image, final float quality )
	throws IOException
	{
		final Iterator<ImageWriter> imageWriters = ImageIO.getImageWritersByMIMEType( "image/jpeg" );
		if ( !imageWriters.hasNext() )
		{
			throw new IOException( "No image writers available for 'image/jpeg'." );
		}

		final ImageWriter imageWriter = imageWriters.next();
		final ImageWriteParam param = imageWriter.getDefaultWriteParam();
		param.setCompressionMode( ImageWriteParam.MODE_EXPLICIT );
		param.setCompressionQuality( quality );

		final FileImageOutputStream fileOut = new FileImageOutputStream( file );
		try
		{
			imageWriter.setOutput( fileOut );
			imageWriter.write( null, new IIOImage( image, null, null ), param );
			imageWriter.dispose();
		}
		finally
		{
			fileOut.close();
		}
	}
}
