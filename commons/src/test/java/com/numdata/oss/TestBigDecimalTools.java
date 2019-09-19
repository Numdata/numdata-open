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
package com.numdata.oss;

import java.math.*;
import java.util.*;

import static com.numdata.oss.BigDecimalTools.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link BigDecimalTools} class.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "UnpredictableBigDecimalConstructorCall" )
public class TestBigDecimalTools
{
	/**
	 * Test {@link BigDecimalTools#toBigDecimal} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testToBigDecimal()
	throws Exception
	{
		assertEquals( "Test 1", new BigDecimal( "123" ), toBigDecimal( 123 ) );
		assertEquals( "Test 2", new BigDecimal( "123" ), toBigDecimal( 123L ) );
		assertEquals( "Test 3", new BigDecimal( "123.45" ), toBigDecimal( 123.45 ) );
		assertEquals( "Test 4", new BigDecimal( "123.999999" ), toBigDecimal( 123.999999 ) );
		assertEquals( "Test 5", new BigDecimal( "124" ), toBigDecimal( 123.9999999 ) );
		assertEquals( "Test 6", new BigDecimal( "123.9999999" ), toBigDecimal( 123.9999999, 7 ) );
		assertEquals( "Test 7", new BigDecimal( "123.8889" ), toBigDecimal( 123.8888888, 4 ) );
		assertEquals( "Test 8", new BigDecimal( "124" ), toBigDecimal( 123.9999999, 3 ) );
		assertEquals( "Test 9", new BigDecimal( "0.124" ), toBigDecimal( 123.999999, 3, -3 ) );
		assertEquals( "Test 10", new BigDecimal( "123.457" ), toBigDecimal( 123456789.0, 3, -6 ) );
		assertEquals( "Test 11", new BigDecimal( "123456.789" ), toBigDecimal( 123456789.0, 3, -3 ) );
		assertEquals( "Test 12", new BigDecimal( "123456789" ), toBigDecimal( 123456789.0, 3, 0 ) );
		assertEquals( "Test 13", new BigDecimal( "123456789000" ), toBigDecimal( 123456789.0, 3, 3 ) );
		assertEquals( "Test 14", new BigDecimal( "123456789000000" ), toBigDecimal( 123456789.0, 3, 6 ) );
		assertEquals( "Test 15", new BigDecimal( "1" ), toBigDecimal( 0.5, 0 ) );
		assertEquals( "Test 16", new BigDecimal( "-1" ), toBigDecimal( -0.5, 0 ) );
		assertEquals( "Test 17", new BigDecimal( "123" ), toBigDecimal( 123.49999999, 0 ) );
	}

	/**
	 * Test {@link BigDecimalTools#isNegative} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testIsNegative()
	throws Exception
	{
		assertFalse( "Test 1", BigDecimalTools.isNegative( new BigDecimal( 0 ) ) );
		assertFalse( "Test 2", BigDecimalTools.isNegative( new BigDecimal( 0 ).negate() ) );
		assertFalse( "Test 3", BigDecimalTools.isNegative( new BigDecimal( 0.0 ) ) );
		assertFalse( "Test 4", BigDecimalTools.isNegative( new BigDecimal( "0.00" ) ) );
		assertFalse( "Test 5", BigDecimalTools.isNegative( new BigDecimal( -0.0 ) ) );
		assertFalse( "Test 6", BigDecimalTools.isNegative( new BigDecimal( "-0.0" ) ) );
		assertFalse( "Test 7", BigDecimalTools.isNegative( new BigDecimal( 1 ) ) );
		assertFalse( "Test 8", BigDecimalTools.isNegative( new BigDecimal( -1 ).negate() ) );
		assertFalse( "Test 9", BigDecimalTools.isNegative( new BigDecimal( "1.0" ) ) );
		assertFalse( "Test 10", BigDecimalTools.isNegative( new BigDecimal( 0.0000000000000000001 ) ) );
		assertFalse( "Test 11", BigDecimalTools.isNegative( new BigDecimal( Long.MAX_VALUE ) ) );
		assertTrue( "Test 12", BigDecimalTools.isNegative( new BigDecimal( -1 ) ) );
		assertTrue( "Test 13", BigDecimalTools.isNegative( new BigDecimal( 1 ).negate() ) );
		assertTrue( "Test 14", BigDecimalTools.isNegative( new BigDecimal( "-1.0" ) ) );
		assertTrue( "Test 15", BigDecimalTools.isNegative( new BigDecimal( -0.0000000000000000001 ) ) );
		assertTrue( "Test 16", BigDecimalTools.isNegative( new BigDecimal( Long.MIN_VALUE ) ) );
	}

	/**
	 * Test {@link BigDecimalTools#isZero} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testIsZero()
	throws Exception
	{
		assertTrue( "Test 1", BigDecimalTools.isZero( new BigDecimal( 0 ) ) );
		assertTrue( "Test 2", BigDecimalTools.isZero( new BigDecimal( 0 ).negate() ) );
		assertTrue( "Test 3", BigDecimalTools.isZero( new BigDecimal( 0.0 ) ) );
		assertTrue( "Test 4", BigDecimalTools.isZero( new BigDecimal( "0.00" ) ) );
		assertTrue( "Test 5", BigDecimalTools.isZero( new BigDecimal( -0.0 ) ) );
		assertTrue( "Test 6", BigDecimalTools.isZero( new BigDecimal( "-0.0" ) ) );
		assertFalse( "Test 7", BigDecimalTools.isZero( new BigDecimal( 1 ) ) );
		assertFalse( "Test 8", BigDecimalTools.isZero( new BigDecimal( -1 ).negate() ) );
		assertFalse( "Test 9", BigDecimalTools.isZero( new BigDecimal( "1.0" ) ) );
		assertFalse( "Test 10", BigDecimalTools.isZero( new BigDecimal( 0.0000000000000000001 ) ) );
		assertFalse( "Test 11", BigDecimalTools.isZero( new BigDecimal( Long.MAX_VALUE ) ) );
		assertFalse( "Test 12", BigDecimalTools.isZero( new BigDecimal( -1 ) ) );
		assertFalse( "Test 13", BigDecimalTools.isZero( new BigDecimal( 1 ).negate() ) );
		assertFalse( "Test 14", BigDecimalTools.isZero( new BigDecimal( "-1.0" ) ) );
		assertFalse( "Test 15", BigDecimalTools.isZero( new BigDecimal( -0.0000000000000000001 ) ) );
		assertFalse( "Test 16", BigDecimalTools.isZero( new BigDecimal( Long.MIN_VALUE ) ) );
	}

	/**
	 * Test {@link BigDecimalTools#isPositive} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testIsPositive()
	throws Exception
	{
		assertFalse( "Test 1", BigDecimalTools.isPositive( new BigDecimal( 0 ) ) );
		assertFalse( "Test 2", BigDecimalTools.isPositive( new BigDecimal( 0 ).negate() ) );
		assertFalse( "Test 3", BigDecimalTools.isPositive( new BigDecimal( 0.0 ) ) );
		assertFalse( "Test 4", BigDecimalTools.isPositive( new BigDecimal( "0.00" ) ) );
		assertFalse( "Test 5", BigDecimalTools.isPositive( new BigDecimal( -0.0 ) ) );
		assertFalse( "Test 6", BigDecimalTools.isPositive( new BigDecimal( "-0.0" ) ) );
		assertTrue( "Test 7", BigDecimalTools.isPositive( new BigDecimal( 1 ) ) );
		assertTrue( "Test 8", BigDecimalTools.isPositive( new BigDecimal( -1 ).negate() ) );
		assertTrue( "Test 9", BigDecimalTools.isPositive( new BigDecimal( "1.0" ) ) );
		assertTrue( "Test 10", BigDecimalTools.isPositive( new BigDecimal( 0.0000000000000000001 ) ) );
		assertTrue( "Test 11", BigDecimalTools.isPositive( new BigDecimal( Long.MAX_VALUE ) ) );
		assertFalse( "Test 12", BigDecimalTools.isPositive( new BigDecimal( -1 ) ) );
		assertFalse( "Test 13", BigDecimalTools.isPositive( new BigDecimal( 1 ).negate() ) );
		assertFalse( "Test 14", BigDecimalTools.isPositive( new BigDecimal( "-1.0" ) ) );
		assertFalse( "Test 15", BigDecimalTools.isPositive( new BigDecimal( -0.0000000000000000001 ) ) );
		assertFalse( "Test 16", BigDecimalTools.isPositive( new BigDecimal( Long.MIN_VALUE ) ) );
	}

	/**
	 * Test {@link BigDecimalTools#isEqual} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testIsEqual()
	throws Exception
	{
		assertTrue( "Test 1", BigDecimalTools.isEqual( new BigDecimal( "0" ), new BigDecimal( "0.0" ) ) );
		//noinspection BigDecimalEquals
		assertFalse( "Test 2", new BigDecimal( "0" ).equals( new BigDecimal( "0.0" ) ) );
	}

	/**
	 * Test {@link BigDecimalTools#roundCents} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testRoundCents()
	throws Exception
	{
		assertEquals( "Test 1", new BigDecimal( "123.45" ), roundCents( new BigDecimal( "123.453" ) ) );
		assertEquals( "Test 2", new BigDecimal( "123.45" ), roundCents( new BigDecimal( "123.454" ) ) );
		assertEquals( "Test 3", new BigDecimal( "123.46" ), roundCents( new BigDecimal( "123.455" ) ) );
		assertEquals( "Test 4", new BigDecimal( "123.46" ), roundCents( new BigDecimal( "123.456" ) ) );
		assertEquals( "Test 5", new BigDecimal( "123.46" ), roundCents( new BigDecimal( "123.463" ) ) );
		assertEquals( "Test 6", new BigDecimal( "123.46" ), roundCents( new BigDecimal( "123.464" ) ) ); /* test HALF_EVEN / HALF_UP difference */
		assertEquals( "Test 7", new BigDecimal( "123.46" ), roundCents( new BigDecimal( "123.465" ) ) );
		assertEquals( "Test 8", new BigDecimal( "123.47" ), roundCents( new BigDecimal( "123.466" ) ) );
		assertEquals( "Test 9", new BigDecimal( "-123.45" ), roundCents( new BigDecimal( "-123.453" ) ) );
		assertEquals( "Test 10", new BigDecimal( "-123.45" ), roundCents( new BigDecimal( "-123.454" ) ) );
		assertEquals( "Test 11", new BigDecimal( "-123.46" ), roundCents( new BigDecimal( "-123.455" ) ) );
		assertEquals( "Test 12", new BigDecimal( "-123.46" ), roundCents( new BigDecimal( "-123.456" ) ) );
		assertEquals( "Test 13", new BigDecimal( "-123.46" ), roundCents( new BigDecimal( "-123.463" ) ) );
		assertEquals( "Test 14", new BigDecimal( "-123.46" ), roundCents( new BigDecimal( "-123.464" ) ) ); /* test HALF_EVEN / HALF_UP difference */
		assertEquals( "Test 15", new BigDecimal( "-123.46" ), roundCents( new BigDecimal( "-123.465" ) ) );
		assertEquals( "Test 16", new BigDecimal( "-123.47" ), roundCents( new BigDecimal( "-123.466" ) ) );
		assertEquals( "Test 17", new BigDecimal( "123" ), roundCents( new BigDecimal( "123" ) ) );
		assertEquals( "Test 17", new BigDecimal( "123.00" ), roundCents( new BigDecimal( "123.00" ) ) );
		assertEquals( "Test 18", new BigDecimal( "1.23E+5" ), roundCents( new BigDecimal( "123E+3" ) ) );
	}

	/**
	 * Test {@link BigDecimalTools#getPercentage} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testGetPercentage()
	throws Exception
	{
		assertEquals( "Test 1", new BigDecimal( "1.23456" ), getPercentage( new BigDecimal( "123.456" ), new BigDecimal( 1 ) ) );
		assertEquals( "Test 2", new BigDecimal( "12.34560" ), getPercentage( new BigDecimal( "123.456" ), new BigDecimal( 10 ) ) );
		assertEquals( "Test 3", new BigDecimal( "123.45600" ), getPercentage( new BigDecimal( "123.456" ), new BigDecimal( 100 ) ) );
		assertEquals( "Test 4", new BigDecimal( "21.604800" ), getPercentage( new BigDecimal( "123.456" ), new BigDecimal( "17.5" ) ) );
	}

	/**
	 * Test {@link BigDecimalTools#getPercentFactor} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testGetPercentFactor()
	throws Exception
	{
		assertEquals( "Test 1", new BigDecimal( "0.01" ), getPercentFactor( new BigDecimal( 1 ) ) );
		assertEquals( "Test 2", new BigDecimal( "0.10" ), getPercentFactor( new BigDecimal( 10 ) ) );
		assertEquals( "Test 3", new BigDecimal( "1.00" ), getPercentFactor( new BigDecimal( 100 ) ) );
		assertEquals( "Test 4", new BigDecimal( "0.175" ), getPercentFactor( new BigDecimal( "17.5" ) ) );
	}

	/**
	 * Test {@link BigDecimalTools#parse} method.
	 *
	 * @throws Exception if test fails.
	 */
	@Test
	public void testParse()
	throws Exception
	{
		assertEquals( "Test 1", new BigDecimal( "123456.789" ), parse( Locale.US, "123,456.789" ) );
		assertEquals( "Test 2", new BigDecimal( "123456.789" ), parse( Locale.GERMANY, "123.456,789" ) );
	}
}
