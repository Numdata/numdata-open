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
package com.numdata.uri;

import java.io.*;
import java.net.*;

/**
 * Provides access to data identified by a {@link URI}, while encapsulating the
 * underlying protocol. This allows for underlying connections or sessions to be
 * kept alive to service multiple requests. This is similar to
 * {@link URLConnection}, but without the hassle (e.g. system properties to
 * identify packages containing URL stream handlers) and with less focus on HTTP.
 *
 * @author G. Meinders
 */
public interface URIConnection
{
	/**
	 * Reads content from the URI.
	 *
	 * @return  Content for the URI.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	byte[] read()
		throws IOException;

	/**
	 * Writes the given content to the URI.
	 *
	 * @param   content     Content to be written.
	 * @param   append      {@code true} to append to existing content;
	 *                      {@code false} to overwrite.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	void write( byte[] content, boolean append )
		throws IOException;

	/**
	 * Returns an input stream that reads from the URI connection.
	 *
	 * @return  Input stream.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	InputStream getInputStream()
		throws IOException;

	/**
	 * Returns an output stream that writes to the URI connection.
	 *
	 * @param   append  {@code true} to append to existing content;
	 *                  {@code false} to overwite.
	 *
	 * @return  Output stream.
	 *
	 * @throws  IOException if an I/O error occurs.
	 */
	OutputStream getOutputStream( boolean append )
		throws IOException;
}
