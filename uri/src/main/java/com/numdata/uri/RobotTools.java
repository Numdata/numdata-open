/*
 * Copyright (c) 2018, Numdata BV, The Netherlands.
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
package com.numdata.uri;

import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;

import org.jetbrains.annotations.*;

/**
 * This utility method can be used to help using the {@link Robot} API.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "WeakerAccess" )
public class RobotTools
{

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private RobotTools()
	{
	}

	/**
	 * Special key pattern.
	 */
	private static final Pattern SPECIAL_KEYS = Pattern.compile( "(?i)<(?:(?:f([1-9]|1[0-2]))|(c(?:on)?tro?l)|(alt)|(shift)|(win)|(?:delay:(\\d+))|(wait))>" );
	//                                                                        1               2               3     4       5              6       7

	/**
	 * Special: {@link KeyEvent#VK_F1 function} key.
	 */
	private static final int FUNCTION_KEY = 1;

	/**
	 * Special: {@link KeyEvent#VK_CONTROL control} key.
	 */
	private static final int CONTROL = 2;

	/**
	 * Special: {@link KeyEvent#VK_ALT alt} key.
	 */
	private static final int ALT = 3;

	/**
	 * Special: {@link KeyEvent#VK_SHIFT shift} key.
	 */
	private static final int SHIFT = 4;

	/**
	 * Special: {@link KeyEvent#VK_WINDOWS win} key.
	 */
	private static final int WINDOWS = 5;

	/**
	 * Special: call {@link Robot#delay(int)}.
	 */
	private static final int DELAY = 6;

	/**
	 * Special: call {@link Robot#waitForIdle()}.
	 */
	private static final int WAIT = 7;

	/**
	 * Send string to keyboard.
	 *
	 * @param string String to send.
	 *
	 * @throws AWTException if unable to access the keyboard (see {@link
	 * Robot}).
	 */
	public static void sendString( @NotNull final CharSequence string )
	throws AWTException
	{
		sendString( new Robot(), string );
	}

	/**
	 * Send string to keyboard.
	 *
	 * @param robot  {@link Robot} to use.
	 * @param string String to send.
	 */
	public static void sendString( @NotNull final Robot robot, @NotNull final CharSequence string )
	{
		int modifiers = 0;
		final int length = string.length();

		final Matcher matcher = SPECIAL_KEYS.matcher( string );
		for ( int start = 0; start < length; start = matcher.end() )
		{
			final boolean found = matcher.find();
			final int end = found ? matcher.start() : length;

			for ( int i = start; i < end; i++ )
			{
				final char ch = string.charAt( i );
				final int keyCode = KeyEvent.getExtendedKeyCodeForChar( ch );
				if ( keyCode != KeyEvent.VK_UNDEFINED )
				{
					if ( Character.isUpperCase( ch ) )
					{
						modifiers |= ( 1 << SHIFT );
					}

					try
					{
						sendKey( robot, modifiers, keyCode );
					}
					catch ( final IllegalArgumentException e )
					{
						throw new RuntimeException( "Failed to send key for '" + ch + "' character (modifiers=" + Integer.toBinaryString( modifiers ) + ", keyCode=" + keyCode, e );
					}
				}
				modifiers = 0;
			}

			// done if no special found
			if ( !found )
			{
				break;
			}

			// find first matched group
			int group = 1;
			String subsequence;
			do
			{
				subsequence = matcher.group( ++group );
			}
			while ( subsequence == null );

			// handle special
			switch ( group )
			{
				case FUNCTION_KEY:
					sendKey( robot, modifiers, KeyEvent.VK_F1 + Integer.parseInt( subsequence ) - 1 );
					modifiers = 0;
					break;

				case CONTROL:
				case ALT:
				case SHIFT:
				case WINDOWS:
					modifiers |= ( 1 << group );
					break;

				case DELAY:
					robot.delay( Integer.parseInt( subsequence ) );
					break;

				case WAIT:
					robot.waitForIdle();
					break;
			}
		}
	}

	/**
	 * Send key with optional modifiers to the keyboard.
	 *
	 * @param robot   {@link Robot} to use.
	 * @param modifiers Modifier keys to hold down.
	 * @param keyCode Key code to send.
	 */
	private static void sendKey( @NotNull final Robot robot, final int modifiers, final int keyCode )
	{
		robot.delay( 10 );
		robot.waitForIdle();

		final boolean control = ( ( modifiers & ( 1 << CONTROL ) ) != 0 );
		if ( control )
		{
			robot.keyPress( KeyEvent.VK_CONTROL );
		}

		final boolean shift = ( ( modifiers & ( 1 << SHIFT ) ) != 0 );
		if ( shift )
		{
			robot.keyPress( KeyEvent.VK_SHIFT );
		}

		final boolean alt = ( ( modifiers & ( 1 << ALT ) ) != 0 );
		if ( alt )
		{
			robot.keyPress( KeyEvent.VK_ALT );
		}

		final boolean windows = ( ( modifiers & ( 1 << WINDOWS ) ) != 0 );
		if ( windows )
		{
			robot.keyPress( KeyEvent.VK_WINDOWS );
		}

		robot.keyPress( keyCode );
		robot.keyRelease( keyCode );

		if ( alt )
		{
			robot.keyRelease( KeyEvent.VK_ALT );
		}

		if ( windows )
		{
			robot.keyRelease( KeyEvent.VK_WINDOWS );
		}

		if ( shift )
		{
			robot.keyRelease( KeyEvent.VK_SHIFT );
		}

		if ( control )
		{
			robot.keyRelease( KeyEvent.VK_CONTROL );
		}
	}
}
