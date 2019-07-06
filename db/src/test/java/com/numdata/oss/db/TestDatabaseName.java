/*
 * Copyright (c) 2019-2019, Numdata BV, The Netherlands.
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

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link DatabaseName}.
 *
 * @author Peter S. Heijnen
 */
public class TestDatabaseName
{
	@Test
	public void testValueOf1()
	{
		final String input = "databaseName@databaseServer:12345;user=databaseUser;pass=databasePassword@user:pass@host:port";
		final DatabaseName databaseName = DatabaseName.valueOf( input );
		assertEquals( "Failed to parse 'database' property from '" + input + '\'', "databaseName", databaseName.getDatabase() );
		assertEquals( "Failed to parse 'host' property from '" + input + '\'', "databaseServer", databaseName.getHost() );
		assertEquals( "Failed to parse 'port' property from '" + input + '\'', Integer.valueOf( 12345 ), databaseName.getPort() );
		assertEquals( "Failed to parse 'user' property from '" + input + '\'', "databaseUser", databaseName.getUser() );
		assertEquals( "Failed to parse 'password' property from '" + input + '\'', "databasePassword", databaseName.getPassword() );
		assertEquals( "Failed to parse 'tunnel' property from '" + input + '\'', "user:pass@host:port", databaseName.getTunnel() );
	}

	@Test
	public void testValueOf2()
	{
		final String input = "databaseName";
		final DatabaseName databaseName = DatabaseName.valueOf( input );
		assertEquals( "Failed to parse 'database' property from '" + input + '\'', "databaseName", databaseName.getDatabase() );
		assertEquals( "Failed to parse 'host' property from '" + input + '\'', null, databaseName.getHost() );
		assertEquals( "Failed to parse 'port' property from '" + input + '\'', null, databaseName.getPort() );
		assertEquals( "Failed to parse 'user' property from '" + input + '\'', null, databaseName.getUser() );
		assertEquals( "Failed to parse 'password' property from '" + input + '\'', null, databaseName.getPassword() );
		assertEquals( "Failed to parse 'tunnel' property from '" + input + '\'', null, databaseName.getTunnel() );
	}

	@Test
	public void testValueOf3()
	{
		final String input = "databaseName@databaseServer";
		final DatabaseName databaseName = DatabaseName.valueOf( input );
		assertEquals( "Failed to parse 'database' property from '" + input + '\'', "databaseName", databaseName.getDatabase() );
		assertEquals( "Failed to parse 'host' property from '" + input + '\'', "databaseServer", databaseName.getHost() );
		assertEquals( "Failed to parse 'port' property from '" + input + '\'', null, databaseName.getPort() );
		assertEquals( "Failed to parse 'user' property from '" + input + '\'', null, databaseName.getUser() );
		assertEquals( "Failed to parse 'password' property from '" + input + '\'', null, databaseName.getPassword() );
		assertEquals( "Failed to parse 'tunnel' property from '" + input + '\'', null, databaseName.getTunnel() );
	}

	@Test
	public void testValueOf4()
	{
		final String input = "databaseName@databaseServer;user=databaseUser";
		final DatabaseName databaseName = DatabaseName.valueOf( input );
		assertEquals( "Failed to parse 'database' property from '" + input + '\'', "databaseName", databaseName.getDatabase() );
		assertEquals( "Failed to parse 'host' property from '" + input + '\'', "databaseServer", databaseName.getHost() );
		assertEquals( "Failed to parse 'port' property from '" + input + '\'', null, databaseName.getPort() );
		assertEquals( "Failed to parse 'user' property from '" + input + '\'', "databaseUser", databaseName.getUser() );
		assertEquals( "Failed to parse 'password' property from '" + input + '\'', null, databaseName.getPassword() );
		assertEquals( "Failed to parse 'tunnel' property from '" + input + '\'', null, databaseName.getTunnel() );
	}

	@Test
	public void testValueOf5()
	{
		final String input = "databaseName@databaseServer@sshHost";
		final DatabaseName databaseName = DatabaseName.valueOf( input );
		assertEquals( "Failed to parse 'database' property from '" + input + '\'', "databaseName", databaseName.getDatabase() );
		assertEquals( "Failed to parse 'host' property from '" + input + '\'', "databaseServer", databaseName.getHost() );
		assertEquals( "Failed to parse 'port' property from '" + input + '\'', null, databaseName.getPort() );
		assertEquals( "Failed to parse 'user' property from '" + input + '\'', null, databaseName.getUser() );
		assertEquals( "Failed to parse 'password' property from '" + input + '\'', null, databaseName.getPassword() );
		assertEquals( "Failed to parse 'tunnel' property from '" + input + '\'', "sshHost", databaseName.getTunnel() );
	}

	@Test
	public void testValueOf6()
	{
		final String input = "databaseName@databaseServer:123";
		final DatabaseName databaseName = DatabaseName.valueOf( input );
		assertEquals( "Failed to parse 'database' property from '" + input + '\'', "databaseName", databaseName.getDatabase() );
		assertEquals( "Failed to parse 'host' property from '" + input + '\'', "databaseServer", databaseName.getHost() );
		assertEquals( "Failed to parse 'port' property from '" + input + '\'', Integer.valueOf( 123 ), databaseName.getPort() );
		assertEquals( "Failed to parse 'user' property from '" + input + '\'', null, databaseName.getUser() );
		assertEquals( "Failed to parse 'password' property from '" + input + '\'', null, databaseName.getPassword() );
		assertEquals( "Failed to parse 'tunnel' property from '" + input + '\'', null, databaseName.getTunnel() );
	}

	@Test
	public void testValueOf7()
	{
		final String input = "databaseName";
		final DatabaseName defaults = new DatabaseName( "defaultHost", 54321, "defaultDatabase", "defaultUser", "defaultPassword", "defaultTunnel" );
		final DatabaseName databaseName = DatabaseName.valueOf( input, defaults );
		assertEquals( "Failed to parse 'database' property from '" + input + '\'', "databaseName", databaseName.getDatabase() );
		assertEquals( "Failed to parse 'host' property from '" + input + '\'', "defaultHost", databaseName.getHost() );
		assertEquals( "Failed to parse 'port' property from '" + input + '\'', Integer.valueOf( 54321 ), databaseName.getPort() );
		assertEquals( "Failed to parse 'user' property from '" + input + '\'', "defaultUser", databaseName.getUser() );
		assertEquals( "Failed to parse 'password' property from '" + input + '\'', "defaultPassword", databaseName.getPassword() );
		assertEquals( "Failed to parse 'tunnel' property from '" + input + '\'', "defaultTunnel", databaseName.getTunnel() );
	}
}
