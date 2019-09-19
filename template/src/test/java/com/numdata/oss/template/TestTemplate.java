/*
 * Copyright (c) 2011-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.template;

import java.io.*;
import java.util.*;

import com.numdata.oss.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit tests for {@link Template} and {@link TemplateIO}.
 *
 * @author G. Meinders
 */
public class TestTemplate
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestTemplate.class.getName();

	/**
	 * Tests that {@code test1.xml} is properly parsed by {@link
	 * TemplateIO#read(InputStream)}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void testReadTemplate()
	throws IOException
	{
		System.out.println( CLASS_NAME + ".testReadTemplate()" );

		final Class<? extends TestTemplate> clazz = getClass();
		final Template template = TemplateIO.read( clazz.getResourceAsStream( "test1.xml" ) );

		final List<TemplateContent> content = template.getContent();
		assertEquals( "Unexpected content.", 5, content.size() );

		final TemplateContent content0 = content.get( 0 );
		assertEquals( "Unexpected content type.", CharacterContent.class, content0.getClass() );
		assertEquals( "Unexpected content.", "Hello ", ( (CharacterContent)content0 ).getContent() );

		final TemplateContent content1 = content.get( 1 );
		assertEquals( "Unexpected content type.", VariableContent.class, content1.getClass() );
		assertEquals( "Unexpected variable.", "b", ( (VariableContent)content1 ).getVariable() );

		final TemplateContent content2 = content.get( 2 );
		assertEquals( "Unexpected content type.", CharacterContent.class, content2.getClass() );
		assertEquals( "Unexpected variable.", " World\n", ( (CharacterContent)content2 ).getContent() );

		final TemplateContent content3 = content.get( 3 );
		assertEquals( "Unexpected content type.", CursorPosition.class, content3.getClass() );

		final TemplateContent content4 = content.get( 4 );
		assertEquals( "Unexpected content type.", VariableContent.class, content4.getClass() );
		assertEquals( "Unexpected variable.", "a", ( (VariableContent)content4 ).getVariable() );

		final List<TemplateInput> inputs = template.getInputs();
		assertEquals( "Unexpected inputs.", 1, inputs.size() );

		final TemplateInput input0 = inputs.get( 0 );
		assertEquals( "Unexpected variable.", "a", input0.getVariable() );
		final LocalizedString input0Message = input0.getMessage();
		assertNotNull( "Missing message.", input0Message );
		assertEquals( "Unexpected message.", "unit test", input0Message.get() );
	}

	/**
	 * Tests that {@code test1.xml} generates the correct output.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void testGenerateTemplate()
	throws IOException
	{
		System.out.println( CLASS_NAME + ".testGenerateTemplate()" );

		final Class<? extends TestTemplate> clazz = getClass();
		final Template template = TemplateIO.read( clazz.getResourceAsStream( "test1.xml" ) );
		template.setVariable( "a", "Success!" );
		template.setVariable( "b", "Unit Test" );

		final StringBuilder builder = new StringBuilder();
		final TemplateOutput output = new TemplateOutput( template, builder );
		template.generate( output );

		assertEquals( "Unexpected output.", "Hello Unit Test World\nSuccess!", builder.toString() );
		assertEquals( "Unexpected cursor position.", 22, output.getCursorPosition() );
	}
}
