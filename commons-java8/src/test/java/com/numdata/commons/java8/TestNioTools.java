/*
 * Copyright (c) 2020, Numdata BV, The Netherlands.
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
import java.util.*;
import java.util.zip.*;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link NioTools}.
 *
 * @author Gerrit Meinders
 */
@SuppressWarnings( "JavaDoc" )
public class TestNioTools
{
	@Test
	public void testUnzip()
	throws Exception
	{
		final Path dir = Files.createTempDirectory( "root" );
		try
		{
			System.out.println( "Temporary directory: " + dir );

			System.out.println( "Create 'test.zip'" );
			final Path zipFile = dir.resolve( "test.zip" );
			try ( final ZipOutputStream zip = new ZipOutputStream( Files.newOutputStream( zipFile ) ) )
			{
				final ZipEntry subdir = new ZipEntry( "subdir/" );
				zip.putNextEntry( subdir );

				zip.putNextEntry( new ZipEntry( "subdir/test.txt" ) );
				zip.write( "Hello".getBytes() );
			}

			System.out.println( "Unzip 'test.zip'" );
			NioTools.unzip( dir, zipFile );

			System.out.println( "Check contents directory" );
			final Collection<Object> expectedFiles = new TreeSet<>();
			expectedFiles.add( "" );
			expectedFiles.add( "test.zip" );
			expectedFiles.add( "subdir" );
			expectedFiles.add( "subdir" + File.separator + "test.txt" );

			final Collection<String> actualFiles = Files.walk( dir ).map( dir::relativize ).map( Object::toString ).collect( toCollection( TreeSet::new ) );
			assertEquals( "Unexpected list of files after unzip", expectedFiles, actualFiles );
		}
		finally
		{
			System.out.println( "Delete " + dir + " directory" );
			NioTools.deleteRecursively( dir );
		}
	}

	@Test
	public void testCopyFilesRecursively()
	throws IOException
	{
		final Path root = Files.createTempDirectory( "root" );
		try
		{
			final List<Path> dirs = Arrays.asList(
			Paths.get( "dir1" ),
			Paths.get( "dir1", "dir2" ),
			Paths.get( "dir3" ),
			Paths.get( "dir4" ),
			Paths.get( "dir4", "dir5" )
			);

			final List<Path> files = Arrays.asList(
			Paths.get( "file1" ),
			Paths.get( "file2" ),
			Paths.get( "dir1", "dir2", "file3" ),
			Paths.get( "dir1", "file4" ),
			Paths.get( "dir4", "file5" )
			);

			final Path src = root.resolve( "src" );
			Files.createDirectory( src );

			for ( final Path dir : dirs )
			{
				Files.createDirectory( src.resolve( dir ) );
			}

			final byte[] fileContents = "Generated for unit test".getBytes();
			for ( final Path file : files )
			{
				Files.write( src.resolve( file ), fileContents );
			}

			final Path dir1 = root.resolve( "dir1" );
			final Path dir2 = root.resolve( "dir2" );
			final Path dir2link = root.resolve( "dir2link" );
			final Path dir3 = root.resolve( "dir3" );

			for ( int i = 0; i <= 2; i++ )
			{
				final Path dest;
				switch ( i )
				{
					default:
					case 0:
						Files.createDirectory( dir1 );
						dest = dir1;
						break;
					case 1:
						Files.createDirectory( dir2 );
						try
						{
							Files.createSymbolicLink( dir2link, dir2 );
						}
						catch ( FileSystemException e )
						{
							System.err.println( "WARNING: Symbolic link cannot be tested on this platform: " + e );
							System.err.println( "The test will continue, but 'dir2link' will be a directory instead of a symbolic link to a directory." );
						}
						dest = dir2link;
						break;
					case 2:
						dest = dir3;
						break;
				}

				NioTools.copyFilesRecursively( src, dest );

				assertTrue( "Missing directory", Files.isDirectory( dest ) );

				for ( final Path dir : dirs )
				{
					final Path resolved = dest.resolve( dir );
					assertTrue( "Missing directory: " + resolved, Files.isDirectory( resolved ) );
				}

				for ( final Path file : files )
				{
					final Path resolved = dest.resolve( file );
					assertTrue( "Missing file: " + resolved, Files.isRegularFile( resolved ) );
					assertArrayEquals( "Unexpected contents for file: " + resolved, fileContents, Files.readAllBytes( resolved ) );

				}
			}
		}
		finally
		{
			NioTools.deleteRecursively( root );
		}
	}
}
