/*
 * Copyright (c) 2017-2017, Numdata BV, The Netherlands.
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
import javax.accessibility.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

import org.jetbrains.annotations.*;

/**
 * Customized {@link JComboBox} with a wide combo box pop-up.
 * <pre>
 * +----------+
 * | Narrow |V|
 * +----------+------------+
 * |Wide combo box item #1 |
 * |Wide combo box item #2 |
 * |Wide combo box item #3 |
 * |Wide combo box item #4 |
 * +-----------------------+
 * </pre>
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "unused", "WeakerAccess", "rawtypes", "unchecked" } )
public class WidePopupComboBox
extends JComboBox
{
	/**
	 * See {@link JComboBox#JComboBox()}.
	 */
	public WidePopupComboBox()
	{
	}

	/**
	 * See {@link JComboBox#JComboBox(ComboBoxModel)}.
	 *
	 * @param model Combo box model.
	 */
	public WidePopupComboBox( @NotNull final ComboBoxModel model )
	{
		super( model );
	}

	/**
	 * See {@link JComboBox#JComboBox(Object[])}.
	 *
	 * @param items Items to add to combo box
	 */
	public WidePopupComboBox( @NotNull final Object[] items )
	{
		super( items );
	}

	/**
	 * Flag to indicate that we're inside the {@link #doLayout()} method.
	 */
	private boolean _layingOut = false;

	@Override
	public void doLayout()
	{
		try
		{
			_layingOut = true;
			super.doLayout();
		}
		finally
		{
			_layingOut = false;
		}
	}

	/**
	 * The combo box popup width is determined by the combo box editor size,
	 * which is retrieved using this {@link #getSize} method. It seems that this
	 * method is normally only called when the popup is going to be displayed,
	 * so we carelessly override it here to determine the popup size ourselves.
	 *
	 * Tested against Java 1.6 and Java 1.7. All bets are off!
	 *
	 * @inheritdoc
	 */
	@NotNull
	@Override
	public Dimension getSize()
	{
		final Dimension result = super.getSize();
		if ( !_layingOut )
		{
			result.width = Math.max( result.width, getPopupWidth() );
		}

		return result;
	}

	/**
	 * Get width of combo box popup.
	 *
	 * @return Width of combo box popup.
	 */
	protected int getPopupWidth()
	{
		final Dimension preferredSize = getPreferredSize();
		int result = preferredSize.width;

		final Object prototypeValue = getPrototypeDisplayValue();
		if ( prototypeValue != null )
		{
			final ComboBoxModel model = getModel();
			final int modelSize = model.getSize();
			if ( modelSize > 0 )
			{
				final JList popupList = getPopupList();
				if ( popupList != null )
				{
					ListCellRenderer tempRenderer = getRenderer();
					if ( tempRenderer == null )
					{
						tempRenderer = new DefaultListCellRenderer();
					}

					final int prototypeWidth = getDisplayWidth( tempRenderer, popupList, prototypeValue, -1 );

					int maxWidth = 0;
					for ( int i = 0; i < modelSize; i++ )
					{
						maxWidth = Math.max( maxWidth, getDisplayWidth( tempRenderer, popupList, model.getElementAt( i ), i ) );
					}

					result += maxWidth - prototypeWidth;
				}
			}
		}

		return result;
	}

	/**
	 * Get display width of value. The width is taken from the cell renderer
	 * component's preferred size.
	 *
	 * @param cellRenderer Renderer to use.
	 * @param list         Target {@link JList}.
	 * @param value        Value to get width for.
	 * @param index        Index of popup item (-1 = selected item).
	 *
	 * @return Display width.
	 *
	 * @see JComboBox#setRenderer
	 */
	protected int getDisplayWidth( final ListCellRenderer cellRenderer, final JList list, final Object value, final int index )
	{
		final Component component = cellRenderer.getListCellRendererComponent( list, value, index, false, false );
		final Dimension size = component.getPreferredSize();
		return size.width;
	}

	/**
	 * Get the list used by the combo box popup.
	 *
	 * @return {@link JList} for popup; {@code null} if we couldn't find it.
	 */
	@Nullable
	protected JList getPopupList()
	{
		JList result = null;

		final ComboBoxUI comboBoxUI = getUI();
		final int childrenCount = comboBoxUI.getAccessibleChildrenCount( this );
		for ( int i = 0; i < childrenCount; i++ )
		{
			final Accessible child = comboBoxUI.getAccessibleChild( this, i );
			if ( child instanceof ComboPopup )
			{
				result = ( (ComboPopup)child ).getList();
				break;
			}
		}

		return result;
	}
}
