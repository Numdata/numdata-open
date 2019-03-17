/*
 * (C) Copyright Numdata BV 2019-2019 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.oss.measurement;

import java.beans.*;

import org.jetbrains.annotations.*;

/**
 * Property change event with additional information for measurements.
 *
 * @author Gerrit Meinders
 */
@SuppressWarnings( "serial" )
public class MeasurablePropertyChangeEvent
extends PropertyChangeEvent
{
	/**
	 * User interaction used to make the change.
	 */
	@Nullable
	private InteractionType _interactionType = null;

	/**
	 * Constructs a new instance.
	 * @param source       Source object.
	 * @param propertyName Property name.
	 * @param oldValue     Old property value.
	 * @param newValue     New property value.
	 */
	public MeasurablePropertyChangeEvent( final Object source, final String propertyName, final Object oldValue, final Object newValue )
	{
		super( source, propertyName, oldValue, newValue );
	}

	@Nullable
	public InteractionType getInteractionType()
	{
		return _interactionType;
	}

	public void setInteractionType( @Nullable final InteractionType interactionType )
	{
		_interactionType = interactionType;
	}
}
