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
import javax.swing.text.*;

/**
 * This {@link JTextField} consumes 'regular' key events, so they will not
 * trigger action in ancestor components.
 *
 * @author Peter S. Heijnen
 */
public class KeyConsumingTextField
extends JTextField
{
	/**
	 * Constructs a new {@code JTextField}. A default model is created, the initial
	 * string is {@code null}, and the number of columns is set to 0.
	 */
	public KeyConsumingTextField()
	{
	}

	/**
	 * Constructs a new {@code JTextField} initialized with the specified text. A
	 * default model is created and the number of columns is 0.
	 *
	 * @param text the text to be displayed, or {@code null}
	 */
	public KeyConsumingTextField( final String text )
	{
		super( text );
	}

	/**
	 * Constructs a new empty {@code JTextField} with the specified number of
	 * columns. A default model is created and the initial string is set to {@code
	 * null}.
	 *
	 * @param columns number of columns to use to calculate the preferred width; if
	 *                columns is set to zero, the preferred width will be whatever
	 *                naturally results from the component implementation
	 */
	public KeyConsumingTextField( final int columns )
	{
		super( columns );
	}

	/**
	 * Constructs a new {@code JTextField} initialized with the specified text and
	 * columns. A default model is created.
	 *
	 * @param text    text to be displayed, or {@code null}
	 * @param columns number of columns to use to calculate the preferred width; if
	 *                columns is set to zero, the preferred width will be whatever
	 *                naturally results from the component implementation
	 */
	public KeyConsumingTextField( final String text, final int columns )
	{
		super( text, columns );
	}

	/**
	 * Constructs a new {@code JTextField} that uses the given text storage model
	 * and the given number of columns. This is the constructor through which the
	 * other constructors feed. If the document is {@code null}, a default model is
	 * created.
	 *
	 * @param doc     text storage to use; if {@code null}, a default will
	 *                be provided by calling the {@code createDefaultModel} method
	 * @param text    initial string to display, or {@code null}
	 * @param columns number of columns to use to calculate the preferred width >=
	 *                0; if {@code columns} is set to zero, the preferred width
	 *                will be whatever naturally results from the component
	 *                implementation
	 *
	 * @throws IllegalArgumentException if {@code columns} < 0
	 */
	public KeyConsumingTextField( final Document doc, final String text, final int columns )
	{
		super( doc, text, columns );
	}

	@Override
	protected boolean processKeyBinding( final KeyStroke ks, final KeyEvent e, final int condition, final boolean pressed )
	{
		return super.processKeyBinding( ks, e, condition, pressed ) || ( e.getModifiers() == 0 || e.getModifiers() == InputEvent.SHIFT_MASK );
	}
}
