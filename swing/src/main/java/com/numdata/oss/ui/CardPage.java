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

/**
 * This interface defines a page that is managed by the CardPanel component.
 * Implementors must extend Component!
 *
 * @author S. Bouwman
 * @author Peter S. Heijnen
 */
public interface CardPage
{
	/**
	 * Page action: Initialize GUI. Performed just before the first SHOWN
	 * action.
	 *
	 * @see #pageAction
	 */
	int INIT_GUI = 0;

	/**
	 * Page action: Page shown. Performed just before the page is shown.
	 *
	 * @see #pageAction
	 */
	int SHOWN = 1;

	/**
	 * Page action: Page showing. Performed just after the page is shown.
	 *
	 * @see #pageAction
	 */
	int SHOWING = 2;

	/**
	 * Page action: Page hidden. Performed just after the page is hidden.
	 *
	 * @see #pageAction
	 */
	int HIDDEN = 3;

	/**
	 * Description of page. (This text is used as tooltip text)
	 *
	 * @return Description of page.
	 */
	String getDescription();

	/**
	 * Get title of page.
	 *
	 * @return Title of page.
	 */
	String getTitle();

	/**
	 * This method is called when certain actions are performed on the page.
	 * Possible action values are:
	 *
	 * <table>
	 *
	 * <tr><td>{@link #INIT_GUI}</td><td>Just before the first SHOWN
	 * action</td></tr>
	 *
	 * <tr><td>{@link #SHOWN}</td><td>Just before the page is shown</td></tr>
	 *
	 * <tr><td>{@link #SHOWING}</td><td>Just after the page is shown</td></tr>
	 *
	 * <tr><td>{@link #HIDDEN}</td><td>Just after the page is hidden</td></tr>
	 *
	 * </table>
	 *
	 * @param parent CardPanel that controls this page.
	 * @param action Action as defined above.
	 */
	void pageAction( CardPanel parent, int action );
}
