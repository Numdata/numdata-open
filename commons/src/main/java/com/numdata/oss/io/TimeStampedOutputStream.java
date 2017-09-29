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
import java.text.*;
import java.util.*;

/**
 * This stream inserts a timestamp before each line of text that is outputted
 * through it. Its obvious application is filtering output to log files.
 *
 * @author H.B.J. te Lintelo
 */
public final class TimeStampedOutputStream
extends FilterOutputStream
{
	/**
	 * Date format used in log files.
	 */
	private static final DateFormat LOG_DATE_FORMAT = new SimpleDateFormat( "yyyyMMdd hh:mm:ss.SSS" );

	/**
	 * Time stamp needed when writing to file.
	 */
	private boolean _writeTimeStamp = true;

	/**
	 * Create a TimeStampedOutputStream.
	 *
	 * @param os OutputStream where to write the log.
	 */
	public TimeStampedOutputStream( final OutputStream os )
	{
		super( os );
	}

	/**
	 * This write method, is used to put a 'timestamp' after each line in the
	 * log file.
	 *
	 * @param b the {@code byte}.
	 *
	 * @throws IOException if an I/O error occurs. In particular, an {@code
	 * IOException} may be thrown if the output stream has been closed.
	 */
	public void write( final int b )
	throws IOException
	{
		if ( _writeTimeStamp )
		{
			_writeTimeStamp = false;
			write( LOG_DATE_FORMAT.format( new Date() ).getBytes() );
			write( ' ' );
			write( '-' );
			write( ' ' );
		}

		if ( b == '\n' )
		{
			_writeTimeStamp = true;
		}

		super.write( b );
	}
}
