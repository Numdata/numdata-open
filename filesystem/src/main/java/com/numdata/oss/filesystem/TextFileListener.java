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
import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.io.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * {@link FileSystemMonitorListener} that can be used to process lines from
 * monitored text files.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "WeakerAccess", "unused" } )
public class TextFileListener
implements FileSystemMonitorListener
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( TextFileListener.class );

	/**
	 * File encoding. If empty, use system encoding.
	 */
	private String _fileEncoding = null;

	/**
	 * Try to read only new lines from processed files.
	 */
	private boolean _newLinesOnly = true;

	/**
	 * Path to last file processed by {@link #processFile}.
	 */
	private String _lastPath = null;

	/**
	 * Length of last file processed by {@link #processFile}.
	 */
	private long _lastLength = 0;

	/**
	 * Line processor.
	 */
	@Nullable
	private LineProcessor _lineProcessor = null;

	/**
	 * Create instance.
	 */
	public TextFileListener()
	{
	}

	/**
	 * Create instance.
	 *
	 * @param lineProcessor Line processor.
	 */
	public TextFileListener( @NotNull final LineProcessor lineProcessor )
	{
		_lineProcessor = lineProcessor;
	}

	public String getFileEncoding()
	{
		return _fileEncoding;
	}

	public void setFileEncoding( final String fileEncoding )
	{
		_fileEncoding = fileEncoding;
	}

	@Nullable
	public LineProcessor getLineProcessor()
	{
		return _lineProcessor;
	}

	public void setLineProcessor( @Nullable final LineProcessor lineProcessor )
	{
		_lineProcessor = lineProcessor;
	}

	public boolean isNewLinesOnly()
	{
		return _newLinesOnly;
	}

	public void setNewLinesOnly( final boolean newLinesOnly )
	{
		_newLinesOnly = newLinesOnly;
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
	}

	@Override
	public void fileModified( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	{
		try
		{
			processFile( monitor, handle );
		}
		catch ( final IOException e )
		{
			LOG.warn( "Failed to process new file: " + monitor.getPath( handle ) + ": " + e, e );
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
	protected void processFile( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	throws IOException
	{
		final LineProcessor lineProcessor = getLineProcessor();
		if ( lineProcessor == null )
		{
			LOG.info( "Not processing '" + monitor.getPath( handle ) + "' file, because no line processor is set" );
		}
		else
		{
			final List<String> lines = readLines( monitor, handle );
			if ( !lines.isEmpty() )
			{
				lineProcessor.processLines( monitor, handle, lines );
			}
		}
	}

	/**
	 * Read lines from the given file.
	 *
	 * If the {@link #isNewLinesOnly() new lines only} option is set and the
	 * same file is read, try to read only new lines from the file.
	 *
	 * @param monitor File system monitor reporting the file.
	 * @param handle  Identifies the file to process.
	 *
	 * @return Lines that were read.
	 *
	 * @throws IOException if there was a problem reading the file.
	 */
	@NotNull
	protected List<String> readLines( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle )
	throws IOException
	{
		final boolean debug = LOG.isDebugEnabled();
		final boolean trace = debug || LOG.isTraceEnabled();

		final String lastPath = _lastPath;
		final long lastLength = _lastLength;
		final String path = monitor.getPath( handle );
		final boolean newLinesOnly = isNewLinesOnly();
		final boolean sameFile = path.equals( lastPath );
		final boolean keepLines = sameFile || !newLinesOnly;

		if ( debug )
		{
			LOG.debug( "readLines() path=" + TextTools.quote( path ) +
			           ", newLinesOnly=" + newLinesOnly +
			           ", keepLines=" + keepLines +
			           ", sameFile=" + sameFile +
			           ( sameFile ? "" : ", lastPath=" + TextTools.quote( lastPath ) ) +
			           ( ( lastPath != null ) ? ", lastLength=" + lastLength : "" ) );
		}

		int lineCount = 0;
		final List<String> lines;

		final InputStream is = monitor.readFile( handle );
		try
		{
			// try to continue reading from last position
			final long readOffset;
			if ( sameFile && newLinesOnly && ( lastLength > 0 ) )
			{
				final long skipped = is.skip( lastLength );
				if ( trace )
				{
					LOG.trace( "Last length was " + lastLength + " => skipped over " + skipped + " byte(s)" );
				}

				readOffset = ( skipped > 0 ) ? skipped : 0;
			}
			else
			{
				readOffset = 0;
			}

			final CountingInputStream countingIS = new CountingInputStream( is );
			final String fileEncoding = getFileEncoding();
			final InputStreamReader reader = TextTools.isEmpty( fileEncoding ) ? new InputStreamReader( countingIS ) : new InputStreamReader( countingIS, fileEncoding );
			lines = keepLines ? new ArrayList<String>() : Collections.<String>emptyList();

			final StringBuilder lineBuffer = new StringBuilder();

			int i = ( (Reader)reader ).read();
			while ( i >= 0 )
			{
				if ( ( i == (int)'\r' ) || ( i == (int)'\n' ) )
				{
					lineCount++;
					if ( keepLines )
					{
						lines.add( lineBuffer.toString() );
					}

					lineBuffer.setLength( 0 );
					countingIS.mark( 0x10000 );

					final int n = ( (Reader)reader ).read();
					if ( ( i != n ) && ( ( n == (int)'\r' ) || ( n == (int)'\n' ) ) )
					{
						countingIS.mark( 0x10000 );
						i = ( (Reader)reader ).read();
					}
					else
					{
						i = n;
					}
				}
				else
				{
					lineBuffer.append( (char)i );
					i = ( (Reader)reader ).read();
				}
			}

			// handle unfinished text file at end of file
			if ( lineBuffer.length() > 0 )
			{
				if ( newLinesOnly )
				{
					// if expecting more input, reset to start of text line
					countingIS.reset();
				}
				else
				{
					// if we always read the entire file, keep the unfinished line as well
					lineCount++;
					lines.add( lineBuffer.toString() );
				}
			}

			_lastPath = path;
			_lastLength = readOffset + countingIS.getBytesRead();
		}
		finally
		{
			is.close();
		}

		if ( debug )
		{
			LOG.debug( "readLines() read " + lineCount + " line(s), returning " + lines.size() + " line(s)" );
		}

		return lines;
	}

	/**
	 * Processor for text lines.
	 */
	public interface LineProcessor
	{
		/**
		 * Process (new) lines from text file.
		 *
		 * @param monitor File system monitor reporting the file.
		 * @param handle  Identifies the file to process.
		 * @param lines   Text file lines.
		 */
		void processLines( @NotNull final FileSystemMonitor monitor, @NotNull final Object handle, @NotNull final List<String> lines );
	}
}
