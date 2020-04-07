/*
 * Copyright (c) 2008-2020, Numdata BV, The Netherlands.
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
package com.numdata.oss.net;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

import com.numdata.oss.*;
import com.numdata.oss.io.*;
import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * This class defines the client/server protocol.
 *
 * @author Peter S. Heijnen
 */
public class Protocol
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( Protocol.class );

	/**
	 * Send packet to remote.
	 *
	 * @param out    Stream to send message to.
	 * @param packet Packet to send.
	 *
	 * @throws IOException if a problem occurred while sending the message.
	 */
	public static void send( @NotNull final OutputStream out, @NotNull final Packet packet )
	throws IOException
	{
		final MessageDigest digest = getDigest();

		final DigestOutputStream dos = new DigestOutputStream( out, digest );

		final GZIPOutputStream gzipOutputStream = new GZIPOutputStream( dos );

		DataStreamTools.writeString( gzipOutputStream, packet.getMessage() );

		// IMPORTANT: should not close temporary stream, otherwise the stream would be closed as well, which we don't want!
		@SuppressWarnings( { "IOResourceOpenedButNotSafelyClosed", "resource" } ) final ObjectOutput objectOutputStream = new ObjectOutputStream( gzipOutputStream );
		objectOutputStream.writeObject( packet.getAttributes() );
		gzipOutputStream.finish();

		out.write( digest.digest() );
	}

	/**
	 * Receive packet from remote.
	 *
	 * @param in            Stream to receive message from.
	 * @param contentLength Content length of message.
	 *
	 * @return {@link Packet} that was received.
	 *
	 * @throws IOException if a problem occurred while receiving the message.
	 */
	@NotNull
	public static Packet receive( @NotNull final InputStream in, final int contentLength )
	throws IOException
	{
		final MessageDigest digest = getDigest();

		if ( contentLength < digest.getDigestLength() )
		{
			throw new ProtocolException( "content too small" );
		}

		final byte[] data = DataStreamTools.readByteArray( in, contentLength - digest.getDigestLength() );
		digest.update( data );

		final byte[] hash = DataStreamTools.readByteArray( in, digest.getDigestLength() );

		if ( !Arrays.equals( hash, digest.digest() ) )
		{
			throw new ProtocolException( "data corrupted" );
		}

		final String message;
		final byte[] decompressed;
		{
			try ( final GZIPInputStream gzipInputStream = new GZIPInputStream( new ByteArrayInputStream( data ) ) )
			{
				message = DataStreamTools.readString( gzipInputStream );
				if ( message == null )
				{
					throw new ProtocolException( new NullPointerException( "message" ) );
				}

				decompressed = DataStreamTools.readByteArray( gzipInputStream );
			}
		}

		final Map<String, Serializable> attributes;
		try
		{
			// IMPORTANT: should not close temporary stream, otherwise the stream would be closed as well, which we don't want!
			@SuppressWarnings( { "IOResourceOpenedButNotSafelyClosed", "resource" } ) final ObjectInputStream objectInputStream = new ObjectInputStream( new ByteArrayInputStream( decompressed ) );
			attributes = (Map<String, Serializable>)objectInputStream.readObject();
		}
		catch ( final ClassNotFoundException e )
		{
			final String warning = "Class not found while deserializing message.";
			LOG.warn( warning, e );
			throw new ProtocolException( appendDataDump( warning + " (" + e.getMessage() + ')', decompressed ), e );
		}
		catch ( final IOException e )
		{
			final String warning = "Error in serialized message.";
			LOG.warn( warning, e );
			throw new ProtocolException( appendDataDump( warning + " (" + e.getMessage() + ')', decompressed ), e );
		}

		final Packet result = new Packet( message );

		if ( attributes != null )
		{
			for ( final Map.Entry<String, Serializable> attribute : attributes.entrySet() )
			{
				result.setAttribute( attribute.getKey(), attribute.getValue() );
			}
		}

		return result;
	}

	/**
	 * Append data dump to the given message.
	 *
	 * @param message Message to append dump to.
	 * @param data    Data to dump.
	 *
	 * @return Message with data appended.
	 */
	@NotNull
	private static String appendDataDump( @NotNull final String message, @NotNull final byte[] data )
	{
		final StringWriter buffer = new StringWriter();
		buffer.append( message ).append( "\nSerialized data dump:\n" );
		final int maxLength = 1024;
		if ( data.length > maxLength )
		{
			buffer.append( "(dumping only first " + maxLength + " bytes of message; actual data size was " ).append( String.valueOf( data.length ) ).append( " bytes)\n" );
		}
		try
		{
			HexDump.write( buffer, data, 0, Math.min( data.length, maxLength ) );
		}
		catch ( final IOException e ) // impossible with StringWriter
		{
			throw new AssertionError( e );
		}
		return buffer.toString();
	}

	/**
	 * Get message digest algorithm.
	 *
	 * @return Message digest algorithm.
	 *
	 * @throws ProtocolException if the required algorithm is not available.
	 */
	@NotNull
	private static MessageDigest getDigest()
	throws ProtocolException
	{
		try
		{
			return MessageDigest.getInstance( "MD5" );
		}
		catch ( final NoSuchAlgorithmException e )
		{
			throw new ProtocolException( "MD5 unavailable", e );
		}
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private Protocol()
	{
	}
}
