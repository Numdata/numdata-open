/*
 * Copyright (c) 2009-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.log;

import java.io.*;

import com.numdata.oss.*;

/**
 * Log message.
 *
 * @author  Peter S. Heijnen
 */
public class LogMessage
	implements Serializable
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -2236599273751689179L;

	/**
	 * Name of log (e.g. class name).
	 */
	public String name;

	/**
	 * Log level.
	 */
	public int level;

	/**
	 * Log message.
	 */
	public String message;

	/**
	 * Throwable associated with log message.
	 */
	public RemoteException throwable;

	/**
	 * Identifies the thread that produced the log message.
	 */
	public String threadName;

	/**
	 * Default constructor for empty log message.
	 */
	public LogMessage()
	{
		this( null , -1 , null , null, null );
	}

	/**
	 * Construct log message.
	 *
	 * @param   name        Name of log (e.g. class name).
	 * @param   level       Log level.
	 * @param   message     Log message.
	 * @param   throwable   Throwable associated with log message.
	 * @param   threadName  Identifies the thread that produced the log message.
	 */
	public LogMessage( final String name, final int level, final String message, final Throwable throwable, final String threadName )
	{
		this.name = name;
		this.level = level;
		this.message = message;
		this.throwable = ( throwable instanceof RemoteException ) ? (RemoteException)throwable : ( throwable != null ) ? new RemoteException( throwable ) : null;
		this.threadName = threadName;
	}
}
