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
package com.numdata.oss.io;

import java.io.*;
import java.nio.channels.*;

import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * Provides utility methods for working with files and the file system.
 *
 * @author G. Meinders
 */
public class FileTools
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FileTools.class );

	/**
	 * Deletes the given file and, if the file denotes a directory, all files it
	 * contains, recursively.
	 *
	 * @param file File to be deleted.
	 *
	 * @return {@code true} if the file was deleted; otherwise {@code false}, in
	 *         which case, some files may have been deleted.
	 */
	public static boolean deleteRecursively( @NotNull final File file )
	{
		final boolean result;
		if ( file.isDirectory() )
		{
			boolean recursiveResult = true;
			final File[] files = file.listFiles();
			if ( files != null )
			{
				for ( final File child : files )
				{
					recursiveResult &= deleteRecursively( child );
				}
			}
			result = file.delete() && recursiveResult;
		}
		else
		{
			result = file.delete();
		}
		return result;
	}

	/**
	 * Visits the files matching the given filter, starting at the given file or
	 * directory, recursively visiting any underlying files and directories.
	 * Directories and all underlying files and directories are always scanned, but
	 * only visited when they match the given file filter. As such, the file filter
	 * needn't include directories for the recursion to work.
	 *
	 * @param file    File to start with.
	 * @param filter  File filter to be used.
	 * @param visitor Visitor that accepts matching files.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void visitRecursively( @NotNull final File file, @Nullable final FileFilter filter, @NotNull final FileVisitor visitor )
	throws IOException
	{
		if ( ( filter == null ) || filter.accept( file ) )
		{
			visitor.visit( file );
		}

		if ( file.isDirectory() )
		{
			final File[] files = ( filter == null ) ? file.listFiles() : file.listFiles( new IncludeDirectoriesFileFilter( filter ) );

			if ( files != null )
			{
				for ( final File child : files )
				{
					visitRecursively( child, filter, visitor );
				}
			}
		}
	}

	/**
	 * Copies the content of the specified source file to the destination file.
	 *
	 * @param source      Source file.
	 * @param destination Destination file.
	 *
	 * @throws IllegalArgumentException if {@code source} and {@code destination}
	 * are the same file.
	 * @throws IOException if an I/O error occurs.
	 */
	public static void copy( @NotNull final File source, @NotNull final File destination )
	throws IOException
	{
		if ( LOG.isTraceEnabled() )
		{
			LOG.trace( "Copying '" + source + "' to '" + destination + '\'' );
		}

		if ( isSameFile( source, destination ) )
		{
			throw new IllegalArgumentException( "source and destination are the same file" );
		}

		FileInputStream in = null;
		FileOutputStream out = null;

		try
		{
			in = new FileInputStream( source );
			out = new FileOutputStream( destination );

			final FileChannel inChannel = in.getChannel();
			final FileChannel outChannel = out.getChannel();

			final long size = inChannel.size();
			for ( long bytesTransfered = 0L; bytesTransfered < size; )
			{
				//noinspection AssignmentToForLoopParameter
				bytesTransfered += inChannel.transferTo( bytesTransfered, size - bytesTransfered, outChannel );
			}
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
	}

	/**
	 * Returns whether two abstract path names represent the same file, based on
	 * canonical equivalence.
	 *
	 * @param first  First file.
	 * @param second Second file.
	 *
	 * @return {@code true} if the files are the same.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static boolean isSameFile( @NotNull final File first, @NotNull final File second )
	throws IOException
	{
		boolean result = false;

		if ( first.equals( second ) )
		{
			final String firstCanonical = first.getCanonicalPath();
			final String secondCanonical = second.getCanonicalPath();

			result = firstCanonical.equals( secondCanonical );
		}

		return result;
	}

	/**
	 * Returns a relative path to {@code file} from {@code context}. If no relative
	 * path is found, the {@code file} is returned as-is.
	 *
	 * @param context Context file. This is assumed to be a directory.
	 * @param file    File to be relativized.
	 *
	 * @return Relative path to {@code file}.
	 */
	public static File relativize( final File context, final File file )
	{
		File result = file;

		if ( file.isAbsolute() )
		{
			final String path = file.getPath();

			File ancestor = context;

			int depth = 0;
			do
			{
				final String ancestorPath = ancestor.getPath();
				if ( path.startsWith( ancestorPath ) )
				{
					final boolean trailingSeparator = ancestorPath.charAt( ancestorPath.length() - 1 ) == File.separatorChar;

					if ( trailingSeparator || // not common, but the root directory is an example
					     ( path.length() == ancestorPath.length() ) || // exact match
					     ( path.charAt( ancestorPath.length() ) == File.separatorChar ) ) // avoid partial file name match
					{
						final StringBuilder builder = new StringBuilder();

						// Remove uncommon parts of the path.
						while ( depth-- > 0 )
						{
							builder.append( ".." );
							builder.append( File.separatorChar );
						}

						final int start = trailingSeparator ? ancestorPath.length() : ( ancestorPath.length() + 1 );
						if ( start < path.length() )
						{
							builder.append( path.substring( start ) );
						}

						result = new File( builder.toString() );

						break;
					}
				}
				ancestor = ancestor.getParentFile();

				depth++;
			}
			while ( ancestor != null );
		}
		else
		{
			final String contextPath = context.getPath();
			final String filePath = file.getPath();

			if ( ( filePath.length() > contextPath.length() + 1 ) && filePath.startsWith( contextPath ) )
			{
				final boolean trailingSeparator = ( contextPath.charAt( contextPath.length() - 1 ) == File.separatorChar );
				if ( trailingSeparator || ( filePath.charAt( contextPath.length() ) == File.separatorChar ) )
				{
					result = new File( filePath.substring( contextPath.length() + ( trailingSeparator ? 0 : 1 ) ) );
				}
			}
		}

		return result;
	}

	/**
	 * This class MUST NOT be instantiated.
	 */
	private FileTools()
	{
	}

	/**
	 * File filter that includes directories in addition to any files accepted by
	 * the underlying file filter.
	 */
	private static class IncludeDirectoriesFileFilter
	implements FileFilter
	{
		/**
		 * File filter to be applied.
		 */
		@NotNull
		private final FileFilter _fileFilter;

		/**
		 * Constructs a new file filter.
		 *
		 * @param fileFilter Underlying file filter.
		 */
		private IncludeDirectoriesFileFilter( @NotNull final FileFilter fileFilter )
		{
			_fileFilter = fileFilter;
		}

		public boolean accept( final File pathname )
		{
			return pathname.isDirectory() || _fileFilter.accept( pathname );
		}
	}
}
