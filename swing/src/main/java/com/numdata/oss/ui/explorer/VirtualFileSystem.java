/*
 * Copyright (c) 2007-2017, Numdata BV, The Netherlands.
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

/**
 * This interface represents a filesystem. A filesystem provides access to
 * {@link VirtualFile}s that represent a hierarchical set of files.
 *
 * This interface is primarily used to interface with an {@link Explorer}.
 *
 * Concrete implementation may retrieve the contents of a database or a disk
 * filesystem.
 *
 * @author Wijnand Wieskamp
 */
public interface VirtualFileSystem
{
	/**
	 * Get file with the specified path.
	 *
	 * @param path Path to file.
	 *
	 * @return Virtual file.
	 *
	 * @throws NullPointerException if {@code path} is {@code null}.
	 * @throws FileNotFoundException if no file with the specified path was
	 * found.
	 * @throws IOException if there is a problem accessing the file system.
	 */
	VirtualFile getFile( String path )
	throws IOException;

	/**
	 * Get all available folders within the filesystem's context.
	 *
	 * @param folderPath Context to get folders for.
	 *
	 * @return List of {@link VirtualFile}s for folders in the filesystem.
	 *
	 * @throws IOException if the folders could not be retrieved.
	 */
	List<VirtualFile> getFolders( String folderPath )
	throws IOException;

	/**
	 * Get contents of the specified folder.
	 *
	 * @param folderPath Path of folder whose contents to retrieve.
	 *
	 * @return List of {@link VirtualFile}s in the specified folder.
	 *
	 * @throws FileNotFoundException if the specified folder was not found.
	 * @throws IOException if the folder contents could not be retrieved.
	 */
	List<VirtualFile> getFolderContents( String folderPath )
	throws IOException;

	/**
	 * Get combined path for the specified folder and path name.
	 *
	 * @param folderPath Path to folder.
	 * @param filename   File name.
	 *
	 * @return Combined path.
	 */
	String getPath( String folderPath, String filename );

	/**
	 * Creates a new folder at the specified path.
	 *
	 * @param folderPath Path to folder to be created.
	 *
	 * @return Virtual file representing the created folder.
	 *
	 * @throws IOException if the folder could not be created.
	 */
	VirtualFile createFolder( String folderPath )
	throws IOException;
}
