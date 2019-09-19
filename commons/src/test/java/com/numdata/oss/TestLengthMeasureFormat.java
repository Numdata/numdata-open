/*
 * Copyright (c) 2015-2017, Numdata BV, The Netherlands.
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

import java.math.*;
import java.util.*;

import org.jetbrains.annotations.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link LengthMeasureFormat} class.
 *
 * @author Peter S. Heijnen
 */
public class TestLengthMeasureFormat
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestLengthMeasureFormat.class.getName();

	/**
	 * Test parse and format using the {@link LengthMeasureFormat} class.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testParseAndFormat()
	throws Exception
	{
		final String where = CLASS_NAME + ".testParseAndFormat()";
		System.out.println( where );

		class Test
		{
			final String _text;

			final LengthMeasurePreferences _preferences;

			final Locale _locale;

			Test( final String text, final LengthMeasurePreferences preferences, final Locale locale )
			{
				_text = text;
				_preferences = preferences;
				_locale = locale;
			}
		}

		final LengthMeasurePreferences decimal4 = LengthMeasurePreferences.getMetricInstance();
		decimal4.setMinScale( 4 );

		final LengthMeasurePreferences decimalComma4 = LengthMeasurePreferences.getMetricInstance();
		decimalComma4.setMinScale( 4 );

		final LengthMeasurePreferences engineering4 = LengthMeasurePreferences.getEngineeringInstance();
		engineering4.setFeetZero( true );
		engineering4.setInchesZero( true );
		engineering4.setMinScale( 4 );

		final LengthMeasurePreferences engineeringComma4 = LengthMeasurePreferences.getEngineeringInstance();
		engineeringComma4.setFeetZero( true );
		engineeringComma4.setInchesZero( true );
		engineeringComma4.setMinScale( 4 );

		for ( final Test test : Arrays.asList(
		new Test( "1.5000", decimal4, Locale.ENGLISH ),
		new Test( "1,5000", decimalComma4, Locale.GERMAN ),
		new Test( "1500", LengthMeasurePreferences.getMetricInstance(), Locale.GERMAN ),
		new Test( "1500.0000", decimal4, Locale.ENGLISH ),
		new Test( "1500,0000", decimalComma4, Locale.GERMAN ),
		new Test( "-1500.0000", decimal4, Locale.ENGLISH ),
		new Test( "-1500,0000", decimalComma4, Locale.GERMAN ),
		new Test( "0'-1.5000\"", engineering4, Locale.ENGLISH ),
		new Test( "0'-1,5000\"", engineeringComma4, Locale.GERMAN ),
		new Test( "125'-0.0000\"", engineering4, Locale.ENGLISH ),
		new Test( "125'-0,0000\"", engineeringComma4, Locale.GERMAN ),
		new Test( "-125'-0.0000\"", engineering4, Locale.ENGLISH ),
		new Test( "-125'-0,0000\"", engineeringComma4, Locale.GERMAN ),
		new Test( "0'-1 1/2\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
		new Test( "-0'-1 1/2\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
		new Test( "125'-0\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
		new Test( "-125'-0\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
		new Test( "1 1/2", LengthMeasurePreferences.getFractionalInstance(), Locale.US ),
		new Test( "-1 1/2", LengthMeasurePreferences.getFractionalInstance(), Locale.US ),
		new Test( "5'", LengthMeasurePreferences.getArchitecturalInstance(), Locale.US ),
		new Test( "60\"", createArchitecturalPreferences( false, "-", " " ), Locale.US ),
		new Test( "5'-9\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
		new Test( "-5'-9\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
		new Test( "5' 9\"", createArchitecturalPreferences( true, " ", " " ), Locale.US ),
		new Test( "5'9\"", createArchitecturalPreferences( true, "", " " ), Locale.US ),
		new Test( "5'-1/2\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
		new Test( "5' 1/2\"", createArchitecturalPreferences( true, " ", " " ), Locale.US ),
		new Test( "5'1/2\"", createArchitecturalPreferences( true, "", " " ), Locale.US ),
		new Test( "5'-9-1/2\"", createArchitecturalPreferences( true, "-", "-" ), Locale.US ),
		new Test( "-5'-9-1/2\"", createArchitecturalPreferences( true, "-", "-" ), Locale.US ),
		new Test( "5' 9-1/2\"", createArchitecturalPreferences( true, " ", "-" ), Locale.US ),
		new Test( "5'9-1/2\"", createArchitecturalPreferences( true, "", "-" ), Locale.US ),
		new Test( "5'-9 1/2\"", createArchitecturalPreferences( true, "-", " " ), Locale.US ),
		new Test( "5' 9 1/2\"", createArchitecturalPreferences( true, " ", " " ), Locale.US ),
		new Test( "5'9 1/2\"", createArchitecturalPreferences( true, "", " " ), Locale.US ) ) )
		{
			final String text = test._text;
			final LengthMeasurePreferences preferences = test._preferences;
			final Locale locale = test._locale;
			System.out.println( " - Test: " + text );

			final LengthMeasureFormat measureFormat = preferences.getNumberFormat( locale );
			measureFormat.setUsingBigValues( true );

			final Number parsed1 = measureFormat.parse( text );
			assertEquals( "Format did not reproduce original text from parsed 'big' number: " + parsed1, text, measureFormat.format( parsed1 ) );

			measureFormat.setUsingBigValues( false );
			final Number parsed2 = measureFormat.parse( text );
			assertEquals( "Format did not reproduce original text from parsed 'non-big' number: " + parsed2, text, measureFormat.format( parsed2 ) );
		}
	}

	/**
	 * Create {@link LengthMeasurePreferences} based on {@link
	 * LengthMeasurePreferences#getArchitecturalInstance()}.
	 *
	 * @param feet                   Whether to include feet in output.
	 * @param feetInchesSeparator    Separator to insert between feet and
	 *                               inches.
	 * @param wholeFractionSeparator Separator to insert between whole and
	 *                               fraction.
	 *
	 * @return {@link LengthMeasurePreferences}.
	 */
	@NotNull
	private LengthMeasurePreferences createArchitecturalPreferences( final boolean feet, @NotNull final String feetInchesSeparator, @NotNull final String wholeFractionSeparator )
	{
		final LengthMeasurePreferences result = LengthMeasurePreferences.getArchitecturalInstance();
		result.setFeet( feet );
		result.setFeetZero( true );
		result.setFeetInchesSeparator( feetInchesSeparator );
		result.setInchesZero( true );
		result.setWholeFractionSeparator( wholeFractionSeparator );
		return result;
	}

	/**
	 * Test {@link LengthMeasureFormat#format} methods.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testFormat()
	throws Exception
	{
		final String where = CLASS_NAME + ".testFormat()";
		System.out.println( where );

		class Test
		{
			final Number _number;

			final String _text;

			final LengthMeasurePreferences _preferences;

			Test( final Number number, final String text, final LengthMeasurePreferences preferences )
			{
				_number = number;
				_text = text;
				_preferences = preferences;

			}
		}

		final Locale locale = Locale.US;

		final LengthMeasurePreferences decimal4 = LengthMeasurePreferences.getMetricInstance();
		decimal4.setMinScale( 4 );

		final LengthMeasurePreferences engineering4 = LengthMeasurePreferences.getEngineeringInstance();
		engineering4.setFeetZero( true );
		engineering4.setInchesZero( true );
		engineering4.setMinScale( 4 );

		for ( final Test test : Arrays.asList(
		new Test( new BigDecimal( "0.1" ), "0.1", LengthMeasurePreferences.getMetricInstance() ),
		new Test( new BigDecimal( "0.1" ), "0.1\"", LengthMeasurePreferences.getEngineeringInstance() ),
		new Test( new BigDecimal( "0.1" ), "3/32\"", LengthMeasurePreferences.getArchitecturalInstance() ),
		new Test( new BigDecimal( "0.1" ), "1/10", LengthMeasurePreferences.getFractionalInstance() ),
		new Test( new BigRational( 1, 3 ), "0.333333", LengthMeasurePreferences.getMetricInstance() ),
		new Test( new BigRational( 1, 3 ), "0.333333\"", LengthMeasurePreferences.getEngineeringInstance() ),
		new Test( new BigRational( 1, 3 ), "21/64\"", LengthMeasurePreferences.getArchitecturalInstance() ),
		new Test( new BigRational( 1, 3 ), "1/3", LengthMeasurePreferences.getFractionalInstance() ),
		new Test( new BigRational( 1, 4 ), "0.25", LengthMeasurePreferences.getMetricInstance() ),
		new Test( new BigRational( 1, 4 ), "0.25\"", LengthMeasurePreferences.getEngineeringInstance() ),
		new Test( new BigRational( 1, 4 ), "1/4\"", LengthMeasurePreferences.getArchitecturalInstance() ),
		new Test( new BigRational( 1, 4 ), "1/4", LengthMeasurePreferences.getFractionalInstance() ),
		new Test( new BigRational( 1, 5 ), "0.2", LengthMeasurePreferences.getMetricInstance() ),
		new Test( new BigRational( 1, 5 ), "0.2\"", LengthMeasurePreferences.getEngineeringInstance() ),
		new Test( new BigRational( 1, 5 ), "13/64\"", LengthMeasurePreferences.getArchitecturalInstance() ),
		new Test( new BigRational( 1, 5 ), "1/5", LengthMeasurePreferences.getFractionalInstance() ),
		new Test( new BigRational( 1, 6 ), "0.166667", LengthMeasurePreferences.getMetricInstance() ),
		new Test( new BigRational( 1, 6 ), "0.166667\"", LengthMeasurePreferences.getEngineeringInstance() ),
		new Test( new BigRational( 1, 6 ), "11/64\"", LengthMeasurePreferences.getArchitecturalInstance() ),
		new Test( new BigRational( 1, 6 ), "1/6", LengthMeasurePreferences.getFractionalInstance() )
		) )
		{
			final Number number = test._number;
			final LengthMeasurePreferences preferences = test._preferences;
			final String text = test._text;
			System.out.println( " - Test: " + number + " => " + text );

			final LengthMeasureFormat measureFormat = preferences.getNumberFormat( locale );
			assertEquals( "Failed to properly format number " + number, text, measureFormat.format( number ) );
		}
	}

	/**
	 * Test unit conversion.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testUnitConversion()
	throws Exception
	{
		final String where = CLASS_NAME + ".testUnitConversion()";
		System.out.println( where );

		class Test
		{
			final Number _number;

			final String _text;

			final LengthMeasurePreferences _preferences;

			Test( final Number number, final String text, final LengthMeasurePreferences preferences )
			{
				_number = number;
				_text = text;
				_preferences = preferences;

			}
		}

		final Locale locale = Locale.US;

		final LengthMeasurePreferences mmToInches = LengthMeasurePreferences.getArchitecturalInstance();
		mmToInches.setBaseUnit( LengthMeasurePreferences.LengthUnit.MILLIMETER );

		final LengthMeasurePreferences mmToCm = LengthMeasurePreferences.getMetricInstance();
		mmToCm.setBaseUnit( LengthMeasurePreferences.LengthUnit.MILLIMETER );
		mmToCm.setDisplayUnit( LengthMeasurePreferences.LengthUnit.CENTIMETER );

		for ( final Test test : Arrays.asList(
		new Test( 25.4, "1\"", mmToInches ),
		new Test( 25.4, "2.54", mmToCm )
		) )
		{
			final Number number = test._number;
			final LengthMeasurePreferences preferences = test._preferences;
			final String text = test._text;
			System.out.println( " - Test: " + number + " => " + text );

			final LengthMeasureFormat measureFormat = preferences.getNumberFormat( locale );
			final String formatted = measureFormat.format( number );
			assertEquals( "Failed to properly format number " + number, text, formatted );
			final Number parsed = measureFormat.parse( formatted );
			assertEquals( "Failed to properly format number " + number, parsed, number );
		}
	}
}
