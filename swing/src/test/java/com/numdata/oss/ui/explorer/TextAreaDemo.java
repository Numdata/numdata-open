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
package com.numdata.oss.ui.explorer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

import com.numdata.oss.ui.*;

/**
 * Simple demonstration of {@link TextArea}.
 *
 * @author  Peter S. Heijnen
 */
public class TextAreaDemo
	extends JPanel
{
	private static final Locale LOCALE = Locale.ENGLISH;

	private static final String TEXT1 = "Resize to see wrapping/clipping behavior!";
	private static final String TEXT2 = "This is single line of text which is quite long, but will not wrap. It can only be clipped. Below this line we add a small vertical gap of 10 pixels to offset the paragraph below it a bit.";
	private static final String TEXT3 = "This is a longer piece of text appended as a paragraph, so it should be nicely wrapped to fit the area until the end of the area is reached, where it may be clipped.";

	private static final Font FONT1 = new Font( "dialog", Font.BOLD, 16 );
	private static final Font FONT2 = new Font( "dialog", Font.ITALIC, 12 );
	private static final Font FONT3 = new Font( "dialog", Font.PLAIN, 14 );

	private static final Color COLOR1 = Color.BLUE;
	private static final Color COLOR2 = Color.BLACK;
	private static final Color COLOR3 = Color.BLACK;

	/**
	 * Horizontal alignment of text area.
	 */
	private int _horizontalAlignment = SwingConstants.LEFT;

	/**
	 * Vertical alignment of the text area.
	 */
	private int _verticalAlignment = SwingConstants.TOP;

	/**
	 * Run application.
	 *
	 * @param   args    Command-line arguments.
	 */
	public static void main( final String[] args )
	{
		SwingUtilities.invokeLater( new Runnable()
		{
			@Override
			public void run()
			{
				new TextAreaDemo();
			}
		} );
	}

	/**
	 * Construct application.
	 */
	private TextAreaDemo()
	{
		final TextAreaPane textAreaPane = new TextAreaPane();

		final JComboBox horizontalCombo = new JComboBox( new Object[] { "Left", "Center", "Right" } );
		horizontalCombo.setSelectedIndex( 0 );
		horizontalCombo.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				final int i = horizontalCombo.getSelectedIndex();
				_horizontalAlignment = ( i == 0 ) ? SwingConstants.LEFT : ( i == 1 ) ? SwingConstants.CENTER : SwingConstants.RIGHT;
				textAreaPane.repaint();
			}
		} );

		final JComboBox verticalCombo = new JComboBox( new Object[] { "Top", "Center", "Bottom" } );
		verticalCombo.setSelectedIndex( 0 );
		verticalCombo.addItemListener( new ItemListener()
		{
			@Override
			public void itemStateChanged( final ItemEvent e )
			{
				final int i = verticalCombo.getSelectedIndex();
				_verticalAlignment = ( i == 0 ) ? SwingConstants.TOP : ( i == 1 ) ? SwingConstants.CENTER : SwingConstants.BOTTOM;
				textAreaPane.repaint();
			}
		} );


		final Box controls = new Box( BoxLayout.X_AXIS );
		controls.add( horizontalCombo );
		controls.add( verticalCombo );

		final JPanel contentPane = new JPanel( new BorderLayout() );
		contentPane.add( textAreaPane, BorderLayout.CENTER );
		contentPane.add( controls, BorderLayout.SOUTH );

		final Class<?> demoClass = getClass();
		final JFrame frame = new JFrame( demoClass.getSimpleName() );
		frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		frame.setContentPane( contentPane );
		WindowTools.center( frame, 400, 400 );
		frame.setVisible( true );
	}

	/**
	 * Component that shows the {@link TextArea}.
	 */
	private class TextAreaPane
		extends JComponent
	{
		/**
		 * Construct component.
		 */
		TextAreaPane()
		{
			setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
		}

		@Override
		protected void paintComponent( final Graphics g )
		{
			super.paintComponent( g );

			final Insets insets = getInsets();

			final int x = insets.left;
			final int y = insets.top;
			final int h = getHeight() - insets.left - insets.right;
			final int w = getWidth() - insets.top - insets.bottom;

			final TextArea textArea = new TextArea( w, h, _horizontalAlignment, _verticalAlignment );

			textArea.appendLine( g, TEXT1, LOCALE, FONT1, COLOR1 );
			textArea.appendLine( g, TEXT2, LOCALE, FONT2, COLOR2 );
			textArea.appendVerticalGap( 10 );
			textArea.appendParagraph( g, TEXT3, LOCALE, FONT3, COLOR3 );

			textArea.paint( g, x, y );
		}
	}
}
