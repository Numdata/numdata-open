/*
 * Copyright (c) 2007-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.junit;

import java.util.*;

import org.jetbrains.annotations.*;

/**
 * Provides methods to facilitate testing functionality related to
 * enumerations.
 *
 * @author G. Meinders
 */
public class EnumTester
{
	/**
	 * This utility class MUST NOT be instantiated.
	 */
	private EnumTester()
	{
	}

	/**
	 * Returns a list of the enumeration constants for all enumerations declared by
	 * the specified class.
	 *
	 * @param forClass Class containing the enumerations to list the enumeration
	 *                 constant names of.
	 *
	 * @return List of enumeration constant names for the given class.
	 */
	public static List<String> getEnumConstantList( final Class<?> forClass )
	{
		return getEnumConstantList( forClass, false );
	}

	/**
	 * Returns a list of the enumeration constants for all enumerations declared by
	 * the specified class.
	 *
	 * @param forClass         Class that is a or contains enumerations.
	 * @param includeInherited Include enumerations of ancestor classes.
	 *
	 * @return List of enumeration constant names for the given class.
	 */
	public static List<String> getEnumConstantList( final Class<?> forClass, final boolean includeInherited )
	{
		final List<String> result = new ArrayList<String>();

		for ( Class<?> clazz = forClass; clazz != null; clazz = includeInherited ? clazz.getSuperclass() : null )
		{
			if ( clazz.isEnum() )
			{
				for ( final Object constant : clazz.getEnumConstants() )
				{
					result.add( ( (Enum<?>)constant ).name() );
				}
			}

			for ( final Class<?> innerClass : clazz.getDeclaredClasses() )
			{
				result.addAll( getEnumConstantList( innerClass ) );
			}

		}

		return result;
	}

	/**
	 * Returns list of enumeration constant names.
	 *
	 * @param enumClass Enumeration class to get constants from.
	 *
	 * @return List of enumeration constant names for the given class.
	 */
	@NotNull
	public static List<String> getEnumConstantNames( @NotNull final Class<? extends Enum<?>> enumClass )
	{
		return getEnumConstantNames( null, enumClass );
	}

	/**
	 * Returns list of enumeration constant names.
	 *
	 * @param prefix    Optional prefix to prepend to each enum constant name.
	 * @param enumClass Enumeration class to get constants from.
	 *
	 * @return List of enumeration constant names for the given class.
	 */
	@NotNull
	public static List<String> getEnumConstantNames( @Nullable final String prefix, @NotNull final Class<? extends Enum<?>> enumClass )
	{
		final Enum<?>[] constants = enumClass.getEnumConstants();
		final List<String> result = new ArrayList<String>( constants.length );
		for ( final Object constant : constants )
		{
			result.add( ( prefix != null ) ? prefix + constant : constant.toString() );
		}
		return result;
	}
}
