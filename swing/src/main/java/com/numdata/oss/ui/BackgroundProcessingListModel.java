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
package com.numdata.oss.ui;

import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

import com.numdata.oss.*;

/**
 * List model that automatically processes elements in the background. A typical
 * application would be retrieving additional data for list elements, like
 * database records, files, or images.
 *
 * The {@link #process} method must be implemented to perform the background
 * task for each element. It will be called automatically from a background
 * thread one data is added to the model. The {@link #processed} and {@link
 * #done} methods can be overridden to handle processed elements on the <i>Event
 * Dispatch Thread</i>, typically to update the user interface.
 *
 * NOTE: Elements that have been retrieved more recently are processed with
 * higher priority. This is intended to improve the user experience when the
 * model is used for a {@link javax.swing.JList}, in which case visible items
 * are processed first.
 *
 * @param <T> type of elements in this model.
 *
 * @author Peter S. Heijnen
 */
public abstract class BackgroundProcessingListModel<T>
extends ArrayListModel<T>
{
	/**
	 * Queue of elements to fetch.
	 */
	private final transient Queue<QueueElement<T>> _queue = new LinkedHashList<QueueElement<T>>();

	/**
	 * Worker that processes the queue in the background.
	 */
	private transient QueueWorker _worker;

	/**
	 * Serialized data version.
	 */
	private static final long serialVersionUID = 306770265034722112L;

	/**
	 * Construct model.
	 */
	protected BackgroundProcessingListModel()
	{
		_worker = null;
	}

	/**
	 * Executed on background worker thread to process elements.
	 *
	 * @param element Element to process.
	 */
	protected abstract void process( T element );

	/**
	 * Executed on the <i>Event Dispatch Thread</i> for elements that were
	 * previously processed by the {@link #process} method. <p> The argument is
	 * a list, because multiple elements may have been processed before this
	 * method is called. These elements will be coalesced. <p> The default
	 * implementation fires list update events for elements that (still) exist
	 * in the list model.
	 *
	 * @param elements Elements that were processed.
	 * @param progress Queue progress percentage (0-100).
	 *
	 * @see SwingWorker#process(List)
	 */
	protected void processed( final List<T> elements, final int progress )
	{
		int start = -1;
		int end = -3;

		/*
		 * NOTE: 'end = -2' won't work, because then 'end + 1' is -1, which is
		 *       a possible return value of 'indexOf' (i.e. item not found),
		 *       resulting in an event with negative indices.
		 */

		for ( final T element : elements )
		{
			final int index = indexOf( element );
			if ( index >= 0 )
			{
				if ( index == end + 1 )
				{
					end = index;
					if ( start == -1 )
					{
						start = index;
					}
				}
				else
				{
					if ( start <= end )
					{
						fireContentsChanged( start, end );
					}

					start = index;
					end = index;
				}
			}
		}

		if ( start <= end )
		{
			fireContentsChanged( start, end );
		}
	}

	/**
	 * Executed on the <i>Event Dispatch Thread</i> after all elements in the
	 * queue have been processed.
	 *
	 * @param elements  Elements that were processed .
	 * @param cancelled Whether the background process was cancelled or not.
	 *
	 * @see SwingWorker#done
	 */
	protected void done( final List<T> elements, final boolean cancelled )
	{
		_worker = null;

		final boolean empty;
		synchronized ( _queue )
		{
			empty = _queue.isEmpty();
		}

		if ( !empty )
		{
			final QueueWorker worker = new QueueWorker();
			_worker = worker;
			worker.execute();
		}
	}

	/**
	 * Returns the element at the specified position in this list.
	 *
	 * This re-queues elements to give them top priority.
	 *
	 * @inheritdoc
	 */
	@Override
	public T get( final int index )
	{
		synchronized ( _queue )
		{
			final T result = super.get( index );

			if ( result != null )
			{
				final QueueElement<T> queueElement = new QueueElement<T>( result );
				if ( _queue.remove( queueElement ) ) /* only requeue if it's queued */
				{
					addToQueue( queueElement );
				}
			}

			return result;
		}
	}

	/*
	 * Override methods that change the contents of the model to update the queue.
	 */
	@Override
	public boolean add( final T element )
	{
		synchronized ( _queue )
		{
			if ( element != null )
			{
				addToQueue( new QueueElement<T>( element ) );
			}

			return super.add( element );
		}
	}

	@Override
	public void add( final int index, final T element )
	{
		synchronized ( _queue )
		{
			if ( element != null )
			{
				addToQueue( new QueueElement<T>( element ) );
			}

			super.add( index, element );
		}
	}

	@Override
	public boolean addAll( final Collection<? extends T> collection )
	{
		synchronized ( _queue )
		{
			for ( final T element : collection )
			{
				if ( element != null )
				{
					final QueueElement<T> queueElement = new QueueElement<T>( element );
					_queue.remove( queueElement );
					addToQueue( queueElement );
				}
			}
			return super.addAll( collection );
		}
	}

	@Override
	public boolean addAll( final int index, final Collection<? extends T> collection )
	{
		synchronized ( _queue )
		{
			for ( final T element : collection )
			{
				if ( element != null )
				{
					final QueueElement<T> queueElement = new QueueElement<T>( element );
					_queue.remove( queueElement );
					addToQueue( queueElement );
				}
			}
			return super.addAll( index, collection );
		}
	}

	@Override
	public void clear()
	{
		synchronized ( _queue )
		{
			_queue.clear();
			super.clear();
		}
	}

	@Override
	public T remove( final int index )
	{
		synchronized ( _queue )
		{
			final T result = super.remove( index );
			if ( ( result != null ) && !contains( result ) )
			{
				_queue.remove( new QueueElement<T>( result ) );
			}
			return result;
		}
	}

	@Override
	public void removeRange( final int fromIndex, final int toIndex )
	{
		synchronized ( _queue )
		{
			final int modCount = toIndex - fromIndex;
			final Object[] removedElements = new Object[ modCount ];
			for ( int i = 0; i < removedElements.length; i++ )
			{
				removedElements[ i ] = super.get( fromIndex + i );
			}

			super.removeRange( fromIndex, toIndex );

			for ( final Object element : removedElements )
			{
				if ( ( element != null ) && !contains( element ) )
				{
					_queue.remove( new QueueElement<T>( (T)element ) );
				}
			}
		}
	}

	@Override
	public T set( final int index, final T element )
	{
		synchronized ( _queue )
		{
			final T result = super.set( index, element );
			if ( result != element )
			{
				if ( ( result != null ) && !contains( result ) )
				{
					_queue.remove( new QueueElement<T>( result ) );
				}

				if ( element != null )
				{
					final QueueElement<T> queueElement = new QueueElement<T>( element );
					_queue.remove( queueElement );
					addToQueue( queueElement );
				}
			}
			return result;
		}
	}

	@Override
	protected void finalize()
	throws Throwable
	{
		_queue.clear();

		final QueueWorker worker = _worker;
		if ( worker != null )
		{
			_worker = null;
			worker.cancel( true );
		}

		clear();
		super.finalize();
	}

	/**
	 * This method is called internally to add new elements to the queue. It
	 * will automatically start a {@link QueueWorker} to process the queue.
	 *
	 * @param queueElement Element to add to queue.
	 */
	private void addToQueue( final QueueElement<T> queueElement )
	{
		synchronized ( _queue )
		{
			_queue.add( queueElement );
		}

		if ( _worker == null )
		{
			final QueueWorker worker = new QueueWorker();
			_worker = worker;
			worker.execute();
		}
	}

	/**
	 * Worker that processes the queue in this model.
	 */
	public class QueueWorker
	extends SwingWorker<List<T>, T>
	{
		@Override
		protected List<T> doInBackground()
		{
			final ArrayList<T> result = new ArrayList<T>();

			setProgress( 0 );

			QueueElement<T> queueElement;
			while ( !isCancelled() )
			{
				synchronized ( _queue )
				{
					queueElement = _queue.poll();
				}

				if ( queueElement == null )
				{
					break;
				}

				final T element = queueElement.element;
				if ( !result.contains( element ) )
				{
					BackgroundProcessingListModel.this.process( element );

					for ( Iterator<T> i = result.iterator(); i.hasNext(); )
					{
						if ( !contains( i.next() ) )
						{
							i.remove();
						}
					}

					if ( contains( element ) )
					{
						result.add( element );

						final int progress;
						synchronized ( _queue )
						{
							progress = ( 100 * result.size() ) / ( result.size() + _queue.size() );
						}
						setProgress( progress );

						publish( element );
					}
				}
			}

			return result;
		}

		@Override
		protected void process( final List<T> elements )
		{
			processed( elements, getProgress() );
		}

		@Override
		protected void done()
		{
			try
			{
				BackgroundProcessingListModel.this.done( get(), isCancelled() );
			}
			catch ( InterruptedException e )
			{
				e.printStackTrace();
			}
			catch ( ExecutionException e )
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * This class is used to wrap list elements in the queue.
	 *
	 * @param <T> type of element.
	 */
	static class QueueElement<T>
	implements Comparable<QueueElement<T>>
	{
		/**
		 * List element.
		 */
		T element;

		/**
		 * Value of {@link System#nanoTime()} when last accessed.
		 */
		long time;

		/**
		 * Construct new queue element.
		 *
		 * @param element Element to queue.
		 */
		QueueElement( final T element )
		{
			this.element = element;
			time = System.nanoTime();
		}

		public int hashCode()
		{
			return ( element != null ) ? element.hashCode() : 0;
		}

		public boolean equals( final Object other )
		{
			final boolean result;

			final T element = this.element;

			if ( other == this )
			{
				result = true;
			}
			else if ( other instanceof QueueElement )
			{
				final T otherElement = ( (QueueElement<T>)other ).element;
				result = ( element == null ) ? ( otherElement == null ) : element.equals( otherElement );
			}
			else
			{
				result = false;
			}

			return result;
		}

		@Override
		public int compareTo( final QueueElement<T> other )
		{
			return ( time > other.time ) ? -1 : 1;
		}
	}
}
