/*
 * Copyright (c) 2019-2020, Numdata BV, The Netherlands.
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
package com.numdata.commons.java8;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.*;
import java.util.zip.*;

import static com.numdata.commons.java8.FunctionTools.*;
import org.jetbrains.annotations.*;

/**
 * This class contains utilities for working the Java 8 {@code java.nio}
 * package.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "unused", "FinalClass" } )
public final class NioTools
{
	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private NioTools()
	{
	}

	/**
	 * Unpack ZIP archive to the given directory.
	 *
	 * @param toDirectory Directory to extract files to.
	 * @param zipFile     ZIP archive to unpack.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void unzip( @NotNull final Path toDirectory, @NotNull final Path zipFile )
	throws IOException
	{
		try ( final InputStream in = Files.newInputStream( zipFile ) )
		{
			unzip( toDirectory, in );
		}
	}

	/**
	 * Unpack ZIP archive to the given directory.
	 *
	 * @param toDirectory Directory to extract files to.
	 * @param in          Stream to read ZIP archive from.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void unzip( @NotNull final Path toDirectory, @NotNull final InputStream in )
	throws IOException
	{
		try ( final ZipInputStream zipIn = new ZipInputStream( in ) )
		{
			for ( ZipEntry entry = zipIn.getNextEntry(); entry != null; entry = zipIn.getNextEntry() )
			{
				final Path file = toDirectory.resolve( entry.getName().replace( '\\', File.separatorChar ) );
				if ( entry.isDirectory() )
				{
					Files.createDirectories( file );
				}
				else
				{
					Files.createDirectories( file.getParent() );
					Files.copy( zipIn, file, StandardCopyOption.REPLACE_EXISTING );
				}
			}
		}
	}

	/**
	 * Recursively copy files from a directory to the web application root.
	 *
	 * @param fromDirectory Directory to copy contents from.
	 * @param toDirectory   Directory to copy contents to.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void copyFilesRecursively( @NotNull final Path fromDirectory, @NotNull final Path toDirectory )
	throws IOException
	{
		if ( !Files.exists( toDirectory ) )
		{
			Files.createDirectories( toDirectory );
		}

		Files.walkFileTree( fromDirectory, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult preVisitDirectory( final Path dir, final BasicFileAttributes attrs )
			throws IOException
			{
				if ( !dir.equals( fromDirectory ) )
				{
					Files.createDirectories( toDirectory.resolve( fromDirectory.relativize( dir ) ) );
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile( final Path file, final BasicFileAttributes attrs )
			throws IOException
			{
				Files.copy( file, toDirectory.resolve( fromDirectory.relativize( file ) ), StandardCopyOption.REPLACE_EXISTING );
				return FileVisitResult.CONTINUE;
			}
		} );
	}

	/**
	 * Recursively delete files and folders.
	 *
	 * @param path Path to file or directory to delete.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	public static void deleteRecursively( @NotNull final Path path )
	throws IOException
	{
		Files.walk( path )
		     .sorted( Comparator.reverseOrder() )
		     .forEach( liftConsumerException( Files::delete ) );
	}
}
