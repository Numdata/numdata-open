/*
 * Copyright (c) 2005-2017, Numdata BV, The Netherlands.
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

import static com.numdata.oss.deployment.ProgramFeatures.UserLevel.*;

/**
 * This class defines a method to define configurable program features. This can
 * be used, for example, to define features depending on the user level.
 *
 * @author Peter S. Heijnen
 */
public class ProgramFeatures
{
	/**
	 * Defines access levels for users.
	 */
	public enum UserLevel
	{
		/**
		 * User level for something that should be true for any user level.
		 */
		ALWAYS,

		/**
		 * Proposed user level intended for observers. An observer is a person, who
		 * should be able to view data, but not modify anything.
		 */
		OBSERVER,

		/**
		 * Proposed user level intended for novice users. A novice user has limited
		 * knowledge of the software and should not be confronted with complex
		 * features, and should be prevented from modifying advanced settings.
		 */
		NOVICE,

		/**
		 * Proposed user level intended for beginning users. A beginner user has
		 * basic knowledge of the software and is able to use all the
		 * fundamental features of the software.
		 */
		BEGINNER,

		/**
		 * Proposed user level intended for normal users. This is the typical
		 * (experienced) user of the software and should have full access to most
		 * features. He may, however, be prevented from modifying critical data,
		 * typically related to the general operation of the software.
		 */
		NORMAL,

		/**
		 * Proposed user level intended for advanced users. This type of user
		 * may require advanced features to accomplish complex tasks.
		 */
		ADVANCED,

		/**
		 * Proposed user level intended for expert users. This type of user needs
		 * full control over even the most advanced features of the software. The
		 * only type of data he should nog be allowed to change, is data related
		 * to the configuration of the software itself.
		 */
		EXPERT,

		/**
		 * Proposed user level intended for software configurators. This is the
		 * highest practical user level. It should only be used during software
		 * configuration and system administration tasks. Typical settings would
		 * include program directories, network connections, and user access rights.
		 */
		CONFIGURATOR,

		/**
		 * Non-practical user-level that exceeds all others, to be used during
		 * software development, debugging, etc. This may be used to enable stack
		 * traces, memory dumps, program traps, etc. Should not be used for
		 * normal operation of the software.
		 */
		DEVELOPER,

		/**
		 * User level for something that should be false for any user level.
		 */
		NEVER;

		/**
		 * Returns the highest user level from the given levels.
		 *
		 * @param   levels  User levels.
		 *
		 * @return  Highest user level.
		 *
		 * @throws  IllegalArgumentException if no levels are specified.
		 */
		public static UserLevel highest( final UserLevel... levels )
		{
			if ( ( levels == null ) || ( levels.length == 0 ) )
			{
				throw new IllegalArgumentException( "levels" );
			}

			UserLevel result = levels[ 0 ];
			for ( final UserLevel level : levels )
			{
				if ( level.ordinal() > result.ordinal() )
				{
					result = level;
				}
			}

			return result;
		}

		/**
		 * Returns whether this user level includes more features than the given
		 * user level.
		 *
		 * @param   level   User level.
		 *
		 * @return  {@code true} if this user level includes more features
		 *          than the given user level;
		 *          {@code false} otherwise.
		 */
		public boolean isHigherThan( final UserLevel level )
		{
			return ordinal() > level.ordinal();
		}

		/**
		 * Returns whether this user level includes at least all features
		 * included in the given user level.
		 *
		 * @param   level   User level.
		 *
		 * @return  {@code true} if this user level includes at least all
		 *          features included in the given user level;
		 *          {@code false} otherwise.
		 */
		public boolean isAtLeast( final UserLevel level )
		{
			return ordinal() >= level.ordinal();
		}
	}

	/**
	 * Utility class is not supposed to be instantiated.
	 */
	private ProgramFeatures()
	{
	}

	/**
	 * Parse user level from string value. If the string value is incorrect,
	 * then {@link UserLevel#ALWAYS} will be returned.
	 *
	 * @param   level   String value with user level to parse.
	 *
	 * @return  User level.
	 */
	static UserLevel parseUserLevel( final String level )
	{
		UserLevel result = ALWAYS;

		if ( level != null )
		{
			final String trimmed   = level.trim();
			final String uppercase = trimmed.toUpperCase();

			try
			{
				result = UserLevel.valueOf( uppercase );
			}
			catch ( IllegalArgumentException e )
			{
				/* ignore, will return ALWAYS */
			}
		}

		return result;
	}

	/**
	 * Parse user level from enumeration value.
	 *
	 * @param   level   String value with user level to parse.
	 *
	 * @return  User level.
	 */
	static String formatUserLevel( final UserLevel level )
	{
		final String result = level.name();
		return result.toLowerCase();
	}
}
