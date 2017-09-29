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

import com.numdata.oss.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * A form section is a logical section of a form that can contain form fields.
 * It allows visual grouping of fields an offers an optional section title.
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public class FormSection
extends FormContainer
{
	/**
	 * Title of section (or null if nameless section).
	 */
	private String _title;

	/**
	 * HTML text to include to mark required items.
	 */
	public static final String REQUIRED_TAG = "<span class=\"requiredField\">*</span>";

	/**
	 * Constructor for nameless section.
	 */
	public FormSection()
	{
		this( null );
	}

	/**
	 * Constructor for section with title.
	 *
	 * @param title Title of section (or null if nameless section).
	 */
	public FormSection( @Nullable final String title )
	{
		this( null, false, title );
	}

	/**
	 * Constructs a container with the specified name, using either unqualified
	 * or qualified names (see {@link FormComponent#getQualifiedName()}).
	 *
	 * @param name           Name of the container.
	 * @param qualifiedNames {@code true} to use qualified names; {@code false}
	 *                       otherwise.
	 * @param title          Title of section (or null if nameless section).
	 */
	public FormSection( @Nullable final String name, final boolean qualifiedNames, @Nullable final String title )
	{
		super( name, qualifiedNames );

		_title = title;
	}

	/**
	 * Get title of section.
	 *
	 * @return Title of section; {@code null} if section has no title.
	 */
	@Nullable
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Set title of section.
	 *
	 * @param title Title of section ({@code null} => no title).
	 */
	public void setTitle( @Nullable final String title )
	{
		_title = title;
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		if ( !isHideIfReadOnly() || isEditable() )
		{
			/*
			 * Add separator to table if this is not the first component in the
			 * parent container.
			 */
			if ( ( table != null ) && !isFirstContent() )
			{
				table.writeSeparator( iw );
			}

			/*
			 * Add title row to table if we have one.
			 */
			if ( TextTools.isNonEmpty( _title ) )
			{
				// write title row
				if ( table != null )
				{
					table.newRow( iw );
					table.newColumn( iw, 2 );
				}
				iw.print( "<b>" );
				iw.print( _title );
				iw.print( "</b>" );
			}

			/*
			 * Generate output for all components in this container.
			 */
			super.generate( contextPath, form, table, iw, formFactory );

			/*
			 * If this section contains a required editable field, add a message
			 * about this.
			 */
			if ( isEditable() && containsRequiredField() )
			{
				if ( table != null )
				{
					table.newRow( iw );
					table.newColumn( iw, 2 );
				}
				iw.print( "<p class=\"requiredFieldHelp\">" );
				iw.print( REQUIRED_TAG );
				iw.print( ' ' );
				iw.print( ResourceBundleTools.getString( form.getLocale(), FormSection.class, "required" ) );
				iw.print( "</p>" );
			}
		}
	}

	/**
	 * Test whether this component is the first component with content in the
	 * parent container.
	 *
	 * @return {@code true} if this is the first component with content.
	 */
	private boolean isFirstContent()
	{
		boolean result = true;

		final FormContainer parent = getParent();
		final int componentIndex = parent.getComponentIndex( this );
		for ( int i = 0; i < componentIndex; i++ )
		{
			if ( !( parent.getComponent( i ) instanceof FormHiddenField ) )
			{
				result = false;
				break;
			}
		}

		return result;
	}

	/**
	 * Test if this container or any of its descendants contains one or more
	 * required field(s).
	 *
	 * @return {@code true} if a required field was found; {@code false}
	 * otherwise.
	 */
	public boolean containsRequiredField()
	{
		boolean result = false;

		LinkedList<FormContainer> todo = null;
		do
		{
			final FormContainer container = ( todo == null ) ? this : todo.removeFirst();

			for ( int i = 0; i < container.getComponentCount(); i++ )
			{
				final FormComponent component = container.getComponent( i );

				if ( ( component instanceof FormField ) )
				{
					if ( ( (FormField)component ).isRequired() )
					{
						result = true;
						break;
					}
				}
				else if ( component instanceof FormContainer )
				{
					if ( todo == null )
					{
						todo = new LinkedList<FormContainer>();
					}

					todo.add( (FormContainer)component );
				}
			}
		}
		while ( ( todo != null ) && !todo.isEmpty() );

		return result;
	}

	@Override
	public void writeAsText( @NotNull final Appendable out, @NotNull final String indent )
	throws IOException
	{
		final String title = getTitle();
		if ( title != null )
		{
			out.append( '\n' ).append( indent ).append( "[ " ).append( title ).append( " ]" );
		}
		super.writeAsText( out, indent );
	}
}
