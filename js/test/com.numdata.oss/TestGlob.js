/*
 * Copyright (c) 2017-2017 Numdata BV.  All rights reserved.
 *
 * Numdata Open Source Software License, Version 1.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by
 *        Numdata BV (http://www.numdata.com/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Numdata" must not be used to endorse or promote
 *    products derived from this software without prior written
 *    permission of Numdata BV. For written permission, please contact
 *    info@numdata.com.
 *
 * 5. Products derived from this software may not be called "Numdata",
 *    nor may "Numdata" appear in their name, without prior written
 *    permission of Numdata BV.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE NUMDATA BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import { assert } from 'chai';
import Glob from '../../lib/com.numdata.oss/Glob';

describe( 'Glob', function()
{
	const glob = new Glob();

	describe( 'matchPattern', function()
	{
		function test( expected, value, pattern, message = expected ? "Pattern should match." : "Pattern should not match." )
		{
			it( JSON.stringify( { value: value, pattern: pattern } ), function()
			{
				assert( glob.matchPattern( value, pattern ) === expected, message + ': ' + glob.compilePattern( pattern ) );
			} );
		}

		// Empty/null.
		test( true, null, null, "Null/empty values or patterns always match." );
		test( true, "", null, "Null/empty values or patterns always match." );
		test( true, null, "", "Null/empty values or patterns always match." );
		test( true, "", "", "Null/empty values or patterns always match." );
		test( true, "value", null, "Null/empty values or patterns always match." );
		test( true, null, "pattern", "Null/empty values or patterns always match." );
		test( true, "value", "", "Null/empty values or patterns always match." );
		test( true, "", "pattern", "Null/empty values or patterns always match." );

		// Plain
		test( true, "Hello World!", "Hello World!" );
		test( false, "Hello World!", "Hello World" );
		test( false, "Hello World!", "ello World!" );

		// Asterisk
		test( true, "Hello World!", "*" );
		test( true, "Hello World!", "*!" );
		test( true, "Hello World!", "Hello*" );
		test( true, "Hello World!", "*rld!" );
		test( true, "Hello World!", "*lo Wo*" );
		test( false, "Hello World!", "Hello*Glob" );
		test( false, "Hello World!", "Glob*World!" );
		test( false, "Hello World!", "*World" );
		test( false, "Hello World!", "ello*" );

		// Question mark
		test( true, "Hello World!", "Hello World?" );
		test( true, "Hello World!", "?ello World!" );
		test( true, "Hello World!", "He?lo??orld!" );
		test( false, "Hello World!", "Hello Worl?" );
		test( false, "Hello World!", "?llo World!" );
		test( false, "Hello World!", "Hello ?World!" );

		// Pipe
		test( true, "Hello World!", "Hello*|Hello Worl?" );
		test( true, "Hello World!", "Hello*Glob|Hello World?" );
		test( true, "Hello World!", "Hello*|Hello Worl?|Hello World" );
		test( true, "Hello World!", "Hello*Glob|Hello World?|Hello World" );
		test( true, "Hello World!", "Hello*Glob|Hello Worl?|Hello World!" );
		test( false, "Hello World!", "Hello*Glob|Hello Worl?" );
		test( false, "Hello World!", "Hello*Glob|Hello Worl?|Hello World" );

		// Character escapes
		test( true, "Hello . World", "Hello . World" );
		test( true, "Hello $ World", "Hello $ World" );
		test( true, "Hello ( World", "Hello ( World" );
		test( true, "Hello ) World", "Hello ) World" );
		test( true, "Hello + World", "Hello + World" );
		test( true, "Hello [ World", "Hello [ World" );
		test( true, "Hello ] World", "Hello ] World" );
		test( true, "Hello ? World", "Hello \\? World" );
		test( true, "Hello * World", "Hello \\* World" );
		test( true, "Hello \\ World", "Hello \\\\ World" );
		test( false, "Hello World!", "Hello.World!" );
		test( false, "Hello World!", "Hello World!$" );
		test( false, "Hello World!", "(Hello World!)" );
		test( false, "Hello World!", "Hello+ World!" );
		test( false, "Hello World!", "[H]ello [W]orld!" );
		test( false, "Hello World!", "Hello W\\?rld!" );
		test( false, "Hello World!", "Hello \\*!" );
	} );
} );
