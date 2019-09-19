/*
 * Copyright (c) 2004-2019, Numdata BV, The Netherlands.
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

import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * This class defines log services for logging information about a class. It may
 * use any underlying logging infrastructure.
 *
 * The current implementation tries to use {@code Log4jLogTarget}, which uses
 * the Log4j API for the underlying logging infrastructure. The {@code
 * ConsoleLogTarget} is used as fallback option.
 *
 * @author Peter S. Heijnen
 * @see Log4jTarget
 * @see ConsoleTarget
 */
@SuppressWarnings( { "SynchronizationOnStaticField", "AccessOfSystemProperties", "RedundantSuppression", "FinalClass", "ConstantConditions" } )
public final class ClassLogger
{
	/**
	 * Level for 'fatal' log messages.
	 */
	public static final int FATAL = 0;

	/**
	 * Level for 'error' log messages.
	 */
	public static final int ERROR = 1;

	/**
	 * Level for 'info' log messages.
	 */
	public static final int WARN = 2;

	/**
	 * Level for 'info' log messages.
	 */
	public static final int INFO = 3;

	/**
	 * Level for 'debug' log messages.
	 */
	public static final int DEBUG = 4;

	/**
	 * Level for 'trace' log messages.
	 */
	public static final int TRACE = 5;

	/**
	 * Pseudo-level to disable all log messages.
	 */
	public static final int NONE = -1;

	/**
	 * Pseudo-level to enable all log messages.
	 */
	public static final int ALL = 99;

	/**
	 * Use asynchronous logging. If {@code true}, log messages are send to log
	 * target on a separate thread, detached from the application thread; if
	 * {@code false}, log messages are send directly from the application
	 * thread.
	 */
	private static boolean asynchronousLogging = true;

	/**
	 * Registered log targets.
	 */
	private static final List<LogTarget> LOG_TARGETS = new ArrayList<LogTarget>();

	/**
	 * Name of this log.
	 */
	private final String _name;

	/**
	 * Executor service used to send messages to the log.
	 */
	@SuppressWarnings( "StaticNonFinalField" )
	private static ExecutorService executorService = null;

	/**
	 * Get {@code ClassLogger} instance for the specified class.
	 *
	 * @param forClass Class to get the {@code ClassLogger} for.
	 *
	 * @return {@code ClassLogger} for the specified class.
	 */
	public static ClassLogger getFor( final Class<?> forClass )
	{
		return new ClassLogger( forClass.getName() );
	}

	/**
	 * Get log targets. Note that this method returns an independent copy of the
	 * log target list.
	 *
	 * @return Log targets.
	 */
	public static List<LogTarget> getLogTargets()
	{
		final List<LogTarget> result;

		synchronized ( LOG_TARGETS )
		{
			result = new ArrayList<LogTarget>( LOG_TARGETS );
		}

		return result;
	}

	/**
	 * Add a new log target.
	 *
	 * @param logTarget Log target to add.
	 */
	public static void addTarget( @NotNull final LogTarget logTarget )
	{
		synchronized ( LOG_TARGETS )
		{
			LOG_TARGETS.add( logTarget );
		}
	}

	/**
	 * Remove all log targets.
	 */
	public static void removeAllLogTargets()
	{
		synchronized ( LOG_TARGETS )
		{
			LOG_TARGETS.clear();
		}
	}

	/**
	 * Remove a log target. If the log target was not added, this method has no
	 * effect.
	 *
	 * @param logTarget Log target to remove.
	 */
	public static void removeTarget( final LogTarget logTarget )
	{
		synchronized ( LOG_TARGETS )
		{
			LOG_TARGETS.remove( logTarget );
		}
	}

	/**
	 * Parse log level string.
	 *
	 * @param level Log level as string.
	 *
	 * @return Log level;
	 *
	 * @throws IllegalArgumentException if the level could not be parsed.
	 */
	public static int parseLevel( @NotNull final String level )
	{
		final int result;

		if ( "NONE".equalsIgnoreCase( level ) )
		{
			result = NONE;
		}
		else if ( "FATAL".equalsIgnoreCase( level ) )
		{
			result = FATAL;
		}
		else if ( "ERROR".equalsIgnoreCase( level ) )
		{
			result = ERROR;
		}
		else if ( "WARN".equalsIgnoreCase( level ) )
		{
			result = WARN;
		}
		else if ( "INFO".equalsIgnoreCase( level ) )
		{
			result = INFO;
		}
		else if ( "DEBUG".equalsIgnoreCase( level ) )
		{
			result = DEBUG;
		}
		else if ( "TRACE".equalsIgnoreCase( level ) )
		{
			result = TRACE;
		}
		else if ( "ALL".equalsIgnoreCase( level ) )
		{
			result = ALL;
		}
		else
		{
			throw new IllegalArgumentException( level );
		}

		return result;
	}

	/**
	 * Parse log level string.
	 *
	 * @param level Log level as string.
	 *
	 * @return Log level;
	 *
	 * @throws NullPointerException if any argument is {@code null}.
	 * @throws IllegalArgumentException if the level could not be parsed.
	 */
	public static String getLevelName( final int level )
	{
		final String result;

		switch ( level )
		{
			case NONE:
				result = "NONE";
				break;

			case FATAL:
				result = "FATAL";
				break;

			case ERROR:
				result = "ERROR";
				break;

			case WARN:
				result = "WARN";
				break;

			case INFO:
				result = "INFO";
				break;

			case DEBUG:
				result = "DEBUG";
				break;

			case TRACE:
				result = "TRACE";
				break;

			case ALL:
				result = "ALL";
				break;

			default:
				throw new IllegalArgumentException( String.valueOf( level ) );
		}

		return result;
	}

	/*
	 * Static initializer sets up log targets.
	 */
	static
	{
		/*
		 * Use 'asynchronous.logging'
		 */
		try
		{
			asynchronousLogging = "true".equals( System.getProperty( "asynchronous.logging" ) );
		}
		catch ( final SecurityException e )
		{
			/* ignore no access to system property */
		}

		/*
		 * Add {@link Log4jTarget} if Log4j classes are available and a
		 * configuration file (default: 'log4j.xml') is defined.
		 */
		final String l4jLoader = "org.apache.log4j.helpers.Loader";
		final String l4jOptionConverter = "org.apache.log4j.helpers.OptionConverter";

		Object l4jURL;

		final String l4jConfigurationOption = (String)invokeStatic( l4jOptionConverter, "getSystemProperty", new Class<?>[] { String.class, String.class }, new Object[] { "log4j.configuration", null } );
		if ( l4jConfigurationOption == null )
		{
			l4jURL = invokeStatic( l4jLoader, "getResource", String.class, "log4j.xml" );
			if ( l4jURL == null )
			{
				l4jURL = invokeStatic( l4jLoader, "getResource", String.class, "log4j.properties" );
			}
		}
		else
		{
			try
			{
				l4jURL = new URL( l4jConfigurationOption );
			}
			catch ( final MalformedURLException ignored )
			{
				l4jURL = invokeStatic( l4jLoader, "getResource", String.class, l4jConfigurationOption );
			}
		}

		if ( l4jURL != null )
		{
			final Package thisPackage = ClassLogger.class.getPackage();
			final String className = thisPackage.getName() + ".Log4jTarget";

			final LogTarget log4jTarget = (LogTarget)invokeStatic( className, null, new Class<?>[ 0 ], new Object[ 0 ] );
			if ( log4jTarget != null )
			{
				addTarget( log4jTarget );
			}
		}

		/*
		 * Add {@link LogFileTarget} if system properties are set.
		 */
		final LogFileTarget logFileTarget = LogFileTarget.getDefaultInstance();
		if ( logFileTarget != null )
		{
			addTarget( logFileTarget );
		}

		/*
		 * Add {@link LogServer} if system properties are set.
		 */
		final LogServer logServer = LogServer.getDefaultInstance();
		if ( logServer != null )
		{
			addTarget( logServer );
		}

		/*
		 * Add {@link JdkLoggingTarget} if system property is set.
		 */
		if ( Boolean.parseBoolean( System.getProperty( "jdk.logger" ) ) )
		{
			addTarget( new JdkLoggingTarget() );
		}

		/*
		 * Add {@link ConsoleTarget} if system properties are set, or
		 * if no log target has been added yet.
		 */
		final ConsoleTarget consoleTarget = new ConsoleTarget( LOG_TARGETS.isEmpty() ? INFO : NONE, System.err );
		if ( consoleTarget.getLevel() > NONE )
		{
			addTarget( new ConsoleTarget() );
		}
	}

	/**
	 * Utility method to call a static method using reflection.
	 *
	 * @param className  Fully qualified name of class whose method to invoke.
	 * @param methodName Name of method to invoke.
	 * @param argType    First and only argument type.
	 * @param argValue   First and only argument value.
	 *
	 * @return Result from invoked method ({@code null} if void).
	 */
	@Nullable
	private static Object invokeStatic( final String className, final String methodName, final Class<?> argType, final Object argValue )
	{
		return invokeStatic( className, methodName, new Class<?>[] { argType }, new Object[] { argValue } );
	}

	/**
	 * Utility method to call a static method using reflection.
	 *
	 * @param className  Fully qualified name of class whose method to invoke.
	 * @param methodName Name of method to invoke.
	 * @param argTypes   Argument types.
	 * @param argValues  Argument values.
	 *
	 * @return Result from invoked method ({@code null} if void).
	 */
	@Nullable
	private static Object invokeStatic( final String className, final String methodName, final Class<?>[] argTypes, final Object[] argValues )
	{
		Object result = null;

		try
		{
			final Class<?> theClass = Class.forName( className );

			if ( methodName == null )
			{
				final Constructor<?> constructor = theClass.getConstructor( argTypes );
				result = constructor.newInstance( argValues );
			}
			else
			{
				final Method method = theClass.getMethod( methodName, argTypes );
				result = method.invoke( null, argValues );
			}
		}
		catch ( final ClassNotFoundException e )
		{ /* ignore */ }
		catch ( final InstantiationException e )
		{ /* ignore */ }
		catch ( final IllegalAccessException e )
		{ /* ignore */ }
		catch ( final InvocationTargetException e )
		{ /* ignore */ }
		catch ( final NoSuchMethodException e )
		{ /* ignore */ }

		return result;
	}

	/**
	 * Create logger with the specified name.
	 *
	 * @param name Log name (e.g. class name).
	 */
	ClassLogger( final String name )
	{
		_name = name;
	}

	/**
	 * Get name of this logger.
	 *
	 * @return Name of logger ( e.g. class name).
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Test if the specified log level is enabled. This is true if the level is
	 * enabled for at least one of the registered log targets.
	 *
	 * @param level Log level.
	 *
	 * @return {@code true} if the log level is enabled; {@code false} if the
	 * log level is disabled.
	 */
	public boolean isLevelEnabled( final int level )
	{
		return isLevelEnabled( getName(), level );
	}

	/**
	 * Send log message with an associated throwable object to all registered
	 * log targets.
	 *
	 * @param level     Log level.
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void log( final int level, final String message, final Throwable throwable )
	{
		log( getName(), level, message, throwable );
	}

	/**
	 * Send log message with an associated throwable object to all registered
	 * log targets.
	 *
	 * @param level     Log level.
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void log( final int level, final MessageSupplier message, final Throwable throwable )
	{
		log( getName(), level, message, throwable );
	}

	/**
	 * Send log message with an associated throwable object to all registered
	 * log targets.
	 *
	 * @param name      Name of log.
	 * @param level     Log level.
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	static void log( final String name, final int level, final String message, final Throwable throwable )
	{
		if ( isLevelEnabled( name, level ) )
		{
			logImpl( name, level, throwable, message );
		}
	}

	/**
	 * Send log message with an associated throwable object to all registered
	 * log targets.
	 *
	 * @param name      Name of log.
	 * @param level     Log level.
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	private static void log( final String name, final int level, final MessageSupplier message, final Throwable throwable )
	{
		if ( isLevelEnabled( name, level ) )
		{
			logImpl( name, level, throwable, ( message != null ) ? message.get() : null );
		}
	}

	/**
	 * Send log message with an associated throwable object to all registered
	 * log targets. This method does not check whether the given level.
	 *
	 * @param name      Name of log.
	 * @param level     Log level.
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	private static void logImpl( final String name, final int level, final Throwable throwable, final String message )
	{
		final Thread currentThread = Thread.currentThread();
		final String threadName = currentThread.getName();

		if ( asynchronousLogging )
		{
			ExecutorService executor = executorService;
			if ( executor == null )
			{
				final DefaultThreadFactory threadFactory = new DefaultThreadFactory();
				threadFactory.setNamePrefix( ClassLogger.class.getName() );
				threadFactory.setDaemon( true );

				executor = Executors.newSingleThreadExecutor( threadFactory );
				executorService = executor;
			}

			executor.submit( new LogTask( name, level, message, throwable, threadName ) );
		}
		else
		{
			synchronized ( LOG_TARGETS )
			{
				for ( final LogTarget target : LOG_TARGETS )
				{
					target.log( name, level, message, throwable, threadName );
				}
			}
		}
	}

	/**
	 * Test if the specified log level is enabled. This is true if the level is
	 * enabled for at least one of the registered log targets.
	 *
	 * @param name  Name of log (e.g. class name).
	 * @param level Log level.
	 *
	 * @return {@code true} if the log level is enabled; {@code false} if the
	 * log level is disabled.
	 */
	public static boolean isLevelEnabled( final String name, final int level )
	{
		boolean result = false;

		synchronized ( LOG_TARGETS )
		{
			for ( final LogTarget target : LOG_TARGETS )
			{
				if ( target.isLevelEnabled( name, level ) )
				{
					result = true;
					break;
				}
			}
		}

		return result;
	}

	/**
	 * Check if 'fatal' log messages are enabled.
	 *
	 * @return {@code true} if 'fatal' log messages are enabled; {@code false}
	 * otherwise.
	 */
	public boolean isFatalEnabled()
	{
		return isLevelEnabled( FATAL );
	}

	/**
	 * Check if 'error' log messages are enabled.
	 *
	 * @return {@code true} if 'error' log messages are enabled; {@code false}
	 * otherwise.
	 */
	public boolean isErrorEnabled()
	{
		return isLevelEnabled( ERROR );
	}

	/**
	 * Check if 'warn' log messages are enabled.
	 *
	 * @return {@code true} if 'warn' log messages are enabled; {@code false}
	 * otherwise.
	 */
	public boolean isWarnEnabled()
	{
		return isLevelEnabled( WARN );
	}

	/**
	 * Check if 'info' log messages are enabled.
	 *
	 * @return {@code true} if 'info' log messages are enabled; {@code false}
	 * otherwise.
	 */
	public boolean isInfoEnabled()
	{
		return isLevelEnabled( INFO );
	}

	/**
	 * Check if 'debug' log messages are enabled.
	 *
	 * @return {@code true} if 'debug' log messages are enabled; {@code false}
	 * otherwise.
	 */
	public boolean isDebugEnabled()
	{
		return isLevelEnabled( DEBUG );
	}

	/**
	 * Check if 'trace' log messages are enabled.
	 *
	 * @return {@code true} if 'trace' log messages are enabled; {@code false}
	 * otherwise.
	 */
	public boolean isTraceEnabled()
	{
		return isLevelEnabled( TRACE );
	}

	/**
	 * Log 'fatal' message.
	 *
	 * @param message Log message.
	 */
	public void fatal( final String message )
	{
		fatal( message, null );
	}

	/**
	 * Log 'fatal' message.
	 *
	 * @param message Log message.
	 */
	public void fatal( final MessageSupplier message )
	{
		fatal( message, null );
	}

	/**
	 * Log 'error' message.
	 *
	 * @param message Log message.
	 */
	public void error( final String message )
	{
		error( message, null );
	}

	/**
	 * Log 'error' message.
	 *
	 * @param message Log message.
	 */
	public void error( final MessageSupplier message )
	{
		error( message, null );
	}

	/**
	 * Log 'warn' message.
	 *
	 * @param message Log message.
	 */
	public void warn( final String message )
	{
		warn( message, null );
	}

	/**
	 * Log 'warn' message.
	 *
	 * @param message Log message.
	 */
	public void warn( final MessageSupplier message )
	{
		warn( message, null );
	}

	/**
	 * Log 'info' message.
	 *
	 * @param message Log message.
	 */
	public void info( final String message )
	{
		info( message, null );
	}

	/**
	 * Log 'info' message.
	 *
	 * @param message Log message.
	 */
	public void info( final MessageSupplier message )
	{
		info( message, null );
	}

	/**
	 * Log 'debug' message.
	 *
	 * @param message Log message.
	 */
	public void debug( final String message )
	{
		debug( message, null );
	}

	/**
	 * Log 'debug' message.
	 *
	 * @param message Log message.
	 */
	public void debug( final MessageSupplier message )
	{
		debug( message, null );
	}

	/**
	 * Log 'trace' message.
	 *
	 * @param message Log message.
	 */
	public void trace( final String message )
	{
		trace( message, null );
	}

	/**
	 * Log 'trace' message.
	 *
	 * @param message Log message.
	 */
	public void trace( final MessageSupplier message )
	{
		trace( message, null );
	}

	/**
	 * Log 'fatal' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void fatal( final String message, final Throwable throwable )
	{
		log( FATAL, message, throwable );
	}

	/**
	 * Log 'fatal' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void fatal( final MessageSupplier message, final Throwable throwable )
	{
		log( FATAL, message, throwable );
	}

	/**
	 * Log 'error' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void error( final String message, final Throwable throwable )
	{
		log( ERROR, message, throwable );
	}

	/**
	 * Log 'error' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void error( final MessageSupplier message, final Throwable throwable )
	{
		log( ERROR, message, throwable );
	}

	/**
	 * Log 'warn' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void warn( final String message, final Throwable throwable )
	{
		log( WARN, message, throwable );
	}

	/**
	 * Log 'warn' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void warn( final MessageSupplier message, final Throwable throwable )
	{
		log( WARN, message, throwable );
	}

	/**
	 * Log 'info' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void info( final String message, final Throwable throwable )
	{
		log( INFO, message, throwable );
	}

	/**
	 * Log 'info' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void info( final MessageSupplier message, final Throwable throwable )
	{
		log( INFO, message, throwable );
	}

	/**
	 * Log 'debug' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void debug( final String message, final Throwable throwable )
	{
		log( DEBUG, message, throwable );
	}

	/**
	 * Log 'debug' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void debug( final MessageSupplier message, final Throwable throwable )
	{
		log( DEBUG, message, throwable );
	}

	/**
	 * Log 'trace' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void trace( final String message, final Throwable throwable )
	{
		log( TRACE, message, throwable );
	}

	/**
	 * Log 'trace' message with an associated throwable object.
	 *
	 * @param message   Log message.
	 * @param throwable Throwable associated with log message.
	 */
	public void trace( final MessageSupplier message, final Throwable throwable )
	{
		log( TRACE, message, throwable );
	}

	/**
	 * Log method entry.
	 *
	 * @param methodName Name of entered method.
	 */
	public void entering( final String methodName )
	{
		if ( isTraceEnabled() )
		{
			entering( methodName, (Object[])null );
		}
	}

	/**
	 * Log method entry.
	 *
	 * @param methodName Name of entered method.
	 * @param args       Method arguments.
	 */
	public void entering( final String methodName, final Object... args )
	{
		if ( isTraceEnabled() )
		{
			final StringBuffer sb = new StringBuffer();
			sb.append( ">>> " );
			appendClassName( sb, getName() );
			sb.append( '.' );
			sb.append( methodName );
			sb.append( '(' );

			if ( args != null )
			{
				for ( int i = 0; i < args.length; i++ )
				{
					sb.append( ( i > 0 ) ? ", " : " " );
					appendValue( sb, args[ i ] );
					sb.append( ' ' );
				}
			}

			sb.append( ')' );
			trace( sb.toString(), null );
		}
	}

	/**
	 * Log method exit.
	 *
	 * @param methodName Name of exited method.
	 */
	public void exiting( final String methodName )
	{
		if ( isTraceEnabled() )
		{
			final StringBuffer sb = new StringBuffer();
			sb.append( "<<< " );
			appendClassName( sb, getName() );
			sb.append( '.' );
			sb.append( methodName );
			trace( sb.toString(), null );
		}
	}

	/**
	 * Log method exit with result value.
	 *
	 * @param methodName Name of exited method.
	 * @param result     Result value.
	 * @param <T>        Return type.
	 *
	 * @return Result value (returned as-is).
	 */
	public <T> T exiting( final String methodName, final T result )
	{
		if ( isTraceEnabled() )
		{
			final StringBuffer sb = new StringBuffer();
			sb.append( "<<< " );
			appendClassName( sb, getName() );
			sb.append( '.' );
			sb.append( methodName );
			sb.append( " returned " );
			appendValue( sb, result );
			trace( sb.toString(), null );
		}

		return result;
	}

	/**
	 * Log method exit with result value.
	 *
	 * @param methodName Name of exited method.
	 * @param result     Result value.
	 *
	 * @return Result value (returned as-is).
	 */
	public boolean exiting( final String methodName, final boolean result )
	{
		if ( isTraceEnabled() )
		{
			exiting( methodName, result ? Boolean.TRUE : Boolean.FALSE );
		}

		return result;
	}

	/**
	 * Log method exit with result value.
	 *
	 * @param methodName Name of exited method.
	 * @param result     Result value.
	 *
	 * @return Result value (returned as-is).
	 */
	public int exiting( final String methodName, final int result )
	{
		if ( isTraceEnabled() )
		{
			exiting( methodName, Integer.valueOf( result ) );
		}

		return result;
	}

	/**
	 * Log method exit with result value.
	 *
	 * @param methodName Name of exited method.
	 * @param result     Result value.
	 *
	 * @return Result value (returned as-is).
	 */
	public long exiting( final String methodName, final long result )
	{
		if ( isTraceEnabled() )
		{
			exiting( methodName, Long.valueOf( result ) );
		}

		return result;
	}

	/**
	 * Log method exit with result value.
	 *
	 * @param methodName Name of exited method.
	 * @param result     Result value.
	 *
	 * @return Result value (returned as-is).
	 */
	public float exiting( final String methodName, final float result )
	{
		if ( isTraceEnabled() )
		{
			exiting( methodName, new Float( result ) );
		}

		return result;
	}

	/**
	 * Log method exit with result value.
	 *
	 * @param methodName Name of exited method.
	 * @param result     Result value.
	 *
	 * @return Result value (returned as-is).
	 */
	public double exiting( final String methodName, final double result )
	{
		if ( isTraceEnabled() )
		{
			exiting( methodName, new Double( result ) );
		}

		return result;
	}

	/**
	 * Log throwable to be thrown.
	 *
	 * @param methodName Method from which the exception is thrown.
	 * @param throwable  Throwable that is thrown.
	 * @param <T>        Throwable type.
	 *
	 * @return Throwable that is thrown (returned as-is).
	 */
	public <T extends Throwable> T throwing( final String methodName, final T throwable )
	{
		return throwing( TRACE, methodName, throwable );
	}

	/**
	 * Log throwable to be thrown.
	 *
	 * @param logLevel   Level to log message at.
	 * @param methodName Method from which the exception is thrown.
	 * @param throwable  Throwable that is thrown.
	 * @param <T>        Throwable type.
	 *
	 * @return Throwable that is thrown (returned as-is).
	 */
	public <T extends Throwable> T throwing( final int logLevel, final String methodName, final T throwable )
	{
		if ( isLevelEnabled( logLevel ) )
		{
			final Class<?> thowableClass = throwable.getClass();

			final StringBuffer sb = new StringBuffer();
			sb.append( "!!! " );
			appendClassName( sb, getName() );
			sb.append( '.' );
			sb.append( methodName );
			sb.append( " threw " );
			appendClassName( sb, thowableClass.getName() );
			sb.append( "( \"" );
			sb.append( throwable.getMessage() );
			sb.append( "\" )" );
			log( logLevel, sb.toString(), throwable );
		}

		return throwable;
	}

	/**
	 * Append object value to the string buffer. This method tries to provide
	 * the best suitable string representation of the object (e.g. recognizing
	 * arrays, primitive types, strings, etc).
	 *
	 * @param sb    String buffer to append to.
	 * @param value Value to print.
	 */
	private static void appendValue( final StringBuffer sb, final Object value )
	{
		if ( value == null )
		{
			sb.append( "null" );
		}
		else if ( value instanceof String )
		{
			sb.append( '"' );
			sb.append( (String)value );
			sb.append( '"' );
		}
		else if ( ( value instanceof Boolean )
		          || ( value instanceof Number ) )
		{
			sb.append( value );
		}
		else if ( value instanceof Enum )
		{
			final Enum<?> enumValue = (Enum<?>)value;
			final Class<?> enumClass = enumValue.getDeclaringClass();
			sb.append( enumClass.getName() );
			sb.append( '.' );
			sb.append( enumValue.name() );
		}
		else
		{
			final Class<?> argClass = value.getClass();
			if ( argClass.isPrimitive() )
			{
				sb.append( value );
			}
			else
			{
				if ( argClass.isArray() )
				{
					appendClassName( sb, argClass.getComponentType() );
					sb.append( "[ " );
					sb.append( Array.getLength( value ) );
					sb.append( " ]" );
				}
				else
				{
					appendClassName( sb, argClass );
					if ( value instanceof Collection )
					{
						sb.append( "(size=" );
						sb.append( ( (Collection<?>)value ).size() );
						sb.append( ')' );
					}
				}
			}
		}
	}

	/**
	 * Append name of the specified class to the string buffer. Note that only
	 * the class name (not the package name) is included. Arrays are handled by
	 * appending '[]' for each dimension.
	 *
	 * @param sb     String buffer to append to.
	 * @param aClass Class whose name should be appended.
	 */
	private static void appendClassName( final StringBuffer sb, final Class<?> aClass )
	{
		if ( aClass.isArray() )
		{
			appendClassName( sb, aClass.getComponentType() );
			sb.append( "[]" );
		}
		else
		{
			appendClassName( sb, aClass.getName() );
		}
	}

	/**
	 * Append the class name from the specified fully qualified class name to
	 * the string buffer. The package name is stripped from the class name.
	 *
	 * @param sb            String buffer to append to.
	 * @param fullClassName Fully qualified class name whose name to append.
	 */
	private static void appendClassName( final StringBuffer sb, final String fullClassName )
	{
		final int len = fullClassName.length();
		int pos = fullClassName.lastIndexOf( (int)'.' ) + 1;
		if ( pos <= 0 || pos == len )
		{
			sb.append( fullClassName );
		}
		else
		{
			sb.ensureCapacity( sb.length() + len - pos );
			for ( ; pos < len; pos++ )
			{
				sb.append( fullClassName.charAt( pos ) );
			}
		}
	}

	/**
	 * This task sends a log messages to all registered log targets.
	 */
	private static class LogTask
	implements Runnable
	{
		/**
		 * Name of log.
		 */
		private final String _name;

		/**
		 * Log level.
		 */
		private final int _level;

		/**
		 * Log message.
		 */
		private final String _message;

		/**
		 * Throwable associated with log message.
		 */
		private final Throwable _throwable;

		/**
		 * Identifies the thread that produced the log message.
		 */
		private final String _threadName;

		/**
		 * Create task.
		 *
		 * @param name       Name of log.
		 * @param level      Log level.
		 * @param message    Log message.
		 * @param throwable  Throwable associated with log message.
		 * @param threadName Identifies the thread that produced the log
		 *                   message.
		 */
		private LogTask( final String name, final int level, final String message, final Throwable throwable, final String threadName )
		{
			_name = name;
			_level = level;
			_message = message;
			_throwable = throwable;
			_threadName = threadName;
		}

		@Override
		public void run()
		{
			synchronized ( LOG_TARGETS )
			{
				for ( final LogTarget target : LOG_TARGETS )
				{
					target.log( _name, _level, _message, _throwable, _threadName );
				}
			}
		}
	}

	/**
	 * Interface to provide a message string.
	 */
	//@FunctionalInterface
	public interface MessageSupplier
	{
		/**
		 * Returns message.
		 *
		 * @return Message.
		 */
		String get();
	}
}
