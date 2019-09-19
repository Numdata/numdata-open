/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.ui.explorer;

import java.awt.image.*;
import java.io.*;
import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Implements a virtual file system that is stored entirely in memory.
 *
 * @author  G. Meinders
 */
public class MemoryFileSystem
	implements VirtualFileSystem
{
	/**
	 * Root folder.
	 */
	private Folder _root;

	/**
	 * Constructs an empty in-memory virtual file system.
	 */
	public MemoryFileSystem()
	{
		_root = new Folder( null , "" );
	}

	@Override
	public VirtualFile getFile( final String path )
		throws IOException
	{
		if ( path == null )
		{
			throw new NullPointerException();
		}

		if ( path.isEmpty() || ( path.charAt( 0 ) != '/' ) )
		{
			throw new IllegalArgumentException( "Path must be absolute: " + path );
		}

		final File result;

		final String[] pathComponents = path.split( "/" );
		if ( pathComponents.length == 0 )
		{
			result = _root;
		}
		else
		{
			Folder currentFolder = _root;

			for ( int i = 1 ; i < pathComponents.length - 1 ; i++ )
			{
				final String name = pathComponents[ i ];
				if ( name.isEmpty() )
				{
					throw new IllegalArgumentException( "Invalid path: " + path );
				}

				final File file = currentFolder.getFile( name );

				if ( file instanceof Folder )
				{
					currentFolder = (Folder)file;
				}
				else
				{
					throw new FileNotFoundException( path );
				}
			}

			final String fileName = pathComponents[ pathComponents.length - 1 ];

			if ( fileName.isEmpty() )
			{
				throw new IllegalArgumentException( "Invalid path: " + path );
			}

			result = currentFolder.getFile( fileName );
		}

		if ( result == null )
		{
			throw new FileNotFoundException( path );
		}

		return result;
	}

	@Override
	public VirtualFile createFolder( final String path )
		throws IOException
	{
		if ( path == null )
		{
			throw new NullPointerException();
		}

		if ( path.isEmpty() || ( path.charAt( 0 ) != '/' ) )
		{
			throw new IllegalArgumentException( "Path must be absolute: " + path );
		}

		final String      parentPath = getParentPath( path );
		final VirtualFile parentFile = parentPath.isEmpty() ? _root : getFile( parentPath );

		if ( parentFile instanceof Folder )
		{
			final Folder parentFolder = (Folder)parentFile;
			final String fileName     = getFileName( path );

			if ( fileName.isEmpty() )
			{
				throw new IllegalArgumentException( "Invalid path: " + path );
			}

			final Folder result = new Folder( parentFolder , fileName );

			if ( !parentFolder.add( result ) )
			{
				throw new IOException( "File already exists: " + path );
			}

			return result;
		}
		else if ( parentFile == null )
		{
			throw new FileNotFoundException( parentPath );
		}
		else
		{
			throw new IOException( "Not a folder: " + parentPath );
		}
	}

	/**
	 * Creates a file at the specified location.
	 *
	 * @param   path    Path of the file to be created.
	 *
	 * @return  Created file.
	 *
	 * @throws  IOException if the file could not be created.
	 */
	public VirtualFile createFile( final String path )
		throws IOException
	{
		if ( path == null )
		{
			throw new NullPointerException();
		}

		if ( path.isEmpty() || ( path.charAt( 0 ) != '/' ) )
		{
			throw new IllegalArgumentException( "Path must be absolute: " + path );
		}

		final String      parentPath = getParentPath( path );
		final VirtualFile parentFile = parentPath.isEmpty() ? _root : getFile( parentPath );

		if ( parentFile instanceof Folder )
		{
			final Folder parentFolder = (Folder)parentFile;
			final String fileName     = getFileName( path );

			if ( fileName.isEmpty() )
			{
				throw new IllegalArgumentException( "Invalid path: " + path );
			}

			final File result = new ContentFile( parentFolder , fileName );

			if ( !parentFolder.add( result ) )
			{
				throw new IOException( "File already exists: " + path );
			}

			return result;
		}
		else if ( parentFile == null )
		{
			throw new FileNotFoundException( parentPath );
		}
		else
		{
			throw new IOException( "Not a folder: " + parentPath );
		}
	}

	/**
	 * Returns the given path excluding the final path component.
	 *
	 * @param   path    Path to get the parent path for.
	 *
	 * @return  Parent path of the given path.
	 */
	private static String getParentPath( final String path )
	{
		final int lastSeparator = ( path.length() < 2 ) ? 0 : path.lastIndexOf( (int)'/' , path.length() - 2 );
		return path.substring( 0 , lastSeparator );
	}

	/**
	 * Returns the file name from the given path, i.e. the last path component.
	 *
	 * @param   path    Path to get the file name from.
	 *
	 * @return  File name from the given path.
	 */
	private static String getFileName( final String path )
	{
		final int lastSeparator = path.lastIndexOf( (int)'/' );
		return path.substring( lastSeparator + 1 );
	}

	@Override
	public List<VirtualFile> getFolderContents( final String path )
		throws IOException
	{
		final VirtualFile file = path.isEmpty() ? _root : getFile( path );

		if ( file instanceof Folder )
		{
			final Folder folder = (Folder)file;
			return folder.getContents();
		}
		else
		{
			throw new IOException( "Not a folder: " + path );
		}
	}

	@Override
	public List<VirtualFile> getFolders( final String path )
		throws IOException
	{
		final VirtualFile file = getFile( path );
		if ( file instanceof Folder )
		{
			final Folder folder = (Folder)file;
			return folder.getFolders();
		}
		else
		{
			throw new IOException( "Not a folder: " + path );
		}
	}

	@Override
	public String getPath( final String parent , final String name )
	{
		return TextTools.endsWith( parent , '/' ) ? parent + name : parent + '/' + name;
	}

	/**
	 * Returns the content of the specified file.
	 *
	 * @param   path    Path of the file.
	 *
	 * @return  Content of the file.
	 *
	 * @throws  IOException if the given path doesn't denote a file.
	 */
	public Content getFileContent( final String path )
		throws IOException
	{
		final VirtualFile file = getFile( path );
		if ( file instanceof ContentFile )
		{
			return ( (ContentFile)file ).getContent();
		}
		else
		{
			throw new IOException( "Not a file: " + path );
		}
	}

	/**
	 * Sets the content of the specified file. The current content of the file
	 * is discarded.
	 *
	 * @param   path        Path of the file.
	 * @param   content     Content to be set.
	 *
	 * @throws  IOException if the given path doesn't denote a file.
	 */
	public void setFileContent( final String path , final Content content )
		throws IOException
	{
		final VirtualFile file = getFile( path );
		if ( file instanceof ContentFile )
		{
			final ContentFile contentFile = (ContentFile)file;
			contentFile.setContent( content );
		}
		else
		{
			throw new IOException( "Not a file: " + path );
		}
	}

	/**
	 * Deletes all files and folders that the specified folder contains.
	 *
	 * @param   path    Path to a folder.
	 *
	 * @throws  IOException if the given path doesn't denote a folder.
	 */
	public void deleteFolderContents( final String path )
		throws IOException
	{
		final VirtualFile file = getFile( path );

		if ( file instanceof Folder )
		{
			final Folder folder = (Folder)file;
			folder.clear();
		}
		else
		{
			throw new IOException( "Not a folder: " + path );
		}
	}

	/**
	 * Represents the content of a file in an in-memory virtual file system.
	 */
	public interface Content
	{
		/**
		 * Returns the value stored in this object.
		 *
		 * @return  Content value.
		 */
		Object getValue();

		/**
		 * Returns the content type.
		 *
		 * @return  Type of content.
		 */
		String getType();

		/**
		 * Returns the display name of the file for the given locale.
		 *
		 * @param   locale  Locale to be used.
		 *
		 * @return  Display name of the file.
		 */
		@Nullable
		String getDisplayName( @NotNull Locale locale );

		/**
		 * Returns a description of the content for the given locale.
		 *
		 * @param   locale  Locale to be used.
		 *
		 * @return  Description of the content.
		 */
		@Nullable
		String getDescription( @NotNull Locale locale );

		/**
		 * Returns the thumbnail for this content, if any. If thumbnails can
		 * be provided in multiple sizes, the size that best matches the
		 * specified preferred width and height is returned.
		 *
		 * @param   preferredWidth      Preferred width of the thumbnail.
		 * @param   preferredHeight     Preferred height of the thumbnail.
		 *
		 * @return  Thumbnail image.
		 */
		@Nullable
		ImageSource getThumbnail( int preferredWidth , int preferredHeight );
	}

	/**
	 * Represents file content, stored in-memory.
	 */
	public static class MemoryContent
		implements Content
	{
		/**
		 * Value stored in this object.
		 */
		private Object _value;

		/**
		 * Type of content.
		 */
		private String _type;

		/**
		 * Description of the content.
		 */
		private LocalizedString _displayName;

		/**
		 * Description of the content.
		 */
		private LocalizedString _description;

		/**
		 * Thumbnails for the content. Various sizes may be added.
		 */
		private List<BufferedImage> _thumbnails;

		/**
		 * Constructs an empty content object.
		 */
		public MemoryContent()
		{
			_value       = null;
			_type        = null;
			_displayName = new LocalizedString();
			_description = new LocalizedString();
			_thumbnails  = new ArrayList<BufferedImage>();
		}

		@Override
		public Object getValue()
		{
			return _value;
		}

		/**
		 * Sets the value stored in this object and the associated type.
		 *
		 * @param   value   Value to be set.
		 * @param   type    Content type.
		 */
		public void setValue( final Object value , final String type )
		{
			_value = value;
			_type  = type;
		}

		@Override
		public String getType()
		{
			return _type;
		}

		@Override
		public String getDisplayName( @NotNull final Locale locale )
		{
			return _displayName.get( locale );
		}

		/**
		 * Sets the display name of the content.
		 *
		 * @param   displayName     Display name to be set.
		 */
		public void setDisplayName( @NotNull final LocalizedString displayName )
		{
			_displayName.set( displayName );
		}

		@Override
		public String getDescription( @NotNull final Locale locale )
		{
			return _description.get( locale );
		}

		/**
		 * Sets the description of the content.
		 *
		 * @param   description     Description to be set.
		 */
		public void setDescription( @NotNull final LocalizedString description )
		{
			_description.set( description );
		}

		@Nullable
		@Override
		public ImageSource getThumbnail( final int preferredWidth , final int preferredHeight )
		{
			BufferedImage nearest       = null;
			int           nearestOffset = Integer.MAX_VALUE;

			for ( final BufferedImage thumbnail : _thumbnails )
			{
				final int offset = Math.abs( preferredWidth  - thumbnail.getWidth()  ) +
				                   Math.abs( preferredHeight - thumbnail.getHeight() );

				if ( offset < nearestOffset )
				{
					nearest       = thumbnail;
					nearestOffset = offset;
				}
			}

			return ( nearest == null ) ? null : new MemoryImageSource( nearest );
		}

		/**
		 * Adds a thumbnail image.
		 *
		 * @param   thumbnail   Thumbnail to be added.
		 */
		public void addThumbnail( final BufferedImage thumbnail )
		{
			if ( thumbnail == null )
			{
				throw new NullPointerException( "thumbnail" );
			}

			_thumbnails.add( thumbnail );
		}
	}

	/**
	 * Represents a file in the file system.
	 */
	private abstract static class File
		implements VirtualFile
	{
		/**
		 * Parent folder.
		 */
		private final Folder _parent;

		/**
		 * Name of the file.
		 */
		private final String _name;

		/**
		 * Constructs a new file with the given parent folder with the given
		 * name. The file is not added to the parent folder.
		 *
		 * @param   parent  Folder containing the file.
		 * @param   name    Name of the file.
		 */
		protected File( final Folder parent , final String name )
		{
			_parent = parent;
			_name   = name;
		}

		@NotNull
		@Override
		public final String getName()
		{
			return _name;
		}

		@Nullable
		@Override
		public String getDisplayName( @NotNull final Locale locale )
		{
			return getName();
		}

		@NotNull
		@Override
		public String getPath()
		{
			final StringBuilder result = new StringBuilder();
			getPath( result );
			return result.toString();
		}

		/**
		 * Appends the path of the file to the given string builder.
		 *
		 * @param   result  String builder to append the file's path to.
		 */
		private void getPath( final StringBuilder result )
		{
			final File parent = _parent;

			if ( parent == null )
			{
				result.append( '/' );
			}
			else
			{
				parent.getPath( result );
				if ( result.charAt( result.length() - 1 ) != '/' )
				{
					result.append( '/' );
				}
				result.append( _name );
			}
		}
	}

	/**
	 * Represents a file with content, i.e. a 'regular' file.
	 */
	private static class ContentFile
		extends File
	{
		/**
		 * Content of the file.
		 */
		private Content _content;

		/**
		 * Constructs a new file with the given parent folder with the given
		 * name. The file is not added to the parent folder.
		 *
		 * @param   parent  Folder containing the file.
		 * @param   name    Name of the file.
		 */
		ContentFile( final Folder parent , final String name )
		{
			super( parent , name );
			_content = new MemoryContent();
		}

		@Override
		public boolean canWrite()
		{
			return true;
		}

		@Nullable
		@Override
		public String getDisplayName( @NotNull final Locale locale )
		{
			return _content.getDisplayName( locale );
		}

		@Override
		public String getDescription( @NotNull final Locale locale )
		{
			return _content.getDescription( locale );
		}

		@Override
		public ImageSource getThumbnail( final int preferredWidth , final int preferredHeight )
		{
			return _content.getThumbnail( preferredWidth , preferredHeight );
		}

		@Override
		public String getType()
		{
			return _content.getType();
		}

		@Override
		public boolean isDirectory()
		{
			return false;
		}

		/**
		 * Returns the content of the file.
		 *
		 * @return  Content of the file.
		 */
		public Content getContent()
		{
			return _content;
		}

		/**
		 * Sets the content of the file.
		 *
		 * @param   content     Content of the file.
		 */
		public void setContent( final Content content )
		{
			_content = content;
		}
	}

	/**
	 * Represents a folder.
	 */
	private static class Folder
		extends File
	{
		/**
		 * Files that the folder contains.
		 */
		private final List<VirtualFile> _contents;

		/**
		 * Constructs a new folder with the given parent folder with the given
		 * name. The folder is not added to the parent folder.
		 *
		 * @param   parent  Folder containing the folder.
		 * @param   name    Name of the folder.
		 */
		Folder( final Folder parent , final String name )
		{
			super( parent , name );
			_contents = new ArrayList<VirtualFile>();
		}

		@Override
		public boolean canWrite()
		{
			return true;
		}

		@Nullable
		@Override
		public String getDescription( @NotNull final Locale locale )
		{
			return null;
		}

		/**
		 * Returns the files that the folder contains. This includes folders.
		 *
		 * @return  All files in this folder.
		 */
		public List<VirtualFile> getContents()
		{
			return Collections.unmodifiableList( _contents );
		}

		/**
		 * Returns the folders that the folder contains.
		 *
		 * @return  Sub-folders in this folder.
		 */
		public List<VirtualFile> getFolders()
		{
			final List<VirtualFile> result = new ArrayList<VirtualFile>();

			for ( final VirtualFile content : _contents )
			{
				if ( content instanceof Folder )
				{
					result.add( content );
				}
			}

			return result;
		}

		@Override
		public ImageSource getThumbnail( final int preferredWidth , final int preferredHeight )
		{
			return null;
		}

		@Nullable
		@Override
		public String getType()
		{
			return null;
		}

		@Override
		public boolean isDirectory()
		{
			return true;
		}

		/**
		 * Returns the file from this folder with the given name, if any.
		 *
		 * @param   name    Name of the file.
		 *
		 * @return  File with the given name, if any.
		 */
		@Nullable
		public File getFile( @NotNull final String name )
		{
			File result = null;

			for ( final VirtualFile content : _contents )
			{
				if ( name.equals( content.getName() ) )
				{
					result = (File)content;
					break;
				}
			}

			return result;
		}

		/**
		 * Adds a file to the folder.
		 *
		 * @param   file    File to be added.
		 *
		 * @return  {@code true} if the file was added;
		 *          {@code false} if the file could not be added, because
		 *          a file with the same name already exists.
		 */
		public boolean add( final File file )
		{
			final boolean result;

			if ( getFile( file.getName() ) == null )
			{
				result = true;
				_contents.add( file );
			}
			else
			{
				result = false;
			}

			return result;
		}

		/**
		 * Removes the file with the given name from the folder.
		 *
		 * @param   name    Name of the file to be removed.
		 *
		 * @return  {@code true} if the file was removed;
		 *          {@code false} if the file could not be removed, because
		 *          the folder doesn't contain a file with the given name.
		 */
		public boolean remove( final String name )
		{
			boolean result = false;

			for ( final Iterator<VirtualFile> i = _contents.iterator() ; i.hasNext() ; )
			{
				final VirtualFile content = i.next();

				if ( name.equals( content.getName() ) )
				{
					i.remove();
					result = true;
					break;
				}
			}

			return result;
		}

		/**
		 * Clear contents.
		 */
		public void clear()
		{
			_contents.clear();
		}
	}
}
