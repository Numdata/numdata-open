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
package com.numdata.commons.java8;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link StreamTools}.
 *
 * @author Peter S. Heijnen
 */
public class TestStreamTools
{
	@Test
	public void testStream()
	{
		assertEquals( "StreamTools.stream( [ 1, 2, 3 ] )", Arrays.asList( 1, 2, 3 ), StreamTools.stream( Arrays.asList( 1, 2, 3 ) ).collect( toList() ) );
	}

	@Test
	public void testToMap()
	{
		{
			final Map<String, Integer> input = new HashMap<>();
			input.put( "one", 1 );
			input.put( "two", 2 );
			input.put( "three", 3 );

			final Map<String, Integer> expected = new TreeMap<>();
			expected.put( "one", 1 );
			expected.put( "three", 3 );
			expected.put( "two", 2 );

			assertEquals( "[ 'one'->1, 'two'->2, 'three'->3 ].collect( StreamTools.toMap(...) )", expected, input.entrySet().stream().collect( StreamTools.toMap( Map.Entry::getKey, Map.Entry::getValue, TreeMap::new ) ) );
		}

		try
		{
			final List<Map.Entry<String, Integer>> input = Arrays.asList(
			new AbstractMap.SimpleEntry<>( "nodupe", 1 ),
			new AbstractMap.SimpleEntry<>( "dupe", 2 ),
			new AbstractMap.SimpleEntry<>( "dupe", 3 ) );

			//noinspection ResultOfMethodCallIgnored
			input.stream().collect( StreamTools.toMap( Map.Entry::getKey, Map.Entry::getValue, HashMap::new ) );
			fail( "[ 'nodupe'->1, 'dupe'->2, 'dupe'->3 ].collect( StreamTools.toMap(...) ) should have failed" );
		}
		catch ( final IllegalStateException ignored )
		{
		}
	}

	@Test
	public void testToSet()
	{
		assertSame( "StreamTools.toSet( Stream.empty() )", Collections.emptySet(), StreamTools.toSet( Stream.empty() ) );
		assertEquals( "StreamTools.toSet( Stream.of( 3, 3, 2, 2, 1, 1 ) )", new HashSet<>( Arrays.asList( 1, 2, 3 ) ), StreamTools.toSet( Stream.of( 3, 3, 2, 2, 1, 1 ).parallel() ) );
		assertEquals( "StreamTools.toSet( Stream.of( 3, 3, 2, 2, 1, 1 ), TreeSet::new )", new TreeSet<>( Arrays.asList( 1, 2, 3 ) ), StreamTools.toSet( Stream.of( 3, 3, 2, 2, 1, 1 ).parallel(), TreeSet::new ) );
		assertEquals( "StreamTools.toSet( Stream.of( 3, 3, 2, 2, 1, 1 ), LinkedHashSet::new )", new LinkedHashSet<>( Arrays.asList( 3, 2, 1 ) ), StreamTools.toSet( Stream.of( 3, 3, 2, 2, 1, 1 ).parallel(), LinkedHashSet::new ) );

		StreamTools.toSet( (Stream<Integer>)Proxy.newProxyInstance( Stream.class.getClassLoader(), new Class<?>[] { Stream.class }, ( proxy, method, args ) -> {
			assertEquals( "Expected call to reduce() method, but was: " + method, "reduce", method.getName() );
			assertEquals( "Expected call to reduce(identity,accumulator,combiner) method, but was: " + method, 3, args.length );

			assertSame( "Expected 'identity' to be 'Collections.emptySet()'", Collections.emptySet(), args[ 0 ] );

			final BiFunction<Set<Integer>, Integer, Set<Integer>> accumulator = (BiFunction<Set<Integer>, Integer, Set<Integer>>)args[ 1 ];
			final Set<Integer> set = accumulator.apply( Collections.emptySet(), 1 );
			assertEquals( "'accumulator.apply( empty, 1 )' should return 'LinkedHashSet'", LinkedHashSet.class, set.getClass() );
			assertEquals( "'accumulator.apply( empty, 1 )' should have added 1", new LinkedHashSet<>( Collections.singletonList( 1 ) ), set );
			assertSame( "'accumulator.apply( set, 2 )' should return 'set'", set, accumulator.apply( set, 2 ) );
			assertEquals( "'accumulator.apply( set, 2 )' have added 2", new LinkedHashSet<>( Arrays.asList( 1, 2 ) ), set );

			final BinaryOperator<Set<Integer>> combiner = (BinaryOperator<Set<Integer>>)args[ 2 ];

			final Set<Integer> empty = Collections.emptySet();
			final Set<Integer> set1 = new HashSet<>( Arrays.asList( 1, 2, 3 ) );
			final Set<Integer> set2 = new HashSet<>( Arrays.asList( 3, 4, 5 ) );
			assertSame( "'combiner.apply( empty, set1 )' should return 'set1'", set1, combiner.apply( empty, set1 ) );
			assertSame( "'combiner.apply( set1, empty )' should return 'set1'", set1, combiner.apply( set1, empty ) );
			assertSame( "'combiner.apply( set1, set2 )' should return 'set1'", set1, combiner.apply( set1, set2 ) );
			assertEquals( "'combiner.apply( set1, set2 )' should updated 'set1'", new HashSet<>( Arrays.asList( 1, 2, 3, 4, 5 ) ), set1 );
			assertEquals( "'combiner.apply( set1, set2 )' should have left 'set2' unchanged", new HashSet<>( Arrays.asList( 3, 4, 5 ) ), set2 );

			return Collections.emptySet();
		} ), LinkedHashSet::new );
	}
}
