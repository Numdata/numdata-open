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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.JPanel;

import com.numdata.oss.ResourceBundleTools;

/**
 * This panel is used as a "progress bar" for a ({@link Wizard}). It
 * indicates what page of a {@link CardPanel} is currently active.
 *
 * @see     Wizard
 *
 * @author  Peter S. Heijnen
 */
final class WizardProgressPanel
	extends JPanel
{
	/**
	 * Resource bundle for internationalization.
	 */
	private final ResourceBundle _res;

	/**
	 * Card panel to base the progress indicator on.
	 */
	private final CardPanel _cardPanel;

	/** Total height of component.                            */ private static final int     PROGRESS_HEIGHT         = 29;
	/** Diameter of bubble for each step.                     */ private static final int     PROGRESS_BUBBLESIZE     = 19;
	/** Number of pixels between and after last bubble.       */ private static final int     PROGRESS_SPACING        = 10;
	/** Main background color.                                */ private static final Color   PROGRESS_BACKGROUND     = new Color( 158 , 158 , 158 );
	/** Text color in bubble.                                 */ private static final Color   PROGRESS_FOREGROUND     = Color.black;
	/** Background color of bubble.                           */ private static final Color   PROGRESS_FILL           = Color.white;
	/** Text color in current bubble.                         */ private static final Color   PROGRESS_CUR_FOREGROUND = Color.white;
	/** Background color of current bubble.                   */ private static final Color   PROGRESS_CUR_FILL       = Color.black;

	/**
	 * Construct progress panel.
	 *
	 * @param   locale      Locale for internationalization.
	 * @param   cardPanel   Card panel to base the progress indicator on.
	 */
	WizardProgressPanel( final Locale locale , final CardPanel cardPanel )
	{
		_res = ResourceBundleTools.getBundle( WizardProgressPanel.class , locale );

		setMinimumSize( new Dimension( 1 , PROGRESS_HEIGHT ) );
		setPreferredSize( new Dimension( 1 , PROGRESS_HEIGHT ) );
		setBackground( PROGRESS_BACKGROUND );
		setOpaque( true );

		_cardPanel = cardPanel;
	}

	protected void paintComponent( final Graphics g )
	{
		final CardPanel  cardPanel = _cardPanel;
		final Graphics2D g2d       = (Graphics2D)g;

		g2d.setColor( getBackground() );
		g2d.fillRect( 0 , 0 , getWidth() , getHeight() );

		DrawTools.drawEtchHorizontal( g2d , 0 , 0 , getWidth() );
		DrawTools.drawEtchHorizontal( g2d , 0 , getHeight() - 2 , getWidth() );

		final FontMetrics fm = g2d.getFontMetrics();

		final int pageCount = cardPanel.getPageCount();
		final int stepSize = PROGRESS_SPACING + PROGRESS_BUBBLESIZE;
		      int x  = getWidth() - pageCount * stepSize;
		final int y  = ( getHeight() - PROGRESS_BUBBLESIZE ) >> 1;
		final int ty = y + (( PROGRESS_BUBBLESIZE - fm.getHeight() ) >> 1) + fm.getAscent() + fm.getLeading();

		final RenderingHints oldHints = g2d.getRenderingHints();
		g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING , RenderingHints.VALUE_ANTIALIAS_ON );

		for ( int step = 0 ; step < pageCount ; step++ )
		{
			final String  text     = String.valueOf( step + 1 );
			final int     tx       = x + ( ( PROGRESS_BUBBLESIZE - fm.stringWidth( text ) ) >> 1 );
			final boolean selected = ( step == cardPanel.getActivePageIndex() );

			g2d.setColor( selected ? PROGRESS_CUR_FILL : PROGRESS_FILL );
			g2d.fillOval( x , y , PROGRESS_BUBBLESIZE , PROGRESS_BUBBLESIZE );
			g2d.setColor( selected ? PROGRESS_CUR_FOREGROUND : PROGRESS_FOREGROUND );
			g2d.drawString( text , tx , ty );

			if ( step < pageCount - 1 )
			{
				g2d.setColor( PROGRESS_FOREGROUND );
				g2d.drawLine( x + PROGRESS_BUBBLESIZE - 1 , getHeight() >> 1 , x + stepSize - 1 , getHeight() >> 1 );
			}

			x += stepSize;
		}

		g2d.setRenderingHints( oldHints );
	}

	public String getToolTipText( final MouseEvent event )
	{
		final CardPanel cardPanel = _cardPanel;
		final int       pageCount = cardPanel.getPageCount();
		final int       stepSize  = PROGRESS_SPACING + PROGRESS_BUBBLESIZE;
		final int       x         = event.getX();
//			final int       y         = event.getY();

		String text = null;

		int cx = getWidth() - pageCount * stepSize;
//			int cy  = ( getHeight() - PROGRESS_BUBBLESIZE ) >> 1;
		if ( x < cx )
		{
//				_tooltip.setDelay( -1 );
			text = _res.getString( "progressTooltip" );
		}
		else
		{
			for ( int step = 0 ; step < pageCount ; step++ )
			{
				cx += stepSize;
				if ( x >= cx )
					continue;

// @FIXME Wish to set tooltip display delay, Swing doesn't have that feature!
//					_tooltip.setDelay( 250 );
				final CardPage page = _cardPanel.getPage( step );

				text = MessageFormat.format( _res.getString( "stepTooltip" ) , new Object[] { new Integer( step + 1 ) , new Integer( pageCount ) } ) + ": " + page.getDescription();
				break;
			}
		}

		return ( text == null ) ? super.getToolTipText( event ) : text;
	}
}
