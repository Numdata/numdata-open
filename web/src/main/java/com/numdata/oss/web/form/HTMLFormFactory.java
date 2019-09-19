/*
 * Copyright (c) 2008-2017, Numdata BV, The Netherlands.
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
package com.numdata.oss.web.form;

import java.io.*;
import java.util.*;
import javax.servlet.jsp.*;

import com.numdata.oss.web.*;
import org.jetbrains.annotations.*;

/**
 * This interface describes a factory for HTML forms.
 *
 * @author G.B.M. Rupert
 */
public interface HTMLFormFactory
{
	/**
	 * Get factory to create HTML tables for forms.
	 *
	 * @return Table factory.
	 */
	@NotNull
	HTMLTableFactory getTableFactory();

	/**
	 * Generates HTML for current form.
	 *
	 * @param contextPath    Web application context path.
	 * @param out            Writer to use for output.
	 * @param formAttributes Attributes of {@code &lt;form&gt;} element.
	 * @param title          Form title.
	 * @param table          HTML table.
	 * @param buttons        Buttons related to form (typically for
	 *                       submission).
	 *
	 * @throws IOException when writing failed.
	 */
	void writeFormPre( @NotNull String contextPath, @NotNull JspWriter out, @NotNull Map<String, String> formAttributes, @Nullable String title, @Nullable HTMLTable table, @NotNull List<FormButton> buttons )
	throws IOException;

	/**
	 * Generates HTML for current form.
	 *
	 * @param contextPath Web application context path.
	 * @param out         Writer to use for output.
	 * @param table       HTML table.
	 * @param buttons     Buttons related to form (typically for submission).
	 *
	 * @throws IOException when writing failed.
	 */
	void writeFormPost( @NotNull String contextPath, @NotNull JspWriter out, @Nullable HTMLTable table, @NotNull List<FormButton> buttons )
	throws IOException;

	/**
	 * Write field label to form.
	 *
	 * @param contextPath Web application context path.
	 * @param table       HTML table.
	 * @param out         Writer to use for output.
	 * @param formLabel   Label to write.
	 *
	 * @throws IOException when writing failed.
	 */
	void writeLabel( @NotNull final String contextPath, @Nullable final HTMLTable table, @NotNull final JspWriter out, @NotNull final FormLabel formLabel )
	throws IOException;

	/**
	 * Write 'back' button with the specified caption text.
	 *
	 * @param contextPath Path to context containing page.
	 * @param out         Writer used for output to JSP page.
	 * @param text        Caption text.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeBackButton( @NotNull String contextPath, @NotNull JspWriter out, @NotNull String text )
	throws IOException;

	/**
	 * Write 'Browse...' button.
	 *
	 * @param out  JspWriter to use for output.
	 * @param name Name of button field.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeBrowseButton( @NotNull JspWriter out, @NotNull String name )
	throws IOException;

	/**
	 * This method generates HTML code for a button.
	 *
	 * @param out        Writer used for output to JSP page.
	 * @param link       HTML reference for button action.
	 * @param text       Button text.
	 * @param attributes Attributes to set for HTML element.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeButton( @NotNull JspWriter out, @NotNull String link, @NotNull String text, @Nullable Map<String, String> attributes )
	throws IOException;

	/**
	 * Generates a checkbox with specified name and value.
	 *
	 * @param contextPath Path to context containing page.
	 * @param out         Writer to use for output.
	 * @param editable    Allow user to edit the value.
	 * @param tag         Name of element.
	 * @param value       The currently selected value.
	 * @param attributes  Attributes to set for INPUT element.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeCheckbox( @NotNull String contextPath, @NotNull JspWriter out, boolean editable, @NotNull String tag, boolean value, @Nullable Map<String, String> attributes )
	throws IOException;

	/**
	 * Generates a radio button with specified name, value, and state.
	 *
	 * @param contextPath Path to context containing page.
	 * @param out         Writer to use for output.
	 * @param editable    Allow user to edit the value.
	 * @param tag         Name of element.
	 * @param value       Radio button value.
	 * @param checked     Whether the radio is checked or not.
	 * @param attributes  Attributes to set for INPUT element.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeRadioButton( @NotNull String contextPath, @NotNull JspWriter out, boolean editable, @NotNull String tag, @NotNull final String value, boolean checked, @Nullable Map<String, String> attributes )
	throws IOException;

	/**
	 * Generates a choice box with specified name, value, and choices.
	 *
	 * The list of choices is a string array with two elements for each choice.
	 * The first element if the actual value; the second element is the value
	 * description presented to the user.
	 *
	 * @param out        Writer to use for output.
	 * @param editable   Allow user to edit the value.
	 * @param name       Name of element.
	 * @param selected   The currently selected value.
	 * @param values     List of available values.
	 * @param labels     List of value labels.
	 * @param attributes Attributes to set for SELECT element.
	 *
	 * @return JspWriter to use for future output from the page (possibly
	 * wrapped).
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	IndentingJspWriter writeChoice( @NotNull JspWriter out, boolean editable, @NotNull String name, @Nullable String selected, @NotNull List<String> values, @NotNull List<String> labels, @Nullable Map<String, String> attributes )
	throws IOException;

	/**
	 * Write hidden form field.
	 *
	 * @param out   Writer to use for output.
	 * @param name  Name of field.
	 * @param value Value to assign to field.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeHiddenField( @NotNull JspWriter out, @NotNull String name, @NotNull String value )
	throws IOException;

	/**
	 * Write 'submit' button with the specified caption text.
	 *
	 * Style names MUST NOT contain spaces. They SHOULD be valid CSS2
	 * identifiers, matching the regular expression (case insensitive): {@code
	 * [a-z]([a-z][0-9-])*}. (Note that the CSS specification also allows any
	 * Unicode character from {@code \u0080} onwards to be used in an
	 * identifier.)
	 *
	 * @param out        Writer used for output to JSP page.
	 * @param name       Name of input element.
	 * @param text       Caption text.
	 * @param attributes Extra attributes for element.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeSubmitButton( @NotNull JspWriter out, @Nullable String name, @NotNull String text, @Nullable Map<String, String> attributes )
	throws IOException;

	/**
	 * Write 'submit' button with the specified image.
	 *
	 * @param contextPath Path to context containing page.
	 * @param out         Writer used for output to JSP page.
	 * @param name        Name of input element.
	 * @param image       Image to use.
	 * @param attributes  Extra attributes for element.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeSubmitImage( @NotNull String contextPath, @NotNull JspWriter out, @Nullable String name, @NotNull String image, @Nullable Map<String, String> attributes )
	throws IOException;

	/**
	 * Generates a text area with specified name, value, and size.
	 *
	 * @param out        Writer to use for output.
	 * @param editable   Allow user to edit the value.
	 * @param tag        Name of element.
	 * @param value      The currently selected value.
	 * @param cols       Number of columns in text area.
	 * @param rows       Number of rows in text area.
	 * @param attributes Extra attributes for element.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeTextArea( @NotNull JspWriter out, boolean editable, @NotNull String tag, @Nullable String value, int cols, int rows, @Nullable Map<String, String> attributes )
	throws IOException;

	/**
	 * Generates a text field with specified name, value, and size.
	 *
	 * @param out                 Writer to use for output.
	 * @param editable            Allow user to edit the value.
	 * @param tag                 Name of element.
	 * @param id                  ID of element.
	 * @param value               The currently selected value.
	 * @param size                Size of text field (number of character
	 *                            positions).
	 * @param maxLength           Maximum length of text value.
	 * @param password            Field is a password vs. regular text field.
	 * @param disableAutoComplete Disable auto-complete (enabled by default).
	 * @param invalid             Whether the current value is invalid.
	 * @param attributes          Extra attributes for element.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeTextField( @NotNull JspWriter out, boolean editable, @NotNull String tag, @Nullable String id, @Nullable String value, int size, int maxLength, boolean password, boolean disableAutoComplete, boolean invalid, @Nullable Map<String, String> attributes )
	throws IOException;

	/**
	 * Generates a text field with specified name, value, and size.
	 *
	 * @param out       Writer to use for output.
	 * @param editable  Allow user to edit the value.
	 * @param tag       Name of element.
	 * @param value     The currently selected value.
	 * @param size      Size of text field (number of character positions).
	 * @param maxLength Maximum length of text value.
	 *
	 * @throws IOException if an I/O error occurred.
	 */
	void writeTextField( @NotNull JspWriter out, boolean editable, @NotNull String tag, @Nullable String value, int size, int maxLength )
	throws IOException;
}
