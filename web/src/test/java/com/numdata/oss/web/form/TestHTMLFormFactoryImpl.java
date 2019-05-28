/*
 * Copyright (c) 2019, Numdata BV, The Netherlands.
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

package com.numdata.oss.web.form;

import java.io.*;
import java.util.*;

import com.numdata.oss.web.*;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test for {@link HTMLFormFactoryImpl}.
 *
 * @author Gerrit Meinders
 */
public class TestHTMLFormFactoryImpl
{
	/**
	 * String that requires escapes in HTML.
	 */
	private static final String TRICKY_STRING = "Test & \"<quality>\"";

	/**
	 * {@link #TRICKY_STRING} escaped for use in HTML attributes.
	 */
	private static final String TRICKY_STRING_ATTRIBUTE = "Test &amp; &quot;&lt;quality&gt;&quot;";

	/**
	 * {@link #TRICKY_STRING} escaped for use in HTML character data.
	 */
	private static final String TRICKY_STRING_CHARACTER_DATA = "Test &amp; \"&lt;quality&gt;\"";

	/**
	 * URL that requires escapes in HTML.
	 */
	private static final String TRICKY_URL = "https://example.com/tricky?a=b&c=d#fragment";

	/**
	 * {@link #TRICKY_URL} escaped for use in HTML attributes.
	 */
	private static final String TRICKY_URL_ATTRIBUTE = "https://example.com/tricky?a=b&amp;c=d#fragment";

	/**
	 * Relative URL that requires escapes in HTML.
	 */
	private static final String TRICKY_PATH = "tricky?a=b&c=d#fragment";

	/**
	 * {@link #TRICKY_PATH} escaped for use in HTML attributes.
	 */
	private static final String TRICKY_PATH_ATTRIBUTE = "tricky?a=b&amp;c=d#fragment";

	/**
	 * Instance to test.
	 */
	private HTMLFormFactoryImpl _instance = null;

	/**
	 * Servlet context path.
	 */
	private final String _contextPath = "/servletContext";

	/**
	 * Writes indented output.
	 */
	private IndentingJspWriter _out = null;

	/**
	 * Stores output written by {@link #_out}.
	 */
	private StringWriter _buffer = null;

	@Before
	public void setUp()
	{
		_instance = new HTMLFormFactoryImpl( new HTMLTableFactoryImpl() );
		_buffer = new StringWriter();
		_out = IndentingJspWriter.create( _buffer, 2, 0 );
	}

	@After
	public void tearDown()
	{
		_instance = null;
		_buffer = null;
		_out = null;
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeFormPre}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeFormPre()
	throws IOException
	{
		final String where = getClass().getName() + ".writeFormPre()";
		System.out.println( where );

		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put( "data-tricky", TRICKY_STRING );

		_instance.writeFormPre( _contextPath, _out, attributes, TRICKY_STRING, null, Collections.<FormButton>emptyList() );
		assertOutput( "<div class=\"form\">\n" +
		              "  <form data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">\n" +
		              "    <div class=\"title\">" + TRICKY_STRING_CHARACTER_DATA + "</div>\n" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeFormPost}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeFormPost()
	throws IOException
	{
		final String where = getClass().getName() + ".writeFormPost()";
		System.out.println( where );

		final List<FormButton> buttons = Arrays.asList(
		new FormButton( FormButton.SUBMIT, TRICKY_STRING, "style1", "style2" ),
		new FormButton( FormButton.BACK, TRICKY_STRING ),
		new FormButton( FormButton.BROWSE, TRICKY_STRING ),
		new FormButton( FormButton.IMAGE, TRICKY_STRING )
		);

		_out.setIndentDepth( 2 );
		_instance.writeFormPost( _contextPath, _out, null, buttons );
		assertOutput( "    <div class=\"buttons\">\n" +
		              "      <input type=\"submit\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" class=\"style1 style2\">\n" +
		              "      <input type=\"button\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" onclick=\"history.back();\">\n" +
		              "      <input type=\"file\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\">\n" +
		              "      <input type=\"image\" src=\"" + TRICKY_STRING_ATTRIBUTE + "\" alt=\"" + TRICKY_STRING_ATTRIBUTE + "\">\n" +
		              "    </div>\n" +
		              "  </form>\n" +
		              "</div>\n" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeLabel}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeLabel()
	throws IOException
	{
		final String where = getClass().getName() + ".writeLabel()";
		System.out.println( where );

		final FormField field = new FormTextField( new VariableFieldTarget( TRICKY_STRING, TRICKY_STRING ) );
		field.setRequired( true );
		field.setException( new Exception( TRICKY_STRING ) );
		field.setDescription( TRICKY_STRING );

		final FormLabel label = new FormLabel( TRICKY_STRING, field );

		final FormContainer formContainer = new FormContainer();
		formContainer.addComponent( label );
		formContainer.addComponent( field );

		_instance.writeLabel( _contextPath, null, _out, label );
		assertOutput( TRICKY_STRING_CHARACTER_DATA + ": " +
		              "<span class=\"requiredField\">*</span>" +
		              "<p class=\"error\">" + TRICKY_STRING_CHARACTER_DATA + "</p>" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeBackButton}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeBackButton()
	throws IOException
	{
		final String where = getClass().getName() + ".writeBackButton()";
		System.out.println( where );

		_instance.writeBackButton( _contextPath, _out, TRICKY_STRING );
		assertOutput( "<input type=\"button\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" onclick=\"history.back();\">" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeBrowseButton}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeBrowseButton()
	throws IOException
	{
		final String where = getClass().getName() + ".writeBrowseButton()";
		System.out.println( where );

		_instance.writeBrowseButton( _out, TRICKY_STRING );
		assertOutput( "<input type=\"file\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeButton}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeButton()
	throws IOException
	{
		final String where = getClass().getName() + ".writeButton()";
		System.out.println( where );

		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put( "data-tricky", TRICKY_STRING );

		_instance.writeButton( _out, TRICKY_URL, TRICKY_STRING, attributes );
		assertOutput( "<input type=\"button\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" " +
		              "onclick=\"window.location=&#39;" + TRICKY_URL_ATTRIBUTE + "&#39;;\" " +
		              "data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeCheckbox}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeCheckbox()
	throws IOException
	{
		final String where = getClass().getName() + ".writeCheckbox()";
		System.out.println( where );

		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put( "data-tricky", TRICKY_STRING );

		_instance.writeCheckbox( _contextPath, _out, true, TRICKY_STRING, true, attributes );
		assertOutput( "<input type=\"checkbox\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" checked=\"\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\" class=\"checkbox\">" );
		_instance.writeCheckbox( _contextPath, _out, true, TRICKY_STRING, false, attributes );
		assertOutput( "<input type=\"checkbox\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\" class=\"checkbox\">" );
		_instance.writeCheckbox( _contextPath, _out, false, TRICKY_STRING, true, attributes );
		assertOutput( "<input type=\"checkbox\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" checked=\"\" disabled=\"\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\" class=\"checkbox\">" );
		_instance.writeCheckbox( _contextPath, _out, false, TRICKY_STRING, false, attributes );
		assertOutput( "<input type=\"checkbox\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" disabled=\"\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\" class=\"checkbox\">" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeRadioButton}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeRadioButton()
	throws IOException
	{
		final String where = getClass().getName() + ".writeRadioButton()";
		System.out.println( where );

		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put( "data-tricky", TRICKY_STRING );

		_instance.writeRadioButton( _contextPath, _out, true, TRICKY_STRING, TRICKY_STRING, true, attributes );
		assertOutput( "<input type=\"radio\" class=\"radio\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" checked=\"\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
		_instance.writeRadioButton( _contextPath, _out, true, TRICKY_STRING, TRICKY_STRING, false, attributes );
		assertOutput( "<input type=\"radio\" class=\"radio\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
		_instance.writeRadioButton( _contextPath, _out, false, TRICKY_STRING, TRICKY_STRING, true, attributes );
		assertOutput( "<input type=\"radio\" class=\"radio\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" checked=\"\" disabled=\"\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
		_instance.writeRadioButton( _contextPath, _out, false, TRICKY_STRING, TRICKY_STRING, false, attributes );
		assertOutput( "<input type=\"radio\" class=\"radio\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" disabled=\"\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeChoice}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeChoice()
	throws IOException
	{
		final String where = getClass().getName() + ".writeChoice()";
		System.out.println( where );

		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put( "data-tricky", TRICKY_STRING );

		_instance.writeChoice( _out, true, TRICKY_STRING, TRICKY_STRING, Collections.singletonList( TRICKY_STRING ), Collections.singletonList( TRICKY_STRING ), attributes );
		assertOutput( "<select id=\"" + TRICKY_STRING_ATTRIBUTE + "\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">\n" +
		              "  <option value=\"" + TRICKY_STRING_ATTRIBUTE + "\" selected=\"selected\">" + TRICKY_STRING_CHARACTER_DATA + "</option>\n" +
		              "</select>\n" );
		_instance.writeChoice( _out, false, TRICKY_STRING, TRICKY_STRING, Collections.singletonList( TRICKY_STRING ), Collections.singletonList( TRICKY_STRING ), attributes );
		assertOutput( TRICKY_STRING_CHARACTER_DATA );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeHiddenField}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeHiddenField()
	throws IOException
	{
		final String where = getClass().getName() + ".writeHiddenField()";
		System.out.println( where );

		_instance.writeHiddenField( _out, TRICKY_STRING, TRICKY_STRING );
		assertOutput( "<input type=\"hidden\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\">\n" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeSubmitButton}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeSubmitButton()
	throws IOException
	{
		final String where = getClass().getName() + ".writeSubmitButton()";
		System.out.println( where );

		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put( "data-tricky", TRICKY_STRING );

		_instance.writeSubmitButton( _out, TRICKY_STRING, TRICKY_STRING, attributes );
		assertOutput( "<input type=\"submit\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeSubmitImage}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeSubmitImage()
	throws IOException
	{
		final String where = getClass().getName() + ".writeSubmitImage()";
		System.out.println( where );

		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put( "data-tricky", TRICKY_STRING );

		_instance.writeSubmitImage( _contextPath, _out, TRICKY_STRING, TRICKY_URL, attributes );
		assertOutput( "<input type=\"image\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" src=\"" + TRICKY_URL_ATTRIBUTE + "\" alt=\"" + TRICKY_PATH_ATTRIBUTE + "\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
		_instance.writeSubmitImage( _contextPath, _out, TRICKY_STRING, "/" + TRICKY_PATH, attributes );
		assertOutput( "<input type=\"image\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" src=\"" + _contextPath + "/" + TRICKY_PATH_ATTRIBUTE + "\" alt=\"" + TRICKY_PATH_ATTRIBUTE + "\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeTextArea}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeTextArea()
	throws IOException
	{
		final String where = getClass().getName() + ".writeTextArea()";
		System.out.println( where );

		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put( "data-tricky", TRICKY_STRING );

		_instance.writeTextArea( _out, true, TRICKY_STRING, TRICKY_STRING, 40, 5, attributes );
		assertOutput( "<textarea name=\"" + TRICKY_STRING_ATTRIBUTE + "\" cols=\"40\" rows=\"5\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" + TRICKY_STRING_CHARACTER_DATA + "</textarea>" );
		_instance.writeTextArea( _out, false, TRICKY_STRING, TRICKY_STRING, 40, 5, attributes );
		assertOutput( "<pre>" + TRICKY_STRING_CHARACTER_DATA + "</pre>" );
	}

	/**
	 * Tests {@link HTMLFormFactoryImpl#writeTextField}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@Test
	public void writeTextField()
	throws IOException
	{
		final String where = getClass().getName() + ".writeTextField()";
		System.out.println( where );

		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put( "data-tricky", TRICKY_STRING );

		_instance.writeTextField( _out, true, TRICKY_STRING, TRICKY_STRING, TRICKY_STRING, 20, 40, false, false, false, attributes );
		assertOutput( "<input type=\"text\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" size=\"20\" maxlength=\"40\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
		_instance.writeTextField( _out, true, TRICKY_STRING, TRICKY_STRING, TRICKY_STRING, 20, 40, true, true, true, attributes );
		assertOutput( "<input type=\"password\" name=\"" + TRICKY_STRING_ATTRIBUTE + "\" id=\"" + TRICKY_STRING_ATTRIBUTE + "\" size=\"20\" maxlength=\"40\" value=\"" + TRICKY_STRING_ATTRIBUTE + "\" autocomplete=\"off\" class=\"invalid\" data-tricky=\"" + TRICKY_STRING_ATTRIBUTE + "\">" );
		_instance.writeTextField( _out, false, TRICKY_STRING, TRICKY_STRING, TRICKY_STRING, 20, 40, false, false, false, attributes );
		assertOutput( TRICKY_STRING_CHARACTER_DATA );
		_instance.writeTextField( _out, false, TRICKY_STRING, TRICKY_STRING, TRICKY_STRING, 20, 40, true, true, true, attributes );
		assertOutput( TRICKY_STRING_CHARACTER_DATA );
	}

	private void assertOutput( final String expected )
	{
		final String actual = _buffer.toString();
		assertEquals( "Unexpected output.", expected, actual );
		_buffer = new StringWriter();
		_out = IndentingJspWriter.create( _buffer, 2, 0 );
	}
}
