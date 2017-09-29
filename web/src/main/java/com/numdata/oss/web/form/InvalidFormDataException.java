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
package com.numdata.oss.web.form;

import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Exception used to indicate errors that occur within web forms.
 *
 * @author S. Bouwman
 */
public class InvalidFormDataException
extends Exception
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 5507478106549962603L;

	/**
	 * Problem type.
	 */
	public enum Type
	{
		/**
		 * Problem type: unspecified problem.
		 */
		UNSPECIFIED,

		/**
		 * Problem type: database error.
		 */
		DATABASE_ERROR,

		/**
		 * Problem type: info missing.
		 */
		INFO_MISSING,

		/**
		 * Problem type: malformed user name.
		 */
		MALFORMED_USER,

		/**
		 * Problem type: malformed password.
		 */
		MALFORMED_PASSWORD,

		/**
		 * Problem type: malformed E-mail address.
		 */
		MALFORMED_EMAIL,

		/**
		 * Problem type: duplicate user detected.
		 */
		DUPE_USER,

		/**
		 * Problem type: failed to store db record.
		 */
		STORE_ERROR,

		/**
		 * Problem type: duplicate record.
		 */
		DUPLICATE_RECORD,

		/**
		 * Problem type: invalid numeric entry.
		 */
		INVALID_NUMBER,

		/**
		 * Problem type: invalid date entry.
		 */
		INVALID_DATE,

		/**
		 * Problem type: invalid data was entered.
		 */
		INVALID_DATA,
	}

	/**
	 * Problem type.
	 */
	private final Type _type;

	/**
	 * Explicit exception title. If no title is set, the title is derived from the
	 * problem type.
	 */
	private final String _title;

	/**
	 * Construct exception to indicate errors from within web forms.
	 *
	 * @param type Problem type (see class comments).
	 */
	public InvalidFormDataException( @NotNull final Type type )
	{
		_type = type;
		_title = null;
	}

	/**
	 * Construct exception to indicate errors from within web forms.
	 *
	 * @param message Detail message.
	 * @param type    Problem type.
	 */
	public InvalidFormDataException( @Nullable final String message, @NotNull final Type type )
	{
		super( message );
		_type = type;
		_title = null;
	}

	/**
	 * Construct exception to indicate errors from within web forms.
	 *
	 * @param type  Problem type.
	 * @param cause Throwable that caused this exception.
	 */
	public InvalidFormDataException( @NotNull final Type type, @Nullable final Throwable cause )
	{
		this( type.name(), type, cause );
	}

	/**
	 * Construct exception to indicate errors from within web forms.
	 *
	 * @param message Detail message.
	 * @param type    Problem type.
	 * @param cause   Throwable that caused this exception.
	 */
	public InvalidFormDataException( @Nullable final String message, @NotNull final Type type, @Nullable final Throwable cause )
	{
		this( null, message, type, cause );
	}

	/**
	 * Construct exception to indicate errors from within web forms.
	 *
	 * @param title   Explicit title.
	 * @param message Detail message.
	 */
	public InvalidFormDataException( @Nullable final String title, @Nullable final String message )
	{
		this( title, message, Type.UNSPECIFIED, null );
	}

	/**
	 * Construct exception to indicate errors from within web forms.
	 *
	 * @param title   Explicit title.
	 * @param message Detail message.
	 * @param cause   Throwable that caused this exception.
	 */
	public InvalidFormDataException( @Nullable final String title, @Nullable final String message, @Nullable  final Throwable cause )
	{
		this( title, message, Type.UNSPECIFIED, cause );
	}

	/**
	 * Construct exception to indicate errors from within web forms.
	 *
	 * @param title   Explicit title.
	 * @param message Detail message.
	 * @param type    Problem type.
	 * @param cause   Throwable that caused this exception.
	 */
	public InvalidFormDataException( @Nullable final String title, @Nullable final String message, @NotNull final Type type, @Nullable final Throwable cause )
	{
		super( message, cause );
		_title = title;
		_type = type;
	}

	/**
	 * Get problem type.
	 *
	 * @return {@link Type}.
	 */
	@NotNull
	public Type getType()
	{
		return _type;
	}


	/**
	 * Get explicit exception title.
	 *
	 * @return Explicit exception title; {@code null} if the title is derived
	 *         from the problem type.
	 */
	@Nullable
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Get localized title.
	 *
	 * @param locale Locale to use.
	 *
	 * @return Title.
	 */
	@NotNull
	public String getLocalizedTitle( @NotNull final Locale locale )
	{
		String result = getTitle();
		if ( result == null )
		{
			final Type type = getType();
			final ResourceBundle bundle = ResourceBundleTools.getBundleHierarchy( getClass(), locale );
			result = ResourceBundleTools.getString( bundle, type.name() + ".title", type.name() );
		}

		return result;
	}

	/**
	 * Get localized message.
	 *
	 * @param locale Locale to use.
	 *
	 * @return Message.
	 */
	@NotNull
	public String getLocalizedMessage( @NotNull final Locale locale )
	{
		String result = getMessage();
		if ( result == null )
		{
			final Type type = getType();
			final ResourceBundle bundle = ResourceBundleTools.getBundleHierarchy( getClass(), locale );
			result = ResourceBundleTools.getString( bundle, type.name() + ".message", type.name() );
		}

		return result;
	}
}
