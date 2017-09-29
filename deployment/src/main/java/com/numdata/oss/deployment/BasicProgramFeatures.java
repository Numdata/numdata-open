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
package com.numdata.oss.deployment;

import java.io.*;
import java.util.*;

import com.numdata.oss.*;
import com.numdata.oss.deployment.ProgramFeatures.*;
import org.jetbrains.annotations.*;
import org.json.*;

/**
 * Basic program features implementation, based on an in-memory store of default
 * (availability) and extended (writability) user levels for each feature.
 *
 * @author Gerrit Meinders
 */
public class BasicProgramFeatures
{
	/**
	 * Switch to enable warning messages (on {@code System.err}) when unknown
	 * feature properties are requested. This can be very useful when a new
	 * software profile is established, or when new features have been added.
	 */
	private boolean _unknownFeatureWarningsEnabled;

	/**
	 * Feature set.
	 */
	@NotNull
	private final Map<String, Feature> _features = new HashMap<String, Feature>();

	/**
	 * Default user level.
	 */
	@NotNull
	private UserLevel _defaultUserLevel;

	/**
	 * Extended user level.
	 */
	@NotNull
	private UserLevel _extendedUserLevel;

	/**
	 * Switch to indicate whether the default or extended user level should be
	 * used to determine the 'current' feature availability and writability.
	 */
	private boolean _extendedUserLevelEnabled;

	/**
	 * Constructs a new instance.
	 */
	public BasicProgramFeatures()
	{
		_unknownFeatureWarningsEnabled = true;
		_defaultUserLevel = UserLevel.NORMAL;
		_extendedUserLevel = UserLevel.EXPERT;
		_extendedUserLevelEnabled = false;
	}

	/**
	 * Constructs a copy of the given instance.
	 *
	 * @param original Instance to copy.
	 * @param deep     {@code true} for a deep copy; {@code false} for a shallow
	 *                 copy.
	 */
	protected BasicProgramFeatures( @NotNull final BasicProgramFeatures original, final boolean deep )
	{
		_unknownFeatureWarningsEnabled = original._unknownFeatureWarningsEnabled;
		_defaultUserLevel = original._defaultUserLevel;
		_extendedUserLevel = original._extendedUserLevel;
		_extendedUserLevelEnabled = original._extendedUserLevelEnabled;

		if ( deep )
		{
			for ( final Map.Entry<String, Feature> entry : original._features.entrySet() )
			{
				_features.put( entry.getKey(), new Feature( entry.getValue() ) );
			}
		}
		else
		{
			_features.putAll( original._features );
		}
	}

	/**
	 * Creates a deep copy of this instance.
	 *
	 * @return Program features.
	 */
	public BasicProgramFeatures deepCopy()
	{
		return new BasicProgramFeatures( this, true );
	}

	/**
	 * Creates a shallow copy of this instance. Program feature definitions are
	 * shared.
	 *
	 * @return Program features using the same program feature definitions.
	 */
	public BasicProgramFeatures shallowCopy()
	{
		return new BasicProgramFeatures( this, false );
	}

	/**
	 * Disable warning messages about requested but unknown features (on {@link
	 * System#err}. These messages can be very useful when a new software
	 * profile is established, or when new features have been added, but can be
	 * annoying in test environments.
	 */
	public void disableUnknownFeatureWarnings()
	{
		_unknownFeatureWarningsEnabled = false;
	}

	/**
	 * Get current user level based. This returns either the default user level,
	 * or the extended user level.
	 *
	 * @return The current user level.
	 *
	 * @see #getDefaultUserLevel
	 * @see #getExtendedUserLevel
	 * @see #isExtendedUserLevelEnabled
	 */
	@NotNull
	public UserLevel getCurrentUserLevel()
	{
		return ( isExtendedUserLevelEnabled() ? getExtendedUserLevel() : getDefaultUserLevel() );
	}

	/**
	 * Get default user level.
	 *
	 * @return Default user level.
	 *
	 * @see #getCurrentUserLevel
	 */
	@NotNull
	public UserLevel getDefaultUserLevel()
	{
		return _defaultUserLevel;
	}

	/**
	 * Set default user level.
	 *
	 * @param level Default user level to use.
	 *
	 * @see #getCurrentUserLevel
	 * @see #setExtendedUserLevelEnabled
	 */
	public void setDefaultUserLevel( @NotNull final UserLevel level )
	{
		_defaultUserLevel = level;
	}

	/**
	 * Get extended user level.
	 *
	 * @return Extended user level.
	 *
	 * @see #getCurrentUserLevel
	 */
	@NotNull
	public UserLevel getExtendedUserLevel()
	{
		return _extendedUserLevel;
	}

	/**
	 * Set extended user level.
	 *
	 * @param level Extended user level.
	 *
	 * @see #getCurrentUserLevel
	 * @see #setExtendedUserLevelEnabled
	 */
	public void setExtendedUserLevel( @NotNull final UserLevel level )
	{
		_extendedUserLevel = level;
	}

	/**
	 * Determine whether the default or extended user level is used to determine
	 * the 'current' user level used to determine feature availability and
	 * writability.
	 *
	 * @return {@code true} if the extended user level is used; {@code false} if
	 * the default user level is used.
	 */
	public boolean isExtendedUserLevelEnabled()
	{
		return _extendedUserLevelEnabled;
	}

	/**
	 * Select whether the default or extended user level should be used to
	 * determine the 'current' user level used to determine feature availability
	 * and writability.
	 *
	 * @param extended {@code true} if the extended user level should be used;
	 *                 {@code false} if the default user level should be used.
	 */
	public void setExtendedUserLevelEnabled( final boolean extended )
	{
		_extendedUserLevelEnabled = extended;
	}

	/**
	 * Set availability and writability level of a feature. This overrides any
	 * previous or default levels for the feature (by default, all features are
	 * available and writable).
	 *
	 * @param name              Name of feature to set levels for.
	 * @param availabilityLevel User level at which the feature becomes
	 *                          available (and writable).
	 */
	public void setFeature( @NotNull final String name, @NotNull final UserLevel availabilityLevel )
	{
		setFeature( name, availabilityLevel, availabilityLevel );
	}

	/**
	 * Set availability and writability level of a feature. This overrides any
	 * previous or default levels for the feature (by default, all features are
	 * available and writable).
	 *
	 * @param name              Name of feature to set levels for.
	 * @param availabilityLevel User level at which the feature becomes
	 *                          available.
	 * @param writabilityLevel  User level at which the feature becomes
	 *                          writable.
	 */
	public void setFeature( @NotNull final String name, @NotNull final UserLevel availabilityLevel, @NotNull final UserLevel writabilityLevel )
	{
		final Feature feature = lookupOrCreate( name );
		feature.setAvailabilityLevel( availabilityLevel );
		feature.setWritabilityLevel( UserLevel.highest( availabilityLevel, writabilityLevel ) );
	}

	/**
	 * Returns all defined program features.
	 *
	 * @return Program features.
	 */
	@NotNull
	public Map<String, Feature> getFeatures()
	{
		return Collections.unmodifiableMap( _features );
	}

	/**
	 * Returns the settings for the specified program feature. If the feature
	 * doesn't exist, it is created.
	 *
	 * @param name Program feature name.
	 *
	 * @return Program feature settings.
	 */
	@NotNull
	public Feature getFeature( @NotNull final String name )
	{
		return lookupOrCreate( name );
	}

	/**
	 * Removes the settings for the specified program feature.
	 *
	 * @param name Program feature name.
	 */
	public void removeFeature( final String name )
	{
		_features.remove( name );
	}

	/**
	 * Removes all program feature settings.
	 */
	public void clearFeatures()
	{
		_features.clear();
	}

	/**
	 * Check if the specified program feature's availability and/or writability
	 * if different for the current default user level and expert user level.
	 *
	 * @param name Name of feature to check.
	 *
	 * @return {@code true} if the feature's availability and/or writability is
	 * different for normal and expert users; {@code false} otherwise.
	 *
	 * @see #isDifferentForLevels
	 * @see #getDefaultUserLevel
	 * @see #getExtendedUserLevel
	 */
	public boolean isDifferentForDefaultAndExtendedLevel( @NotNull final String name )
	{
		return isDifferentForLevels( name, getDefaultUserLevel(), getExtendedUserLevel() );
	}

	/**
	 * Check if the specified program feature's availability and/or writability
	 * if different for the specified levels.
	 *
	 * @param name   Name of feature to check.
	 * @param level1 One of the levels to be checked.
	 * @param level2 One of the levels to be checked.
	 *
	 * @return {@code true} if the feature's availability and/or writability is
	 * different for the specified level; {@code false} otherwise.
	 *
	 * @see #isAvailableAtLevel
	 * @see #isWritableAtLevel
	 */
	public boolean isDifferentForLevels( @NotNull final String name, @NotNull final UserLevel level1, @NotNull final UserLevel level2 )
	{
		final boolean result;

		if ( level1 != level2 )
		{
			final Feature feature = lookup( name );
			if ( feature != null )
			{
				final UserLevel availabilityLevel = feature._availabilityLevel;
				final UserLevel writabilityLevel = feature._writabilityLevel;

				result = ( availabilityLevel != null ) && ( ( level1.isAtLeast( availabilityLevel ) ) != ( level2.isAtLeast( availabilityLevel ) ) ) ||
				         ( writabilityLevel != null ) && ( ( level1.isAtLeast( writabilityLevel ) ) != ( level2.isAtLeast( writabilityLevel ) ) );
			}
			else
			{
				result = false;
			}
		}
		else
		{
			result = false;
		}

		return result;
	}

	/**
	 * Check if the specified program feature is available at all. This will
	 * return {@code true}, if either:<ol>
	 *
	 * <li>the feature has not been registered; or</li>
	 *
	 * <li>the {@code availabilityLevel} of the feature is equal or less than
	 * either the default or the extended availability level.</li>
	 *
	 * </ol>
	 *
	 * @param name Name of feature to check.
	 *
	 * @return {@code true} if the feature is available at all.
	 *
	 * @see #isAvailableAtLevel
	 * @see #isAvailableNow
	 * @see #getDefaultUserLevel
	 * @see #getExtendedUserLevel
	 * @see #isWritableAtAll
	 */
	public boolean isAvailableAtAll( @NotNull final String name )
	{
		return isAvailableAtLevel( name, UserLevel.highest( getDefaultUserLevel(), getExtendedUserLevel() ) );
	}

	/**
	 * Check if the specified program feature is available at the specified user
	 * level. This will return {@code true}, if either:
	 *
	 * <ol>
	 *
	 * <li>the feature has not been registered; or</li>
	 *
	 * <li>the {@code availabilityLevel} of the feature is equal to or less than
	 * the supplied {@code userLevel}.</li>
	 *
	 * </ol>
	 *
	 * @param name      Name of feature to check.
	 * @param userLevel User level to check.
	 *
	 * @return {@code true} if the feature is available at the specified level.
	 *
	 * @see #isAvailableAtAll
	 * @see #isAvailableNow
	 * @see #isWritableAtLevel
	 */
	public boolean isAvailableAtLevel( @NotNull final String name, @NotNull final UserLevel userLevel )
	{
		final Feature feature = lookup( name );
		return ( feature == null ) || feature._availabilityLevel == null || userLevel.isAtLeast( feature._availabilityLevel );
	}

	/**
	 * Check if the specified program feature is currently available. This is
	 * {@code true} if the {@code isAvailableAtLevel()} method returns {@code
	 * true} for the default user level.
	 *
	 * @param name Name of feature to check.
	 *
	 * @return {@code true} if the feature is currently available.
	 *
	 * @see #isAvailableAtAll
	 * @see #isAvailableAtLevel
	 * @see #getCurrentUserLevel
	 * @see #isWritableNow
	 */
	public boolean isAvailableNow( @NotNull final String name )
	{
		return isAvailableAtLevel( name, getCurrentUserLevel() );
	}

	/**
	 * Check if the specified program feature is writable at all. This will
	 * return {@code true}, if either: <ol>
	 *
	 * <li>the feature has not been registered; or</li>
	 *
	 * <li>the {@code writabilityLevel} of the feature is equal or less than
	 * either the default or the extended writability level.</li>
	 *
	 * </ol>
	 *
	 * @param name Name of feature to check.
	 *
	 * @return {@code true} if the feature is writable at all.
	 *
	 * @see #isWritableNow
	 * @see #isWritableAtLevel
	 * @see #getDefaultUserLevel
	 * @see #getExtendedUserLevel
	 * @see #isAvailableAtAll
	 */
	public boolean isWritableAtAll( @NotNull final String name )
	{
		return isWritableAtLevel( name, UserLevel.highest( getDefaultUserLevel(), getExtendedUserLevel() ) );
	}

	/**
	 * Check if the specified program feature is writable at the specified user
	 * level.  This will return {@code true}, if either: <ol>
	 *
	 * <li>the feature has not been registered; or</li>
	 *
	 * <li>the {@code writabilityLevel} of the feature is equal to or less than
	 * the supplied {@code userLevel}.</li>
	 *
	 * </ol>
	 *
	 * @param name      Name of feature to check.
	 * @param userLevel User level to check.
	 *
	 * @return {@code true} if the feature is writable at the specified level.
	 *
	 * @see #isWritableAtAll
	 * @see #isWritableNow
	 * @see #isAvailableAtLevel
	 */
	public boolean isWritableAtLevel( @NotNull final String name, @NotNull final UserLevel userLevel )
	{
		final Feature feature = lookup( name );
		return ( feature == null ) || feature._writabilityLevel == null || userLevel.isAtLeast( feature._writabilityLevel );
	}

	/**
	 * Check if the specified program feature is currently writable. This is
	 * {@code true} if the {@code isWritableAtLevel()} method returns {@code
	 * true} for the default user level.
	 *
	 * @param name Name of feature to check.
	 *
	 * @return {@code true} if the feature is currently writable.
	 *
	 * @see #isWritableAtAll
	 * @see #isWritableAtLevel
	 * @see #getCurrentUserLevel
	 * @see #isAvailableNow
	 */
	public boolean isWritableNow( @NotNull final String name )
	{
		return isWritableAtLevel( name, getCurrentUserLevel() );
	}

	/**
	 * Returns a resource for the specified program feature.
	 *
	 * @param name         Name of feature.
	 * @param resource     Resource key.
	 * @param defaultValue Default value.
	 *
	 * @return Resource or default value.
	 */
	@Nullable
	public Object getResource( @NotNull final String name, @NotNull final String resource, @Nullable final Object defaultValue )
	{
		final Feature feature = lookup( name );
		return ( ( feature != null ) && feature._resources.containsKey( resource ) ) ? feature._resources.get( resource ) : defaultValue;
	}

	/**
	 * Returns a localized string from a program feature resource. If the
	 * resource is not defined ({@code null}), the result is an empty localized
	 * string.
	 *
	 * @param name        Program feature name.
	 * @param resourceKey Resource key.
	 *
	 * @return Localized string.
	 */
	@NotNull
	public LocalizedString getLocalizedStringResource( @NotNull final String name, @NotNull final String resourceKey )
	{
		final Feature feature = lookup( name );
		return feature != null ? feature.getLocalizedStringResource( resourceKey ) : new LocalizedString();
	}

	/**
	 * Sets a resource for the specified program feature. Setting a value of
	 * {@code null} removes the resource.
	 *
	 * @param name     Name of feature.
	 * @param resource Resource key.
	 * @param value    Value to set; {@code null} to remove.
	 */
	public void setResource( @NotNull final String name, @NotNull final String resource, @Nullable final Object value )
	{
		if ( value == null )
		{
			final Feature feature = lookup( name );
			if ( feature != null )
			{
				feature.setResource( resource, null );
				if ( feature.isEmpty() )
				{
					_features.remove( name );
				}
			}
		}
		else
		{
			final Feature feature = lookupOrCreate( name );
			feature.setResource( resource, value );
		}
	}

	/**
	 * Load feature set from a stream. The stream is assumed to use ISO 8859-1
	 * character encoding (for consistency with the {@link Properties} class).
	 *
	 * @param in Stream to read features from.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void load( @NotNull final InputStream in )
	throws IOException
	{
		load( new InputStreamReader( in, "ISO-8859-1" ) );
	}

	/**
	 * Load feature set from a stream.
	 *
	 * @param in Stream to read features from.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	public void load( @NotNull final Reader in )
	throws IOException
	{
		final BufferedReader bIn = in instanceof BufferedReader ? (BufferedReader)in : new BufferedReader( in );
		loadAutoDetectType( bIn );
	}

	/**
	 * Load feature set from a stream, automatically detecting the content type
	 * of the stream (JSON or Properties).
	 *
	 * @param in Stream to read features from.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	private void loadAutoDetectType( final BufferedReader in )
	throws IOException
	{
		in.mark( 1 );
		final boolean isJSON = (char)in.read() == '{';
		in.reset();

		if ( isJSON )
		{
			load( new JSONObject( new JSONTokener( in ) ) );
		}
		else
		{
			final Properties properties = new Properties();
			properties.load( in );
			load( properties );
		}
	}

	/**
	 * Load feature set from a string.
	 *
	 * @param source String to read features from.
	 */
	public void load( @NotNull final String source )
	{
		if ( TextTools.startsWith( source, '{' ) )
		{
			final JSONObject programFeaturesAsJson = new JSONObject( source );
			load( programFeaturesAsJson );
		}
		else
		{
			load( PropertyTools.fromString( source ) );
		}
	}

	/**
	 * Load feature set from a {@link Properties} object. Values should be in
	 * the format:
	 * <pre>
	 *    [{availabilityLevel}[,{writabilityLevel}]]
	 * </pre>
	 * If the {@code writabilityLevel} is not specified, it will be set to the
	 * same value as {@code availabilityLevel}; if neither is specified, then
	 * both levels are set to {@link UserLevel#ALWAYS}.
	 *
	 * If the {@code writabilityLevel} value is lower than the {@code
	 * availabilityLevel} value, than the {@code writabilityLevel} will be
	 * raised to the same value as {@code availabilityLevel}.
	 *
	 * @param properties A {@link Properties} to load features from.
	 */
	public void load( @NotNull final Properties properties )
	{
		for ( final String name : properties.stringPropertyNames() )
		{
			final String value = properties.getProperty( name );
			final String[] tokens = TextTools.tokenize( value, ',' );

			final UserLevel availabilityLevel = ( tokens.length > 0 ) ? ProgramFeatures.parseUserLevel( tokens[ 0 ] ) : UserLevel.ALWAYS;
			final UserLevel writabilityLevel = ( tokens.length > 1 ) ? ProgramFeatures.parseUserLevel( tokens[ 1 ] ) : availabilityLevel;

			setFeature( name, availabilityLevel, writabilityLevel );
		}
	}

	/**
	 * Loads program features from the given JSON object.
	 *
	 * @param json JSON object.
	 */
	public void load( @NotNull final JSONObject json )
	{
		for ( final String name : json.keySet() )
		{
			final JSONObject jsonFeature = json.getJSONObject( name );

			final UserLevel availabilityLevel = jsonFeature.optEnum( UserLevel.class, "read" );
			final UserLevel writabilityLevel = jsonFeature.optEnum( UserLevel.class, "write", availabilityLevel );
			if ( ( availabilityLevel != null ) && ( writabilityLevel != null ) )
			{
				setFeature( name, availabilityLevel, writabilityLevel );
			}

			final JSONObject jsonResources = jsonFeature.optJSONObject( "resources" );
			if ( jsonResources != null )
			{
				for ( final String resourceKey : jsonResources.keySet() )
				{
					setResource( name, resourceKey, jsonResources.get( resourceKey ) );
				}
			}
		}
	}

	/**
	 * Save feature set to a {@link Properties} object. Program features
	 * resources as not included.
	 *
	 * This is written with values written in the format:
	 * <pre>
	 *    [{availabilityLevel}[,{writabilityLevel}]]
	 * </pre>
	 * If the {@code writabilityLevel} is not specified, it will be set to the
	 * same value as {@code availabilityLevel}; if neither is specified, then
	 * both levels are set to {@link UserLevel#ALWAYS}.
	 *
	 * If the {@code writabilityLevel} value is lower than the {@code
	 * availabilityLevel} value, than the {@code writabilityLevel} will be
	 * raised to the same value as {@code availabilityLevel}.
	 *
	 * @param properties A {@link Properties} to save features to.
	 */
	public void save( @NotNull final Properties properties )
	{
		final StringBuilder builder = new StringBuilder();

		for ( final Map.Entry<String, Feature> entry : _features.entrySet() )
		{
			final String name = entry.getKey();
			final Feature feature = entry.getValue();
			if ( feature != null )
			{
				final UserLevel availabilityLevel = feature._availabilityLevel;
				final UserLevel writabilityLevel = feature._writabilityLevel;

				if ( ( availabilityLevel != null ) && ( writabilityLevel != null ) )
				{
					builder.setLength( 0 );
					builder.append( ProgramFeatures.formatUserLevel( availabilityLevel ) );
					if ( availabilityLevel != writabilityLevel )
					{
						builder.append( ',' );
						builder.append( ProgramFeatures.formatUserLevel( writabilityLevel ) );
					}

					properties.setProperty( name, builder.toString() );
				}
			}
		}
	}

	/**
	 * Saves the program features as a JSON object.
	 *
	 * @return Program features as a JSON object.
	 */
	public JSONObject save()
	{
		final JSONObject result = new JSONObject();

		for ( final Map.Entry<String, BasicProgramFeatures.Feature> featureEntry : getFeatures().entrySet() )
		{
			final BasicProgramFeatures.Feature feature = featureEntry.getValue();
			if ( !feature.isEmpty() )
			{
				final JSONObject jsonFeature = new JSONObject();

				final UserLevel availabilityLevel = feature.getAvailabilityLevel();
				final UserLevel writabilityLevel = feature.getWritabilityLevel();
				if ( ( availabilityLevel != null ) && ( writabilityLevel != null ) )
				{
					jsonFeature.put( "read", availabilityLevel );
					if ( availabilityLevel != writabilityLevel )
					{
						jsonFeature.put( "write", writabilityLevel );
					}
				}

				final Map<String, Object> resources = feature.getResources();
				if ( !resources.isEmpty() )
				{
					final JSONObject jsonResources = new JSONObject();
					for ( final Map.Entry<String, Object> resourceEntry : resources.entrySet() )
					{
						final String resourceKey = resourceEntry.getKey();
						Object resourceValue = resourceEntry.getValue();
						if ( resourceValue instanceof LocalizedString )
						{
							resourceValue = new JSONObject( ( (LocalizedString)resourceValue ).toMap() );
						}
						else
						{
							resourceValue = JSONObject.wrap( resourceValue );
						}
						jsonResources.put( resourceKey, resourceValue );
					}
					jsonFeature.put( "resources", jsonResources );
				}

				result.put( featureEntry.getKey(), jsonFeature );
			}
		}

		return result;
	}

	/**
	 * Internal method to look-up a feature by its name.
	 *
	 * @param name Name of feature to look-up.
	 *
	 * @return Feature that was requested; {@code null} if the feature is not
	 * registered yet.
	 */
	@Nullable
	Feature lookup( @NotNull final String name )
	{
		final Map<String, Feature> features = _features;
		Feature result = features.get( name );

		if ( result == null )
		{
			if ( _unknownFeatureWarningsEnabled && !features.containsKey( name ) )
			{
				System.err.println( "WARNING: Unknown feature '" + name + "' requested." );
				features.put( name, null );
			}

			result = null;
		}

		return result;
	}

	/**
	 * Internal method to look-up a feature by its name.
	 *
	 * @param name Name of feature to look-up.
	 *
	 * @return Feature that was requested.
	 */
	@NotNull
	Feature lookupOrCreate( @NotNull final String name )
	{
		Feature result = _features.get( name );

		if ( result == null )
		{
			result = new Feature();
			_features.put( name, result );
		}

		return result;
	}

	/**
	 * This class is used internally to store feature properties.
	 */
	public static class Feature
	{
		/**
		 * User level at which the feature becomes available; {@code null} if
		 * not set.
		 */
		@Nullable
		UserLevel _availabilityLevel = null;

		/**
		 * User level at which the feature becomes writable; {@code null} if not
		 * set.
		 */
		@Nullable
		UserLevel _writabilityLevel = null;

		/**
		 * Resources configured for this program feature.
		 */
		final Map<String, Object> _resources = new HashMap<String, Object>();

		/**
		 * Constructs a new instance.
		 */
		public Feature()
		{
		}

		/**
		 * Constructs a deep copy of the given instance.
		 *
		 * @param original Instance to copy.
		 */
		public Feature( @NotNull final Feature original )
		{
			_availabilityLevel = original._availabilityLevel;
			_writabilityLevel = original._writabilityLevel;
			_resources.putAll( original._resources );
		}

		@Nullable
		public UserLevel getAvailabilityLevel()
		{
			return _availabilityLevel;
		}

		public void setAvailabilityLevel( @Nullable final UserLevel availabilityLevel )
		{
			_availabilityLevel = availabilityLevel;
		}

		@Nullable
		public UserLevel getWritabilityLevel()
		{
			return _writabilityLevel;
		}

		public void setWritabilityLevel( @Nullable final UserLevel writabilityLevel )
		{
			_writabilityLevel = writabilityLevel;
		}

		/**
		 * Returns a mutable map with the resources for the program feature.
		 *
		 * @return Mutable map of resources.
		 */
		public Map<String, Object> getResources()
		{
			//noinspection ReturnOfCollectionOrArrayField
			return _resources;
		}

		/**
		 * Returns a resource for this program feature.
		 *
		 * @param key Resource key.
		 *
		 * @return Resource or {@code null}.
		 */
		@Nullable
		public Object getResource( @NotNull final String key )
		{
			return getResource( key, null );
		}

		/**
		 * Returns a resource for this program feature.
		 *
		 * @param key          Resource key.
		 * @param defaultValue Default value.
		 *
		 * @return Resource or default value.
		 */
		@Nullable
		public Object getResource( @NotNull final String key, @Nullable final Object defaultValue )
		{
			final Object result = _resources.get( key );
			return result == null ? defaultValue : result;
		}

		/**
		 * Returns a localized string resource for this program feature. If the
		 * resource is not defined, an empty localized string is returned.
		 *
		 * @param key Resource key.
		 *
		 * @return Localized string.
		 */
		public LocalizedString getLocalizedStringResource( final String key )
		{
			final Object resource = getResource( key, null );

			final LocalizedString result;
			if ( resource == null )
			{
				result = new LocalizedString();
			}
			else if ( resource instanceof LocalizedString )
			{
				result = (LocalizedString)resource;
			}
			else if ( resource instanceof JSONObject )
			{
				result = new LocalizedString();
				final JSONObject json = (JSONObject)resource;
				for ( final String locale : json.keySet() )
				{
					result.set( locale, json.getString( locale ) );
				}
			}
			else
			{
				throw new IllegalArgumentException( "Unsupported resource type: " + resource.getClass() );
			}

			return result;
		}

		/**
		 * Sets a resource for this program feature. Setting a value of {@code
		 * null} removes the resource.
		 *
		 * @param key   Resource key.
		 * @param value Value to set; {@code null} to remove.
		 */
		public void setResource( @NotNull final String key, @Nullable final Object value )
		{
			if ( value == null )
			{
				_resources.remove( key );
			}
			else
			{
				_resources.put( key, value );
			}
		}

		/**
		 * Returns whether all properties are {@code null} or empty.
		 *
		 * @return {@code true} if empty.
		 */
		public boolean isEmpty()
		{
			return ( _availabilityLevel == null ) && ( _writabilityLevel == null ) && _resources.isEmpty();
		}

		@Override
		public boolean equals( final Object obj )
		{
			boolean result = obj instanceof Feature;
			if ( result )
			{
				final Feature other = (Feature)obj;
				result = MathTools.equals( _availabilityLevel, other._availabilityLevel ) &&
				         MathTools.equals( _writabilityLevel, other._writabilityLevel ) &&
				         _resources.equals( other._resources );
			}
			return result;
		}

		@Override
		public int hashCode()
		{
			return ( _availabilityLevel == null ? 0 : _availabilityLevel.hashCode() ) ^
			       ( _writabilityLevel == null ? 0 : _writabilityLevel.hashCode() ) ^
			       _resources.hashCode();
		}
	}
}
