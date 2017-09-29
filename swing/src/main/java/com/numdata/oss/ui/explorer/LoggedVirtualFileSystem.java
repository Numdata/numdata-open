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
package com.numdata.oss.ui.explorer;

import java.io.IOException;
import java.util.List;

/**
 * This class can be used to log a {@link VirtualFileSystem}. It wraps a given
 * virtual file system and prints some log messages when its methods are used.
 *
 * @author  G.B.M. Rupert
 */
public class LoggedVirtualFileSystem
	implements VirtualFileSystem
{
	/**
	 * Virtual file system that is being logged.
	 */
	private final VirtualFileSystem _target;

	/**
	 * Construct new {@link LoggedVirtualFileSystem}.
	 *
	 * @param   target  Virtual file system to log.
	 */
	public LoggedVirtualFileSystem( final VirtualFileSystem target )
	{
		System.out.println( "LoggedVirtualFileSystem.LoggedVirtualFileSystem( '" + target + "' )" );

		_target = target;
	}

	public VirtualFile getFile( final String path )
		throws IOException
	{
		System.out.println( "LoggedVirtualFileSystem.getFile( '" + path + "' )" );

		return _target.getFile( path );
	}

	public List<VirtualFile> getFolders( final String folderPath )
		throws IOException
	{
		System.out.println( "LoggedVirtualFileSystem.getFolders( '" + folderPath + "' )" );

		return _target.getFolders( folderPath );
	}

	public List<VirtualFile> getFolderContents( final String folderPath )
		throws IOException
	{
		System.out.println( "LoggedVirtualFileSystem.getFolderContents( '" + folderPath + "' )" );

		return _target.getFolderContents( folderPath );
	}

	public String getPath( final String folderPath , final String filename )
	{
		System.out.println( "LoggedVirtualFileSystem.getPath( '" + folderPath + "' , '" + filename + "' )" );

		return _target.getPath( folderPath , filename );
	}

	public VirtualFile createFolder( final String folderPath )
		throws IOException
	{
		System.out.println( "LoggedVirtualFileSystem.createFolder( '" + folderPath + "' )" );

		return _target.createFolder( folderPath );
	}
}
