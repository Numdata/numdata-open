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
package com.numdata.oss.net;

import java.io.*;
import java.security.*;
import java.util.*;
import java.util.zip.*;

import com.numdata.oss.*;
import com.numdata.oss.io.*;

/**
 * This class defines the client/server protocol.
 *
 * @author Peter S. Heijnen
 */
public class Protocol
{
	/**
	 * Send packet to remote.
	 *
	 * @param out    Stream to send message to.
	 * @param packet Packet to send.
	 *
	 * @throws IOException if a problem occurred while sending the message.
	 * @throws NullPointerException if either argument is {@code null}.
	 */
	public static void send( final OutputStream out, final Packet packet )
	throws IOException
	{
		final MessageDigest digest = getDigest();

		final DigestOutputStream dos = new DigestOutputStream( out, digest );

		final GZIPOutputStream gzipOutputStream = new GZIPOutputStream( dos );

		DataStreamTools.writeString( gzipOutputStream, packet.getMessage() );

		@SuppressWarnings ( "IOResourceOpenedButNotSafelyClosed" ) final ObjectOutputStream objectOutputStream = new ObjectOutputStream( gzipOutputStream );
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
	public static Packet receive( final InputStream in, final int contentLength )
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
			final GZIPInputStream gzipInputStream = new GZIPInputStream( new ByteArrayInputStream( data ) );
			try
			{
				message = DataStreamTools.readString( gzipInputStream );
				if ( message == null )
				{
					throw new ProtocolException( new NullPointerException( "message" ) );
				}

				decompressed = DataStreamTools.readByteArray( gzipInputStream );
			}
			finally
			{
				gzipInputStream.close();
			}
		}

		final Map<String, Serializable> attributes;
		try
		{
			@SuppressWarnings ( "IOResourceOpenedButNotSafelyClosed" ) final ObjectInputStream objectInputStream = new ObjectInputStream( new ByteArrayInputStream( decompressed ) );
			attributes = (Map<String, Serializable>)objectInputStream.readObject();
		}
		catch ( ClassNotFoundException e )
		{
			System.err.println( "Class not found while deserializing message." );
			dumpData( decompressed );

			throw new ProtocolException( e.getMessage(), e );
		}
		catch ( IOException e )
		{
			System.err.println( "Error in serialized message. Serialized data dump:" );
			dumpData( decompressed );

			throw new ProtocolException( e.getMessage(), e );
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
	 * Dump data to console.
	 *
	 * @param data Data to dump.
	 *
	 * @throws IOException if an error occurs while accessing resources.
	 */
	private static void dumpData( final byte[] data )
	throws IOException
	{
		System.err.println( "Serialized data dump:" );
		final int maxLength = 1024;
		if ( data.length > maxLength )
		{
			System.err.println( "(dumping only first " + maxLength + " bytes of message; actual data size was " + data.length + " bytes)" );
		}
		TextTools.hexdump( System.err, data, 0, Math.min( data.length, maxLength ) );
	}

	/**
	 * Get message digest algorithm.
	 *
	 * @return Message digest algorithm.
	 *
	 * @throws ProtocolException if the required algorithm is not available.
	 */
	private static MessageDigest getDigest()
	throws ProtocolException
	{
		try
		{
			return MessageDigest.getInstance( "MD5" );
		}
		catch ( NoSuchAlgorithmException e )
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
