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
package com.numdata.oss.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class to access Flick Web Service. Only for demo purposes. Code copied from
 * Sun's web site.
 *
 * @noinspection JavaDoc
 */
public class FlickrWebClient
	extends DefaultHandler
{
	public static final String KEY = "339db1433e5f6f11f3ad54135e6c07a9";
	private static final String IMAGE_URL = "http://static.flickr.com";
	private static final String SEARCH_URL = "http://api.flickr.com/services/rest/?method=flickr.photos.search";

	public static List<ImageInfo> searchImages( final String text , final int maxImages  )
	{
		List<ImageInfo> result = null;

		String encodedText;
		try
		{
			encodedText = URLEncoder.encode( text , "UTF-8" );
		}
		catch ( UnsupportedEncodingException ex )
		{
			ex.printStackTrace();
			encodedText = text;
		}

		InputStream is = null;
		try
		{
			final URL url = new URL( SEARCH_URL + "&api_key=" + KEY + "&per_page=" + String.valueOf( maxImages ) + "&text=" + encodedText );
			is = url.openStream();
			result = parseImageInfo( is );
		}
		catch ( MalformedURLException mfe )
		{
			mfe.printStackTrace();
		}
		catch ( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{
			if ( is != null )
			{
				try
				{
					is.close();
				}
				catch ( IOException e )
				{
					/* silently ignore , we don't want to obscure other exceptions */
				}
			}
		}

		return result;
	}

	public static Icon retrieveThumbNail( final ImageInfo info )
	{
		ImageIcon result;

		try
		{
			final URL imgUrl = new URL( IMAGE_URL + "/" + info.getServer() + "/" + info.getId() + "_" + info.getSecret() + "_s.jpg" );
			result = new ImageIcon( imgUrl );
		}
		catch ( MalformedURLException ex )
		{
			ex.printStackTrace();
			result = null;
		}

		return result;
	}

	/**
	 * The image service returns an XML document that contains information about
	 * the matched images. Parse that document to extract that information and
	 * create a list of ImageInfo objects.
	 */
	private static List<ImageInfo> parseImageInfo( final InputStream is )
	{
		final List<ImageInfo> result = new ArrayList<ImageInfo>();

		final DefaultHandler handler = new DefaultHandler()
			{
				public void startElement( final String uri, final String localName, final String qName, final Attributes attributes )
					throws SAXException
				{
					if ( "rsp".equals( qName ) )
					{
						final String status = attributes.getValue( "stat" );
						if ( !"ok".equalsIgnoreCase( status ) )
						{
							throw new SAXException( "Response Error" );
						}
					}
					else if ( "photo".equals( qName ) )
					{
						final String id = attributes.getValue( "id" );
						final String owner = attributes.getValue( "owner" );
						final String secret = attributes.getValue( "secret" );
						final String server = attributes.getValue( "server" );
						final String title = attributes.getValue( "title" );
						final boolean bPublic = "1".equals( attributes.getValue( "ispublic" ) );
						final boolean bFriend = "1".equals( attributes.getValue( "isfriend" ) );
						final boolean bFamily = "1".equals( attributes.getValue( "isfamily" ) );

						result.add( new ImageInfo( id , owner , secret , server , title , bPublic , bFriend , bFamily ) );
					}
				}
			};

		try
		{
			final SAXParserFactory factory = SAXParserFactory.newInstance();
			final SAXParser saxParser = factory.newSAXParser();
			saxParser.parse( is, handler );
		}
		catch ( ParserConfigurationException ex )
		{
			ex.printStackTrace();
		}
		catch ( SAXException ex )
		{
			ex.printStackTrace();
		}
		catch ( IOException ex )
		{
			ex.printStackTrace();
		}
		return result;
	}


	/**
	 * Information about an image from the Flickr service.
	 *
	 * @noinspection JavaDoc
	 */
	public static class ImageInfo
		implements Comparable<ImageInfo>
	{
		private String _id = null;

		private String _owner = null;

		private String _secret = null;

		private String _server = null;

		private String _title = null;

		private boolean _bPublic = false;

		private boolean _bFriend = false;

		private boolean _bFamily = false;

		private Icon _thumbnail = null;

		long _lastRequested = 0L;

		public int compareTo( final ImageInfo o )
		{
			return ( _lastRequested > o._lastRequested ) ? -1 : ( _lastRequested < o._lastRequested ) ?  1 : 0;
		}

		public void request()
		{
			_lastRequested = System.nanoTime();
		}

		public ImageInfo()
		{
		}

		public ImageInfo( final String id, final String owner, final String secret, final String server, final String title, final boolean bPublic, final boolean bFriend, final boolean bFamily )
		{
			this( id, owner, secret, server, title, bPublic, bFriend, bFamily, null );
		}

		public ImageInfo( final String id, final String owner, final String secret, final String server,
		                  final String title, final boolean bPublic, final boolean bFriend, final boolean bFamily,
		                  final Icon thumbnail )
		{
			_id = id;
			_owner = owner;
			_secret = secret;
			_server = server;
			_title = title;
			_bPublic = bPublic;
			_bFriend = bFriend;
			_bFamily = bFamily;
			_thumbnail = thumbnail;
		}

		public void setId( final String id )
		{
			_id = id;
		}

		public String getId()
		{
			return _id;
		}

		public void setOwner( final String owner )
		{
			_owner = owner;
		}

		public String getOwner()
		{
			return _owner;
		}

		public void setSecret( final String secret )
		{
			_secret = secret;
		}

		public String getSecret()
		{
			return _secret;
		}

		public void setServer( final String server )
		{
			_server = server;
		}

		public String getServer()
		{
			return _server;
		}

		public void setTitle( final String title )
		{
			_title = title;
		}

		public String getTitle()
		{
			return _title;
		}

		public boolean isFamily()
		{
			return _bFamily;
		}

		public void setFamily( final boolean bFamily )
		{
			_bFamily = bFamily;
		}

		public boolean isFriend()
		{
			return _bFriend;
		}

		public void setFriend( final boolean bFriend )
		{
			_bFriend = bFriend;
		}

		public boolean isPublic()
		{
			return _bPublic;
		}

		public void setPublic( final boolean bPublic )
		{
			_bPublic = bPublic;
		}


		public void setThumbnail( final Icon thumbnail )
		{
			_thumbnail = thumbnail;
		}

		public Icon getThumbnail()
		{
			return _thumbnail;
		}

	}
}
