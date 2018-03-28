/*
 * Copyright (c) 2017-2018, Numdata BV, The Netherlands.
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
package com.numdata.oss.db;

import java.lang.reflect.*;

import org.jetbrains.annotations.*;

/**
 * {@link ClassHandler} implementation for {@link SampleRecord}.
 *
 * @author  Peter S. Heijnen
 */
public class SampleRecordDbHandler
	extends ReflectedClassHandler
{
	/**
	 * MySQL create statement for table.
	 */
	public static final String MYSQL_CREATE_STATEMENT = "CREATE TABLE `SampleTable` (\n" +
		"  `ID` int(11) NOT NULL auto_increment,\n" +
		"  `string` varchar(60) default NULL,\n" +
		"  `timestamp` datetime NOT NULL,\n" +
		"  `date` datetime default NULL,\n" +
		"  `localizedString` mediumtext NOT NULL,\n" +
		"  `stringList` mediumtext NOT NULL,\n" +
		"  `nestedProperties` mediumtext NOT NULL,\n" +
		"  `nullableNestedProperties` mediumtext default NULL,\n" +
		"  PRIMARY KEY  (`ID`)\n" +
		");";

	/**
	 * Construct {@link ClassHandler} for {@link SampleRecord}.
	 */
	public SampleRecordDbHandler()
	{
		super( SampleRecord.class );
	}

	@NotNull
	@Override
	public String getCreateStatement()
	{
		return MYSQL_CREATE_STATEMENT;
	}

	@Nullable
	@Override
	protected FieldHandler createFieldHandler( @NotNull final Field field )
	{
		final FieldHandler result;

		final String name = field.getName();

		if ( SampleRecord.STRING_LIST.equals( name ) )
		{
			result = new StringCollectionFieldHandler( _clazz, name );
		}
		else
		{
			result = super.createFieldHandler( field );
		}

		return result;
	}
}
