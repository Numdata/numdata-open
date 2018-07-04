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
import java.lang.reflect.*;
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
	 * {@link KeyEvent#getExtendedKeyCodeForChar(int)} method.
	 */
	@Nullable
	private static final Method GET_EXTENDED_KEY_CODE_FOR_CHAR_METHOD;

	static
	{
		Method method = null;
		try
		{
			//noinspection JavaReflectionMemberAccess
			method = KeyEvent.class.getMethod( "getExtendedKeyCodeForChar", char.class );
		}
		catch ( final Exception ignored )
		{
		}
		GET_EXTENDED_KEY_CODE_FOR_CHAR_METHOD = method;
	}

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

				final int keyCode = getKeyCodeForChar( ch );
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
	 * Get key code for the given character.
	 *
	 * This method is Java 6 compatible; for Java 7+ it will simply call {@link
	 * KeyEvent#getExtendedKeyCodeForChar(int)}.
	 *
	 * @param ch Character.
	 *
	 * @return Key code; {@link KeyEvent#VK_UNDEFINED} if no key code is
	 * available.
	 *
	 * @see KeyEvent#getExtendedKeyCodeForChar
	 */
	private static int getKeyCodeForChar( final char ch )
	{
		int keyCode = -1;
		if ( GET_EXTENDED_KEY_CODE_FOR_CHAR_METHOD != null )
		{
			try
			{
				//noinspection JavaReflectionMemberAccess
				keyCode = (Integer)GET_EXTENDED_KEY_CODE_FOR_CHAR_METHOD.invoke( null, ch );
			}
			catch ( final Exception ignored )
			{
			}
		}

		if ( keyCode < 0 )
		{
			switch ( ch )
			{
				case '\b': // 010
					keyCode = KeyEvent.VK_BACK_SPACE;
					break;

				case '\t': // 011
					keyCode = KeyEvent.VK_TAB;
					break;

				case '\n': // 012
					keyCode = KeyEvent.VK_ENTER;
					break;

//				case '\020':
//					keyCode = KeyEvent.VK_HOME;
//					break;

//				case '\021':
//					keyCode = KeyEvent.VK_END;
//					break;

//				case '\022':
//					keyCode = KeyEvent.VK_WINDOWS;
//					break;

				case '\033':
					keyCode = KeyEvent.VK_ESCAPE;
					break;

				case ' ':
					keyCode = KeyEvent.VK_SPACE;
					break;

				case '0':
					keyCode = KeyEvent.VK_0;
					break;

				case '1':
					keyCode = KeyEvent.VK_1;
					break;

				case '2':
					keyCode = KeyEvent.VK_2;
					break;

				case '3':
					keyCode = KeyEvent.VK_3;
					break;

				case '4':
					keyCode = KeyEvent.VK_4;
					break;

				case '5':
					keyCode = KeyEvent.VK_5;
					break;

				case '6':
					keyCode = KeyEvent.VK_6;
					break;

				case '7':
					keyCode = KeyEvent.VK_7;
					break;

				case '8':
					keyCode = KeyEvent.VK_8;
					break;

				case '9':
					keyCode = KeyEvent.VK_9;
					break;

				case 'A':
				case 'a':
					keyCode = KeyEvent.VK_A;
					break;

				case 'B':
				case 'b':
					keyCode = KeyEvent.VK_B;
					break;

				case 'C':
				case 'c':
					keyCode = KeyEvent.VK_C;
					break;

				case 'D':
				case 'd':
					keyCode = KeyEvent.VK_D;
					break;

				case 'E':
				case 'e':
					keyCode = KeyEvent.VK_E;
					break;

				case 'F':
				case 'f':
					keyCode = KeyEvent.VK_F;
					break;

				case 'G':
				case 'g':
					keyCode = KeyEvent.VK_G;
					break;

				case 'H':
				case 'h':
					keyCode = KeyEvent.VK_H;
					break;

				case 'I':
				case 'i':
					keyCode = KeyEvent.VK_I;
					break;

				case 'J':
				case 'j':
					keyCode = KeyEvent.VK_J;
					break;

				case 'K':
				case 'k':
					keyCode = KeyEvent.VK_K;
					break;

				case 'L':
				case 'l':
					keyCode = KeyEvent.VK_L;
					break;

				case 'M':
				case 'm':
					keyCode = KeyEvent.VK_M;
					break;

				case 'N':
				case 'n':
					keyCode = KeyEvent.VK_N;
					break;

				case 'O':
				case 'o':
					keyCode = KeyEvent.VK_O;
					break;

				case 'P':
				case 'p':
					keyCode = KeyEvent.VK_P;
					break;

				case 'Q':
				case 'q':
					keyCode = KeyEvent.VK_Q;
					break;

				case 'R':
				case 'r':
					keyCode = KeyEvent.VK_R;
					break;

				case 'S':
				case 's':
					keyCode = KeyEvent.VK_S;
					break;

				case 'T':
				case 't':
					keyCode = KeyEvent.VK_T;
					break;

				case 'U':
				case 'u':
					keyCode = KeyEvent.VK_U;
					break;

				case 'V':
				case 'v':
					keyCode = KeyEvent.VK_V;
					break;

				case 'W':
				case 'w':
					keyCode = KeyEvent.VK_W;
					break;

				case 'X':
				case 'x':
					keyCode = KeyEvent.VK_X;
					break;

				case 'Y':
				case 'y':
					keyCode = KeyEvent.VK_Y;
					break;

				case 'Z':
				case 'z':
					keyCode = KeyEvent.VK_Z;
					break;

				case '!':
					keyCode = KeyEvent.VK_EXCLAMATION_MARK;
					break;

				case '@':
					keyCode = KeyEvent.VK_AT;
					break;

				case '#':
					keyCode = KeyEvent.VK_NUMBER_SIGN;
					break;

				case '$':
					keyCode = KeyEvent.VK_DOLLAR;
					break;

//				case '%':
//					keyCode = KeyEvent.VK_PERCENT;
//					break;

				case '^':
					keyCode = KeyEvent.VK_CIRCUMFLEX;
					break;

				case '&':
					keyCode = KeyEvent.VK_AMPERSAND;
					break;

				case '*':
					keyCode = KeyEvent.VK_ASTERISK;
					break;

				case '(':
					keyCode = KeyEvent.VK_LEFT_PARENTHESIS;
					break;

				case ')':
					keyCode = KeyEvent.VK_RIGHT_PARENTHESIS;
					break;

				case '-':
					keyCode = KeyEvent.VK_MINUS;
					break;

				case '_':
					keyCode = KeyEvent.VK_UNDERSCORE;
					break;

				case '=':
					keyCode = KeyEvent.VK_EQUALS;
					break;

				case '+':
					keyCode = KeyEvent.VK_PLUS;
					break;

				case '[':
					keyCode = KeyEvent.VK_OPEN_BRACKET;
					break;

				case ']':
					keyCode = KeyEvent.VK_CLOSE_BRACKET;
					break;

				case '{':
					keyCode = KeyEvent.VK_BRACELEFT;
					break;

				case '}':
					keyCode = KeyEvent.VK_BRACERIGHT;
					break;

				case '\\':
					keyCode = KeyEvent.VK_BACK_SLASH;
					break;

//				case '|':
//					keyCode = KeyEvent.VK_PIPE;
//					break;

				case ';':
					keyCode = KeyEvent.VK_SEMICOLON;
					break;

				case ':':
					keyCode = KeyEvent.VK_COLON;
					break;

				case '\'':
					keyCode = KeyEvent.VK_QUOTE;
					break;

				case '"':
					keyCode = KeyEvent.VK_QUOTEDBL;
					break;

				case ',':
					keyCode = KeyEvent.VK_COMMA;
					break;

				case '.':
					keyCode = KeyEvent.VK_PERIOD;
					break;

				case '/':
					keyCode = KeyEvent.VK_SLASH;
					break;

				case '<':
					keyCode = KeyEvent.VK_LESS;
					break;

				case '>':
					keyCode = KeyEvent.VK_GREATER;
					break;

//				case '?':
//					keyCode = KeyEvent.VK_QUESTION;
//					break;

				case '`':
					keyCode = KeyEvent.VK_BACK_SLASH;
					break;

//				case '~':
//					keyCode = KeyEvent.VK_TILDE;
//					break;

				default:
					keyCode = KeyEvent.VK_UNDEFINED;
			}
		}

		return keyCode;
	}

	/**
	 * Send key with optional modifiers to the keyboard.
	 *
	 * @param robot     {@link Robot} to use.
	 * @param modifiers Modifier keys to hold down.
	 * @param keyCode   Key code to send.
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
