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

import com.numdata.oss.*;
import com.numdata.oss.web.*;
import com.numdata.oss.web.HTMLTable.*;
import org.jetbrains.annotations.*;

/**
 * This component defines a new column in a table-based web form.
 *
 * @author Peter S. Heijnen
 */
public class FormColumn
extends FormComponent
{
	/**
	 * Start new table row.
	 */
	private boolean _newRow = false;

	/**
	 * Column defines a header vs. a data column.
	 */
	private boolean _head = false;

	/**
	 * Column span to use (1 or less for unspecified).
	 */
	private int _colspan = 1;

	/**
	 * Horizontal alignment.
	 */
	private HorizontalAlignment _align = HorizontalAlignment.DEFAULT;

	/**
	 * Additional attributes for HTML {@code &lt;td/th&gt;} element.
	 */
	private final Properties _attributes = new Properties();

	/**
	 * Additional attributes for HTML {@code &lt;tr&gt;} element.
	 */
	private final Properties _rowAttributes = new Properties();

	/**
	 * Column text content (optional).
	 */
	private String _text = null;

	/**
	 * Create new instance.
	 */
	public FormColumn()
	{

	}

	/**
	 * Create new instance.
	 *
	 * @param newRow  Create a new table row.
	 * @param head    Column defines a header vs. a data column.
	 * @param colspan Column span to use (1 or less for unspecified).
	 */
	public FormColumn( final boolean newRow, final boolean head, final int colspan )
	{
		_newRow = newRow;
		_head = head;
		_colspan = colspan;
	}

	public boolean isNewRow()
	{
		return _newRow;
	}

	public void setNewRow( final boolean newRow )
	{
		_newRow = newRow;
	}

	public boolean isHead()
	{
		return _head;
	}

	public void setHead( final boolean head )
	{
		_head = head;
	}

	public int getColspan()
	{
		return _colspan;
	}

	public void setColspan( final int colspan )
	{
		_colspan = colspan;
	}

	public HorizontalAlignment getAlign()
	{
		return _align;
	}

	public void setAlign( final HorizontalAlignment align )
	{
		_align = align;
	}

	public Properties getAttributes()
	{
		return new Properties( _attributes );
	}

	public void setAttributes( final Properties attributes )
	{
		PropertyTools.copy( attributes, _attributes );
	}

	/**
	 * Set additional HTML {@code &lt;td/th&gt;} element attribute.
	 *
	 * @param name  Name of attribute of HTML-element to set.
	 * @param value Value of attribute of HTML-element to set.
	 */
	public void setAttributes( final String name, final String value )
	{
		_attributes.setProperty( name, value );
	}

	public Properties getRowAttributes()
	{
		return new Properties( _rowAttributes );
	}

	public void setRowAttributes( final Properties rowAttributes )
	{
		PropertyTools.copy( rowAttributes, _rowAttributes );
	}

	/**
	 * Set additional HTML {@code &lt;tr&gt;} element attribute.
	 *
	 * @param name  Name of attribute of HTML-element to set.
	 * @param value Value of attribute of HTML-element to set.
	 */
	public void setRowAttribute( final String name, final String value )
	{
		_rowAttributes.setProperty( name, value );
	}

	public void setText( final String text )
	{
		_text = text;
	}

	public String getText()
	{
		return _text;
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter out, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		if ( table == null )
		{
			throw new IllegalStateException( "trying to generate column without a table" );
		}

		if ( isNewRow() || isFirst() )
		{
			table.newRow( out, _rowAttributes );
		}

		table.newColumn( out, _head, _colspan, _align, _attributes );

		final String text = getText();
		if ( TextTools.isNonEmpty( text ) )
		{
			HTMLTools.writePlainText( text, out );
		}
	}

	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	{
		return SubmitStatus.UNKNOWN;
	}
}
