/*
 * Copyright (c) 2018, Numdata BV, The Netherlands.
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
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * Provides notifications when files in a file system (or part of a file system)
 * are added, removed or modified.
 *
 * @author G. Meinders
 */
@SuppressWarnings( { "unused", "WeakerAccess" } )
public abstract class FileSystemMonitor
implements ResourceMonitor
{
	/**
	 * How to handle newly found files.
	 */
	public enum NewFileHandling
	{
		/**
		 * Keep all newly found files.
		 */
		KEEP_ALL,

		/**
		 * Keep only last of newly found files.
		 */
		KEEP_LAST,

		/**
		 * Do not keep any newly found files.
		 */
		KEEP_NONE
	}

	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FileSystemMonitor.class );

	/**
	 * Handling of files that are found during the initial run of the monitor.
	 */
	@NotNull
	private NewFileHandling _initialFileHandling = NewFileHandling.KEEP_ALL;

	/**
	 * Time between updates, in milliseconds. This applied only to
	 * implementations that need to use some kind of polling mechanism.
	 * Event-driven implementations may ignore this setting.
	 */
	private long _delay;

	/**
	 * Listeners registered with the monitor.
	 */
	@NotNull
	private final Collection<FileSystemMonitorListener> _listeners = new ArrayList<FileSystemMonitorListener>();

	/**
	 * Keeps track of the last modification time for each file within the scope
	 * of the monitor.
	 */
	@NotNull
	private final Map<Object, Date> _modificationTimeByFile = new LinkedHashMap<Object, Date>();

	/**
	 * Constructs a new file system monitor. While running, the monitor actively
	 * queries the underlying file system for changes. The delay between there
	 * updates is specified by {@code delay}.
	 *
	 * @param delay Time between updates, in milliseconds.
	 */
	protected FileSystemMonitor( final long delay )
	{
		_delay = delay;
	}

	/**
	 * Creates a file system monitor for monitoring the specified resource.
	 *
	 * @param uri URI identifying the resource to be monitored.
	 *
	 * @return Monitor for the specified resource.
	 *
	 * @throws IllegalArgumentException if the given URI is not supported.
	 */
	public static FileSystemMonitor createForURI( final URI uri )
	{
		final FileSystemMonitor result;
		final long delay = 250L;

		final String scheme = uri.getScheme();

		if ( "smb".equalsIgnoreCase( scheme ) )
		{
			try
			{
				result = new SMBFolderMonitor( uri.toString(), delay );
			}
			catch ( final MalformedURLException e )
			{
				throw new IllegalArgumentException( "Invalid URL: " + uri, e );
			}
		}
		else if ( "file".equalsIgnoreCase( scheme ) )
		{
			final File file = new File( uri );
			if ( file.isDirectory() )
			{
				result = new LocalFolderMonitor( file, delay );
			}
			else
			{
				result = new LocalFileMonitor( file, delay );
			}
		}
		else
		{
			throw new IllegalArgumentException( "Unsupported URI: " + uri );
		}

		return result;
	}

	@NotNull
	public NewFileHandling getInitialFileHandling()
	{
		return _initialFileHandling;
	}

	public void setInitialFileHandling( @NotNull final NewFileHandling handling )
	{
		_initialFileHandling = handling;
	}

	public long getDelay()
	{
		return _delay;
	}

	public void setDelay( final long delay )
	{
		_delay = delay;
	}

	@Override
	public void run()
	{
		String lastException = null;
		NewFileHandling newFileHandling = getInitialFileHandling();

		while ( !Thread.interrupted() )
		{
			try
			{
				checkForUpdates( newFileHandling );
				newFileHandling = NewFileHandling.KEEP_ALL;
			}
			catch ( final IOException e )
			{
				final StringWriter w = new StringWriter();
				e.printStackTrace( new PrintWriter( w, true ) );

				final String exceptionMessage = w.toString();
				String logMessage = exceptionMessage;
				if ( logMessage.contains( "No route to host" ) ||
				     logMessage.contains( "UnknownHostException" ) )
				{
					logMessage = "No route to host (wrong hostname/IP or host offline?)";
				}

				if ( TextTools.equals( logMessage, lastException ) )
				{
					LOG.error( "Again: " + logMessage );
				}
				else
				{
					LOG.error( exceptionMessage );
					lastException = logMessage;
				}
			}

			try
			{
				Thread.sleep( getDelay() );
			}
			catch ( final InterruptedException e )
			{
				break;
			}
		}

		LOG.info( "File system monitor '" + getName() + "' was interrupted" );
	}

	@Override
	public void stop()
	{
	}

	/**
	 * Checks for added, removed and modified files and notifies the monitor's
	 * listeners where needed.
	 *
	 * @param newFileHandling How to handle newly found files.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void checkForUpdates( @NotNull final NewFileHandling newFileHandling )
	throws IOException
	{
		LOG.trace( "checkForUpdates" );

		final Map<Object, Date> modificationTimeByFile = _modificationTimeByFile;
		final Map<Object, Date> newModificationTimeByFile = new HashMap<Object, Date>();

		/*
		 * Update modification times.
		 */
		final List<Object> files = new ArrayList<Object>( listFiles() );
		for ( final Object file : files )
		{
			newModificationTimeByFile.put( file, lastModified( file ) );
		}

		/*
		 * Sort files by modification time.
		 */
		Collections.sort( files, new Comparator<Object>()
		{
			@Override
			public int compare( final Object o1, final Object o2 )
			{
				final Date lastModified1 = newModificationTimeByFile.get( o1 );
				final Date lastModified2 = newModificationTimeByFile.get( o2 );
				return lastModified1.compareTo( lastModified2 );
			}
		} );

		/*
		 * Check for added, removed and modified files.
		 */
		for ( final Iterator<Object> iterator = files.iterator(); iterator.hasNext(); )
		{
			final Object file = iterator.next();
			final Date lastModified = modificationTimeByFile.get( file );
			final Date newLastModified = newModificationTimeByFile.get( file );

			if ( lastModified == null )
			{
				modificationTimeByFile.put( file, newLastModified );
				if ( ( newFileHandling == NewFileHandling.KEEP_ALL ) ||
				     ( newFileHandling == NewFileHandling.KEEP_LAST && !iterator.hasNext() ) )
				{
					fireFileAddedEvent( file );
				}
			}
			else if ( newLastModified == null )
			{
				modificationTimeByFile.remove( file );
				fireFileRemovedEvent( file );
			}
			else if ( !newLastModified.equals( lastModified ) )
			{
				modificationTimeByFile.put( file, newLastModified );
				fireFileModifiedEvent( file );
			}
		}
	}

	/**
	 * Lists all files being monitored.
	 *
	 * @return Files being monitored.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	protected abstract List<Object> listFiles()
	throws IOException;

	/**
	 * Returns the time at which the specified file was last modified.
	 *
	 * @param handle Identifies the file.
	 *
	 * @return Modification time.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	protected abstract Date lastModified( @NotNull Object handle )
	throws IOException;

	/**
	 * Returns whether the specified file is a directory.
	 *
	 * @param handle Identifies the file.
	 *
	 * @return {@code true} if the specified file is a directory.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract boolean isDirectory( @NotNull Object handle )
	throws IOException;

	/**
	 * Returns the path name for the specified file.
	 *
	 * @param handle Identifies the file.
	 *
	 * @return Path name.
	 */
	public abstract String getPath( @NotNull Object handle );

	/**
	 * Returns an input stream for reading from the specified file.
	 *
	 * @param handle Identifies the file to read from.
	 *
	 * @return Input stream for the specified file.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract InputStream readFile( @NotNull Object handle )
	throws IOException;

	/**
	 * Returns an output stream for writing to the specified file.
	 *
	 * @param handle Identifies the file to write to.
	 *
	 * @return Output stream for the specified file.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract OutputStream writeFile( @NotNull Object handle )
	throws IOException;

	/**
	 * Renames the specified file to the given name.
	 *
	 * @param handle  Identifies the file to be renamed.
	 * @param newName New name for the file.
	 *
	 * @return Identifies the renamed file.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract Object renameFile( @NotNull Object handle, String newName )
	throws IOException;

	/**
	 * Deletes the specified file.
	 *
	 * @param handle Identifies the file to be deleted.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public abstract void deleteFile( @NotNull Object handle )
	throws IOException;

	/**
	 * Returns whether the resource being monitored is available.
	 *
	 * @return {@code true} if the resource is available.
	 */
	public abstract boolean isResourceAvailable();

	/**
	 * Adds a file system monitor listener.
	 *
	 * @param listener Listener to be added.
	 */
	public void addListener( @NotNull final FileSystemMonitorListener listener )
	{
		_listeners.add( listener );
	}

	/**
	 * Removes a file system monitor listener.
	 *
	 * @param listener Listener to be removed.
	 */
	public void removeListener( @NotNull final FileSystemMonitorListener listener )
	{
		_listeners.remove( listener );
	}

	/**
	 * Notifies listeners that the specified file was added to the file system.
	 *
	 * @param file Identifies the file that was added.
	 */
	protected void fireFileAddedEvent( @NotNull final Object file )
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "fireFileAddedEvent( " + file + " )" );
		}

		for ( final FileSystemMonitorListener listener : _listeners )
		{
			try
			{
				listener.fileAdded( this, file );
			}
			catch ( final Exception e )
			{
				LOG.error( "Unhandled exception in 'fileAdded' method of " + listener, e );
			}
		}
	}

	/**
	 * Notifies listeners that the specified file was modified.
	 *
	 * @param file Identifies the file that was modified.
	 */
	protected void fireFileModifiedEvent( @NotNull final Object file )
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "fireFileModifiedEvent( " + file + " )" );
		}

		for ( final FileSystemMonitorListener listener : _listeners )
		{
			try
			{
				listener.fileModified( this, file );
			}
			catch ( final Exception e )
			{
				LOG.error( "Unhandled exception in 'fileModified' method of " + listener, e );
			}
		}
	}

	/**
	 * Notifies listeners that the specified file was removed from the file
	 * system.
	 *
	 * @param file Identifies the file that was removed.
	 */
	protected void fireFileRemovedEvent( @NotNull final Object file )
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "fireFileRemovedEvent( " + file + " )" );
		}

		for ( final FileSystemMonitorListener listener : _listeners )
		{
			try
			{
				listener.fileRemoved( this, file );
			}
			catch ( final Exception e )
			{
				LOG.error( "Unhandled exception in 'fileRemoved' method of " + listener, e );
			}
		}
	}
}
