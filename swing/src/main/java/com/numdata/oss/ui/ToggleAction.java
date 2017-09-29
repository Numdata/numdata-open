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

import java.util.ResourceBundle;

/**
 * A toggle action can be used for actions that should be performed when a
 * toggle is made. In a UI such a toggle is typically shown as a check box.
 *
 * @author  Peter S. Heijnen
 */
public abstract class ToggleAction
	extends BasicAction
{
	/**
	 * Construct action.
	 *
	 * @param   bundle  Resource bundle to get settings from.
	 * @param   key     Resource key to use (also used as action command).
	 *
	 * @see     BasicAction#BasicAction(ResourceBundle,String)
	 */
	protected ToggleAction( final ResourceBundle bundle , final String key )
	{
		super( bundle, key );
	}

	public void run()
	{
		setValue( !getValue() );
	}

	/**
	 * Get toggle value.
	 *
	 * @return  Toggle value.
	 */
	public abstract boolean getValue();

	/**
	 * Set toggle value.
	 *
	 * @param   value   Toggle value.
	 */
	public abstract void setValue( boolean value );
}
