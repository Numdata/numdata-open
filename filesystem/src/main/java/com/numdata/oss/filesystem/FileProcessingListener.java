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
import java.util.*;

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
	private boolean _deleteAfterProcessing = false;

	/**
	 * Delete files older than this amount of minutes.
	 */
	private int _maximumAge = -1;

	/**
	 * URI to move file to after processing.
	 */
	@Nullable
	private URI _moveLocation = null;

	public boolean isDeleteAfterProcessing()
	{
		return _deleteAfterProcessing;
	}

	public void setDeleteAfterProcessing( final boolean deleteAfterProcessing )
	{
		_deleteAfterProcessing = deleteAfterProcessing;
	}

	public int getMaximumAge()
	{
		return _maximumAge;
	}

	public void setMaximumAge( final int maximumAge )
	{
		_maximumAge = maximumAge;
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
	public void fileSkipped( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle, @NotNull final SkipReason reason )
	{
		//noinspection EnumSwitchStatementWhichMissesCases
		switch ( reason )
		{
			case SINGLE_FILE:
			case INITIAL_FILE_HANDLING:
			case NOT_MODIFIED:
				applyMaximumAge( monitor, handle );
				break;
		}
	}

	@Override
	public void fileAdded( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	{
		fileModified( monitor, handle );
	}

	@Override
	public void fileModified( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	{
		boolean process = false;
		try
		{
			process = preProcess( monitor, handle );
		}
		catch ( final IOException e )
		{
			LOG.warn( "Failed to pre-process file: " + monitor.getPath( handle ) + ": " + e, e );
		}

		if ( process )
		{
			try
			{
				processFile( monitor, handle );
			}
			catch ( final IOException e )
			{
				LOG.warn( "Failed to process file: " + monitor.getPath( handle ) + ": " + e, e );
			}
			finally
			{
				try
				{
					postProcess( monitor, handle );
				}
				catch ( final IOException e )
				{
					LOG.warn( "Failed to post-process file: " + monitor.getPath( handle ) + ": " + e, e );
				}
			}
		}
	}

	@Override
	public void fileRemoved( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	{
	}

	/**
	 * This method applies the {@link #getMaximumAge() maximum age} setting to
	 * the given file.
	 *
	 * @param monitor File system monitor reporting the file.
	 * @param handle  Identifies the file to process.
	 *
	 * @return {@code true} if file was deleted due to age.
	 */
	@SuppressWarnings( "WeakerAccess" )
	protected boolean applyMaximumAge( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	{
		boolean result = false;
		final int maximumAge = getMaximumAge();
		if ( maximumAge > 0 )
		{
			try
			{
				final Date lastModified = monitor.lastModified( handle );
				if ( lastModified != null )
				{
					final long age = (int)( ( System.currentTimeMillis() - lastModified.getTime() ) / 60000L );
					if ( age > maximumAge )
					{
						result = true;
						if ( LOG.isDebugEnabled() )
						{
							LOG.debug( "Deleting old file: " + monitor.getPath( handle ) + " (age=" + age + ", maximumAge=" + maximumAge + ')' );
						}
						monitor.deleteFile( handle );
					}
				}
			}
			catch ( final Exception e )
			{
				if ( LOG.isDebugEnabled() )
				{
					LOG.debug( "Failed to apply 'maximumAge' rule to " + monitor.getPath( handle ) + ": " + e, e );
				}
			}
		}

		return result;
	}

	/**
	 * Called before file is processed.
	 *
	 * @param monitor File system monitor reporting the file.
	 * @param handle  Identifies the file to process.
	 *
	 * @return {@code true} if file should be processed.
	 *
	 * @throws IOException if there was a problem processing the file.
	 */
	@SuppressWarnings( { "RedundantThrows", "WeakerAccess" } )
	protected boolean preProcess( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	throws IOException
	{
		return !applyMaximumAge( monitor, handle );
	}

	/**
	 * Process file.
	 *
	 * @param monitor File system monitor reporting the file.
	 * @param handle  Identifies the file to process.
	 *
	 * @throws IOException if there was a problem processing the file.
	 */
	protected abstract void processFile( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	throws IOException;

	/**
	 * Called after file is processed.
	 *
	 * @param monitor File system monitor reporting the file.
	 * @param handle  Identifies the file to process.
	 *
	 * @throws IOException if there was a problem processing the file.
	 */
	protected void postProcess( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	throws IOException
	{
		if ( isDeleteAfterProcessing() )
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
