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
package com.numdata.oss.ui.explorer;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.jetbrains.annotations.NotNull;

/**
 * An image source that retrieves an image from a URL.
 *
 * @author Gerrit Meinders
 */
public class URLImageSource
	implements ImageSource
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -5448564244357727139L;

	/**
	 * URL to retrieve the image from.
	 */
	private final URL _url;

	/**
	 * Constructs a new image source from the given URL.
	 *
	 * @param   url     URL to get an image from.
	 */
	public URLImageSource( final URL url )
	{
		_url = url;
	}

	@Override
	@NotNull
	public Image getImage()
		throws IOException
	{
		final URLConnection urlConnection = _url.openConnection();
		urlConnection.setAllowUserInteraction( false );
		urlConnection.setDoInput( true );
		urlConnection.setUseCaches( true );

		final Image image = (Image)urlConnection.getContent( new Class<?>[] { Image.class } );
		if ( image == null )
		{
			throw new IOException( "Failed to load image from " + _url );
		}

		return image;
	}

	/**
	 * Returns the URL of the image.
	 *
	 * @return Image URL.
	 */
	public URL getUrl()
	{
		return _url;
	}
}
