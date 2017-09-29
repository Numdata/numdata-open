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

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * A file stored in a {@link VirtualFileSystem}.
 *
 * @author  Peter S. Heijnen.
 */
public interface VirtualFile
{
	/**
	 * Tests whether this file can be written.
	 *
	 * @return  {@code true} if this file can be written;
	 *          {@code false} otherwise.
	 */
	boolean canWrite();

	/**
	 * Returns the description of this file for the given locale.
	 *
	 * @param   locale  Locale to be used.
	 *
	 * @return  Description of this file.
	 */
	@Nullable
	String getDescription( @NotNull Locale locale );

	/**
	 * Returns the name of the file.
	 *
	 * @return  Name of the file.
	 */
	@NotNull
	String getName();

	/**
	 * Returns the display name of the file for the given locale.
	 *
	 * @param   locale  Locale to be used.
	 *
	 * @return  Display name of the file;
	 *          {@code null} if no explicit display name is available,
	 */
	@Nullable
	String getDisplayName( @NotNull Locale locale );

	/**
	 * Returns the path of the file.
	 *
	 * @return  Path of the file.
	 */
	@NotNull
	String getPath();

	/**
	 * Returns the thumbnail image of the file if available.
	 *
	 * @param preferredWidth    the preferred width of the thumbnail.
	 * @param preferredHeight   the preferred height of the thumbnail.
	 *
	 * @return  Thumbnail of the file if available.
	 */
	@Nullable
	ImageSource getThumbnail( int preferredWidth, int preferredHeight );

	/**
	 * Get file type.
	 *
	 * @return  File type.
	 */
	@Nullable
	String getType();

	/**
	 * Whether or not this file is a directory.
	 *
	 * @return  {@code true} if this file is a directory;
	 *          {@code false} otherwise.
	 */
	boolean isDirectory();
}
