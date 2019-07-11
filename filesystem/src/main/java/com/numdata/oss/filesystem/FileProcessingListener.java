/*
 * Copyright (c) 2018-2019, Numdata BV, The Netherlands.
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

import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * {@link FileSystemMonitorListener} that is used to process all
 * created/modified files and ignore deleted files. It has the option to delete
 * or move processed files.
 *
 * @author Peter S. Heijnen
 */
public abstract class FileProcessingListener
implements FileSystemMonitorListener
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FileProcessingListener.class );

	/**
	 * Whether to delete the file after processing.
	 */
	private boolean _delete = false;

	/**
	 * URI to move file to after processing.
	 */
	@Nullable
	private URI _moveLocation = null;

	public boolean isDelete()
	{
		return _delete;
	}

	public void setDelete( final boolean delete )
	{
		_delete = delete;
	}

	@Nullable
	public URI getMoveLocation()
	{
		return _moveLocation;
	}

	public void setMoveLocation( @Nullable final URI moveLocation )
	{
		_moveLocation = moveLocation;
	}

	@Override
	public void fileAdded( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	{
		try
		{
			processFile( monitor, handle );
		}
		catch ( final IOException e )
		{
			LOG.warn( "Failed to process new file: " + monitor.getPath( handle ) + ": " + e, e );
		}
		finally
		{
			try
			{
				postProcess( monitor, handle );
			}
			catch ( final IOException e )
			{
				LOG.warn( "Failed to post-process new file: " + monitor.getPath( handle ) + ": " + e, e );
			}
		}
	}

	@Override
	public void fileModified( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	{
		try
		{
			processFile( monitor, handle );
			postProcess( monitor, handle );
		}
		catch ( final IOException e )
		{
			LOG.warn( "Failed to process modified file: " + monitor.getPath( handle ) + ": " + e, e );
		}
		finally
		{
			try
			{
				postProcess( monitor, handle );
			}
			catch ( final IOException e )
			{
				LOG.warn( "Failed to post-process modified file: " + monitor.getPath( handle ) + ": " + e, e );
			}
		}
	}

	@Override
	public void fileRemoved( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	{
	}

	/**
	 * Process file.
	 *
	 * @param monitor File system monitor reporting the file.
	 * @param handle  Identifies the file to process.
	 *
	 * @throws IOException if there was a problem reading the file.
	 */
	protected abstract void processFile( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	throws IOException;

	/**
	 * Called after file is processed.
	 *
	 * @param monitor File system monitor reporting the file.
	 * @param handle  Identifies the file to process.
	 *
	 * @throws IOException if there was a problem reading the file.
	 */
	protected void postProcess( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	throws IOException
	{
		if ( isDelete() )
		{
			if ( LOG.isDebugEnabled() )
			{
				LOG.debug( "postProcess: delete " + handle );
			}

			monitor.deleteFile( handle );
		}
		else
		{
			final URI moveLocation = getMoveLocation();
			if ( moveLocation != null )
			{
				monitor.moveFile( handle, moveLocation );
			}
		}
	}

}
