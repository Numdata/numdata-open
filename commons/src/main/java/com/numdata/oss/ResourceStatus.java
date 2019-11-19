/*
 * Copyright (c) 2011-2019, Numdata BV, The Netherlands.
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
package com.numdata.oss;

import org.jetbrains.annotations.*;

/**
 * Describes the status of a resource.
 *
 * @author Peter S. Heijnen
 */
public class ResourceStatus
{
	/**
	 * Status.
	 */
	public enum Status
	{
		/**
		 * No external resource is defined or access to the external resource is
		 * explicitly disabled.
		 */
		DISABLED,

		/**
		 * An external resource is specified, but that resource is (temporarily)
		 * unreachable (due to link failure, power down, service offline, access
		 * denied, resource busy, etc).
		 */
		UNREACHABLE,

		/**
		 * The external resource is online and available for use.
		 */
		AVAILABLE,

		/**
		 * The external resource is accessible, but currently not available for
		 * use (busy, temporarily disabled, in service mode, etc).
		 */
		UNAVAILABLE,

		/**
		 * Status is unknown (due to missing mechanism for determining the
		 * status).
		 */
		UNKNOWN,
	}

	/**
	 * Status of resource.
	 */
	@NotNull
	private Status _status = Status.UNKNOWN;

	/**
	 * Exception that was caught.
	 */
	@Nullable
	private Exception _exception = null;

	/**
	 * Details about status.
	 */
	@Nullable
	private String _details = null;

	/**
	 * Last time that the resource was available. -1 if never or unknown.
	 */
	private long _lastOnline = -1L;

	@NotNull
	public Status getStatus()
	{
		return _status;
	}

	public void setStatus( @NotNull final Status status )
	{
		_status = status;
	}

	@Nullable
	public Exception getException()
	{
		return _exception;
	}

	public void setException( @Nullable final Exception exception )
	{
		_exception = exception;
	}

	@Nullable
	public String getDetails()
	{
		return _details;
	}

	public void setDetails( @Nullable final String details )
	{
		_details = details;
	}

	public long getLastOnline()
	{
		return _lastOnline;
	}

	public void setLastOnline( final long lastOnline )
	{
		_lastOnline = lastOnline;
	}
}
