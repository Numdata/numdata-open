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

/**
 * Element of a JNLP file.
 *
 * @author Peter S. Heijnen
 * @see JnlpFile
 */
public abstract class JnlpElement
{
	/**
	 * Write element to the JNLP file.
	 *
	 * @param out XML stream writer.
	 *
	 * @throws XMLStreamException if XML output could not be written.
	 */
	public abstract void write( final XMLStreamWriter out )
	throws XMLStreamException;

	/**
	 * Read element from a JNLP file.
	 *
	 * @param in XML stream reader.
	 *
	 * @throws XMLStreamException if a parse error occurs.
	 */
	public abstract void read( final XMLStreamReader in )
	throws XMLStreamException;

	/**
	 * Utility method to write an optional text element to an XML document. This
	 * method does nothing when the {@code text} parameter is set to {@code
	 * null}.
	 *
	 * @param out  XML output stream.
	 * @param name Name of element to write.
	 * @param text Text content of element.
	 *
	 * @throws XMLStreamException if XML output could not be written.
	 */
	protected static void writeOptionalTextElement( final XMLStreamWriter out, final String name, final String text )
	throws XMLStreamException
	{
		if ( text != null )
		{
			out.writeStartElement( name );
			out.writeCharacters( text );
			out.writeEndElement();
		}
	}

	/**
	 * Utility method to write an optional boolean element to an XML document.
	 * This writes an empty element with the given name if the {@code condition}
	 * is {@code true}; this method does nothing if the {@code condition}
	 * parameter is set to {@code false}.
	 *
	 * @param out       XML output stream.
	 * @param name      Name of element to write.
	 * @param condition If set, write empty element; if not set, do nothing.
	 *
	 * @throws XMLStreamException if XML output could not be written.
	 */
	protected static void writeOptionalBooleanElement( final XMLStreamWriter out, final String name, final boolean condition )
	throws XMLStreamException
	{
		if ( condition )
		{
			out.writeEmptyElement( name );
		}
	}

	/**
	 * Utility method to write an optional attribute of an element to an XML
	 * document. This method does nothing when the {@code value} parameter is
	 * set to {@code null}.
	 *
	 * @param out   XML output stream.
	 * @param name  Name of attribute to write.
	 * @param value Attribute value.
	 *
	 * @throws XMLStreamException if XML output could not be written.
	 */
	protected static void writeOptionalAttribute( final XMLStreamWriter out, final String name, final Object value )
	throws XMLStreamException
	{
		if ( value != null )
		{
			out.writeAttribute( name, value.toString() );
		}
	}

	/**
	 * Utility method to write a mandatory attribute of an element to an XML
	 * document. This method throws an {@link XMLStreamException} if the {@code
	 * value} parameter is set to {@code null}.
	 *
	 * @param out   XML output stream.
	 * @param name  Name of attribute to write.
	 * @param value Attribute value.
	 *
	 * @throws XMLStreamException if XML output could not be written.
	 */
	protected static void writeMandatoryAttribute( final XMLStreamWriter out, final String name, final Object value )
	throws XMLStreamException
	{
		if ( value == null )
		{
			throw new XMLStreamException( name + " attribute was not set" );
		}

		out.writeAttribute( name, value.toString() );
	}
}
