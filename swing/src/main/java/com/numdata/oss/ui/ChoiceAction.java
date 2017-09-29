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

import java.awt.event.*;
import javax.swing.*;

/**
 * A choice action can be used for actions that should be performed when a
 * choice is made. In a UI such a choice is typically shown as a combo box.
 *
 * @author  G.B.M. Rupert
 */
public abstract class ChoiceAction
	extends AbstractAction
{
	/**
	 * Property name.
	 */
	public static final String SELECTED_VALUE = "selectedValue";

	/**
	 * Returns the label for the given value.
	 *
	 * @param   value   Value to get a label for.
	 *
	 * @return  Label for the value.
	 */
	public abstract Object getLabel( Object value );

	/**
	 * Returns the values that can be chosen.
	 *
	 * @return  Choice values.
	 */
	public abstract Object[] getValues();

	/**
	 * Get the value of the currently selected choice.
	 *
	 * @return  The value of the currently selected choice.
	 */
	public abstract Object getSelectedValue();

	/**
	 * Sets the currently selected choice.
	 *
	 * @param   selectedValue   Value to be selected.
	 */
	public abstract void setSelectedValue( Object selectedValue );

	@Override
	public void actionPerformed( final ActionEvent e )
	{
		/* Ignore? */
	}
}
