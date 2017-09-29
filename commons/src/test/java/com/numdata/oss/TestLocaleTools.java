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
package com.numdata.oss;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for the {@link LocaleTools} class.
 *
 * @author G. Meinders
 */
public class TestLocaleTools
{
	/**
	 * Tests the {@link LocaleTools#parseLocale(String)} method.
	 */
	@Test
	public void testParseLocale()
	{
		assertEquals( "Unexpected locale.", new Locale( "", "", "" ), LocaleTools.parseLocale( "" ) );
		assertEquals( "Unexpected locale.", new Locale( "", "", "" ), LocaleTools.parseLocale( "_" ) );
		assertEquals( "Unexpected locale.", new Locale( "", "", "" ), LocaleTools.parseLocale( "__" ) );
		assertEquals( "Unexpected locale.", new Locale( "nl", "", "" ), LocaleTools.parseLocale( "nl" ) );
		assertEquals( "Unexpected locale.", new Locale( "nl", "", "" ), LocaleTools.parseLocale( "nl_" ) );
		assertEquals( "Unexpected locale.", new Locale( "nl", "", "" ), LocaleTools.parseLocale( "nl__" ) );
		assertEquals( "Unexpected locale.", new Locale( "", "NL", "" ), LocaleTools.parseLocale( "_NL" ) );
		assertEquals( "Unexpected locale.", new Locale( "nl", "NL", "" ), LocaleTools.parseLocale( "nl_NL" ) );
		assertEquals( "Unexpected locale.", new Locale( "", "", "Linux" ), LocaleTools.parseLocale( "__Linux" ) );
		assertEquals( "Unexpected locale.", new Locale( "nl", "", "Linux" ), LocaleTools.parseLocale( "nl__Linux" ) );
		assertEquals( "Unexpected locale.", new Locale( "", "NL", "Linux" ), LocaleTools.parseLocale( "_NL_Linux" ) );
		assertEquals( "Unexpected locale.", new Locale( "nl", "NL", "Linux" ), LocaleTools.parseLocale( "nl_NL_Linux" ) );
	}
}
