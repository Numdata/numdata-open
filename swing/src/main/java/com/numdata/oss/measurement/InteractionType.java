/*
 * (C) Copyright Numdata BV 2019-2019 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
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
