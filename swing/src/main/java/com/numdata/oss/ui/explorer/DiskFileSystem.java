/*
 * (C) Copyright Numdata BV 2007-2017
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link VirtualFileSystem} for local disk file system.
 *
 * @author  Wijnand Wieskamp
 */
public class DiskFileSystem
	implements VirtualFileSystem
{

	/**
	 * Filter to apply to files.
	 */
	private FilenameFilter _filenameFilter;

	/**
	 * Construct disk file system.
	 *
	 * @throws  IOException when directory or file can't be read.
	 */
	public DiskFileSystem()
		throws IOException
	{
		this( null );
	}

	/**
	 * Construct disk   file system.
	 *
	 * @param   filenameFilter  {@link FilenameFilter} to select appropriate files.
	 *
	 * @throws  IOException when directory or file can't be read.
	 */
	public DiskFileSystem( final FilenameFilter filenameFilter )
		throws IOException
	{
		_filenameFilter = filenameFilter;
	}

	public VirtualFile getFile( final String path )
		throws IOException
	{
		if ( path == null )
			throw new NullPointerException( path );

		final File file = new File( path );
		if ( !file.exists() )
			throw new FileNotFoundException( path );

		return new DiskFile( file );
	}

	public List<VirtualFile> getFolders( final String folderPath )
		throws IOException
	{
		final File folder = getFolder( folderPath );
		final List<VirtualFile> result = new ArrayList<VirtualFile>();

		File file = folder;
		do
		{
			result.add( 0, new DiskFile( file ) );
			file = file.getParentFile();
		}
		while ( file != null );
		for ( final File subFolder : folder.listFiles() )
		{
			if ( subFolder.isDirectory() )
				result.add( new DiskFile( subFolder ) );
		}
		return result;
	}

	public List<VirtualFile> getFolderContents( final String folderPath )
		throws IOException
	{
		final File folder = getFolder( folderPath );

		final File[] files = folder.listFiles( _filenameFilter );
		if ( files == null )
			throw new FileNotFoundException( folderPath );

		final List<VirtualFile> result = new ArrayList<VirtualFile>( files.length );
		for ( final File file : files )
			result.add( new DiskFile( file ) );

		return result;
	}

	public String getPath( final String folderPath , final String filename )
	{
		final File file = new File( folderPath, filename );
		return file.getPath();
	}

	public VirtualFile createFolder( final String folderPath )
		throws IOException
	{
		try
		{
			final File file = new File ( folderPath );
			if ( !file.mkdir() )
				throw new IOException( "Failed to create folder" );

			return new DiskFile( file );
		}
		catch ( SecurityException e )
		{
			throw new IOException( e.getMessage() , e );
		}
	}

	/**
	 * Get actual folder based on the specified folder path.
	 *
	 * @param   folderPath      Absolute path to folder.
	 *
	 * @return  {@link File} instance of the folder.
	 *
	 * @throws  FileNotFoundException if the specified folder does not exist.
	 */
	private static File getFolder( final String folderPath )
		throws FileNotFoundException
	{
		final File result = new File( folderPath );
		if ( !result.isDirectory() )
			throw new FileNotFoundException( folderPath );
		return result;
	}
}
