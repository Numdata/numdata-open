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

import com.numdata.oss.log.*;
import jcifs.smb.*;
import org.jetbrains.annotations.*;

/**
 * Provides part information from files in a folder shared over the network
 * using SMB. Files are deleted after being processed.
 *
 * @author G. Meinders
 */
@SuppressWarnings( "OverlyStrongTypeCast" )
public class SMBFileSystemMonitor
extends FileSystemMonitor
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( SMBFileSystemMonitor.class );

	/**
	 * Folder to be monitored.
	 */
	private final SmbFile _smbFile;

	/**
	 * Constructs a new part information source that monitors the specified SMB
	 * folder.
	 *
	 * @param url   URL to the SMB folder to be monitored.
	 * @param delay Time between up-to-date checks.
	 *
	 * @throws MalformedURLException if the given URL is invalid.
	 */
	public SMBFileSystemMonitor( @NotNull final String url, final long delay )
	throws MalformedURLException
	{
		super( delay );
		setAlwaysModified( true );
		_smbFile = new SmbFile( url );
	}

	@NotNull
	@Override
	public String getName()
	{
		return _smbFile.getPath();
	}

	@Override
	public boolean isAvailable()
	{
		boolean result;

		try
		{
			result = _smbFile.exists();
		}
		catch ( final SmbException e )
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
		final List<Object> result;

		if ( _smbFile.isDirectory() )
		{
			final SmbFile[] files = _smbFile.listFiles();
			result = ( files == null ) ? Collections.emptyList() : Arrays.asList( (Object[])files );
		}
		else
		{
			result = Collections.<Object>singletonList( _smbFile );
		}

		return result;
	}

	@Override
	protected Date lastModified( @NotNull final Object handle )
	{
		return new Date( ( (SmbFile)handle ).getLastModified() );
	}

	@Override
	public boolean isDirectory( @NotNull final Object handle )
	throws IOException
	{
		return ( (SmbFile)handle ).isDirectory();
	}

	@Override
	public String getPath( @NotNull final Object handle )
	{
		return ( (SmbFile)handle ).getPath();
	}

	@Override
	public InputStream readFile( @NotNull final Object handle )
	throws IOException
	{
		return ( (SmbFile)handle ).getInputStream();
	}

	@Override
	public OutputStream writeFile( @NotNull final Object handle )
	throws IOException
	{
		return ( (SmbFile)handle ).getOutputStream();
	}

	@Override
	public Object renameFile( @NotNull final Object handle, final String newName )
	throws IOException
	{
		final SmbFile file = (SmbFile)handle;
		final SmbFile newFile = new SmbFile( file.getParent(), newName );
		file.renameTo( newFile );
		return newFile;
	}

	@Override
	void moveFile( @NotNull final Object handle, @NotNull final URI location )
	throws IOException
	{
		final SmbFile file = (SmbFile)handle;
		final String scheme = location.getScheme();
		if ( "smb".equals( scheme ) || ( scheme == null ) )
		{
			final SmbFile newFile = new SmbFile( file.getParent(), location.toString() );
			file.renameTo( newFile );
		}
		else
		{
			super.moveFile( handle, location );
		}
	}

	@Override
	public void deleteFile( @NotNull final Object handle )
	throws IOException
	{
		final SmbFile file = (SmbFile)handle;
		file.delete();
	}

	@Override
	public boolean isResourceAvailable()
	{
		boolean result = false;
		try
		{
			result = _smbFile.exists();
		}
		catch ( final SmbException e )
		{
			// Ignore.
		}
		return result;
	}
}
