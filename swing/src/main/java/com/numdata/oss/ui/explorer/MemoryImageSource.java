/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import javax.imageio.ImageIO;

import org.jetbrains.annotations.NotNull;

/**
 * An image source that provides an image from memory.
 *
 * To support serialization of the image source, the underlying image must
 * implement either {@link RenderedImage} or {@link Serializable}. If no
 * serialization is needed, any image may be used.
 *
 * @author  Gerrit Meinders
 */
public class MemoryImageSource
	implements ImageSource
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 8980329913231413293L;

	/**
	 * Image provided by this source.
	 */
	private Image _image;

	/**
	 * Constructs a new image source providing the given image.
	 *
	 * @param   image   Image to create a source for.
	 */
	public MemoryImageSource( @NotNull final Image image )
	{
		_image = image;
	}

	@NotNull
	public Image getImage()
		throws IOException
	{
		return _image;
	}

	/**
	 * Writes the image to the given stream. If the image is a rendered image,
	 * it is written in PNG format. Otherwise, an attempt is made to serialize
	 * the image object.
	 *
	 * @param   out     Output stream to write to.
	 *
	 * @throws  IOException if an I/O error occurs.
	 * @throws  NotSerializableException if the image does not implement
	 *          {@link RenderedImage} and is itself not serializable.
	 */
	private void writeObject( final ObjectOutputStream out )
		throws IOException
	{
		final Image image = _image;
		if ( image instanceof RenderedImage )
		{
			out.writeBoolean( true );
			ImageIO.write( (RenderedImage)image , "png" , out );
		}
		else
		{
			out.writeBoolean( false );
			out.writeObject( _image );
		}
	}

	/**
	 * Reads an image from the given stream, as written by {@link #writeObject}.
	 *
	 * @param   in      Input stream to read from.
	 *
	 * @throws  IOException if an I/O error occurs.
	 * @throws  ClassNotFoundException if an unknown class is encountered.
	 */
	private void readObject( final ObjectInputStream in )
		throws IOException , ClassNotFoundException
	{
		if ( in.readBoolean() )
		{
			_image = ImageIO.read( in );
		}
		else
		{
			_image = (Image)in.readObject();
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + "[" + _image + "]";
	}
}
