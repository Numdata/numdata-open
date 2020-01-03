/*
 * Copyright (c) 2009-2019, Numdata BV, The Netherlands.
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
import java.util.regex.*;

import com.numdata.oss.*;
import com.numdata.oss.io.*;
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
		 * Keep only if single file was found.
		 */
		KEEP_SINGLE,

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
	 * Path filter. This filter is applied to the {@link #getPath(Object) file
	 * path}.
	 */
	@Nullable
	private Pattern _pathFilter = null;

	/**
	 * Whether to monitor only a single file.
	 */
	private boolean _singleFile = false;

	/**
	 * Always assume that a file is modified, even if the modification time and
	 * file size have not changed. This may be useful if the state information
	 * reported by the file system is cache and may therefore be out-of-date,
	 * which cause issues if changes need to be picked up immediately.
	 */
	private boolean _alwaysModified = false;

	/**
	 * Listeners registered with the monitor.
	 */
	@NotNull
	private final Collection<FileSystemMonitorListener> _listeners = new ArrayList<FileSystemMonitorListener>();

	/**
	 * Map with last known modification time for each file within the scope of
	 * the monitor.
	 */
	@NotNull
	private Map<Object, Date> _modificationTimeByFile = Collections.emptyMap();

	/**
	 * Last exception that occurred.
	 */
	@Nullable
	private Exception _lastException = null;

	/**
	 * Last time that the connection was established.
	 */
	private long _lastUpdated = -1;

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
				result = new SMBFileSystemMonitor( uri.toString(), delay );
			}
			catch ( final MalformedURLException e )
			{
				throw new IllegalArgumentException( "Invalid URL: " + uri, e );
			}
		}
		else if ( "file".equalsIgnoreCase( scheme ) )
		{
			final File file = new File( uri );
			if ( TextTools.endsWith( uri.getPath(), '/' ) || file.isDirectory() )
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

	@Nullable
	public Pattern getPathFilter()
	{
		return _pathFilter;
	}

	public void setPathFilter( @Nullable final Pattern pathFilter )
	{
		_pathFilter = pathFilter;
	}

	/**
	 * Set path filter using a wildcard pattern.
	 *
	 * @param wildcardFilter Wildcard pattern.
	 * @param caseSensitive  Match case-sensitive vs case-insensitive.
	 *
	 * @see TextTools#wildcardPatternToRegex(String)
	 */
	public void setPathFilter( @Nullable final String wildcardFilter, final boolean caseSensitive )
	{
		Pattern pattern = null;
		if ( !TextTools.isEmpty( wildcardFilter ) )
		{
			String regex = TextTools.wildcardPatternToRegex( wildcardFilter );
			if ( ( wildcardFilter.indexOf( '/' ) < 0 ) && ( wildcardFilter.indexOf( '\\' ) < 0 ) )
			{
				regex = "(?:.*[/\\\\])?" + regex;
			}

			pattern = Pattern.compile( regex, caseSensitive ? 0 : Pattern.CASE_INSENSITIVE );
		}
		setPathFilter( pattern );
	}

	public void setSingleFile( final boolean singleFile )
	{
		_singleFile = singleFile;
	}

	public boolean isSingleFile()
	{
		return _singleFile;
	}

	public boolean isAlwaysModified()
	{
		return _alwaysModified;
	}

	public void setAlwaysModified( final boolean alwaysModified )
	{
		_alwaysModified = alwaysModified;
	}

	@Nullable
	public Exception getLastException()
	{
		return _lastException;
	}

	public long getLastUpdated()
	{
		return _lastUpdated;
	}

	@NotNull
	@Override
	public ResourceStatus getStatus()
	{
		final ResourceStatus result = new ResourceStatus();
		result.setLastOnline( getLastUpdated() );
		try
		{
			listFiles();

			final Exception exception = getLastException();
			if ( exception == null )
			{
				result.setStatus( ResourceStatus.Status.AVAILABLE );
				result.setDetails( "Running (initialFileHandling=" + getInitialFileHandling() + ", delay=" + getDelay() + ", pathFilter=" + TextTools.quote( getPathFilter() ) + ", singleFile=" + isSingleFile() + ", alwaysModified=" + isAlwaysModified() );
			}
			else
			{
				result.setStatus( ResourceStatus.Status.UNAVAILABLE );
				result.setDetails( "File system is available, but update failed" );
				result.setException( exception );
			}
		}
		catch ( final Exception e )
		{
			_lastException = e;
			result.setStatus( ResourceStatus.Status.UNREACHABLE );
			result.setDetails( "Failed to access file system" );
			result.setException( e );
		}

		return result;
	}

	@Override
	public void run()
	{
		LOG.debug( "run()" );

		_lastException = null;
		String lastExceptionMessage = null;
		NewFileHandling newFileHandling = getInitialFileHandling();

		while ( !Thread.interrupted() )
		{
			try
			{
				checkForUpdates( newFileHandling );
				newFileHandling = NewFileHandling.KEEP_ALL;
				_lastException = null;
				_lastUpdated = System.currentTimeMillis();
			}
			catch ( final Exception e )
			{
				_lastException = e;
				final StringWriter w = new StringWriter();
				e.printStackTrace( new PrintWriter( w, true ) );

				final String exceptionMessage = w.toString();
				String logMessage = exceptionMessage;
				if ( logMessage.contains( "No route to host" ) ||
				     logMessage.contains( "UnknownHostException" ) )
				{
					logMessage = "No route to host (wrong hostname/IP or host offline?)";
				}

				if ( TextTools.equals( logMessage, lastExceptionMessage ) )
				{
					LOG.warn( "Again: " + e );
				}
				else
				{
					LOG.warn( "Failed to check for updates: " + e, e );
					lastExceptionMessage = logMessage;
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
		final boolean trace = LOG.isTraceEnabled();
		LOG.trace( "checkForUpdates" );

		/*
		 * Update modification times.
		 */
		final AugmentedList<Object> files = new AugmentedArrayList<Object>( listFiles() );
		if ( files.isEmpty() )
		{
			if ( trace )
			{
				LOG.trace( "checkForUpdates: found no files" );
			}
		}
		else
		{
			final Pattern pathFilter = getPathFilter();
			if ( pathFilter != null )
			{
				if ( trace )
				{
					LOG.trace( "checkForUpdates: apply path filter " + pathFilter );
				}
				for ( final Iterator<Object> it = files.iterator(); it.hasNext(); )
				{
					final Object file = it.next();
					if ( !pathFilter.matcher( getPath( file ) ).matches() )
					{
						fireFileSkippedEvent( file, FileSystemMonitorListener.SkipReason.PATH_FILTER );
						it.remove();
					}
				}
			}

			if ( trace )
			{
				LOG.trace( "checkForUpdates: have " + files.size() + " file(s)" );
			}

			final Map<Object, Date> newModificationTimeByFile = new HashMap<Object, Date>();
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

			if ( isSingleFile() && ( files.size() > 1 ) )
			{
				final Object file = files.getLast();
				for ( int i = 0; i < files.size() - 1; i++ )
				{
					fireFileSkippedEvent( files.get( i ), FileSystemMonitorListener.SkipReason.SINGLE_FILE );
				}
				files.removeRange( 0, files.size() - 1 );

				final Date lastModified = newModificationTimeByFile.get( file );
				newModificationTimeByFile.clear();
				newModificationTimeByFile.put( file, lastModified );
			}

			final Map<Object, Date> oldModificationTimeByFile = _modificationTimeByFile;

			/*
			 * Check for added and modified files.
			 */
			for ( final Iterator<Object> iterator = files.iterator(); iterator.hasNext(); )
			{
				final Object file = iterator.next();
				final Date oldLastModified = oldModificationTimeByFile.get( file );
				final Date newLastModified = newModificationTimeByFile.get( file );
				if ( oldLastModified == null )
				{
					final boolean keep = ( newFileHandling == NewFileHandling.KEEP_ALL ) ||
					                     ( newFileHandling == NewFileHandling.KEEP_LAST && !iterator.hasNext() ) ||
					                     ( newFileHandling == NewFileHandling.KEEP_SINGLE && ( files.size() == 1 ) );
					if ( trace )
					{
						LOG.trace( "new file: " + getPath( file ) + ", newFileHandling=" + newFileHandling + " => keep=" + keep );
					}
					if ( keep )
					{
						fireFileAddedEvent( file );
					}
					else
					{
						fireFileSkippedEvent( file, FileSystemMonitorListener.SkipReason.INITIAL_FILE_HANDLING );
					}
				}
				else if ( isAlwaysModified() || !newLastModified.equals( oldLastModified ) )
				{
					if ( trace )
					{
						LOG.trace( "modified file: " + getPath( file ) );
					}
					fireFileModifiedEvent( file );
				}
				else
				{
					fireFileSkippedEvent( file, FileSystemMonitorListener.SkipReason.NOT_MODIFIED );
				}
			}

			/*
			 * Check for removed files.
			 */
			for ( final Iterator<Object> it = oldModificationTimeByFile.keySet().iterator(); it.hasNext(); )
			{
				final Object file = it.next();
				if ( !newModificationTimeByFile.containsKey( file ) )
				{
					if ( trace )
					{
						LOG.trace( "removed file: " + getPath( file ) );
					}
					it.remove();
					fireFileRemovedEvent( file );
				}
			}

			_modificationTimeByFile = newModificationTimeByFile;

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
	 * Moves the specified file to the given location.
	 *
	 * @param handle   Identifies the file to be deleted.
	 * @param location Target location.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	void moveFile( @NotNull final Object handle, @NotNull final URI location )
	throws IOException
	{
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "moveFile( " + handle + ", " + location );
		}

		InputStream in = null;
		OutputStream out = null;
		try
		{
			final String path = getPath( handle );
			final String srcName = path.substring( Math.max( path.lastIndexOf( '/' ), path.lastIndexOf( '\\' ) ) + 1 );

			in = readFile( handle );

			if ( "file".equals( location.getScheme() ) )
			{
				final File dst = new File( location );
				final boolean directory = TextTools.endsWith( location.getPath(), '/' ) || dst.isDirectory();
				final File dstFile = directory ? new File( dst, srcName ) : dst;

				final File parentFile = dstFile.getParentFile();
				if ( parentFile != null )
				{
					parentFile.mkdirs();
				}

				out = new FileOutputStream( dstFile );
			}
			else
			{
				final boolean directory = TextTools.endsWith( location.getPath(), '/' );
				final URL dstUrl = ( directory ? location.resolve( srcName ) : location ).toURL();

				out = dstUrl.openConnection().getOutputStream();
			}

			DataStreamTools.pipe( out, in );
		}
		finally
		{
			if ( in != null )
			{
				in.close();
			}
			if ( out != null )
			{
				out.close();
			}
		}

		deleteFile( handle );
	}

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
	 * Notifies listeners that the specified file was skipped.
	 *
	 * @param file   Identifies the file that was skipped.
	 * @param reason Reason why file was skipped.
	 */
	protected void fireFileSkippedEvent( @NotNull final Object file, @NotNull final FileSystemMonitorListener.SkipReason reason )
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "fireFileSkippedEvent( " + getPath( file ) + ", " + reason + " )" );
		}

		for ( final FileSystemMonitorListener listener : _listeners )
		{
			try
			{
				listener.fileSkipped( this, file, reason );
			}
			catch ( final Exception e )
			{
				LOG.error( "Unhandled exception in 'fileSkipped' method of " + listener, e );
			}
		}
	}

	/**
	 * Notifies listeners that the specified file was added to the file system.
	 *
	 * @param file Identifies the file that was added.
	 */
	protected void fireFileAddedEvent( @NotNull final Object file )
	{
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "fireFileAddedEvent( " + getPath( file ) + " )" );
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
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "fireFileModifiedEvent( " + getPath( file ) + " )" );
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
		if ( LOG.isDebugEnabled() )
		{
			LOG.debug( "fireFileRemovedEvent( " + getPath( file ) + " )" );
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

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + '@' + Integer.toHexString( System.identityHashCode( this ) ) +
		       "{name=" + getName() +
		       ", initialFileHandling=" + getInitialFileHandling() +
		       ", delay=" + getDelay() +
		       ", pathFilter=" + TextTools.quote( getPathFilter() ) +
		       ", singleFile=" + isSingleFile() +
		       ", alwaysModified=" + isAlwaysModified() +
		       '}';
	}
}
