/*
 * Copyright (c) 2020-2020, Numdata BV, The Netherlands.
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

import java.io.*;
import java.math.*;
import java.sql.*;
import java.util.*;

import static com.numdata.oss.junit.JUnitTools.*;
import org.jetbrains.annotations.*;
import static org.junit.Assert.*;
import org.junit.*;
import static org.mockito.Mockito.*;

/**
 * {@link ResultSetClone} unit test.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( { "ConstantConditions", "resource", "deprecation" } )
public class TestResultSetClone
{
	@Test
	public void testConstantConditions()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		final ResultSetClone rsc = new ResultSetClone( resultSet );
		assertEquals( "Unexpected 'fetchSize'", 0, rsc.getFetchSize() );
		assertException( "setFetchSize()", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.setFetchSize( 1 ) );
		assertException( "getStatement()", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), rsc::getStatement );
		assertException( "getCursorName()", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), rsc::getCursorName );
		assertEquals( "getType()", ResultSet.TYPE_SCROLL_INSENSITIVE, rsc.getType() );
		assertEquals( "getConcurrency()", ResultSet.CONCUR_READ_ONLY, rsc.getConcurrency() );
		assertNull( "getWarnings()", rsc.getWarnings() );
		rsc.clearWarnings(); // no-op
		assertEquals( "getHoldability()", ResultSet.HOLD_CURSORS_OVER_COMMIT, rsc.getHoldability() );
		assertFalse( "isWrapperFor( ResultSet.class )", rsc.isWrapperFor( ResultSet.class ) );
		assertException( "unwrap( ResultSet.class )", new SQLException( "not a wrapper" ), () -> rsc.unwrap( ResultSet.class ) );
	}

	@Test
	public void testClose()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		final ResultSetClone rsc = new ResultSetClone( resultSet );
		assertException( "getStatement()", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), rsc::getStatement );
		assertFalse( "Unexpected 'isClosed()' before 'close()'", rsc.isClosed() );
		rsc.close();
		assertTrue( "Unexpected 'isClosed()' after 'close()'", rsc.isClosed() );
		assertException( "close() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::close );
		assertException( "isBeforeFirst() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::isBeforeFirst );
		assertException( "isAfterLast() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::isAfterLast );
		assertException( "isFirst() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::isFirst );
		assertException( "isLast() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::isLast );
		assertException( "beforeFirst() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::beforeFirst );
		assertException( "afterLast() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::afterLast );
		assertException( "first() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::first );
		assertException( "last() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::last );
		assertException( "getRow() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::getRow );
		assertException( "absolute( 0 ) after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), () -> rsc.absolute( 0 ) );
		assertException( "relative( 0 ) after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), () -> rsc.relative( 0 ) );
		assertException( "wasNull() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), rsc::wasNull );
	}

	@Test
	public void testInsert()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		final ResultSetClone rsc = new ResultSetClone( resultSet );

		assertException( "insertRow()", new SQLException( ResultSetClone.NOT_WRITABLE ), rsc::insertRow );
		assertFalse( "rowInserted()", rsc.rowInserted() );
		assertException( "moveToInsertRow()", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), rsc::moveToInsertRow );
		assertException( "moveToCurrentRow()", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), rsc::moveToCurrentRow );
	}

	@Test
	public void testUpdate()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 1 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		final ResultSetClone rsc = new ResultSetClone( resultSet );

		assertException( "updateRow()", new SQLException( ResultSetClone.NOT_WRITABLE ), rsc::updateRow );
		assertFalse( "rowUpdated()", rsc.rowUpdated() );
		assertException( "cancelRowUpdates()", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), rsc::cancelRowUpdates );

		assertException( "updateArray()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateArray( 1, mock( Array.class ) ) );
		assertException( "updateArray()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateArray( "first", mock( Array.class ) ) );
		assertException( "updateAsciiStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateAsciiStream( 1, new ByteArrayInputStream( new byte[] { 1, 2, 3 } ) ) );
		assertException( "updateAsciiStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateAsciiStream( 1, new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 1 ) );
		assertException( "updateAsciiStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateAsciiStream( 1, new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 2L ) );
		assertException( "updateAsciiStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateAsciiStream( "first", new ByteArrayInputStream( new byte[] { 1, 2, 3 } ) ) );
		assertException( "updateAsciiStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateAsciiStream( "first", new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 1 ) );
		assertException( "updateAsciiStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateAsciiStream( "first", new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 2L ) );
		assertException( "updateBigDecimal()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBigDecimal( 1, new BigDecimal( "1234.56" ) ) );
		assertException( "updateBigDecimal()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBigDecimal( "first", new BigDecimal( "1234.56" ) ) );
		assertException( "updateBinaryStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBinaryStream( 1, new ByteArrayInputStream( new byte[] { 1, 2, 3 } ) ) );
		assertException( "updateBinaryStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBinaryStream( 1, new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 1 ) );
		assertException( "updateBinaryStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBinaryStream( 1, new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 2L ) );
		assertException( "updateBinaryStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBinaryStream( "first", new ByteArrayInputStream( new byte[] { 1, 2, 3 } ) ) );
		assertException( "updateBinaryStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBinaryStream( "first", new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 1 ) );
		assertException( "updateBinaryStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBinaryStream( "first", new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 2L ) );
		assertException( "updateBlob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBlob( 1, mock( Blob.class ) ) );
		assertException( "updateBlob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBlob( 1, new ByteArrayInputStream( new byte[] { 1, 2, 3 } ) ) );
		assertException( "updateBlob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBlob( 1, new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 2L ) );
		assertException( "updateBlob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBlob( "first", mock( Blob.class ) ) );
		assertException( "updateBlob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBlob( "first", new ByteArrayInputStream( new byte[] { 1, 2, 3 } ) ) );
		assertException( "updateBlob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBlob( "first", new ByteArrayInputStream( new byte[] { 1, 2, 3 } ), 2L ) );
		assertException( "updateBoolean()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBoolean( 1, true ) );
		assertException( "updateBoolean()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBoolean( "first", true ) );
		assertException( "updateByte()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateByte( 1, (byte)1 ) );
		assertException( "updateByte()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateByte( "first", (byte)1 ) );
		assertException( "updateBytes()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBytes( 1, new byte[] { 1, 2, 3 } ) );
		assertException( "updateBytes()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateBytes( "first", new byte[] { 1, 2, 3 } ) );
		assertException( "updateCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateCharacterStream( 1, new StringReader( "Something" ) ) );
		assertException( "updateCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateCharacterStream( 1, new StringReader( "Something" ), 1 ) );
		assertException( "updateCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateCharacterStream( 1, new StringReader( "Something" ), 2L ) );
		assertException( "updateCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateCharacterStream( "first", new StringReader( "Something" ) ) );
		assertException( "updateCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateCharacterStream( "first", new StringReader( "Something" ), 1 ) );
		assertException( "updateCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateCharacterStream( "first", new StringReader( "Something" ), 2L ) );
		assertException( "updateClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateClob( 1, mock( Clob.class ) ) );
		assertException( "updateClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateClob( 1, new StringReader( "Something" ) ) );
		assertException( "updateClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateClob( 1, new StringReader( "Something" ), 2L ) );
		assertException( "updateClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateClob( "first", mock( Clob.class ) ) );
		assertException( "updateClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateClob( "first", new StringReader( "Something" ) ) );
		assertException( "updateClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateClob( "first", new StringReader( "Something" ), 2L ) );
		assertException( "updateDate()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateDate( 1, mock( java.sql.Date.class ) ) );
		assertException( "updateDate()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateDate( "first", mock( java.sql.Date.class ) ) );
		assertException( "updateDouble()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateDouble( 1, 1234.5 ) );
		assertException( "updateDouble()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateDouble( "first", 1234.5 ) );
		assertException( "updateFloat()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateFloat( 1, 1234.5f ) );
		assertException( "updateFloat()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateFloat( "first", 1234.5f ) );
		assertException( "updateInt()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateInt( 1, 123 ) );
		assertException( "updateInt()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateInt( "first", 123 ) );
		assertException( "updateLong()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateLong( 1, 1234567890L ) );
		assertException( "updateLong()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateLong( "first", 1234567890L ) );
		assertException( "updateNCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNCharacterStream( 1, new StringReader( "Something" ) ) );
		assertException( "updateNCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNCharacterStream( 1, new StringReader( "Something" ), 2L ) );
		assertException( "updateNCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNCharacterStream( "first", new StringReader( "Something" ) ) );
		assertException( "updateNCharacterStream()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNCharacterStream( "first", new StringReader( "Something" ), 2L ) );
		assertException( "updateNClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNClob( 1, mock( NClob.class ) ) );
		assertException( "updateNClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNClob( "first", mock( NClob.class ) ) );
		assertException( "updateNClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNClob( 1, new StringReader( "Something" ) ) );
		assertException( "updateNClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNClob( 1, new StringReader( "Something" ), 2L ) );
		assertException( "updateNClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNClob( "first", new StringReader( "Something" ) ) );
		assertException( "updateNClob()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNClob( "first", new StringReader( "Something" ), 2L ) );
		assertException( "updateNString()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNString( 1, "Something" ) );
		assertException( "updateNString()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNString( "first", "Something" ) );
		assertException( "updateNull()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNull( 1 ) );
		assertException( "updateNull()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateNull( "first" ) );
		assertException( "updateObject()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateObject( 1, "Object", 1 ) );
		assertException( "updateObject()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateObject( 1, "Object" ) );
		assertException( "updateObject()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateObject( "first", "Object", 1 ) );
		assertException( "updateObject()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateObject( "first", "Object" ) );
		assertException( "updateRef()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateRef( 1, mock( Ref.class ) ) );
		assertException( "updateRef()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateRef( "first", mock( Ref.class ) ) );
		assertException( "updateRowId()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateRowId( 1, mock( RowId.class ) ) );
		assertException( "updateRowId()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateRowId( "first", mock( RowId.class ) ) );
		assertException( "updateShort()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateShort( 1, (short)1234 ) );
		assertException( "updateShort()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateShort( "first", (short)1234 ) );
		assertException( "updateSQLXML()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateSQLXML( 1, mock( SQLXML.class ) ) );
		assertException( "updateSQLXML()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateSQLXML( "first", mock( SQLXML.class ) ) );
		assertException( "updateString()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateString( 1, "Something" ) );
		assertException( "updateString()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateString( "first", "Something" ) );
		assertException( "updateTime()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateTime( 1, new Time( 123456 ) ) );
		assertException( "updateTime()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateTime( "first", new Time( 123456 ) ) );
		assertException( "updateTimestamp()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateTimestamp( 1, new Timestamp( 123456 ) ) );
		assertException( "updateTimestamp()", new SQLException( ResultSetClone.NOT_WRITABLE ), () -> rsc.updateTimestamp( "first", new Timestamp( 123456 ) ) );
	}

	@Test
	public void testDelete()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		final ResultSetClone rsc = new ResultSetClone( resultSet );

		assertException( "deleteRow()", new SQLException( ResultSetClone.NOT_WRITABLE ), rsc::deleteRow );
		assertFalse( "rowDeleted()", rsc.rowDeleted() );
		assertException( "refreshRow()", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), rsc::refreshRow );
	}

	@Test
	public void testFindColumn()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnLabel( 1 ) ).thenReturn( "label1" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "last" );
		when( metaData.getColumnLabel( 2 ) ).thenReturn( "label2" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );

		final ResultSetClone rsc = new ResultSetClone( resultSet );
		assertEquals( "findColumn( 'first' )", 1, rsc.findColumn( "first" ) );
		assertEquals( "findColumn( 'label1' )", 1, rsc.findColumn( "label1" ) );
		assertEquals( "findColumn( 'LAST' )", 2, rsc.findColumn( "LAST" ) );
		assertEquals( "findColumn( 'LABEL2' )", 2, rsc.findColumn( "LABEL2" ) );
		assertException( "findColumn( null )", new SQLException( ResultSetClone.NO_SUCH_COLUMN + " null" ), () -> rsc.findColumn( null ) );
		assertException( "findColumn( 'xxx' )", new SQLException( ResultSetClone.NO_SUCH_COLUMN + " 'xxx'" ), () -> rsc.findColumn( "xxx" ) );
		assertException( "findColumn( '' )", new SQLException( ResultSetClone.NO_SUCH_COLUMN + " ''" ), () -> rsc.findColumn( "" ) );
	}

	@Test
	public void testFetchDirection()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		final ResultSetClone rsc = new ResultSetClone( resultSet );
		assertEquals( "Unexpected initial 'fetchDirection'", ResultSet.FETCH_UNKNOWN, rsc.getFetchDirection() );
		rsc.setFetchDirection( ResultSet.FETCH_FORWARD );
		assertEquals( "Unexpected 'fetchDirection' after setting to FETCH_FORWARD", ResultSet.FETCH_FORWARD, rsc.getFetchDirection() );
		rsc.setFetchDirection( ResultSet.FETCH_REVERSE );
		assertEquals( "Unexpected 'fetchDirection' after setting to FETCH_REVERSE", ResultSet.FETCH_REVERSE, rsc.getFetchDirection() );
		assertEquals( "Unexpected 'fetchDirection'", ResultSet.FETCH_REVERSE, rsc.getFetchDirection() );
		//noinspection MagicConstant
		assertException( "findColumn( null )", new SQLException( ResultSetClone.INVALID_FETCH_DIRECTION + " - 999" ), () -> rsc.setFetchDirection( 999 ) );
	}

	@Test
	public void testNextEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( false );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertFalse( "next()", rsc.next() );
		assertEquals( "getRow()", 0, rsc.getRow() );
		assertFalse( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "isAfterLast()", rsc.isAfterLast() );
		assertFalse( "isFirst()", rsc.isFirst() );
		assertFalse( "isLast()", rsc.isLast() );
	}

	@Test
	public void testNextSingleRow()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "1st next()", rsc.next() );
		assertEquals( "1st getRow()", 1, rsc.getRow() );
		assertFalse( "1st isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "1st isAfterLast()", rsc.isAfterLast() );
		assertTrue( "1st isFirst()", rsc.isFirst() );
		assertTrue( "1st isLast()", rsc.isLast() );
		assertFalse( "2nd next()", rsc.next() );
		assertEquals( "2nd getRow()", 0, rsc.getRow() );
		assertFalse( "2nd isBeforeFirst()", rsc.isBeforeFirst() );
		assertTrue( "2nd isAfterLast()", rsc.isAfterLast() );
		assertFalse( "2nd isFirst()", rsc.isFirst() );
		assertFalse( "2nd isLast()", rsc.isLast() );
		assertFalse( "3rd next()", rsc.next() );
	}

	@Test
	public void testNextTwoRows()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John", "Jane" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "1st next()", rsc.next() );
		assertEquals( "1st getRow()", 1, rsc.getRow() );
		assertFalse( "1st isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "1st isAfterLast()", rsc.isAfterLast() );
		assertTrue( "1st isFirst()", rsc.isFirst() );
		assertFalse( "1st isLast()", rsc.isLast() );
		assertTrue( "2nd next()", rsc.next() );
		assertEquals( "2nd getRow()", 2, rsc.getRow() );
		assertFalse( "2nd isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "2nd isAfterLast()", rsc.isAfterLast() );
		assertFalse( "2nd isFirst()", rsc.isFirst() );
		assertTrue( "2nd isLast()", rsc.isLast() );
		assertFalse( "3rd next()", rsc.next() );
		assertEquals( "3rd getRow()", 0, rsc.getRow() );
		assertFalse( "3rd isBeforeFirst()", rsc.isBeforeFirst() );
		assertTrue( "3rd isAfterLast()", rsc.isAfterLast() );
		assertFalse( "3rd isFirst()", rsc.isFirst() );
		assertFalse( "3rd isLast()", rsc.isLast() );
		assertFalse( "4th next()", rsc.next() );
	}

	@Test
	public void testPreviousEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( false );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertFalse( "previous()", rsc.previous() );
		assertEquals( "getRow()", 0, rsc.getRow() );
		assertFalse( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "isAfterLast()", rsc.isAfterLast() );
		assertFalse( "isFirst()", rsc.isFirst() );
		assertFalse( "isLast()", rsc.isLast() );
	}

	@Test
	public void testPreviousSingleRow()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertFalse( "1st previous()", rsc.previous() );
		assertEquals( "1st getRow()", 0, rsc.getRow() );
		assertTrue( "1st isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "1st isAfterLast()", rsc.isAfterLast() );
		assertFalse( "1st isFirst()", rsc.isFirst() );
		assertFalse( "1st isLast()", rsc.isLast() );
		rsc.afterLast();
		assertTrue( "2nd previous()", rsc.previous() );
		assertEquals( "2nd getRow()", 1, rsc.getRow() );
		assertFalse( "2nd isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "2nd isAfterLast()", rsc.isAfterLast() );
		assertTrue( "2nd isFirst()", rsc.isFirst() );
		assertTrue( "2nd isLast()", rsc.isLast() );
		assertFalse( "3rd previous()", rsc.previous() );
		assertEquals( "3rd getRow()", 0, rsc.getRow() );
		assertTrue( "3rd isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "3rd isAfterLast()", rsc.isAfterLast() );
		assertFalse( "3rd isFirst()", rsc.isFirst() );
		assertFalse( "3rd isLast()", rsc.isLast() );
		assertFalse( "4th previous()", rsc.previous() );
	}

	@Test
	public void testPreviousTwoRows()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John", "Jane" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertFalse( "1st previous()", rsc.previous() );
		assertEquals( "1st getRow()", 0, rsc.getRow() );
		assertTrue( "1st isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "1st isAfterLast()", rsc.isAfterLast() );
		assertFalse( "1st isFirst()", rsc.isFirst() );
		assertFalse( "1st isLast()", rsc.isLast() );
		rsc.afterLast();
		assertTrue( "2nd previous()", rsc.previous() );
		assertEquals( "2nd getRow()", 2, rsc.getRow() );
		assertFalse( "2nd isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "2nd isAfterLast()", rsc.isAfterLast() );
		assertFalse( "2nd isFirst()", rsc.isFirst() );
		assertTrue( "2nd isLast()", rsc.isLast() );
		assertTrue( "3rd previous()", rsc.previous() );
		assertEquals( "3rd getRow()", 1, rsc.getRow() );
		assertFalse( "3rd isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "3rd isAfterLast()", rsc.isAfterLast() );
		assertTrue( "3rd isFirst()", rsc.isFirst() );
		assertFalse( "3rd isLast()", rsc.isLast() );
		assertFalse( "4th previous()", rsc.previous() );
		assertEquals( "4th getRow()", 0, rsc.getRow() );
		assertTrue( "4th isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "4th isAfterLast()", rsc.isAfterLast() );
		assertFalse( "4th isFirst()", rsc.isFirst() );
		assertFalse( "4th isLast()", rsc.isLast() );
		assertFalse( "5th previous()", rsc.previous() );
	}

	@Test
	public void testBeforeFirstEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( false );
		final ResultSet rsc = new ResultSetClone( resultSet );

		rsc.next();
		rsc.beforeFirst();

		assertEquals( "getRow()", 0, rsc.getRow() );
		assertFalse( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "isAfterLast()", rsc.isAfterLast() );
		assertFalse( "isFirst()", rsc.isFirst() );
		assertFalse( "isLast()", rsc.isLast() );
	}

	@Test
	public void testBeforeFirstNonEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, true, true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John", "Jane", "Dave" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		rsc.next();
		rsc.beforeFirst();

		assertEquals( "getRow()", 0, rsc.getRow() );
		assertTrue( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "isAfterLast()", rsc.isAfterLast() );
		assertFalse( "isFirst()", rsc.isFirst() );
		assertFalse( "isLast()", rsc.isLast() );
	}

	@Test
	public void testAfterLastEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( false );
		final ResultSet rsc = new ResultSetClone( resultSet );

		rsc.afterLast();

		assertEquals( "getRow()", 0, rsc.getRow() );
		assertFalse( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "isAfterLast()", rsc.isAfterLast() );
		assertFalse( "isFirst()", rsc.isFirst() );
		assertFalse( "isLast()", rsc.isLast() );
	}

	@Test
	public void testAfterLastNonEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, true, true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John", "Jane", "Dave" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		rsc.afterLast();

		assertEquals( "getRow()", 0, rsc.getRow() );
		assertFalse( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertTrue( "isAfterLast()", rsc.isAfterLast() );
		assertFalse( "isFirst()", rsc.isFirst() );
		assertFalse( "isLast()", rsc.isLast() );
	}

	@Test
	public void testFirstEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( false );
		final ResultSet rsc = new ResultSetClone( resultSet );

		rsc.next();
		rsc.first();

		assertEquals( "getRow()", 0, rsc.getRow() );
		assertFalse( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "isAfterLast()", rsc.isAfterLast() );
		assertFalse( "isFirst()", rsc.isFirst() );
		assertFalse( "isLast()", rsc.isLast() );
	}

	@Test
	public void testFirstNonEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, true, true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John", "Jane", "Dave" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		rsc.first();

		assertEquals( "getRow()", 1, rsc.getRow() );
		assertFalse( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "isAfterLast()", rsc.isAfterLast() );
		assertTrue( "isFirst()", rsc.isFirst() );
		assertFalse( "isLast()", rsc.isLast() );
	}

	@Test
	public void testLastEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( false );
		final ResultSet rsc = new ResultSetClone( resultSet );

		rsc.last();

		assertEquals( "getRow()", 0, rsc.getRow() );
		assertFalse( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "isAfterLast()", rsc.isAfterLast() );
		assertFalse( "isFirst()", rsc.isFirst() );
		assertFalse( "isLast()", rsc.isLast() );
	}

	@Test
	public void testLastNonEmpty()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, true, true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John", "Jane", "Dave" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		rsc.last();

		assertEquals( "getRow()", 3, rsc.getRow() );
		assertFalse( "isBeforeFirst()", rsc.isBeforeFirst() );
		assertFalse( "isAfterLast()", rsc.isAfterLast() );
		assertFalse( "isFirst()", rsc.isFirst() );
		assertTrue( "isLast()", rsc.isLast() );
	}

	@Test
	public void testGetArray()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( "Something" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertException( "getArray( 'second' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getArray( "second" ) );

		assertException( "getArray( 2 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getArray( 2 ) );
	}

	@Test
	public void testGetAsciiStream()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( "Something" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertException( "getAsciiStream( 'second' )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getAsciiStream( "second" ) );

		assertException( "getAsciiStream( 2 )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getAsciiStream( 2 ) );
	}

	@Test
	public void testGetBigDecimal()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 3 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( "Something" );
		when( resultSet.getObject( 3 ) ).thenReturn( 1234.5 );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertNull( "getBigDecimal( 'first' )", rsc.getBigDecimal( "first" ) );
		assertException( "getBigDecimal( 'second' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getBigDecimal( "second" ) );
		assertEquals( "getBigDecimal( 'third' )", new BigDecimal( "1234.5" ), rsc.getBigDecimal( "third" ) );

		assertNull( "getBigDecimal( 'first', 0 )", rsc.getBigDecimal( "first", 0 ) );
		assertException( "getBigDecimal( 'second', 0 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getBigDecimal( "second", 0 ) );
		assertEquals( "getBigDecimal( 'third', 0 )", new BigDecimal( "1235" ), rsc.getBigDecimal( "third", 0 ) );

		assertNull( "getBigDecimal( 1 )", rsc.getBigDecimal( 1 ) );
		assertException( "getBigDecimal( 2' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getBigDecimal( 2 ) );
		assertEquals( "getBigDecimal( 3 )", new BigDecimal( "1234.5" ), rsc.getBigDecimal( 3 ) );

		assertNull( "getBigDecimal( 1, 0 )", rsc.getBigDecimal( 1, 0 ) );
		assertException( "getBigDecimal( 2, 0 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getBigDecimal( 2, 0 ) );
		assertEquals( "getBigDecimal( 3, 0 )", new BigDecimal( "1235" ), rsc.getBigDecimal( 3, 0 ) );
	}

	@Test
	public void testGetBinaryStream()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new byte[] { 1, 2, 3 } );
		when( resultSet.getObject( 3 ) ).thenReturn( "Something" );
		when( resultSet.getObject( 4 ) ).thenReturn( 1234 );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertNull( "getBinaryStream( 'first' )", rsc.getBinaryStream( "first" ) );
		assertArrayEquals( "getBinaryStream( 'second' )", new byte[] { 1, 2, 3 }, readBytes( rsc.getBinaryStream( "second" ) ) );
		assertArrayEquals( "getBinaryStream( 'third' )", "Something".getBytes(), readBytes( rsc.getBinaryStream( "third" ) ) );
		assertException( "getBinaryStream( 'fourth' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + Integer.class ), () -> rsc.getBinaryStream( "fourth" ) );

		assertNull( "getBinaryStream( 1 )", rsc.getBinaryStream( 1 ) );
		assertArrayEquals( "getBinaryStream( 2 )", new byte[] { 1, 2, 3 }, readBytes( rsc.getBinaryStream( 2 ) ) );
		assertArrayEquals( "getBinaryStream( 3 )", "Something".getBytes(), readBytes( rsc.getBinaryStream( 3 ) ) );
		assertException( "getBinaryStream( 4 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + Integer.class ), () -> rsc.getBinaryStream( 4 ) );
	}

	private static byte[] readBytes( @NotNull final InputStream in )
	throws IOException
	{
		final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for ( int i = in.read(); i >= 0; i = in.read() )
		{
			buffer.write( i );
		}
		return buffer.toByteArray();
	}

	@Test
	public void testGetBlob()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new byte[] { 1, 2, 3 } );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertException( "getBlob( 'second' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getBlob( "second" ) );

		assertException( "getBlob( 2 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getBlob( 2 ) );
	}

	@Test
	public void testGetBoolean()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 5 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );
		when( metaData.getColumnName( 5 ) ).thenReturn( "fifth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( true );
		when( resultSet.getObject( 3 ) ).thenReturn( 0 );
		when( resultSet.getObject( 4 ) ).thenReturn( "true" );
		when( resultSet.getObject( 5 ) ).thenReturn( new byte[ 0 ] );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertFalse( "getBoolean( 'first' )", rsc.getBoolean( "first" ) );
		assertTrue( "getBoolean( 'second' )", rsc.getBoolean( "second" ) );
		assertFalse( "getBoolean( 'third' )", rsc.getBoolean( "third" ) );
		assertTrue( "getBoolean( 'fourth' )", rsc.getBoolean( "fourth" ) );
		assertException( "getBoolean( 'fifth' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + byte[].class ), () -> rsc.getBoolean( "fifth" ) );

		assertFalse( "getBoolean( 1 )", rsc.getBoolean( 1 ) );
		assertTrue( "getBoolean( 2 )", rsc.getBoolean( 2 ) );
		assertFalse( "getBoolean( 3 )", rsc.getBoolean( 3 ) );
		assertTrue( "getBoolean( 4 )", rsc.getBoolean( 4 ) );
		assertException( "getBoolean( 5 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + byte[].class ), () -> rsc.getBoolean( 5 ) );
	}

	@Test
	public void testGetByte()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 3 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( 123 );
		when( resultSet.getObject( 3 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertEquals( "getByte( 'first' )", 0, rsc.getByte( "first" ) );
		assertEquals( "getByte( 'second' )", (byte)123, rsc.getByte( "second" ) );
		assertException( "getByte( 'third' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getByte( "third" ) );

		assertEquals( "getByte( 1 )", 0, rsc.getByte( 1 ) );
		assertEquals( "getByte( 2 )", (byte)123, rsc.getByte( 2 ) );
		assertException( "getByte( 3 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getByte( 3 ) );
	}

	@Test
	public void testGetBytes()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new byte[] { 1, 2, 3 } );
		when( resultSet.getObject( 3 ) ).thenReturn( "Something" );
		when( resultSet.getObject( 4 ) ).thenReturn( 1234 );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertNull( "getBytes( 'first' )", rsc.getBytes( "first" ) );
		assertArrayEquals( "getBytes( 'second' )", new byte[] { 1, 2, 3 }, rsc.getBytes( "second" ) );
		assertArrayEquals( "getBytes( 'third' )", "Something".getBytes(), rsc.getBytes( "third" ) );
		assertException( "getBytes( 'fourth' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + Integer.class ), () -> rsc.getBytes( "fourth" ) );

		assertNull( "getBytes( 1 )", rsc.getBytes( 1 ) );
		assertArrayEquals( "getBytes( 2 )", new byte[] { 1, 2, 3 }, rsc.getBytes( 2 ) );
		assertArrayEquals( "getBytes( 3 )", "Something".getBytes(), rsc.getBytes( 3 ) );
		assertException( "getBytes( 4 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + Integer.class ), () -> rsc.getBytes( 4 ) );
	}

	@Test
	public void testGetCharacterStream()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new char[] { 'a', 'b', 'c' } );
		when( resultSet.getObject( 3 ) ).thenReturn( "Something" );
		when( resultSet.getObject( 4 ) ).thenReturn( 1234 );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertNull( "getCharacterStream( 'first' )", rsc.getCharacterStream( "first" ) );
		assertEquals( "getCharacterStream( 'second' )", "abc", readCharacters( rsc.getCharacterStream( "second" ) ) );
		assertEquals( "getCharacterStream( 'third' )", "Something", readCharacters( rsc.getCharacterStream( "third" ) ) );
		assertException( "getCharacterStream( 'fourth' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + Integer.class ), () -> rsc.getCharacterStream( "fourth" ) );

		assertNull( "getCharacterStream( 1 )", rsc.getCharacterStream( 1 ) );
		assertEquals( "getCharacterStream( 2 )", "abc", readCharacters( rsc.getCharacterStream( 2 ) ) );
		assertEquals( "getCharacterStream( 3 )", "Something", readCharacters( rsc.getCharacterStream( 3 ) ) );
		assertException( "getCharacterStream( 4 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + Integer.class ), () -> rsc.getCharacterStream( 4 ) );
	}

	private static String readCharacters( @NotNull final Reader in )
	throws IOException
	{
		final StringBuilder buffer = new StringBuilder();
		for ( int i = in.read(); i >= 0; i = in.read() )
		{
			buffer.append( (char)i );
		}
		return buffer.toString();
	}

	@Test
	public void testGetClob()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new char[] { 'a', 'b', 'c' } );
		when( resultSet.getObject( 3 ) ).thenReturn( "Something" );
		when( resultSet.getObject( 4 ) ).thenReturn( 1234 );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertException( "getClob( 'first' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getClob( "first" ) );
		assertException( "getClob( 'second' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getClob( "second" ) );
		assertException( "getClob( 'third' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getClob( "third" ) );
		assertException( "getClob( 'fourth' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getClob( "fourth" ) );

		assertException( "getClob( 1 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getClob( 1 ) );
		assertException( "getClob( 2 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getClob( 2 ) );
		assertException( "getClob( 3 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getClob( 3 ) );
		assertException( "getClob( 4 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getClob( 4 ) );
	}

	@Test
	public void testGetDate()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 5 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );
		when( metaData.getColumnName( 5 ) ).thenReturn( "fifth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new java.sql.Date( 123456 ) );
		when( resultSet.getObject( 3 ) ).thenReturn( new java.util.Date( 1234567 ) );
		when( resultSet.getObject( 4 ) ).thenReturn( 12345678 );
		when( resultSet.getObject( 5 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertNull( "getDate( 'first' )", rsc.getDate( "first" ) );
		assertEquals( "getDate( 'second' )", new java.sql.Date( 123456 ), rsc.getDate( "second" ) );
		assertEquals( "getDate( 'third' )", new java.sql.Date( 1234567 ), rsc.getDate( "third" ) );
		assertEquals( "getDate( 'fourth' )", new java.sql.Date( 12345678 ), rsc.getDate( "fourth" ) );
		assertException( "getDate( 'fifth' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getDate( "fifth" ) );

		assertException( "getDate( 'first', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getDate( "first", Calendar.getInstance() ) );
		assertException( "getDate( 'second', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getDate( "second", Calendar.getInstance() ) );
		assertException( "getDate( 'third', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getDate( "third", Calendar.getInstance() ) );
		assertException( "getDate( 'fourth', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getDate( "fourth", Calendar.getInstance() ) );
		assertException( "getDate( 'fifth', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getDate( "fifth", Calendar.getInstance() ) );
	}

	@Test
	public void testGetDouble()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 3 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new BigDecimal( "1234.5" ) );
		when( resultSet.getObject( 3 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertEquals( "getDouble( 'first' )", 0, rsc.getDouble( "first" ), 0.0 );
		assertEquals( "getDouble( 'second' )", 1234.5, rsc.getDouble( "second" ), 0.0 );
		assertException( "getDouble( 'third' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getDouble( "third" ) );

		assertEquals( "getDouble( 1 )", 0, rsc.getDouble( 1 ), 0.0 );
		assertEquals( "getDouble( 2 )", 1234.5, rsc.getDouble( 2 ), 0.0 );
		assertException( "getDouble( 3 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getDouble( 3 ) );
	}

	@Test
	public void testGetFloat()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 3 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new BigDecimal( "1234.5" ) );
		when( resultSet.getObject( 3 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertEquals( "getFloat( 'first' )", 0, rsc.getFloat( "first" ), 0.0f );
		assertEquals( "getFloat( 'second' )", 1234.5f, rsc.getFloat( "second" ), 0.0f );
		assertException( "getFloat( 'third' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getFloat( "third" ) );

		assertEquals( "getFloat( 1 )", 0, rsc.getFloat( 1 ), 0.0f );
		assertEquals( "getFloat( 2 )", 1234.5f, rsc.getFloat( 2 ), 0.0f );
		assertException( "getFloat( 3 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getFloat( 3 ) );
	}

	@Test
	public void testGetInt()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 3 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( 1234.5 );
		when( resultSet.getObject( 3 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertEquals( "getInt( 'first' )", 0, rsc.getInt( "first" ) );
		assertEquals( "getInt( 'second' )", 1234, rsc.getInt( "second" ) );
		assertException( "getInt( 'third' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getInt( "third" ) );

		assertEquals( "getInt( 1 )", 0, rsc.getInt( 1 ) );
		assertEquals( "getInt( 2 )", 1234, rsc.getInt( 2 ) );
		assertException( "getInt( 3 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getInt( 3 ) );
	}

	@Test
	public void testGetLong()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 3 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( 1234567890123.0 );
		when( resultSet.getObject( 3 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertEquals( "getLong( 'first' )", 0, rsc.getLong( "first" ) );
		assertEquals( "getLong( 'second' )", 1234567890123L, rsc.getLong( "second" ) );
		assertException( "getLong( 'third' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getLong( "third" ) );

		assertEquals( "getLong( 1 )", 0, rsc.getLong( 1 ) );
		assertEquals( "getLong( 2 )", 1234567890123L, rsc.getLong( 2 ) );
		assertException( "getLong( 3 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getLong( 3 ) );
	}

	@Test
	public void testGetNCharacterStream()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new char[] { 'a', 'b', 'c' } );
		when( resultSet.getObject( 3 ) ).thenReturn( "Something" );
		when( resultSet.getObject( 4 ) ).thenReturn( 1234 );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertException( "getNCharacterStream( 'first' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNCharacterStream( "first" ) );
		assertException( "getNCharacterStream( 'second' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNCharacterStream( "second" ) );
		assertException( "getNCharacterStream( 'third' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNCharacterStream( "third" ) );
		assertException( "getNCharacterStream( 'fourth' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNCharacterStream( "fourth" ) );

		assertException( "getNCharacterStream( 1 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNCharacterStream( 1 ) );
		assertException( "getNCharacterStream( 2 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNCharacterStream( 2 ) );
		assertException( "getNCharacterStream( 3 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNCharacterStream( 3 ) );
		assertException( "getNCharacterStream( 4 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNCharacterStream( 4 ) );
	}

	@Test
	public void testGetNClob()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new char[] { 'a', 'b', 'c' } );
		when( resultSet.getObject( 3 ) ).thenReturn( "Something" );
		when( resultSet.getObject( 4 ) ).thenReturn( 1234 );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertException( "getNClob( 'first' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNClob( "first" ) );
		assertException( "getNClob( 'second' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNClob( "second" ) );
		assertException( "getNClob( 'third' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNClob( "third" ) );
		assertException( "getNClob( 'fourth' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNClob( "fourth" ) );

		assertException( "getNClob( 1 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNClob( 1 ) );
		assertException( "getNClob( 2 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNClob( 2 ) );
		assertException( "getNClob( 3 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNClob( 3 ) );
		assertException( "getNClob( 4 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNClob( 4 ) );
	}

	@Test
	public void testGetNString()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new char[] { 'a', 'b', 'c' } );
		when( resultSet.getObject( 3 ) ).thenReturn( "Something" );
		when( resultSet.getObject( 4 ) ).thenReturn( 1234 );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertException( "getNString( 'first' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNString( "first" ) );
		assertException( "getNString( 'second' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNString( "second" ) );
		assertException( "getNString( 'third' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNString( "third" ) );
		assertException( "getNString( 'fourth' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNString( "fourth" ) );

		assertException( "getNString( 1 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNString( 1 ) );
		assertException( "getNString( 2 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNString( 2 ) );
		assertException( "getNString( 3 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNString( 3 ) );
		assertException( "getNString( 4 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getNString( 4 ) );
	}

	@Test
	public void testGetObject()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 3 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( 1234.5 );
		when( resultSet.getObject( 3 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertException( "getObject() before first row", new SQLException( ResultSetClone.CURSOR_BEFORE_FIRST ), () -> rsc.getObject( "first" ) );
		assertTrue( "next()", rsc.next() );

		assertNull( "getObject( 'first' )", rsc.getObject( "first" ) );
		assertEquals( "getObject( 'second' )", 1234.5, rsc.getObject( "second" ) );
		assertEquals( "getObject( 'third' )", "String", rsc.getObject( "third" ) );

		assertException( "getObject( 0 )", new SQLException( ResultSetClone.INVALID_COLUMN_INDEX ), () -> rsc.getObject( 0 ) );
		assertNull( "getObject( 1 )", rsc.getObject( 1 ) );
		assertEquals( "getObject( 2 )", 1234.5, rsc.getObject( 2 ) );
		assertEquals( "getObject( 3 )", "String", rsc.getObject( 3 ) );
		assertException( "getObject( 4 )", new SQLException( ResultSetClone.INVALID_COLUMN_INDEX ), () -> rsc.getObject( 4 ) );

		assertNull( "getObject( 'first', String.class )", rsc.getObject( "first", String.class ) );
		assertException( "getObject( 'second', String.class )", ClassCastException.class, () -> rsc.getObject( "second", String.class ) );
		assertEquals( "getObject( 'third', String.class )", "String", rsc.getObject( "third", String.class ) );

		assertNull( "getObject( 1, String.class )", rsc.getObject( 1, String.class ) );
		assertException( "getObject( 2, String.class )", ClassCastException.class, () -> rsc.getObject( 2, String.class ) );
		assertEquals( "getObject( 3, String.class )", "String", rsc.getObject( 3, String.class ) );

		final Map<String, Class<?>> typeMap = Collections.emptyMap();
		assertException( "getObject( 'first', map )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getObject( "first", typeMap ) );
		assertException( "getObject( 'second', map )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getObject( "second", typeMap ) );
		assertException( "getObject( 'third', map )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getObject( "third", typeMap ) );

		assertException( "getObject( 1, map )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getObject( 1, typeMap ) );
		assertException( "getObject( 2, map )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getObject( 2, typeMap ) );
		assertException( "getObject( 3, map )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getObject( 3, typeMap ) );

		assertFalse( "next()", rsc.next() );
		assertException( "getObject() after last row", new SQLException( ResultSetClone.CURSOR_AFTER_LAST ), () -> rsc.getObject( "first" ) );

		rsc.close();
		assertException( "getObject() after close()", new SQLException( ResultSetClone.RESULT_CLOSED ), () -> rsc.getObject( "first" ) );
	}

	@Test
	public void testGetRef()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertException( "getRef( 'first' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getRef( "first" ) );

		assertException( "getRef( 1 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getRef( 1 ) );
	}

	@Test
	public void testGetRowId()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertException( "getRowId( 'first' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getRowId( "first" ) );

		assertException( "getRowId( 1 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getRowId( 1 ) );
	}

	@Test
	public void testGetShort()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 3 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( 12345L );
		when( resultSet.getObject( 3 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertEquals( "getShort( 'first' )", 0, rsc.getShort( "first" ) );
		assertEquals( "getShort( 'second' )", (short)12345, rsc.getShort( "second" ) );
		assertException( "getShort( 'third' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getShort( "third" ) );

		assertEquals( "getShort( '1 )", 0, rsc.getShort( 1 ) );
		assertEquals( "getShort( '2 )", (short)12345, rsc.getShort( 2 ) );
		assertException( "getShort( '3 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getShort( 3 ) );
	}

	@Test
	public void testGetSQLXML()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 4 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertException( "getSQLXML( 'first' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getSQLXML( "first" ) );

		assertException( "getSQLXML( 1 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getSQLXML( 1 ) );
	}

	@Test
	public void testGetString()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 3 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( 12345L );
		when( resultSet.getObject( 3 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertNull( "getString( 'first' )", rsc.getString( "first" ) );
		assertEquals( "getString( 'second' )", "12345", rsc.getString( "second" ) );
		assertEquals( "getString( 'third' )", "String", rsc.getString( "third" ) );

		assertNull( "getString( 1 )", rsc.getString( 1 ) );
		assertEquals( "getString( 2 )", "12345", rsc.getString( 2 ) );
		assertEquals( "getString( 3 )", "String", rsc.getString( 3 ) );
	}

	@Test
	public void testGetTime()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 5 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );
		when( metaData.getColumnName( 5 ) ).thenReturn( "fifth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new java.sql.Time( 123456 ) );
		when( resultSet.getObject( 3 ) ).thenReturn( new java.util.Date( 1234567 ) );
		when( resultSet.getObject( 4 ) ).thenReturn( 12345678 );
		when( resultSet.getObject( 5 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertNull( "getTime( 'first' )", rsc.getTime( "first" ) );
		assertEquals( "getTime( 'second' )", new java.sql.Time( 123456 ), rsc.getTime( "second" ) );
		assertEquals( "getTime( 'third' )", new java.sql.Time( 1234567 ), rsc.getTime( "third" ) );
		assertEquals( "getTime( 'fourth' )", new java.sql.Time( 12345678 ), rsc.getTime( "fourth" ) );
		assertException( "getTime( 'fifth' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getTime( "fifth" ) );

		assertNull( "getTime( 1 )", rsc.getTime( 1 ) );
		assertEquals( "getTime( 2 )", new java.sql.Time( 123456 ), rsc.getTime( 2 ) );
		assertEquals( "getTime( 3 )", new java.sql.Time( 1234567 ), rsc.getTime( 3 ) );
		assertEquals( "getTime( 4 )", new java.sql.Time( 12345678 ), rsc.getTime( 4 ) );
		assertException( "getTime( 5 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getTime( 5 ) );

		assertException( "getTime( 'first', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( "first", Calendar.getInstance() ) );
		assertException( "getTime( 'second', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( "second", Calendar.getInstance() ) );
		assertException( "getTime( 'third', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( "third", Calendar.getInstance() ) );
		assertException( "getTime( 'fourth', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( "fourth", Calendar.getInstance() ) );
		assertException( "getTime( 'fifth', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( "fifth", Calendar.getInstance() ) );

		assertException( "getTime( 1, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( 1, Calendar.getInstance() ) );
		assertException( "getTime( 2, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( 2, Calendar.getInstance() ) );
		assertException( "getTime( 3, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( 3, Calendar.getInstance() ) );
		assertException( "getTime( 4, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( 4, Calendar.getInstance() ) );
		assertException( "getTime( 5, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTime( 5, Calendar.getInstance() ) );
	}

	@Test
	public void testGetTimestamp()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 5 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );
		when( metaData.getColumnName( 3 ) ).thenReturn( "third" );
		when( metaData.getColumnName( 4 ) ).thenReturn( "fourth" );
		when( metaData.getColumnName( 5 ) ).thenReturn( "fifth" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( new java.sql.Timestamp( 123456 ) );
		when( resultSet.getObject( 3 ) ).thenReturn( new java.util.Date( 1234567 ) );
		when( resultSet.getObject( 4 ) ).thenReturn( 12345678 );
		when( resultSet.getObject( 5 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertNull( "getTimestamp( 'first' )", rsc.getTimestamp( "first" ) );
		assertEquals( "getTimestamp( 'second' )", new java.sql.Timestamp( 123456 ), rsc.getTimestamp( "second" ) );
		assertEquals( "getTimestamp( 'third' )", new java.sql.Timestamp( 1234567 ), rsc.getTimestamp( "third" ) );
		assertEquals( "getTimestamp( 'fourth' )", new java.sql.Timestamp( 12345678 ), rsc.getTimestamp( "fourth" ) );
		assertException( "getTimestamp( 'fifth' )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getTimestamp( "fifth" ) );

		assertNull( "getTimestamp( 1 )", rsc.getTimestamp( 1 ) );
		assertEquals( "getTimestamp( 2 )", new java.sql.Timestamp( 123456 ), rsc.getTimestamp( 2 ) );
		assertEquals( "getTimestamp( 3 )", new java.sql.Timestamp( 1234567 ), rsc.getTimestamp( 3 ) );
		assertEquals( "getTimestamp( 4 )", new java.sql.Timestamp( 12345678 ), rsc.getTimestamp( 4 ) );
		assertException( "getTimestamp( 5 )", new SQLException( ResultSetClone.INCOMPATIBLE_TYPE + " - " + String.class ), () -> rsc.getTimestamp( 5 ) );

		assertException( "getTimestamp( 'first', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( "first", Calendar.getInstance() ) );
		assertException( "getTimestamp( 'second', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( "second", Calendar.getInstance() ) );
		assertException( "getTimestamp( 'third', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( "third", Calendar.getInstance() ) );
		assertException( "getTimestamp( 'fourth', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( "fourth", Calendar.getInstance() ) );
		assertException( "getTimestamp( 'fifth', calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( "fifth", Calendar.getInstance() ) );

		assertException( "getTimestamp( 1, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( 1, Calendar.getInstance() ) );
		assertException( "getTimestamp( 2, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( 2, Calendar.getInstance() ) );
		assertException( "getTimestamp( 3, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( 3, Calendar.getInstance() ) );
		assertException( "getTimestamp( 4, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( 4, Calendar.getInstance() ) );
		assertException( "getTimestamp( 5, calendar )", new SQLException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getTimestamp( 5, Calendar.getInstance() ) );
	}

	@Test
	public void testGetUnicodeStream()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( "String" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertException( "getUnicodeStream( 'first' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getUnicodeStream( "first" ) );
		assertException( "getUnicodeStream( 'second' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getUnicodeStream( "second" ) );

		assertException( "getUnicodeStream( 1 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getUnicodeStream( 1 ) );
		assertException( "getUnicodeStream( 2 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getUnicodeStream( 2 ) );
	}

	@Test
	public void testGetURL()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( "http://info.cern.ch/hypertext/WWW/TheProject.html" );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertTrue( "next()", rsc.next() );

		assertException( "getURL( 'first' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getURL( "first" ) );
		assertException( "getURL( 'second' )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getURL( "second" ) );

		assertException( "getURL( 1 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getURL( 1 ) );
		assertException( "getURL( 2 )", new SQLFeatureNotSupportedException( ResultSetClone.NOT_IMPLEMENTED ), () -> rsc.getURL( 2 ) );
	}

	@Test
	public void testWasNull()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "second" );

		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, false );
		when( resultSet.getObject( 2 ) ).thenReturn( 123 );
		final ResultSet rsc = new ResultSetClone( resultSet );

		assertFalse( "1st wasNull()", rsc.wasNull() );
		assertTrue( "next()", rsc.next() );
		assertFalse( "1st wasNull()", rsc.wasNull() );
		rsc.getObject( 1 );
		assertTrue( "2nd wasNull()", rsc.wasNull() );
		rsc.getObject( 2 );
		assertFalse( "3rd wasNull()", rsc.wasNull() );
	}

	@Test
	public void testGetData()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "last" );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John", "Jane" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSetClone rsc = new ResultSetClone( resultSet );
		final Object[][] expected =
		{
		{ "John", "Joe" },
		{ "Jane", "Joe" }
		};
		assertArrayEquals( expected, rsc.getData() );
	}

	@Test
	public void testToFriendlyString()
	throws Exception
	{
		final ResultSetMetaData metaData = mock( ResultSetMetaData.class );
		when( metaData.getColumnCount() ).thenReturn( 2 );
		when( metaData.getColumnName( 1 ) ).thenReturn( "first" );
		when( metaData.getColumnLabel( 1 ) ).thenReturn( "First" );
		when( metaData.getColumnType( 1 ) ).thenReturn( Types.VARCHAR );
		when( metaData.getColumnTypeName( 1 ) ).thenReturn( "VARCHAR(10)" );
		when( metaData.getColumnName( 2 ) ).thenReturn( "last" );
		when( metaData.getColumnLabel( 2 ) ).thenReturn( "Last" );
		when( metaData.getColumnType( 2 ) ).thenReturn( Types.VARCHAR );
		when( metaData.getColumnTypeName( 2 ) ).thenReturn( "VARCHAR(20)" );
		final ResultSet resultSet = mock( ResultSet.class );
		when( resultSet.getMetaData() ).thenReturn( metaData );
		when( resultSet.getFetchSize() ).thenReturn( 100 );
		when( resultSet.next() ).thenReturn( true, true, false );
		when( resultSet.getObject( 1 ) ).thenReturn( "John", "Jane" );
		when( resultSet.getObject( 2 ) ).thenReturn( "Joe" );
		final ResultSetClone rsc = new ResultSetClone( resultSet );
		final String expected = "+-------------------+------------------+\n" +
		                        "| First:VARCHAR(10) | Last:VARCHAR(20) |\n" +
		                        "+-------------------+------------------+\n" +
		                        "| John              | Joe              |\n" +
		                        "| Jane              | Joe              |\n" +
		                        "+-------------------+------------------+\n";
		assertEquals( expected, rsc.toFriendlyString() );
	}

}
