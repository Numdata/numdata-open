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
package com.numdata.oss;

import java.util.*;

/**
 * This interface adds some features to the {@link List} interface.
 *
 * @param <E> Element type.
 *
 * @author Peter S. Heijnen
 */
public interface AugmentedList<E>
extends List<E>
{
	/**
	 * Ensure that the internal storage capacity of the list if enough to retain
	 * at least the specified number of elements. This does alter the actual
	 * contents of the list.
	 *
	 * This can be used by the implementing class to efficiently prepare
	 * internal storage for the requested number of elements. Implementations
	 * can safely ignore calls to this method.
	 *
	 * @param capacity Minimum number of element that need to be stored.
	 */
	void ensureCapacity( int capacity );

	/**
	 * Removes all elements in the specified range from this list.
	 *
	 * @param startIndex Start of removed range (inclusive).
	 * @param endIndex   End of removed range (exclusive).
	 *
	 * @see #remove(int)
	 * @see #clear
	 */
	void removeRange( int startIndex, int endIndex );

	/**
	 * Set number of elements in the list to the specified value. If the list
	 * has too few elements, {@code null}-elements are added; if the list has
	 * too many elements, the list is truncated at the end.
	 *
	 * @param length Desired length of list.
	 */
	void setLength( int length );

	/**
	 * Sets all elements of the list, such that it is equal to the given list.
	 *
	 * @param list Elements to be set.
	 */
	void setAll( List<? extends E> list );

	/**
	 * Gets the first element in this list.
	 *
	 * @return First element in this list
	 *
	 * @throws NoSuchElementException if this list is empty.
	 */
	E getFirst();

	/**
	 * Gets the last element in this list.
	 *
	 * @return Last element in this list.
	 *
	 * @throws NoSuchElementException if this list is empty.
	 */
	E getLast();

	/**
	 * Removes and returns the first element from this list.
	 *
	 * @return First element from this list.
	 *
	 * @throws NoSuchElementException if this list is empty.
	 */
	E removeFirst();

	/**
	 * Removes and returns the last element from this list.
	 *
	 * @return Last element from this list.
	 *
	 * @throws NoSuchElementException if this list is empty.
	 */
	E removeLast();
}
