/*
 * Copyright (c) 2014-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.ui;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Table cell renderer that applies a {@link Format} to cell values. If the
 * format can't be applied to a cell value, the original cell value is used
 * as-is.
 *
 * @author Gerrit Meinders
 */
public class FormatTableCellRenderer
extends DefaultTableCellRenderer
{
	/**
	 * Format to apply.
	 */
	private Format _format;

	/**
	 * Constructs a new instance.
	 *
	 * @param format Format to apply.
	 */
	public FormatTableCellRenderer( final Format format )
	{
		_format = format;
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param format              Format to apply.
	 * @param horizontalAlignment Horizontal alignment.
	 */
	public FormatTableCellRenderer( final Format format, final int horizontalAlignment )
	{
		this( format );
		setHorizontalAlignment( horizontalAlignment );
	}

	@Override
	public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column )
	{
		Object displayValue = value;
		if ( value != null )
		{
			try
			{
				displayValue = _format.format( value );
			}
			catch ( IllegalArgumentException ignored )
			{
			}
		}
		return super.getTableCellRendererComponent( table, displayValue, isSelected, hasFocus, row, column );
	}
}
