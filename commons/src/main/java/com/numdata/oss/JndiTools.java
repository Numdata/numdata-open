/*
 * Copyright (c) 2012-2017, Numdata BV, The Netherlands.
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

import javax.naming.*;

/**
 * Provides utilities for accessing JNDI.
 *
 * @author G. Meinders
 */
public class JndiTools
{
	/**
	 * Java component environment context.
	 *
	 * "java:" defines the Java namespace. "comp" (for "component") is the only
	 * binding in the root context. "env" (for "environment") is reserved for
	 * environment-related bindings.
	 */
	public static final String JAVA_COMP_ENV = "java:comp/env/";

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private JndiTools()
	{
	}

	/**
	 * Binds a name relative to {@link #JAVA_COMP_ENV java:/comp/env/} to an
	 * object. Sub-contexts are created as needed.
	 *
	 * @param name   Name relative to 'java:/comp/env/' to be bound.
	 * @param object Object to be bound.
	 *
	 * @return Object that was bound.
	 *
	 * @throws NamingException if a naming exception is encountered.
	 */
	public static <T> T bind( final String name, final T object )
	throws NamingException
	{
		final InitialContext initialContext = new InitialContext();
		try
		{
			bind( initialContext, JAVA_COMP_ENV + name, object );
		}
		finally
		{
			initialContext.close();
		}

		return object;
	}

	/**
	 * Binds a name to an object. Sub-contexts are created as needed.
	 *
	 * @param context Context to bind to.
	 * @param name    Name to be bound.
	 * @param object  Object to be bound.
	 *
	 * @return Object that was bound.
	 *
	 * @throws NamingException if a naming exception is encountered.
	 */
	public static <T> T bind( final Context context, final String name, final T object )
	throws NamingException
	{
		final int slash = name.lastIndexOf( '/' );
		final Context subContext = ( slash < 0 ) ? context : getSubContext( context, name.substring( 0, slash ) );
		subContext.bind( name.substring( slash + 1 ), object );

		return object;
	}

	/**
	 * Binds a name relative to {@link #JAVA_COMP_ENV java:/comp/env/} to an
	 * object, overwriting any existing binding. Sub-contexts are created as
	 * needed.
	 *
	 * @param name   Name relative to 'java:/comp/env/' to be bound.
	 * @param object Object to be bound.
	 *
	 * @return Object that was bound.
	 *
	 * @throws NamingException if a naming exception is encountered.
	 */
	public static <T> T rebind( final String name, final T object )
	throws NamingException
	{
		final InitialContext initialContext = new InitialContext();
		try
		{
			rebind( initialContext, JAVA_COMP_ENV + name, object );
		}
		finally
		{
			initialContext.close();
		}

		return object;
	}

	/**
	 * Binds a name to an object, overwriting any existing binding. Sub-contexts
	 * are created as needed.
	 *
	 * @param context Context to bind to.
	 * @param name    Name to be bound.
	 * @param object  Object to be bound.
	 *
	 * @return Object that was bound.
	 *
	 * @throws NamingException if a naming exception is encountered.
	 */
	public static <T> T rebind( final Context context, final String name, final T object )
	throws NamingException
	{
		final int slash = name.lastIndexOf( '/' );
		final Context subContext = ( slash < 0 ) ? context : getSubContext( context, name.substring( 0, slash ) );
		subContext.rebind( name.substring( slash + 1 ), object );

		return object;
	}

	/**
	 * Binds a name to an object. Sub-contexts are created as needed.
	 *
	 * @param context Context to bind to.
	 * @param path    Name to be bound.
	 *
	 * @return Object that was bound.
	 *
	 * @throws NamingException if a naming exception is encountered.
	 */
	public static Context getSubContext( final Context context, final String path )
	throws NamingException
	{
		Context result = context;

		for ( final String element : TextTools.tokenize( path, '/' ) )
		{
			Object object;
			try
			{
				object = result.lookup( element );
			}
			catch ( final NamingException ignored )
			{
				object = result.createSubcontext( element );
			}

			if ( !( object instanceof Context ) )
			{
				throw new NotContextException( "Lookup of intermediate context '" + element + "' in path '" + path + "' returned no context, but '" + object + "'." );
			}

			result = (Context)object;
		}

		return result;
	}

	/**
	 * Retrieves the named object relative to {@link #JAVA_COMP_ENV
	 * java:/comp/env/}.
	 *
	 * @param name Name of the object relative to 'java:/comp/env/'.
	 *
	 * @return Object bound to the name.
	 *
	 * @throws NamingException if a naming exception is encountered.
	 */
	public static <T> T lookup( final String name )
	throws NamingException
	{
		final String[] pathElements = name.split( "/" );

		Context currentContext = InitialContext.doLookup( JAVA_COMP_ENV );
		for ( int i = 0; i < pathElements.length - 1; i++ )
		{
			currentContext = (Context)currentContext.lookup( pathElements[ i ] );
		}

		return (T)currentContext.lookup( pathElements[ pathElements.length - 1 ] );
		// TODO Find out if this method can be replaced with:
		//		return (T)currentContext.lookup( currentContext.getNameParser( "" ).parse( name ) );
	}

	/**
	 * Unbinds a name relative to {@link #JAVA_COMP_ENV java:/comp/env/}.
	 *
	 * @param name Name relative to 'java:/comp/env/' to be unbound.
	 *
	 * @throws NamingException if a naming exception is encountered.
	 */
	public static void unbind( final String name )
	throws NamingException
	{
		final InitialContext initialContext = new InitialContext();
		try
		{
			initialContext.unbind( JAVA_COMP_ENV + name );
		}
		finally
		{
			initialContext.close();
		}
	}

}
