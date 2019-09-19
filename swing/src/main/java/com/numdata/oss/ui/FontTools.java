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

import java.awt.*;
import java.util.*;
import java.util.regex.*;

/**
 * This class contains various utility methods related to the {@link Font}
 * class.
 *
 * @author Peter S. Heijnen
 */
public class FontTools
{
	/**
	 * Special name patterns that should be avoided.
	 *
	 * @see #isRegular
	 */
	private static final Pattern SPECIAL_NAME = Pattern.compile( "[Bb][Oo][Ll][Dd]|[Ii][Tt][Aa][Ll][Ii][Cc]" );

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private FontTools()
	{
	}

	/**
	 * Get first matching physical front from a prioritized list of font names
	 * and derive a font with the specified properties from it.
	 *
	 * <dl><dt>RECOMMENDATION:</dt><dd>Always include at least one of the
	 * logical font names (e.g. "{@code SansSerif}" or "{@code DialogInput}") to
	 * make sure the result will never be {@code null}</dd></dl>
	 *
	 * @param fontNames Prioritized list of desired font names.
	 * @param style     Font style.
	 * @param height    Font height.
	 *
	 * @return Font matching the arguments; {@code null} if no such font is
	 * found.
	 */
	public static Font getFont( final String[] fontNames, final int style, final float height )
	{
		final Font font = getFont( fontNames );
		return ( font != null ) ? ( ( style == 0 ) && ( height == 1.0f ) ) ? font : font.deriveFont( style, height ) : null;
	}

	/**
	 * Get first matching physical front from a prioritized list of font names.
	 *
	 * <dl><dt>RECOMMENDATION:</dt><dd>Always include at least one of the
	 * logical font names (e.g. "{@code SansSerif}" or "{@code DialogInput}") to
	 * make sure the result will never be {@code null}</dd></dl>
	 *
	 * @param fontNames Prioritized list of desired font names.
	 *
	 * @return Font matching the argument; {@code null} if no such font is
	 * found.
	 */
	public static Font getFont( final String[] fontNames )
	{
		Font result = null;

		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final Font[] allFonts = ge.getAllFonts();

		for ( int i = 0; ( result == null ) && ( i < fontNames.length ); i++ )
		{
			final String name = fontNames[ i ];

			for ( int j = 0; j < allFonts.length; j++ )
			{
				final Font font = allFonts[ j ];

				if ( isRegular( font ) && name.equals( font.getFamily() ) )
				{
					result = font;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Get set of available font families available on this system.
	 *
	 * @return Set of available font families.
	 */
	public static Set getFontFamilies()
	{
		final Set result = new HashSet();

		final GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

		final Font[] allFonts = ge.getAllFonts();
		for ( int i = 0; i < allFonts.length; i++ )
		{
			final Font font = allFonts[ i ];
			if ( isRegular( font ) )
			{
				result.add( font.getFamily() );
			}
		}

		return result;
	}

	/**
	 * Test if the specified font is a 'regular' one (no special attributes
	 * set).
	 *
	 * @param font Font to test.
	 *
	 * @return {@code true} if the font is regular; {@code false} if the font
	 * has special attributes.
	 */
	private static boolean isRegular( final Font font )
	{
		boolean result = ( font.getStyle() == 0 );

		if ( result )
		{
			final Matcher matcher = SPECIAL_NAME.matcher( font.getFontName() );
			result = !matcher.find();
		}

		return result;
	}
}
