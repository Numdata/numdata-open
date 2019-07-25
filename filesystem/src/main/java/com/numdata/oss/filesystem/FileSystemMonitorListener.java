/*
 * Copyright (c) 2006-2019, Numdata BV, The Netherlands.
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
package com.numdata.oss.filesystem;

import org.jetbrains.annotations.*;

/**
 * Interface for objects interested in file system changes reported by a {@link
 * FileSystemMonitor}.
 *
 * @author G. Meinders
 */
public interface FileSystemMonitorListener
{
	/**
	 * Reason for {@link #fileSkipped} method.
	 */
	enum SkipReason
	{
		/**
		 * Due to {@link FileSystemMonitor#getPathFilter()}.
		 */
		PATH_FILTER,

		/**
		 * Due to {@link FileSystemMonitor#isSingleFile()}.
		 */
		SINGLE_FILE,

		/**
		 * Due to {@link FileSystemMonitor#getInitialFileHandling()}.
		 */
		INITIAL_FILE_HANDLING,

		/**
		 * File is not modified.
		 */
		NOT_MODIFIED
	}

	/**
	 * Notifies the listener that a file was skipped.
	 *
	 * @param monitor File system monitor reporting the change.
	 * @param handle  Identifies the file that was skipped.
	 * @param reason  Reason why file was skipped.
	 */
	void fileSkipped( @NotNull FileSystemMonitor monitor, @NotNull Object handle, @NotNull SkipReason reason );

	/**
	 * Notifies the listener that a file was added to the file system.
	 *
	 * @param monitor File system monitor reporting the change.
	 * @param handle  Identifies the file that was added.
	 */
	void fileAdded( @NotNull FileSystemMonitor monitor, @NotNull Object handle );

	/**
	 * Notifies the listener that a file in the file system was modified.
	 *
	 * @param monitor File system monitor reporting the change.
	 * @param handle  Identifies the file that was modified.
	 */
	void fileModified( @NotNull FileSystemMonitor monitor, @NotNull Object handle );

	/**
	 * Notifies the listener that a file was removed to the file system.
	 *
	 * @param monitor File system monitor reporting the change.
	 * @param handle  Identifies the file that was removed.
	 */
	void fileRemoved( @NotNull FileSystemMonitor monitor, @NotNull Object handle );
}
