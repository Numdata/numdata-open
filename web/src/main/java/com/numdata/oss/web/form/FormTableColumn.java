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
 * This component can be used to start new columns/rows in table-based forms.
 *
 * @author Peter S. Heijnen
 */
public class FormTableColumn
extends FormComponent
{
	/**
	 * Start new row before starting column.
	 */
	private boolean _newRow = true;

	/**
	 * Extra attributes for HTML row element.
	 */
	@Nullable
	private Properties _rowAttributes = null;

	/**
	 * Create header vs. data column.
	 */
	private boolean _head = false;

	/**
	 * Column span to use (1 or less for unspecified).
	 */
	private int _colspan = 1;

	/**
	 * Horizontal alignment.
	 */
	@NotNull
	private HorizontalAlignment _align = HorizontalAlignment.DEFAULT;

	/**
	 * Extra attributes for HTML column element.
	 */
	@Nullable
	private Properties _columnAttributes = null;

	/**
	 * Constructs component to create new table row / column.
	 */
	public FormTableColumn()
	{
		this( true );
	}

	/**
	 * Constructs component to create new table row / column.
	 *
	 * @param newRow Start new row before starting column.
	 */
	public FormTableColumn( final boolean newRow )
	{
		_newRow = newRow;
	}

	/**
	 * Get horizontal column alignment.
	 *
	 * @return Horizontal column alignment.
	 */
	@NotNull
	public HorizontalAlignment getAlign()
	{
		return _align;
	}

	/**
	 * Set horizontal column alignment.
	 *
	 * @param align Horizontal column alignment.
	 */
	public void setAlign( @NotNull final HorizontalAlignment align )
	{
		_align = align;
	}

	/**
	 * Extra attributes for HTML column element.
	 *
	 * @return Extra attributes for HTML column element.
	 */
	@NotNull
	public Properties getColumnAttributes()
	{
		final Properties result = new Properties();
		if ( _columnAttributes != null )
		{
			PropertyTools.copy( _columnAttributes, result );
		}
		return result;
	}

	/**
	 * Set extra attributes for HTML column element.
	 *
	 * @param columnAttributes Extra attributes for HTML column element.
	 */
	public void setColumnAttributes( @NotNull final Properties columnAttributes )
	{
		final Properties privateCopy = new Properties();
		PropertyTools.copy( columnAttributes, privateCopy );
		_columnAttributes = privateCopy;
	}

	/**
	 * Get column span used.
	 *
	 * @return Column span (1 or less for unspecified).
	 */
	public int getColspan()
	{
		return _colspan;
	}

	/**
	 * Set column span to use.
	 *
	 * @param colspan Column span to use (1 or less for unspecified).
	 */
	public void setColspan( final int colspan )
	{
		_colspan = colspan;
	}

	/**
	 * Get type of column that is created.
	 *
	 * @return {@code true} if a header column is created; {@code false} if a
	 * data column is created.
	 */
	public boolean isHead()
	{
		return _head;
	}

	/**
	 * Set type of column to created.
	 *
	 * @param head {@code true} to create a header column; {@code false} to
	 *             create a data column.
	 */
	public void setHead( final boolean head )
	{
		_head = head;
	}

	/**
	 * Get new row option.
	 *
	 * @return {@code true} if a new row is started before the column; {@code
	 * false} otherwise (do not start new row).
	 */
	public boolean isNewRow()
	{
		return _newRow;
	}

	/**
	 * Set new row option.
	 *
	 * @param newRow {@code true} to start a new row before the column; {@code
	 *               false} otherwise (do not start new row).
	 */
	public void setNewRow( final boolean newRow )
	{
		_newRow = newRow;
	}

	/**
	 * Extra attributes for HTML row element.
	 *
	 * @return Extra attributes for HTML row element.
	 */
	@NotNull
	public Properties getRowAttributes()
	{
		final Properties result = new Properties();
		if ( _rowAttributes != null )
		{
			PropertyTools.copy( _rowAttributes, result );
		}
		return result;
	}

	/**
	 * Set extra attributes for HTML row element.
	 *
	 * @param rowAttributes Extra attributes for HTML row element.
	 */
	public void setRowAttributes( @NotNull final Properties rowAttributes )
	{
		final Properties privateCopy = new Properties();
		PropertyTools.copy( rowAttributes, privateCopy );
		_rowAttributes = privateCopy;
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		if ( table != null )
		{
			if ( _newRow )
			{
				table.newRow( iw, _rowAttributes );
			}

			table.newColumn( iw, _head, _colspan, _align, _columnAttributes );
		}
	}

	@NotNull
	@Override
	public SubmitStatus submitData( @NotNull final HttpServletRequest request )
	{
		return SubmitStatus.UNKNOWN;
	}
}