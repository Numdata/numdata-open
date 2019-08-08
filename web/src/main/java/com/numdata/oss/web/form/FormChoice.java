/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * Choice box for web forms.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public class FormChoice
extends FormField
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( FormChoice.class );

	/**
	 * Available options for choice.
	 */
	private List<String> _optionValues;

	/**
	 * Descriptions of options for choice.
	 */
	private List<String> _optionLabels;

	/**
	 * If set, automatically submit the form if a selection is made.
	 */
	private boolean _autoSubmit;

	/**
	 * Set value to {@code null} if it is empty.
	 *
	 * @see TextTools#isEmpty
	 */
	private boolean _nullIfEmpty = false;

	/**
	 * Link function. Called by get a link for a option value.
	 */
	@Nullable
	private LinkFunction _linkFunction = null;

	/**
	 * Constructor.
	 *
	 * @param target Target object of field.
	 *
	 * @throws IllegalArgumentException if the option list lengths do not
	 * match.
	 */
	public FormChoice( @NotNull final FieldTarget target )
	{
		super( target );

		_optionValues = new ArrayList<String>();
		_optionLabels = new ArrayList<String>();
		_autoSubmit = false;
	}

	/**
	 * Constructor.
	 *
	 * @param target       Target object of field.
	 * @param optionValues Available options for choice.
	 * @param optionLabels Descriptions of options for choice.
	 */
	public FormChoice( @NotNull final FieldTarget target, @NotNull final String[] optionValues, @NotNull final String[] optionLabels )
	{
		super( target );

		if ( optionValues.length != optionLabels.length )
		{
			throw new IllegalArgumentException( getName() + ": " + optionValues.length + " != " + optionLabels.length );
		}

		_optionValues = new ArrayList<String>( Arrays.asList( optionValues ) );
		_optionLabels = new ArrayList<String>( Arrays.asList( optionLabels ) );
		_autoSubmit = false;
	}

	/**
	 * Constructor.
	 *
	 * @param target       Target object of field.
	 * @param optionValues Available options for choice.
	 * @param optionLabels Descriptions of options for choice.
	 */
	public FormChoice( @NotNull final FieldTarget target, @NotNull final Collection<String> optionValues, @NotNull final Collection<String> optionLabels )
	{
		super( target );

		if ( optionValues.size() != optionLabels.size() )
		{
			throw new IllegalArgumentException( getName() + ": " + optionValues.size() + " != " + optionLabels.size() );
		}

		_optionValues = new ArrayList<String>( optionValues );
		_optionLabels = new ArrayList<String>( optionLabels );
		_autoSubmit = false;
	}

	/**
	 * Constructor.
	 *
	 * @param target    Target object of field.
	 * @param optionMap Available options for choice.
	 */
	public FormChoice( final FieldTarget target, final Map<String, String> optionMap )
	{
		super( target );

		final List<String> optionValues = new ArrayList<String>( optionMap.size() );
		final List<String> optionLabels = new ArrayList<String>( optionMap.size() );

		for ( final Map.Entry<String, String> entry : optionMap.entrySet() )
		{
			optionValues.add( entry.getKey() );
			optionLabels.add( entry.getValue() );
		}

		_optionValues = optionValues;
		_optionLabels = optionLabels;
	}

	public boolean isAutoSubmit()
	{
		return _autoSubmit;
	}

	public void setAutoSubmit( final boolean autoSubmit )
	{
		_autoSubmit = autoSubmit;
	}

	public boolean isNullIfEmpty()
	{
		return _nullIfEmpty;
	}

	public void setNullIfEmpty( final boolean nullIfEmpty )
	{
		_nullIfEmpty = nullIfEmpty;
	}

	public List<String> getOptionValues()
	{
		return Collections.unmodifiableList( _optionValues );
	}

	public void setOptionValues( final Collection<String> optionValues )
	{
		_optionValues = new ArrayList<String>( optionValues );
	}

	public List<String> getOptionLabels()
	{
		return Collections.unmodifiableList( _optionLabels );
	}

	public void setOptionLabels( final Collection<String> optionLabels )
	{
		_optionLabels = new ArrayList<String>( optionLabels );
	}

	/**
	 * Adds an option.
	 *
	 * @param value Value.
	 * @param label Label.
	 */
	public void addOption( @NotNull final String value, @NotNull final String label )
	{
		_optionValues.add( value );
		_optionLabels.add( label );
	}

	@Nullable
	public LinkFunction getLinkFunction()
	{
		return _linkFunction;
	}

	public void setLinkFunction( @Nullable final LinkFunction linkFunction )
	{
		_linkFunction = linkFunction;
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		final boolean editable = isEditable();
		final Map<String, String> attributes = ( editable && isAutoSubmit() ) ? Collections.singletonMap( "onchange", "document.forms['" + form.getName() + "'].submit();" ) : null;

		final String selected = getValue();

		final LinkFunction linkFunction = !editable && ( selected != null ) ? getLinkFunction() : null;
		final String link = ( linkFunction != null ) ? linkFunction.getLink( contextPath, selected ) : null;
		if ( link != null )
		{
			iw.write( "<a href=\"" );
			HTMLTools.escapeAttributeValue( iw, link );
			iw.write( "\">" );
		}

		formFactory.writeChoice( iw, editable, getName(), selected, getOptionValues(), getOptionLabels(), attributes );

		if ( link != null )
		{
			iw.write( "</a>" );
		}
	}

	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	{
		final SubmitStatus result;

		if ( isEditable() )
		{
			final String value = request.getParameter( getName() );
			if ( value != null )
			{
				setValue( ( isNullIfEmpty() && TextTools.isEmpty( value ) ) ? null : value );
				result = SubmitStatus.SUBMITTED;
			}
			else
			{
				LOG.trace( "Not submitted, because '" + getName() + "' parameter is not set" );
				result = SubmitStatus.NOT_SUBMITTED;
			}
		}
		else
		{
			result = SubmitStatus.UNKNOWN;
		}

		return result;
	}

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		final List<String> optionValues = getOptionValues();
		final List<String> optionLabels = getOptionLabels();
		final String value = getValue();
		final int selectedIndex = optionValues.indexOf( value );

		HTMLTools.escapeCharacterData( out, ( selectedIndex < 0 ) ? ( value == null ) ? "" : value : optionLabels.get( selectedIndex ) );

		if ( selectedIndex < 0 )
		{
			out.append( " (unknown value!) " );
		}
	}

	/**
	 * Function to get link for a selected value.
	 */
	public interface LinkFunction
	{
		/**
		 * Returns link for selected value.
		 *
		 * @param contextPath Web application context path.
		 * @param value       Selected value.
		 *
		 * @return Link for selected value; {@code null} if no link is
		 * available.
		 */
		@Nullable
		String getLink( @NotNull final String contextPath, @Nullable final String value );
	}
}
