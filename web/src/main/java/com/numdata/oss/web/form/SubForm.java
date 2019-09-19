/*
 * Copyright (c) 2009-2017, Numdata BV, The Netherlands.
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
 * This class defines a sub-form container.
 *
 * @author Peter S. Heijnen
 */
public class SubForm
extends FormContainer
{
	/**
	 * HTML table factory.
	 */
	private HTMLTableFactory _tableFactory = new HTMLTableFactoryImpl();

	/**
	 * Title for inner table.
	 */
	private String _title = null;

	/**
	 * Attributes for inner {@code <table>} element.
	 */
	private final Properties _tableAttributes = PropertyTools.create( "class", "subform" );

	/**
	 * Construct sub-table container.
	 */
	public SubForm()
	{
	}

	/**
	 * Construct container.
	 *
	 * @param name Name of the field.
	 */
	public SubForm( final String name )
	{
		super( name );
	}

	/**
	 * Constructs a container with the specified name, using either unqualified
	 * or qualified names (see {@link FormComponent#getQualifiedName()}).
	 *
	 * @param name           Name of the container.
	 * @param qualifiedNames {@code true} to use qualified names; {@code false}
	 *                       otherwise.
	 */
	public SubForm( final String name, final boolean qualifiedNames )
	{
		super( name, qualifiedNames );
	}

	/**
	 * Get HTML table factory for inner table.
	 *
	 * @return HTML table factory for inner table.
	 */
	public HTMLTableFactory getTableFactory()
	{
		return _tableFactory;
	}

	/**
	 * Set HTML table factory for inner table.
	 *
	 * @param tableFactory HTML table factory to use for inner table.
	 *
	 * @throws NullPointerException if the argument is {@code null}.
	 */
	public void setTableFactory( @NotNull final HTMLTableFactory tableFactory )
	{
		_tableFactory = tableFactory;
	}

	/**
	 * Get title for inner table.
	 *
	 * @return Title for inner table; {@code null} if inner table has no title.
	 */
	@Nullable
	public String getTitle()
	{
		return _title;
	}

	/**
	 * Set title for inner table.
	 *
	 * @param title Title for inner table, or {@code null} if none.
	 */
	public void setTitle( @Nullable final String title )
	{
		_title = title;
	}

	@Override
	protected void generate( @NotNull final String contextPath, @NotNull final Form form, @Nullable final HTMLTable table, @NotNull final IndentingJspWriter iw, @NotNull final HTMLFormFactory formFactory )
	throws IOException
	{
		if ( ( !isHideIfReadOnly() || isEditable() ) && ( getVisibleComponentCount() > 0 ) )
		{
			final HTMLTable innerTable = _tableFactory.createTable( _title, 3 );
			innerTable.newTable( iw, _tableAttributes );
			super.generate( contextPath, form, innerTable, iw, formFactory );
			innerTable.endTable( iw );
		}
	}
}
