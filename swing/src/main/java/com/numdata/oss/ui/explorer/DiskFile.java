/*
 * (C) Copyright Numdata BV 2008-2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package com.numdata.oss.ui.explorer;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Implementation of {@link VirtualFile} for a file on disk.
 *
 * @author  G. Meinders
 */
class DiskFile
	implements VirtualFile
{
	/**
	 * Real file on disk.
	 */
	@NotNull
	private final File _realFile;

	/**
	 * Filename suffix to use for thumbnail images.
	 */
	@NotNull
	private final String _thumbnailSuffix;

	/**
	 * Flag to indicate that {@link #getThumbnail} was called before, so we
	 * can reuse the previous result.
	 */
	private boolean _thumbnailRequested;

	/**
	 * Cached result of {@link #getThumbnail} method.
	 */
	@Nullable
	private ImageSource _thumbnail;

	/**
	 * Construct virtual disk file.
	 *
	 * @param   realFile    Real file on disk.
	 */
	DiskFile( @NotNull final File realFile )
	{
		_realFile           = realFile;
		_thumbnailSuffix    = "-thumbnail.png";
		_thumbnailRequested = false;
		_thumbnail          = null;
	}

	@NotNull
	@Override
	public String getName()
	{
		return _realFile.getName();
	}

	@Override
	public String getDisplayName( @NotNull final Locale locale )
	{
		return getName();
	}

	@Override
	public String getType()
	{
		final String result;

		if ( isDirectory() )
		{
			result = null;
		}
		else
		{
			final String name = _realFile.getName();
			if ( name != null )
			{
				final int dot = name.lastIndexOf( (int)'.' );
				result = ( dot >= 0 ) ? name.substring( dot + 1 ) : "";
			}
			else
			{
				result = null;
			}
		}

		return result;
	}

	@Override
	public boolean canWrite()
	{
		return _realFile.canWrite();
	}

	@Override
	public boolean isDirectory()
	{
		return _realFile.isDirectory();
	}

	@Nullable
	@Override
	public String getDescription( @NotNull final Locale locale )
	{
		return null;
	}

	@NotNull
	@Override
	public String getPath()
	{
		return _realFile.getPath();
	}

	@Nullable
	@Override
	public ImageSource getThumbnail( final int preferredWidth, final int preferredHeight )
	{
		ImageSource result = _thumbnail;
		if ( !_thumbnailRequested )
		{
			final File   file     = _realFile;
			final String basename = getBasename( file );

			final File thumbnailFile = new File( file.getParentFile(), basename + _thumbnailSuffix );
			if ( thumbnailFile .canRead() )
			{
				try
				{
					final URI thumbnailURI = thumbnailFile.toURI();
					result = new URLImageSource( thumbnailURI.toURL() );
				}
				catch ( MalformedURLException e )
				{
					System.err.println( "Error reading thumbnail '" + thumbnailFile.getPath() + "':" + e );
					/* ignore => will not have thumbnail */
				}
			}

			_thumbnail = result;
			_thumbnailRequested = true;
		}

		return result;
	}

	/**
	 * Get basename of the specified file. The basename if the file name with
	 * any parent folder and extention (last suffix starting with dot) removed.
	 *
	 * @param   file    File whose basename to get.
	 *
	 * @return  Basename of file.
	 */
	private static String getBasename( @NotNull final File file )
	{
		final String filename = file.getName();
		final int    lastDot  = filename.lastIndexOf( (int)'.' );

		return ( lastDot < 0 ) ? filename : filename.substring( 0, lastDot );
	}
}
