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
import javax.swing.border.*;

/**
 * This class decorates a component with a border depending on whether the
 * component or one of its descendants has focus.
 *
 * @author Peter S. Heijnen
 */
public class FocusBorderDecorator
	extends FocusContainerMonitor
	implements FocusListener
{
	/**
	 * Component to decorate.
	 */
	private final JComponent _targetComponent;

	/**
	 * Border to use if neither the target component nor any of its
	 * descendants has focus.
	 */
	private Border _regularBorder = null;

	/**
	 * Border to use if the target component or one of its descendants has
	 * focus.
	 */
	private Border _focusBorder = null;

	/**
	 * Construct decorator.
	 *
	 * @param   targetComponent     Component to decorate.
	 */
	public FocusBorderDecorator( final JComponent targetComponent )
	{
		this( targetComponent, targetComponent.getBorder(), targetComponent.getBorder() );
	}

	/**
	 * Construct decorator.
	 *
	 * @param   targetComponent     Component to decorate.
	 * @param   regularBorder       Border to use if neither the target
	 *                              component nor any of its descendants has
	 *                              focus.
	 * @param   focusBorder         Border to use if the target component or
	 *                              one of its descendants has focus.
	 */
	public FocusBorderDecorator( final JComponent targetComponent, final Border regularBorder, final Border focusBorder )
	{
		init( targetComponent, this );

		_targetComponent = targetComponent;
		_regularBorder = regularBorder;
		_focusBorder = focusBorder;
		updateDecoration();
	}

	/**
	 * Determine whether the target component contains focus or not.
	 *
	 * @return  {@code true} if the target component or one of its descendants
	 *          has focus.
	 */
	protected boolean isFocused()
	{
		return FocusContainerMonitor.containsFocus( _targetComponent );
	}

	/**
	 * Update decoration of the target component based on the current keyboard
	 * focus of the target component.
	 */
	protected void updateDecoration()
	{
		_targetComponent.setBorder( isFocused() ? _focusBorder : _regularBorder );
	}

	/**
	 * Get border used if the target component or one of its descendants has
	 * focus.
	 *
	 * @return  Border used if the target component or one of its
	 *          descendants has focus.
	 */
	public Border getFocusBorder()
	{
		return _focusBorder;
	}

	/**
	 * Set border to use if the target component or one of its descendants
	 * has focus.
	 *
	 * @param   border  Border to use if the target component or one of its
	 *                  descendants has focus.
	 */
	public void setFocusBorder( final Border border )
	{
		_focusBorder = border;
		updateDecoration();
	}

	/**
	 * Get border used if neither the target component nor any of its
	 * descendants has focus.
	 *
	 * @return  Border used if neither the target component nor any of its
	 *          descendants has focus.
	 */
	public Border getRegularBorder()
	{
		return _regularBorder;
	}

	/**
	 * Set border to use if neither the target component nor any of its
	 * descendants has focus.
	 *
	 * @param   border  Border to use if neither the target component nor
	 *                  any of its descendants has focus.
	 */
	public void setRegularBorder( final Border border )
	{
		_regularBorder = border;
		updateDecoration();
	}

	@Override
	public void focusGained( final FocusEvent e )
	{
		updateDecoration();
	}

	@Override
	public void focusLost( final FocusEvent e )
	{
		updateDecoration();
	}
}
