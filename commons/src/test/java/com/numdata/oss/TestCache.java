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

import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.*;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test for the {@link Cache} class.
 *
 * @author G. Meinders
 */
@SuppressWarnings( "UnsecureRandomNumberGeneration" )
public class TestCache
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestCache.class.getName();

	/**
	 * Instance being tested.
	 */
	private Cache<?, ?> _cache = null;

	@Before
	public void setUp()
	throws Exception
	{
		final Cache.DefaultCachingPolicy policy = new Cache.DefaultCachingPolicy( 0.5, 2, 2, 4 );
		_cache = new Cache<Object, Object>( policy, LinkedHashMap.class );
	}

	@After
	public void tearDown()
	throws Exception
	{
		_cache = null;
	}

	/**
	 * Tests insertion of key-value pairs into the cache.
	 */
	@Test
	public void testPut()
	{
		System.out.println( CLASS_NAME + ".testPut" );
		final Cache<String, Object> cache = (Cache<String, Object>)_cache;

		final Object value1 = new Object();
		final Object value2 = new Object();
		final Object value3 = new Object();

		/*
		 * Insert entries.
		 */
		assertNull( "Unexpected value.", cache.put( "hello", value1 ) );
		assertNull( "Unexpected value.", cache.put( "caching", value2 ) );
		assertNull( "Unexpected value.", cache.put( "map", value3 ) );
		assertNull( "Unexpected value.", cache.put( "world", null ) );

		/*
		 * Check that all keys are present.
		 */
		assertTrue( "Missing entry.", cache.containsKey( "hello" ) );
		assertTrue( "Missing entry.", cache.containsKey( "caching" ) );
		assertTrue( "Missing entry.", cache.containsKey( "map" ) );
		assertTrue( "Missing entry.", cache.containsKey( "world" ) );

		/*
		 * Check that all values are correct.
		 */
		assertEquals( "Unexpected value.", value1, cache.get( "hello" ) );
		assertEquals( "Unexpected value.", value2, cache.get( "caching" ) );
		assertEquals( "Unexpected value.", value3, cache.get( "map" ) );
		assertNull( "Unexpected value.", cache.get( "world" ) );

		/*
		 * Replace values and check the result.
		 */
		assertEquals( "Unexpected value.", value3, cache.put( "map", value2 ) );
		assertEquals( "Unexpected value.", value2, cache.get( "map" ) );

		assertNull( "Unexpected value.", cache.put( "world", value3 ) );
		assertEquals( "Unexpected value.", value3, cache.get( "world" ) );
	}

	/**
	 * Tests iteration of the cache's 'keySet', 'values' and 'entrySet'.
	 */
	@Test
	public void testIterators()
	{
		System.out.println( CLASS_NAME + ".testIterators" );
		final Cache<String, Object> cache = (Cache<String, Object>)_cache;

		final Object value1 = new Object();
		final Object value2 = new Object();
		final Object value3 = new Object();

		assertNull( "Unexpected value.", cache.put( "hello", value1 ) );
		assertNull( "Unexpected value.", cache.put( "caching", value2 ) );
		assertNull( "Unexpected value.", cache.put( "map", value2 ) );
		assertNull( "Unexpected value.", cache.put( "world", value3 ) );

		/*
		 * Check items returned by key iterator.
		 */
		final Set<String> keySet = cache.keySet();
		final Iterator<String> keyIterator = keySet.iterator();

		assertEquals( "Unexpected key.", "hello", keyIterator.next() );
		assertEquals( "Unexpected key.", "caching", keyIterator.next() );
		assertEquals( "Unexpected key.", "map", keyIterator.next() );
		assertEquals( "Unexpected key.", "world", keyIterator.next() );

		/*
		 * Check items returned by value iterator.
		 */
		final Collection<Object> values = cache.values();
		final Iterator<Object> valueIterator = values.iterator();

		assertEquals( "Unexpected value.", value1, valueIterator.next() );
		assertEquals( "Unexpected value.", value2, valueIterator.next() );
		assertEquals( "Unexpected value.", value2, valueIterator.next() );
		assertEquals( "Unexpected value.", value3, valueIterator.next() );

		/*
		 * Check items returned by entry iterator.
		 */
		final Set<Map.Entry<String, Object>> entrySet = cache.entrySet();
		final Iterator<Map.Entry<String, Object>> entryIterator = entrySet.iterator();

		Map.Entry<String, Object> entry;
		entry = entryIterator.next();
		assertEquals( "Unexpected key.", "hello", entry.getKey() );
		assertEquals( "Unexpected value.", value1, entry.getValue() );
		entry = entryIterator.next();
		assertEquals( "Unexpected key.", "caching", entry.getKey() );
		assertEquals( "Unexpected value.", value2, entry.getValue() );
		entry = entryIterator.next();
		assertEquals( "Unexpected key.", "map", entry.getKey() );
		assertEquals( "Unexpected value.", value2, entry.getValue() );
		entry = entryIterator.next();
		assertEquals( "Unexpected key.", "world", entry.getKey() );
		assertEquals( "Unexpected value.", value3, entry.getValue() );
	}

	/**
	 * Tests the default caching policy, {@link Cache.DefaultCachingPolicy}.
	 */
	@Test
	public void testDefaultCachingPolicy()
	{
		System.out.println( CLASS_NAME + ".testDefaultCachingPolicy" );
		final Cache<String, Object> cache = (Cache<String, Object>)_cache;

		final Object value1 = new Object();
		final Object value2 = new Object();
		final Object value3 = new Object();
		final Object value4 = new Object();
		final Object value5 = new Object();
		final Object value6 = new Object();
		final Object value7 = new Object();
		final Object value8 = new Object();
		final Object value9 = new Object();
		final Object value10 = new Object();

		/*
		 * Verify that the minimum number of hard references is created.
		 */
		cache.put( "item-1", value1 );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-1" ) );
		assertEquals( "Unexpected number of soft references.", 0, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 1, cache.getHardReferences() );

		cache.put( "item-2", value2 );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-1" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-2" ) );
		assertEquals( "Unexpected number of soft references.", 0, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		/*
		 * Verify that the minimum number of soft references is created.
		 * Verify that hard references are replaced with soft ones in response
		 * to usage (including creation).
		 */
		cache.put( "item-3", value3 );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-1" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-2" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-3" ) );
		assertEquals( "Unexpected number of soft references.", 1, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		cache.put( "item-4", value4 );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-1" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-2" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-3" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-4" ) );
		assertEquals( "Unexpected number of soft references.", 2, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		cache.get( "item-1" );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-1" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-2" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-3" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-4" ) );
		assertEquals( "Unexpected number of soft references.", 2, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		/*
		 * Verify that the softness is applied.
		 * Verify that hard references are replaced with soft ones in response
		 * to usage (including creation).
		 */
		cache.put( "item-5", value5 );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-1" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-2" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-3" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-5" ) );
		assertEquals( "Unexpected number of soft references.", 3, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		cache.put( "item-6", value6 );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-1" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-2" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-3" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-5" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-6" ) );
		assertEquals( "Unexpected number of soft references.", 3, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 3, cache.getHardReferences() );

		/*
		 * Verify that the policy is properly applied on removal.
		 */
		// Remove a hard reference. Remaining references are unaffected.
		cache.remove( "item-1" );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-2" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-3" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-5" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-6" ) );
		assertEquals( "Unexpected number of soft references.", 3, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		// Remove another hard reference. One of the remaining references must be hardened.
		cache.remove( "item-6" );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-2" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-3" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-4" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-5" ) );
		assertEquals( "Unexpected number of soft references.", 2, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		// Restore the cache to its previous size.
		cache.put( "item-6", value6 );
		cache.put( "item-1", value1 );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-1" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-2" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-3" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-4" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-5" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-6" ) );
		assertEquals( "Unexpected number of soft references.", 3, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 3, cache.getHardReferences() );

		// Remove a soft reference. One of the remaining references must be softened.
		cache.remove( "item-2" );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-1" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-3" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-5" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-6" ) );
		assertEquals( "Unexpected number of soft references.", 3, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		// Remove another soft reference. Remaining references are unaffected.
		cache.remove( "item-3" );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-1" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-5" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-6" ) );
		assertEquals( "Unexpected number of soft references.", 2, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		// Restore the cache to its previous size.
		cache.put( "item-3", value3 );
		cache.put( "item-2", value2 );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-1" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-2" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-3" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-5" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-6" ) );
		assertEquals( "Unexpected number of soft references.", 3, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 3, cache.getHardReferences() );

		/*
		 * Verify that the maximum number of hard references is applied.
		 */
		cache.put( "item-7", value7 );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-1" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-2" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-3" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-5" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-6" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-7" ) );
		assertEquals( "Unexpected number of soft references.", 4, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 3, cache.getHardReferences() );

		cache.put( "item-8", value8 );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-1" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-2" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-3" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-5" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-6" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-7" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-8" ) );
		assertEquals( "Unexpected number of soft references.", 4, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 4, cache.getHardReferences() );

		cache.put( "item-9", value9 );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-1" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-2" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-3" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-5" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-6" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-7" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-8" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-9" ) );
		assertEquals( "Unexpected number of soft references.", 5, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 4, cache.getHardReferences() );

		cache.put( "item-10", value10 );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-1" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-2" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-3" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-5" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-6" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-7" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-8" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-9" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-10" ) );
		assertEquals( "Unexpected number of soft references.", 6, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 4, cache.getHardReferences() );

		/*
		 * Verify that the policy properly handles clear and garbage collection.
		 */
		cache.clearSoft2(); // Simulates garbage collection under memory demand.
		assertEquals( "Unexpected size.", 5, cache.size() );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-4" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-7" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-8" ) );
		assertTrue( "Expected soft reference.", cache.isSoftReference( "item-9" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-10" ) );
		assertEquals( "Unexpected number of soft references.", 3, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		/*
		 * Verify that the policy properly handles clear and garbage collection.
		 */
		cache.clearSoft(); // Simulates garbage collection under memory demand.
		assertEquals( "Unexpected size.", 2, cache.size() );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-7" ) );
		assertFalse( "Expected hard reference.", cache.isSoftReference( "item-10" ) );
		assertEquals( "Unexpected number of soft references.", 0, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 2, cache.getHardReferences() );

		/*
		 * Verify that the cache can be cleared.
		 */
		cache.clear();
		assertEquals( "Unexpected size.", 0, cache.size() );
		assertEquals( "Unexpected number of soft references.", 0, cache.getSoftReferences() );
		assertEquals( "Unexpected number of hard references.", 0, cache.getHardReferences() );
	}

	/**
	 * Tests the cache's entry set, as returned by {@link Cache#entrySet()}.
	 */
	@Test
	public void testEntrySet()
	{
		System.out.println( CLASS_NAME + ".testEntrySet" );

		final Cache<String, Object> cache = (Cache<String, Object>)_cache;
		final Set<Map.Entry<String, Object>> entrySet = cache.entrySet();

		final Object value1 = new Object();
		final Object value2 = new Object();
		final Object value3 = new Object();
		final Object value4 = new Object();

		/*
		 * Insert entries and check that the set reflects those changes.
		 */
		assertEquals( "Unexpected entry set size.", 0, entrySet.size() );
		assertFalse( "Entry is not in the entry set yet.", entrySet.contains( new TestMapEntry<String, Object>( "item-1", value1 ) ) );

		cache.put( "item-1", value1 );
		assertEquals( "Unexpected entry set size.", 1, entrySet.size() );
		assertTrue( "Entry should be in the entry set.", entrySet.contains( new TestMapEntry<String, Object>( "item-1", value1 ) ) );

		cache.put( "item-2", value2 );
		assertEquals( "Unexpected entry set size.", 2, entrySet.size() );
		assertTrue( "Entry should be in the entry set.", entrySet.contains( new TestMapEntry<String, Object>( "item-2", value2 ) ) );

		cache.put( "item-3", value3 );
		assertEquals( "Unexpected entry set size.", 3, entrySet.size() );
		assertTrue( "Entry should be in the entry set.", entrySet.contains( new TestMapEntry<String, Object>( "item-3", value3 ) ) );

		cache.put( "item-4", value4 );
		assertEquals( "Unexpected entry set size.", 4, entrySet.size() );
		assertTrue( "Entry should be in the entry set.", entrySet.contains( new TestMapEntry<String, Object>( "item-4", value4 ) ) );

		for ( final Map.Entry<String, Object> entry : entrySet )
		{
			assertTrue( "Entry should be in the entry set.", entrySet.contains( entry ) );
		}

		/*
		 * Check entry iteration and entry removal using the iterator.
		 */
		final Iterator<Map.Entry<String, Object>> iterator = entrySet.iterator();

		// Remove the first entry.
		assertTrue( "Expected next element.", iterator.hasNext() );
		assertEquals( "Unexpected entry.", new TestMapEntry<String, Object>( "item-1", value1 ), iterator.next() );
		iterator.remove();
		assertEquals( "Unexpected entry set size.", 3, entrySet.size() );
		assertEquals( "Unexpected cache size.", entrySet.size(), cache.size() );
		assertEquals( "Inconsistent reference counts.", entrySet.size(), cache.getSoftReferences() + cache.getHardReferences() );
		assertFalse( "Entry should no longer be in the entry set.", entrySet.contains( new TestMapEntry<String, Object>( "item-1", value1 ) ) );
		try
		{
			iterator.remove();
			fail( "Expected 'IllegalStateException' due to duplicate removal." );
		}
		catch ( final IllegalStateException e )
		{
			/* Success! */
		}
		assertEquals( "Unexpected entry set size after duplicate removal.", 3, entrySet.size() );
		assertEquals( "Unexpected cache size after duplicate removal.", entrySet.size(), cache.size() );
		assertEquals( "Inconsistent reference counts after duplicate removal.", entrySet.size(), cache.getSoftReferences() + cache.getHardReferences() );

		// Fail to remove the second entry due to a call to 'hasNext'.
		assertTrue( "Expected next element.", iterator.hasNext() );
		assertEquals( "Unexpected entry.", new TestMapEntry<String, Object>( "item-2", value2 ), iterator.next() );
		assertTrue( "Expected next element.", iterator.hasNext() );
		try
		{
			iterator.remove();
			fail( "Expected 'IllegalStateException' due to call to 'hasNext'." );
		}
		catch ( final Exception e )
		{
			/* Success! */
		}
		assertEquals( "Unexpected entry set size after failed removal.", 3, entrySet.size() );
		assertEquals( "Unexpected cache size after failed removal.", entrySet.size(), cache.size() );
		assertEquals( "Inconsistent reference counts after failed removal.", entrySet.size(), cache.getSoftReferences() + cache.getHardReferences() );

		// Remove the third entry.
		assertTrue( "Expected next element.", iterator.hasNext() );
		assertEquals( "Unexpected entry.", new TestMapEntry<String, Object>( "item-3", value3 ), iterator.next() );
		iterator.remove();
		assertEquals( "Unexpected entry set size.", 2, entrySet.size() );
		assertEquals( "Unexpected cache size.", entrySet.size(), cache.size() );
		assertEquals( "Inconsistent reference counts.", entrySet.size(), cache.getSoftReferences() + cache.getHardReferences() );
		assertFalse( "Entry should no longer be in the entry set.", entrySet.contains( new TestMapEntry<String, Object>( "item-3", value3 ) ) );
		try
		{
			iterator.remove();
			fail( "Expected 'IllegalStateException' due to duplicate removal." );
		}
		catch ( final IllegalStateException e )
		{
			/* Success! */
		}
		assertEquals( "Unexpected entry set size after duplicate removal.", 2, entrySet.size() );
		assertEquals( "Unexpected cache size after duplicate removal.", entrySet.size(), cache.size() );
		assertEquals( "Inconsistent reference counts after duplicate removal.", entrySet.size(), cache.getSoftReferences() + cache.getHardReferences() );

		// Remove the fourth entry.
		assertTrue( "Expected next element.", iterator.hasNext() );
		assertEquals( "Unexpected entry.", new TestMapEntry<String, Object>( "item-4", value4 ), iterator.next() );
		iterator.remove();
		assertEquals( "Unexpected entry set size.", 1, entrySet.size() );
		assertEquals( "Unexpected cache size.", entrySet.size(), cache.size() );
		assertEquals( "Inconsistent reference counts.", entrySet.size(), cache.getSoftReferences() + cache.getHardReferences() );
		assertFalse( "Entry should no longer be in the entry set.", entrySet.contains( new TestMapEntry<String, Object>( "item-4", value4 ) ) );
		try
		{
			iterator.remove();
			fail( "Expected 'IllegalStateException' due to duplicate removal." );
		}
		catch ( final IllegalStateException e )
		{
			/* Success! */
		}
		assertEquals( "Unexpected entry set size after duplicate removal.", 1, entrySet.size() );
		assertEquals( "Unexpected cache size after duplicate removal.", entrySet.size(), cache.size() );
		assertEquals( "Inconsistent reference counts after duplicate removal.", entrySet.size(), cache.getSoftReferences() + cache.getHardReferences() );
	}

	/**
	 * Tests a cache with a one-to-one index.
	 */
	@Test
	public void testOneToOneIndex()
	{
		System.out.println( CLASS_NAME + ".testOneToOneIndex" );

		class TestObject
		{
			private final Integer _id;

			private final String _unique;

			TestObject( final Integer id, final String unique )
			{
				_id = id;
				_unique = unique;
			}
		}

		final TestObject value1 = new TestObject( 1, "hello" );
		final TestObject value2 = new TestObject( 2, "caching" );
		final TestObject value3 = new TestObject( 3, "and" );
		final TestObject value4 = new TestObject( 4, "indexing" );
		final TestObject value5 = new TestObject( 5, "world" );

		final Cache<Integer, TestObject> cache = (Cache<Integer, TestObject>)_cache;

		/*
		 * Define an index for the '_unique' attribute.
		 */
		final Cache.Attribute<TestObject, String> unique = new Cache.Attribute<TestObject, String>()
		{
			@Override
			public String index( final TestObject value )
			{
				return value._unique;
			}
		};
		final Cache.OneToOneIndex<Integer, TestObject, String> index = new Cache.OneToOneIndex<Integer, TestObject, String>( unique );
		cache.addIndex( index );

		cache.put( value1._id, value1 );
		cache.put( value2._id, value2 );
		cache.put( value3._id, value3 );
		cache.put( value4._id, value4 );
		cache.put( value5._id, value5 );

		assertSame( "Cache should contain object.", value1, cache.get( 1 ) );
		assertSame( "Cache should contain object.", value2, cache.get( 2 ) );
		assertSame( "Cache should contain object.", value3, cache.get( 3 ) );
		assertSame( "Cache should contain object.", value4, cache.get( 4 ) );
		assertSame( "Cache should contain object.", value5, cache.get( 5 ) );

		assertSame( "Index failed to retrieve object.", value1, index.get( cache, "hello" ) );
		assertSame( "Index failed to retrieve object.", value2, index.get( cache, "caching" ) );
		assertSame( "Index failed to retrieve object.", value3, index.get( cache, "and" ) );
		assertSame( "Index failed to retrieve object.", value4, index.get( cache, "indexing" ) );
		assertSame( "Index failed to retrieve object.", value5, index.get( cache, "world" ) );

		index.remove( cache, "caching" );
		index.remove( cache, "and" );
		index.remove( cache, "indexing" );

		assertSame( "Index failed to retrieve object.", value1, index.get( cache, "hello" ) );
		assertSame( "Index failed to retrieve object.", null, index.get( cache, "caching" ) );
		assertSame( "Index failed to retrieve object.", null, index.get( cache, "and" ) );
		assertSame( "Index failed to retrieve object.", null, index.get( cache, "indexing" ) );
		assertSame( "Index failed to retrieve object.", value5, index.get( cache, "world" ) );

		assertSame( "Cache should contain object.", value1, cache.get( 1 ) );
		assertSame( "Cache shouldn't contain object.", null, cache.get( 2 ) );
		assertSame( "Cache shouldn't contain object.", null, cache.get( 3 ) );
		assertSame( "Cache shouldn't contain object.", null, cache.get( 4 ) );
		assertSame( "Cache should contain object.", value5, cache.get( 5 ) );
	}

	/**
	 * Tests a cache with a one-to-many index.
	 */
	@Test
	public void testOneToManyIndex()
	{
		System.out.println( CLASS_NAME + ".testOneToManyIndex" );

		class TestObject
		{
			private final Integer _id;

			private final String _unique;

			TestObject( final Integer id, final String unique )
			{
				_id = id;
				_unique = unique;
			}
		}

		final TestObject value1 = new TestObject( 1, "hello" );
		final TestObject value2 = new TestObject( 2, "caching" );
		final TestObject value3 = new TestObject( 3, "and" );
		final TestObject value4 = new TestObject( 4, "indexing" );
		final TestObject value5 = new TestObject( 5, "world" );

		final Cache<Integer, TestObject> cache = (Cache<Integer, TestObject>)_cache;

		/*
		 * Define an index for the '_unique' attribute.
		 */
		final Cache.Attribute<TestObject, String> unique = new Cache.Attribute<TestObject, String>()
		{
			@Override
			public String index( final TestObject value )
			{
				return value._unique;
			}
		};
		final Cache.OneToOneIndex<Integer, TestObject, String> index = new Cache.OneToOneIndex<Integer, TestObject, String>( unique );
		cache.addIndex( index );

		cache.put( value1._id, value1 );
		cache.put( value2._id, value2 );
		cache.put( value3._id, value3 );
		cache.put( value4._id, value4 );
		cache.put( value5._id, value5 );

		assertSame( "Cache should contain object.", value1, cache.get( 1 ) );
		assertSame( "Cache should contain object.", value2, cache.get( 2 ) );
		assertSame( "Cache should contain object.", value3, cache.get( 3 ) );
		assertSame( "Cache should contain object.", value4, cache.get( 4 ) );
		assertSame( "Cache should contain object.", value5, cache.get( 5 ) );

		assertSame( "Index failed to retrieve object.", value1, index.get( cache, "hello" ) );
		assertSame( "Index failed to retrieve object.", value2, index.get( cache, "caching" ) );
		assertSame( "Index failed to retrieve object.", value3, index.get( cache, "and" ) );
		assertSame( "Index failed to retrieve object.", value4, index.get( cache, "indexing" ) );
		assertSame( "Index failed to retrieve object.", value5, index.get( cache, "world" ) );

		index.remove( cache, "caching" );
		index.remove( cache, "and" );
		index.remove( cache, "indexing" );

		assertSame( "Index failed to retrieve object.", value1, index.get( cache, "hello" ) );
		assertSame( "Index failed to retrieve object.", null, index.get( cache, "caching" ) );
		assertSame( "Index failed to retrieve object.", null, index.get( cache, "and" ) );
		assertSame( "Index failed to retrieve object.", null, index.get( cache, "indexing" ) );
		assertSame( "Index failed to retrieve object.", value5, index.get( cache, "world" ) );

		assertSame( "Cache should contain object.", value1, cache.get( 1 ) );
		assertSame( "Cache shouldn't contain object.", null, cache.get( 2 ) );
		assertSame( "Cache shouldn't contain object.", null, cache.get( 3 ) );
		assertSame( "Cache shouldn't contain object.", null, cache.get( 4 ) );
		assertSame( "Cache should contain object.", value5, cache.get( 5 ) );
	}

	/**
	 * Tests the performance of the default caching policy.
	 */
	@Test
	public void testPolicyPerformance()
	{
		System.out.println( CLASS_NAME + ".testPolicyPerformance()" );

		final Cache<Integer, Double> cache = (Cache<Integer, Double>)_cache;
		final Cache.DefaultCachingPolicy policy = new Cache.DefaultCachingPolicy();
		final ReferenceQueue<Object> queue = new ReferenceQueue<Object>();

		final long start = System.nanoTime();

		final int iterations = 100000;
		for ( int i = 0; i < iterations; i++ )
		{
			final FlexibleReference<Object> reference = new FlexibleReference<Object>( Math.random(), queue );
			if ( i % 2 == 0 )
			{
				reference.soften();
			}
			policy.referenceUsed( cache, reference );

			if ( System.nanoTime() - start > TimeUnit.SECONDS.toNanos( 5L ) )
			{
				fail( "Time limit exceeded. Performance is *really* poor." );
			}
		}

		final long end = System.nanoTime();

		final double duration = (double)( ( end - start ) / 1000000L ) / 1000.0;
		System.out.println( " - Caching policy performance is " + iterations + " iterations in " + duration + " s" );

		assertTrue( "Caching policy performance is too low. (" + duration + " s for " + iterations + " iterations)", duration < 3.0 );
	}

	/**
	 * Tests the performance of the cache when inserting values.
	 */
	@Test
	public void testCachePerformance()
	{
		System.out.println( CLASS_NAME + ".testCachePerformance()" );

		final Map<Integer, Double> cache = (Map<Integer, Double>)_cache;

		final long start = System.nanoTime();

		final int iterations = 100000;
		for ( int i = 0; i < iterations; i++ )
		{
			cache.put( i, Math.random() );

			if ( System.nanoTime() - start > TimeUnit.SECONDS.toNanos( 5L ) )
			{
				fail( "Time limit exceeded. Performance is *really* poor." );
			}
		}

		final long end = System.nanoTime();

		final double duration = (double)( ( end - start ) / 1000000L ) / 1000.0;
		System.out.println( " - Put performance is " + iterations + " iterations in " + duration + " s" );

		assertTrue( "Put performance is too low. (" + duration + " s for " + iterations + " iterations)", duration < 2.0 );
	}

	/**
	 * Map entry used for testing.
	 *
	 * @param <K> Key type.
	 * @param <V> Value type.
	 */
	@SuppressWarnings( "NewExceptionWithoutArguments" )
	private static class TestMapEntry<K, V>
	implements Map.Entry<K, V>
	{
		/**
		 * Key of the entry.
		 */
		private final K _key;

		/**
		 * Value of the entry.
		 */
		private final V _value;

		/**
		 * Constructs a new map entry with the given key and value.
		 *
		 * @param key   Key of the entry.
		 * @param value Value of the entry.
		 */
		private TestMapEntry( final K key, final V value )
		{
			_key = key;
			_value = value;
		}

		@Override
		public K getKey()
		{
			return _key;
		}

		@Override
		public V getValue()
		{
			return _value;
		}

		@Override
		public V setValue( final V value )
		{
			throw new UnsupportedOperationException();
		}

		public int hashCode()
		{
			return ( _key == null ? 0 : _key.hashCode() ) ^
			       ( _value == null ? 0 : _value.hashCode() );
		}

		public boolean equals( final Object obj )
		{
			final boolean result;
			if ( obj == this )
			{
				result = true;
			}
			else if ( obj instanceof Map.Entry )
			{
				final Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
				result = ( _key == null ? entry.getKey() == null : _key.equals( entry.getKey() ) ) &&
				         ( _value == null ? entry.getValue() == null : _value.equals( entry.getValue() ) );
			}
			else
			{
				result = false;
			}
			return result;
		}
	}
}
