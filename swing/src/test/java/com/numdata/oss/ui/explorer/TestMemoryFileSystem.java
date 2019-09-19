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

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for the {@link MemoryFileSystem} class.
 *
 * @author G. Meinders
 */
public class TestMemoryFileSystem
{
	/**
	 * Tests retrieval of folder contents.
	 *
	 * @throws IOException if the test fails due to an I/O error.
	 */
	@Test
	public void testGetFolderContents()
	throws IOException
	{
		final MemoryFileSystem fs = new MemoryFileSystem();

		final VirtualFile folder = fs.createFolder( "/test" );
		assertTrue( "Folder must be a directory.", folder.isDirectory() );
		assertEquals( "Wrong folder name.", "test", folder.getName() );
		assertEquals( "Wrong path.", "/test", folder.getPath() );

		final VirtualFile subFolder = fs.createFolder( fs.getPath( folder.getPath(), "more" ) );
		assertTrue( "Folder must be a directory.", subFolder.isDirectory() );
		assertEquals( "Wrong folder name.", "more", subFolder.getName() );
		assertEquals( "Wrong path.", "/test/more", subFolder.getPath() );

		final List<VirtualFile> folderContents = fs.getFolderContents( folder.getPath() );
		assertEquals( "Unexpected contents size.", 1, folderContents.size() );
		assertSame( "Unexpected contents.", subFolder, folderContents.get( 0 ) );

		final List<VirtualFile> rootContents = fs.getFolderContents( "/" );
		assertEquals( "Unexpected contents size.", 1, rootContents.size() );
		assertSame( "Unexpected contents.", folder, rootContents.get( 0 ) );
	}

	/**
	 * Tests retrieval of files.
	 *
	 * @throws IOException if the test fails due to an I/O error.
	 */
	@Test
	public void testGetFile()
	throws IOException
	{
		final MemoryFileSystem fs = new MemoryFileSystem();

		final VirtualFile file = fs.createFile( "/test" );
		final VirtualFile folder = fs.createFolder( "/folder" );
		final VirtualFile folderFolder = fs.createFolder( "/folder/folder" );
		final VirtualFile folderFolderFile = fs.createFolder( "/folder/folder/file" );

		assertSame( "Unexpected file.", file, fs.getFile( "/test" ) );
		assertSame( "Unexpected file.", folder, fs.getFile( "/folder" ) );
		assertSame( "Unexpected file.", folderFolder, fs.getFile( "/folder/folder" ) );
		assertSame( "Unexpected file.", folderFolderFile, fs.getFile( "/folder/folder/file" ) );

		try
		{
			fs.getFile( "/this/path/doesn't/exist" );
			fail( "Expected an exception." );
		}
		catch ( FileNotFoundException e )
		{
			// Success!
		}

		try
		{
			fs.getFile( null );
			fail( "Expected an exception." );
		}
		catch ( NullPointerException e )
		{
			// Success!
		}

		try
		{
			fs.getFile( "" );
			fail( "Expected an exception." );
		}
		catch ( IllegalArgumentException e )
		{
			// Success!
		}

		try
		{
			fs.getFile( "test" );
			fail( "++Expected an exception." );
		}
		catch ( IllegalArgumentException e )
		{
			// Success!
		}
	}
}
