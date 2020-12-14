/*
 * Copyright (c) 2014-2020, Numdata BV, The Netherlands.
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

import java.sql.*;
import java.util.*;
import java.util.regex.*;
import javax.sql.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Utility class to modify tables in an existing database to match the Java
 * definition.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "JDBCExecuteWithNonConstantString", "UseOfSystemOutOrSystemErr" } )
public class DatabaseTableUpdater
{
	/**
	 * Handling for situations where data loss may/will occur.
	 */
	public enum DataLossHandling
	{
		/**
		 * Refuse to perform update.
		 */
		REFUSE,

		/**
		 * Skip the update. Leave the subject data as is.
		 */
		SKIP,

		/**
		 * Perform the update regardless of possible data loss.
		 */
		FORCE
	}

	/**
	 * Character set to use for all tables.
	 */
	private static final String CHARACTER_SET = "utf8";

	/**
	 * Update database structure of the specified tables.
	 *
	 * @param verbose           Be verbose about updates.
	 * @param realUpdates       Perform real updates vs. just print queries.
	 * @param createIfNecessary Create table if it does not exist.
	 * @param dataLossHandling  How to handle updates that (may) cause data
	 *                          loss.
	 * @param dataSource        Database source to connect to database.
	 * @param dbName            Name of database ({@code null} if unknown).
	 * @param tableClasses      Tables to be updated.
	 *
	 * @throws Exception if the update fails.
	 */
	public static void updateTables( final boolean verbose, final boolean realUpdates, final boolean createIfNecessary, @NotNull final DataLossHandling dataLossHandling, @NotNull final DataSource dataSource, @Nullable final String dbName, @NotNull final Iterable<Class<?>> tableClasses )
	throws Exception
	{
		for ( final Class<?> tableClass : tableClasses )
		{
			try
			{
				updateTable( verbose, realUpdates, createIfNecessary, dataLossHandling, dataSource, dbName, tableClass );
			}
			catch ( final SQLException e )
			{
				throw new SQLException( "Failed to update table " + tableClass, e );
			}
		}
	}

	/**
	 * Update table structure.
	 *
	 * @param verbose           Be verbose about updates.
	 * @param realUpdates       Perform real updates vs. just print queries.
	 * @param createIfNecessary Create table if it does not exist.
	 * @param dataLossHandling  How to handle updates that (may) cause data
	 *                          loss.
	 * @param dataSource        Database source to connect to database.
	 * @param dbName            Name of database ({@code null} if unknown).
	 * @param tableClass        Database table record class.
	 *
	 * @throws SQLException if there was a problem accessing the database.
	 * @throws IllegalArgumentException if something bad was given as argument.
	 */
	public static void updateTable( final boolean verbose, final boolean realUpdates, final boolean createIfNecessary, @NotNull final DataLossHandling dataLossHandling, @NotNull final DataSource dataSource, @Nullable final String dbName, @NotNull final Class<?> tableClass )
	throws SQLException
	{
		final DbServices db = new DbServices( dataSource );

		final String tableName = db.getTableName( tableClass );
		final ClassHandler classHandler = getClassHandler( tableClass );
		final String javaCreateStatement = classHandler.getCreateStatement();

		final String tableReference = '`' + ( ( dbName != null ) ? dbName + "`.`" : "" ) + tableName + '`';

		final Connection connection = dataSource.getConnection();
		try
		{
			final Statement statement = connection.createStatement();
			try
			{
				boolean tableExists;
				try
				{
					final ResultSet resultSet = statement.executeQuery( "SELECT 1 FROM " + tableName + " WHERE 0=1" );
					resultSet.close();
					tableExists = true;
				}
				catch ( final Exception ignored )
				{
					tableExists = false;
				}

				if ( tableExists )
				{
					if ( verbose )
					{
						System.out.println( "Updating structure of " + tableReference );
					}

					String dbCreateStatement = getCreateTable( tableName, statement );
					if ( !dbCreateStatement.contains( "DEFAULT CHARSET=" + CHARACTER_SET ) )
					{
						statement.executeUpdate( "alter table " + tableName + " character set " + CHARACTER_SET );
						dbCreateStatement = getCreateTable( tableName, statement );
					}

					final List<String> javaCreateLines = getCreateLines( javaCreateStatement );
					final List<String> dbCreateLines = getCreateLines( dbCreateStatement );

					// convert some MySQL/MariaDb defaults back to older syntax
					for ( int i = 0; i < dbCreateLines.size(); i++ )
					{
						dbCreateLines.set( i, dbCreateLines.get( i )
						                                   .replaceFirst( "DEFAULT (-?[\\d.]+)$", "DEFAULT '$1'" )
						                                   .replaceFirst( "(TEXT|BLOB) DEFAULT NULL", "$1" )
						                                   .replaceFirst( "CURRENT_TIMESTAMP\\(\\)", "CURRENT_TIMESTAMP" ) );
					}

					if ( !javaCreateLines.equals( dbCreateLines ) )
					{
						for ( final String dbCreateLine : dbCreateLines )
						{
							if ( !javaCreateLines.contains( dbCreateLine ) )
							{
								final String id = getFirstID( dbCreateLine );
								if ( id == null )
								{
									throw new RuntimeException( "No column name in create line in DB: " + dbCreateLine + " from create statement: " + dbCreateStatement );
								}

								if ( isKeyDefinition( dbCreateLine ) )
								{
									final String javaCreateLine = getKeyDefinition( javaCreateLines, id );
									if ( javaCreateLine == null )
									{
										dropKey( realUpdates, dataSource, tableReference, id );
									}
								}
							}
						}

						for ( final String dbCreateLine : dbCreateLines )
						{
							if ( !javaCreateLines.contains( dbCreateLine ) )
							{
								final String id = getFirstID( dbCreateLine );
								if ( id == null )
								{
									throw new RuntimeException( "No column name in create line in DB: " + dbCreateLine + " from create statement: " + dbCreateStatement );
								}

								if ( isColumnDefinition( dbCreateLine ) )
								{
									final String javaCreateLine = getColumnDefinition( javaCreateLines, id );
									if ( javaCreateLine != null )
									{
										modifyColumn( realUpdates, dataLossHandling, dataSource, tableReference, id, dbCreateLine, javaCreateLine );
									}
									else
									{
										if ( realUpdates && ( dataLossHandling == DataLossHandling.REFUSE ) )
										{
											System.out.print( "     ALTER TABLE " + tableReference + " DROP `" + id + "`;" );
											throw new RuntimeException( "Refusing to drop " + tableReference + ".`" + id + "` column" );
										}

										dropColumn( realUpdates && ( dataLossHandling == DataLossHandling.FORCE ), dataSource, tableReference, id );
									}
								}
								else if ( isKeyDefinition( dbCreateLine ) )
								{
									final String javaCreateLine = getKeyDefinition( javaCreateLines, id );
									if ( javaCreateLine != null )
									{
										modifyKey( realUpdates, dataSource, tableReference, id, javaCreateLine );
									}
								}
								else if ( !isPrimaryKeyDefinition( dbCreateLine ) )
								{
									throw new RuntimeException( "Unrecognized create line in DB: " + dbCreateLine + " from create statement: " + dbCreateStatement );
								}
							}
						}

						String lastColumn = null;

						for ( final String javaCreateLine : javaCreateLines )
						{
							final String id = getFirstID( javaCreateLine );
							if ( id == null )
							{
								throw new RuntimeException( "No column name in create line in Java: " + javaCreateLine + " from create statement: " + javaCreateStatement );
							}

							if ( !dbCreateLines.contains( javaCreateLine ) )
							{
								if ( isColumnDefinition( javaCreateLine ) )
								{
									final String dbCreateLine = getColumnDefinition( dbCreateLines, id );
									if ( dbCreateLine == null )
									{
										addColumn( realUpdates, dataSource, tableReference, javaCreateLine, lastColumn );
									}
								}
								else if ( isKeyDefinition( javaCreateLine ) || isPrimaryKeyDefinition( javaCreateLine ) )
								{
									final String dbCreateLine = getKeyDefinition( dbCreateLines, id );
									if ( dbCreateLine == null )
									{
										addKey( realUpdates, dataSource, tableReference, javaCreateLine );
									}
								}
								else
								{
									throw new RuntimeException( "Unrecognized create line in DB: " + javaCreateLine + " from create statement: " + javaCreateStatement );
								}
							}

							if ( isColumnDefinition( javaCreateLine ) )
							{
								lastColumn = id;
							}
						}
					}
				}
				else // table does not exist
				{
					if ( createIfNecessary )
					{
						if ( verbose )
						{
							System.out.println( "Creating " + tableReference );
						}

						statement.executeUpdate( javaCreateStatement.replaceAll( "\\);$", ") character set " + CHARACTER_SET + ';' ) );
					}
				}
			}
			finally
			{
				statement.close();
			}
		}
		finally
		{
			connection.close();
		}
	}

	/**
	 * Returns the 'CREATE TABLE' statement for the given table.
	 *
	 * @param tableName Name of the table.
	 * @param statement Statement to use.
	 *
	 * @return 'CREATE TABLE' statement.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	@NotNull
	private static String getCreateTable( @NotNull final String tableName, @NotNull final Statement statement )
	throws SQLException
	{
		String result;

		final String versionString = getVersion( statement );
		final boolean mariadb = versionString.endsWith( "-MariaDB" ); // e.g. 10.3.9-MariaDB
		boolean atLeastMariaDb10_3 = false;
		if ( mariadb )
		{
			final Matcher matcher = Pattern.compile( "(\\d+)\\.(\\d+)(\\D.*)?" ).matcher( versionString );
			if ( matcher.matches() )
			{
				final int major = Integer.parseInt( matcher.group( 1 ) );
				final int minor = Integer.parseInt( matcher.group( 2 ) );

				atLeastMariaDb10_3 = ( major > 10 ) || ( ( major == 10 ) && ( minor >= 3 ) );
			}
		}

		final String query = "show create table " + tableName;

		final ResultSet tableDescriptionResultSet = statement.executeQuery( query );
		try
		{
			if ( !tableDescriptionResultSet.next() )
			{
				throw new RuntimeException( "No result from: " + query );
			}

			result = tableDescriptionResultSet.getString( "Create Table" );

			if ( atLeastMariaDb10_3 )
			{
				result = result.replaceAll( "CHARACTER SET \\w+", "" )
				               .replaceAll( "(text|blob) DEFAULT NULL", "$1" )
				               .replaceAll( "current_timestamp\\(\\)", "CURRENT_TIMESTAMP" )
				               .replaceAll( "DEFAULT (-?\\d+(?:\\.\\d+)?)", "DEFAULT '$1'" );
			}
//			System.out.println(result );
		}
		finally
		{
			tableDescriptionResultSet.close();
		}

		return result;
	}

	/**
	 * Returns the version string.
	 *
	 * @param statement Statement to use.
	 *
	 * @return 'VERSION()' value.
	 *
	 * @throws SQLException if an error occurs while accessing the database.
	 */
	@NotNull
	private static String getVersion( @NotNull final Statement statement )
	throws SQLException
	{
		final String result;
		final String query = "SELECT VERSION()";

		final ResultSet tableDescriptionResultSet = statement.executeQuery( query );
		try
		{
			if ( !tableDescriptionResultSet.next() )
			{
				throw new RuntimeException( "No result from: " + query );
			}
			result = tableDescriptionResultSet.getString( 1 );
		}
		finally
		{
			tableDescriptionResultSet.close();
		}

		return result;
	}

	/**
	 * Get {@link ClassHandler} for a table class.
	 *
	 * @param tableClass Table class to get handler for.
	 *
	 * @return {@link ClassHandler}.
	 */
	@NotNull
	private static ClassHandler getClassHandler( final Class<?> tableClass )
	{
		return DbServices.getClassHandler( tableClass );
	}

	/**
	 * Get CREATE lines from CREATE statement.
	 *
	 * @param createStatement CREATE statement.
	 *
	 * @return CREATE lines.
	 */
	private static List<String> getCreateLines( final String createStatement )
	{
		final List<String> result = TextTools.tokenize( createStatement.replaceAll( "[\\t ]+", " " ), '\n', true );

		int i = 0;
		while ( i < result.size() )
		{
			final String line = result.get( i );

			if ( line.startsWith( "CREATE " ) ||
			     TextTools.startsWith( line, ')' ) )
			{
				result.remove( i );
			}
			else
			{
				final StringBuilder sb = new StringBuilder( line );
				int length = sb.length();

				/* remove '\s*,' suffix */
				if ( TextTools.endsWith( sb, ',' ) )
				{
					length--;
					while ( ( length > 0 ) && Character.isWhitespace( sb.charAt( length - 1 ) ) )
					{
						length--;
					}
					sb.setLength( length );
				}

				/* convert all unquoted parts of the string to upper case */
				char quote = '\0';
				for ( int pos = 0; pos < length; pos++ )
				{
					final char ch = sb.charAt( pos );
					if ( quote != '\0' )
					{
						if ( ch == quote )
						{
							quote = '\0';
						}
					}
					else if ( ch == '\'' || ch == '`' || ch == '"' )
					{
						quote = ch;
					}
					else
					{
						sb.setCharAt( pos, Character.toUpperCase( ch ) );
					}
				}

				result.set( i++, sb.toString() );
			}
		}

		return result;
	}

	/**
	 * Test if the specified CREATE line is a column definition.
	 *
	 * @param createLine CREATE line.
	 *
	 * @return {@code true} if the CREATE line is a column definition; {@code
	 * false} otherwise.
	 */
	private static boolean isColumnDefinition( final CharSequence createLine )
	{
		return TextTools.startsWith( createLine, '`' );
	}

	/**
	 * Get definition for the specified column.
	 *
	 * @param createLines CREATE lines.
	 * @param columnName  Name of column.
	 *
	 * @return CREATE line with column definition; {@code null} if not found.
	 */
	@Nullable
	private static String getColumnDefinition( final Iterable<String> createLines, final String columnName )
	{
		String result = null;

		for ( final String createLine : createLines )
		{
			if ( isColumnDefinition( createLine ) && columnName.equalsIgnoreCase( getFirstID( createLine ) ) )
			{
				result = createLine;
				break;
			}
		}

		return result;
	}

	/**
	 * Test if the specified CREATE line is a key definition.
	 *
	 * @param createLine CREATE line.
	 *
	 * @return {@code true} if the CREATE line is a key definition; {@code
	 * false} otherwise.
	 */
	private static boolean isKeyDefinition( final String createLine )
	{
		return createLine.startsWith( "KEY " ) || createLine.startsWith( "UNIQUE KEY " ) || createLine.startsWith( "FULLTEXT KEY " );
	}

	/**
	 * Test if the specified CREATE line is a primary key definition.
	 *
	 * @param createLine CREATE line.
	 *
	 * @return {@code true} if the CREATE line is a primary key definition;
	 * {@code false} otherwise.
	 */
	private static boolean isPrimaryKeyDefinition( final String createLine )
	{
		return createLine.startsWith( "PRIMARY KEY " );
	}

	/**
	 * Get definition for the specified key.
	 *
	 * @param createLines CREATE lines.
	 * @param keyName     Name of key.
	 *
	 * @return CREATE line with key definition; {@code null} if not found.
	 */
	@Nullable
	private static String getKeyDefinition( final Iterable<String> createLines, final String keyName )
	{
		String result = null;

		for ( final String createLine : createLines )
		{
			if ( isKeyDefinition( createLine ) && keyName.equalsIgnoreCase( getFirstID( createLine ) ) )
			{
				result = createLine;
				break;
			}
		}

		return result;
	}

	/**
	 * Get first back-quoted identifier in CREATE line.
	 *
	 * @param createLine CREATE line for key.
	 *
	 * @return First encountered back-quoted identifier.
	 */
	@Nullable
	private static String getFirstID( final String createLine )
	{
		final int start = createLine.indexOf( '`' ) + 1;
		final int end = ( start > 0 ) ? createLine.indexOf( '`', start ) : -1;
		return ( end > start ) ? createLine.substring( start, end ) : null;
	}

	/**
	 * Add column to table.
	 *
	 * @param realUpdates    Perform real updates vs. just print queries.
	 * @param dataSource     Database source to connect to database.
	 * @param tableReference SQL reference to table.
	 * @param createLine     CREATE line for column.
	 * @param after          Name of column after which to add the column.
	 *
	 * @throws SQLException if an error occurred while accessing the database.
	 */
	private static void addColumn( final boolean realUpdates, @NotNull final DataSource dataSource, @NotNull final String tableReference, @NotNull final String createLine, @Nullable final String after )
	throws SQLException
	{
		executeUpdate( realUpdates, dataSource, "ALTER TABLE " + tableReference + " ADD " + createLine + ( ( after != null ) ? " AFTER `" + after + '`' : "" ) + ';' );
	}

	/**
	 * Modify column of table.
	 *
	 * @param realUpdates      Perform real updates vs. just print queries.
	 * @param dataLossHandling How to handle updates that (may) cause data
	 *                         loss.
	 * @param dataSource       Database source to connect to database.
	 * @param tableReference   SQL reference to table.
	 * @param columnName       Name of column.
	 * @param oldCreateLine    Old CREATE line for column (from database).
	 * @param newCreateLine    New CREATE line for key (from specification).
	 *
	 * @throws SQLException if an error occurred while accessing the database.
	 */
	private static void modifyColumn( final boolean realUpdates, @NotNull final DataLossHandling dataLossHandling, final DataSource dataSource, final String tableReference, final String columnName, final String oldCreateLine, final String newCreateLine )
	throws SQLException
	{
		final List<String> oldTokens = TextTools.tokenize( oldCreateLine, ' ', true );
		final String oldType = oldTokens.get( 1 );
		final String oldBaseType = oldType.replaceAll( "\\(.*", "" );
		final List<String> newTokens = TextTools.tokenize( newCreateLine, ' ', true );
		final String newType = newTokens.get( 1 );
		final String newBaseType = newType.replaceAll( "\\(.*", "" );

		final String updatePrefix = "ALTER TABLE " + tableReference + " MODIFY ";
		System.out.println( "     (" + TextTools.getFixed( "old definition:", updatePrefix.length() - 2, true, '.' ) + ' ' + oldCreateLine + ')' );

		if ( oldType.equalsIgnoreCase( newType ) )
		{
			executeUpdate( realUpdates, dataSource, updatePrefix + newCreateLine + ';' );
		}
		else if ( "enum('true','false')".equalsIgnoreCase( oldType ) && "tinyint(1)".equalsIgnoreCase( newType ) )
		{
			executeUpdate( realUpdates, dataSource, updatePrefix + newCreateLine + ';' );
			executeUpdate( realUpdates, dataSource, "UPDATE " + tableReference + " SET `" + columnName + "`=(`" + columnName + "`=1);" );
		}
		else if ( "enum('false','true')".equalsIgnoreCase( oldType ) && "tinyint(1)".equalsIgnoreCase( newType ) )
		{
			executeUpdate( realUpdates, dataSource, updatePrefix + newCreateLine + ';' );
			executeUpdate( realUpdates, dataSource, "UPDATE " + tableReference + " SET `" + columnName + "`=(`" + columnName + "`=2);" );
		}
		else if ( isEnumBaseType( oldBaseType ) && isEnumBaseType( newBaseType ) )
		{
			final Set<String> oldValues = getEnumValues( oldType );
			final Set<String> newValues = getEnumValues( newType );
			final boolean possibleDataLoss = !newValues.containsAll( oldValues );
			final boolean updateAllowed = handleDataLoss( dataLossHandling, realUpdates, possibleDataLoss, tableReference, oldCreateLine, newCreateLine,
			                                              "Possible data loss due to modified enumeration type" );

			executeUpdate( updateAllowed, dataSource, updatePrefix + newCreateLine + ';' );
		}
		else if ( isCharacterBaseType( oldBaseType ) && isBinaryBaseType( newBaseType ) )
		{
			final boolean possibleDataLoss = getMaximumLength( oldType ) > getMaximumLength( newType );
			final boolean updateAllowed = handleDataLoss( dataLossHandling, realUpdates, possibleDataLoss, tableReference, oldCreateLine, newCreateLine,
			                                              "Possible data loss due to decreased capacity" );

			if ( oldCreateLine.contains( "CHARACTER SET" ) )
			{
				// Convert to default character set first.
				executeUpdate( updateAllowed, dataSource, updatePrefix + '`' + columnName + "` " + oldBaseType + ';' );
			}
			executeUpdate( updateAllowed, dataSource, updatePrefix + newCreateLine + ';' );
		}
		else if ( ( isBinaryBaseType( oldBaseType ) &&
		            isBinaryBaseType( newBaseType ) ) ||
		          ( ( isCharacterBaseType( oldBaseType ) || isEnumBaseType( oldBaseType ) ) &&
		            ( isCharacterBaseType( newBaseType ) || isEnumBaseType( newBaseType ) ) ) )
		{
			final boolean possibleDataLoss = getMaximumLength( oldType ) > getMaximumLength( newType );
			final boolean updateAllowed = handleDataLoss( dataLossHandling, realUpdates, possibleDataLoss, tableReference, oldCreateLine, newCreateLine,
			                                              "Possible data loss due to decreased capacity" );

			executeUpdate( updateAllowed, dataSource, updatePrefix + newCreateLine + ';' );
		}
		else if ( ( ( "date".equalsIgnoreCase( oldBaseType ) || "datetime".equalsIgnoreCase( oldBaseType ) || "time".equalsIgnoreCase( oldBaseType ) ) &&
		            ( "date".equalsIgnoreCase( newBaseType ) || "datetime".equalsIgnoreCase( newBaseType ) || "time".equalsIgnoreCase( newBaseType ) ) ) ||
		          ( ( "bigint".equalsIgnoreCase( oldBaseType ) || "decimal".equalsIgnoreCase( oldBaseType ) || "double".equalsIgnoreCase( oldBaseType ) || "float".equalsIgnoreCase( oldBaseType ) || "int".equalsIgnoreCase( oldBaseType ) || "smallint".equalsIgnoreCase( oldBaseType ) || "tinyint".equalsIgnoreCase( oldBaseType ) ) &&
		            ( "bigint".equalsIgnoreCase( newBaseType ) || "decimal".equalsIgnoreCase( newBaseType ) || "double".equalsIgnoreCase( newBaseType ) || "float".equalsIgnoreCase( newBaseType ) || "int".equalsIgnoreCase( newBaseType ) || "smallint".equalsIgnoreCase( newBaseType ) || "tinyint".equalsIgnoreCase( newBaseType ) ) ) )
		{
			executeUpdate( realUpdates, dataSource, updatePrefix + newCreateLine + ';' );
		}
		else
		{
			final String message = "Don't know how to convert '" + oldType + "' to '" + newType + "'";
			if ( dataLossHandling != DataLossHandling.SKIP )
			{
				throw new RuntimeException( message + "\nOld create line: " + oldCreateLine + "\nNew create line: " + newCreateLine + "\nTable reference: " + tableReference );
			}
			System.err.println( "WARNING: " + message );
		}
	}

	/**
	 * Handles possible data loss according to the specified method.
	 *
	 * @param dataLossHandling Method for handling data loss handling.
	 * @param realUpdates      Whether real updates are requested.
	 * @param possibleDataLoss Whether data loss may occur.
	 * @param tableReference   SQL reference to table.
	 * @param oldCreateLine    Old CREATE line for column (from database).
	 * @param newCreateLine    New CREATE line for key (from specification).
	 * @param message          Warning/error message.
	 *
	 * @return Whether real updates are allowed.
	 */
	private static boolean handleDataLoss( @NotNull final DataLossHandling dataLossHandling, final boolean realUpdates, final boolean possibleDataLoss, final String tableReference, final String oldCreateLine, final String newCreateLine, final String message )
	{
		if ( possibleDataLoss )
		{
			if ( realUpdates && ( dataLossHandling == DataLossHandling.REFUSE ) )
			{
				throw new RuntimeException( message + "\nOld create line: " + oldCreateLine + "\nNew create line: " + newCreateLine + "\nTable reference: " + tableReference );
			}

			System.out.println( "WARNING: " + message );
		}

		return realUpdates && ( !possibleDataLoss || ( dataLossHandling == DataLossHandling.FORCE ) );
	}

	/**
	 * Returns whether the given base type is a binary type.
	 *
	 * @param baseType Column base type.
	 *
	 * @return {@code true} for binary types.
	 */
	private static boolean isBinaryBaseType( @NotNull final String baseType )
	{
		return "binary".equalsIgnoreCase( baseType ) || "varbinary".equalsIgnoreCase( baseType ) || "tinyblob".equalsIgnoreCase( baseType ) || "blob".equalsIgnoreCase( baseType ) || "mediumblob".equalsIgnoreCase( baseType ) || "longblob".equalsIgnoreCase( baseType );
	}

	/**
	 * Returns whether the given base type is a character type.
	 *
	 * @param baseType Column base type.
	 *
	 * @return {@code true} for character types.
	 */
	private static boolean isCharacterBaseType( @NotNull final String baseType )
	{
		return "char".equalsIgnoreCase( baseType ) || "varchar".equalsIgnoreCase( baseType ) || "tinytext".equalsIgnoreCase( baseType ) || "text".equalsIgnoreCase( baseType ) || "mediumtext".equalsIgnoreCase( baseType ) || "longtext".equalsIgnoreCase( baseType );
	}

	/**
	 * Returns whether the given base type is an enum type.
	 *
	 * @param baseType Column base type.
	 *
	 * @return {@code true} for enum types.
	 */
	private static boolean isEnumBaseType( @NotNull final String baseType )
	{
		return "enum".equalsIgnoreCase( baseType );
	}

	/**
	 * Returns the maximum length for values of the given type.
	 *
	 * @param type Column type.
	 *
	 * @return Maximum length.
	 */
	private static long getMaximumLength( @NotNull final String type )
	{
		final long result;
		final String[] baseAndLength = type.split( "[()]" );
		if ( baseAndLength.length == 1 )
		{
			result = getDefaultMaximumLength( baseAndLength[ 0 ] );
		}
		else if ( baseAndLength.length == 2 )
		{
			if ( isEnumBaseType( baseAndLength[ 0 ] ) )
			{
				int maxLength = 0;
				for ( final String enumValue : getEnumValues( type ) )
				{
					maxLength = Math.max( maxLength, enumValue.length() );
				}
				result = maxLength;
			}
			else
			{
				result = Integer.parseInt( baseAndLength[ 1 ] );
			}
		}
		else
		{
			throw new IllegalArgumentException( "Unsupported type: " + type );
		}
		return result;
	}

	/**
	 * Returns the default maximum length for values of the given base type.
	 *
	 * @param baseType Column base type.
	 *
	 * @return Default maximum length.
	 */
	private static long getDefaultMaximumLength( @NotNull final String baseType )
	{
		final long result;
		if ( "binary".equalsIgnoreCase( baseType ) ||
		     "char".equalsIgnoreCase( baseType ) )
		{
			result = 1;
		}
		else if ( "tinyblob".equalsIgnoreCase( baseType ) ||
		          "tinytext".equalsIgnoreCase( baseType ) )
		{
			result = 0xff; //2^8 - 1
		}
		else if ( "blob".equalsIgnoreCase( baseType ) ||
		          "text".equalsIgnoreCase( baseType ) )
		{
			result = 0xffff; //2^16 - 1
		}
		else if ( "mediumblob".equalsIgnoreCase( baseType ) ||
		          "mediumtext".equalsIgnoreCase( baseType ) )
		{
			result = 0xffffff; //2^24 - 1
		}
		else if ( "longblob".equalsIgnoreCase( baseType ) ||
		          "longtext".equalsIgnoreCase( baseType ) )
		{
			result = 0xffffffffL; //2^32 - 1
		}
		else
		{
			throw new IllegalArgumentException( "Unsupported base type: " + baseType );
		}
		return result;
	}

	/**
	 * Returns the enumeration values from an SQL enum type definition.
	 *
	 * @param type Enumeration type definition.
	 *
	 * @return Enumeration values.
	 */
	@NotNull
	private static Set<String> getEnumValues( @NotNull final String type )
	{
		final String valueList = type.substring( type.indexOf( '(' ) + 1, type.indexOf( ')' ) );
		final String[] values = valueList.split( "," );
		// TODO: Remove quotes, maybe.
		return new HashSet<String>( Arrays.asList( values ) );
	}

	/**
	 * Drop column of table.
	 *
	 * @param realUpdates    Perform real updates vs. just print queries.
	 * @param dataSource     Database source to connect to database.
	 * @param tableReference SQL reference to table.
	 * @param columnName     Name of column.
	 *
	 * @throws SQLException if an error occurred while accessing the database.
	 */
	private static void dropColumn( final boolean realUpdates, final DataSource dataSource, final String tableReference, final String columnName )
	throws SQLException
	{
		executeUpdate( realUpdates, dataSource, "ALTER TABLE " + tableReference + " DROP `" + columnName + "`;" );
	}

	/**
	 * Add key to table.
	 *
	 * @param realUpdates    Perform real updates vs. just print queries.
	 * @param dataSource     Database source to connect to database.
	 * @param tableReference SQL reference to table.
	 * @param newCreateLine  CREATE line for key.
	 *
	 * @throws SQLException if an error occurred while accessing the database.
	 */
	private static void addKey( final boolean realUpdates, final DataSource dataSource, final String tableReference, final String newCreateLine )
	throws SQLException
	{
		executeUpdate( realUpdates, dataSource, "ALTER TABLE " + tableReference + " ADD " + newCreateLine + ';' );
	}

	/**
	 * Modify key of table.
	 *
	 * @param realUpdates    Perform real updates vs. just print queries.
	 * @param dataSource     Database source to connect to database.
	 * @param tableReference SQL reference to table.
	 * @param keyName        Name of key.
	 * @param newCreateLine  CREATE line for key.
	 *
	 * @throws SQLException if an error occurred while accessing the database.
	 */
	private static void modifyKey( final boolean realUpdates, final DataSource dataSource, final String tableReference, final String keyName, final String newCreateLine )
	throws SQLException
	{
		dropKey( realUpdates, dataSource, tableReference, keyName );
		addKey( realUpdates, dataSource, tableReference, newCreateLine );
	}

	/**
	 * Drop key of table.
	 *
	 * @param realUpdates    Perform real updates vs. just print queries.
	 * @param dataSource     Database source to connect to database.
	 * @param tableReference SQL reference to table.
	 * @param keyName        Name of key.
	 *
	 * @throws SQLException if an error occurred while accessing the database.
	 */
	private static void dropKey( final boolean realUpdates, final DataSource dataSource, final String tableReference, final String keyName )
	throws SQLException
	{
		executeUpdate( realUpdates, dataSource, "ALTER TABLE " + tableReference + " DROP KEY `" + keyName + "`;" );
	}

	/**
	 * Execute update query.
	 *
	 * @param realUpdates Perform real updates vs. just print queries.
	 * @param dataSource  Database source to connect to database.
	 * @param query       Update query to execute.
	 *
	 * @throws SQLException if an error occurred while accessing the database.
	 */
	private static void executeUpdate( final boolean realUpdates, @NotNull final DataSource dataSource, @NotNull final String query )
	throws SQLException
	{
		if ( realUpdates )
		{
			System.out.print( "     " );
			System.out.println( query );

			final Connection connection = dataSource.getConnection();
			try
			{
				final Statement statement = connection.createStatement();
				try
				{
					statement.executeUpdate( query );
				}
				finally
				{
					statement.close();
				}
			}
			finally
			{
				connection.close();
			}
		}
		else
		{
			System.out.print( "###  " );
			System.out.println( query );
		}
	}

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private DatabaseTableUpdater()
	{
	}
}
