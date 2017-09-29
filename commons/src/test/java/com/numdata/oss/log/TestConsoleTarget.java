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
package com.numdata.oss.log;

import java.io.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * This class tests the {@link ConsoleTarget} class.
 *
 * @author Peter S. Heijnen
 */
public class TestConsoleTarget
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestConsoleTarget.class.getName();

	/**
	 * Log levels.
	 */
	private static final int[] LEVELS =
	{
	ClassLogger.NONE,
	ClassLogger.FATAL,
	ClassLogger.ERROR,
	ClassLogger.WARN,
	ClassLogger.INFO,
	ClassLogger.DEBUG,
	ClassLogger.TRACE,
	ClassLogger.ALL
	};

	/**
	 * {@link ConsoleTarget#log} method.
	 */
	@Test
	public void testLog()
	{
		System.out.println( CLASS_NAME + ".testMessageLogging()" );

		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		final PrintStream out = new PrintStream( bos );
		final ConsoleTarget target = new ConsoleTarget( ClassLogger.INFO, out );
		final String logName = ConsoleTarget.class.getName();

		for ( final int loggerLevel : LEVELS )
		{
			final String loggerLevelName = ClassLogger.getLevelName( loggerLevel );

			for ( int messageLevelIndex = 1; messageLevelIndex < LEVELS.length - 1; messageLevelIndex++ )
			{
				final int messageLevel = LEVELS[ messageLevelIndex ];
				final String messageLevelName = ClassLogger.getLevelName( messageLevel );

				System.out.println( " > Logger level: " + loggerLevelName + ", message level: " + messageLevelName );

				/*
				 * Initialize logger.
				 */
				out.flush();
				bos.reset();
				target.setLevel( loggerLevel );

				/*
				 * Generate test messages.
				 */
				int nrTests = 0;
				if ( ( messageLevel != ClassLogger.NONE ) && ( messageLevel != ClassLogger.ALL ) )
				{
					target.log( logName, messageLevel, null, null, null );
					target.log( logName, messageLevel, "test2", null, null );
					target.log( logName, messageLevel, null, new Throwable(), null );
					target.log( logName, messageLevel, "test4", new Throwable(), null );
					nrTests += 4;
				}

				/*
				 * Analyze output.
				 */
				int nrLines = 0;
				{
					out.flush();
					for ( final byte b : bos.toByteArray() )
					{
						if ( b == (byte)'\n' )
						{
							nrLines++;
						}
					}
				}

				/*
				 * Check amount of output.
				 */
				if ( loggerLevel < messageLevel )
				{
					assertEquals( "should be silent", 0, nrLines );
				}
				else if ( loggerLevel < ClassLogger.DEBUG )
				{
					assertTrue( "should be extra-long", nrLines > nrTests );
				}
			}
		}
	}

	/**
	 * Test {@link ConsoleTarget#isLevelEnabled} method.
	 */
	@Test
	public void testLevelTests()
	{
		System.out.println( CLASS_NAME + ".testLevelTests()" );

		final PrintStream out = new PrintStream( new ByteArrayOutputStream() );
		final ConsoleTarget logger = new ConsoleTarget( ClassLogger.INFO, out );

		for ( final int loggerLevel : LEVELS )
		{
			final String loggerLevelName = ClassLogger.getLevelName( loggerLevel );

			System.out.println( " > Logger level: " + loggerLevelName );
			logger.setLevel( loggerLevel );

			assertEquals( "isFatalEnabled() failed", loggerLevel >= ClassLogger.FATAL, logger.isLevelEnabled( null, ClassLogger.FATAL ) );
			assertEquals( "isErrorEnabled() failed", loggerLevel >= ClassLogger.ERROR, logger.isLevelEnabled( null, ClassLogger.ERROR ) );
			assertEquals( "isWarnEnabled() failed", loggerLevel >= ClassLogger.WARN, logger.isLevelEnabled( null, ClassLogger.WARN ) );
			assertEquals( "isInfoEnabled() failed", loggerLevel >= ClassLogger.INFO, logger.isLevelEnabled( null, ClassLogger.INFO ) );
			assertEquals( "isDebugEnabled() failed", loggerLevel >= ClassLogger.DEBUG, logger.isLevelEnabled( null, ClassLogger.DEBUG ) );
			assertEquals( "isTraceEnabled() failed", loggerLevel >= ClassLogger.TRACE, logger.isLevelEnabled( null, ClassLogger.TRACE ) );
		}
	}
}
