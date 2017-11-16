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

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

import com.numdata.oss.log.*;
import com.numdata.oss.net.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * Check box for web forms.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public class FormCheckbox
extends FormField
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FormCheckbox.class );

	/**
	 * Whether to automatically submit the form if the check box state is
	 * changed.
	 */
	private boolean _autoSubmit = false;

	/**
	 * Constructor for check box.
	 *
	 * @param target Target object for this field.
	 */
	public FormCheckbox( final FieldTarget target )
	{
		super( target );
	}

	/**
	 * Get field value as boolean.
	 *
	 * @return Boolean field value.
	 */
	@Nullable
	public Boolean getBooleanValue()
	{
		final String value = getValue();

		return ( value == null ) ? null :
		       "1".equalsIgnoreCase( value ) ||
		       "true".equalsIgnoreCase( value ) ||
		       "on".equalsIgnoreCase( value ) ||
		       "yes".equalsIgnoreCase( value ) ||
		       "y".equalsIgnoreCase( value );
	}

	/**
	 * Set field value as boolean.
	 *
	 * @param value Boolean field value.
	 */
	public void setBooleanValue( final boolean value )
	{
		setValue( Boolean.toString( value ) );
	}

	/**
	 * Set field value as boolean.
	 *
	 * @param value Boolean field value.
	 */
	public void setBooleanValue( final Boolean value )
	{
		setValue( ( value != null ) ? Boolean.toString( value ) : null );
	}

	public boolean isAutoSubmit()
	{
		return _autoSubmit;
	}

	public void setAutoSubmit( final boolean autoSubmit )
	{
		_autoSubmit = autoSubmit;
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		Map<String, String> attributes = null;

		if ( isEditable() )
		{
			iw.print( "<input type=\"hidden\" name=\"hidden_" );
			iw.print( getName() );
			iw.print( "\" value=\"true\">" );

			if ( isAutoSubmit() )
			{
				attributes = Collections.singletonMap( "onchange", "document.forms['" + form.getName() + "'].submit();" );
			}
		}

		final Boolean value = getBooleanValue();
		formFactory.writeCheckbox( contextPath, iw, isEditable(), getName(), ( value != null ) && value, attributes );
	}

	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	{
		final SubmitStatus result;

		if ( isEditable() )
		{
			final String name = getName();
			final boolean checked = request.getParameter( name ) != null;
			if ( checked || request.getParameter( "hidden_" + name ) != null )
			{
				setValue( Boolean.toString( checked ) );
				result = SubmitStatus.SUBMITTED;
			}
			else
			{
				LOG.trace( "Not submitted, because '" + name + "' nor 'hidden_" + name + "' parameter is set" );
				result = SubmitStatus.NOT_SUBMITTED;
			}
		}
		else
		{
			result = SubmitStatus.UNKNOWN;
		}

		return result;
	}

	/**
	 * Returns the query string fragment needed to represent the checkbox.
	 *
	 * @return Query string fragment.
	 */
	public String getQueryString()
	{
		final String name = getName();
		final Boolean booleanValue = getBooleanValue();
		final boolean checked = Boolean.TRUE.equals( booleanValue );
		return UrlTools.urlEncode( ( checked ? "" : "hidden_" ) + name + "=true" );
	}

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		final Boolean value = getBooleanValue();
		out.append( '[' ).append( ( value != null ) && value ? 'v' : ' ' ).append( "] " );
	}
}
