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
package com.numdata.oss;

/**
 * Exception sent from a remote server that has been made suitable for
 * serialization by stripping away all remote class and object references
 * and using string representations instead.
 *
 * @author  Peter S. Heijnen
 */
public class RemoteException
	extends RuntimeException
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -2682230515988318057L;

	/**
	 * Class name of source exception.
	 */
	private final String _sourceClass;

	/**
	 * String representation of exception.
	 */
	private final String _sourceAsString;

	/**
	 * Construct exception.
	 *
	 * @param   source      {@link Throwable} to represent.
	 */
	public RemoteException( final Throwable source )
	{
		super( source.getMessage() );
		setStackTrace( source.getStackTrace() );

		final Class<? extends Throwable> sourceClass = source.getClass();
		_sourceClass = sourceClass.getName();
		_sourceAsString = source.toString();

		final Throwable sourceCause = source.getCause();
		if ( sourceCause != null )
		{
			initCause( ( sourceCause instanceof RemoteException ) ? sourceCause : new RemoteException( sourceCause ) );
		}
	}

	@Override
	public RemoteException getCause()
	{
		return (RemoteException)super.getCause();
	}

	/**
	 * Get class name of source exception.
	 *
	 * @return  Class name of source exception.
	 */
	public String getSourceClass()
	{
		return _sourceClass;
	}

	@Override
	public String toString()
	{
		return _sourceAsString;
	}
}
