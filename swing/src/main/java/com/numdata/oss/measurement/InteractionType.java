/*
 * Copyright (c) 2019, Numdata BV, The Netherlands.
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
package com.numdata.oss.measurement;

import org.jetbrains.annotations.*;

/**
 * Defines a kind of user interaction, e.g. pressing a button or using a wizard.
 *
 * @author Gerrit Meinders
 */
public class InteractionType
{
	/**
	 * Unknown.
	 */
	public static final InteractionType UNKNOWN = new InteractionType( "unknown" );

	/**
	 * Regular button.
	 */
	public static final InteractionType BUTTON = new InteractionType( "button" );

	/**
	 * Tool bar button.
	 */
	public static final InteractionType TOOL_BAR = new InteractionType( "tool bar" );

	/**
	 * Menu item.
	 */
	public static final InteractionType MENU_ITEM = new InteractionType( "menu item" );

	/**
	 * Accelerator key.
	 */
	public static final InteractionType ACCELERATOR_KEY = new InteractionType( "accelerator key" );

	/**
	 * Property editor.
	 */
	public static final InteractionType PROPERTY_EDITOR = new InteractionType( "property editor" );

	/**
	 * Wizard.
	 */
	public static final InteractionType WIZARD = new InteractionType( "wizard" );

	/**
	 * Name describing the interaction type.
	 */
	@NotNull
	private final String _name;

	/**
	 * Constructs a new instance.
	 *
	 * @param name Name describing the interaction type.
	 */
	public InteractionType( @NotNull final String name )
	{
		_name = name;
	}

	@Override
	public int hashCode()
	{
		return _name.hashCode();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return obj instanceof InteractionType && _name.equals( ( (InteractionType)obj )._name );
	}

	@Override
	public String toString()
	{
		return _name;
	}
}
