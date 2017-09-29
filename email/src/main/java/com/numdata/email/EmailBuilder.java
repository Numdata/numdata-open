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
package com.numdata.email;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.*;

import com.numdata.oss.*;
import com.numdata.oss.io.*;
import org.jetbrains.annotations.*;

/**
 * Builder for email messages.
 *
 * @author Peter S. Heijnen
 */
public class EmailBuilder
{
	/**
	 * Email settings. If set to {@code null}, JavaMail defaults are used.
	 */
	private EmailSettings _emailSettings;

	/**
	 * 'from' address.
	 */
	private InternetAddress _from = null;

	/**
	 * 'reply-to' address.
	 */
	private InternetAddress _replyTo = null;

	/**
	 * 'to' recipient.
	 */
	private final List<InternetAddress> _to = new ArrayList<InternetAddress>();

	/**
	 * 'cc' recipient.
	 */
	private final List<InternetAddress> _cc = new ArrayList<InternetAddress>();

	/**
	 * 'bcc' recipient.
	 */
	private final List<InternetAddress> _bcc = new ArrayList<InternetAddress>();

	/**
	 * Message subject.
	 */
	private String _subject = null;

	/**
	 * Message body by subtype. Plain text body should have empty subtype.
	 */
	private final Map<String, String> _body = new LinkedHashMap<String, String>();

	/**
	 * Attachments.
	 */
	private final List<Attachment> _attachments = new ArrayList<Attachment>();

	/**
	 * Constructs a new instance with default settings.
	 */
	public EmailBuilder()
	{
		this( null );
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param emailSettings Email settings to be used ({@code null} to use
	 *                      default settings).
	 */
	public EmailBuilder( @Nullable final EmailSettings emailSettings )
	{
		_emailSettings = emailSettings;
	}

	/**
	 * Get email settings.
	 *
	 * @return Email settings; {@code null} if JavaMail defaults are used.
	 */
	public EmailSettings getEmailSettings()
	{
		return _emailSettings;
	}

	/**
	 * Set email settings.
	 *
	 * @param emailSettings Email settings; {@code null} to use JavaMail
	 *                      defaults.
	 */
	public void setEmailSettings( final EmailSettings emailSettings )
	{
		_emailSettings = emailSettings;
	}

	/**
	 * Build message.
	 *
	 * @throws MessagingException if there was a problem composing or sending
	 * the message.
	 */
	public void send()
	throws MessagingException
	{
		final Session session = createMailSession();
		Transport.send( createMessage( session ) );
	}

	/**
	 * Create message.
	 *
	 * @return Message that was built.
	 *
	 * @throws MessagingException if there was a problem composing the message.
	 */
	public MimeMessage createMessage()
	throws MessagingException
	{
		final Session session = createMailSession();
		return createMessage( session );
	}

	/**
	 * Create message.
	 *
	 * @param session Mail session to use.
	 *
	 * @return Message that was built.
	 *
	 * @throws MessagingException if there was a problem composing the message.
	 */
	public MimeMessage createMessage( final Session session )
	throws MessagingException
	{
		final MimeMessage message = new MimeMessage( session );

		final InternetAddress from = getFrom();
		message.setFrom( from );

		final InternetAddress replyTo = getReplyTo();
		if ( replyTo != null )
		{
			message.setReplyTo( new Address[] { replyTo } );
		}

		final List<InternetAddress> to = getTo();
		if ( !to.isEmpty() )
		{
			message.setRecipients( Message.RecipientType.TO, to.toArray( new Address[ to.size() ] ) );
		}

		final List<InternetAddress> cc = getCC();
		if ( !cc.isEmpty() )
		{
			message.setRecipients( Message.RecipientType.CC, cc.toArray( new Address[ cc.size() ] ) );
		}

		final List<InternetAddress> bcc = getBCC();
		if ( !bcc.isEmpty() )
		{
			message.setRecipients( Message.RecipientType.BCC, bcc.toArray( new Address[ bcc.size() ] ) );
		}

		message.setSubject( getSubject() );

		final Map<String, String> body = _body;
		if ( body.isEmpty() )
		{
			throw new MessagingException( "Message has no body" );
		}

		final MimeMultipart bodyAlternatives = new MimeMultipart( "alternative" );
		for ( final Map.Entry<String, String> entry : body.entrySet() )
		{
			final MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setText( entry.getValue(), "UTF-8", entry.getKey() );
			bodyAlternatives.addBodyPart( bodyPart );
		}

		final List<Attachment> attachments = getAttachments();
		if ( attachments.isEmpty() )
		{
			message.setContent( bodyAlternatives );
		}
		else
		{
			final MimeMultipart bodyAndAttachments = new MimeMultipart( "mixed" );

			final MimeBodyPart bodyPart = new MimeBodyPart();
			bodyPart.setContent( bodyAlternatives );
			bodyAndAttachments.addBodyPart( bodyPart );

			for ( final Attachment attachment : attachments )
			{
				final MimeBodyPart attachmentPart = new MimeBodyPart();

				final DataHandler dataHandler = attachment.getDataHandler();
				if ( dataHandler == null )
				{
					throw new MessagingException( "No data handler set" );
				}

				attachmentPart.setDataHandler( dataHandler );

				final String fileName = attachment.getFileName();
				if ( ( fileName != null ) && !fileName.isEmpty() )
				{
					attachmentPart.setFileName( fileName );
				}

				final String id = attachment.getContentId();
				if ( ( id != null ) && !id.isEmpty() )
				{
					attachmentPart.setHeader( "Content-ID", '<' + id + '>' );
				}

				bodyAndAttachments.addBodyPart( attachmentPart );
			}

			message.setContent( bodyAndAttachments );
		}

		message.setSentDate( new Date() );
		return message;
	}

	/**
	 * Creates a JavaMail session based on the email settings.
	 *
	 * @return JavaMail session.
	 */
	public Session createMailSession()
	{
		return Session.getInstance( getSessionProperties() );
	}

	/**
	 * Get properties to use for mail session from mail settings.
	 *
	 * @return Java mail session properties.
	 */
	public Properties getSessionProperties()
	{
		final Properties result = new Properties();

		final EmailSettings emailSettings = _emailSettings;
		if ( emailSettings != null )
		{
			final String protocol = emailSettings.getProtocol();
			if ( !TextTools.isEmpty( protocol ) )
			{
				result.setProperty( "mail.transport.protocol", protocol );

				if ( "smtp".equals( protocol ) )
				{
					final String timeout = "30000"; // in milliseconds; default is infinite
					result.setProperty( "mail.smtp.connectiontimeout", timeout );
					result.setProperty( "mail.smtp.timeout", timeout );
				}
			}

			final String host = emailSettings.getHost();
			if ( !TextTools.isEmpty( host ) )
			{
				result.setProperty( "mail.host", host );
			}

			final int port = emailSettings.getPort();
			if ( port > 0 )
			{
				result.setProperty( "mail." + emailSettings.getProtocol() + ".port", String.valueOf( port ) );
			}

			final String user = emailSettings.getUser();
			if ( !TextTools.isEmpty( user ) )
			{
				result.setProperty( "mail.user", user );
			}

			final String password = emailSettings.getPassword();
			if ( !TextTools.isEmpty( password ) )
			{
				result.setProperty( "mail.password", password );
			}
		}
		return result;
	}

	/**
	 * Get 'from' address.
	 *
	 * @return 'from' address.
	 */
	public InternetAddress getFrom()
	{
		return _from;
	}

	/**
	 * Set 'from' address.
	 *
	 * @param email Email address.
	 *
	 * @throws AddressException if an invalid address was specified.
	 */
	public void setFrom( final String email )
	throws AddressException
	{
		setFrom( new InternetAddress( email ) );
	}

	/**
	 * Set 'from' address.
	 *
	 * @param email Email address.
	 * @param name  Personal name.
	 *
	 * @throws UnsupportedEncodingException if an invalid address was
	 * specified.
	 */
	public void setFrom( final String email, final String name )
	throws UnsupportedEncodingException
	{
		setFrom( new InternetAddress( email, name ) );
	}

	/**
	 * Set 'from' address.
	 *
	 * @param address 'from' address.
	 */
	public void setFrom( final InternetAddress address )
	{
		_from = address;
	}

	/**
	 * Get message subject.
	 *
	 * @return Message subject.
	 */
	public String getSubject()
	{
		return _subject;
	}

	/**
	 * Set message subject.
	 *
	 * @param subject Message subject.
	 */
	public void setSubject( final String subject )
	{
		_subject = subject;
	}

	/**
	 * Get 'reply-to' address.
	 *
	 * @return 'reply-to' address.
	 */
	public InternetAddress getReplyTo()
	{
		return _replyTo;
	}

	/**
	 * Set 'reply-to' address.
	 *
	 * @param email Email address.
	 *
	 * @throws AddressException if an invalid address was specified.
	 */
	public void setReplyTo( final String email )
	throws AddressException
	{
		setReplyTo( new InternetAddress( email ) );
	}

	/**
	 * Set 'reply-to' address.
	 *
	 * @param email Email address.
	 * @param name  Personal name.
	 *
	 * @throws UnsupportedEncodingException if an invalid address was
	 * specified.
	 */
	public void setReplyTo( final String email, final String name )
	throws UnsupportedEncodingException
	{
		setReplyTo( new InternetAddress( email, name ) );
	}

	/**
	 * Set 'reply-to' address.
	 *
	 * @param address 'reply-to' address.
	 */
	public void setReplyTo( final InternetAddress address )
	{
		_replyTo = address;
	}

	/**
	 * Get addressees of email.
	 *
	 * @return Addressees of email.
	 */
	public List<InternetAddress> getTo()
	{
		return Collections.unmodifiableList( _to );
	}

	/**
	 * Add addressee.
	 *
	 * @param email Email address.
	 *
	 * @throws AddressException if an invalid address was specified.
	 */
	public void addTo( final String email )
	throws AddressException
	{
		_to.add( new InternetAddress( email ) );
	}

	/**
	 * Add addressee.
	 *
	 * @param email Email address.
	 * @param name  Personal name.
	 *
	 * @throws UnsupportedEncodingException if an invalid address was
	 * specified.
	 */
	public void addTo( final String email, final String name )
	throws UnsupportedEncodingException
	{
		_to.add( new InternetAddress( email, name ) );
	}

	/**
	 * Add addressee.
	 *
	 * @param address Address to add.
	 */
	public void addTo( final InternetAddress address )
	{
		_to.add( address );
	}

	/**
	 * Get cc (carbon copy) addresses of email.
	 *
	 * @return CC (carbon copy) addresses of email.
	 */
	public List<InternetAddress> getCC()
	{
		return Collections.unmodifiableList( _cc );
	}

	/**
	 * Add cc (carbon copy) address.
	 *
	 * @param email Email address.
	 *
	 * @throws AddressException if an invalid address was specified.
	 */
	public void addCC( final String email )
	throws AddressException
	{
		_cc.add( new InternetAddress( email ) );
	}

	/**
	 * Add cc (carbon copy) address.
	 *
	 * @param email Email address.
	 * @param name  Personal name.
	 *
	 * @throws UnsupportedEncodingException if an invalid address was
	 * specified.
	 */
	public void addCC( final String email, final String name )
	throws UnsupportedEncodingException
	{
		_cc.add( new InternetAddress( email, name ) );
	}

	/**
	 * Add cc (carbon copy) address.
	 *
	 * @param address Address to add.
	 */
	public void addCC( final InternetAddress address )
	{
		_cc.add( address );
	}

	/**
	 * Get bcc (blind carbon copy) addresses of email.
	 *
	 * @return BCC (blind carbon copy) addresses of email.
	 */
	public List<InternetAddress> getBCC()
	{
		return Collections.unmodifiableList( _bcc );
	}

	/**
	 * Add bcc (blind carbon copy) address.
	 *
	 * @param email Email address.
	 *
	 * @throws AddressException if an invalid address was specified.
	 */
	public void addBCC( final String email )
	throws AddressException
	{
		_bcc.add( new InternetAddress( email ) );
	}

	/**
	 * Add bcc (blind carbon copy) address.
	 *
	 * @param email Email address.
	 * @param name  Personal name.
	 *
	 * @throws UnsupportedEncodingException if an invalid address was
	 * specified.
	 */
	public void addBCC( final String email, final String name )
	throws UnsupportedEncodingException
	{
		_bcc.add( new InternetAddress( email, name ) );
	}

	/**
	 * Add bcc (blind carbon copy) address.
	 *
	 * @param address Address to add.
	 */
	public void addBCC( final InternetAddress address )
	{
		_bcc.add( address );
	}

	/**
	 * Get attachments of message.
	 *
	 * @return Attachments of message.
	 */
	public List<Attachment> getAttachments()
	{
		return Collections.unmodifiableList( _attachments );
	}

	/**
	 * Add attachment to message.
	 *
	 * @param fileName File name of attachment.
	 * @param data     Attachment data.
	 * @param mimeType Mime type of data.
	 */
	public void addAttachment( @NotNull final String fileName, @NotNull final byte[] data, @NotNull final String mimeType )
	{
		final Attachment attachment = new Attachment();
		attachment.setFileName( fileName );
		attachment.setData( data, mimeType );
		_attachments.add( attachment );
	}

	/**
	 * Add attachment to message.
	 *
	 * @param attachment Attachment to add.
	 */
	public void addAttachment( final Attachment attachment )
	{
		_attachments.add( attachment );
	}

	/**
	 * Set plain text message body.
	 *
	 * @param text Plain text message body
	 */
	public void setText( @NotNull final String text )
	{
		setBody( "plain", text );
	}

	/**
	 * Set HTML message body.
	 *
	 * @param text HTML message body
	 */
	public void setHtml( @NotNull final String text )
	{
		setBody( "html", text );
	}

	/**
	 * Set body.
	 *
	 * @param subtype Body subtype (e.g. "plain"=plain, "html"=HTML).
	 * @param text    Body text.
	 */
	public void setBody( @NotNull final String subtype, @NotNull final String text )
	{
		_body.put( subtype, text );
	}

	/**
	 * Email attachment.
	 */
	public static class Attachment
	{
		/**
		 * File name of attachment.
		 */
		private String _fileName = null;

		/**
		 * Content id for attachment, if any.
		 */
		private String _contentId = null;

		/**
		 * Data handler for attachment.
		 */
		private DataHandler _dataHandler = null;

		/**
		 * Get content id for attachment, if any.
		 *
		 * @return Content ID for attachment; {@code null} if attachment has no
		 * content id.
		 */
		@Nullable
		public String getContentId()
		{
			return _contentId;
		}

		/**
		 * Set content id of attachment.
		 *
		 * @param id Content ID of attachment.
		 */
		public void setContentId( final String id )
		{
			_contentId = id;
		}

		/**
		 * Get content id for attachment. Create a new content id if needed.
		 *
		 * @return Content id for attachment.
		 */
		@NotNull
		public String getOrCreateId()
		{
			String result = _contentId;
			if ( result == null )
			{
				result = UUID.randomUUID().toString();
				_contentId = result;
			}
			return result;
		}

		/**
		 * Get 'href' value to use when embedding this attachment (probably an
		 * image) in the email.
		 *
		 * @return 'href' value.
		 */
		public String getEmbeddedHref()
		{
			return "cid:" + getOrCreateId();
		}

		/**
		 * Get file name of attachment.
		 *
		 * @return File name of attachment; {@code null} if attachment has no
		 * file name.
		 */
		public String getFileName()
		{
			return _fileName;
		}

		/**
		 * Set file name of attachment.
		 *
		 * @param fileName File name of attachment; {@code null} if attachment
		 *                 has no file name.
		 */
		public void setFileName( final String fileName )
		{
			_fileName = fileName;
		}

		/**
		 * Get data handler for attachment.
		 *
		 * A data handler is required to provide the attachment data.
		 *
		 * @return data handler for attachment
		 */
		public DataHandler getDataHandler()
		{
			return _dataHandler;
		}

		/**
		 * Set data handler for attachment.
		 *
		 * A data handler is required to provide the attachment data.
		 *
		 * @param dataHandler Data handler for attachment
		 */
		public void setDataHandler( final DataHandler dataHandler )
		{
			_dataHandler = dataHandler;
		}

		/**
		 * Set data source for attachment.
		 *
		 * This is a convenience method to set the data handler based on a
		 * {@link DataSource}.
		 *
		 * @param dataSource Data source for attachment
		 */
		public void setDataSource( final DataSource dataSource )
		{
			setDataHandler( new DataHandler( dataSource ) );
		}

		/**
		 * Set data for attachment.
		 *
		 * This is a convenience method to set the data handler.
		 *
		 * @param data     Attachment data.
		 * @param mimeType Mime type of data.
		 */
		public void setData( final byte[] data, final String mimeType )
		{
			setDataSource( new ByteArrayDataSource( data, mimeType ) );
		}

		/**
		 * Sets the data for the attachment based on the given URL.
		 *
		 * @param source URL to retrieve the data from.
		 *
		 * @throws IOException if an I/O error occurs.
		 */
		public void setData( final URL source )
		throws IOException
		{
			final URLConnection urlConnection = source.openConnection();
			urlConnection.setDoOutput( false );
			urlConnection.setDoInput( true );
			urlConnection.setUseCaches( false );

			final byte[] data = DataStreamTools.readByteArray( urlConnection.getInputStream() );
			setData( data, urlConnection.getContentType() );
		}
	}
}
