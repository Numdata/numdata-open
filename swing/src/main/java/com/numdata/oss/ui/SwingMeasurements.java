/*
 * (C) Copyright Numdata BV 2019-2019 - All Rights Reserved
 *
 * This software may not be used, copied, modified, or distributed in any
 * form without express permission from Numdata BV. Please contact Numdata BV
 * for license information.
 */
package com.numdata.oss.ui;

import java.awt.event.*;
import javax.swing.*;

import com.numdata.oss.measurement.*;
import org.jetbrains.annotations.*;

/**
 * Provides utilities for measuring user interactions in a Swing UI.
 *
 * @author Gerrit Meinders
 */
public class SwingMeasurements
{
	/**
	 * Derives an interaction type from the given action event.
	 *
	 * @param event Action event.
	 *
	 * @return Interaction type.
	 */
	@NotNull
	public static InteractionType getInteractionType( @Nullable final ActionEvent event )
	{
		InteractionType interactionType = InteractionType.UNKNOWN;
		if ( event != null )
		{
			final Object source = event.getSource();
			if ( ( source != null ) && ( source.getClass().getEnclosingClass() != null ) && JToolBar.class.isAssignableFrom( source.getClass().getEnclosingClass() ) )
			{
				interactionType = InteractionType.TOOL_BAR;
			}
			else if ( source instanceof JMenuItem )
			{
				// A horrible hack, but the only reasonably reliable method I could come up with. (Hint: getModifiers() is useless.)
				boolean accelerator = false;
				for ( final StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace() )
				{
					if ( "javax.swing.JMenuBar".equals( stackTraceElement.getClassName() ) &&
					     "processBindingForKeyStrokeRecursive".equals( stackTraceElement.getMethodName() ) )
					{
						accelerator = true;
						break;
					}
				}

				if ( accelerator )
				{
					interactionType = InteractionType.ACCELERATOR_KEY;
				}
				else
				{
					interactionType = InteractionType.MENU_ITEM;
				}
			}
			else if ( source instanceof JButton )
			{
				interactionType = InteractionType.BUTTON;
			}
		}
		return interactionType;
	}

	/**
	 * Not used.
	 */
	private SwingMeasurements()
	{
	}
}
