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
 * Records user interface measurements.
 *
 * @author Gerrit Meinders
 */
public interface MeasurementRecorder
{
	/**
	 * Notifies the recorder of a user interaction.
	 *
	 * @param actionCommand   Identifies the action that was performed.
	 * @param interactionType Provides additional information about the user interaction.
	 */
	void recordEvent( @NotNull String actionCommand, @NotNull InteractionType interactionType );
}
