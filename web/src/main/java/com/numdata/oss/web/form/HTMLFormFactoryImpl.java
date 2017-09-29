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
import javax.servlet.jsp.*;

import com.numdata.oss.*;
import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * Default implementation of {@link HTMLFormFactory} interface.
 *
 * @author G.B.M. Rupert
 */
public class HTMLFormFactoryImpl
implements HTMLFormFactory
{
	/**
	 * Factory to create HTML tables for forms.
	 */
	@NotNull
	private final HTMLTableFactory _tableFactory;

	/**
	 * Construct form factory.
	 *
	 * @param tableFactory Factory to create HTML tables for forms.
	 */
	public HTMLFormFactoryImpl( @NotNull final HTMLTableFactory tableFactory )
	{
		_tableFactory = tableFactory;
	}

	@NotNull
	@Override
	public HTMLTableFactory getTableFactory()
	{
		return _tableFactory;
	}

	@Override
	public void writeFormPre( @NotNull final String contextPath, @NotNull final JspWriter out, @NotNull final Map<String, String> attributes, @Nullable final String title, @Nullable final HTMLTable table, @NotNull final List<FormButton> buttons )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 1 );
		iw.println( "<div class=\"form\">" );
		iw.indentIn();
		iw.print( "<form" );
		HTMLTools.writeAttributes( iw, attributes );
		iw.println( ">" );
		iw.indentIn();
		if ( title != null )
		{
			iw.print( "<div class=\"title\">" );
			iw.print( title );
			iw.println( "</div>" );
		}

		if ( table != null )
		{
			final String tableClass = ( title == null ) ? "form notitle" : "form";
			table.newTable( iw, PropertyTools.create( "class", tableClass ) );
		}
	}

	@Override
	public void writeFormPost( @NotNull final String contextPath, @NotNull final JspWriter out, @Nullable final HTMLTable table, @NotNull final List<FormButton> buttons )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 1 );

		if ( table != null )
		{
			table.endTable( iw );
		}

		/*
		 * write buttons
		 */
		if ( !buttons.isEmpty() )
		{
			iw.println( "<div class=\"buttons\">" );
			iw.indentIn();

			for ( final FormButton button : buttons )
			{
				button.generate( contextPath, iw, this );
				iw.println();
			}

			iw.indentOut();
			iw.println( "</div>" );
		}

		/*
		 * write form end
		 */
		iw.indentOut();
		iw.println( "</form>" );

		iw.indentOut();
		iw.println( "</div>" );
	}

	@Override
	public void writeLabel( @NotNull final String contextPath, @Nullable final HTMLTable table, @NotNull final JspWriter out, @NotNull final FormLabel formLabel )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 6 );
		final String text = formLabel.getText();
		final boolean editable = formLabel.isEditable();
		final FormField field = formLabel.getField();
		final boolean empty = ( text == null ) || TextTools.isEmpty( text );

		if ( table != null )
		{
			table.newRow( iw );
			table.newColumn( iw, PropertyTools.create( "valign", "baseline" ) );
		}

		if ( !empty )
		{
			HTMLTools.writeText( iw, text );
			iw.print( ":" );
		}

		if ( editable && ( field != null ) && field.isEditable() && field.isRequired() )
		{
			iw.append( " <span class=\"requiredField\">*</span>" );
		}

		if ( table != null )
		{
			final FormComponent next = formLabel.getNext();
			if ( ( next instanceof FormField ) || ( next instanceof FormStaticText ) || ( next instanceof FormContainer ) )
			{
				table.newColumn( iw, PropertyTools.create( "valign", "baseline" ) );
			}
		}

		final String errorMessage = formLabel.getErrorMessage();
		if ( errorMessage != null )
		{
			iw.print( "<p class=\"error\">" );
			iw.print( errorMessage );
			iw.print( "</p>" );
		}
	}

	@Override
	public void writeBackButton( @Nullable final String contextPath, @NotNull final JspWriter out, @NotNull final String text )
	throws IOException
	{
		writeButton( out, "javascript:history.back();", text, null );
	}

	@Override
	public void writeBrowseButton( @NotNull final JspWriter out, @NotNull final String name )
	throws IOException
	{
		out.print( "<input type=\"file\" name=\"" );
		HTMLTools.escapeAttributeValue( out, name );
		out.print( "\">" );
	}

	@Override
	public void writeButton( @NotNull final JspWriter out, @NotNull final String link, @NotNull final String text, @Nullable final Map<String, String> attributes )
	throws IOException
	{
		final Map<String, String> input = new LinkedHashMap<String, String>();
		input.put( "type", "button" );
		input.put( "value", text );
		input.put( "onClick", "parent.location='" + link + '\'' );

		if ( attributes != null )
		{
			input.putAll( attributes );
		}

		out.print( "<input" );
		HTMLTools.writeAttributes( out, input );
		out.print( '>' );
	}


	@Override
	public void writeCheckbox( @NotNull final String contextPath, @NotNull final JspWriter out, final boolean editable, @NotNull final String tag, final boolean value, @Nullable final Map<String, String> attributes )
	throws IOException
	{
		final Map<String, String> input = new LinkedHashMap<String, String>();
		input.put( "type", "checkbox" );
		input.put( "id", tag );
		input.put( "name", tag );

		if ( value )
		{
			input.put( "checked", "" );
		}

		if ( !editable )
		{
			input.put( "disabled", "" ); // 'readonly' would only prevent user from changing the value of the field, not from interacting with the field.
		}

		if ( attributes != null )
		{
			input.putAll( attributes );
		}

		HTMLTools.addClasses( input, "checkbox" );

		out.print( "<input" );
		HTMLTools.writeAttributes( out, input );
		out.print( '>' );
	}

	@Override
	public void writeRadioButton( @Nullable final String contextPath, @NotNull final JspWriter out, final boolean editable, @NotNull final String tag, @NotNull final String value, final boolean checked, @Nullable final Map<String, String> attributes )
	throws IOException
	{
		final Map<String, String> input = new LinkedHashMap<String, String>();
		input.put( "type", "radio" );
		input.put( "class", "radio" );
		input.put( "id", tag );
		input.put( "name", tag );
		input.put( "value", value );

		if ( checked )
		{
			input.put( "checked", "" );
		}

		if ( !editable )
		{
			input.put( "disabled", "" ); // 'readonly' would only prevent user from changing the value of the field, not from interacting with the field.
		}

		if ( attributes != null )
		{
			input.putAll( attributes );
		}

		out.print( "<input" );
		HTMLTools.writeAttributes( out, input );
		out.print( '>' );
	}

	@Override
	public IndentingJspWriter writeChoice( @NotNull final JspWriter out, final boolean editable, @NotNull final String name, @Nullable final String selected, @NotNull final List<String> values, @NotNull final List<String> labels, @Nullable final Map<String, String> attributes )
	throws IOException
	{
		final IndentingJspWriter iw = IndentingJspWriter.create( out, 2, 2 );

		final int selectedIndex = ( selected == null ) ? -1 : ArrayTools.indexOf( selected, values );

		if ( editable )
		{
			final Map<String, String> select = new LinkedHashMap<String, String>();
			select.put( "id", name );
			select.put( "name", name );

			if ( attributes != null )
			{
				select.putAll( attributes );
			}

			iw.print( "<select" );
			HTMLTools.writeAttributes( out, select );
			iw.println( ">" );
			iw.indentIn();

			for ( int index = 0; index < values.size(); index++ )
			{
				final Object value = values.get( index );
				final Object label = labels.get( index );

				iw.print( "<option value=\"" );
				if ( value != null )
				{
					HTMLTools.escapeAttributeValue( iw, value.toString() );
				}
				iw.print( '"' );

				if ( index == selectedIndex )
				{
					iw.print( " selected=\"selected\"" );
				}

				iw.print( '>' );
				if ( label != null )
				{
					String labelString = String.valueOf( label );

					final int newline = labelString.indexOf( '\n' );
					if ( newline > 0 )
					{
						labelString = labelString.substring( 0, newline );
					}

					HTMLTools.writeText( iw, labelString );
				}
				iw.println( "</option>" );
			}

			iw.indentOut();
			iw.println( "</select>" );
		}
		else if ( selectedIndex >= 0 )
		{
			final int savedIndent = suspendIndent( out );
			iw.print( labels.get( selectedIndex ) );
			restoreIndent( out, savedIndent );
		}
		else
		{
			iw.print( ( selected != null ) ? selected : "-" );
		}

		return iw;
	}

	@Override
	public void writeHiddenField( @NotNull final JspWriter out, @NotNull final String name, @NotNull final String value )
	throws IOException
	{
		out.print( "<input type=\"hidden\" name=\"" );
		HTMLTools.escapeAttributeValue( out, name );
		out.print( "\" value=\"" );
		HTMLTools.escapeAttributeValue( out, value );
		out.println( "\">" );
	}

	@Override
	public void writeSubmitButton( @NotNull final JspWriter out, @Nullable final String name, @NotNull final String text, @Nullable final Map<String, String> attributes )
	throws IOException
	{
		final Map<String, String> input = new LinkedHashMap<String, String>();
		input.put( "type", "submit" );
		if ( name != null )
		{
			input.put( "name", name );
		}
		input.put( "value", text );

		if ( attributes != null )
		{
			input.putAll( attributes );
		}

		out.print( "<input" );
		HTMLTools.writeAttributes( out, input );
		out.print( '>' );
	}

	@Override
	public void writeSubmitImage( @NotNull final String contextPath, @NotNull final JspWriter out, @Nullable final String name, @NotNull final String image, @Nullable final Map<String, String> attributes )
	throws IOException
	{
		final Map<String, String> input = new LinkedHashMap<String, String>();
		input.put( "type", "image" );
		if ( ( name != null ) && !name.isEmpty() )
		{
			input.put( "name", name );
		}
		input.put( "src", TextTools.startsWith( image, '/' ) ? contextPath + image : image );
		input.put( "alt", image.substring( image.lastIndexOf( (int)'/' ) + 1 ) );

		if ( attributes != null )
		{
			input.putAll( attributes );
		}

		out.print( "<input" );
		HTMLTools.writeAttributes( out, input );
		out.print( '>' );
	}

	@Override
	public void writeTextArea( @NotNull final JspWriter out, final boolean editable, @NotNull final String tag, @Nullable final String value, final int cols, final int rows, @Nullable final Map<String, String> attributes )
	throws IOException
	{
		if ( editable )
		{
			final Map<String, String> textarea = new LinkedHashMap<String, String>();
			textarea.put( "name", tag );
			if ( cols > 0 )
			{
				textarea.put( "cols", String.valueOf( cols ) );
			}
			if ( rows > 0 )
			{
				textarea.put( "rows", String.valueOf( rows ) );
			}

			if ( attributes != null )
			{
				textarea.putAll( attributes );
			}

			out.print( "<textarea" );
			HTMLTools.writeAttributes( out, textarea );
			out.print( '>' );

			/*
			 * Write value. Make sure the edited value will not be indented.
			 */
			if ( value != null )
			{
				final int savedIndent = suspendIndent( out );
				HTMLTools.writePlainText( value, out );
				restoreIndent( out, savedIndent );
			}

			out.print( "</textarea>" );
		}
		else
		{
			if ( ( value == null ) || TextTools.isEmpty( value ) )
			{
				out.print( "&nbsp;" );
			}
			else
			{
				out.print( "<pre>" );

				final int savedIndent = suspendIndent( out );
				HTMLTools.writePlainText( value, out );
				restoreIndent( out, savedIndent );

				out.print( "</pre>" );
			}
		}
	}

	@Override
	public void writeTextField( @NotNull final JspWriter out, final boolean editable, @NotNull final String tag, @Nullable final String id, @Nullable final String value, final int size, final int maxLength, final boolean password, final boolean disableAutoComplete, final boolean invalid, @Nullable final Map<String, String> attributes )
	throws IOException
	{
		if ( editable )
		{
			final Map<String, String> input = new LinkedHashMap<String, String>();
			input.put( "type", password ? "password" : "text" );
			input.put( "name", tag );

			if ( ( id != null ) && !id.isEmpty() )
			{
				input.put( "id", id );
			}

			if ( size > 0 )
			{
				input.put( "size", String.valueOf( size ) );
			}

			if ( maxLength > 0 )
			{
				input.put( "maxlength", String.valueOf( maxLength ) );
			}

			if ( value != null )
			{
				input.put( "value", String.valueOf( value ) );
			}

			if ( disableAutoComplete )
			{
				input.put( "autocomplete", "off" );
			}

			if ( invalid )
			{
				input.put( "class", "invalid" );
			}

//			if ( attributes != null )
//			{
//				actualAttributes.putAll( attributes );
//			}

			out.print( "<input" );
			HTMLTools.writeAttributes( out, input );
			out.print( '>' );
		}
		else
		{
			if ( ( value == null ) || TextTools.isEmpty( value ) )
			{
				out.print( "&nbsp;" );
			}
			else
			{
				HTMLTools.writeText( out, value );
			}
		}
	}

	@Override
	public void writeTextField( @NotNull final JspWriter out, final boolean editable, @NotNull final String tag, @Nullable final String value, final int size, final int maxLength )
	throws IOException
	{
		writeTextField( out, editable, tag, null, value, size, maxLength, false, false, false, null );
	}

	/**
	 * Temporary suspend indenting of output. The current indenting depth is
	 * saved and temporary reset to zero.
	 *
	 * @param out JSP writer that is being used.
	 *
	 * @return Indent depth that was used (needed to restore indenting by {@link
	 * #restoreIndent}).
	 */
	private static int suspendIndent( final JspWriter out )
	{
		int savedIndent = -1;
		if ( out instanceof IndentingJspWriter )
		{
			savedIndent = ( (IndentingJspWriter)out ).getIndentDepth();
			( (IndentingJspWriter)out ).setIndentDepth( 0 );
		}
		return savedIndent;
	}

	/**
	 * Restore indenting depth after suspending it.
	 *
	 * @param out         JSP writer that is being used.
	 * @param savedIndent Saved indent depth from {@link #suspendIndent}).
	 */
	private static void restoreIndent( final JspWriter out, final int savedIndent )
	{
		if ( out instanceof IndentingJspWriter )
		{
			( (IndentingJspWriter)out ).setIndentDepth( savedIndent );
		}
	}
}
