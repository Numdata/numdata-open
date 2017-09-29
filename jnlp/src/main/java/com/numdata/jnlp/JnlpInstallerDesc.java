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
package com.numdata.jnlp;

import javax.xml.stream.*;

import com.numdata.oss.io.*;

/**
 * An installer extension describes an application that is executed only once,
 * the first time the JNLP file is used on the local system.
 *
 * The installer extension is intended to install platform-specific native code
 * that requires a more complicated setup than simply loading a native library
 * into the JVM, such as installing a JRE or device driver. The installer
 * executed by the JNLP Client must be a Java Technology-based application. Note
 * that this does not limit the kind of code that can be installed or executed.
 *
 * The installer communicates with the JNLP Client using the {@code
 * ExtensionInstallerService}. Using this service, the installer informs the
 * JNLP Client what native libraries should be loaded into the JVM when the
 * extension is used, or, in the case of a JRE installer, inform the JNLP Client
 * how the installed JRE can be launched.
 *
 * Installers should avoid having to reboot the client machine if at all
 * possible. While some JNLP Clients may be able to continue with the
 * installation/launch after a reboot, this ability is not required.
 *
 * @author Peter S. Heijnen
 */
public class JnlpInstallerDesc
extends JnlpElement
{
	/**
	 * The name of the class containing the {@code main} method of the
	 * installer. This attribute can be omitted if the {@code main} class can be
	 * found from the {@code Main-Class} manifest entry in the main JAR file.
	 */
	private String _mainClass = null;

	public String getMainClass()
	{
		return _mainClass;
	}

	public void setMainClass( final String mainClass )
	{
		_mainClass = mainClass;
	}

	@Override
	public void write( final XMLStreamWriter out )
	throws XMLStreamException
	{
		out.writeEmptyElement( "installer-desc" );
		writeOptionalAttribute( out, "main-class", getMainClass() );
	}

	@Override
	public void read( final XMLStreamReader in )
	throws XMLStreamException
	{
		setMainClass( in.getAttributeValue( null, "main-class" ) );
		XMLTools.skipElement( in );
	}
}
