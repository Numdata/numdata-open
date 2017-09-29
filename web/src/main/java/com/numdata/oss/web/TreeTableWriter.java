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
package com.numdata.oss.web;

import java.io.*;
import java.util.*;
import javax.servlet.jsp.*;
import javax.swing.tree.*;

import com.numdata.oss.*;
import org.jetbrains.annotations.*;

/**
 * Writer for trees based on JFC {@link TreeNode} or tree tables based on {@link
 * TreeTableNode} objects.
 *
 * @author Peter S. Heijnen
 */
public class TreeTableWriter
{
	/**
	 * Locale to use for internationalization.
	 */
	protected final Locale _locale;

	/**
	 * HTML table factory.
	 */
	protected final HTMLTableFactory _tableFactory;

	/**
	 * Construct writer.
	 *
	 * @param locale       Locale to use for internationalization.
	 * @param tableFactory HTML table factory.
	 */
	public TreeTableWriter( final Locale locale, final HTMLTableFactory tableFactory )
	{
		_locale = locale;
		_tableFactory = tableFactory;
	}

	/**
	 * Print tree as nested tree table.
	 *
	 * @param out      Output to write list to.
	 * @param idPrefix Prefix to use for node ID's (e.g. 'node').
	 * @param root     Root of tree to print.
	 * @param selected Selected node in tree.
	 *
	 * @throws IOException if there was a problem writing the output.
	 * @throws NullPointerException if {@code out} or {@code root} is {@code
	 * null}.
	 */
	public void writeTreeTable( @NotNull final JspWriter out, @NotNull final String idPrefix, @NotNull final TreeTableNode<?> root, @Nullable final TreeNode selected )
	throws IOException
	{
		final Collection<String> attributes = getAttributes( root );

		final List<String> columnNames = new ArrayList<String>( attributes.size() + 1 );
		columnNames.add( "&nbsp;" );
		for ( final String attribute : attributes )
		{
			columnNames.add( getAttributeName( attribute ) );
		}

		final HTMLTable table = createTable( out, columnNames );
		newTable( out, table );

		for ( final Iterator<TreeTableNode<?>> it = new TreeNodeIterator<TreeTableNode<?>>( root ); it.hasNext(); )
		{
			final TreeTableNode<?> node = it.next();

			final String id = getNodeID( idPrefix, node );
			final int tier = 1 + getNodeDepth( node );
			final boolean isBranch = ( node.getChildCount() > 0 );
			final boolean isCollapsed = ( isBranch && isTreeNodeCollapsed( node, selected ) );
			//noinspection ObjectEquality
			final boolean isSelected = ( node == selected );
			final boolean isVisible = isNodeVisible( node, selected );

			newRow( out, table, isVisible ? null : PropertyTools.create( "class", "hidden" ) );
			table.newColumn( out );

			out.print( "<div id=\"" );
			out.print( id );
			out.print( "\" class=\"treenode tier" );
			out.print( tier );
			out.print( isBranch ? " branch" : " leaf" );
			if ( isCollapsed )
			{
				out.print( " collapsed" );
			}
			if ( isBranch )
			{
				out.print( "\" onclick=\"toggleTreeBranch(this)" );
			}
			out.print( "\"></div>" );

			out.print( "<div class=\"treevalue tier" );
			out.print( tier );
			out.print( isBranch ? " branch" : " leaf" );
			if ( isCollapsed )
			{
				out.print( " collapsed" );
			}
			if ( isSelected )
			{
				out.print( " selected" );
			}
			out.print( "\">" );
			writeNodeValue( out, node );
			out.print( "</div>" );

			for ( final String attribute : attributes )
			{
				writeAttributeColumn( out, table, node, attribute );
			}
		}
		table.endTable( out );
	}

	/**
	 * Start new table.
	 *
	 * @param out   Output to write to.
	 * @param table HTML table being written.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	protected void newTable( final JspWriter out, final HTMLTable table )
	throws IOException
	{
		table.newTable( out );
	}

	/**
	 * Create new table row.
	 *
	 * @param out           Output to write to.
	 * @param table         HTML table being written.
	 * @param rowAttributes Attributes of TR element.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	protected void newRow( final JspWriter out, final HTMLTable table, @Nullable final Properties rowAttributes )
	throws IOException
	{
		table.newRow( out, rowAttributes );
	}

	/**
	 * Get attributes from tree.
	 *
	 * @param root Root of tree.
	 *
	 * @return Attributes in tree.
	 */
	public Collection<String> getAttributes( final TreeTableNode<?> root )
	{
		final Collection<String> result = new HashSet<String>();

		for ( final Iterator<TreeTableNode<?>> it = new TreeNodeIterator<TreeTableNode<?>>( root ); it.hasNext(); )
		{
			final TreeTableNode<?> node = it.next();
			result.addAll( node.getAttributeNames() );
		}

		return result;
	}

	/**
	 * Print tree as nested list.
	 *
	 * @param out      Output to write list to.
	 * @param root     Root of tree to print.
	 * @param selected Selected node in tree.
	 *
	 * @throws IOException if there was a problem writing the output.
	 * @throws NullPointerException if {@code out} or {@code root} is {@code
	 * null}.
	 */
	public void writeTree( @NotNull final JspWriter out, final TreeNode root, final TreeNode selected )
	throws IOException
	{
		int indentLevel = 0;

		for ( final Iterator<TreeNode> it = new TreeNodeIterator<TreeNode>( root ); it.hasNext(); )
		{
			final TreeNode node = it.next();

			int newIndentLevel = 0;
			for ( TreeNode cur = node; cur != null; cur = cur.getParent() )
			{
				newIndentLevel++;
			}

			if ( newIndentLevel > indentLevel )
			{
				while ( indentLevel < newIndentLevel )
				{
					out.println( "<ul>" );
					out.println( "<li>" );
					indentLevel++;
				}
			}
			else if ( newIndentLevel == indentLevel )
			{
				out.println( "</li>" );
				out.println( "<li>" );
			}
			else
			{
				while ( indentLevel > newIndentLevel )
				{
					out.println( "</li>" );
					out.println( "</ul>" );
					indentLevel--;
				}
				out.println( "<li>" );
			}

			//noinspection ObjectEquality
			if ( node == selected )
			{
				out.print( "<b>" );
			}

			out.print( node );

			//noinspection ObjectEquality
			if ( node == selected )
			{
				out.print( "</b>" );
			}
		}

		while ( indentLevel > 0 )
		{
			out.println( "</li>" );
			out.println( "</ul>" );
			indentLevel--;
		}
	}

	/**
	 * Create HTML tree table that will be written.
	 *
	 * @param out         Output writer.
	 * @param columnNames Table column names.
	 *
	 * @return {@link HTMLTable}.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	@NotNull
	protected HTMLTable createTable( @NotNull final JspWriter out, @NotNull final List<String> columnNames )
	throws IOException
	{
		return _tableFactory.createTable( columnNames );
	}

	/**
	 * Write node value to HTML document.
	 *
	 * @param out  Output writer.
	 * @param node Node to get value for.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	protected void writeNodeValue( @NotNull final JspWriter out, @NotNull final TreeNode node )
	throws IOException
	{
		final Object value = ( node instanceof TreeTableNode<?> ) ? ( (TreeTableNode<?>)node ).getValue() : node;
		out.write( ( value != null ) ? value.toString() : "&nbsp;" );
	}

	/**
	 * Write node attribute value to HTML document.
	 *
	 * @param out       Output writer.
	 * @param table     HTMLT table being written.
	 * @param node      Node to get attribute from.
	 * @param attribute Attribute to get value for.
	 *
	 * @throws IOException if an I/O error occurs.
	 */
	protected void writeAttributeColumn( @NotNull final JspWriter out, final HTMLTable table, @NotNull final TreeTableNode<?> node, @NotNull final String attribute )
	throws IOException
	{
		table.newColumn( out );

		final Object value = node.getAttributeValue( attribute );
		out.write( ( value != null ) ? value.toString() : "&nbsp;" );
	}

	/**
	 * Get unique ID to identity nodes. The node ID is the specified prefix
	 * followed by a hyphen (-) and child index (starting at 1) for each tree
	 * nesting level starting from the root node. The root node will simply
	 * return the prefix string as ID.
	 *
	 * @param prefix Prefix for ID (e.g. 'node').
	 * @param node   Node whose ID to determine.
	 *
	 * @return Node ID string.
	 */
	protected String getNodeID( final String prefix, final TreeNode node )
	{
		final StringBuilder sb = new StringBuilder();

		for ( TreeNode cursor = node; cursor.getParent() != null; cursor = cursor.getParent() )
		{
			final TreeNode parent = cursor.getParent();
			final int nodeIndex = 1 + parent.getIndex( cursor );
			sb.insert( 0, nodeIndex );
			sb.insert( 0, '-' );
		}

		sb.insert( 0, prefix );

		return sb.toString();
	}

	/**
	 * Get node depth (i.e. distance from root node).
	 *
	 * @param node Node whose depth to determine.
	 *
	 * @return Node depth (0 if node is root).
	 */
	protected int getNodeDepth( final TreeNode node )
	{
		int result = -1;

		for ( TreeNode cursor = node; cursor != null; cursor = cursor.getParent() )
		{
			result++;
		}

		return result;
	}

	/**
	 * Test if a node is (initially!) visible. This is true if the specified
	 * node is or is the child of the selected node or any its ancestors
	 *
	 * @param node     Node to test.
	 * @param selected Selected node in tree.
	 *
	 * @return {@code true} if the node is visible; {@code false} otherwise.
	 */
	public boolean isNodeVisible( final TreeNode node, @Nullable final TreeNode selected )
	{
		boolean result = true;

		for ( TreeNode cursor = selected; cursor != null; cursor = cursor.getParent() )
		{
			result = ( node == cursor ) || ( cursor.getIndex( node ) >= 0 );
			if ( result )
			{
				break;
			}
		}

		return result;
	}

	/**
	 * Test if a node is (initially!) collapsed. This is true if the specified
	 * node is not the sected node nor any of it's ancestors.
	 *
	 * @param node     Node to test.
	 * @param selected Selected node in tree.
	 *
	 * @return {@code true} if the node is collapsed; {@code false} otherwise.
	 */
	protected boolean isTreeNodeCollapsed( final TreeNode node, final TreeNode selected )
	{
		boolean result = false;

		for ( TreeNode cursor = selected; cursor != null; cursor = cursor.getParent() )
		{
			result = ( node != cursor );
			if ( !result )
			{
				break;
			}
		}

		return result;
	}

	/**
	 * Get name of node attribute.
	 *
	 * @param attribute Attribute to get name of.
	 *
	 * @return Attribute name.
	 */
	protected String getAttributeName( @NotNull final String attribute )
	{
		return attribute;
	}
}
