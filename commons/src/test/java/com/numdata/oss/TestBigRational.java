/* BigRational.java -- dynamically sized big rational numbers.
**
** Copyright (C) 2002-2010 Eric Laroche.  All rights reserved.
**
** @author Eric Laroche <laroche@lrdev.com>
** @version @(#)$Id: BigRational.java,v 1.3 2010/03/24 20:11:34 laroche Exp $
**
** This program is free software;
** you can redistribute it and/or modify it.
**
** This program is distributed in the hope that it will be useful,
** but WITHOUT ANY WARRANTY; without even the implied warranty of
** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
**
*/

package com.numdata.oss;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link BigRational}.
 *
 * @author Eric Laroche &lt;laroche@lrdev.com&gt;
 * @version @(#)$Id: BigRational.java,v 1.3 2010/03/24 20:11:34 laroche Exp $
 */
public class TestBigRational
{
	/**
	 * Run tests.
	 */
	@Test
	public void test()
	{
		// implementation can be commented out.

		// note: don't use C style comments here,
		// in order to be able to comment the test code out altogether.

		// note: only testing the _public_ interfaces.
		// typically not testing the aliases.

		// BigRational default radix
		// well, this makes sense, doesn't it?
		assertTrue( BigRational.DEFAULT_RADIX == 10 );

		// note: we're not using BigIntegers in these tests

		// constructors

		// "BigRational zero quotient"
		try
		{
			new BigRational( 2, 0 );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}

		// "BigRational equality"

		// long/long ctor
		assertTrue( ( new BigRational( 21, 35 ) ).equals( new BigRational( 3, 5 ) ) );
		assertTrue( ( new BigRational( -21, 35 ) ).equals( new BigRational( -3, 5 ) ) );
		assertTrue( ( new BigRational( -21, 35 ) ).equals( new BigRational( 3, -5 ) ) );
		assertTrue( ( new BigRational( 21, -35 ) ).equals( new BigRational( -3, 5 ) ) );
		assertTrue( ( new BigRational( -21, -35 ) ).equals( new BigRational( 3, 5 ) ) );

		// long ctor
		assertTrue( ( new BigRational( 1 ) ).equals( new BigRational( 1 ) ) );
		assertTrue( ( new BigRational( 0 ) ).equals( new BigRational( 0 ) ) );
		assertTrue( ( new BigRational( 2 ) ).equals( new BigRational( 2 ) ) );
		assertTrue( ( new BigRational( -1 ) ).equals( new BigRational( -1 ) ) );

		// string ctors

		// "BigRational normalization"

		assertTrue( ( new BigRational( "11" ) ).toString().equals( "11" ) );
		assertTrue( ( new BigRational( "-11" ) ).toString().equals( "-11" ) );
		assertTrue( ( new BigRational( "+11" ) ).toString().equals( "11" ) );

		assertTrue( ( new BigRational( "21/35" ) ).toString().equals( "3/5" ) );
		assertTrue( ( new BigRational( "-21/35" ) ).toString().equals( "-3/5" ) );
		assertTrue( ( new BigRational( "21/-35" ) ).toString().equals( "-3/5" ) );
		// special, but defined
		assertTrue( ( new BigRational( "-21/-35" ) ).toString().equals( "3/5" ) );
		assertTrue( ( new BigRational( "+21/35" ) ).toString().equals( "3/5" ) );
		// special, but defined
		assertTrue( ( new BigRational( "21/+35" ) ).toString().equals( "3/5" ) );
		// special, but defined
		assertTrue( ( new BigRational( "+21/+35" ) ).toString().equals( "3/5" ) );

		// "BigRational special formats"
		// 1/x
		assertTrue( ( new BigRational( "/3" ) ).toString().equals( "1/3" ) );
		assertTrue( ( new BigRational( "-/3" ) ).toString().equals( "-1/3" ) );
		assertTrue( ( new BigRational( "/-3" ) ).toString().equals( "-1/3" ) );
		// x/1
		assertTrue( ( new BigRational( "3/" ) ).toString().equals( "3" ) );
		assertTrue( ( new BigRational( "-3/" ) ).toString().equals( "-3" ) );
		assertTrue( ( new BigRational( "3/-" ) ).toString().equals( "-3" ) );
		assertTrue( ( new BigRational( "-3/-" ) ).toString().equals( "3" ) );
		// special, but defined
		assertTrue( ( new BigRational( "/" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "-/" ) ).toString().equals( "-1" ) );
		assertTrue( ( new BigRational( "/-" ) ).toString().equals( "-1" ) );
		// even more special, but defined
		assertTrue( ( new BigRational( "-/-" ) ).toString().equals( "1" ) );

		// "BigRational normalization"
		assertTrue( ( new BigRational( "3.4" ) ).toString().equals( "17/5" ) );
		assertTrue( ( new BigRational( "-3.4" ) ).toString().equals( "-17/5" ) );
		assertTrue( ( new BigRational( "+3.4" ) ).toString().equals( "17/5" ) );

		// "BigRational missing leading/trailing zero"
		assertTrue( ( new BigRational( "5." ) ).toString().equals( "5" ) );
		assertTrue( ( new BigRational( "-5." ) ).toString().equals( "-5" ) );
		assertTrue( ( new BigRational( ".7" ) ).toString().equals( "7/10" ) );
		assertTrue( ( new BigRational( "-.7" ) ).toString().equals( "-7/10" ) );
		// special, but defined
		assertTrue( ( new BigRational( "." ) ).toString().equals( "0" ) );
		assertTrue( ( new BigRational( "-." ) ).toString().equals( "0" ) );
		assertTrue( ( new BigRational( "-" ) ).toString().equals( "0" ) );
		// special, but defined
		assertTrue( ( new BigRational( "" ) ).toString().equals( "0" ) );

		// "BigRational radix"
		assertTrue( ( new BigRational( "f/37", 0x10 ) ).toString().equals( "3/11" ) );
		assertTrue( ( new BigRational( "f.37", 0x10 ) ).toString().equals( "3895/256" ) );
		assertTrue( ( new BigRational( "-dcba.efgh", 23 ) ).toString().equals( "-46112938320/279841" ) );
		assertTrue( ( new BigRational( "1011101011010110", 2 ) ).toString( 0x10 ).equals( "bad6" ) );

		// illegal radixes
		try
		{
			new BigRational( "101", 1 );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "101", 0 );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "101", -1 );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "101", -2 );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			( new BigRational( "33" ) ).toString( 1 );
			assertTrue( false );
		}
		catch ( IllegalArgumentException e )
		{
			// empty
		}
		try
		{
			( new BigRational( "33" ) ).toString( 0 );
			assertTrue( false );
		}
		catch ( IllegalArgumentException e )
		{
			// empty
		}
		try
		{
			( new BigRational( "33" ) ).toString( -1 );
			assertTrue( false );
		}
		catch ( IllegalArgumentException e )
		{
			// empty
		}
		try
		{
			( new BigRational( "33" ) ).toString( -2 );
			assertTrue( false );
		}
		catch ( IllegalArgumentException e )
		{
			// empty
		}
		try
		{
			( new BigRational( "33" ) ).toStringDot( 4, 1 );
			assertTrue( false );
		}
		catch ( IllegalArgumentException e )
		{
			// empty
		}
		try
		{
			( new BigRational( "33" ) ).toStringDotRelative( 4, 1 );
			assertTrue( false );
		}
		catch ( IllegalArgumentException e )
		{
			// empty
		}
		try
		{
			( new BigRational( "33" ) ).toStringExponent( 4, 1 );
			assertTrue( false );
		}
		catch ( IllegalArgumentException e )
		{
			// empty
		}

		// "BigRational slash and dot"
		assertTrue( ( new BigRational( "2.5/3" ) ).toString().equals( "5/6" ) );
		assertTrue( ( new BigRational( "2/3.5" ) ).toString().equals( "4/7" ) );
		assertTrue( ( new BigRational( "2.5/3.5" ) ).toString().equals( "5/7" ) );
		assertTrue( ( new BigRational( "-2.5/3" ) ).toString().equals( "-5/6" ) );
		assertTrue( ( new BigRational( "-2/3.5" ) ).toString().equals( "-4/7" ) );
		assertTrue( ( new BigRational( "-2.5/3.5" ) ).toString().equals( "-5/7" ) );
		assertTrue( ( new BigRational( "2.5/-3" ) ).toString().equals( "-5/6" ) );
		assertTrue( ( new BigRational( "2/-3.5" ) ).toString().equals( "-4/7" ) );
		assertTrue( ( new BigRational( "2.5/-3.5" ) ).toString().equals( "-5/7" ) );
		assertTrue( ( new BigRational( "-2.5/-3" ) ).toString().equals( "5/6" ) );
		assertTrue( ( new BigRational( "-2/-3.5" ) ).toString().equals( "4/7" ) );
		assertTrue( ( new BigRational( "-2.5/-3.5" ) ).toString().equals( "5/7" ) );

		// "BigRational multiple signs, embedded signs"
		try
		{
			new BigRational( "+-2/3" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "-+2/3" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "++2/3" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "--2/3" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "2-/3" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "2+/3" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}

		// "BigRational sign in fraction"
		try
		{
			new BigRational( "2.+3" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "2.-3" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "2.3+" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( "2.3-" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}

		// slash nesting
		try
		{
			assertTrue( ( new BigRational( "2/3/5" ) ).toString().equals( "2/15" ) );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		// assertTrue((new BigRational("2/3/5/7")).toString().equals("2/105"));
		// similar tests with sign, dot, etc, left out for the time being

		// representations with exponent
		assertTrue( ( new BigRational( "1E2" ) ).toString().equals( "100" ) );
		assertTrue( ( new BigRational( "1E1" ) ).toString().equals( "10" ) );
		assertTrue( ( new BigRational( "1E0" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "1E-1" ) ).toString().equals( "1/10" ) );
		assertTrue( ( new BigRational( "1E-2" ) ).toString().equals( "1/100" ) );
		assertTrue( ( new BigRational( "-65.4E-3" ) ).toString().equals( "-327/5000" ) );
		assertTrue( ( new BigRational( "7.89629601826806E13" ) ).toStringDot( 4 ).equals( "78962960182680.6000" ) );

		// special representations with exponent

		assertTrue( ( new BigRational( "1E+1" ) ).toString().equals( "10" ) );
		assertTrue( ( new BigRational( "1E+0" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "1E-0" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "1E" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "1E+" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "1E-" ) ).toString().equals( "1" ) );

		assertTrue( ( new BigRational( "77E+1" ) ).toString().equals( "770" ) );
		assertTrue( ( new BigRational( "77E+0" ) ).toString().equals( "77" ) );
		assertTrue( ( new BigRational( "77E-0" ) ).toString().equals( "77" ) );
		assertTrue( ( new BigRational( "77E" ) ).toString().equals( "77" ) );
		assertTrue( ( new BigRational( "77E+" ) ).toString().equals( "77" ) );
		assertTrue( ( new BigRational( "77E-" ) ).toString().equals( "77" ) );

		assertTrue( ( new BigRational( "E" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "E+1" ) ).toString().equals( "10" ) );
		assertTrue( ( new BigRational( "E+0" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "E-0" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "E+" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "E-" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "+E" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "+E+1" ) ).toString().equals( "10" ) );
		assertTrue( ( new BigRational( "+E+0" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "+E-0" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "+E+" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "+E-" ) ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( "-E" ) ).toString().equals( "-1" ) );
		assertTrue( ( new BigRational( "-E+1" ) ).toString().equals( "-10" ) );
		assertTrue( ( new BigRational( "-E+0" ) ).toString().equals( "-1" ) );
		assertTrue( ( new BigRational( "-E-0" ) ).toString().equals( "-1" ) );
		assertTrue( ( new BigRational( "-E+" ) ).toString().equals( "-1" ) );
		assertTrue( ( new BigRational( "-E-" ) ).toString().equals( "-1" ) );

		assertTrue( ( new BigRational( "12.34/56.78" ) ).toString().equals( "617/2839" ) );
		assertTrue( ( new BigRational( "12.34E1/56.78" ) ).toString().equals( "6170/2839" ) );
		assertTrue( ( new BigRational( "12.34/56.78E1" ) ).toString().equals( "617/28390" ) );
		assertTrue( ( new BigRational( "12.34E1/56.78E1" ) ).toString().equals( "617/2839" ) );
		assertTrue( ( new BigRational( "12.34E-1/56.78" ) ).toString().equals( "617/28390" ) );
		assertTrue( ( new BigRational( "12.34/56.78E-1" ) ).toString().equals( "6170/2839" ) );
		assertTrue( ( new BigRational( "12.34E-1/56.78E-1" ) ).toString().equals( "617/2839" ) );
		assertTrue( ( new BigRational( "-12.34/56.78" ) ).toString().equals( "-617/2839" ) );
		assertTrue( ( new BigRational( "-12.34E1/56.78" ) ).toString().equals( "-6170/2839" ) );
		assertTrue( ( new BigRational( "-12.34/56.78E1" ) ).toString().equals( "-617/28390" ) );
		assertTrue( ( new BigRational( "-12.34E1/56.78E1" ) ).toString().equals( "-617/2839" ) );
		assertTrue( ( new BigRational( "-12.34E-1/56.78" ) ).toString().equals( "-617/28390" ) );
		assertTrue( ( new BigRational( "-12.34/56.78E-1" ) ).toString().equals( "-6170/2839" ) );
		assertTrue( ( new BigRational( "-12.34E-1/56.78E-1" ) ).toString().equals( "-617/2839" ) );
		assertTrue( ( new BigRational( "12.34/-56.78" ) ).toString().equals( "-617/2839" ) );
		assertTrue( ( new BigRational( "12.34E1/-56.78" ) ).toString().equals( "-6170/2839" ) );
		assertTrue( ( new BigRational( "12.34/-56.78E1" ) ).toString().equals( "-617/28390" ) );
		assertTrue( ( new BigRational( "12.34E1/-56.78E1" ) ).toString().equals( "-617/2839" ) );
		assertTrue( ( new BigRational( "12.34E-1/-56.78" ) ).toString().equals( "-617/28390" ) );
		assertTrue( ( new BigRational( "12.34/-56.78E-1" ) ).toString().equals( "-6170/2839" ) );
		assertTrue( ( new BigRational( "12.34E-1/-56.78E-1" ) ).toString().equals( "-617/2839" ) );
		assertTrue( ( new BigRational( "-12.34/-56.78" ) ).toString().equals( "617/2839" ) );
		assertTrue( ( new BigRational( "-12.34E1/-56.78" ) ).toString().equals( "6170/2839" ) );
		assertTrue( ( new BigRational( "-12.34/-56.78E1" ) ).toString().equals( "617/28390" ) );
		assertTrue( ( new BigRational( "-12.34E1/-56.78E1" ) ).toString().equals( "617/2839" ) );
		assertTrue( ( new BigRational( "-12.34E-1/-56.78" ) ).toString().equals( "617/28390" ) );
		assertTrue( ( new BigRational( "-12.34/-56.78E-1" ) ).toString().equals( "6170/2839" ) );
		assertTrue( ( new BigRational( "-12.34E-1/-56.78E-1" ) ).toString().equals( "617/2839" ) );

		// fractional exponent
		try
		{
			new BigRational( "1E2.5" );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}

		// exponent overflow
		try
		{
			new BigRational( "1E" + ( (long)Integer.MAX_VALUE * 3 ) );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}

		// exponent nesting
		try
		{
			assertTrue( ( new BigRational( "1E2E3" ) ).equals( ( new BigRational( 10 ) ).power( 2000 ) ) );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		// assertTrue((new BigRational("1E2E3")).equals(new BigRational("1E2000")));
		// assertTrue((new BigRational("-1E2E3")).equals(new BigRational("-1E2000")));
		// assertTrue((new BigRational("1E-2E3")).equals(new BigRational("1E-2000")));
		// assertTrue((new BigRational("-1E-2E3")).equals(new BigRational("-1E-2000")));
		// // assertTrue((new BigRational("1E2E-3")).equals(new BigRational("1E0.002")));

		// null
		try
		{
			new BigRational( (String)null );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}

		// NaN, Infinities
		try
		{
			new BigRational( String.valueOf( Double.NaN ) );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( String.valueOf( Double.POSITIVE_INFINITY ) );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			new BigRational( String.valueOf( Double.NEGATIVE_INFINITY ) );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}

		// clone
		// [clone() not so much needed, since the BigRational objects are immutable]
		assertTrue( BigRational.ONE == BigRational.ONE );
		assertTrue( BigRational.ONE.equals( BigRational.ONE ) );
		try
		{
			assertTrue( BigRational.ONE.clone() != BigRational.ONE );
			assertTrue( BigRational.ONE.clone().equals( BigRational.ONE ) );
			assertTrue( BigRational.ONE.equals( BigRational.ONE.clone() ) );
		}
		catch ( CloneNotSupportedException e )
		{
			assertTrue( false );
		}
		assertTrue( new BigRational( BigRational.ONE ) != BigRational.ONE );
		assertTrue( ( new BigRational( BigRational.ONE ) ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.equals( new BigRational( BigRational.ONE ) ) );

		// scaling

		// zero scale
		// "BigRational normalization"
		assertTrue( ( new BigRational( 123456, 0, BigRational.DEFAULT_RADIX ) ).toString().equals( "123456" ) );
		assertTrue( ( new BigRational( 123456, 1, BigRational.DEFAULT_RADIX ) ).toString().equals( "61728/5" ) );
		assertTrue( ( new BigRational( 123456, 2, BigRational.DEFAULT_RADIX ) ).toString().equals( "30864/25" ) );
		assertTrue( ( new BigRational( 123456, 5, BigRational.DEFAULT_RADIX ) ).toString().equals( "3858/3125" ) );
		// <1
		assertTrue( ( new BigRational( 123456, 6, BigRational.DEFAULT_RADIX ) ).toString().equals( "1929/15625" ) );
		// negative scale
		assertTrue( ( new BigRational( 123456, -1, BigRational.DEFAULT_RADIX ) ).toString().equals( "1234560" ) );
		assertTrue( ( new BigRational( 123456, -2, BigRational.DEFAULT_RADIX ) ).toString().equals( "12345600" ) );

		// "BigRational constants"
		assertTrue( BigRational.ZERO.toString().equals( "0" ) );
		assertTrue( BigRational.ONE.toString().equals( "1" ) );
		assertTrue( BigRational.MINUS_ONE.toString().equals( "-1" ) );

		// predicates (isPositive, isNegative, isZero, isOne, isMinusOne, isInteger)

		assertTrue( !BigRational.ZERO.isPositive() );
		assertTrue( !( new BigRational( 0 ) ).isPositive() );
		assertTrue( BigRational.ONE.isPositive() );
		assertTrue( ( new BigRational( 1 ) ).isPositive() );
		assertTrue( !BigRational.MINUS_ONE.isPositive() );
		assertTrue( !( new BigRational( -1 ) ).isPositive() );
		assertTrue( ( new BigRational( 77 ) ).isPositive() );
		assertTrue( !( new BigRational( -77 ) ).isPositive() );
		assertTrue( ( new BigRational( 3, 5 ) ).isPositive() );
		assertTrue( !( new BigRational( -3, 5 ) ).isPositive() );

		assertTrue( !BigRational.ZERO.isNegative() );
		assertTrue( !( new BigRational( 0 ) ).isNegative() );
		assertTrue( !BigRational.ONE.isNegative() );
		assertTrue( !( new BigRational( 1 ) ).isNegative() );
		assertTrue( BigRational.MINUS_ONE.isNegative() );
		assertTrue( ( new BigRational( -1 ) ).isNegative() );
		assertTrue( !( new BigRational( 77 ) ).isNegative() );
		assertTrue( ( new BigRational( -77 ) ).isNegative() );
		assertTrue( !( new BigRational( 3, 5 ) ).isNegative() );
		assertTrue( ( new BigRational( -3, 5 ) ).isNegative() );

		assertTrue( BigRational.ZERO.isZero() );
		assertTrue( ( new BigRational( 0 ) ).isZero() );
		assertTrue( !BigRational.ONE.isZero() );
		assertTrue( !( new BigRational( 1 ) ).isZero() );
		assertTrue( !BigRational.MINUS_ONE.isZero() );
		assertTrue( !( new BigRational( -1 ) ).isZero() );
		assertTrue( !( new BigRational( 77 ) ).isZero() );
		assertTrue( !( new BigRational( -77 ) ).isZero() );
		assertTrue( !( new BigRational( 3, 5 ) ).isZero() );
		assertTrue( !( new BigRational( -3, 5 ) ).isZero() );

		assertTrue( !BigRational.ZERO.isOne() );
		assertTrue( !( new BigRational( 0 ) ).isOne() );
		assertTrue( BigRational.ONE.isOne() );
		assertTrue( ( new BigRational( 1 ) ).isOne() );
		assertTrue( !BigRational.MINUS_ONE.isOne() );
		assertTrue( !( new BigRational( -1 ) ).isOne() );
		assertTrue( !( new BigRational( 77 ) ).isOne() );
		assertTrue( !( new BigRational( -77 ) ).isOne() );
		assertTrue( !( new BigRational( 3, 5 ) ).isOne() );
		assertTrue( !( new BigRational( -3, 5 ) ).isOne() );

		assertTrue( !BigRational.ZERO.isMinusOne() );
		assertTrue( !( new BigRational( 0 ) ).isMinusOne() );
		assertTrue( !BigRational.ONE.isMinusOne() );
		assertTrue( !( new BigRational( 1 ) ).isMinusOne() );
		assertTrue( BigRational.MINUS_ONE.isMinusOne() );
		assertTrue( ( new BigRational( -1 ) ).isMinusOne() );
		assertTrue( !( new BigRational( 77 ) ).isMinusOne() );
		assertTrue( !( new BigRational( -77 ) ).isMinusOne() );
		assertTrue( !( new BigRational( 3, 5 ) ).isMinusOne() );
		assertTrue( !( new BigRational( -3, 5 ) ).isMinusOne() );

		assertTrue( BigRational.ZERO.isInteger() );
		assertTrue( ( new BigRational( 0 ) ).isInteger() );
		assertTrue( BigRational.ONE.isInteger() );
		assertTrue( ( new BigRational( 1 ) ).isInteger() );
		assertTrue( BigRational.MINUS_ONE.isInteger() );
		assertTrue( ( new BigRational( -1 ) ).isInteger() );
		assertTrue( ( new BigRational( 77 ) ).isInteger() );
		assertTrue( ( new BigRational( -77 ) ).isInteger() );
		assertTrue( !( new BigRational( 3, 5 ) ).isInteger() );
		assertTrue( !( new BigRational( -3, 5 ) ).isInteger() );

		// "BigRational string dot"

		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( 4 ).equals( "1234.5678" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( 5 ).equals( "1234.56780" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( 6 ).equals( "1234.567800" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( 3 ).equals( "1234.568" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( 2 ).equals( "1234.57" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( 1 ).equals( "1234.6" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( 0 ).equals( "1235" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( -1 ).equals( "1230" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( -2 ).equals( "1200" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( -3 ).equals( "1000" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( -4 ).equals( "0" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( -5 ).equals( "0" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDot( -6 ).equals( "0" ) );

		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDot( -2 ).equals( "8800" ) );
		assertTrue( ( new BigRational( "0.0148" ) ).toStringDot( 6 ).equals( "0.014800" ) );
		assertTrue( ( new BigRational( "0.0148" ) ).toStringDot( 4 ).equals( "0.0148" ) );
		assertTrue( ( new BigRational( "0.0148" ) ).toStringDot( 3 ).equals( "0.015" ) );
		assertTrue( ( new BigRational( "0.0148" ) ).toStringDot( 2 ).equals( "0.01" ) );
		assertTrue( ( new BigRational( "0.0148" ) ).toStringDot( 1 ).equals( "0.0" ) );
		assertTrue( ( new BigRational( "0.001" ) ).toStringDot( 4 ).equals( "0.0010" ) );
		assertTrue( ( new BigRational( "0.001" ) ).toStringDot( 3 ).equals( "0.001" ) );
		assertTrue( ( new BigRational( "0.001" ) ).toStringDot( 2 ).equals( "0.00" ) );
		assertTrue( ( new BigRational( "0.001" ) ).toStringDot( 1 ).equals( "0.0" ) );
		assertTrue( ( new BigRational( "0.001" ) ).toStringDot( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.001" ) ).toStringDot( -1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.001" ) ).toStringDot( -2 ).equals( "0" ) );

		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( 4 ).equals( "-1234.5678" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( 5 ).equals( "-1234.56780" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( 6 ).equals( "-1234.567800" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( 3 ).equals( "-1234.568" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( 2 ).equals( "-1234.57" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( 1 ).equals( "-1234.6" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( 0 ).equals( "-1235" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( -1 ).equals( "-1230" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( -2 ).equals( "-1200" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( -3 ).equals( "-1000" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( -4 ).equals( "0" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDot( -5 ).equals( "0" ) );

		assertTrue( ( new BigRational( "0.0" ) ).toStringDot( 4 ).equals( "0.0000" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDot( 3 ).equals( "0.000" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDot( 2 ).equals( "0.00" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDot( 1 ).equals( "0.0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDot( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDot( -1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDot( -2 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDot( -3 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDot( -4 ).equals( "0" ) );

		// "BigRational string dot radix"
		assertTrue( ( new BigRational( "1234.5678", 20 ) ).toStringDot( 3, 20 ).equals( "1234.567" ) );
		assertTrue( ( new BigRational( "abcd.5b7g", 20 ) ).toStringDot( -2, 20 ).equals( "ac00" ) );

		// logarithm; rounded floor
		// commented out since method is not public
		// assertTrue((new BigRational("1")).logarithm(10) == 0);
		// assertTrue((new BigRational("1.0001")).logarithm(10) == 0);
		// assertTrue((new BigRational("1.1")).logarithm(10) == 0);
		// assertTrue((new BigRational("2")).logarithm(10) == 0);
		// assertTrue((new BigRational("5")).logarithm(10) == 0);
		// assertTrue((new BigRational("9.999")).logarithm(10) == 0);
		// assertTrue((new BigRational("10")).logarithm(10) == 1);
		// assertTrue((new BigRational("10.0001")).logarithm(10) == 1);
		// assertTrue((new BigRational("99")).logarithm(10) == 1);
		// assertTrue((new BigRational("100")).logarithm(10) == 2);
		// assertTrue((new BigRational("999")).logarithm(10) == 2);
		// assertTrue((new BigRational("1000")).logarithm(10) == 3);
		// assertTrue((new BigRational(".5")).logarithm(10) == -1);
		// assertTrue((new BigRational(".2")).logarithm(10) == -1);
		// assertTrue((new BigRational(".10001")).logarithm(10) == -1);
		// assertTrue((new BigRational(".1")).logarithm(10) == -2);
		// assertTrue((new BigRational(".0999")).logarithm(10) == -2);
		// assertTrue((new BigRational(".010001")).logarithm(10) == -2);
		// assertTrue((new BigRational(".01")).logarithm(10) == -3);
		// assertTrue((new BigRational(".00999")).logarithm(10) == -3);
		// assertTrue((new BigRational(".001")).logarithm(10) == -4);

		// assertTrue((new BigRational("1")).logarithm(5) == 0);
		// assertTrue((new BigRational("1.0001")).logarithm(5) == 0);
		// assertTrue((new BigRational("1.1")).logarithm(5) == 0);
		// assertTrue((new BigRational("2")).logarithm(5) == 0);
		// assertTrue((new BigRational("4")).logarithm(5) == 0);
		// assertTrue((new BigRational("4.999")).logarithm(5) == 0);
		// assertTrue((new BigRational("5")).logarithm(5) == 1);
		// assertTrue((new BigRational("5.0001")).logarithm(5) == 1);
		// assertTrue((new BigRational("24")).logarithm(5) == 1);
		// assertTrue((new BigRational("25")).logarithm(5) == 2);
		// assertTrue((new BigRational("124")).logarithm(5) == 2);
		// assertTrue((new BigRational("125")).logarithm(5) == 3);
		// assertTrue((new BigRational(".5")).logarithm(5) == -1);
		// assertTrue((new BigRational(".3")).logarithm(5) == -1);
		// assertTrue((new BigRational(".20001")).logarithm(5) == -1);
		// assertTrue((new BigRational(".2")).logarithm(5) == -2);
		// assertTrue((new BigRational(".1999")).logarithm(5) == -2);
		// assertTrue((new BigRational(".040001")).logarithm(5) == -2);
		// assertTrue((new BigRational(".04")).logarithm(5) == -3);
		// assertTrue((new BigRational(".03999")).logarithm(5) == -3);
		// assertTrue((new BigRational(".008")).logarithm(5) == -4);

		// assertTrue(BigRational.valueOf("2E0").logarithm(10) == 0);
		// assertTrue(BigRational.valueOf("2E1").logarithm(10) == 1);
		// assertTrue(BigRational.valueOf("2E2").logarithm(10) == 2);
		// assertTrue(BigRational.valueOf("2E20").logarithm(10) == 20);
		// assertTrue(BigRational.valueOf("2E200").logarithm(10) == 200);

		// assertTrue(ilog2(1) == 0);
		// assertTrue(ilog2(2) == 1);
		// assertTrue(ilog2(3) == 1);
		// assertTrue(ilog2(4) == 2);
		// assertTrue(ilog2(5) == 2);
		// assertTrue(ilog2(6) == 2);
		// assertTrue(ilog2(7) == 2);
		// assertTrue(ilog2(8) == 3);

		// stringDotRelative

		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 8 ).equals( "1234.5678" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 9 ).equals( "1234.56780" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 10 ).equals( "1234.567800" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 7 ).equals( "1234.568" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 6 ).equals( "1234.57" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 5 ).equals( "1234.6" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 4 ).equals( "1235" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 3 ).equals( "1230" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 2 ).equals( "1200" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 1 ).equals( "1000" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( -1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringDotRelative( -2 ).equals( "0" ) );

		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 8 ).equals( "-1234.5678" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 9 ).equals( "-1234.56780" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 10 ).equals( "-1234.567800" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 7 ).equals( "-1234.568" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 6 ).equals( "-1234.57" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 5 ).equals( "-1234.6" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 4 ).equals( "-1235" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 3 ).equals( "-1230" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 2 ).equals( "-1200" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 1 ).equals( "-1000" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( -1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringDotRelative( -2 ).equals( "0" ) );

		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 8 ).equals( "0.00012345678" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 9 ).equals( "0.000123456780" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 10 ).equals( "0.0001234567800" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 7 ).equals( "0.0001234568" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 6 ).equals( "0.000123457" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 5 ).equals( "0.00012346" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 4 ).equals( "0.0001235" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 3 ).equals( "0.000123" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 2 ).equals( "0.00012" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 1 ).equals( "0.0001" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( -1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringDotRelative( -2 ).equals( "0" ) );

		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 8 ).equals( "-0.00012345678" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 9 ).equals( "-0.000123456780" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 10 ).equals( "-0.0001234567800" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 7 ).equals( "-0.0001234568" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 6 ).equals( "-0.000123457" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 5 ).equals( "-0.00012346" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 4 ).equals( "-0.0001235" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 3 ).equals( "-0.000123" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 2 ).equals( "-0.00012" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 1 ).equals( "-0.0001" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( -1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringDotRelative( -2 ).equals( "0" ) );

		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 8 ).equals( "8765.4321" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 9 ).equals( "8765.43210" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 10 ).equals( "8765.432100" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 7 ).equals( "8765.432" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 6 ).equals( "8765.43" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 5 ).equals( "8765.4" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 4 ).equals( "8765" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 3 ).equals( "8770" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 2 ).equals( "8800" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 1 ).equals( "9000" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( -1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "8765.4321" ) ).toStringDotRelative( -2 ).equals( "0" ) );

		assertTrue( ( new BigRational( "0.0" ) ).toStringDotRelative( 4 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDotRelative( 3 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDotRelative( 2 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDotRelative( 1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDotRelative( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDotRelative( -1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDotRelative( -2 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDotRelative( -3 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.0" ) ).toStringDotRelative( -4 ).equals( "0" ) );

		assertTrue( ( new BigRational( 1499 ) ).toStringDotRelative( 1 ).equals( "1000" ) );
		assertTrue( ( new BigRational( 1500 ) ).toStringDotRelative( 1 ).equals( "2000" ) );
		assertTrue( ( new BigRational( 1501 ) ).toStringDotRelative( 1 ).equals( "2000" ) );
		assertTrue( ( new BigRational( 1001 ) ).toStringDotRelative( 1 ).equals( "1000" ) );
		assertTrue( ( new BigRational( 1000 ) ).toStringDotRelative( 1 ).equals( "1000" ) );
		assertTrue( ( new BigRational( 999 ) ).toStringDotRelative( 1 ).equals( "1000" ) );
		assertTrue( ( new BigRational( 951 ) ).toStringDotRelative( 1 ).equals( "1000" ) );
		assertTrue( ( new BigRational( 950 ) ).toStringDotRelative( 1 ).equals( "1000" ) );
		assertTrue( ( new BigRational( 949 ) ).toStringDotRelative( 1 ).equals( "900" ) );

		// check inverted radix power cases too
		assertTrue( ( new BigRational( 1, 1 ) ).toStringDotRelative( 1 ).equals( "1" ) );
		assertTrue( ( new BigRational( 1, 2 ) ).toStringDotRelative( 1 ).equals( "0.5" ) );
		assertTrue( ( new BigRational( 1, 3 ) ).toStringDotRelative( 1 ).equals( "0.3" ) );
		assertTrue( ( new BigRational( 1, 5 ) ).toStringDotRelative( 1 ).equals( "0.2" ) );
		assertTrue( ( new BigRational( 1, 10 ) ).toStringDotRelative( 1 ).equals( "0.1" ) );
		assertTrue( ( new BigRational( 1, 20 ) ).toStringDotRelative( 1 ).equals( "0.05" ) );
		assertTrue( ( new BigRational( 1, 30 ) ).toStringDotRelative( 1 ).equals( "0.03" ) );
		assertTrue( ( new BigRational( 1, 50 ) ).toStringDotRelative( 1 ).equals( "0.02" ) );
		assertTrue( ( new BigRational( 1, 100 ) ).toStringDotRelative( 1 ).equals( "0.01" ) );
		assertTrue( ( new BigRational( 1, 200 ) ).toStringDotRelative( 1 ).equals( "0.005" ) );
		assertTrue( ( new BigRational( 1, 300 ) ).toStringDotRelative( 1 ).equals( "0.003" ) );
		assertTrue( ( new BigRational( 1, 500 ) ).toStringDotRelative( 1 ).equals( "0.002" ) );
		assertTrue( ( new BigRational( 1, 1000 ) ).toStringDotRelative( 1 ).equals( "0.001" ) );
		assertTrue( ( new BigRational( 1, 4 ) ).toStringDotRelative( 1 ).equals( "0.3" ) );
		assertTrue( ( new BigRational( 1, 11 ) ).toStringDotRelative( 1 ).equals( "0.09" ) );
		assertTrue( ( new BigRational( 1, 12 ) ).toStringDotRelative( 1 ).equals( "0.08" ) );
		assertTrue( ( new BigRational( 1, 31 ) ).toStringDotRelative( 1 ).equals( "0.03" ) );
		assertTrue( ( new BigRational( 1, 99 ) ).toStringDotRelative( 1 ).equals( "0.01" ) );
		assertTrue( ( new BigRational( "0.0100001" ) ).toStringDotRelative( 1 ).equals( "0.01" ) );

		// intermediately excess zeros
		assertTrue( ( new BigRational( 1, 101 ) ).toStringDotRelative( 1 ).equals( "0.01" ) );
		assertTrue( ( new BigRational( "0.0099999" ) ).toStringDotRelative( 1 ).equals( "0.01" ) );

		// exponent representation

		assertTrue( ( new BigRational( "1234.5678" ) ).toStringExponent( 8 ).equals( "1.2345678E3" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringExponent( 4 ).equals( "1.235E3" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringExponent( 2 ).equals( "1.2E3" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringExponent( 1 ).equals( "1E3" ) );
		assertTrue( ( new BigRational( "1234.5678" ) ).toStringExponent( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringExponent( 8 ).equals( "1.2345678E-4" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringExponent( 4 ).equals( "1.235E-4" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringExponent( 2 ).equals( "1.2E-4" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringExponent( 1 ).equals( "1E-4" ) );
		assertTrue( ( new BigRational( "0.00012345678" ) ).toStringExponent( 0 ).equals( "0" ) );

		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringExponent( 8 ).equals( "-1.2345678E3" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringExponent( 4 ).equals( "-1.235E3" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringExponent( 2 ).equals( "-1.2E3" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringExponent( 1 ).equals( "-1E3" ) );
		assertTrue( ( new BigRational( "-1234.5678" ) ).toStringExponent( 0 ).equals( "0" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringExponent( 8 ).equals( "-1.2345678E-4" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringExponent( 4 ).equals( "-1.235E-4" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringExponent( 2 ).equals( "-1.2E-4" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringExponent( 1 ).equals( "-1E-4" ) );
		assertTrue( ( new BigRational( "-0.00012345678" ) ).toStringExponent( 0 ).equals( "0" ) );

		assertTrue( ( new BigRational( "1234" ) ).toStringExponent( 8 ).equals( "1.234E3" ) );
		assertTrue( ( new BigRational( "1234" ) ).toStringExponent( 4 ).equals( "1.234E3" ) );
		assertTrue( ( new BigRational( "1234" ) ).toStringExponent( 2 ).equals( "1.2E3" ) );
		assertTrue( ( new BigRational( "1234" ) ).toStringExponent( 1 ).equals( "1E3" ) );
		assertTrue( ( new BigRational( "1234" ) ).toStringExponent( 0 ).equals( "0" ) );

		assertTrue( ( new BigRational( "10" ) ).toStringExponent( 6 ).equals( "1E1" ) );
		assertTrue( ( new BigRational( "1" ) ).toStringExponent( 6 ).equals( "1" ) );
		assertTrue( ( new BigRational( "0" ) ).toStringExponent( 6 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.1" ) ).toStringExponent( 6 ).equals( "1E-1" ) );
		assertTrue( ( new BigRational( "10" ) ).toStringExponent( 1 ).equals( "1E1" ) );
		assertTrue( ( new BigRational( "1" ) ).toStringExponent( 1 ).equals( "1" ) );
		assertTrue( ( new BigRational( "0" ) ).toStringExponent( 1 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0.1" ) ).toStringExponent( 1 ).equals( "1E-1" ) );

		assertTrue( BigRational.valueOf( "2E222" ).toStringExponent( 4 ).equals( "2E222" ) );
		assertTrue( BigRational.valueOf( "-2E222" ).toStringExponent( 4 ).equals( "-2E222" ) );
		assertTrue( BigRational.valueOf( "2E-222" ).toStringExponent( 4 ).equals( "2E-222" ) );
		assertTrue( BigRational.valueOf( "-2E-222" ).toStringExponent( 4 ).equals( "-2E-222" ) );

		// non-default radix
		assertTrue( new BigRational( "2E222", 5 ).toStringExponent( 4, 5 ).equals( "2E222" ) );
		assertTrue( new BigRational( "-2E222", 5 ).toStringExponent( 4, 5 ).equals( "-2E222" ) );
		assertTrue( new BigRational( "2E-222", 5 ).toStringExponent( 4, 5 ).equals( "2E-222" ) );
		assertTrue( new BigRational( "-2E-222", 5 ).toStringExponent( 4, 5 ).equals( "-2E-222" ) );

		// note: some of the arithmetic long forms not tested as well.

		// "BigRational add"
		assertTrue( ( new BigRational( 3, 5 ) ).add( new BigRational( 7, 11 ) ).toString().equals( "68/55" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).add( new BigRational( -7, 11 ) ).toString().equals( "-2/55" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).add( new BigRational( 7, 11 ) ).toString().equals( "2/55" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).add( new BigRational( -7, 11 ) ).toString().equals( "-68/55" ) );
		// same denominator
		assertTrue( ( new BigRational( 3, 5 ) ).add( new BigRational( 1, 5 ) ).toString().equals( "4/5" ) );
		// with integers
		assertTrue( ( new BigRational( 3, 5 ) ).add( new BigRational( 1 ) ).toString().equals( "8/5" ) );
		assertTrue( ( new BigRational( 2 ) ).add( new BigRational( 3, 5 ) ).toString().equals( "13/5" ) );
		// zero
		assertTrue( ( new BigRational( 3, 5 ) ).add( BigRational.ZERO ).toString().equals( "3/5" ) );
		assertTrue( BigRational.ZERO.add( new BigRational( 3, 5 ) ).toString().equals( "3/5" ) );

		// "BigRational subtract"
		assertTrue( ( new BigRational( 3, 5 ) ).subtract( new BigRational( 7, 11 ) ).toString().equals( "-2/55" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).subtract( new BigRational( -7, 11 ) ).toString().equals( "68/55" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).subtract( new BigRational( 7, 11 ) ).toString().equals( "-68/55" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).subtract( new BigRational( -7, 11 ) ).toString().equals( "2/55" ) );
		// same denominator
		assertTrue( ( new BigRational( 3, 5 ) ).subtract( new BigRational( 1, 5 ) ).toString().equals( "2/5" ) );
		// with integers
		assertTrue( ( new BigRational( 3, 5 ) ).subtract( new BigRational( 1 ) ).toString().equals( "-2/5" ) );
		assertTrue( ( new BigRational( 2 ) ).subtract( new BigRational( 3, 5 ) ).toString().equals( "7/5" ) );
		// zero
		assertTrue( ( new BigRational( 3, 5 ) ).subtract( BigRational.ZERO ).toString().equals( "3/5" ) );
		assertTrue( BigRational.ZERO.subtract( new BigRational( 3, 5 ) ).toString().equals( "-3/5" ) );

		// normalization, e.g after subtract
		// "BigRational normalization"
		assertTrue( ( new BigRational( 7, 5 ) ).subtract( new BigRational( 2, 5 ) ).compareTo( 1 ) == 0 );
		assertTrue( ( new BigRational( 7, 5 ) ).subtract( new BigRational( 7, 5 ) ).compareTo( 0 ) == 0 );
		assertTrue( ( new BigRational( 7, 5 ) ).subtract( new BigRational( 12, 5 ) ).compareTo( -1 ) == 0 );

		// "BigRational multiply"
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( new BigRational( 7, 11 ) ).toString().equals( "21/55" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( new BigRational( -7, 11 ) ).toString().equals( "-21/55" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).multiply( new BigRational( 7, 11 ) ).toString().equals( "-21/55" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).multiply( new BigRational( -7, 11 ) ).toString().equals( "21/55" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( 7 ).toString().equals( "21/5" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).multiply( 7 ).toString().equals( "-21/5" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( -7 ).toString().equals( "-21/5" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).multiply( -7 ).toString().equals( "21/5" ) );

		// multiply() with integers, 0, etc. (some repetitions too)
		// "BigRational multiply"
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( new BigRational( 7, 1 ) ).toString().equals( "21/5" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( new BigRational( 1, 7 ) ).toString().equals( "3/35" ) );
		assertTrue( ( new BigRational( 3, 1 ) ).multiply( new BigRational( 7, 11 ) ).toString().equals( "21/11" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( new BigRational( 0 ) ).toString().equals( "0" ) );
		assertTrue( ( new BigRational( 0 ) ).multiply( new BigRational( 3, 5 ) ).toString().equals( "0" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( new BigRational( 1 ) ).toString().equals( "3/5" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( new BigRational( -1 ) ).toString().equals( "-3/5" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).multiply( new BigRational( -1, 3 ) ).toString().equals( "-1/5" ) );

		// special cases (zeroing, negating)
		assertTrue( BigRational.ZERO.multiply( BigRational.ZERO ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ZERO.multiply( BigRational.ONE ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ONE.multiply( BigRational.ZERO ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ZERO.multiply( BigRational.MINUS_ONE ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.MINUS_ONE.multiply( BigRational.ZERO ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ZERO.multiply( new BigRational( 5 ) ).equals( BigRational.ZERO ) );
		assertTrue( ( new BigRational( 5 ) ).multiply( BigRational.ZERO ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ZERO.multiply( new BigRational( -5 ) ).equals( BigRational.ZERO ) );
		assertTrue( ( new BigRational( -5 ) ).multiply( BigRational.ZERO ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ONE.multiply( BigRational.ONE ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.multiply( BigRational.MINUS_ONE ).equals( BigRational.MINUS_ONE ) );
		assertTrue( BigRational.MINUS_ONE.multiply( BigRational.ONE ).equals( BigRational.MINUS_ONE ) );
		assertTrue( BigRational.ONE.multiply( new BigRational( 5 ) ).equals( new BigRational( 5 ) ) );
		assertTrue( ( new BigRational( 5 ) ).multiply( BigRational.ONE ).equals( new BigRational( 5 ) ) );
		assertTrue( BigRational.ONE.multiply( new BigRational( -5 ) ).equals( new BigRational( -5 ) ) );
		assertTrue( ( new BigRational( -5 ) ).multiply( BigRational.ONE ).equals( new BigRational( -5 ) ) );
		assertTrue( BigRational.MINUS_ONE.multiply( BigRational.MINUS_ONE ).equals( BigRational.ONE ) );
		assertTrue( BigRational.MINUS_ONE.multiply( new BigRational( 5 ) ).equals( new BigRational( -5 ) ) );
		assertTrue( ( new BigRational( 5 ) ).multiply( BigRational.MINUS_ONE ).equals( new BigRational( -5 ) ) );
		assertTrue( BigRational.MINUS_ONE.multiply( new BigRational( -5 ) ).equals( new BigRational( 5 ) ) );
		assertTrue( ( new BigRational( -5 ) ).multiply( BigRational.MINUS_ONE ).equals( new BigRational( 5 ) ) );

		// "BigRational divide"
		assertTrue( ( new BigRational( 3, 5 ) ).divide( new BigRational( 7, 11 ) ).toString().equals( "33/35" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).divide( new BigRational( -7, 11 ) ).toString().equals( "-33/35" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).divide( new BigRational( 7, 11 ) ).toString().equals( "-33/35" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).divide( new BigRational( -7, 11 ) ).toString().equals( "33/35" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).divide( 7 ).toString().equals( "3/35" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).divide( 7 ).toString().equals( "-3/35" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).divide( -7 ).toString().equals( "-3/35" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).divide( -7 ).toString().equals( "3/35" ) );

		assertTrue( ( new BigRational( 0 ) ).divide( new BigRational( 7, 11 ) ).toString().equals( "0" ) );

		// "BigRational divide"
		try
		{
			( new BigRational( 3, 5 ) ).divide( new BigRational( 0 ) );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}
		try
		{
			( new BigRational( -3, 5 ) ).divide( new BigRational( 0 ) );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}
		try
		{
			( new BigRational( 3, 5 ) ).divide( 0 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}
		try
		{
			( new BigRational( -3, 5 ) ).divide( 0 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}

		try
		{
			( new BigRational( 0 ) ).divide( 0 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}

		// "BigRational power"
		assertTrue( ( new BigRational( 3, 5 ) ).power( 7 ).toString().equals( "2187/78125" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).power( 7 ).toString().equals( "-2187/78125" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).power( -7 ).toString().equals( "78125/2187" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).power( -7 ).toString().equals( "-78125/2187" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).power( 6 ).toString().equals( "729/15625" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).power( 6 ).toString().equals( "729/15625" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).power( 0 ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).power( 0 ).toString().equals( "1" ) );
		assertTrue( ( new BigRational( 0 ) ).power( 1 ).toString().equals( "0" ) );
		assertTrue( ( new BigRational( 1 ) ).power( 0 ).toString().equals( "1" ) );

		assertTrue( ( new BigRational( 3, 5 ) ).power( 0 ).equals( BigRational.ONE ) );
		assertTrue( ( new BigRational( 3, 5 ) ).power( 1 ).equals( new BigRational( 3, 5 ) ) );
		assertTrue( ( new BigRational( 3, 5 ) ).power( -1 ).equals( ( new BigRational( 3, 5 ) ).invert() ) );
		assertTrue( ( new BigRational( 2 ) ).power( 2 ).equals( new BigRational( 4 ) ) );

		// special cases
		assertTrue( BigRational.ZERO.power( 1 ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ZERO.power( 2 ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ZERO.power( 3 ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ZERO.power( 4 ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.ONE.power( 0 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.power( 1 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.power( 2 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.power( 3 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.power( 4 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.power( -1 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.power( -2 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.power( -3 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.power( -4 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.MINUS_ONE.power( 0 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.MINUS_ONE.power( 1 ).equals( BigRational.MINUS_ONE ) );
		assertTrue( BigRational.MINUS_ONE.power( 2 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.MINUS_ONE.power( 3 ).equals( BigRational.MINUS_ONE ) );
		assertTrue( BigRational.MINUS_ONE.power( 4 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.MINUS_ONE.power( -1 ).equals( BigRational.MINUS_ONE ) );
		assertTrue( BigRational.MINUS_ONE.power( -2 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.MINUS_ONE.power( -3 ).equals( BigRational.MINUS_ONE ) );
		assertTrue( BigRational.MINUS_ONE.power( -4 ).equals( BigRational.ONE ) );

		// "BigRational zeroth power of zero"
		try
		{
			( new BigRational( 0 ) ).power( 0 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}
		try
		{
			BigRational.ZERO.power( -1 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}
		try
		{
			BigRational.ZERO.power( -2 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}

		// "BigRational remainder"
		assertTrue( ( new BigRational( 5 ) ).remainder( new BigRational( 3 ) ).equals( new BigRational( 2 ) ) );
		assertTrue( ( new BigRational( -5 ) ).remainder( new BigRational( 3 ) ).equals( new BigRational( -2 ) ) );
		assertTrue( ( new BigRational( 5 ) ).remainder( new BigRational( -3 ) ).equals( new BigRational( 2 ) ) );
		assertTrue( ( new BigRational( -5 ) ).remainder( new BigRational( -3 ) ).equals( new BigRational( -2 ) ) );
		assertTrue( ( new BigRational( 0 ) ).remainder( new BigRational( 1 ) ).equals( new BigRational( 0 ) ) );
		assertTrue( ( new BigRational( "5.6" ) ).remainder( new BigRational( "1.8" ) ).equals( new BigRational( 1, 5 ) ) );
		assertTrue( ( new BigRational( "-5.6" ) ).remainder( new BigRational( "1.8" ) ).equals( new BigRational( -1, 5 ) ) );
		assertTrue( ( new BigRational( "5.6" ) ).remainder( new BigRational( "-1.8" ) ).equals( new BigRational( 1, 5 ) ) );
		assertTrue( ( new BigRational( "-5.6" ) ).remainder( new BigRational( "-1.8" ) ).equals( new BigRational( -1, 5 ) ) );
		assertTrue( ( new BigRational( "1" ) ).remainder( new BigRational( "0.13" ) ).equals( new BigRational( "0.09" ) ) );

		try
		{
			BigRational.ONE.remainder( 0 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}
		try
		{
			BigRational.ZERO.remainder( 0 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}

		// "BigRational modulus"
		assertTrue( ( new BigRational( 5 ) ).modulus( new BigRational( 3 ) ).equals( new BigRational( 2 ) ) );
		assertTrue( ( new BigRational( -5 ) ).modulus( new BigRational( 3 ) ).equals( new BigRational( 1 ) ) );
		assertTrue( ( new BigRational( 5 ) ).modulus( new BigRational( -3 ) ).equals( new BigRational( -1 ) ) );
		assertTrue( ( new BigRational( -5 ) ).modulus( new BigRational( -3 ) ).equals( new BigRational( -2 ) ) );
		assertTrue( ( new BigRational( 0 ) ).modulus( new BigRational( 1 ) ).equals( new BigRational( 0 ) ) );
		assertTrue( ( new BigRational( "5.6" ) ).modulus( new BigRational( "1.8" ) ).equals( new BigRational( 1, 5 ) ) );
		assertTrue( ( new BigRational( "-5.6" ) ).modulus( new BigRational( "1.8" ) ).equals( new BigRational( 8, 5 ) ) );
		assertTrue( ( new BigRational( "5.6" ) ).modulus( new BigRational( "-1.8" ) ).equals( new BigRational( -8, 5 ) ) );
		assertTrue( ( new BigRational( "-5.6" ) ).modulus( new BigRational( "-1.8" ) ).equals( new BigRational( -1, 5 ) ) );
		assertTrue( ( new BigRational( "1" ) ).modulus( new BigRational( "0.13" ) ).equals( new BigRational( "0.09" ) ) );

		try
		{
			BigRational.ONE.modulus( 0 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}
		try
		{
			BigRational.ZERO.modulus( 0 );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}

		// "BigRational signum"
		assertTrue( ( new BigRational( 0 ) ).signum() == 0 );
		assertTrue( ( new BigRational( 1 ) ).signum() == 1 );
		assertTrue( ( new BigRational( -1 ) ).signum() == -1 );
		assertTrue( ( new BigRational( 2 ) ).signum() == 1 );
		assertTrue( ( new BigRational( -2 ) ).signum() == -1 );
		assertTrue( ( new BigRational( 3, 5 ) ).signum() == 1 );
		assertTrue( ( new BigRational( -3, 5 ) ).signum() == -1 );

		// "BigRational absolute"
		assertTrue( ( new BigRational( 0 ) ).absolute().toString().equals( "0" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).absolute().toString().equals( "3/5" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).absolute().toString().equals( "3/5" ) );
		assertTrue( ( new BigRational( 1 ) ).absolute().toString().equals( "1" ) );
		assertTrue( ( new BigRational( -1 ) ).absolute().toString().equals( "1" ) );
		assertTrue( BigRational.ZERO.absolute().equals( BigRational.ZERO ) );
		assertTrue( BigRational.ONE.absolute().equals( BigRational.ONE ) );
		assertTrue( BigRational.MINUS_ONE.absolute().equals( BigRational.ONE ) );

		// "BigRational negate"
		assertTrue( ( new BigRational( 0 ) ).negate().toString().equals( "0" ) );
		assertTrue( ( new BigRational( 1 ) ).negate().toString().equals( "-1" ) );
		assertTrue( ( new BigRational( -1 ) ).negate().toString().equals( "1" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).negate().toString().equals( "-3/5" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).negate().toString().equals( "3/5" ) );
		assertTrue( BigRational.ZERO.negate().equals( BigRational.ZERO ) );
		assertTrue( BigRational.ONE.negate().equals( BigRational.MINUS_ONE ) );
		assertTrue( BigRational.MINUS_ONE.negate().equals( BigRational.ONE ) );

		// "BigRational invert"
		assertTrue( ( new BigRational( 3, 5 ) ).invert().toString().equals( "5/3" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).invert().toString().equals( "-5/3" ) );
		assertTrue( ( new BigRational( 11, 7 ) ).invert().toString().equals( "7/11" ) );
		assertTrue( ( new BigRational( -11, 7 ) ).invert().toString().equals( "-7/11" ) );
		assertTrue( ( new BigRational( 1 ) ).invert().toString().equals( "1" ) );
		assertTrue( ( new BigRational( -1 ) ).invert().toString().equals( "-1" ) );
		assertTrue( ( new BigRational( 2 ) ).invert().toString().equals( "1/2" ) );
		assertTrue( BigRational.ONE.invert().equals( BigRational.ONE ) );
		assertTrue( BigRational.MINUS_ONE.invert().equals( BigRational.MINUS_ONE ) );

		try
		{
			BigRational.ZERO.invert();
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}

		// "BigRational minimum"
		assertTrue( ( new BigRational( 3, 5 ) ).minimum( new BigRational( 7, 11 ) ).toString().equals( "3/5" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).minimum( new BigRational( 7, 11 ) ).toString().equals( "-3/5" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).minimum( new BigRational( -7, 11 ) ).toString().equals( "-7/11" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).minimum( new BigRational( -7, 11 ) ).toString().equals( "-7/11" ) );
		assertTrue( ( new BigRational( 7, 11 ) ).minimum( new BigRational( 3, 5 ) ).toString().equals( "3/5" ) );

		// "BigRational maximum"
		assertTrue( ( new BigRational( 3, 5 ) ).maximum( new BigRational( 7, 11 ) ).toString().equals( "7/11" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).maximum( new BigRational( 7, 11 ) ).toString().equals( "7/11" ) );
		assertTrue( ( new BigRational( 3, 5 ) ).maximum( new BigRational( -7, 11 ) ).toString().equals( "3/5" ) );
		assertTrue( ( new BigRational( -3, 5 ) ).maximum( new BigRational( -7, 11 ) ).toString().equals( "-3/5" ) );
		assertTrue( ( new BigRational( 7, 11 ) ).maximum( new BigRational( 3, 5 ) ).toString().equals( "7/11" ) );

		// equals()

		// "BigRational [polymorph] equals"
		assertTrue( BigRational.ONE.equals( BigRational.ONE ) );
		assertTrue( BigRational.ONE.equals( new BigRational( 3, 3 ) ) );
		assertTrue( BigRational.ONE.equals( new BigRational( 987654, 987654 ) ) );
		assertTrue( !BigRational.ONE.equals( new BigRational( 987653, 987654 ) ) );
		assertTrue( BigRational.ZERO.equals( new BigRational( 0, 987654 ) ) );
		assertTrue( BigRational.MINUS_ONE.equals( new BigRational( -987654, 987654 ) ) );
		assertTrue( !BigRational.MINUS_ONE.equals( new BigRational( -987653, 987654 ) ) );
		assertTrue( ( new BigRational( 3, 5 ) ).equals( new BigRational( 3, 5 ) ) );
		assertTrue( !( new BigRational( 3, 5 ) ).equals( new BigRational( 5, 3 ) ) );
		assertTrue( BigRational.ZERO.equals( BigRational.ZERO ) );
		assertTrue( BigRational.ONE.equals( BigRational.ONE ) );
		assertTrue( BigRational.MINUS_ONE.equals( BigRational.MINUS_ONE ) );
		assertTrue( !BigRational.ZERO.equals( BigRational.ONE ) );
		assertTrue( !BigRational.ONE.equals( BigRational.ZERO ) );
		assertTrue( !BigRational.ONE.equals( BigRational.MINUS_ONE ) );
		assertTrue( !BigRational.MINUS_ONE.equals( BigRational.ONE ) );

		// following tests address things that changed from earlier version;
		// i.e. will fail with that version
		assertTrue( !BigRational.ZERO.equals( new Integer( 0 ) ) );
		assertTrue( !( new BigRational( 3 ) ).equals( new Integer( 3 ) ) );
		assertTrue( !BigRational.ZERO.equals( new Long( 0 ) ) );
		assertTrue( !( new BigRational( 3 ) ).equals( new Long( 3 ) ) );
		assertTrue( !BigRational.ZERO.equals( "0" ) );
		assertTrue( !( new BigRational( 3 ) ).equals( "3" ) );

		assertTrue( !( new Integer( 0 ) ).equals( BigRational.ZERO ) );
		assertTrue( !( new Long( 0 ) ).equals( BigRational.ZERO ) );
		assertTrue( !( new String( "0" ) ).equals( BigRational.ZERO ) );

		assertTrue( !BigRational.ONE.equals( new RuntimeException() ) );
		assertTrue( !( new RuntimeException() ).equals( BigRational.ONE ) );

		// "BigRational hash code"
		assertTrue( ( new BigRational( 3, 5 ) ).hashCode() == ( new BigRational( 6, 10 ) ).hashCode() );

		// well, the necessity of these hashCode() inequality tests ain't undisputed.
		// so they're commented out; since not a misbehavior or error.
		// assertTrue(BigRational.ONE.hashCode() != BigRational.ZERO.hashCode());
		// assertTrue((new BigRational(3, 5)).hashCode() != (new BigRational(5, 3)).hashCode());
		// assertTrue((new BigRational(3, 5)).hashCode() != (new BigRational(-3, 5)).hashCode());
		// assertTrue((new BigRational(4)).hashCode() != (new BigRational(8)).hashCode());

		// "BigRational compare to"
		assertTrue( ( new BigRational( 3, 5 ) ).compareTo( new BigRational( 3, 5 ) ) == 0 );
		assertTrue( ( new BigRational( 3, 5 ) ).compareTo( new BigRational( 5, 3 ) ) == -1 );
		assertTrue( ( new BigRational( 5, 3 ) ).compareTo( new BigRational( 3, 5 ) ) == 1 );
		assertTrue( ( new BigRational( 3, 5 ) ).compareTo( new BigRational( -5, 3 ) ) == 1 );
		assertTrue( ( new BigRational( -5, 3 ) ).compareTo( new BigRational( 3, 5 ) ) == -1 );

		assertTrue( BigRational.ZERO.compareTo( BigRational.ZERO ) == 0 );
		assertTrue( BigRational.ZERO.compareTo( BigRational.ONE ) == -1 );
		assertTrue( BigRational.ONE.compareTo( BigRational.ZERO ) == 1 );
		assertTrue( BigRational.ZERO.compareTo( BigRational.MINUS_ONE ) == 1 );
		assertTrue( BigRational.MINUS_ONE.compareTo( BigRational.ZERO ) == -1 );
		assertTrue( BigRational.ONE.compareTo( BigRational.MINUS_ONE ) == 1 );
		assertTrue( BigRational.MINUS_ONE.compareTo( BigRational.ONE ) == -1 );
		assertTrue( BigRational.ONE.compareTo( BigRational.ONE ) == 0 );

		// "BigRational polymorph compare to"
		assertTrue( ( new BigRational( 3, 5 ) ).compareTo( (Object)new BigRational( 3, 5 ) ) == 0 );
		assertTrue( ( new BigRational( 3, 5 ) ).compareTo( (Object)new BigRational( 5, 3 ) ) == -1 );
		assertTrue( ( new BigRational( 5, 3 ) ).compareTo( (Object)new BigRational( 3, 5 ) ) == 1 );
		assertTrue( ( new BigRational( 3, 5 ) ).compareTo( (Object)new BigRational( -5, 3 ) ) == 1 );
		assertTrue( ( new BigRational( -5, 3 ) ).compareTo( (Object)new BigRational( 3, 5 ) ) == -1 );

		// "BigRational small type compare to"
		assertTrue( ( new BigRational( 3, 5 ) ).compareTo( 0 ) != 0 );
		assertTrue( ( new BigRational( 0 ) ).compareTo( 0 ) == 0 );
		assertTrue( ( new BigRational( 1 ) ).compareTo( 1 ) == 0 );
		assertTrue( ( new BigRational( 2 ) ).compareTo( 2 ) == 0 );
		assertTrue( ( new BigRational( 3, 5 ) ).compareTo( new Long( 0 ) ) != 0 );
		assertTrue( ( new BigRational( 1 ) ).compareTo( new Long( 1 ) ) == 0 );
		assertTrue( ( new BigRational( 1 ) ).compareTo( new Integer( 1 ) ) == 0 );

		// "BigRational long/int value"
		assertTrue( ( new BigRational( 7 ) ).longValue() == 7 );
		assertTrue( ( new BigRational( -7 ) ).intValue() == -7 );
		assertTrue( BigRational.ZERO.longValue() == 0 );
		assertTrue( BigRational.MINUS_ONE.longValue() == -1 );
		assertTrue( BigRational.MINUS_ONE.intValue() == -1 );
		assertTrue( BigRational.MINUS_ONE.shortValue() == -1 );
		assertTrue( BigRational.MINUS_ONE.byteValue() == -1 );

		// round()/floor()/ceiling()

		// "BigRational round"

		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_UP ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_DOWN ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_CEILING ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_FLOOR ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_HALF_UP ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_HALF_DOWN ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_HALF_CEILING ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_HALF_FLOOR ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_HALF_EVEN ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.49" ) ).round( BigRational.ROUND_HALF_ODD ).toString().equals( "23" ) );

		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_UP ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_DOWN ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_CEILING ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_FLOOR ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_HALF_UP ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_HALF_DOWN ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_HALF_CEILING ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_HALF_FLOOR ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_HALF_EVEN ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.49" ) ).round( BigRational.ROUND_HALF_ODD ).toString().equals( "-23" ) );

		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_UP ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_DOWN ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_CEILING ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_FLOOR ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_HALF_UP ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_HALF_DOWN ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_HALF_CEILING ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_HALF_FLOOR ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_HALF_EVEN ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.51" ) ).round( BigRational.ROUND_HALF_ODD ).toString().equals( "24" ) );

		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_UP ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_DOWN ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_CEILING ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_FLOOR ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_HALF_UP ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_HALF_DOWN ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_HALF_CEILING ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_HALF_FLOOR ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_HALF_EVEN ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.51" ) ).round( BigRational.ROUND_HALF_ODD ).toString().equals( "-24" ) );

		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_UP ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_DOWN ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_CEILING ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_FLOOR ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_HALF_UP ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_HALF_DOWN ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_HALF_CEILING ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_HALF_FLOOR ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_HALF_EVEN ).toString().equals( "24" ) );
		assertTrue( ( new BigRational( "23.5" ) ).round( BigRational.ROUND_HALF_ODD ).toString().equals( "23" ) );

		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_UP ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_DOWN ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_CEILING ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_FLOOR ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_HALF_UP ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_HALF_DOWN ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_HALF_CEILING ).toString().equals( "-23" ) );
		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_HALF_FLOOR ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_HALF_EVEN ).toString().equals( "-24" ) );
		assertTrue( ( new BigRational( "-23.5" ) ).round( BigRational.ROUND_HALF_ODD ).toString().equals( "-23" ) );

		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_UP ).toString().equals( "22" ) );
		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_DOWN ).toString().equals( "22" ) );
		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_CEILING ).toString().equals( "22" ) );
		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_FLOOR ).toString().equals( "22" ) );
		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_HALF_UP ).toString().equals( "22" ) );
		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_HALF_DOWN ).toString().equals( "22" ) );
		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_HALF_CEILING ).toString().equals( "22" ) );
		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_HALF_FLOOR ).toString().equals( "22" ) );
		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_HALF_EVEN ).toString().equals( "22" ) );
		assertTrue( ( new BigRational( "22" ) ).round( BigRational.ROUND_HALF_ODD ).toString().equals( "22" ) );

		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_UP ).toString().equals( "-22" ) );
		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_DOWN ).toString().equals( "-22" ) );
		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_CEILING ).toString().equals( "-22" ) );
		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_FLOOR ).toString().equals( "-22" ) );
		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_HALF_UP ).toString().equals( "-22" ) );
		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_HALF_DOWN ).toString().equals( "-22" ) );
		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_HALF_CEILING ).toString().equals( "-22" ) );
		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_HALF_FLOOR ).toString().equals( "-22" ) );
		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_HALF_EVEN ).toString().equals( "-22" ) );
		assertTrue( ( new BigRational( "-22" ) ).round( BigRational.ROUND_HALF_ODD ).toString().equals( "-22" ) );

		// "BigRational round unnecessary"
		assertTrue( ( new BigRational( "23" ) ).round( BigRational.ROUND_UNNECESSARY ).toString().equals( "23" ) );
		assertTrue( ( new BigRational( "-23" ) ).round( BigRational.ROUND_UNNECESSARY ).toString().equals( "-23" ) );

		try
		{
			( new BigRational( "23.5" ) ).round( BigRational.ROUND_UNNECESSARY );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}
		try
		{
			( new BigRational( "-23.5" ) ).round( BigRational.ROUND_UNNECESSARY );
			assertTrue( false );
		}
		catch ( ArithmeticException e )
		{
			// empty
		}

		// round special cases
		assertTrue( BigRational.ZERO.round().equals( BigRational.ZERO ) );
		assertTrue( BigRational.ZERO.round( BigRational.ROUND_UNNECESSARY ).equals( BigRational.ZERO ) );

		// "BigRational integer part"
		assertTrue( ( new BigRational( "56.8" ) ).integerPart().toString().equals( "56" ) );
		assertTrue( ( new BigRational( "-56.8" ) ).integerPart().toString().equals( "-56" ) );
		assertTrue( ( new BigRational( "0.8" ) ).integerPart().toString().equals( "0" ) );
		assertTrue( ( new BigRational( "-0.8" ) ).integerPart().toString().equals( "0" ) );

		// "BigRational fractional part"
		assertTrue( ( new BigRational( "56.8" ) ).fractionalPart().equals( new BigRational( "0.8" ) ) );
		assertTrue( ( new BigRational( "-56.8" ) ).fractionalPart().equals( new BigRational( "-0.8" ) ) );
		assertTrue( ( new BigRational( "0.8" ) ).fractionalPart().equals( new BigRational( "0.8" ) ) );
		assertTrue( ( new BigRational( "-0.8" ) ).fractionalPart().equals( new BigRational( "-0.8" ) ) );

		// "BigRational integer and fractional part"
		assertTrue( ( new BigRational( "56.8" ) ).integerAndFractionalPart()[ 0 ].equals( new BigRational( "56" ) ) );
		assertTrue( ( new BigRational( "56.8" ) ).integerAndFractionalPart()[ 1 ].equals( new BigRational( "0.8" ) ) );
		assertTrue( ( new BigRational( "-56.8" ) ).integerAndFractionalPart()[ 0 ].equals( new BigRational( "-56" ) ) );
		assertTrue( ( new BigRational( "-56.8" ) ).integerAndFractionalPart()[ 1 ].equals( new BigRational( "-0.8" ) ) );

		// sanity/harmlessness of using internal constants of commonly used numbers
		// scanned in by commonly used radixes
		assertTrue( ( new BigRational( "0", 2 ) ).toString( 2 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0", 10 ) ).toString( 10 ).equals( "0" ) );
		assertTrue( ( new BigRational( "0", 16 ) ).toString( 16 ).equals( "0" ) );
		assertTrue( ( new BigRational( "1", 2 ) ).toString( 2 ).equals( "1" ) );
		assertTrue( ( new BigRational( "1", 10 ) ).toString( 10 ).equals( "1" ) );
		assertTrue( ( new BigRational( "1", 16 ) ).toString( 16 ).equals( "1" ) );
		assertTrue( ( new BigRational( "-1", 2 ) ).toString( 2 ).equals( "-1" ) );
		assertTrue( ( new BigRational( "-1", 10 ) ).toString( 10 ).equals( "-1" ) );
		assertTrue( ( new BigRational( "-1", 16 ) ).toString( 16 ).equals( "-1" ) );
		assertTrue( ( new BigRational( "10", 2 ) ).toString( 2 ).equals( "10" ) );
		assertTrue( ( new BigRational( "10", 10 ) ).toString( 10 ).equals( "10" ) );
		assertTrue( ( new BigRational( "10", 16 ) ).toString( 16 ).equals( "10" ) );

		// more constants
		assertTrue( ( new BigRational( "2", 10 ) ).toString( 10 ).equals( "2" ) );
		assertTrue( ( new BigRational( "2", 16 ) ).toString( 16 ).equals( "2" ) );
		assertTrue( ( new BigRational( "-2", 10 ) ).toString( 10 ).equals( "-2" ) );
		assertTrue( ( new BigRational( "-2", 16 ) ).toString( 16 ).equals( "-2" ) );
		assertTrue( ( new BigRational( "16", 10 ) ).toString( 10 ).equals( "16" ) );
		assertTrue( ( new BigRational( "16", 16 ) ).toString( 16 ).equals( "16" ) );

		// floating point
		// remember: [IEEE 754] floats are exact where the quotient is a power of two

		assertTrue( BigRational.valueOf( 0.0 ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.valueOf( 1.0 ).equals( BigRational.ONE ) );
		assertTrue( BigRational.valueOf( -1.0 ).equals( BigRational.MINUS_ONE ) );

		assertTrue( BigRational.valueOf( 2.0 ).equals( BigRational.valueOf( 2 ) ) );
		assertTrue( BigRational.valueOf( -2.0 ).equals( BigRational.valueOf( -2 ) ) );
		assertTrue( BigRational.valueOf( 4.0 ).equals( BigRational.valueOf( 4 ) ) );
		assertTrue( BigRational.valueOf( -4.0 ).equals( BigRational.valueOf( -4 ) ) );
		assertTrue( BigRational.valueOf( 16.0 ).equals( BigRational.valueOf( 16 ) ) );
		assertTrue( BigRational.valueOf( -16.0 ).equals( BigRational.valueOf( -16 ) ) );

		assertTrue( BigRational.valueOf( 3.0 ).equals( BigRational.valueOf( 3 ) ) );
		assertTrue( BigRational.valueOf( -3.0 ).equals( BigRational.valueOf( -3 ) ) );
		assertTrue( BigRational.valueOf( 6.0 ).equals( BigRational.valueOf( 6 ) ) );
		assertTrue( BigRational.valueOf( -6.0 ).equals( BigRational.valueOf( -6 ) ) );
		assertTrue( BigRational.valueOf( 12.0 ).equals( BigRational.valueOf( 12 ) ) );
		assertTrue( BigRational.valueOf( -12.0 ).equals( BigRational.valueOf( -12 ) ) );

		assertTrue( BigRational.valueOf( 0.5 ).equals( new BigRational( 1, 2 ) ) );
		assertTrue( BigRational.valueOf( -0.5 ).equals( new BigRational( -1, 2 ) ) );
		assertTrue( BigRational.valueOf( 0.25 ).equals( new BigRational( 1, 4 ) ) );
		assertTrue( BigRational.valueOf( -0.25 ).equals( new BigRational( -1, 4 ) ) );
		assertTrue( BigRational.valueOf( 0.0625 ).equals( new BigRational( 1, 16 ) ) );
		assertTrue( BigRational.valueOf( -0.0625 ).equals( new BigRational( -1, 16 ) ) );

		assertTrue( BigRational.valueOf( 1.5 ).equals( new BigRational( 3, 2 ) ) );
		assertTrue( BigRational.valueOf( -1.5 ).equals( new BigRational( -3, 2 ) ) );
		assertTrue( BigRational.valueOf( 0.75 ).equals( new BigRational( 3, 4 ) ) );
		assertTrue( BigRational.valueOf( -0.75 ).equals( new BigRational( -3, 4 ) ) );
		assertTrue( BigRational.valueOf( 0.375 ).equals( new BigRational( 3, 8 ) ) );
		assertTrue( BigRational.valueOf( -0.375 ).equals( new BigRational( -3, 8 ) ) );

		// other conversion direction

		assertTrue( BigRational.ZERO.doubleValue() == 0.0 );
		assertTrue( BigRational.ONE.doubleValue() == 1.0 );
		assertTrue( BigRational.MINUS_ONE.doubleValue() == -1.0 );

		assertTrue( BigRational.valueOf( 2 ).doubleValue() == 2.0 );
		assertTrue( BigRational.valueOf( -2 ).doubleValue() == -2.0 );
		assertTrue( BigRational.valueOf( 4 ).doubleValue() == 4.0 );
		assertTrue( BigRational.valueOf( -4 ).doubleValue() == -4.0 );
		assertTrue( BigRational.valueOf( 16 ).doubleValue() == 16.0 );
		assertTrue( BigRational.valueOf( -16 ).doubleValue() == -16.0 );

		assertTrue( BigRational.valueOf( 3 ).doubleValue() == 3.0 );
		assertTrue( BigRational.valueOf( -3 ).doubleValue() == -3.0 );
		assertTrue( BigRational.valueOf( 6 ).doubleValue() == 6.0 );
		assertTrue( BigRational.valueOf( -6 ).doubleValue() == -6.0 );
		assertTrue( BigRational.valueOf( 12 ).doubleValue() == 12.0 );
		assertTrue( BigRational.valueOf( -12 ).doubleValue() == -12.0 );

		assertTrue( ( new BigRational( 1, 2 ) ).doubleValue() == 0.5 );
		assertTrue( ( new BigRational( -1, 2 ) ).doubleValue() == -0.5 );
		assertTrue( ( new BigRational( 1, 4 ) ).doubleValue() == 0.25 );
		assertTrue( ( new BigRational( -1, 4 ) ).doubleValue() == -0.25 );
		assertTrue( ( new BigRational( 1, 16 ) ).doubleValue() == 0.0625 );
		assertTrue( ( new BigRational( -1, 16 ) ).doubleValue() == -0.0625 );

		assertTrue( ( new BigRational( 3, 2 ) ).doubleValue() == 1.5 );
		assertTrue( ( new BigRational( -3, 2 ) ).doubleValue() == -1.5 );
		assertTrue( ( new BigRational( 3, 4 ) ).doubleValue() == 0.75 );
		assertTrue( ( new BigRational( -3, 4 ) ).doubleValue() == -0.75 );
		assertTrue( ( new BigRational( 3, 8 ) ).doubleValue() == 0.375 );
		assertTrue( ( new BigRational( -3, 8 ) ).doubleValue() == -0.375 );

		// conversion forth and back

		assertTrue( BigRational.valueOf( 0.0 ).doubleValue() == 0.0 );
		assertTrue( BigRational.valueOf( 1.0 ).doubleValue() == 1.0 );
		assertTrue( BigRational.valueOf( -1.0 ).doubleValue() == -1.0 );
		assertTrue( BigRational.valueOf( 2.0 ).doubleValue() == 2.0 );
		assertTrue( BigRational.valueOf( -2.0 ).doubleValue() == -2.0 );

		// maximal and minimal values, and near there
		assertTrue( BigRational.valueOf( Double.MAX_VALUE ).doubleValue() == Double.MAX_VALUE );
		assertTrue( BigRational.valueOf( -Double.MAX_VALUE ).doubleValue() == -Double.MAX_VALUE );
		assertTrue( BigRational.valueOf( Double.MAX_VALUE / 16 ).doubleValue() == Double.MAX_VALUE / 16 );
		assertTrue( BigRational.valueOf( -Double.MAX_VALUE / 16 ).doubleValue() == -Double.MAX_VALUE / 16 );
		// [subnormal value]
		assertTrue( BigRational.valueOf( Double.MIN_VALUE ).doubleValue() == Double.MIN_VALUE );
		assertTrue( BigRational.valueOf( -Double.MIN_VALUE ).doubleValue() == -Double.MIN_VALUE );
		assertTrue( BigRational.valueOf( Double.MIN_VALUE * 16 ).doubleValue() == Double.MIN_VALUE * 16 );
		assertTrue( BigRational.valueOf( -Double.MIN_VALUE * 16 ).doubleValue() == -Double.MIN_VALUE * 16 );

		// overflow
		assertTrue( BigRational.valueOf( Double.MAX_VALUE ).multiply( 2 ).doubleValue() == Double.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( Double.MAX_VALUE ).multiply( 4 ).doubleValue() == Double.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( Double.MAX_VALUE ).multiply( BigRational.valueOf( 1.2 ) ).doubleValue() == Double.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( Double.MAX_VALUE ).multiply( 16 ).doubleValue() == Double.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( -Double.MAX_VALUE ).multiply( 2 ).doubleValue() == Double.NEGATIVE_INFINITY );
		assertTrue( BigRational.valueOf( -Double.MAX_VALUE ).multiply( 4 ).doubleValue() == Double.NEGATIVE_INFINITY );
		assertTrue( BigRational.valueOf( -Double.MAX_VALUE ).multiply( BigRational.valueOf( 1.2 ) ).doubleValue() == Double.NEGATIVE_INFINITY );
		assertTrue( BigRational.valueOf( -Double.MAX_VALUE ).multiply( 16 ).doubleValue() == Double.NEGATIVE_INFINITY );

		// underflow
		// note that the (double)x==(double)y test yields true for 0.0==-0.0
		assertTrue( BigRational.valueOf( Double.MIN_VALUE ).divide( 2 ).doubleValue() == 0.0 );
		assertTrue( BigRational.valueOf( Double.MIN_VALUE ).divide( 4 ).doubleValue() == 0.0 );
		assertTrue( BigRational.valueOf( Double.MIN_VALUE ).divide( BigRational.valueOf( 1.2 ) ).doubleValue() == 0.0 );
		assertTrue( BigRational.valueOf( Double.MIN_VALUE ).divide( 16 ).doubleValue() == 0.0 );
		// returning -0.0 (signed zero)
		assertTrue( BigRational.valueOf( -Double.MIN_VALUE ).divide( 2 ).doubleValue() == -0.0 );
		assertTrue( BigRational.valueOf( -Double.MIN_VALUE ).divide( 4 ).doubleValue() == -0.0 );
		assertTrue( BigRational.valueOf( -Double.MIN_VALUE ).divide( BigRational.valueOf( 1.2 ) ).doubleValue() == -0.0 );
		assertTrue( BigRational.valueOf( -Double.MIN_VALUE ).divide( 16 ).doubleValue() == -0.0 );

		// signed underflow, alternative tests
		assertTrue( String.valueOf( BigRational.valueOf( Double.MIN_VALUE ).divide( 2 ).doubleValue() ).equals( "0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( Double.MIN_VALUE ).divide( 4 ).doubleValue() ).equals( "0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( Double.MIN_VALUE ).divide( BigRational.valueOf( 1.2 ) ).doubleValue() ).equals( "0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( Double.MIN_VALUE ).divide( 16 ).doubleValue() ).equals( "0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( -Double.MIN_VALUE ).divide( 2 ).doubleValue() ).equals( "-0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( -Double.MIN_VALUE ).divide( 4 ).doubleValue() ).equals( "-0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( -Double.MIN_VALUE ).divide( BigRational.valueOf( 1.2 ) ).doubleValue() ).equals( "-0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( -Double.MIN_VALUE ).divide( 16 ).doubleValue() ).equals( "-0.0" ) );

		// ulp
		assertTrue( BigRational.valueOf( 1.0 - StrictMath.ulp( 1.0 ) ).doubleValue() == 1.0 - StrictMath.ulp( 1.0 ) );
		assertTrue( BigRational.valueOf( 1.0 + StrictMath.ulp( 1.0 ) ).doubleValue() == 1.0 + StrictMath.ulp( 1.0 ) );
		assertTrue( BigRational.valueOf( 1.0 - StrictMath.ulp( 1.0 ) / 2 ).doubleValue() == 1.0 - StrictMath.ulp( 1.0 ) / 2 );
		assertTrue( BigRational.valueOf( 1.0 + StrictMath.ulp( 1.0 ) / 2 ).doubleValue() == 1.0 + StrictMath.ulp( 1.0 ) / 2 );
		assertTrue( BigRational.valueOf( 1.0 - StrictMath.ulp( 1.0 ) / 4 ).doubleValue() == 1.0 - StrictMath.ulp( 1.0 ) / 4 );
		assertTrue( BigRational.valueOf( 1.0 + StrictMath.ulp( 1.0 ) / 4 ).doubleValue() == 1.0 + StrictMath.ulp( 1.0 ) / 4 );

		// mantissa rounding
		// a delta of ulp/4 is expected to be rounded
		assertTrue( BigRational.valueOf( 1.0 ).subtract( BigRational.valueOf( StrictMath.ulp( 1.0 ) ).divide( 4 ) ).doubleValue() == 1.0 );
		assertTrue( BigRational.valueOf( 16.0 ).subtract( BigRational.valueOf( StrictMath.ulp( 16.0 ) ).divide( 4 ) ).doubleValue() == 16.0 );
		// note: MAX_VALUE is not a power-of-two, so it won't run into the mantissa rounding case in question;
		// and 1/MIN_VALUE is larger than MAX_VALUE (due to MIN_VALUE being subnormal)
		// assertTrue(BigRational.valueOf(0x1P1023).subtract(BigRational.valueOf(StrictMath.ulp(0x1P1023)).divide(4)).doubleValue() == 0x1P1023);

		// more values in between [0 and max/min]
		assertTrue( BigRational.valueOf( StrictMath.sqrt( Double.MAX_VALUE ) ).doubleValue() == StrictMath.sqrt( Double.MAX_VALUE ) );
		assertTrue( BigRational.valueOf( StrictMath.pow( Double.MAX_VALUE, 0.2 ) ).doubleValue() == StrictMath.pow( Double.MAX_VALUE, 0.2 ) );
		assertTrue( BigRational.valueOf( Double.MAX_VALUE ).power( 2 ).doubleValue() == Double.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( Double.MAX_VALUE ).power( 5 ).doubleValue() == Double.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( StrictMath.sqrt( Double.MIN_VALUE ) ).doubleValue() == StrictMath.sqrt( Double.MIN_VALUE ) );
		assertTrue( BigRational.valueOf( StrictMath.pow( Double.MIN_VALUE, 0.2 ) ).doubleValue() == StrictMath.pow( Double.MIN_VALUE, 0.2 ) );
		assertTrue( BigRational.valueOf( Double.MIN_VALUE ).power( 2 ).doubleValue() == 0.0 );
		assertTrue( BigRational.valueOf( Double.MIN_VALUE ).power( 5 ).doubleValue() == 0.0 );

		// Infinities, NaNs
		try
		{
			BigRational.valueOf( Double.POSITIVE_INFINITY );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			BigRational.valueOf( Double.NEGATIVE_INFINITY );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			BigRational.valueOf( Double.NaN );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}

		assertTrue( BigRational.valueOf( StrictMath.E ).doubleValue() == StrictMath.E );
		assertTrue( BigRational.valueOf( StrictMath.PI ).doubleValue() == StrictMath.PI );
		assertTrue( BigRational.valueOf( StrictMath.pow( StrictMath.E, 2 ) ).doubleValue() == StrictMath.pow( StrictMath.E, 2 ) );
		assertTrue( BigRational.valueOf( StrictMath.sqrt( StrictMath.E ) ).doubleValue() == StrictMath.sqrt( StrictMath.E ) );
		assertTrue( BigRational.valueOf( StrictMath.pow( StrictMath.PI, 2 ) ).doubleValue() == StrictMath.pow( StrictMath.PI, 2 ) );
		assertTrue( BigRational.valueOf( StrictMath.sqrt( StrictMath.PI ) ).doubleValue() == StrictMath.sqrt( StrictMath.PI ) );

		// same tests with single precision float

		assertTrue( BigRational.valueOf( 0.0f ).equals( BigRational.ZERO ) );
		assertTrue( BigRational.valueOf( 1.0f ).equals( BigRational.ONE ) );
		assertTrue( BigRational.valueOf( -1.0f ).equals( BigRational.MINUS_ONE ) );

		assertTrue( BigRational.valueOf( 2.0f ).equals( BigRational.valueOf( 2 ) ) );
		assertTrue( BigRational.valueOf( -2.0f ).equals( BigRational.valueOf( -2 ) ) );
		assertTrue( BigRational.valueOf( 4.0f ).equals( BigRational.valueOf( 4 ) ) );
		assertTrue( BigRational.valueOf( -4.0f ).equals( BigRational.valueOf( -4 ) ) );
		assertTrue( BigRational.valueOf( 16.0f ).equals( BigRational.valueOf( 16 ) ) );
		assertTrue( BigRational.valueOf( -16.0f ).equals( BigRational.valueOf( -16 ) ) );

		assertTrue( BigRational.valueOf( 3.0f ).equals( BigRational.valueOf( 3 ) ) );
		assertTrue( BigRational.valueOf( -3.0f ).equals( BigRational.valueOf( -3 ) ) );
		assertTrue( BigRational.valueOf( 6.0f ).equals( BigRational.valueOf( 6 ) ) );
		assertTrue( BigRational.valueOf( -6.0f ).equals( BigRational.valueOf( -6 ) ) );
		assertTrue( BigRational.valueOf( 12.0f ).equals( BigRational.valueOf( 12 ) ) );
		assertTrue( BigRational.valueOf( -12.0f ).equals( BigRational.valueOf( -12 ) ) );

		assertTrue( BigRational.valueOf( 0.5f ).equals( new BigRational( 1, 2 ) ) );
		assertTrue( BigRational.valueOf( -0.5f ).equals( new BigRational( -1, 2 ) ) );
		assertTrue( BigRational.valueOf( 0.25f ).equals( new BigRational( 1, 4 ) ) );
		assertTrue( BigRational.valueOf( -0.25f ).equals( new BigRational( -1, 4 ) ) );
		assertTrue( BigRational.valueOf( 0.0625f ).equals( new BigRational( 1, 16 ) ) );
		assertTrue( BigRational.valueOf( -0.0625f ).equals( new BigRational( -1, 16 ) ) );

		assertTrue( BigRational.valueOf( 1.5f ).equals( new BigRational( 3, 2 ) ) );
		assertTrue( BigRational.valueOf( -1.5f ).equals( new BigRational( -3, 2 ) ) );
		assertTrue( BigRational.valueOf( 0.75f ).equals( new BigRational( 3, 4 ) ) );
		assertTrue( BigRational.valueOf( -0.75f ).equals( new BigRational( -3, 4 ) ) );
		assertTrue( BigRational.valueOf( 0.375f ).equals( new BigRational( 3, 8 ) ) );
		assertTrue( BigRational.valueOf( -0.375f ).equals( new BigRational( -3, 8 ) ) );

		// other conversion direction

		assertTrue( BigRational.ZERO.floatValue() == 0.0f );
		assertTrue( BigRational.ONE.floatValue() == 1.0f );
		assertTrue( BigRational.MINUS_ONE.floatValue() == -1.0f );

		assertTrue( BigRational.valueOf( 2 ).floatValue() == 2.0f );
		assertTrue( BigRational.valueOf( -2 ).floatValue() == -2.0f );
		assertTrue( BigRational.valueOf( 4 ).floatValue() == 4.0f );
		assertTrue( BigRational.valueOf( -4 ).floatValue() == -4.0f );
		assertTrue( BigRational.valueOf( 16 ).floatValue() == 16.0f );
		assertTrue( BigRational.valueOf( -16 ).floatValue() == -16.0f );

		assertTrue( BigRational.valueOf( 3 ).floatValue() == 3.0f );
		assertTrue( BigRational.valueOf( -3 ).floatValue() == -3.0f );
		assertTrue( BigRational.valueOf( 6 ).floatValue() == 6.0f );
		assertTrue( BigRational.valueOf( -6 ).floatValue() == -6.0f );
		assertTrue( BigRational.valueOf( 12 ).floatValue() == 12.0f );
		assertTrue( BigRational.valueOf( -12 ).floatValue() == -12.0f );

		assertTrue( ( new BigRational( 1, 2 ) ).floatValue() == 0.5f );
		assertTrue( ( new BigRational( -1, 2 ) ).floatValue() == -0.5f );
		assertTrue( ( new BigRational( 1, 4 ) ).floatValue() == 0.25f );
		assertTrue( ( new BigRational( -1, 4 ) ).floatValue() == -0.25f );
		assertTrue( ( new BigRational( 1, 16 ) ).floatValue() == 0.0625f );
		assertTrue( ( new BigRational( -1, 16 ) ).floatValue() == -0.0625f );

		assertTrue( ( new BigRational( 3, 2 ) ).floatValue() == 1.5f );
		assertTrue( ( new BigRational( -3, 2 ) ).floatValue() == -1.5f );
		assertTrue( ( new BigRational( 3, 4 ) ).floatValue() == 0.75f );
		assertTrue( ( new BigRational( -3, 4 ) ).floatValue() == -0.75f );
		assertTrue( ( new BigRational( 3, 8 ) ).floatValue() == 0.375f );
		assertTrue( ( new BigRational( -3, 8 ) ).floatValue() == -0.375f );

		// conversion forth and back

		assertTrue( BigRational.valueOf( 0.0f ).floatValue() == 0.0f );
		assertTrue( BigRational.valueOf( 1.0f ).floatValue() == 1.0f );
		assertTrue( BigRational.valueOf( -1.0f ).floatValue() == -1.0f );
		assertTrue( BigRational.valueOf( 2.0f ).floatValue() == 2.0f );
		assertTrue( BigRational.valueOf( -2.0f ).floatValue() == -2.0f );

		// maximal and minimal values, and near there
		assertTrue( BigRational.valueOf( Float.MAX_VALUE ).floatValue() == Float.MAX_VALUE );
		assertTrue( BigRational.valueOf( -Float.MAX_VALUE ).floatValue() == -Float.MAX_VALUE );
		assertTrue( BigRational.valueOf( Float.MAX_VALUE / 16 ).floatValue() == Float.MAX_VALUE / 16 );
		assertTrue( BigRational.valueOf( -Float.MAX_VALUE / 16 ).floatValue() == -Float.MAX_VALUE / 16 );
		// [subnormal value]
		assertTrue( BigRational.valueOf( Float.MIN_VALUE ).floatValue() == Float.MIN_VALUE );
		assertTrue( BigRational.valueOf( -Float.MIN_VALUE ).floatValue() == -Float.MIN_VALUE );
		assertTrue( BigRational.valueOf( Float.MIN_VALUE * 16 ).floatValue() == Float.MIN_VALUE * 16 );
		assertTrue( BigRational.valueOf( -Float.MIN_VALUE * 16 ).floatValue() == -Float.MIN_VALUE * 16 );

		// overflow
		assertTrue( BigRational.valueOf( Float.MAX_VALUE ).multiply( 2 ).floatValue() == Float.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( Float.MAX_VALUE ).multiply( 4 ).floatValue() == Float.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( Float.MAX_VALUE ).multiply( BigRational.valueOf( 1.2f ) ).floatValue() == Float.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( Float.MAX_VALUE ).multiply( 16 ).floatValue() == Float.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( -Float.MAX_VALUE ).multiply( 2 ).floatValue() == Float.NEGATIVE_INFINITY );
		assertTrue( BigRational.valueOf( -Float.MAX_VALUE ).multiply( 4 ).floatValue() == Float.NEGATIVE_INFINITY );
		assertTrue( BigRational.valueOf( -Float.MAX_VALUE ).multiply( BigRational.valueOf( 1.2f ) ).floatValue() == Float.NEGATIVE_INFINITY );
		assertTrue( BigRational.valueOf( -Float.MAX_VALUE ).multiply( 16 ).floatValue() == Float.NEGATIVE_INFINITY );

		// underflow
		// note that the (float)x==(float)y test yields true for 0.0f==-0.0f
		assertTrue( BigRational.valueOf( Float.MIN_VALUE ).divide( 2 ).floatValue() == 0.0f );
		assertTrue( BigRational.valueOf( Float.MIN_VALUE ).divide( 4 ).floatValue() == 0.0f );
		assertTrue( BigRational.valueOf( Float.MIN_VALUE ).divide( BigRational.valueOf( 1.2f ) ).floatValue() == 0.0f );
		assertTrue( BigRational.valueOf( Float.MIN_VALUE ).divide( 16 ).floatValue() == 0.0f );
		// returning -0.0f (signed zero)
		assertTrue( BigRational.valueOf( -Float.MIN_VALUE ).divide( 2 ).floatValue() == -0.0f );
		assertTrue( BigRational.valueOf( -Float.MIN_VALUE ).divide( 4 ).floatValue() == -0.0f );
		assertTrue( BigRational.valueOf( -Float.MIN_VALUE ).divide( BigRational.valueOf( 1.2f ) ).floatValue() == -0.0f );
		assertTrue( BigRational.valueOf( -Float.MIN_VALUE ).divide( 16 ).floatValue() == -0.0f );

		// signed underflow, alternative tests
		assertTrue( String.valueOf( BigRational.valueOf( Float.MIN_VALUE ).divide( 2 ).floatValue() ).equals( "0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( Float.MIN_VALUE ).divide( 4 ).floatValue() ).equals( "0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( Float.MIN_VALUE ).divide( BigRational.valueOf( 1.2f ) ).floatValue() ).equals( "0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( Float.MIN_VALUE ).divide( 16 ).floatValue() ).equals( "0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( -Float.MIN_VALUE ).divide( 2 ).floatValue() ).equals( "-0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( -Float.MIN_VALUE ).divide( 4 ).floatValue() ).equals( "-0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( -Float.MIN_VALUE ).divide( BigRational.valueOf( 1.2f ) ).floatValue() ).equals( "-0.0" ) );
		assertTrue( String.valueOf( BigRational.valueOf( -Float.MIN_VALUE ).divide( 16 ).floatValue() ).equals( "-0.0" ) );

		// ulp
		assertTrue( BigRational.valueOf( 1.0f - StrictMath.ulp( 1.0f ) ).floatValue() == 1.0f - StrictMath.ulp( 1.0f ) );
		assertTrue( BigRational.valueOf( 1.0f + StrictMath.ulp( 1.0f ) ).floatValue() == 1.0f + StrictMath.ulp( 1.0f ) );
		assertTrue( BigRational.valueOf( 1.0f - StrictMath.ulp( 1.0f ) / 2 ).floatValue() == 1.0f - StrictMath.ulp( 1.0f ) / 2 );
		assertTrue( BigRational.valueOf( 1.0f + StrictMath.ulp( 1.0f ) / 2 ).floatValue() == 1.0f + StrictMath.ulp( 1.0f ) / 2 );
		assertTrue( BigRational.valueOf( 1.0f - StrictMath.ulp( 1.0f ) / 4 ).floatValue() == 1.0f - StrictMath.ulp( 1.0f ) / 4 );
		assertTrue( BigRational.valueOf( 1.0f + StrictMath.ulp( 1.0f ) / 4 ).floatValue() == 1.0f + StrictMath.ulp( 1.0f ) / 4 );

		// mantissa rounding
		// a delta of ulp/4 is expected to be rounded
		assertTrue( BigRational.valueOf( 1.0f ).subtract( BigRational.valueOf( StrictMath.ulp( 1.0f ) ).divide( 4 ) ).floatValue() == 1.0f );
		assertTrue( BigRational.valueOf( 16.0f ).subtract( BigRational.valueOf( StrictMath.ulp( 16.0f ) ).divide( 4 ) ).floatValue() == 16.0f );
		// note: MAX_VALUE is not a power-of-two, so it won't run into the mantissa rounding case in question;
		// and 1/MIN_VALUE is larger than MAX_VALUE (due to MIN_VALUE being subnormal)
		// assertTrue(BigRational.valueOf(0x1P127f).subtract(BigRational.valueOf(StrictMath.ulp(0x1P127f)).divide(4)).floatValue() == 0x1P127f);

		// more values in between [0 and max/min]
		assertTrue( BigRational.valueOf( (float)StrictMath.sqrt( Float.MAX_VALUE ) ).floatValue() == (float)StrictMath.sqrt( Float.MAX_VALUE ) );
		assertTrue( BigRational.valueOf( (float)StrictMath.pow( Float.MAX_VALUE, 0.2f ) ).floatValue() == (float)StrictMath.pow( Float.MAX_VALUE, 0.2f ) );
		assertTrue( BigRational.valueOf( Float.MAX_VALUE ).power( 2 ).floatValue() == Float.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( Float.MAX_VALUE ).power( 5 ).floatValue() == Float.POSITIVE_INFINITY );
		assertTrue( BigRational.valueOf( (float)StrictMath.sqrt( Float.MIN_VALUE ) ).floatValue() == (float)StrictMath.sqrt( Float.MIN_VALUE ) );
		assertTrue( BigRational.valueOf( (float)StrictMath.pow( Float.MIN_VALUE, 0.2f ) ).floatValue() == (float)StrictMath.pow( Float.MIN_VALUE, 0.2f ) );
		assertTrue( BigRational.valueOf( Float.MIN_VALUE ).power( 2 ).floatValue() == 0.0f );
		assertTrue( BigRational.valueOf( Float.MIN_VALUE ).power( 5 ).floatValue() == 0.0f );

		// Infinities, NaNs
		try
		{
			BigRational.valueOf( Float.POSITIVE_INFINITY );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			BigRational.valueOf( Float.NEGATIVE_INFINITY );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}
		try
		{
			BigRational.valueOf( Float.NaN );
			assertTrue( false );
		}
		catch ( NumberFormatException e )
		{
			// empty
		}

		assertTrue( BigRational.valueOf( (float)StrictMath.E ).floatValue() == (float)StrictMath.E );
		assertTrue( BigRational.valueOf( (float)StrictMath.PI ).floatValue() == (float)StrictMath.PI );
		assertTrue( BigRational.valueOf( (float)StrictMath.pow( (float)StrictMath.E, 2 ) ).floatValue() == (float)StrictMath.pow( (float)StrictMath.E, 2 ) );
		assertTrue( BigRational.valueOf( (float)StrictMath.sqrt( (float)StrictMath.E ) ).floatValue() == (float)StrictMath.sqrt( (float)StrictMath.E ) );
		assertTrue( BigRational.valueOf( (float)StrictMath.pow( (float)StrictMath.PI, 2 ) ).floatValue() == (float)StrictMath.pow( (float)StrictMath.PI, 2 ) );
		assertTrue( BigRational.valueOf( (float)StrictMath.sqrt( (float)StrictMath.PI ) ).floatValue() == (float)StrictMath.sqrt( (float)StrictMath.PI ) );

		// toIeee754 rounding vs. double to float rounding
		// tests commented out: we don't really care if our mode of rounding
		// [doubles indirectly to floats] (round-half-up) is different
		// (e.g. from round-half-even)
		// assertTrue(BigRational.valueOf(0.0).floatValue() == (float)0.0);
		// assertTrue(BigRational.valueOf(1.0).floatValue() == (float)1.0);
		// assertTrue(BigRational.valueOf(0.5).floatValue() == (float)0.5);
		// assertTrue(BigRational.valueOf(0.1).floatValue() == (float)0.1);
		// assertTrue(BigRational.valueOf((double)0.1f).floatValue() == (float)(double)0.1f);
		// assertTrue(BigRational.valueOf((double)0.2f).floatValue() == (float)(double)0.2f);
		// assertTrue(BigRational.valueOf((double)0.3f).floatValue() == (float)(double)0.3f);
		// assertTrue(BigRational.valueOf((double)0.4f).floatValue() == (float)(double)0.4f);
		// assertTrue(BigRational.valueOf(1.0 + 0.4 * StrictMath.ulp(1.0f)).floatValue() == (float)(1.0 + 0.4 * StrictMath.ulp(1.0f)));
		// assertTrue(BigRational.valueOf(1.0 + 0.8 * StrictMath.ulp(1.0f)).floatValue() == (float)(1.0 + 0.8 * StrictMath.ulp(1.0f)));
		// assertTrue(BigRational.valueOf(1.0 + 1.6 * StrictMath.ulp(1.0f)).floatValue() == (float)(1.0 + 1.6 * StrictMath.ulp(1.0f)));

		// IEEE 754 constants
		// commented out since not public
		// assertTrue(BigRational.HALF_FLOAT_FRACTION_SIZE + BigRational.HALF_FLOAT_EXPONENT_SIZE + 1 == 0x10);
		// assertTrue(BigRational.SINGLE_FLOAT_FRACTION_SIZE + BigRational.SINGLE_FLOAT_EXPONENT_SIZE + 1 == 0x20);
		// assertTrue(BigRational.DOUBLE_FLOAT_FRACTION_SIZE + BigRational.DOUBLE_FLOAT_EXPONENT_SIZE + 1 == 0x40);
		// assertTrue(BigRational.QUAD_FLOAT_FRACTION_SIZE + BigRational.QUAD_FLOAT_EXPONENT_SIZE + 1 == 0x80);
		// assertTrue(BigRational.HALF_FLOAT_FRACTION_SIZE >= 0);
		// assertTrue(BigRational.HALF_FLOAT_EXPONENT_SIZE >= 0);
		// // assertTrue(BigRational.HALF_FLOAT_EXPONENT_SIZE <= 64 - 1);
		// assertTrue(BigRational.HALF_FLOAT_EXPONENT_SIZE <= 31);
		// assertTrue(BigRational.SINGLE_FLOAT_FRACTION_SIZE >= 0);
		// assertTrue(BigRational.SINGLE_FLOAT_EXPONENT_SIZE >= 0);
		// // assertTrue(BigRational.SINGLE_FLOAT_EXPONENT_SIZE <= 64 - 1);
		// assertTrue(BigRational.SINGLE_FLOAT_EXPONENT_SIZE <= 31);
		// assertTrue(BigRational.DOUBLE_FLOAT_FRACTION_SIZE >= 0);
		// assertTrue(BigRational.DOUBLE_FLOAT_EXPONENT_SIZE >= 0);
		// // assertTrue(BigRational.DOUBLE_FLOAT_EXPONENT_SIZE <= 64 - 1);
		// assertTrue(BigRational.DOUBLE_FLOAT_EXPONENT_SIZE <= 31);
		// assertTrue(BigRational.QUAD_FLOAT_FRACTION_SIZE >= 0);
		// assertTrue(BigRational.QUAD_FLOAT_EXPONENT_SIZE >= 0);
		// // assertTrue(BigRational.QUAD_FLOAT_EXPONENT_SIZE <= 64 - 1);
		// assertTrue(BigRational.QUAD_FLOAT_EXPONENT_SIZE <= 31);

		// direct fromIeee754/toIeee754 methods

		// half (5 exponent bits)
		assertTrue( BigRational.valueOfHalfBits( (short)0 ).equals( BigRational.valueOf( "0" ) ) );
		assertTrue( BigRational.valueOf( "0" ).halfBitsValue() == 0 );
		assertTrue( BigRational.valueOfHalfBits( (short)0x3c00 ).equals( BigRational.valueOf( "1" ) ) );
		assertTrue( BigRational.valueOf( "1" ).halfBitsValue() == (short)0x3c00 );
		assertTrue( BigRational.valueOfHalfBits( (short)0xbc00 ).equals( BigRational.valueOf( "-1" ) ) );
		assertTrue( BigRational.valueOf( "-1" ).halfBitsValue() == (short)0xbc00 );
		assertTrue( BigRational.valueOfHalfBits( (short)0x3e00 ).equals( BigRational.valueOf( "1.5" ) ) );
		assertTrue( BigRational.valueOf( "1.5" ).halfBitsValue() == (short)0x3e00 );
		assertTrue( BigRational.valueOfHalfBits( (short)0xbe00 ).equals( BigRational.valueOf( "-1.5" ) ) );
		assertTrue( BigRational.valueOf( "-1.5" ).halfBitsValue() == (short)0xbe00 );
		assertTrue( BigRational.valueOfHalfBits( (short)0x3f00 ).equals( BigRational.valueOf( "1.75" ) ) );
		assertTrue( BigRational.valueOf( "1.75" ).halfBitsValue() == (short)0x3f00 );
		assertTrue( BigRational.valueOfHalfBits( (short)0xbf00 ).equals( BigRational.valueOf( "-1.75" ) ) );
		assertTrue( BigRational.valueOf( "-1.75" ).halfBitsValue() == (short)0xbf00 );

		// single (8 exponent bits)
		assertTrue( BigRational.valueOfSingleBits( 0 ).equals( BigRational.valueOf( "0" ) ) );
		assertTrue( BigRational.valueOf( "0" ).singleBitsValue() == 0 );
		assertTrue( BigRational.valueOfSingleBits( 0x3f800000 ).equals( BigRational.valueOf( "1" ) ) );
		assertTrue( BigRational.valueOf( "1" ).singleBitsValue() == 0x3f800000 );
		assertTrue( BigRational.valueOfSingleBits( 0xbf800000 ).equals( BigRational.valueOf( "-1" ) ) );
		assertTrue( BigRational.valueOf( "-1" ).singleBitsValue() == 0xbf800000 );
		assertTrue( BigRational.valueOfSingleBits( 0x3fc00000 ).equals( BigRational.valueOf( "1.5" ) ) );
		assertTrue( BigRational.valueOf( "1.5" ).singleBitsValue() == 0x3fc00000 );
		assertTrue( BigRational.valueOfSingleBits( 0xbfc00000 ).equals( BigRational.valueOf( "-1.5" ) ) );
		assertTrue( BigRational.valueOf( "-1.5" ).singleBitsValue() == 0xbfc00000 );
		assertTrue( BigRational.valueOfSingleBits( 0x3fe00000 ).equals( BigRational.valueOf( "1.75" ) ) );
		assertTrue( BigRational.valueOf( "1.75" ).singleBitsValue() == 0x3fe00000 );
		assertTrue( BigRational.valueOfSingleBits( 0xbfe00000 ).equals( BigRational.valueOf( "-1.75" ) ) );
		assertTrue( BigRational.valueOf( "-1.75" ).singleBitsValue() == 0xbfe00000 );

		// double (11 exponent bits)
		assertTrue( BigRational.valueOfDoubleBits( 0 ).equals( BigRational.valueOf( "0" ) ) );
		assertTrue( BigRational.valueOf( "0" ).doubleBitsValue() == 0 );
		assertTrue( BigRational.valueOfDoubleBits( 0x3ff0000000000000l ).equals( BigRational.valueOf( "1" ) ) );
		assertTrue( BigRational.valueOf( "1" ).doubleBitsValue() == 0x3ff0000000000000l );
		assertTrue( BigRational.valueOfDoubleBits( 0xbff0000000000000l ).equals( BigRational.valueOf( "-1" ) ) );
		assertTrue( BigRational.valueOf( "-1" ).doubleBitsValue() == 0xbff0000000000000l );
		assertTrue( BigRational.valueOfDoubleBits( 0x3ff8000000000000l ).equals( BigRational.valueOf( "1.5" ) ) );
		assertTrue( BigRational.valueOf( "1.5" ).doubleBitsValue() == 0x3ff8000000000000l );
		assertTrue( BigRational.valueOfDoubleBits( 0xbff8000000000000l ).equals( BigRational.valueOf( "-1.5" ) ) );
		assertTrue( BigRational.valueOf( "-1.5" ).doubleBitsValue() == 0xbff8000000000000l );
		assertTrue( BigRational.valueOfDoubleBits( 0x3ffc000000000000l ).equals( BigRational.valueOf( "1.75" ) ) );
		assertTrue( BigRational.valueOf( "1.75" ).doubleBitsValue() == 0x3ffc000000000000l );
		assertTrue( BigRational.valueOfDoubleBits( 0xbffc000000000000l ).equals( BigRational.valueOf( "-1.75" ) ) );
		assertTrue( BigRational.valueOf( "-1.75" ).doubleBitsValue() == 0xbffc000000000000l );

		// quadBitsEqual
		assertTrue( BigRational.quadBitsEqual( new long[] { 0xfffeffffffffffffl, 0xffffffffffffffffl, }, new long[] { 0xfffeffffffffffffl, 0xffffffffffffffffl, } ) );
		assertTrue( !BigRational.quadBitsEqual( new long[] { 0xfffeffffffffffffl, 0xffffffffffffffffl, }, new long[] { 0xffffffffffffffffl, 0xfffeffffffffffffl, } ) );

		// quad (15 exponent bits)
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOf( "0" ).quadBitsValue(), new long[] { 0, 0, } ) );
		assertTrue( BigRational.valueOfQuadBits( new long[] { 0x3fff000000000000l, 0, } ).equals( BigRational.valueOf( "1" ) ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOf( "1" ).quadBitsValue(), new long[] { 0x3fff000000000000l, 0, } ) );
		assertTrue( BigRational.valueOfQuadBits( new long[] { 0xbfff000000000000l, 0, } ).equals( BigRational.valueOf( "-1" ) ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOf( "-1" ).quadBitsValue(), new long[] { 0xbfff000000000000l, 0, } ) );
		assertTrue( BigRational.valueOfQuadBits( new long[] { 0x3fff800000000000l, 0, } ).equals( BigRational.valueOf( "1.5" ) ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOf( "1.5" ).quadBitsValue(), new long[] { 0x3fff800000000000l, 0, } ) );
		assertTrue( BigRational.valueOfQuadBits( new long[] { 0xbfff800000000000l, 0, } ).equals( BigRational.valueOf( "-1.5" ) ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOf( "-1.5" ).quadBitsValue(), new long[] { 0xbfff800000000000l, 0, } ) );
		assertTrue( BigRational.valueOfQuadBits( new long[] { 0x3fffc00000000000l, 0, } ).equals( BigRational.valueOf( "1.75" ) ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOf( "1.75" ).quadBitsValue(), new long[] { 0x3fffc00000000000l, 0, } ) );
		assertTrue( BigRational.valueOfQuadBits( new long[] { 0xbfffc00000000000l, 0, } ).equals( BigRational.valueOf( "-1.75" ) ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOf( "-1.75" ).quadBitsValue(), new long[] { 0xbfffc00000000000l, 0, } ) );

		// more quad tests
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0000000000000000l, 0x0000000000000000l, } ).quadBitsValue(), new long[] { 0x0000000000000000l, 0x0000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0000000000000001l, 0x0000000000000000l, } ).quadBitsValue(), new long[] { 0x0000000000000001l, 0x0000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0000000000000000l, 0x8000000000000000l, } ).quadBitsValue(), new long[] { 0x0000000000000000l, 0x8000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0000000000000000l, 0x0000000000000001l, } ).quadBitsValue(), new long[] { 0x0000000000000000l, 0x0000000000000001l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0000000000000002l, 0x0000000000000000l, } ).quadBitsValue(), new long[] { 0x0000000000000002l, 0x0000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0000000000000000l, 0x4000000000000000l, } ).quadBitsValue(), new long[] { 0x0000000000000000l, 0x4000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0000000000000000l, 0x0000000000000002l, } ).quadBitsValue(), new long[] { 0x0000000000000000l, 0x0000000000000002l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x7ffe000000000000l, 0x0000000000000000l, } ).quadBitsValue(), new long[] { 0x7ffe000000000000l, 0x0000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x7ffe000000000001l, 0x0000000000000000l, } ).quadBitsValue(), new long[] { 0x7ffe000000000001l, 0x0000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x7ffe000000000000l, 0x8000000000000000l, } ).quadBitsValue(), new long[] { 0x7ffe000000000000l, 0x8000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x7ffe000000000000l, 0x0000000000000001l, } ).quadBitsValue(), new long[] { 0x7ffe000000000000l, 0x0000000000000001l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x7ffe000000000002l, 0x0000000000000000l, } ).quadBitsValue(), new long[] { 0x7ffe000000000002l, 0x0000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x7ffe000000000000l, 0x4000000000000000l, } ).quadBitsValue(), new long[] { 0x7ffe000000000000l, 0x4000000000000000l, } ) );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x7ffe000000000000l, 0x0000000000000002l, } ).quadBitsValue(), new long[] { 0x7ffe000000000000l, 0x0000000000000002l, } ) );

		// forth and back

		// most bits possible set (i.e. largest negative)
		assertTrue( BigRational.valueOfHalfBits( (short)0xfbff ).halfBitsValue() == (short)0xfbff );
		assertTrue( BigRational.valueOfSingleBits( 0xff7fffff ).singleBitsValue() == 0xff7fffff );
		assertTrue( BigRational.valueOfDoubleBits( 0xffefffffffffffffl ).doubleBitsValue() == 0xffefffffffffffffl );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0xfffeffffffffffffl, 0xffffffffffffffffl, } ).quadBitsValue(), new long[] { 0xfffeffffffffffffl, 0xffffffffffffffffl, } ) );

		// smallest non-subnormal number
		assertTrue( BigRational.valueOfHalfBits( (short)0x0400 ).halfBitsValue() == (short)0x0400 );
		assertTrue( BigRational.valueOfSingleBits( 0x00800000 ).singleBitsValue() == 0x00800000 );
		assertTrue( BigRational.valueOfDoubleBits( 0x0010000000000000l ).doubleBitsValue() == 0x0010000000000000l );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0001000000000000l, 0x0000000000000000l, } ).quadBitsValue(), new long[] { 0x0001000000000000l, 0x0000000000000000l, } ) );
		// largest subnormal number
		assertTrue( BigRational.valueOfHalfBits( (short)0x02ff ).halfBitsValue() == (short)0x02ff );
		assertTrue( BigRational.valueOfSingleBits( 0x004fffff ).singleBitsValue() == 0x004fffff );
		assertTrue( BigRational.valueOfDoubleBits( 0x0008ffffffffffffl ).doubleBitsValue() == 0x0008ffffffffffffl );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x00008fffffffffffl, 0xffffffffffffffffl, } ).quadBitsValue(), new long[] { 0x00008fffffffffffl, 0xffffffffffffffffl, } ) );
		// largest subnormal number with one bit set only
		assertTrue( BigRational.valueOfHalfBits( (short)0x0200 ).halfBitsValue() == (short)0x0200 );
		assertTrue( BigRational.valueOfSingleBits( 0x00400000 ).singleBitsValue() == 0x00400000 );
		assertTrue( BigRational.valueOfDoubleBits( 0x0008000000000000l ).doubleBitsValue() == 0x0008000000000000l );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0000800000000000l, 0x0000000000000000l, } ).quadBitsValue(), new long[] { 0x0000800000000000l, 0x0000000000000000l, } ) );
		// one less of above
		assertTrue( BigRational.valueOfHalfBits( (short)0x01ff ).halfBitsValue() == (short)0x01ff );
		assertTrue( BigRational.valueOfSingleBits( 0x003fffff ).singleBitsValue() == 0x003fffff );
		assertTrue( BigRational.valueOfDoubleBits( 0x0007ffffffffffffl ).doubleBitsValue() == 0x0007ffffffffffffl );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x00007fffffffffffl, 0xffffffffffffffffl, } ).quadBitsValue(), new long[] { 0x00007fffffffffffl, 0xffffffffffffffffl, } ) );
		// half of above
		assertTrue( BigRational.valueOfHalfBits( (short)0x0100 ).halfBitsValue() == (short)0x0100 );
		assertTrue( BigRational.valueOfSingleBits( 0x00200000 ).singleBitsValue() == 0x00200000 );
		assertTrue( BigRational.valueOfDoubleBits( 0x0004000000000000l ).doubleBitsValue() == 0x0004000000000000l );
		assertTrue( BigRational.quadBitsEqual( BigRational.valueOfQuadBits( new long[] { 0x0000400000000000l, 0x0000000000000000l, } ).quadBitsValue(), new long[] { 0x0000400000000000l, 0x0000000000000000l, } ) );

		// round-off vs. exact
		assertTrue( BigRational.valueOfHalfBits( BigRational.valueOf( "0.125" ).halfBitsValue() ).equals( BigRational.valueOf( "0.125" ) ) );
		assertTrue( BigRational.valueOfSingleBits( BigRational.valueOf( "0.125" ).singleBitsValue() ).equals( BigRational.valueOf( "0.125" ) ) );
		assertTrue( BigRational.valueOfDoubleBits( BigRational.valueOf( "0.125" ).doubleBitsValue() ).equals( BigRational.valueOf( "0.125" ) ) );
		assertTrue( BigRational.valueOfQuadBits( BigRational.valueOf( "0.125" ).quadBitsValue() ).equals( BigRational.valueOf( "0.125" ) ) );
		assertTrue( !BigRational.valueOfHalfBits( BigRational.valueOf( "0.1" ).halfBitsValue() ).equals( BigRational.valueOf( "0.1" ) ) );
		assertTrue( !BigRational.valueOfSingleBits( BigRational.valueOf( "0.1" ).singleBitsValue() ).equals( BigRational.valueOf( "0.1" ) ) );
		assertTrue( !BigRational.valueOfDoubleBits( BigRational.valueOf( "0.1" ).doubleBitsValue() ).equals( BigRational.valueOf( "0.1" ) ) );
		assertTrue( !BigRational.valueOfQuadBits( BigRational.valueOf( "0.1" ).quadBitsValue() ).equals( BigRational.valueOf( "0.1" ) ) );

		// done.
	}
}
