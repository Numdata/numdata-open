/*
 * Copyright (c) 2017-2021, Unicon Creation BV, The Netherlands.
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

import java.util.*;

import com.numdata.oss.deployment.ProgramFeatures.*;
import static com.numdata.oss.deployment.ProgramFeatures.UserLevel.*;
import com.numdata.oss.junit.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link BasicProgramFeatures}.
 *
 * @author Peter S. Heijnen
 */
public class TestBasicProgramFeatures
{
	@Test
	public void testGetCurrentUserLevel()
	{
		final BasicProgramFeatures programFeatures = new BasicProgramFeatures();
		programFeatures.setDefaultUserLevel( CONFIGURATOR );
		programFeatures.setExtendedUserLevel( DEVELOPER );

		programFeatures.setExtendedUserLevelEnabled( false );
		assertEquals( "Did not get default user level", CONFIGURATOR, programFeatures.getCurrentUserLevel() );

		programFeatures.setExtendedUserLevelEnabled( true );
		assertEquals( "Did not get extended user level", DEVELOPER, programFeatures.getCurrentUserLevel() );
	}

	@Test
	public void testIsDifferentForDefaultAndExtendedLevel()
	{
		final String here = getClass().getName() + ".testIsDifferentForDefaultAndExtendedLevel()";

		class Test
		{
			final UserLevel _availabilityLevel;

			final UserLevel _writabilityLevel;

			final UserLevel _defaultLevel;

			final UserLevel _extendedLevel;

			final boolean _expected;

			private Test( final UserLevel availabilityLevel, final UserLevel writabilityLevel, final UserLevel defaultLevel, final UserLevel extendedLevel, final boolean expected )
			{
				_availabilityLevel = availabilityLevel;
				_writabilityLevel = writabilityLevel;
				_defaultLevel = defaultLevel;
				_extendedLevel = extendedLevel;
				_expected = expected;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
		/* Test #1 */ new Test( NORMAL, NEVER, NORMAL, EXPERT, false ),
		/* Test #2 */ new Test( ALWAYS, NORMAL, NORMAL, EXPERT, false ),
		/* Test #3 */ new Test( ALWAYS, EXPERT, NORMAL, EXPERT, true ),
		/* Test #4 */ new Test( OBSERVER, NOVICE, NORMAL, EXPERT, false ),
		/* Test #5 */ new Test( NOVICE, NORMAL, NORMAL, EXPERT, false ),
		/* Test #6 */ new Test( NORMAL, EXPERT, NORMAL, EXPERT, true ),
		/* Test #7 */ new Test( EXPERT, EXPERT, NORMAL, EXPERT, true ),
		/* Test #8 */ new Test( CONFIGURATOR, DEVELOPER, NORMAL, EXPERT, false ),
		/* Test #9 */ new Test( DEVELOPER, DEVELOPER, NORMAL, EXPERT, false ),
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final UserLevel availabilityLevel = test._availabilityLevel;
			final UserLevel writabilityLevel = test._writabilityLevel;
			final UserLevel defaultLevel = test._defaultLevel;
			final UserLevel extendedLevel = test._extendedLevel;
			final boolean expected = test._expected;
			final String description = "Test #" + ( i + 1 );

			System.out.println( " - " + description +
			                    ": availabilityLevel=" + availabilityLevel +
			                    ", writabilityLevel=" + writabilityLevel +
			                    ", defaultLevel=" + defaultLevel +
			                    ", extendedLevel=" + extendedLevel );

			final String name = here;

			final BasicProgramFeatures programFeatures = new BasicProgramFeatures();
			final BasicProgramFeatures.Feature feature = programFeatures.lookupOrCreate( name );

			assertNotNull( "Missing feature: " + name, feature );
			feature._availabilityLevel = availabilityLevel;
			feature._writabilityLevel = writabilityLevel;

			programFeatures.setDefaultUserLevel( defaultLevel );
			programFeatures.setExtendedUserLevel( extendedLevel );

			assertEquals( description, expected, programFeatures.isDifferentForDefaultAndExtendedLevel( name ) );

			programFeatures.setDefaultUserLevel( extendedLevel );
			programFeatures.setExtendedUserLevel( defaultLevel );

			assertEquals( description + " - reversed", expected, programFeatures.isDifferentForDefaultAndExtendedLevel( name ) );
		}
	}

	@Test
	public void testIsDifferentForLevels()
	{
		final String here = getClass().getName() + ".testIsDifferentForLevels()";

		class Test
		{
			final UserLevel _availabilityLevel;

			final UserLevel _writabilityLevel;

			final UserLevel _level1;

			final UserLevel _level2;

			final boolean _expected;

			private Test( final UserLevel availabilityLevel, final UserLevel writabilityLevel, final UserLevel level1, final UserLevel level2, final boolean expected )
			{
				_availabilityLevel = availabilityLevel;
				_writabilityLevel = writabilityLevel;
				_level1 = level1;
				_level2 = level2;
				_expected = expected;
			}
		}

		/*
		 * Define tests to execute.
		 */
		final Test[] tests =
		{
		/* Test #1 */ new Test( NORMAL, NEVER, NORMAL, EXPERT, false ),
		/* Test #2 */ new Test( ALWAYS, NORMAL, NORMAL, EXPERT, false ),
		/* Test #3 */ new Test( ALWAYS, EXPERT, NORMAL, EXPERT, true ),
		/* Test #4 */ new Test( OBSERVER, NOVICE, NORMAL, EXPERT, false ),
		/* Test #5 */ new Test( NOVICE, NORMAL, NORMAL, EXPERT, false ),
		/* Test #6 */ new Test( NORMAL, EXPERT, NORMAL, EXPERT, true ),
		/* Test #7 */ new Test( EXPERT, EXPERT, NORMAL, EXPERT, true ),
		/* Test #8 */ new Test( CONFIGURATOR, DEVELOPER, NORMAL, EXPERT, false ),
		/* Test #9 */ new Test( DEVELOPER, DEVELOPER, NORMAL, EXPERT, false ),
		};

		/*
		 * Execute tests.
		 */
		for ( int i = 0; i < tests.length; i++ )
		{
			final Test test = tests[ i ];
			final UserLevel availabilityLevel = test._availabilityLevel;
			final UserLevel writabilityLevel = test._writabilityLevel;
			final UserLevel level1 = test._level1;
			final UserLevel level2 = test._level2;
			final boolean expected = test._expected;
			final String description = "Test #" + ( i + 1 );

			System.out.println( " - " + description +
			                    ": availabilityLevel=" + availabilityLevel +
			                    ", writabilityLevel=" + writabilityLevel +
			                    ", level1=" + level1 +
			                    ", level2=" + level2 );

			final String name = here;

			final BasicProgramFeatures programFeatures = new BasicProgramFeatures();
			final BasicProgramFeatures.Feature feature = programFeatures.lookupOrCreate( name );

			assertNotNull( "Missing feature: " + name, feature );
			feature._availabilityLevel = availabilityLevel;
			feature._writabilityLevel = writabilityLevel;

			assertEquals( description + " - level1 vs level2", expected, programFeatures.isDifferentForLevels( name, level1, level2 ) );
			assertEquals( description + " - level2 vs level1", expected, programFeatures.isDifferentForLevels( name, level2, level1 ) );
		}
	}

	@Test
	public void testIsWritableAtAll()
	{
		final String here = getClass().getName() + ".testIsWritableAtAll()";

		final BasicProgramFeatures programFeatures = new BasicProgramFeatures();
		programFeatures.disableUnknownFeatureWarnings();

		assertTrue( "isWritableAtAll( 'non-existing' )", programFeatures.isWritableAtAll( here + "-non-existing" ) );

		final String alwaysName = here + "-ALWAYS";
		assertNull( "Should not already have 'ALWAYS' feature object", programFeatures.lookup( alwaysName ) );
		final BasicProgramFeatures.Feature always = programFeatures.lookupOrCreate( alwaysName );
		assertNotNull( "Missing feature: " + alwaysName, always );
		always._availabilityLevel = ALWAYS;
		always._writabilityLevel = ALWAYS;
		assertTrue( "isWritableAtAll( 'non-existing' )", programFeatures.isWritableAtAll( alwaysName ) );

		final String normalName = here + "-NORMAL";
		assertNull( "Should not already have 'NORMAL' feature object", programFeatures.lookup( normalName ) );
		final BasicProgramFeatures.Feature normal = programFeatures.lookupOrCreate( normalName );
		assertNotNull( "Missing feature: " + normalName, normal );
		normal._availabilityLevel = NORMAL;
		normal._writabilityLevel = NORMAL;
		assertTrue( "isWritableAtAll( 'non-existing' )", programFeatures.isWritableAtAll( normalName ) );

		final String neverName = here + "-NEVER";
		assertNull( "Should not already have 'NEVER' feature object", programFeatures.lookup( neverName ) );
		final BasicProgramFeatures.Feature never = programFeatures.lookupOrCreate( neverName );
		assertNotNull( "Missing feature: " + neverName, never );
		never._availabilityLevel = NEVER;
		never._writabilityLevel = NEVER;
		assertFalse( "isWritableAtAll( 'non-existing' )", programFeatures.isWritableAtAll( neverName ) );
	}

	@Test
	public void testSaveLoadJson()
	{
		final BasicProgramFeatures programFeatures1 = new BasicProgramFeatures();

		final BasicProgramFeatures.Feature feature1 = programFeatures1.lookupOrCreate( "feature1" );
		feature1.setAvailabilityLevel( BEGINNER );
		feature1.setWritabilityLevel( EXPERT );

		final BasicProgramFeatures.Feature feature2 = programFeatures1.lookupOrCreate( "feature2" );
		feature2.setAvailabilityLevel( NORMAL );

		final BasicProgramFeatures.Feature feature3 = programFeatures1.lookupOrCreate( "feature3" );
		feature3.setAvailabilityLevel( EXPERT );
		feature3.setWritabilityLevel( DEVELOPER );

		final BasicProgramFeatures.Feature feature4 = programFeatures1.lookupOrCreate( "feature4" );
		feature4.setAvailabilityLevel( ALWAYS );
		feature4.setWritabilityLevel( NEVER );

		programFeatures1.save();
	}

	@Test
	public void testResources()
	{
		final Class<?> clazz = ProgramFeatures.class;
		final ResourceBundleTester tester = ResourceBundleTester.forClass( clazz );
		tester.addDeclaredEnums( clazz );
		tester.addExpectedKey( "programFeatures" );
		tester.addExpectedKey( "defaultUserLevel" );
		tester.addExpectedKey( "extendedUserLevel" );
		tester.addExpectedKey( "resources" );
		tester.run();
	}

	@Test
	public void testEnumSets()
	{
		final BasicProgramFeatures features = new BasicProgramFeatures();
		features.setFeature( "set1." + ALWAYS, ALWAYS, OBSERVER );
		features.setFeature( "set1." + OBSERVER, OBSERVER, NOVICE );
		features.setFeature( "set1." + NOVICE, NOVICE, BEGINNER );
		features.setFeature( "set1." + BEGINNER, BEGINNER, NORMAL );
		features.setFeature( "set1." + NORMAL, NORMAL, ADVANCED );
		features.setFeature( "set1." + ADVANCED, ADVANCED, EXPERT );
		features.setFeature( "set1." + EXPERT, EXPERT, CONFIGURATOR );
		features.setFeature( "set1." + CONFIGURATOR, CONFIGURATOR, DEVELOPER );
		features.setFeature( "set1." + DEVELOPER, DEVELOPER, NEVER );
		features.setFeature( "set1." + NEVER, NEVER );

		features.setDefaultUserLevel( NORMAL );
		features.setExtendedUserLevel( EXPERT );
		features.setExtendedUserLevelEnabled( false );

		assertEquals( "Unexpected 'available now' set", EnumSet.of( ALWAYS, OBSERVER, NOVICE, BEGINNER, NORMAL ),
		              features.getAvailableNowEnumSet( "set1", ProgramFeatures.UserLevel.class ) );

		assertEquals( "Unexpected 'available at all' set", EnumSet.of( ALWAYS, OBSERVER, NOVICE, BEGINNER, NORMAL, ADVANCED, EXPERT ),
		              features.getAvailableAtAllEnumSet( "set1", ProgramFeatures.UserLevel.class ) );

		assertEquals( "Unexpected 'writable now' set", EnumSet.of( ALWAYS, OBSERVER, NOVICE, BEGINNER ),
		              features.getWritableNowEnumSet( "set1", ProgramFeatures.UserLevel.class ) );

		assertEquals( "Unexpected 'writable at all' set", EnumSet.of( ALWAYS, OBSERVER, NOVICE, BEGINNER, NORMAL, ADVANCED ),
		              features.getWritableAtAllEnumSet( "set1", ProgramFeatures.UserLevel.class ) );

		features.setExtendedUserLevelEnabled( true );

		assertEquals( "Unexpected 'available now' set", EnumSet.of( ALWAYS, OBSERVER, NOVICE, BEGINNER, NORMAL, ADVANCED, EXPERT ),
		              features.getAvailableNowEnumSet( "set1", ProgramFeatures.UserLevel.class ) );

		assertEquals( "Unexpected 'available at all' set", EnumSet.of( ALWAYS, OBSERVER, NOVICE, BEGINNER, NORMAL, ADVANCED, EXPERT ),
		              features.getAvailableAtAllEnumSet( "set1", ProgramFeatures.UserLevel.class ) );

		assertEquals( "Unexpected 'writable now' set", EnumSet.of( ALWAYS, OBSERVER, NOVICE, BEGINNER, NORMAL, ADVANCED ),
		              features.getWritableNowEnumSet( "set1", ProgramFeatures.UserLevel.class ) );

		assertEquals( "Unexpected 'writable at all' set", EnumSet.of( ALWAYS, OBSERVER, NOVICE, BEGINNER, NORMAL, ADVANCED ),
		              features.getWritableAtAllEnumSet( "set1", ProgramFeatures.UserLevel.class ) );
	}
}
