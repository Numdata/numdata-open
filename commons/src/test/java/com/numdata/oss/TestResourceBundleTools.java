/*
 * Copyright (c) 2003-2017, Numdata BV, The Netherlands.
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

import java.io.*;
import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link ResourceBundleTools} class.
 *
 * @author Peter S. Heijnen
 */
public class TestResourceBundleTools
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestResourceBundleTools.class.getName();

	/**
	 * Get sample resource bundle to perform tests with.
	 *
	 * @return ResourceBundle.
	 *
	 * @throws Exception if resource bundle could not be provided.
	 */
	@SuppressWarnings( "SpellCheckingInspection" )
	public static ResourceBundle getTestBundle()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".getTestBundle()" );

		return new PropertyResourceBundle( new ByteArrayInputStream(
		( "ID        = Beslag\n"
		  + "IDs       = Beslag\n"
		  + "code      = Code\n"
		  + "type      = Type\n"
		  + "typeList  = ACCESSORY \\\n\t   | DAMPER| DOWEL\\\n |DRAWER \\\n"
		  + "\t   | EXCENTER|GRIP| HINGE \\\n\t   | LEG \\\n\t   | SCREW\n"
		  + "status    = Status\n"
		  + '\n'
		  + "# types\n"
		  + "ACCESSORY = Accessoire\n"
		  + "DAMPER    = Demper\n"
		  + "DOWEL     = Deuvel\n"
		  + "DRAWER    = Lade\n"
		  + "EXCENTER  = Excenter\n"
		  + "GRIP      = Greep\n"
		  + "HINGE     = Scharnier\n"
		  + "LEG       = Poot\n"
		  + "SCREW     = Schroef\n"
		).getBytes() ) );
	}

	/**
	 * Test the getResourceChoices() method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGetResourceChoices()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetResourceChoices()" );

		final ResourceBundle testBundle = getTestBundle();

		final String[] list = ResourceBundleTools.getStringList( testBundle, "typeList" );
		assertNotNull( "getResourceList( 'typeList' ) failed", list );

		final String[] choices = ResourceBundleTools.getChoices( testBundle, "type" );
		assertNotNull( "getResourceChoices( 'type' ) failed", choices );

		assertEquals( "getResourceList/Choices result length does not match", list.length * 2, choices.length );

		for ( int i = 0, j = 0; i < list.length; i++, j += 2 )
		{
			assertEquals( "list[" + i + "] vs. choices[" + j + ']', list[ i ], choices[ j ] );
			assertEquals( "getString( '" + list[ i ] + "' ) vs. choices[" + ( j + 1 ) + ']', testBundle.getString( list[ i ] ), choices[ j + 1 ] );
		}
	}
}
