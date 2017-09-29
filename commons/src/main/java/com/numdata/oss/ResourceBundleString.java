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
package com.numdata.oss;

import java.io.*;
import java.util.*;

import org.jetbrains.annotations.*;

/**
 * This class provides a localized string value from a resource bundle.
 *
 * @author Peter S. Heijnen
 */
public class ResourceBundleString
extends AbstractLocalizableString
implements Serializable
{
	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = -3177144440720572753L;

	/**
	 * Class whose resource bundle is used.
	 */
	private Class<?> _resourceClass;

	/**
	 * Name of resource in bundle.
	 */
	private String _resourceName;

	/**
	 * Construct localizable resource bundle string.
	 */
	public ResourceBundleString()
	{
		_resourceClass = null;
		_resourceName = null;
	}

	/**
	 * Construct localizable resource bundle string.
	 *
	 * @param resourceClass Class whose resource bundle is used.
	 * @param resourceName  Name of resource in bundle.
	 */
	public ResourceBundleString( @NotNull final Class<?> resourceClass, @NotNull final String resourceName )
	{
		_resourceClass = resourceClass;
		_resourceName = resourceName;
	}

	/**
	 * Writes the object to the given object output stream.
	 *
	 * @param out Output stream to write to.
	 *
	 * @throws IOException if an I/O error occurs.
	 * @see Serializable
	 */
	private void writeObject( final ObjectOutputStream out )
	throws IOException
	{
		out.writeUTF( _resourceClass.getName() );
		out.writeUTF( _resourceName );
	}

	/**
	 * Reads the object from the given object input stream.
	 *
	 * @param in Input stream to read from.
	 *
	 * @throws IOException if an I/O error occurs.
	 * @throws ClassNotFoundException if the class of a serialized object could
	 * not be found.
	 * @see Serializable
	 */
	private void readObject( final ObjectInputStream in )
	throws IOException, ClassNotFoundException
	{
		final String className = in.readUTF();
		final String resourceName = in.readUTF();

		_resourceName = resourceName;

		try
		{
			_resourceClass = Class.forName( className );
		}
		catch ( final ClassNotFoundException e )
		{
			throw new ClassNotFoundException( "Resource bundle class " + className + " not found. (Needed to localize '" + resourceName + "'.)", e );
		}
	}

	/**
	 * Parse string that was generated by {@link #toString}.
	 *
	 * @param string String to parse.
	 *
	 * @return Resource bundle string.
	 *
	 * @throws IllegalArgumentException if the string was malformed.
	 */
	@NotNull
	public static ResourceBundleString parse( @NotNull final String string )
	{
		final Properties properties = PropertyTools.fromString( string );
		if ( properties == null )
		{
			throw new IllegalArgumentException( string );
		}

		final String resourceClass = properties.getProperty( "resourceClass" );
		final String resourceName = properties.getProperty( "resourceName" );

		if ( ( resourceClass == null ) || ( resourceName == null ) )
		{
			throw new IllegalArgumentException( string );
		}

		try
		{
			return new ResourceBundleString( Class.forName( resourceClass ), resourceName );
		}
		catch ( final ClassNotFoundException e )
		{
			throw new IllegalArgumentException( '\'' + resourceClass + "' class not found", e );
		}
	}

	@Override
	public String get( @Nullable final Locale locale )
	{
		return ResourceBundleTools.getString( locale, _resourceClass, _resourceName );
	}

	/**
	 * Returns the class whose resource bundle is used.
	 *
	 * @return Class whose resource bundle is used.
	 */
	public Class<?> getResourceClass()
	{
		return _resourceClass;
	}


	/**
	 * Set class whose resource bundle is used.
	 *
	 * @param resourceClass Class whose resource bundle is used.
	 */
	public void setResourceClass( final Class<?> resourceClass )
	{
		_resourceClass = resourceClass;
	}

	/**
	 * Get name of resource in bundle.
	 *
	 * @return Name of resource in bundle.
	 */
	public String getResourceName()
	{
		return _resourceName;
	}

	/**
	 * Set name of resource in bundle.
	 *
	 * @param resourceName Name of resource in bundle.
	 */
	public void setResourceName( final String resourceName )
	{
		_resourceName = resourceName;
	}

	@Override
	public String toString()
	{
		return PropertyTools.toString( PropertyTools.create( "resourceClass", _resourceClass.getName(), "resourceName", _resourceName ) );
	}
}
