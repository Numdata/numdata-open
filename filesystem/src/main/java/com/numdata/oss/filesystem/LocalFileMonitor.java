/*
 * Copyright (c) 2006-2019, Numdata BV, The Netherlands.
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
package com.numdata.oss.filesystem;

import java.io.*;
import java.net.*;
import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.io.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * Monitors a single file on the local file system.
 *
 * @author G. Meinders
 */
public class LocalFileMonitor
extends FileSystemMonitor
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( LocalFileMonitor.class );

	/**
	 * File to be monitored.
	 */
	protected File _file;

	/**
	 * Constructs a new file monitor.
	 *
	 * @param file  File to be monitored.
	 * @param delay Time between updates, in milliseconds.
	 */
	public LocalFileMonitor( @NotNull final File file, final long delay )
	{
		super( delay );
		_file = file;
	}

	@NotNull
	@Override
	public String getName()
	{
		final URI uri = _file.toURI();
		return uri.toString();
	}

	@Override
	public boolean isAvailable()
	{
		boolean result;

		try
		{
			result = _file.exists();
		}
		catch ( final SecurityException e )
		{
			LOG.trace( "isAvailable caused: " + e, e );
			result = false;
		}

		return result;
	}

	@Override
	protected List<Object> listFiles()
	throws IOException
	{
		return _file.exists() ? Collections.<Object>singletonList( _file ) : Collections.emptyList();
	}

	@Override
	protected Date lastModified( @NotNull final Object handle )
	{
		return new Date( ( (File)handle ).lastModified() );
	}

	@Override
	public boolean isDirectory( @NotNull final Object handle )
	{
		return ( (File)handle ).isDirectory();
	}

	@Override
	public String getPath( @NotNull final Object handle )
	{
		return ( (File)handle ).getPath();
	}

	@Override
	public InputStream readFile( @NotNull final Object handle )
	throws IOException
	{
		return new FileInputStream( (File)handle );
	}

	@Override
	public OutputStream writeFile( @NotNull final Object handle )
	throws IOException
	{
		return new FileOutputStream( (File)handle );
	}

	@Override
	public Object renameFile( @NotNull final Object handle, final String newName )
	throws IOException
	{
		final File file = (File)handle;
		final File newFile = new File( file.getParent(), newName );
		if ( !file.renameTo( newFile ) )
		{
			throw new IOException( "Failed to rename file: " + file );
		}
		return newFile;
	}

	@Override
	public void deleteFile( @NotNull final Object handle )
	throws IOException
	{
		final File file = (File)handle;
		if ( !file.delete() )
		{
			throw new IOException( "Failed to delete file: " + file );
		}
	}

	@Override
	void moveFile( @NotNull final Object handle, @NotNull final URI location )
	throws IOException
	{
		if ( "file".equals( location.getScheme() ) || ( location.getScheme() == null ) )
		{
			final File src = (File)handle;
			final File dst = new File( src, location.getPath() );
			final File dstFile = TextTools.endsWith( location.getPath(), '/' ) || dst.isDirectory() ? new File( dst, src.getName() ) : dst;
			final File parentFile = dstFile.getParentFile();
			if ( parentFile != null )
			{
				parentFile.mkdirs();
			}

			if ( !src.renameTo( dstFile ) )
			{
				FileTools.copy( src, dstFile );
				src.delete();
			}
		}
		else
		{
			super.moveFile( handle, location );
		}
	}

	@Override
	public boolean isResourceAvailable()
	{
		return true;
	}
}
