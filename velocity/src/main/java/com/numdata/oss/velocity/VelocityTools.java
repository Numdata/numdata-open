/*
 * Copyright (c) 2010-2021, Unicon Creation BV, The Netherlands.
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
package com.numdata.oss.velocity;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import com.numdata.oss.*;
import com.numdata.oss.log.*;
import org.apache.velocity.context.*;
import org.apache.velocity.exception.*;
import org.apache.velocity.runtime.*;
import org.apache.velocity.tools.*;
import org.apache.velocity.tools.generic.*;
import org.jetbrains.annotations.*;

/**
 * Provides tools for using Apache Velocity.
 *
 * @author Peter S. Heijnen
 */
public class VelocityTools
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( VelocityTools.class );

	/**
	 * Pattern to parse line and column numbers in velocity exception message.
	 */
	private static final Pattern VELOCITY_LOG_FILE_POSITION_PATTERN = Pattern.compile( "\\[line (\\d+), column (\\d+)]" );

	/**
	 * Pattern matching any common line ending.
	 */
	private static final Pattern END_OF_LINE = Pattern.compile( "\r\n|\r|\n" );

	/**
	 * Shared engine instance. Created on-demand by {@link
	 * #getSharedRuntime()}.
	 */
	private static RuntimeInstance sharedRuntime = null;

	/**
	 * Shared {@link ToolManager} for Velocity Tools.
	 */
	private static ToolManager toolManager = null;

	/**
	 * Utility/Application class is not supposed to be instantiated.
	 */
	private VelocityTools()
	{
	}

	/**
	 * Get shared Velocity {@link RuntimeInstance}.
	 *
	 * @return Shared Velocity {@link RuntimeInstance}.
	 */
	public static RuntimeInstance getSharedRuntime()
	{
		RuntimeInstance engine = sharedRuntime;
		if ( engine == null )
		{
			engine = createRuntime();
			sharedRuntime = engine;

			try
			{
				engine.init();
			}
			catch ( final Exception e )
			{
				LOG.error( "Failed to initialize velocity engine: " + e, e );
			}
		}

		return engine;
	}

	/**
	 * Get shared {@link ToolManager Velocity Tools manager}.
	 *
	 * @return {@link ToolManager Velocity Tools manager}.
	 */
	@NotNull
	public static ToolManager getSharedToolManager()
	{
		ToolManager toolManager = VelocityTools.toolManager;
		if ( toolManager == null )
		{
			toolManager = new ToolManager();
			VelocityTools.toolManager = toolManager;
		}
		return toolManager;
	}

	/**
	 * Construct Velocity context.
	 *
	 * @return Velocity context.
	 */
	@NotNull
	public static Context creatContext()
	{
		final ToolManager toolManager = getSharedToolManager();
		final ToolContext result = toolManager.createContext();
		result.put( "date", new DateTool() );
		return result;
	}

	/**
	 * Create a new Velocity {@link RuntimeInstance}.
	 *
	 * @return Velocity {@link RuntimeInstance}.
	 */
	@NotNull
	public static RuntimeInstance createRuntime()
	{
		return new RuntimeInstance();
	}

	/**
	 * Evaluate the input string using the given context using Velocity.
	 *
	 * @param context Context to use in rendering input string.
	 * @param logTag  Tag (template name) for log messages.
	 * @param input   Input string containing the VTL to be rendered.
	 *
	 * @return Rendered output.
	 */
	@Nullable
	@Contract( "!null, _, _ -> !null" )
	public static String evaluate( @Nullable final String input, @Nullable final Context context, @NotNull final String logTag )
	{
		return evaluate( input, getSharedRuntime(), context, logTag );
	}

	/**
	 * Evaluate the input string using the given context using Velocity.
	 *
	 * @param input   Input string containing the VTL to be rendered.
	 * @param runtime Velocity runtime to use.
	 * @param context Context to use in rendering input string.
	 * @param logTag  Tag (template name) for log messages.
	 *
	 * @return Rendered output.
	 */
	@Nullable
	@Contract( "!null, _, _, _ -> !null" )
	public static String evaluate( @Nullable final String input, @NotNull final RuntimeServices runtime, @Nullable final Context context, @NotNull final String logTag )
	{
		String result = input;

		if ( ( result != null ) && !result.isEmpty() )
		{
			final StringWriter writer = new StringWriter();
			try
			{
				runtime.evaluate( context, writer, logTag, input );
			}
			catch ( final VelocityException e )
			{
				String message = e.getMessage();

				final Matcher matcher = VELOCITY_LOG_FILE_POSITION_PATTERN.matcher( message );
				if ( matcher.find() )
				{
					final int lineNumber = Integer.parseInt( matcher.group( 1 ) );
					//final int column = Integer.parseInt( matcher.group( 2 ) );
					if ( lineNumber > 0 )
					{
						final String[] lines = END_OF_LINE.split( input );
						if ( lineNumber <= lines.length )
						{
							final StringBuilder sb = new StringBuilder();
							sb.append( "Evaluation of '" ).append( logTag ).append( "' failed: " ).append( message );

							for ( int index = Math.max( 1, lineNumber - 2 ); index <= Math.min( lineNumber + 2, lines.length ); index++ )
							{
								sb.append( '\n' ).append( index ).append( ": " ).append( lines[ index - 1 ] );
							}

							message = sb.toString();
							LOG.debug( message, e );
							throw new VelocityException( message, e );
						}
					}
				}

				throw e;
			}
			result = writer.toString();
		}

		return result;
	}

	/**
	 * Evaluates a boolean expression using the given context using Velocity. If
	 * the boolean expression is {@code null} or empty it evaluates to {@code
	 * false}.
	 *
	 * Beware that Velocity has rather interesting {@code null} and empty string
	 * handling:<ul>
	 *
	 * <li>Undefined properties evaluate to {@code null}.</li>
	 *
	 * <li>{@code null} evaluates to {@code false} when used in a boolean
	 * context.</li>
	 *
	 * <li>The empty string evaluates to {@code true} when used in a boolean
	 * context.</li>
	 *
	 * </ul>
	 *
	 * A null-safe empty string check is a bit clumsy as a result: {@code
	 * "$!foo" == ""}.
	 *
	 * @param expression Boolean expression to evaluate.
	 * @param context    Context to be used.
	 * @param logTag     Tag (template name) for log messages.
	 *
	 * @return Value of the boolean expression.
	 */
	public static boolean evaluateBoolean( @Nullable final CharSequence expression, @NotNull final Context context, @NotNull final String logTag )
	{
		return evaluateBoolean( expression, getSharedRuntime(), context, logTag );
	}

	/**
	 * Evaluates a boolean expression using the given context using Velocity. If
	 * the boolean expression is {@code null} or empty it evaluates to {@code
	 * false}.
	 *
	 * Beware that Velocity has rather interesting {@code null} and empty string
	 * handling:<ul>
	 *
	 * <li>Undefined properties evaluate to {@code null}.</li>
	 *
	 * <li>{@code null} evaluates to {@code false} when used in a boolean
	 * context.</li>
	 *
	 * <li>The empty string evaluates to {@code true} when used in a boolean
	 * context.</li>
	 *
	 * </ul>
	 *
	 * A null-safe empty string check is a bit clumsy as a result: {@code
	 * "$!foo" == ""}.
	 *
	 * @param expression Boolean expression to evaluate.
	 * @param runtime    Velocity runtime to use.
	 * @param context    Context to use in rendering input string.
	 * @param logTag     Tag (template name) for log messages.
	 *
	 * @return Value of the boolean expression.
	 */
	public static boolean evaluateBoolean( @Nullable final CharSequence expression, @NotNull final RuntimeServices runtime, @NotNull final Context context, @NotNull final String logTag )
	{
		return !TextTools.isEmpty( expression ) && Boolean.parseBoolean( evaluate( "#if(" + expression + ")true#{else}false#end", runtime, context, logTag ) );
	}

	/**
	 * Creates a regular expression that matches the output of a Velocity
	 * template. Template variables are replaced with regular expression
	 * fragments, specified by the {@code variableReplacements} parameter. If no
	 * regular expression is given for a variable, it will match any string
	 * ({@code .*}).
	 *
	 * @param template             Velocity template to be converted to a
	 *                             regular expression.
	 * @param variableReplacements Maps variable names to regular expressions
	 *                             that match their values.
	 * @param capturingGroups      Receives the capturing group index in the
	 *                             resulting pattern for each variable
	 *                             encountered in the input.
	 * @param flags                Match flags for the regular expression; see
	 *                             {@link Pattern#compile(String, int)}.
	 *
	 * @return Regular expression that matches the output of the template.
	 */
	@NotNull
	public static Pattern createPattern( @NotNull final String template, @NotNull final Map<String, String> variableReplacements, @NotNull final Map<String, Integer> capturingGroups, final int flags )
	{
		final Context context = new VariableReplacementContext( variableReplacements, new CapturingGroupInfo( capturingGroups ), "" );
		final String templatePattern = "\\Q" + evaluate( template, context, "createPattern" ) + "\\E";
		return Pattern.compile( templatePattern, flags );
	}

	/**
	 * Magic Velocity context used to implement {@link #createPattern}. It can
	 * resolve any nested property and applies the variable replacement map to
	 * it. Unknown variables/properties are mapped to {@code ".*"}.
	 */
	private static class VariableReplacementContext
	implements Context
	{
		/**
		 * Maps variable names to regular expressions that match their values.
		 */
		@NotNull
		private final Map<String, String> _variableReplacements;

		/**
		 * Receives the capturing group index in the resulting pattern for each
		 * variable.
		 */
		@NotNull
		private final CapturingGroupInfo _capturingGroupInfo;

		/**
		 * Scope of the current context, i.e. the expression used to access it,
		 * or the empty string for the root context.
		 */
		@NotNull
		private final String _scope;

		/**
		 * Constructs a new instance.
		 *
		 * @param variableReplacements Maps variables to regular expressions.
		 * @param capturingGroupInfo   Receives capturing group info.
		 * @param scope                Scope name; empty for root context.
		 */
		private VariableReplacementContext( @NotNull final Map<String, String> variableReplacements, @NotNull final CapturingGroupInfo capturingGroupInfo, @NotNull final String scope )
		{
			_variableReplacements = variableReplacements;
			_capturingGroupInfo = capturingGroupInfo;
			_scope = scope;
		}

		@Override
		public Object put( final String key, final Object value )
		{
			throw new UnsupportedOperationException();
		}

		@Nullable
		@Override
		public Object get( @Nullable final String key )
		{
			Object result = null;
			if ( ( key != null ) && !key.startsWith( ".literal." ) )
			{
				// Magic! Allows the context to resolve any (nested) property.
				result = new VariableReplacementContext( _variableReplacements, _capturingGroupInfo, _scope.isEmpty() ? key : _scope + '.' + key );
			}
			return result;
		}

		@Override
		public boolean containsKey( final String s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String[] getKeys()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Object remove( final String s )
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString()
		{
			// Magic! This is called when Velocity renders the variable.
			_capturingGroupInfo.add( _scope );
			return "\\E(" + _variableReplacements.getOrDefault( _scope, ".*" ) + ")\\Q";
		}
	}

	/**
	 * Receives the capturing group index in the resulting pattern for each
	 * variable.
	 */
	private static class CapturingGroupInfo
	{
		/**
		 * Receives the capturing group index in the resulting pattern for each
		 * variable.
		 */
		@NotNull
		private final Map<String, Integer> _capturingGroups;

		/**
		 * Capturing group index.
		 */
		private int _groupIndex = 0;

		/**
		 * Constructs a new instance.
		 *
		 * @param capturingGroups Receives the capturing group index in the resulting pattern for each variable.
		 */
		private CapturingGroupInfo( @NotNull final Map<String, Integer> capturingGroups )
		{
			_capturingGroups = capturingGroups;
		}

		/**
		 * Adds an entry for a capturing group that matches the specified
		 * variable.
		 *
		 * @param variableName Variable name.
		 */
		public void add( @NotNull final String variableName )
		{
			_capturingGroups.put( variableName, ++_groupIndex );
		}
	}
}
