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

import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Unit test for {@link LinkedHashList}.
 *
 * @author G. Meinders
 */
public class TestLinkedHashList
{
	/**
	 * Name of this class.
	 */
	private static final String CLASS_NAME = TestLinkedHashList.class.getName();

	/**
	 * Tests that objects are properly removed from the list by the {@link
	 * LinkedHashList#poll()} method.
	 */
	@Test
	public void testPoll()
	{
		System.out.println( CLASS_NAME + ".testPoll()" );

		final LinkedHashList<String> list = new LinkedHashList<String>();
		list.add( "a" );
		list.add( "a" );
		assertEquals( "Unexpected element.", "a", list.poll() );
		assertEquals( "Unexpected element.", "a", list.poll() );
		assertNull( "Removed non-existant element.", list.poll() );
		assertFalse( "Removed non-existant element.", list.remove( "a" ) );
	}

	/**
	 * Tests traversal using {@link LinkedHashList#iterator()}.
	 */
	@Test
	public void testIteratorTraversal()
	{
		System.out.println( CLASS_NAME + ".testIteratorTraversal()" );
		final LinkedHashList<String> list = new LinkedHashList<String>();

		final Iterator<String> emptyIterator = list.iterator();
		assertFalse( emptyIterator.hasNext() );

		list.add( "a" );
		final Iterator<String> singleIterator = list.iterator();
		assertTrue( singleIterator.hasNext() );
		assertEquals( "a", singleIterator.next() );
		assertFalse( singleIterator.hasNext() );

		list.add( "b" );
		list.add( "c" );
		final Iterator<String> iterator = list.iterator();
		assertTrue( iterator.hasNext() );
		assertEquals( "a", iterator.next() );
		assertTrue( iterator.hasNext() );
		assertEquals( "b", iterator.next() );
		assertTrue( iterator.hasNext() );
		assertEquals( "c", iterator.next() );
		assertFalse( iterator.hasNext() );

		final Iterator<String> removeIterator = list.iterator();
		assertTrue( removeIterator.hasNext() );
		assertEquals( "a", removeIterator.next() );
		removeIterator.remove();
		assertEquals( Arrays.asList( "b", "c" ), list );
		assertTrue( removeIterator.hasNext() );
		assertEquals( "b", removeIterator.next() );
		assertTrue( removeIterator.hasNext() );
		assertEquals( "c", removeIterator.next() );
		assertFalse( removeIterator.hasNext() );
		removeIterator.remove();
		assertEquals( Arrays.asList( "b" ), list );
	}

	/**
	 * Tests {@link LinkedHashList#descendingIterator()}.
	 */
	@Test
	public void testDescendingIterator()
	{
		System.out.println( CLASS_NAME + ".testDescendingIterator()" );
		final LinkedHashList<String> list = new LinkedHashList<String>();

		final Iterator<String> emptyIterator = list.descendingIterator();
		assertFalse( emptyIterator.hasNext() );

		list.add( "a" );
		final Iterator<String> singleIterator = list.descendingIterator();
		assertTrue( singleIterator.hasNext() );
		assertEquals( "a", singleIterator.next() );
		assertFalse( singleIterator.hasNext() );

		list.add( "b" );
		list.add( "c" );
		final Iterator<String> iterator = list.descendingIterator();
		assertTrue( iterator.hasNext() );
		assertEquals( "c", iterator.next() );
		assertTrue( iterator.hasNext() );
		assertEquals( "b", iterator.next() );
		assertTrue( iterator.hasNext() );
		assertEquals( "a", iterator.next() );
		assertFalse( iterator.hasNext() );
	}

	/**
	 * Tests {@link LinkedHashList#indexOf(Object)}.
	 */
	@Test
	public void testIndexOf()
	{
		System.out.println( CLASS_NAME + ".testIndexOf()" );
		final LinkedHashList<String> list = new LinkedHashList<String>();

		assertEquals( -1, list.indexOf( "a" ) );

		list.add( "a" );
		assertEquals( 0, list.indexOf( "a" ) );
		assertEquals( -1, list.indexOf( "b" ) );

		list.add( "c" );
		list.add( "b" );
		list.add( "a" );
		list.add( "c" );

		assertEquals( 0, list.indexOf( "a" ) );
		assertEquals( 2, list.indexOf( "b" ) );
		assertEquals( 1, list.indexOf( "c" ) );
	}

	/**
	 * Tests {@link LinkedHashList#lastIndexOf(Object)}.
	 */
	@Test
	public void testLastIndexOf()
	{
		System.out.println( CLASS_NAME + ".testLastIndexOf()" );
		final LinkedHashList<String> list = new LinkedHashList<String>();

		assertEquals( -1, list.lastIndexOf( "a" ) );

		list.add( "a" );
		assertEquals( 0, list.lastIndexOf( "a" ) );
		assertEquals( -1, list.lastIndexOf( "b" ) );

		list.add( "b" );
		list.add( "c" );
		list.add( "a" );
		list.add( "c" );

		assertEquals( 3, list.lastIndexOf( "a" ) );
		assertEquals( 1, list.lastIndexOf( "b" ) );
		assertEquals( 4, list.lastIndexOf( "c" ) );
	}

	/**
	 * Tests {@link LinkedHashList#remove(int)}.
	 */
	@Test
	public void testRemoveByIndex()
	{
		System.out.println( CLASS_NAME + ".testRemoveByIndex()" );
		final LinkedHashList<String> list = new LinkedHashList<String>();

		list.add( "a" );
		assertEquals( "a", list.remove( 0 ) );

		list.add( "a" );
		list.add( "b" );
		list.add( "c" );
		list.add( "a" );
		list.add( "c" );
		assertEquals( "c", list.remove( 4 ) );
		assertEquals( "c", list.remove( 2 ) );
		assertEquals( "b", list.remove( 1 ) );
		assertEquals( "a", list.remove( 1 ) );
		assertEquals( "a", list.remove( 0 ) );
		assertTrue( list.isEmpty() );
	}

	/**
	 * Tests {@link LinkedHashList#removeFirstOccurrence(Object)}.
	 */
	@Test
	public void testRemoveFirstOccurrence()
	{
		System.out.println( CLASS_NAME + ".testRemoveFirstOccurrence()" );
		final LinkedHashList<String> list = new LinkedHashList<String>();

		assertFalse( list.removeFirstOccurrence( "a" ) );

		list.add( "a" );
		assertTrue( list.removeFirstOccurrence( "a" ) );
		assertFalse( list.removeFirstOccurrence( "a" ) );
		assertTrue( list.isEmpty() );

		list.add( "a" );
		list.add( "c" );
		list.add( "b" );
		list.add( "a" );
		list.add( "c" );

		assertTrue( list.removeFirstOccurrence( "a" ) );
		assertEquals( Arrays.asList( "c", "b", "a", "c" ), list );
		assertTrue( list.removeFirstOccurrence( "b" ) );
		assertEquals( Arrays.asList( "c", "a", "c" ), list );
		assertTrue( list.removeFirstOccurrence( "a" ) );
		assertEquals( Arrays.asList( "c", "c" ), list );
		assertTrue( list.removeFirstOccurrence( "c" ) );
		assertFalse( list.removeFirstOccurrence( "a" ) );
		assertFalse( list.removeFirstOccurrence( "b" ) );
		assertEquals( Arrays.asList( "c" ), list );
		assertTrue( list.removeFirstOccurrence( "c" ) );
		assertTrue( list.isEmpty() );
	}

	/**
	 * Tests {@link LinkedHashList#removeLastOccurrence(Object)}.
	 */
	@Test
	public void testRemoveLastOccurrence()
	{
		System.out.println( CLASS_NAME + ".testRemoveLastOccurrence()" );
		final LinkedHashList<String> list = new LinkedHashList<String>();

		assertFalse( list.removeLastOccurrence( "a" ) );

		list.add( "a" );
		assertTrue( list.removeLastOccurrence( "a" ) );
		assertFalse( list.removeLastOccurrence( "a" ) );
		assertTrue( list.isEmpty() );

		list.add( "a" );
		list.add( "c" );
		list.add( "b" );
		list.add( "a" );
		list.add( "c" );

		assertTrue( list.removeLastOccurrence( "a" ) );
		assertEquals( Arrays.asList( "a", "c", "b", "c" ), list );
		assertTrue( list.removeLastOccurrence( "b" ) );
		assertEquals( Arrays.asList( "a", "c", "c" ), list );
		assertTrue( list.removeLastOccurrence( "c" ) );
		assertEquals( Arrays.asList( "a", "c" ), list );
		assertTrue( list.removeLastOccurrence( "a" ) );
		assertFalse( list.removeLastOccurrence( "a" ) );
		assertFalse( list.removeLastOccurrence( "b" ) );
		assertEquals( Arrays.asList( "c" ), list );
		assertTrue( list.removeLastOccurrence( "c" ) );
		assertTrue( list.isEmpty() );
	}

	/**
	 * Tests {@link LinkedHashList#addAll(Collection)} and {@link
	 * LinkedHashList#addAll(int, Collection)}
	 */
	@Test
	public void testAddAll()
	{
		System.out.println( CLASS_NAME + ".testAddAll()" );
		final LinkedHashList<String> list = new LinkedHashList<String>();

		list.addAll( Collections.<String>emptyList() );
		assertTrue( list.isEmpty() );

		list.addAll( Arrays.asList( "a", "b", "c" ) );
		assertEquals( Arrays.asList( "a", "b", "c" ), list );

		list.addAll( 2, Arrays.asList( "d", "e" ) );
		assertEquals( Arrays.asList( "a", "b", "d", "e", "c" ), list );

		list.addAll( 0, Arrays.asList( "f" ) );
		assertEquals( Arrays.asList( "f", "a", "b", "d", "e", "c" ), list );

		list.addAll( 6, Arrays.asList( "g", "h" ) );
		assertEquals( Arrays.asList( "f", "a", "b", "d", "e", "c", "g", "h" ), list );
	}
}
