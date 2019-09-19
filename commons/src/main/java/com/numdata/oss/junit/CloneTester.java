/*
 * Copyright (c) 2004-2017 Numdata BV.  All rights reserved.
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
package com.numdata.oss.junit;

import java.lang.reflect.*;

import com.numdata.oss.*;
import org.junit.*;

/**
 * JUnit unit tool class to help with testing clone operations.
 *
 * @author Peter S. Heijnen
 */
public final class CloneTester
extends Assert
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private CloneTester()
	{
	}

	/**
	 * Asserts that an object was (deep) cloned properly.
	 *
	 * @param messagePrefix Prefix to failure messages.
	 * @param original      Original object that was cloned.
	 * @param clone         The cloned object.
	 *
	 * @throws Exception if the test fails.
	 */
	public static void assertDeepClone( final String messagePrefix, final Object original, final Object clone )
	throws Exception
	{
		final String actualPrefix = ( ( messagePrefix != null ) ? messagePrefix + " - " : "" );

		if ( original == null )
		{
			assertNull( actualPrefix + "Original is null, but clone is not?", original );
		}
		else if ( clone == null )
		{
			assertNull( actualPrefix + "Original is not null, but clone is null?", original );
		}
		else
		{
			if ( ( original instanceof Enum<?> ) || ( clone instanceof Enum<?> ) )
			{
				assertSame( actualPrefix + "Clone and original must be same enum", original, clone );
			}
			else
			{
				assertNotSame( actualPrefix + "Clone and original are the same object", original, clone );

				final Class<?> originalClass = original.getClass();
				final Class<?> cloneClass = clone.getClass();
				assertEquals( actualPrefix + "Clone and original are of a different class", originalClass, cloneClass );

				for ( final Field field : originalClass.getFields() )
				{
					final int fieldModifiers = field.getModifiers();
					final Class<?> fieldType = field.getType();
					final String fieldName = field.getName();

					if ( Modifier.isPublic( fieldModifiers )
					     && !Modifier.isNative( fieldModifiers )
					     && !Modifier.isStatic( fieldModifiers ) )
					{
						final Class<?> declaringClass = field.getDeclaringClass();
						final String className = declaringClass.getName();
						final String propertyName = className.substring( 0, className.lastIndexOf( (int)'.' ) + 1 ) + fieldName;
						final Object originalValue = field.get( original );
						final Object cloneValue = field.get( clone );

						if ( fieldType.isPrimitive()
						     || ( fieldType == Boolean.class )
						     || ( fieldType == String.class )
						     || Number.class.isAssignableFrom( fieldType ) )
						{
							assertEquals( actualPrefix + "Cloned '" + propertyName + "' value corrupted.", originalValue, cloneValue );
						}
						else if ( ( originalValue != null ) || ( cloneValue != null ) )
						{
							if ( ArrayTools.isValidType( originalValue ) || ArrayTools.isValidType( cloneValue ) )
							{
								assertNotSame( actualPrefix + "Cloned '" + propertyName + "' property is same instance as original (copy by reference, not cloned)", originalValue, cloneValue );
								ArrayTester.assertEquals( actualPrefix + "Clone constructor", "original." + propertyName, "cloned." + propertyName, originalValue, cloneValue );
							}
							else
							{
								assertDeepClone( actualPrefix + "Cloned '" + propertyName + "' value corrupted.", originalValue, cloneValue );
							}
						}
					}
				}
			}
		}
	}
}
