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
package com.numdata.oss.web;

import com.numdata.oss.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link HTMLTools} class.
 *
 * @author Peter S. Heijnen
 */
public class TestHTMLTools
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestHTMLTools.class.getName();

	/**
	 * Test {@link HTMLTools#getCharForHtmlCode} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGetCharForHtmlCode()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetCharForHtmlCode" );

		assertEquals( "test #1 '&' failed", '&', HTMLTools.getCharForHtmlCode( "&amp", 0, 4 ) );
		assertEquals( "test #2 '&' failed", '&', HTMLTools.getCharForHtmlCode( "&amp;", 0, 4 ) );
		assertEquals( "test #3 '&' failed", '&', HTMLTools.getCharForHtmlCode( "&amp;-", 0, 4 ) );
		assertEquals( "test #4 '&' failed", '&', HTMLTools.getCharForHtmlCode( "-&amp;", 1, 4 ) );
		assertEquals( "test #5 '&' failed", '&', HTMLTools.getCharForHtmlCode( "-&amp;-", 1, 4 ) );
		assertEquals( "test #6 '&' failed", '&', HTMLTools.getCharForHtmlCode( "-&#38;-", 1, 4 ) );
	}

	/**
	 * Test {@link HTMLTools#getURI} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testGetURI()
	throws Exception
	{
		System.out.println( CLASS_NAME + ".testGetURI" );

		assertEquals( "test #1 failed", "", HTMLTools.getURI( null, null ) );
		assertEquals( "test #2 failed", "", HTMLTools.getURI( null, "" ) );
		assertEquals( "test #3 failed", "", HTMLTools.getURI( "", null ) );
		assertEquals( "test #4 failed", "", HTMLTools.getURI( "", "" ) );
		assertEquals( "test #5 failed", "page", HTMLTools.getURI( null, "page" ) );
		assertEquals( "test #6 failed", "page", HTMLTools.getURI( "", "page" ) );
		assertEquals( "test #7 failed", "page", HTMLTools.getURI( "/", "page" ) );
		assertEquals( "test #8 failed", "page", HTMLTools.getURI( "/context", "page" ) );
		assertEquals( "test #9 failed", "page", HTMLTools.getURI( "/context/", "page" ) );
		assertEquals( "test #10 failed", "page?par=value", HTMLTools.getURI( "/context/", "page?par=value" ) );
		assertEquals( "test #11 failed", "?par=value", HTMLTools.getURI( "/context/", "?par=value" ) );
		assertEquals( "test #12 failed", "/servlet", HTMLTools.getURI( null, "/servlet" ) );
		assertEquals( "test #13 failed", "/servlet", HTMLTools.getURI( "", "/servlet" ) );
		assertEquals( "test #14 failed", "/servlet", HTMLTools.getURI( "/", "/servlet" ) );
		assertEquals( "test #15 failed", "/context/servlet", HTMLTools.getURI( "/context", "/servlet" ) );
		assertEquals( "test #16 failed", "/context/servlet", HTMLTools.getURI( "/context/", "/servlet" ) );
		assertEquals( "test #17 failed", "/servlet?par=value", HTMLTools.getURI( "", "/servlet?par=value" ) );
		assertEquals( "test #18 failed", "/context/servlet?par=value", HTMLTools.getURI( "/context", "/servlet?par=value" ) );
		assertEquals( "test #19 failed", "http://server/", HTMLTools.getURI( null, "http://server/" ) );
		assertEquals( "test #20 failed", "http://server/page", HTMLTools.getURI( "/context/", "http://server/page" ) );
		assertEquals( "test #21 failed", "http://server/page?par=value", HTMLTools.getURI( "/context/", "http://server/page?par=value" ) );
		assertEquals( "test #22 failed", "javascript:go()", HTMLTools.getURI( null, "javascript:go()" ) );
		assertEquals( "test #23 failed", "javascript:go()", HTMLTools.getURI( "", "javascript:go()" ) );
		assertEquals( "test #24 failed", "javascript:go()", HTMLTools.getURI( "/context/", "javascript:go()" ) );
	}
}
