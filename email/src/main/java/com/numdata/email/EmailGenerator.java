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

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;

/**
 * This E-mail generator reads an E-mail template to construct an E-mail
 * instance. The template contains blocks with meta-info (header) and content.
 * It may also contain field references of named objects that must be supplied
 * by the invoker of the generator.
 *
 * Each block in the E-mail template is preceded by a block identifier. This is
 * a line containing the block identifier string surrounded by square brackets
 * (whitespace surrounding these square brackets is ignored). ANY TEXT BEFORE A
 * BLOCK IDENTIFIER IS IGNORED. The block identifier string describes the block
 * type and optionally block parameters.
 *
 * The format of each block depends on the block type, but shall never include a
 * block identifier itself. The following paragraphs describe each of the
 * possible block types.<dl>
 *
 * <dt>[HEADER]</dt><dd>This block defines E-mail header lines. The format is
 * the same as the E-mail native format. A header line should contain no leading
 * whitespace, a field name, a colon, a space, followed by the field value.
 * Lines that do not match these criteria are silently ignored.</dd>
 *
 * <dt>[CONTENT: type] (typically '[CONTENT: text/plain]' or '[CONTENT:
 * text/html]').</dt><dd>This block defines a content block of the generated
 * E-mail message. The content is read line-by-line and inserted into the
 * generated E-mail.</dd>
 *
 * <dd>The content may contain field references using the format
 * [objectName.fieldName]. The objectName must match an object in the
 * namedObjects hashtable that was supplied to the generator. Note that both
 * object name and field name are case sensitive. Fields are retrieved using
 * Java's reflection mechanism.</dd>
 *
 * </dl>
 *
 * @author Peter S. Heijnen
 */
public final class EmailGenerator
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( EmailGenerator.class );

	/**
	 * URL to E-mail template files.
	 */
	private final URL _templateBaseURL;

	/**
	 * Construct E-mail generator for the specified base URL.
	 *
	 * @param templateBaseURL URL to E-mail template files.
	 */
	public EmailGenerator( final URL templateBaseURL )
	{
		_templateBaseURL = templateBaseURL;
	}

	/**
	 * Send an E-mail message from a specified template using the specified tag
	 * objects to fill in tag values that may occur in the template.
	 *
	 * @param code       Template code (normally a filename).
	 * @param tagObjects Values for tag objects to fill in tag values.
	 *
	 * @return {@code true} if E-mail was sent successfully; {@code false} if
	 * there was a problem reading the template or sending the E-mail message.
	 */
	public boolean send( final String code, final Map<String, Object> tagObjects )
	{
		LOG.entering( "send", "code", code, "tagObjects", tagObjects );
		final boolean result;

		final EMail email = generate( code, tagObjects );
		if ( email == null )
		{
			result = false;
			LOG.error( "Sending E-mail template '" + code + "' failed (Template not found)" );
		}
		else
		{
			result = email.send();
			if ( !result )
			{
				LOG.error( "Sending E-mail template '" + code + "' failed (SMTP error)" );
			}
		}

		return LOG.exiting( "sendEmail", result );
	}

	/**
	 * Generate an E-mail message from a specified template using the specified
	 * tag objects to fill in tag values that may occur in the template.
	 *
	 * @param code       Template code (normally a filename).
	 * @param tagObjects Values for tag objects to fill in tag values.
	 *
	 * @return EMail message resulting from template text; {@code null} if there
	 * was a problem reading the template.
	 */
	public EMail generate( final String code, final Map<String, Object> tagObjects )
	{
		LOG.entering( "generate", "code", code, "tagObjects", tagObjects );
		EMail result = null;

		try
		{
			final URL url = new URL( _templateBaseURL, code );

			final String template = TextTools.loadText( url );
			if ( template != null )
			{
				final Map<String, Object> combinedTagObjects = new HashMap<String, Object>( 2 );
				combinedTagObjects.put( "code", code );
				if ( tagObjects != null )
				{
					combinedTagObjects.putAll( tagObjects );
				}

				result = generate( TextTools.tokenize( template, '\n' ), combinedTagObjects );
			}
		}
		catch ( MalformedURLException notRelative )
		{
			/* path was not a valid relative path */
		}

		return LOG.exiting( "generate", result );
	}

	/**
	 * Generate an E-mail message from a specified template using the specified
	 * tag objects to fill in tag values that may occur in the template.
	 *
	 * @param template   Template text as array of string lines (no newlines
	 *                   allowed).
	 * @param tagObjects Values for tag objects to fill in tag values.
	 *
	 * @return EMail message resulting from template text.
	 */
	private static EMail generate( final String[] template, final Map<String, Object> tagObjects )
	{
		final EMail email = new EMail();
		final StringBuffer contentBuffer = new StringBuffer();

		boolean inHeader = false;
		String inContent = null;

		for ( final String line : template )
		{
			/*
			 * Handle block identifiers.
			 */
			final String id = getBlockIdentifier( line );
			if ( id != null )
			{
				boolean idFound = false;
				final String content = inContent;

				if ( "HEADER".equalsIgnoreCase( id ) )
				{
					inHeader = true;
					inContent = null;
					continue;
				}

				if ( id.regionMatches( true, 0, "CONTENT:", 0, 8 ) )
				{
					idFound = true;
					inHeader = false;
					inContent = String.valueOf( TextTools.getTrimmedSubsequence( id, 8, id.length() ) );
				}

				/*
				 * If a block identifier was found, write out the previous content block.
				 * Continue with next line.
				 */
				if ( idFound )
				{
					addEMailPart( email, content, contentBuffer.toString() );
					contentBuffer.setLength( 0 );
					continue;
				}
			}

			/*
			 * If the line contains data, use to set a E-mail header, or append
			 * the line to the current content block. Anything that doesn't fit in
			 * will be silently ignored.
			 */
			if ( inHeader )
			{
				setEMailHeader( email, tagObjects, line );
			}
			else
			{
				appendTaggedString( contentBuffer, tagObjects, line );
				contentBuffer.append( '\n' );
			}
		}

		addEMailPart( email, inContent, contentBuffer.toString() );
		return email;
	}

	/**
	 * Add email part to message.
	 *
	 * @param email   EMail message to add the part to.
	 * @param content Content type / subtype.
	 * @param data    Message part data.
	 */
	private static void addEMailPart( final EMail email, final String content, final String data )
	{
		if ( ( email != null ) && ( content != null ) && ( data != null ) && !data.isEmpty() )
		{
			final EMailPart part = new EMailPart( data );

			final int i = content.indexOf( (int)'/' );
			if ( i > 0 )
			{
				part.contentType = TextTools.getTrimmedSubstring( content, 0, i );
				part.contentSubType = TextTools.getTrimmedSubstring( content, i + 1, content.length() );
			}

			email.addPart( part );
		}
	}

	/**
	 * This method is called by {@link #appendTaggedString} to add a single tag
	 * to the destination buffer. There is no guarantee that the tag is valid.
	 *
	 * A tag comes in the format '{object name}[.{field name}]' (the field name
	 * is optional). The object name is used to find a value object in the
	 * collection of tag objects passed to this method. If a field name is set,
	 * that field must be a public field of the object. If the object is not
	 * found, the tag itself will be appended to the destination buffer.
	 *
	 * @param dest       Destination buffer for text with tags replaced.
	 * @param tagObjects Tag objects to fill in tag values.
	 * @param tag        Source string (with tags).
	 */
	private static void appendTag( final StringBuffer dest, final Map<String, Object> tagObjects, final String tag )
	{
		/*
		 * Ignore any obviously illegal requests.
		 */
		if ( ( tagObjects != null ) && ( tag != null ) && !tag.isEmpty() && ( dest != null ) )
		{
			/*
			 * Parse tag into object and field name.
			 */
			final int i = tag.indexOf( (int)'.' );
			final String objectName = ( i > 0 ) ? tag.substring( 0, i ) : tag;
			final String fieldName = ( i > 0 ) ? tag.substring( i + 1 ) : null;

			/*
			 * Find the named object and if a field name was specified, try to get its
			 * value from the object.
			 */
			final Object target = tagObjects.get( objectName );
			Object value = null;

			if ( target != null )
			{
				if ( fieldName != null )
				{
					try
					{
						final Class<?> targetClass = target.getClass();

						final Field field = targetClass.getField( fieldName );
						value = field.get( target );
						if ( value == null )
						{
							value = "null";
						}
					}
					catch ( Exception e )
					{
						/* ignore reflection errors, use target as value */
					}
				}
				else
				{
					value = target;
				}
			}

			/*
			 * If a value was found, append it; if not, add the tag to the destination buffer.
			 */
			if ( value != null )
			{
				dest.append( value );
			}
			else
			{
				dest.append( '[' );
				dest.append( tag );
				dest.append( ']' );
			}
		}
	}

	/**
	 * This method takes a source line and appends it to the specified
	 * destination buffer while replacing special template tags in the source
	 * string.
	 *
	 * Each line in the template is fed through this method to replace any tags
	 * inside. A tag is recognized by some text between square brackets. A
	 * square bracket in the text may be escaped using a backslash (\).
	 *
	 * @param dest       Destination buffer for text with tags replaced.
	 * @param tagObjects Tag objects to fill in tag values.
	 * @param source     Source string (with tags).
	 */
	private static void appendTaggedString( final StringBuffer dest, final Map<String, Object> tagObjects, final String source )
	{
		final int length = source.length();

		int pos = 0;
		while ( pos < length )
		{
			final char c = source.charAt( pos++ );

			/*
			 * Handle normal and escaped (using backslash) characters.
			 */
			if ( c == '\\' && pos < length )
			{
				dest.append( source.charAt( pos++ ) );
				continue;
			}

			if ( c != '[' )
			{
				dest.append( c );
				continue;
			}

			/*
			 * Handle '[' (possible start of tag)
			 */
			StringBuffer tag = null;
			final int oldPos = pos;

			while ( true )
			{
				final int end = source.indexOf( (int)']', pos );
				if ( end >= pos )
				{
					if ( tag == null )
					{
						tag = new StringBuffer( end - pos );
					}

					if ( end > pos )
					{
						if ( source.charAt( end - 1 ) == '\\' )
						{
							tag.append( source.substring( pos, end - 1 ) );
							tag.append( ']' );
							pos = end + 1;
							continue;
						}

						tag.append( source.substring( pos, end ) );
						pos = end + 1;
					}
				}
				break;
			}

			if ( tag != null )
			{
				final String tagValue = tag.toString();
				appendTag( dest, tagObjects, tagValue.trim() );
			}
			else
			{
				pos = oldPos;
				dest.append( c );
			}
		}
	}

	/**
	 * Get a so-called 'block identifier' from the specified template line. A
	 * block identifier is a strings between square brackets as single content
	 * of a line e.g. '[Header]'. If the line matches the criteria, the string
	 * between brackets is returned (with leading and trailing whitespace
	 * removed).
	 *
	 * @param line Template line to test.
	 *
	 * @return Block identifier string; {@code null} if the line did not match
	 * the criteria for a block identifier.
	 */
	private static String getBlockIdentifier( final String line )
	{
		final int length = line.length();

		/*
		 * Find starting '[' (must be first non-whitespace character of a line.
		 */
		int start = 0;
		while ( true )
		{
			if ( start == length )
			{
				return null;
			}

			final char c = line.charAt( start++ );

			if ( c == '[' )
			{
				break;
			}

			if ( !Character.isWhitespace( c ) )
			{
				return null;
			}
		}

		/*
		 * Ignore whitespace after '['.
		 */
		while ( Character.isWhitespace( line.charAt( start ) ) )
		{
			start++;
		}

		/*
		 * Find ending ']' (must be the last non-whitespace character of a line).
		 */
		int end = length - 1;
		while ( true )
		{
			if ( end <= start )
			{
				return null;
			}

			final char c = line.charAt( end-- );

			if ( c == ']' )
			{
				break;
			}

			if ( !Character.isWhitespace( c ) )
			{
				return null;
			}
		}

		/*
		 * Ignore whitespace before ']'.
		 */
		while ( Character.isWhitespace( line.charAt( end ) ) )
		{
			end--;
		}

		/*
		 * Fail if the is nothing between the brackets.
		 */
		if ( start == end )
		{
			return null;
		}

		/*
		 * Fail if there are any '[' or ']' between the outer ones.
		 */
		for ( int i = start; i <= end; i++ )
		{
			final char c = line.charAt( i );
			if ( c == '[' || c == ']' )
			{
				return null;
			}
		}

		/*
		 * We have a winner! Return the string between the brackets.
		 */
		return line.substring( start, end + 1 );
	}

	/**
	 * Sets a header line for the specified E-mail message. The header line must
	 * be in standard E-mail header format '{field}: {value}' whitespace around
	 * the colon and and the start or end of the line are ignored. Also, header
	 * field names are not case-sensitive.
	 *
	 * @param email      EMail message to modify.
	 * @param tagObjects Tag objects to fill in tag values.
	 * @param header     Header text (with tags).
	 */
	private static void setEMailHeader( final EMail email, final Map<String, Object> tagObjects, final String header )
	{
		/*
		 * Ignore obviously illegal request.
		 */
		if ( ( email != null ) && ( header != null ) && !header.isEmpty() )
		{
			/*
			 * Parse line into field and value (separated by colon).
			 */
			final int i = header.indexOf( (int)':' );
			if ( i >= 0 )
			{
				final String field = TextTools.getTrimmedSubstring( header, 0, i );
				if ( !field.isEmpty() )
				{
					String value = TextTools.getTrimmedSubstring( header, i + 1, header.length() );
					if ( !value.isEmpty() )
					{
						/*
						 * Replace tags in value.
						 */
						final StringBuffer sb = new StringBuffer( value.length() );
						appendTaggedString( sb, tagObjects, value );
						value = sb.toString();

						/*
						 * Set field values.
						 */
						if ( "From".equalsIgnoreCase( field ) )
						{
							email.from = value;
						}
						else if ( "Reply-To".equalsIgnoreCase( field ) )
						{
							email.replyTo = value;
						}
						else if ( "To".equalsIgnoreCase( field ) )
						{
							email.addRecipient( value );
						}
						else if ( "Subject".equalsIgnoreCase( field ) )
						{
							email.subject = value;
						}
						else
						{
							throw new RuntimeException( "Invalid E-mail header field '" + field + "' set to '" + value + "'!" );
						}
					}
				}
			}
		}
	}
}
