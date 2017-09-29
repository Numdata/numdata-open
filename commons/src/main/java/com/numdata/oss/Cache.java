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

import com.numdata.oss.log.*;
import org.jetbrains.annotations.*;

/**
 * A map that references some or all of its values in such a way that they may
 * be reclaimed during garbage collection if there is a demand for more memory.
 * The cache's keys are always hard-referenced. This is contrary to the {@link
 * WeakHashMap}, which uses special referencing of the map's keys, not the
 * values.
 *
 * Values stored in the cache are referenced using a {@link
 * FlexibleReference}, such that the values can be either hard-referenced or
 * soft-referenced. This in turn affects the garbage collector, which doesn't
 * collect hard-referenced objects, while it does collect soft-referenced
 * objects (though only if more memory is needed.)
 *
 * When a value in the cache is garbage collected, all related entries are
 * removed. Because garbage collection may happen at any time, <strong>no
 * guarantee can be given that sequential calls to {@link #containsKey} and
 * {@link #get} will provide consistent results</strong>. This can be avoided by
 * calling {@code containsKey} <em>after</em> {@code get}, if it returns {@code
 * null}. The cache's iterators do not suffer from this problem.
 *
 * A {@link CachingPolicy} defines which method of referencing is used for a
 * particular value, based on some heuristic. The policy may decide to change
 * references when a value is accessed or in response to a value being removed,
 * such that the less expensive values are soft-referenced while more expensive
 * values are hard-referenced. See {@link DefaultCachingPolicy} for details
 * about the default caching policy.
 *
 * To store keys and values, the cache uses a {@link Map} as its underlying
 * data structure. By default a {@link HashMap} is used, but another map type
 * can be specified at construction. The underlying map must have a no-argument
 * constructor, must be modifiable and must support {@code null} values.
 *
 * The iterators of this map support the {@link Iterator#remove() remove}
 * operation under the additional condition that {@link Iterator#hasNext()
 * hasNext} must not be called between the calls to {@link Iterator#next() next}
 * and {@code remove}.
 *
 * <strong>TODO: Implement fail-fast behavior on the iterators.</strong>
 *
 * @param <K> Key type.
 * @param <V> Value type.
 *
 * @author G. Meinders
 */
public class Cache<K, V>
implements Map<K, V>
{
	/**
	 * Log used for messages related to this class.
	 */
	private static final ClassLogger LOG = ClassLogger.getFor( Cache.class );

	/**
	 * Keeps track of entries that were reclaimed by the garbage collector.
	 */
	private final ReferenceQueue<V> _queue;

	/**
	 * Underlying map implementation, which stores values indirectly through a
	 * {@link CacheReference}. Any {@code null} values are stored as such,
	 * without creating a {@code CacheReference}, such that {@code null} values
	 * can be distinguished from references that have been cleared.
	 */
	private final Map<K, CacheReference> _map;

	/**
	 * View of the keys in the map. (created as needed)
	 */
	private Set<K> _keySet;

	/**
	 * View of the values in the map. (created as needed)
	 */
	private Collection<V> _values;

	/**
	 * View of the entries in the map. (created as needed)
	 */
	private Set<Entry<K, V>> _entrySet;

	/**
	 * Current number of soft references.
	 */
	private int _softReferences;

	/**
	 * Current number of hard references.
	 */
	private int _hardReferences;

	/**
	 * Caching policy to determine whether values should be referenced using
	 * hard or soft references.
	 */
	private final CachingPolicy _cachingPolicy;

	/**
	 * Registered indices, updated whenever the cache is modified.
	 */
	private final List<Index<K, V, ?>> _indices;

	/**
	 * Constructs a new cache.
	 *
	 * The cache uses the default caching policy and has a {@link HashMap} as
	 * its underlying data structure.
	 */
	public Cache()
	{
		this( new DefaultCachingPolicy(), HashMap.class );
	}

	/**
	 * Constructs a new cache containing all entries in the given map.
	 *
	 * The cache uses the default caching policy and has a {@link HashMap} as
	 * its underlying data structure.
	 *
	 * @param map Mappings to be placed in this map.
	 */
	@SuppressWarnings( "OverridableMethodCallDuringObjectConstruction" )
	public Cache( final Map<? extends K, ? extends V> map )
	{
		this();
		putAll( map );
	}

	/**
	 * Constructs a new cache using the given caching policy.
	 *
	 * @param cachingPolicy Caching policy to be used.
	 */
	public Cache( final CachingPolicy cachingPolicy )
	{
		this( cachingPolicy, HashMap.class );
	}

	/**
	 * Constructs a new cache based on the specified map implementation with the
	 * given caching policy.
	 *
	 * @param cachingPolicy Caching policy to be used.
	 * @param mapClass      Type of map to be used as an underlying data
	 *                      structure for storing cache entries.
	 */
	public Cache( final CachingPolicy cachingPolicy, @SuppressWarnings( "rawtypes" ) final Class<? extends Map> mapClass )
	{
		if ( cachingPolicy == null )
		{
			throw new NullPointerException( "cachingPolicy" );
		}

		if ( mapClass == null )
		{
			throw new NullPointerException( "mapClass" );
		}

		_cachingPolicy = cachingPolicy;

		_softReferences = 0;
		_hardReferences = 0;

		try
		{
			_map = (Map<K, CacheReference>)mapClass.getConstructor().newInstance();
		}
		catch ( final Exception e )
		{
			throw new IllegalArgumentException( "mapClass", e );
		}

		_queue = new ReferenceQueue<V>();

		_keySet = null;
		_values = null;
		_entrySet = null;

		_indices = new ArrayList<Index<K, V, ?>>();
	}

	@Override
	public int size()
	{
		int result = 0;
		if ( !_map.isEmpty() )
		{
			removeStaleEntries();
			result = _map.size();
		}
		return result;
	}

	@Override
	public boolean isEmpty()
	{
		return size() == 0;
	}

	@Override
	public boolean containsKey( final Object key )
	{
		removeStaleEntries();
		return _map.containsKey( key );
	}

	@Override
	public boolean containsValue( final Object value )
	{
		final Collection<V> values = values();
		return values.contains( value );
	}

	@Nullable
	@Override
	public V get( final Object key )
	{
		removeStaleEntries();
		final CacheReference reference = _map.get( key );
		referenceUsed( reference );
		return dereference( reference );
	}

	@Nullable
	@Override
	public V put( final K key, final V value )
	{
		removeStaleEntries();
		return putImpl( key, value );
	}

	/**
	 * Adds an entry for the given key-value pair to the cache and returns the
	 * value that was replaced, if any.
	 *
	 * NOTE: This method is only suitable for internal use, because it
	 * doesn't remove stale entries.
	 *
	 * @param key   Key to be put.
	 * @param value Value to be put.
	 *
	 * @return Replaced value, if any.
	 */
	@Nullable
	private V putImpl( final K key, final V value )
	{
		final CacheReference reference = createReference( key, value );
		final CacheReference replaced = _map.put( key, reference );
		final V result = disposeReference( replaced );
		addToIndices( key, value );
		return result;
	}

	@Nullable
	@Override
	public V remove( final Object key )
	{
		removeStaleEntries();
		final CacheReference removed = _map.remove( key );
		final V result = disposeReference( removed );
		return result;
	}

	@Override
	public void putAll( @NotNull final Map<? extends K, ? extends V> map )
	{
		removeStaleEntries();
		for ( final Entry<? extends K, ? extends V> entry : map.entrySet() )
		{
			putImpl( entry.getKey(), entry.getValue() );
		}
	}

	@Override
	public void clear()
	{
		final Collection<CacheReference> references = _map.values();
		for ( final Iterator<CacheReference> it = references.iterator(); it.hasNext(); )
		{
			final CacheReference reference = it.next();
			it.remove();
			disposeReference( reference );
		}
	}

	/**
	 * Removes all objects that are currently soft referenced from the cache.
	 * The caching policy may choose to change some of the remaining references
	 * to soft references. As a result, the cache may contain both soft and hard
	 * references after calling this method.
	 */
	void clearSoft()
	{
		final Collection<CacheReference> values = _map.values();
		for ( final CacheReference reference : values )
		{
			if ( reference.isSoft() )
			{
				reference.enqueue();
				reference.clear();
			}
		}

		removeStaleEntries();
	}

	/**
	 * Same as {@link #clearSoft()}, except that one soft reference is left
	 * intact, as the garbage collector would do when an iterator is in use.
	 */
	void clearSoft2()
	{
		boolean first = true;
		final Collection<CacheReference> values = _map.values();
		for ( final CacheReference reference : values )
		{
			if ( reference.isSoft() )
			{
				if ( first )
				{
					first = false;
				}
				else
				{
					reference.enqueue();
					reference.clear();
				}
			}
		}

		removeStaleEntries();
	}

	/**
	 * Represents the contents of the cache as a list of key-value pairs,
	 * enclosed in curly brackets. Soft references are marked by an asterisk
	 * after the key. For example: {first=value, second*=otherValue, ...}
	 *
	 * @return String representation of the cache.
	 */
	public String toString()
	{
		final String result;

		final Set<K> keySet = keySet();
		final Iterator<K> iterator = keySet.iterator();

		if ( !iterator.hasNext() )
		{
			result = "{}";
		}
		else
		{
			final StringBuilder builder = new StringBuilder();
			builder.append( '{' );
			while ( true )
			{
				// Use the underlying map directly, instead of using an entry
				// set, to prevent interference with the caching policy.
				final K key = iterator.next();
				final CacheReference reference = _map.get( key );
				final V value = dereference( reference );

				builder.append( key );
				if ( ( reference != null ) && ( reference.isSoft() ) )
				{
					builder.append( '*' );
				}
				builder.append( '=' );
				builder.append( value );

				if ( iterator.hasNext() )
				{
					builder.append( ", " );
				}
				else
				{
					break;
				}
			}
			builder.append( '}' );

			result = builder.toString();
		}

		return result;
	}

	/**
	 * Determines whether values should be soft-referenced or hard-referenced.
	 */
	public interface CachingPolicy
	{
		/**
		 * Returns whether the next entry to be added to the given cache should
		 * use a soft reference.
		 *
		 * @param cache Cache to be checked.
		 *
		 * @return {@code true} if the next entry should be soft-referenced;
		 * {@code false} otherwise.
		 */
		boolean createSoftReference( final Cache<?, ?> cache );

		/**
		 * Notifies the policy of a reference being used.
		 *
		 * This method is not called for {@code null} values.
		 *
		 * @param cache     Cache that was used.
		 * @param reference Reference that was used.
		 */
		void referenceUsed( final Cache<?, ?> cache, final FlexibleReference<?> reference );

		/**
		 * Notifies the policy of a reference that is no longer contained in the
		 * cache.
		 *
		 * @param cache     Cache that the reference was removed from.
		 * @param reference Reference that was disposed.
		 */
		void referenceDisposed( final Cache<?, ?> cache, final FlexibleReference<?> reference );

		/**
		 * Notifies the policy that the softness of a reference has changed.
		 *
		 * @param cache     Cache containing the reference.
		 * @param reference Reference that has changed.
		 * @param softened  {@code true} if the reference was softened; {@code
		 *                  false} if the reference was hardened.
		 */
		void referenceSoftnessChanged( final Cache<?, ?> cache, final FlexibleReference<?> reference, final boolean softened );
	}

	/**
	 * Default caching policy, which implements a least-recently-used (LRU)
	 * caching algorithm.
	 *
	 * Each object in the cache may be soft-referenced or hard-referenced.
	 * The number of each type of reference is determined from the softness
	 * ratio, i.e. the number of soft references relative to the total number of
	 * (non-null) references in the cache. The softness ratio may be biases for
	 * small and large cache sizes by specifying minimum and maximum amounts of
	 * soft and hard references.
	 *
	 * Using the default settings of the policy, the number of soft and hard
	 * references is always determined by the softness. The same applies if the
	 * minimum amounts of each type of reference have been reached and the
	 * maximum amounts have not been exceeded.
	 *
	 * <pre>
	 * &lt;-------- soft references -------->||&lt;---- hard references ---->
	 *                                    ||
	 * +----|---------------|-------------||------------------|-------+
	 * |    |               |             ||                  |       |
	 * +----|---------------|-------------||------------------|-------+
	 *      |               |             ||                  |
	 * minimum_soft    maximum_hard    softness          minimum_hard
	 * </pre>
	 *
	 * By specifying minimum and maximum values, the behavior of the cache
	 * changes. As the cache gets larger, the number of hard references will
	 * reach the maximum number of hard references, if specified. From that
	 * point on, soft references are used even if the softness is exceeded, as
	 * shown below.
	 *
	 * <pre>
	 * &lt;---------- soft references --------->||&lt;-- hard references -->
	 *                                       ||
	 * +-------|----------------|------------||---------------|-------+
	 * |       |                |            ||               |       |
	 * +-------|----------------|------------||---------------|-------+
	 *         |                |            ||               |
	 *    minimum_soft       softness    maximum_hard    minimum_hard
	 * </pre>
	 *
	 * For smaller caches, the number of references of each type is
	 * determined entirely by the specified minimum amounts. In this case, hard
	 * references take precedence over soft references until the minimum number
	 * of hard references is reached.
	 *
	 * <pre>
	 * &lt;-- soft references -->||&lt;---------- hard references ---------->
	 *                        ||
	 * +----------------------||---------------|----------------------+
	 * |                      ||               |                      |
	 * +----------------------||---------------|----------------------+
	 *                        ||               |
	 *                   minimum_hard     minimum_soft
	 * </pre>
	 */
	public static class DefaultCachingPolicy
	implements CachingPolicy
	{
		/**
		 * Relative amount of soft references in the cache.
		 */
		private final double _softness;

		/**
		 * Minimum number of soft references in the cache. For small caches,
		 * this value only takes effect when the number of references exceeds
		 * {@link #_minimumHardReferences}.
		 */
		private final int _minimumSoftReferences;

		/**
		 * Maximum number of hard references in the cache. For small caches,
		 * this value precedes the {@link #_minimumSoftReferences} parameter.
		 */
		private final int _minimumHardReferences;

		/**
		 * Maximum number of hard references in the cache.
		 */
		private final int _maximumHardReferences;

		/**
		 * Keeps track of order in which soft-referenced entries were last
		 * used.
		 */
		private final List<Usage> _softUsages;

		/**
		 * Keeps track of order in which hard-referenced entries were last
		 * used.
		 */
		private final List<Usage> _hardUsages;

		/**
		 * Constructs a default caching policy. The policy has a softness of
		 * {@code 0.25} and places no constraints on the number of soft and hard
		 * references.
		 */
		public DefaultCachingPolicy()
		{
			this( 0.25 );
		}

		/**
		 * Constructs a default caching policy with the given softness ratio.
		 * The policy places no constraints on the number of soft and hard
		 * references.
		 *
		 * @param softness Relative amount of soft references in the cache,
		 *                 {@code 0.0 &lt;= softness &lt;= 1.0}
		 *
		 * @throws IllegalArgumentException if {@code softness} falls outside of
		 * the allowed range, 0.0 to 1.0 (inclusive).
		 */
		public DefaultCachingPolicy( final double softness )
		{
			this( softness, 0, 0, Integer.MAX_VALUE );
		}

		/**
		 * Constructs a default caching policy with the given parameters.
		 *
		 * @param softness Relative amount of soft references in the cache,
		 *                 {@code 0.0 &lt;= softness &lt;= 1.0}
		 * @param minSoft  Minimum number of soft references made before
		 *                 applying the {@code softness}.
		 * @param minHard  Minimum number of hard references made before making
		 *                 any soft references.
		 * @param maxHard  Maximum number of hard references that the cache may
		 *                 contain at any time.
		 *
		 * @throws IllegalArgumentException if {@code softness} falls outside of
		 * the allowed range, 0.0 to 1.0 (inclusive).
		 */
		public DefaultCachingPolicy( final double softness, final int minSoft, final int minHard, final int maxHard )
		{
			if ( ( softness < 0.0 ) || ( softness > 1.0 ) )
			{
				throw new IllegalArgumentException( "softness: " + softness );
			}

			if ( minSoft < 0 )
			{
				throw new IllegalArgumentException( "minSoft" );
			}
			if ( minHard < 0 )
			{
				throw new IllegalArgumentException( "minHard" );
			}
			if ( minHard > maxHard )
			{
				throw new IllegalArgumentException( "minHard > maxHard" );
			}

			_softness = softness;

			_minimumSoftReferences = minSoft;
			_minimumHardReferences = minHard;
			_maximumHardReferences = maxHard;

			_softUsages = new LinkedHashList<Usage>();
			_hardUsages = new LinkedHashList<Usage>();
		}

		@Override
		public boolean createSoftReference( final Cache<?, ?> cache )
		{
			final int softReferences = cache.getSoftReferences();
			final int hardReferences = cache.getHardReferences();

			return ( hardReferences >= _minimumHardReferences ) &&
			       ( ( softReferences < _minimumSoftReferences ) ||
			         ( hardReferences >= _maximumHardReferences ) ||
			         ( (double)softReferences / (double)( softReferences + hardReferences + 1 ) < _softness ) );
		}

		@Override
		public void referenceUsed( final Cache<?, ?> cache, final FlexibleReference<?> reference )
		{
			if ( reference != null )
			{
				final Usage usage = new Usage( reference );

				if ( reference.isSoft() )
				{
					_softUsages.remove( usage );
					_softUsages.add( usage );

					final FlexibleReference<?> counterbalance = getCounterbalance( reference, false );
					if ( ( counterbalance != null ) && reference.harden() )
					{
						counterbalance.soften();
					}
				}
				else
				{
					_hardUsages.remove( usage );
					_hardUsages.add( usage );
				}
			}
		}

		@Override
		public void referenceDisposed( final Cache<?, ?> cache, final FlexibleReference<?> reference )
		{
			final Usage usage = new Usage( reference );
			final boolean removed = _softUsages.remove( usage ) || _hardUsages.remove( usage );

			if ( removed )
			{
				FlexibleReference<?> counterbalance = null;

				if ( !reference.isHard() )
				{
					if ( ( cache.getHardReferences() > 0 ) && needSoftReference( cache ) )
					{
						counterbalance = getCounterbalance( reference, false );
						if ( counterbalance != null )
						{
							counterbalance.soften();
						}
					}
				}

				if ( ( counterbalance == null ) && !reference.isSoft() )
				{
					if ( ( cache.getSoftReferences() > 0 ) && needHardReference( cache ) )
					{
						counterbalance = getCounterbalance( reference, true );
						if ( counterbalance != null )
						{
							counterbalance.harden();
						}
					}
				}
			}
		}

		/**
		 * Returns whether a soft reference should be switched to a hard
		 * reference for the given cache.
		 *
		 * @param cache Cache to be checked.
		 *
		 * @return {@code true} if a soft reference should be switched; {@code
		 * false} otherwise.
		 */
		private boolean needHardReference( final Cache<?, ?> cache )
		{
			final int softReferences = cache.getSoftReferences();
			final int hardReferences = cache.getHardReferences();

			return ( hardReferences < _minimumHardReferences ) ||
			       ( ( softReferences > _minimumSoftReferences ) &&
			         ( hardReferences < _maximumHardReferences ) &&
			         ( (double)( softReferences - 1 ) / (double)( softReferences + hardReferences ) >= _softness ) );
		}

		/**
		 * Returns whether a hard reference should be switched to a soft
		 * reference for the given cache.
		 *
		 * @param cache Cache to be checked.
		 *
		 * @return {@code true} if a soft reference should be switched; {@code
		 * false} otherwise.
		 */
		private boolean needSoftReference( final Cache<?, ?> cache )
		{
			final int softReferences = cache.getSoftReferences();
			final int hardReferences = cache.getHardReferences();

			return ( hardReferences >= _minimumHardReferences ) &&
			       ( ( softReferences < _minimumSoftReferences ) ||
			         ( hardReferences >= _maximumHardReferences ) ||
			         ( (double)softReferences / (double)( softReferences + hardReferences ) < _softness ) );
		}

		/**
		 * Finds a reference to act as a counterbalance when the given reference
		 * is switched to a different type. The returned reference may be
		 * switched in the opposite way, keeping the amounts of soft and hard
		 * references equal.
		 *
		 * @param reference Reference to find a counterbalance for.
		 * @param soft      {@code true} to find a soft reference; {@code false}
		 *                  to find a hard reference.
		 *
		 * @return Reference to be used as a counterbalance, if any.
		 */
		@Nullable
		@SuppressWarnings( "ObjectEquality" )
		private FlexibleReference<?> getCounterbalance( final FlexibleReference<?> reference, final boolean soft )
		{
			FlexibleReference<?> result = null;

			if ( soft )
			{
				final List<Usage> usages = _softUsages;
				for ( final ListIterator<Usage> it = usages.listIterator( usages.size() ); it.hasPrevious(); )
				{
					final Usage usage = it.previous();

					final FlexibleReference<?> candidate = usage._reference;
					if ( candidate != reference )
					{
						result = usage._reference;
						break;
					}
				}
			}
			else
			{
				final List<Usage> usages = _hardUsages;
				for ( final Usage usage : usages )
				{
					final FlexibleReference<?> candidate = usage._reference;
					if ( candidate != reference )
					{
						result = usage._reference;
						break;
					}
				}
			}

			return result;
		}

		@Override
		public void referenceSoftnessChanged( final Cache<?, ?> cache, final FlexibleReference<?> reference, final boolean softened )
		{
			final Usage usage = new Usage( reference );
			if ( softened )
			{
				_hardUsages.remove( usage );
				_softUsages.add( usage );
			}
			else
			{
				_softUsages.remove( usage );
				_hardUsages.add( usage );
			}
		}

		/**
		 * Represents a usage of a references in the cache.
		 */
		private static class Usage
		{
			/**
			 * Reference that was used.
			 */
			private final FlexibleReference<?> _reference;

			/**
			 * Constructs a new usage for the given reference.
			 *
			 * @param reference Reference that was used.
			 */
			private Usage( final FlexibleReference<?> reference )
			{
				_reference = reference;
			}

			public boolean equals( final Object obj )
			{
				final boolean result;
				if ( obj == this )
				{
					result = true;
				}
				else if ( obj instanceof Usage )
				{
					final Usage other = (Usage)obj;
					result = ( _reference == other._reference ) || _reference.equals( other._reference );
				}
				else
				{
					result = false;
				}
				return result;
			}

			public int hashCode()
			{
				return _reference.hashCode();
			}
		}
	}

	/**
	 * Returns the number of soft references currently in the cache.
	 *
	 * @return Number of soft references in the cache.
	 */
	protected int getSoftReferences()
	{
		return _softReferences;
	}

	/**
	 * Returns the number of hard references currently in the cache.
	 *
	 * @return Number of hard references in the cache.
	 */
	protected int getHardReferences()
	{
		return _hardReferences;
	}

	/**
	 * Creates a reference to the value of the given key-value pair.
	 *
	 * @param key   Key associated with the value.
	 * @param value Value to be referenced.
	 *
	 * @return Reference to the value; {@code null} if the value is {@code
	 * null}.
	 */
	@Nullable
	private CacheReference createReference( final K key, final V value )
	{
		CacheReference result = null;
		if ( value != null )
		{
			final boolean soft = _cachingPolicy.createSoftReference( this );

			result = new CacheReference( key, value, _queue );
			if ( soft )
			{
				result.soften();
			}
			referenceUsed( result );
		}
		return result;
	}

	/**
	 * Notifies the caching policy that the given reference was used, and
	 * changes the reference between soft-referencing and hard-referencing if
	 * necessary. If the reference is changed, another reference is changed as
	 * well, to balance the number of soft and hard references.
	 *
	 * @param reference Reference that was used.
	 */
	private void referenceUsed( final CacheReference reference )
	{
		if ( reference != null )
		{
			_cachingPolicy.referenceUsed( this, reference );
		}
	}

	/**
	 * Notifies that map that the given reference will no longer be used.
	 *
	 * @param reference Reference to be disposed.
	 *
	 * @return Value associated with the reference.
	 */
	@Nullable
	private V disposeReference( final CacheReference reference )
	{
		final V result = dereference( reference );
		if ( reference != null )
		{
			if ( reference.isHard() )
			{
				_hardReferences--;
			}
			else // A cleared reference must have been soft.
			{
				_softReferences--;
			}
			_cachingPolicy.referenceDisposed( this, reference );
			removeFromIndices( reference.getKey() );
		}
		return result;
	}

	/**
	 * Returns the value pointed to by the given reference.
	 *
	 * @param reference Reference to be dereferenced.
	 *
	 * @return Value of the reference; {@code null} if the reference is cleared
	 * or if no reference was given.
	 */
	@Nullable
	private V dereference( final CacheReference reference )
	{
		return ( reference == null ) ? null : reference.get();
	}

	/**
	 * Removes all entries of which the value is cleared from the cache.
	 */
	private void removeStaleEntries()
	{
		final boolean trace = LOG.isTraceEnabled();
		final List<K> removedKeys = trace ? new ArrayList<K>() : null;

		CacheReference reference;
		while ( ( reference = (CacheReference)_queue.poll() ) != null )
		{
			final K key = reference.getKey();
			disposeReference( reference );
			_map.remove( key );

			if ( trace )
			{
				removedKeys.add( key );
			}
		}

		if ( trace && !removedKeys.isEmpty() )
		{
			LOG.trace( "Removed stale entries for keys " + removedKeys );
		}
	}

	/**
	 * Returns whether the value associated with the given key is
	 * soft-referenced. Intended for testing purposes only.
	 *
	 * @param key Key to be checked.
	 *
	 * @return {@code true} if the key's value is soft-referenced; {@code false}
	 * otherwise.
	 *
	 * @throws NoSuchElementException if the cache doesn't contain the key.
	 */
	boolean isSoftReference( final Object key )
	{
		if ( !_map.containsKey( key ) )
		{
			throw new NoSuchElementException( "for key: " + key );
		}

		final CacheReference reference = _map.get( key );
		return ( reference != null ) && reference.isSoft();
	}

	/**
	 * Reference to a value in the cache with its associated key.
	 */
	private class CacheReference
	extends FlexibleReference<V>
	{
		/**
		 * Key associated with the reference.
		 */
		private final K _key;

		/**
		 * Constructs a new reference to the value of the given key-value pair.
		 *
		 * @param key   Key associated with the value.
		 * @param value Value to be referenced.
		 * @param queue Reference queue to register with; {@code null} if not
		 *              needed.
		 */
		private CacheReference( final K key, final V value, final ReferenceQueue<? super V> queue )
		{
			super( value, queue );
			_key = key;

			_hardReferences++;
		}

		/**
		 * Returns the key associated with the reference.
		 *
		 * @return Associated key object.
		 */
		public K getKey()
		{
			return _key;
		}

		@Override
		public boolean soften()
		{
			final boolean result = super.soften();
			if ( result )
			{
				_softReferences++;
				_hardReferences--;
				_cachingPolicy.referenceSoftnessChanged( Cache.this, this, true );
			}
			return result;
		}

		@Override
		public boolean harden()
		{
			final boolean result = super.harden();
			if ( result )
			{
				_softReferences--;
				_hardReferences++;
				_cachingPolicy.referenceSoftnessChanged( Cache.this, this, false );
			}
			return result;
		}

		public String toString()
		{
			final K key = _key;
			final String value = isSoft() ? key + "*" :
			                     !isHard() ? key + "-" : String.valueOf( key );
			return "reference[" + value + ']';
		}
	}

	@Override
	@NotNull
	public Set<K> keySet()
	{
		removeStaleEntries();
		final Set<K> keySet = _keySet;
		return ( keySet != null ) ? keySet : ( _keySet = new KeySet() );
	}

	@Override
	@NotNull
	public Collection<V> values()
	{
		removeStaleEntries();
		final Collection<V> values = _values;
		return ( values != null ) ? values : ( _values = new Values() );
	}

	@Override
	@NotNull
	public Set<Entry<K, V>> entrySet()
	{
		removeStaleEntries();
		final Set<Entry<K, V>> entrySet = _entrySet;
		return ( entrySet != null ) ? entrySet : ( _entrySet = new EntrySet() );
	}

	/**
	 * Implements a view of the keys in the cache.
	 */
	private class KeySet
	extends AbstractSet<K>
	{
		@NotNull
		@Override
		public Iterator<K> iterator()
		{
			return new KeyIterator();
		}

		@Override
		public int size()
		{
			return _map.size();
		}
	}

	/**
	 * Implements a view of the values in the cache.
	 */
	private class Values
	extends AbstractCollection<V>
	{
		@NotNull
		@Override
		public Iterator<V> iterator()
		{
			return new ValueIterator();
		}

		@Override
		public int size()
		{
			return _map.size();
		}
	}

	/**
	 * Implements a view of the entries in the cache.
	 */
	private class EntrySet
	extends AbstractSet<Entry<K, V>>
	{
		@NotNull
		@Override
		public Iterator<Entry<K, V>> iterator()
		{
			return new EntryIterator();
		}

		@Override
		public int size()
		{
			return _map.size();
		}
	}

	/**
	 * Implements an iterator over the view of the keys in the cache.
	 */
	private class KeyIterator
	extends CacheIterator<K>
	{
		@Override
		public K next()
		{
			final Entry<K, V> nextEntry = nextEntry();
			return nextEntry.getKey();
		}
	}

	/**
	 * Implements an iterator over the view of the values in the cache.
	 */
	private class ValueIterator
	extends CacheIterator<V>
	{
		@Override
		public V next()
		{
			final Entry<K, V> nextEntry = nextEntry();
			return nextEntry.getValue();
		}
	}

	/**
	 * Implements an iterator over the view of the entries in the cache.
	 */
	private class EntryIterator
	extends CacheIterator<Entry<K, V>>
	{
		@Override
		public Entry<K, V> next()
		{
			return nextEntry();
		}
	}

	/**
	 * Base-class for iterators over the views of the cache, providing iteration
	 * over the entries of the cache (excluding stale ones) to its sub-classes.
	 */
	private abstract class CacheIterator<E>
	implements Iterator<E>
	{
		/**
		 * Iterator over the entries in the underlying data structure.
		 */
		private final Iterator<Entry<K, CacheReference>> _iterator;

		/**
		 * Entry that to be returned by the next call to {@link #nextEntry()}.
		 */
		private CacheEntry _next;

		/**
		 * Whether next has been set since the last call to {@link
		 * #nextEntry()}.
		 */
		private boolean _hasNext;

		/**
		 * Constructs a new iterator.
		 */
		protected CacheIterator()
		{
			final Set<Entry<K, CacheReference>> entries = _map.entrySet();
			final Iterator<Entry<K, CacheReference>> iterator = entries.iterator();

			_iterator = iterator;
			_next = null;
			_hasNext = false;
		}

		@Override
		public boolean hasNext()
		{
			if ( !_hasNext )
			{
				nextEntryImpl();
			}
			return ( _next != null );
		}

		/**
		 * Returns the next entry in the iteration.
		 *
		 * @return Next entry.
		 *
		 * @throws NoSuchElementException if there are no more entries.
		 */
		public Entry<K, V> nextEntry()
		{
			if ( !_hasNext )
			{
				nextEntryImpl();
			}

			final Entry<K, V> next = _next;
			if ( next == null )
			{
				//noinspection NewExceptionWithoutArguments
				throw new NoSuchElementException();
			}

			_hasNext = false;
			return next;
		}

		/**
		 * Moves the iterator to the next entry, skipping any stale entries.
		 */
		private void nextEntryImpl()
		{
			final Iterator<Entry<K, CacheReference>> iterator = _iterator;

			CacheEntry next = null;
			while ( iterator.hasNext() )
			{
				final Entry<K, CacheReference> entry = iterator.next();

				final CacheReference reference = entry.getValue();
				if ( reference != null )
				{
					final V value = reference.get();
					if ( value == null )
					{
						iterator.remove();
					}
					else
					{
						next = new CacheEntry( entry );
						break;
					}
				}
				else
				{
					next = new CacheEntry( entry );
					break;
				}
			}
			_next = next;
			_hasNext = true;
		}

		@Override
		public void remove()
		{
			if ( _hasNext )
			{
				throw new IllegalStateException( "hasNext was called before remove" );
			}

			if ( _next == null )
			{
				throw new IllegalStateException( "next not called or element already removed" );
			}

			_iterator.remove();
			disposeReference( _next.getReference() );
		}
	}

	/**
	 * An entry from the cache, implemented as a view of an entry in the
	 * underlying data structure.
	 */
	private class CacheEntry
	implements Entry<K, V>
	{
		/**
		 * Entry in the underlying data structure.
		 */
		private final Entry<K, CacheReference> _entry;

		/**
		 * Value of the entry, hard-referenced to prevent garbage collection.
		 */
		private V _value;

		/**
		 * Constructs a new entry, implemented as a view of the given entry.
		 *
		 * @param entry Entry to be viewed.
		 */
		CacheEntry( final Entry<K, CacheReference> entry )
		{
			_entry = entry;
			_value = dereference( entry.getValue() );
		}

		@Override
		public K getKey()
		{
			return _entry.getKey();
		}

		@Override
		public V getValue()
		{
			referenceUsed( _entry.getValue() );
			return _value;
		}

		@Nullable
		@Override
		public V setValue( final V value )
		{
			_value = value;
			final K key = _entry.getKey();
			final CacheReference reference = createReference( key, value );
			final CacheReference replaced = _entry.setValue( reference );
			final V result = disposeReference( replaced );
			addToIndices( key, value );
			return result;
		}

		/**
		 * Returns the reference to the value of this entry.
		 *
		 * @return Reference to the value of the entry.
		 */
		public CacheReference getReference()
		{
			return _entry.getValue();
		}

		public int hashCode()
		{
			final K key = _entry.getKey();
			final V value = _value;

			return ( key == null ? 0 : key.hashCode() ) ^
			       ( value == null ? 0 : value.hashCode() );
		}

		public boolean equals( final Object obj )
		{
			final boolean result;
			if ( obj == this )
			{
				result = true;
			}
			else if ( obj instanceof Entry )
			{
				final K key = _entry.getKey();
				final V value = _value;

				final Entry<?, ?> entry = (Entry<?, ?>)obj;
				result = ( key == null ? entry.getKey() == null : key.equals( entry.getKey() ) ) &&
				         ( value == null ? entry.getValue() == null : value.equals( entry.getValue() ) );
			}
			else
			{
				result = false;
			}
			return result;
		}
	}

	/**
	 * Adds an index to the cache.
	 *
	 * @param index Index to be added.
	 */
	public void addIndex( final Index<K, V, ?> index )
	{
		_indices.add( index );
		for ( final Entry<K, V> entry : entrySet() )
		{
			index.addToIndex( entry.getKey(), entry.getValue() );
		}
	}

	/**
	 * Removes an index from the cache.
	 *
	 * @param index Index to be removed.
	 */
	public void removeIndex( final Index<K, V, ?> index )
	{
		_indices.remove( index );
	}

	/**
	 * Adds the specified map entry to all indices.
	 *
	 * @param key   Key of the entry.
	 * @param value Value of the entry.
	 */
	private void addToIndices( final K key, final V value )
	{
		for ( final Index<K, V, ?> index : _indices )
		{
			index.addToIndex( key, value );
		}
	}

	/**
	 * Removes the given key from all indices.
	 *
	 * @param key Key to be removed.
	 */
	private void removeFromIndices( final K key )
	{
		for ( final Index<K, V, ?> index : _indices )
		{
			index.removeFromIndex( key );
		}
	}

	/**
	 * An attribute that can be used to index values.
	 *
	 * @param <V> Value type.
	 * @param <I> Indexed attribute type.
	 */
	public interface Attribute<V, I>
	{
		/**
		 * Returns the indexed attribute for the given cached value.
		 *
		 * @param value Value to be indexed.
		 *
		 * @return Indexed attribute value.
		 */
		I index( @Nullable final V value );
	}

	/**
	 * Allows for access to a map using an indexed attribute, instead of the
	 * keys from the map.
	 *
	 * @param <K> Key type of the map.
	 * @param <V> Value type of the map.
	 * @param <I> Indexed attribute type.
	 */
	public abstract static class Index<K, V, I>
	{
		/**
		 * Attribute on which the index is based.
		 */
		@NotNull
		protected final Attribute<V, I> _attribute;

		/**
		 * Constructs a new index using the given attribute.
		 *
		 * @param attribute Attribute to be indexed.
		 */
		protected Index( @NotNull final Attribute<V, I> attribute )
		{
			_attribute = attribute;
		}

		/**
		 * Clear entire index.
		 */
		public abstract void clear();

		/**
		 * Adds the specified map entry to the index.
		 *
		 * @param key   Key of the entry.
		 * @param value Value of the entry.
		 */
		public abstract void addToIndex( @Nullable final K key, @Nullable final V value );

		/**
		 * Removes the given key from the index.
		 *
		 * @param key Key of the entry.
		 */
		public abstract void removeFromIndex( @Nullable final K key );
	}

	/**
	 * Implements a one-to-one index of a map.
	 *
	 * @param <K> Key type of the map.
	 * @param <V> Value type of the map.
	 * @param <I> Indexed attribute type.
	 */
	public static class OneToOneIndex<K, V, I>
	extends Index<K, V, I>
	{
		/**
		 * Maps each indexed attribute value to a key in the indexed map.
		 */
		@NotNull
		private final Map<I, K> _attributeToKey;

		/**
		 * Constructs a new one-to-one index of the given attribute.
		 *
		 * @param attribute Attribute to be indexed.
		 */
		public OneToOneIndex( @NotNull final Attribute<V, I> attribute )
		{
			super( attribute );
			_attributeToKey = new HashMap<I, K>();
		}

		@Override
		public void clear()
		{
			_attributeToKey.clear();
		}

		@Override
		public void addToIndex( @Nullable final K key, @Nullable final V value )
		{
			_attributeToKey.put( _attribute.index( value ), key );
		}

		@Override
		public void removeFromIndex( @Nullable final K key )
		{
			final Collection<K> indexedKeys = _attributeToKey.values();
			for ( final Iterator<K> it = indexedKeys.iterator(); it.hasNext(); )
			{
				final K indexedKey = it.next();
				if ( ( key == null ) ? ( indexedKey == null ) : key.equals( indexedKey ) )
				{
					it.remove();
				}
			}
		}

		/**
		 * Returns the value associated with the given attribute value from the
		 * given map.
		 *
		 * @param map       Map to retrieve values from.
		 * @param attribute Attribute value to be looked up.
		 *
		 * @return List of matching values.
		 */
		@Nullable
		public V get( @NotNull final Map<K, V> map, @Nullable final I attribute )
		{
			return map.get( getKey( attribute ) );
		}

		/**
		 * Returns the key associated with the given attribute value.
		 *
		 * @param attribute Attribute value to get key for.
		 *
		 * @return Key for the given attribute.
		 */
		@Nullable
		public K getKey( @Nullable final I attribute )
		{
			return _attributeToKey.get( attribute );
		}

		/**
		 * Removes the entry associated from the given attribute value from the
		 * given map.
		 *
		 * @param map       Map to retrieve values from.
		 * @param attribute Attribute value to be looked up.
		 *
		 * @return Removed value, if any.
		 */
		@Nullable
		public V remove( @SuppressWarnings( "TypeMayBeWeakened" ) @NotNull final Cache<K, V> map, @Nullable final I attribute )
		{
			return map.remove( _attributeToKey.remove( attribute ) );
		}
	}

	/**
	 * Implements a one-to-many index of a map.
	 *
	 * @param <K> Key type of the map.
	 * @param <V> Value type of the map.
	 * @param <I> Indexed attribute type.
	 */
	public static class OneToManyIndex<K, V, I>
	extends Index<K, V, I>
	{
		/**
		 * Set of keys associated with each value in the index.
		 */
		@NotNull
		private final Map<I, Set<K>> _indexToKey;

		/**
		 * During the {@link #remove(Cache, Object)} method, the key that is
		 * currently being removed from the cache. Otherwise {@code null}.
		 */
		@Nullable
		private K _removeKey = null;

		/**
		 * Constructs a new one-to-many index of the given attribute.
		 *
		 * @param attribute Attribute to be indexed.
		 */
		public OneToManyIndex( @NotNull final Attribute<V, I> attribute )
		{
			super( attribute );
			_indexToKey = new HashMap<I, Set<K>>();
		}

		@Override
		public void clear()
		{
			_indexToKey.clear();
		}

		@Override
		public void addToIndex( @Nullable final K key, @Nullable final V value )
		{
			final I index = _attribute.index( value );

			Set<K> values = _indexToKey.get( index );
			if ( values == null )
			{
				values = new LinkedHashSet<K>();
				_indexToKey.put( index, values );
			}

			values.add( key );
		}

		@Override
		public void removeFromIndex( @Nullable final K key )
		{
			//noinspection ObjectEquality
			if ( _removeKey != key )
			{
				for ( final Set<K> indexedKeys : _indexToKey.values() )
				{
					for ( final Iterator<K> it = indexedKeys.iterator(); it.hasNext(); )
					{
						final K indexedKey = it.next();
						if ( ( key == null ) ? ( indexedKey == null ) : key.equals( indexedKey ) )
						{
							it.remove();
						}
					}
				}
			}
		}

		/**
		 * Returns all values from the cache that match the given indexed
		 * value.
		 *
		 * @param cache Cache to retrieve values from.
		 * @param index Index value being looked up.
		 *
		 * @return List of matching values.
		 */
		@NotNull
		public List<V> get( @NotNull final Map<K, V> cache, @Nullable final I index )
		{
			final List<V> result;

			final Set<K> keys = _indexToKey.get( index );
			if ( keys == null )
			{
				result = Collections.emptyList();
			}
			else
			{
				final Iterable<K> keysCopy = new ArrayList<K>( keys );

				result = new ArrayList<V>( keys.size() );
				for ( final K key : keysCopy )
				{
					final V value = cache.get( key );
					if ( ( value != null ) || cache.containsKey( key ) )
					{
						result.add( value );
					}
				}
			}

			return result;
		}

		/**
		 * Removes the mappings associated with the given indexed value from the
		 * cache.
		 *
		 * @param cache Cache to remove mappings from.
		 * @param index Index to remove all associated mappings for.
		 *
		 * @return List of removed values.
		 */
		@SuppressWarnings( "FieldRepeatedlyAccessedInMethod" )
		@NotNull
		public List<V> remove( @SuppressWarnings( "TypeMayBeWeakened" ) @NotNull final Cache<K, V> cache, @Nullable final I index )
		{
			final List<V> result;

			final Set<K> keys = _indexToKey.remove( index );
			if ( keys == null )
			{
				result = Collections.emptyList();
			}
			else
			{
				result = new ArrayList<V>( keys.size() );
				for ( final K key : keys )
				{
					_removeKey = key;
					result.add( cache.remove( key ) );
				}
				_removeKey = null;
			}

			return result;
		}
	}
}
