/*
 * Copyright (c) 2020-2020, Numdata BV, The Netherlands.
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

import org.jetbrains.annotations.*;

/**
 * Log channel.
 *
 * @author Peter S. Heijnen
 */
public interface LogChannel
{
	/**
	 * A {@link LogChannel} that logs nothing.
	 */
	@NotNull
	NullChannel NULL = new NullChannel();

	/**
	 * Send log message to this channel.
	 *
	 * @param message Log message.
	 */
	void log( @Nullable final String message );

	/**
	 * Send log message to this channel.
	 *
	 * @param message Log message.
	 */
	void log( @Nullable final ClassLogger.MessageSupplier message );

	/**
	 * Send log message with an associated throwable object to this channel.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	void log( @Nullable final ClassLogger.MessageSupplier message, @Nullable final Throwable throwable );

	/**
	 * Send log message with an associated throwable object to this channel.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	void log( @Nullable final String message, @Nullable final Throwable throwable );

	/**
	 * Implementation of {@link LogChannel} that does nothing.
	 */
	class NullChannel
	implements LogChannel
	{
		@Override
		public void log( @Nullable final String message )
		{
		}

		@Override
		public void log( @Nullable final ClassLogger.MessageSupplier message )
		{
		}

		@Override
		public void log( @Nullable final ClassLogger.MessageSupplier message, @Nullable final Throwable throwable )
		{
		}

		@Override
		public void log( @Nullable final String message, @Nullable final Throwable throwable )
		{
		}
	}

	/**
	 * Implementation of {@link LogChannel} that actually logs.
	 */
	class RealChannel
	implements LogChannel
	{
		/**
		 * Name of log.
		 */
		@NotNull
		private final String _name;

		/**
		 * Log level.
		 */
		private final int _level;

		/**
		 * Create channel for the given level.
		 *
		 * @param name  Name of log.
		 * @param level Log level.
		 */
		public RealChannel( @NotNull final String name, final int level )
		{
			_name = name;
			_level = level;
		}

		@Override
		public void log( @Nullable final String message )
		{
			log( message, null );
		}

		@Override
		public void log( @Nullable final ClassLogger.MessageSupplier message )
		{
			log( message, null );
		}

		@Override
		public void log( @Nullable final ClassLogger.MessageSupplier message, @Nullable final Throwable throwable )
		{
			log( ( message != null ) ? message.get() : null, throwable );
		}

		@Override
		public void log( @Nullable final String message, @Nullable final Throwable throwable )
		{
			ClassLogger.logImpl( _name, _level, throwable, message );
		}
	}

}
