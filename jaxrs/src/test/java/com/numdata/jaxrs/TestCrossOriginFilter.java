/*
 * Copyright (c) 2020, Numdata BV, The Netherlands.
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

package com.numdata.jaxrs;

import java.net.*;
import java.util.ArrayList;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.ws.rs.container.*;
import javax.ws.rs.core.*;

import static java.util.Arrays.*;
import static java.util.Collections.*;
import static org.junit.Assert.*;
import org.junit.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link CrossOriginFilter}.
 *
 * @author Gerrit Meinders
 */
public class TestCrossOriginFilter
{
	/**
	 * Tests {@link CrossOriginFilter#init(FilterConfig)}.
	 */
	@Test
	public void testInitFromFilterConfig()
	{
		final CrossOriginFilter filter = new CrossOriginFilter();
		assertEquals( "Unexpected origins", emptyList(), new ArrayList<>( filter.getOrigins() ) );
		assertFalse( "Unexpected allowCredentials", filter.isAllowCredentials() );
		assertTrue( "Unexpected wildcard", filter.isWildcard() );

		final FilterConfig filterConfig = mock( FilterConfig.class );
		when( filterConfig.getInitParameter( "origins" ) ).thenReturn( "www.example.com www.acme.org" );
		when( filterConfig.getInitParameter( "allowCredentials" ) ).thenReturn( "true" );
		filter.init( filterConfig );
		assertEquals( "Unexpected origins", asList( "www.example.com", "www.acme.org" ), new ArrayList<>( filter.getOrigins() ) );
		assertTrue( "Unexpected allowCredentials", filter.isAllowCredentials() );
		assertFalse( "Unexpected wildcard", filter.isWildcard() );

		when( filterConfig.getInitParameter( "origins" ) ).thenReturn( "www.example.com www.acme.org *" );
		when( filterConfig.getInitParameter( "allowCredentials" ) ).thenReturn( "true" );
		filter.init( filterConfig );
		assertEquals( "Unexpected origins", asList( "www.example.com", "www.acme.org" ), new ArrayList<>( filter.getOrigins() ) );
		assertTrue( "Unexpected allowCredentials", filter.isAllowCredentials() );
		assertTrue( "Unexpected wildcard", filter.isWildcard() );
	}

	/**
	 * Tests the filter with the Servlet API.
	 *
	 * @throws Exception if the test fails.
	 */
	@Test
	public void testFilterServlet()
	throws Exception
	{
		final CrossOriginFilter filter = new CrossOriginFilter();
		final FilterConfig filterConfig = mock( FilterConfig.class );
		when( filterConfig.getInitParameter( "origins" ) ).thenReturn( "www.example.com www.acme.org" );
		when( filterConfig.getInitParameter( "allowCredentials" ) ).thenReturn( "true" );
		filter.init( filterConfig );

		//noinspection TypeMayBeWeakened
		final CrossOriginFilter wildcardFilter = new CrossOriginFilter();

		// Origin not specified
		{
			final HttpServletRequest request = mock( HttpServletRequest.class );
			when( request.getRequestURL() ).thenReturn( new StringBuffer( "https://www.example.com/hello/world" ) );
			final HttpServletResponse response = mock( HttpServletResponse.class );
			final FilterChain chain = mock( FilterChain.class );
			filter.doFilter( request, response, chain );
			verifyNoInteractions( response );
		}

		// Wildcard
		{
			final HttpServletRequest request = mock( HttpServletRequest.class );
			when( request.getRequestURL() ).thenReturn( new StringBuffer( "https://www.example.com/hello/world" ) );
			final HttpServletResponse response = mock( HttpServletResponse.class );
			final FilterChain chain = mock( FilterChain.class );
			wildcardFilter.doFilter( request, response, chain );
			verify( response ).setHeader( "Access-Control-Allow-Origin", "*" );
			verify( response, never() ).setHeader( same( "Access-Control-Allow-Credentials" ), anyString() );
		}

		// Origin not allowed
		{
			final HttpServletRequest request = mock( HttpServletRequest.class );
			when( request.getRequestURL() ).thenReturn( new StringBuffer( "https://www.example.com/hello/world" ) );
			when( request.getHeader( "Origin" ) ).thenReturn( "www.example.org" );
			final HttpServletResponse response = mock( HttpServletResponse.class );
			final FilterChain chain = mock( FilterChain.class );
			filter.doFilter( request, response, chain );
			verifyNoInteractions( response );
		}

		// Origin allowed
		{
			final HttpServletRequest request = mock( HttpServletRequest.class );
			when( request.getRequestURL() ).thenReturn( new StringBuffer( "https://www.example.com/hello/world" ) );
			when( request.getHeader( "Origin" ) ).thenReturn( "www.acme.org" );
			final HttpServletResponse response = mock( HttpServletResponse.class );
			final FilterChain chain = mock( FilterChain.class );
			filter.doFilter( request, response, chain );
			verify( response ).setHeader( "Access-Control-Allow-Origin", "www.acme.org" );
			verify( response ).setHeader( "Access-Control-Allow-Credentials", "true" );
		}
	}

	/**
	 * Tests the filter with the JAX-RS API.
	 */
	@Test
	public void testFilterJersey()
	{
		final CrossOriginFilter filter = new CrossOriginFilter();
		final ServletConfig servletConfig = mock( ServletConfig.class );
		when( servletConfig.getInitParameter( CrossOriginFilter.class.getName() + ".origins" ) ).thenReturn( "www.example.com www.acme.org" );
		when( servletConfig.getInitParameter( CrossOriginFilter.class.getName() + ".allowCredentials" ) ).thenReturn( "true" );
		filter._jerseyServletConfig = servletConfig;

		final CrossOriginFilter wildcardFilter = new CrossOriginFilter();
		wildcardFilter._jerseyServletConfig = mock( ServletConfig.class );

		// Origin not specified
		{
			final UriInfo uriInfo = mock( UriInfo.class );
			when( uriInfo.getRequestUri() ).thenReturn( URI.create( "https://www.example.com/hello/world" ) );
			final ContainerRequestContext requestContext = mock( ContainerRequestContext.class );
			when( requestContext.getUriInfo() ).thenReturn( uriInfo );

			final ContainerResponseContext responseContext = mock( ContainerResponseContext.class );
			final MultivaluedMap<String, Object> responseHeaders = mock( MultivaluedMap.class );
			when( responseContext.getHeaders() ).thenReturn( responseHeaders );

			filter.filter( requestContext, responseContext );
			verifyNoInteractions( responseContext );
		}

		// Wildcard
		{
			final UriInfo uriInfo = mock( UriInfo.class );
			when( uriInfo.getRequestUri() ).thenReturn( URI.create( "https://www.example.com/hello/world" ) );
			final ContainerRequestContext requestContext = mock( ContainerRequestContext.class );
			when( requestContext.getUriInfo() ).thenReturn( uriInfo );

			final ContainerResponseContext responseContext = mock( ContainerResponseContext.class );
			final MultivaluedMap<String, Object> responseHeaders = mock( MultivaluedMap.class );
			when( responseContext.getHeaders() ).thenReturn( responseHeaders );

			wildcardFilter.filter( requestContext, responseContext );
			verify( responseHeaders ).putSingle( "Access-Control-Allow-Origin", "*" );
			verify( responseHeaders, never() ).putSingle( same( "Access-Control-Allow-Credentials" ), anyString() );
		}

		// Origin not allowed
		{
			final UriInfo uriInfo = mock( UriInfo.class );
			when( uriInfo.getRequestUri() ).thenReturn( URI.create( "https://www.example.com/hello/world" ) );
			final ContainerRequestContext requestContext = mock( ContainerRequestContext.class );
			when( requestContext.getUriInfo() ).thenReturn( uriInfo );

			when( requestContext.getHeaderString( "Origin" ) ).thenReturn( "www.example.org" );

			final ContainerResponseContext responseContext = mock( ContainerResponseContext.class );
			final MultivaluedMap<String, Object> responseHeaders = mock( MultivaluedMap.class );
			when( responseContext.getHeaders() ).thenReturn( responseHeaders );

			filter.filter( requestContext, responseContext );
			verifyNoInteractions( responseContext );
		}

		// Origin allowed
		{
			final UriInfo uriInfo = mock( UriInfo.class );
			when( uriInfo.getRequestUri() ).thenReturn( URI.create( "https://www.example.com/hello/world" ) );
			final ContainerRequestContext requestContext = mock( ContainerRequestContext.class );
			when( requestContext.getUriInfo() ).thenReturn( uriInfo );

			when( requestContext.getHeaderString( "Origin" ) ).thenReturn( "www.acme.org" );

			final ContainerResponseContext responseContext = mock( ContainerResponseContext.class );
			final MultivaluedMap<String, Object> responseHeaders = mock( MultivaluedMap.class );
			when( responseContext.getHeaders() ).thenReturn( responseHeaders );

			filter.filter( requestContext, responseContext );
			verify( responseHeaders ).putSingle( "Access-Control-Allow-Origin", "www.acme.org" );
			verify( responseHeaders ).putSingle( "Access-Control-Allow-Credentials", "true" );
		}
	}
}
