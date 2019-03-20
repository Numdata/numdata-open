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
