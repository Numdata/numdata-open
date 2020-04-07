/*
 * Copyright (c) 2020, Numdata BV, The Netherlands.
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

package com.numdata.oss.velocity;

import java.util.*;
import java.util.regex.*;

import static java.util.Collections.*;
import org.apache.velocity.*;
import org.jetbrains.annotations.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link VelocityTools}.
 *
 * @author Gerrit Meinders
 */
public class TestVelocityTools
{
	/**
	 * Special value for escaped Velocity variables. By setting this value,
	 * {@link #assertPattern} knows to expect no value.
	 */
	private static final String ESCAPED_VALUE = "ignoreEscapedValue";

	/**
	 * Tests {@link VelocityTools#createPattern}.
	 */
	@Test
	public void createPattern()
	{
		final String where = getClass().getName() + ".createPattern()";
		System.out.println( where );

		assertEquals( "Unexpected flags.", 0, VelocityTools.createPattern( "Hello world!", emptyMap(), emptyMap(), 0 ).flags() );
		assertEquals( "Unexpected flags.", Pattern.CASE_INSENSITIVE, VelocityTools.createPattern( "Hello world!", emptyMap(), emptyMap(), Pattern.CASE_INSENSITIVE ).flags() );

		assertPattern( "Hello ${name} world!", singletonMap( "name", "happy" ),
		               "Hello happy world!", emptyMap() );

		assertPattern( "Hello ${name} world!", singletonMap( "name", "happy" ),
		               "Hello happy world!", singletonMap( "name", "[a-z]+" ) );

		final Map<String, Object> variableValues = new HashMap<>();
		variableValues.put( "var1", "awesome" );
		variableValues.put( "var2", "1337" );

		final Map<String, String> variableReplacements = new HashMap<>();
		variableReplacements.put( "var1", "[a-z]+" );
		variableReplacements.put( "var2", "[0-9]+" );

		assertPattern( "Hello ${var1} ${var2} world!", variableValues,
		               "Hello awesome 1337 world!", variableReplacements );

		assertPattern( "Hello ${var2} ${var1} world!", variableValues,
		               "Hello 1337 awesome world!", variableReplacements );

		assertPattern( "Windows path: C:\\Temp\\\\${name}.txt", singletonMap( "name", "example" ),
		               "Windows path: C:\\Temp\\example.txt", emptyMap() );

		assertPattern( "Hello \\${escaped} world!", singletonMap( "escaped", ESCAPED_VALUE ),
		               "Hello ${escaped} world!", emptyMap() );

		// Note that the pattern will still match anything in place of '${undefined}'.
		assertPattern( "Hello ${undefined} world!", emptyMap(),
		               "Hello ${undefined} world!", emptyMap() );

		// Note that the pattern will still match anything in place of '$!{hidden}'.
		assertPattern( "Hello $!{hidden} world!", emptyMap(),
		               "Hello  world!", emptyMap() );
	}

	/**
	 * Asserts that the given template can be filled with the given variable
	 * values to produce the expected output. Also asserts that the values can
	 * be extracted from the template output using the pattern created by
	 * {@link VelocityTools#createPattern} with the given variable replacements.
	 *
	 * @param template             Velocity template.
	 * @param variableValues       Values of variables in the template.
	 * @param expectedOutput       Result of the template for the given values.
	 * @param variableReplacements Variable replacements used to create a pattern.
	 */
	private static void assertPattern( @NotNull final String template, @NotNull final Map<String, Object> variableValues, @NotNull final String expectedOutput, @NotNull final Map<String, String> variableReplacements )
	{
		final Map<String, Integer> capturingGroups = new HashMap<>();
		final String templateOutput = VelocityTools.evaluate( template, new VelocityContext( variableValues ), "assertPattern" );
		assertEquals( "Template does not produce expected output.", expectedOutput, templateOutput );

		final Pattern pattern = VelocityTools.createPattern( template, variableReplacements, capturingGroups, Pattern.CASE_INSENSITIVE );
		final Matcher matcher = pattern.matcher( templateOutput );
		assertTrue( "Created pattern \"" + pattern.pattern() + "\" does not match template output \"" + templateOutput + "\"", matcher.matches() );

		for ( final Map.Entry<String, Object> entry : variableValues.entrySet() )
		{
			final Integer group = capturingGroups.get( entry.getKey() );
			if ( entry.getValue().equals( ESCAPED_VALUE ) )
			{
				assertNull( "Unexpected capturing group for escaped variable " + entry.getKey(), group );
			}
			else
			{
				assertNotNull( "Missing capturing group for variable " + entry.getKey(), group );
				assertEquals( "Unexpected value for 'name'.", entry.getValue(), matcher.group( group ) );
			}
		}
	}
}
