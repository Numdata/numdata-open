/*
 * Copyright (c) 2013-2017, Numdata BV, The Netherlands.
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
import java.math.*;
import java.net.*;
import java.util.*;

import com.numdata.oss.web.*;
import com.numdata.servlets.test.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link FormNumberField}.
 *
 * @author Peter S. Heijnen
 */
public class TestFormNumberField
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestFormNumberField.class.getName();

	/**
	 * Test number formatting and parsing.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testFormatAndParse()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testNumberFormatting()" );

		final Locale locale = Locale.GERMANY; // use local with comma instead of dot as decimal separator
		final Form form = new Form( locale, "formName", "formTitle", true );

		System.out.println( " - Big decimal target" );
		{
			final ReflectedFieldTarget bigDecimalTarget = new ReflectedFieldTarget( new TestTarget(), "bigDecimal" );

			final FormNumberField bigDecimalField = new FormNumberField( bigDecimalTarget, 2, BigDecimal.ZERO, null );
			form.addComponent( bigDecimalField );

			assertEquals( "Bad format", "123,46", bigDecimalField.format( new BigDecimal( "123.456" ) ) );
			assertEquals( "Invalid value", "123,46", bigDecimalField.getValue() );
			assertEquals( "Failed to parse", new BigDecimal( "123.46" ), bigDecimalField.parse( "123,456" ) );
			assertEquals( "Invalid number value", new BigDecimal( "123.46" ), bigDecimalField.getNumberValue() ); /* formatted and parsed */
		}

		System.out.println( " - Double target" );
		{
			final ReflectedFieldTarget doubleTarget = new ReflectedFieldTarget( new TestTarget(), "double" );

			final FormNumberField doubleField = new FormNumberField( doubleTarget, 2, null, new BigDecimal( 999 ) );
			form.addComponent( doubleField );

			assertEquals( "Bad format", "123,46", doubleField.format( 123.46 ) );
			assertEquals( "Invalid value", "123,46", doubleField.getValue() );
			assertEquals( "Failed to parse", new BigDecimal( "123.46" ), doubleField.parse( "123,456" ) );
			assertEquals( "Invalid number value", new BigDecimal( "123.46" ), doubleField.getNumberValue() ); /* formatted and parsed */
		}

		System.out.println( " - Integer target" );
		{
			final ReflectedFieldTarget integerTarget = new ReflectedFieldTarget( new TestTarget(), "integer" );

			final FormNumberField integerField = new FormNumberField( integerTarget, 0, new BigDecimal( -999 ), null );
			form.addComponent( integerField );

			assertEquals( "Bad format", "123", integerField.format( 123 ) );
			assertEquals( "Invalid value", "123", integerField.getValue() );
			assertEquals( "Failed to parse", new BigDecimal( "123" ), integerField.parse( "123" ) );
			try
			{
				assertEquals( "Failed to parse", new BigDecimal( "123" ), integerField.parse( "123,456" ) );
				fail( "should refuse to parse '123,456'" );
			}
			catch ( final NumberFormatException ignored )
			{
				/* this is expected */
			}
			assertEquals( "Invalid number value", new BigDecimal( "123" ), integerField.getNumberValue() ); /* formatted and parsed */
		}

		System.out.println( " - Property target" );
		{
			final Properties properties = new Properties();
			properties.setProperty( "name", "123.456" );
			final PropertyTarget propertyTarget = new PropertyTarget( properties, "name" );

			final FormNumberField propertyField = new FormNumberField( propertyTarget, 1, new BigDecimal( -999 ), null );
			form.addComponent( propertyField );
			assertEquals( "Bad format", "123,5", propertyField.format( new BigDecimal( "123.456" ) ) );
			assertEquals( "Invalid value", "123,5", propertyField.getValue() );
			assertEquals( "Failed to parse", new BigDecimal( "123.5" ), propertyField.parse( "123,456" ) );
			assertEquals( "Invalid number value", new BigDecimal( "123.5" ), propertyField.getNumberValue() ); /* formatted and parsed */
		}
	}

	/**
	 * Test number formatting and parsing.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGenerate()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGenerate()" );

		final Locale locale = Locale.GERMANY; // use local with comma instead of dot as decimal separator
		final Form form = new Form( locale, "formName", "formTitle", true );

		final HTMLTable htmlTable = new HTMLTable();
		final StringWriter bufferWriter = new StringWriter();
		final StringBuffer buffer = bufferWriter.getBuffer();
		final IndentingJspWriter jspWriter = IndentingJspWriter.create( bufferWriter, 1, 0 );
		final HTMLFormFactory formFactory = new HTMLFormFactoryImpl( new HTMLTableFactoryImpl() );

		System.out.println( " - Big decimal target" );
		{
			final ReflectedFieldTarget bigDecimalTarget = new ReflectedFieldTarget( new TestTarget(), "bigDecimal" );

			final FormNumberField bigDecimalField = new FormNumberField( bigDecimalTarget, 2, BigDecimal.ZERO, null );
			form.addComponent( bigDecimalField );

			bigDecimalField.generate( "/", form, htmlTable, jspWriter, formFactory );

			final String output = buffer.toString();
			System.out.println( "     [" + output + ']' );
			buffer.setLength( 0 );

			assertTrue( "Bad name in output", output.contains( "name=\"bigDecimal\"" ) );
			assertTrue( "Bad value in output", output.contains( "value=\"123,46\"" ) );
		}

		System.out.println( " - Double target" );
		{
			final ReflectedFieldTarget doubleTarget = new ReflectedFieldTarget( new TestTarget(), "double" );

			final FormNumberField doubleField = new FormNumberField( doubleTarget, 2, null, new BigDecimal( 999 ) );
			form.addComponent( doubleField );

			doubleField.generate( "/", form, htmlTable, jspWriter, formFactory );

			final String output = buffer.toString();
			System.out.println( "     [" + output + ']' );
			buffer.setLength( 0 );

			assertTrue( "Bad name in output", output.contains( "name=\"double\"" ) );
			assertTrue( "Bad value in output", output.contains( "value=\"123,46\"" ) );
		}

		System.out.println( " - Integer target" );
		{
			final ReflectedFieldTarget integerTarget = new ReflectedFieldTarget( new TestTarget(), "integer" );

			final FormNumberField integerField = new FormNumberField( integerTarget, 0, new BigDecimal( -999 ), null );
			form.addComponent( integerField );

			integerField.generate( "/", form, htmlTable, jspWriter, formFactory );

			final String output = buffer.toString();
			System.out.println( "     [" + output + ']' );
			buffer.setLength( 0 );

			assertTrue( "Bad name in output", output.contains( "name=\"integer\"" ) );
			assertTrue( "Bad value in output", output.contains( "value=\"123\"" ) );
		}

		System.out.println( " - Property target" );
		{
			final Properties properties = new Properties();
			properties.setProperty( "name", "123.456" );
			final PropertyTarget propertyTarget = new PropertyTarget( properties, "name" );

			final FormNumberField propertyField = new FormNumberField( propertyTarget, 1, new BigDecimal( -999 ), null );
			form.addComponent( propertyField );

			propertyField.generate( "/", form, htmlTable, jspWriter, formFactory );

			final String output = buffer.toString();
			System.out.println( "     [" + output + ']' );
			buffer.setLength( 0 );

			assertTrue( "Bad name in output", output.contains( "name=\"name\"" ) );
			assertTrue( "Bad value in output", output.contains( "value=\"123,5\"" ) );
		}
	}

	/**
	 * Test number range check.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testRangeCheck()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testRangeCheck()" );

		final Locale locale = Locale.GERMANY; // use local with comma instead of dot as decimal separator
		final Form form = new Form( locale, "formName", "formTitle", true );

		System.out.println( " - no limit" );
		{
			final TestTarget testTarget = new TestTarget();
			final ReflectedFieldTarget bigDecimalTarget = new ReflectedFieldTarget( testTarget, "bigDecimal" );

			final FormNumberField numberField = new FormNumberField( bigDecimalTarget, 2, null, null );
			form.addComponent( numberField );

			submitValue( numberField, "123,46" );

			submitValue( numberField, "-123,4567" );

			submitValue( numberField, "12346,78" );
		}

		System.out.println( " - minimum limit" );
		{
			final TestTarget testTarget = new TestTarget();
			final ReflectedFieldTarget bigDecimalTarget = new ReflectedFieldTarget( testTarget, "bigDecimal" );

			final FormNumberField numberField = new FormNumberField( bigDecimalTarget, 2, BigDecimal.ZERO, null );
			form.addComponent( numberField );

			submitValue( numberField, "123,46" );

			try
			{
				submitValue( numberField, "-123,4567" );
				fail( "should refuse out-of-range value" );
			}
			catch ( final InvalidFormDataException e )
			{
				assertEquals( "Unexpected exception type", InvalidFormDataException.Type.INVALID_NUMBER, e.getType() );
			}

			submitValue( numberField, "12346,78" );
		}

		System.out.println( " - maximum limit" );
		{
			final TestTarget testTarget = new TestTarget();
			final ReflectedFieldTarget bigDecimalTarget = new ReflectedFieldTarget( testTarget, "bigDecimal" );

			final FormNumberField numberField = new FormNumberField( bigDecimalTarget, 2, null, new BigDecimal( 1000 ) );
			form.addComponent( numberField );

			submitValue( numberField, "123,46" );

			submitValue( numberField, "-123,4567" );

			try
			{
				submitValue( numberField, "12346,78" );
				fail( "should refuse out-of-range value" );
			}
			catch ( final InvalidFormDataException e )
			{
				assertEquals( "Unexpected exception type", InvalidFormDataException.Type.INVALID_NUMBER, e.getType() );
			}
		}

		System.out.println( " - minimum and maximum limit" );
		{
			final TestTarget testTarget = new TestTarget();
			final ReflectedFieldTarget bigDecimalTarget = new ReflectedFieldTarget( testTarget, "bigDecimal" );

			final FormNumberField numberField = new FormNumberField( bigDecimalTarget, 2, BigDecimal.ZERO, new BigDecimal( 1000 ) );
			form.addComponent( numberField );

			submitValue( numberField, "123,46" );

			numberField.setValue( "123,46" );

			try
			{
				submitValue( numberField, "-123,4567" );
				fail( "should refuse out-of-range value" );
			}
			catch ( final InvalidFormDataException e )
			{
				assertEquals( "Unexpected exception type", InvalidFormDataException.Type.INVALID_NUMBER, e.getType() );
			}

			try
			{
				submitValue( numberField, "12346,78" );
				fail( "should refuse out-of-range value" );
			}
			catch ( final InvalidFormDataException e )
			{
				assertEquals( "Unexpected exception type", InvalidFormDataException.Type.INVALID_NUMBER, e.getType() );
			}
		}
	}

	/**
	 * Submit value to field.
	 *
	 * @param numberField Target field.
	 * @param value       Value to submit.
	 *
	 * @throws InvalidFormDataException if the field detected a problem.
	 */
	private static void submitValue( final FormField numberField, final String value )
	throws InvalidFormDataException
	{
		final String name = numberField.getName();
		assertNotNull( "Field has no name?", name );

		final ServletTestContext servletContext = new ServletTestContext();
		final URL url;
		try
		{
			url = new URL( "http", "host", 80, "/test/" );
		}
		catch ( final MalformedURLException e )
		{
			throw new AssertionError( e ); // should never happen
		}

		final HttpServletTestRequest request = new HttpServletTestRequest( servletContext, url );
		request.setParameter( name, value );

		numberField.submitData( request );
	}

	/**
	 * Target used for field.
	 */
	public static class TestTarget
	{
		private BigDecimal _bigDecimal = new BigDecimal( "123.456" );

		private double _double = 123.456;

		private int _integer = 123;

		public BigDecimal getBigDecimal()
		{
			return _bigDecimal;
		}

		public void setBigDecimal( final BigDecimal bigDecimal )
		{
			_bigDecimal = bigDecimal;
		}

		public double getDouble()
		{
			return _double;
		}

		public void setDouble( final double aDouble )
		{
			_double = aDouble;
		}

		public int getInteger()
		{
			return _integer;
		}

		public void setInteger( final int integer )
		{
			_integer = integer;
		}
	}
}
