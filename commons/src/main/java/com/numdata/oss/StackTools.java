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
package com.numdata.oss;

import java.io.*;
import javax.swing.text.html.*;

import org.jetbrains.annotations.*;

/**
 * This class defines utility methods to work on stack traces.
 *
 * @author Peter S. Heijnen
 */
public class StackTools
{
	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private StackTools()
	{
	}

	/**
	 * Append stack trace from {@link Throwable} to a {@link StringBuffer}. The
	 * stack trace can be filtered using the {@code stopTraceAt} and {@code
	 * excludeJavaAPI} arguments. If {@code throwable} is {@code null}, this
	 * method has no effect.
	 *
	 * @param sb             Buffer to append stack trace to.
	 * @param throwable      Throwable object to get stack trace from.
	 * @param stopTraceAt    Stop stack trace when this class is encountered
	 *                       ({@code null} to always show whole stack).
	 * @param excludeJavaAPI Exclude Java API classes from stack trace.
	 */
	public static void appendStackTrace( @NotNull final StringBuilder sb, @Nullable final Throwable throwable, @Nullable final Class<?> stopTraceAt, final boolean excludeJavaAPI )
	{
		if ( throwable != null )
		{
			sb.append( throwable );
			sb.append( '\n' );

			final StackTraceElement[] trace = createTrace( throwable, 0, stopTraceAt, excludeJavaAPI );
			for ( final StackTraceElement element : trace )
			{
				sb.append( "\tat " );
				sb.append( element );
				sb.append( '\n' );
			}

			StackTraceElement[] effectTrace = trace;
			for ( Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause() )
			{
				sb.append( "Caused by: " );
				sb.append( cause );
				sb.append( '\n' );

				final StackTraceElement[] causeTrace = createTrace( cause, 0, stopTraceAt, excludeJavaAPI );

				final int commonFrames = getCommonFrameCount( causeTrace, effectTrace );

				for ( int i = 0; i < causeTrace.length - commonFrames; i++ )
				{
					sb.append( "\tat " );
					sb.append( causeTrace[ i ] );
					sb.append( '\n' );
				}

				if ( commonFrames != 0 )
				{
					sb.append( "\t... " );
					sb.append( commonFrames );
					sb.append( " more" );
					sb.append( '\n' );
				}

				effectTrace = causeTrace;
			}
		}
	}

	/**
	 * Create a new stack trace for the specified {@link Throwable} object. The
	 * stack trace can be filtered using the {@code stopTraceAt} and {@code
	 * excludeJavaAPI} arguments.
	 *
	 * @param throwable      Throwable to get stack trace from.
	 * @param startIndex     Stack element index to start trace at.
	 * @param stopTraceAt    Stop stack trace when this class is encountered
	 *                       ({@code null} to always show whole stack).
	 * @param excludeJavaAPI Exclude Java API classes from stack trace.
	 *
	 * @return Stack trace (first element is the most recent entry).
	 */
	public static StackTraceElement[] createTrace( final Throwable throwable, final int startIndex, @Nullable final Class<?> stopTraceAt, final boolean excludeJavaAPI )
	{
		final StackTraceElement[] result;

		final StackTraceElement[] fullTrace = throwable.getStackTrace();

		int endIndex = fullTrace.length;
		if ( excludeJavaAPI )
		{
			while ( endIndex > startIndex )
			{
				final String className = fullTrace[ --endIndex ].getClassName();

				if ( !className.startsWith( "java." )
				     && !className.startsWith( "javax.swing." )
				     && !className.startsWith( "sun." ) )
				{
					endIndex++;
					break;
				}
			}
		}

		if ( stopTraceAt != null )
		{
			final String stopAtName = stopTraceAt.getName();

			for ( int i = endIndex; --i >= startIndex; )
			{
				final String className = fullTrace[ i ].getClassName();

				if ( stopAtName.equals( className ) )
				{
					endIndex = i + 1;
					break;
				}
			}
		}

		if ( ( startIndex == 0 ) && ( endIndex == fullTrace.length ) )
		{
			result = fullTrace;
		}
		else
		{
			result = new StackTraceElement[ endIndex - startIndex ];
			System.arraycopy( fullTrace, startIndex, result, 0, endIndex - startIndex );
		}

		return result;
	}

	/**
	 * Get number of frames in the specified stack trace that are the same as
	 * another stack trace. The trace is compared back to front.
	 *
	 * @param trace Stack trace to analyse.
	 * @param other Stack trace to compare with.
	 *
	 * @return Number of trailing frames in {@code trace} that are the same as
	 * {@code other}; {@code 0} if no frames are the same.
	 */
	private static int getCommonFrameCount( final StackTraceElement[] trace, final StackTraceElement[] other )
	{
		final int framesInCommon;
		{
			int m = trace.length - 1;
			int n = other.length - 1;

			while ( ( m >= 0 ) && ( n >= 0 ) && trace[ m ].equals( other[ n ] ) )
			{
				m--;
				n--;
			}

			framesInCommon = trace.length - 1 - m;
		}
		return framesInCommon;
	}

	/**
	 * Get short-form description for a stack trace element. This simply takes
	 * the output of {@link StackTraceElement#toString()}, and removed the
	 * package name from it.
	 *
	 * @param element Stack trace element to describe.
	 *
	 * @return Short-form description.
	 */
	public static String getShortDescription( final StackTraceElement element )
	{
		final String className = element.getClassName();
		final String toString = element.toString();

		return toString.substring( className.lastIndexOf( (int)'.' ) );
	}

	/**
	 * Get stack trace from {@link Throwable} object as string. The stack trace
	 * can be filtered using the {@code stopTraceAt} and {@code excludeJavaAPI}
	 * arguments.
	 *
	 * @param throwable      Throwable object to get stack trace from.
	 * @param stopTraceAt    Stop stack trace when this class is encountered
	 *                       ({@code null} to always show whole stack).
	 * @param excludeJavaAPI Exclude Java API classes from stack trace.
	 *
	 * @return Stack trace message; {@code null} if {@code throwable} is {@code
	 * null}.
	 */
	@Nullable
	public static String getStackTrace( @Nullable final Throwable throwable, @Nullable final Class<?> stopTraceAt, final boolean excludeJavaAPI )
	{
		final String result;

		if ( throwable != null )
		{
			final StringBuilder sb = new StringBuilder();
			appendStackTrace( sb, throwable, stopTraceAt, excludeJavaAPI );
			result = sb.toString();
		}
		else
		{
			result = null;
		}

		return result;
	}

	/**
	 * Print stack trace to the specified {@link PrintStream}. The stack trace
	 * can be filtered using the {@code stopTraceAt} and {@code excludeJavaAPI}
	 * arguments.
	 *
	 * @param ps             {@link PrintStream} to print stack trace to.
	 * @param throwable      Throwable object to print stack trace of.
	 * @param stopTraceAt    Stop stack trace when this class is encountered
	 *                       ({@code null} to always show whole stack).
	 * @param excludeJavaAPI Exclude Java API classes from stack trace.
	 */
	public static void printStackTrace( @NotNull final PrintStream ps, @NotNull final Throwable throwable, @Nullable final Class<?> stopTraceAt, final boolean excludeJavaAPI )
	{
		ps.println( throwable );

		final StackTraceElement[] trace = createTrace( throwable, 0, stopTraceAt, excludeJavaAPI );
		for ( final StackTraceElement element : trace )
		{
			ps.print( "\tat " );
			ps.println( element );
		}

		StackTraceElement[] effectTrace = trace;
		for ( Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause() )
		{
			ps.print( "Caused by: " );
			ps.println( cause );

			final StackTraceElement[] causeTrace = createTrace( cause, 0, stopTraceAt, excludeJavaAPI );

			final int commonFrames = getCommonFrameCount( causeTrace, effectTrace );

			for ( int i = 0; i < causeTrace.length - commonFrames; i++ )
			{
				ps.print( "\tat " );
				ps.println( causeTrace[ i ] );
			}

			if ( commonFrames != 0 )
			{
				ps.print( "\t... " );
				ps.print( commonFrames );
				ps.println( " more" );
			}

			effectTrace = causeTrace;
		}
	}

	/**
	 * Print stack trace to the specified {@link PrintWriter}. The stack trace
	 * can be filtered using the {@code stopTraceAt} and {@code excludeJavaAPI}
	 * arguments.
	 *
	 * @param pw             {@link PrintWriter} to print stack trace to.
	 * @param throwable      Throwable object to print stack trace of.
	 * @param stopTraceAt    Stop stack trace when this class is encountered
	 *                       ({@code null} to always show whole stack).
	 * @param excludeJavaAPI Exclude Java API classes from stack trace.
	 */
	public static void printStackTrace( @NotNull final PrintWriter pw, @NotNull final Throwable throwable, @Nullable final Class<?> stopTraceAt, final boolean excludeJavaAPI )
	{
		pw.println( throwable );

		final StackTraceElement[] trace = createTrace( throwable, 0, stopTraceAt, excludeJavaAPI );
		for ( final StackTraceElement element : trace )
		{
			pw.print( "\tat " );
			pw.println( element );
		}

		StackTraceElement[] effectTrace = trace;
		for ( Throwable cause = throwable.getCause(); cause != null; cause = cause.getCause() )
		{
			pw.print( "Caused by: " );
			pw.println( cause );

			final StackTraceElement[] causeTrace = createTrace( cause, 0, stopTraceAt, excludeJavaAPI );

			final int commonFrames = getCommonFrameCount( causeTrace, effectTrace );

			for ( int i = 0; i < causeTrace.length - commonFrames; i++ )
			{
				pw.print( "\tat " );
				pw.println( causeTrace[ i ] );
			}

			if ( commonFrames != 0 )
			{
				pw.print( "\t... " );
				pw.print( commonFrames );
				pw.println( " more" );
			}

			effectTrace = causeTrace;
		}
	}

	/**
	 * Print stack trace to {@link System#err}.
	 *
	 * @param throwable Throwable object to print stack trace of.
	 */
	public static void printStackTrace( @NotNull final Throwable throwable )
	{
		printStackTrace( System.err, throwable, null, true );
	}

	/**
	 * Prints the stack trace for the given throwable to the given appendable
	 * character sequence.
	 *
	 * @param out       Character sequence to append to.
	 * @param throwable Throwable to get the stack trace from.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void printStackTraceAsHTML( @NotNull final Appendable out, @NotNull final Throwable throwable )
	throws IOException
	{
		printStackTraceAsHTML( out, throwable, null );
	}

	/**
	 * Prints the stack trace for the given throwable to the given appendable
	 * character sequence.
	 *
	 * @param out         Character sequence to append to.
	 * @param throwable   Throwable to get the stack trace from.
	 * @param stopTraceAt Stop stack trace when this class is encountered
	 *                    ({@code null} to always show whole stack).
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public static void printStackTraceAsHTML( @NotNull final Appendable out, @NotNull final Throwable throwable, @Nullable final Class<?> stopTraceAt )
	throws IOException
	{
		StackTraceElement[] causedTrace = null;

		Throwable cause = throwable;
		do
		{
			final StackTraceElement[] trace = cause.getStackTrace();

			int m = trace.length - 1;

			//noinspection ObjectEquality
			if ( cause != throwable )
			{
				out.append( '\n' );
				out.append( "Caused by: " );
				int n = causedTrace.length - 1;

				while ( ( m >= 0 ) && ( n >= 0 ) && trace[ m ].equals( causedTrace[ n ] ) )
				{
					m--;
					n--;
				}
			}
			out.append( cause.toString() );
			out.append( '\n' );

			final int framesInCommon = trace.length - 1 - m;

			for ( int i = 0; i <= m; i++ )
			{
				final StackTraceElement element = trace[ i ];

				final String className = element.getClassName();
				if ( stopTraceAt != null && className.equals( stopTraceAt.getName() ) )
				{
					break;
				}
				final int firstDollar = className.indexOf( (int)'$' );
				final int classDot = className.lastIndexOf( (int)'.', ( firstDollar == -1 ) ? className.length() : firstDollar );

				out.append( "\tat " );
				if ( classDot > 0 )
				{
					out.append( "<span class='package'>" );
					out.append( className.substring( 0, classDot ) );
					out.append( "</span>." );
				}
				out.append( "<span class='class'>" );
				out.append( className.substring( classDot + 1 ) );
				out.append( "</span>.<span class='method'>" );
				HTMLTools.writeText( out, element.getMethodName() );
				out.append( "</span>" );

				out.append( '(' );
				if ( element.isNativeMethod() )
				{
					out.append( "<span class='native'>Native Method</span>" );
				}
				else
				{
					out.append( "<span class='file'>" );
					out.append( element.getFileName() );
					out.append( "</span>" );
					out.append( ':' );
					out.append( "<span class='line'>" );
					out.append( String.valueOf( element.getLineNumber() ) );
					out.append( "</span>" );
				}
				out.append( ")\n" );
			}

			if ( framesInCommon != 0 )
			{
				out.append( "\t... " ).append( String.valueOf( framesInCommon ) ).append( " more\n" );
			}

			causedTrace = trace;
			cause = cause.getCause();
		}
		while ( cause != null );
	}

	/**
	 * Adds rules to the given style sheet to style stack traces generated by
	 * {@link #printStackTraceAsHTML}.
	 *
	 * @param styleSheet Style sheet to be updated.
	 */
	public static void addStackTraceStyles( final StyleSheet styleSheet )
	{
		styleSheet.addRule( ".method, .line { font-weight: bold; }" );
		styleSheet.addRule( ".package { color: #444444; }" );
		styleSheet.addRule( ".class, .method { color: #0000cc; }" );
		styleSheet.addRule( ".line { color: #cc0000; }" );
	}

	/**
	 * Call {@link Throwable#toString} for the given {@link Throwable} including
	 * its cause(s).
	 *
	 * @param throwable Throwable to convert te string.
	 *
	 * @return String representation of {@code throwable} including cause(s).
	 */
	public static String toStringIncludingCauses( final Throwable throwable )
	{
		final String result;

		Throwable cause = throwable.getCause();
		if ( cause == null )
		{
			result = throwable.toString();
		}
		else
		{
			final StringBuilder sb = new StringBuilder();
			sb.append( throwable );

			while ( cause != null )
			{
				sb.append( " caused by: " );
				sb.append( cause );
				cause = cause.getCause();
			}

			result = sb.toString();
		}

		return result;
	}
}
