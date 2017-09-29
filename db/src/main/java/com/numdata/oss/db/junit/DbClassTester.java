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
package com.numdata.oss.db.junit;

import java.io.*;
import java.lang.reflect.*;
import java.math.*;
import java.sql.*;
import java.util.*;
import java.util.Date;
import javax.sql.*;

import com.numdata.oss.*;
import com.numdata.oss.db.*;
import com.numdata.oss.io.*;
import org.jetbrains.annotations.*;
import org.junit.*;

/**
 * (J)Unit test tool class to help with testing database classes.
 *
 * @author Peter S. Heijnen
 */
public class DbClassTester
extends Assert
{
	/**
	 * Database services to use.
	 */
	@NotNull
	protected final DbServices _db;

	/**
	 * Database class to test.
	 */
	@NotNull
	private final Class<?> _dbClass;

	/**
	 * Test integrity of the specified database class. This requires a working
	 * database connection!
	 *
	 * @param db      Database services to use.
	 * @param dbClass Database class to test.
	 *
	 * @throws Exception if the test failed.
	 */
	public static void testDbClass( @NotNull final DbServices db, @NotNull final Class<?> dbClass )
	throws Exception
	{
		final DbClassTester tester = new DbClassTester( db, dbClass );
		tester.test();
	}

	/**
	 * Construct class tester.
	 *
	 * @param db      Database services to use.
	 * @param dbClass Database class to test.
	 */
	public DbClassTester( @NotNull final DbServices db, @NotNull final Class<?> dbClass )
	{
		_db = db;
		_dbClass = dbClass;
	}

	/**
	 * Test integrity of the database class.
	 *
	 * @throws Exception if the test fails.
	 */
	public void test()
	throws Exception
	{
		System.out.println( "Testing database class: " + _dbClass.getName() );
		testCreateTable();
		testDefaultConstructor();
		testSerializability();
		testConsistency();
	}

	/**
	 * Try to create table in memory.
	 *
	 * @throws Exception if the test fails.
	 */
	protected void testCreateTable()
	throws Exception
	{
		System.out.println( " - Create table (in-memory)" );

		final HsqlDbServices memoryDb = new HsqlDbServices();
		try
		{
			memoryDb.createTable( _dbClass );
		}
		finally
		{
			memoryDb.shutdown();
		}
	}

	/**
	 * Test default constructor.
	 *
	 * @throws Exception if the test fails.
	 */
	protected void testDefaultConstructor()
	throws Exception
	{
		System.out.println( " - Test default constructor" );

		try
		{
			final Constructor<?> constructor = _dbClass.getConstructor();
			constructor.newInstance();
		}
		catch ( final IllegalAccessException ignored )
		{
			throw new AssertionError( "Can't create '" + _dbClass.getName() + "' instance! (class or default constructor not public?)" );
		}
		catch ( final InstantiationException ignored )
		{
			throw new AssertionError( "Can't create '" + _dbClass.getName() + "' instance! (no default constructor defined?)" );
		}
	}

	/**
	 * Test serializability.
	 *
	 * @throws Exception if the test fails.
	 */
	protected void testSerializability()
	throws Exception
	{
		System.out.println( " - Test serializability" );

		assertTrue( "Class '" + _dbClass.getName() + "' does not implement 'Serializable' interface!", Serializable.class.isAssignableFrom( _dbClass ) );

		final ObjectOutputStream oos = new ObjectOutputStream( new NullOutputStream() );
		try
		{
			oos.writeObject( Serializable.class.cast( _dbClass.getConstructor().newInstance() ) );
		}
		finally
		{
			oos.close();
		}
	}

	/**
	 * Test consistency between the Java class and the database table.
	 *
	 * @throws Exception if the test fails.
	 */
	private void testConsistency()
	throws Exception
	{
		System.out.println( " - Test consistency between Java class and database table" );

		final Map<String, String> dbTypes = getColumnsFromDatabase();
		final Map<String, Class<?>> javaTypes = getJavaFields();
		testMissingProperties( javaTypes, dbTypes );
		testPropertyTypes( javaTypes, dbTypes );
	}

	/**
	 * Test whether properties are missing from the database or the Java class.
	 *
	 * @param javaTypes Java field types.
	 * @param dbTypes   Database column types.
	 */
	private void testMissingProperties( final Map<String, Class<?>> javaTypes, final Map<String, String> dbTypes )
	{
		final String dbTable = _db.getTableName( _dbClass );

		/*
		 * Find fields in table, but not in class.
		 */
		for ( String name : dbTypes.keySet() )
		{
			for ( final String javaField : javaTypes.keySet() )
			{
				if ( name.equals( javaField ) )
				{
					name = null;
					break;
				}
			}

			assertNull( "Field '" + name + "' is defined in '" + dbTable + "' table, but not in '" + _dbClass.getName() + "' class", name );
		}

		/*
		 * Find fields in class, but not in table.
		 */
		for ( String name : javaTypes.keySet() )
		{
			for ( final String dbField : dbTypes.keySet() )
			{
				if ( name.equals( dbField ) )
				{
					name = null;
					break;
				}
			}

			assertNull( "Field '" + name + "' is defined in '" + _dbClass.getName() + "' class, but not in '" + dbTable + "' table", name );
		}
	}

	/**
	 * Test whether column types match between Java and the database.
	 *
	 * @param javaTypes Java field types.
	 * @param dbTypes   Database column types.
	 *
	 * @throws Exception if the test fails.
	 */
	private void testPropertyTypes( final Map<String, Class<?>> javaTypes, final Map<String, String> dbTypes )
	throws Exception
	{
		ClassHandler classHandler = null;

		final TableRecord tableRecord = _dbClass.getAnnotation( TableRecord.class );
		if ( tableRecord != null )
		{
			final String handlerImpl = tableRecord.handlerImpl();
			if ( !handlerImpl.isEmpty() )
			{
				final Class<?> handlerClass = Class.forName( handlerImpl );
				final Constructor<?> constructor = handlerClass.getConstructor();
				classHandler = (ClassHandler)constructor.newInstance();
			}
		}

		/*
		 * Check types.
		 */
		for ( final Map.Entry<String, Class<?>> entry : javaTypes.entrySet() )
		{
			final String name = entry.getKey();

			Class<?> javaType = entry.getValue();

			if ( classHandler != null )
			{
				final FieldHandler fieldHandlerForColumn = classHandler.getFieldHandlerForColumn( name );
				if ( fieldHandlerForColumn != null )
				{
					javaType = fieldHandlerForColumn.getSqlType();
				}
			}

			final String dbType = dbTypes.get( name );
			if ( dbType == null )
			{
				throw new AssertionError( "Unit test is wrong, should have db and Java type here" );
			}

			testPropertyType( name, javaType, dbType );
		}
	}

	/**
	 * Test whether a type in Java matches a type in the database.
	 *
	 * @param name     Property name.
	 * @param javaType Java type.
	 * @param dbType   Database type.
	 */
	protected void testPropertyType( final String name, final Class<?> javaType, final String dbType )
	{
		final boolean ok;

		if ( String.class.isAssignableFrom( javaType ) ||
		     Properties.class.isAssignableFrom( javaType ) ||
		     LocalizedString.class.isAssignableFrom( javaType ) )
		{
			final String dbBaseType = getDbBaseType( dbType );

			ok = "char".equals( dbBaseType ) ||
			     "varchar".equals( dbBaseType ) ||
			     "mediumtext".equals( dbBaseType ) ||
			     "text".equals( dbBaseType ) ||
			     "tinytext".equals( dbBaseType );
		}
		else if ( BigDecimal.class.isAssignableFrom( javaType ) )
		{
			final String dbBaseType = getDbBaseType( dbType );

			ok = ( "decimal".equalsIgnoreCase( dbBaseType ) );
		}
		else if ( boolean.class.isAssignableFrom( javaType ) || Boolean.class.isAssignableFrom( javaType ) )
		{
			ok = ( "bit".equalsIgnoreCase( dbType ) ||
			       "tinyint(1)".equalsIgnoreCase( dbType ) );
		}
		else if ( byte[].class.isAssignableFrom( javaType ) )
		{
			ok = ( "blob".equalsIgnoreCase( dbType ) ||
			       "mediumblob".equalsIgnoreCase( dbType ) );
		}
		else if ( Date.class.isAssignableFrom( javaType ) )
		{
			ok = ( "datetime".equalsIgnoreCase( dbType ) ||
			       "date".equalsIgnoreCase( dbType ) );
		}
		else if ( double.class.isAssignableFrom( javaType ) || Double.class.isAssignableFrom( javaType ) )
		{
			ok = ( "double".equalsIgnoreCase( dbType ) );
		}
		else if ( Enum.class.isAssignableFrom( javaType ) )
		{
			ok = testEnumType( name, javaType, dbType );
		}
		else if ( float.class.isAssignableFrom( javaType ) || Float.class.isAssignableFrom( javaType ) )
		{
			ok = ( "float".equalsIgnoreCase( dbType ) );
		}
		else if ( int.class.isAssignableFrom( javaType ) || Integer.class.isAssignableFrom( javaType ) )
		{
			final String dbBaseType = getDbBaseType( dbType );

			ok = ( "int".equalsIgnoreCase( dbBaseType ) ||
			       "tinyint".equalsIgnoreCase( dbBaseType ) );
		}
		else if ( long.class.isAssignableFrom( javaType ) || Long.class.isAssignableFrom( javaType ) )
		{
			final String dbBaseType = getDbBaseType( dbType );

			ok = ( "long".equalsIgnoreCase( dbBaseType ) );
		}
		else
		{
			throw new AssertionError( "Unit test is not able to test Java type '" + javaType.getSimpleName() + "' for field '" + name + "' with database type '" + dbType + '\'' );
		}

		assertTrue( "Field '" + _dbClass.getName() + '.' + name + "' is a '" + javaType.getSimpleName() + "', but a '" + dbType + "' in the database", ok );
	}

	/**
	 * Test whether an enum type in Java matches an enum type in the database.
	 *
	 * @param name     Property name.
	 * @param javaType Enumeration type in Java.
	 * @param dbType   Column type in the database.
	 *
	 * @return {@code true} if the types match.
	 */
	protected boolean testEnumType( final String name, final Class<?> javaType, final String dbType )
	{
		final boolean result;

		final Collection<String> javaEnumValues = getJavaEnumValues( javaType );
		final Collection<String> dbEnumValues = getDbEnumValues( dbType );
		if ( ( javaEnumValues != null ) && ( dbEnumValues != null ) )
		{
			result = dbEnumValues.equals( javaEnumValues );
			assertTrue( "Enumeration values for field '" + name + "' with type '" + javaType.getSimpleName() + "' are not consistent:\nJava    : " + javaEnumValues + "\nDatabase: " + dbEnumValues, result );
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Get enumeration values from Java type.
	 *
	 * @param javaType Java enumeration type.
	 *
	 * @return Enumeration values; {@code null} if Java type is not an
	 * enumeration.
	 */
	@Nullable
	protected Collection<String> getJavaEnumValues( @NotNull final Class<?> javaType )
	{
		final Collection<String> result;

		final Object[] enumConstants = javaType.getEnumConstants();
		if ( enumConstants != null )
		{
			result = new TreeSet<String>();
			for ( final Object constant : enumConstants )
			{
				result.add( String.valueOf( constant ) );
			}
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * Get enumeration values from database column type.
	 *
	 * @param dbType Database column type.
	 *
	 * @return Enumeration values; {@code null} if column type is not an
	 * enumeration.
	 */
	@Nullable
	protected Collection<String> getDbEnumValues( @NotNull final String dbType )
	{
		final String dbBaseType = getDbBaseType( dbType );
		final int bracket1 = dbType.indexOf( '(' );
		final int bracket2 = dbType.lastIndexOf( ')' );

		final Collection<String> result;
		if ( "enum".equals( dbBaseType ) && ( bracket1 > 0 ) && ( bracket2 > bracket1 ) )
		{
			result = new TreeSet<String>();

			final String substring = dbType.substring( bracket1 + 1, bracket2 );
			for ( final String s : substring.split( "," ) )
			{
				if ( ( TextTools.startsWith( s, '\'' ) && TextTools.endsWith( s, '\'' ) ) ||
				     ( TextTools.startsWith( s, '"' ) && TextTools.endsWith( s, '"' ) ) )
				{
					result.add( s.substring( 1, s.length() - 1 ) );
				}
				else
				{
					result.add( s );
				}
			}
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * Get columns defined in the database.
	 *
	 * @return Column types mapped by name in schema ordering.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	protected Map<String, String> getColumnsFromDatabase()
	throws SQLException
	{
		final DataSource dataSource = _db.getDataSource();
		final String tableName = _db.getTableName( _dbClass );

		final Map<String, String> result = new LinkedHashMap<String, String>();

		final ResultSet resultSet = JdbcTools.executeQuery( dataSource, "describe " + tableName );
		try
		{
			while ( resultSet.next() )
			{
				final String field = resultSet.getString( 1 );
				final String type = resultSet.getString( 2 );

				result.put( field, type );
			}
		}
		finally
		{
			resultSet.close();
		}


		return result;
	}

	/**
	 * Get 'base' type of column type in the database. The base type consists of
	 * all leading letters from the column type in lower-case (i.g.
	 * 'VARCHAR(30)' would return 'varchar').
	 *
	 * @param dbType Database column type.
	 *
	 * @return Base type of column in lower case.
	 */
	protected String getDbBaseType( final CharSequence dbType )
	{
		final int end = dbType.length();

		int len = 0;
		while ( ( len < end ) && Character.isLetter( dbType.charAt( len ) ) )
		{
			len++;
		}

		final char[] chars = new char[ len ];
		for ( int i = 0; i < len; i++ )
		{

			chars[ i ] = Character.toLowerCase( dbType.charAt( i ) );
		}


		return new String( chars );
	}

	/**
	 * Get public non-static fields from Java class.
	 *
	 * @return Field types mapped by name in declaration ordering.
	 */
	protected Map<String, Class<?>> getJavaFields()
	{
		final Map<String, Class<?>> result = new LinkedHashMap<String, Class<?>>();

		for ( final Field field : _dbClass.getFields() )
		{
			if ( !Modifier.isStatic( field.getModifiers() ) )
			{
				final String name = field.getName();
				final Class<?> type = field.getType();

				result.put( name, type );
			}
		}

		return result;
	}

}
