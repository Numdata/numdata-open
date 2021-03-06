/*
 * Copyright (c) 2010-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.velocity;

import java.lang.reflect.*;
import java.util.*;

import com.numdata.oss.*;
import org.apache.velocity.context.*;
import org.jetbrains.annotations.*;

/**
 * This Velocity {@link Context} implementation uses bean properties as
 * variables. This is useful if a single object described the complete context;
 * in such situations prefixing all variables references with a name for that
 * one bean is a bit useless.
 *
 * @author Peter S. Heijnen
 */
public class VelocityBeanContext
implements Context
{
	/**
	 * Bean to get properties from.
	 */
	private Object _bean = null;

	/**
	 * Bean we're caching data for. If a bean of another class is set, this
	 * will get updated and the cached reflection objects will be cleared.
	 */
	private Class<?> _beanClass = null;

	/**
	 * Cached property getters for the current bean class.
	 */
	private final Map<String, Method> _getters = new HashMap<>();

	/**
	 * Cached property setters for the current bean class.
	 */
	private final Map<String, Method> _setters = new HashMap<>();

	/**
	 * Cached fields for the current bean class.
	 */
	private final Map<String, Field> _fields = new HashMap<>();

	/**
	 * Cached bean property names.
	 */
	private final Set<String> _beanPropertyNames = new HashSet<>();

	/**
	 * Variables that are automatically created by the velocity template.
	 */
	private final Map<String, Object> _variables = new HashMap<>();

	/**
	 * Construct context.
	 */
	public VelocityBeanContext()
	{
	}

	/**
	 * Construct context.
	 *
	 * @param bean Bean to get properties from.
	 */
	public VelocityBeanContext( final Object bean )
	{
		setBean( bean );
	}

	/**
	 * Set bean to get properties from.
	 *
	 * @param bean Bean to get properties from.
	 */
	public void setBean( final Object bean )
	{
		_bean = bean;
	}

	/**
	 * Internal method to inspect the current bean properties.
	 */
	private void inspectBean()
	{
		final Object bean = _bean;

		final Class<?> beanClass = ( bean != null ) ? bean.getClass() : null;
		if ( beanClass != _beanClass )
		{
			final Map<String, Method> getters = _getters;
			final Map<String, Method> setters = _setters;
			final Map<String, Field> fields = _fields;
			final Set<String> beanPropertyNames = _beanPropertyNames;

			getters.clear();
			setters.clear();
			fields.clear();

			if ( beanClass != null )
			{
				_beanClass = beanClass;

				for ( final Method method : beanClass.getMethods() )
				{
					final int modifiers = method.getModifiers();
					final String name = method.getName();
					final Class<?>[] parameterTypes = method.getParameterTypes();

					if ( !Modifier.isStatic( modifiers ) && ( name.length() > 2 ) )
					{
						if ( parameterTypes.length == 0 )
						{
							if ( ( name.length() > 3 ) && name.startsWith( "get" ) )
							{
								getters.put( TextTools.decapitalize( name.substring( 3 ) ), method );
							}
							else if ( name.startsWith( "is" ) )
							{
								getters.put( TextTools.decapitalize( name.substring( 2 ) ), method );
							}
						}
						else if ( parameterTypes.length == 1 )
						{
							if ( ( name.length() > 3 ) && name.startsWith( "set" ) )
							{
								setters.put( TextTools.decapitalize( name.substring( 3 ) ), method );
							}
						}
					}
				}

				final Set<String> setterNames = setters.keySet();
				setterNames.retainAll( getters.keySet() );

				beanPropertyNames.clear();
				beanPropertyNames.addAll( getters.keySet() );

				for ( final Field field : beanClass.getFields() )
				{
					final int modifiers = field.getModifiers();
					if ( !Modifier.isStatic( modifiers ) )
					{
						final String name = field.getName();
						if ( beanPropertyNames.add( name ) )
						{
							fields.put( name, field );
						}
					}
				}
			}
		}
	}

	@Nullable
	@Override
	public Object get( final String key )
	{
		final Object result;

		if ( key.startsWith( ".literal." ) )
		{
			result = null;
		}
		else
		{
			inspectBean();

			if ( _beanPropertyNames.contains( key ) )
			{
				final Object bean = _bean;

				try
				{
					final Method getter = _getters.get( key );
					if ( getter != null )
					{
						result = getter.invoke( bean );
					}
					else
					{
						final Field field = _fields.get( key );
						if ( field != null )
						{
							result = field.get( bean );
						}
						else
						{
							throw new AssertionError( "No getter or field for known property '" + key + "' in " + bean );
						}
					}
				}
				catch ( final IllegalAccessException e )
				{
					throw new IllegalArgumentException( '\'' + key + "' not accessible", e );
				}
				catch ( final InvocationTargetException e )
				{
					throw new IllegalArgumentException( "Getting '" + key + "' caused an internal error", e );
				}
			}
			else
			{
				result = _variables.get( key );
			}
		}

		return result;
	}

	@Override
	public boolean containsKey( final String key )
	{
		boolean result = _variables.containsKey( key );

		if ( !result && ( key != null ) )
		{
			inspectBean();
			result = _beanPropertyNames.contains( key );
		}

		return result;
	}

	@Override
	public Object put( final String key, final Object value )
	{
		final Object result;

		inspectBean();
		if ( _beanPropertyNames.contains( key ) )
		{
			final Object bean = _bean;

			try
			{
				final Method getter = _getters.get( key );
				if ( getter != null )
				{
					final Method setter = _setters.get( key );
					if ( setter == null )
					{
						throw new IllegalArgumentException( "Trying to set read-only property '" + key + "' in " + bean + " to " + value );
					}

					result = getter.invoke( bean );
					setter.invoke( bean, value );
				}
				else
				{
					final Field field = _fields.get( key );
					if ( field != null )
					{
						result = field.get( bean );
						field.set( bean, value );
					}
					else
					{
						throw new AssertionError( "No getter or field for known property '" + key + "' in " + bean );
					}
				}
			}
			catch ( final IllegalAccessException e )
			{
				throw new IllegalArgumentException( '\'' + key + "' not accessible", e );
			}
			catch ( final InvocationTargetException e )
			{
				throw new IllegalArgumentException( "Getting '" + key + "' caused an internal error", e );
			}
		}
		else
		{
			result = _variables.put( key, value );
		}

		return result;
	}

	@Override
	public String[] getKeys()
	{
		inspectBean();

		final HashSet<String> result = new HashSet<>();
		result.addAll( _beanPropertyNames );
		result.addAll( _variables.keySet() );
		return result.toArray( new String[ 0 ] );
	}

	@Nullable
	@Override
	public Object remove( final String key )
	{
		final Object result;

		if ( _variables.containsKey( key ) )
		{
			result = _variables.remove( key );
		}
		else if ( _beanPropertyNames.contains( key ) )
		{
			throw new IllegalArgumentException( "Can't remove bean property '" + key + '\'' );
		}
		else
		{
			result = null;
		}

		return result;
	}
}
