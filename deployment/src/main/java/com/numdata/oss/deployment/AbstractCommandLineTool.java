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
package com.numdata.oss.deployment;

import java.io.*;
import java.net.*;
import java.util.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Base class for command-line tools.
 *
 * @author Peter S. Heijnen
 */
@SuppressWarnings( "unused" )
public abstract class AbstractCommandLineTool
{
	/**
	 * Command-line argument definitions.
	 */
	private final List<ToolOption> _argumentDefinitions = new ArrayList<ToolOption>();

	/**
	 * Command-line argument definitions.
	 */
	private List<String> _argumentValues = Collections.emptyList();

	/**
	 * Program option definitions.
	 */
	private final List<ToolOption> _optionDefinitions = new ArrayList<ToolOption>();

	/**
	 * Program option argument values.
	 */
	private final Map<String, List<String>> _optionValues = new HashMap<String, List<String>>();

	/**
	 * Construct tool.
	 */
	protected AbstractCommandLineTool()
	{
	}

	/**
	 * Add (mandatory) program argument.
	 *
	 * @param name        Name of program argument.
	 * @param description Description of program argument.
	 */
	protected void defineArgument( @NotNull final String name, @NotNull final String description )
	{
		defineArgument( new ToolOption( name, '<' + name + '>', description, false ) );
	}

	/**
	 * Add (mandatory) program argument.
	 *
	 * @param name        Name of program argument.
	 * @param label       Label for program argument.
	 * @param description Description of program argument.
	 */
	protected void defineArgument( @NotNull final String name, @NotNull final String label, @NotNull final String description )
	{
		defineArgument( new ToolOption( name, label, description, false ) );
	}

	/**
	 * Add (mandatory) program argument.
	 *
	 * @param definition Program argument definition.
	 */
	protected void defineArgument( @NotNull final ToolOption definition )
	{
		_argumentDefinitions.add( definition );
	}

	/**
	 * Get program argument definitions.
	 *
	 * @return List of named program argument definitions.
	 */
	@NotNull
	protected List<ToolOption> getArgumentDefinitions()
	{
		return Collections.unmodifiableList( _argumentDefinitions );
	}

	/**
	 * Get program argument definition.
	 *
	 * @param name Name of program argument.
	 *
	 * @return Program argument definition; {@code null} if program argument is
	 * not defined.
	 */
	@Nullable
	protected ToolOption getArgumentDefinition( @NotNull final String name )
	{
		ToolOption result = null;

		for ( final ToolOption definition : _argumentDefinitions )
		{
			if ( name.equals( definition.getName() ) )
			{
				result = definition;
				break;
			}
		}

		return result;
	}

	/**
	 * Get program argument definition.
	 *
	 * @param name Name of program argument.
	 */
	protected void removeArgumentDefinition( @NotNull final String name )
	{
		for ( final Iterator<ToolOption> it = _argumentDefinitions.iterator(); it.hasNext(); )
		{
			final ToolOption definition = it.next();
			if ( name.equals( definition.getName() ) )
			{
				it.remove();
			}
		}
	}

	/**
	 * Get program argument definition.
	 */
	protected void removeAllArgumentDefinitions()
	{
		_argumentDefinitions.clear();
	}

	/**
	 * Get program argument value.
	 *
	 * @param argumentName Name of argument.
	 *
	 * @return Program argument value.
	 *
	 * @throws IllegalArgumentException if argument with given name is
	 * undefined.
	 */
	@NotNull
	public String getArgument( @NotNull final String argumentName )
	{
		return _argumentValues.get( getArgumentIndex( argumentName ) );
	}

	/**
	 * Get program argument value.
	 *
	 * @param argumentName Name of argument.
	 * @param defaultValue Default argument value to return.
	 *
	 * @return Program argument value; {@code defaultValue} if argument is not
	 * set.
	 */
	public String getArgument( @NotNull final String argumentName, @Nullable final String defaultValue )
	{
		String result;
		try
		{
			result = getArgument( argumentName );
		}
		catch ( final IllegalArgumentException ignored )
		{
			result = defaultValue;
		}
		//noinspection ConstantConditions
		return result;
	}

	/**
	 * Get program argument value.
	 *
	 * @param argumentName Name of argument.
	 *
	 * @return Program argument value; {@code defaultValue} if argument is not
	 * set.
	 */
	public int getIntArgument( @NotNull final String argumentName )
	{
		return Integer.parseInt( getArgument( argumentName ).trim() );
	}

	/**
	 * Get program argument value.
	 *
	 * @param argumentName Name of argument.
	 * @param defaultValue Default argument value to return.
	 *
	 * @return Program argument value; {@code defaultValue} if argument is not
	 * set.
	 */
	public int getIntArgument( @NotNull final String argumentName, final int defaultValue )
	{
		final String value = getArgument( argumentName, null );
		return ( ( value == null ) || value.isEmpty() ) ? defaultValue : Integer.parseInt( value.trim() );
	}

	/**
	 * Get index of program argument value.
	 *
	 * @param argumentName Name of argument.
	 *
	 * @return Program argument index.
	 *
	 * @throws IllegalArgumentException if argument with given name is
	 * undefined.
	 */
	private int getArgumentIndex( @NotNull final String argumentName )
	{
		ToolOption argument = null;
		int index = -1;
		final List<ToolOption> argumentDefinitions = getArgumentDefinitions();
		for ( int i = 0; i < argumentDefinitions.size(); i++ )
		{
			final ToolOption candidate = argumentDefinitions.get( i );
			if ( argumentName.equals( candidate.getName() ) )
			{
				argument = candidate;
				index = i;
				break;
			}
		}

		if ( ( argument == null ) || ( index < 0 ) )
		{
			throw new IllegalArgumentException( "program argument '" + argumentName + "' is undefined" );
		}

		if ( index >= _argumentValues.size() )
		{
			abortWithSyntax( "missing " + argument.getLabel() + " program argument" );
		}

		return index;
	}

	/**
	 * Get program argument value and all following program arguments.
	 *
	 * @param argumentName Name of argument.
	 *
	 * @return Program argument and all following arguments.
	 *
	 * @throws IllegalArgumentException if argument with given name is
	 * undefined.
	 */
	@NotNull
	public List<String> getVariableArgument( @NotNull final String argumentName )
	{
		return new ArrayList<String>( _argumentValues.subList( getArgumentIndex( argumentName ), _argumentValues.size() ) );
	}

	/**
	 * Get extra program arguments.
	 *
	 * @return Extra arguments after named program arguments, if any.
	 */
	@NotNull
	public List<String> getExtraArguments()
	{
		final List<ToolOption> argumentDefinitions = getArgumentDefinitions();
		final List<String> argumentValues = _argumentValues;
		return new ArrayList<String>( argumentValues.subList( argumentDefinitions.size(), argumentValues.size() ) );
	}

	/**
	 * Add (mandatory) program option.
	 *
	 * @param name        Name of program option.
	 * @param description Description of program option.
	 */
	protected void defineOption( @NotNull final String name, @NotNull final String description )
	{
		_optionDefinitions.add( new ToolOption( name, '-' + name, description, false ) );
	}

	/**
	 * Add (mandatory) program option.
	 *
	 * @param name             Name of program option.
	 * @param requiresArgument Whether the option requires an argument.
	 * @param label            Label for program option.
	 * @param description      Description of program option.
	 */
	protected void defineOption( @NotNull final String name, final boolean requiresArgument, @NotNull final String label, @NotNull final String description )
	{
		_optionDefinitions.add( new ToolOption( name, label, description, requiresArgument ) );
	}

	/**
	 * Get program option definitions.
	 *
	 * @return List of named program option definitions.
	 */
	@NotNull
	protected List<ToolOption> getOptionDefinitions()
	{
		return Collections.unmodifiableList( _optionDefinitions );
	}

	/**
	 * Get program option definition.
	 *
	 * @param name Name of program option.
	 *
	 * @return Program option definition; {@code null} if program option is not
	 * defined.
	 */
	@Nullable
	protected ToolOption getOptionDefinition( @NotNull final String name )
	{
		ToolOption result = null;

		for ( final ToolOption definition : _optionDefinitions )
		{
			if ( name.equals( definition.getName() ) )
			{
				result = definition;
				break;
			}
		}

		return result;
	}

	/**
	 * Get program option definition.
	 *
	 * @param name Name of program option.
	 */
	protected void removeOptionDefinition( @NotNull final String name )
	{
		for ( final Iterator<ToolOption> it = _optionDefinitions.iterator(); it.hasNext(); )
		{
			final ToolOption definition = it.next();
			if ( name.equals( definition.getName() ) )
			{
				it.remove();
			}
		}
	}

	/**
	 * Get program option definition.
	 */
	protected void removeAllOptionDefinitions()
	{
		_optionDefinitions.clear();
	}

	/**
	 * Get option argument values.
	 *
	 * @param optionName Name of option.
	 *
	 * @return Program option argument values.
	 */
	@NotNull
	public List<String> getOptionValues( @NotNull final String optionName )
	{
		final List<String> valueList = _optionValues.get( optionName );
		return ( valueList == null ) ? Collections.<String>emptyList() : Collections.unmodifiableList( valueList );
	}

	/**
	 * Get option argument value.
	 *
	 * @param optionName Name of option.
	 *
	 * @return Program option argument value; {@code null} if option argument is
	 * not set.
	 */
	@Nullable
	public String getOption( @NotNull final String optionName )
	{
		final List<String> valueList = _optionValues.get( optionName );
		return ( valueList == null ) ? null : valueList.get( valueList.size() - 1 );
	}

	/**
	 * Get option argument value.
	 *
	 * @param optionName Name of option.
	 *
	 * @return Program option argument value.
	 */
	@NotNull
	public String getRequiredOption( @NotNull final String optionName )
	{
		final List<String> values = _optionValues.get( optionName );
		if ( values == null )
		{
			throw new MissingOptionException( optionName );
		}
		return values.get( values.size() - 1 );
	}

	/**
	 * Get option argument value.
	 *
	 * @param optionName   Name of option.
	 * @param defaultValue Default value to return.
	 *
	 * @return Program option argument value; {@code defaultValue} if option is
	 * not set.
	 */
	public String getOption( @NotNull final String optionName, @Nullable final String defaultValue )
	{
		String result = getOption( optionName );
		if ( result == null )
		{
			result = defaultValue;
		}
		//noinspection ConstantConditions
		return result;
	}

	/**
	 * Get option argument value.
	 *
	 * @param optionName Name of option.
	 *
	 * @return Program option argument value.
	 */
	public int getIntOption( @NotNull final String optionName )
	{
		final String value = getOption( optionName );
		if ( ( value == null ) || value.isEmpty() )
		{
			throw new IllegalArgumentException( "Missing argument for '" + optionName + "' option" );
		}

		return Integer.parseInt( value.trim() );
	}

	/**
	 * Get option argument value.
	 *
	 * @param optionName   Name of option.
	 * @param defaultValue Default value to return.
	 *
	 * @return Program option argument value; {@code defaultValue} if option is
	 * not set.
	 */
	public int getIntOption( @NotNull final String optionName, final int defaultValue )
	{
		final String value = getOption( optionName );
		return ( ( value == null ) || value.isEmpty() ) ? defaultValue : Integer.parseInt( value.trim() );
	}

	/**
	 * Get option argument value.
	 *
	 * @param optionName Name of option.
	 *
	 * @return Program option argument value; {@code null} if option is not set.
	 */
	@Nullable
	public Integer getIntegerOption( @NotNull final String optionName )
	{
		final String value = getOption( optionName );
		return ( ( value == null ) || value.isEmpty() ) ? null : Integer.valueOf( value.trim() );
	}

	/**
	 * Get option argument value.
	 *
	 * @param optionName   Name of option.
	 * @param defaultValue Default value to return.
	 *
	 * @return Program option argument value; {@code defaultValue} if option is
	 * not set.
	 */
	@NotNull
	public Integer getIntegerOption( @NotNull final String optionName, @NotNull final Integer defaultValue )
	{
		final String value = getOption( optionName );
		return ( ( value == null ) || value.isEmpty() ) ? defaultValue : Integer.valueOf( value.trim() );
	}

	/**
	 * Get option argument value.
	 *
	 * @param optionName Name of option.
	 *
	 * @return Program option argument value.
	 */
	public boolean getBooleanOption( @NotNull final String optionName )
	{
		final String value = getOption( optionName );
		if ( ( value == null ) || value.isEmpty() )
		{
			throw new IllegalArgumentException( "Missing argument for '" + optionName + "' option" );
		}

		return Boolean.parseBoolean( value.trim() );
	}

	/**
	 * Get option argument value.
	 *
	 * @param optionName   Name of option.
	 * @param defaultValue Default value to return.
	 *
	 * @return Program option argument value; {@code defaultValue} if option is
	 * not set.
	 */
	public boolean getBooleanOption( @NotNull final String optionName, final boolean defaultValue )
	{
		final String value = getOption( optionName );
		return ( ( value == null ) || value.isEmpty() ) ? defaultValue : Boolean.parseBoolean( value.trim() );
	}

	/**
	 * Run tool.
	 *
	 * @param args Command-line arguments.
	 *
	 * @throws Exception if the tool crashes.
	 */
	public void launch( @NotNull final String... args )
	throws Exception
	{
		processCommandLine( args );
		processConfigFile();
		try
		{
			run();
		}
		catch ( MissingOptionException e )
		{
			System.err.println( e.getMessage() );
			System.err.println();
			printSyntax( System.err );
		}
	}

	/**
	 * Processes program options and arguments from the configuration file
	 * '.ivenza' in the current directory, if found. Existing values are not
	 * overwritten.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	protected void processConfigFile()
	throws IOException
	{
		final File profileFile = new File( ".ivenza" );
		if ( profileFile.exists() )
		{
			// Add nulls for any missing arguments.
			final List<String> argumentValues = new ArrayList<String>( _argumentDefinitions.size() );
			while ( argumentValues.size() < _argumentDefinitions.size() )
			{
				argumentValues.add( null );
			}

			final FileInputStream in = new FileInputStream( profileFile );
			try
			{
				final Properties properties = new Properties();
				properties.load( in );

				System.err.println( "Applying arguments/options from configuration file" );
				final List<ToolOption> argumentDefinitions = getArgumentDefinitions();
				for ( int index = 0; index < argumentDefinitions.size(); index++ )
				{
					final ToolOption argument = argumentDefinitions.get( index );
					if ( properties.containsKey( argument.getName() ) )
					{
						final String value = properties.getProperty( argument.getName() );
						System.err.println( " - Argument " + argument.getName() + " = " + value );
						argumentValues.set( index, value );
					}
					else if ( properties.containsKey( argument.getName() + "*" ) )
					{
						if ( index == argumentValues.size() - 1 )
						{
							final String values = properties.getProperty( argument.getName() + "*" );
							final String[] valueArray = values.split( "\\s+" );
							System.err.println( " - Variable argument " + argument.getName() + " = " + Arrays.toString( valueArray ) );
							for ( int i = 0; i < valueArray.length; i++ )
							{
								final String value = valueArray[ i ];
								if ( i == 0 )
								{
									argumentValues.set( index, value );
								}
								else
								{
									argumentValues.add( value );
								}
							}
						}
						else
						{
							System.err.println( " - Ignoring variable argument " + argument.getName() + ": it is not the last argument for this tool." );
						}
					}
				}

				final Map<String, List<String>> optionValues = _optionValues;
				for ( final ToolOption option : getOptionDefinitions() )
				{
					final String optionName = option.getName();
					if ( properties.containsKey( optionName ) )
					{
						final String value = properties.getProperty( optionName );

						List<String> valueList = optionValues.get( optionName );
						if ( valueList == null )
						{
							valueList = new ArrayList<String>();
							optionValues.put( optionName, valueList );
							valueList.add( value );
							System.err.println( " - Option " + optionName + " = " + value );
						}
						else
						{
							System.err.println( " - Option " + optionName + " = " + value + " (overridden by command-line: " + valueList + ")" );
						}
					}
				}

				System.err.println();
			}
			finally
			{
				in.close();
			}

			// Remove trailing null arguments, which were added above.
			while ( !argumentValues.isEmpty() && argumentValues.get( argumentValues.size() - 1 ) == null )
			{
				argumentValues.remove( argumentValues.size() - 1 );
			}

			// Fill gaps in the config file with arguments.
			final Iterator<String> commandLineIterator = _argumentValues.iterator();
			for ( final ListIterator<String> configFileIterator = argumentValues.listIterator(); configFileIterator.hasNext() && commandLineIterator.hasNext(); )
			{
				if ( configFileIterator.next() == null )
				{
					configFileIterator.set( commandLineIterator.next() );
				}
			}

			// Append any remaining arguments.
			while ( commandLineIterator.hasNext() )
			{
				argumentValues.add( commandLineIterator.next() );
			}

			// Replace original arguments.
			_argumentValues.clear();
			_argumentValues.addAll( argumentValues );
		}
	}

	/**
	 * Process command-line arguments.
	 *
	 * @param args Command-line arguments.
	 */
	protected void processCommandLine( @NotNull final String... args )
	{
		final List<ToolOption> optionDefinitions = getOptionDefinitions();
		final List<String> commandLineArgumentValues = new ArrayList<String>( args.length );

		final List<String> argList = Arrays.asList( args );
		final Map<String, List<String>> optionValues = _optionValues;

		for ( final Iterator<String> it = argList.iterator(); it.hasNext(); )
		{
			final String arg = it.next();

			if ( TextTools.startsWith( arg, '-' ) )
			{
				final String optionName = arg.substring( 1 );

				ToolOption option = null;
				for ( final ToolOption possibleOption : optionDefinitions )
				{
					if ( possibleOption.getName().equals( optionName ) )
					{
						option = possibleOption;
						break;
					}
				}

				if ( option == null )
				{
					abortWithSyntax( "unrecognized option: " + arg );
				}
				else
				{
					final String optionValue;

					if ( option.isRequiresArgument() )
					{
						if ( !it.hasNext() )
						{
							abortWithSyntax( "Missing argument for " + option.getLabel() + " option" );
						}
						optionValue = it.next();
					}
					else
					{
						optionValue = "true";
					}

					List<String> valueList = optionValues.get( optionName );
					if ( valueList == null )
					{
						valueList = new ArrayList<String>();
						optionValues.put( optionName, valueList );
					}
					valueList.add( optionValue );
				}
			}
			else
			{
				commandLineArgumentValues.add( arg );
			}
		}

		_argumentValues = commandLineArgumentValues;
	}

	/**
	 * Run tool.
	 *
	 * @throws Exception if the tool crashes.
	 */
	protected abstract void run()
	throws Exception;

	/**
	 * Abort program with the given error message.
	 *
	 * @param message Error message.
	 */
	@SuppressWarnings( "CallToSystemExit" )
	protected void abort( @NotNull final String message )
	{
		System.err.println( "error: " + message );
		System.exit( 1 );
	}

	/**
	 * Abort program with the given error message including the syntax
	 * description.
	 *
	 * @param message Error message.
	 */
	@SuppressWarnings( "CallToSystemExit" )
	protected void abortWithSyntax( @NotNull final String message )
	{
		System.err.println( "error: " + message );
		System.err.println();
		printSyntax( System.err );
		System.exit( 1 );
	}

	/**
	 * Print program syntax.
	 *
	 * @param out Stream to print syntax to.
	 */
	@SuppressWarnings( "NullableProblems" )
	protected void printSyntax( @NotNull final PrintStream out )
	{
		final Collection<ToolOption> optionDefinitions = getOptionDefinitions();
		final List<ToolOption> argumentDefinitions = getArgumentDefinitions();

		int maxOptionLabelLength = 0;
		for ( final ToolOption option : optionDefinitions )
		{
			maxOptionLabelLength = Math.max( maxOptionLabelLength, option.getLabel().length() );
		}
		final boolean hasOptions = maxOptionLabelLength > 0;

		int maxArgLabelLength = 0;
		for ( final ToolOption option : argumentDefinitions )
		{
			maxArgLabelLength = Math.max( maxArgLabelLength, option.getLabel().length() );
		}

		final boolean hasArguments = ( maxArgLabelLength > 0 );

		if ( hasOptions || hasArguments )
		{
			out.print( "Arguments:" );
			if ( hasOptions )
			{
				out.print( " [<option> [...]]" );
			}
			for ( final ToolOption option : argumentDefinitions )
			{
				out.print( ' ' );
				out.print( option.getLabel() );
			}
			out.println();

			if ( hasOptions )
			{
				out.println();
				out.println( "Options:" );
				final String indent = '\n' + TextTools.getFixed( maxOptionLabelLength + 4, ' ' );
				for ( final ToolOption option : optionDefinitions )
				{
					out.print( "  " );
					out.print( TextTools.getFixed( option.getLabel(), maxOptionLabelLength, false, ' ' ) );
					out.print( "  " );
					out.println( option.getDescription().replaceAll( "\n", indent ) );
				}
			}

			if ( hasArguments )
			{
				out.println();
				out.println( "Arguments:" );
				final String indent = '\n' + TextTools.getFixed( maxArgLabelLength + 4, ' ' );
				for ( final ToolOption option : argumentDefinitions )
				{
					out.print( "  " );
					out.print( TextTools.getFixed( option.getLabel(), maxArgLabelLength, false, ' ' ) );
					out.print( "  " );
					out.println( option.getDescription().replaceAll( "\n", indent ) );
				}
			}
		}
	}

	/**
	 * Convert path list to URI list.
	 *
	 * @param path Path list.
	 *
	 * @return URI.
	 */
	@NotNull
	protected static URI getUriForPath( @NotNull final String path )
	{
		URI result;
		try
		{
			result = new URI( path );
		}
		catch ( final URISyntaxException ignored )
		{
			final File file = new File( path );
			if ( !file.canRead() )
			{
				throw new IllegalArgumentException( "Sorry, can't find resource '" + path + '\'' );
			}

			result = file.toURI();
		}

		return result;
	}

	/**
	 * Convert path list to URI list.
	 *
	 * @param paths Path list.
	 *
	 * @return URI list.
	 */
	@NotNull
	protected static List<URI> getUriForPaths( @NotNull final Collection<String> paths )
	{
		final List<URI> result = new ArrayList<URI>( paths.size() );
		for ( final String path : paths )
		{
			result.add( getUriForPath( path ) );
		}

		return result;
	}

	/**
	 * Convert path list to URL list.
	 *
	 * @param path Path.
	 *
	 * @return URL.
	 */
	@NotNull
	protected static URL getUrlForPath( @NotNull final String path )
	{
		URL result;
		try
		{
			result = new URL( path );
		}
		catch ( final MalformedURLException ignored )
		{
			final File file = new File( path );
			if ( !file.canRead() )
			{
				throw new IllegalArgumentException( "Sorry, can't find resource '" + path + '\'' );
			}

			final URI uri = file.toURI();
			try
			{
				result = uri.toURL();
			}
			catch ( final MalformedURLException e )
			{
				throw new IllegalArgumentException( "Sorry, can't find resource '" + path + '\'', e );
			}
		}

		return result;
	}

	/**
	 * Convert path list to URL list.
	 *
	 * @param paths Path list.
	 *
	 * @return URL list.
	 */
	@NotNull
	protected static List<URL> getUrlForPaths( @NotNull final Collection<String> paths )
	{
		final List<URL> result = new ArrayList<URL>( paths.size() );
		for ( final String path : paths )
		{
			result.add( getUrlForPath( path ) );
		}

		return result;
	}

	/**
	 * Program option.
	 */
	protected static class ToolOption
	{
		/**
		 * Name of option.
		 */
		@NotNull
		private final String _name;

		/**
		 * Label for option.
		 */
		@NotNull
		private final String _label;

		/**
		 * Description of option.
		 */
		@NotNull
		private final String _description;

		/**
		 * Whether this option requires an argument.
		 */
		private final boolean _requiresArgument;

		/**
		 * Create option.
		 *
		 * @param name             Name of option.
		 * @param label            Label for option.
		 * @param description      Description of option.
		 * @param requiresArgument Whether this option requires an argument.
		 */
		public ToolOption( @NotNull final String name, @NotNull final String label, @NotNull final String description, final boolean requiresArgument )
		{
			_name = name;
			_label = label;
			_description = description;
			_requiresArgument = requiresArgument;
		}

		/**
		 * Get name of option.
		 *
		 * @return Name of option.
		 */
		@NotNull
		public String getName()
		{
			return _name;
		}

		/**
		 * Get label for option.
		 *
		 * @return Label for option.
		 */
		@NotNull
		public String getLabel()
		{
			return _label;
		}

		/**
		 * Get description of option.
		 *
		 * @return Description of option.
		 */
		@NotNull
		public String getDescription()
		{
			return _description;
		}

		/**
		 * Get whether this option requires an argument.
		 *
		 * @return Whether this option requires an argument.
		 */
		public boolean isRequiresArgument()
		{
			return _requiresArgument;
		}
	}
}
