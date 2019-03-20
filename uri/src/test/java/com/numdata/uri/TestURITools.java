/*
 * Copyright (c) 2017-2019, Numdata BV, The Netherlands.
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
package com.numdata.uri;

import java.net.*;
import java.util.*;

import com.numdata.oss.*;
import com.numdata.uri.URITools.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit tests for {@link URITools}.
 *
 * @author G. Meinders
 */
public class TestURITools
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestURITools.class.getName();

	@Test
	public void testURIPath()
	{
		System.out.println( CLASS_NAME + ".testURIPath()" );

		class Test
		{
			private final String _uri;

			private final boolean _absolute;

			private final boolean _directory;

			private final String _fileName;

			private final String _directoryName;

			private final List<String> _segments;

			private final Map<String, String> _parameters;

			Test( final String uri, final boolean absolute, final boolean directory, final String directoryName, final String fileName, final String[] segments, final String[] parameters )
			{
				_uri = uri;
				_absolute = absolute;
				_directory = directory;
				_directoryName = directoryName;
				_fileName = fileName;
				_segments = Arrays.asList( segments );
				_parameters = new LinkedHashMap<String, String>();
				for ( int i = 0; i < parameters.length; i += 2 )
				{
					_parameters.put( parameters[ i ], parameters[ i + 1 ] );
				}
			}
		}

		final Test[] tests =
		{
		// Relative
		new Test( "", false, false, "", "", new String[] {}, new String[ 0 ] ),
		new Test( "a", false, false, "", "a", new String[] { "a" }, new String[ 0 ] ),
		new Test( "a/", false, true, "a/", "", new String[] { "a" }, new String[ 0 ] ),
		new Test( "a/b", false, false, "a/", "b", new String[] { "a", "b" }, new String[ 0 ] ),
		new Test( "a/b/", false, true, "a/b/", "", new String[] { "a", "b" }, new String[ 0 ] ),
		new Test( "a/b;x=y;z=", false, false, "a/", "b", new String[] { "a", "b" }, new String[] { "x", "y", "z", "" } ),
		new Test( "a/b;x%3Dy=z", false, false, "a/", "b", new String[] { "a", "b" }, new String[] { "x=y", "z" } ),
		new Test( "a/b%3Bx=y", false, false, "a/", "b;x=y", new String[] { "a", "b;x=y" }, new String[ 0 ] ),
		new Test( "a%2Fb", false, false, "", "a/b", new String[] { "a/b" }, new String[ 0 ] ),
		// Currently unsupported:
		//  - a;x=y;z=/b
		//  - a;x=y;z=/b;w=v

		// Absolute
		new Test( "/", true, true, "/", "", new String[] {}, new String[ 0 ] ),
		new Test( "/a", true, false, "/", "a", new String[] { "a" }, new String[ 0 ] ),
		new Test( "/a/", true, true, "/a/", "", new String[] { "a" }, new String[ 0 ] ),
		new Test( "/a/b", true, false, "/a/", "b", new String[] { "a", "b" }, new String[ 0 ] ),
		new Test( "/a/b/", true, true, "/a/b/", "", new String[] { "a", "b" }, new String[ 0 ] ),
		new Test( "/a/b;x=y;z=", true, false, "/a/", "b", new String[] { "a", "b" }, new String[] { "x", "y", "z", "" } ),
		// Currently unsupported:
		//  - /a;x=y;z=/b
		//  - /a;x=y;z=/b;w=v"

		// Absolute with scheme
		new Test( "scheme:/a", true, false, "/", "a", new String[] { "a" }, new String[ 0 ] ),
		new Test( "scheme:/a/", true, true, "/a/", "", new String[] { "a" }, new String[ 0 ] ),
		new Test( "scheme:/a/b", true, false, "/a/", "b", new String[] { "a", "b" }, new String[ 0 ] ),
		new Test( "scheme:/a/b/", true, true, "/a/b/", "", new String[] { "a", "b" }, new String[ 0 ] ),

		// Absolute with scheme and empty authority
		new Test( "scheme:///a:", true, false, "/", "a:", new String[] { "a:" }, new String[ 0 ] ),
		new Test( "scheme:///a:/", true, true, "/a:/", "", new String[] { "a:" }, new String[ 0 ] ),
		new Test( "scheme:///a:/b", true, false, "/a:/", "b", new String[] { "a:", "b" }, new String[ 0 ] ),
		new Test( "scheme:///a:/b/", true, true, "/a:/b/", "", new String[] { "a:", "b" }, new String[ 0 ] ),

		// Absolute with scheme and non-empty authority
		new Test( "scheme://example.com", false, false, "", "", new String[] {}, new String[ 0 ] ),
		//                                                ^-- The path is empty, i.e. not absolute, even though the URI is absolute!
		new Test( "scheme://example.com/", true, true, "/", "", new String[] {}, new String[ 0 ] ),
		new Test( "scheme://example.com/a", true, false, "/", "a", new String[] { "a" }, new String[ 0 ] ),
		new Test( "scheme://example.com/a/", true, true, "/a/", "", new String[] { "a", }, new String[ 0 ] ),
		new Test( "scheme://example.com/a/b", true, false, "/a/", "b", new String[] { "a", "b" }, new String[ 0 ] ),
		new Test( "scheme://example.com/a/b/", true, true, "/a/b/", "", new String[] { "a", "b" }, new String[ 0 ] ),
		new Test( "scheme://example.com/a/b;x=y;z=", true, false, "/a/", "b", new String[] { "a", "b" }, new String[] { "x", "y", "z", "" } ),
		// Currently unsupported:
		//  - scheme://example.com/a;x=y;z=/b
		//  - scheme://example.com/a;x=y;z=/b;w=v
		};

		for ( final Test test : tests )
		{
			System.out.println( " - Test \"" + test._uri + '"' );

			final URI uri = URI.create( test._uri );
			final URIPath path = new URIPath( uri );

			assertEquals( "Unexpected absolute flag for URI: " + test._uri, test._absolute, path.isAbsolute() );
			assertEquals( "Unexpected directory flag for URI: " + test._uri, test._directory, path.isDirectory() );

			assertEquals( "Unexpected path segments for URI: " + test._uri, test._segments, path.getSegments() );
			assertEquals( "Unexpected parameters for URI: " + test._uri, test._parameters, path.getParameters() );

			assertEquals( "Unexpected directory name for URI: " + test._uri, test._directoryName, path.getDirectory() );
			assertEquals( "Unexpected file name for URI: " + test._uri, test._fileName, path.getFile() );

			assertEquals( "Path doesn't match. Incorrect parse or toString?", uri.getRawPath(), path.toString() );
		}
	}

	/**
	 * Test {@link URITools#resolve} method.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testResolve()
	{
		System.out.println( CLASS_NAME + ".testResolve" );

		final URI[][] tests =
		{
		{ URI.create( "scheme://example.com/a/b/" ), URI.create( "c" ), URI.create( "scheme://example.com/a/b/c" ) },
		{ URI.create( "scheme://example.com/a/b/" ), URI.create( "/c" ), URI.create( "scheme://example.com/c" ) },
		{ URI.create( "scheme://example.com/a/b/" ), URI.create( "other:/c" ), URI.create( "other:/c" ) },
		{ URI.create( "jar:file:///a/b.jar!/c" ), URI.create( "d" ), URI.create( "jar:file:///a/b.jar!/d" ) },
		{ URI.create( "jar:file:///a/b.jar!/c" ), URI.create( "d/" ), URI.create( "jar:file:///a/b.jar!/d/" ) },
		{ URI.create( "jar:file:///a/b.jar!/c" ), URI.create( "d/e" ), URI.create( "jar:file:///a/b.jar!/d/e" ) },
		{ URI.create( "jar:file:///a/b.jar!/c" ), URI.create( "/d" ), URI.create( "jar:file:///a/b.jar!/d" ) },
		{ URI.create( "file:////server/share/dir/" ), URI.create( "a" ), URI.create( "file:////server/share/dir/a" ) },
		{ URI.create( "file:////server/share/dir/" ), URI.create( "a/b" ), URI.create( "file:////server/share/dir/a/b" ) },
		{ URI.create( "file:////server/share/file" ), URI.create( "a" ), URI.create( "file:////server/share/a" ) },
		};

		for ( int i = 0; i < tests.length; i++ )
		{
			final URI[] test = tests[ i ];
			final URI context = test[ 0 ];
			final URI uri = test[ 1 ];
			final URI expected = test[ 2 ];

			System.out.println( " - Test #" + ( i + 1 ) + ": URITools.resolve( " + TextTools.quote( context ) + ", " + TextTools.quote( context ) + " )" );
			final URI actual = URITools.resolve( context, uri );
			System.out.println( "     > " + TextTools.quote( actual ) );
			assertEquals( "Rendered wrong result", expected, actual );
		}
	}
}
