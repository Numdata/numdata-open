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
package com.numdata.oss.ui.explorer;

/**
 * A view that display each item as a thumbnail labeled with the item's name.
 * The description of the item is shown as a tool tip.
 *
 * @author  G. Meinders
 */
public class ThumbnailView
	extends ListView
{
	/**
	 * Construct new thumbnail view.
	 *
	 * @param   explorer    Explorer that holds the model for this view.
	 * @param   sorted      {@code true} to sort items in the view.
	 */
	public ThumbnailView( final Explorer explorer , final boolean sorted )
	{
		super( explorer, sorted );

		/*
		 * Wrap icons horizontally.
		 *
		 * In horizontal wrapping mode, the list puts each item on a new row
		 * until the visible row count is reached. To prevent this and create
		 * a purely horizontal layout, the visible row count must be set to 0.
		 */
		setLayoutOrientation( HORIZONTAL_WRAP );
		setVisibleRowCount( 0 );

		/*
		 * Render files as thumbnails.
		 */
		setCellRenderer( new ThumbnailItem( explorer ) );
	}
}
