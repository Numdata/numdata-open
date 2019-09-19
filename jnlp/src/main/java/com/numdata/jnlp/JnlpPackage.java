/*
 * Copyright (c) 2011-2017, Numdata BV, The Netherlands.
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
package com.numdata.jnlp;

import javax.xml.stream.*;

/**
 * Indicates to the JNLP Client which packages or classes are implemented in
 * which JAR files.
 *
 * The package element only makes sense to use with lazily-downloaded resources,
 * since all other resources will already be available to the JVM. Thus, it will
 * already know what packages are implemented in those JAR files. However, it
 * can direct the JNLP Client to download the right lazy JAR resources, instead
 * of having to download each individual resource one at a time to check.
 *
 * @author Peter S. Heijnen
 */
public class JnlpPackage
extends JnlpElement
{
	/**
	 * Package or class name (e.g. 'org.example.*', 'org.example.MyClass').
	 */
	private String _name = null;

	/**
	 * Which part must be downloaded for the package.
	 */
	private String _part = null;

	/**
	 * If set, sub-packages are also matched.
	 */
	private Boolean _recursive = null;

	public String getName()
	{
		return _name;
	}

	public void setName( final String name )
	{
		_name = name;
	}

	public String getPart()
	{
		return _part;
	}

	public void setPart( final String part )
	{
		_part = part;
	}

	public Boolean getRecursive()
	{
		return _recursive;
	}

	public void setRecursive( final Boolean recursive )
	{
		_recursive = recursive;
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		out.writeEmptyElement( "package" );
		writeMandatoryAttribute( out, "name", getName() );
		writeMandatoryAttribute( out, "part", getPart() );
		writeOptionalAttribute( out, "recursive", getRecursive() );
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		throw new UnsupportedOperationException( "Not implemented yet." );
	}
}
