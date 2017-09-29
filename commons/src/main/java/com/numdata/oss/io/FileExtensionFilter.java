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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.*;

/**
 * This class filters files with a given extension. It can be used by a {@code
 * JFileChooser} or by the {@code File.listFiles()} method.
 *
 * @author H.B.J. te Lintelo
 */
public final class FileExtensionFilter
extends FileFilter
implements java.io.FileFilter, FilenameFilter
{
	/**
	 * Extension to filter.
	 */
	private final String _extension;

	/**
	 * Description of filter.
	 */
	private final String _description;

	/**
	 * Include directories in result.
	 */
	private final boolean _includeDirectories;

	/**
	 * Construct filter.
	 *
	 * @param extension          Extension to filter.
	 * @param description        Description of filter.
	 * @param includeDirectories Include directories in result.
	 */
	public FileExtensionFilter( final String extension, final String description, final boolean includeDirectories )
	{
		_extension = extension;
		_description = description;
		_includeDirectories = includeDirectories;
	}

	/**
	 * Check if filename matches the extension filter.
	 *
	 * @param filename Filename to match.
	 *
	 * @return {@code true} if file is correct; {@code false} if file does not
	 * match.
	 */
	private boolean isFilenameMatch( final String filename )
	{
		final int fl = filename.length();
		final int el = _extension.length();

		return ( ( fl < 1 ) || ( filename.charAt( 0 ) != '.' ) )
		       && ( filename.regionMatches( true, fl - el, _extension, 0, el ) );
	}

	/**
	 * Accept method for the {@code FilenameFilter} interface. Tests if a
	 * specified file should be included in a file list.
	 *
	 * @param dir  the directory in which the file was found.
	 * @param name the name of the file.
	 *
	 * @return {@code true} if and only if the name should be included in the
	 * file list; {@code false} otherwise.
	 */
	public boolean accept( final File dir, final String name )
	{
		return isFilenameMatch( name ) || ( _includeDirectories || !new File( dir, name ).isDirectory() );
	}

	/**
	 * Whether the given file is accepted by this filter.
	 *
	 * @param f The file to check.
	 *
	 * @return {@code true} when the given file is accepted.
	 */
	public boolean accept( final File f )
	{
		return isFilenameMatch( f.getName() ) || ( _includeDirectories || !f.isDirectory() );
	}

	/**
	 * The description of this filter. For example: "JPG and GIF Images"
	 *
	 * @return String containing the description of the filter.
	 *
	 * @see FileView#getName
	 */
	public String getDescription()
	{
		return _description;
	}
}
