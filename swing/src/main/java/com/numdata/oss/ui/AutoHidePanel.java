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
package com.numdata.oss.ui;

import java.awt.*;
import javax.swing.*;

import org.jetbrains.annotations.*;

/**
 * This panel {@link JPanel} is automatically hidden when if contains no
 * visible components.
 *
 * @author  Peter S. Heijnen
 */
public class AutoHidePanel
	extends JPanel
{
	/**
	 * Creates a double-buffered panel with a {@code FlowLayout}.
	 */
	public AutoHidePanel()
	{
	}

	/**
	 * Creates a panel with a {@code FlowLayout} and the specified buffering
	 * strategy.
	 *
	 * @param   doubleBuffered  Enable double-buffering.
	 */
	public AutoHidePanel( final boolean doubleBuffered )
	{
		super( doubleBuffered );
	}

	/**
	 * Creates a double-buffered panel with the specified layout manager.
	 *
	 * @param   layout  Layout manager.
	 */
	public AutoHidePanel( @Nullable final LayoutManager layout )
	{
		super( layout );
	}

	/**
	 * Creates a panel with the specified layout manager and buffering
	 * strategy.
	 *
	 * @param   layout          Layout manager.
	 * @param   doubleBuffered  Enable double-buffering.
	 */
	public AutoHidePanel( @Nullable final LayoutManager layout, final boolean doubleBuffered )
	{
		super( layout, doubleBuffered );
	}

	@Override
	public boolean isVisible()
	{
		boolean result = false;

		if ( super.isVisible() )
		{
			for ( final Component component : getComponents() )
			{
				if ( component.isVisible() )
				{
					result = true;
					break;
				}
			}
		}

		return result;
	}
}
