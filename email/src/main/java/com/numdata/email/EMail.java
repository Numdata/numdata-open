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
package com.numdata.email;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * This class represents an E-mail message.
 *
 * @author  Peter S. Heijnen
 */
public final class EMail
	extends EMailPart
{
	/**
	 * Date format used for the 'Message-ID' header.
	 */
	private static final SimpleDateFormat MSGID_DATE_FORMAT =
		new SimpleDateFormat( "yy_MM_dd_mm_ss_SSS" );

	/**
	 * SMPT compatible data format.
	 *
	 * @see	#getSmtpDate
	 */
	private static final SimpleDateFormat SMTP_DATE_FORMAT =
		new SimpleDateFormat( "dd  yy hh:mm " );

	/**
	 * Names of months. Use in combination with SMTP_DATA_FORMAT.
	 *
	 * @see	#getSmtpDate
	 */
	private static final String[] MONTH_NAMES =
		{ "Jan" , "Feb" , "Mar" , "Apr" , "May" , "Jun" ,
		  "Jul" , "Aug" , "Sep" , "Oct" , "Nov" , "Dec" };

	/**
	 * Originator.
	 */
	public String from = "";

	/**
	 * Addresses of recipients.
	 */
	private final List<String> _recipients = new ArrayList<String>();

	/**
	 * Addresses of carbon copies.
	 */
	private final List<String> _carbonCopies = new ArrayList<String>();

	/**
	 * Reply-to address of originator.
	 */
	public String replyTo = "";

	/**
	 * Message subject.
	 */
	public String subject = "";

	/**
	 * Character encoding.
	 */
	public final String charSet = "us-ascii";

	/**
	 * Internet address from which to connect to the SMTP server.
	 */
	public final String smtpMyAddress = "localhost";

	/**
	 * Internet address of SMTP server.
	 */
	public String smtpServer = "localhost";

	/**
	 * SMTP server port.
	 */
	public final int smtpPort = 25;

	/**
	 * Get recipient addresses.
	 *
	 * @return  Iterator for recipients.
	 */
	public final Iterator<String> getRecipients()
	{
		return _recipients.iterator();
	}

	/**
	 * Add recipient address.
	 *
	 * @param   dest	Recipient address to add.
	 */
	public final void addRecipient( final String dest )
	{
		_recipients.add( dest );
	}

	/**
	 * Get carbon copy addresses.
	 *
	 * @return  Iterator for carbon copies.
	 */
	public final Iterator<String> getCarbonCopies()
	{
		return _carbonCopies.iterator();
	}

	/**
	 * Add carbon copy address.
	 *
	 * @param   dest    Carbon copy address to add.
	 */
	public final void addCarbonCopy( final String dest )
	{
		_carbonCopies.add( dest );
	}

	/**
	 * Append E-mail content to the specified buffer. Must have set originator,
	 * recipient(s), and subject.
	 *
	 * @param   sb      Buffer to append the E-mail content to (target).
	 */
	public final void appendContent( final StringBuffer sb )
	{
		if ( ( from == null ) || ( from.length() == 0 ) )
			throw new IllegalArgumentException( "missing 'from'" );

		if ( _recipients.size() < 1 )
			throw new IllegalArgumentException( "missing 'to'" );

		if ( ( subject == null ) || ( subject.length() == 0 ) )
			throw new IllegalArgumentException( "missing 'subject'" );

		/*
		 * Add primary mail header:
		 *  - From:
		 *  - To:
		 *  - Subject:
		 *  - Date:
		 *  - Message-ID:
		 *  - cc:
		 *  - Reply-To:
		 */
		sb.append( "From: " );
		sb.append( from );

		sb.append( "\r\nTo: " );
		for ( int i = 0 ; i < _recipients.size() ; i++ )
		{
			if ( i > 0 ) sb.append( ";\r\n    " );
			sb.append( _recipients.get( i ) );
		}

		sb.append( "\r\nSubject: " );
		sb.append( subject );

		sb.append( "\r\nDate: " );
		sb.append( getSmtpDate() );

		sb.append( "\r\nMessage-ID: " );
		sb.append( getMsgId() );

		for ( int i = 0 ; i < _carbonCopies.size() ; i++ )
		{
			sb.append( ( i == 0 ) ? "\r\ncc: " : ";\r\n    " );
			sb.append( _carbonCopies.get( i ) );
		}

		if ( replyTo != null && replyTo.length() > 0 )
		{
			sb.append( "\r\nReply-To: " );
			sb.append( replyTo );
		}

		sb.append( "\r\nMIME-Version: 1.0" );

		/*
		 * Add parts to content.
		 */
		super.appendContent( sb );
	}

	/**
	 * Get value for 'Message-ID' header of message.
	 *
	 * @return  Value for 'Message-ID' header of message.
	 */
	public final String getMsgId()
	{
		final Calendar calendar = Calendar.getInstance();
		return "SODA_" + MSGID_DATE_FORMAT.format( calendar.getTime() ) + "@" + smtpMyAddress;
	}

	/**
	 * Get current date/time in SMTP format.
	 *
	 * @return  Current data/time in SMTP format.
	 */
	public static String getSmtpDate()
	{
		final Calendar calendar = Calendar.getInstance();
		final String   smtpDate = SMTP_DATE_FORMAT.format( calendar.getTime() );
		final TimeZone timeZone = TimeZone.getDefault();

		return
			smtpDate.substring( 0 , 3 ) +
			MONTH_NAMES[ calendar.get( Calendar.MONTH ) ] +
			smtpDate.substring( 3 ) +
			timeZone.getDisplayName( false , TimeZone.SHORT );
	}

	/**
	 * Convenience method to quickly send an E-mail message.
	 *
	 * @param   from		Originator name / address.
	 * @param   to			Recipient name / address.
	 * @param   subject		Message subject.
	 * @param   mailServer	SMTP server name.
	 * @param   message		Message body.
	 *
	 * @return  {@code true} if successful;
	 *			{@code false} otherwise.
	 */
	public static boolean quickSend( final String from , final String to , final String subject , final String mailServer , final String message )
	{
		final EMail mail = new EMail();

		mail.from = from;
		mail.addRecipient( to );

		mail.subject = subject;

		mail.smtpServer = mailServer;

		mail.addPart( new EMailPart( message ) );

		return mail.send();
	}

	/**
	 * Send E-mail message to the default SMTP server.
	 *
	 * @return  {@code true} if successful;
	 *			{@code false} otherwise.
	 */
	public final boolean send()
	{
		return SMTP.send( this );
	}

}
