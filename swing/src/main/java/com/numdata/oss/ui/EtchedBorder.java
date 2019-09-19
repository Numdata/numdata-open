/*
 * Copyright (c) 2005-2017, Numdata BV, The Netherlands.
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.border.Border;

/**
 * This class offers basically the same functionality as the standard Swing
 * {@link javax.swing.border.EtchedBorder}, but provides more flexible handling
 * of its insets. If insets are larger than 2, the etch will be centered in the
 * inset area; if the inset is smaller, the etch will not be drawn.
 *
 * @author  Peter S. Heijnen
 */
public class EtchedBorder
	implements Border
{
	/**
	 * Raised etched type.
	 */
	public static final int RAISED = 0;

	/**
	 * Lowered etched type.
	 */
	public static final int LOWERED = 1;

	/**
	 * Type of etch ({@link #LOWERED} or {@link #RAISED}).
	 */
	private final int _etchType;

	/**
	 * Color to use for the etched highlight.
	 */
	private final int _top;

	/**
	 * Color to use for the etched shadow.
	 */
	private final int _left;

	/**
	 * Top inset of border.
	 */
	private final int _bottom;

	/**
	 * Left inset of border.
	 */
	private final int _right;

	/**
	 * Bottom inset of border.
	 */
	private final Color _highlight;

	/**
	 * Right inset of border.
	 */
	private final Color _shadow;

	/**
	 * Creates a lowered etched border with insets for a etch around the content
	 * and colors derived from the background color of the component passed into
	 * the {@link #paintBorder} method.
	 */
	public EtchedBorder()
	{
		this( LOWERED , null , null , 2 , 2 , 2 , 2 );
	}

	/**
	 * Creates an lowered etched border with the specified insets and colors
	 * derived from the background color of the component passed into the
	 * {@link #paintBorder} method.
	 *
	 * @param   top         Top inset of border.
	 * @param   left        Left inset of border.
	 * @param   bottom      Bottom inset of border.
	 * @param   right       Right inset of border.
	 */
	public EtchedBorder( final int top , final int left , final int bottom , final int right )
	{
		this( LOWERED , null , null , top , left , bottom , right );
	}

	/**
	 * Creates an etched border with the specified etch-type, highlight color,
	 * shadow color, and insets.
	 *
	 * @param   etchType    Type of etch ({@link #LOWERED} or {@link #RAISED}).
	 * @param   highlight   Color to use for the etched highlight.
	 * @param   shadow      Color to use for the etched shadow.
	 * @param   top         Top inset of border.
	 * @param   left        Left inset of border.
	 * @param   bottom      Bottom inset of border.
	 * @param   right       Right inset of border.
	 */
	public EtchedBorder( final int etchType , final Color highlight , final Color shadow , final int top , final int left , final int bottom , final int right )
	{
		_etchType  = etchType;
		_top       = top;
		_left      = left;
		_bottom    = bottom;
		_right     = right;
		_highlight = highlight;
		_shadow    = shadow;
	}

	public void paintBorder( final Component c , final Graphics g , final int x , final int y , final int width , final int height )
	{
		final Color background = c.getBackground();
		final Color shadow     = ( _shadow != null ) ? _shadow : background.darker();
		final Color highlight  = ( _highlight != null ) ? _highlight : background.brighter();

		final boolean hasTop    = ( _top    >= 2 );
		final boolean hasLeft   = ( _left   >= 2 );
		final boolean hasBottom = ( _bottom >= 2 );
		final boolean hasRight  = ( _right  >= 2 );

		final int x1 = x +          ( hasLeft   ? _left   / 2 - 1 : 0 );
		final int y1 = y +          ( hasTop    ? _top    / 2 - 1 : 0 );
		final int x2 = x + width  - ( hasRight  ? _right  / 2     : 1 );
		final int y2 = y + height - ( hasBottom ? _bottom / 2     : 1 );

		g.setColor( ( _etchType == LOWERED ) ? shadow : highlight );

		if ( hasTop && hasLeft && hasBottom && hasRight )
		{
			g.drawRect( x1 , y1 , x2 - x1 - 1 , y2 - y1 - 1 );
		}
		else
		{
			if ( hasTop    ) g.drawLine( x1 , y1 , hasRight ? x2 - 1 : x2 , y1 );
			if ( hasLeft   ) g.drawLine( x1 , y1 , x1 , hasBottom ? y2 - 1 : y2 );
			if ( hasBottom ) g.drawLine( x1 , y2 - 1 , hasRight ? x2 - 1 : x2 , y2 - 1 );
			if ( hasRight  ) g.drawLine( x2 - 1 , y1 , x2 - 1 , hasBottom ? y2 - 1 : y2 );
		}

		g.setColor( ( _etchType == LOWERED ) ? highlight : shadow );

		if ( hasTop    ) g.drawLine( hasLeft ? x1 + 1 : x1 , y1 + 1 , hasRight ? x2 - 2 : x2 , y1 + 1 );
		if ( hasLeft   ) g.drawLine( x1 + 1 , hasTop ? y1 + 1 : y1 , x1 + 1 , hasBottom ? y2 - 2 : y2 );
		if ( hasBottom ) g.drawLine( x1 , y2 , x2 , y2 );
		if ( hasRight  ) g.drawLine( x2 , y1 , x2 , y2 );
	}

	public Insets getBorderInsets( final Component c )
	{
		return new Insets( _top, _left, _bottom, _right );
	}

	public boolean isBorderOpaque()
	{
		return ( ( ( _top    == 0 ) || ( _top    == 2 ) )
		         && ( ( _left   == 0 ) || ( _left   == 2 ) )
		         && ( ( _bottom == 0 ) || ( _bottom == 2 ) )
		         && ( ( _right  == 0 ) || ( _right  == 2 ) ) );
	}
}
