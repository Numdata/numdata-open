/*
 * Copyright (c) 2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.net;

import java.net.*;
import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class has some utility methods for building an URL or URI.
 *
 * @author Peter S. Heijnen
 */
public class TestUrlTools
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestUrlTools.class.getName();

	/**
	 * Test {@link UrlTools#toURI} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testToURI()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testToURI" );

		assertEquals( "Test #1", new URI( "http://www.numdata.com/" ), UrlTools.toURI( new URL( "http://www.numdata.com/" ) ) );
	}

	/**
	 * Test {@link UrlTools#toURL} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testToURL()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testToURL" );

		assertEquals( "Test #1", new URL( "http://www.numdata.com/" ), UrlTools.toURL( new URI( "http://www.numdata.com/" ) ) );
	}

	/**
	 * Test {@link UrlTools#buildUrl} methods.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testBuildUrl()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testBuildUrl" );

		assertEquals( "Test #1", new URL( "http://www.numdata.com/nice/page?key1=0&key2=%2B%E2%82%A0%26%CE%B1&key3=null" ), UrlTools.buildUrl( new URL( "http://www.numdata.com/index.php" ), "/nice/page", "key1", 0, "key2", "+\u20A0&\u03B1", "key3", null ) );
		assertEquals( "Test #2", new URL( "http://www.numdata.com/nice/page?key1=0&key2=%2B%E2%82%A0%26%CE%B1&key3=null" ), UrlTools.buildUrl( new URI( "http://www.numdata.com/folder/" ), "/nice/page", "key1", 0, "key2", "+\u20A0&\u03B1", "key3", null ) );
		assertEquals( "Test #3", new URL( "http://www.numdata.com/nice/context/path/?key1=0&key2=%2B%E2%82%A0%26%CE%B1&key3=null" ), UrlTools.buildUrl( new URL( "http://www.numdata.com/nice/page" ), "context/path/", Arrays.<Object>asList( "key1", 0, "key2", "+\u20A0&\u03B1", "key3", null ) ) );
		assertEquals( "Test #4", new URL( "http://www.numdata.com/context/path/?key1=0&key2=%2B%E2%82%A0%26%CE%B1&key3=null" ), UrlTools.buildUrl( new URI( "http://www.numdata.com/" ), "context/path/", Arrays.<Object>asList( "key1", 0, "key2", "+\u20A0&\u03B1", "key3", null ) ) );
	}

	/**
	 * Test {@link UrlTools#buildUri} methods.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testBuildUri()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testBuildUrI" );

		assertEquals( "Test #1", new URI( "http://www.numdata.com/nice/page?key1=0&key2=%2B%E2%82%A0%26%CE%B1&key3=null" ), UrlTools.buildUri( new URI( "http://www.numdata.com/" ), "/nice/page", "key1", 0, "key2", "+\u20A0&\u03B1", "key3", null ) );
		assertEquals( "Test #2", new URI( "http://www.numdata.com/context/path/?key1=0&key2=%2B%E2%82%A0%26%CE%B1&key3=null" ), UrlTools.buildUri( new URI( "http://www.numdata.com/" ), "context/path/", Arrays.<Object>asList( "key1", 0, "key2", "+\u20A0&\u03B1", "key3", null ) ) );
	}

	/**
	 * Test {@link UrlTools#buildSpec} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testBuildSpec()
	throws Exception
	{
		assertEquals( "Test #1", "", UrlTools.buildSpec( "", Collections.emptyList() ) );
		assertEquals( "Test #2", "?a=b", UrlTools.buildSpec( "", Arrays.asList( "a", "b" ) ) );
		assertEquals( "Test #3", "?a=b&c=d", UrlTools.buildSpec( "", Arrays.asList( "a", "b", "c", "d" ) ) );
		assertEquals( "Test #4", "?", UrlTools.buildSpec( "?", Collections.emptyList() ) );
		assertEquals( "Test #5", "?a=b", UrlTools.buildSpec( "?", Arrays.asList( "a", "b" ) ) );
		assertEquals( "Test #6", "?a=b&c=d", UrlTools.buildSpec( "?", Arrays.asList( "a", "b", "c", "d" ) ) );
		assertEquals( "Test #7", "page", UrlTools.buildSpec( "page", Collections.emptyList() ) );
		assertEquals( "Test #8", "page?a=b", UrlTools.buildSpec( "page", Arrays.asList( "a", "b" ) ) );
		assertEquals( "Test #9", "page?a=b&c=d", UrlTools.buildSpec( "page", Arrays.asList( "a", "b", "c", "d" ) ) );
		assertEquals( "Test #10", "page?", UrlTools.buildSpec( "page?", Collections.emptyList() ) );
		assertEquals( "Test #11", "page?a=b", UrlTools.buildSpec( "page?", Arrays.asList( "a", "b" ) ) );
		assertEquals( "Test #12", "page?a=b&c=d", UrlTools.buildSpec( "page?", Arrays.asList( "a", "b", "c", "d" ) ) );
		assertEquals( "Test #13", "page?x=1", UrlTools.buildSpec( "page?x=1", Collections.emptyList() ) );
		assertEquals( "Test #14", "page?x=1&a=b", UrlTools.buildSpec( "page?x=1", Arrays.asList( "a", "b" ) ) );
		assertEquals( "Test #15", "page?x=1&a=b&c=d", UrlTools.buildSpec( "page?x=1", Arrays.asList( "a", "b", "c", "d" ) ) );
		assertEquals( "Test #16", "page?x=1&y=2", UrlTools.buildSpec( "page?x=1&y=2", Collections.emptyList() ) );
		assertEquals( "Test #17", "page?x=1&y=2&a=b", UrlTools.buildSpec( "page?x=1&y=2", Arrays.asList( "a", "b" ) ) );
		assertEquals( "Test #18", "page?x=1&y=2&a=b&c=d", UrlTools.buildSpec( "page?x=1&y=2", Arrays.asList( "a", "b", "c", "d" ) ) );
		assertEquals( "Test #19", "page?x=1&y=2&", UrlTools.buildSpec( "page?x=1&y=2&", Collections.emptyList() ) );
		assertEquals( "Test #20", "page?x=1&y=2&a=b", UrlTools.buildSpec( "page?x=1&y=2&", Arrays.asList( "a", "b" ) ) );
		assertEquals( "Test #21", "page?x=1&y=2&a=b&c=d", UrlTools.buildSpec( "page?x=1&y=2&", Arrays.asList( "a", "b", "c", "d" ) ) );
	}

	/**
	 * Test {@link UrlTools#appendParameter} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testAppendParameter()
	throws Exception
	{
		assertEquals( "Test #1a", "?a=b", UrlTools.appendParameter( "", "a", "b" ) );
		final StringBuilder sb1 = new StringBuilder( "" );
		UrlTools.appendParameter( sb1, "a", "b" );
		assertEquals( "Test #1b", "?a=b", sb1.toString() );

		assertEquals( "Test #2a", "?a=b", UrlTools.appendParameter( "?", "a", "b" ) );
		final StringBuilder sb2 = new StringBuilder( "?" );
		UrlTools.appendParameter( sb2, "a", "b" );
		assertEquals( "Test #2b", "?a=b", sb2.toString() );

		assertEquals( "Test #3a", "page?a=b", UrlTools.appendParameter( "page", "a", "b" ) );
		final StringBuilder sb3 = new StringBuilder( "page" );
		UrlTools.appendParameter( sb3, "a", "b" );
		assertEquals( "Test #3b", "page?a=b", sb3.toString() );

		assertEquals( "Test #4a", "page?a=b", UrlTools.appendParameter( "page?", "a", "b" ) );
		final StringBuilder sb4 = new StringBuilder( "page?" );
		UrlTools.appendParameter( sb4, "a", "b" );
		assertEquals( "Test #4b", "page?a=b", sb4.toString() );

		assertEquals( "Test #5a", "page?x=1&a=b", UrlTools.appendParameter( "page?x=1", "a", "b" ) );
		final StringBuilder sb5 = new StringBuilder( "page?x=1" );
		UrlTools.appendParameter( sb5, "a", "b" );
		assertEquals( "Test #5b", "page?x=1&a=b", sb5.toString() );

		assertEquals( "Test #6a", "page?x=1&y=2&a=b", UrlTools.appendParameter( "page?x=1&y=2", "a", "b" ) );
		final StringBuilder sb6 = new StringBuilder( "page?x=1&y=2" );
		UrlTools.appendParameter( sb6, "a", "b" );
		assertEquals( "Test #6b", "page?x=1&y=2&a=b", sb6.toString() );

		assertEquals( "Test #7a", "page?x=1&y=2&a=b", UrlTools.appendParameter( "page?x=1&y=2&", "a", "b" ) );
		final StringBuilder sb7 = new StringBuilder( "page?x=1&y=2&" );
		UrlTools.appendParameter( sb7, "a", "b" );
		assertEquals( "Test #7b", "page?x=1&y=2&a=b", sb7.toString() );
	}

	/**
	 * Test {@link UrlTools#urlEncode} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testUrlEncode()
	throws Exception
	{
		assertEquals( "Test #1", "a+b%26c%2Bd%2Fe%3Ff%E2%82%A0g%CE%B1h", UrlTools.urlEncode( "a b&c+d/e?f\u20A0g\u03B1h" ) );
	}
}
