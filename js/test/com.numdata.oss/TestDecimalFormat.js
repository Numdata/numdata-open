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

import { assert } from 'chai';
import DecimalFormat from '../../lib/com.numdata.oss/DecimalFormat';

describe( 'DecimalFormat', function()
{
	const inputs = [ 0, 0.001, 1, -1, 123456, 0.123456, 0.654321, 123.456, -123.456, 123456.789012 ];

	const tests = [
		{ pattern: '0', expected: ['0', '0', '1', '-1', '123456', '0', '1', '123', '-123', '123457' ] },
		{ pattern: '00', expected: ['00', '00', '01', '-01', '123456', '00', '01', '123', '-123', '123457' ] },
		{ pattern: '0.0', expected: ['0.0', '0.0', '1.0', '-1.0', '123456.0', '0.1', '0.7', '123.5', '-123.5', '123456.8' ] },
		{ pattern: '0.000', expected: ['0.000', '0.001', '1.000', '-1.000', '123456.000', '0.123', '0.654', '123.456', '-123.456', '123456.789' ] },
		{ pattern: '0.#', expected: ['0', '0', '1', '-1', '123456', '0.1', '0.7', '123.5', '-123.5', '123456.8' ] },
		{ pattern: '0.##', expected: ['0', '0', '1', '-1', '123456', '0.12', '0.65', '123.46', '-123.46', '123456.79' ] },
		{ pattern: '0.###', expected: ['0', '0.001', '1', '-1', '123456', '0.123', '0.654', '123.456', '-123.456', '123456.789' ] },
		{ pattern: '.0', expected: ['.0', '.0', '1.0', '-1.0', '123456.0', '.1', '.7', '123.5', '-123.5', '123456.8' ] },
		{ pattern: '0.00##', expected: ['0.00', '0.001', '1.00', '-1.00', '123456.00', '0.1235', '0.6543', '123.456', '-123.456', '123456.789' ] }
	];

	tests.forEach( test =>
	{
		describe( 'pattern: ' + test.pattern, function() {
			const decimalFormat = new DecimalFormat( test.pattern );
			for ( let i = 0; i < inputs.length; i++ )
			{
				const expected = test.expected[ i ];
				it( 'format( ' + inputs[ i ] + ' ) -> ' + expected, function() {
					assert.equal( decimalFormat.format( inputs[ i ] ), expected );
				} );
			}
		} );
	} );
} );

// Expected output was verified using the following Java class:
/*
import java.text.*;
import java.util.*;

import com.numdata.oss.ensemble.*;
import org.junit.*;
import static org.junit.Assert.assertEquals;

public class TestDecimalFormat
{
	@Test
	public void format()
	{
		final double[] inputs = { 0, 0.001, 1, -1, 123456, 0.123456, 0.654321, 123.456, -123.456, 123456.789012 };

		final List<Duet<String, List<String>>> tests = Arrays.<Duet<String, List<String>>>asList(
		new BasicDuet<String, List<String>>( "0", Arrays.asList( "0", "0", "1", "-1", "123456", "0", "1", "123", "-123", "123457" ) ),
		new BasicDuet<String, List<String>>( "00", Arrays.asList( "00", "00", "01", "-01", "123456", "00", "01", "123", "-123", "123457" ) ),
		new BasicDuet<String, List<String>>( "0.0", Arrays.asList( "0.0", "0.0", "1.0", "-1.0", "123456.0", "0.1", "0.7", "123.5", "-123.5", "123456.8" ) ),
		new BasicDuet<String, List<String>>( "0.000", Arrays.asList( "0.000", "0.001", "1.000", "-1.000", "123456.000", "0.123", "0.654", "123.456", "-123.456", "123456.789" ) ),
		new BasicDuet<String, List<String>>( "0.#", Arrays.asList( "0", "0", "1", "-1", "123456", "0.1", "0.7", "123.5", "-123.5", "123456.8" ) ),
		new BasicDuet<String, List<String>>( "0.##", Arrays.asList( "0", "0", "1", "-1", "123456", "0.12", "0.65", "123.46", "-123.46", "123456.79" ) ),
		new BasicDuet<String, List<String>>( "0.###", Arrays.asList( "0", "0.001", "1", "-1", "123456", "0.123", "0.654", "123.456", "-123.456", "123456.789" ) ),
		new BasicDuet<String, List<String>>( ".0", Arrays.asList( ".0", ".0", "1.0", "-1.0", "123456.0", ".1", ".7", "123.5", "-123.5", "123456.8" ) ),
		new BasicDuet<String, List<String>>( "0.00##", Arrays.asList( "0.00", "0.001", "1.00", "-1.00", "123456.00", "0.1235", "0.6543", "123.456", "-123.456", "123456.789" ) )
		);

		for ( final Duet<String, List<String>> test : tests )
		{
			final DecimalFormat decimalFormat = new DecimalFormat( test.getValue1() );
			for ( int i = 0; i < inputs.length; i++ )
			{
				final String expected = test.getValue2().get( i );
				assertEquals( "pattern: " + test.getValue1() + ": format( " + inputs[ i ] + " ) -> " + expected, expected, decimalFormat.format( inputs[ i ] ) );
			}
		}
	}
}
 */
